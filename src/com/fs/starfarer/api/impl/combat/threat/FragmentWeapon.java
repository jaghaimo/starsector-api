package com.fs.starfarer.api.impl.combat.threat;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public interface FragmentWeapon {
	public int getNumFragmentsToFire();
	

	default public void showNoFragmentSwarmWarning(WeaponAPI w, ShipAPI ship) {
		boolean playerShip = Global.getCurrentState() == GameState.COMBAT &&
				Global.getCombatEngine() != null && Global.getCombatEngine().getPlayerShip() == ship;
		
		if (playerShip) {
			RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
			if (swarm == null && ship.getFullTimeDeployed() > 0.1f) {
				Global.getCombatEngine().maintainStatusForPlayerShip(w.getSpec(),
						Global.getSettings().getSpriteName("ui", "icon_tactical_fragment_swarm"),
						w.getDisplayName(), 
						"REQ: FRAGMENT SWARM",
						true);
			}
		}
	}
}
