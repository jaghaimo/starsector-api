package com.fs.starfarer.api.impl.combat.dweller;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;


public class DarkenedGazeSystemAI implements ShipSystemAIScript {

	protected ShipAPI ship;
	protected CombatEngineAPI engine;
	protected ShipwideAIFlags flags;
	protected ShipSystemAPI system;
	protected DarkenedGazeSystemScript script;
	protected float systemFluxPerSecond;
	
	protected IntervalUtil tracker = new IntervalUtil(0.75f, 1.25f);
	
	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
		
		script = (DarkenedGazeSystemScript)system.getScript();
		
		systemFluxPerSecond = system.getFluxPerSecond();
	}
	
	protected ShipAPI targetOverride = null;
	
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		tracker.advance(amount);
		
		//boolean toggle = system.getSpec().isToggle();
		
		if (system.isActive()) {
			if (ship.getAI() instanceof ShipAIPlugin) {
				ShipAIPlugin b = (ShipAIPlugin) ship.getAI();
				b.setTargetOverride(targetOverride); // needs to be set every frame
			}
		} else {
			targetOverride = null;
		}
		
		if (tracker.intervalElapsed()) {
			
			if (system.getCooldownRemaining() > 0) return;
			if (system.isOutOfAmmo()) return;
			if (ship.getFluxTracker().isOverloadedOrVenting()) return;
			
			if (target != null) {
				if (target.isHulk() || !target.isAlive()) {
					target = null;
				}
			}
			
			float activeTimeRemaining = (ship.getMaxFlux() - ship.getCurrFlux()) / Math.max(1f, systemFluxPerSecond);
			
			boolean missilesInOpenArc = false;
			// too easy to shut it down with missiles
			if (missileDangerDir != null && false) {
				missilesInOpenArc = Misc.isInArc(ship.getFacing(), DarkenedGazeSystemScript.SHIELD_OPENING, 
									Misc.getAngleInDegrees(missileDangerDir));
			}
			
			boolean inRange = false;
			boolean inArc = false;
			boolean isFarFromArc = false;
			if (target != null) {
				float range = Misc.getDistance(ship.getLocation(), target.getLocation()) - 
							  Misc.getTargetingRadius(ship.getLocation(), target, false);
				inRange = range < script.getRange();
				inArc = Misc.isInArc(ship.getFacing(), 5f, 
						Misc.getAngleInDegrees(ship.getLocation(), target.getLocation()));
				if (!inArc) {
					isFarFromArc = !Misc.isInArc(ship.getFacing(), Math.max(30f, 60f - range * 0.05f), 
							Misc.getAngleInDegrees(ship.getLocation(), target.getLocation()));
				}
			}
			
			Vector2f to = Misc.getUnitVectorAtDegreeAngle(ship.getFacing());
			to.scale(script.getRange());
			Vector2f.add(ship.getLocation(), to, to);
			boolean ffDanger = false;
			if (script.isFFAConcern() ) {
				ffDanger = Global.getSettings().getFriendlyFireDanger(ship, null, 
								ship.getLocation(), to, Float.MAX_VALUE, 3f, script.getRange()) > 0.1f;
			} else {
				// pretend FF concern, so it doesn't fire right through friendlies but can clip them without
				// worrying too much about it
				ffDanger = Global.getSettings().getFriendlyFireDanger(ship, null, 
						ship.getLocation(), to, Float.MAX_VALUE, 3f, script.getRange()) > 0.5f;
			}
			if (system.isActive()) {
				flags.setFlag(AIFlags.DO_NOT_VENT);
				
				if (target == null || !inRange || isFarFromArc || missilesInOpenArc || ffDanger) {
					giveCommand();
					return;
				}
				
				if (activeTimeRemaining < 3f) {
					giveCommand();
					return;
				}
				
				return;
			}
			
			float minFireTime = system.getSpecAPI().getIn() + 6f;
			float fluxLevel = ship.getFluxLevel();
			
			if (fluxLevel > 0.9f || activeTimeRemaining < minFireTime) {
				return;
			}
			
			if (inRange && inArc && !missilesInOpenArc && !ffDanger) {
				giveCommand();
				targetOverride = target;
			}
		}
	}

	
	public void giveCommand() {
		ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
	}

}






















