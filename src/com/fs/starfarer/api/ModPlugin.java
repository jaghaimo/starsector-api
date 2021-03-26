package com.fs.starfarer.api;

import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.DroneLauncherShipSystemAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.thoughtworks.xstream.XStream;

/**
 * <b>NOTE: Instead of implementing this interface, extend BaseModPlugin instead.</b> 
 * 
 * A new instance of a mod's modPlugin class will be created when the game starts.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface ModPlugin {

	/**
	 * Called after all the core loading has finished and all the scripts have finished being compiled.
	 * Gives the mod an opportunity to load its own data using the relevant SettingsAPI methods.
	 * 
	 * @throws Exception
	 */
	void onApplicationLoad() throws Exception;
	
	/**
	 * Called when a new game is being created. This is the preferred way to generate the contents of the Sector,
	 * and is meant to replace generators.csv (which remains used for backwards compatibility).
	 */
	void onNewGame();
	
	/**
	 * Called before onGameLoad() and only if this mod was not previously enabled for the loaded saved game, or if this is
	 * a new game.
	 * @param wasEnabledBefore true if the mod was enabled for this saved game at some point in the past, though not immediately prior (or this method wouldn't be called at all).
	 */
	void onEnabled(boolean wasEnabledBefore);
	
	/**
	 * Called after a game has been loaded. Also called when a new game is created, after the onNewGame() plugins for all the
	 * mods have been called.
	 */
	void onGameLoad(boolean newGame);
	
	/**
	 * Called before a game is saved.
	 */
	void beforeGameSave();
	
	/**
	 * Called after a game is saved.
	 */
	void afterGameSave();
	
	
	/**
	 * Called if saving the game failed for some reason, such as running out of memory.
	 */
	void onGameSaveFailed();
	
	void onNewGameAfterProcGen();
	
	/**
	 * Called after the economy is loaded from economy.json and the initial steps are taken.
	 * At this point, calling get getMarket() on entities will return the proper market.
	 * The sequence of calls is:
	 * onNewGame(); // can add markets to the economy here programmatically
	 * //load economy and step it
	 * onNewGameAfterEconomyLoad();
	 */
	void onNewGameAfterEconomyLoad();
	
	
	/**
	 * After the new game has stepped through its initial 2 months.
	 */
	void onNewGameAfterTimePass();
	
	
	
	/**
	 * Called after F8 is pressed in combat while in devMode.
	 * Note: the game becomes potentially unstable after an F8-dev mode reload. That is,
	 * crashes may occur that would not have occured otherwise and are not indicative of bugs.
	 */
	void onDevModeF8Reload();
	
	/**
	 * Called to pick an AI implementation for a specific ship. Here instead of in CampaignPlugin to support custom
	 * AI for missions.
	 * 
	 * @param member Can be null.
	 * @param ship Can not be null. Could be a ship or a fighter.
	 * @return null, or a custom AI implementation.
	 */
	PluginPick<ShipAIPlugin> pickShipAI(FleetMemberAPI member, ShipAPI ship);
	
	/**
	 * Called to pick an AI implementation for a specific weapon.
	 * @param weapon
	 * @return
	 */
	PluginPick<AutofireAIPlugin> pickWeaponAutofireAI(WeaponAPI weapon);
	
	
	/**
	 * Called to pick drone AI implementation.
	 * @param drone
	 * @param mothership
	 * @param system use getSpecJson() to get at the system spec, if needed.
	 * @return
	 */
	PluginPick<ShipAIPlugin> pickDroneAI(ShipAPI drone, ShipAPI mothership, DroneLauncherShipSystemAPI system);
	
	
	/**
	 * Called to pick missile AI implementation.
	 * @param drone
	 * @param launchingShip
	 * @return
	 */
	PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip);

	
	
	void configureXStream(XStream x);
	
}









