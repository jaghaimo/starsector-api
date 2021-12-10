package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ForceConcentration {

	// these actually get applied in CoordinatedManeuversScript, based on the ship having
	// Stats.HAS_FORCE_CONCENTRATION_BONUS > 0
	public static float ZERO_FLUX_SPEED_BONUS_SMALL = 25f;
	public static float ZERO_FLUX_SPEED_BONUS = 100f;
	public static float ZERO_FLUX_ACCEL_BONUS = ZERO_FLUX_SPEED_BONUS;
	public static float ZERO_FLUX_TURN_BONUS = 20f;
	public static float ZERO_FLUX_TURN_ACCEL_BONUS = ZERO_FLUX_TURN_BONUS * 2f;
	
	public static float COMMAND_POINT_REGEN_PERCENT = 100f;
	
	
	
//	public static class Level1 implements ShipSkillEffect {
//
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			//stats.getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).modifyFlat(id, EXTRA_MODS);
//		}
//
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			//stats.getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).unmodifyFlat(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "Able to build " + EXTRA_MODS + " more permanent hullmod* into ships";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.FLEET;
//		}
//	}

	
	public static class Level2 implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			//stats.getDynamic().getMod(Stats.HAS_FORCE_CONCENTRATION_BONUS_MOD).modifyFlat(id, 1f);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			//stats.getDynamic().getMod(Stats.HAS_FORCE_CONCENTRATION_BONUS_MOD).unmodifyFlat(id);
		}

		public String getEffectDescription(float level) {
			return "+" + (int)ZERO_FLUX_SPEED_BONUS + " to 0-flux speed boost and a high maneuverability bonus if no enemy presence nearby, +" + 
							(int)ZERO_FLUX_SPEED_BONUS_SMALL + " to 0-flux boost otherwise";
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	public static class Level3 implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.CAN_DEPLOY_LEFT_RIGHT_MOD).modifyFlat(id, 1f);
		}
		
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.CAN_DEPLOY_LEFT_RIGHT_MOD).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "Able to deploy ships of all size classes from the flanks in all combat scenarios";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	public static class Level4 extends BaseSkillEffectDescription implements CharacterStatsSkillEffect {
		
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getStat(Stats.COMMAND_POINT_RATE_COMMANDER).modifyFlat(id, COMMAND_POINT_REGEN_PERCENT / 100f);
		}
		
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.COMMAND_POINT_RATE_COMMANDER).unmodify(id);
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
				TooltipMakerAPI info, float width) {
			init(stats, skill);

			float opad = 10f;
			Color c = Misc.getBasePlayerColor();
			//info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "fleet");
			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "fleet");
			info.addSpacer(opad);
			info.addPara("%s faster command point recovery", 0f, hc, hc,
					"" + (int) COMMAND_POINT_REGEN_PERCENT + "%");
		}
		
		public String getEffectDescription(float level) {
			return null;
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
}





