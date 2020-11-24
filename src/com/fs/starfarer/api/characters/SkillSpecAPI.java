package com.fs.starfarer.api.characters;

import java.util.Set;

public interface SkillSpecAPI {

	boolean isAptitudeEffect();
	String getId();
	String getDescription();
	void setDescription(String description);
	String getName();
	void setName(String name);
	String getGoverningAptitudeId();
	String getSpriteName();
	void setSpriteName(String spriteName);
	float getOrder();
	void setOrder(float order);
	boolean isCombatOfficerSkill();
	void setCombatOfficerSkill(boolean combatOfficerSkill);
	
	Set<String> getTags();
	void addTag(String tag);
	boolean hasTag(String tag);
	boolean isAdminSkill();
	boolean isAdmiralSkill();
	

}
