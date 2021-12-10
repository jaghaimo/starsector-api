package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.util.Misc;

public class AptitudeDesc {
	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			//return "The maximum level of all skills governed by this aptitude is limited to the level of the aptitude.";
			return BaseIntelPlugin.BULLET + "Choose enough skills in lower tiers to unlock the next tier\n"
				  +BaseIntelPlugin.BULLET + "It's possible to skip a tier by taking more skills in lower tiers instead\n"
				  +BaseIntelPlugin.BULLET + "Reaching the top tier requires 4 skill points, plus 1 more to take one of the top skills\n"
				  +BaseIntelPlugin.BULLET + "Taking the second top tier skill requires an additional 2 points spent in lower tier skills\n"
				  +BaseIntelPlugin.BULLET + "Skills that only affect the piloted ship can be made \"elite\" at the cost of a " + Misc.STORY + " point\n"
				  +BaseIntelPlugin.BULLET + "Skills that have been made elite in the past can be re-made elite at no cost\n"
			
			;
			// pick one to unlock next tier
			// can wrap around
			// spend " + Misc.STORY + " points to make elite
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getHighlightColor();
			Color s = Misc.getStoryOptionColor();
			return new Color[] {h, h, h, s};
		}
		public String[] getHighlights() {
			return new String[] {"4", "1", "2", "" + Misc.STORY + " point"};
		}
		public Color getTextColor() {
			return Misc.getTextColor();
			//return null;
		}
	}
	
//	public static class CombatDesc implements DescriptionSkillEffect {
//		public String getString() {
//			return "The maximum level of all skills governed by this aptitude is limited to the level of the aptitude.";
////			return "The maximum level of all skills governed by this aptitude is limited to the level of the aptitude." +
////					"\n\n" + 
////					"All combat skills have a Mastery effect that applies to all ships in the fleet, including your flagship, " +
////					"when the skill reaches level three.";
//		}
//		public Color[] getHighlightColors() {
//			Color c = Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"); 
//			return new Color[] {c};
//		}
//		public String[] getHighlights() {
//			return new String[] {"Mastery"};
//		}
//		public Color getTextColor() {
//			return null;
//		}
//	}

	
}
