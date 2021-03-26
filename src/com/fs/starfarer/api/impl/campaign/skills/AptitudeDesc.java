package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.util.Misc;

public class AptitudeDesc {
	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			//return "The maximum level of all skills governed by this aptitude is limited to the level of the aptitude.";
			return BaseIntelPlugin.BULLET + "Choose one of the skills in a tier to unlock the next tier\n"
				  +BaseIntelPlugin.BULLET + "Can wrap around after learning a skill in the top tier\n"
				  +BaseIntelPlugin.BULLET + "Some skills can be made \"elite\" at the cost of a " + Misc.STORY + " point\n"
			
			;
			// pick one to unlock next tier
			// can wrap around
			// spend " + Misc.STORY + " points to make elite
		}
		public Color[] getHighlightColors() {
			return new Color[] {Misc.getStoryOptionColor()};
		}
		public String[] getHighlights() {
			return new String[] {"" + Misc.STORY + " point"};
		}
		public Color getTextColor() {
			return null;
		}
	}
	
	public static class CombatDesc implements DescriptionSkillEffect {
		public String getString() {
			return "The maximum level of all skills governed by this aptitude is limited to the level of the aptitude.";
//			return "The maximum level of all skills governed by this aptitude is limited to the level of the aptitude." +
//					"\n\n" + 
//					"All combat skills have a Mastery effect that applies to all ships in the fleet, including your flagship, " +
//					"when the skill reaches level three.";
		}
		public Color[] getHighlightColors() {
			Color c = Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"); 
			return new Color[] {c};
		}
		public String[] getHighlights() {
			return new String[] {"Mastery"};
		}
		public Color getTextColor() {
			return null;
		}
	}

	
}
