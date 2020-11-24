package com.fs.starfarer.api.impl.campaign.intel.bases;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI.EconomyUpdateListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.PersonBountyIntel.BountyResult;
import com.fs.starfarer.api.impl.campaign.intel.PersonBountyIntel.BountyResultType;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.PirateBaseRumorBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.intel.raid.PirateRaidActionStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.PirateRaidAssembleStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;
import com.fs.starfarer.api.impl.campaign.intel.raid.ReturnStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.TravelStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidDelegate;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.impl.campaign.procgen.MarkovNames;
import com.fs.starfarer.api.impl.campaign.procgen.MarkovNames.MarkovNameResult;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.AddedEntity;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class PirateBaseIntel extends BaseIntelPlugin implements EveryFrameScript, FleetEventListener,
																EconomyUpdateListener, RaidDelegate {
	
	public static enum PirateBaseTier {
		TIER_1_1MODULE,
		TIER_2_1MODULE,
		TIER_3_2MODULE,
		TIER_4_3MODULE,
		TIER_5_3MODULE,
	}
	
	public static Object BOUNTY_EXPIRED_PARAM = new Object();
	public static Object DISCOVERED_PARAM = new Object();
	
	public static class BaseBountyData {
		public float bountyElapsedDays = 0f;
		public float bountyDuration = 0;
		public float baseBounty = 0;
		public float repChange = 0;
		public FactionAPI bountyFaction = null;
	}
	
	public static Logger log = Global.getLogger(PirateBaseIntel.class);
	
	protected StarSystemAPI system;
	protected MarketAPI market;
	protected SectorEntityToken entity;
	
	protected float elapsedDays = 0f;
	protected float duration = 45f;
	
	protected BaseBountyData bountyData = null;
	
	protected PirateBaseTier tier;
	protected PirateBaseTier matchedStationToTier = null;
	
	protected IntervalUtil monthlyInterval = new IntervalUtil(20f, 40f);
	protected int raidTimeoutMonths = 0;
	
	public PirateBaseIntel(StarSystemAPI system, String factionId, PirateBaseTier tier) {
		this.system = system;
		this.tier = tier;
	
		market = Global.getFactory().createMarket(Misc.genUID(), "Pirate Base", 3);
		market.setSize(3);
		market.setHidden(true);
		
		market.setFactionId(Factions.PIRATES);
		
		market.setSurveyLevel(SurveyLevel.FULL);
		
		market.setFactionId(factionId);
		market.addCondition(Conditions.POPULATION_3);
		
		market.addIndustry(Industries.POPULATION);
		market.addIndustry(Industries.SPACEPORT);
		market.addIndustry(Industries.MILITARYBASE);
		
		market.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market.addSubmarket(Submarkets.SUBMARKET_BLACK);
		
		market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());
		
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.IN_ASTEROID_BELT, 10f);
		weights.put(LocationType.IN_ASTEROID_FIELD, 10f);
		weights.put(LocationType.IN_RING, 10f);
		weights.put(LocationType.IN_SMALL_NEBULA, 10f);
		weights.put(LocationType.GAS_GIANT_ORBIT, 10f);
		weights.put(LocationType.PLANET_ORBIT, 10f);
		WeightedRandomPicker<EntityLocation> locs = BaseThemeGenerator.getLocations(null, system, null, 100f, weights);
		EntityLocation loc = locs.pick();
		
		if (loc == null) {
			endImmediately();
			return;
		}
		
		AddedEntity added = BaseThemeGenerator.addNonSalvageEntity(system, loc, Entities.MAKESHIFT_STATION, factionId);
		
		if (added == null || added.entity == null) {
			endImmediately();
			return;
		}
		
		entity = added.entity;
		
		
		String name = generateName();
		if (name == null) {
			endImmediately();
			return;
		}
		
		market.setName(name);
		entity.setName(name);
		
		
//		boolean down = false;
//		if (entity.getOrbitFocus() instanceof PlanetAPI) {
//			PlanetAPI planet = (PlanetAPI) entity.getOrbitFocus();
//			if (!planet.isStar()) {
//				down = true;
//			}
//		}
//		if (down) {
//			BaseThemeGenerator.convertOrbitPointingDown(entity);
//		}
		BaseThemeGenerator.convertOrbitWithSpin(entity, -5f);
		
		market.setPrimaryEntity(entity);
		entity.setMarket(market);
		
		entity.setSensorProfile(1f);
		entity.setDiscoverable(true);
		entity.getDetectedRangeMod().modifyFlat("gen", 5000f);
		
		market.setEconGroup(market.getId());
		market.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true);
		
		market.reapplyIndustries();
		
		Global.getSector().getEconomy().addMarket(market, true);
		
		log.info(String.format("Added pirate base in [%s], tier: %s", system.getName(), tier.name()));
		
		Global.getSector().getIntelManager().addIntel(this, true);
		timestamp = null;
		
		Global.getSector().getListenerManager().addListener(this);
		Global.getSector().getEconomy().addUpdateListener(this);
		
		updateTarget();
		
