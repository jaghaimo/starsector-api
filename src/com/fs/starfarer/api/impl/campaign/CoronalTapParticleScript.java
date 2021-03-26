package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.world.ZigLeashAssignmentAI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class CoronalTapParticleScript implements EveryFrameScript {

	protected SectorEntityToken tap;
	
	protected IntervalUtil spawn = new IntervalUtil(0.05f, 0.1f);
	
	public CoronalTapParticleScript(SectorEntityToken tap) {
		super();
		this.tap = tap;
	}

	public void advance(float amount) {
		if (tap.isDiscoverable()) return;
		if (!tap.getMemoryWithoutUpdate().contains("$usable")) return;
		if (!tap.isInCurrentLocation()) return;

		if (spawn == null) { // nothing to see here, just making an older dev save work 
			spawn = new IntervalUtil(0.05f, 0.1f);
		}
		
		float days = Misc.getDays(amount);
		spawn.advance(days * 5f);
		if (spawn.intervalElapsed()) {

//			float minDur = 0.5f;
//			float maxDur = 0.75f;
			float minSize = 10f;
			float maxSize = 20f;
			
			float sizeMult = 30f;
			
			minSize *= sizeMult;
			maxSize *= sizeMult;
			
			if ((float) Math.random() < 0.01f) {
				ZigLeashAssignmentAI.spawnMote(tap);
			}
			
			
			
			List<PlanetAPI> stars = new ArrayList<PlanetAPI>();
			PlanetAPI closest = null;
			float minDist = Float.MAX_VALUE;
			for (PlanetAPI star : tap.getContainingLocation().getPlanets()) {
				if (!star.isStar()) continue;
				
				float ctcDist = Misc.getDistance(tap.getLocation(), star.getLocation());
				float dist = ctcDist - star.getRadius() - tap.getRadius();
				if (dist > 2000) continue;
				
				if (ctcDist < minDist) {
					minDist = ctcDist;
					closest = star;
				}
				
				stars.add(star);
			}
//			float minDist = Float.MAX_VALUE;
//			closest = null;
//			for (PlanetAPI star : tap.getContainingLocation().getPlanets()) {
//				if (!star.isStar()) continue;
//				
//				float dist = Misc.getDistance(tap.getLocation(), star.getLocation());
//				if (dist < minDist) {
//					minDist = dist;
//					closest = star;
//				}
//			}
//			if (closest != null) {
//				stars.add(closest);
//			}
			
			for (PlanetAPI star : stars) {
				float dirToTap = Misc.getAngleInDegrees(star.getLocation(), tap.getLocation());
				Vector2f unitToTap = Misc.getUnitVectorAtDegreeAngle(dirToTap);
				Vector2f focusLoc = new Vector2f(unitToTap);
				focusLoc.scale(100f + star.getRadius() + tap.getRadius());
				Vector2f.add(star.getLocation(), focusLoc, focusLoc);
				
				//focusLoc = tap.getLocation();
				
				float ctcDist = Misc.getDistance(focusLoc, star.getLocation());
				float dist = ctcDist - star.getRadius() - tap.getRadius();
				
				int glows = 3;
				float minRadius = dist + (star.getRadius() * 0.2f);
				float maxRadius = dist + (star.getRadius() * 0.4f);
				
				Color color = star.getSpec().getCoronaColor();
				
				float colorScale = 0.25f;
				colorScale = 0.1f + 0.15f / (float) stars.size();
				if (closest == star) colorScale = 0.25f;
				color = Misc.scaleColor(color, colorScale);
				//Color white = Misc.scaleColor(Color.white, colorScale);
				
				float dirToStar = Misc.getAngleInDegrees(focusLoc, star.getLocation());
				float arc = star.getRadius() / ((float) Math.PI * (ctcDist - tap.getRadius())) * 360f; 
				
				for (int i = 0; i < glows; i++) {
					float radius = minRadius + (float) Math.random() * (maxRadius - minRadius);

					float angle = dirToStar - arc / 2f + arc * (float) Math.random();
					
					Vector2f unit = Misc.getUnitVectorAtDegreeAngle(angle);
					float x = unit.x * radius + focusLoc.x;
					float y = unit.y * radius + focusLoc.y;
					
					Vector2f loc = new Vector2f(x, y);
					//Vector2f loc = Misc.getPointAtRadius(focusLoc, radius);
					
					Vector2f vel = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(loc, focusLoc));
					float travelDist = Misc.getDistance(loc, focusLoc) - tap.getRadius() * 0.7f - 100f;
					travelDist = Math.min(travelDist, maxRadius - minRadius + 100f);
//					float dur = minDur + (float) Math.random() * (maxDur - minDur);
//					dur *= 2f;
//					float speed = travelDist / dur;
					float speed = 100f + 100f * (float) Math.random();
					float dur = travelDist / speed;
					
					vel.scale(speed);
					
					float size = minSize + (float) Math.random() * (maxSize - minSize);
					
					float rampUp = 0.5f;
					//Misc.addGlowyParticle(tap.getContainingLocation(), loc, vel, size, rampUp, dur, color);
					
					tap.getContainingLocation().addParticle(loc, vel,
							size, 0.4f, rampUp, dur, color);
					tap.getContainingLocation().addParticle(loc, vel,
							size * 0.25f, 0.4f, rampUp, dur, color);
//					tap.getContainingLocation().addParticle(loc, vel,
//							size * 0.15f, 1f, rampUp, dur, white);
				}
			}
		}
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
}












