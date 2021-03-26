package com.fs.starfarer.api;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketSpecAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatReadinessPlugin;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipSystemSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.AbilitySpecAPI;
import com.fs.starfarer.api.loading.BarEventSpec;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.loading.EventSpecAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.loading.PersonMissionSpec;
import com.fs.starfarer.api.loading.RoleEntryAPI;
import com.fs.starfarer.api.loading.TerrainSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.plugins.LevelupPlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.ListMap;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface SettingsAPI {
	int getBattleSize();
	
	/**
	 * Can be used outside the campaign, unlike FactoryAPI.createPerson().
	 * @return
	 */
	PersonAPI createPerson();
	
	LabelAPI createLabel(String text, String font);
	
	float getBonusXP(String key);
	
	float getFloat(String key);
	boolean getBoolean(String key);
	ClassLoader getScriptClassLoader();
	
	boolean isDevMode();
	void setDevMode(boolean devMode);
	
	Color getColor(String id);
	
	Object getInstanceOfScript(String className);
	
	/**
	 * Gets a string from a given category in strings.json
	 * @param category
	 * @param id
	 * @return
	 */
	String getString(String category, String id);
	
	/**
	 * File must already have been loaded. Mostly useful for retrieving ship and weapon sprites and other such.
	 * @param filename
	 * @return
	 */
	SpriteAPI getSprite(String filename);
	
	/**
	 * Gets a sprite loaded using the "graphics" section in data/config/settings.json.
	 * @param category
	 * @param key
	 * @return
	 */
	SpriteAPI getSprite(String category, String key);
	
	/**
	 * Same as the method that takes (String category, String key). SpriteId is just a container class
	 * for category + id.
	 * @param id
	 * @return
	 */
	SpriteAPI getSprite(SpriteId id);
	
	String getSpriteName(String category, String id);
	
	/**
	 * Use / instead of \ in paths for compatibility (required to work on OS X and Linux). 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	InputStream openStream(String filename) throws IOException;
	
	/**
	 * Use / instead of \ in paths for compatibility (required to work on OS X and Linux). 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	String loadText(String filename) throws IOException;
	
	/**
	 * Use / instead of \ in paths for compatibility (required to work on OS X and Linux). 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	JSONObject loadJSON(String filename) throws IOException, JSONException;
	
	/**
	 * Returns an array of JSONObjects with keys corresponding to the columns in the csv file.
	 * 
	 * Use / instead of \ in paths for compatibility (required to work on OS X and Linux).
	 * 
	 * @param filename
	 * @return
	 * @throws IOException 
	 * @throws JSONException 
	 */
	JSONArray loadCSV(String filename) throws IOException, JSONException;
	
	/**
	 * Useful for building a mod that allows other mods to override some of its data.
	 * 
	 * Merges on spreadsheet level only; rows with matching ids get replaced by non-master rows.
	 * 
	 * If multiple non-master rows have the same id, one will be picked in an undefined manner.
	 * 
	 * 
	 * @param idColumn the column to be used as the key for merging spreadsheets.
	 * @param path the location of the spreadsheet, i.e. "mydata/spreadsheet.csv".
	 * @param masterMod name of the mod that has the master copy of the spreadsheet, which will be used as the base version to add to/modify during the merge. 
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	JSONArray getMergedSpreadsheetDataForMod(String idColumn, String path, String masterMod) throws IOException, JSONException;
	
	JSONObject getMergedJSONForMod(String path, String masterMod) throws IOException, JSONException;
	
	
	/**
	 * Virtual pixels, i.e. divided by getScreenScaleMult().
	 * @return
	 */
	float getScreenWidth();
	
	/**
	 * Virtual pixels, i.e. divided by getScreenScaleMult().
	 * @return
	 */
	float getScreenHeight();
	
	float getScreenWidthPixels();
	float getScreenHeightPixels();
	
	/**
	 * Gets entry from descriptions.csv. 
	 * @param id
	 * @param type
	 * @return description, or null if not found.
	 */
	Description getDescription(String id, Type type);
	
	CombatReadinessPlugin getCRPlugin();
	
	int getCodeFor(String key);
	
	
	
	
	WeaponSpecAPI getWeaponSpec(String weaponId);

	void loadTexture(String filename) throws IOException;

	
	/**
	 * Bit of a hack to have this method here. It's just a way to call into some unexposed utility code.
	 * @param from
	 * @param target
	 * @param considerShield
	 * @return
	 */
	float getTargetingRadius(Vector2f from, CombatEntityAPI target, boolean considerShield);
	
	
	ShipVariantAPI getVariant(String variantId);

	/**
	 * Plugins returned by this method are cached and persistent across multiple saves. They should not have
	 * any data members that point at campaign data or it will cause a memory leak.
	 * @param id
	 * @return
	 */
	Object getPlugin(String id);

	List<String> getSortedSkillIds();
	
	SkillSpecAPI getSkillSpec(String skillId);

	String getString(String key);

	AbilitySpecAPI getAbilitySpec(String abilityId);

	List<String> getSortedAbilityIds();

	float getBaseTravelSpeed();

	float getSpeedPerBurnLevel();

	float getUnitsPerLightYear();

	int getMaxShipsInFleet();

	TerrainSpecAPI getTerrainSpec(String terrainId);
	EventSpecAPI getEventSpec(String eventId);
	CustomEntitySpecAPI getCustomEntitySpec(String id);

	GameState getCurrentState();

	int getMaxSensorRange();
	int getMaxSensorRangeHyper();
	int getMaxSensorRange(LocationAPI loc);

	List<String> getAllVariantIds();
	List<String> getAptitudeIds();
	List<String> getSkillIds();
	LevelupPlugin getLevelupPlugin();
	String getVersionString();

	JSONObject loadJSON(String filename, String modId) throws IOException, JSONException;
	JSONArray loadCSV(String filename, String modId) throws IOException, JSONException;
	String loadText(String filename, String modId) throws IOException, JSONException;

	ModManagerAPI getModManager();

	float getBaseFleetSelectionRadius();
	float getFleetSelectionRadiusPerUnitSize();
	float getMaxFleetSelectionRadius();

	List<RoleEntryAPI> getEntriesForRole(String factionId, String role);
	void addEntryForRole(String factionId, String role, String variantId, float weight);
	void removeEntryForRole(String factionId, String role, String variantId);

	List<RoleEntryAPI> getDefaultEntriesForRole(String role);
	void addDefaultEntryForRole(String role, String variantId, float weight);
	void removeDefaultEntryForRole(String role, String variantId);

	void profilerBegin(String id);
	void profilerEnd();

	List<PlanetSpecAPI> getAllPlanetSpecs();

	Object getSpec(Class c, String id, boolean nullOnNotFound);
	void putSpec(Class c, String id, Object spec);
	Collection<Object> getAllSpecs(Class c);

	/**
	 * @param n 1 to 3999.
	 * @return
	 */
	String getRoman(int n);
	void greekLetterReset();
	String getNextCoolGreekLetter(Object context);
	String getNextGreekLetter(Object context);

	MarketConditionSpecAPI getMarketConditionSpec(String conditionId);
	
	
	ShipAIPlugin createDefaultShipAI(ShipAPI ship, ShipAIConfig config);

	HullModSpecAPI getHullModSpec(String modId);
	FighterWingSpecAPI getFighterWingSpec(String wingId);
	List<HullModSpecAPI> getAllHullModSpecs();
	List<FighterWingSpecAPI> getAllFighterWingSpecs();

	List<WeaponSpecAPI> getAllWeaponSpecs();

	boolean isSoundEnabled();

	boolean isInCampaignState();

	boolean isGeneratingNewGame();

	float getAngleInDegreesFast(Vector2f v);
	float getAngleInDegreesFast(Vector2f from, Vector2f to);

	CommoditySpecAPI getCommoditySpec(String commodityId);

	ShipHullSpecAPI getHullSpec(String hullId);

	int computeNumFighterBays(ShipVariantAPI variant);

	
	/**
	 * For modding purposes, always returns true.
	 * @return
	 */
	boolean isInGame();

	Object getNewPluginInstance(String id);
	String getControlStringForAbilitySlot(int index);

	
	/**
	 * Total hack instead of moving the Controls enum out to the .api project. Use at own peril.
	 * @param name
	 * @return
	 */
	String getControlStringForEnumName(String name);

	boolean isNewPlayer();

	IndustrySpecAPI getIndustrySpec(String industryId);

	List<CommoditySpecAPI> getAllCommoditySpecs();

	int getInt(String key);

	List<IndustrySpecAPI> getAllIndustrySpecs();

	SpecialItemSpecAPI getSpecialItemSpec(String itemId);

	List<SpecialItemSpecAPI> getAllSpecialItemSpecs();

	List<ShipHullSpecAPI> getAllShipHullSpecs();

	SpriteAPI getSprite(String category, String id, boolean emptySpriteOnNotFound);

	ShipVariantAPI createEmptyVariant(String hullVariantId, ShipHullSpecAPI hullSpec);

	/**
	 * For default ship roles.
	 * @return
	 */
	ListMap<String> getHullIdToVariantListMap();

	
	
	String readTextFileFromCommon(String filename) throws IOException;
	
	/**
	 * Max size 10k.
	 * @param filename
	 * @param data
	 * @throws IOException
	 */
	void writeTextFileToCommon(String filename, String data) throws IOException;

	boolean fileExistsInCommon(String filename);
	void deleteTextFileFromCommon(String filename);
	
	Color getBasePlayerColor();
	Color getDesignTypeColor(String designType);

	boolean doesVariantExist(String variantId);

	void addCommodityInfoToTooltip(TooltipMakerAPI tooltip, float initPad, CommoditySpecAPI spec, 
								   int max, boolean withText, boolean withSell, boolean withBuy);

	
	JSONObject getJSONObject(String key) throws JSONException;
	JSONArray getJSONArray(String key) throws JSONException;

	/**
	 * Should be used when faction data needs to be accessed outside the campaign.
	 * @param factionId
	 * @return
	 */
	FactionAPI createBaseFaction(String factionId);

	List<MarketConditionSpecAPI> getAllMarketConditionSpecs();
	List<SubmarketSpecAPI> getAllSubmarketSpecs();

	float getMinArmorFraction();

	float getMaxArmorDamageReduction();

	ShipSystemSpecAPI getShipSystemSpec(String id);
	List<ShipSystemSpecAPI> getAllShipSystemSpecs();

	float getScreenScaleMult();
	int getAASamples();

	/**
	 * Converted to virtual screen coordinates, i.e. adjusted for screen scaling.
	 * @return
	 */
	int getMouseX();
	
	/**
	 * Converted to virtual screen coordinates, i.e. adjusted for screen scaling.
	 * @return
	 */
	int getMouseY();

	int getShippingCapacity(MarketAPI market, boolean inFaction);
	JSONObject getSettingsJSON();
	/**
	 * Must be called after making any changes to result of getSettingsJSON().
	 */
	void resetCached();

	void setFloat(String key, Float value);
	void setBoolean(String key, Boolean value);

	List<PersonMissionSpec> getAllMissionSpecs();
	PersonMissionSpec getMissionSpec(String id);
	
	List<BarEventSpec> getAllBarEventSpecs();
	BarEventSpec getBarEventSpec(String id);

	void setAutoTurnMode(boolean autoTurnMode);
	boolean isAutoTurnMode();

	boolean isShowDamageFloaties();

	float getFloatFromArray(String key, int index);

	int getIntFromArray(String key, int index);

}
