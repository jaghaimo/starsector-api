package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.Stage;
import com.fs.starfarer.api.impl.campaign.intel.group.FGRaidAction.FGRaidType;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel.FGIEventListener;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI.GenericRaidParams;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission.FleetStyle;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.ComplicationRepImpact;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.BombardType;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class RemnantHostileActivityFactor extends BaseHostileActivityFactor 
							implements FGIEventListener {

	public RemnantHostileActivityFactor(HostileActivityEventIntel intel) {
		super(intel);
		
		//Global.getSector().getListenerManager().addListener(this);
	}
	
	public String getProgressStr(BaseEventIntel intel) {
		return "";
	}
	
	public String getDesc(BaseEventIntel intel) {
		return "Remnant";
	}
	
	public String getNameForThreatList(boolean first) {
		return "Remnant";
	}


	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) <= 0) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getFaction(Factions.REMNANTS).getBaseUIColor();
	}

	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("Remnant fleets have been sighted in your space.", opad);
			}
		};
	}

	public boolean shouldShow(BaseEventIntel intel) {
		return getProgress(intel) > 0;
	}

	@Override
	public int getProgress(BaseEventIntel intel) {
		return super.getProgress(intel);
	}
	
	public Color getNameColor(float mag) {
		if (mag <= 0f) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getFaction(Factions.REMNANTS).getBaseUIColor();
	}
	
	
	@Override
	public int getMaxNumFleets(StarSystemAPI system) {
		return 0; // being spawned normally already
	}
	
	public CampaignFleetAPI createFleet(StarSystemAPI system, Random random) {
	
//		float f = intel.getMarketPresenceFactor(system);
//		
//		int difficulty = 4 + (int) Math.round(f * 4f);
//		
//		FleetCreatorMission m = new FleetCreatorMission(random);
//		m.beginFleet();
//		
//		Vector2f loc = system.getLocation();
//		String factionId = Factions.REMNANTS;
//		
//		m.createQualityFleet(difficulty, factionId, loc);
//		
//		m.triggerSetFleetType(FleetTypes.RAIDER);
//		
//		m.triggerSetPirateFleet();
//		m.triggerMakeHostile();
//		m.triggerMakeNonHostileToFaction(Factions.REMNANTS);
//		m.triggerMakeHostileToAllTradeFleets();
//		m.triggerMakeNonHostileToFaction(Factions.PIRATES);
//		m.triggerMakeLowRepImpact();
//		m.triggerFleetAllowLongPursuit();
//		
//		m.triggerSetFleetFlag(RAIDER_FLEET);
//		
//		m.triggerFleetMakeFaster(true, 0, true);
//		
//		//m.triggerSetFleetMaxShipSize(3);
//		
//		
//		CampaignFleetAPI fleet = m.createFleet();
//
//		return fleet;
		
		return null;
	}
	

	
	
	public void addBulletPointForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
										 ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		Color c = Global.getSector().getFaction(Factions.REMNANTS).getBaseUIColor();
		info.addPara("Impending Remnant attack", initPad, tc, c, "Remnant");
	}
	
	public void addBulletPointForEventReset(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
			ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		info.addPara("Remnant attack averted", tc, initPad);
	}
	
	public void addStageDescriptionForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info) {
		float small = 0f;
		float opad = 10f;
		
		small = 8f;

		Color c = Global.getSector().getFaction(Factions.REMNANTS).getBaseUIColor();

		if (!(stage.rollData instanceof HAERandomEventData)) return;
		HAERandomEventData data = (HAERandomEventData) stage.rollData;
		if (!(data.custom instanceof MarketAPI)) return;
		
		MarketAPI target = (MarketAPI) data.custom;
		
		Color h = Misc.getHighlightColor();
		info.addPara("There are signs of activity at a Remnant Nexus in your system. Intel evaluates "
				+ "a high probability of an attack fleet being assembled to saturation-bombard " +
				target.getName() + ".",
				small, Misc.getNegativeHighlightColor(), "saturation-bombard");
		
		LabelAPI label = info.addPara("Defeating the attack fleet would only put off the problem, "
				+ "but destroying the Remnant Nexus will end the threat for good.", 
				opad);
		label.setHighlight("Remnant Nexus", "end the threat");
		label.setHighlightColors(
				Global.getSector().getFaction(Factions.REMNANTS).getBaseUIColor(),
				Misc.getPositiveHighlightColor());
		
		c = Global.getSector().getFaction(Factions.REMNANTS).getBaseUIColor();
		stage.beginResetReqList(info, true, "crisis", opad);
		info.addPara("The %s is destroyed", 0f, c, "Remnant Nexus");
		stage.endResetReqList(info, false, "crisis", -1, -1);
		
		addBorder(info, Global.getSector().getFaction(Factions.REMNANTS).getBaseUIColor());
	}
	
	
	public String getEventStageIcon(HostileActivityEventIntel intel, EventStageData stage) {
		return Global.getSector().getFaction(Factions.REMNANTS).getCrest();
	}

	public TooltipCreator getStageTooltipImpl(final HostileActivityEventIntel intel, final EventStageData stage) {
		if (stage.id == Stage.HA_EVENT) {
			return getDefaultEventTooltip("Remnant attack", intel, stage);
		}
		return null;
	}

	public static CampaignFleetAPI getRemnantNexus(StarSystemAPI system) {
		if (system == null) return null;
//		if (!system.hasTag(Tags.THEME_REMNANT_MAIN)) return null;
//		if (system.hasTag(Tags.THEME_REMNANT_DESTROYED)) return null;
			
		for (CampaignFleetAPI fleet : system.getFleets()) {
			if (!fleet.isStationMode()) continue;
			if (!Factions.REMNANTS.equals(fleet.getFaction().getId())) continue;
			
			return fleet;
		}
		return null;
	}
	
	
	public float getEventFrequency(HostileActivityEventIntel intel, EventStageData stage) {
		if (stage.id == Stage.HA_EVENT) {
			
			MarketAPI target = findAttackTarget(intel, stage);
			if (target == null || target.getStarSystem() == null) return 0f;
			
			CampaignFleetAPI nexus = getRemnantNexus(target.getStarSystem());
			if (nexus == null) return 0f;
			
			return 10f;
		}
		return 0f;
	}
	

	public void rollEvent(HostileActivityEventIntel intel, EventStageData stage) {
		MarketAPI target = findAttackTarget(intel, stage);
		if (target == null) return;
		
		HAERandomEventData data = new HAERandomEventData(this, stage);
		data.custom = target;
		stage.rollData = data;
		intel.sendUpdateIfPlayerHasIntel(data, false);
	}
	
	public boolean fireEvent(HostileActivityEventIntel intel, EventStageData stage) {
		MarketAPI target = findAttackTarget(intel, stage);
		if (target == null || target.getStarSystem() == null) return false;
		
		CampaignFleetAPI nexus = getRemnantNexus(target.getStarSystem());
		if (nexus == null) return false;
	
		stage.rollData = null;
		return startAttack(nexus, target, target.getStarSystem(), stage, getRandomizedStageRandom(3));
	}
	
	
	public MarketAPI findAttackTarget(HostileActivityEventIntel intel, EventStageData stage) {
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(getRandomizedStageRandom(3));
		
		for (StarSystemAPI system : Misc.getPlayerSystems(false)) {
			CampaignFleetAPI nexus = getRemnantNexus(system);
			if (nexus != null) {
				for (MarketAPI curr : Misc.getMarketsInLocation(system, Factions.PLAYER)) {
					picker.add(curr, curr.getSize() * curr.getSize() * curr.getSize());
				}
			}
		}
		return picker.pick();
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
		
		EventStageData stage = intel.getDataFor(Stage.HA_EVENT);
		if (stage != null && stage.rollData instanceof HAERandomEventData && 
				((HAERandomEventData)stage.rollData).factor == this) {
			
			HAERandomEventData data = (HAERandomEventData) stage.rollData;
			if (data.custom instanceof MarketAPI) {
				MarketAPI target = (MarketAPI) data.custom;
				if (getRemnantNexus(target.getStarSystem()) == null) {
					intel.resetHA_EVENT();
				}
			}
		}

	}
	

	
	public boolean startAttack(CampaignFleetAPI nexus, MarketAPI target, StarSystemAPI system, EventStageData stage, Random random) {
		//System.out.println("RANDOM: " + random.nextLong());
		
		GenericRaidParams params = new GenericRaidParams(new Random(random.nextLong()), true);
		
		params.makeFleetsHostile = true;
		params.remnant = true;
		
		params.factionId = nexus.getFaction().getId();
		
		MarketAPI fake = Global.getFactory().createMarket(nexus.getId(), nexus.getName(), 3);
		fake.setPrimaryEntity(nexus);
		fake.setFactionId(params.factionId);
		fake.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat(
				"nexus_" + nexus.getId(), 1f);
		
		//nexus.setMarket(fake); // can actually trade etc then, no good
		
		params.source = fake;
		
		params.prepDays = 0f;
		params.payloadDays = 27f + 7f * random.nextFloat();
		
		params.raidParams.where = system;
		params.raidParams.type = FGRaidType.SEQUENTIAL;
		params.raidParams.tryToCaptureObjectives = false;
		params.raidParams.allowedTargets.add(target);
		params.raidParams.allowNonHostileTargets = true;
		params.raidParams.setBombardment(BombardType.SATURATION);
		params.forcesNoun = "remnant forces";
		
		params.style = FleetStyle.STANDARD;
		params.repImpact = ComplicationRepImpact.FULL;
		
		
		// standard Askonia fleet size multiplier with no shortages/issues is a bit over 230%
		float fleetSizeMult = 1f;
		boolean damaged = nexus.getMemoryWithoutUpdate().getBoolean("$damagedStation");
		if (damaged) {
			fleetSizeMult = 0.5f;
		}
		
		float totalDifficulty = fleetSizeMult * 50f;
		
		totalDifficulty -= 10;
		params.fleetSizes.add(10);
		
		while (totalDifficulty > 0) {
			int min = 6;
			int max = 10;
			
			int diff = min + random.nextInt(max - min + 1);
			
			params.fleetSizes.add(diff);
			totalDifficulty -= diff;
		}
		
		
//		SindrianDiktatPunitiveExpedition punex = new SindrianDiktatPunitiveExpedition(params);
//		punex.setListener(this);
//		Global.getSector().getIntelManager().addIntel(punex);
		
		GenericRaidFGI raid = new GenericRaidFGI(params);
		raid.setListener(this);
		Global.getSector().getIntelManager().addIntel(raid);
		
		return true;
	}

	public void reportFGIAborted(FleetGroupIntel intel) {
		// TODO: anything? Nothing happens if you defeat it, it's purely avoiding a bombardment
	}

}




