package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AsteroidAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.util.Misc;

public class AsteroidImpact implements EveryFrameScript {

	public static float SAFE_BURN_LEVEL = 5.01f;
	public static float IMPACT_SPEED_DELTA = Global.getSettings().getSpeedPerBurnLevel();
	public static float DURATION_SECONDS = 0.2f;
	
	protected CampaignFleetAPI fleet;
	protected float elapsed;
	protected float angle;
	protected float impact = IMPACT_SPEED_DELTA;
	
	public AsteroidImpact(CampaignFleetAPI fleet) {
		this.fleet = fleet;
		
		Vector2f v = fleet.getVelocity();
		angle = Misc.getAngleInDegrees(v);
		float speed = v.length();
		if (speed < 10) angle = fleet.getFacing();

		float mult = Misc.getFleetRadiusTerrainEffectMult(fleet);
		
		//float arc = 120f;
		float arc = 120f - 60f * mult; // larger fleets suffer more direct collisions that slow them down more
		
		angle += (float) Math.random() * arc - arc/2f;
		angle += 180f;
		
		if (fleet.getCurrBurnLevel() <= SAFE_BURN_LEVEL || mult <= 0) {
			elapsed = DURATION_SECONDS;
		} else if (fleet.isInCurrentLocation()) {
			Vector2f test = Global.getSector().getPlayerFleet().getLocation();
			float dist = Misc.getDistance(test, fleet.getLocation());
			if (dist < HyperspaceTerrainPlugin.STORM_STRIKE_SOUND_RANGE) {
				float volumeMult = 1f - (dist / HyperspaceTerrainPlugin.STORM_STRIKE_SOUND_RANGE);
				volumeMult = (float) Math.sqrt(volumeMult);
				//volumeMult *= 0.4f;
				volumeMult *= 0.75f;
				volumeMult *= 0.5f + 0.5f * mult;
				if (volumeMult > 0) {
					//Global.getSoundPlayer().playSound("collision_asteroids", 1f, 1f * volumeMult, fleet.getLocation(), Misc.ZERO);
					Global.getSoundPlayer().playSound("hit_shield_heavy_gun", 1f, 1f * volumeMult, fleet.getLocation(), Misc.ZERO);
				}
			}
			
			if (fleet.isPlayerFleet()) {
				Global.getSector().getCampaignUI().addMessage("Asteroid impact on drive bubble", Misc.getNegativeHighlightColor());
			}
			
//			if (fleet.isPlayerFleet()) {
//				System.out.println("SPAWNED ---------");
//			}

//			int num = (int) (6f + (float) Math.random() * 5f);
//			for (int i = 0; i < num; i++) {
//				angle = angle + (float) Math.random() * 30f - 15f;
//				float size = 10f + (float) Math.random() * 6f;
//				float size = 10f + (float) Math.random() * 6f;
//				size = 4f + 4f * (float) Math.random();
//				
//				AsteroidAPI asteroid = fleet.getContainingLocation().addAsteroid(size);
//				asteroid.setFacing((float) Math.random() * 360f);
//				Vector2f av = Misc.getUnitVectorAtDegreeAngle(angle + 180f);
//				av.scale(fleet.getVelocity().length() + (20f + 20f * (float) Math.random()) * mult);
//				asteroid.getVelocity().set(av);
//				Vector2f al = Misc.getUnitVectorAtDegreeAngle(angle + 180f);
//				//al.scale(fleet.getRadius() + asteroid.getRadius());
//				al.scale(fleet.getRadius());
//				Vector2f.add(al, fleet.getLocation(), al);
//				asteroid.setLocation(al.x, al.y);
//				
//				float sign = Math.signum(asteroid.getRotation());
//				asteroid.setRotation(sign * (50f + 50f * (float) Math.random()));
//				
//				Misc.fadeInOutAndExpire(asteroid, 0.2f, 1.5f + 1f * (float) Math.random(), 1f);
//			}
			
			float size = 10f + (float) Math.random() * 6f;
			size *= 0.67f;
			AsteroidAPI asteroid = fleet.getContainingLocation().addAsteroid(size);
			asteroid.setFacing((float) Math.random() * 360f);
			Vector2f av = Misc.getUnitVectorAtDegreeAngle(angle + 180f);
			av.scale(fleet.getVelocity().length() + (20f + 20f * (float) Math.random()) * mult);
			asteroid.getVelocity().set(av);
			Vector2f al = Misc.getUnitVectorAtDegreeAngle(angle + 180f);
			//al.scale(fleet.getRadius() + asteroid.getRadius());
			al.scale(fleet.getRadius());
			Vector2f.add(al, fleet.getLocation(), al);
			asteroid.setLocation(al.x, al.y);
			
			float sign = Math.signum(asteroid.getRotation());
			asteroid.setRotation(sign * (50f + 50f * (float) Math.random()));
			
			Misc.fadeInOutAndExpire(asteroid, 0.2f, 1f + 1f * (float) Math.random(), 1f);
			
			//mult = 1f;
			Vector2f iv = fleet.getVelocity();
			iv = new Vector2f(iv);
			iv.scale(0.7f);
			float glowSize = 100f + 100f * mult + 50f * (float) Math.random();
			Color color = new Color(255, 165, 100, 255);
			Misc.addHitGlow(fleet.getContainingLocation(), al, iv, glowSize, color);
		}
		
//		if (fleet.isPlayerFleet()) {
//			fleet.addFloatingText("Impact!", Misc.getNegativeHighlightColor(), 0.5f);
//		}
	}

	public void advance(float amount) {
		
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
		
		//dir.scale(IMPACT_SPEED_DELTA * mult * amount * 75f * (0.5f + (float) Math.random() * 0.5f));
		
		float mult = Misc.getFleetRadiusTerrainEffectMult(fleet);
		dir.scale(IMPACT_SPEED_DELTA * mult * amount * 
				(fleet.getCurrBurnLevel() * 10f) * (0.5f + (float) Math.random() * 0.5f));
		
		Vector2f v = fleet.getVelocity();
		fleet.setVelocity(v.x + dir.x, v.y + dir.y);
		
//		Vector2f loc = fleet.getLocation();
//		fleet.setLocation(loc.x + dir.x, loc.y + dir.y);
		
		elapsed += amount;
		
	}

	public boolean isDone() {
		return elapsed >= DURATION_SECONDS;
	}

	public boolean runWhilePaused() {
		return false;
	}

}
