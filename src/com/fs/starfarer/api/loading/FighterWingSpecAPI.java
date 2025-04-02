package com.fs.starfarer.api.loading;

import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public interface FighterWingSpecAPI extends WithSourceMod {

	boolean isBomber();
	boolean isAssault();
	boolean isSupport();
	boolean isInterceptor();
	boolean isRegularFighter();
	WingRole getRole();
	void setRole(WingRole role);
	FormationType getFormation();
	void setFormation(FormationType formation);
	String getId();
	void setId(String id);
	int getNumFighters();
	void setNumFighters(int numFighters);
	String getVariantId();
	void setVariantId(String variantId);
	float getRefitTime();
	void setRefitTime(float refitTime);
	int getFleetPoints();
	void setFleetPoints(int fleetPoints);
	float getBaseValue();
	void setBaseValue(float baseValue);
	ShipVariantAPI getVariant();
	float getAttackRunRange();
	void setAttackRunRange(float attackRunRange);
	Set<String> getTags();
	void addTag(String tag);
	boolean hasTag(String tag);
	int getTier();
	void setTier(int tier);
	String getRoleDesc();
	void setRoleDesc(String roleDesc);
	float getRarity();
	void setRarity(float rarity);
	String getWingName();
	String getAutofitCategory();
	List<String> getAutofitCategoriesInPriorityOrder();
	
	float getAttackPositionOffset();
	void setAttackPositionOffset(float attackPositionOffset);
	
	//float getOpCost();
	void setOpCost(float opCost);
	float getOpCost(MutableShipStatsAPI shipStats);
	void resetAutofitPriorityCategories();
	float getRange();
	void setRange(float range);

}


