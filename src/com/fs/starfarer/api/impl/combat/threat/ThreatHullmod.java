package com.fs.starfarer.api.impl.combat.threat;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * Hidden hullmod for managing the special properties of Threat ships.
 * @author Alex
 *
 */
public class ThreatHullmod extends BaseHullMod {

	public static String SHIP_BEING_RECLAIMED = "ship_being_reclaimed";
	
	public static String HIVE_UNIT = "hive_unit";
	public static String FABRICATOR_UNIT = "fabricator_unit";
	
	public static float AIM_BONUS = 1f;
	public static float MISSILE_GUIDANCE_BONUS = 1f;
	public static float CR_BONUS = 30f;
	public static float SENSOR_PROFILE_MULT = 0f;
	public static float EW_PENALTY_MULT = 0.5f;
	
//	public static float HIVE_UNIT_REGEN_RATE_MULT = 8f;
//	public static float HIVE_UNIT_SWARM_SIZE_MULT = 2f;
	public static float HIVE_UNIT_REGEN_RATE_MULT = 2f;
	public static float HIVE_UNIT_SWARM_SIZE_MULT = 4f;
	public static float FABRICATOR_UNIT_REGEN_RATE_MULT = 1f;
	public static float FABRICATOR_UNIT_SWARM_SIZE_MULT = 2f;
	
	public static float MODULE_DAMAGE_TAKEN_MULT = 0.5f;
	public static float EMP_DAMAGE_TAKEN_MULT = 0.5f;
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ConstructionSwarmSystemScript.init();
		
		stats.getAutofireAimAccuracy().modifyFlat(id, AIM_BONUS);
		stats.getMissileGuidance().modifyFlat(id, MISSILE_GUIDANCE_BONUS);
		stats.getMaxCombatReadiness().modifyFlat(id, CR_BONUS * 0.01f);
		
		stats.getSensorProfile().modifyMult(id, SENSOR_PROFILE_MULT);
		
		stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_PENALTY_MOD).modifyMult(id, EW_PENALTY_MULT);
		
		stats.getEmpDamageTakenMult().modifyMult(id, EMP_DAMAGE_TAKEN_MULT);
		stats.getEngineDamageTakenMult().modifyMult(id, MODULE_DAMAGE_TAKEN_MULT);
		stats.getWeaponDamageTakenMult().modifyMult(id, MODULE_DAMAGE_TAKEN_MULT);
		stats.getDynamic().getMod(Stats.CAN_REPAIR_MODULES_UNDER_FIRE).modifyFlat(id, 1f);
		
		if (stats.getVariant() != null && stats.getVariant().getHullSpec().getHullId().equals(HIVE_UNIT)) {
			stats.getDynamic().getStat(Stats.FRAGMENT_SWARM_RESPAWN_RATE_MULT).modifyMult(id, HIVE_UNIT_REGEN_RATE_MULT);
			stats.getDynamic().getMod(Stats.FRAGMENT_SWARM_SIZE_MOD).modifyMult(id, HIVE_UNIT_SWARM_SIZE_MULT);
//			stats.getDynamic().getStat(Stats.FRAGMENT_SWARM_RESPAWN_RATE_MULT).modifyMult(id, 2f);
//			stats.getDynamic().getMod(Stats.FRAGMENT_SWARM_SIZE_MOD).modifyMult(id, 4f);
		} else if (stats.getVariant() != null && stats.getVariant().getHullSpec().getHullId().equals(FABRICATOR_UNIT)) {
			stats.getDynamic().getStat(Stats.FRAGMENT_SWARM_RESPAWN_RATE_MULT).modifyMult(id, FABRICATOR_UNIT_REGEN_RATE_MULT);
			stats.getDynamic().getMod(Stats.FRAGMENT_SWARM_SIZE_MOD).modifyMult(id, FABRICATOR_UNIT_SWARM_SIZE_MULT);
		}
		
		//if (hullSize == HullSize.FIGHTER) {
			//stats.getEnergyAmmoRegenMult().modifyMult(id, 1000f);
		//}
		
		// no officers, but baseline affected by certain skill effects
		// or not - actually really difficult with these
		if (hullSize != HullSize.FIGHTER) {
//			new ImpactMitigation.Level2().apply(stats, hullSize, id, 1f);
//			new ImpactMitigation.Level4().apply(stats, hullSize, id, 1f);
//			
//			new DamageControl.Level3().apply(stats, hullSize, id, 1f);
//			new DamageControl.Level4().apply(stats, hullSize, id, 1f);
//			new DamageControl.Level8().apply(stats, hullSize, id, 1f); // elite
//			
//			new TargetAnalysis.Level2().apply(stats, hullSize, id, 1f);
//			new TargetAnalysis.Level3().apply(stats, hullSize, id, 1f);
//			new TargetAnalysis.Level1A().apply(stats, hullSize, id, 1f); // elite
//			new TargetAnalysis.Level1().apply(stats, hullSize, id, 1f); // elite
//			new TargetAnalysis.Level4().apply(stats, hullSize, id, 1f); // elite
//			
//			new BallisticMastery.Level1().apply(stats, hullSize, id, 1f);
//			new BallisticMastery.Level2().apply(stats, hullSize, id, 1f);
		}		
	}
	

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
//		CombatEngineAPI engine = Global.getCombatEngine();
//		if (!engine.hasPluginOfClass(ThreatCombatStrategyForBothSidesPlugin.class)) {
//			engine.addPlugin(new ThreatCombatStrategyForBothSidesPlugin());
//		}
//		if (ship.getSystem() != null && ship.getSystem().getScript() instanceof EnergyLashActivatedSystem) {
//			EnergyLashActivatedSystem script = (EnergyLashActivatedSystem) ship.getSystem().getScript();
//			script.resetAfterShipCreation(ship);
//		}
	}
	
	
	@Override
	public void applyEffectsAfterShipAddedToCombatEngine(ShipAPI ship, String id) {
		CombatEngineAPI engine = Global.getCombatEngine();
		if (!engine.hasPluginOfClass(ThreatCombatStrategyForBothSidesPlugin.class)) {
			engine.addPlugin(new ThreatCombatStrategyForBothSidesPlugin());
		}
	}


	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);
		
		if (!ship.isHulk() || ship.hasTag(SHIP_BEING_RECLAIMED)) return;
		if (ThreatCombatStrategyAI.isFabricator(ship)) return;
		
		float elapsedAsHulk = 0f;
		String key = "elapsedAsHulkKey";
		if (ship.getCustomData().containsKey(key)) {
			elapsedAsHulk = (float) ship.getCustomData().get(key);
		}
		elapsedAsHulk += amount;
		ship.setCustomData(key, elapsedAsHulk);
		if (elapsedAsHulk > 1f) {
			CombatEngineAPI engine = Global.getCombatEngine();
			int owner = ship.getOriginalOwner();
			boolean found = false;
			for (ShipAPI curr : engine.getShips()) {
				if (curr == ship || curr.getOwner() != owner) continue;
				if (curr.isHulk() || curr.getOwner() == 100) continue;
				if (!ThreatCombatStrategyAI.isFabricator(curr)) continue;
				if (curr.getCurrentCR() >= 1f) continue;
				found = true;
				break;
			}
			if (found) {
				Global.getCombatEngine().addPlugin(new ThreatShipReclamationScript(ship, 3f));
			} else {
				ship.setCustomData(key, 0f);
			}
		}
	}


	@Override
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
	}

	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color t = Misc.getTextColor();
		Color g = Misc.getGrayColor();
	
		tooltip.addPara("Threat hulls have a number of shared properties.", opad);
		
		tooltip.addSectionHeading("Campaign", Alignment.MID, opad);
		tooltip.addPara("Sensor profile reduced to %s.", opad, h, "0");
		
		tooltip.addSectionHeading("Combat", Alignment.MID, opad);
		tooltip.addPara("Target leading accuracy increased to maximum for all weapons, including missiles. Effect "
				+ "of enemy ECM rating reduced by %s.", opad, h, "" + (int) Math.round(EW_PENALTY_MULT * 100f) + "%");
		tooltip.addPara("Weapon and engine damage taken is reduced by %s. EMP damage taken is reduced by %s. In "
				+ "addition, repairs of damaged but functional weapons and engines can continue while they are under fire.",
				opad, h,
				"" + (int) Math.round((1f - MODULE_DAMAGE_TAKEN_MULT) * 100f) + "%",
				"" + (int) Math.round((1f - EMP_DAMAGE_TAKEN_MULT) * 100f) + "%");
		
		
	}
	
	public float getTooltipWidth() {
		return super.getTooltipWidth();
	}
	

}
