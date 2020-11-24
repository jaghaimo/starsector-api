package com.fs.starfarer.api.impl.campaign.intel.raid;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI.FleetActionDelegate;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class RaidIntel extends BaseIntelPlugin implements RouteFleetSpawner {

	public static Object UPDATE_FAILED = new Object();
	public static Object UPDATE_RETURNING = new Object();
	
	public static enum RaidStageStatus {
		ONGOING,
		SUCCESS,
		FAILURE,
	}
	
	public static interface RaidDelegate {
		void notifyRaidEnded(RaidIntel raid, RaidStageStatus status);
	}
	
	public static interface RaidStage {
		RaidStageStatus getStatus();
		void advance(float amount);
		void notifyStarted();
		float getExtraDaysUsed();
		void showStageInfo(TooltipMakerAPI info);
		float getElapsed();
		float getMaxDays();
	}
	
	protected int currentStage = 0;
	protected int failStage = -1;
	protected List<RaidStage> stages = new ArrayList<RaidStage>();
	
	protected String id = Misc.genUID();
	protected String sid = "raid_" + id;
	
	protected float extraDays = 60f;
	protected StarSystemAPI system;
	protected FactionAPI faction;
	protected float defenderStr = 0f;
	protected RaidDelegate delegate;
	
	public RaidIntel(StarSystemAPI system, FactionAPI faction, RaidDelegate delegate) {
		
		//RAID_DEBUG = false;
		
		this.system = system;
		this.faction = faction;
		this.delegate = delegate;
		
		Global.getSector().addScript(this);
//		Global.getSector().getIntelManager().addIntel(this);
		
		
//		List<MarketAPI> targets = new ArrayList<MarketAPI>();
//		for (MarketAPI market : Misc.getMarketsInLocation(system)) {
//			if (market.getFaction().isHostileTo(getFaction())) {
//				targets.add(market);
//			}
//		}
		defenderStr = WarSimScript.getEnemyStrength(getFaction(), system);
	}
	
	public StarSystemAPI getSystem() {
		return system;
	}


	public int getCurrentStage() {
		return currentStage;
	}
	
	public int getStageIndex(RaidStage stage) {
		return stages.indexOf(stage);
	}
	
	public int getFailStage() {
		return failStage;
	}

	public OrganizeStage getOrganizeStage() {
		for (RaidStage stage : stages) {
			if (stage instanceof OrganizeStage) {
				return (OrganizeStage) stage;
			}
		}
		return null;
	}
	public AssembleStage getAssembleStage() {
		for (RaidStage stage : stages) {
			if (stage instanceof AssembleStage) {
				return (AssembleStage) stage;
			}
		}
		return null;
		//return (AssembleStage) stages.get(0);
	}
	
	public ActionStage getActionStage() {
		for (RaidStage stage : stages) {
			if (stage instanceof ActionStage) {
				return (ActionStage) stage;
			}
		}
		return null;
		//return (AssembleStage) stages.get(0);
	}
	
	public void addStage(RaidStage stage) {
		stages.add(stage);
	}
	
	public String getRouteSourceId() {
		return sid; 
	}
	
	public float getExtraDays() {
		return extraDays;
	}

	public void setExtraDays(float extraDays) {
		this.extraDays = extraDays;
	}



	@Override
	public boolean canMakeVisibleToPlayer(boolean playerInRelayRange) {
		return super.canMakeVisibleToPlayer(playerInRelayRange);
	}

	public boolean shouldSendUpdate() {
		if (DebugFlags.SEND_UPDATES_WHEN_NO_COMM || Global.getSector().getIntelManager().isPlayerInRangeOfCommRelay()) {
			return true;
		}
		if (system != null && system == Global.getSector().getCurrentLocation()) {
			return true;
		}
		
		return isPlayerTargeted();
	}
	
	public boolean isPlayerTargeted() {
		ActionStage action = getActionStage();
		if (action != null && action.isPlayerTargeted()) return true;
		return false;
	}
	
	public String getCommMessageSound() {
		if (isPlayerTargeted() && !isSendingUpdate()) {
			return getSoundColonyThreat();
		}
		
		if (isSendingUpdate()) {
			return getSoundStandardUpdate();
		}
		return getSoundMajorPosting();
	}
	
	@Override
	protected void advanceImpl(float amount) {
		super.advanceImpl(amount);
	
		if (currentStage >= stages.size()) {
			endAfterDelay();
			// do we really need an update after the raiders have returned to their source colonies?
			// as far as the player is concerned, the raid is really over when they head back, not when they get back
			
			// actually, the return stage finishes immediately since its updateStatus() sets to SUCCESS right away
			// so this update gets sent right after the action stage succeeds
			if (shouldSendUpdate()) {
				sendUpdateIfPlayerHasIntel(UPDATE_RETURNING, false);
			}
			return;
		}
		
		RaidStage stage = stages.get(currentStage);
		
		stage.advance(amount);
		
		RaidStageStatus status = stage.getStatus();
		if (status == RaidStageStatus.SUCCESS) {
			currentStage++;
			setExtraDays(Math.max(0, getExtraDays() - stage.getExtraDaysUsed()));
			if (currentStage < stages.size()) {
				stages.get(currentStage).notifyStarted();
			}
			return;
		} else if (status == RaidStageStatus.FAILURE) {
			failedAtStage(stage);
			failStage = currentStage;
			endAfterDelay();
			if (shouldSendUpdate()) {
				sendUpdateIfPlayerHasIntel(UPDATE_FAILED, false);
			}
		}
	}
	
	public void forceFail(boolean withUpdate) {
		int index = currentStage;
		if (index >= stages.size()) index = stages.size() - 1;
		failedAtStage(stages.get(index));
		failStage = currentStage;
		endAfterDelay();
		if (withUpdate && shouldSendUpdate()) {
			sendUpdateIfPlayerHasIntel(UPDATE_FAILED, false);
		}
	}
	
	protected void failedAtStage(RaidStage stage) {
		
	}
	

	@Override
	protected void notifyEnded() {
		super.notifyEnded();
		Global.getSector().removeScript(this);
	}
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		if (delegate != null) {
			RaidStageStatus status = RaidStageStatus.SUCCESS;
			if (failStage >= 0) {
				status = RaidStageStatus.FAILURE;
			}
			delegate.notifyRaidEnded(this, status);
		}
	}

	
	public float getETA() {
		int curr = getCurrentStage();
		float eta = 0f;
		for (RaidStage stage : stages) {
			if (stage instanceof ActionStage) {
				break;
			}
			//RouteLocationCalculator.getTravelDays(((TravelStage)stage).from, ((TravelStage)stage).to)
			int index = getStageIndex(stage);
			if (index < curr) {
				continue;
			}
			if (stage instanceof OrganizeStage) {
				eta += Math.max(0f, stage.getMaxDays() - stage.getElapsed());
			} else if (stage instanceof AssembleStage) {
				eta += Math.max(0f, 10f - stage.getElapsed());
			} else if (stage instanceof TravelStage) {
				float travelDays = RouteLocationCalculator.getTravelDays(getAssembleStage().gatheringPoint, system.getHyperspaceAnchor());
				eta += Math.max(0f, travelDays - stage.getElapsed());
			}
		}
		return eta;
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
		
		float eta = getETA();
		
		info.addPara("Faction: " + faction.getDisplayName(), initPad, tc,
				 	 faction.getBaseUIColor(), faction.getDisplayName());
		initPad = 0f;
		
		int max = 0;
		MarketAPI target = null;
		for (MarketAPI other : Misc.getMarketsInLocation(system)) {
			if (!other.getFaction().isHostileTo(faction)) continue;
			int size = other.getSize();
			if (size > max || (size == max && other.getFaction().isPlayerFaction())) {
				max = size;
				target = other;
			}
		}
		
		
		if (target != null) {
			FactionAPI other = target.getFaction();
			info.addPara("Target: " + other.getDisplayName(), initPad, tc,
					     other.getBaseUIColor(), other.getDisplayName());
		}
		
		if (isUpdate) {
			if (failStage < 0) {
				info.addPara("Colonies in the " + system.getNameWithLowercaseType() + " have been raided", 
				  	 	 	tc, initPad);
			} else {
				info.addPara("The raid on the " + system.getNameWithLowercaseType() + " has failed", 
			  	 	 	tc, initPad);
			}
		} else {
			info.addPara(system.getNameWithLowercaseType(), 
					  	 tc, initPad);
		}
		initPad = 0f;
		if (eta > 1 && failStage < 0) {
			String days = getDaysString(eta);
			info.addPara("Estimated %s " + days + " until arrival", 
					initPad, tc, h, "" + (int)Math.round(eta));
			initPad = 0f;
		}
		
		unindent(info);
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		
		if (isPlayerTargeted() && false) {
			info.setParaSmallInsignia();
		} else {
			info.setParaFontDefault();
		}
		
		info.addPara(getName(), c, 0f);
		info.setParaFontDefault();
		addBulletPoints(info, mode);
	}
	
	protected MarketAPI getFirstSource() {
		AssembleStage as = getAssembleStage();
		if (as == null) return null;
		if (as.getSources() == null || as.getSources().isEmpty()) return null;
		return as.getSources().get(0);
	}
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		info.addImage(getFactionForUIColors().getLogo(), width, 128, opad);
		
		FactionAPI faction = getFaction();
		String has = faction.getDisplayNameHasOrHave();
		String is = faction.getDisplayNameIsOrAre();
		
		AssembleStage as = getAssembleStage();
		MarketAPI source = getFirstSource();
		
		//float raidStr = as.getSpawnFP();
		float raidStr = as.getOrigSpawnFP();
		raidStr = Misc.getAdjustedStrength(raidStr, source);
		
