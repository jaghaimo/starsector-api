package com.fs.starfarer.api.impl.campaign;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI.SkillLevelAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.SkillData;
import com.fs.starfarer.api.util.SkillData.SkillsForAptitude;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class OfficerLevelupPluginImpl implements OfficerLevelupPlugin {

	//public static float XP_MULT = 5f;
	public static float XP_MULT = Global.getSettings().getFloat("officerXPRequiredMult");
	
	public static float EXPONENT_BEYOND_MAX_SPECIFIED_LEVEL = 1.1f;
	
	public static long [] XP_PER_LEVEL = new long [] {
		0,		// level 1
		12000,
		20000,
		35000,
		45000,  // level 5
		50000,
		50000,
		50000,
		50000,
		50000,
	};
	
	public static long [] TOTAL_XP_PER_LEVEL = new long [XP_PER_LEVEL.length];
	
	static {
		long total = 0;
		for (int i = 0; i < XP_PER_LEVEL.length; i++) {
			total += XP_PER_LEVEL[i];
			TOTAL_XP_PER_LEVEL[i] = total;
		}
	}
	
	public long getXPForLevel(int level) {
		if (level <= 1) return 0;
		
		if (level - 1 < TOTAL_XP_PER_LEVEL.length) {
			return (long) (TOTAL_XP_PER_LEVEL[level - 1] * XP_MULT);
		}
		
		int maxSpecified = TOTAL_XP_PER_LEVEL.length;
		long curr = TOTAL_XP_PER_LEVEL[maxSpecified - 1];
		long last = XP_PER_LEVEL[maxSpecified - 1];
		for (int i = maxSpecified; i < level; i++) {
			last *= EXPONENT_BEYOND_MAX_SPECIFIED_LEVEL;
			curr += last;
		}

		return (long) (curr * XP_MULT);
	}
	
	public static void main(String[] args) {
		for (int i = 1; i <= 6; i++) {
			System.out.println("Level " + i + ": " + new OfficerLevelupPluginImpl().getXPForLevel(i));
		}
	}

	public int getMaxLevel(PersonAPI person) {
		if (person != null && person.getMemoryWithoutUpdate().contains(MemFlags.OFFICER_MAX_LEVEL)) {
			return person.getMemoryWithoutUpdate().getInt(MemFlags.OFFICER_MAX_LEVEL);
		}
		int bonus = 0;
		if (person != null) {
			MutableCharacterStatsAPI stats = person.getFleetCommanderStats();
			if (stats != null) {
				bonus = (int) stats.getDynamic().getMod(Stats.OFFICER_MAX_LEVEL_MOD).computeEffective(0);
			}
		}
		return (int) Global.getSettings().getFloat("officerMaxLevel") + bonus;
	}
	
	public int getMaxEliteSkills(PersonAPI person) {
		if (person != null && person.getMemoryWithoutUpdate().contains(MemFlags.OFFICER_MAX_ELITE_SKILLS)) {
			return person.getMemoryWithoutUpdate().getInt(MemFlags.OFFICER_MAX_ELITE_SKILLS);
		}
		int bonus = 0;
		if (person != null) {
			MutableCharacterStatsAPI stats = person.getFleetCommanderStats();
			if (stats != null) {
				bonus = (int) stats.getDynamic().getMod(Stats.OFFICER_MAX_ELITE_SKILLS_MOD).computeEffective(0);
			}
		}
		return (int) Global.getSettings().getFloat("officerMaxEliteSkills") + bonus;
	}

	public List<String> pickLevelupSkillsV2(PersonAPI person, Random random) {
		if (random == null) random = new Random();
		
		
		List<SkillSpecAPI> leftovers = new ArrayList<SkillSpecAPI>();
		List<List<SkillSpecAPI>> unknownTiers = new ArrayList<List<SkillSpecAPI>>();
		
		MutableCharacterStatsAPI stats = person.getStats();
		int level = stats.getLevel();
		
		for (String ap : SkillData.getAptitudes().keySet()) {
			SkillsForAptitude skills = SkillData.getSkills(ap);
			int tier = 0;
			for (List<SkillSpecAPI> list : skills.tiers) {
				tier++;
				
				List<SkillSpecAPI> unknown = new ArrayList<SkillSpecAPI>();
				for (SkillSpecAPI skill : list) {
					if (!skill.isCombatOfficerSkill()) continue;
					if (skill.hasTag(Skills.TAG_DEPRECATED)) continue;
					if (skill.hasTag(Skills.TAG_PLAYER_ONLY)) continue;
					if (stats.getSkillLevel(skill.getId()) <= 0) {
						unknown.add(skill);
					}
				}
				if (list.size() == unknown.size() && (tier < 4 || level >= 3)) {
					unknownTiers.add(list);
				} else {
					leftovers.addAll(unknown);
				}
			}
		}
		
		int max = 4;
		if (Misc.isMentored(person)) {
			max = 6;
		}
		List<String> result = new ArrayList<String>();
		
		if (!unknownTiers.isEmpty()) {
			WeightedRandomPicker<List<SkillSpecAPI>> picker = new WeightedRandomPicker<List<SkillSpecAPI>>(random);
			picker.addAll(unknownTiers);
			while (!picker.isEmpty() && result.size() < max) {
				List<SkillSpecAPI> pick = picker.pickAndRemove();
				for (SkillSpecAPI s : pick) {
					if (result.size() >= max) break; 
					result.add(s.getId());
				}
			}
		}
		
		if (!leftovers.isEmpty()) {
			WeightedRandomPicker<SkillSpecAPI> picker = new WeightedRandomPicker<SkillSpecAPI>(random);
			picker.addAll(leftovers);
			while (!picker.isEmpty() && result.size() < max) {
				SkillSpecAPI pick = picker.pickAndRemove();
				result.add(pick.getId());
			}
		}
		
		return result;
	}
	
	
	public List<String> pickLevelupSkillsV3(PersonAPI person, Random random) {
		if (random == null) random = new Random();
		
		
		List<SkillSpecAPI> top = new ArrayList<SkillSpecAPI>();
		List<SkillSpecAPI> leftovers = new ArrayList<SkillSpecAPI>();
		
		MutableCharacterStatsAPI stats = person.getStats();
		int level = stats.getLevel();
		
		for (String ap : SkillData.getAptitudes().keySet()) {
			SkillsForAptitude skills = SkillData.getSkills(ap);
			for (List<SkillSpecAPI> list : skills.tiers) {
				boolean topTier = false;
				for (SkillSpecAPI skill : list) {
					if (!skill.isCombatOfficerSkill()) continue;
					if (skill.hasTag(Skills.TAG_DEPRECATED)) continue;
					if (skill.hasTag(Skills.TAG_PLAYER_ONLY)) continue;
					if (stats.getSkillLevel(skill.getId()) <= 0) {
						if (skill.getTier() == 5) topTier = true;
						if (!topTier || level >= 3) {
							if (topTier) {
								top.add(skill);
							} else {
								leftovers.add(skill);
							}
						}
					}
				}
			}
		}
		
		int max = 4;
		if (Misc.isMentored(person)) {
			max = 6;
		}
		if (person.getMemoryWithoutUpdate().contains(MemFlags.OFFICER_SKILL_PICKS_PER_LEVEL)) {
			max = person.getMemoryWithoutUpdate().getInt(MemFlags.OFFICER_SKILL_PICKS_PER_LEVEL);
		}
		List<String> result = new ArrayList<String>();
		
		if (!top.isEmpty()) {
			WeightedRandomPicker<SkillSpecAPI> picker = new WeightedRandomPicker<SkillSpecAPI>(random);
			picker.addAll(top);
			while (!picker.isEmpty() && result.size() < max) {
				SkillSpecAPI pick = picker.pickAndRemove();
				result.add(pick.getId());
			}
		}
		if (!leftovers.isEmpty()) {
			WeightedRandomPicker<SkillSpecAPI> picker = new WeightedRandomPicker<SkillSpecAPI>(random);
			picker.addAll(leftovers);
			while (!picker.isEmpty() && result.size() < max) {
				SkillSpecAPI pick = picker.pickAndRemove();
				result.add(pick.getId());
			}
		}
		
		return result;
	}
	
	
	public List<String> pickLevelupSkills(PersonAPI person, Random random) {
		if (true) return pickLevelupSkillsV3(person, random);
		if (random == null) random = new Random();
		
		boolean hasCarrierSkills = false;
		for (SkillLevelAPI skill : person.getStats().getSkillsCopy()) {
			if (!skill.getSkill().isCombatOfficerSkill()) continue;
			
			if (skill.getSkill().hasTag(Skills.TAG_CARRIER)) {
				hasCarrierSkills = true;
				break;
			}
		}
		
		WeightedRandomPicker<String> nonMaxedSkills = new WeightedRandomPicker<String>(random);
		WeightedRandomPicker<String> knownSkills = new WeightedRandomPicker<String>(random);
		WeightedRandomPicker<String> carrierSkills = new WeightedRandomPicker<String>(random);
		WeightedRandomPicker<String> knownCarrierSkills = new WeightedRandomPicker<String>(random);
		WeightedRandomPicker<String> nonCarrierSkills = new WeightedRandomPicker<String>(random);
		WeightedRandomPicker<String> knownNonCarrierSkills = new WeightedRandomPicker<String>(random);
		List<String> allSkillIds = Global.getSettings().getSortedSkillIds();
		int knownSkillCount = 0;
		for (String skillId : allSkillIds) {
			SkillSpecAPI skill = Global.getSettings().getSkillSpec(skillId);
			if (skill.isCombatOfficerSkill()) {
				addSkill(person, nonMaxedSkills, skillId);
				float level = person.getStats().getSkillLevel(skillId);
				if (level > 0) {
					knownSkillCount++;
					addSkill(person, knownSkills, skillId);
				}
				if (skill.hasTag(Skills.TAG_CARRIER)) {
					addSkill(person, carrierSkills, skillId);
					if (level > 0) addSkill(person, knownCarrierSkills, skillId);
				} else {
					addSkill(person, nonCarrierSkills, skillId);
					if (level > 0) addSkill(person, knownNonCarrierSkills, skillId);
				}
			}
		}
		List<String> result = new ArrayList<String>();

		if (!knownSkills.isEmpty()) {
			String pick = knownSkills.pickAndRemove();
			nonMaxedSkills.remove(pick);
			carrierSkills.remove(pick);
			nonCarrierSkills.remove(pick);
			knownCarrierSkills.remove(pick);
			knownNonCarrierSkills.remove(pick);
			result.add(pick);
		}
		
		int maxSkillsPerOfficer = 10;
		if (knownSkillCount >= maxSkillsPerOfficer) {
			if (hasCarrierSkills && !knownCarrierSkills.isEmpty()) {
				String pick = knownCarrierSkills.pickAndRemove();
				result.add(pick);
			} else if (!knownNonCarrierSkills.isEmpty() && result.size() == 0) {
				String pick = knownNonCarrierSkills.pickAndRemove();
				result.add(pick);
			} else if (!knownSkills.isEmpty()) {
				String pick = knownSkills.pickAndRemove();
				result.add(pick);
			}
		} else {
			if (hasCarrierSkills && !carrierSkills.isEmpty()) {
				String pick = carrierSkills.pickAndRemove();
				result.add(pick);
			} else if (!nonCarrierSkills.isEmpty() && result.size() == 0) {
				String pick = nonCarrierSkills.pickAndRemove();
				result.add(pick);
			} else if (!nonMaxedSkills.isEmpty()) {
				String pick = nonMaxedSkills.pickAndRemove();
				result.add(pick);
			}
		}
		
		if (result.size() < 2 && knownSkillCount < maxSkillsPerOfficer) {
			for (String id : result) {
				nonMaxedSkills.remove(id);
			}
			if (!nonMaxedSkills.isEmpty()) {
				String pick = nonMaxedSkills.pickAndRemove();
				knownSkills.remove(pick);
				result.add(pick);
			}
		}
		
		return result;
	}
	
	
	
	private void addSkill(PersonAPI person, WeightedRandomPicker<String> picker, String skill) {
		if (person.getStats().getSkillLevel(skill) >= 3) return;
		picker.add(skill);
	}
	
	

//	public static void main(String[] args) {
//		LevelupPluginImpl impl = new LevelupPluginImpl();
//		for (int i = 0; i <= 100; i++) {
//			System.out.println(String.format("% 4d: % 20d", i, impl.getXPForLevel(i)));
//		}
//	}

}