//		if ((float) Math.random() > 0.067f) {
//			SectorEntityToken raidDest = Global.getSector().getEconomy().getMarket("yama").getPrimaryEntity();
//			startRaid(raidDest.getStarSystem(), getRaidFP());
//		}
		
		PortsideBarData.getInstance().addEvent(new PirateBaseRumorBarEvent(this));
	}
	
	@Override
	public boolean isHidden() {
		if (super.isHidden()) return true;
		//if (true) return false;
		return timestamp == null;
	}


	public float getRaidFP() {
		float base = getBaseRaidFP();
		return base * (0.75f + (float) Math.random() * 0.5f);
	}
	public float getBaseRaidFP() {
		float base = 100f;
		switch (tier) {
		case TIER_1_1MODULE: base = 100f; break;
		case TIER_2_1MODULE: base = 150f; break;
		case TIER_3_2MODULE: base = 250f; break;
		case TIER_4_3MODULE: base = 300f; break;
		case TIER_5_3MODULE: base = 450f; break;
		}
		return base * (0.75f + (float) Math.random() * 0.5f);
	}
	
	public void notifyRaidEnded(RaidIntel raid, RaidStageStatus status) {
		if (status == RaidStageStatus.SUCCESS) {
			raidTimeoutMonths = 0;
		} else {
			float base = getBaseRaidFP();
			float raidFP = raid.getAssembleStage().getOrigSpawnFP();
			raidTimeoutMonths += Math.round(raidFP / base) * 2;
		}
	}
	
	public void startRaid(StarSystemAPI target, float raidFP) {
		boolean hasTargets = false;
		for (MarketAPI curr : Misc.getMarketsInLocation(target)) {
			if (curr.getFaction().isHostileTo(getFactionForUIColors())) {
				hasTargets = true;
				break;
			}
		}
		
		if (!hasTargets) return;
		
		RaidIntel raid = new RaidIntel(target, getFactionForUIColors(), this);
		
		//float raidFP = 1000;
		float successMult = 0.5f;
		
		JumpPointAPI gather = null;
		List<JumpPointAPI> points = system.getEntities(JumpPointAPI.class);
		float min = Float.MAX_VALUE;
		for (JumpPointAPI curr : points) {
			float dist = Misc.getDistance(entity.getLocation(), curr.getLocation());
			if (dist < min) {
				min = dist;
				gather = curr;
			}
		}
		
		
		PirateRaidAssembleStage assemble = new PirateRaidAssembleStage(raid, gather, this);
		assemble.addSource(market);
		assemble.setSpawnFP(raidFP);
		assemble.setAbortFP(raidFP * successMult);
		raid.addStage(assemble);
		
		
		SectorEntityToken raidJump = RouteLocationCalculator.findJumpPointToUse(getFactionForUIColors(), target.getCenter());
		
		TravelStage travel = new TravelStage(raid, gather, raidJump, false);
		travel.setAbortFP(raidFP * successMult);
		raid.addStage(travel);
		
		PirateRaidActionStage action = new PirateRaidActionStage(raid, target);
		action.setAbortFP(raidFP * successMult);
		raid.addStage(action);
		
		raid.addStage(new ReturnStage(raid));
		
		boolean shouldNotify = raid.shouldSendUpdate();
		Global.getSector().getIntelManager().addIntel(raid, !shouldNotify);
//		if (!Misc.getMarketsInLocation(target, Factions.PLAYER).isEmpty() || true) {
//			Global.getSector().getIntelManager().addIntel(raid);
//		} else {
//			Global.getSector().getIntelManager().queueIntel(raid);
//		}
	}
	
	public StarSystemAPI getSystem() {
		return system;
	}

	protected String pickStationType() {
		WeightedRandomPicker<String> stations = new WeightedRandomPicker<String>();
		
		if (getFactionForUIColors().getCustom().has(Factions.CUSTOM_PIRATE_BASE_STATION_TYPES)) {
			try {
				JSONObject json = getFactionForUIColors().getCustom().getJSONObject(Factions.CUSTOM_PIRATE_BASE_STATION_TYPES);
				for (String key : JSONObject.getNames(json)) {
					stations.add(key, (float) json.optDouble(key, 0f));
				}
			} catch (JSONException e) {
				stations.clear();
			}
		}
		
		if (stations.isEmpty()) {
			stations.add(Industries.ORBITALSTATION, 5f);
			stations.add(Industries.ORBITALSTATION_MID, 3f);
			stations.add(Industries.ORBITALSTATION_HIGH, 1f);
		}
		
		//stations.add(Industries.STARFORTRESS, 100000f);
		return stations.pick();
	}
	
	protected Industry getStationIndustry() {
		for (Industry curr : market.getIndustries()) {
			if (curr.getSpec().hasTag(Industries.TAG_STATION)) {
				return curr;
			}
		}
		return null;
	}
	
	protected void updateStationIfNeeded() {
		if (matchedStationToTier == tier) return;
		
 		matchedStationToTier = tier;
		monthsAtCurrentTier = 0;
		
		Industry stationInd = getStationIndustry();
		
		String currIndId = null;
		if (stationInd != null) {
			currIndId = stationInd.getId();
			market.removeIndustry(stationInd.getId(), null, false);
			stationInd = null;
		}
		
		if (currIndId == null) {
			currIndId = pickStationType();
		}
		
		if (currIndId == null) return;
		
		market.addIndustry(currIndId);
		stationInd = getStationIndustry();
		if (stationInd == null) return;
		
		stationInd.finishBuildingOrUpgrading();
		

		CampaignFleetAPI fleet = Misc.getStationFleet(entity);
		if (fleet == null) return;
		
		List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
		if (members.size() < 1) return;
		
		fleet.inflateIfNeeded();
		
		FleetMemberAPI station = members.get(0);
		
		WeightedRandomPicker<Integer> picker = new WeightedRandomPicker<Integer>();
		int index = 1; // index 0 is station body
		for (String slotId : station.getVariant().getModuleSlots()) {
			ShipVariantAPI mv = station.getVariant().getModuleVariant(slotId);
			if (Misc.isActiveModule(mv)) {
				picker.add(index, 1f);
			}
			index++;
		}
		
		float removeMult = 0f;
		
		switch (tier) {
		case TIER_1_1MODULE:
		case TIER_2_1MODULE:
			removeMult = 0.67f;
			break;
		case TIER_3_2MODULE:
			removeMult = 0.33f;
			break;
		case TIER_4_3MODULE:
		case TIER_5_3MODULE:
			removeMult = 0;
			break;
		
		}
		
		int remove = Math.round(picker.getItems().size() * removeMult);
		if (remove < 1 && removeMult > 0) remove = 1;
		if (remove >= picker.getItems().size()) {
			remove = picker.getItems().size() - 1;
		}
		
		for (int i = 0; i < remove; i++) {
			Integer pick = picker.pickAndRemove();
			if (pick != null) {
				station.getStatus().setHullFraction(pick, 0f);
				station.getStatus().setDetached(pick, true);
				station.getStatus().setPermaDetached(pick, true);
			}
		}
	}
	
	public CampaignFleetAPI getAddedListenerTo() {
		return addedListenerTo;
	}



	protected CampaignFleetAPI addedListenerTo = null;
	@Override
	protected void advanceImpl(float amount) {
		//makeKnown();
		float days = Global.getSector().getClock().convertToDays(amount);
		//days *= 1000f;
		//Global.getSector().getCurrentLocation().getName()
		//entity.getContainingLocation().getName()
		if (getPlayerVisibleTimestamp() == null && entity.isInCurrentLocation() && isHidden()) {
			makeKnown();
			sendUpdateIfPlayerHasIntel(DISCOVERED_PARAM, false);
		}
		
		
		//System.out.println("Name: " + market.getName());
		
		if (!sentBountyUpdate && bountyData != null && 
				(Global.getSector().getIntelManager().isPlayerInRangeOfCommRelay() ||
						(!isHidden() && DebugFlags.SEND_UPDATES_WHEN_NO_COMM))) {
			makeKnown();
			sendUpdateIfPlayerHasIntel(bountyData, false);
			sentBountyUpdate = true;
		}
		
		CampaignFleetAPI fleet = Misc.getStationFleet(market);
		if (fleet != null && addedListenerTo != fleet) {
			if (addedListenerTo != null) {
				addedListenerTo.removeEventListener(this);
			}
			fleet.addEventListener(this);
			addedListenerTo = fleet;			
		}
		
		
		if (target != null) {
			if (getAffectedMarkets(target).isEmpty()) {
				clearTarget();
			}
		}
		
		if (DebugFlags.RAID_DEBUG) {
			days *= 100f;
		}
		
		monthlyInterval.advance(days);
		if (monthlyInterval.intervalElapsed()) {
//			if (targetPlayerColonies) {
//				System.out.println("wefwefwe");
//			}
			monthsWithSameTarget++;
			raidTimeoutMonths--;
			if (raidTimeoutMonths < 0) raidTimeoutMonths = 0;
			
			if ((monthsWithSameTarget > 6 && (float) Math.random() < 0.2f) || target == null) {
				updateTarget();
			}
			if (target != null && 
					(float) Math.random() < monthsWithSameTarget * 0.05f && 
					bountyData == null) {
				setBounty();
			}
			//if (target != null && (float) Math.random() < 0.2f && raidTimeoutMonths <= 0) {
			boolean allowRandomRaids = PirateBaseManager.getInstance().getDaysSinceStart() > Global.getSettings().getFloat("noPirateRaidDays");
			
			if (target != null && 
					(((float) Math.random() < 0.2f && allowRandomRaids) || 
							targetPlayerColonies) && raidTimeoutMonths <= 0) {
				startRaid(target, getRaidFP());
				raidTimeoutMonths = 2 + (int)Math.round((float) Math.random() * 3f);
			}
			
			checkForTierChange();
		}

//		if (bountyData == null && target != null) {
//			setBounty();
//		}
		
		if (bountyData != null) {
			boolean canEndBounty = !entity.isInCurrentLocation();
			bountyData.bountyElapsedDays += days;
			if (bountyData.bountyElapsedDays > bountyData.bountyDuration && canEndBounty) {
				endBounty();
			}
		}
		
		//elapsedDays += days;
//		if (elapsedDays >= duration && !isDone()) {
//			endAfterDelay();
//			boolean current = market.getContainingLocation() == Global.getSector().getCurrentLocation();
//			sendUpdateIfPlayerHasIntel(new Object(), !current);
//			return;
//		}
		
		updateStationIfNeeded();
	}
	
	protected void checkForTierChange() {
		if (bountyData != null) return;
		if (entity.isInCurrentLocation()) return;
		
		float minMonths = Global.getSettings().getFloat("pirateBaseMinMonthsForNextTier");
		if (monthsAtCurrentTier > minMonths) {
			float prob = (monthsAtCurrentTier - minMonths) * 0.1f;
			if ((float) Math.random() < prob) {
				PirateBaseTier next = getNextTier(tier);
				if (next != null) {
					tier = next;
					updateStationIfNeeded();
					monthsAtCurrentTier = 0;
					return;
				}
			}
		}
		
		monthsAtCurrentTier++;
	}
	
	protected PirateBaseTier getNextTier(PirateBaseTier tier) {
		switch (tier) {
		case TIER_1_1MODULE: return PirateBaseTier.TIER_2_1MODULE;
		case TIER_2_1MODULE: return PirateBaseTier.TIER_3_2MODULE;
		case TIER_3_2MODULE: return PirateBaseTier.TIER_4_3MODULE;
		case TIER_4_3MODULE: return PirateBaseTier.TIER_5_3MODULE;
		case TIER_5_3MODULE: return null;
		}
		return null;
	}
	
	protected PirateBaseTier getPrevTier(PirateBaseTier tier) {
		switch (tier) {
		case TIER_1_1MODULE: return null;
		case TIER_2_1MODULE: return PirateBaseTier.TIER_1_1MODULE;
		case TIER_3_2MODULE: return PirateBaseTier.TIER_2_1MODULE;
		case TIER_4_3MODULE: return PirateBaseTier.TIER_3_2MODULE;
		case TIER_5_3MODULE: return PirateBaseTier.TIER_4_3MODULE;
		}
		return null;
	}

	public void makeKnown() {
		makeKnown(null);
	}
	public void makeKnown(TextPanelAPI text) {
//		entity.setDiscoverable(null);
//		entity.setSensorProfile(null);
//		entity.getDetectedRangeMod().unmodify("gen");
		
		if (getPlayerVisibleTimestamp() == null) {
			Global.getSector().getIntelManager().removeIntel(this);
			Global.getSector().getIntelManager().addIntel(this, text == null, text);
		}
	}
	
	public float getTimeRemainingFraction() {
		float f = 1f - elapsedDays / duration;
		return f;
	}
	
	

	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		log.info(String.format("Removing pirate base at [%s]", system.getName()));
		Global.getSector().getListenerManager().removeListener(this);
		clearTarget();
		
		Global.getSector().getEconomy().removeMarket(market);
		Global.getSector().getEconomy().removeUpdateListener(this);
		Misc.removeRadioChatter(market);
		market.advance(0f);
	}
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
	}



	protected BountyResult result = null;
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (isEnding()) return;
		
		//CampaignFleetAPI station = Misc.getStationFleet(market); // null here since it's the skeleton station at this point
		if (addedListenerTo != null && fleet == addedListenerTo) {
			Misc.fadeAndExpire(entity);
			endAfterDelay();
			
			result = new BountyResult(BountyResultType.END_OTHER, 0, null);
			
			if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE && 
					param instanceof BattleAPI) {
				BattleAPI battle = (BattleAPI) param;
				if (battle.isPlayerInvolved()) {
					int payment = 0;
					if (bountyData != null) {
						payment = (int) (bountyData.baseBounty * battle.getPlayerInvolvementFraction());
					}
					if (payment > 0) {
						Global.getSector().getPlayerFleet().getCargo().getCredits().add(payment);
						
						CustomRepImpact impact = new CustomRepImpact();
						impact.delta = bountyData.repChange * battle.getPlayerInvolvementFraction();
						if (impact.delta < 0.01f) impact.delta = 0.01f;
						ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
								new RepActionEnvelope(RepActions.CUSTOM, 
										impact, null, null, false, true),
										bountyData.bountyFaction.getId());
						
						result = new BountyResult(BountyResultType.END_PLAYER_BOUNTY, payment, rep);
					} else {
						result = new BountyResult(BountyResultType.END_PLAYER_NO_REWARD, 0, null);
					}
				}
			}
			
			boolean sendUpdate = DebugFlags.SEND_UPDATES_WHEN_NO_COMM ||
			 					 result.type != BountyResultType.END_OTHER ||
			 					 Global.getSector().getIntelManager().isPlayerInRangeOfCommRelay();
			sendUpdate = true;
			if (sendUpdate) {
				sendUpdateIfPlayerHasIntel(result, false);
			}
			
			PirateBaseManager.getInstance().incrDestroyed();
			PirateBaseManager.markRecentlyUsedForBase(system);
		}
	}

	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		
	}
	
	public boolean runWhilePaused() {
		return false;
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
		
		
		if (bountyData != null && result == null) {
			if (getListInfoParam() != BOUNTY_EXPIRED_PARAM) {
				if (isUpdate || mode != ListInfoMode.IN_DESC) {
					FactionAPI faction = bountyData.bountyFaction;
					info.addPara("Bounty faction: " + faction.getDisplayName(), initPad, tc,
							faction.getBaseUIColor(), faction.getDisplayName());
					initPad = 0f;
				}
				info.addPara("%s reward", initPad, tc, h, Misc.getDGSCredits(bountyData.baseBounty));
				addDays(info, "remaining", bountyData.bountyDuration - bountyData.bountyElapsedDays, tc);
			}
		}
		
		if (result != null && bountyData != null) {
			switch (result.type) {
			case END_PLAYER_BOUNTY:
				info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(result.payment));
				CoreReputationPlugin.addAdjustmentMessage(result.rep.delta, bountyData.bountyFaction, null, 
						null, null, info, tc, isUpdate, 0f);
				break;
			case END_TIME:
				break;
			case END_OTHER:
				break;
			
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
	
	public String getSortString() {
		String base = Misc.ucFirst(getFactionForUIColors().getPersonNamePrefix());
		return base + " Base";
		//return "Pirate Base";
	}
	
	public String getName() {
		String base = Misc.ucFirst(getFactionForUIColors().getPersonNamePrefix());
		
		if (getListInfoParam() == bountyData && bountyData != null) {
			return base + " Base - Bounty Posted";
		} else if (getListInfoParam() == BOUNTY_EXPIRED_PARAM) {
			return base + " Base - Bounty Expired";
		}
		
		if (result != null) {
			if (result.type == BountyResultType.END_PLAYER_BOUNTY) {
				return base + " Base - Bounty Completed";
			} else if (result.type == BountyResultType.END_PLAYER_NO_REWARD) {
				return base + " Base - Destroyed";
			}
		}
		
		String name = market.getName();
		if (isEnding()) {
			//return "Base Abandoned - " + name;
			return base + " Base - Abandoned";
		}
		if (getListInfoParam() == DISCOVERED_PARAM) {
			return base + " Base - Discovered";
		}
		if (entity.isDiscoverable()) {
			return base + " Base - Exact Location Unknown";
		}
		return base + " Base - " + name;
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return market.getFaction();
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}
	
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		//info.addPara(getName(), c, 0f);
		
		//info.addSectionHeading(getName(), Alignment.MID, 0f);
		
		FactionAPI faction = market.getFaction();
		
		info.addImage(faction.getLogo(), width, 128, opad);
		
		String has = faction.getDisplayNameHasOrHave();
		
		info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + has + 
				" established a base in the " + 
				market.getContainingLocation().getNameWithLowercaseType() + ". " +
						"The base serves as a staging ground for raids against nearby colonies.",
				opad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
		
		if (!entity.isDiscoverable()) {
			switch (tier) {
			case TIER_1_1MODULE:
				info.addPara("It has very limited defensive capabilities " +
							"and is protected by a few fleets.", opad);
				break;
			case TIER_2_1MODULE:
				info.addPara("It has limited defensive capabilities " +
							"and is protected by a small number of fleets.", opad);
				break;
			case TIER_3_2MODULE:
				info.addPara("It has fairly well-developed defensive capabilities " +
							 "and is protected by a considerable number of fleets.", opad);
				break;
			case TIER_4_3MODULE:
				info.addPara("It has very well-developed defensive capabilities " +
						 	 "and is protected by a large number of fleets.", opad);
				break;
			case TIER_5_3MODULE:
				info.addPara("It has very well-developed defensive capabilities " +
					 	 	 "and is protected by a large number of fleets. Both the " +
					 	 	 "base and the fleets have elite-level equipment, at least by pirate standards.", opad);
				break;
			
			}
		} else {
			info.addPara("You have not yet discovered the exact location or capabilities of this base.", opad);
		}
			
		info.addSectionHeading("Recent events", 
							   faction.getBaseUIColor(), faction.getDarkUIColor(), Alignment.MID, opad);
			
		if (target != null && !getAffectedMarkets(target).isEmpty() && !isEnding()) {
			info.addPara("Pirates operating from this base have been targeting the " + 
					target.getNameWithLowercaseType() + ".", opad); 
		}
		
		if (bountyData != null) {
			info.addPara(Misc.ucFirst(bountyData.bountyFaction.getDisplayNameWithArticle()) + " " +
					bountyData.bountyFaction.getDisplayNameHasOrHave() + 
					" posted a bounty for the destruction of this base.",
					opad, bountyData.bountyFaction.getBaseUIColor(), 
					bountyData.bountyFaction.getDisplayNameWithArticleWithoutArticle());
			
			if (result != null && result.type == BountyResultType.END_PLAYER_BOUNTY) {
				info.addPara("You have successfully completed this bounty.", opad);
			}
			
			addBulletPoints(info, ListInfoMode.IN_DESC);
		}
		
		if (result != null) {
			if (result.type == BountyResultType.END_PLAYER_NO_REWARD) {
				info.addPara("You have destroyed this base.", opad);				
			} else if (result.type == BountyResultType.END_OTHER) {
				info.addPara("It is rumored that this base is no longer operational.", opad);				
			}
		}

	}
	
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "pirate_base");
		//return market.getFaction().getCrest();
	}
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		if (bountyData != null) {
			tags.add(Tags.INTEL_BOUNTY);
		}
		tags.add(Tags.INTEL_EXPLORATION);
		
		if (target != null && !Misc.getMarketsInLocation(target, Factions.PLAYER).isEmpty()) {
			tags.add(Tags.INTEL_COLONIES);
		}
		
		tags.add(market.getFactionId());
		if (bountyData != null) {
			tags.add(bountyData.bountyFaction.getId());
		}
		return tags;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		//return market.getPrimaryEntity();
		if (market.getPrimaryEntity().isDiscoverable()) {
			return system.getCenter();
		}
		return market.getPrimaryEntity();
	}


	
	
	
	protected String generateName() {
		MarkovNames.loadIfNeeded();
		
		MarkovNameResult gen = null;
		for (int i = 0; i < 10; i++) {
			gen = MarkovNames.generate(null);
			if (gen != null) {
				String test = gen.name;
				if (test.toLowerCase().startsWith("the ")) continue;
				String p = pickPostfix();
				if (p != null && !p.isEmpty()) {
					test += " " + p;
				}
				if (test.length() > 22) continue;
				
				return test;
			}
		}
		return null;
	}
	
	protected String pickPostfix() {
		WeightedRandomPicker<String> post = new WeightedRandomPicker<String>();
		post.add("Asylum");
		post.add("Astrome");
		post.add("Barrage");
		post.add("Briganderie");
		post.add("Camp");
		post.add("Cover");
		post.add("Citadel");
		post.add("Den");
		post.add("Donjon");
		post.add("Depot");
		post.add("Fort");
		post.add("Freehold");
		post.add("Freeport");
		post.add("Freehaven");
		post.add("Free Orbit");
		post.add("Galastat");
		post.add("Garrison");
		post.add("Harbor");
		post.add("Haven");
		post.add("Headquarters");
		post.add("Hideout");
		post.add("Hideaway");
		post.add("Hold");
		post.add("Lair");
		post.add("Locus");
		post.add("Main");
		post.add("Mine Depot");
		post.add("Nexus");
		post.add("Orbit");
		post.add("Port");
		post.add("Post");
		post.add("Presidio");
		post.add("Prison");
		post.add("Platform");
		post.add("Corsairie");
		post.add("Refuge");
		post.add("Retreat");
		post.add("Refinery");
		post.add("Shadow");
		post.add("Safehold");
		post.add("Starhold");
		post.add("Starport");
		post.add("Stardock");
		post.add("Sanctuary");
		post.add("Station");
		post.add("Spacedock");
		post.add("Tertiary");
		post.add("Terminus");
		post.add("Terminal");
		post.add("Tortuga");
		post.add("Ward");
		post.add("Warsat");
		return post.pick();
	}

	public void commodityUpdated(String commodityId) {
		CommodityOnMarketAPI com = market.getCommodityData(commodityId);
		int curr = 0;
		String modId = market.getId();
		StatMod mod = com.getAvailableStat().getFlatStatMod(modId);
		if (mod != null) {
			curr = Math.round(mod.value);
		}
		
		int a = com.getAvailable() - curr;
		int d = com.getMaxDemand();
		if (d > a) {
			//int supply = Math.max(1, d - a - 1);
			int supply = Math.max(1, d - a);
			com.getAvailableStat().modifyFlat(modId, supply, "Brought in by raiders");
		}
	}

	public void economyUpdated() {

		float fleetSizeBonus = 1f;
		float qualityBonus = 0f;
		int light = 0;
		int medium = 0;
		int heavy = 0;
		
		switch (tier) {
		case TIER_1_1MODULE:
			qualityBonus = 0f;
			fleetSizeBonus = 0.2f;
			break;
		case TIER_2_1MODULE:
			qualityBonus = 0.2f;
			fleetSizeBonus = 0.3f;
			light = 2;
			break;
		case TIER_3_2MODULE:
			qualityBonus = 0.3f;
			fleetSizeBonus = 0.4f;
			light = 2;
			medium = 1;
			break;
		case TIER_4_3MODULE:
			qualityBonus = 0.4f;
			fleetSizeBonus = 0.5f;
			light = 2;
			medium = 2;
			break;
		case TIER_5_3MODULE:
			qualityBonus = 0.5f;
			fleetSizeBonus = 0.75f;
			light = 2;
			medium = 2;
			heavy = 2;
			break;
		}
		
		market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).
									modifyFlatAlways(market.getId(), qualityBonus,
									"Development level");
		
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlatAlways(market.getId(),
									fleetSizeBonus, 
		  							"Development level");
		
		
		String modId = market.getId();
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).modifyFlat(modId, light);
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(modId, medium);
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(modId, heavy);
	}

	public boolean isEconomyListenerExpired() {
		return isEnded();
	}
	
	public MarketAPI getMarket() {
		return market;
	}


	protected void setBounty() {
		bountyData = new BaseBountyData();
		float base = 100000f;
		switch (tier) {
		case TIER_1_1MODULE:
			base = Global.getSettings().getFloat("pirateBaseBounty1");
			bountyData.repChange = 0.02f;
			break;
		case TIER_2_1MODULE:
			base = Global.getSettings().getFloat("pirateBaseBounty2");
			bountyData.repChange = 0.05f;
			break;
		case TIER_3_2MODULE:
			base = Global.getSettings().getFloat("pirateBaseBounty3");
			bountyData.repChange = 0.06f;
			break;
		case TIER_4_3MODULE:
			base = Global.getSettings().getFloat("pirateBaseBounty4");
			bountyData.repChange = 0.07f;
			break;
		case TIER_5_3MODULE:
			base = Global.getSettings().getFloat("pirateBaseBounty5");
			bountyData.repChange = 0.1f;
			break;
		}
		
		bountyData.baseBounty = base * (0.9f + (float) Math.random() * 0.2f);
		
		bountyData.baseBounty = (int)(bountyData.baseBounty / 10000) * 10000;
		
		
		WeightedRandomPicker<FactionAPI> picker = new WeightedRandomPicker<FactionAPI>();
		for (MarketAPI curr : Global.getSector().getEconomy().getMarkets(target)) {
			if (curr.getFaction().isPlayerFaction()) continue;
			if (affectsMarket(curr)) {
				picker.add(curr.getFaction(), (float) Math.pow(2f, curr.getSize()));
			}
		}
		
		FactionAPI faction = picker.pick();
		if (faction == null) {
			bountyData = null;
			return;
		}
		
		bountyData.bountyFaction = faction;
		bountyData.bountyDuration = 180f;
		bountyData.bountyElapsedDays = 0f;
		
		Misc.makeImportant(entity, "baseBounty");
		
		sentBountyUpdate = false;
//		makeKnown();
//		sendUpdateIfPlayerHasIntel(bountyData, false);
	}
	
	protected boolean sentBountyUpdate = false;
	protected void endBounty() {
		sendUpdateIfPlayerHasIntel(BOUNTY_EXPIRED_PARAM, false);
		bountyData = null;
		sentBountyUpdate = false;
		Misc.makeUnimportant(entity, "baseBounty");
	}
	
	protected int monthsWithSameTarget = 0;
	protected int monthsAtCurrentTier = 0;
	protected StarSystemAPI target = null;
	public void updateTarget() {
		StarSystemAPI newTarget = pickTarget();
		if (newTarget == target) return;

		clearTarget();
		
		target = newTarget;
		monthsWithSameTarget = 0;
		
		if (target != null) {
//			for (MarketAPI curr : Global.getSector().getEconomy().getMarkets(system)) {
//				curr.addCondition(Conditions.PIRATE_ACTIVITY, this);
//			}
			new PirateActivityIntel(target, this);
//			PirateActivityIntel intel = new PirateActivityIntel(target, this);
//			if (!isPlayerVisible()) {
//				Global.getSector().getIntelManager().queueIntel(intel);
//			} else {
//				Global.getSector().getIntelManager().addIntel(intel);
//			}
		}
	}
	
	public StarSystemAPI getTarget() {
		return target;
	}

	protected void clearTarget() {
		if (target != null) {
			target = null;
			monthsWithSameTarget = 0;
		}
	}
	
	public List<MarketAPI> getAffectedMarkets(StarSystemAPI system) {
		List<MarketAPI> result = new ArrayList<MarketAPI>();
		for (MarketAPI curr : Global.getSector().getEconomy().getMarkets(system)) {
			if (!affectsMarket(curr)) continue;
			result.add(curr);
		}
		return result;
	}
	
	public boolean affectsMarket(MarketAPI market) {
		if (market.isHidden()) return false;
		if (market.getFaction() == this.market.getFaction()) return false;
		return true;
	}
	
	
	public void setTargetPlayerColonies(boolean targetPlayerColonies) {
		this.targetPlayerColonies = targetPlayerColonies;
	}
	public boolean isTargetPlayerColonies() {
		return targetPlayerColonies;
	}
	public StarSystemAPI getForceTarget() {
		return forceTarget;
	}
	public void setForceTarget(StarSystemAPI forceTarget) {
		this.forceTarget = forceTarget;
	}
	protected boolean targetPlayerColonies = false;
	protected StarSystemAPI forceTarget = null;
	
	protected StarSystemAPI pickTarget() {
		
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>();
		boolean forceTargetIsValid = false;
		for (StarSystemAPI system : Global.getSector().getEconomy().getStarSystemsWithMarkets()) {
			float score = 0f;
			for (MarketAPI curr : Global.getSector().getEconomy().getMarkets(system)) {
				if (!affectsMarket(curr)) continue;
				if (targetPlayerColonies && !curr.getFaction().isPlayerFaction()) continue;
				
				if (system == forceTarget) {
					forceTargetIsValid = true;
				}
				if (curr.hasCondition(Conditions.PIRATE_ACTIVITY)) continue;
				
//				if (curr.getId().equals("jangala")) {
//					score += 10000000f;
//				}
				
				float w = curr.getSize();
				
				float dist = Misc.getDistance(curr.getPrimaryEntity(), market.getPrimaryEntity());
				float mult = 1f - Math.max(0f, dist - 20000f) / 20000f;
				if (mult < 0.1f) mult = 0.1f;
				if (mult > 1) mult = 1;
				
				if (!targetPlayerColonies && curr.getFaction().isPlayerFaction()) {
					if (dist > 20000) continue;
				}
				
				score += w * mult;
				
			}
			picker.add(system, score);
		}
		
		if (forceTargetIsValid) {
			return forceTarget;
		}
		
		return picker.pick();
	}
	
	public List<ArrowData> getArrowData(SectorMapAPI map) {
		if (target == null|| target == entity.getContainingLocation()) return null;
		
		List<ArrowData> result = new ArrayList<ArrowData>();
		
		SectorEntityToken entityFrom = entity;
		if (map != null) {
			SectorEntityToken iconEntity = map.getIntelIconEntity(this);
			if (iconEntity != null) {
				entityFrom = iconEntity;
			}
		}
		
		ArrowData arrow = new ArrowData(entityFrom, target.getCenter());
		arrow.color = getFactionForUIColors().getBaseUIColor();
		result.add(arrow);
		
		return result;
	}
	
	public float getAccessibilityPenalty() {
		switch (tier) {
		case TIER_1_1MODULE: return 0.1f;
		case TIER_2_1MODULE: return 0.2f;
		case TIER_3_2MODULE: return 0.3f;
		case TIER_4_3MODULE: return 0.4f;
		case TIER_5_3MODULE: return 0.5f;
		}
		return 0f;
	}
	
	public float getStabilityPenalty() {
		switch (tier) {
		case TIER_1_1MODULE: return 1f;
		case TIER_2_1MODULE: return 1f;
		case TIER_3_2MODULE: return 2f;
		case TIER_4_3MODULE: return 2f;
		case TIER_5_3MODULE: return 3f;
		}
		return 0f;
	}

	public PirateBaseTier getTier() {
		return tier;
	}

	public SectorEntityToken getEntity() {
		return entity;
	}

}










