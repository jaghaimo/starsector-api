package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.characters.AfterShipCreationSkillEffect;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class DamageControl {
	
	public static float SECONDS_PER_PROC = 2f;
	
	public static float INSTA_REPAIR = 0.25f;
	public static float CREW_LOSS_REDUCTION = 50;
	public static float MODULE_REPAIR_BONUS = 50;
	public static float HULL_DAMAGE_REDUCTION = 25;
	public static float EMP_DAMAGE_REDUCTION = 25;
	
	public static float ELITE_DAMAGE_THRESHOLD = 500;
	public static float ELITE_DAMAGE_REDUCTION_PERCENT = 60;
	
	public static float ELITE_DAMAGE_TO_HULL_PERCENT = 15;
	
	public static class Level2 implements ShipSkillEffect {

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
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level1 implements ShipSkillEffect {
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, 1000f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "If lost in combat, ship is almost always recoverable";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}

	public static class Level3 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			float timeMult = 1f / ((100f + MODULE_REPAIR_BONUS) / 100f);
			stats.getCombatWeaponRepairTimeMult().modifyMult(id, timeMult);
			stats.getCombatEngineRepairTimeMult().modifyMult(id, timeMult);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getCombatWeaponRepairTimeMult().unmodify(id);
			stats.getCombatEngineRepairTimeMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "" + (int)(MODULE_REPAIR_BONUS) + "% faster in-combat weapon and engine repairs";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level4 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getHullDamageTakenMult().modifyMult(id, 1f - HULL_DAMAGE_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getHullDamageTakenMult().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "-" + (int)(HULL_DAMAGE_REDUCTION) + "% hull damage taken";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level5 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDynamic().getMod(Stats.INSTA_REPAIR_FRACTION).modifyFlat(id, INSTA_REPAIR);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.INSTA_REPAIR_FRACTION).unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "" + (int) Math.round(INSTA_REPAIR * 100f) + "% of hull and armor damage taken repaired after combat ends, at no cost";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level6 extends BaseSkillEffectDescription implements AfterShipCreationSkillEffect {
		public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
			ship.addListener(new DamageControlDamageTakenMod(ship));
		}

		public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
			ship.removeListenerOfClass(DamageControlDamageTakenMod.class);
		}
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {}
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {}
		
		public String getEffectDescription(float level) {
			return null;
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);

			Color c = hc;
			float level = stats.getSkillLevel(skill.getId());
			if (level < 2) {
				c = dhc;
			}
			String seconds = "" + (int) SECONDS_PER_PROC + " seconds";
			if (SECONDS_PER_PROC == 1f) seconds = "second";
			//info.addPara("Single-hit hull damage above %s points has the portion above %s reduced by %s",
			info.addPara("At most once every " +  seconds + ", single-hit hull damage above %s points has the portion above %s reduced by %s",
					0f, c, c,
					"" + (int) ELITE_DAMAGE_THRESHOLD,
					"" + (int) ELITE_DAMAGE_THRESHOLD,
					"" + (int) ELITE_DAMAGE_REDUCTION_PERCENT + "%"
			);
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	
	public static class DamageControlDamageTakenMod implements DamageTakenModifier, AdvanceableListener {
		protected ShipAPI ship;
		protected float sinceProc = SECONDS_PER_PROC + 1f;
		public DamageControlDamageTakenMod(ShipAPI ship) {
			this.ship = ship;
		}
		
		public void advance(float amount) {
			sinceProc += amount;
		}
		
		public String modifyDamageTaken(Object param, CombatEntityAPI target, 
										DamageAPI damage, Vector2f point,
										boolean shieldHit) {
			if (!shieldHit && sinceProc > SECONDS_PER_PROC) {
				float mult = 1f - ELITE_DAMAGE_REDUCTION_PERCENT / 100f;
				ship.setNextHitHullDamageThresholdMult(ELITE_DAMAGE_THRESHOLD, mult);
				sinceProc = 0f;
			}
			return null;
		}
	}

	public static class Level7 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDamageToTargetHullMult().modifyPercent(id, ELITE_DAMAGE_TO_HULL_PERCENT);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDamageToTargetHullMult().unmodifyPercent(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(ELITE_DAMAGE_TO_HULL_PERCENT) + "% damage dealt to hull";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	
	
	public static class Level8Desc implements DescriptionSkillEffect {
		public String getString() {
			return "\n\n*Normally, a damaged but functional module will not be repaired until 5 seconds have passed "
					+ "without it taking damage.";
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getHighlightColor();
			h = Misc.getDarkHighlightColor();
			return new Color[] {h};
		}
		public String[] getHighlights() {
			return new String [] {"5"};
		}
		public Color getTextColor() {
			return null;
		}
	}
	public static class Level8 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDynamic().getMod(Stats.CAN_REPAIR_MODULES_UNDER_FIRE).modifyFlat(id, 1f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.CAN_REPAIR_MODULES_UNDER_FIRE).unmodifyFlat(id);
		}	
		
		public String getEffectDescription(float level) {
			return "Repairs of damaged but functional weapons and engines can continue while they are under fire*";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	
}






