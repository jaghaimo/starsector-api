package com.fs.starfarer.api.impl.combat;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.TimeoutTracker;

public class GravitonBeamEffect implements BeamEffectPlugin {

	public static float EFFECT_DUR = 1f;
	
	public static float DAMAGE_PERCENT_ONE = 5f;
	public static float DAMAGE_PERCENT_TWO = 8f;
	public static float DAMAGE_PERCENT_THREE = 10f;
	
	protected boolean wasZero = true;
	
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		CombatEntityAPI target = beam.getDamageTarget();
		if (target instanceof ShipAPI && beam.getBrightness() >= 1f && beam.getWeapon() != null) {
			float dur = beam.getDamage().getDpsDuration();
			// needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
			if (!wasZero) dur = 0;
			wasZero = beam.getDamage().getDpsDuration() <= 0;
			
			// beam tick, apply damage modifier effect if needed
			if (dur > 0) {
				boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
				if (hitShield) {
					ShipAPI ship = (ShipAPI) target;
					if (!ship.hasListenerOfClass(GravitonBeamDamageTakenMod.class)) {
						ship.addListener(new GravitonBeamDamageTakenMod(ship));
					}
					List<GravitonBeamDamageTakenMod> listeners = ship.getListeners(GravitonBeamDamageTakenMod.class);
					if (listeners.isEmpty()) return; // ???
					
					GravitonBeamDamageTakenMod listener = listeners.get(0);
					listener.notifyHit(beam.getWeapon());
				}
			}
		}
	}
	

	public static String DAMAGE_MOD_ID = "gravitonbeam_dam_mod";

	public static class GravitonBeamDamageTakenMod implements AdvanceableListener {
							//implements DamageTakenModifier, AdvanceableListener {
		protected ShipAPI ship;
		protected TimeoutTracker<WeaponAPI> recentHits = new TimeoutTracker<WeaponAPI>();
		public GravitonBeamDamageTakenMod(ShipAPI ship) {
			this.ship = ship;
			//ship.addListener(new GravitonBeamDamageTakenModRemover(ship));
		}
		
		public void notifyHit(WeaponAPI w) {
			recentHits.add(w, EFFECT_DUR, EFFECT_DUR);
			
		}
		
		public void advance(float amount) {
			recentHits.advance(amount);
			
			int beams = recentHits.getItems().size();

			float bonus = 0;
			if (beams == 1) {
				bonus = DAMAGE_PERCENT_ONE;
			} else if (beams == 2) {
				bonus = DAMAGE_PERCENT_TWO;
			} else if (beams >= 3) {
				bonus = DAMAGE_PERCENT_THREE;
			}
			
			if (bonus > 0) {
				ship.getMutableStats().getShieldDamageTakenMult().modifyMult(DAMAGE_MOD_ID, 1f + bonus * 0.01f);
			} else {
				ship.removeListener(this);
				ship.getMutableStats().getShieldDamageTakenMult().unmodify(DAMAGE_MOD_ID);
			}
		}

		public String modifyDamageTaken(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			return null;
		}

	}

}
