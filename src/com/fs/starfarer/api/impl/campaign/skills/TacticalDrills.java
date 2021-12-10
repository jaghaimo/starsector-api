package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.characters.FleetTotalItem;
import com.fs.starfarer.api.characters.FleetTotalSource;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class TacticalDrills {
	
	public static float DAMAGE_PERCENT = 5;
	
	public static int ATTACK_BONUS = 50;
	public static float CASUALTIES_MULT = 0.75f;
	
	
	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect, FleetTotalSource {
		
		public FleetTotalItem getFleetTotalItem() {
			return getCombatOPTotal();
		}
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (!isCivilian(stats)) {
				float damBonus = computeAndCacheThresholdBonus(stats, "td_dam", DAMAGE_PERCENT, ThresholdBonusType.OP);
				stats.getBallisticWeaponDamageMult().modifyPercent(id, damBonus);
				stats.getEnergyWeaponDamageMult().modifyPercent(id, damBonus);
				stats.getMissileWeaponDamageMult().modifyPercent(id, damBonus);
			}
		}
			
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getBallisticWeaponDamageMult().unmodifyPercent(id);
			stats.getEnergyWeaponDamageMult().unmodifyPercent(id);
			stats.getMissileWeaponDamageMult().unmodifyPercent(id);
		}
		
		public String getEffectDescription(float level) {
			return null;
		}
			
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);
			
			FleetDataAPI data = getFleetData(null);
			float damBonus = computeAndCacheThresholdBonus(data, stats, "td_dam", DAMAGE_PERCENT, ThresholdBonusType.OP);
			
			info.addPara("+%s weapon damage for combat ships (maximum: %s)", 0f, hc, hc,
					"" + (int) damBonus + "%",
					"" + (int) DAMAGE_PERCENT + "%");
			addOPThresholdInfo(info, data, stats, OP_THRESHOLD);
			
			//info.addSpacer(5f);
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	

	public static class Level2 extends BaseSkillEffectDescription implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).modifyPercent(id, ATTACK_BONUS, "Tactical drills");
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).unmodifyPercent(id);
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
						TooltipMakerAPI info, float width) {
			init(stats, skill);

			float opad = 10f;
			Color c = Misc.getBasePlayerColor();
			//info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "fleet");
			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "ground operations");
			info.addSpacer(opad);
			info.addPara("+%s effectiveness of ground operations such as raids", 0f, hc, hc,
					"" + (int) ATTACK_BONUS + "%");
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(ATTACK_BONUS) + "% effectiveness of ground operations such as raids"; 
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
			stats.getDynamic().getStat(Stats.PLANETARY_OPERATIONS_CASUALTIES_MULT).modifyMult(id, CASUALTIES_MULT, "Tactical drills");
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.PLANETARY_OPERATIONS_CASUALTIES_MULT).unmodifyMult(id);
		}
		
		public String getEffectDescription(float level) {
			return "-" + (int)Math.round((1f - CASUALTIES_MULT) * 100f) + "% marine casualties suffered during ground operations such as raids"; 
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
}





