package com.fs.starfarer.api.impl.combat.threat;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;


public class EnergyLashSystemAI implements ShipSystemAIScript {

	protected ShipAPI ship;
	protected CombatEngineAPI engine;
	protected ShipwideAIFlags flags;
	protected ShipSystemAPI system;
	protected EnergyLashSystemScript script;
	
	protected IntervalUtil tracker = new IntervalUtil(0.5f, 1f);
	
	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
		
		script = (EnergyLashSystemScript)system.getScript();
	}
	
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		tracker.advance(amount);
		
		if (tracker.intervalElapsed()) {
			if (system.getCooldownRemaining() > 0) return;
			if (system.isOutOfAmmo()) return;
			if (system.isActive()) return;
			if (ship.getFluxTracker().isOverloadedOrVenting()) return;
			
			ShipAPI pick = getWeightedTargets(target).getItemWithHighestWeight();
			if (pick != null) {
				ship.getAIFlags().setFlag(AIFlags.CUSTOM1, 1.5f, pick);
				ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
				//System.out.println("Lash target: " + pick);
			}
		}
	}
	
	public List<ShipAPI> getPossibleTargets() {
		List<ShipAPI> result = new ArrayList<>();
		CombatEngineAPI engine = Global.getCombatEngine();
		
		List<ShipAPI> ships = engine.getShips();
		for (ShipAPI other : ships) {
			if (other == ship) continue;
			if (!script.isValidLashTarget(ship, other)) continue;
			if (!script.isInRange(ship, other)) continue;
			result.add(other);
		}
		return result;
	}
	
	public WeightedRandomPicker<ShipAPI> getWeightedTargets(ShipAPI shipTarget) {
		WeightedRandomPicker<ShipAPI> picker = new WeightedRandomPicker<>();
		
		for (ShipAPI other : getPossibleTargets()) {
			float w = 0f;
			if (ship.getOwner() == other.getOwner()) {
				if (ThreatSwarmAI.isAttackSwarm(other)) {
					RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(other);
					if (swarm != null && !VoltaicDischargeOnFireEffect.isSwarmPhaseMode(other)) {
						w = 0.5f;
					} else {
						continue;
					}
				} else {
					if (other.getSystem() == null) continue;
					if (!(other.getSystem().getScript() instanceof EnergyLashActivatedSystem)) continue;
					if (other.getSystem().getCooldownRemaining() > 0) continue;
					if (other.getSystem().isActive()) continue;
					if (other.getFluxTracker().isOverloadedOrVenting()) continue;
					
					EnergyLashActivatedSystem otherSystem = (EnergyLashActivatedSystem) other.getSystem().getScript();
					w = otherSystem.getCurrentUsefulnessLevel(ship, other);
				}
//				if (other.getSystem().getSpecAPI().hasTag(Tags.SHIP_SYSTEM_MOVEMENT)) {
//				}
//				if (other.getSystem().getSpecAPI().hasTag(Tags.SHIP_SYSTEM_OFFENSIVE)) {
//					Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
//					if (test instanceof ShipAPI) {
//						ShipAPI otherTarget = (ShipAPI) test;
//						float dist = Misc.getDistance(other.getLocation(), otherTarget.getLocation());
//						dist -= other.getCollisionRadius() + otherTarget.getCollisionRadius();
//						if (dist < 1500f) {
//						}
//					}
//				}
			} else {
				ShieldAPI targetShield = other.getShield();
				boolean targetShieldsFacingUs = targetShield != null &&
							targetShield.isOn() &&
							Misc.isInArc(targetShield.getFacing(), Math.max(30f, targetShield.getActiveArc()),
									other.getLocation(), ship.getLocation());
				if (targetShieldsFacingUs && EnergyLashSystemScript.DAMAGE <= 0) continue;
				
				float dist = Misc.getDistance(ship.getLocation(), other.getLocation());
				dist -= ship.getCollisionRadius() + other.getCollisionRadius();
				if (dist < 0) dist = 0;
				if (other == shipTarget) {
					w += 0.25f;
				}
				if (dist < 1000f) {
					w += (1f - (dist / 1000f)) * 0.5f;
				}
				if (other.isPhased()) {
					w += 0.5f;
				}
				w += 0.01f;
			}
			picker.add(other, w);
		}
		return picker;
	}

}






















