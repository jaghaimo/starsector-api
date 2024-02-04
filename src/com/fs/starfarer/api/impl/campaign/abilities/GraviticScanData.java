package com.fs.starfarer.api.impl.campaign.abilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OrbitalStationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2.SlipstreamSegment;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class GraviticScanData {

	public static class GSPing {
		public float arc;
		public float angle;
		public float grav;
		public FaderUtil fader;
		public boolean withSound = false;
		public GSPing(float angle, float arc, float grav, float in, float out) {
			this.arc = arc;
			this.angle = angle;
			this.grav = grav;
			fader = new FaderUtil(0, in, out, false, true);
			fader.fadeIn();
		}
		
		public void advance(float days) {
			fader.advance(days);
			if (withSound && fader.getBrightness() >= 0.5f) {
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
				float dist = 1000f + (1f - Math.min(1f, grav / 200f)) * 1450f;
				loc.scale(dist);
				Vector2f.add(loc, Global.getSector().getPlayerFleet().getLocation(), loc);
				Global.getSoundPlayer().playSound("ui_neutrino_detector_ping", 1, 1, loc, new Vector2f());
				withSound = false;
			}
		}
		
		public boolean isDone() {
			return fader.isFadedOut();
		}
		
		
	}
	
	private GraviticScanAbility ability;
	
	
	private int resolution = 360;
	transient private float [] data;
	
	
	private List<GSPing> pings = new ArrayList<GSPing>();
	//private IntervalUtil noiseInterval = new IntervalUtil(0.01f, 0.02f);
	
	private IntervalUtil planetInterval = new IntervalUtil(0.01f, 0.01f);
	private IntervalUtil specialInterval = new IntervalUtil(0.075f, 0.125f);
	//private IntervalUtil specialInterval = new IntervalUtil(0.15f, 0.25f);
	
	public GraviticScanData(GraviticScanAbility ability) {
		this.ability = ability;
	}

	public void advance(float days) {
		if (ability.getFleet() == null || ability.getFleet().getContainingLocation() == null) return;
		
//		if (ability.getFleet().isInHyperspace()) {
//			data = null;
//			return;
//		}
		
		Iterator<GSPing> iter = pings.iterator();
		while (iter.hasNext()) {
			GSPing ping = iter.next();
			ping.advance(days);
			if (ping.isDone()) {
				iter.remove();
			}
		}
		
		
//		noiseInterval.advance(days);
//		if (noiseInterval.intervalElapsed() && false) {
//			float noiseLevel = getNoiseLevel();
//			int num = Math.round(noiseLevel * 10);
//			num = 1;
//			for (int i = 0; i < num; i++) {
//				float angle = (float) Math.random() * 360f;
////				float arc = 5f + 10f * (float) Math.random() + 10 * noiseLevel;
////				float grav = 5f + 50f * (float) Math.random() * noiseLevel;
//				float arc = 5f + 10f * (float) Math.random();
//				float grav = 30f + 80f * (float) Math.random();
//				
////				float in = 0.02f + 0.02f * (float) Math.random();
////				float out = 0.02f + 0.02f * (float) Math.random();
//				
//				float in = 0.05f + 0.1f * (float) Math.random();
//				in *= 0.25f;
//				float out = in;
//				
//				GSPing ping = new GSPing(angle, arc, grav, in, out);
//				pings.add(ping);
//			}
//		}
		
		planetInterval.advance(days);
		if (planetInterval.intervalElapsed()) {
			maintainHighSourcePings();
		}
		
		specialInterval.advance(days);
		if (specialInterval.intervalElapsed()) {
			doSpecialPings();
		}
		
		
		
		updateData();
		
		//System.out.println("Pings: " + pings.size());
	}
	
	
	
	public void updateData() {
		data = new float[resolution];
		
	
		float max = 0f;
		float incr = 360f / (float) resolution;
		for (GSPing ping : pings) {
			
			float b = ping.fader.getBrightness();
			if (b <= 0) continue;
			
			//b = (float) Math.sqrt(b);
			//b *= b;
			
			float arc = ping.arc;
			float mid = ping.angle;
			float half = (float) Math.ceil(0.5f * arc / incr);
			for (float i = -half; i <= half; i++) {
				float curr = mid + incr * i;
				int index = getIndex(curr);
				
				float intensity = 1f - Math.abs(i / half);
				intensity *= intensity;
				float value = ping.grav * intensity * b;
				data[index] += value;
				//float min = Math.min(data[index], value);
				//data[index] = Math.max(data[index], value);
				//if (data[index] > max) max = data[index];
			}
		}
		
	}
	
	public float getDataAt(float angle) {
		if (data == null) return 0f;
		int index = getIndex(angle);
		return data[index];
	}
	
	public int getIndex(float angle) {
		angle = Misc.normalizeAngle(angle);
		int index = (int)Math.floor(resolution * angle/360f);
		return index;
	}
	
	private int initialCount = 0;
	private List<SectorEntityToken> special = new ArrayList<SectorEntityToken>();


	//private float totalForce;
	public void doSpecialPings() {
		CampaignFleetAPI fleet = ability.getFleet();
		boolean abyss = Misc.isInAbyss(fleet);
		//abyss = false;
		if (fleet.isInHyperspace() && !abyss) return;
		
		Vector2f loc = fleet.getLocation();
		LocationAPI location = fleet.getContainingLocation();
		
		float neutrinoLowSkipProb = 0.8f;
		if (special.isEmpty()) {
//			for (SectorEntityToken entity : location.getAsteroids()) {
//				special.add(entity);
//			}
			for (Object object : location.getEntities(CustomCampaignEntityAPI.class)) {
				if (object instanceof SectorEntityToken) {
					SectorEntityToken entity = (SectorEntityToken) object;
					
					boolean neutrinoHigh = entity.hasTag(Tags.NEUTRINO_HIGH);
					if (neutrinoHigh) continue;
					
					if (abyss && !Misc.isInAbyss(entity)) continue;
					
					boolean neutrino = entity.hasTag(Tags.NEUTRINO);
					boolean neutrinoLow = entity.hasTag(Tags.NEUTRINO_LOW);
					boolean station = entity.hasTag(Tags.STATION);
					
					
					
					if (!neutrino && !neutrinoLow && !station) continue;
					if (neutrinoLow && (float) Math.random() < neutrinoLowSkipProb) continue;
					
					special.add(entity);
				}
			}
//			for (Object object : location.getEntities(OrbitalStationAPI.class)) {
//				if (object instanceof SectorEntityToken) {
//					SectorEntityToken entity = (SectorEntityToken) object;
//					special.add(entity);
//				}
//			}
			for (CampaignFleetAPI curr : location.getFleets()) {
				if (fleet == curr) continue;
				
				boolean neutrinoHigh = curr.hasTag(Tags.NEUTRINO_HIGH);
				if (neutrinoHigh) continue;
				
				if (abyss && !Misc.isInAbyss(fleet)) continue;
				
				if ((float) Math.random() < neutrinoLowSkipProb) continue;
				special.add(curr);
			}
			
			initialCount = special.size();
		}
		
		int batch = (int) Math.ceil(initialCount / 1f);
		for (int i = 0; i < batch; i++) {
			if (special.isEmpty()) break;
			
			SectorEntityToken curr = special.remove(0);
			
			float dist = Misc.getDistance(loc, curr.getLocation());
			
			float arc = Misc.computeAngleSpan(curr.getRadius(), dist);
			arc *= 2f;
			if (arc < 15) arc = 15;
			if (arc > 150f) arc = 150f;
			//arc += 30f;
			float angle = Misc.getAngleInDegrees(loc, curr.getLocation());
			
			float g = getGravity(curr);
			g *= getRangeGMult(dist);

			float in = 0.05f + 0.1f * (float) Math.random();
			in *= 0.25f;
			float out = in;
			out *= 2f;
			GSPing ping = new GSPing(angle, arc, g, in, out);
			ping.withSound = true;
			pings.add(ping);
		}
		
		
		long seed = (long) (location.getLocation().x * 1300000 + location.getLocation().y * 3700000 + 1213324234234L);
		Random random = new Random(seed);
		
		int numFalse = random.nextInt(5);
		//System.out.println(numFalse);
		
		for (int i = 0; i < numFalse; i++) {
			
			boolean constant = random.nextFloat() > 0.25f;
			if (!constant && (float) Math.random() < neutrinoLowSkipProb) {
				random.nextFloat();
				random.nextFloat();
				continue;
			}
			
			float arc = 15;
			float angle = random.nextFloat() * 360f;
			float in = 0.05f + 0.1f * (float) Math.random();
			in *= 0.25f;
			float out = in;
			out *= 2f;
			
			float g = 80 + random.nextFloat() * 60;
			
			GSPing ping = new GSPing(angle, arc, g, in, out);
			ping.withSound = true;
			pings.add(ping);
		}
			
		
	}
	
	public float getRangeGMult(float range) {
		range -= 3000;
		if (range < 0) range = 0;
		
		float max = 15000;
		if (range > max) range = max;
		
		
		return 1f - 0.85f * range / max;
	}
	
	
	public void maintainSlipstreamPings() {
		CampaignFleetAPI fleet = ability.getFleet();
		Vector2f loc = fleet.getLocation();
		LocationAPI location = fleet.getContainingLocation();
		
		float range = GraviticScanAbility.SLIPSTREAM_DETECTION_RANGE;

		if (Misc.isInsideSlipstream(fleet) || Misc.isInAbyss(fleet)) return;
		
		for (CampaignTerrainAPI ter : location.getTerrainCopy()) {
			if (ter.getPlugin() instanceof SlipstreamTerrainPlugin2) {
				SlipstreamTerrainPlugin2 plugin = (SlipstreamTerrainPlugin2) ter.getPlugin();
				if (plugin.containsEntity(fleet)) continue;
				List<SlipstreamSegment> inRange = new ArrayList<SlipstreamSegment>();
				List<SlipstreamSegment> near = plugin.getSegmentsNear(loc, range);
				int skip = 0;
				for (SlipstreamSegment curr : near) {
					if (skip > 0) {
						skip--;
						continue;
					}
					if (curr.bMult <= 0) continue;
					float dist = Misc.getDistance(loc, curr.loc);
					if (dist < range) {
						inRange.add(curr);
						skip = 5;
					}
				}
				if (!inRange.isEmpty()) {
					for (SlipstreamSegment curr : inRange) {
						float dist = Misc.getDistance(loc, curr.loc);
						
						float arc = Misc.computeAngleSpan(curr.width, dist);
						arc *= 2f;
						if (arc > 150f) arc = 150f;
						if (arc < 20) arc = 20;
						//arc += 30f;
						float angle = Misc.getAngleInDegrees(loc, curr.loc);
						float g = 500f;
						g *= .1f;
						g *= getRangeGMult(dist);
						float in = planetInterval.getIntervalDuration() * 5f;
						float out = in;
						GSPing ping = new GSPing(angle, arc, g, in, out);
						pings.add(ping);
					}
				}
			}
		}
		
	}
	
	
	public void maintainHighSourcePings() {
		CampaignFleetAPI fleet = ability.getFleet();
		Vector2f loc = fleet.getLocation();
		LocationAPI location = fleet.getContainingLocation();
		
		maintainSlipstreamPings();
		
		boolean abyss = Misc.isInAbyss(fleet);
		if (fleet.isInHyperspace() && !abyss) {
			return;
		}
		
		
//		Vector2f netForce = new Vector2f();

		List<SectorEntityToken> all = new ArrayList<SectorEntityToken>(location.getPlanets());
		for (Object object : location.getEntities(CustomCampaignEntityAPI.class)) {
			if (object instanceof SectorEntityToken) {
				SectorEntityToken entity = (SectorEntityToken) object;
				if (abyss && !Misc.isInAbyss(entity)) continue;
				
				boolean neutrinoHigh = entity.hasTag(Tags.NEUTRINO_HIGH);
				if (neutrinoHigh) {
					all.add(entity);
				}
			}
		}
		for (CampaignFleetAPI curr : location.getFleets()) {
			if (fleet == curr) continue;
			if (abyss && !Misc.isInAbyss(fleet)) continue;
			boolean neutrinoHigh = curr.hasTag(Tags.NEUTRINO_HIGH);
			if (neutrinoHigh) {
				all.add(curr);
			}
		}
		
		for (Object object : location.getEntities(OrbitalStationAPI.class)) {
			if (object instanceof SectorEntityToken) {
				SectorEntityToken entity = (SectorEntityToken) object;
				if (abyss && !Misc.isInAbyss(entity)) continue;
				all.add(entity);
			}
		}
		
		for (Object object : location.getJumpPoints()) {
			if (object instanceof SectorEntityToken) {
				SectorEntityToken entity = (SectorEntityToken) object;
				if (abyss && !Misc.isInAbyss(entity)) continue;
				all.add(entity);
			}
		}
		
		
		for (SectorEntityToken entity : all) {
			if (entity instanceof PlanetAPI) {
				PlanetAPI planet = (PlanetAPI) entity;
				if (planet.getSpec().isNebulaCenter()) continue;
			}
			if (entity.getRadius() <= 0) continue;
			
			float dist = Misc.getDistance(loc, entity.getLocation());
			
			float arc = Misc.computeAngleSpan(entity.getRadius(), dist);
			arc *= 2f;
			if (arc > 150f) arc = 150f;
			if (arc < 20) arc = 20;
			//arc += 30f;
			float angle = Misc.getAngleInDegrees(loc, entity.getLocation());
			
			float g = getGravity(entity);
			//g /= dist;
			
			g *= .1f;
			if (entity.hasTag(Tags.NEUTRINO_HIGH) || entity instanceof OrbitalStationAPI) {
				g *= 2f;
			}
			
			g *= getRangeGMult(dist);
			
//			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
//			dir.scale(g);
//			Vector2f.add(netForce, dir, netForce);
//			if (Misc.isInArc(90, 30, angle)) {
//				System.out.println("fwefewf");
//			}
			float in = planetInterval.getIntervalDuration() * 5f;
			float out = in;
			GSPing ping = new GSPing(angle, arc, g, in, out);
			pings.add(ping);
		}
		
//		for (String key : objectPings.keySet()) {
//			if (!seen.contains(key)) {
//				GSPing ping = objectPings.get(key);
//				ping.fader.setBounceDown(true);
//			}
//		}
		
		//totalForce = netForce.length();
		//totalForce = maxG;
		
		//System.out.println("Pings: " + pings.size());
		//System.out.println("Noise: " + getNoiseLevel());
		//System.out.println("Force: " + totalForce);
	}
	
//	public float getTotalForce() {
//		return totalForce;
//	}
//	
//	public float getNoiseLevel() {
//		//if (true) return 0f;
//		
//		float minForce = 20f;
//		float noiseOneAt = 150;
//		
//		if (totalForce <= minForce) return 0f;
//		float noise = (totalForce - minForce) / (noiseOneAt - minForce);
//		if (noise > 1) noise = 1;
//		return noise;
//	}

	public float getGravity(SectorEntityToken entity) {
		float g = entity.getRadius();
		
		if (entity instanceof PlanetAPI) {
			PlanetAPI planet = (PlanetAPI) entity;
			//if (g < 200) g = 200;
			
			g *= 2f;
			
			if (planet.getSpec().isBlackHole()) {
				g *= 2f;
			}
		}
		
		if (entity instanceof OrbitalStationAPI) {
			g *= 4f;
			if (g > 200) g = 200;
		}
		
		if (entity instanceof CustomCampaignEntityAPI) {
			g *= 4f;
			if (g > 200) g = 200;
		}
		
		if (entity instanceof CampaignFleetAPI) {
			g *= 2f;
			if (g > 200) g = 200;
		}
		
//		if (entity.getName().equals("Asteroid")) {
//			g *= 50f;
//		}
		
		return g;
	}
	
}



























