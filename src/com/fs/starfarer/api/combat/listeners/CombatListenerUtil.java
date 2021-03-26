package com.fs.starfarer.api.combat.listeners;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;


public class CombatListenerUtil {
	
	public static List<String> modifyDamageDealt(Object param, CombatEntityAPI target, 
										    	 DamageAPI damage, Vector2f point, boolean shieldHit) {
		return modifyDamageDealt(Global.getCombatEngine().getListenerManager().getListeners(DamageDealtModifier.class),
								 param, target, damage, point, shieldHit);
	}
	
	public static List<String> modifyDamageDealt(ShipAPI attacker, Object param, CombatEntityAPI target, 
												 DamageAPI damage, Vector2f point, boolean shieldHit) {
		if (attacker == null || attacker.getListenerManager() == null) return null;
		return modifyDamageDealt(attacker.getListeners(DamageDealtModifier.class), param, target, damage, point, shieldHit);
	}
	
	protected static List<String> modifyDamageDealt(List<DamageDealtModifier> mods, Object param, CombatEntityAPI target, 
												 DamageAPI damage, Vector2f point, boolean shieldHit) {
		List<String> ids = null;
		mods = new ArrayList<DamageDealtModifier>(mods);
		for (DamageDealtModifier x : mods) {
			String id = x.modifyDamageDealt(param, target, damage, point, shieldHit);
			if (id != null) {
				if (ids == null) ids = new ArrayList<String>();
				ids.add(id);
			}
		}
		return ids;
	}
	
	
	
	
	public static List<String> modifyDamageTaken(Object param, CombatEntityAPI target, 
												 DamageAPI damage, Vector2f point, boolean shieldHit) {
		return modifyDamageTaken(Global.getCombatEngine().getListenerManager().getListeners(DamageTakenModifier.class),
								param, target, damage, point, shieldHit);
	}

	public static List<String> modifyDamageTaken(ShipAPI ship, Object param, CombatEntityAPI target, 
												 DamageAPI damage, Vector2f point, boolean shieldHit) {
		// ship == target, yes
		if (ship == null || ship.getListenerManager() == null) return null;
		return modifyDamageTaken(ship.getListeners(DamageTakenModifier.class), param, target, damage, point, shieldHit);
	}

	protected static List<String> modifyDamageTaken(List<DamageTakenModifier> mods, Object param, CombatEntityAPI target, 
													DamageAPI damage, Vector2f point, boolean shieldHit) {
		List<String> ids = null;
		mods = new ArrayList<DamageTakenModifier>(mods);
		for (DamageTakenModifier x : mods) {
			String id = x.modifyDamageTaken(param, target, damage, point, shieldHit);
			if (id != null) {
				if (ids == null) ids = new ArrayList<String>();
				ids.add(id);
			}
		}
		return ids;
	}

	public static void reportDamageApplied(ShipAPI ship, Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
		for (DamageListener x : new ArrayList<DamageListener>(Global.getCombatEngine().getListenerManager().getListeners(DamageListener.class))) {
			x.reportDamageApplied(source, target, result);
		}
		if (ship != null) {
			for (DamageListener x : new ArrayList<DamageListener>(ship.getListeners(DamageListener.class))) {
				x.reportDamageApplied(source, target, result);
			}
		}
	}
	
	public static float getWeaponRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
		float mod = 0f;
		if (ship != null) {
			for (WeaponRangeModifier x : new ArrayList<WeaponRangeModifier>(ship.getListeners(WeaponRangeModifier.class))) {
				mod += x.getWeaponRangePercentMod(ship, weapon);
			}
		}
		return mod;
	}
	
	public static float getWeaponRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
		float mod = 1f;
		if (ship != null && ship.getListenerManager() != null) {
			for (WeaponRangeModifier x : new ArrayList<WeaponRangeModifier>(ship.getListeners(WeaponRangeModifier.class))) {
				mod *= x.getWeaponRangeMultMod(ship, weapon);
			}
		}
		return mod;
	}
	
	public static float getWeaponRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
		float mod = 0f;
		if (ship != null && ship.getListenerManager() != null) {
			for (WeaponRangeModifier x : new ArrayList<WeaponRangeModifier>(ship.getListeners(WeaponRangeModifier.class))) {
				mod += x.getWeaponRangeFlatMod(ship, weapon);
			}
		}
		return mod;
	}
	
	public static int getWeaponOPCost(MutableShipStatsAPI stats, WeaponSpecAPI weapon, int currCost) {
		int cost = currCost;
		if (stats != null && stats.getListenerManager() != null) {
			for (WeaponOPCostModifier x : new ArrayList<WeaponOPCostModifier>(stats.getListeners(WeaponOPCostModifier.class))) {
				cost = x.getWeaponOPCost(stats, weapon, cost);
			}
		}
		return cost;
	}
	
	public static int getFighterOPCost(MutableShipStatsAPI stats, FighterWingSpecAPI fighter, int currCost) {
		int cost = currCost;
		if (stats != null && stats.getListenerManager() != null) {
			for (FighterOPCostModifier x : new ArrayList<FighterOPCostModifier>(stats.getListeners(FighterOPCostModifier.class))) {
				cost = x.getFighterOPCost(stats, fighter, cost);
			}
		}
		return cost;
	}
}












