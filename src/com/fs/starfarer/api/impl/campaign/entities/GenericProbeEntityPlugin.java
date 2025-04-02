package com.fs.starfarer.api.impl.campaign.entities;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.CampaignEngineGlowIndividualEngine;
import com.fs.starfarer.api.util.CampaignEngineGlowUtil;
import com.fs.starfarer.api.util.CampaignEntityMovementUtil;
import com.fs.starfarer.api.util.Misc;

/**
 * Not fully implemented. Approximate TODO:
 * 
 * Sprite size/facing
 * Smoother movement rather than move/turn/move
 * Smoother transition into orbit
 * Add trigger range for detecting when travel to a destination is finished
 * Add failsafe to detect when travel is taking too long
 * Make not clickable/figure out if it should have a tooltip
 * Possibly: add contrail?
 * 
 * @author Alex
 *
 */
public class GenericProbeEntityPlugin extends BaseCustomEntityPlugin { // implements EngineGlowControls {

	public static enum ProbeActionType {
		TRAVEL,
		ASSUME_ORBIT,
		EMIT_PING,
		PERFORM_ACTION,
		STOP,
		WAIT,
	}
	
	public static class GenericProbeParams {
		public float maxBurn = 20f;
		public float burnAccel = 5f;
		public float maxTurnRate = 120f;
		public float turnAccel = 120f;
		
		public Color fringe = new Color(255, 100, 0, 255);
		public Color flame = new Color(255, 165, 100, 255);
		public Color core = new Color(255, 255, 255, 255);
		public float engineGlowShiftRate = 2f;
		public float engineGlowLength = 100f;
		public float engineGlowWidth = 15f;
		public float engineGlowGlowSize = 100f;
		public float engineGlowTexSpanMult = 0.1f;
		
		protected List<ProbeAction> actions = new ArrayList<>();
		
		public void travelTo(Vector2f location) {
			ProbeAction a = new ProbeAction(ProbeActionType.TRAVEL);
			a.location = location;
			actions.add(a);
		}
		public void travelTo(SectorEntityToken target) {
			ProbeAction a = new ProbeAction(ProbeActionType.TRAVEL);
			a.target = target;
			actions.add(a);
		}
		public void travelInDir(float dir, float duration) {
			ProbeAction a = new ProbeAction(ProbeActionType.TRAVEL);
			a.dir = dir;
			a.duration = duration;
			actions.add(a);
		}
		
		public void assumeOrbit(SectorEntityToken target, float radius, float duration) {
			ProbeAction a = new ProbeAction(ProbeActionType.ASSUME_ORBIT);
			a.target = target;
			a.radius = radius;
			a.duration = duration;
			actions.add(a);
		}
		
		public void emitPing(String pingId) {
			ProbeAction a = new ProbeAction(ProbeActionType.EMIT_PING);
			a.pingId = pingId;
			actions.add(a);
		}
		public void performAction(Script action) {
			ProbeAction a = new ProbeAction(ProbeActionType.PERFORM_ACTION);
			a.action = action;
			actions.add(a);
		}
		public void wait(float duration) {
			ProbeAction a = new ProbeAction(ProbeActionType.WAIT);
			a.duration = duration;
			actions.add(a);
		}
		public void stop(float duration) {
			ProbeAction a = new ProbeAction(ProbeActionType.STOP);
			a.duration = duration;
			actions.add(a);
		}
	}
	
	
	public static class ProbeAction {
		public ProbeActionType type;
		public SectorEntityToken target;
		public Vector2f location;
		public float dir;
		public float radius;
		public float duration;
		public String pingId;
		public Script action;
		public ProbeAction(ProbeActionType type) {
			this.type = type;
		}
		
	}
	
