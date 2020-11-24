package com.fs.starfarer.api;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public class Global {
	private static SettingsAPI settingsAPI;
	private static SectorAPI sectorAPI;
	private static FactoryAPI factory;
	private static SoundPlayerAPI soundPlayer;
	private static CombatEngineAPI combatEngine;
	
	
	public static GameState getCurrentState() {
		return settingsAPI.getCurrentState();
	}
	
	@SuppressWarnings("unchecked")
	public static Logger getLogger(Class c) {
		Logger log = Logger.getLogger(c);
		return log;
	}
	
	/**
	 * Should only be used in the campaign.
	 * @return
	 */
	public static FactoryAPI getFactory() {
		return factory;
	}

	public static void setFactory(FactoryAPI factory) {
		Global.factory = factory;
	}

	public static SoundPlayerAPI getSoundPlayer() {
		return soundPlayer;
	}

	public static void setSoundPlayer(SoundPlayerAPI sound) {
		Global.soundPlayer = sound;
	}

	public static SettingsAPI getSettings() {
		return settingsAPI;
	}

	public static void setSettings(SettingsAPI api) {
		Global.settingsAPI = api;
	} 
	
	public static SectorAPI getSector() {
		return sectorAPI;
	}
	
	public static CombatEngineAPI getCombatEngine() {
		return combatEngine;
	}

	public static void setCombatEngine(CombatEngineAPI combatEngine) {
		Global.combatEngine = combatEngine;
	}

	public static void setSector(SectorAPI api) {
		Global.sectorAPI = api;
	}
	
	@Deprecated
	public static SettingsAPI getSettingsAPI() {
		return settingsAPI;
	}
	@Deprecated
	public static SectorAPI getSectorAPI() {
		return sectorAPI;
	}
}
