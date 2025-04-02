package com.fs.starfarer.api.characters;

import java.util.List;
import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.characters.LevelBasedEffect.ScopeDescription;
import com.fs.starfarer.api.loading.WithSourceMod;

public interface SkillSpecAPI extends WithSourceMod {

	public interface SkillEffectSpecAPI {
		String getGoverningSkill();
		SkillEffectType getType();
		ShipSkillEffect getAsShipEffect();
		FleetTotalSource getAsFleetTotalSource();
		AfterShipCreationSkillEffect getAsAfterShipCreationEffect();
		MarketSkillEffect getAsMarketEffect();
		CharacterStatsSkillEffect getAsStatsEffect();
		FleetStatsSkillEffect getAsFleetEffect();
		LevelBasedEffect getAsLevelBasedEffect();
		DescriptionSkillEffect getAsDescriptionEffect();
		String getEffectClass();
		//List<HullModUnlock> getHullModUnlocks();
		List<String> getAbilityUnlocks();
		List<String> getUnlockedHullMods(float level);
		boolean isLevelBased();
		void setLevelBased(boolean levelBased);
		float getRequiredSkillLevel();
		void setRequiredSkillLevel(float requiredSkillLevel);
		void setGoverningSkill(String governingSkill);
		String getName();
		void setName(String name);
		List<String> getHullmods();
	}
	
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
	int getReqPoints();
	void setReqPoints(int reqPoints);
	int getReqPointsPer();
	void setReqPointsPer(int reqPointsPer);
	Set<String> getAllHullmodUnlocks();
	Set<String> getAllAbilityUnlocks();
	int getGoverningAptitudeOrder();
	List<SkillEffectSpecAPI> getEffectsAPI();
	

}
