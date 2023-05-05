package com.fs.starfarer.api.campaign;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.campaign.comm.MessagePriority;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventManagerAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventPlugin;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.rules.RulesAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.loading.CampaignPingSpec;


/**
 * Note: generics can not be used in scripts.
 * They are used in this API purely to show the return/parameter types of functions more clearly.
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
@SuppressWarnings("unchecked")
public interface SectorAPI {
	/**
	 * Can be used (including by classes that are not part of the savegame - i.e. ModPlugin or 
	 * transient CampaignPlugin implementations) to save data between sessions.
	 * @return
	 */
	Map<String, Object> getPersistentData();
	
	void registerPlugin(CampaignPlugin plugin);
	void unregisterPlugin(String pluginId);
	
	StarSystemAPI getStarSystem(String name);
	StarSystemAPI createStarSystem(String name);
	List<StarSystemAPI> getStarSystems();
	void removeStarSystem(StarSystemAPI system);
	void setCurrentLocation(LocationAPI location);
	
	LocationAPI getHyperspace();
	
	/**
	 * @param fleet
	 * @param jumpLocation can be null. If not, fleet will attempt to reach it before jumping. Failure will result in aborted jump.
	 * @param dest
	 */
	void doHyperspaceTransition(CampaignFleetAPI fleet, SectorEntityToken jumpLocation, JumpDestination dest);
	
	
	//CampaignFleetAPI createFleet(String factionId, String fleetTypeId);
	CampaignClockAPI getClock();
	
	CampaignFleetAPI getPlayerFleet();
	
	FactionAPI getFaction(String factionId);
	List<FactionAPI> getAllFactions();
	
	List<String> getAllWeaponIds();
	List<String> getAllEmptyVariantIds();
	List<String> getAllFighterWingIds();
	
	
	CampaignUIAPI getCampaignUI();
	
	CampaignEventManagerAPI getEventManager();
	
	void setPaused(boolean paused);
	boolean isPaused();
	
	void addScript(EveryFrameScript script);
	void removeScriptsOfClass(Class c);
	void removeScript(EveryFrameScript script);
	
	void addTransientScript(EveryFrameScript script);
	void removeTransientScript(EveryFrameScript script);
	
	
	LocationAPI getCurrentLocation();
	
	LocationAPI getRespawnLocation();
	void setRespawnLocation(LocationAPI respawnLocation);
	Vector2f getRespawnCoordinates();
	
	
	CharacterDataAPI getCharacterData();
	
	
	/**
	 * Does nothing. Replaced with getIntelManager().
	 */
	@Deprecated void reportEventStage(CampaignEventPlugin event, String stage, MessagePriority priority);
	
	/**
	 * Does nothing. Replaced with getIntelManager().
	 */
	@Deprecated void reportEventStage(CampaignEventPlugin event, String stage, SectorEntityToken sendFrom, MessagePriority priority);
	
	/**
	 * Does nothing. Replaced with getIntelManager().
	 */
	@Deprecated void reportEventStage(CampaignEventPlugin event, String stage, SectorEntityToken sendFrom, MessagePriority priority, OnMessageDeliveryScript onDelivery);
	
	RulesAPI getRules();
	
	
	long getLastPlayerBattleTimestamp();
	boolean isLastPlayerBattleWon();
	
	/**
	 * "global" memory.
	 * @return
	 */
	MemoryAPI getMemory();
	MemoryAPI getMemoryWithoutUpdate();
	
	IntelDataAPI getIntel();
	
	
	/**
	 * Will look for the entity in all LocationAPIs.
	 * @param id
	 * @return
	 */
	SectorEntityToken getEntityById(String id);
	

	/**
	 * Iterates through hyperspace and all star systems.
	 * @param tag
	 * @return
	 */
	List<SectorEntityToken> getEntitiesWithTag(String tag);
	
	
	EconomyAPI getEconomy();

	
	
	
	void addListener(CampaignEventListener listener);
	void addTransientListener(CampaignEventListener listener);
	void addListenerWithTimeout(CampaignEventListener listener, float daysToKeep);
	void removeListener(CampaignEventListener listener);
	List<CampaignEventListener> getAllListeners();
	
	void reportPlayerMarketTransaction(PlayerMarketTransaction transaction);
	void reportFleetDespawned(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param);
	
	
	// this gets called automatically when a fleet is added to a location for the first time
	//void reportFleetSpawned(CampaignFleetAPI fleet);
	
	
	void reportFleetJumped(CampaignFleetAPI fleet, SectorEntityToken from, JumpDestination to);
	void reportFleetReachedEntity(CampaignFleetAPI fleet, SectorEntityToken entity);
	
	
	/**
	 * Called once per each autoresolve round of the battle.
	 * Also called after a player battle occurred.
	 * 
	 * Fleets and their compositons snapshotted right before the autoresolve round.
	 * @param battle
	 * @param primaryWinner 
	 */
	void reportBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle);
	
	/**
	 * Called after an autoresolve round that finishes a battle, after reportBattleOccurred.
	 * Also called after a player battle occurred.
	 * 
	 * Fleets and their compositons snapshotted right before the autoresolve round.
	 * @param battle
	 * @param primaryWinner
	 */
	void reportBattleFinished(CampaignFleetAPI primaryWinner, BattleAPI battle);

	
	/**
	 * Adjust the player's reputation with the specified faction, based on the action.
	 * 
	 * This method will pick the highest-priority ReputationActionResponsePlugin and
	 * call its handlePlayerReputationAction() method with these parameters.
	 * 
	 * See CoreCampaignPluginImpl.handlePlayerReputationAction() for an implementation example.
	 * 
	 * @param action
	 * @param faction
	 * @return
	 */
	ReputationAdjustmentResult adjustPlayerReputation(Object action, String factionId);
	
	/**
	 * Adjust the player's reputation with the specified person, based on the action.
	 * 
	 * This method will pick the highest-priority ReputationActionResponsePlugin and
	 * call its handlePlayerReputationAction() method with these parameters.
	 * 
	 * See CoreCampaignPluginImpl.handlePlayerReputationAction() for an implementation example.
	 * 
	 * @param action
	 * @param person
	 * @return
	 */
	ReputationAdjustmentResult adjustPlayerReputation(Object action, PersonAPI person);

	
	/**
	 * See data/campaign/pings.json for ping types.
	 * @param entity
	 * @param pingType
	 */
	EveryFrameScript addPing(SectorEntityToken entity, String pingType);

	
	/**
	 * Shouldn't have to call this as the relevant showInteractionDialog() methods already do.
	 * @param dialog
	 */
	void reportShowInteractionDialog(InteractionDialogAPI dialog);

	
	/**
	 * Player opened the core UI while interacting with an entity that has a market.
	 * @param market
	 */
	void reportPlayerOpenedMarket(MarketAPI market);

	
	void reportPlayerReputationChange(String faction, float delta);
	void reportPlayerReputationChange(PersonAPI person, float delta);
	
	void reportPlayerEngagement(EngagementResultAPI result);
	
	/**
	 * true during the during-game-creation time passing.
	 * @return
	 */
	boolean isInFastAdvance();

	PersonAPI getPlayerPerson();

	
	void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market);
	void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot);
	void reportPlayerClosedMarket(MarketAPI market);

	void reportPlayerActivatedAbility(AbilityPlugin ability, Object param);
	void reportPlayerDeactivatedAbility(AbilityPlugin ability, Object param);
	
	void reportPlayerDumpedCargo(CargoAPI cargo);
	
	ImportantPeopleAPI getImportantPeople();

	FactionAPI getPlayerFaction();

	EveryFrameScript addPing(SectorEntityToken entity, String pingType, Color colorOverride);

	ViewportAPI getViewport();

	/**
	 * All star systems and hyperspace.
	 * @return
	 */
	List<LocationAPI> getAllLocations();

	String getDifficulty();
	void setDifficulty(String difficulty);

	boolean isIronMode();

	boolean hasScript(Class<?> clazz);

	PluginPickerAPI getPluginPicker();

	String getSeedString();
	void setSeedString(String seedString);

	String genUID();

	PersistentUIDataAPI getUIData();

	void setInFastAdvance(boolean isInNewGameAdvance);
	boolean isInNewGameAdvance();
	void setInNewGameAdvance(boolean isInNewGameAdvance);

	void setPlayerFleet(CampaignFleetAPI playerFleet);

	void setLastPlayerBattleTimestamp(long lastPlayerBattleTimestamp);

	void setLastPlayerBattleWon(boolean lastPlayerBattleWon);

	void reportPlayerDidNotTakeCargo(CargoAPI cargo);

	List<EveryFrameScript> getScripts();
	List<EveryFrameScript> getTransientScripts();

	GenericPluginManagerAPI getGenericPlugins();

	EveryFrameScript addPing(SectorEntityToken entity, CampaignPingSpec custom);

	void reportEconomyMonthEnd();
	void reportEconomyTick(int iterIndex);

	MutableCharacterStatsAPI getPlayerStats();

	AutofitVariantsAPI getAutofitVariants();

	IntelManagerAPI getIntelManager();
	
	ListenerManagerAPI getListenerManager();

	void removeTransientScriptsOfClass(Class c);

	long getPlayerBattleSeed();
	void setPlayerBattleSeed(long nextPlayerBattleSeed);

	NascentGravityWellAPI createNascentGravityWell(SectorEntityToken target, float radius);

	boolean hasTransientScript(Class<?> clazz);

	List<SectorEntityToken> getCustomEntitiesWithTag(String tag);

	void doHyperspaceTransition(CampaignFleetAPI fleetAPI, SectorEntityToken jumpLocation, JumpDestination dest, float initialDelay);

	void layInCourseFor(SectorEntityToken target);

	boolean isFastForwardIteration();

	void setFastForwardIteration(boolean isFastForwardIteration);

	/**
	 * Same as getCharacterData().getMemoryWithoutUpdate()
	 * @return
	 */
	MemoryAPI getPlayerMemoryWithoutUpdate();

}






