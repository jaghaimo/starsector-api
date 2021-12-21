package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class CyberneticAugmentation {
	
	public static float MAX_ELITE_SKILLS_BONUS = 2;
	public static float ECCM_BONUS = 5;
	
	public static boolean isOfficer(MutableShipStatsAPI stats) {
		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			return !ship.getCaptain().isDefault();
		} else {
			FleetMemberAPI member = stats.getFleetMember();
			if (member == null) return false;
			return !member.getCaptain().isDefault();
		}
	}
	
	
	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			int base = (int)Global.getSettings().getInt("officerMaxEliteSkills");
			return "*The base maximum number of elite skills per officer is " + base + "."; 
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getDarkHighlightColor();
			return new Color[] {h};
		}
		public String[] getHighlights() {
			int base = (int)Global.getSettings().getInt("officerMaxEliteSkills");
			return new String [] {"" + base};
		}
		public Color getTextColor() {
			return null;
		}
	}
	
	public static class Level1 implements CharacterStatsSkillEffect {
		
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.OFFICER_MAX_ELITE_SKILLS_MOD).modifyFlat(id, MAX_ELITE_SKILLS_BONUS);
		}
		
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.OFFICER_MAX_ELITE_SKILLS_MOD).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) MAX_ELITE_SKILLS_BONUS + " to maximum number of elite skills* for officers under your command";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.NONE;
		}
	}
	
	public static class Level2 extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
				TooltipMakerAPI info, float width) {
			init(stats, skill);
			float opad = 10f;
			Color c = Misc.getBasePlayerColor();
			//info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "fleet");
			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "all ships with officers, including flagship");
			info.addSpacer(opad);
			
//			info.addPara("Negates up to %s of the weapon range penalty for superior enemy Electronic Warfare", 0f, hc, hc,
//					"" + (int) ECCM_BONUS + "%");
			info.addPara("Reduces the weapon range penalty due to superior enemy Electronic Warfare by up to %s percentage points", 0f, hc, hc,
					"" + (int) ECCM_BONUS + "");
		}

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (isOfficer(stats)) {
				stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_PENALTY_MOD).modifyFlat(id, -ECCM_BONUS);
			}
		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_PENALTY_MOD).unmodifyFlat(id);
		}
	}
}
