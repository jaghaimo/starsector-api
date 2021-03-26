package com.fs.starfarer.api.impl.campaign.terrain;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.util.Misc;

public class ShoveFleetScript implements EveryFrameScript {

	public static float IMPACT_SPEED_DELTA = Global.getSettings().getSpeedPerBurnLevel();
	public static float DURATION_SECONDS = 0.1f;
	
	protected CampaignFleetAPI fleet;
	protected float elapsed;
	protected float angle;
	protected float intensity;
	protected float impact = IMPACT_SPEED_DELTA;
	protected Vector2f dV;
	
	public ShoveFleetScript(CampaignFleetAPI fleet, float direction, float intensity) {
		this.fleet = fleet;
		this.intensity = intensity;
		this.angle = direction;
		
		//DURATION_SECONDS = 0.1f;
		
		dV = Misc.getUnitVectorAtDegreeAngle(angle);
		
		float fleetWeightFactor = (float) fleet.getFleetPoints() / 200f;
		if (fleetWeightFactor > 1f) fleetWeightFactor = 1f;
		fleetWeightFactor = 0.5f + 1f * (1f - fleetWeightFactor);
		
		float speed = IMPACT_SPEED_DELTA * 40f * intensity;
		float impact = speed * 1f * fleetWeightFactor; 
		dV.scale(impact);
		dV.scale(1f / DURATION_SECONDS);
	}

	public void advance(float amount) {
		
		fleet.setOrbit(null);
		
		Vector2f v = fleet.getVelocity();
		fleet.setVelocity(v.x + dV.x * amount, v.y + dV.y * amount);
		
//		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
//		
//		float fleetWeightFactor = (float) fleet.getFleetPoints() / 200f;
//		if (fleetWeightFactor > 1f) fleetWeightFactor = 1f;
//		fleetWeightFactor = 0.5f + 1f * (1f - fleetWeightFactor);
//		
//		dir.scale(IMPACT_SPEED_DELTA * intensity * fleetWeightFactor* amount * 
//			(Math.min(20f, Math.max(10f, fleet.getCurrBurnLevel())) * 50f) * (0.5f + (float) Math.random() * 0.5f));
//		
//		Vector2f v = fleet.getVelocity();
//		fleet.setVelocity(v.x + dir.x, v.y + dir.y);
		
		elapsed += amount;
		
	}

	public boolean isDone() {
		return elapsed >= DURATION_SECONDS;
	}

	public boolean runWhilePaused() {
		return false;
	}

}
