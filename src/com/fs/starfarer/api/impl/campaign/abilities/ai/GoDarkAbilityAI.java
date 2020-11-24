package com.fs.starfarer.api.impl.campaign.abilities.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.ai.FleetAIFlags;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.abilities.GoDarkAbility;
import com.fs.starfarer.api.impl.campaign.events.BaseEventPlugin.MarketFilter;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class GoDarkAbilityAI extends BaseAbilityAI {

	private IntervalUtil interval = new IntervalUtil(0.05f, 0.15f);

//	public GoDarkAbilityAI(AbilityPlugin ability, ModularFleetAIAPI ai) {
//		super(ability, ai);
//	}

	public void advance(float days) {
		interval.advance(days * EmergencyBurnAbilityAI.AI_FREQUENCY_MULT);
		if (!interval.intervalElapsed()) return;
		
		
		MemoryAPI mem = fleet.getMemoryWithoutUpdate();
		
		
		if (ability.isActiveOrInProgress()) {
			mem.set(FleetAIFlags.HAS_SPEED_PENALTY, true, 0.2f);
			mem.set(FleetAIFlags.HAS_LOWER_DETECTABILITY, true, 0.2f);
		}
		
		CampaignFleetAPI pursueTarget = mem.getFleet(FleetAIFlags.PURSUIT_TARGET);
		CampaignFleetAPI fleeingFrom = mem.getFleet(FleetAIFlags.NEAREST_FLEEING_FROM);
		Vector2f travelDest = mem.getVector2f(FleetAIFlags.TRAVEL_DEST);
		boolean wantsTransponderOn = mem.getBoolean(FleetAIFlags.WANTS_TRANSPONDER_ON);
		
//		if (pursueTarget != null && pursueTarget.isPlayerFleet() && fleeingFrom != null) {
//			System.out.println("dfsdfwefwe");
//		}
		
		// if being pursued
		if (fleeingFrom != null) {
			float dist = Misc.getDistance(fleet.getLocation(), fleeingFrom.getLocation()) - fleet.getRadius() - fleeingFrom.getRadius();
			if (dist < 0) return;
			float detRange = fleeingFrom.getMaxSensorRangeToDetect(fleet);
			float ourSpeed = fleet.getFleetData().getBurnLevel();
			float theirSpeed = fleeingFrom.getFleetData().getBurnLevel();
			// slower than closest pursuer, but could hide using go dark: do it
			if (!ability.isActiveOrInProgress()) {
				if (dist > detRange * GoDarkAbility.DETECTABILITY_MULT + 100f && ourSpeed < theirSpeed &&
						dist > detRange && dist < detRange + 300f) {
					ability.activate();
				}
			} else { // already seen by them, or faster than them with "go dark" off
				if (dist < detRange || ourSpeed / GoDarkAbility.MAX_BURN_MULT  > theirSpeed) {
					ability.deactivate();
				}
			}
			return;
		}
		
		// if wants to use transponder & not being pursued, don't do it
		//if (wantsTransponderOn) {
		if (fleet.isTransponderOn()) {
			return;
		}
		
//		if (pursueTarget != null && pursueTarget.isPlayerFleet()) {
//			System.out.println("dfsdfwefwe");
//		}
		
		// if ok with using it, and not being pursued
		// if pursuing, target can't see us, and we're gaining: leave on
		// if pursuing, target can see us or we're losing groung: turn off
		if (pursueTarget != null) {
			float closingSpeed = Misc.getClosingSpeed(fleet.getLocation(), pursueTarget.getLocation(), 
													  fleet.getVelocity(),pursueTarget.getVelocity());
			if (closingSpeed <= 1 && ability.isActiveOrInProgress()) {
				ability.deactivate();
			}
			return;
		}
		
		// is the destination nearby? turn on
		boolean smuggler = mem.getBoolean(MemFlags.MEMORY_KEY_SMUGGLER);
		boolean pirate = mem.getBoolean(MemFlags.MEMORY_KEY_PIRATE);
		// don't use for general pirates; too grief-y
		pirate = false;
		
		boolean nearestMarketHostile = false;
		MarketAPI nearestMarket = Misc.findNearestLocalMarket(fleet, 2000, new MarketFilter() {
			public boolean acceptMarket(MarketAPI market) {
				return true;
			}
		});
		if (nearestMarket != null && nearestMarket.getFaction().isHostileTo(fleet.getFaction())) {
			nearestMarketHostile = true;
		}
		
		if ((smuggler || pirate || nearestMarketHostile) && !ability.isActiveOrInProgress() && travelDest != null) {
			float dist = Misc.getDistance(fleet.getLocation(), travelDest);
			if (dist < 1500) {
				ability.activate();
			}
			return;
		}
	}
}






