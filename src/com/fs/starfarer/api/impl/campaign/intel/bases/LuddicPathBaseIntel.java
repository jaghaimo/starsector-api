package com.fs.starfarer.api.impl.campaign.intel.bases;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI.EconomyUpdateListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
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
import com.fs.starfarer.api.impl.campaign.intel.bar.events.LuddicPathBaseBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel.BaseBountyData;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;
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

public class LuddicPathBaseIntel extends BaseIntelPlugin implements EveryFrameScript, FleetEventListener,
																EconomyUpdateListener, RaidDelegate {
	
	public static Object BOUNTY_EXPIRED_PARAM = new Object();
	public static Object DISCOVERED_PARAM = new Object();
	
	public static Logger log = Global.getLogger(LuddicPathBaseIntel.class);
	
	protected StarSystemAPI system;
	protected MarketAPI market;
	protected SectorEntityToken entity;
	
	protected float elapsedDays = 0f;
	protected float duration = 45f;
	
	protected BaseBountyData bountyData = null;
	
	protected IntervalUtil monthlyInterval = new IntervalUtil(20f, 40f);
	protected int monthsNoBounty = 0;
	
	protected boolean large = false;
	
	protected Random random = new Random();
	
	public LuddicPathBaseIntel(StarSystemAPI system, String factionId) {
		this.system = system;
	
		market = Global.getFactory().createMarket(Misc.genUID(), "Luddic Path Base", 3);
		market.setSize(3);
		market.setHidden(true);
		
		market.setFactionId(Factions.LUDDIC_PATH);
		
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
		
		BaseThemeGenerator.convertOrbitWithSpin(entity, -5f);
		
		market.setPrimaryEntity(entity);
		entity.setMarket(market);
		
		entity.setSensorProfile(1f);
		entity.setDiscoverable(true);
		entity.getDetectedRangeMod().modifyFlat("gen", 5000f);
		
		market.setEconGroup(market.getId());
		
		market.reapplyIndustries();
		
		Global.getSector().getEconomy().addMarket(market, true);
		
		Global.getSector().getIntelManager().addIntel(this, true);
		if (!DebugFlags.PATHER_BASE_DEBUG) {
			timestamp = null;
		}
		
		Global.getSector().getListenerManager().addListener(this);
		Global.getSector().getEconomy().addUpdateListener(this);
		
		large = random.nextFloat() > 0.5f;
		updateStationIfNeeded(large);
		
		PortsideBarData.getInstance().addEvent(new LuddicPathBaseBarEvent(this));
		
		log.info(String.format("Added luddic path base in [%s], isLarge: %s", system.getName(), "" + large));
	}
	
	@Override
	public boolean isHidden() {
		//if (true) return false;
		if (super.isHidden()) return true;
		return timestamp == null;
	}

//	public float getRaidFP() {
//		float base = getBaseRaidFP();
//		return base * (0.75f + (float) Math.random() * 0.5f);
//	}
//	public float getBaseRaidFP() {
//		float base = 100f;
//		return base * (0.75f + (float) Math.random() * 0.5f);
//	}
//	
	public void notifyRaidEnded(RaidIntel raid, RaidStageStatus status) {
		if (status == RaidStageStatus.SUCCESS) {
		} else {
		}
	}
//	
//	public void startRaid(StarSystemAPI target, float raidFP) {
//		boolean hasTargets = false;
//		for (MarketAPI curr : Misc.getMarketsInLocation(target)) {
//			if (curr.getFaction().isHostileTo(getFactionForUIColors())) {
//				hasTargets = true;
//				break;
//			}
//		}
//		
//		if (!hasTargets) return;
//		
//		RaidIntel raid = new RaidIntel(target, getFactionForUIColors(), this);
//		
//		//float raidFP = 1000;
//		float successMult = 0.75f;
//		
//		JumpPointAPI gather = null;
//		List<JumpPointAPI> points = system.getEntities(JumpPointAPI.class);
//		float min = Float.MAX_VALUE;
//		for (JumpPointAPI curr : points) {
//			float dist = Misc.getDistance(entity.getLocation(), curr.getLocation());
//			if (dist < min) {
//				min = dist;
//				gather = curr;
//			}
//		}
//		
//		
//		PirateRaidAssembleStage assemble = new PirateRaidAssembleStage(raid, gather, this);
//		assemble.addSource(market);
//		assemble.setSpawnFP(raidFP);
//		assemble.setAbortFP(raidFP * successMult);
//		raid.addStage(assemble);
//		
//		
//		SectorEntityToken raidJump = RouteLocationCalculator.findJumpPointToUse(getFactionForUIColors(), target.getCenter());
//		
//		TravelStage travel = new TravelStage(raid, gather, raidJump, false);
//		travel.setAbortFP(raidFP * successMult * successMult);
//		raid.addStage(travel);
//		
//		PirateRaidActionStage action = new PirateRaidActionStage(raid, target);
//		action.setAbortFP(raidFP * successMult * successMult * successMult);
//		raid.addStage(action);
//		
//		raid.addStage(new ReturnStage(raid));
//		
//		if (!Misc.getMarketsInLocation(target, Factions.PLAYER).isEmpty()) {
//			Global.getSector().getIntelManager().addIntel(raid);
//		} else {
//			Global.getSector().getIntelManager().queueIntel(raid);
//		}
//	}
	
	public StarSystemAPI getSystem() {
		return system;
	}

	protected String pickStationType(boolean large) {
		WeightedRandomPicker<String> stations = new WeightedRandomPicker<String>();
		
		//large = true;
		
		try {
			JSONObject json = getFactionForUIColors().getCustom().getJSONObject(Factions.CUSTOM_PATHER_BASES_SMALL);
			if (large) json = getFactionForUIColors().getCustom().getJSONObject(Factions.CUSTOM_PATHER_BASES_LARGE);
				for (String key : JSONObject.getNames(json)) {
					stations.add(key, (float) json.optDouble(key, 0f));
				}
			if (stations.isEmpty()) {
				stations.add(Industries.ORBITALSTATION, 5f);
			}
		} catch (JSONException e) {
			stations.clear();
		}
		
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
	
	protected void updateStationIfNeeded(boolean large) {
		Industry stationInd = getStationIndustry();
		
		String currIndId = null;
		if (stationInd != null) {
			currIndId = stationInd.getId();
			market.removeIndustry(stationInd.getId(), null, false);
			stationInd = null;
		}
		
		if (currIndId == null) {
			currIndId = pickStationType(large);
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
	}
	
	
	protected CampaignFleetAPI addedListenerTo = null;
	@Override
	protected void advanceImpl(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		//days *= 1000f;
		//Global.getSector().getCurrentLocation().getName()
		//entity.getContainingLocation().getName()
		if (getPlayerVisibleTimestamp() == null && entity.isInCurrentLocation() && isHidden()) {
			makeKnown();
			sendUpdateIfPlayerHasIntel(DISCOVERED_PARAM, false);
		}
		
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
		
		monthlyInterval.advance(days);
		if (monthlyInterval.intervalElapsed()) {
			if (bountyData == null && random.nextFloat() < Math.min(0.3f, monthsNoBounty * 0.02f)) {
				setBounty();
			} else {
				monthsNoBounty++;
			}
		}

//		if (bountyData == null) {
//			setBounty();
//		}
		
		if (bountyData != null) {
			boolean canEndBounty = !entity.isInCurrentLocation();
			bountyData.bountyElapsedDays += days;
			if (bountyData.bountyElapsedDays > bountyData.bountyDuration && canEndBounty) {
				endBounty();
			}
		}
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
		log.info(String.format("Removing luddic path base at [%s]", system.getName()));
		Global.getSector().getListenerManager().removeListener(this);
		
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
			
			for (LuddicPathCellsIntel cell : LuddicPathCellsIntel.getCellsForBase(this, false)) {
				cell.makeSleeper(Global.getSettings().getFloat("patherCellDisruptionDuration"));
				if (cell.getMarket().isPlayerOwned() || DebugFlags.PATHER_BASE_DEBUG) {
					cell.sendUpdateIfPlayerHasIntel(LuddicPathCellsIntel.UPDATE_DISRUPTED, false);
				}
			}
			
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

		FactionAPI faction = market.getFaction();
		
		info.addImage(faction.getLogo(), width, 128, opad);
		
		String has = faction.getDisplayNameHasOrHave();
		
		info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + has + 
				" established a base in the " + 
				market.getContainingLocation().getNameWithLowercaseType() + ". " +
						"The base serves to provide material support to active Pather cells on nearby colonies, enabling them " +
						"to cause widespread damage and destruction.",
				opad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
		
		if (!entity.isDiscoverable()) {
			if (large) {
				info.addPara("It has well-developed defensive capabilities " +
						 	 "and is protected by a large number of fleets.", opad);
			} else {
				info.addPara("It has extremely well-developed defensive capabilities " +
					 	 	 "and is protected by a large number of fleets.", opad);
			
			}
		} else {
			info.addPara("You have not yet discovered the exact location or capabilities of this base.", opad);
		}
		info.addSectionHeading("Recent events", 
							   faction.getBaseUIColor(), faction.getDarkUIColor(), Alignment.MID, opad);
		
		
		List<LuddicPathCellsIntel> cells = LuddicPathCellsIntel.getCellsForBase(this, false);
		if (!cells.isEmpty()) {
			float initPad = opad;
		
			info.addPara("This base is known to be providing support to active Pather cells at the following colonies:", opad);
			for (LuddicPathCellsIntel intel : cells) {
				addMarketToList(info, intel.getMarket(), initPad);
				initPad = 0f;
			}
			initPad = 0f;
		} else {
			info.addPara("You do not know of any active pather cells this base might be providing support to.", opad);			
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
		return Global.getSettings().getSpriteName("intel", "pather_base");
		//return market.getFaction().getCrest();
	}
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		if (bountyData != null) {
			tags.add(Tags.INTEL_BOUNTY);
		}
		tags.add(Tags.INTEL_EXPLORATION);
		
//		if (target != null && !Misc.getMarketsInLocation(target, Factions.PLAYER).isEmpty()) {
//			tags.add(Tags.INTEL_COLONIES);
//		}
		
		for (LuddicPathCellsIntel cell : LuddicPathCellsIntel.getCellsForBase(this, true)) {
			if (cell.getMarket().isPlayerOwned()) {
				tags.add(Tags.INTEL_COLONIES);
				break;
			}
		}
		
		tags.add(market.getFactionId());
		if (bountyData != null) {
			tags.add(bountyData.bountyFaction.getId());
		}
		return tags;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
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
	
	private String pickPostfix() {
		WeightedRandomPicker<String> post = new WeightedRandomPicker<String>();
		post.add("Asylum");
		//post.add("Base"); -> otherwise intel title can look like this: "Luddic Path Base: Scrimshaw Base"
		post.add("Citadel");
		post.add("Hammer");
		post.add("Harbor");
		post.add("Haven");
		post.add("Hold");
		post.add("Locus");
		post.add("Nexus");
		post.add("Refuge");
		post.add("Sanctuary");
		post.add("Sanctum");
		post.add("Shadow");
		post.add("Shelter");
		post.add("Safehold");
		post.add("Terminus");
		post.add("Principle");
		post.add("Offering");
		post.add("Devotion");
		post.add("Atonement");
		post.add("Cleansing");
		post.add("Oblation");
		post.add("Sacrement");
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
			com.getAvailableStat().modifyFlat(modId, (d - a), "Brought in by smugglers");
		}
	}

	public void economyUpdated() {
		float qualityBonus = 0f;
		int light = 0;
		int medium = 0;
		int heavy = 0;
		
		if (large) {
			qualityBonus = 0.5f;
			light = 4;
			medium = 4;
			heavy = 3;
		} else {
			qualityBonus = 0f;
			light = 3;
			medium = 2;
			heavy = 1;
		}
		
		market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).
									modifyFlatAlways(market.getId(), qualityBonus,
									"Development level");
		
		float fleetSizeBonus = 0.5f;
		if (large) fleetSizeBonus = 1f;
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
		
		List<IntelInfoPlugin> bases = Global.getSector().getIntelManager().getIntel(LuddicPathBaseIntel.class);
		for (IntelInfoPlugin curr : bases) {
			LuddicPathBaseIntel intel = (LuddicPathBaseIntel) curr;
			if (intel != this && intel.bountyData != null) {
				return;
			}
		}
		
		bountyData = new BaseBountyData();
		float base = 100000f;
		if (large) {
			base = Global.getSettings().getFloat("luddicPathBaseBountyLarge");
			bountyData.repChange = 0.05f;
		} else {
			base = Global.getSettings().getFloat("luddicPathBaseBountySmall");
			bountyData.repChange = 0.1f;
		}
		
		bountyData.baseBounty = base * (0.9f + (float) Math.random() * 0.2f);
		bountyData.baseBounty = (int)(bountyData.baseBounty / 10000) * 10000;
		
		
		WeightedRandomPicker<FactionAPI> picker = new WeightedRandomPicker<FactionAPI>();
		for (LuddicPathCellsIntel cell : LuddicPathCellsIntel.getCellsForBase(this, false)) {
			FactionAPI faction = cell.getMarket().getFaction();
			//if (faction.isPlayerFaction()) continue;
			picker.add(faction, (float) Math.pow(2f, cell.getMarket().getSize()));
		}
		
		FactionAPI faction = picker.pick();
		// player faction is in picker to reduce bounties offered on cells that are already bothering the player
		if (faction == null || faction.isPlayerFaction()) {
			bountyData = null;
			return;
		}
		
		bountyData.bountyFaction = faction;
		bountyData.bountyDuration = 180f;
		bountyData.bountyElapsedDays = 0f;
		
		monthsNoBounty = 0;
		Misc.makeImportant(entity, "baseBounty");
		
		
//		makeKnown();
//		sendUpdateIfPlayerHasIntel(bountyData, false);
		sentBountyUpdate = false;
	}
	
	protected boolean sentBountyUpdate = false;
	protected void endBounty() {
		sendUpdateIfPlayerHasIntel(BOUNTY_EXPIRED_PARAM, false);
		bountyData = null;
		monthsNoBounty = 0;
		Misc.makeUnimportant(entity, "baseBounty");
		sentBountyUpdate = false;
	}
	
	
	public List<ArrowData> getArrowData(SectorMapAPI map) {
		return null;
	}

	public SectorEntityToken getEntity() {
		return entity;
	}

	
}












