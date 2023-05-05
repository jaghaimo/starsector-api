package com.fs.starfarer.api.loading;

import java.util.Set;

import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.HullModFleetEffect;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public interface HullModSpecAPI {

	HullModEffect getEffect();
	HullModFleetEffect getFleetEffect();
	boolean isAlwaysUnlocked();
	boolean isHidden();
	boolean isHiddenEverywhere();
	void setHidden(boolean isHidden);
	void setHiddenEverywhere(boolean isHiddenEverywhere);
	void setAlwaysUnlocked(boolean isStarting);
	String getEffectClass();
	void setEffectClass(String effectClass);
	String getDisplayName();
	void setDisplayName(String displayName);
	String getId();
	void setId(String id);
	String getDescriptionFormat();
	void setDescriptionFormat(String descriptionFormat);
	int getFrigateCost();
	void setFrigateCost(int frigateCost);
	int getDestroyerCost();
	void setDestroyerCost(int destroyerCost);
	int getCruiserCost();
	void setCruiserCost(int cruiserCost);
	int getCapitalCost();
	void setCapitalCost(int capitalCost);
	int getTier();
	void setTier(int tier);
	String getSpriteName();
	void setSpriteName(String spriteName);
	int getCostFor(HullSize size);
	Set<String> getTags();
	void addTag(String tag);
	boolean hasTag(String tag);
	float getBaseValue();
	void setBaseValue(float baseValue);
	float getRarity();
	void setRarity(float rarity);
	
	String getDescription(HullSize size);
	String getManufacturer();
	Set<String> getUITags();
	void addUITag(String tag);
	boolean hasUITag(String tag);
	void setManufacturer(String manufacturer);
	String getSModDescription(HullSize hullSize);
	void setSModEffectFormat(String sModEffectFormat);
	String getSModEffectFormat();

}
