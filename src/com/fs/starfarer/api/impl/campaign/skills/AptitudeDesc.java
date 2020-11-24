package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;

public class AptitudeDesc {
	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			return "The maximum level of all skills governed by this aptitude is limited to the level of the aptitude.";
		}
		public Color[] getHighlightColors() {
			return null;
		}
		public String[] getHighlights() {
			return null;
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
