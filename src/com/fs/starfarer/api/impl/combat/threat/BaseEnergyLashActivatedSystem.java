package com.fs.starfarer.api.impl.combat.threat;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

public abstract class BaseEnergyLashActivatedSystem extends BaseShipSystemScript implements EnergyLashActivatedSystem {
	
	protected boolean inited = false;
	
	protected void init(ShipAPI ship) {
		ship.getSystem().setAmmo(0);
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		//boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			//player = ship == Global.getCombatEngine().getPlayerShip();
		} else {
			return;
		}
		if (ship.getSystem() == null) return;
		
		if (!inited) {
			init(ship);
			inited = true;
		}
		
		applyImpl(ship, stats, id, state, effectLevel);
	}
	
	protected abstract void applyImpl(ShipAPI ship, MutableShipStatsAPI stats, String id, State state, float effectLevel);
	
	
	@Override
	public void hitWithEnergyLash(ShipAPI overseer, ShipAPI ship) {
		if (ship.getSystem() == null) return;
		
		ship.getSystem().setAmmo(1);
		ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
	}

	protected void setStandardJitter(ShipAPI ship, State state, float effectLevel) {
		if (ship.isHulk()) return;
		
		float jitterLevel = effectLevel;
		jitterLevel = 0.5f + 0.5f * jitterLevel;
		if (state == State.OUT) {
			jitterLevel *= jitterLevel;
		}
		Color base = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR;
		Color overColor = Misc.setAlpha(base, 255);;
		ship.setJitter(this, overColor, jitterLevel, 1, 0f, 4f);
		ship.setJitterShields(false);
		ship.setCircularJitter(true);
	}
	
	protected void makeAllGroupsAutofireOneFrame(ShipAPI ship) {
		for (WeaponGroupAPI g : ship.getWeaponGroupsCopy()) {
			if (!g.isAutofiring()) {
				g.toggleOn();
			}
		}
		ship.resetSelectedGroup();
		ship.blockCommandForOneFrame(ShipCommand.TOGGLE_AUTOFIRE);
		ship.blockCommandForOneFrame(ShipCommand.SELECT_GROUP);
		ship.blockCommandForOneFrame(ShipCommand.USE_SELECTED_GROUP);
		ship.blockCommandForOneFrame(ShipCommand.FIRE);
		
		Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
		if (test instanceof ShipAPI) {
			ShipAPI target = (ShipAPI) test;
			ship.setShipTarget(target);
		}
	}
	
}








