package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class BestOfTheBest {

	public static int EXTRA_MODS = 1;
	public static float DEPLOYMENT_BONUS = 0.1f;
	

	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			int max = Misc.MAX_PERMA_MODS;
			return "*The base maximum number of permanent hullmods you're able to build into a ship is " +
					max + "."
					;
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getHighlightColor();
			h = Misc.getDarkHighlightColor();
			return new Color[] {h, h, h};
		}
		public String[] getHighlights() {
			int max = Misc.MAX_PERMA_MODS;
			return new String [] {"" + max};
		}
		public Color getTextColor() {
			return null;
		}
	}
	
	
	public static class Level1 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).modifyFlat(id, EXTRA_MODS);
		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "Able to build " + EXTRA_MODS + " more permanent hullmod* into ships";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level2 implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MIN_FRACTION_OF_BATTLE_SIZE_BONUS_MOD).modifyFlat(id, DEPLOYMENT_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MIN_FRACTION_OF_BATTLE_SIZE_BONUS_MOD).unmodifyFlat(id);
		}

		public String getEffectDescription(float level) {
//			return "Deployment points increased as if holding an objective worth " + 
//						(int)Math.round(DEPLOYMENT_BONUS * 100f) + "% of the battle size (equivalent to a Comm Relay)";
			return "Deployment points bonus from objectives is at least " + 
			(int)Math.round(DEPLOYMENT_BONUS * 100f) + "% of the battle size, even if holding no objectives";
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
}





