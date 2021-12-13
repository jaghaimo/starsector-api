package com.fs.starfarer.api.impl.campaign.tutorial;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.SourceBasedFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class RogueMinerMiscFleetManager extends SourceBasedFleetManager {

	protected float daysRemaining = 40f;
	
	public RogueMinerMiscFleetManager(SectorEntityToken source) {
		super(source, 3f, 0, 5, 5f);
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		if (isDone()) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		daysRemaining -= days;
		if (daysRemaining <= 0 && source != null) {
			// source is Derinkuyu; going back to independent status
			if (source.getMarket() != null && source.getMarket().isInEconomy()) {
				source.setFaction(Factions.INDEPENDENT);
				source.getMarket().setFactionId(Factions.INDEPENDENT);
				
				FactionAPI ind = Global.getSector().getFaction(Factions.INDEPENDENT);
				if (source.getMarket() != null && source.getMarket().getSubmarket(Submarkets.SUBMARKET_OPEN) != null) {
					source.getMarket().getSubmarket(Submarkets.SUBMARKET_OPEN).setFaction(ind);
				}
			}
			
			setDone(true);
		}
	}



	@Override
	protected CampaignFleetAPI spawnFleet() {
		if (source == null) return null;
		CampaignFleetAPI fleet = createEmptyRogueFleet("Rogue Miner Force", false);
	
		WeightedRandomPicker<String> picker1 = new WeightedRandomPicker<String>();
		picker1.add("cerberus_d_Standard");
		picker1.add("hound_d_Standard");
		picker1.add("kite_pirates_Raider");
		//picker1.add("shepherd_Frontier");
		
		WeightedRandomPicker<String> picker2 = new WeightedRandomPicker<String>();
		picker2.add("hammerhead_d_CS");
		picker2.add("enforcer_d_Strike");
		picker2.add("sunder_d_Assault");
		picker2.add("buffalo2_FS");
		picker2.add("condor_Support");
		picker2.add("condor_Attack");
		
		
		fleet.getFleetData().addFleetMember(picker1.pick());
		fleet.getFleetData().addFleetMember(picker1.pick());
		if ((float) Math.random() > 0.5f) fleet.getFleetData().addFleetMember(picker1.pick());
		fleet.getFleetData().addFleetMember(picker2.pick());
		if ((float) Math.random() > 0.5f) fleet.getFleetData().addFleetMember(picker2.pick());
		
		fleet.getFleetData().sort();
		
		LocationAPI location = source.getContainingLocation();
		location.addEntity(fleet);
		fleet.setLocation(source.getLocation().x, source.getLocation().y);
		
		
		WeightedRandomPicker<MarketAPI> marketPicker = new WeightedRandomPicker<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market == source) continue;
			if (market.isHidden()) continue;
			if (Factions.PIRATES.equals(market.getFactionId())) {
				float dist = Misc.getDistance(source.getLocationInHyperspace(), market.getLocationInHyperspace());
				float w = Math.max(1000f, 50000f - dist);
				marketPicker.add(market, w);
			}
		}
		
		MarketAPI market = marketPicker.pick();
		if (market != null) {
			fleet.addAssignment(FleetAssignment.RAID_SYSTEM, ((StarSystemAPI)source.getContainingLocation()).getCenter(), 40f);
			Misc.giveStandardReturnAssignments(fleet, market.getPrimaryEntity(), "heading to", false);
		} else {
			fleet.addAssignment(FleetAssignment.RAID_SYSTEM, ((StarSystemAPI)source.getContainingLocation()).getCenter(), 100f);
			Misc.giveStandardReturnToSourceAssignments(fleet, false);
		}
		
		
		return fleet;
	}
	
	public static CampaignFleetAPI createEmptyRogueFleet(String name, boolean withFaction) {
		CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(Factions.PIRATES, FleetTypes.MERC_SCOUT, null);
		
		fleet.setName(name);
		fleet.setNoFactionInName(!withFaction);
	
		fleet.removeAbility(Abilities.INTERDICTION_PULSE);
		
		fleet.getStats().getFleetwideMaxBurnMod().modifyMult("tutorial", 0.6f);
		//fleet.getStats().getDetectedRangeMod().modifyFlat("tutorial", 500f);
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
		fleet.getMemoryWithoutUpdate().set("$rogueMiner", true);
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_NO_MILITARY_RESPONSE, true);
		
		return fleet;
	}
	
	
	public static CampaignFleetAPI createGuardFleet(boolean stronger) {
		CampaignFleetAPI fleet = createEmptyRogueFleet("Rogue Miner Force", false);
	
		WeightedRandomPicker<String> picker1 = new WeightedRandomPicker<String>();
		picker1.add("cerberus_d_Standard");
		picker1.add("hound_d_Standard");
		picker1.add("kite_pirates_Raider");
		picker1.add("shepherd_Frontier");
		
		WeightedRandomPicker<String> picker2 = new WeightedRandomPicker<String>();
		picker2.add("hammerhead_d_CS");
		picker2.add("enforcer_d_Strike");
		picker2.add("sunder_d_Assault");
		picker2.add("buffalo2_FS");
		
		
		fleet.getFleetData().addFleetMember(picker1.pick());
		if (stronger) fleet.getFleetData().addFleetMember(picker1.pick());
		if ((float) Math.random() > 0.5f) fleet.getFleetData().addFleetMember(picker1.pick());
		fleet.getFleetData().addFleetMember(picker2.pick());
		
		fleet.getFleetData().sort();
		
		return fleet;
	}
}










