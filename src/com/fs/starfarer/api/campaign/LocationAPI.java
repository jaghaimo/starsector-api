package com.fs.starfarer.api.campaign;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface LocationAPI {
	
	String getId();
	
	/**
	 * Whether the location's advance() method was/will be called this frame. Always returns true for
	 * the current location.
	 * @return
	 */
	boolean activeThisFrame();
	
	String getBackgroundTextureFilename();
	void setBackgroundTextureFilename(String backgroundTextureFilename);
	
	void addSpawnPoint(SpawnPointPlugin point);
	void removeSpawnPoint(SpawnPointPlugin point);
	List<SpawnPointPlugin> getSpawnPoints();
	
	void spawnFleet(SectorEntityToken anchor, float xOffset, float yOffset, CampaignFleetAPI fleet);

	/**
	 * Not actually added to the location, and doesn't need to be. Can be added via addEntity if it needs to have an orbit.
	 * @param x
	 * @param y
	 * @return
	 */
	SectorEntityToken createToken(float x, float y);
	
	SectorEntityToken createToken(Vector2f loc);
	
	
	void addEntity(SectorEntityToken entity);
	void removeEntity(SectorEntityToken entity);

	
	PlanetAPI addPlanet(String id, SectorEntityToken focus, String name, String type,
						float angle, float radius, float orbitRadius, float orbitDays);
	SectorEntityToken addAsteroidBelt(SectorEntityToken focus, int numAsteroids, float orbitRadius, float width, float minOrbitDays, float maxOrbitDays);

	SectorEntityToken addAsteroidBelt(SectorEntityToken focus, int numAsteroids,
			float orbitRadius, float width, float minOrbitDays,
			float maxOrbitDays, String terrainId, String optionalName);

	void addOrbitalJunk(SectorEntityToken focus, String junkType, int num,
						float minSize, float maxSize, float orbitRadius, float width,
						float minOrbitDays, float maxOrbitDays, float minSpin, float maxSpin);
	
	
	/**
	 * Texture must have vertical, equal width bands in it. Each band must tile vertically with itself.
	 * 
	 * Returns a RingBandAPI - i.e. the visuals.
	 * @param focus
	 * @param category graphics category in settings.json
	 * @param key id within category
	 * @param bandWidthInTexture
	 * @param bandIndex
	 * @param color
	 * @param bandWidthInEngine
	 * @param orbitDays
	 * @param middleRadius
	 * @return
	 */
	RingBandAPI addRingBand(SectorEntityToken focus, String category, String key,
					 float bandWidthInTexture, int bandIndex, Color color,
					 float bandWidthInEngine, float middleRadius, float orbitDays);

	
	/**
	 * Same as above, but with a "terrain" ring also being added.
	 * If there are multiple rings occupying the same location, it's best to only
	 * have one of them add terrain.
	 * 
	 * Returns the terrain entity, NOT the RingBandAPI visuals.
	 * 
	 * @param focus
	 * @param category
	 * @param key
	 * @param bandWidthInTexture
	 * @param bandIndex
	 * @param color
	 * @param bandWidthInEngine
	 * @param middleRadius
	 * @param orbitDays
	 * @param terrainId
	 * @param optionalName
	 * @return
	 */
	SectorEntityToken addRingBand(SectorEntityToken focus, String category,
			String key, float bandWidthInTexture, int bandIndex, Color color,
			float bandWidthInEngine, float middleRadius, float orbitDays,
			String terrainId, String optionalName);

	
//	/**
//	 * Add station with custom graphic and radius.
//	 * @param id
//	 * @param focus
//	 * @param category key in graphics section in settings.jsno
//	 * @param key in category
//	 * @param radius radius. Sprite will be sized to (radius * 2, radius * 2)
//	 * @param angle
//	 * @param orbitRadius
//	 * @param orbitDays
//	 * @param name
//	 * @param factionId
//	 * @return
//	 */
//	SectorEntityToken addOrbitalStation(String id, SectorEntityToken focus,
//										String category, String key, float radius, 
//										float angle, float orbitRadius, float orbitDays,
//										String name, String factionId);
//	SectorEntityToken addOrbitalStation(String id, SectorEntityToken focus,
//										float angle, float orbitRadius, float orbitDays,
//										String name, String factionId);
	
	
	/**
	 * Adds a custom entity.
	 * Use SectorEntityToken.setFixedLocation() or .setCircularOrbit (or setOrbit) to set its location and/or orbit.
	 * @param id unique id. autogenerated if null.
	 * @param name default name for entity used if this is null
	 * @param type id in custom_entities.json
	 * @param factionId defaults to "neutral" if not specified
	 * @return
	 */
	public CustomCampaignEntityAPI addCustomEntity(String id, String name, String type,
			 								 String factionId);
	
	/**
	 * Adds a custom entity with a radius/spritWidth/spriteHeight different than
	 * those defined for this entity type in custom_entities.json.
	 * Use SectorEntityToken.setFixedLocation() or .setCircularOrbit (or setOrbit) to set its location and/or orbit.
	 * @param id unique id. autogenerated if null.
	 * @param name default name for entity used if this is null
	 * @param type id in custom_entities.json
	 * @param factionId defaults to "neutral" if not specified
	 * @param radius
	 * @param spriteWidth
	 * @param spriteHeight
	 * @return
	 */
	public CustomCampaignEntityAPI addCustomEntity(String id, String name, String type,
			 								 String factionId, float radius, float spriteWidth, float spriteHeight);	
	
	
	public SectorEntityToken addTerrain(String terrainId, Object param);
	
	/**
	 * Examples:
	 * 		getEntities(JumpPointAPI.class) - gets all jump points
	 * 		getEntities(CampaignFleetAPI.class) - gets all fleets
	 * 
	 * General version of getFleets(), getPlanets(), etc
	 * 
	 * @param implementedClassOrInterface
	 * @return
	 */
	List getEntities(Class implementedClassOrInterface);
	
	List<SectorEntityToken> getEntitiesWithTag(String tag);
	
	List<CampaignFleetAPI> getFleets();
	List<PlanetAPI> getPlanets();
	
	/**
	 * Use getEntitiesWithTag(Tags.STATION) instead, in order to pick up custom entities
	 * that are acting as stations. Regular stations also have this tag and will also be picked
	 * up by that method.
	 * @return
	 */
	@Deprecated List<SectorEntityToken> getOrbitalStations();
	List<SectorEntityToken> getAsteroids();	
	
	/**
	 * Use getEntityById() instead
	 * @param name
	 * @return
	 */
	@Deprecated
	SectorEntityToken getEntityByName(String name);
	SectorEntityToken getEntityById(String id);
	Vector2f getLocation();

	boolean isHyperspace();
	
	/**
	 * Will run every time this location's advance() is called. Note that locations
	 * that are not "current" may run at a lower number of "frames" per second. 
	 * @param script
	 */
	void addScript(EveryFrameScript script);
	void removeScriptsOfClass(Class c);
	void removeScript(EveryFrameScript script);
	
	String getName();
	void setName(String name);

	List<SectorEntityToken> getAllEntities();

	
	SectorEntityToken addCorona(SectorEntityToken star, float extraRadius, float windBurnLevel, float flareProbability, float crLossMult);
	SectorEntityToken addCorona(SectorEntityToken star, String terrainType, float extraRadius, float windBurnLevel, float flareProbability, float crLossMult);
	List<CampaignTerrainAPI> getTerrainCopy();

	Map<String, Object> getPersistentData();

	AsteroidAPI addAsteroid(float radius);

	void setBackgroundOffset(float x, float y);

	SectorEntityToken addRadioChatter(SectorEntityToken entity, float extraRadius);

	void updateAllOrbits();
	
	boolean isNebula();

	String getNameWithLowercaseType();

	List<FleetStubAPI> getFleetStubs();
	void removeFleetStub(FleetStubAPI stub);
	void addFleetStub(FleetStubAPI stub);

	Constellation getConstellation();

	boolean isInConstellation();

	String getNameWithTypeIfNebula();

	Collection<String> getTags();
	boolean hasTag(String tag);
	void addTag(String tag);
	void removeTag(String tag);
	void clearTags();

	CustomCampaignEntityAPI addCustomEntity(String id, String name,
			String type, String factionId, float radius, float spriteWidth,
			float spriteHeight, Object pluginParams);

	CustomCampaignEntityAPI addCustomEntity(String id, String name,
			String type, String factionId, Object pluginParams);

	List<SectorEntityToken> getJumpPoints();

	long getLastPlayerVisitTimestamp();

	float getDaysSinceLastPlayerVisit();

	
	/** 
	 * Similar to getEntitiesWithTag(), but for custom entities only. More performant
	 * since there are less entities to iterate through.
	 * 
	 * @param tag
	 * @return
	 */
	List<CustomCampaignEntityAPI> getCustomEntitiesWithTag(String tag);

	List<EveryFrameScript> getScripts();

	void addHitParticle(Vector2f loc, Vector2f vel, float size, float brightness, float duration, Color color);
	
	void renderingLayersUpdated(SectorEntityToken entity);

	MemoryAPI getMemoryWithoutUpdate();

	ParticleControllerAPI addParticle(Vector2f loc, Vector2f vel, float size, float brightness, float rampUp, float duration, Color color);

	String getNameWithNoType();

	boolean isCurrentLocation();

	String getNameWithLowercaseTypeShort();

	String getNameWithTypeShort();

	List<NascentGravityWellAPI> getGravityWells();

	List<CustomCampaignEntityAPI> getCustomEntities();

}




