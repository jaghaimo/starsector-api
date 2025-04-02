package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class ElectronicWarfare {
	
//	public static final float LEVEL_1_BONUS = 0f;
//	public static final float LEVEL_2_BONUS = 5f;
//	public static final float LEVEL_3_BONUS = 5f;
	
	public static float PER_SHIP_BONUS = 1f;
	
	public static float CAP_RANGE = 500f;
	public static float CAP_RATE = 5f;

	
//	public static float getBase(HullSize hullSize) {
//		float value = 0f;
//		switch (hullSize) {
//		case CAPITAL_SHIP: value = 4f; break;
//		case CRUISER: value = 3f; break;
//		case DESTROYER: value = 2f; break;
//		case FRIGATE: value = 1f; break;
//		}
//		return value;
//	}

	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			String max = (int)ElectronicWarfareScript.BASE_MAXIMUM + "%";
//			return "*Enemy weapon range is reduced by half of the total ECM rating of the deployed ships, " +
//				"up to a maximum of " + max + 
//				". Does not apply to fighters, affects all weapons including missiles.";
//			return "*Reduces enemy weapon range. The total reduction is the lesser of " + max + 
//					" and the combined ECM rating for both sides. " + 
//					"The side with the lower ECM rating gets a higher penalty. " +
//					"Does not apply to fighters, affects all weapons including missiles.";
			
			return "*Enemy weapon range is reduced by the total ECM rating of your deployed ships, "
						+ "up to a maximum of " + max + ". This penalty is reduced by the ratio "
								+ "of the enemy ECM rating to yours." + 
						"Does not apply to fighters, affects all weapons including missiles.";
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getHighlightColor();
			h = Misc.getDarkHighlightColor();
			return new Color[] {h, h, h};
		}
		public String[] getHighlights() {
			String max = (int)ElectronicWarfareScript.BASE_MAXIMUM + "%";
//			String jammer = "+" + (int)ElectronicWarfareScript.PER_JAMMER + "%";
//			return new String [] {jammer, max};
			//return new String [] {max, "combined", "relative"};
			//return new String [] {"half", max};
			return new String [] {max};
			//return new String [] {"half", "maximum of " + max};
		}
		public Color getTextColor() {
			return null;
		}
	}
	
	public static class Level0WithNewline extends Level0 {
		public String getString() {
			return "\n" + super.getString();
		}
	}
	
	public static class Level1A implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (!BaseSkillEffectDescription.isCivilian(stats)) {
				stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_FLAT).modifyFlat(id, PER_SHIP_BONUS);
			}
		}
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_FLAT).unmodify(id);
		}
		public String getEffectDescription(float level) {
			//return "+1-4" + "% to ECM rating of ships, depending on ship size";
			//return "Every deployed ship increases ECM rating* of fleet by " + (int) PER_SHIP_BONUS + "%";
			return "Every deployed combat ship contributes +" + (int) PER_SHIP_BONUS + "% to ECM rating* of fleet";
		}
		public String getEffectPerLevelDescription() {
			return null;
		}
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	
	public static class Level1B implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (!BaseSkillEffectDescription.isCivilian(stats)) {
				stats.getDynamic().getMod(Stats.SHIP_OBJECTIVE_CAP_RANGE_MOD).modifyFlat(id, CAP_RANGE);
				stats.getDynamic().getStat(Stats.SHIP_OBJECTIVE_CAP_RATE_MULT).modifyMult(id, CAP_RATE);
			}
		}
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.SHIP_OBJECTIVE_CAP_RANGE_MOD).unmodifyFlat(id);
			stats.getDynamic().getStat(Stats.SHIP_OBJECTIVE_CAP_RATE_MULT).unmodifyMult(id);
		}
		public String getEffectDescription(float level) {
			return "Combat objectives are captured much more quickly and from longer range";
		}
		public String getEffectPerLevelDescription() {
			return null;
		}
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	public static class Level1C implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDynamic().getMod(Stats.SHIP_BELONGS_TO_FLEET_THAT_CAN_COUNTER_EW).modifyFlat(id, 1f);
		}
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.SHIP_BELONGS_TO_FLEET_THAT_CAN_COUNTER_EW).unmodifyFlat(id);
		}
		public String getEffectDescription(float level) {
			String excess = "" + (int)Math.round(ElectronicWarfareScript.BASE_MAXIMUM * 2f);
			return "Half of your fleet's excess (above " + excess + "%) ECM rating reduces the maximum range penalty due to enemy ECM";
		}
		public String getEffectPerLevelDescription() {
			return null;
		}
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
}
