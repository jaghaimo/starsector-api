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
import com.fs.starfarer.api.util.Misc;

public class GateExplosionScript implements EveryFrameScript {

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
						jp.getMemoryWithoutUpdate().set(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY, true);
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