//		String strDesc = "";
//		// fp, multiplied by roughly 0.25 to 4, depending on quality, colony size, doctrine
//		if (raidStr < 150) {
//			strDesc = "very weak";
//		} else if (raidStr < 300) {
//			strDesc = "somewhat weak";
//		} else if (raidStr < 500) {
//			strDesc = "fairly strong";
//		} else if (raidStr < 1000) {
//			strDesc = "strong";
//		} else {
//			strDesc = "very strong";
//		}
		
		String strDesc = getRaidStrDesc();
		int numFleets = (int) getOrigNumFleets();
		String fleets = "fleets";
		if (numFleets == 1) fleets = "fleet";
		
		LabelAPI label = info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + is + 
				" conducting a raid of the " + system.getName() + ". The raiding forces are " +
						"projected to be " + strDesc + 
						" and likely comprised of " + numFleets + " " + fleets + ".",
				opad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
		label.setHighlight(faction.getDisplayNameWithArticleWithoutArticle(), strDesc, "" + numFleets);
		label.setHighlightColors(faction.getBaseUIColor(), h, h);
		
		List<MarketAPI> targets = new ArrayList<MarketAPI>();
		for (MarketAPI market : Misc.getMarketsInLocation(system)) {
			if (market.getFaction().isHostileTo(faction)) {
				targets.add(market);
			}
		}
		
		defenderStr = WarSimScript.getEnemyStrength(getFaction(), system);
		
		List<MarketAPI> safe = new ArrayList<MarketAPI>();
		List<MarketAPI> unsafe = new ArrayList<MarketAPI>();
		for (MarketAPI market : targets) {
			float defensiveStr = defenderStr + WarSimScript.getStationStrength(market.getFaction(), system, market.getPrimaryEntity());
			if (defensiveStr > raidStr * 1.25f) {
				safe.add(market);
			} else {
				unsafe.add(market);
			}
		}
		
		if (!isEnding()) {
			if (targets.isEmpty()) {
				info.addPara("There are no colonies for the raid to target in the system.", opad);
			} else {
				boolean showSafe = false;
				if (raidStr < defenderStr * 0.75f) {
					info.addPara("The raiding forces should be outmatched by fleets defending the system. In the absence of " +
							"other factors, the raid is unlikely to find success.", opad);
				} else if (raidStr < defenderStr * 1.25f) {
					info.addPara("The raiding forces are evenly matched with fleets defending the system.", opad);
					showSafe = true;
				} else {
					info.addPara("The raiding forces are superior to the fleets defending the system.", opad);
					showSafe = true;
				}
				if (showSafe) {
					if (safe.size() == targets.size()) {
						info.addPara("However, all colonies should be safe from the raid, " +
									 "owing to their orbital defenses.", opad);
					} else {
						info.addPara("Considering orbital defenses (if any), the following colonies are " +
								"at risk from the raid:", opad);
						float initPad = opad;
						for (MarketAPI market : unsafe) {
							addMarketToList(info, market, initPad);
							initPad = 0f;
						}
						
	//					info.addPara("Unless the raid is stopped, these colonies " +
	//							"may suffer " +
	//							"reduced stability, infrastructure damage, and a possible loss of stockpiled resources.", opad);
						
					}
				}
			}
		}
		
		info.addSectionHeading("Status", 
				   faction.getBaseUIColor(), faction.getDarkUIColor(), Alignment.MID, opad);
		
		for (RaidStage stage : stages) {
			stage.showStageInfo(info);
			if (getStageIndex(stage) == failStage) break;
		}
	}
	
	
	
	@Override
	public String getIcon() {
		return faction.getCrest();
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_MILITARY);
		if (!Misc.getMarketsInLocation(system, Factions.PLAYER).isEmpty()) {
			tags.add(Tags.INTEL_COLONIES);
		}
		tags.add(getFaction().getId());
		return tags;
	}
	
	public String getSortString() {
		return "Raid";
	}
	
	public String getName() {
		String base = Misc.ucFirst(getFaction().getPersonNamePrefix()) + " Raid";
		if (isEnding()) {
			if (isSendingUpdate() && failStage >= 0) {
				return base + " - Failed";
			}
			for (RaidStage stage : stages) {
				if (stage instanceof ActionStage && stage.getStatus() == RaidStageStatus.SUCCESS) {
					return base + " - Successful";
				}
			}
			return base + " - Over";
		}
		return base;
	}
	
	public boolean isFailed() {
		return failStage >= 0;
	}
	
	public boolean isSucceeded() {
		for (RaidStage stage : stages) {
			if (stage instanceof ActionStage && stage.getStatus() == RaidStageStatus.SUCCESS) {
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return getFaction();
	}
	
	public FactionAPI getFaction() {
		return faction;
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return system.getHyperspaceAnchor();
	}
	
	public List<ArrowData> getArrowData(SectorMapAPI map) {
		AssembleStage as = getAssembleStage();
		if (as == null || !as.isSourceKnown()) return null;
		
		
		SectorEntityToken from = as.gatheringPoint;
		if (system == null|| system == from.getContainingLocation()) return null;
		
		List<ArrowData> result = new ArrayList<ArrowData>();
		
		SectorEntityToken entityFrom = from;
		if (map != null && delegate instanceof IntelInfoPlugin && delegate != this) {
			SectorEntityToken iconEntity = map.getIntelIconEntity((IntelInfoPlugin)delegate);
			if (iconEntity != null) {
				entityFrom = iconEntity;
			}
		}
		
		ArrowData arrow = new ArrowData(entityFrom, system.getCenter());
		arrow.color = getFactionForUIColors().getBaseUIColor();
		arrow.width = 20f;
		result.add(arrow);
		
		return result;
	}

	public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
		return false;
	}

	public boolean shouldRepeat(RouteData route) {
		return false;
	}

	public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
	}

	
	
	public CampaignFleetAPI spawnFleet(RouteData route) {
		
		Random random = route.getRandom();
		
		MarketAPI market = route.getMarket();
		CampaignFleetAPI fleet = createFleet(market.getFactionId(), route, market, null, random);
		
		if (fleet == null || fleet.isEmpty()) return null;
		
		//fleet.addEventListener(this);
		
		market.getContainingLocation().addEntity(fleet);
		fleet.setFacing((float) Math.random() * 360f);
		// this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
		fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().x);
		
		fleet.addScript(createAssignmentAI(fleet, route));
		
		return fleet;
	}
	
	public RouteFleetAssignmentAI createAssignmentAI(CampaignFleetAPI fleet, RouteData route) {
		ActionStage action = getActionStage();
		FleetActionDelegate delegate = null;
		if (action instanceof FleetActionDelegate) {
			delegate = (FleetActionDelegate) action;
		}
		return new RaidAssignmentAI(fleet, route, delegate);
	}
	
	public CampaignFleetAPI createFleet(String factionId, RouteData route, MarketAPI market, Vector2f locInHyper, Random random) {
		if (random == null) random = new Random();
		
		OptionalFleetData extra = route.getExtra();
		
		float combat = extra.fp;
		float tanker = extra.fp * (0.1f + random.nextFloat() * 0.05f);
		float transport = extra.fp * (0.1f + random.nextFloat() * 0.05f);
		float freighter = 0f;
		combat -= tanker;
		combat -= transport;
		
		FleetParamsV3 params = new FleetParamsV3(
				market, 
				locInHyper,
				factionId,
				route == null ? null : route.getQualityOverride(),
				extra.fleetType,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				transport, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod, won't get used since routes mostly have quality override set
				);
		//params.ignoreMarketFleetSizeMult = true; // already accounted for in extra.fp
//		if (DebugFlags.RAID_DEBUG) {
//			params.qualityOverride = 1f;
//		}
		if (route != null) {
			params.timestamp = route.getTimestamp();
		}
		params.random = random;
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		
		if (fleet == null || fleet.isEmpty()) return null;
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_WAR_FLEET, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_RAIDER, true);
		
		if (fleet.getFaction().getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR)) {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
		}
		
		String postId = Ranks.POST_PATROL_COMMANDER;
		String rankId = Ranks.SPACE_COMMANDER;
		
		fleet.getCommander().setPostId(postId);
		fleet.getCommander().setRankId(rankId);
		
		return fleet;
	}
	
	
	public float getRaidFPAdjusted() {
		//AssembleStage as = getAssembleStage();
		//MarketAPI source = as.getSources().get(0);
		MarketAPI source = getFirstSource();
		float raidFP = getRaidFP();
		
		// fp, multiplied by roughly 0.25 to 4, depending on quality, colony size, doctrine
		float raidStr = Misc.getAdjustedFP(raidFP, source);
		return raidStr;
	}
	
	public float getRaidFP() {
		AssembleStage as = getAssembleStage();
		float raidStr = 0f;
		for (RouteData route : as.getRoutes()) {
			CampaignFleetAPI fleet = route.getActiveFleet();
			if (fleet != null) {
				float mult = Misc.getAdjustedFP(1f, route.getMarket());
				if (mult < 1) mult = 1f;
				raidStr += fleet.getFleetPoints() / mult;
			} else {
				raidStr += route.getExtra().fp;
			}
		}
		if (raidStr <= 0 || as.getSpawnFP() > 0) {
			raidStr = Math.max(as.getOrigSpawnFP(), raidStr);
		}
		float raidFP = raidStr;
		return raidFP;
	}
	
	public float getNumFleets() {
		AssembleStage as = getAssembleStage();
		float num = as.getRoutes().size();
		if (as.getSpawnFP() > 0) {
			num = Math.max(num, as.getOrigSpawnFP() / as.getLargeSize(false));
		}
		if (num < 1) num = 1;
		return num;
	}
	
	public float getOrigNumFleets() {
		AssembleStage as = getAssembleStage();
		float num = (float) Math.ceil(as.getOrigSpawnFP() / as.getLargeSize(false));
		if (num < 1) num = 1;
		return num;
	}
	
	public float getRaidStr() {
//		AssembleStage as = getAssembleStage();
//		MarketAPI source = as.getSources().get(0);
		MarketAPI source = getFirstSource();
		float raidFP = getRaidFP();
		
		// fp, multiplied by roughly 0.25 to 4, depending on quality, colony size, doctrine
		float raidStr = Misc.getAdjustedStrength(raidFP, source);
		return raidStr;
	}
	
	protected String getRaidStrDesc() {
		return Misc.getStrengthDesc(getRaidStr());
	}

	public void addStandardStrengthComparisons(TooltipMakerAPI info, 
									MarketAPI target, FactionAPI targetFaction, 
									boolean withGround, boolean withBombard,
									String raid, String raids) {
		Color h = Misc.getHighlightColor();
		float opad = 10f;
		
		//AssembleStage as = getAssembleStage();
		//MarketAPI source = as.getSources().get(0);
		float raidFP = getRaidFPAdjusted() / getNumFleets();
		float raidStr = getRaidStr();
		
		//float defenderStr = WarSimScript.getEnemyStrength(getFaction(), system);
		float defenderStr = WarSimScript.getFactionStrength(targetFaction, system);
		float defensiveStr = defenderStr + WarSimScript.getStationStrength(targetFaction, system, target.getPrimaryEntity());
		
		float assumedRaidGroundStr = raidFP * Misc.FP_TO_GROUND_RAID_STR_APPROX_MULT;
		float re = MarketCMD.getRaidEffectiveness(target, assumedRaidGroundStr);
		
		String spaceStr = "";
		String groundStr = "";
		String outcomeDesc = null;
		boolean even = false;
		if (raidStr < defensiveStr * 0.75f) {
			spaceStr = "outmatched";
			if (outcomeDesc == null) outcomeDesc = "The " + raid + " is likely to be defeated in orbit";
		} else if (raidStr < defensiveStr * 1.25f) {
			spaceStr = "evenly matched";
			if (outcomeDesc == null) outcomeDesc = "The " + raids + " outcome is uncertain";
			even = true;
		} else {
			spaceStr = "superior";
			if (!withGround && !withBombard) {
				if (outcomeDesc == null) outcomeDesc = "The " + raid + " is likely to be successful";
			}
		}
		
		if (withGround) {
			if (re < 0.33f) {
				groundStr = "outmatched";
				if (outcomeDesc == null || even) outcomeDesc = "The " + raid + " is likely to be largely repelled by the ground defences";
			} else if (re < 0.66f) {
				groundStr = "evenly matched";
				if (outcomeDesc == null) outcomeDesc = "The " + raids + " outcome is uncertain";
			} else {
				groundStr = "superior";
				if (outcomeDesc == null) outcomeDesc = "The " + raid + " is likely to be successful";
			}
			//info.addPara("Compared to the defenses of " + target.getName() + ", the " + raids + " space forces are %s " +
			info.addPara("Compared to the defenses, the " + raids + " space forces are %s " +
					"and its ground forces are %s." +
					" " + outcomeDesc + ".", opad, h, spaceStr, groundStr);
		} else if (withBombard) {
			float required = MarketCMD.getBombardmentCost(target, null);
			float available = raidFP * Misc.FP_TO_BOMBARD_COST_APPROX_MULT;
			
			if (required * .67 > available) {
				groundStr = "outmatched";
				if (outcomeDesc == null) outcomeDesc = "The bombardment is likely to be countered by the ground defences";
			} else if (required * 1.33f > available) {
				groundStr = "evenly matched";
				if (outcomeDesc == null) outcomeDesc = "The bombardment's outcome is uncertain";
			} else {
				groundStr = "superior";
				if (outcomeDesc == null) outcomeDesc = "The bombardment is likely to be successful";
			}
			//info.addPara("Compared to the defenses of " + target.getName() + ", the " + raids + " space forces are %s " +
			info.addPara("Compared to the defenses, the " + raids + " space forces are %s. " +
					"" + outcomeDesc + ".", opad, h, spaceStr, groundStr);
			
		} else {
			info.addPara("Compared to the defenses of " + target.getName() + ", " +
						 "the " + raids + " space forces are %s." +
						 " " + outcomeDesc + ".", opad, h, spaceStr, groundStr);
		}
	}
	
	@Override
	public IntelSortTier getSortTier() {
		if (isPlayerTargeted() && false) {
			return IntelSortTier.TIER_2;
		}
		return super.getSortTier();
	}
}







