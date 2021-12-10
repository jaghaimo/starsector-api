package com.fs.starfarer.api.characters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI.SkillLevelAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.plog.OfficerSkillGainRecord;
import com.fs.starfarer.api.impl.campaign.plog.PlaythroughLog;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation;
import com.fs.starfarer.api.util.Misc;

public class SkillsChangeOfficerEffect extends BaseSkillsChangeEffect {

	public static class OfficerEffectData {
		OfficerDataAPI data;
		int newLevel;
		List<String> removeSkills = new ArrayList<String>();
		List<String> removeElite = new ArrayList<String>();
		FleetMemberAPI member;
		boolean unusable = false;
		boolean makeMercenary;
		
		ButtonAPI buttonMerc;
		ButtonAPI buttonOther;
		
		public boolean hasChanges() {
			return data.getPerson().getStats().getLevel() != newLevel || unusable ||
					!removeSkills.isEmpty() || !removeElite.isEmpty() || makeMercenary;
		}
	}
	
	public static class OfficerDataMap {
		Map<PersonAPI, OfficerEffectData> map = new LinkedHashMap<PersonAPI, SkillsChangeOfficerEffect.OfficerEffectData>();
	}
	public void setMap(OfficerDataMap map, Map<String, Object> dataMap) {
		String key = getClass().getSimpleName();
		dataMap.put(key, map);
	}
	public OfficerDataMap getMap(Map<String, Object> dataMap) {
		String key = getClass().getSimpleName();
		OfficerDataMap map = (OfficerDataMap)dataMap.get(key);
		if (map == null) {
			map = new OfficerDataMap();
			dataMap.put(key, map);
		}
		return map;
	}
	
	
	public int getMaxLevel(MutableCharacterStatsAPI stats) {
		int bonus = (int) stats.getDynamic().getMod(Stats.OFFICER_MAX_LEVEL_MOD).computeEffective(0);
		return (int) Global.getSettings().getFloat("officerMaxLevel") + bonus;
	}
	
	public int getMaxEliteSkills(MutableCharacterStatsAPI stats) {
		int bonus = (int) stats.getDynamic().getMod(Stats.OFFICER_MAX_ELITE_SKILLS_MOD).computeEffective(0);
		return (int) Global.getSettings().getFloat("officerMaxEliteSkills") + bonus;
	}
	
	public int getNumEliteSkills(PersonAPI person) {
		int num = 0;
		for (SkillLevelAPI sl : person.getStats().getSkillsCopy()) {
			if (sl.getLevel() >= 2f) {
				num++;
			}
		}
		return num;
	}
	
