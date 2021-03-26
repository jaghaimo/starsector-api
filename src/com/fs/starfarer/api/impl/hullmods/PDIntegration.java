package com.fs.starfarer.api.impl.hullmods;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

public class PDIntegration extends BaseHullMod {

	public static int OP_REDUCTION = 3;
	public static float DAMAGE_BONUS_PERCENT = 25f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.SMALL_PD_MOD).modifyFlat(id, -OP_REDUCTION);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new PDIDamageDealtMod());
	}

	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + OP_REDUCTION;
		if (index == 1) return "" + (int) Math.round(DAMAGE_BONUS_PERCENT) + "%";
		return null;
	}
	
	@Override
	public boolean affectsOPCosts() {
		return true;
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null || ship.getVariant() == null) return true; // autofit
		if (!ship.getVariant().hasHullMod("pdintegration")) return true; // can always add

		for (String slotId : ship.getVariant().getFittedWeaponSlots()) {
			WeaponSpecAPI spec = ship.getVariant().getWeaponSpec(slotId);
			if (spec.getAIHints().contains(AIHints.PD)) return false;
		}
		return true;
	}

	@Override
	public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		return "Can not remove while ship has point-defense weapons installed";
	}
	
	
	
	public static class PDIDamageDealtMod implements DamageDealtModifier {
		public String modifyDamageDealt(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			WeaponAPI weapon = null;
			if (param instanceof DamagingProjectileAPI) {
				weapon = ((DamagingProjectileAPI)param).getWeapon();
			} else if (param instanceof BeamAPI) {
				weapon = ((BeamAPI)param).getWeapon();
			} else if (param instanceof MissileAPI) {
				weapon = ((MissileAPI)param).getWeapon();
			}
			
			if (weapon == null) return null;
			if (!weapon.hasAIHint(AIHints.PD)) return null;

			String id = "pdi_dam_mod";
			damage.getModifier().modifyPercent(id, DAMAGE_BONUS_PERCENT);
			
			return id;
		}
	}
	
}



