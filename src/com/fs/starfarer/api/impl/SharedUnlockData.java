package com.fs.starfarer.api.impl;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.impl.codex.CodexEntryPlugin;
import com.fs.starfarer.api.impl.codex.CodexIntelAdder;

/**
 * DO NOT store references to the instance anywhere that makes them end up in the savefile,
 * this does not belong in the campaign savefile.
 * @author Alex
 *
 */
public class SharedUnlockData extends BaseSharedJSONFile {

	public static String SHARED_UNLOCKS_DATA_FILE = "core_shared_unlocks.json";
	
	public static String ITEMS = "items";
	public static String ILLUSTRATIONS = "illustrations";
	public static String SHIPS = "ships";
	public static String FIGHTERS = "fighters";
	public static String WEAPONS = "weapons";
	
	public static String SHIP_SYSTEMS = "ship_systems";
	public static String HULLMODS = "hullmods";
	public static String COMMODITIES = "commodities";
	public static String INDUSTRIES = "industries";
	public static String PLANETS = "planets";
	public static String CONDITIONS = "conditions";
	public static String SKILLS = "skills";
	public static String ABILITIES = "abilities";
	
	protected static SharedUnlockData instance;
	
	public static SharedUnlockData get() {
		if (instance == null) {
			instance = new SharedUnlockData();
			instance.loadIfNeeded();
		}
		return instance;
	}

	@Override
	protected String getFilename() {
		return SHARED_UNLOCKS_DATA_FILE;
	}
	
	public boolean isPlayerAwareOfSpecialItem(String itemId) {
		return getSet(ITEMS).contains(itemId);
	}
	
	public boolean reportPlayerAwareOfSpecialItem(String itemId, boolean withSave) {
		return reportPlayerAwareOfThing(itemId, ITEMS, CodexDataV2.getItemEntryId(itemId), withSave);
	}
	
	public boolean isPlayerAwareOfIllustration(String key) {
		return getSet(ILLUSTRATIONS).contains(key);
	}
	
	public boolean reportPlayerAwareOfIllustration(String key, boolean withSave) {
		return reportPlayerAwareOfThing(key, ILLUSTRATIONS, CodexDataV2.getGalleryEntryId(key), withSave);
	}
	
	public boolean isPlayerAwareOfShip(String hullId) {
		return getSet(SHIPS).contains(hullId);
	}
	
	public boolean reportPlayerAwareOfShip(String hullId, boolean withSave) {
		return reportPlayerAwareOfThing(hullId, SHIPS, CodexDataV2.getShipEntryId(hullId), withSave);
	}
	
	public boolean isPlayerAwareOfFighter(String fighterId) {
		return getSet(FIGHTERS).contains(fighterId);
	}
	
	public boolean reportPlayerAwareOfFighter(String fighterId, boolean withSave) {
		return reportPlayerAwareOfThing(fighterId, FIGHTERS, CodexDataV2.getFighterEntryId(fighterId), withSave);
	}
	
	public boolean isPlayerAwareOfWeapon(String weaponId) {
		return getSet(WEAPONS).contains(weaponId);
	}
	
	public boolean reportPlayerAwareOfWeapon(String weaponId, boolean withSave) {
		return reportPlayerAwareOfThing(weaponId, WEAPONS, CodexDataV2.getWeaponEntryId(weaponId), withSave);
	}
	
	
	public boolean isPlayerAwareOfShipSystem(String sysId) {
		return getSet(SHIP_SYSTEMS).contains(sysId);
	}
	
	public boolean reportPlayerAwareOfShipSystem(String sysId, boolean withSave) {
		return reportPlayerAwareOfThing(sysId, SHIP_SYSTEMS, CodexDataV2.getShipSystemEntryId(sysId), withSave);
	}
	
	public boolean isPlayerAwareOfHullmod(String hullmodId) {
		return getSet(HULLMODS).contains(hullmodId);
	}
	