	protected GenericProbeParams params;
	protected CampaignEntityMovementUtil movement;
	protected CampaignEngineGlowUtil engineGlow;
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		this.params = (GenericProbeParams) pluginParams;
		readResolve();
	}
	
	Object readResolve() {
		if (engineGlow == null) {
			engineGlow = new CampaignEngineGlowUtil(entity, params.fringe, params.core, params.flame,
													params.engineGlowShiftRate);
			CampaignEngineGlowIndividualEngine engine = new CampaignEngineGlowIndividualEngine(
					90f, params.engineGlowLength, params.engineGlowWidth, params.engineGlowGlowSize,
					new Vector2f(-10f, 0f), engineGlow);
			engine.setFlameTexSpanMult(params.engineGlowTexSpanMult);
			engineGlow.addEngine(engine);
		}
		
		if (movement == null) {
			float maxSpeed = Misc.getSpeedForBurnLevel(params.maxBurn);
			float accel = Misc.getSpeedForBurnLevel(params.burnAccel);
			movement = new CampaignEntityMovementUtil(entity, params.turnAccel, params.maxBurn, accel, maxSpeed);
			movement.setEngineGlow(engineGlow);
		}
		
		return this;
	}
	
	public CampaignEntityMovementUtil getMovement() {
		return movement;
	}

	public CampaignEngineGlowUtil getEngineGlow() {
		return engineGlow;
	}
	
	public void advance(float amount) {
		if (entity.isInCurrentLocation()) {
			engineGlow.advance(amount);
		}
		
		if (entity.hasTag(Tags.FADING_OUT_AND_EXPIRING) || params.actions.isEmpty()) {
			Misc.fadeAndExpire(entity);
			engineGlow.showSuppressed();
			return;
		}

		ProbeAction curr = params.actions.get(0);
		ProbeAction next = null;
		if (params.actions.size() > 1) next = params.actions.get(1);
		
		if (curr.type == ProbeActionType.TRAVEL) {
			if (curr.location == null && curr.target == null) {
				movement.moveInDirection(curr.dir);
				curr.duration -= amount;
				if (curr.duration <= 0) {
					params.actions.remove(0);
				}
			} else {
				Vector2f loc = curr.location;
				if (loc == null) loc = curr.target.getLocation();
				float dist = Misc.getDistance(entity.getLocation(), loc);
				
				if (next != null && next.type == ProbeActionType.ASSUME_ORBIT) {
					float orbitAngle = Misc.getAngleInDegrees(next.target.getLocation(), entity.getLocation());
					Vector2f away = Misc.getUnitVectorAtDegreeAngle(orbitAngle);
					away.scale(next.target.getRadius() + next.radius * 0.8f);
					loc = Vector2f.add(loc, away, new Vector2f());
				}
				movement.moveToLocation(loc);
				
				float checkDist = 100f + entity.getRadius();
				if (curr.target != null) checkDist += curr.target.getRadius();
				if (next != null && next.type == ProbeActionType.ASSUME_ORBIT) {
					checkDist -= 100f;
					checkDist += next.radius;
				}
				if (dist < checkDist) {
					params.actions.remove(0);
				}
			}
			
		} else if (curr.type == ProbeActionType.ASSUME_ORBIT) {
			if (entity.getOrbit() == null || entity.getOrbitFocus() != curr.target) {
				movement.leaveOrbit();
				float orbitAngle = Misc.getAngleInDegrees(curr.target.getLocation(), entity.getLocation());
				float orbitRadius = Misc.getDistance(entity.getLocation(), curr.target.getLocation());
				float orbitDays = orbitRadius / (10f + (float) Math.random() * 5f);
				orbitDays *= 0.2f;
				entity.setCircularOrbit(curr.target, orbitAngle, orbitRadius, orbitDays);
			}
			curr.duration -= amount;
			if (curr.duration <= 0) {
				params.actions.remove(0);
			}
		} else if (curr.type == ProbeActionType.EMIT_PING) {
			VisibilityLevel level = entity.getVisibilityLevelToPlayerFleet();
			if (level != VisibilityLevel.NONE) {
				Global.getSector().addPing(entity, Pings.REMOTE_SURVEY);
			}
			params.actions.remove(0);
		} else if (curr.type == ProbeActionType.STOP || curr.type == ProbeActionType.WAIT) {
			if (curr.type == ProbeActionType.STOP) {
				movement.stop();
			}
			curr.duration -= amount;
			if (curr.duration <= 0) {
				params.actions.remove(0);
			}
		} else if (curr.type == ProbeActionType.PERFORM_ACTION) {
			curr.action.run();
			params.actions.remove(0);
		}
		
		movement.advance(amount);
		
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		float alphaMult = viewport.getAlphaMult();
		alphaMult *= entity.getSensorFaderBrightness(); 
		alphaMult *= entity.getSensorContactFaderBrightness();
		if (alphaMult <= 0f) return;
		
		engineGlow.render(alphaMult);
	}
	
	public float getRenderRange() {
		return entity.getRadius() + 1000f; // for engine glow/trails
	}

	

}








