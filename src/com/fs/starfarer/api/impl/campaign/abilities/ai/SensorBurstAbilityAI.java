package com.fs.starfarer.api.impl.campaign.abilities.ai;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.ai.FleetAIFlags;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.events.BaseEventPlugin.MarketFilter;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class SensorBurstAbilityAI extends BaseAbilityAI {

	private IntervalUtil interval = new IntervalUtil(0.05f, 0.15f);

//	public SensorBurstAbilityAI(AbilityPlugin ability, ModularFleetAIAPI ai) {
//		super(ability, ai);
//	}

	public void advance(float days) {
		interval.advance(days);
		if (!interval.intervalElapsed()) return;
		
		MemoryAPI mem = fleet.getMemoryWithoutUpdate();
		
		if (ability.isActiveOrInProgress()) {
			mem.set(FleetAIFlags.HAS_SPEED_PENALTY, true, 0.2f);
			mem.set(FleetAIFlags.HAS_VISION_BONUS, true, 0.2f);
			mem.set(FleetAIFlags.HAS_HIGHER_DETECTABILITY, true, 0.2f);
			return;
		}
		
		if (fleet.getOrbit() != null) {
			return;
		}
		
		CampaignFleetAPI pursueTarget = mem.getFleet(FleetAIFlags.PURSUIT_TARGET);
		CampaignFleetAPI fleeingFrom = mem.getFleet(FleetAIFlags.NEAREST_FLEEING_FROM);
		
		// being pursued: definitely not
		if (fleeingFrom != null) {
			return;
		}
		
		// pursuing an enemy, but can't see them anymore
		if (pursueTarget != null) {
			VisibilityLevel level = pursueTarget.getVisibilityLevelTo(fleet);
			if (level != VisibilityLevel.NONE) return; // can already see them
			
			
			float daysUnseen = mem.getFloat(FleetAIFlags.DAYS_TARGET_UNSEEN);
			float prob = (daysUnseen - 1f) * 0.1f;
			//prob /= EmergencyBurnAbilityAI.AI_FREQUENCY_MULT;
			
			MarketAPI nearestMarket = Misc.findNearestLocalMarket(fleet, 1000000, new MarketFilter() {
				public boolean acceptMarket(MarketAPI market) {
					return true;
				}
			});
			if (nearestMarket != null && nearestMarket.getFaction().isHostileTo(fleet.getFaction())) {
				prob *= 0.25f;
			}
			
			if (Math.random() < prob) {
				ability.activate();
			}
			
			return;
		}
		
		boolean trader = mem.getBoolean(MemFlags.MEMORY_KEY_TRADE_FLEET);
		boolean smuggler = mem.getBoolean(MemFlags.MEMORY_KEY_SMUGGLER);
		if (trader || smuggler) {
			return;
		}
		
		// not pursuing or being pursued, use now and again; more often if nearest market is friendly
		MarketAPI nearestMarket = Misc.findNearestLocalMarket(fleet, 1000000, new MarketFilter() {
			public boolean acceptMarket(MarketAPI market) {
				return true;
			}
		});
		float prob = 0.01f;
		if (nearestMarket != null && nearestMarket.getFaction().isHostileTo(fleet.getFaction())) {
			prob *= 0.25f;
		}
		if (Math.random() < prob) {
			ability.activate();
		}
	}
}






