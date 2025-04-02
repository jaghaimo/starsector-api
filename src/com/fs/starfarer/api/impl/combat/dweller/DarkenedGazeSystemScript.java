package com.fs.starfarer.api.impl.combat.dweller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.ShroudNegativeParticleFilter;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;

public class DarkenedGazeSystemScript extends BaseShipSystemScript implements ShroudNegativeParticleFilter {
	
	public static float SHIELD_OPENING = 90f;
	public static float TURN_RATE_REDUCTION = 0.85f;
	
	public static float DAMAGE_TAKEN_MULT = 2f;
	
	public static String DARKENED_GAZE_SYSTEM_TAG = "darkened_gaze_system_tag";
	public static String DARKENED_GAZE_PRIMARY_WEAPON_TAG = "darkened_gaze_system_tag";
	
	
	protected List<WeaponAPI> weapons = null; 
	protected float elapsedActive = 0f; 

	
	protected void findWeapons(ShipAPI ship) {
		if (weapons != null) return;
		
		ship.addTag(DARKENED_GAZE_SYSTEM_TAG);
		
		weapons = new ArrayList<>();
		int index = 0;
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.getSlot().isDecorative()) {
//				if (index == 0 || index == 1 || index == 2 || index == 3) {
//					index++;
//					continue;
//				}
				//if (index == 8) weapons.clear();
				weapons.add(w);
				//if (index == 8) break;
				//break;
			}
			index++;
		}
		
		float min = 10000000f;
		WeaponAPI primary = null;
		//middle-most weapon is "primary"
		for (WeaponAPI w : weapons) {
			float test = w.getSlot().getLocation().y;
			if (test < min) {
				min = test;
				primary = w;
			}
		}
		if (primary != null) {
			primary.setCustom(DARKENED_GAZE_PRIMARY_WEAPON_TAG);
		}
		
		Collections.sort(weapons, (w1, w2) -> {
			return (int) Math.signum(Math.abs(w1.getSlot().getLocation().y) - Math.abs(w2.getSlot().getLocation().y));
		});
		
		// hardcoded to assume 9 beams
		float incr = 0.15f;
		float [] offsets = new float [] 
				{0f, incr, -incr, 2f * incr, -2f * incr, 3f * incr, -3f * incr, 4f * incr, -4f * incr}; 
		for (int i = 0; i < weapons.size(); i++) {
			WeaponAPI w = weapons.get(i);
			w.ensureClonedSpec();
			w.getSpec().getHardpointAngleOffsets().clear();
			w.getSpec().getHardpointAngleOffsets().add(offsets[i]);
			w.getSpec().getTurretAngleOffsets().clear();
			w.getSpec().getTurretAngleOffsets().add(offsets[i]);
			// so that there's no FF
			((BeamWeaponSpecAPI)w.getSpec()).setCollisionClass(CollisionClass.RAY_FIGHTER);
		}
	}
	
	public boolean isFFAConcern() {
		if (weapons.size() == 0) return false;
		return ((BeamWeaponSpecAPI)weapons.get(0).getSpec()).getCollisionClass() == CollisionClass.RAY;
	}
	
	public float getRange() {
		if (weapons == null || weapons.isEmpty()) return 0f;
		return weapons.get(0).getRange();
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		findWeapons(ship);
		
		ShieldAPI shield = ship.getShield();
		if (shield != null) {
			shield.forceFacing(ship.getFacing() + 180f);
			if (!ship.getFluxTracker().isOverloadedOrVenting()) {
				shield.toggleOn();
			}
			ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
		}
		
		DwellerShroud shroud = DwellerShroud.getShroudFor(ship);
		
		if (state == State.IN) {
			if (shield != null) {
				float currOpening = effectLevel * SHIELD_OPENING;
				shield.setArc(360f - currOpening);
			}
//			if (shroud != null && effectLevel < 0.5f) {
//				SwarmMember added = shroud.addMember();
//				//added.flash();
//				float arc = 360f - SHIELD_OPENING;
//				float angle = ship.getFacing() + 180f + arc/2f - arc * (float) Math.random();
//				added.offset = Misc.getUnitVectorAtDegreeAngle(angle);
//				added.offset.scale(shroud.getParams().maxOffset * 0.7f);
//				Vector2f offset = Misc.getUnitVectorAtDegreeAngle(angle);
//				offset.scale(shroud.getParams().maxOffset + 200f + (float) Math.random() * 100f);
//				Vector2f.add(ship.getLocation(), offset, added.loc);
//			}
			
		} else if (state == State.ACTIVE) {
			for (WeaponAPI w : weapons) {
				//if ((float) Math.random() > 0.97f) {
					w.setForceFireOneFrame(true);
				//}
			}
		} else if (state == State.OUT) {
			if (shield != null) {
				float currOpening = effectLevel * SHIELD_OPENING;
				shield.setArc(360f - currOpening);
			}	
		}
		
		if (state == State.IN || state == State.ACTIVE) {
			if (shroud != null) {
				shroud.getShroudParams().negativeParticleClearCenterAreaRadius = 150f; 
				shroud.getShroudParams().negativeParticleGenRate = 0.5f;
				//shroud.getShroudParams().negativeParticleColorOverride = new Color(0,0,0,255);
				shroud.getShroudParams().negativeParticleFilter = this;
			}
			
			
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(ship.getFacing() + 180f);
			float amount = Global.getCombatEngine().getElapsedInLastFrame();
			
			
			float accel = ConvulsiveLungeSystemScript.PARTICLE_WINDUP_ACCEL * amount * effectLevel;
			if (shroud != null) {
				for (SwarmMember p : shroud.getMembers()) {
					float currAngle = Misc.getAngleInDegrees(ship.getLocation(), p.loc);
					float angleDiff = Misc.getAngleDiff(currAngle, ship.getFacing());
					float accelMult = 1f;
					if (angleDiff > SHIELD_OPENING * 0.5f) {
						//accelMult = SHIELD_OPENING * 0.5f / angleDiff;
						accelMult = 0f;
					}
					
					p.vel.x += dir.x * accel * accelMult;
					p.vel.y += dir.y * accel * accelMult;
				}
			}
			if (state == State.ACTIVE) {
				elapsedActive += amount;
				float f = Math.min(elapsedActive, 1f) * 1f;
				f = 1f - f;
				//float beamSpeedMult = 1f + (float) Math.sqrt(f) * 2f;
//				float beamSpeedMult = 1f + f * f * 2f;
//				ship.getMutableStats().getBeamSpeedMod().modifyMult(id, beamSpeedMult);
			}
			
			ship.getMutableStats().getMaxTurnRate().modifyMult(id, 1f - TURN_RATE_REDUCTION * effectLevel);
			ship.getMutableStats().getHullDamageTakenMult().modifyMult(id, 1f + (DAMAGE_TAKEN_MULT - 1f) * effectLevel);
		} else {
			if (shroud != null) {
				shroud.getShroudParams().negativeParticleClearCenterAreaRadius = 50f;
				shroud.getShroudParams().negativeParticleGenRate = 1f;
				//shroud.getShroudParams().negativeParticleColorOverride = null;
				shroud.getShroudParams().negativeParticleFilter = null;
			}
			elapsedActive = 0f;
			ship.getMutableStats().getMaxTurnRate().unmodifyMult(id);
			ship.getMutableStats().getHullDamageTakenMult().unmodifyMult(id);
			//ship.getMutableStats().getBeamSpeedMod().unmodifyMult(id);
		}
		
		ship.getAIFlags().setFlag(AIFlags.BACK_OFF_MIN_RANGE, 1f, getRange() - 300f);
		
		if (state == State.IDLE) {
			shield.setArc(360f);
		}
			
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}
	
	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		return super.getInfoText(system, ship);
	}

	@Override
	public boolean isParticleOk(DwellerShroud shroud, Vector2f loc) {
		// only filtering when in/active, not idle/out/cooldown, so don't worry about those cases
		if (shroud == null || shroud.getAttachedTo() == null) return true;
		float angle = Misc.getAngleInDegrees(shroud.getAttachedTo().getLocation(), loc);
		return !Misc.isInArc(shroud.getAttachedTo().getFacing(), SHIELD_OPENING, angle);
	}

	
}








