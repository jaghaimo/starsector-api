package com.fs.starfarer.api.impl.combat.dweller;

import java.util.List;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.dweller.DwellerCombatPlugin.DCPPlugin;
import com.fs.starfarer.api.impl.combat.dweller.DwellerCombatPlugin.WobblyPart;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.DwellerShroudParams;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class ShroudedTendrilShipCreator extends BaseDwellerShipCreator {

	
	public static float FLUX_COST_MULT = 0.33f;
	
	@Override
	public void initBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		super.initBeforeShipCreation(hullSize, stats, id);
		
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, FLUX_COST_MULT);
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, FLUX_COST_MULT);
		stats.getMissileWeaponFluxCostMod().modifyMult(id, FLUX_COST_MULT);
	}

	
	@Override
	protected DwellerCombatPlugin createPlugin(final ShipAPI ship) {
		DwellerCombatPlugin plugin = super.createPlugin(ship);
		plugin.setPlugin(new DCPPlugin() {
			IntervalUtil interval = new IntervalUtil(0.5f, 1.5f);
			boolean anythingLargerNearby = false;
			@Override
			public void advance(DwellerCombatPlugin plugin, float amount) {
				interval.advance(amount);
				if (interval.intervalElapsed()) {
					CombatFleetManagerAPI fleetManager = Global.getCombatEngine().getFleetManager(ship.getOriginalOwner());
					if (fleetManager != null) {
						anythingLargerNearby = false;
						for (DeployedFleetMemberAPI curr : fleetManager.getDeployedCopyDFM()) {
							if (curr.getShip() == null) continue;
							if (curr.getShip().getHullSize().ordinal() > ship.getHullSize().ordinal()) {
								float dist = Misc.getDistance(ship.getLocation(), curr.getLocation());
								dist -= ship.getCollisionRadius() + curr.getShip().getCollisionRadius();
								if (dist < 3000f) {
									anythingLargerNearby = true;
									break;
								}
							}
						}
					}
				}
				if (ship.getSinceLastDamageTaken() < 0.5f && anythingLargerNearby) {
					ship.getAIFlags().setFlag(AIFlags.BACK_OFF, 1f);
					ship.getAIFlags().setFlag(AIFlags.BACK_OFF_MIN_RANGE, 1f, 2000f);
					ship.getAIFlags().unsetFlag(AIFlags.DO_NOT_BACK_OFF);
				} else {
					ship.getAIFlags().setFlag(AIFlags.DO_NOT_BACK_OFF, 1f);
					ship.getAIFlags().unsetFlag(AIFlags.BACK_OFF);
					ship.getAIFlags().unsetFlag(AIFlags.BACK_OFF_MIN_RANGE);
				}
			}
		});
		
		List<DwellerShipPart> parts = plugin.getParts();
		parts.clear();
		
		float scale = 1f;
		scale = 0.75f;
		
		WobblyPart part = new WobblyPart("shrouded_tendril_base", 2f * scale, 1f, new Vector2f(0, 0), 0f);
		parts.add(part);
		
		Color glow = DwellerCombatPlugin.STANDARD_PART_GLOW_COLOR;
		
//		part = new WobblyPart("clusterA", 1f * scale, 3, 3, 2f, new Vector2f(70f * scale, 0), 0f);
//		part.color = glow;
//		part.additiveBlend = true;
		//part.setWeaponActivated();
		//parts.add(part);
		
		part = new WobblyPart("clusterB", 1f * scale, 3, 3, 2f, new Vector2f(30f * scale, 0), 0f);
		part.color = glow;
		part.additiveBlend = true;
		//part.setFluxActivated();
//		part.setWeaponActivated();
//		part.setShieldActivated();
//		part.setSystemActivated();
		parts.add(part);
		
//		part = new WobblyPart("coronet_stalks", 0.5f * scale, 3, 3, 2f, new Vector2f(100f * scale, 0), 0f);
//		part.color = glow;
//		part.additiveBlend = true;
		//part.setShieldActivated();
		//parts.add(part);
		
		return plugin;
	}

	@Override
	protected void modifyBaselineShroudParams(ShipAPI ship, DwellerShroudParams params) {
		params.maxOffset = 80f;
		params.initialMembers = (int) (100f / 4f);
		params.initialMembers = 70;
		params.baseMembersToMaintain = params.initialMembers;
		
		params.flashFrequency /= 2f;
		
		//params.flashFrequency /= 3.5f;
		//params.numToFlash = 1;
		//params.flashFrequency = 17f;
		//params.numToFlash = 2;
		
		params.negativeParticleAreaMult = 1.25f;
		//params.negativeParticleNumBase = 0;
//		params.negativeParticleSizeMult = 1.25f;
		
		params.overloadGlowSizeMult *= 0.75f;
		
//		params.maxSpeed += 500f;
//		params.springStretchMult = 30f;
//		params.baseFriction *= 10f;
//		params.frictionRange *= 1f;
	}

//	@Override
//	public void advance(DwellerCombatPlugin plugin, float amount) {
//		CombatEntityAPI attachedTo = plugin.getAttachedTo();
//		if (attachedTo instanceof ShipAPI) {
//			ShipAPI ship = (ShipAPI) attachedTo;
//			if (ship.getSinceLastDamageTaken() < 0.5f) {
//				ship.getAIFlags().setFlag(AIFlags.BACK_OFF, 1f);
//				ship.getAIFlags().setFlag(AIFlags.BACK_OFF_MIN_RANGE, 1f, 2000f);
//				ship.getAIFlags().unsetFlag(AIFlags.DO_NOT_BACK_OFF);
//			} else {
//				ship.getAIFlags().setFlag(AIFlags.DO_NOT_BACK_OFF, 1f);
//				ship.getAIFlags().unsetFlag(AIFlags.BACK_OFF);
//			}
//			//ship.getAIFlags().setFlag(AIFlags.DO_NOT_BACK_OFF, 1f);
//		}
//	}
	
}









