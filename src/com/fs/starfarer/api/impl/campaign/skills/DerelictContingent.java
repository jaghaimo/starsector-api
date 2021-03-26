package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
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
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class DerelictContingent {
	
	public static float MAX_DMODS = 5;
	public static float CREW_LOSS_REDUCTION_PER_DMOD = 10f;
	public static float AVOID_DAMAGE_CHANCE_PER_DMOD = 15f;
	public static float AVOID_DAMAGE_DAMAGE_MULT = 0.1f;
	public static float CR_PER_DMOD = 3f;
	
	public static float SHIELDLESS_ARMOR_BONUS_PER_DMOD = 0.05f;
	
	
	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			String min = "" + (int) Math.round(Global.getSettings().getMinArmorFraction() * 100f) + "%";
			return 
					"*Maximum effect reached " +
					"at " + (int) MAX_DMODS + " d-mods." +
					"**The effective armor value can not go below a percentage of " +
					"its original value for calculating the amount of damage reduction it provides. " +
					"The base minimum armor value is " + min + " of the original value. "
					;
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getHighlightColor();
			h = Misc.getDarkHighlightColor();
			return new Color[] {h, h};
		}
		public String[] getHighlights() {
			String min = "" + (int) Math.round(Global.getSettings().getMinArmorFraction() * 100f) + "%";
			return new String [] {"" + (int) MAX_DMODS, min};
		}
		public Color getTextColor() {
			return null;
		}
	}
	
	
	public static boolean isDmoddedAndOfficer(MutableShipStatsAPI stats) {
		if (stats == null) return false;
		
		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			if (ship.getCaptain().isDefault()) return false;
			return DModManager.getNumDMods(ship.getVariant()) > 0;
		} else { 
			FleetMemberAPI member = stats.getFleetMember();
			if (member == null) return false;
			if (member.getCaptain().isDefault()) return false;
			return DModManager.getNumDMods(member.getVariant()) > 0;
		}
		
	}

	public static String AVOID_HULL_DAMAGE_CHANCE = "avoid_hull_damage_chance";
	
	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect, AfterShipCreationSkillEffect {
		public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
			MutableShipStatsAPI stats = ship.getMutableStats();
			if (isDmoddedAndOfficer(stats)) {
				ship.addListener(new DCDamageTakenMod(ship));
				
				float dmods = DModManager.getNumDMods(ship.getVariant());
				if (dmods <= 0) return;
				if (dmods > MAX_DMODS) dmods = MAX_DMODS;
				
				if (ship.getShield() == null) {
					stats.getMinArmorFraction().modifyFlat(id, SHIELDLESS_ARMOR_BONUS_PER_DMOD * dmods);
				}
			}
		}

		public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
			ship.removeListenerOfClass(DCDamageTakenMod.class);
			
			if (ship.getShield() == null) {
				MutableShipStatsAPI stats = ship.getMutableStats();
				stats.getMinArmorFraction().unmodifyFlat(id);
			}
		}
		
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (isDmoddedAndOfficer(stats)) {
				FleetMemberAPI member = stats.getFleetMember();
				float dmods = DModManager.getNumDMods(member.getVariant());
				if (dmods <= 0) return;
				if (dmods > MAX_DMODS) dmods = MAX_DMODS;

				stats.getCrewLossMult().modifyMult(id, 1f - ((CREW_LOSS_REDUCTION_PER_DMOD * dmods) * 0.01f));
				stats.getMaxCombatReadiness().modifyFlat(id, (CR_PER_DMOD * dmods) * 0.01f, "Derelict Contingent skill");
				stats.getDynamic().getMod(AVOID_HULL_DAMAGE_CHANCE).modifyFlat(id, AVOID_DAMAGE_CHANCE_PER_DMOD * dmods * 0.01f);
			}
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getCrewLossMult().unmodifyMult(id);
			stats.getMaxCombatReadiness().unmodifyFlat(id);
			stats.getDynamic().getMod(AVOID_HULL_DAMAGE_CHANCE).unmodifyFlat(id);
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
				TooltipMakerAPI info, float width) {
			init(stats, skill);

			info.addPara("%s chance per d-mod* to have incoming hull damage reduced by %s", 0f, hc, hc,
					"" + (int)AVOID_DAMAGE_CHANCE_PER_DMOD + "%",
					"" + (int)Math.round((1f - AVOID_DAMAGE_DAMAGE_MULT) * 100f) + "%"
			);
			info.addPara("%s crew lost due to hull damage in combat per d-mod*", 0f, hc, hc,
					"-" + (int)CREW_LOSS_REDUCTION_PER_DMOD + "%"
			);
			info.addPara("%s maximum combat readiness per d-mod*", 0f, hc, hc,
					"+" + (int)CR_PER_DMOD + "%"
			);
			
			info.addSpacer(5f);
			info.addPara("%s minimum armor value** for damage reduction per d-mod for unshielded ships", 0f, hc, hc,
					"+" + (int)Math.round(SHIELDLESS_ARMOR_BONUS_PER_DMOD * 100f) + "%"
			);
			
//			info.addPara("+%s weapon damage (maximum: %s)", 0f, hc, hc,
//					"" + (int) damBonus + "%",
//					"" + (int) DAMAGE_PERCENT + "%");
//			addOPThresholdInfo(info, data, stats, OP_LOW_THRESHOLD);

			//info.addSpacer(5f);
		}
	}
	
	public static String DAMAGE_MOD_ID = "dc_dam_mod";
	public static class DCDamageTakenModRemover implements DamageTakenModifier, AdvanceableListener {
		protected ShipAPI ship;
		public DCDamageTakenModRemover(ShipAPI ship) {
			this.ship = ship;
		}
		public String modifyDamageTaken(Object param, CombatEntityAPI target,
				DamageAPI damage, Vector2f point, boolean shieldHit) {
			return null;
		}

		public void advance(float amount) {
			if (!ship.hasListenerOfClass(DCDamageTakenMod.class)) {
				ship.removeListener(this);
				ship.getMutableStats().getHullDamageTakenMult().unmodifyMult(DAMAGE_MOD_ID);
			}
		}
		
	}
	public static class DCDamageTakenMod implements DamageTakenModifier, AdvanceableListener {
		protected ShipAPI ship;
		public DCDamageTakenMod(ShipAPI ship) {
			this.ship = ship;
			ship.addListener(new DCDamageTakenModRemover(ship));
		}
		
		public void advance(float amount) {
			
		}

		public String modifyDamageTaken(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			MutableShipStatsAPI stats = ship.getMutableStats();
			stats.getHullDamageTakenMult().unmodifyMult(DAMAGE_MOD_ID);
			
			if (shieldHit) return null;

			float chance = stats.getDynamic().getMod(AVOID_HULL_DAMAGE_CHANCE).computeEffective(0f);
			if (Math.random() >= chance) {
				return null;
			}
			
			stats.getHullDamageTakenMult().modifyMult(DAMAGE_MOD_ID, AVOID_DAMAGE_DAMAGE_MULT);
			
			return null;
		}

	}

}
