package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI.SkillLevelAPI;
import com.fs.starfarer.api.characters.PersonAPI;
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
	
	public static float MAX_ELITE_SKILLS_BONUS = 1;
	public static float ECCM_BONUS = 5;
	
	public static float BONUS_PER_ELITE_SKILL = 1f;
	
	public static boolean isOfficer(MutableShipStatsAPI stats) {
		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			if (ship.getCaptain().isAICore()) return false;
			return !ship.getCaptain().isDefault();
		} else {
			FleetMemberAPI member = stats.getFleetMember();
			if (member == null) return false;
			if (member.getCaptain().isAICore()) return false;
			return !member.getCaptain().isDefault();
		}
	}
	
	public static boolean isFlagship(MutableShipStatsAPI stats) {
		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			if (ship.getFleetMember() != null && 
					ship.getFleetMember().getFleetCommander() == ship.getCaptain()) {
				return true;
			}
			return ship.getCaptain().isPlayer();
		} else {
			FleetMemberAPI member = stats.getFleetMember();
			if (member == null) return false;
			if (member.isFlagship()) {
				return true;
			}
			return member.getCaptain().isPlayer();
		}
	}
	
	public static float getNumEliteSkillsOfFleetCommander(MutableShipStatsAPI stats) {
		FleetMemberAPI member = stats.getFleetMember();
		if (member == null && stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			member = ship.getFleetMember();
		}
		
		if (member == null) return 0f;
		PersonAPI person = member.getFleetCommanderForStats();
		if (person ==  null) person = member.getFleetCommander();
		if (person == null) return 0f;
		
		MutableCharacterStatsAPI fcStats = person.getStats();
		if (fcStats == null) return 0f;
		return getNumEliteSkills(fcStats);
	}
	
	public static float getNumEliteSkills(MutableCharacterStatsAPI stats) {
		float count = 0f;
		for (SkillLevelAPI sl : stats.getSkillsCopy()) {
			if (sl.getLevel() >= 2f && sl.getSkill().isElite()) {
				count++;
			}
		}
		return count;
	}
	
	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			int base = (int)Global.getSettings().getInt("officerMaxEliteSkills");
			return "\n*The base maximum number of elite skills per officer is " + base + "."; 
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
	
	public static class Level3 extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
				TooltipMakerAPI info, float width) {
			init(stats, skill);
			float opad = 10f;
			Color c = Misc.getBasePlayerColor();
			//info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "fleet");
			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "all ships with officers (but not AI cores), including flagship");
			info.addSpacer(opad);
			
			
			float count = getNumEliteSkills(stats);
			float bonus = count * BONUS_PER_ELITE_SKILL;
			
			info.addPara("%s damage dealt and %s damage taken (%s for each elite skill you have)", 0f, hc, hc,
					"+" + (int) Math.round(bonus) + "%",
					"-" + (int) Math.round(bonus) + "%",
					"" + (int) Math.round(BONUS_PER_ELITE_SKILL) + "%"
					);
			info.addPara("The damage-dealt bonus is doubled for the flagship", hc, 0f);
//			info.addPara("%s damage taken (%s per your elite skill), doubled for flagship", 0f, hc, hc,
//					"-" + (int) Math.round(bonus) + "%",
//					"" + (int) Math.round(BONUS_PER_ELITE_SKILL) + "%"
//					);
		}

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (isOfficer(stats)) {
				float count = getNumEliteSkillsOfFleetCommander(stats);
				float bonusDealt = count * BONUS_PER_ELITE_SKILL;
				float bonusTaken = count * BONUS_PER_ELITE_SKILL;
				if (isFlagship(stats)) {
					bonusDealt *= 2f;
					//bonusTaken *= 2f;
				}
				
				stats.getArmorDamageTakenMult().modifyMult(id, 1f - bonusTaken / 100f);
				stats.getHullDamageTakenMult().modifyMult(id, 1f - bonusTaken / 100f);
				stats.getShieldDamageTakenMult().modifyMult(id, 1f - bonusTaken / 100f);
				
				stats.getBallisticWeaponDamageMult().modifyPercent(id, bonusDealt);
				stats.getEnergyWeaponDamageMult().modifyPercent(id, bonusDealt);
				stats.getMissileWeaponDamageMult().modifyPercent(id, bonusDealt);
			}
		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getArmorDamageTakenMult().unmodifyMult(id);
			stats.getHullDamageTakenMult().unmodifyMult(id);
			stats.getShieldDamageTakenMult().unmodifyMult(id);
			
			stats.getBallisticWeaponDamageMult().unmodify(id);
			stats.getEnergyWeaponDamageMult().unmodify(id);
			stats.getMissileWeaponDamageMult().unmodify(id);
		}
	}
}




