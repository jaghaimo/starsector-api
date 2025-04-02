package com.fs.starfarer.api.impl.campaign.intel.events;

import java.util.List;
import java.util.Random;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.Stage;
import com.fs.starfarer.api.impl.campaign.intel.events.TriTachyonStandardActivityCause.CompetitorData;
import com.fs.starfarer.api.impl.campaign.intel.group.FGRaidAction.FGRaidType;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel.FGIEventListener;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI.GenericRaidParams;
import com.fs.starfarer.api.impl.campaign.intel.group.SindrianDiktatPunitiveExpedition;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission.FleetStyle;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.BombardType;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.Misc;

public class SindrianDiktatHostileActivityFactor extends BaseHostileActivityFactor 
							implements FGIEventListener {

	public static String DEFEATED_DIKTAT_ATTACK = "$defeatedDiktatAttack";
	public static String MADE_DIKTAT_DEAL = "$makeDiktatDeal";
	public static String BROKE_DIKTAT_DEAL = "$brokeDiktatDeal";
	
	public static String RAIDER_FLEET = "$diktatRaider";
	
	public static boolean isMadeDeal() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(MADE_DIKTAT_DEAL);
	}
	public static void setMadeDeal(boolean value) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(MADE_DIKTAT_DEAL, value);
	}
	
	public static boolean brokeDeal() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(BROKE_DIKTAT_DEAL);
	}
	public static void setBrokeDeal(boolean broke) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(BROKE_DIKTAT_DEAL, broke);
	}
	
	public static boolean isPlayerDefeatedDiktatAttack() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(DEFEATED_DIKTAT_ATTACK);
	}
	public static void setPlayerDefeatedDiktatAttack() {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(DEFEATED_DIKTAT_ATTACK, true);
	}
	
	public SindrianDiktatHostileActivityFactor(HostileActivityEventIntel intel) {
		super(intel);
		
		//Global.getSector().getListenerManager().addListener(this);
	}
	
	public String getProgressStr(BaseEventIntel intel) {
		return "";
	}
	
	public String getDesc(BaseEventIntel intel) {
		return "Sindrian Diktat";
	}
	
	public String getNameForThreatList(boolean first) {
		return "Sindrian Diktat";
	}


	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) <= 0) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getFaction(Factions.DIKTAT).getBaseUIColor();
	}

	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("You've attracted the attention of the Sindrian Diktat.", 0f);
				
				tooltip.addPara("Sindrian raider fleets have been sighted in your space, "
						+ "attacking trade fleets regardless of their factional allegiance.", opad);
			}
		};
	}

	public boolean shouldShow(BaseEventIntel intel) {
		return getProgress(intel) > 0;
	}

	@Override
	public int getProgress(BaseEventIntel intel) {
		if (!checkFactionExists(Factions.DIKTAT, true)) {
			return 0;
		}
		return super.getProgress(intel);
	}
	
	public Color getNameColor(float mag) {
		if (mag <= 0f) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getFaction(Factions.DIKTAT).getBaseUIColor();
	}
	
	
	@Override
	public int getMaxNumFleets(StarSystemAPI system) {
		return Global.getSettings().getInt("diktatMaxFleets");
	}
	
	public CampaignFleetAPI createFleet(StarSystemAPI system, Random random) {
	
		float f = intel.getMarketPresenceFactor(system);
		
		int difficulty = 4 + (int) Math.round(f * 4f);
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = system.getLocation();
		String factionId = Factions.DIKTAT;
		
		m.createQualityFleet(difficulty, factionId, loc);
		
		m.triggerSetFleetType(FleetTypes.RAIDER);
		
		m.triggerSetPirateFleet();
		m.triggerMakeHostile();
		m.triggerMakeNonHostileToFaction(Factions.DIKTAT);
		m.triggerMakeNonHostileToFaction(Factions.PIRATES);
		m.triggerMakeLowRepImpact();
		m.triggerFleetAllowLongPursuit();
		m.triggerMakeHostileToAllTradeFleets();
		m.triggerMakeEveryoneJoinBattleAgainst();
		
		m.triggerSetFleetFlag(RAIDER_FLEET);
		
		m.triggerFleetMakeFaster(true, 0, true);
		
		//m.triggerSetFleetMaxShipSize(3);
		
		
		CampaignFleetAPI fleet = m.createFleet();

		return fleet;
	}
	

	
	
	public void addBulletPointForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
										 ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		Color c = Global.getSector().getFaction(Factions.DIKTAT).getBaseUIColor();
		info.addPara("Impending Sindrian Diktat attack", initPad, tc, c, "Sindrian Diktat");
	}
	
	public void addBulletPointForEventReset(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
			ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		info.addPara("Sindrian Diktat attack averted", tc, initPad);
	}
	
	public void addStageDescriptionForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info) {
		float small = 0f;
		float opad = 10f;
		
		small = 8f;

		Color c = Global.getSector().getFaction(Factions.DIKTAT).getBaseUIColor();
		
		Color h = Misc.getHighlightColor();
		info.addPara("You've received intel that the Sindrian Diktat is planning an attack to "
				+ "saturation-bombard your fuel production facilities.",
				small, Misc.getNegativeHighlightColor(), "saturation-bombard");

//		LabelAPI label = info.addPara("If the attack is defeated, your standing with the Hegemony "
//				+ "and the independents will increase substantially, and the Diktat will likely abandon "
//				+ "further efforts against you. In addition, your ability to export fuel will be improved.", 
//				opad);
//		label.setHighlight("Hegemony", "independents", "increase substantially", "Diktat",
//				"ability to export fuel will be improved");
//		label.setHighlightColors(Global.getSector().getFaction(Factions.HEGEMONY).getBaseUIColor(),
//				Global.getSector().getFaction(Factions.INDEPENDENT).getBaseUIColor(),
//				Misc.getPositiveHighlightColor(),
//				Global.getSector().getFaction(Factions.DIKTAT).getBaseUIColor(),
//				Misc.getPositiveHighlightColor());
		
		LabelAPI label = info.addPara("If the attack is defeated the Diktat will likely abandon "
				+ "further efforts against you, and your ability to export fuel will be improved.", 
				opad);
		label.setHighlight("Diktat",
				"export fuel", "improved");
		label.setHighlightColors(
				Global.getSector().getFaction(Factions.DIKTAT).getBaseUIColor(),
				Misc.getPositiveHighlightColor(),
				Misc.getPositiveHighlightColor());
		
		c = Global.getSector().getFaction(Factions.DIKTAT).getBaseUIColor();
		stage.beginResetReqList(info, true, "crisis", opad);
		info.addPara("You go to %s and make an agreement with the Diktat", 0f, c, "Sindria");
		info.addPara("%s is tactically bombarded", 0f, c, "Sindria");
		info.addPara("Fuel production on %s is significantly disrupted", 0f, c, "Sindria");
		stage.endResetReqList(info, false, "crisis", -1, -1);
		
		addBorder(info, Global.getSector().getFaction(Factions.DIKTAT).getBaseUIColor());
	}
	
	
	public String getEventStageIcon(HostileActivityEventIntel intel, EventStageData stage) {
		return Global.getSector().getFaction(Factions.DIKTAT).getCrest();
	}

	public TooltipCreator getStageTooltipImpl(final HostileActivityEventIntel intel, final EventStageData stage) {
		if (stage.id == Stage.HA_EVENT) {
			return getDefaultEventTooltip("Sindrian Diktat attack", intel, stage);
		}
		return null;
	}
	
	public static Industry getSindrianFuelProd() {
		MarketAPI sindria = getSindria(false);
		if (sindria == null) return null;
		
		Industry prod = sindria.getIndustry(Industries.FUELPROD);
		return prod;
	}
	
	public static MarketAPI getSindria(boolean requireMilitaryBase) {
		MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
		if (sindria == null || sindria.hasCondition(Conditions.DECIVILIZED) ||
				!sindria.getFactionId().equals(Factions.DIKTAT)) {
			return null;
		}
		if (requireMilitaryBase) {
			Industry b = sindria.getIndustry(Industries.MILITARYBASE);
			if (b == null) b = sindria.getIndustry(Industries.HIGHCOMMAND);
			if (b == null || b.isDisrupted() || !b.isFunctional()) {
				return null;
			}
		}
		return sindria;
	}
	
	
	public float getEventFrequency(HostileActivityEventIntel intel, EventStageData stage) {
		if (stage.id == Stage.HA_EVENT) {
			
			if (isPlayerDefeatedDiktatAttack() || getSindria(true) == null) {
				return 0f;
			}
			
			if (isMadeDeal()) return 0f;
			
			if (SindrianDiktatPunitiveExpedition.get() != null) {
				return 0f;
			}
			
			MarketAPI target = findExpeditionTarget(intel, stage);
			MarketAPI source = getSindria(true);
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
		MarketAPI target = findExpeditionTarget(intel, stage);
		MarketAPI source = getSindria(true);
		
		if (source == null || target == null) {
			return false;
		}
	
		stage.rollData = null;
		return startAttack(source, target, target.getStarSystem(), stage, getRandomizedStageRandom(3));
	}
	
	
	public static MarketAPI findExpeditionTarget(HostileActivityEventIntel intel, EventStageData stage) {
		List<CompetitorData> data = SindrianDiktatStandardActivityCause.computePlayerCompetitionData();
		CountingMap<MarketAPI> counts = new CountingMap<MarketAPI>();
		
		for (CompetitorData curr : data) {
			for (MarketAPI market : curr.competitorProducers) {
				StarSystemAPI system = market.getStarSystem();
				if (system == null) continue;
				int weight = market.getCommodityData(curr.commodityId).getMaxSupply();
				counts.add(market, weight);
			}
		}
		
		return counts.getLargest();
	}
	
	
	public void reportFGIAborted(FleetGroupIntel intel) {
		setPlayerDefeatedDiktatAttack();
		
		DiktatFuelBonusScript.grantBonus();
	}

	
	@Override
	public void notifyFactorRemoved() {
		//Global.getSector().getListenerManager().removeListener(this);
	}

	public void notifyEventEnding() {
		notifyFactorRemoved();
	}


	@Override
	public void advance(float amount) {
		super.advance(amount);
		
//		float days = Misc.getDays(amount);
		
//		if (!Global.getSector().getListenerManager().hasListener(this)) {
//			Global.getSector().getListenerManager().addListener(this);
//		}
		
//		String key = "$wdfwefwe";
//		if (!Global.getSector().getMemoryWithoutUpdate().getBoolean(key)) {
//			MarketAPI target = findExpeditionTarget(intel, null);
//			MarketAPI source = getSindria(true);
//			startAttack(source, target, target.getStarSystem(), null, new Random());
//			Global.getSector().getMemoryWithoutUpdate().set(key, true);
//		}
		
		EventStageData stage = intel.getDataFor(Stage.HA_EVENT);
		if (stage != null && stage.rollData instanceof HAERandomEventData && 
				((HAERandomEventData)stage.rollData).factor == this) {
			
			Industry prod = getSindrianFuelProd();
			boolean prodOk = prod != null && prod.getSpecialItem() != null && !prod.isDisrupted();
			if (getSindria(true) == null || !prodOk) {
				intel.resetHA_EVENT();
			}
		}

	}
	

	
	public boolean startAttack(MarketAPI source, MarketAPI target, StarSystemAPI system, EventStageData stage, Random random) {
		GenericRaidParams params = new GenericRaidParams(new Random(random.nextLong()), true);
		
		params.makeFleetsHostile = false; // will be made hostile when they arrive, not before
		
		params.factionId = source.getFactionId();
		params.source = source;
		
		params.prepDays = 14f + random.nextFloat() * 14f;
		params.payloadDays = 27f + 7f * random.nextFloat();
		
		params.raidParams.where = system;
		params.raidParams.type = FGRaidType.SEQUENTIAL;
		params.raidParams.tryToCaptureObjectives = false;
		params.raidParams.allowedTargets.add(target);
		params.raidParams.allowNonHostileTargets = true;
		params.raidParams.setBombardment(BombardType.SATURATION);
		
		params.style = FleetStyle.STANDARD;
		
		
		// standard Askonia fleet size multiplier with no shortages/issues is a bit over 230%
		float fleetSizeMult = source.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).computeEffective(0f);
		
		float f = intel.getMarketPresenceFactor(system);
		
		float totalDifficulty = fleetSizeMult * 15f * (0.6f + 0.4f * f);
		
		if (totalDifficulty < 15) {
			return false;
		}
		if (totalDifficulty > 100) {
			totalDifficulty = 100;
		}
		
		totalDifficulty -= 10;
		
		params.fleetSizes.add(10);
		
		while (totalDifficulty > 0) {
			int min = 6;
			int max = 10;
			
			//int diff = Math.round(StarSystemGenerator.getNormalRandom(random, min, max));
			int diff = min + random.nextInt(max - min + 1);
			
			params.fleetSizes.add(diff);
			totalDifficulty -= diff;
		}
		
		
		SindrianDiktatPunitiveExpedition punex = new SindrianDiktatPunitiveExpedition(params);
		punex.setListener(this);
		Global.getSector().getIntelManager().addIntel(punex);
		
//		GenericRaidFGI raid = new GenericRaidFGI(params);
//		raid.setListener(this);
//		Global.getSector().getIntelManager().addIntel(raid);
		
		return true;
	}
	
}




