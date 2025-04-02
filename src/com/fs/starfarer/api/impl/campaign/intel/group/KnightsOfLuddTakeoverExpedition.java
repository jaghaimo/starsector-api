package com.fs.starfarer.api.impl.campaign.intel.group;

import java.util.Random;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import com.fs.starfarer.api.impl.campaign.HasslePlayerScript;
import com.fs.starfarer.api.impl.campaign.NPCHassler;
import com.fs.starfarer.api.impl.campaign.econ.RecentUnrest;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.LuddicChurchHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.group.FGBlockadeAction.FGBlockadeParams;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;



public class KnightsOfLuddTakeoverExpedition extends BlockadeFGI implements EconomyTickListener {

	public static int STABILITY_PER_MONTH_FULL = 2;
	public static int STABILITY_PER_MONTH_PARTIAL = 1;
	
	public static float NUM_OTHER_FLEETS_MULT = 0.25f;
	
	public static final String STABILITY_UPDATE = "stability_update";
	public static final String TAKEOVER_UPDATE = "takeover_update";
	
	public static final String BLOCKADING = "$KOLT_isBlockading";
	
	public static final String KOLT_FLEET = "$KOLT_fleet";
	public static final String ARMADA = "$KOLT_armada";
	public static final String PICKET = "$KOLT_picket";
	
	
	public static String KEY = "$KOLT_ref";
	public static KnightsOfLuddTakeoverExpedition get() {
		return (KnightsOfLuddTakeoverExpedition) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	
	public KnightsOfLuddTakeoverExpedition(GenericRaidParams params, FGBlockadeParams blockadeParams) {
		super(params, blockadeParams);
		
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		Global.getSector().getListenerManager().addListener(this);
	}
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		Global.getSector().getMemoryWithoutUpdate().unset(KEY);
		Global.getSector().getListenerManager().removeListener(this);
	}
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
	}

	@Override
	protected CampaignFleetAPI createFleet(int size, float damage) {
		
		Random r = getRandom();
		
		Vector2f loc = origin.getLocationInHyperspace();
		
		FleetCreatorMission m = new FleetCreatorMission(r);
		
		m.beginFleet();
		
		m.createFleet(params.style, size, params.factionId, loc);
		
		if (size == 10) {
			m.triggerSetFleetDoctrineOther(5, 0);
			m.triggerSetFleetSize(FleetSize.MAXIMUM);
			m.triggerSetFleetSizeFraction(1.4f);
			
			m.triggerSetFleetQuality(FleetQuality.HIGHER);
		}
		
		
		m.triggerSetFleetFlag(KOLT_FLEET);
		
		m.setFleetSource(params.source);
		setFleetCreatorQualityFromRoute(m);
		m.setFleetDamageTaken(damage);
	
		m.triggerSetWarFleet();
		m.triggerMakeLowRepImpact();
		
		//m.triggerMakeHostile();
		m.triggerMakeAlwaysSpreadTOffHostility();
		
		if (size >= 8) {
			m.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
			m.triggerFleetAddCommanderSkill(Skills.TACTICAL_DRILLS, 1);
			m.triggerFleetAddCommanderSkill(Skills.CARRIER_GROUP, 1);
		} else {
			// only set during the action phase, not here
			//m.triggerSetFleetHasslePlayer(LuddicChurchHostileActivityFactor.HASSLE_REASON);
		}
		
		CampaignFleetAPI fleet = m.createFleet();
		
		if (fleet != null) {
			if (size >= 8) {
				setNeverStraggler(fleet);
			} else {
				fleet.addScript(new NPCHassler(fleet, getTargetSystem()));
				fleet.getMemoryWithoutUpdate().set(PICKET, true);
				
				fleet.setName("Knights of Ludd Watchers");
				fleet.setNoFactionInName(true);
			}
			
			if (size == 10) {
				fleet.setName("Knights of Ludd Holy Armada");
				fleet.setNoFactionInName(true);
				fleet.getMemoryWithoutUpdate().set(ARMADA, true);
				fleet.getCommander().setRankId(Ranks.SPACE_ADMIRAL);
			}
		}
		
		
		return fleet;
	}

	
	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		if (isSpawnedFleets()) {
			if (isEnded() || isEnding() || isAborted() || isCurrent(RETURN_ACTION)) {
				for (CampaignFleetAPI curr : getFleets()) {
					curr.getMemoryWithoutUpdate().set(BLOCKADING, false);
				}
				return;
			}
			
			if (isCurrent(PAYLOAD_ACTION)) {
				for (CampaignFleetAPI curr : getFleets()) {
					curr.getMemoryWithoutUpdate().set(BLOCKADING, true);
				}
			}
		}
	}

	@Override
	protected void periodicUpdate() {
		super.periodicUpdate();
		
		if ((isEnded() || isEnding() || isSucceeded() || isFailed() || isAborted())) {
			return;
		}
			
		if (HostileActivityEventIntel.get() == null) { // possible if the player has no colonies 
			abort();
			return;
		}
		
		MarketAPI target = blockadeParams.specificMarket;
		if (target != null && !target.hasCondition(Conditions.LUDDIC_MAJORITY)) {
			//setFailedButNotDefeated(true);
			finish(false);
			return;
		}
		
		FGAction action = getCurrentAction();
		if (action instanceof FGBlockadeAction) {
			MutableStatWithTempMods stat = HostileActivityEventIntel.get().getNumFleetsStat(getTargetSystem());
			stat.addTemporaryModMult(1f, "KOLBlockade", null, NUM_OTHER_FLEETS_MULT);
		}
		
		if (!isSpawnedFleets() || isSpawning()) return;
		
		int armada = 0;
		for (CampaignFleetAPI curr : getFleets()) {
			if (curr.getMemoryWithoutUpdate().getBoolean(ARMADA)) {
				armada++;
			}
		}
		
		if (armada <= 0) {
			abort();
		}
		
		if (action instanceof FGBlockadePlanetAction) {
			FGBlockadePlanetAction blockade = (FGBlockadePlanetAction) action;
			if (blockade.getPrimary() != null) {
				for (CampaignFleetAPI curr : getFleets()) {
					if (blockade.getPrimary().getContainingLocation() != curr.getContainingLocation()) {
						continue;
					}
					if (!curr.getMemoryWithoutUpdate().getBoolean(PICKET)) {
						curr.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED, true, 0.4f);
					}
				}
			}
		}
	}


	
	
	protected String getOfString() {
		return "targeting";
	}
	
	protected GenericPayloadAction createPayloadAction() {
		FGBlockadePlanetAction action = new FGBlockadePlanetAction(blockadeParams, params.payloadDays);
		action.setSuccessFractionOverride(0f);
		return action;
	}
	
	protected void applyBlockadeCondition() {
//		int str = getRelativeFGStrength(getTargetSystem());
//		if (str < 0) {
//			unapplyBlockadeCondition();
//			return;
//		}
//		
//		for (MarketAPI market : Misc.getMarketsInLocation(getTargetSystem(), blockadeParams.targetFaction)) {
//			if (!market.hasCondition(Conditions.BLOCKADED)) {
//				market.addCondition(Conditions.BLOCKADED, this);
//			}
//		}
	}
	
	protected void unapplyBlockadeCondition() {
//		for (MarketAPI market : Misc.getMarketsInLocation(getTargetSystem(), blockadeParams.targetFaction)) {
//			market.removeCondition(Conditions.BLOCKADED);
//		}
	}
	
	

	@Override
	protected void addUpdateBulletPoints(TooltipMakerAPI info, Color tc, Object param, ListInfoMode mode,
										float initPad) {
		
		Object p = getListInfoParam();
		if (STABILITY_UPDATE.equals(p)) {
			int penalty = getStabilityPenaltyPerMonth();
			MarketAPI target = blockadeParams.specificMarket;
			LabelAPI label = info.addPara(target.getName() + " stability reduced by %s", initPad, tc,
					Misc.getHighlightColor(), "" + penalty);
			label.setHighlightColors(target.getFaction().getBaseUIColor(), Misc.getHighlightColor());
			label.setHighlight(target.getName(), "" + penalty);
		} else if (TAKEOVER_UPDATE.equals(p)) {
			
		} else {
			super.addUpdateBulletPoints(info, tc, param, mode, initPad);
		}
	}


	protected void addTargetingBulletPoint(TooltipMakerAPI info, Color tc, Object param, ListInfoMode mode, float initPad) {
		MarketAPI target = blockadeParams.specificMarket;
//		StarSystemAPI system = raidAction.getWhere();
//		Color s = raidAction.getSystemNameHighlightColor();
		LabelAPI label = info.addPara("Targeting " + target.getName(), tc, initPad);
		label.setHighlightColors(target.getFaction().getBaseUIColor());
		label.setHighlight(target.getName());
	}
	
	@Override
	protected void addBasicDescription(TooltipMakerAPI info, float width, float height, float opad) {
		info.addImage(getFaction().getLogo(), width, 128, opad);
		
		MarketAPI target = blockadeParams.specificMarket;
		
		StarSystemAPI system = raidAction.getWhere();
		
		String noun = getNoun();
		
		LabelAPI label = info.addPara(
				Misc.ucFirst(faction.getPersonNamePrefixAOrAn()) + " %s " + noun + " " + getOfString() + " "
				+ target.getName() + " in the " + system.getNameWithLowercaseType() + ".", opad,
				faction.getBaseUIColor(), faction.getPersonNamePrefix());
		label.setHighlightColors(faction.getBaseUIColor(), target.getFaction().getBaseUIColor());
		label.setHighlight(faction.getPersonNamePrefix(), target.getName());
	}
	
	protected void addAssessmentSection(TooltipMakerAPI info, float width, float height, float opad) {
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		FactionAPI faction = getFaction();
		MarketAPI target = blockadeParams.specificMarket;
		
		String noun = getNoun();
		String forcesNoun = getForcesNoun();
		if (!isEnding() && !isSucceeded() && !isFailed()) {
			
			FactionAPI other = Global.getSector().getFaction(blockadeParams.targetFaction);
			boolean hostile = getFaction().isHostileTo(blockadeParams.targetFaction);
			
			info.addSectionHeading("Assessment", 
							faction.getBaseUIColor(), faction.getDarkUIColor(), Alignment.MID, opad);
			
			boolean started = isCurrent(PAYLOAD_ACTION);
			float remaining = getETAUntil(PAYLOAD_ACTION, true) - getETAUntil(TRAVEL_ACTION, true);
			if (remaining > 0 && remaining < 1) remaining = 1;
			String days = (int)remaining == 1 ? "day" : "days";
			
			if (started) days = "more " + days;
			
			LabelAPI label = info.addPara("The operation will last for approximately %s " + days
					+ ", causing a progressive loss of stability " + target.getOnOrAt() + 
					" %s. If stability reaches zero, %s will permanently fall under %s control." ,
					opad, h,
					"" + (int) remaining,
					target.getName(), target.getName(),
					faction.getDisplayName());
			label.setHighlight("" + (int) remaining, 
								target.getName(), target.getName(),
								"will permanently fall",
								faction.getDisplayName());
			label.setHighlightColors(h, other.getBaseUIColor(), Misc.getTextColor(), 
					Misc.getNegativeHighlightColor(), faction.getBaseUIColor());
			//hostile = true;
			if (!hostile) {
				info.addPara("The " + forcesNoun + " are not nominally hostile, but will harass shipping and "
						+ "attempt to maintain control of the volume around the colony and undermine your "
						+ "authority.", opad, 
						Misc.getHighlightColor(), "not nominally hostile");
			} else {
				info.addPara("The " + forcesNoun + " are actively hostile, and will engage any orbital defenses, and "
						+ " conduct planetside operations to undermine your authority.", 
						opad, Misc.getNegativeHighlightColor(), "actively hostile");
			}
			
//			addStrengthDesc(info, opad, getTargetSystem(), forcesNoun, 
//					"the " + noun + " is unlikely to be effective",
//					"the " + noun + " is likely to only be partially effective",
//					"the " + noun + " is likely to be fully effective");
			
			addStrengthDesc(info, opad, target, forcesNoun, 
					"the colony is unlikely to be in danger",
					"the colony may be in danger",
					"the colony is in danger");
			
			addPostAssessmentSection(info, width, height, opad);
		}
	}
	
	@Override
	protected void addPostAssessmentSection(TooltipMakerAPI info, float width, float height, float opad) {
	}

	
	protected void addPayloadActionStatus(TooltipMakerAPI info, float width, float height, float opad) {
		StarSystemAPI to = raidAction.getWhere();
		info.addPara("Conducting operations in the " +
				to.getNameWithLowercaseTypeShort() + ".", opad);
		
		int penalty = getStabilityPenaltyPerMonth();
		MarketAPI target = blockadeParams.specificMarket;
		bullet(info);
		if (penalty > 0) {
			LabelAPI label = info.addPara(target.getName() + " stability: %s per month", opad,
					Misc.getHighlightColor(), "-" + penalty);
			label.setHighlightColors(target.getFaction().getBaseUIColor(), Misc.getHighlightColor());
			label.setHighlight(target.getName(), "-" + penalty);
		} else {
			info.addPara("%s stability unaffected", opad,
					target.getFaction().getBaseUIColor(), target.getName());

			unindent(info);
		}
		
	}


	public int getStabilityPenaltyPerMonth() {
		int str = getRelativeFGStrength(getTargetSystem());
		if (str < 0) {
			return 0;
		} else if (str == 0) {
			return STABILITY_PER_MONTH_PARTIAL;
		} else {
			return STABILITY_PER_MONTH_FULL;
		}
	}
	
	public void reportEconomyTick(int iterIndex) {
		// do here so that it's not synched up with the month-end income report etc
		if (iterIndex == 0) {
			if (!isCurrent(PAYLOAD_ACTION)) return;
			
			MarketAPI target = blockadeParams.specificMarket;
			
			int penalty = getStabilityPenaltyPerMonth();
			if (penalty > 0) {
				RecentUnrest.get(target).add(penalty, "Luddic Church takeover operation");
				target.reapplyConditions();
				sendUpdateIfPlayerHasIntel(STABILITY_UPDATE, false);
			}
			
			if (target.getStabilityValue() <= 0) {
				performTakeover(false);
			}
		}
	}


	public void reportEconomyMonthEnd() {
		
	}
	
	protected boolean voluntary;
	public void performTakeover(boolean voluntary) {
		this.voluntary = voluntary;
		MarketAPI target = blockadeParams.specificMarket;
		target.setFactionId(Factions.LUDDIC_CHURCH);
		target.setPlayerOwned(false);
		target.setAdmin(null);
		target.setFreePort(false);
		target.setUseStockpilesForShortages(false);
		
		for (SectorEntityToken curr : target.getConnectedEntities()) {
			curr.setFaction(Factions.LUDDIC_CHURCH);
		}
		
		if (!target.hasSubmarket(Submarkets.SUBMARKET_OPEN)) {
			target.addSubmarket(Submarkets.SUBMARKET_OPEN);
		}
		if (!target.hasSubmarket(Submarkets.SUBMARKET_BLACK)) {
			target.addSubmarket(Submarkets.SUBMARKET_BLACK);
		}
		if (Misc.isMilitary(target) || 
				target.hasIndustry(Industries.MILITARYBASE) || 
				target.hasIndustry(Industries.HIGHCOMMAND)) {
			if (!target.hasSubmarket(Submarkets.GENERIC_MILITARY)) {
				target.addSubmarket(Submarkets.GENERIC_MILITARY);
			}
		}
		
		RecentUnrest.get(target).setPenalty(0);
		
		
		if (getCurrentAction() instanceof FGBlockadePlanetAction) {
			FGBlockadePlanetAction action = (FGBlockadePlanetAction) getCurrentAction();
			action.setSuccessFractionOverride(1f);
			action.setActionFinished(true);
		}
		
		
		if (target.getStarSystem() != null) {
			for (CampaignFleetAPI fleet : target.getStarSystem().getFleets()) {
				MemoryAPI mem = fleet.getMemoryWithoutUpdate();
				String type = mem.getString(MemFlags.HASSLE_TYPE);
				if (LuddicChurchHostileActivityFactor.HASSLE_REASON.equals(type)) {
					mem.unset(MemFlags.WILL_HASSLE_PLAYER);
					mem.unset(MemFlags.HASSLE_TYPE);
					mem.set(HasslePlayerScript.HASSLE_COMPLETE_KEY, true);
					fleet.removeScriptsOfClass(NPCHassler.class);
				}
			}
		}
	}

	public String getCommMessageSound() {
		if (isSendingUpdate() && isSucceeded() && 
				isCurrent(RETURN_ACTION) && !voluntary) {
			return Sounds.REP_LOSS;
		}
		return super.getCommMessageSound();
	}	
}




