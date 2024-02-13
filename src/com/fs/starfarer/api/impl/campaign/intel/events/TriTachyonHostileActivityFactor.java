package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.EconomyRouteData;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.Stage;
import com.fs.starfarer.api.impl.campaign.intel.events.TriTachyonStandardActivityCause.CompetitorData;
import com.fs.starfarer.api.impl.campaign.intel.events.ttcr.TTCRCommerceRaidersDestroyedFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ttcr.TTCRIndustryDisruptedFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ttcr.TTCRMercenariesDefeatedFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ttcr.TTCRPoints;
import com.fs.starfarer.api.impl.campaign.intel.events.ttcr.TTCRTradeFleetsDestroyedFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ttcr.TriTachyonCommerceRaiding;
import com.fs.starfarer.api.impl.campaign.intel.group.FGRaidAction.FGRaidType;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel.FGIEventListener;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI.GenericRaidParams;
import com.fs.starfarer.api.impl.campaign.intel.group.TTMercenaryAttack;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission.FleetStyle;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.ComplicationRepImpact;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

public class TriTachyonHostileActivityFactor extends BaseHostileActivityFactor 
							implements FGIEventListener, FleetEventListener, ColonyPlayerHostileActListener {

	public static String COUNTER_RAIDED_TRITACH = "$counterRaidedTriTach";
	public static String DEFEATED_MERC_ATTACK = "$defeatedTTMercAttack";
	public static String BRIBED_MERC_ATTACK = "$bribedTTMercAttack";
	
	public static String COMMERCE_RAIDER_FLEET = "$triTachCommerceRaider";
	
	public static float DEALT_WITH_MERC_PROGRESS_MULT = 0.25f;
	
	public static boolean isPlayerCounterRaidedTriTach() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(COUNTER_RAIDED_TRITACH);
	}
	public static void setPlayerCounterRaidedTriTach() {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(COUNTER_RAIDED_TRITACH, true);
	}
	
	public static boolean isPlayerDefeatedMercAttack() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(DEFEATED_MERC_ATTACK);
	}
	public static void setPlayerDefeatedMercAttack() {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(DEFEATED_MERC_ATTACK, true);
	}
	
	public static boolean isDealtWithMercAttack() {
		return isPlayerDefeatedMercAttack() || isPlayerBribedMercAttack();
	}
	
	/**
	 * This is actually set in rules, so: method never called
	 */
	public static boolean isPlayerBribedMercAttack() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(BRIBED_MERC_ATTACK);
	}
	public static void setPlayerBribedMercAttack() {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(BRIBED_MERC_ATTACK, true);
	}
	
	
	
	protected TimeoutTracker<Industry> recentlyDisrupted = new TimeoutTracker<Industry>();
	
	
	public TriTachyonHostileActivityFactor(HostileActivityEventIntel intel) {
		super(intel);
		
		Global.getSector().getListenerManager().addListener(this);
	}
	
	protected Object readResolve() {
		if (recentlyDisrupted == null) {
			recentlyDisrupted = new TimeoutTracker<Industry>();
		}
		return this;
	}
	
	public String getProgressStr(BaseEventIntel intel) {
		return "";
	}
	
	@Override
	public int getProgress(BaseEventIntel intel) {
		if (!checkFactionExists(Factions.TRITACHYON, true)) {
			return 0;
		}
		return super.getProgress(intel);
	}
	
	public String getDesc(BaseEventIntel intel) {
		return "Tri-Tachyon Corporation";
	}
	
	public String getNameForThreatList(boolean first) {
		return "Tri-Tachyon";
	}


	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) <= 0) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getFaction(Factions.TRITACHYON).getBaseUIColor();
	}

	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("Your independent polity has become enough of a presence in the Sector to "
						+ "start cutting into the profit margins of the Tri-Tachyon Corporation.", 0f);
				
				tooltip.addPara("Independent-flagged commerce raiders - little more than sanctioned pirates - "
						+ "have been sighted in your space, "
						+ "attacking trade fleets regardless of their factional allegiance.", opad);
				
				addDealtSectionToTooltip(tooltip, opad);
			}
		};
	}
	
	public static void addDealtSectionToTooltip(TooltipMakerAPI tooltip, float opad) {
		if (isDealtWithMercAttack()) {
			tooltip.addPara("You've dealt with the mercenary attack sent against you, and " +
					"this has considerably cooled the enthusiasm for continued aggression. " +
					"Commerce-raiding continues, but the event progress value is reduced by %s, "
					+ "and no further large-scale attacks are likely to be mounted.", opad,
					Misc.getHighlightColor(), Strings.X + DEALT_WITH_MERC_PROGRESS_MULT);
		}
	}

	public boolean shouldShow(BaseEventIntel intel) {
		return getProgress(intel) > 0;
	}


	public Color getNameColor(float mag) {
		if (mag <= 0f) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getFaction(Factions.TRITACHYON).getBaseUIColor();
	}
	
	
	@Override
	public int getMaxNumFleets(StarSystemAPI system) {
		return Global.getSettings().getInt("triTachyonMaxFleets");
	}
	
	public CampaignFleetAPI createFleet(StarSystemAPI system, Random random) {
	
		float f = intel.getMarketPresenceFactor(system);
		
		// even if magnitude is not factored in, if it's 0 fleets won't spawn
		// and its value affects the likelihood of tritach fleets spawning
		//getEffectMagnitude(system);
		
		int difficulty = 4 + (int) Math.round(f * 4f);
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = system.getLocation();
		String factionId = Factions.TRITACHYON;
		if (random.nextFloat() < 0.5f) {
			factionId = Factions.MERCENARY;
		}
		
		m.createQualityFleet(difficulty, factionId, loc);
		
		if (difficulty <= 5) {
			m.triggerSetFleetQuality(FleetQuality.SMOD_1);
		} else if (difficulty <= 7) {
			m.triggerSetFleetQuality(FleetQuality.SMOD_2);
		} else {
			m.triggerSetFleetQuality(FleetQuality.SMOD_3);
		}
		
		m.triggerSetFleetFaction(Factions.INDEPENDENT);
		m.triggerSetFleetType(FleetTypes.COMMERCE_RAIDERS);
		
		m.triggerSetPirateFleet();
		//m.triggerMakeHostile();
		m.triggerMakeNonHostileToFaction(Factions.TRITACHYON);
		m.triggerMakeHostileToAllTradeFleets();
		m.triggerMakeNonHostileToFaction(Factions.PIRATES);
		m.triggerMakeNoRepImpact();
		m.triggerFleetAllowLongPursuit();
		
		m.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
		m.triggerFleetAddCommanderSkill(Skills.ELECTRONIC_WARFARE, 1);
//		m.triggerFleetAddCommanderSkill(Skills.FLUX_REGULATION, 1);
//		m.triggerFleetAddCommanderSkill(Skills.PHASE_CORPS, 1);
//		m.triggerFleetAddCommanderSkill(Skills.CARRIER_GROUP, 1);
		
		m.triggerSetFleetFlag(COMMERCE_RAIDER_FLEET);
		
		int tugs = 0;
		if (Factions.MERCENARY.equals(factionId)) {
			tugs = random.nextInt(3);
		}
		
		m.triggerFleetMakeFaster(true, tugs, true);
		m.triggerSetFleetMaxShipSize(3);
		
		
		CampaignFleetAPI fleet = m.createFleet();

		return fleet;
	}
	

	
	
	public void addBulletPointForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
										 ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		Color c = Global.getSector().getFaction(Factions.TRITACHYON).getBaseUIColor();
		info.addPara("Impending Tri-Tachyon mercenary attack", initPad, tc, c, "Tri-Tachyon");
	}
	
	public void addBulletPointForEventReset(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
			ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		info.addPara("Tri-Tachyon mercenary attack averted", tc, initPad);
	}
	
	public void addStageDescriptionForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info) {
		float small = 0f;
		float opad = 10f;
		
		small = 8f;

		Color c = Global.getSector().getFaction(Factions.TRITACHYON).getBaseUIColor();
		
		Color h = Misc.getHighlightColor();
		info.addPara("You've received intel that the Tri-Tachyon Corporation is allocating funds to hire and "
				+ "equip a mercenary company to raid and disrupt your industrial base.",
				small, Misc.getNegativeHighlightColor(), "raid and disrupt your industrial base");

		info.addPara("If the mercenary attack is defeated, it will go a long way towards convincing "
				+ "the Tri-Tachyon Corporation to abandon its anti-competitive efforts.", 
				//opad, tri, "Tri-Tachyon Corporation");
				opad, h, "abandon its anti-competitive efforts");
		
		stage.beginResetReqList(info, true, "crisis", opad);
		info.addPara("The %s is convinced that its efforts are unprofitable", 0f, c, "Tri-Tachyon Corporation");
		stage.endResetReqList(info, false, "crisis", -1, -1);
		
		addBorder(info, Global.getSector().getFaction(Factions.TRITACHYON).getBaseUIColor());
	}
	
	
	public String getEventStageIcon(HostileActivityEventIntel intel, EventStageData stage) {
		return Global.getSector().getFaction(Factions.TRITACHYON).getCrest();
	}

	public TooltipCreator getStageTooltipImpl(final HostileActivityEventIntel intel, final EventStageData stage) {
		if (stage.id == Stage.HA_EVENT) {
			return getDefaultEventTooltip("Tri-Tachyon mercenary attack", intel, stage);
		}
		return null;
	}
	
	
	public float getEventFrequency(HostileActivityEventIntel intel, EventStageData stage) {
		if (stage.id == Stage.HA_EVENT) {
			
			if (isPlayerCounterRaidedTriTach() || getPrimaryTriTachyonSystem() == null) {
				return 0f;
			}
			
			if (isPlayerDefeatedMercAttack() || isPlayerBribedMercAttack()) {
				return 0f;
			}
			
			StarSystemAPI target = findExpeditionTarget(intel, stage);
			MarketAPI source = findExpeditionSource(intel, stage, target);
			if (target != null && source != null) {
				return 10f;
			}
		}
		return 0;
	}
	

	public void rollEvent(HostileActivityEventIntel intel, EventStageData stage) {
		HAERandomEventData data = new HAERandomEventData(this, stage);
		stage.rollData = data;
		intel.sendUpdateIfPlayerHasIntel(data, false);
	}
	
	public boolean fireEvent(HostileActivityEventIntel intel, EventStageData stage) {
		StarSystemAPI target = findExpeditionTarget(intel, stage);
		MarketAPI source = findExpeditionSource(intel, stage, target);
		
		if (source == null || target == null) {
			return false;
		}
	
		stage.rollData = null;
		return startMercenaryAttack(source, target, stage, intel, getRandomizedStageRandom(3));
	}
	
	
	public static StarSystemAPI findExpeditionTarget(HostileActivityEventIntel intel, EventStageData stage) {
		List<CompetitorData> data = TriTachyonStandardActivityCause.computePlayerCompetitionData();
		CountingMap<StarSystemAPI> counts = new CountingMap<StarSystemAPI>();
		
		for (CompetitorData curr : data) {
			for (MarketAPI market : curr.competitorProducers) {
				StarSystemAPI system = market.getStarSystem();
				if (system == null) continue;
				int weight = market.getCommodityData(curr.commodityId).getMaxSupply();
				counts.add(system, weight);
			}
		}
		
		return counts.getLargest();
	}
	
	public static MarketAPI findExpeditionSource(HostileActivityEventIntel intel, EventStageData stage, StarSystemAPI target) {
		if (getNortia() != null) return getNortia();
		
		
		CountingMap<MarketAPI> scores = new CountingMap<MarketAPI>();
		for (MarketAPI market : Misc.getFactionMarkets(Factions.TRITACHYON)) {
			int size = market.getSize();
			int weight = size;
			if (!Misc.isMilitary(market)) weight += size * 10;
			if (market.hasIndustry(Industries.ORBITALWORKS)) weight += size;
			if (market.hasIndustry(Industries.HEAVYINDUSTRY)) weight += size;
			
			scores.add(market, weight);
		}
		
		return scores.getLargest();
	}
	
	public static MarketAPI getNortia() {
		MarketAPI nortia = Global.getSector().getEconomy().getMarket("nortia");
		if (nortia == null || nortia.hasCondition(Conditions.DECIVILIZED)) {
			return null;
		}
		return nortia;
	}
	

	
	
	public void reportFGIAborted(FleetGroupIntel intel) {
		setPlayerDefeatedMercAttack();
		TriTachyonCommerceRaiding.addFactorCreateIfNecessary(new TTCRMercenariesDefeatedFactor(), null);
	}
	
	
	
	@Override
	public void notifyFactorRemoved() {
		Global.getSector().getListenerManager().removeListener(this);
	}

	public void notifyEventEnding() {
		notifyFactorRemoved();
	}


	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		float days = Misc.getDays(amount);
		recentlyDisrupted.advance(days);
	
