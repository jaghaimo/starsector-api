package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class NeuralLink {
	
	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			String control = Global.getSettings().getControlStringForEnumName(NeuralLinkScript.TRANSFER_CONTROL);
			String desc = Global.getSettings().getControlDescriptionForEnumName(NeuralLinkScript.TRANSFER_CONTROL);
			return "\n*Use the \"" + desc + "\" control [" + control + "] to switch between ships.";
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getHighlightColor();
			h = Misc.getDarkHighlightColor();
			return new Color[] {h};
		}
		public String[] getHighlights() {
			String control = Global.getSettings().getControlStringForEnumName(NeuralLinkScript.TRANSFER_CONTROL);
			String desc = Global.getSettings().getControlDescriptionForEnumName(NeuralLinkScript.TRANSFER_CONTROL);
			return new String [] {control};
		}
		public Color getTextColor() {
			return null;
		}
	}
	
	
	public static class Level1 extends BaseSkillEffectDescription implements CharacterStatsSkillEffect {

		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			if (stats.isPlayerStats()) {
				stats.getDynamic().getMod(Stats.HAS_NEURAL_LINK).modifyFlat(id, 1f);
			}
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			if (stats.isPlayerStats()) {
				stats.getDynamic().getMod(Stats.HAS_NEURAL_LINK).unmodifyFlat(id);
			}
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
				TooltipMakerAPI info, float width) {
			init(stats, skill);
			String dp = "" + (int)NeuralLinkScript.INSTANT_TRANSFER_DP;
			
//			HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(HullMods.NEURAL_INTERFACE);
//			HullModEffect e = modSpec.getEffect();
//			HullSize size = HullSize.CAPITAL_SHIP;
//			final String [] params = new String [] { 
//					 e.getDescriptionParam(0, size, null),
//					 e.getDescriptionParam(1, size, null),
//					 e.getDescriptionParam(2, size, null),
//					 e.getDescriptionParam(3, size, null),
//					 e.getDescriptionParam(4, size, null),
//					 e.getDescriptionParam(5, size, null),
//					 e.getDescriptionParam(6, size, null),
//					 e.getDescriptionParam(7, size, null),
//					 e.getDescriptionParam(8, size, null),
//					 e.getDescriptionParam(9, size, null)
//				};
//			info.addPara(modSpec.getDescription(size).replaceAll("\\%", "%%"), 0f, hc, hc, params);
			
//			info.addPara("Allows two ships to benefit from your combat skills at the same time and enables rapid switching between them. If the combined deployment cost "
//					+ "is " + dp + " points or below, the neural transfer is instant, otherwise it takes "
//							+ "a few seconds.", hc, 0f);
//			info.addPara("Certain skill effects that apply at the start of combat or earlier - such as increasing ammo capacity or combat readiness - "
//					+ "only apply to the flagship.", hc, 5f);
			
			
			info.addPara("Allows two ships to benefit from your combat skills at the same time "
					+ "and enables rapid switching between them.", hc, 0f);
			info.addPara("The destination ship "
					+ "must not be under the command of an officer or an AI core.", hc, 5f);
//			info.addPara("Certain skill effects that apply at the start of combat or earlier - such as increasing ammo capacity or combat readiness - "
//					+ "only apply to the flagship.", hc, 5f);			
			//info.addSpacer(5f);
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}

}





