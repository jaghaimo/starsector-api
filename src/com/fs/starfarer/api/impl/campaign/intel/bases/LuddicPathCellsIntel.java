package com.fs.starfarer.api.impl.campaign.intel.bases;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.econ.RecentUnrest;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.EconomyRouteData;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.HA_CMD;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class LuddicPathCellsIntel extends BaseIntelPlugin implements RouteFleetSpawner, FleetEventListener {

	public static String USED_PLANETBUSTER_KEY = "$core_lpUsedPlanetbuster";
	
	public static float INCIDENT_PROB = Global.getSettings().getFloat("luddicPathCellsIncidentProbabilityPerMonth");
	public static float MIN_WARNING_DAYS = Global.getSettings().getFloat("luddicPathCellsIncidentWarningMinDays");
	
//	public static float DISRUPTION_MIN = 90;
//	public static float DISRUPTION_RANGE  = 60;
	
	public static float MIN_SABOTAGE = Global.getSettings().getFloatFromArray("luddicPathSabotageDays", 0); 
	public static float MAX_SABOTAGE = Global.getSettings().getFloatFromArray("luddicPathSabotageDays", 1);	
	
	
	public static Object UPDATE_DISSOLVED = new Object();
	public static Object UPDATE_DISRUPTED = new Object();
	
	public static Object INCIDENT_PREP = new Object();
	public static Object INCIDENT_PREVENTED = new Object();
	public static Object INCIDENT_HAPPENED = new Object();
	
	
	public static enum IncidentType {
		REDUCED_STABILITY,
		INDUSTRY_SABOTAGE,
		PLANETBUSTER,
	}
	
	protected boolean sleeper = false;
	protected float sleeperTimeout = 0f;
	
	protected MarketAPI market;
	
	protected IntervalUtil incidentTracker = new IntervalUtil(20f, 40f);
	protected Random random = new Random();
	
	protected int numIncidentAttempts = 0;
	
	protected float incidentDelay = 0f;
	protected IncidentType incidentType = null;
	protected RouteData smuggler = null;
	
	protected IncidentType prevIncident = null;
	protected boolean prevIncidentSucceeded = false;
	protected float sincePrevIncident = 0f;
	protected Object prevIncidentData = null;
	
	protected float inertiaTime = 0f; // time the cell has existed despite not being a priority for the LP to maintain
	
	
	public LuddicPathCellsIntel(MarketAPI market, boolean sleeper) {
		this.market = market;
		this.sleeper = sleeper;
		
		if (!market.isPlayerOwned()) {
			setPostingLocation(market.getPrimaryEntity());
		}
		
		if (!market.hasCondition(Conditions.PATHER_CELLS)) {
			market.addCondition(Conditions.PATHER_CELLS, this);
		}
		
		Global.getSector().addScript(this);
		
		if (market.isPlayerOwned() || DebugFlags.PATHER_BASE_DEBUG) {
			Global.getSector().getIntelManager().addIntel(this);
		} else {
			Global.getSector().getIntelManager().queueIntel(this);
		}
	}
	
	public static LuddicPathBaseIntel getClosestBase(MarketAPI market) {
		List<IntelInfoPlugin> bases = Global.getSector().getIntelManager().getIntel(LuddicPathBaseIntel.class);
		float minDist = Float.MAX_VALUE;
		LuddicPathBaseIntel closest = null;
		for (IntelInfoPlugin curr : bases) {
			LuddicPathBaseIntel intel = (LuddicPathBaseIntel) curr;
			float dist = Misc.getDistance(intel.getMarket().getLocationInHyperspace(), market.getLocationInHyperspace());
			if (dist < minDist) {
				minDist = dist;
				closest = intel;
			}
		}
		return closest;
	}
	
	public static List<LuddicPathCellsIntel> getCellsForBase(LuddicPathBaseIntel base, boolean includeSleeper) {
		List<LuddicPathCellsIntel> result = new ArrayList<LuddicPathCellsIntel>();
		
		List<IntelInfoPlugin> cells = Global.getSector().getIntelManager().getIntel(LuddicPathCellsIntel.class);
		for (IntelInfoPlugin curr : cells) {
			LuddicPathCellsIntel intel = (LuddicPathCellsIntel) curr;
			if (!includeSleeper && intel.isSleeper()) continue;
			if (getClosestBase(intel.getMarket()) == base) {
				result.add(intel);
			}
		}
		return result;
	}
	
	public static LuddicPathCellsIntel getCellsForMarket(MarketAPI market) {
		if (market == null) return null;
		List<IntelInfoPlugin> cells = Global.getSector().getIntelManager().getIntel(LuddicPathCellsIntel.class);
		for (IntelInfoPlugin curr : cells) {
			LuddicPathCellsIntel intel = (LuddicPathCellsIntel) curr;
			if (intel.getMarket() == market) return intel;
		}
		return null;
	}
	
	
	public MarketAPI getMarket() {
		return market;
	}

	@Override
	public boolean canMakeVisibleToPlayer(boolean playerInRelayRange) {
		return super.canMakeVisibleToPlayer(playerInRelayRange);
	}

	@Override
	protected void notifyEnded() {
		super.notifyEnded();
		Global.getSector().removeScript(this);
	}


	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		if (market.hasCondition(Conditions.PATHER_CELLS)) {
			market.removeCondition(Conditions.PATHER_CELLS);
		}
	}
	
	
	public void makeSleeper() {
		makeSleeper(-1);
	}
	public void makeSleeper(float sleeperTimeout) {
		if (sleeperTimeout >= 0) {
			this.sleeperTimeout = sleeperTimeout;
		}
		sleeper = true;
	}
	public void makeActiveIfPossible() {
		if (sleeperTimeout <= 0) {
			sleeper = false;
		}
	}
	

	@Override
	protected void advanceImpl(float amount) {
		super.advanceImpl(amount);
		
		float days = Misc.getDays(amount);
		
		inertiaTime += days;
		
		if (sleeperTimeout > 0) {
			sleeperTimeout -= days;
			if (sleeperTimeout < 0) {
				sleeperTimeout = 0;
			}
		}
		
		if (!market.isInEconomy()) {
			endImmediately();
			return;
		}
		
		if (isSleeper()) return;
		
		// incidents handled through HostileActivityEventIntel now
		// not anymore since the change from Hostile Activity -> Colony Crises
		//if (market.isPlayerOwned()) return;
		
		if (DebugFlags.PATHER_BASE_DEBUG) {
			days *= 200f;
		}
		
		if (prevIncident != null) {
			float mult = 1f;
			if (DebugFlags.PATHER_BASE_DEBUG) {
				mult = 1f / 20f;
			}
			sincePrevIncident += days * mult;
			if (sincePrevIncident >= 180f) {
				sincePrevIncident = 0f;
				prevIncident = null;
				prevIncidentData = null;
				prevIncidentSucceeded = false;
			}
		}
//		if (market.isPlayerOwned()) {
//			System.out.println("wefwefwe");
//		}
		if (incidentType == null && prevIncident == null) {
			incidentTracker.advance(days);
			if (incidentTracker.intervalElapsed() && random.nextFloat() < INCIDENT_PROB) {
				prepareIncident();
				numIncidentAttempts++;
			}
		} else if (incidentType != null) {
			if (incidentDelay > 0 && smuggler == null) {
				incidentDelay -= days;
			}
			
			if (smuggler == null && incidentDelay <= 0) {
				beginIncident();
			}
//				if(market.isPlayerOwned()) {
//					System.out.println("efwwefew");
//				}
//			smuggler.getActiveFleet().getLocation()
//			smuggler.getActiveFleet().getContainingLocation()
			if (smuggler != null) {
				RouteSegment segment = smuggler.getCurrent();
				if (segment != null && segment.getId() == EconomyFleetRouteManager.ROUTE_TRAVEL_SRC) {
//					if(market.isPlayerOwned()) {
//						System.out.println("efwwefew");
//					}
					doIncident();
				}
			}
		}
	}
	
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		boolean isUpdate = getListInfoParam() != null;
		
		if (mode != ListInfoMode.IN_DESC) {
			addMarketToList(info, market, initPad, tc);
			initPad = 0f;
		}
		//info.addPara(market., initPad)
		
		if (isUpdate) {
			if (getListInfoParam() == INCIDENT_HAPPENED) {
				if (!prevIncidentSucceeded) {
					info.addPara("Incident averted by local security forces", initPad);
				} else {
					switch (prevIncident) {
					case INDUSTRY_SABOTAGE:
						Industry ind = (Industry) prevIncidentData;
						String days = getDays(ind.getDisruptedDays());
						String daysStr = getDaysString(ind.getDisruptedDays());
						info.addPara(ind.getCurrentName() + " operations disrupted for %s " + daysStr, initPad, tc, h, days);
						break;
					case REDUCED_STABILITY:
						info.addPara("Stability reduced by %s", initPad, tc, h, "" + (Integer) prevIncidentData);
						break;
					case PLANETBUSTER:
						info.addPara("Colony destroyed by planetbuster", initPad, tc);
						break;
					}
				}
			}
		}
		
		unindent(info);
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
		addBulletPoints(info, mode);
	}
	
	public void addInterestInfo(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		float opad = 10f;
		
		info.addSectionHeading("Pather interest", getFactionForUIColors().getBaseUIColor(),
				  getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
		
		info.addPara("The following activity is attracting Pather interest, whether due to AI core use or the inherent nature of the industry:", opad);
		
		List<Industry> industries = new ArrayList<Industry>(market.getIndustries());
		Iterator<Industry> iter = industries.iterator();
		while (iter.hasNext()) {
			if (iter.next().isHidden()) {
				iter.remove();
			}
		}
		Collections.sort(industries, new Comparator<Industry>() {
			public int compare(Industry o1, Industry o2) {
				float s1 = o1.getPatherInterest();
				float s2 = o2.getPatherInterest();
				return (int) Math.signum(s2 - s1);
			}
		});
		String indent = "    ";
		float initPad = 5f;
		boolean added = false;
		
		String aiCoreId = market.getAdmin().getAICoreId();
		if (aiCoreId != null) {
			int s = (int) Math.round(LuddicPathBaseManager.AI_CORE_ADMIN_INTEREST);
			if (market.getAdmin().getMemoryWithoutUpdate().getBoolean(MemFlags.SUSPECTED_AI)) {
				info.addPara(indent + "Suspected AI core administrator (%s)", initPad, h, "" + s);
			} else {
				info.addPara(indent + "AI core administrator (%s)", initPad, h, "" + s);
			}
			initPad = 3f;
			added = true;
		}
		
		for (Industry ind : industries) {
			//float score = LuddicPathBaseManager.getLuddicPathMarketInterest(market);
			float score = ind.getPatherInterest();
			if (score > 0) {
				int s = (int) Math.round(score);
				info.addPara(indent + ind.getCurrentName() + " (%s)", initPad, h, "" + s);
				initPad = 3f;
				added = true;
			}
		}
		
		
		
		if (!added) {
			info.addPara(indent + "None", initPad);
		}
		
		
	}
	
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		if (width > 0) { // it's 0 when called from market condition tooltip
			info.addImage(getFactionForUIColors().getLogo(), width, 128, opad);
		}
		
		if (isEnding()) {
			info.addPara("The Pather cells " +
					market.getOnOrAt() + " " + market.getName() + " have been dissolved.", opad);
		} else if (isSleeper() && sleeperTimeout <= 0) {
			info.addPara("There are indications that sleeper Luddic Path cells are being organized " +
					market.getOnOrAt() + " " + market.getName() + ".", opad);
			info.addPara("The Pathers have not made any significant moves, but are apparently preparing " + 
					"to do so if whatever activity they object to - industrial development, or the suspected " +
					"use of AI cores, and other such - continues.", opad);
		} else {
			info.addPara("There are active Luddic Path cells " +
					market.getOnOrAt() + " " + market.getName() + ".", opad);
//			info.addPara("They are engaged in planning acts of terror and industrial sabotage, but " +
//					"need material support - smuggled in from the nearest Pather base - to carry them off."
//					+ " The cells also provide intel to Pather fleets operating in-system.", opad);
			info.addPara("They are engaged in planning acts of terror and industrial sabotage, but " +
					"are unlikely to carry them off unless the overal level of hostile activity in the system "
					+ "provides sufficient cover."
					+ " The cells also provide intel to Pather fleets operating in-system.", opad);
			
			if (sleeperTimeout > 0) {
				int daysNum = (int) Math.round(sleeperTimeout);
				if (daysNum < 1) daysNum = 1;
				String days = getDaysString(daysNum);
				info.addPara("However, the base supporting these cells is no longer operational. " +
						"It is projected that establishing a new support network will take at least " +
						"%s " + days + ", provided another base exists.", opad,
						h,
						"" + daysNum);
			} else {
				LuddicPathBaseIntel base = LuddicPathCellsIntel.getClosestBase(market);
				if (base != null) {
					if (base.isPlayerVisible()) {
						info.addPara("The Pather base at the " + base.getMarket().getStarSystem().getNameWithLowercaseType() + 
							" is providing support to these cells.", opad);
					} else {
						info.addPara("You do not know the location of the Pather base providing support to these cells.", opad);
					}
					
					info.addPara("If the base is destroyed, it will take some time to organize " +
							"support from another base, and both ground and fleet operations will be disrupted.", opad);
				} 
			}
		}
		
		if (!isEnding()) {
			info.addSectionHeading("Impact", getFactionForUIColors().getBaseUIColor(),
								  getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
			
			if (!isSleeper()) {
				float stability = LuddicPathCells.STABLITY_PENALTY;
//				info.addPara("%s stability. Possibility of various acts of terror and sabotage, " +
//						"if smugglers from a Luddic Path base are able to provide material support.", 
//						opad, h,
//						"-" + (int)stability);
				info.addPara("%s stability.", 
						opad, h,
						"-" + (int)stability);
			} else {
				if (sleeperTimeout <= 0) { // only show for actual sleeper cells, not "disrupted" active cells
					info.addPara("No perceptible impact on operations as of yet.", opad);
				} else {
					info.addPara("No impact on operations due to lack of material support.", opad);
				}
			}
			
			//addInterestInfo(info, width, height);
		}
		
		if (prevIncident != null || incidentType == IncidentType.PLANETBUSTER) {
			info.addSectionHeading("Recent events", getFactionForUIColors().getBaseUIColor(),
					  getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
			
			if (prevIncidentSucceeded) {
				if (incidentType == IncidentType.PLANETBUSTER) {
					info.addPara("There are indications that the Pather cells are preparing to sneak " +
							"a planetbuster onto " + market.getName() + ". " + 
							"If they succeed, the colony will effectively be destroyed.", opad);
				} else if (prevIncident != null) {
					switch (prevIncident) {
					case INDUSTRY_SABOTAGE:
						if (prevIncidentData instanceof Industry) {
							Industry ind = (Industry) prevIncidentData;
							if (ind.getDisruptedDays() > 2) {
								String days = getDays(ind.getDisruptedDays());
								String daysStr = getDaysString(ind.getDisruptedDays());
								info.addPara("The Pather cells have conducted a successful act of sabotage, " +
										"disrupting " + ind.getCurrentName() + " operations for %s " + daysStr + ".", 
										opad, h, days);
							}
						}
						break;
					case REDUCED_STABILITY:
						info.addPara("The Pather cells have conducted low-level attacks on various " +
								"industrial, military, and civilian targets, reducing stability by %s.",
								opad, h, "" + (Integer) prevIncidentData);
						break;
					case PLANETBUSTER:
						info.addPara("The Pather cells have smuggled a planetbuster onto " + market.getName() + 
								" and detonated it. The colony has been effectively destroyed.", opad);
						break;
					}
				}
			} else {
				if (prevIncident != null) {
					switch (prevIncident) {
					case INDUSTRY_SABOTAGE:
						if (prevIncidentData instanceof Industry) {
							Industry ind = (Industry) prevIncidentData;
							info.addPara("An attempted act of sabotage against " +
										ind.getCurrentName() + " operations was averted by the local security forces.", 
										opad);
						}
						break;
					case REDUCED_STABILITY:
						info.addPara("Multiple planned attacks against various industrial, " +
								"military, and civilian targets " +
								" were averted by the local security forces.", 
								opad);
						break;
					case PLANETBUSTER:
						info.addPara("The Pather cells have smuggled a planetbuster onto " + market.getName() + 
								", but the local security forces were able to locate and disarm it, thereby " +
								"saving the colony.", opad);
						break;
					}
				}
			}
			
			addBulletPoints(info, ListInfoMode.IN_DESC);
		}
		
		if (!isEnding()) {
			addInterestInfo(info, width, height);
		}
	}
	
	public List<ArrowData> getArrowData(SectorMapAPI map) {
		if (sleeperTimeout > 0) return null;
		
		LuddicPathBaseIntel base = LuddicPathCellsIntel.getClosestBase(market);
		if (base == null || !base.isPlayerVisible()) return null;
		
		List<ArrowData> result = new ArrayList<ArrowData>();
		
		
		SectorEntityToken entityFrom = base.getMapLocation(map);
		if (map != null) {
			SectorEntityToken iconEntity = map.getIntelIconEntity(base);
			if (iconEntity != null) {
				entityFrom = iconEntity;
			}
		}
		
		ArrowData arrow = new ArrowData(entityFrom, market.getPrimaryEntity());
		arrow.color = getFactionForUIColors().getBaseUIColor();
		result.add(arrow);
		
		return result;
	}
	
	@Override
	public String getIcon() {
		if (isSleeper()) {
			return Global.getSettings().getSpriteName("intel", "sleeper_cells");
		}
		return Global.getSettings().getSpriteName("intel", "active_cells");
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Factions.LUDDIC_PATH);
		
		if (market.isPlayerOwned() && !isSleeper()) {
			tags.add(Tags.INTEL_COLONIES);
		}
		
		return tags;
	}
	
	public String getSortString() {
		String base = Misc.ucFirst(getFactionForUIColors().getPersonNamePrefix());
		if (sleeper) {
			return base + " D"; // so it goes after "Luddic Path Base"
		}
		return base + " C"; // so it goes after "Luddic Path Base"
	}
	
	public String getName() {
		String base = "Luddic Path Cells";
		
		if (isSendingUpdate()) {
			if (getListInfoParam() == INCIDENT_HAPPENED) {
				if (prevIncidentSucceeded) {
					return base + " - Incident";
				} else {
					return base + " - Incident Averted";
				}
			}
		}
		
		if (isEnding()) {
			return base + " - Dissolved";
		}
		if (sleeperTimeout > 0) {
			return base + " - Disrupted";
		}
		if (isSleeper()) {
			return base + " - Sleeper";
		} else {
			return base + " - Active";
		}
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return Global.getSector().getFaction(Factions.LUDDIC_PATH);
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return market.getPrimaryEntity();
	}

	
	@Override
	public String getCommMessageSound() {
		return super.getCommMessageSound();
	}

	public boolean isSleeper() {
		if (Factions.PLAYER.equals(market.getFactionId())) {
			if (HA_CMD.playerHasPatherAgreement()) {
				return true;
			}
		}
		return sleeper;
	}

	public void setSleeper(boolean sleeper) {
		this.sleeper = sleeper;
	}

	public float getSleeperTimeout() {
		return sleeperTimeout;
	}

	public void setSleeperTimeout(float sleeperTimeout) {
		this.sleeperTimeout = sleeperTimeout;
	}

	
	public String getRouteSourceId() {
		return EconomyFleetRouteManager.SOURCE_ID;
		//return "pather_cells_smuggler";
	}
	
	
	public void prepareIncident() {
		abortIncident();
		//if (incidentType != null) return;
		LuddicPathBaseIntel base = LuddicPathCellsIntel.getClosestBase(getMarket());
		if (base == null) return;
		
		WeightedRandomPicker<IncidentType> types = new WeightedRandomPicker<IncidentType>(random);
		
		types.add(IncidentType.REDUCED_STABILITY, 10f);
		if (numIncidentAttempts >= 3 || !market.isPlayerOwned()) {
			types.add(IncidentType.INDUSTRY_SABOTAGE, 10f);
		}
		
//		if (numIncidentAttempts >= 10 && market.getSize() >= 5 && 
//				!Global.getSector().getMemory().is(USED_PLANETBUSTER_KEY, true)) {
//			types.add(IncidentType.PLANETBUSTER, 5f);
//		}
		
		incidentType = types.pick();
		//incidentType = IncidentType.REDUCED_STABILITY;
		incidentDelay = MIN_WARNING_DAYS + random.nextFloat() * MIN_WARNING_DAYS;
		
		
		if (incidentType == IncidentType.PLANETBUSTER) {
			incidentDelay = MIN_WARNING_DAYS * 4f + random.nextFloat() * 30f;
			Global.getSector().getMemoryWithoutUpdate().set(USED_PLANETBUSTER_KEY, true, 1500f);
		}
		
//		if (market.isPlayerOwned()) {
//			sendUpdateIfPlayerHasIntel(INCIDENT_PREP, false);
//		}
	}
	
	public void beginIncident() {
		LuddicPathBaseIntel base = LuddicPathCellsIntel.getClosestBase(getMarket());
		if (base == null) {
			abortIncident();
			return;
		}
		
		sendSmuggler(base);
	}
	
	public void abortIncident() {
		incidentDelay = 0;
		incidentType = null;
		if (smuggler != null && smuggler.getActiveFleet() != null) {
			smuggler.getActiveFleet().removeEventListener(this);
		}
		smuggler = null;
	}
	
	protected boolean checkSuccess() {
		float pSuccess = 1f - market.getStabilityValue() * 0.075f;
		return random.nextFloat() < pSuccess;
	}
	
	public void doIncident() {
		if (incidentType == null) return;
		
		prevIncidentData = null;
		
		boolean success = checkSuccess();
		
		if (incidentType == IncidentType.REDUCED_STABILITY) {
			if (success) {
				RecentUnrest.get(market).add(3, 
						Misc.ucFirst(Global.getSector().getFaction(Factions.LUDDIC_PATH).getPersonNamePrefix()) + " sabotage");
				prevIncidentData = 3;
			}
		} else if (incidentType == IncidentType.INDUSTRY_SABOTAGE) {
			WeightedRandomPicker<Industry> picker = new WeightedRandomPicker<Industry>(random);
			for (Industry ind : market.getIndustries()) {
				if (!ind.canBeDisrupted()) continue;
				picker.add(ind, ind.getPatherInterest());
			}
			Industry target = picker.pick();
			if (target == null) {
				abortIncident();
				return;
			}
			
			prevIncidentData = target;
			if (success) {
				float disruptionDur = MIN_SABOTAGE + random.nextFloat() * (MAX_SABOTAGE - MIN_SABOTAGE);
				target.setDisrupted(disruptionDur, true);
			}
		} else if (incidentType == IncidentType.PLANETBUSTER) {
			// ??? turn into lava planet, remove market, etc
		}
		
		prevIncident = incidentType;
		sincePrevIncident = 0f;
		prevIncidentSucceeded = success;
		
		
		if (DebugFlags.SEND_UPDATES_WHEN_NO_COMM || Global.getSector().getIntelManager().isPlayerInRangeOfCommRelay() 
				|| market.isPlayerOwned()) {
			if (market.isPlayerOwned() || 
					incidentType == IncidentType.INDUSTRY_SABOTAGE ||
					incidentType == IncidentType.PLANETBUSTER) {
				sendUpdateIfPlayerHasIntel(INCIDENT_HAPPENED, false);
			}
		}
		
		abortIncident();
	}
	
	
	protected void sendSmuggler(LuddicPathBaseIntel base) {
		String sid = getRouteSourceId();
		
		SectorEntityToken from = base.getMarket().getPrimaryEntity();
		SectorEntityToken to = getMarket().getPrimaryEntity();
		
		EconomyRouteData data = new EconomyRouteData();
		data.from = base.getMarket();
		data.to = market;
		data.smuggling = true;
		data.cargoCap = 400;
		data.fuelCap = 200;
		
		OptionalFleetData extra = new OptionalFleetData(data.from);
		extra.fleetType = FleetTypes.TRADE_SMUGGLER;
		
		RouteData route = RouteManager.getInstance().addRoute(sid, base.getMarket(), Misc.genRandomSeed(), extra, this, data);
		extra.strength = 50f;
		extra.strength = Misc.getAdjustedStrength(extra.strength, base.getMarket());
		
		
		float orbitDays = 3f + random.nextFloat() * 3f;
		float travelDays = RouteLocationCalculator.getTravelDays(from, to);
		if (DebugFlags.PATHER_BASE_DEBUG) travelDays *= 0.1f;
		
		route.addSegment(new RouteSegment(EconomyFleetRouteManager.ROUTE_SRC_LOAD, orbitDays, from));
		route.addSegment(new RouteSegment(EconomyFleetRouteManager.ROUTE_TRAVEL_DST, travelDays, from, to));
		route.addSegment(new RouteSegment(EconomyFleetRouteManager.ROUTE_DST_UNLOAD, orbitDays * 0.5f, to));
		route.addSegment(new RouteSegment(EconomyFleetRouteManager.ROUTE_DST_LOAD, orbitDays * 0.5f, to));
		route.addSegment(new RouteSegment(EconomyFleetRouteManager.ROUTE_TRAVEL_SRC, travelDays, to, from));
		route.addSegment(new RouteSegment(EconomyFleetRouteManager.ROUTE_SRC_UNLOAD, orbitDays, from));
		
		smuggler = route;
	}
	
	public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
		
	}

	public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
		return false;
	}

	public boolean shouldRepeat(RouteData route) {
		return false;
	}

	public CampaignFleetAPI spawnFleet(RouteData route) {
		Random random = new Random();
		if (route.getSeed() != null) {
			random = new Random(route.getSeed());
		}
		
		CampaignFleetAPI fleet = EconomyFleetRouteManager.createTradeRouteFleet(route, random);
		if (fleet == null) return null;;
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_DO_NOT_IGNORE_PLAYER, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
		fleet.addEventListener(this);
		fleet.addScript(new EconomyFleetAssignmentAI(fleet, route));
		return fleet;
	}

	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		if (smuggler == null || smuggler.getActiveFleet() == null) return;
		
		CampaignFleetAPI active = smuggler.getActiveFleet();
		if (!battle.isInvolved(active)) return;
		
		if (battle.getSideFor(active) != battle.getSideFor(primaryWinner)) {
			abortIncident();
		}
	}

	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (smuggler != null && fleet == smuggler.getActiveFleet()) {
			abortIncident();
		}
	}

	public float getInertiaTime() {
		return inertiaTime;
	}

	public void setInertiaTime(float inertiaTime) {
		this.inertiaTime = inertiaTime;
	}
	
	
}







