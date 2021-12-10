package com.fs.starfarer.api.impl.hullmods;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.skills.NeuralLinkScript;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class PhaseAnchor extends BaseHullMod {

	public static float PHASE_DISSIPATION_MULT = 2f;
	public static float ACTIVATION_COST_MULT = 0f;
	
	public static float CR_LOSS_MULT_FOR_EMERGENCY_DIVE = 1f;
	
	public static class PhaseAnchorScript implements AdvanceableListener, HullDamageAboutToBeTakenListener {
		public ShipAPI ship;
		public boolean emergencyDive = false;
		public float diveProgress = 0f;
		public FaderUtil diveFader = new FaderUtil(1f, 1f);
		public PhaseAnchorScript(ShipAPI ship) {
			this.ship = ship;
		}
		
		public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
			//if (ship.getCurrentCR() <= 0) return false;
			
			if (!emergencyDive) {
				String key = "phaseAnchor_canDive";
				boolean canDive = !Global.getCombatEngine().getCustomData().containsKey(key);
				float depCost = 0f;
				if (ship.getFleetMember() != null) {
					depCost = ship.getFleetMember().getDeployCost();
				}
				float crLoss = CR_LOSS_MULT_FOR_EMERGENCY_DIVE * depCost;
				canDive &= ship.getCurrentCR() >= crLoss;
				
				float hull = ship.getHitpoints();
				if (damageAmount >= hull && canDive) {
					ship.setHitpoints(1f);
					
					//ship.setCurrentCR(Math.max(0f, ship.getCurrentCR() - crLoss));
					if (ship.getFleetMember() != null) { // fleet member is fake during simulation, so this is fine
						ship.getFleetMember().getRepairTracker().applyCREvent(-crLoss, "Emergency phase dive");
						//ship.getFleetMember().getRepairTracker().setCR(ship.getFleetMember().getRepairTracker().getBaseCR() + crLoss);
					}
					emergencyDive = true;
					Global.getCombatEngine().getCustomData().put(key, true);
					
					if (!ship.isPhased()) {
						Global.getSoundPlayer().playSound("system_phase_cloak_activate", 1f, 1f, ship.getLocation(), ship.getVelocity());
					}
				}
			}
			
			if (emergencyDive) {
				return true;
			}
			
			return false;
		}

		public void advance(float amount) {
			String id = "phase_anchor_modifier";
			if (emergencyDive) {
				Color c = ship.getPhaseCloak().getSpecAPI().getEffectColor2();
				c = Misc.setAlpha(c, 255);
				c = Misc.interpolateColor(c, Color.white, 0.5f);
				
				if (diveProgress == 0f) {
					if (ship.getFluxTracker().showFloaty()) {
						float timeMult = ship.getMutableStats().getTimeMult().getModifiedValue();
						Global.getCombatEngine().addFloatingTextAlways(ship.getLocation(),
								"Emergency dive!",
								NeuralLinkScript.getFloatySize(ship), c, ship, 16f * timeMult, 3.2f/timeMult, 1f/timeMult, 0f, 0f,
								1f);
					}
				}
				
				diveFader.advance(amount);
				ship.setRetreating(true, false);
				
				ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
				diveProgress += amount * ship.getPhaseCloak().getChargeUpDur();
				float curr = ship.getExtraAlphaMult();
				ship.getPhaseCloak().forceState(SystemState.IN, Math.min(1f, Math.max(curr, diveProgress)));
				ship.getMutableStats().getHullDamageTakenMult().modifyMult(id, 0f);
				
				if (diveProgress >= 1f) {
					if (diveFader.isIdle()) {
						Global.getSoundPlayer().playSound("phase_anchor_vanish", 1f, 1f, ship.getLocation(), ship.getVelocity());
					}
					diveFader.fadeOut();
					diveFader.advance(amount);
					float b = diveFader.getBrightness();
					ship.setExtraAlphaMult2(b);
					
					float r = ship.getCollisionRadius() * 5f;
					ship.setJitter(this, c, b, 20, r * (1f - b));
					
					if (diveFader.isFadedOut()) {
						ship.getLocation().set(0, -1000000f);
					}
				}
			}
			
			
			boolean phased = ship.isPhased();
			if (ship.getPhaseCloak() != null && ship.getPhaseCloak().isChargedown()) {
				phased = false;
			}
			
			MutableShipStatsAPI stats = ship.getMutableStats();
			if (phased) {
				stats.getFluxDissipation().modifyMult(id, PHASE_DISSIPATION_MULT);
				stats.getBallisticRoFMult().modifyMult(id, PHASE_DISSIPATION_MULT);
				stats.getEnergyRoFMult().modifyMult(id, PHASE_DISSIPATION_MULT);
				stats.getMissileRoFMult().modifyMult(id, PHASE_DISSIPATION_MULT);
				stats.getBallisticAmmoRegenMult().modifyMult(id, PHASE_DISSIPATION_MULT);
				stats.getEnergyAmmoRegenMult().modifyMult(id, PHASE_DISSIPATION_MULT);
				stats.getMissileAmmoRegenMult().modifyMult(id, PHASE_DISSIPATION_MULT);
				
				// doesn't actually work, needs to update the ammo tracker in the system and this isn't handled
				// probably overpowered anyway...
				//stats.getSystemRegenBonus().modifyMult(id, PHASE_DISSIPATION_MULT);
			} else {
				stats.getFluxDissipation().unmodifyMult(id);
				stats.getBallisticRoFMult().unmodifyMult(id);
				stats.getEnergyRoFMult().unmodifyMult(id);
				stats.getMissileRoFMult().unmodifyMult(id);
				stats.getBallisticAmmoRegenMult().unmodifyMult(id);
				stats.getEnergyAmmoRegenMult().unmodifyMult(id);
				stats.getMissileAmmoRegenMult().unmodifyMult(id);
				//stats.getSystemRegenBonus().unmodifyMult(id);
			}
		}

	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new PhaseAnchorScript(ship));
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getPhaseCloakActivationCostBonus().modifyMult(id, 0f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "zero";
		if (index == 1) return "" + (int)PHASE_DISSIPATION_MULT + Strings.X;
		if (index == 2) return "" + (int)CR_LOSS_MULT_FOR_EMERGENCY_DIVE + Strings.X;
		
		//		if (index == 1) return "" + (int) Math.round(PHASE_TIME_BONUS) + "%";
//		float multWithoutMod = PhaseCloakStats.MAX_TIME_MULT;
//		float multWithMod = 1f + (multWithoutMod - 1f) * (1f + PHASE_TIME_BONUS/100f);
//		if (index == 2) return "" + (int) Math.round(multWithoutMod) + Strings.X;
//		if (index == 3) return "" + (int) Math.round(multWithMod) + Strings.X;
//
//		if (index == 4) return "" + (int) Math.round((1f - FLUX_THRESHOLD_DECREASE_MULT) * 100f) + "%";
//		if (index == 5) return "" + (int) Math.round(PhaseCloakStats.BASE_FLUX_LEVEL_FOR_MIN_SPEED * 100f) + "%";
//		if (index == 6) return "" + (int)Math.round(
//				PhaseCloakStats.BASE_FLUX_LEVEL_FOR_MIN_SPEED * 100f * FLUX_THRESHOLD_DECREASE_MULT) + "%";
//		
		//if (index == 0) return "" + (int) Math.round(PHASE_COOLDOWN_REDUCTION) + "%";
		return null;
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod(HullMods.ADAPTIVE_COILS)) return false;
		return ship.getHullSpec().isPhase();
	}

	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod(HullMods.ADAPTIVE_COILS)) {
			return "Incompatible with Adaptive Phase Coils";
		}
		if (!ship.getHullSpec().isPhase()) {
			return "Can only be installed on phase ships";
		}
		return super.getUnapplicableReason(ship);
	}
	
}

