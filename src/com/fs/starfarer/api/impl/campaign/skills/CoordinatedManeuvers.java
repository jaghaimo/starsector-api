package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class CoordinatedManeuvers {
	
	public static final float CP_BONUS = 3f;
	
	public static float NAV_FRIGATES = 6f;
	public static float NAV_DESTROYERS = 3f;
	public static float NAV_OTHER = 1f;
	
	public static float CP_REGEN_FRIGATES = 50f;
	public static float CP_REGEN_DESTROYERS = 25f;
	

	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			String max = (int)CoordinatedManeuversScript.BASE_MAXIMUM + "%";
//			String buoy = "+" + (int)CoordinatedManeuversScript.PER_BUOY + "%";
//			return "Does not apply to fighters. Bonus from each ship only applies to other ships.\n" +
//				   "Nav buoys grant " + buoy + " each, up to a maximum of " + max + " without skill.";
//			return "Nav buoys grant " + buoy + " top speed each, up to a maximum of " + max + " without skills. " +
//					"Does not apply to fighters. Bonus from each ship does not apply to itself.";
			return "*The total nav rating for the deployed ships of the fleet increases the top speed of all ships " +
			   	   "in the fleet, up to a maximum of " +
			   	   "" + max + ". Does not apply to fighters.";			
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getHighlightColor();
			h = Misc.getDarkHighlightColor();
			return new Color[] {h, h};
		}
		public String[] getHighlights() {
			String max = (int)CoordinatedManeuversScript.BASE_MAXIMUM + "%";
			String jammer = "+" + (int)CoordinatedManeuversScript.PER_BUOY + "%";
			return new String [] {jammer, max};
		}
		public Color getTextColor() {
			return null;
		}
	}
	
	public static boolean isFrigateOrDestroyerAndOfficer(MutableShipStatsAPI stats) {
		FleetMemberAPI member = stats.getFleetMember();
		if (member == null) return false;
		// applies at least 1% in all cases now
		//if (!member.isFrigate() && !member.isDestroyer()) return false;
		
		return !member.getCaptain().isDefault();
	}
	//return "Every deployed ship grants +1-4% (depending on ship size) to top speed of allied ships";
	public static class Level1A extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (isFrigateOrDestroyerAndOfficer(stats)) {
				float bonus = 0f;
				if (hullSize == HullSize.FRIGATE) bonus = NAV_FRIGATES;
				if (hullSize == HullSize.DESTROYER) bonus = NAV_DESTROYERS;
				if (hullSize == HullSize.CRUISER || hullSize == HullSize.CAPITAL_SHIP) bonus = NAV_OTHER;
				if (bonus > 0f) {
					stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_FLAT).modifyFlat(id, bonus);
				}
			}
		}
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_FLAT).unmodify(id);
		}
		public String getEffectDescription(float level) {
//			return "+" + (int)NAV_FRIGATES + "% to nav rating* of fleet when piloting a frigate, " +
//				   "+" + (int) NAV_DESTROYERS + "% when piloting a destroyer";
			return null;
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
				TooltipMakerAPI info, float width) {
			init(stats, skill);

			float opad = 10f;
			info.addPara("+%s to nav rating* of fleet for deployed frigates, " +
					     "+%s for destroyers, +%s for larger hulls", 0f, hc, hc,
					     "" + (int) NAV_FRIGATES + "%",
					     "" + (int) NAV_DESTROYERS + "%",
					     "" + (int) NAV_OTHER + "%"
					     );
			//info.addSpacer(opad);
			
//			Color c = Misc.getBasePlayerColor();
//			info.addPara("Affects: %s", opad, Misc.getGrayColor(), c, "fleet");
//			info.addSpacer(opad);
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level1B extends BaseSkillEffectDescription implements CharacterStatsSkillEffect {

		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getCommandPoints().modifyFlat(id, CP_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getCommandPoints().unmodify(id);
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
				TooltipMakerAPI info, float width) {
			init(stats, skill);

//			float opad = 10f;
//			Color c = Misc.getBasePlayerColor();
//			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "fleet");
//			info.addSpacer(opad);
			info.addPara("+%s command points", 0f, hc, hc,
					"" + (int) CP_BONUS + "");
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) CP_BONUS + " command points";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level1C extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (isFrigateOrDestroyerAndOfficer(stats)) {
				float bonus = 0f;
				if (hullSize == HullSize.FRIGATE) bonus = CP_REGEN_FRIGATES;
				if (hullSize == HullSize.DESTROYER) bonus = CP_REGEN_DESTROYERS;
				if (bonus > 0f) {
					stats.getDynamic().getMod(Stats.COMMAND_POINT_RATE_FLAT).modifyFlat(id, bonus * 0.01f);
				}
			}
		}
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.COMMAND_POINT_RATE_FLAT).unmodify(id);
		}
		public String getEffectDescription(float level) {
			return null;
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
				TooltipMakerAPI info, float width) {
			init(stats, skill);

			info.addPara("+%s to command point recovery rate from deployed frigates, " +
					     "+%s from destroyers", 0f, hc, hc,
					     "" + (int) CP_REGEN_FRIGATES + "%",
					     "" + (int) CP_REGEN_DESTROYERS + "%");
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
}
