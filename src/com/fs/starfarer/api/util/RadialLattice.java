package com.fs.starfarer.api.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class RadialLattice {
	
	public static class RadialLatticeBucket {
		public Set<StarSystemAPI> systems = new LinkedHashSet<StarSystemAPI>();
		public Set<MarketAPI> markets = new LinkedHashSet<MarketAPI>();
		public float angle;
		
		public RadialLatticeBucket(float angle) {
			this.angle = angle;
		}
		
		
	}
	
	protected long updateTimestamp;
	protected List<RadialLatticeBucket> buckets = new ArrayList<RadialLatticeBucket>();
	
	protected int numBuckets = 8;
	protected float arc = (360 / numBuckets * 2) + 20;
	
	public void update() {
		
		buckets.clear();
		float anglePer = 360f / (numBuckets * 2);
		
		for (int i = 0; i < numBuckets; i++) {
			RadialLatticeBucket bucket = new RadialLatticeBucket(i * anglePer);
			buckets.add(bucket);
		}
		
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		Vector2f origin = playerFleet.getLocation();
		
		
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			float angle = Misc.getAngleInDegrees(origin, system.getLocation());
		}
		
	}
	
	
	public RadialLatticeBucket getBucket(float angle) {
		angle = Misc.normalizeAngle(angle);
		
		float anglePer = 360f / (numBuckets * 2);
		
		int index = (int) (angle / anglePer);
		if (index >= numBuckets) index -= numBuckets;
		
		return buckets.get(index);
	}
	
	
	
		
}











