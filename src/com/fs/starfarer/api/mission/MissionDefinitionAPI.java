package com.fs.starfarer.api.mission;

import java.awt.Color;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;

/**
 * Used by the MissionDefinition.java script to create the contents of a mission.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface MissionDefinitionAPI {

	/**
	 * Set various parameters for a fleet. Must be called once each for FleetSide.PLAYER and FleetSide.ENEMY.
	 * Must be called before any ships are added to the fleet.
	 * @param side
	 * @param shipNamePrefix Prepended to any randomly picked ship names. "ISS", "HSS", etc.
	 * @param goal
	 * @param useDefaultAI For now, set true for side = ENEMY and false for side = PLAYER. Later, may supply custom AI.
	 */
	public void initFleet(FleetSide side, String shipNamePrefix, FleetGoal goal, boolean useDefaultAI);
	
	/**
	 * Set various parameters for a fleet. Must be called once each for FleetSide.PLAYER and FleetSide.ENEMY.
	 * Must be called before any ships are added to the fleet.
	 * @param side
	 * @param shipNamePrefix Prepended to any randomly picked ship names. "ISS", "HSS", etc.
	 * @param goal
	 * @param useDefaultAI For now, set true for side = ENEMY and false for side = PLAYER. Later, may supply custom AI.
	 * @param commandRating Added to the pool of available command points.
	 */
	public void initFleet(FleetSide side, String shipNamePrefix, FleetGoal goal, boolean useDefaultAI, int commandRating);
	
	
	/**
	 * Add a ship variant to a fleet. The variant ID refers to one of the variants found in data/variants
	 * and data/variants/fighters.
	 * @param side
	 * @param variantId
	 * @param type
	 * @param isFlagship Set to true for the player's ship, false otherwise.
	 */
	public FleetMemberAPI addToFleet(FleetSide side, String variantId, FleetMemberType type, boolean isFlagship);
	
	
	/**
	 * Same as the other addToFleet method, except you can specify the ship's name.
	 * @param side
	 * @param variantId
	 * @param type
	 * @param shipName Full ship name, including prefix.
	 * @param isFlagship Set to true for the player's ship, false otherwise.
	 */
	public FleetMemberAPI addToFleet(FleetSide side, String variantId, FleetMemberType type, String shipName, boolean isFlagship);
	
	
	/**
	 * Indicates that losing the named ship causes immediate defeat for that side. Ship must have been added
	 * with an explicit name.
	 * @param shipName
	 */
	public void defeatOnShipLoss(String shipName);
	
	/**
	 * Adds a line to the bulleted list under "Tactical Objectives" in the mission description.
	 * @param item Text, with no leading dash.
	 */
	public void addBriefingItem(String item);
	
	/**
	 * Set a small blurb for the fleet that shows up on the mission detail and
	 * mission results screens to identify it. 
	 * @param side
	 * @param tagline
	 */
	public void setFleetTagline(FleetSide side, String tagline);
	
	/**
	 * Initialize map with the given size. By convention, 0,0 should be at the center.
	 * In other words, -minX = maxX and -minY = maxY.
	 * @param minX in pixels.
	 * @param maxX in pixels.
	 * @param minY in pixels.
	 * @param maxY in pixels.
	 */
	public void initMap(float minX, float maxX, float minY, float maxY);
	
	/**
	 * Adds a circular nebula to the map. The shape is slightly randomized.
	 * @param x
	 * @param y
	 * @param radius in pixels.
	 */
	public void addNebula(float x, float y, float radius);
	
	/**
	 * Add a battlefield objective to the map.
	 * 
	 * @param x
	 * @param y
	 * @param type one of "comm_relay", "nav_buoy", or "sensor_array".
	 * @param importance
	 */
	@Deprecated
	public void addObjective(float x, float y, String type, BattleObjectiveAPI.Importance importance);
	
	/**
	 * Add a battlefield objective to the map.
	 * 
	 * @param x
	 * @param y
	 * @param type one of "comm_relay", "nav_buoy", or "sensor_array".
	 */
	public void addObjective(float x, float y, String type);
	
	/**
	 * Add a planet or star to the map. 
	 * 
	 * @param x
	 * @param y
	 * @param radius
	 * @param type available types are defined in data/config/planets.json
	 * @param gravity Equal to maximum speed boost the planet provides to nearby ships.
	 */
	public void addPlanet(float x, float y, float radius, String type, float gravity);
	
	
	/**
	 * Add a planet or star to the map. 
	 * 
	 * @param x
	 * @param y
	 * @param radius
	 * @param type available types are defined in data/config/planets.json
	 * @param gravity Equal to maximum speed boost the planet provides to nearby ships.
	 * @param backgroundPlanet If true, planet is in the "background" and doesn't exert gravity or move relative to the viewport. x and y are then screen coordinates.
	 */
	void addPlanet(float x, float y, float radius, String type, float gravity, boolean backgroundPlanet);
	
	
	/**
	 * Bases the look of the planet on the PlanetAPI passed in. The look may have been altered dynamically
	 * using PlanetAPI.getSpec(), which would not be reflected by the planet's base type, as defined in planets.json.
	 * @param x
	 * @param y
	 * @param radius
	 * @param planet
	 * @param gravity
	 * @param backgroundPlanet
	 */
	void addPlanet(float x, float y, float radius, PlanetAPI planet, float gravity, boolean backgroundPlanet);
	
	/**
	 * Background planets at 0,0 get rendered at the center of the screen when the view is at the center of the map.
	 * 
	 * The bgWidth and bgHeight determine how much off-center the planet moves at the edges of the map.
	 * @param bgWidth
	 * @param bgHeight
	 */
	void setPlanetBgSize(float bgWidth, float bgHeight);
	
	/**
	 * Add an asteroid field to the map. An asteroid field is a band of asteroids moving in a direction
	 * across the map.
	 * @param x x coordinate of any point along the middle of the belt.
	 * @param y y coordinate of any point along the middle of the belt.
	 * @param angle direction, with 0 being to the right and 90 being up.
	 * @param width width of belt in pixels.
	 * @param minSpeed minimum speed of spawned asteroids.
	 * @param maxSpeed maximum speed of spawned asteroids.
	 * @param quantity approximate number of asteroids to keep in play as they're destroyed/move off map.
	 */
	public void addAsteroidField(float x, float y, float angle, float width,
								 float minSpeed, float maxSpeed, int quantity);
	
	
	public void addRingAsteroids(float x, float y, float angle, float width,
					float minSpeed, float maxSpeed, int quantity);

	
	/**
	 * Returns the fleet point cost of a fighter wing or ship variant.
	 * @param id
	 * @return
	 */
	public int getFleetPointCost(String id);

	
	public void addPlugin(EveryFrameCombatPlugin plugin);
	public void setBackgroundSpriteName(String background);
	public void addFleetMember(FleetSide side, FleetMemberAPI member);
	
	
	/**
	 * Make the background animate the same way it does in hyperspace.
	 * @param hyperspaceMode
	 */
	void setHyperspaceMode(boolean hyperspaceMode);

	void setNebulaTex(String nebulaTex);
	void setNebulaMapTex(String nebulaMapTex);

	void setBackgroundGlowColor(Color backgroundGlowColor);

	void initFleet(FleetSide side, String shipNamePrefix, FleetGoal goal,
			boolean useDefaultAI, int commandRating, int allyCommandRating);

	BattleCreationContext getContext();

	PersonAPI getDefaultCommander(FleetSide side);

	boolean hasNebula();
}





