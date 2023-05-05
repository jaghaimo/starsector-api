package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.FleetTotalItem;
import com.fs.starfarer.api.characters.FleetTotalSource;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.AICoreOfficerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.hullmods.Automated;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class AutomatedShips {
	
	public static float MAX_CR_BONUS = 100f;

	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			int alpha = (int) Math.round(AICoreOfficerPluginImpl.ALPHA_MULT);
			int beta = (int) Math.round(AICoreOfficerPluginImpl.BETA_MULT);
			int gamma = (int) Math.round(AICoreOfficerPluginImpl.GAMMA_MULT);
			
			return "*The total \"automated ship points\" are equal to the deployment points cost of " +
				"all automated ships in the fleet, with a multiplier for installed AI cores - " +
				alpha + Strings.X + " for an Alpha Core, " +			
				beta + Strings.X + " for a Beta Core, and " +			
				gamma + Strings.X + " for a Gamma Core. "
						+ "Due to safety interlocks, ships with AI cores do not contribute to the deployment point distribution.";
//			int alpha = AICoreOfficerPluginImpl.ALPHA_POINTS;
//			int beta = AICoreOfficerPluginImpl.BETA_POINTS;
//			int gamma = AICoreOfficerPluginImpl.GAMMA_POINTS;
//			if (BaseSkillEffectDescription.USE_RECOVERY_COST) {
//				return "*The total \"automated ship points\" are equal to the deployment recovery cost of " +
//						"all automated ships in the fleet, plus extra points for installed AI cores - " +
//						alpha + " for an Alpha Core, " +			
//						beta + " for a Beta Core, and " +			
//						gamma + " for a Gamma Core."
//						;
//			} else {
//				return "*The total \"automated ship points\" are equal to the ordnance points of " +
//						"all automated ships in the fleet, plus extra points for AI cores installed on any of the " +
//						"automated ships - " + 
//						alpha + " for an Alpha Core, " +			
//						beta + " for a Beta Core, and " +			
//						gamma + " for a Gamma Core."
//						;
//			}
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getHighlightColor();
			h = Misc.getDarkHighlightColor();
			Color bad = Misc.getNegativeHighlightColor();
			bad = Misc.setAlpha(bad, 200);
			return new Color[] {h, h, h, bad};
		}
		public String[] getHighlights() {
//			int alpha = AICoreOfficerPluginImpl.ALPHA_POINTS;
//			int beta = AICoreOfficerPluginImpl.BETA_POINTS;
//			int gamma = AICoreOfficerPluginImpl.GAMMA_POINTS;
			int alpha = (int) Math.round(AICoreOfficerPluginImpl.ALPHA_MULT);
			int beta = (int) Math.round(AICoreOfficerPluginImpl.BETA_MULT);
			int gamma = (int) Math.round(AICoreOfficerPluginImpl.GAMMA_MULT);
			return new String [] {"" + alpha + Strings.X, "" + beta + Strings.X, "" + gamma + Strings.X, 
					"do not contribute to the deployment point distribution"};
		}
		public Color getTextColor() {
			return null;
		}
	}
	
	
	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect, FleetTotalSource {
		
		public FleetTotalItem getFleetTotalItem() {
			return getAutomatedPointsTotal();
		}
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (Misc.isAutomated(stats) && 
					!Automated.isAutomatedNoPenalty(stats)) {
				float crBonus = computeAndCacheThresholdBonus(stats, "auto_cr", MAX_CR_BONUS, ThresholdBonusType.AUTOMATED_POINTS);
				SkillSpecAPI skill = Global.getSettings().getSkillSpec(Skills.AUTOMATED_SHIPS);
				stats.getMaxCombatReadiness().modifyFlat(id, crBonus * 0.01f, skill.getName() + " skill");
			}
		}
			
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMaxCombatReadiness().unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return null;
		}
			
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);
			
			FleetDataAPI data = getFleetData(null);
			float crBonus = computeAndCacheThresholdBonus(data, stats, "auto_cr", MAX_CR_BONUS, ThresholdBonusType.AUTOMATED_POINTS);
			
			String partially = "";
			String penalty = "" + (int)Math.round(Automated.MAX_CR_PENALTY * 100f) + "%%";
			if ((int) crBonus < 100f) partially = "partially ";
			info.addPara("+%s combat readiness (maximum: %s); " + partially + "offsets built-in " + penalty + " penalty", 0f, hc, hc,
					"" + (int) crBonus + "%",
					"" + (int) MAX_CR_BONUS + "%");
			addAutomatedThresholdInfo(info, data, stats);
			
			//info.addSpacer(5f);
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	public static class Level2 extends BaseSkillEffectDescription implements CharacterStatsSkillEffect {

		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			if (stats.isPlayerStats()) {
				Misc.getAllowedRecoveryTags().add(Tags.AUTOMATED_RECOVERABLE);
			}
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			if (stats.isPlayerStats()) {
				Misc.getAllowedRecoveryTags().remove(Tags.AUTOMATED_RECOVERABLE);
			}
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
				TooltipMakerAPI info, float width) {
			init(stats, skill);
			info.addPara("Enables the recovery of some automated ships, such as derelict drones", hc, 0f);
			info.addPara("Automated ships can only be captained by AI cores", hc, 0f);
			info.addSpacer(5f);
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}

}





