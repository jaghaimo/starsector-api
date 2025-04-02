package com.fs.starfarer.api.impl.combat.dweller;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.DwellerShroudParams;
import com.fs.starfarer.api.util.Misc;

public class BaseDwellerShipCreator implements DwellerShipCreator {

	public static float IMPACT_VOLUME_MULT = 0.33f;
	
	public static float AIM_BONUS = 1f;
	public static float MISSILE_GUIDANCE_BONUS = 1f;
	public static float CR_BONUS = 30f;
	public static float SENSOR_PROFILE_MULT = 0f;
	public static float EW_PENALTY_MULT = 0.5f;
	
	public static float FLAT_RANGE_BONUS = 200f;
	
	public static float FLAT_ARMOR_BONUS = 50f;
	
	
//	public static String STANDARD_SWARM_EXCHANGE_CLASS = "dweller_exchange_class";
//	public static String STANDARD_SWARM_FLOCKING_CLASS = "dweller_flocking_class";
	
	@Override
	public void initBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getAutofireAimAccuracy().modifyFlat(id, AIM_BONUS);
		stats.getMissileGuidance().modifyFlat(id, MISSILE_GUIDANCE_BONUS);
		stats.getMaxCombatReadiness().modifyFlat(id, CR_BONUS * 0.01f);
		
		stats.getSensorProfile().modifyMult(id, SENSOR_PROFILE_MULT);
		
		stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_PENALTY_MOD).modifyMult(id, EW_PENALTY_MULT);
		
		stats.getEnergyWeaponRangeBonus().modifyFlat(id, FLAT_RANGE_BONUS);
		stats.getBallisticWeaponRangeBonus().modifyFlat(id, FLAT_RANGE_BONUS);
		
		stats.getEffectiveArmorBonus().modifyFlat(id, FLAT_ARMOR_BONUS);
	}

	@Override
	public void initAfterShipCreation(ShipAPI ship, String id) {
		ship.setDoNotRenderWeapons(true);
		ship.setNoMuzzleFlash(true);
		ship.setRenderEngines(false);
		ship.setDoNotRenderVentingAnimation(true);
	}
	
	@Override
	public void initAfterShipAddedToCombatEngine(ShipAPI ship, String id) {
		ship.setDoNotRenderSprite(true);
	}


	@Override
	public void initInCombat(ShipAPI ship) {
		initBasicShipProperties(ship);
		
		DwellerShroud shroud = createShroud(ship);
		DwellerCombatPlugin plugin = createPlugin(ship);
		
		setOverloadColorAndText(ship, shroud);
	}
	
	protected DwellerShroud createShroud(ShipAPI ship) {
		DwellerShroud shroud = DwellerShroud.getShroudFor(ship);
		if (shroud == null) {
			shroud = new DwellerShroud(ship, createShroudParams(ship));
			
		}
		return shroud;
	}
	
	protected DwellerShroudParams createShroudParams(ShipAPI ship) {
		DwellerShroudParams params = DwellerShroud.createBaselineParams(ship);
		modifyBaselineShroudParams(ship, params);
		return params;
	}
	
	protected void modifyBaselineShroudParams(ShipAPI ship, DwellerShroudParams params) {
	}
	
	protected DwellerCombatPlugin createPlugin(ShipAPI ship) {
		DwellerCombatPlugin plugin = DwellerCombatPlugin.getDwellerPluginFor(ship);
		if (plugin == null) {
			plugin = new DwellerCombatPlugin(ship);
		}
		return plugin;
	}
	
	protected void initBasicShipProperties(ShipAPI ship) {
		//ship.setDoNotRender(true);
				
		//ship.setExplosionScale(0.75f);
		ship.setExplosionScale(0.001f); // no 0 so the explosion sound plays
		ship.setHulkChanceOverride(0f);
		ship.setImpactVolumeMult(IMPACT_VOLUME_MULT);
		ship.getArmorGrid().clearComponentMap(); // no damage to weapons/engines
		ship.setNoDamagedExplosions(true);
		ship.setSpawnDebris(false);

		ship.setShipCollisionSoundOverride("dweller_collision_ships");
		ship.setAsteroidCollisionSoundOverride("dweller_collision_asteroid_ship");
	}
	
	protected void setOverloadColorAndText(ShipAPI ship, DwellerShroud shroud) {
		Color color = Misc.setAlpha(Misc.setBrightness(shroud.getParams().flashFringeColor, 255), 255);
		ship.getFluxTracker().setOverloadColor(color);
		ship.getFluxTracker().setOverloadText("Stunned!");
	}

	
}








