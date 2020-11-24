package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class RecoveryOperations {
	
	public static final float OWN_WEAPON_WING_RECOVERY_BONUS = 30f;
//	public static final float ENEMY_WEAPON_WING_RECOVERY_BONUS = 15f;
//	public static final float SHIP_RECOVERY_BONUS = 15f;
	public static final float ENEMY_WEAPON_WING_RECOVERY_BONUS = 25f;
	public static final float SHIP_RECOVERY_BONUS = 25f;
	public static final float DMOD_REDUCTION = 2f;
	public static final float CREW_LOSS_REDUCTION = 30f;
	
	public static final float REPAIR_RATE_BONUS = 100f;
	
	//public static final float COMBAT_SALVAGE_BONUS = 10f;
	public static final float FUEL_SALVAGE_BONUS = 50f;
	
	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			float baseW = (int) Math.round(Global.getSettings().getFloat("salvageWeaponProb") * 100f);
			float baseS = (int) Math.round(Global.getSettings().getFloat("baseShipRecoveryChance") * 100f);
			float baseWO = (int) Math.round(Global.getSettings().getFloat("salvageOwnWeaponProb") * 100f);
			float baseSO = (int) Math.round(Global.getSettings().getFloat("baseOwnShipRecoveryChance") * 100f);
//			return String.format(
//				   "Base chance to recover weapons from lost ships is %d%%, or %d%% for weapons from your ships. " +
//				   "Base chance for disabled ships to be recoverable is %d%%, or %d%% for your ships. " +
		   return String.format(
				   "The base chance to be recoverable is %d%% for disabled enemy ships, and %d%% for your ships. " +
				   "The base chance for weapons from disabled enemy ships to be recovered is %d%%, and %d%% for weapons from your ships. " +
			"Ships that have broken apart have half the chance to be recoverable and always suffer lasting structural damage.",
			(int)baseS, (int) baseSO, (int)baseW, (int)baseWO);
//			return "Base chance to recover weapons on lost ships is " + (int) baseW + "%. " +
//					"Base chance to recover disabled ships is " + (int) baseS + "%; " +
//					"ships that were broken apart have half the chance to be recovered and always suffer lasting structural damage.";
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getHighlightColor();
			return new Color[] {h, h, h, h};
		}
		public String[] getHighlights() {
			String baseW = "" + (int) Math.round(Global.getSettings().getFloat("salvageWeaponProb") * 100f) + "%";
			String baseS = "" + (int) Math.round(Global.getSettings().getFloat("baseShipRecoveryChance") * 100f) + "%";
			String baseWO = "" + (int) Math.round(Global.getSettings().getFloat("salvageOwnWeaponProb") * 100f) + "%";
			String baseSO = "" + (int) Math.round(Global.getSettings().getFloat("baseOwnShipRecoveryChance") * 100f) + "%";
			//return new String [] {baseW, baseWO, baseS, baseSO};
			return new String [] {baseS, baseSO, baseW, baseWO};
		}
		public Color getTextColor() {
			return null;
		}
	}
	
	public static class Level1 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.OWN_WEAPON_RECOVERY_MOD).modifyFlat(id, OWN_WEAPON_WING_RECOVERY_BONUS * 0.01f);	
			stats.getDynamic().getMod(Stats.OWN_WING_RECOVERY_MOD).modifyFlat(id, OWN_WEAPON_WING_RECOVERY_BONUS * 0.01f);	
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.OWN_WEAPON_RECOVERY_MOD).unmodify(id);	
			stats.getDynamic().getMod(Stats.OWN_WING_RECOVERY_MOD).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) (OWN_WEAPON_WING_RECOVERY_BONUS) + "% chance to recover weapons and fighter LPCs from own ships lost in battle";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level1B implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.ENEMY_WEAPON_RECOVERY_MOD).modifyFlat(id, ENEMY_WEAPON_WING_RECOVERY_BONUS * 0.01f);	
			stats.getDynamic().getMod(Stats.ENEMY_WING_RECOVERY_MOD).modifyFlat(id, ENEMY_WEAPON_WING_RECOVERY_BONUS * 0.01f);	
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.ENEMY_WEAPON_RECOVERY_MOD).unmodify(id);	
			stats.getDynamic().getMod(Stats.ENEMY_WING_RECOVERY_MOD).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) (ENEMY_WEAPON_WING_RECOVERY_BONUS) + "% chance to recover weapons and fighter LPCs from enemy ships";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level2 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.SHIP_RECOVERY_MOD).modifyFlat(id, SHIP_RECOVERY_BONUS * 0.01f);	
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.SHIP_RECOVERY_MOD).unmodify(id);	
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) (SHIP_RECOVERY_BONUS) + "% chance to recover disabled ships after battle";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	
	public static class Level2A implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getCrewLossMult().modifyMult(id, 1f - CREW_LOSS_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getCrewLossMult().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "-" + (int)(CREW_LOSS_REDUCTION) + "% crew lost due to hull damage in combat";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	public static class Level2B implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getStat(Stats.NON_COMBAT_CREW_LOSS_MULT).modifyMult(id, 1f - CREW_LOSS_REDUCTION / 100f);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.NON_COMBAT_CREW_LOSS_MULT).unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "-" + (int)(CREW_LOSS_REDUCTION) + "% crew lost in non-combat operations";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	
	public static class Level3 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.SHIP_DMOD_REDUCTION).modifyFlat(id, DMOD_REDUCTION);	
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.SHIP_DMOD_REDUCTION).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			//return "Recovered non-friendly ships have an average of " + (int) DMOD_REDUCTION + " less subsystem with lasting damage";
			//return "Recovered ships have up to " + (int) DMOD_REDUCTION + " less d-mods";
			return "Recovered ships have fewer d-mods on average";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level3B implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
//			stats.getDynamic().getStat(Stats.BATTLE_SALVAGE_MULT_FLEET).modifyFlat(id, COMBAT_SALVAGE_BONUS * 0.01f);
//			stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET).modifyFlat(id, COMBAT_SALVAGE_BONUS * 0.01f);
			stats.getDynamic().getStat(Stats.FUEL_SALVAGE_VALUE_MULT_FLEET).modifyFlat(id, FUEL_SALVAGE_BONUS * 0.01f);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
//			stats.getDynamic().getStat(Stats.BATTLE_SALVAGE_MULT_FLEET).unmodify(id);
//			stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET).unmodify(id);
			stats.getDynamic().getStat(Stats.FUEL_SALVAGE_VALUE_MULT_FLEET).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			float max = 0f;
			max += FUEL_SALVAGE_BONUS;
			return "+" + (int) max + "% fuel salvaged";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
}
