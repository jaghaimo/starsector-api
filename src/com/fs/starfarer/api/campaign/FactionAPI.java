package com.fs.starfarer.api.campaign;

import java.awt.Color;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.JSONObject;

import com.fs.starfarer.api.campaign.rules.HasMemory;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.RelationshipAPI;
import com.fs.starfarer.api.fleet.ShipFilter;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface FactionAPI extends HasMemory {
	
	public static enum ShipPickMode {
		IMPORTED,
		ALL,
		PRIORITY_THEN_ALL,
		PRIORITY_ONLY,
	}
	public static class ShipPickParams implements Cloneable {
		public ShipPickMode mode;
		public int maxFP = 1000;
		public Long timestamp = null;
		public Boolean blockFallback = null;
		
		public ShipPickParams(ShipPickMode mode, int maxFP, Long timestamp, Boolean blockFallback) {
			this.mode = mode;
			this.maxFP = maxFP;
			this.timestamp = timestamp;
			this.blockFallback = blockFallback;
		}
		public ShipPickParams(ShipPickMode mode, int maxFP, Long timestamp) {
			this.mode = mode;
			this.maxFP = maxFP;
			this.timestamp = timestamp;
		}
		public ShipPickParams(ShipPickMode mode, int maxFP) {
			this(mode, maxFP, null);
		}
		public ShipPickParams(ShipPickMode mode) {
			this(mode, 1000);
		}
		public ShipPickParams() {
			this(ShipPickMode.PRIORITY_THEN_ALL, 1000);
		}
		@Override
		public ShipPickParams clone() {
			try {
				return (ShipPickParams) super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	
		public static ShipPickParams all() {
			return new ShipPickParams(ShipPickMode.ALL);
		}
		public static ShipPickParams priority() {
			return new ShipPickParams(ShipPickMode.PRIORITY_THEN_ALL);
		}
		public static ShipPickParams imported() {
			return new ShipPickParams(ShipPickMode.IMPORTED);
		}
	}
	
	
	void adjustRelationship(String id, float delta);
	boolean adjustRelationship(String id, float delta, RepLevel limit);
	void setRelationship(String id, float newValue);
	void setRelationship(String id, RepLevel level);
	
	boolean ensureAtBest(String id, RepLevel level);
	boolean ensureAtWorst(String id, RepLevel level);

	RepLevel getRelationshipLevel(FactionAPI faction);
	RepLevel getRelationshipLevel(String id);
	boolean isAtWorst(String id, RepLevel level);
	boolean isAtWorst(FactionAPI other, RepLevel level);
	boolean isAtBest(String id, RepLevel level);
	boolean isAtBest(FactionAPI other, RepLevel level);
	boolean isHostileTo(FactionAPI other);
	boolean isHostileTo(String other);
	
//	boolean isNeutralTo(FactionAPI other);
//	boolean isFriendlyTo(FactionAPI other);
//	boolean isNeutralTo(String other);
//	boolean isFriendlyTo(String other);
	
	
	float getRelationship(String id);
	String getId();
	String getDisplayName();
	String getDisplayNameWithArticle();
	
	Color getColor();
	Color getBaseUIColor();
	Color getGridUIColor();
	Color getDarkUIColor();
	Color getSecondaryUIColor();
	
	/**
	 * Brighter/slightly cyan version of getBaseUIColor()
	 * @return
	 */
	Color getBrightUIColor();
		
	boolean isNeutralFaction();
	boolean isPlayerFaction();
	
	
	List<String> getStockFleetIds();

//	boolean isIllegal(String commodityId);
	//boolean isHostile(FactionAPI other);
	
	MemoryAPI getMemory();
	
	
	
	/**
	 * May add more than one ship if a fallback specifies to add multiple ships.
	 * (For example, 2 small freighters if a medium freighter isn't available.)
	 * 
	 * Returns a total weight of ships added to the fleet. Generally will return
	 * 1 when ships were added, 0 when they weren't, and a number >1 when adding, say,
	 * a medium ship instead of a small one because no small ones are available.
	 * @param role
	 * @param maxFP
	 * @param fleet
	 * @return
	 */
	float pickShipAndAddToFleet(String role, ShipPickParams params, CampaignFleetAPI fleet);
	
	String getFleetTypeName(String type);
	String getDisplayNameLong();
	String getDisplayNameLongWithArticle();
	String getEntityNamePrefix();
	
	
	Color getRelColor(String otherFactionId);
	Set<String> getIllegalCommodities();
	boolean isIllegal(String commodityId);
	boolean isIllegal(CargoStackAPI stack);
	
	List<ShipRolePick> pickShip(String role, ShipPickParams params);
	List<ShipRolePick> pickShip(String role, ShipPickParams params, ShipFilter filter, Random random);
	
	void makeCommodityIllegal(String commodityId);
	void makeCommodityLegal(String commodityId);
	
	float getTariffFraction();
	float getTollFraction();
	float getFineFraction();
	String getInternalCommsChannel();
	
	PersonAPI createRandomPerson();
	PersonAPI createRandomPerson(Gender gender);
	String getLogo();
	
	JSONObject getCustom();
	MemoryAPI getMemoryWithoutUpdate();
	Color getRelColor(RepLevel level);
	RelationshipAPI getRelToPlayer();
	
	String getRank(String id);
	String getPost(String id);
	String getDisplayNameIsOrAre();
	
	String pickPersonality();
	boolean getCustomBoolean(String key);
	String getCustomString(String key);
	
	
	boolean isShowInIntelTab();
	void setShowInIntelTab(boolean isShowInIntelTab);
	String getCrest();
	String getPersonNamePrefix();
	String getPersonNamePrefixAOrAn();
	String pickRandomShipName();
	float pickShipAndAddToFleet(String role, ShipPickParams params, CampaignFleetAPI fleet, Random random);
	
	Set<String> getVariantsForRole(String roleId);
	PersonAPI createRandomPerson(Gender gender, Random random);
	PersonAPI createRandomPerson(Random random);
	float getCustomFloat(String key);
	int getSecondarySegments();
	
	String getDisplayNameOverride();
	void setDisplayNameOverride(String displayNameOverride);
	String getDisplayNameWithArticleOverride();
	void setDisplayNameWithArticleOverride(String displayNameWithArticleOverride);
	String getDisplayIsOrAreOverride();
	void setDisplayIsOrAreOverride(String displayIsOrAreOverride);
	String getShipNamePrefixOverride();
	void setShipNamePrefixOverride(String shipNamePrefixOverride);
	String getPersonNamePrefixAOrAnOverride();
	void setPersonNamePrefixAOrAnOverride(String personNamePrefixAOrAnOverride);
	String getFactionLogoOverride();
	void setFactionLogoOverride(String factionLogoOverride);
	String getFactionCrestOverride();
	void setFactionCrestOverride(String factionCrestOverride);
	WeightedRandomPicker<String> getPortraits(Gender gender);
	
	Set<String> getKnownShips();
	void addKnownShip(String hullId, boolean setTimestamp);
	
	/**
	 * All of the blueprints specified in the .faction file are re-added to the faction 
	 * every time a savegame is loaded. To make blueprint removal permanent, the list of 
	 * things-to-remove needs to be stored and they need to be re-removed on every game load.
	 */
	void removeKnownShip(String hullId);
	Set<String> getKnownWeapons();
	void addKnownWeapon(String weaponId, boolean setTimestamp);
	
	/**
	 * All of the blueprints specified in the .faction file are re-added to the faction 
	 * every time a savegame is loaded. To make blueprint removal permanent, the list of 
	 * things-to-remove needs to be stored and they need to be re-removed on every game load.
	 */
	void removeKnownWeapon(String weaponId);
	Set<String> getKnownFighters();
	void addKnownFighter(String wingId, boolean setTimestamp);
	
	/**
	 * All of the blueprints specified in the .faction file are re-added to the faction 
	 * every time a savegame is loaded. To make blueprint removal permanent, the list of 
	 * things-to-remove needs to be stored and they need to be re-removed on every game load.
	 */
	void removeKnownFighter(String wingId);
	Set<String> getKnownIndustries();
	void addKnownIndustry(String industryId);
	void removeKnownIndustry(String industryId);
	boolean knowsShip(String hullId);
	boolean knowsWeapon(String weaponId);
	boolean knowsFighter(String wingId);
	boolean knowsIndustry(String industryId);
	
	Set<String> getPriorityShips();
	void addPriorityShip(String hullId);
	void removePriorityShip(String hullId);
	boolean isShipPriority(String hullId);
	Set<String> getPriorityWeapons();
	void addPriorityWeapon(String weaponId);
	void removePriorityWeapon(String weaponId);
	boolean isWeaponPriority(String weaponId);
	Set<String> getPriorityFighters();
	void addPriorityFighter(String wingId);
	void removePriorityFighter(String wingId);
	boolean isFighterPriority(String wingId);
	
	
	boolean isAutoEnableKnownWeapons();
	void setAutoEnableKnownWeapons(boolean autoEnableKnownWeapons);
	boolean isAutoEnableKnownShips();
	void setAutoEnableKnownShips(boolean autoEnableKnownShips);
	boolean isAutoEnableKnownFighters();
	void setAutoEnableKnownFighters(boolean autoEnableKnownFighters);
	boolean isAutoEnableKnownHullmods();
	void setAutoEnableKnownHullmods(boolean autoEnableKnownHullmods);
	
	void addKnownHullMod(String modId);
	void removeKnownHullMod(String modId);
	boolean knowsHullMod(String modId);
	Set<String> getKnownHullMods();
	void addPriorityHullMod(String modId);
	void removePriorityHullMod(String modId);
	boolean isHullModPriority(String modId);
	Set<String> getPriorityHullMods();
	FactionDoctrineAPI getDoctrine();
	Map<String, Float> getVariantOverrides();
	
	/**
	 * Hulls that are restricted to specific variants, defined in "variantOverrides" section of the .faction file.
	 * @return
	 */
	LinkedHashSet<String> getOverriddenHulls();
	Map<String, Float> getHullFrequency();
	
	/**
	 * Hulls that will be in fleets even when the market's ship & weapons supply is from another faction.
	 * Generally faction-specific skins of base hulls known to all factions.
	 * @return
	 */
	Set<String> getAlwaysKnownShips();
	void addUseWhenImportingShip(String hullId);
	void removeUseWhenImportingShip(String hullId);
	boolean useWhenImportingShip(String hullId);
	
	/**
	 * Should be called after direct manipulation of the faction's known/always known/priority ship hulls.
	 * Automatically called by the add/removeXXXShip methods.
	 */
	void clearShipRoleCache();
	WeightedRandomPicker<String> getPersonalityPicker();
	FactionProductionAPI getProduction();
	Map<String, Long> getWeaponTimestamps();
	Map<String, Long> getFighterTimestamps();
	Map<String, Long> getShipTimestamps();
	void setShipTimestampToNow(String hullId);
	void setWeaponTimestampToNow(String weaponId);
	void setFighterTimestampToNow(String wingId);
	
	boolean isShipKnownAt(String hullId, Long timestamp);
	boolean isWeaponKnownAt(String weaponId, Long timestamp);
	boolean isFighterKnownAt(String wing, Long timestamp);
	int getNumAvailableForRole(String roleId, ShipPickMode mode);
	
	String getDisplayNameHasOrHave();
	String getDisplayNameWithArticleWithoutArticle();
	String pickRandomShipName(Random random);
	
	/**
	 * Used to figure out how many fleet points raids/expeditions etc divide out for each "large" fleet.
	 * When going above 30 ships, fleets replace some smaller ships with larger ones. This FP limit is
	 * where that still produces fleets that aren't *too* top-heavy.
	 * @return
	 */
	float getApproximateMaxFPPerFleet(ShipPickMode mode);
	Map<String, String> getMusicMap();
	String getBarSound();
	int getRepInt(String id);
	String pickVoice(PersonImportance importance, Random random);
	String getShipNamePrefix();
	
	Map<String, Float> getWeaponSellFrequency();
	Map<String, Float> getFighterSellFrequency();
	Map<String, Float> getHullmodSellFrequency();
	FactionSpecAPI getFactionSpec();
	
	void initSpecIfNeeded();
	JSONObject getCustomJSONObject(String key);
	
	
}




