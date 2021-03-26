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
 * Extend this class instead of implementing ModPlugin for convenience if you do not
 * intend to implement all the methods. This will also help avoid your mod breaking
 * when new methods are added to ModPlugin, since default implemenations will be
 * added here and your implementation will inherit them.
 * 
 * @author Alex Mosolov
 * 
 *
 * Copyright 2013 Fractal Softworks, LLC
 */
public class BaseModPlugin implements ModPlugin {
	
	public void afterGameSave() {
		
	}

	public void beforeGameSave() {
		
	}
	
	public void onGameSaveFailed() {
		
	}

	public void onApplicationLoad() throws Exception {
		
	}

	public void onEnabled(boolean wasEnabledBefore) {
		
	}

	public void onGameLoad(boolean newGame) {
		
	}

	public void onNewGame() {
		
	}
	
	public void onNewGameAfterEconomyLoad() {
		
	}

	public PluginPick<ShipAIPlugin> pickShipAI(FleetMemberAPI member, ShipAPI ship) {
		return null;
	}

	public PluginPick<AutofireAIPlugin> pickWeaponAutofireAI(WeaponAPI weapon) {
		return null;
	}

	public PluginPick<ShipAIPlugin> pickDroneAI(ShipAPI drone,
			ShipAPI mothership, DroneLauncherShipSystemAPI system) {
		return null;
	}

	public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile,
			ShipAPI launchingShip) {
		return null;
	}

	public void onNewGameAfterTimePass() {
		
	}

	public void configureXStream(XStream x) {
		
	}

	public void onNewGameAfterProcGen() {
		// TODO Auto-generated method stub
		
	}

	public void onDevModeF8Reload() {
		// TODO Auto-generated method stub
		
	}

}
