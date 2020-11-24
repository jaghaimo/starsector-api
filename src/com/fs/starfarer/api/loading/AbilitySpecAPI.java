package com.fs.starfarer.api.loading;

import java.util.Set;

import com.fs.starfarer.api.campaign.ai.AbilityAIPlugin;
import com.fs.starfarer.api.characters.AbilityPlugin;

public interface AbilitySpecAPI {

	String getId();
	boolean isUnlockedAtStart();
	boolean isAIDefault();
	
	int getSortOrder();
	
	String getWorldOn();
	String getWorldOff();
	String getWorldLoop();
	String getUIOn();
	String getUIOff();
	String getUILoop();
	float getMusicSuppression();
	Set<String> getTags();
	void addTag(String tag);
	boolean hasTag(String tag);
	String getAIPluginClass();
	float getActivationDays();
	float getActivationCooldown();
	float getDurationDays();
	float getDeactivationDays();
	float getDeactivationCooldown();
	String getIconName();
	String getName();
	
	AbilityAIPlugin getNewAIPluginInstance(AbilityPlugin ability);
	
	
	boolean hasOppositeTag(String tag);
	String getOppositeTag(String tag);
	boolean isPositiveTag(String tag);
}
