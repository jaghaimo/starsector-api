package com.fs.starfarer.api.impl.combat;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class PlasmaJetsStats extends BaseShipSystemScript {

	public static float SPEED_BONUS = 125f;
	public static float TURN_BONUS = 20f;
	
	private Color color = new Color(100,255,100,255);
	
//	private Color [] colors = new Color[] {
//			new Color(140, 100, 235),
//			new Color(180, 110, 210),
//			new Color(150, 140, 190),
//			new Color(140, 190, 210),
//			new Color(90, 200, 170), 
//			new Color(65, 230, 160),
//			new Color(20, 220, 70)
//	};
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
			stats.getMaxTurnRate().unmodify(id);
		} else {
			stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
			stats.getAcceleration().modifyPercent(id, SPEED_BONUS * 3f * effectLevel);
			stats.getDeceleration().modifyPercent(id, SPEED_BONUS * 3f * effectLevel);
			stats.getTurnAcceleration().modifyFlat(id, TURN_BONUS * effectLevel);
			stats.getTurnAcceleration().modifyPercent(id, TURN_BONUS * 5f * effectLevel);
			stats.getMaxTurnRate().modifyFlat(id, 15f);
			stats.getMaxTurnRate().modifyPercent(id, 100f);
		}
		
		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			
			ship.getEngineController().fadeToOtherColor(this, color, new Color(0,0,0,0), effectLevel, 0.67f);
			//ship.getEngineController().fadeToOtherColor(this, Color.white, new Color(0,0,0,0), effectLevel, 0.67f);
			ship.getEngineController().extendFlame(this, 2f * effectLevel, 0f * effectLevel, 0f * effectLevel);
			
//			String key = ship.getId() + "_" + id;
//			Object test = Global.getCombatEngine().getCustomData().get(key);
//			if (state == State.IN) {
//				if (test == null && effectLevel > 0.2f) {
//					Global.getCombatEngine().getCustomData().put(key, new Object());
//					ship.getEngineController().getExtendLengthFraction().advance(1f);
//					for (ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
//						if (engine.isSystemActivated()) {
//							ship.getEngineController().setFlameLevel(engine.getEngineSlot(), 1f);
//						}
//					}
//				}
//			} else {
//				Global.getCombatEngine().getCustomData().remove(key);
//			}
		}
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("improved maneuverability", false);
		} else if (index == 1) {
			return new StatusData("+" + (int)SPEED_BONUS + " top speed", false);
		}
		return null;
	}
}
