package com.fs.starfarer.api.impl.campaign.intel.group;

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
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.group.FGRaidAction.FGRaidParams;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission.FleetStyle;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.ComplicationRepImpact;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.BombardType;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class GenericRaidFGI extends FleetGroupIntel {

	public static String PREPARE_ACTION = "prepare_action";
	public static String TRAVEL_ACTION = "travel_action";
	public static String PAYLOAD_ACTION = "payload_action";
	public static String RETURN_ACTION = "return_action";
	
	
	public static interface GenericPayloadAction extends FGAction {
		Color getSystemNameHighlightColor();
		float getSuccessFraction();
		StarSystemAPI getWhere();
		
	}
	
	public static class GenericRaidParams {
		public Random random = new Random();
		public boolean playerTargeted;
		public boolean remnant = false;
		public MarketAPI source;
		public String factionId;
		public List<Integer> fleetSizes = new ArrayList<Integer>();
		public FleetStyle style = FleetStyle.STANDARD;
		public FGRaidParams raidParams = new FGRaidParams(); 
		public float prepDays = 5f; 
		public float payloadDays = 30f; 
		public boolean makeFleetsHostile = true; 
		public ComplicationRepImpact repImpact = ComplicationRepImpact.LOW;
		public String noun;
		public String forcesNoun;
		public Object custom; 
		public String memoryKey = null;
		
		public GenericRaidParams(Random random, boolean playerTargeted) {
			this.random = random;
			this.playerTargeted = playerTargeted;
			//makeFleetsHostile = playerTargeted; // now supported: making fleets hostile to arbitrary factions
		}
		
	}

	protected GenericRaidParams params;
	protected SectorEntityToken origin;
	protected GenericPayloadAction raidAction;
	protected FGTravelAction travelAction;
	protected FGTravelAction returnAction;
	protected FGWaitAction waitAction;
	
	public static GenericRaidFGI get(String key) {
		return (GenericRaidFGI) Global.getSector().getMemoryWithoutUpdate().get(key);
	}
	
	public GenericRaidFGI(GenericRaidParams params) {
		if (params != null) {
			this.params = params;
			setRandom(params.random);
			initActions();
			
			if (params.memoryKey != null) {
				Global.getSector().getMemoryWithoutUpdate().set(params.memoryKey, this);
			}
		}
	}
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		if (params != null && params.memoryKey != null) {
			Global.getSector().getMemoryWithoutUpdate().unset(params.memoryKey);
		}
	}
	
	protected void initActions() {
		setFaction(params.factionId);
		waitAction = new FGWaitAction(params.source.getPrimaryEntity(), params.prepDays, 
				"preparing for departure");
		addAction(waitAction, PREPARE_ACTION);
		
		raidAction = createPayloadAction();
		
		travelAction = new FGTravelAction(params.source.getPrimaryEntity(), 
				 raidAction.getWhere().getCenter());
		
		addAction(travelAction, TRAVEL_ACTION);
		addAction(raidAction, PAYLOAD_ACTION);
		
		SectorEntityToken returnWhere = params.source.getPrimaryEntity();
		if (returnWhere.getStarSystem() != null) {
			returnWhere = returnWhere.getStarSystem().getCenter();
		}
		returnAction = new FGTravelAction(raidAction.getWhere().getCenter(),
				 										 params.source.getPrimaryEntity());
		returnAction.setTravelText("returning to " + params.source.getPrimaryEntity().getName());
		addAction(returnAction, RETURN_ACTION);
		
		origin = params.source.getPrimaryEntity();
		
		int total = 0;
		for (Integer i : params.fleetSizes) total += i;
		createRoute(params.factionId, total, params.fleetSizes.size(), null);
	}
	
	protected GenericPayloadAction createPayloadAction() {
		return new FGRaidAction(params.raidParams, params.payloadDays);
	}
	
	protected void spawnFleets() {
		
		Float damage = null;
		if (route != null && route.getExtra() != null) {
			damage = route.getExtra().damage;
		}
		if (damage == null) damage = 0f;
		
		WeightedRandomPicker<Integer> picker = new WeightedRandomPicker<Integer>(getRandom());
		picker.addAll(params.fleetSizes);
		
		int total = 0;
		for (Integer i : params.fleetSizes) total += i;
		
		float spawnsToSkip = total * damage * 0.5f;
		float skipped = 0f;
		
		//FactionAPI faction = Global.getSector().getFaction(params.factionId);
		
//		picker.add(1);
//		picker.add(1);
		
		while (!picker.isEmpty()) {
			Integer size = picker.pickAndRemove();
			if (skipped < spawnsToSkip && getRandom().nextFloat() < damage) {
				skipped += size;
				continue;
			}
		
			CampaignFleetAPI fleet = createFleet(size, damage);
			
			if (fleet != null && route != null) {
				setLocationAndCoordinates(fleet, route.getCurrent());
				fleets.add(fleet);
			}
		}
	}
	
	protected CampaignFleetAPI createFleet(int size, float damage) {
		Vector2f loc = origin.getLocationInHyperspace();
		boolean pirate = faction.getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR);
		
		FleetCreatorMission m = new FleetCreatorMission(getRandom());
		
		preConfigureFleet(size, m);
		
		m.beginFleet();
		
		String factionId = getFleetCreationFactionOverride(size);
		if (factionId == null) factionId = params.factionId;
		
		m.createFleet(params.style, size, factionId, loc);
		m.triggerSetFleetFaction(params.factionId);
		
		m.setFleetSource(params.source);
		m.setFleetDamageTaken(damage);
		if (pirate) {
			m.triggerSetPirateFleet();
		} else {
			m.triggerSetWarFleet();
		}
		
		if (params.remnant) {
			m.triggerSetRemnantConfigActive();
		}

		if (params.makeFleetsHostile) {
			for (MarketAPI market : params.raidParams.allowedTargets) {
				m.triggerMakeHostileToFaction(market.getFactionId());
			}
			m.triggerMakeHostile();
			if (Factions.LUDDIC_PATH.equals(faction.getId())) {
				m.triggerFleetPatherNoDefaultTithe();
			}
		}
		
		if (params.repImpact == ComplicationRepImpact.LOW || params.repImpact == null) {
			m.triggerMakeLowRepImpact();
		} else if (params.repImpact == ComplicationRepImpact.NONE) {
			m.triggerMakeNoRepImpact();
		}
		
		if (params.repImpact != ComplicationRepImpact.FULL) {
			m.triggerMakeAlwaysSpreadTOffHostility();
		}
		
		configureFleet(size, m);
		
		CampaignFleetAPI fleet = m.createFleet();
		if (fleet != null) {
			configureFleet(size, fleet);
		}
		
		return fleet;
	}
	
	protected String getFleetCreationFactionOverride(int size) {
		return null;
	}
	
	protected void preConfigureFleet(int size, FleetCreatorMission m) {
		
	}
	protected void configureFleet(int size, FleetCreatorMission m) {
		
	}
	
	protected void configureFleet(int size, CampaignFleetAPI fleet) {
		
	}
	
	protected void addTargetingBulletPoint(TooltipMakerAPI info, Color tc, Object param, ListInfoMode mode, float initPad) {
		StarSystemAPI system = raidAction.getWhere();
		Color s = raidAction.getSystemNameHighlightColor();
		LabelAPI label = info.addPara("Targeting the " + system.getNameWithLowercaseTypeShort(), tc, initPad);
		label.setHighlightColors(s);
		label.setHighlight(system.getNameWithNoType());
	}
	
	protected void addNonUpdateBulletPoints(TooltipMakerAPI info, Color tc, Object param, ListInfoMode mode, float initPad) {
		Color h = Misc.getHighlightColor();
		Color s = raidAction.getSystemNameHighlightColor();
		FGAction curr = getCurrentAction();
		StarSystemAPI system = raidAction.getWhere();
		String forces = getForcesNoun();

		float untilDeployment = getETAUntil(PREPARE_ACTION);
		float untilDeparture = getETAUntil(TRAVEL_ACTION);
		float untilRaid = getETAUntil(PAYLOAD_ACTION);
		float untilReturn = getETAUntil(RETURN_ACTION, true);
		if (!isEnding()) {
			if (mode == ListInfoMode.MESSAGES || getElapsed() <= 0f) { // initial notification only, not updates
				addTargetingBulletPoint(info, tc, param, mode, initPad);
				initPad = 0f;
			}
			if (untilDeployment > 0) {
				addETABulletPoints(null, null, false, untilDeployment, ETAType.DEPLOYMENT, info, tc, initPad);
				initPad = 0f;
			} else if (untilDeparture > 0) {
				addETABulletPoints(null, null, false, untilDeparture, ETAType.DEPARTURE, info, tc, initPad);
				initPad = 0f;
			}
			if (untilRaid > 0 && getSource().getContainingLocation() != system) {
				addETABulletPoints(system.getNameWithLowercaseTypeShort(), s, false, untilRaid, ETAType.ARRIVING,
						   info, tc, initPad);
				initPad = 0f;
			}
			if (untilReturn > 0 && RETURN_ACTION.equals(curr.getId()) && getSource().getContainingLocation() != system &&
					mode != ListInfoMode.INTEL) {
				StarSystemAPI from = getSource().getStarSystem();
				
				addETABulletPoints(from.getNameWithLowercaseTypeShort(), null, false, untilReturn, ETAType.RETURNING,
						   info, tc, initPad);
				initPad = 0f;
			}
			if ((mode == ListInfoMode.INTEL || mode == ListInfoMode.MAP_TOOLTIP) 
					&& curr != null && curr.getId().equals(PAYLOAD_ACTION)) {
				LabelAPI label = info.addPara("Operating in the " + system.getNameWithLowercaseTypeShort(), tc, initPad);
				label.setHighlightColors(s);
				label.setHighlight(system.getNameWithNoType());
				initPad = 0f;
			}
		}
		
		if (mode != ListInfoMode.IN_DESC && isEnding()) {
			if (!isSucceeded()) {
				if (!isAborted() && !isFailed()) {
					info.addPara("The " + forces + " have failed to achieve their objective", tc, initPad);
				} else {
					if (isFailedButNotDefeated()) {
						info.addPara("The " + forces + " have failed to achieve their objective", tc, initPad);
					} else {
						info.addPara("The " + forces + " have been defeated and scatter", tc, initPad);
					}
				}
			}
		}
	}
	
	protected void addUpdateBulletPoints(TooltipMakerAPI info, Color tc, Object param, ListInfoMode mode, float initPad) {
		StarSystemAPI system = raidAction.getWhere();
		String forces = getForcesNoun();
		String noun = getNoun();
		Color s = raidAction.getSystemNameHighlightColor();
		//Color h = Misc.getHighlightColor();
		if (ABORT_UPDATE.equals(param)) {
			if (isInPreLaunchDelay()) {
				info.addPara("The " + noun + " was aborted in the planning stages", tc, initPad);
			} else {
				info.addPara("The " + forces + " have been defeated and scatter", tc, initPad);
			}
		} else if (FLEET_LAUNCH_UPDATE.equals(param)) {
			float untilDeparture = getETAUntil(TRAVEL_ACTION);
			float untilRaid = getETAUntil(PAYLOAD_ACTION);
			info.addPara("Fleet deployment in progress", tc, initPad);
			initPad = 0f;
			if (untilDeparture > 0) {
				addETABulletPoints(null, null, false, untilDeparture, ETAType.DEPARTURE, info, tc, initPad);
			}
			if (untilRaid > 0 && getSource().getContainingLocation() != system) {
				addETABulletPoints(system.getNameWithLowercaseTypeShort(), s, false, untilRaid, ETAType.ARRIVING,
						   info, tc, initPad);
			}
		} else if (PREPARE_ACTION.equals(param)) {
			float untilRaid = getETAUntil(PAYLOAD_ACTION);
			addETABulletPoints(system.getNameWithLowercaseTypeShort(), s, true, untilRaid, ETAType.ARRIVING,
							   info, tc, initPad);
		} else if (TRAVEL_ACTION.equals(param)) {
			addArrivedBulletPoint(system.getNameWithLowercaseTypeShort(), s, info, tc, initPad);
		} else if (PAYLOAD_ACTION.equals(param)) {
			if (isSucceeded()) {
				info.addPara("The " + forces + " are withdrawing", tc, initPad);
			} else {
				if (isAborted()) {
					info.addPara("The " + forces + " have been defeated and scatter", tc, initPad);
				} else {
					info.addPara("The " + forces + " have failed to achieve their objective", tc, initPad);
				}
			}
		}
	}
	
	@Override
	protected boolean shouldSendIntelUpdateWhenActionFinished(FGAction action) {
		if (RETURN_ACTION.equals(action.getId())) return false;
		if (PAYLOAD_ACTION.equals(action.getId())) {
			// if it was aborted, will send an update on abort() instead
			return isSucceeded() || (isFailed() && !isAborted());
		}
		if (TRAVEL_ACTION.equals(action.getId())) {
			if (action instanceof FGTravelAction && (isAborted() || isFailed())) {
				return false; // already sent a notification on abort/fail
			}
			// no update for "arriving" when traveling from one planet in-system to another
			return getSource().getContainingLocation() != raidAction.getWhere();
		}
		
		return super.shouldSendIntelUpdateWhenActionFinished(action);
	}

	protected void addBasicDescription(TooltipMakerAPI info, float width, float height, float opad) {
		info.addImage(getFaction().getLogo(), width, 128, opad);
		
		StarSystemAPI system = raidAction.getWhere();
		
		String noun = getNoun();

		//String aOrAn = Misc.getAOrAnFor(noun);
		//info.addPara(Misc.ucFirst(aOrAn) + " %s " + noun + " against "
		info.addPara(Misc.ucFirst(faction.getPersonNamePrefixAOrAn()) + " %s " + noun + " against "
				+ "the " + system.getNameWithLowercaseTypeShort() + ".", opad,
			faction.getBaseUIColor(), faction.getPersonNamePrefix());
		
		
	}
	
	protected void addAssessmentSection(TooltipMakerAPI info, float width, float height, float opad) {
		Color h = Misc.getHighlightColor();
		
		FactionAPI faction = getFaction();
		
		List<MarketAPI> targets = params.raidParams.allowedTargets;
		
		String noun = getNoun();
		if (!isEnding() && !isSucceeded() && !isFailed()) {
			info.addSectionHeading("Assessment", 
					   faction.getBaseUIColor(), faction.getDarkUIColor(), Alignment.MID, opad);
			if (targets.isEmpty()) {
				info.addPara("There are no colonies for the " + noun + " to target in the system.", opad);
			} else {
				StarSystemAPI system = raidAction.getWhere();
				
				String forces = getForcesNoun();
				
				boolean potentialDanger = addStrengthDesc(info, opad, system, forces, 
											"the " + noun + " is unlikely to find success",
											"the outcome of the " + noun + " is uncertain",
											"the " + noun + " is likely to find success");

				if (potentialDanger) {
					String safe = "should be safe from the " + noun;
					String risk = "are at risk of being raided and losing stability:";
					String highlight = "losing stability:";
					if (params.raidParams.bombardment == BombardType.SATURATION) {
						risk = "are at risk of suffering a saturation bombardment resulting in catastrophic damage:";
						highlight = "catastrophic damage";
					} else if (params.raidParams.bombardment == BombardType.TACTICAL) {
						risk = "are at risk of suffering a tactical bombardment and having their military infrastructure disrupted:";
						highlight = "military infrastructure disrupted";
					} else if (!params.raidParams.disrupt.isEmpty()) {
						risk = "are at risk of being raided and having their operations severely disrupted";
						highlight = "operations severely disrupted";
					}
					if (getAssessmentRiskStringOverride() != null) {
						risk = getAssessmentRiskStringOverride();
					}
					if (getAssessmentRiskStringHighlightOverride() != null) {
						highlight = getAssessmentRiskStringHighlightOverride();
					}
					showMarketsInDanger(info, opad, width, system, targets, 
								safe, risk, highlight);
				}
			}
		}
	}
	
	protected String getAssessmentRiskStringOverride() {
		return null;
	}
	protected String getAssessmentRiskStringHighlightOverride() {
		return null;
	}
	
	
	protected void addPayloadActionStatus(TooltipMakerAPI info, float width, float height, float opad) {
		StarSystemAPI to = raidAction.getWhere();
		info.addPara("Conducting operations in the " +
				to.getNameWithLowercaseTypeShort() + ".", opad);
		
	}
	
	protected void addStatusSection(TooltipMakerAPI info, float width, float height, float opad) {
		FGAction curr = getCurrentAction();

		boolean showStatus = curr != null || isEnding() || isSucceeded();
		
		if (showStatus) {
			String noun = getNoun();
			String forces = getForcesNoun();
			info.addSectionHeading("Status", 
					   faction.getBaseUIColor(), faction.getDarkUIColor(), Alignment.MID, opad);
			if (isEnding() && !isSucceeded()) {
				if (isFailed() || isAborted()) {
					if (isFailedButNotDefeated()) {
						info.addPara("The " + forces + " are withdrawing.", opad);
					} else {
						info.addPara("The " + forces + " have been defeated and any "
								+ "remaining ships are retreating in disarray.", opad);
					}
				} else {
					info.addPara("The " + forces + " are withdrawing.", opad);
				}
			} else if (isEnding() || isSucceeded()) {
				info.addPara("The " + noun + " was successful and the " + forces + " are withdrawing.", opad);
			} else if (curr != null) {
				StarSystemAPI to = raidAction.getWhere();
				if (isInPreLaunchDelay()) {
					if (getSource().getMarket() != null) {
						BaseHubMission.addStandardMarketDesc("The " + noun + " is in the planning stages on", 
								getSource().getMarket(), info, opad);
						boolean mil = isSourceFunctionalMilitaryMarket();
						if (mil) {
							info.addPara("Disrupting the military facilities " + getSource().getMarket().getOnOrAt() + 
									" " + getSource().getMarket().getName() + " will abort the " + noun + ".", opad);
						}
					}
				} else if (PREPARE_ACTION.equals(curr.getId())) {
					if (getSource().getMarket() != null) {
						BaseHubMission.addStandardMarketDesc("Making preparations in orbit around", 
														getSource().getMarket(), info, opad);
					} else {
						info.addPara("Making preparations in orbit around " + getSource().getName() + ".", opad);
					}
				} else if (TRAVEL_ACTION.equals(curr.getId())) {
					if (getSource().getMarket() == null) {
						info.addPara("Traveling to the " +
								to.getNameWithLowercaseTypeShort() + ".", opad);
					} else {
						info.addPara("Traveling from " + getSource().getMarket().getName() + " to the " +
								to.getNameWithLowercaseTypeShort() + ".", opad);
					}
				} else if (RETURN_ACTION.equals(curr.getId())) {
					if (getSource().getMarket() == null) {
						info.addPara("Returning to their port of origin.", opad);
					} else {
						info.addPara("Returning to " + getSource().getMarket().getName() + " in the " +
								origin.getContainingLocation().getNameWithLowercaseTypeShort() + ".", opad);
					}
				} else if (PAYLOAD_ACTION.equals(curr.getId())) {
					addPayloadActionStatus(info, width, height, opad);
				}
			}
		}
	}
	
	public String getNoun() {
		if (params.noun != null) return params.noun;
		
		String noun = "raid";
		if (params.raidParams.bombardment != null) {
			noun = "attack";
		}		
		return noun;
	}
	
	public String getForcesNoun() {
		if (params.forcesNoun != null) return params.forcesNoun;
		
		String forces = "raiding forces";
		if (!getNoun().equals("raid")) {
			forces = "attacking forces";
		}
		return forces;
	}
	
	
	public String getBaseName() {
		return Misc.ucFirst(getFaction().getPersonNamePrefix()) + " " + Misc.ucFirst(getNoun());
	}
	
	public boolean isSucceeded() {
		return raidAction.getSuccessFraction() > 0f && raidAction.isActionFinished() && !isAborted();
	}
	
	public boolean isFailed() {
		return isAborted() || (raidAction.getSuccessFraction() <= 0f && raidAction.isActionFinished());
	}
	
	protected SectorEntityToken getSource() {
		return origin;
	}
	
	protected SectorEntityToken getDestination() {
		return raidAction.getWhere().getHyperspaceAnchor();
	}
	
	protected StarSystemAPI getTargetSystem() {
		return raidAction.getWhere();
	}

	public GenericPayloadAction getRaidAction() {
		return raidAction;
	}

	public FGTravelAction getTravelAction() {
		return travelAction;
	}

	public FGTravelAction getReturnAction() {
		return returnAction;
	}

	public FGWaitAction getWaitAction() {
		return waitAction;
	}
	
	@Override
	public String getCommMessageSound() {
		if (isSendingUpdate()) {
			return getSoundStandardUpdate();
		}
		if (params.playerTargeted) {
			return getSoundColonyThreat();
		}
		return super.getCommMessageSound();
	}
	
	public boolean isPlayerTargeted() {
		return params.playerTargeted;
	}

	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		return super.getIntelTags(map);
	}

	@Override
	public List<ArrowData> getArrowData(SectorMapAPI map) {
		if (isAborted() || isFailed() || isSucceeded() || isEnded() || isEnding()) {
			return null;
		}
		return super.getArrowData(map);
	}

	public GenericRaidParams getParams() {
		return params;
	}

	public void setOrigin(SectorEntityToken origin) {
		this.origin = origin;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		if (getCurrentAction() != null && GenericRaidFGI.PREPARE_ACTION.equals(getCurrentAction().getId()) ||
				getDelayRemaining() > 0) {
			return getSource();
		}
		return getDestination();
	}
	
	public boolean hasCustomRaidAction() {
		return false;
	}
	
	public void doCustomRaidAction(CampaignFleetAPI fleet, MarketAPI market, float raidStr) {
		
	}
}




