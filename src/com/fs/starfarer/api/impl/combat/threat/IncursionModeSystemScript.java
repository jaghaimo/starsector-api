package com.fs.starfarer.api.impl.combat.threat;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.util.Misc;

public class IncursionModeSystemScript extends BaseEnergyLashActivatedSystem {
	
	public static float SPEED_BONUS = 100f;
	public static float FLUX_DISSIPATION_MULT = 3f;
	public static float AMMO_REGEN_MULT = 5f;
	
	
	protected ShipAIConfig origConfig; 
	
	protected void init(ShipAPI ship) {
		super.init(ship);
		if (ship.getShipAI() != null && ship.getShipAI().getConfig() != null) {
			ShipAIConfig config = ship.getShipAI().getConfig();
			origConfig = config.clone();
		}
	}
	
	public void applyImpl(ShipAPI ship, MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS * effectLevel);
		stats.getAcceleration().modifyFlat(id, 2f * SPEED_BONUS * effectLevel);
		stats.getDeceleration().modifyFlat(id, 2f * SPEED_BONUS * effectLevel);
		stats.getEnergyAmmoRegenMult().modifyMult(id, 1f + (AMMO_REGEN_MULT - 1f) * effectLevel);
		stats.getBallisticAmmoRegenMult().modifyMult(id, 1f + (AMMO_REGEN_MULT - 1f) * effectLevel);
		//stats.getMissileAmmoRegenMult().modifyMult(id, AMMO_REGEN_MULT * effectLevel);
		stats.getFluxDissipation().modifyMult(id, 1f + (FLUX_DISSIPATION_MULT - 1f) * effectLevel);
		
		if (ship.getShipAI() != null && ship.getShipAI().getConfig() != null) {
			ShipAIConfig config = ship.getShipAI().getConfig();
			if (effectLevel > 0) {
				config.personalityOverride = Personalities.RECKLESS;
				config.alwaysStrafeOffensively = true;
				config.backingOffWhileNotVentingAllowed = false;
				config.turnToFaceWithUndamagedArmor = false;
				config.burnDriveIgnoreEnemies = true;
			} else {
				config.copyFrom(origConfig);
			}
		}
		
		//if (state != State.IDLE && state != State.OUT) return;
		if (effectLevel <= 0f) return;
		
		ship.getEngineController().extendFlame(ship.getSystem(), 1f * effectLevel, 0f * effectLevel, 0.5f * effectLevel);
		
		ship.getAIFlags().setFlag(AIFlags.DO_NOT_BACK_OFF, 1f);
		ship.getAIFlags().setFlag(AIFlags.DO_NOT_VENT, 1f);
		ship.getAIFlags().setFlag(AIFlags.IGNORES_ORDERS, 1f);
		
		makeAllGroupsAutofireOneFrame(ship);
		
		setStandardJitter(ship, state, effectLevel);
	}
	
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (effectLevel <= 0f) return null;
		
		if (index == 0) {
			return new StatusData("improved maneuverability", false);
		} else if (index == 1) {
			return new StatusData("+" + (int)SPEED_BONUS + " top speed", false);
		} else if (index == 2) {
			return new StatusData("x" + (int)FLUX_DISSIPATION_MULT+ " flux dissipation", false);
		} else if (index == 3) {
			return new StatusData("rapid charge regen", false);
		}
		return null;
	}

	@Override
	public float getCurrentUsefulnessLevel(ShipAPI overseer, ShipAPI ship) {
		if (ship.getSystem().isActive() || ship.getSystem().isChargedown() ||
				ship.getSystem().isChargeup() || ship.getSystem().isCoolingDown()) {
			return 0f;
		}
		
		Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
		if (test instanceof ShipAPI) {
			ShipAPI target = (ShipAPI) test;
			
			float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
			dist -= ship.getCollisionRadius() + target.getCollisionRadius();
			
			float range = getNonMissileWeaponRange(ship);
			float extra = 750f;
			if (dist < range + extra) {
				float distToOverseer = Misc.getDistance(ship.getLocation(), overseer.getLocation());
				distToOverseer -= ship.getCollisionRadius() + overseer.getCollisionRadius();
				float overseerDistFactor = 0f;
				if (distToOverseer < 1000f) {
					float min = 500f;
					overseerDistFactor = (1f - Math.max(0f, distToOverseer - min) / (1000f - min)) * 0.25f;
				}
				return Math.min(1f, 0.5f + Math.min(0.5f, ship.getFluxLevel() * 1f) + overseerDistFactor);
			}
		}
		
		return 0f;
	}
	
	
	public static float getNonMissileWeaponRange(ShipAPI ship) {
		float max = 0f;
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.isDecorative()) continue;
			if (w.getType() == WeaponType.MISSILE) continue;
			max = Math.max(max, w.getRange());
		}
		return max;
	}
	
}