	public OfficerDataMap getEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to) {
		OfficerDataMap result = new OfficerDataMap();
		
		
		int maxOfficersPre = (int) from.getOfficerNumber().getModifiedValue();
		int maxOfficersPost = (int) to.getOfficerNumber().getModifiedValue();

		Map<OfficerDataAPI, FleetMemberAPI> members = new LinkedHashMap<OfficerDataAPI, FleetMemberAPI>();
		Set<OfficerDataAPI> unusable = new LinkedHashSet<OfficerDataAPI>();
		if (maxOfficersPre > maxOfficersPost) {
			int count = 0;
			for (OfficerDataAPI officer : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()) {
				boolean merc = Misc.isMercenary(officer.getPerson());
				if (!merc) {
					count++;
				}
				if (count > maxOfficersPost && !merc) {
					unusable.add(officer);
				}
			}
		}
		
		for (OfficerDataAPI officer : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()) {
			for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
				if (member.getCaptain() == officer.getPerson()) {
					members.put(officer, member);
					break;
				}
			}
		}
		
		int maxLevelPre = getMaxLevel(from);
		int maxLevelPost = getMaxLevel(to);
		
		int maxElitePre = getMaxEliteSkills(from);
		int maxElitePost = getMaxEliteSkills(to);
		
		for (OfficerDataAPI data : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()) {
			PersonAPI person = data.getPerson();
			MutableCharacterStatsAPI stats = person.getStats();
			
			if (Misc.isMercenary(person)) {
				continue;
			}
			
			boolean pods = person.getMemoryWithoutUpdate().getBoolean(MemFlags.EXCEPTIONAL_SLEEPER_POD_OFFICER);
			if (pods) continue;
			
			OfficerEffectData effect = new OfficerEffectData();
			effect.data = data;
			effect.newLevel = stats.getLevel();
			
			if (members.containsKey(data)) {
				effect.member = members.get(data);
			}
			if (unusable.contains(data)) {
				effect.unusable = true;
			}
			
			if (stats.getLevel() > maxLevelPost && maxLevelPost < maxLevelPre) {
				final List<String> skillsInOrderOfGain = new ArrayList<String>();
				for (OfficerSkillGainRecord rec : PlaythroughLog.getInstance().getOfficerSkillsLearned()) {
					if (rec.getPersonId().equals(person.getId()) && !rec.isElite() &&
							stats.getSkillLevel(rec.getSkillId()) > 0) {
						skillsInOrderOfGain.add(rec.getSkillId());
					}
				}
				Collections.reverse(skillsInOrderOfGain);
				
				// if the skill records aren't present, assume it's a cryopods officer
				// it can also be an officer from 0.95a before officer skill gain was tracked
				// so, erring on the side of not nuking skills needlessly
				int skillsToRemove = stats.getLevel() - maxLevelPost;
				for (int i = 0; i < skillsToRemove && !skillsInOrderOfGain.isEmpty(); i++) {
					effect.removeSkills.add(skillsInOrderOfGain.remove(0));
				}
				if (!effect.removeSkills.isEmpty()) {
					effect.newLevel = maxLevelPost;
				}
			}
			
			int numElite = getNumEliteSkills(person);
			if (numElite > maxElitePost && maxElitePost < maxElitePre) {
				final List<String> eliteInOrderOfGain = new ArrayList<String>();
				for (OfficerSkillGainRecord rec : PlaythroughLog.getInstance().getOfficerSkillsLearned()) {
					if (rec.getPersonId().equals(person.getId()) && rec.isElite() &&
							stats.getSkillLevel(rec.getSkillId()) >= 2 &&
							!effect.removeSkills.contains(rec.getSkillId())) {
						eliteInOrderOfGain.add(rec.getSkillId());
					}
				}
				Collections.reverse(eliteInOrderOfGain);
				
				int eliteToRemove = numElite - maxElitePost;
				for (int i = 0; i < eliteToRemove && !eliteInOrderOfGain.isEmpty(); i++) {
					effect.removeElite.add(eliteInOrderOfGain.remove(0));
				}
			}
			
			
			if (effect.hasChanges()) {
				result.map.put(person, effect);
			}
		}
		return result;
	}
	
	
	@Override
	public boolean hasEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to) {
		return !getEffects(from, to).map.isEmpty();
	}

	@Override
	public void printEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to, TooltipMakerAPI info, Map<String, Object> dataMap) {
		super.prepare();
		
		OfficerDataMap map = getEffects(from, to);
		setMap(map, dataMap);
		
		float bw = 470;
		float bh = 25;
		float pad = 3f;
		float opad = 10f;
		
		
		info.addSectionHeading("Officers", base, dark, Alignment.MID, 15f);
		
		info.addPara("Officers whose skills that exceed the new limits will have their excess skills removed, or become mercenaries on a temporary contract. "
				+ "Officers that already had those kinds of skills when acquired will not be affected.", opad,
				Misc.getNegativeHighlightColor(),
				"excess skills removed");
		float initPad = opad;
		for (final OfficerEffectData data : map.map.values()) {
			final PersonAPI person = data.data.getPerson();
			String str = person.getRank() + " " + person.getNameString();
			if (data.member == null) {
				str += ", unassigned, will...";
			} else {
				str += ", commanding the " + data.member.getShipName() +
						" (" + data.member.getHullSpec().getHullNameWithDashClass() + "), will...";
			}

			String mercText = "Become a mercenary, drawing higher pay on a new contract";
			String otherText = "";
			
			if (data.unusable && data.removeElite.isEmpty() && data.removeSkills.isEmpty()) {
				if (data.member == null) {
					otherText = "Become unable to be assigned command of a ship";
				} else {
					otherText = "Be relieved of command, and unable to be assigned command of a ship";
				}
			} else {
				String part1 = "";
				if (data.unusable) {
					part1 = "Be relieved of command, and";
				}
				String part2 = "";
				if (!data.removeElite.isEmpty() && data.removeSkills.isEmpty()) {
					part2 = " lose excess elite skill effects";
				} else if (data.removeElite.isEmpty() && !data.removeSkills.isEmpty()) {
					part2 = " lose excess skills";
				} else {
					part2 = " lose excess skills and elite skill effects";
				}
				if (part1.isEmpty()) {
					part2 = part2.trim();
					otherText = Misc.ucFirst(part2);
				} else {
					otherText = part1 + part2;
				}
			}
			
			info.addPara(str, opad + 5f);
		
			float indent = 40f;
			data.buttonOther = info.addAreaCheckbox(otherText, new Object(), base, dark, bright, bw, bh, opad, true);
			data.buttonOther.getPosition().setXAlignOffset(indent);
			
			info.addTooltipToPrevious(new TooltipCreator() {
				public boolean isTooltipExpandable(Object tooltipParam) {
					return false;
				}
				public float getTooltipWidth(Object tooltipParam) {
					return 450;
				}
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					float opad = 10f;
					float pad = 3f;
					tooltip.addPara("This officer has the following skills:", 0f);
					tooltip.addSkillPanel(person, pad);
					
					if (!data.removeSkills.isEmpty()) {
						tooltip.addPara("They will lose the following skills:", opad);
						PersonAPI fake = Global.getFactory().createPerson();
						for (String skillId : data.removeSkills) {
							fake.getStats().setSkillLevel(skillId, person.getStats().getSkillLevel(skillId));
						}
						tooltip.addSkillPanel(fake, pad);
					}
					if (!data.removeElite.isEmpty()) {
						tooltip.addPara("They will lose the elite effects of the following skills:", opad);
						PersonAPI fake = Global.getFactory().createPerson();
						for (String skillId : data.removeElite) {
							fake.getStats().setSkillLevel(skillId, person.getStats().getSkillLevel(skillId));
						}
						tooltip.addSkillPanel(fake, pad);
					}
				}
			}, TooltipLocation.RIGHT);
			
			//data.buttonMerc = info.addAreaCheckbox(mercText, new Object(), base, dark, bright, bw, bh, pad, true);
			data.buttonMerc = info.addAreaCheckbox(mercText, new Object(), sBase, sDark, sBright, bw, bh, pad, true);
			data.buttonOther.setChecked(true);
			data.buttonMerc.setChecked(false);
			
			info.addTooltipToPrevious(new TooltipCreator() {
				public boolean isTooltipExpandable(Object tooltipParam) {
					return false;
				}
				public float getTooltipWidth(Object tooltipParam) {
					return 450;
				}
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					int payPre = (int) Misc.getOfficerSalary(person, false);
					int payPost = (int) Misc.getOfficerSalary(person, true);
					int contractDur = (int) Global.getSettings().getFloat("officerMercContractDur");
					tooltip.addPara("This officer's pay will be increased from %s to %s per month,"
							+ " and they will want to leave after %s days, when their contract expires.", 0f,
							Misc.getHighlightColor(), 
							Misc.getDGSCredits(payPre),
							Misc.getDGSCredits(payPost),
							"" + contractDur
							);
					tooltip.addPara("Extending the contract beyond that will require a %s.", 10f,
							Misc.getStoryOptionColor(), Misc.STORY + " point");
				}
			}, TooltipLocation.RIGHT);
			
			info.addSpacer(0).getPosition().setXAlignOffset(-indent);
		}
	}
	
	@Override
	public void infoButtonPressed(ButtonAPI button, Object param, Map<String, Object> dataMap) {
		OfficerDataMap map = getMap(dataMap);
		
		for (OfficerEffectData data : map.map.values()) {
			if (data.buttonMerc == button) {
				data.makeMercenary = true;
				data.buttonMerc.setChecked(true);
				data.buttonOther.setChecked(false);
				//System.out.println("Merc button, " + data.data.getPerson().getNameString());
			} else if (data.buttonOther == button) {
				data.makeMercenary = false;
				data.buttonMerc.setChecked(false);
				data.buttonOther.setChecked(true);
				//System.out.println("Other button, " + data.data.getPerson().getNameString());
			}
		}
	}
	
	@Override
	public void applyEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to, Map<String, Object> dataMap) {
		OfficerDataMap map = getMap(dataMap);
		
		for (OfficerEffectData data : map.map.values()) {
			if (data.makeMercenary) {
				Misc.setMercenary(data.data.getPerson(), true);
				Misc.setMercHiredNow(data.data.getPerson());
			} else {
				if (data.unusable && data.member != null) {
					data.member.setCaptain(Global.getFactory().createPerson());
				}
				MutableCharacterStatsAPI stats = data.data.getPerson().getStats();
				for (String id : data.removeElite) {
					stats.setSkillLevel(id, 1);
					PlaythroughLog.getInstance().removeOfficerSkillRecord(data.data.getPerson().getId(), id, true);
				}
				for (String id : data.removeSkills) {
					stats.setSkillLevel(id, 0);
					PlaythroughLog.getInstance().removeOfficerSkillRecord(data.data.getPerson().getId(), id, false);
					PlaythroughLog.getInstance().removeOfficerSkillRecord(data.data.getPerson().getId(), id, true);
				}
				if (data.newLevel != stats.getLevel()) {
					stats.setLevel(data.newLevel);
				}
			}
			
		}
	}

	
}



