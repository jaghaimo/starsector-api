package com.fs.starfarer.api.impl.combat.threat;

import java.util.List;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;

public class AttackSwarmPhaseModeScript  extends BaseEveryFrameCombatPlugin {
	
	public static float DURATION = 20f;
	
	
	protected float timeLeft = 0f;
	protected ShipAPI ship;
	protected Color origColor = Misc.setAlpha(VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR, 60);
	protected float origRadius = 120f;
	protected float origFlashFrequency = 1f;
	protected float origFlashRateMult= 1f;
	protected float glowFadeDelay = 2f;
	
	public AttackSwarmPhaseModeScript(ShipAPI ship) {
		this(ship, DURATION);
	}
	public AttackSwarmPhaseModeScript(ShipAPI ship, float dur) {
		this.ship = ship;
		this.timeLeft = dur;
		
		turnOnPhaseMode();
		
		Global.getCombatEngine().addPlugin(this);
	}
	
	public void turnOnPhaseMode() {
		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
		if (swarm == null) return;

		origColor = swarm.params.flashFringeColor;
		origRadius = swarm.params.flashRadius;
		origFlashFrequency = swarm.params.flashFrequency;
		origFlashRateMult = swarm.params.flashRateMult;

		Color color = Misc.setAlpha(VoltaicDischargeOnFireEffect.PHASE_FRINGE_COLOR, 60);
		swarm.params.flashFringeColor = color;
		swarm.params.flashRadius = 180f;
		swarm.params.tags.add(VoltaicDischargeOnFireEffect.SWARM_TAG_PHASE_MODE);

		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.usesAmmo() && w.getSpec().hasTag(Tags.FRAGMENT_GLOW)) {
				//w.setAmmo(Integer.MAX_VALUE);
				w.setAmmo(1000);
				//w.setMaxAmmo(Integer.MAX_VALUE);
			}
			if (w.getSpec().hasTag(Tags.OVERSEER_CHARGE) || 
					(ship.isFighter() && w.getSpec().hasTag(Tags.OVERSEER_CHARGE_FIGHTER))) {
				w.setAmmo(w.getMaxAmmo());
			}
		}
	}
	
	public void turnOffPhaseMode() {
		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
		if (swarm == null) return;

		swarm.params.flashFringeColor = origColor;
		swarm.params.flashRadius = origRadius;
		swarm.params.flashFrequency = origFlashFrequency;
		swarm.params.flashRateMult = origFlashRateMult;
		swarm.params.tags.remove(VoltaicDischargeOnFireEffect.SWARM_TAG_PHASE_MODE);
		
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.usesAmmo() && w.getSpec().hasTag(Tags.FRAGMENT_GLOW)) {
				w.setAmmo(w.getMaxAmmo());
			}
		}
	}
	
	public void suppressGlow() {
		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
		if (swarm != null) {
			swarm.params.flashFrequency = 0f;
			swarm.params.flashRateMult = 1f;
		}
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.usesAmmo() && w.getSpec().hasTag(Tags.FRAGMENT_GLOW)) {
				w.setAmmo(0);
			}
		}
	}
	
		
	@Override
	public void advance(float amount, List<InputEventAPI> events) {
		if (Global.getCombatEngine().isPaused()) return;
	
		timeLeft -= amount;
		
		if (timeLeft <= 0f) {
			glowFadeDelay -= amount;
			suppressGlow();
			
			if (glowFadeDelay <= 0f) {
				turnOffPhaseMode();
				CombatEngineAPI engine = Global.getCombatEngine();
				engine.removePlugin(this);
			}
		}
	}
}











