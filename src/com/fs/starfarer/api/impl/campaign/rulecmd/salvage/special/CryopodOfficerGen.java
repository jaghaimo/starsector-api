package com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class CryopodOfficerGen {

	public static String EXCEPTIONAL_OFFICERS_CREATED_KEY = "$SleeperPodsSpecialCreator_exceptionalCount";
	
	public static int getNumExceptionalCreated() {
		return Global.getSector().getMemoryWithoutUpdate().getInt(EXCEPTIONAL_OFFICERS_CREATED_KEY);
	}
	
	public static void incrNumExceptionalCreated() {
		int num = getNumExceptionalCreated() + 1;
		Global.getSector().getMemoryWithoutUpdate().set(EXCEPTIONAL_OFFICERS_CREATED_KEY, num);
	}
	
	public static boolean canAddMoreExceptional() {
		return getNumExceptionalCreated() < SalvageSpecialAssigner.MAX_EXCEPTIONAL_PODS_OFFICERS;
	}
	
	
	
	public static class CryopodOfficerTemplate {
		public List<String> base = new ArrayList<String>();
		public List<String> elite = new ArrayList<String>();
		
		public String personality = null;
		
		
		public CryopodOfficerTemplate() {
		}
		public CryopodOfficerTemplate(String personality) {
			this.personality = personality;
		}

		public PersonAPI create(FactionAPI faction, Random random) {
			PersonAPI officer = faction.createRandomPerson(random);
			if (personality != null) {
				officer.setPersonality(personality);
			}
			
			int level = base.size() + elite.size();
			officer.getStats().setLevel(level);
			
			OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
			officer.getStats().setXP(plugin.getXPForLevel(level));
			
			for (String id : base) {
				officer.getStats().setSkillLevel(id, 1);
			}
			for (String id : elite) {
				officer.getStats().setSkillLevel(id, 2);
			}
			return officer;
		}
	}
	
	public static WeightedRandomPicker<CryopodOfficerTemplate> TEMPLATES_NORMAL = new WeightedRandomPicker<CryopodOfficerTemplate>();
	public static WeightedRandomPicker<CryopodOfficerTemplate> TEMPLATES_EXCEPTIONAL = new WeightedRandomPicker<CryopodOfficerTemplate>();

	static {
		CryopodOfficerTemplate t;
		
		// BEGIN LEVEL 7 OFFICERS
		
		// fast high-tech ship
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.TARGET_ANALYSIS);
		t.elite.add(Skills.ENERGY_WEAPON_MASTERY);
		t.elite.add(Skills.FIELD_MODULATION);
		t.elite.add(Skills.GUNNERY_IMPLANTS);
		t.elite.add(Skills.SYSTEMS_EXPERTISE);
		t.base.add(Skills.COMBAT_ENDURANCE);
		t.base.add(Skills.HELMSMANSHIP);
		TEMPLATES_EXCEPTIONAL.add(t, 10f);
		
		// slow high-tech ship
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.HELMSMANSHIP);
		t.elite.add(Skills.ENERGY_WEAPON_MASTERY);
		t.elite.add(Skills.FIELD_MODULATION);
		t.elite.add(Skills.GUNNERY_IMPLANTS);
		t.elite.add(Skills.ORDNANCE_EXPERTISE);
		t.base.add(Skills.TARGET_ANALYSIS);
		t.base.add(Skills.COMBAT_ENDURANCE);
		TEMPLATES_EXCEPTIONAL.add(t, 10f);
		
		// hull/armor tank, low tech
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.DAMAGE_CONTROL);
		t.elite.add(Skills.IMPACT_MITIGATION);
		t.elite.add(Skills.POLARIZED_ARMOR);
		t.elite.add(Skills.BALLISTIC_MASTERY);
		t.elite.add(Skills.TARGET_ANALYSIS);
		t.base.add(Skills.MISSILE_SPECIALIZATION);
		t.base.add(Skills.GUNNERY_IMPLANTS);
		TEMPLATES_EXCEPTIONAL.add(t, 5f);
		
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.ORDNANCE_EXPERTISE);
		t.elite.add(Skills.IMPACT_MITIGATION);
		t.elite.add(Skills.POLARIZED_ARMOR);
		t.elite.add(Skills.BALLISTIC_MASTERY);
		t.elite.add(Skills.TARGET_ANALYSIS);
		t.base.add(Skills.MISSILE_SPECIALIZATION);
		t.base.add(Skills.GUNNERY_IMPLANTS);
		TEMPLATES_EXCEPTIONAL.add(t, 5f);
		
		// phase ship	
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.IMPACT_MITIGATION);
		t.elite.add(Skills.FIELD_MODULATION);
		t.elite.add(Skills.TARGET_ANALYSIS);
		t.elite.add(Skills.SYSTEMS_EXPERTISE);
		t.elite.add(Skills.COMBAT_ENDURANCE);
		t.base.add(Skills.POLARIZED_ARMOR);
		t.base.add(Skills.ENERGY_WEAPON_MASTERY);
		TEMPLATES_EXCEPTIONAL.add(t, 10f);
		

		// generally-ok-for-most-ships, take 1
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.FIELD_MODULATION);
		t.elite.add(Skills.ORDNANCE_EXPERTISE);
		t.elite.add(Skills.TARGET_ANALYSIS);
		t.elite.add(Skills.POINT_DEFENSE);
		t.elite.add(Skills.GUNNERY_IMPLANTS);
		t.base.add(Skills.HELMSMANSHIP);
		t.base.add(Skills.COMBAT_ENDURANCE);
		TEMPLATES_EXCEPTIONAL.add(t, 5f);
		
		// generally-ok-for-most-ships, take 2
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.FIELD_MODULATION);
		t.elite.add(Skills.ORDNANCE_EXPERTISE);
		t.elite.add(Skills.TARGET_ANALYSIS);
		t.elite.add(Skills.IMPACT_MITIGATION);
		t.elite.add(Skills.GUNNERY_IMPLANTS);
		t.base.add(Skills.HELMSMANSHIP);
		t.base.add(Skills.COMBAT_ENDURANCE);
		TEMPLATES_EXCEPTIONAL.add(t, 5f);
		
		
		// SO, ballistic weapons
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.COMBAT_ENDURANCE);
		t.elite.add(Skills.TARGET_ANALYSIS);
		t.elite.add(Skills.SYSTEMS_EXPERTISE);
		t.elite.add(Skills.DAMAGE_CONTROL);
		t.elite.add(Skills.IMPACT_MITIGATION);
		t.base.add(Skills.FIELD_MODULATION);
		t.base.add(Skills.BALLISTIC_MASTERY);
		TEMPLATES_EXCEPTIONAL.add(t, 5f);
		
		// SO, energy weapons
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.COMBAT_ENDURANCE);
		t.elite.add(Skills.TARGET_ANALYSIS);
		t.elite.add(Skills.SYSTEMS_EXPERTISE);
		t.elite.add(Skills.DAMAGE_CONTROL);
		t.elite.add(Skills.IMPACT_MITIGATION);
		t.base.add(Skills.FIELD_MODULATION);
		t.base.add(Skills.ENERGY_WEAPON_MASTERY);
		TEMPLATES_EXCEPTIONAL.add(t, 5f);
		// END LEVEL 7 OFFICERS
		
		
		// BEGIN LEVEL 5 OFFICERS
		
		// generic, take 1
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.IMPACT_MITIGATION);
		t.base.add(Skills.ORDNANCE_EXPERTISE);
		t.base.add(Skills.FIELD_MODULATION);
		t.base.add(Skills.TARGET_ANALYSIS);
		t.base.add(Skills.COMBAT_ENDURANCE);
		TEMPLATES_NORMAL.add(t, 5f);
		
		// generic, take 2
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.TARGET_ANALYSIS);
		t.base.add(Skills.IMPACT_MITIGATION);
		t.base.add(Skills.ORDNANCE_EXPERTISE);
		t.base.add(Skills.FIELD_MODULATION);
		t.base.add(Skills.COMBAT_ENDURANCE);
		TEMPLATES_NORMAL.add(t, 5f);
		
		// generic, take 3
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.SYSTEMS_EXPERTISE);
		t.base.add(Skills.TARGET_ANALYSIS);
		t.base.add(Skills.ORDNANCE_EXPERTISE);
		t.base.add(Skills.FIELD_MODULATION);
		t.base.add(Skills.COMBAT_ENDURANCE);
		TEMPLATES_NORMAL.add(t, 5f);
		
		// low-tech, take 1
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.MISSILE_SPECIALIZATION);
		t.base.add(Skills.TARGET_ANALYSIS);
		t.base.add(Skills.BALLISTIC_MASTERY);
		t.base.add(Skills.IMPACT_MITIGATION);
		t.base.add(Skills.COMBAT_ENDURANCE);
		TEMPLATES_NORMAL.add(t, 5f);
		
		// low-tech, take 2
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.BALLISTIC_MASTERY);
		t.base.add(Skills.GUNNERY_IMPLANTS);
		t.base.add(Skills.TARGET_ANALYSIS);
		t.base.add(Skills.IMPACT_MITIGATION);
		t.base.add(Skills.COMBAT_ENDURANCE);
		TEMPLATES_NORMAL.add(t, 5f);
		
		// low-tech, take 3
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.BALLISTIC_MASTERY);
		t.base.add(Skills.MISSILE_SPECIALIZATION);
		t.base.add(Skills.TARGET_ANALYSIS);
		t.base.add(Skills.IMPACT_MITIGATION);
		t.base.add(Skills.DAMAGE_CONTROL);
		TEMPLATES_NORMAL.add(t, 5f);
		
		// low-tech, take 4
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.HELMSMANSHIP);
		t.base.add(Skills.MISSILE_SPECIALIZATION);
		t.base.add(Skills.TARGET_ANALYSIS);
		t.base.add(Skills.IMPACT_MITIGATION);
		t.base.add(Skills.BALLISTIC_MASTERY);
		TEMPLATES_NORMAL.add(t, 5f);
		
		// high-tech, take 1
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.ENERGY_WEAPON_MASTERY);
		t.base.add(Skills.TARGET_ANALYSIS);
		t.base.add(Skills.MISSILE_SPECIALIZATION);
		t.base.add(Skills.ORDNANCE_EXPERTISE);
		t.base.add(Skills.COMBAT_ENDURANCE);
		TEMPLATES_NORMAL.add(t, 5f);
		
		// high-tech, take 2
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.ENERGY_WEAPON_MASTERY);
		t.base.add(Skills.TARGET_ANALYSIS);
		t.base.add(Skills.HELMSMANSHIP);
		t.base.add(Skills.ORDNANCE_EXPERTISE);
		t.base.add(Skills.COMBAT_ENDURANCE);
		TEMPLATES_NORMAL.add(t, 5f);

		// phase ship, take 1	
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.FIELD_MODULATION);
		t.base.add(Skills.IMPACT_MITIGATION);
		t.base.add(Skills.TARGET_ANALYSIS);
		t.base.add(Skills.SYSTEMS_EXPERTISE);
		t.base.add(Skills.COMBAT_ENDURANCE);
		TEMPLATES_NORMAL.add(t, 5f);
		
		// phase ship, take 2	
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.FIELD_MODULATION);
		t.base.add(Skills.IMPACT_MITIGATION);
		t.base.add(Skills.TARGET_ANALYSIS);
		t.base.add(Skills.MISSILE_SPECIALIZATION);
		t.base.add(Skills.COMBAT_ENDURANCE);
		TEMPLATES_NORMAL.add(t, 5f);
		

		// SO? fairly generic
		t = new CryopodOfficerTemplate();
		t.elite.add(Skills.TARGET_ANALYSIS);
		t.base.add(Skills.COMBAT_ENDURANCE);
		t.base.add(Skills.MISSILE_SPECIALIZATION);
		t.base.add(Skills.IMPACT_MITIGATION);
		t.base.add(Skills.FIELD_MODULATION);
		TEMPLATES_NORMAL.add(t, 10f);
		
		// END LEVEL 5 OFFICERS
		
	}
	
}
























