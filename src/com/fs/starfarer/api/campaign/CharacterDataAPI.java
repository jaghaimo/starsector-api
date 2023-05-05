package com.fs.starfarer.api.campaign;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AdminData;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;

public interface CharacterDataAPI {

	PersonAPI getPerson();
	
	String getName();

	
	MemoryAPI getMemory();
	MemoryAPI getMemoryWithoutUpdate();

	Set<String> getAbilities();
	void addAbility(String id);
	void removeAbility(String id);

	Map<String, Object> getCustom();

	Set<String> getHullMods();
	void addHullMod(String id);
	void removeHullMod(String id);

	boolean knowsHullMod(String id);

	List<AdminData> getAdmins();
	void addAdmin(PersonAPI admin);
	void removeAdmin(PersonAPI admin);

	void setPortraitName(String portraitName);
	void setName(String name, Gender gender);

	Set<String> getSkillsEverMadeElite();

	String getSavefileVersion();

	void setSavefileVersion(String skillVersion);

	String getHonorific();
	void setHonorific(String honorific);

}
