package com.fs.starfarer.api.characters;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.characters.LevelBasedEffect.ScopeDescription;

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
	
	String getAuthor();
	void setAuthor(String author);
	int getTier();
	void setTier(int tier);
	
	boolean isElite();
	void setElite(boolean elite);
	boolean isPermanent();
	void setPermanent(boolean permanent);
	ScopeDescription getScope();
	void setScope(ScopeDescription scope);
	ScopeDescription getScope2();
	void setScope2(ScopeDescription scope2);
	String getScopeStr();
	void setScopeStr(String scopeStr);
	String getScopeStr2();
	void setScopeStr2(String scopeStr2);
	Color getGoverningAptitudeColor();
	String getGoverningAptitudeName();
	

}
