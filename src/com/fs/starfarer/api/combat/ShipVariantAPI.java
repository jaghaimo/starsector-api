package com.fs.starfarer.api.combat;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

public interface ShipVariantAPI {
	ShipVariantAPI clone();
	
	ShipHullSpecAPI getHullSpec();
	
	String getDisplayName();
	String getDesignation();
	Collection<String> getHullMods();
	
	/**
	 * Doesn't clear out built-in hullmods (or permamods), as opposed to getHullMods().clear().
	 */
	void clearHullMods();
	
	EnumSet<ShipTypeHints> getHints();
	
	void addMod(String modId);
	void removeMod(String modId);
	
	void addWeapon(String slotId, String weaponId);
	
	int getNumFluxVents();
	int getNumFluxCapacitors();
	
	/**
	 * Only returns slots that have actual weapons in them, not empty slots.
	 * @return
	 */
	List<String> getNonBuiltInWeaponSlots();
	String getWeaponId(String slotId);
	
	void setNumFluxCapacitors(int capacitors);
	void setNumFluxVents(int vents);
	void setSource(VariantSource source);
	void clearSlot(String slotId);
	
	
	WeaponSpecAPI getWeaponSpec(String slotId);
	Collection<String> getFittedWeaponSlots();

	
	void autoGenerateWeaponGroups();
	boolean hasUnassignedWeapons();
	void assignUnassignedWeapons();
	WeaponGroupSpec getGroup(int index);
	
	int computeOPCost(MutableCharacterStatsAPI stats);
	int computeWeaponOPCost(MutableCharacterStatsAPI stats);
	int computeHullModOPCost();
	int computeHullModOPCost(MutableCharacterStatsAPI stats);
	
	VariantSource getSource();
	boolean isStockVariant();
	boolean isEmptyHullVariant();
	
	void setHullVariantId(String hullVariantId);
	String getHullVariantId();

	List<WeaponGroupSpec> getWeaponGroups();
	void addWeaponGroup(WeaponGroupSpec group);
	
	void setVariantDisplayName(String variantName);

	ShipAPI.HullSize getHullSize();

	boolean isFighter();

	String getFullDesignationWithHullName();

	boolean hasHullMod(String id);

	WeaponSlotAPI getSlot(String slotId);

	boolean isCombat();
	boolean isStation();

	String getWingId(int index);
	void setWingId(int index, String wingId);
	List<String> getWings();
	List<String> getLaunchBaysSlotIds();

	List<String> getFittedWings();

	void setHullSpecAPI(ShipHullSpecAPI hullSpec);

	Set<String> getPermaMods();
	void clearPermaMods();
	void removePermaMod(String modId);
	void addPermaMod(String modId);
	void addPermaMod(String modId, boolean isSMod);

	boolean isCarrier();

	List<String> getSortedMods();

	Set<String> getSuppressedMods();
	void addSuppressedMod(String modId);
	void removeSuppressedMod(String modId);
	void clearSuppressedMods();

	boolean isGoalVariant();
	void setGoalVariant(boolean goalVariant);

	Collection<String> getNonBuiltInHullmods();

	FighterWingSpecAPI getWing(int index);

	int getUnusedOP(MutableCharacterStatsAPI stats);

	boolean isCivilian();

	List<String> getModuleSlots();

	MutableShipStatsAPI getStatsForOpCosts();

	boolean isLiner();
	boolean isFreighter();
	boolean isTanker();

	/**
	 * Whether variant has any unsuppressed dmods.
	 * @return
	 */
	boolean isDHull();

	Map<String, String> getStationModules();

	List<String> getNonBuiltInWings();

	boolean hasTag(String tag);
	void addTag(String tag);
	void removeTag(String tag);
	Collection<String> getTags();
	void clearTags();

	/**
	 * Removes everything non-built-in - weapons, fighters, hullmods - and sets vents/capacitors to 0.
	 */
	void clear();

	
	/**
	 * If autofitted, what the goal variant was. May or may not be set. Must be set for fleet.deflate() to work.
	 * @return
	 */
	String getOriginalVariant();
	
	/**
	 * If autofitted by a FleetInflater, what the goal variant was. May or may not be set. Must be set for fleet.deflate() to work.
	 */
	void setOriginalVariant(String targetVariant);

	ShipVariantAPI getModuleVariant(String slotId);
	void setModuleVariant(String slotId, ShipVariantAPI variant);

	boolean isTransport();
	String getVariantFilePath();

	LinkedHashSet<String> getSMods();

	String getFullDesignationWithHullNameForShip();

	void refreshBuiltInWings();

	boolean hasDMods();

	LinkedHashSet<String> getSModdedBuiltIns();



}

	
	
	
	
	
	
	
	
	