//		if (!Global.getSector().getListenerManager().hasListener(this)) {
//			Global.getSector().getListenerManager().addListener(this);
//		}
		
//		System.out.println("LISTENERS:");
//		for (Object o : Global.getSector().getListenerManager().getListeners(Object.class)) {
//			System.out.println("Listener: " + o.getClass().getSimpleName() + " [" + o.hashCode() + "]");
//		}
//		System.out.println("-----");
//		System.out.println("-----");
//		System.out.println("-----");
//		System.out.println("-----");
		
		EventStageData stage = intel.getDataFor(Stage.HA_EVENT);
		if (stage != null && stage.rollData instanceof HAERandomEventData && 
				((HAERandomEventData)stage.rollData).factor == this) {
			if (isPlayerCounterRaidedTriTach()) {
				intel.resetHA_EVENT();
			}			
		}
		if (isPlayerCounterRaidedTriTach() && TTMercenaryAttack.get() != null) {
			TTMercenaryAttack.get().finish(false);
		}
	}
	
	
	public static StarSystemAPI getPrimaryTriTachyonSystem() {
		CountingMap<StarSystemAPI> counts = new CountingMap<StarSystemAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (!Factions.TRITACHYON.equals(market.getFactionId())) continue;
			StarSystemAPI system = market.getStarSystem();
			if (system == null) continue;
			int size = market.getSize();
			int weight = size;
			if (Misc.isMilitary(market)) weight += size;
			if (market.hasIndustry(Industries.ORBITALWORKS)) weight += size;
			if (market.hasIndustry(Industries.HEAVYINDUSTRY)) weight += size;
			
			counts.add(system, weight);
		}
		return counts.getLargest();
	}

	
	public boolean startMercenaryAttack(MarketAPI source, StarSystemAPI target, 
						EventStageData stage, HostileActivityEventIntel intel, Random random) {
		if (isPlayerCounterRaidedTriTach()) return false;
		if (source == null || target == null) return false;
		
		GenericRaidParams params = new GenericRaidParams(new Random(random.nextLong()), true);
		params.makeFleetsHostile = false; // will be made hostile when they arrive, not before
		params.source = source;
		
		params.prepDays = 21f + random.nextFloat() * 7f;
		params.payloadDays = 27f + 7f * random.nextFloat();
		
		params.raidParams.where = target;
		params.raidParams.type = FGRaidType.SEQUENTIAL;
		
		Set<String> disrupt = new LinkedHashSet<String>();
		for (MarketAPI market : Misc.getMarketsInLocation(target, Factions.PLAYER)) {
			params.raidParams.allowedTargets.add(market);
			params.raidParams.allowNonHostileTargets = true;
			for (Industry ind : market.getIndustries()) {
				if (ind.getSpec().hasTag(Industries.TAG_UNRAIDABLE)) continue;
				disrupt.add(ind.getId());
				
			}
		}
		
		params.raidParams.disrupt.addAll(disrupt);
		params.raidParams.raidsPerColony = Math.min(disrupt.size(), 4);
		if (disrupt.isEmpty()) {
			params.raidParams.raidsPerColony = 2;
		}
		
		if (params.raidParams.allowedTargets.isEmpty()) {
			return false;
		}
		
		params.factionId = Factions.INDEPENDENT;
		params.style = FleetStyle.QUALITY;
		params.repImpact = ComplicationRepImpact.NONE;
		
		
		float fleetSizeMult = 1f;
		
		float f = intel.getMarketPresenceFactor(target);
		
		float totalDifficulty = fleetSizeMult * 50f * (0.5f + 0.5f * f);

		totalDifficulty -= 10;
		params.fleetSizes.add(10); // first size 10 pick becomes the Operational Command
		
		while (totalDifficulty > 0) {
			int min = 3;
			int max = 8;
			
			//int diff = Math.round(StarSystemGenerator.getNormalRandom(random, min, max));
			int diff = min + random.nextInt(max - min + 1);
			
			params.fleetSizes.add(diff);
			totalDifficulty -= diff;
		}
		
		TTMercenaryAttack attack = new TTMercenaryAttack(params);
		attack.setListener(this);
		//attack.setPreFleetDeploymentDelay(30f + random.nextFloat() * 60f);
		//attack.setPreFleetDeploymentDelay(1f);
		Global.getSector().getIntelManager().addIntel(attack, false);		
		
		return true;
	}
	
	
	
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		
	}
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		if (isPlayerCounterRaidedTriTach()) return;
		
		if (!battle.isPlayerInvolved()) return;
		
		if (getProgress(null) <= 0 && TriTachyonCommerceRaiding.get() == null) return;
		
		int traderFP = 0;
		int raiderFP = 0;
		for (CampaignFleetAPI otherFleet : battle.getNonPlayerSideSnapshot()) {
			//if (!Global.getSector().getPlayerFaction().isHostileTo(otherFleet.getFaction())) continue;
			boolean trader = isTraderServingATTColony(otherFleet);
			boolean raider = isCommerceRaider(otherFleet);
			
			if (!trader && !raider) continue;
			
			int mult = 1;
			if (trader) mult = TTCRPoints.TRADE_FLEET_FP_MULT;
			
			for (FleetMemberAPI loss : Misc.getSnapshotMembersLost(otherFleet)) {
				int fp = loss.getFleetPointCost() * mult;
				if (trader) {
					traderFP += fp;
				} else if (raider) {
					raiderFP += fp;
				}
			}
		}
	
		if (traderFP > 0) {
			int points = computeTTCRProgressPoints(traderFP);
			if (points > 0) {
				TTCRTradeFleetsDestroyedFactor factor = new TTCRTradeFleetsDestroyedFactor(points);
				TriTachyonCommerceRaiding.addFactorCreateIfNecessary(factor, null);
			}
		}
		if (raiderFP > 0) {
			int points = computeTTCRProgressPoints(raiderFP);
			if (points > 0) {
				TTCRCommerceRaidersDestroyedFactor factor = new TTCRCommerceRaidersDestroyedFactor(points);
				TriTachyonCommerceRaiding.addFactorCreateIfNecessary(factor, null);
			}
		}
	}
	
	public static boolean isCommerceRaider(CampaignFleetAPI fleet) {
		return fleet.getMemoryWithoutUpdate().getBoolean(TriTachyonHostileActivityFactor.COMMERCE_RAIDER_FLEET);
	}
	public static boolean isTraderServingATTColony(CampaignFleetAPI fleet) {
		boolean trader = Misc.isTrader(fleet);
		boolean smuggler = Misc.isSmuggler(fleet);
		
		if (!trader && !smuggler) return false;
		
		RouteData route = RouteManager.getInstance().getRoute(EconomyFleetRouteManager.SOURCE_ID, fleet);
		if (route == null) return false;
		
		EconomyRouteData data = (EconomyRouteData) route.getCustom();
		if (data == null) return false;
		
		if (data.from != null && Factions.TRITACHYON.equals(data.from.getFactionId())) {
			return true;
		}
		if (data.to != null && Factions.TRITACHYON.equals(data.to.getFactionId())) {
			return true;
		}
			
		return false;
	}
	
	public static int computeTTCRProgressPoints(float fleetPointsDestroyed) {
		if (fleetPointsDestroyed <= 0) return 0;
		
		int points = Math.round(fleetPointsDestroyed / (float) TTCRPoints.FP_PER_POINT);
		if (points < 1) points = 1;
		return points;
	}
	
	public static int computeIndustryDisruptPoints(Industry ind) {
		float base = ind.getSpec().getDisruptDanger().disruptionDays;
		float per = TTCRPoints.BASE_POINTS_FOR_INDUSTRY_DISRUPT;
		
		float days = ind.getDisruptedDays();
		
		int points = (int) Math.round(days / base * per);
		if (points > TTCRPoints.MAX_POINTS_FOR_INDUSTRY_DISRUPT) {
			points = TTCRPoints.MAX_POINTS_FOR_INDUSTRY_DISRUPT;
		}
		return points;
	}
	
	public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market,
			TempData actionData, CargoAPI cargo) {
		
	}
	public void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData,
			Industry industry) {
		if (isPlayerCounterRaidedTriTach()) return;
		if (getProgress(null) <= 0 && TriTachyonCommerceRaiding.get() == null) return;
		
		if (market != null && Factions.TRITACHYON.equals(market.getFactionId())) {
			applyIndustryDisruptionToTTCR(industry, dialog);
		}
	}
	public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
		if (isPlayerCounterRaidedTriTach()) return;
		if (getProgress(null) <= 0 && TriTachyonCommerceRaiding.get() == null) return;
		
		if (market != null && Factions.TRITACHYON.equals(market.getFactionId())) {
			applyMassIndustryDisruptionToTTCR(market, dialog);
		}
	}
	public void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market,
			TempData actionData) {
		if (isPlayerCounterRaidedTriTach()) return;
		if (getProgress(null) <= 0 && TriTachyonCommerceRaiding.get() == null) return;
		
		if (market != null && Factions.TRITACHYON.equals(market.getFactionId())) {
			applyMassIndustryDisruptionToTTCR(market, dialog);
		}
	}
	
	public void applyMassIndustryDisruptionToTTCR(MarketAPI market, InteractionDialogAPI dialog) {
		if (isPlayerCounterRaidedTriTach()) return;
		if (getProgress(null) <= 0 && TriTachyonCommerceRaiding.get() == null) return;
		
		int points = 0;
		for (Industry industry : market.getIndustries()) {
			if (recentlyDisrupted.contains(industry)) continue;
			if (industry.getSpec().hasTag(Industries.TAG_UNRAIDABLE)) continue;
			
			int curr = computeIndustryDisruptPoints(industry);
			if (curr > 0) {
				points += curr;
				recentlyDisrupted.add(industry, industry.getDisruptedDays());
			}
		}
		
		if (points > 0) {
			TTCRIndustryDisruptedFactor factor = new TTCRIndustryDisruptedFactor(
					"Disrupted industries " + market.getOnOrAt() + " " + market.getName(), points);
			TriTachyonCommerceRaiding.addFactorCreateIfNecessary(factor, dialog);
		}
	}
	public void applyIndustryDisruptionToTTCR(Industry industry, InteractionDialogAPI dialog) {
		if (isPlayerCounterRaidedTriTach()) return;
		if (getProgress(null) <= 0 && TriTachyonCommerceRaiding.get() == null) return;
		
		if (!recentlyDisrupted.contains(industry)) {
			if (industry.getSpec().hasTag(Industries.TAG_UNRAIDABLE)) return;
			MarketAPI market = industry.getMarket();
			if (market == null) return;
			
			int points = computeIndustryDisruptPoints(industry);
			if (points > 0) {
				TTCRIndustryDisruptedFactor factor = new TTCRIndustryDisruptedFactor(
						industry.getCurrentName() + " " + market.getOnOrAt() + " " + market.getName() + 
						" disrupted", points);
				TriTachyonCommerceRaiding.addFactorCreateIfNecessary(factor, dialog);
				recentlyDisrupted.add(industry, industry.getDisruptedDays());
			}
		}
	}
	@Override
	public Color getNameColorForThreatList() {
		return super.getNameColorForThreatList();
	}
	
	
}




