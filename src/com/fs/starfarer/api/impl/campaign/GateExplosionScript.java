package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ExplosionEntityPlugin.ExplosionFleetDamage;
import com.fs.starfarer.api.impl.campaign.ExplosionEntityPlugin.ExplosionParams;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class GateExplosionScript implements EveryFrameScript {

	public static class SystemCutOffRemoverScript implements EveryFrameScript {
		public StarSystemAPI system;
		public IntervalUtil interval = new IntervalUtil(0.5f, 1.5f);
		public boolean done;
		public float elapsed = 0f;
		
		public SystemCutOffRemoverScript(StarSystemAPI system) {
			super();
			this.system = system;
		}
		public boolean isDone() {
			return done;
		}
		public boolean runWhilePaused() {
			return false;
		}

		public void advance(float amount) {
			if (done) return;
			
			float days = Global.getSector().getClock().convertToDays(amount);
			elapsed += days;
			interval.advance(days);
			if (interval.intervalElapsed() && elapsed > 10f) { // make sure gate's already exploded
				boolean allJPUsable = true;
				boolean anyJPUsable = false;
				for (SectorEntityToken jp : system.getJumpPoints()) {
					allJPUsable &= !jp.getMemoryWithoutUpdate().getBoolean(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY);
					anyJPUsable |= !jp.getMemoryWithoutUpdate().getBoolean(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY);
				}
				//if (allJPUsable) {
				if (anyJPUsable) {
					system.removeTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER);
					done = true;
				}
			}
		}
		
	}
	
	public static float UNSTABLE_DAYS_MIN = 200;
	public static float UNSTABLE_DAYS_MAX = 400;
	
	protected boolean done = false;
	protected boolean playedWindup = false;
	protected SectorEntityToken explosion = null;
	protected float delay = 0.5f;
	protected float delay2 = 1f;
	
	protected SectorEntityToken gate;
	
	
	public GateExplosionScript(SectorEntityToken gate) {
		this.gate = gate;
		//GateEntityPlugin plugin = (GateEntityPlugin) gate.getCustomPlugin();
		
		// do this immediately so player can't establish a colony between when the gate explosion begins
		// and when it ends
		StarSystemAPI system = gate.getStarSystem();
		if (system != null) {
			system.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER);
			system.addScript(new SystemCutOffRemoverScript(system));
		}
		
		delay = 1.2f; // plus approximately 2 seconds from how long plugin.jitter() takes to build up
		
	}


	public void advance(float amount) {
		if (done) return;
		
		if (!playedWindup) {
			if (gate.isInCurrentLocation()) {
				Global.getSoundPlayer().playSound("gate_explosion_windup", 1f, 1f, gate.getLocation(), Misc.ZERO);
			}
			playedWindup = true;
		}
		
		
		GateEntityPlugin plugin = (GateEntityPlugin) gate.getCustomPlugin();
		plugin.jitter();
		
		if (plugin.getJitterLevel() > 0.9f) {
			delay -= amount;
		}
		if (delay <= 0 && explosion == null) {
			//Misc.fadeAndExpire(gate);

			LocationAPI cl = gate.getContainingLocation();
			Vector2f loc = gate.getLocation();
			Vector2f vel = gate.getVelocity();
			
			float size = gate.getRadius() + 2000f;
			Color color = new Color(255, 165, 100);
			color = new Color(100, 255, 165);
			color = new Color(150, 255, 200);
			color = new Color(100, 200, 150, 255);
			color = new Color(255, 255, 100, 255);
			color = new Color(100, 255, 150, 255);
			//color = new Color(255, 155, 255);
			//ExplosionParams params = new ExplosionParams(color, cl, loc, size, 1f);
			ExplosionParams params = new ExplosionParams(color, cl, loc, size, 2f);
			params.damage = ExplosionFleetDamage.HIGH;
			
			explosion = cl.addCustomEntity(Misc.genUID(), "Gate Explosion", 
											Entities.EXPLOSION, Factions.NEUTRAL, params);
			explosion.setLocation(loc.x, loc.y);
		}
		
		if (explosion != null) {
			delay2 -= amount;
			if (!explosion.isAlive() || delay2 <= 0) {
				done = true;
				
				StarSystemAPI system = gate.getStarSystem();
				if (system != null) {
					for (SectorEntityToken jp : system.getJumpPoints()) {
						float days = UNSTABLE_DAYS_MIN + (UNSTABLE_DAYS_MAX - UNSTABLE_DAYS_MIN) * (float) Math.random();
						jp.getMemoryWithoutUpdate().set(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY, true, days);
					}
				}
			}
		}
	}
	
	
	
	
	public boolean isDone() {
		return done;
	}

	public boolean runWhilePaused() {
		return false;
	}
	
}




