package com.fs.starfarer.api.campaign;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.InteractionDialogImageVisual;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.HasMemory;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;


/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface SectorEntityToken extends HasMemory {
	
	public static enum VisibilityLevel {
		NONE,
		SENSOR_CONTACT,
		COMPOSITION_DETAILS,
		COMPOSITION_AND_FACTION_DETAILS,
	}
	boolean isPlayerFleet();
	
	MarketAPI getMarket();
	void setMarket(MarketAPI market);
	
	/**
	 * For the player fleet, the actual cargo.
	 * For AI fleets, only non-logistics-related stuff - i.e. goods being carried, etc.
	 * NOT supplies/fuel/crew/etc.
	 * 
	 * @return
	 */
	CargoAPI getCargo();
	Vector2f getLocation();
	
	
	/**
	 * The location in hyperspace of the LocationAPI containing this entity, or this entity's location
	 * if it's already in hyperspace.
	 * @return
	 */
	Vector2f getLocationInHyperspace();
	OrbitAPI getOrbit();
	void setOrbit(OrbitAPI orbit);
	
	String getId();
	String getName();
	String getFullName();
	
	void setFaction(String factionId);
	LocationAPI getContainingLocation();
	
	float getRadius();
//	void setRadius(float radius);
	
	FactionAPI getFaction();
	
	String getCustomDescriptionId();
	void setCustomDescriptionId(String customDescriptionId);
	
	void setCustomInteractionDialogImageVisual(InteractionDialogImageVisual visual);
	InteractionDialogImageVisual getCustomInteractionDialogImageVisual();
	
	/**
	 * Whether moving ships and items to and from this entity has a cost.
	 * @param freeTransfer
	 */
	public void setFreeTransfer(boolean freeTransfer);
	public boolean isFreeTransfer();
	
	boolean hasTag(String tag);
	void addTag(String tag);
	void removeTag(String tag);
	Collection<String> getTags();
	void clearTags();
	void setFixedLocation(float x, float y);
	void setCircularOrbit(SectorEntityToken focus, float angle, float orbitRadius, float orbitDays);
	void setCircularOrbitPointingDown(SectorEntityToken focus, float angle, float orbitRadius, float orbitDays);
	void setCircularOrbitWithSpin(SectorEntityToken focus, float angle, float orbitRadius, float orbitDays, float minSpin, float maxSpin);
	/**
	 * Will cause the relevant updateFacts() methods to run to update the entity's
	 * perception of the world. Return value should be stored and re-used rather
	 * than calling this method again, unless a fact update is needed.
	 * @return
	 */
	MemoryAPI getMemory();
	
	/**
	 * Get the memory without updating facts.
	 * @return
	 */
	MemoryAPI getMemoryWithoutUpdate();
	
	float getFacing();
	void setFacing(float facing);
	boolean isInHyperspace();
	void addScript(EveryFrameScript script);
	void removeScript(EveryFrameScript script);
	void removeScriptsOfClass(Class c);
	
	
	/**
	 * True if in system or within commRelayRangeAroundSystem light-years.
	 * @param system
	 * @return
	 */
	boolean isInOrNearSystem(StarSystemAPI system);
	
	boolean isInCurrentLocation();
	
	
	/**
	 * In pixels per second.
	 * @return
	 */
	Vector2f getVelocity();

	void setInteractionImage(String category, String key);

	void setName(String name);

	/**
	 * Returns false if:
	 * 1) getContainingLocation() is null, or
	 * 2) getContainingLocation() doesn't contain this entity
	 * @return
	 */
	boolean isAlive();

	
	PersonAPI getActivePerson();
	void setActivePerson(PersonAPI activePerson);

	
//	ActionIndicatorAPI getActionIndicator();
//	boolean hasActionIndicator();
	
	boolean isVisibleToSensorsOf(SectorEntityToken other);
	boolean isVisibleToPlayerFleet();

	VisibilityLevel getVisibilityLevelToPlayerFleet();
	VisibilityLevel getVisibilityLevelTo(SectorEntityToken other);

	
	void addAbility(String id);
	void removeAbility(String id);
	AbilityPlugin getAbility(String id);
	boolean hasAbility(String id);
	Map<String, AbilityPlugin> getAbilities();

	boolean isTransponderOn();

	void setTransponderOn(boolean transponderOn);

	void addFloatingText(String text, Color color, float duration);

	SectorEntityToken getLightSource();
	Color getLightColor();

	
	
	void setMemory(MemoryAPI memory);

	Map<String, Object> getCustomData();

	Color getIndicatorColor();

	/**
	 * Only returns non-null for custom campaign entities with a plugin.
	 * @return
	 */
	CustomCampaignEntityPlugin getCustomPlugin();

	float getCircularOrbitRadius();
	float getCircularOrbitPeriod();

	SectorEntityToken getOrbitFocus();

	void setId(String id);

	//String getAutogenJumpPointNameInSystem();
	//void setAutogenJumpPointNameInSystem(String autogenJumpPointNameInSystem);
	String getAutogenJumpPointNameInHyper();
	void setAutogenJumpPointNameInHyper(String autogenJumpPointNameInHyper);

	boolean isSkipForJumpPointAutoGen();
	void setSkipForJumpPointAutoGen(boolean skipForJumpPointAutoGen);

	float getCircularOrbitAngle();

	String getCustomEntityType();

	float getSensorStrength();
	void setSensorStrength(Float sensorStrength);
	float getSensorProfile();
	void setSensorProfile(Float sensorProfile);
	StatBonus getDetectedRangeMod();
	StatBonus getSensorRangeMod();

	float getBaseSensorRangeToDetect(float sensorProfile);

	boolean hasSensorStrength();
	boolean hasSensorProfile();
	
	

	/**
	 * Does not includes fleet radii - i.e. the returned range is from
	 * outer edge to outer edge.
	 * @return
	 */
	float getMaxSensorRangeToDetect(SectorEntityToken other);

	boolean isDiscoverable();
	void setDiscoverable(Boolean discoverable);

	CustomEntitySpecAPI getCustomEntitySpec();

	List<DropData> getDropValue();
	List<DropData> getDropRandom();
	void addDropValue(String group, int value);
	void addDropRandom(String group, int chances);
	void addDropRandom(String group, int chances, int value);

	boolean isExpired();
	void setExpired(boolean expired);

	float getSensorFaderBrightness();
	float getSensorContactFaderBrightness();

	void forceSensorFaderBrightness(float b);

	Float getDiscoveryXP();
	void setDiscoveryXP(Float discoveryXP);
	boolean hasDiscoveryXP();

	void addDropValue(DropData data);
	void addDropRandom(DropData data);

	/**
	 * Always use sensor fader brightness for rendering circular indicator around the entity.
	 * @param alwaysUseSensorFaderBrightness
	 */
	void setAlwaysUseSensorFaderBrightness(Boolean alwaysUseSensorFaderBrightness);
	Boolean getAlwaysUseSensorFaderBrightness();

	void advance(float amount);

	boolean hasScriptOfClass(Class c);

	void setContainingLocation(LocationAPI location);

	void clearAbilities();

	Constellation getConstellation();

	boolean isStar();

	Float getSalvageXP();
	void setSalvageXP(Float salvageXP);
	boolean hasSalvageXP();

	void setDetectionRangeDetailsOverrideMult(Float detectionRangeDetailsOverrideMult);
	Float getDetectionRangeDetailsOverrideMult();

	VisibilityLevel getVisibilityLevelOfPlayerFleet();

	void setCircularOrbitAngle(float angle);

	void addFloatingText(String text, Color color, float duration, boolean showWhenOnlySensorContact);

	boolean isSystemCenter();

	StarSystemAPI getStarSystem();

	void clearFloatingText();

	void setLocation(float x, float y);

	void autoUpdateHyperLocationBasedOnInSystemEntityAtRadius(SectorEntityToken entity, float radius);

	void forceSensorContactFaderBrightness(float b);

	void forceSensorFaderOut();

	void setLightSource(SectorEntityToken star, Color color);

	List<EveryFrameScript> getScripts();

}
	
	
	
	
	
	