	public boolean reportPlayerAwareOfHullmod(String hullmodId, boolean withSave) {
		return reportPlayerAwareOfThing(hullmodId, HULLMODS, CodexDataV2.getHullmodEntryId(hullmodId), withSave);
	}
	
	public boolean isPlayerAwareOfCommodity(String commodityId) {
		return getSet(COMMODITIES).contains(commodityId);
	}
	
	public boolean reportPlayerAwareOfCommodity(String commodityId, boolean withSave) {
		return reportPlayerAwareOfThing(commodityId, COMMODITIES, CodexDataV2.getCommodityEntryId(commodityId), withSave);
	}
	
	public boolean isPlayerAwareOfIndustry(String industryId) {
		return getSet(INDUSTRIES).contains(industryId);
	}
	
	public boolean reportPlayerAwareOfIndustry(String industryId, boolean withSave) {
		return reportPlayerAwareOfThing(industryId, INDUSTRIES, CodexDataV2.getIndustryEntryId(industryId), withSave);
	}
	
	public boolean isPlayerAwareOfPlanet(String planetId) {
		return getSet(PLANETS).contains(planetId);
	}
	
	public boolean reportPlayerAwareOfPlanet(String planetId, boolean withSave) {
		return reportPlayerAwareOfThing(planetId, PLANETS, CodexDataV2.getPlanetEntryId(planetId), withSave);
	}
	
	public boolean isPlayerAwareOfCondition(String conditionId) {
		return getSet(CONDITIONS).contains(conditionId);
	}
	
	public boolean reportPlayerAwareOfCondition(String conditionId, boolean withSave) {
		return reportPlayerAwareOfThing(conditionId, CONDITIONS, CodexDataV2.getConditionEntryId(conditionId), withSave);
	}
	
	public boolean isPlayerAwareOfSkill(String skillId) {
		return getSet(SKILLS).contains(skillId);
	}
	
	public boolean reportPlayerAwareOfSkill(String skillId, boolean withSave) {
		return reportPlayerAwareOfThing(skillId, SKILLS, CodexDataV2.getSkillEntryId(skillId), withSave);
	}
	
	public boolean isPlayerAwareOfAbility(String abilityId) {
		return getSet(ABILITIES).contains(abilityId);
	}
	
	public boolean reportPlayerAwareOfAbility(String abilityId, boolean withSave) {
		return reportPlayerAwareOfThing(abilityId, ABILITIES, CodexDataV2.getAbilityEntryId(abilityId), withSave);
	}
	
	public static Map<String, String> ILLUSTRATION_KEY_LOOKUP = null;
	
	public void checkIfImageIsIllustrationAndMakeAware(String spriteName) {
		if (spriteName == null) return;
		if (ILLUSTRATION_KEY_LOOKUP == null) {
			ILLUSTRATION_KEY_LOOKUP = new HashMap<>();
			for (String key : Global.getSettings().getSpriteKeys("illustrations")) {
				String sprite = Global.getSettings().getSpriteName("illustrations", key);
				ILLUSTRATION_KEY_LOOKUP.put(sprite, key);
			}
		}
		
		String key = ILLUSTRATION_KEY_LOOKUP.get(spriteName);
		if (key != null) {
			reportPlayerAwareOfIllustration(key, true);
		}
	}
	
	
	protected boolean reportPlayerAwareOfThing(String thingId, String setId, String codexEntryId, boolean withSave) {
		boolean wasLocked = isEntryLocked(codexEntryId);
		if (addToSet(setId, thingId)) {
			if (wasLocked && !isEntryLocked(codexEntryId)) CodexIntelAdder.get().addEntry(codexEntryId);
			if (withSave) saveIfNeeded();
			return true;
		}
		return false;
	}
	
	public boolean isEntryLocked(String entryId) {
		CodexEntryPlugin entry = CodexDataV2.getEntry(entryId);
		return entry != null && entry.isLocked();
	}
}




















