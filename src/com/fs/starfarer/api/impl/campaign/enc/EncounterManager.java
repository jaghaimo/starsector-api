package com.fs.starfarer.api.impl.campaign.enc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class EncounterManager implements EveryFrameScript, EncounterPointProvider {

	public static String EP_TYPE_SLIPSTREAM = "ep_type_slipstream";
	public static String EP_TYPE_OUTSIDE_SYSTEM = "ep_type_outside_system";
	
	public static float MAX_EP_DIST_ADD = 4000;
	
	public static List<EPEncounterCreator> CREATORS = new ArrayList<EPEncounterCreator>();
	static {
		CREATORS.add(new SlipstreamPirateEPEC());
		CREATORS.add(new SlipstreamLuddicPathEPEC());
		CREATORS.add(new SlipstreamMercenaryEPEC());
		CREATORS.add(new SlipstreamScavengerEPEC());
		CREATORS.add(new SlipstreamNoEPEC());
		
		CREATORS.add(new OutsideSystemRemnantEPEC());
		CREATORS.add(new OutsideSystemNoEPEC());
		
		CREATORS.add(new AbyssalLightEPEC());
		CREATORS.add(new AbyssalRogueStellarObjectEPEC());
		CREATORS.add(new AbyssalRogueStellarObjectDireHintsEPEC());
		CREATORS.add(new AbyssalNoEPEC());
	}
	
	
	public static EncounterManager getInstance() {
		String key = "$encounterManager";
		EncounterManager calc = (EncounterManager) Global.getSector().getMemoryWithoutUpdate().get(key);
		if (calc == null) {
			for (EveryFrameScript curr : Global.getSector().getScripts()) {
				if (curr instanceof EncounterManager) {
					calc = (EncounterManager) curr;
					Global.getSector().getMemoryWithoutUpdate().set(key, calc);
					break;
				}
			}
		}
		return calc;
	}
	
	protected Object readResolve() {
		return this;
	}
	
	protected Random random = new Random();
	protected IntervalUtil interval = new IntervalUtil(0.2f, 0.4f);
	protected TimeoutTracker<String> pointTimeout = new TimeoutTracker<String>();
	protected TimeoutTracker<String> creatorTimeout = new TimeoutTracker<String>();
	
	public EncounterManager() {
		Global.getSector().getListenerManager().addListener(this);
	}
	
	public void advance(float amount) {
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		if (pf == null) return;
		float days = Global.getSector().getClock().convertToDays(amount);
		//days *= 1000f;
		pointTimeout.advance(days);
		creatorTimeout.advance(days);
		
		if (interval.getRandom() == null) {
			interval.setRandom(random);
		}
		
		interval.advance(days);
		if (interval.intervalElapsed()) {
			
			LocationAPI where = Global.getSector().getCurrentLocation();
			if (where == null) return;
			
			float minRange = Global.getSettings().getMaxSensorRange(where);
			float maxRange = minRange + MAX_EP_DIST_ADD;
			
			List<EncounterPoint> points = ListenerUtil.generateEncounterPoints(where);
			EncounterPoint closest = null;
			float minDist = Float.MAX_VALUE;
			
//			System.out.println("EncounterManager potential points");
//			System.out.println("---------------------------------");
//			for (EncounterPoint p : points) {
//				System.out.println(p);
//			}
//			System.out.println("---------------------------------");
			
			for (EncounterPoint p : points) {
				if (pointTimeout.contains(p.id)) {
					continue;
				}
				float dist = Misc.getDistance(pf.getLocation(), p.loc);
				if (dist < minRange || dist > maxRange) continue;
				if (dist < minDist) {
					closest = p;
					minDist = dist;
				}
			}
			
			//System.out.println("Point picked: " + closest);
			
			if (closest != null) {
				WeightedRandomPicker<EPEncounterCreator> picker = new WeightedRandomPicker<EPEncounterCreator>(random);
				for (EPEncounterCreator c : CREATORS) {
					if (creatorTimeout.contains(c.getId())) continue;
					float f = c.getFrequencyForPoint(this, closest);
					if (f > 0f) {
						picker.add(c, f);
					}
				}
				
				EPEncounterCreator c = picker.pick();
				if (c != null) {
					c.createEncounter(this, closest);
					
					float ptTO = c.getPointTimeoutMin() + (c.getPointTimeoutMax() - c.getPointTimeoutMin()) * random.nextFloat();
					float cTO = c.getCreatorTimeoutMin() + (c.getCreatorTimeoutMax() - c.getCreatorTimeoutMin()) * random.nextFloat();
					if (ptTO > 0) {
						pointTimeout.set(closest.id, ptTO);
					}
					if (cTO > 0) {
						creatorTimeout.set(c.getId(), cTO);
					}
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

	public Random getRandom() {
		return random;
	}

	public TimeoutTracker<String> getPointTimeout() {
		return pointTimeout;
	}

	public TimeoutTracker<String> getCreatorTimeout() {
		return creatorTimeout;
	}
	
	
	public List<EncounterPoint> generateEncounterPoints(LocationAPI where) {
		if (!where.isHyperspace()) return null;
		
		List<EncounterPoint> result = new ArrayList<EncounterPoint>();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		float velAngle = Misc.getAngleInDegrees(pf.getVelocity());
		if (pf.getVelocity().length() < 10f) {
			velAngle = pf.getFacing();
		}
		for (CampaignTerrainAPI ter : where.getTerrainCopy()) {
			if (ter.getPlugin() instanceof SlipstreamTerrainPlugin2) {
				SlipstreamTerrainPlugin2 plugin = (SlipstreamTerrainPlugin2) ter.getPlugin();
				List<Vector2f> points = plugin.getEncounterPoints();
				if (points != null) {
					for (Vector2f p : points) {
						float dist = Misc.getDistance(pf.getLocation(), p);
						if (dist > 10000f) continue;
						if (!Misc.isInsideSlipstream(p, 500f)) {
							float diff = Misc.getAngleDiff(velAngle, Misc.getAngleInDegrees(pf.getLocation(), p));
							if (diff < 90f) {
								String id = ter.getId() + "_" + (int)Math.round(p.x) + "_" + (int)Math.round(p.y); 
								EncounterPoint ep = new EncounterPoint(id, where, p, EP_TYPE_SLIPSTREAM);
								result.add(ep);
							}
						}
					}
				}
			}
		}
		return result;
	}
	
}







