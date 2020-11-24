package com.fs.starfarer.api.impl.campaign.abilities.ai;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.events.BaseEventPlugin.MarketFilter;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class TransponderAbilityAI extends BaseAbilityAI {

	private IntervalUtil interval = new IntervalUtil(0.05f, 0.15f);

//	public TransponderAbilityAI(AbilityPlugin ability, ModularFleetAIAPI ai) {
//		super(ability, ai);
//	}

	public void advance(float days) {
		interval.advance(days * EmergencyBurnAbilityAI.AI_FREQUENCY_MULT);
		if (!interval.intervalElapsed()) return;
		
		MemoryAPI mem = fleet.getMemoryWithoutUpdate();
		
//		if (fleet.isInCurrentLocation()) {
//			float dist = Misc.getDistance(fleet.getLocation(), Global.getSector().getPlayerFleet().getLocation());
//			if (dist < fleet.getRadius() + Global.getSector().getPlayerFleet().getRadius() + 10f) {
//				System.out.println("sdfsdfdsXX1e12");
//			}
//		}
		
//		if (ability.isActive()) {
//			return;
//		}
		
		boolean smuggler = mem.getBoolean(MemFlags.MEMORY_KEY_SMUGGLER);
		boolean pirate = mem.getBoolean(MemFlags.MEMORY_KEY_PIRATE);
		boolean patrol = mem.getBoolean(MemFlags.MEMORY_KEY_PATROL_FLEET);
		//patrol |= mem.getBoolean(MemFlags.MEMORY_KEY_WAR_FLEET);
		
		boolean trader = mem.getBoolean(MemFlags.MEMORY_KEY_TRADE_FLEET);
		
		if (smuggler || pirate) {
			if (ability.isActive()) {
				ability.deactivate();
			}
			return;
		}
		
		if (patrol || trader) {
			if (!ability.isActive()) {
				ability.activate();
			}
			return;
		}
		
//		if (fleet.getName().contains("Scavenger") && 
//				Misc.getDistance(fleet.getLocation(), Global.getSector().getPlayerFleet().getLocation()) < 1000f) {
//			System.out.println("wefwefewf");
//		}
		
		if (fleet.isInHyperspace()) {
			float nonHostileSize = 0;
			float hostileSize = 0;
			for (MarketAPI market : Misc.getNearbyMarkets(fleet.getLocation(), 1f)) {
				float size = market.getSize();
				if (market.getFaction().isHostileTo(fleet.getFaction())) {
					hostileSize += size;
				} else {
					nonHostileSize += size;
				}
			}
			
			if (nonHostileSize > hostileSize) {
				if (!ability.isActive()) {
				//if (!fleet.isTransponderOn()) {
					ability.activate();
				}
			} else {
				if (ability.isActive()) {
				//if (fleet.isTransponderOn()) {
					ability.deactivate();
				}
			}
			return;
		}
		
		MarketAPI nearestMarket = Misc.findNearestLocalMarket(fleet, 1000000, new MarketFilter() {
			public boolean acceptMarket(MarketAPI market) {
				return true;
			}
		});

		if (nearestMarket != null) {
			if (nearestMarket.getFaction().isHostileTo(fleet.getFaction())) {
				if (ability.isActive()) {
					ability.deactivate();
				}
			} else {
				if (!ability.isActive()) {
					ability.activate();
				}
			}
			return;
		} else {
//			if (!ability.isActive()) {
//				ability.activate();
//			}
			if (ability.isActive()) {
				ability.deactivate();
			}
			return;
		}
	}
}






