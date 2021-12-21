package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.characters.AfterShipCreationSkillEffect;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.util.Misc;

public class PolarizedArmor {
	
	//public static float ARMOR_FRACTION_BONUS = 0.05f;
	public static float EFFECTIVE_ARMOR_BONUS = 50;
	public static float EMP_BONUS_PERCENT = 50f;
	
	public static float VENT_RATE_BONUS = 25f;
	
	public static float NON_SHIELD_FLUX_LEVEL = 50f;
	
	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			return "\n\n*Ships without a shield or a phase cloak are treated as always having " + (int) NON_SHIELD_FLUX_LEVEL + "% hard flux.";
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getHighlightColor();
			h = Misc.getDarkHighlightColor();
			return new Color[] {h};
		}
		public String[] getHighlights() {
			return new String [] {"" + (int) NON_SHIELD_FLUX_LEVEL + "%"};
		}
		public Color getTextColor() {
			return null;
		}
	}
	
	public static class PolarizedArmorEffectMod implements DamageTakenModifier, AdvanceableListener {
		protected ShipAPI ship;
		protected String id;
		public PolarizedArmorEffectMod(ShipAPI ship, String id) {
			this.ship = ship;
			this.id = id;
		}
		
		public void advance(float amount) {
 			MutableShipStatsAPI stats = ship.getMutableStats();
			
			float fluxLevel = ship.getHardFluxLevel();
			
			if (ship.getShield() == null && !ship.getHullSpec().isPhase() &&
					(ship.getPhaseCloak() == null || !ship.getHullSpec().getHints().contains(ShipTypeHints.PHASE))) {
				fluxLevel = NON_SHIELD_FLUX_LEVEL * 0.01f;
			}
			
			//float armorBonus = ship.getArmorGrid().getArmorRating() * ARMOR_FRACTION_BONUS * fluxLevel;
			float armorBonus = EFFECTIVE_ARMOR_BONUS * fluxLevel;
			float empBonus = EMP_BONUS_PERCENT * fluxLevel;
			//armorBonus = 1090000;
			//wefwef we fe stats.getMaxArmorDamageReduction().modifyFlat(id, 0.1f);
			//stats.getEffectiveArmorBonus().modifyFlat(id, armorBonus);
			stats.getEffectiveArmorBonus().modifyPercent(id, armorBonus);
			stats.getEmpDamageTakenMult().modifyMult(id, 1f - empBonus * 0.01f);
			
			//Color c = new Color(255, 200, 100, 100);
			Color c = ship.getSpriteAPI().getAverageColor();
			c = Misc.setAlpha(c, 127);
			float b = 0f;
			if (fluxLevel > 0.75f) {
				b = 1f * (fluxLevel - 0.75f) / 0.25f;
			}
			if (b > 0) {
				ship.setJitter(this, c, 1f * fluxLevel * b, 1, 0f);
			}
		}

		public String modifyDamageTaken(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			return null;
		}

	}
	
	public static class Level1 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getMaxArmorDamageReduction().modifyFlat(id, 0.05f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMaxArmorDamageReduction().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "Maximum damage reduction by armor increased from 85% to 90%";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	
	public static class Level2 implements AfterShipCreationSkillEffect {
		public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
			ship.addListener(new PolarizedArmorEffectMod(ship, id));
		}
		public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
			MutableShipStatsAPI stats = ship.getMutableStats();
			ship.removeListenerOfClass(PolarizedArmorEffectMod.class);
			stats.getEffectiveArmorBonus().unmodify(id);
			stats.getEmpDamageTakenMult().unmodify(id);
		}
			
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {}
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {}
		
		public String getEffectDescription(float level) {
			//return "Up to +" + (int)Math.round(ARMOR_FRACTION_BONUS * 100f) + "% of base armor for damage reduction calculation only, based on current hard flux level";
			
			return "Up to +" + (int)(EFFECTIVE_ARMOR_BONUS) + "% armor for damage reduction calculation only, based on current hard flux level*";
		}
		public String getEffectPerLevelDescription() {
			return null;
		}
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level3 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {}
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {}
		
		public String getEffectDescription(float level) {
			return "EMP damage taken reduced by up to " + (int)Math.round(EMP_BONUS_PERCENT) + "%, based on current hard flux level*";
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
			stats.getVentRateMult().modifyPercent(id, VENT_RATE_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getVentRateMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(VENT_RATE_BONUS) + "% flux dissipation rate while venting";
			//return "+" + (int)(VENT_RATE_BONUS) + "% flux vent rate";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
}











