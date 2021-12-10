package com.fs.starfarer.api.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Skills;

public class SkillData {

	public static class SkillTier {
		public List<SkillSpecAPI> skills = new ArrayList<SkillSpecAPI>();
	}
	public static class SkillsForAptitude {
		public String aptitudeId;
		public List<List<SkillSpecAPI>> tiers = new ArrayList<List<SkillSpecAPI>>(); 
		public List<SkillSpecAPI> all = new ArrayList<SkillSpecAPI>();
		public SkillsForAptitude(String aptitudeId) {
			this.aptitudeId = aptitudeId;
		}
		
	}
	
	protected static Map<String, SkillsForAptitude> aptitudes = new HashMap<String, SkillsForAptitude>();
	
	public static SkillsForAptitude getSkills(String aptitudeId) {
		SkillsForAptitude skills = SkillData.getAptitudes().get(aptitudeId);
		if (skills == null) skills = new SkillsForAptitude(aptitudeId);
		return skills;
	}
	
	public static Map<String, SkillsForAptitude> getAptitudes() {
		compute();
		return aptitudes;
	}
	
	
	protected static boolean computed = false;
	protected static void compute() {
		if (computed) return;
		computed = true;
		
		aptitudes.clear();
		
		//List<String> aptitudeIds = new ArrayList<String>(Global.getSettings().getAptitudeIds());
		List<String> skillIds = new ArrayList<String>(Global.getSettings().getSkillIds());
		
		for (String skillId : skillIds) {
			SkillSpecAPI skill = Global.getSettings().getSkillSpec(skillId);
			if (skill.isAptitudeEffect()) continue;
			if (skill.hasTag(Skills.TAG_NPC_ONLY)) continue;
			if (skill.hasTag(Skills.TAG_DEPRECATED)) continue;
			
			String aptitudeId = skill.getGoverningAptitudeId();
			if (aptitudeId == null || aptitudeId.isEmpty()) continue;
			
			SkillsForAptitude skills = aptitudes.get(aptitudeId);
			if (skills == null) {
				skills = new SkillsForAptitude(aptitudeId);
				aptitudes.put(aptitudeId, skills);
			}
			
			skills.all.add(skill);
		}
		
		for (String aptitudeId : aptitudes.keySet()) {
			SkillsForAptitude skills = aptitudes.get(aptitudeId);
			
			Collections.sort(skills.all, new Comparator<SkillSpecAPI>() {
				public int compare(SkillSpecAPI o1, SkillSpecAPI o2) {
					int result = o1.getTier() - o2.getTier();
					if (result == 0) {
						result = (int) Math.signum(o1.getOrder() - o2.getOrder());
					}
					return result;
				}
			});
			
			boolean useTier = true;
			for (SkillSpecAPI skill : skills.all) {
				useTier &= skill.getReqPoints() == 0;
			}
			
			int currTier = -1;
			int prevReq = -1;
			List<SkillSpecAPI> soFar = new ArrayList<SkillSpecAPI>();
			for (SkillSpecAPI skill : skills.all) {
				if (!useTier) {
					if (skill.getReqPoints() != prevReq) {
						if (!soFar.isEmpty()) {
							skills.tiers.add(soFar);
						}
						soFar = new ArrayList<SkillSpecAPI>();
						prevReq = skill.getReqPoints();
					}
				} else {
					if (skill.getTier() != currTier) {
						if (!soFar.isEmpty()) {
							skills.tiers.add(soFar);
						}
						soFar = new ArrayList<SkillSpecAPI>();
						currTier = skill.getTier();
					}
				}
				soFar.add(skill);
			}
			if (!soFar.isEmpty()) {
				skills.tiers.add(soFar);
			} 
		}
	}
	
	
	
	
}










