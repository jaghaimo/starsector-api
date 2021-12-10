package com.fs.starfarer.api.impl.campaign.velfield;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.MutatingVertexUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SlipstreamEntityPlugin2 extends BaseCustomEntityPlugin {
	
	public static class SlipstreamSegment {
		public Vector2f locB = new Vector2f();
		public Vector2f loc = new Vector2f();
		public Vector2f dir = new Vector2f();
		public float width;
		
		transient public float wobbledWidth;
		transient public int index = 0;
		transient public Vector2f normal = new Vector2f();
		transient public float tx = 0f;
		transient public float txe1 = 0f;
		transient public float txe2 = 0f;
		transient public float totalLength;
		transient public float lengthToPrev;
		transient public float lengthToNext;
		
		public MutatingVertexUtil wobble1;
		public MutatingVertexUtil wobble2;
		public FaderUtil fader = new FaderUtil(0f, 1f, 1f);
	}
	
	public static class SlipstreamParticle {
		float speed;
		float dist;
		float yPos;
		Color color;
		float remaining;
		float elapsed;
	}
	
	public static class SlipstreamParams2 {
		public String spriteKey1 = "slipstream";
		public String edgeKey = "slipstream_edge3";
		public Color spriteColor = new Color(0.3f, 0.5f, 1f, 1f);
		public Color windGlowColor = new Color(0.3f, 0.5f, 1f, 1f);
		public Color edgeColor = Color.white;
		public float edgeWidth = 256;
		//public float width;
		public float areaPerParticle = 2875;
		public int maxParticles = 2000;
		//public int numParticles;
		public float minSpeed;
		public float maxSpeed;
		public int burnLevel = 30;
		public Color minColor;
		public Color maxColor;
		public float particleFadeInTime = 1f;
		public float minDur = 0f;
		public float maxDur = 4f;
		public float lineLengthFractionOfSpeed = 0.5f;
		public boolean slowDownInWiderSections = false;
		public float widthForMaxSpeed = 1f;
		public float widthForMaxSpeedMinMult = 0.5f;
		public float widthForMaxSpeedMaxMult = 1.5f;
	}
	
	public static int MAX_PARTICLES_ADD_PER_FRAME = 250;
	
	public static float RAD_PER_DEG = 0.01745329251f;
	public static Vector2f rotateAroundOrigin(Vector2f v, float cos, float sin) {
		Vector2f r = new Vector2f();
		r.x = v.x * cos - v.y * sin;
		r.y = v.x * sin + v.y * cos;
		return r;
	}
	

	
	protected SlipstreamParams2 params = new SlipstreamParams2();
	
	protected List<SlipstreamSegment> segments = new ArrayList<SlipstreamEntityPlugin2.SlipstreamSegment>();
	protected float totalLength = 0f;
	
	protected transient List<SlipstreamParticle> particles = new ArrayList<SlipstreamParticle>();
	protected transient int [] lengthToIndexMap;
	protected transient int lengthDivisor;
	
	protected boolean needsRecompute = true;
	protected List<BoundingBox> bounds = new ArrayList<BoundingBox>();
	protected int segmentsPerBox;
	
	public SlipstreamEntityPlugin2() {
	}
	
	public void setNeedsRecompute() {
		this.needsRecompute = true;
	}

	public void updateLengthToIndexMap() {
		float minSegmentLength = Float.MAX_VALUE;
		for (SlipstreamSegment curr : segments) {
			if (curr.lengthToNext > 0 && minSegmentLength > curr.lengthToNext) {
				minSegmentLength = curr.lengthToNext;
			}
		}
		if (minSegmentLength < 50f) minSegmentLength = 50f;
		
		lengthDivisor = (int) (minSegmentLength - 1f); 
		int numIndices = (int) (totalLength / lengthDivisor);
		lengthToIndexMap = new int [numIndices];
		
		int lengthSoFar = 0;
		for (int i = 0; i < segments.size(); i++) {
			SlipstreamSegment curr = segments.get(i);
			while (lengthSoFar < curr.totalLength + curr.lengthToNext) {
				int lengthIndex = lengthSoFar / lengthDivisor;
				if (lengthIndex < lengthToIndexMap.length) {
					lengthToIndexMap[lengthIndex] = i;
				}
				lengthSoFar += lengthDivisor;
			}
		}
	}
	
	public SlipstreamSegment getSegmentForDist(float distAlongStream) {
		if (lengthToIndexMap == null) return null;
		int mapIndex = (int) (distAlongStream / lengthDivisor);
		if (mapIndex < 0 || mapIndex >= lengthToIndexMap.length) return null;
		//System.out.println("Index: " + mapIndex + ", dist: " + distAlongStream);
		int segIndex = lengthToIndexMap[mapIndex];
		SlipstreamSegment segment = segments.get(segIndex);
		while (distAlongStream < segment.totalLength) {
			segIndex--;
			if (segIndex < 0) return null;
			segment = segments.get(segIndex);
		}
		while (distAlongStream > segment.totalLength + segment.lengthToNext) {
			segIndex++;
			if (segIndex >= segments.size()) return null;
			segment = segments.get(segIndex);
		}
		return segment;
	}
	
	public void addSegment(Vector2f loc, float width) {
		SlipstreamSegment s = new SlipstreamSegment();
		s.loc.set(loc);
		s.width = width;
		
		float minRadius = 0f;
		float maxRadius = s.width * 0.05f;
		float rate = maxRadius * 0.5f;
		float angleRate = 50f;
		s.wobble1 = new MutatingVertexUtil(minRadius, maxRadius, rate, angleRate); 
		s.wobble2 = new MutatingVertexUtil(minRadius, maxRadius, rate, angleRate); 
		
		s.fader.fadeIn();
		
		segments.add(s);
		setNeedsRecompute();
	}
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		this.params = (SlipstreamParams2) pluginParams;
		fader.fadeIn();
		readResolve();
	}
	
	public float getRenderRange() {
		return totalLength * 0.6f + 1000f;
		//return totalLength + 1000f;
	}

	Object readResolve() {
		if (particles == null) {
			particles = new ArrayList<SlipstreamParticle>();
		}
		return this;
	}
	
	public void advance(float amount) {
		if (!entity.isInCurrentLocation()) return;
		
		applyEffectToFleets(amount);
		
		fader.advance(amount);

		
//		entity.getLocation().x += Misc.getSpeedForBurnLevel(params.burnLevel) * amount;
//		entity.setFacing(0f);
//		entity.getLocation().set(Global.getSector().getPlayerFleet().getLocation().x + 500, 
//				Global.getSector().getPlayerFleet().getLocation().y + 1000f);
//		entity.getLocation().set(Global.getSector().getPlayerFleet().getLocation());
		
		params.minColor = new Color(0.5f, 0.3f, 0.75f, 0.85f);
		params.maxColor = new Color(0.5f, 0.6f, 1f, 1f);
		params.spriteColor = new Color(0.3f, 0.5f, 1f, 1f);
		params.minDur = 1f;
		params.maxDur = 4f;
		params.minSpeed = 700f;
		params.maxSpeed = 1500f;
		//params.lineLengthFractionOfSpeed = 0.5f;
		//params.lineLengthFractionOfSpeed = 1f;
		//params.lineLengthFractionOfSpeed = 0.15f;
		params.burnLevel = 30;
		//params.burnLevel = 1000;
		//params.particleFadeInTime = 0.01f;
		params.minSpeed = Misc.getSpeedForBurnLevel(params.burnLevel - 5);
		params.maxSpeed = Misc.getSpeedForBurnLevel(params.burnLevel + 5);
		params.lineLengthFractionOfSpeed = 0.25f * Math.max(0.25f, Math.min(1f, 30f / (float) params.burnLevel));
		//params.burnLevel = 200;
		//params.numParticles = 2000;
		//params.numParticles = 1;
		params.minColor = new Color(0.5f, 0.3f, 0.75f, 0.1f);
		params.maxColor = new Color(0.5f, 0.6f, 1f, 0.5f);
		
		
		float width = 512;
		params.widthForMaxSpeed = width;
		params.slowDownInWiderSections = true;
		params.widthForMaxSpeedMinMult = 0.5f;
		
		params.edgeWidth = 256f;
		params.spriteColor = new Color(0.3f, 0.5f, 1f, 0.5f);
		params.edgeColor = new Color(0.3f, 0.5f, 1f, 0.75f);
		params.edgeColor = Color.white;
		
		//params.edgeWidth = 128f;
		//params.minDur = 2f;
		//params.maxDur = 2f;
		params.minDur = 2f;
		params.maxDur = 6f;
		
		params.areaPerParticle = 10000;
		//params.areaPerParticle = 10f;
		//params.maxParticles = 100000;
		//MAX_PARTICLES_ADD_PER_FRAME = 2000;
		
		
//		params.minSpeed = Misc.getSpeedForBurnLevel(16f);
//		params.maxSpeed = Misc.getSpeedForBurnLevel(16f);
//		params.numParticles = 0;
		
		//segments.clear();
//		if (segments.isEmpty()) {
//			float currX = entity.getLocation().x + 200f;
//			addSegment(new Vector2f(currX, entity.getLocation().y), 1600f);
//			currX += 200f;
//			addSegment(new Vector2f(currX, entity.getLocation().y), 1200f);
//			currX += 300f;
//			addSegment(new Vector2f(currX, entity.getLocation().y), 800f);
//			currX += 400f;
//			addSegment(new Vector2f(currX, entity.getLocation().y), 700f);
//			currX += 500f;
//			addSegment(new Vector2f(currX, entity.getLocation().y), 600f);
//			currX += 600f;
//			addSegment(new Vector2f(currX, entity.getLocation().y), 512f);
//		}
		if (segments.isEmpty()) {
			float spacing = 200f;
//			for (int i = 0; i < 100; i++) {
//				addSegment(new Vector2f(entity.getLocation().x + i * spacing, entity.getLocation().y), params.width);
//			}
//			float x = segments.get(segments.size() - 1).loc.x;
//			float y = segments.get(segments.size() - 1).loc.y;
//			addSegment(new Vector2f(x + 200f, y - 100f), params.width);
//			addSegment(new Vector2f(x + 400f, y - 200f), params.width);
//			addSegment(new Vector2f(x + 600f, y - 300f), params.width);
//			addSegment(new Vector2f(x + 800f, y - 400f), params.width);
//			addSegment(new Vector2f(x + 1000f, y - 500f), params.width);
//			addSegment(new Vector2f(x + 1000f, y - 600f), params.width);
//			addSegment(new Vector2f(x + 800f, y - 700f), params.width);
//			addSegment(new Vector2f(x + 600f, y - 800f), params.width);
//			addSegment(new Vector2f(x + 400f, y - 900f), params.width);
//			addSegment(new Vector2f(x + 200f, y - 1000f), params.width);
//			for (int i = 100; i >= 0; i--) {
//				addSegment(new Vector2f(entity.getLocation().x + i * spacing, entity.getLocation().y - 1100), params.width);
//			}
			
			int iter = 1000;
			for (int i = 0; i < iter; i++) {
				float yOff = (float) Math.sin(i * 0.05f);
				addSegment(new Vector2f(entity.getLocation().x + i * spacing,
				//addSegment(new Vector2f(entity.getLocation().x + i * (spacing + (50 - i) * 5),
						entity.getLocation().y + yOff * 2000f), 
						//width);
						//width * (0.7f + (float) Math.random() * 0.7f));
						width + i * 10f);
			}
//			float spacing = 1000f;
//			for (int i = 0; i < 500; i++) {
//				float yOff = 0f;
//				addSegment(new Vector2f(entity.getLocation().x + i * spacing,
//				//addSegment(new Vector2f(entity.getLocation().x + i * (spacing + (50 - i) * 5),
//						entity.getLocation().y + yOff * 2000f), 
//						//width);
//						//width * (0.7f + (float) Math.random() * 0.7f));
//						width + 500f + i * 2f);
//			}
		}
		
		recomputeIfNeeded();
		advanceNearbySegments(amount);
	
		addParticles();
		advanceParticles(amount);
	}
	
	
	public void recomputeIfNeeded() {
		if (!needsRecompute) return;
		recompute();
	}
	
	public void recompute() {
		needsRecompute = false;
		
		// compute average location, set segment indices
		Vector2f avgLoc = new Vector2f();
		for (int i = 0; i < segments.size(); i++) {
			SlipstreamSegment curr = segments.get(i);
			curr.index = i;
			Vector2f.add(avgLoc, curr.loc, avgLoc);
		}

		if (segments.size() > 0) {
			avgLoc.scale(1f / segments.size());
			entity.setLocation(avgLoc.x, avgLoc.y);
		}

		
		SpriteAPI sprite = Global.getSettings().getSprite("misc", params.spriteKey1);
		SpriteAPI edge = Global.getSettings().getSprite("misc", params.edgeKey);
		
		// compute texture coordinates etc
		float tx = 0f;
		float txe1 = 0f;
		float txe2 = 0f;
		float totalLength = 0f;
		for (int i = 0; i < segments.size(); i++) {
			SlipstreamSegment prev = null;
			if (i > 0) prev = segments.get(i - 1);
			SlipstreamSegment curr = segments.get(i);
			SlipstreamSegment next = null;
			SlipstreamSegment next2 = null;
			SlipstreamSegment next3 = null;
			if (i < segments.size() - 1) {
				next = segments.get(i + 1);
			}
			if (i < segments.size() - 2) {
				next2 = segments.get(i + 2);
			}
			if (i < segments.size() - 3) {
				next3 = segments.get(i + 3);
			}

			if (next == null) {
				if (prev != null) {
					curr.dir.set(prev.dir);
				}
			} else {
				Vector2f dir = Vector2f.sub(next.loc, curr.loc, new Vector2f());
				dir = Misc.normalise(dir);
				curr.dir = dir;
			}

			Vector2f dir = curr.dir;
			Vector2f normal = new Vector2f(-dir.y, dir.x);
			curr.normal.set(normal);

			float length = 0f;
			float texLength = 0f;
			float e1TexLength = 0f;
			float e2TexLength = 0f;
			if (prev != null) {
				Vector2f dir2 = Vector2f.sub(curr.loc, prev.loc, new Vector2f());
				length = dir2.length();
				texLength = length / sprite.getWidth();
				texLength = Math.min(texLength, sprite.getHeight() / curr.width);

				Vector2f edgeCurr = new Vector2f(curr.loc);
				edgeCurr.x += curr.normal.x * curr.width * 0.5f;
				edgeCurr.y += curr.normal.y * curr.width * 0.5f;

				Vector2f edgePrev = new Vector2f(prev.loc);
				edgePrev.x += prev.normal.x * prev.width * 0.5f;
				edgePrev.y += prev.normal.y * prev.width * 0.5f;

				float length2 = Vector2f.sub(edgeCurr, edgePrev, new Vector2f()).length();
				e1TexLength = length2 / edge.getWidth() * edge.getHeight() / params.edgeWidth;


				edgeCurr = new Vector2f(curr.loc);
				edgeCurr.x -= curr.normal.x * curr.width * 0.5f;
				edgeCurr.y -= curr.normal.y * curr.width * 0.5f;

				edgePrev = new Vector2f(prev.loc);
				edgePrev.x -= prev.normal.x * prev.width * 0.5f;
				edgePrev.y -= prev.normal.y * prev.width * 0.5f;

				length2 = Vector2f.sub(edgeCurr, edgePrev, new Vector2f()).length();
				e2TexLength = length2 / edge.getWidth() * edge.getHeight() / params.edgeWidth;
			}

			tx += texLength;
			txe1 += e1TexLength;
			txe2 += e2TexLength;
			curr.tx = tx;
			curr.txe1 = txe1;
			curr.txe2 = txe2;
			curr.lengthToPrev = length;

			totalLength += length;
			curr.totalLength = totalLength;
			//curr.lengthToNext = Misc.getDistance(curr.loc, next.loc);
			if (prev != null) {
				prev.lengthToNext = length;
			}

			if (next != null && next2 != null && next3 != null) {
				Vector2f p0 = curr.loc;
				Vector2f p1 = next.loc;
				Vector2f p2 = next2.loc;
				Vector2f p3 = next3.loc;

				float p1ToP2 = Misc.getAngleInDegrees(p1, p2);
				float p2ToP3 = Misc.getAngleInDegrees(p2, p3);
				float diff = Misc.getAngleDiff(p1ToP2, p2ToP3);
				float adjustment = Math.min(diff, Math.max(diff * 0.25f, diff - 10f));
				adjustment = diff * 0.5f;
				//adjustment = diff * 0.25f;
				float angle = p1ToP2 + Misc.getClosestTurnDirection(p1ToP2, p2ToP3) * adjustment * 1f + 180f;
				//angle = Misc.getAngleInDegrees(p3, p2);
				float dist = Misc.getDistance(p2, p1);
				Vector2f p1Adjusted = Misc.getUnitVectorAtDegreeAngle(angle);
				p1Adjusted.scale(dist);
				Vector2f.add(p1Adjusted, p2, p1Adjusted);
				next.locB = p1Adjusted;
			} else if (next != null) {
				next.locB = next.loc;
			}
			if (prev == null) {
				curr.locB = new Vector2f(curr.loc);
			}
		}
		this.totalLength = totalLength;
		
		updateLengthToIndexMap();
		updateBoundingBoxes();
	}
	
	protected void updateBoundingBoxes() {
		segmentsPerBox = (int) Math.sqrt(segments.size()) + 1;
		if (segmentsPerBox < 20) segmentsPerBox = 20;
		
		bounds.clear();
		for (int i = 0; i < segments.size(); i+= segmentsPerBox) {
			List<SlipstreamSegment> section = new ArrayList<SlipstreamSegment>();
			for (int j = i; j < i + segmentsPerBox && j < segments.size(); j++) {
				section.add(segments.get(j));
			}
			if (i + segmentsPerBox < segments.size()) {
				section.add(segments.get(i + segmentsPerBox));
			}
			//BoundingBox box = BoundingBox.create(section);
			//bounds.add(box);
		}
	}
	
	protected void advanceNearbySegments(float amount) {
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		if (pf == null || !entity.isInCurrentLocation()) {
			return;
		}
		
		if (segments.size() > 0) {
			segments.get(0).fader.forceOut();
			segments.get(segments.size() - 1).fader.fadeOut();
		}
		
		ViewportAPI viewport = Global.getSector().getViewport();
		float viewRadius = new Vector2f(viewport.getVisibleWidth() * 0.5f, viewport.getVisibleHeight() * 0.5f).length();
		viewRadius = Math.max(6000f, viewRadius);
		viewRadius += 1000f;
		List<SlipstreamSegment> near = getSegmentsNear(viewport.getCenter(), viewRadius);
		
		// advance fader and wobble, compute wobbledWidth
		for (int i = 0; i < near.size(); i++) {
			SlipstreamSegment curr = near.get(i);
			
			curr.fader.advance(amount);
			
			float r1 = 0.5f + (float) Math.random() * 1f;
			float r2 = 0.5f + (float) Math.random() * 1f;
			curr.wobble1.advance(amount * r1);
			curr.wobble2.advance(amount * r2);
//			curr.wobble1.vector.set(0, 0);
//			curr.wobble2.vector.set(0, 0);
			
			Vector2f p1 = new Vector2f(curr.loc);
			Vector2f p2 = new Vector2f(curr.loc);
			p1.x += curr.normal.x * curr.width * 0.5f;
			p1.y += curr.normal.y * curr.width * 0.5f;
			p2.x -= curr.normal.x * curr.width * 0.5f;
			p2.y -= curr.normal.y * curr.width * 0.5f;
			
			p1.x += curr.wobble1.vector.x;
			p1.y += curr.wobble1.vector.y;
			p2.x += curr.wobble2.vector.x;
			p2.y += curr.wobble2.vector.y;
			
			//curr.wobbledWidth = Misc.getDistance(p1, p2);
			float d = Misc.getDistance(p1, p2);
			curr.wobbledWidth = d - params.edgeWidth * 2f * 0.5f;
			if (curr.wobbledWidth < d * 0.5f) curr.wobbledWidth = d * 0.5f;
			//curr.wobbledWidth = curr.width;
			
			if (curr.index > 0) {
				SlipstreamSegment prev = segments.get(curr.index - 1);
				Vector2f prev1 = new Vector2f(prev.loc);
				Vector2f prev2 = new Vector2f(prev.loc);
				prev1.x += curr.normal.x * curr.width * 0.5f;
				prev1.y += curr.normal.y * curr.width * 0.5f;
				prev2.x -= curr.normal.x * curr.width * 0.5f;
				prev2.y -= curr.normal.y * curr.width * 0.5f;
				
				float maxWobbleRadius = Math.min(prev.width, curr.width) * 0.05f;
				float maxWobble1 = Misc.getDistance(p1, prev1) * 0.33f;
				float maxWobble2 = Misc.getDistance(p2, prev2) * 0.33f;
				maxWobble1 = Math.min(maxWobbleRadius, maxWobble1);
				maxWobble2 = Math.min(maxWobbleRadius, maxWobble2);
				prev.wobble1.radius.setMax(maxWobble1);
				prev.wobble2.radius.setMax(maxWobble2);
				curr.wobble1.radius.setMax(maxWobble1);
				curr.wobble2.radius.setMax(maxWobble2);
			}
		}
	}
	
	
	
	public void addParticles() {
		if (Global.getSector().getPlayerFleet() == null) {
			particles.clear();
			return;
		}
		
		boolean useNewSpawnMethod = true;
		//useNewSpawnMethod = false;
		
		if (useNewSpawnMethod) {
			boolean inCurrentLocation = entity.isInCurrentLocation();
			boolean inHyperspace = entity.isInHyperspace();
			boolean spawnForAllSegments = false;
			ViewportAPI viewport = Global.getSector().getViewport();
			Vector2f locFrom = viewport.getCenter();
			float viewRadius = new Vector2f(viewport.getVisibleWidth() * 0.5f, viewport.getVisibleHeight() * 0.5f).length();
			viewRadius += 2000f;
			viewRadius = Math.max(viewRadius, 10000f);
			if (!inCurrentLocation) {
				if (inHyperspace) {
					viewRadius = 5000f;
					locFrom = Global.getSector().getPlayerFleet().getLocationInHyperspace();
				} else {
					float dist = Misc.getDistanceToPlayerLY(entity);
					spawnForAllSegments = dist < 2f;
				}
			}
			Set<SlipstreamSegment> veryNearSet = new LinkedHashSet<SlipstreamEntityPlugin2.SlipstreamSegment>();
			if (inCurrentLocation) {
				float veryNearRadius = new Vector2f(viewport.getVisibleWidth() * 0.5f, viewport.getVisibleHeight() * 0.5f).length();
				viewRadius += 500f;
				veryNearSet = new LinkedHashSet<SlipstreamEntityPlugin2.SlipstreamSegment>(
							getSegmentsNear(viewport.getCenter(), veryNearRadius));
			}
			
	//		viewRadius *= 0.5f;
	//		viewRadius = 500f;
			
			List<SlipstreamSegment> near;
			if (spawnForAllSegments) {
				 near = new ArrayList<SlipstreamSegment>(segments);
			} else {
				near = getSegmentsNear(locFrom, viewRadius);
			}
			Set<SlipstreamSegment> nearSet = new LinkedHashSet<SlipstreamSegment>(near);
			
			Map<SlipstreamSegment, List<SlipstreamParticle>> particleMap = new LinkedHashMap<SlipstreamEntityPlugin2.SlipstreamSegment, List<SlipstreamParticle>>();
			//for (SlipstreamParticle p : particles) {
			Iterator<SlipstreamParticle> iter = particles.iterator();
			while (iter.hasNext()) {
				SlipstreamParticle p = iter.next();
				SlipstreamSegment seg = getSegmentForDist(p.dist);
				if (seg != null) {
					if (!nearSet.contains(seg)) {
						iter.remove();
						continue;
					}
					
					List<SlipstreamParticle> list = particleMap.get(seg);
					if (list == null) {
						list = new ArrayList<SlipstreamEntityPlugin2.SlipstreamParticle>();
						particleMap.put(seg, list);
					}
					list.add(p);
				}
			}
			
			
			float totalArea = 0f;
			int nearParticles = 0;
			WeightedRandomPicker<SlipstreamSegment> segmentPicker = new WeightedRandomPicker<SlipstreamEntityPlugin2.SlipstreamSegment>();
			
			// figure out how many particles to add total, and also which segments to add them
			// to to achieve a relatively even distribution
			for (int i = 0; i < near.size(); i++) {
				SlipstreamSegment curr = near.get(i);
				if (curr.lengthToNext <= 0) continue; // last segment, can't have particles in it since the stream is over
	
				float area = curr.lengthToNext * curr.width;
				float desiredParticles =  area / params.areaPerParticle;
				if (desiredParticles < 1) desiredParticles = 1;
				
				float particlesInSegment = 0; 
				List<SlipstreamParticle> list = particleMap.get(curr);
				if (list != null) {
					particlesInSegment = list.size();
				}
				
				float mult = 1f;
				// spawn more particles in visible/nearly visible areas
				// better to have less visible particles when the player zooms out while paused
				// than to have less visible particles when zoomed in
				if (veryNearSet.contains(curr)) mult = 10f;
				
				float w = desiredParticles - particlesInSegment;
				w *= mult;
				if (w < 5f) w = 5f;
				segmentPicker.add(curr, w);
				//segmentPicker.add(curr, 1f);
				
				totalArea += area;
				nearParticles += particlesInSegment;
			}
			
			
			int numParticlesBasedOnArea = (int) (totalArea / params.areaPerParticle);
			int actualDesired = numParticlesBasedOnArea;
			if (numParticlesBasedOnArea < 10) numParticlesBasedOnArea = 10;
			if (numParticlesBasedOnArea > params.maxParticles) numParticlesBasedOnArea = params.maxParticles;
			//System.out.println("Area: " + totalArea/params.numParticles);
			//numParticlesBasedOnArea = 20000;
			
			
			int particlesToAdd = numParticlesBasedOnArea - nearParticles;
			if (particlesToAdd > MAX_PARTICLES_ADD_PER_FRAME) {
				particlesToAdd = MAX_PARTICLES_ADD_PER_FRAME;
			}
			particlesToAdd = Math.min(particlesToAdd, params.maxParticles - particles.size());
			
			int added = 0;
			while (added < particlesToAdd) {
				added++;
				SlipstreamSegment seg = segmentPicker.pick();
				if (seg == null) continue;
				
				SlipstreamParticle p = new SlipstreamParticle();
				float fLength = (float) Math.random() * 1f;
				float fWidth = (float) Math.random() * 2f - 1f;
				
				float speed = params.minSpeed + (params.maxSpeed - params.minSpeed) * (float) Math.random();
				float dur = params.minDur + (params.maxDur - params.minDur) * (float) Math.random(); 
				
				p.yPos = fWidth;
				//p.dist = totalLength * fLength;
				p.dist = seg.totalLength + seg.lengthToNext * fLength;
				p.speed = speed;
				
				float intensity = getIntensity(p.yPos);
				float wMult = getWidthBasedSpeedMult(p.dist);
	//			if (wMult <= 0) {
	//				getWidthBasedSpeedMult(p.dist);
	//			}
				float speedMult = (0.65f + 0.35f * intensity) * wMult;
				p.speed *= speedMult;
				
				p.remaining = dur;
				p.color = getRandomColor();
				
				particles.add(p);
			}
			
			//System.out.println("Particles: " + particles.size() + " desired based on area: " + actualDesired);
			
		} else {
			float totalArea = 0f;
			for (int i = 0; i < segments.size(); i++) {
				SlipstreamSegment curr = segments.get(i);
				totalArea += curr.lengthToPrev * curr.width;
			}
			
			int numParticlesBasedOnArea = (int) (totalArea / params.areaPerParticle);
			if (numParticlesBasedOnArea < 10) numParticlesBasedOnArea = 10;
			if (numParticlesBasedOnArea > params.maxParticles) numParticlesBasedOnArea = params.maxParticles;
			//System.out.println("Area: " + totalArea/params.numParticles);
			//numParticlesBasedOnArea = 20000;
		
		
			int added = 0;
			//while (particles.size() < params.numParticles && added < MAX_PARTICLES_ADD_PER_FRAME) {
			while (particles.size() < numParticlesBasedOnArea && added < MAX_PARTICLES_ADD_PER_FRAME) {
				added++;
				
				SlipstreamParticle p = new SlipstreamParticle();
				float fLength = (float) Math.random() * 1f;
				float fWidth = (float) Math.random() * 2f - 1f;
				
				float speed = params.minSpeed + (params.maxSpeed - params.minSpeed) * (float) Math.random();
				float dur = params.minDur + (params.maxDur - params.minDur) * (float) Math.random(); 
				
				p.yPos = fWidth;
				p.dist = totalLength * fLength;
				p.speed = speed;
				
				float intensity = getIntensity(p.yPos);
				float wMult = getWidthBasedSpeedMult(p.dist);
	//			if (wMult <= 0) {
	//				getWidthBasedSpeedMult(p.dist);
	//			}
				float speedMult = (0.65f + 0.35f * intensity) * wMult;
				p.speed *= speedMult;
				
				p.remaining = dur;
				p.color = getRandomColor();
				
				particles.add(p);
			}
		}
	}
	
	public void advanceParticles(float amount) {
		Iterator<SlipstreamParticle> iter = particles.iterator();
		while (iter.hasNext()) {
			SlipstreamParticle p = iter.next();
			p.remaining -= amount;
			p.elapsed += amount;
			if (p.remaining <= 0) {
				iter.remove();
				continue;
			}

			p.dist += p.speed * amount;
		}
	}
	
	public float getWidthBasedSpeedMult(float distAlong) {
		float mult = 1f;
		if (params.slowDownInWiderSections) {
			SlipstreamSegment curr = getSegmentForDist(distAlong);
			if (curr != null) {
				float width = curr.width;
				if (segments.size() > curr.index + 1) {
					SlipstreamSegment next = segments.get(curr.index + 1);
					float f = (distAlong - curr.totalLength) / curr.lengthToNext;
					if (f < 0) f = 0;
					if (f > 1) f = 1;
					width = Misc.interpolate(width, next.width, f);
					mult = Math.min(params.widthForMaxSpeedMaxMult,
							params.widthForMaxSpeedMinMult + (1f - params.widthForMaxSpeedMinMult) * params.widthForMaxSpeed / width);
				}
			}
		}
		return mult;
	}
	public float getIntensity(float yOff) {
		yOff = Math.abs(yOff);
		float intensity = 1f;
		if (yOff > 0.5f) {
			intensity = 1f - 1f * (yOff - 0.5f) / 0.5f;
		}
		return intensity;
	}
	
	public float getFaderBrightness(float distAlong) {
		SlipstreamSegment curr = getSegmentForDist(distAlong);
		if (curr != null) {
			if (segments.size() > curr.index + 1) {
				SlipstreamSegment next = segments.get(curr.index + 1);
				float f = (distAlong - curr.totalLength) / curr.lengthToNext;
				if (f < 0) f = 0;
				if (f > 1) f = 1;
				return Misc.interpolate(curr.fader.getBrightness(), next.fader.getBrightness(), f);
			} else {
				return 0f;
			}
		}
		return 0f;
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		recomputeIfNeeded();
		if (lengthToIndexMap == null) return;
		
		
		if (true && false) {
			//BoundingBox box = BoundingBox.create(segments);
			float mx = Mouse.getX();
			float my = Mouse.getY();
			float wmx = Global.getSector().getViewport().convertScreenXToWorldX(mx);
			float wmy = Global.getSector().getViewport().convertScreenYToWorldY(my);
			boolean inside = false;
			for (BoundingBox box : bounds) {
				box.renderDebug(1f);
				inside |= box.pointNeedsDetailedCheck(new Vector2f(wmx, wmy));
			}
			
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
			GL11.glPointSize(20f);
			GL11.glEnable(GL11.GL_POINT_SMOOTH);
			if (inside) {
				Misc.setColor(Color.green);
			} else {
				Misc.setColor(Color.gray);
			}
			
			GL11.glBegin(GL11.GL_POINTS);
			GL11.glVertex2f(wmx, wmy);
			GL11.glEnd();
			//return;
		}
		
		
		
		
		float viewRadius = new Vector2f(viewport.getVisibleWidth() * 0.5f, viewport.getVisibleHeight() * 0.5f).length();
		viewRadius += 500f;
		
//		viewRadius *= 0.5f;
//		viewRadius = 500f;
		
		List<SlipstreamSegment> near = getSegmentsNear(viewport.getCenter(), viewRadius);
		Set<SlipstreamSegment> nearSet = new LinkedHashSet<SlipstreamSegment>(near);
		
		List<List<SlipstreamSegment>> subsections = new ArrayList<List<SlipstreamSegment>>();
		int prevIndex = -10;
		List<SlipstreamSegment> subsection = new ArrayList<SlipstreamSegment>();
		for (SlipstreamSegment seg : near) {
			if (prevIndex != seg.index -1) {
				if (subsection != null && !subsection.isEmpty()) {
					subsections.add(subsection);
				}
				subsection = new ArrayList<SlipstreamSegment>();
			}
			subsection.add(seg);
			prevIndex = seg.index;
		}
		if (subsection != null && !subsection.isEmpty()) {
			subsections.add(subsection);
		}
		
		SpriteAPI sprite = Global.getSettings().getSprite("misc", params.spriteKey1);
		//sprite.setAdditiveBlend();
		sprite.setNormalBlend();
		sprite.setColor(params.spriteColor);
		
		SpriteAPI edge = Global.getSettings().getSprite("misc", params.edgeKey);
		edge.setNormalBlend();
		edge.setColor(params.edgeColor);
		
		//sprite.setColor(Misc.setAlpha(params.spriteColor1, 255));
		//sprite.setColor(Color.blue);
		for (List<SlipstreamSegment> subsection2 : subsections) {
			renderSegments(sprite, edge, viewport.getAlphaMult(), subsection2);
		}
		
		//sprite.setColor(Color.red);
		//renderLayer(sprite, texProgress2, viewport.getAlphaMult());
		//sprite.setColor(Color.green);
		//renderLayer(sprite, texProgress3, viewport.getAlphaMult());
		
//		int state = 0;
//		for (int i = 0; i < segments.size() - 4; i += 2) {
//			//GL11.glBegin(GL11.GL_POINTS);
//			SlipstreamSegment prev = null;
//			if (i > 0) {
//				prev = segments.get(i - 1);
//			}
//			SlipstreamSegment curr = segments.get(i);
//			SlipstreamSegment next = segments.get(i + 1);	
//			SlipstreamSegment next2 = segments.get(i + 2);
//			SlipstreamSegment next3 = segments.get(i + 3);
//			Vector2f p0 = curr.loc;
//			Vector2f p1 = next.loc;
//			Vector2f p2 = next2.loc;
//			Vector2f p3 = next3.loc;
//			
//			if (state == 0) {
//				state = 1;
//				float p1ToP2 = Misc.getAngleInDegrees(p1, p2);
//				float p2ToP3 = Misc.getAngleInDegrees(p2, p3);
//				float diff = Misc.getAngleDiff(p1ToP2, p2ToP3);
//				float angle = p1ToP2 + Misc.getClosestTurnDirection(p1ToP2, p2ToP3) * diff * 0.5f + 180f;
//				angle = Misc.getAngleInDegrees(p3, p2);
//				float dist = Misc.getDistance(p2, p1);
//				Vector2f p1Adjusted = Misc.getUnitVectorAtDegreeAngle(angle);
//				p1Adjusted.scale(dist);
//				Vector2f.add(p1Adjusted, p2, p1Adjusted);
//				curr.locB.set(p1Adjusted);
//			} else if (state == 1) {
//				curr.locB.set(curr.loc);
//			} else if (state == 2) {
//				
//			}
//		}
		
		
		
		
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		
		float zoom = Global.getSector().getViewport().getViewMult(); 

		//GL11.glLineWidth(2f);
		//GL11.glLineWidth(Math.max(1f, 2f/zoom));
		GL11.glLineWidth(Math.max(1f, Math.min(2f, 2f/zoom)));
		//GL11.glLineWidth(1.5f);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		
		Misc.setColor(new Color(1f, 1f, 1f, 0.5f));
		Misc.setColor(Color.white);
		//GL11.glLineWidth(1f);
		
//		for (SlipstreamSegment seg : segments) {
//			if (seg.totalLength <= 0f && segments.indexOf(seg) > 1) {
//				System.out.println("efwefwefwefe");
//			}
//		}
		
		// draw bezier lines for debug
		for (float offset = -1f; false && offset <= 1f; offset += 0.1f) {
		//for (float offset = 0f; offset <= 0f; offset += 0.1f) {
			GL11.glBegin(GL11.GL_LINE_STRIP);
			float incr = 10f;
			for (float len = 0; len < totalLength; len += incr) {
//				if (len > 10000f) {
//					System.out.println("ewfwefew");
//				}
				/*
				SlipstreamSegment curr = getSegmentForDist(len);
				if (curr == null) continue;
				int index = curr.index;
				if (index >= segments.size() - 2) continue;
				SlipstreamSegment next = segments.get(index + 1);
				SlipstreamSegment next2 = segments.get(index + 2);
				
				if (index % 2 != 0) {
					curr = segments.get(index - 1);
					next = segments.get(index);
					next2 = segments.get(index + 1);
				}
				
				float lenForT = len - curr.totalLength;
				float t = lenForT / (curr.lengthToNext + next.lengthToNext);
				
				//Vector2f p = Misc.bezier(curr.loc, next.loc, next2.loc, t);
				Vector2f p0 = curr.loc;
				Vector2f p1 = next.loc;
				Vector2f p2 = next2.loc;
				
				p0 = new Vector2f(p0);
				p0.x += curr.normal.x * params.width * 0.5f * offset;
				p0.y += curr.normal.y * params.width * 0.5f * offset;
				
				p2 = new Vector2f(p2);
				p2.x += next2.normal.x * params.width * 0.5f * offset;
				p2.y += next2.normal.y * params.width * 0.5f * offset;
				
				p1 = new Vector2f(next.locB);
				p1 = new Vector2f(p1);
				p1.x += next.normal.x * params.width * 0.5f * offset;
				p1.y += next.normal.y * params.width * 0.5f * offset;
				
				Vector2f p = Misc.bezier(p0, p1, p2, t);
				
//				float tPrev = (prev.lengthToNext + len) / (prev.lengthToNext + curr.lengthToNext);
//				Vector2f pPrev = Misc.bezier(prev.loc, curr.loc, next.loc, tPrev);
				//curr.lengthToNext + next.lengthToNext
//				float f = lenForT / curr.lengthToNext;
//				Vector2f perp;
//				if (f < 1f) {
//					perp = Misc.interpolateVector(curr.normal, next.normal, f);
//				} else {
//					f = (lenForT - curr.lengthToNext) / next.lengthToNext;
////					if (f > 1f) {
////						System.out.println("wefwefe " + index);
////					}
//					perp = Misc.interpolateVector(next.normal, next2.normal, f);
//				}
//				perp.scale(offset * params.width * 0.5f);
				//perp.set(0, 0);
				
				//p = Misc.interpolateVector(pPrev, p, 0.5f);
				//GL11.glVertex2f(p.x + perp.x, p.y + perp.y);
				 * 
				 */
				
				Vector2f p = getPointAt(len, offset);
				if (p != null) {
					GL11.glVertex2f(p.x, p.y);
				}
			}
			if (false) {
				Misc.setColor(Color.red);
				for (int i = 0; i < segments.size() - 3; i+=2) {
					//GL11.glBegin(GL11.GL_POINTS);
					SlipstreamSegment prev = null;
					if (i > 0) {
						prev = segments.get(i - 1);
					}
					SlipstreamSegment curr = segments.get(i);
					SlipstreamSegment next = segments.get(i + 1);	
					SlipstreamSegment next2 = segments.get(i + 2);
					SlipstreamSegment next3 = segments.get(i + 3);
					
		//			GL11.glVertex2f(curr.loc.x, curr.loc.y);
		//			GL11.glVertex2f(next.loc.x, next.loc.y);
		//			GL11.glVertex2f(next2.loc.x, next2.loc.y);
					
					Vector2f p0 = curr.loc;
					Vector2f p1 = next.loc;
					Vector2f p2 = next2.loc;
					Vector2f p3 = next3.loc;
					
	//				float p1ToP2 = Misc.getAngleInDegrees(p1, p2);
	//				float p2ToP3 = Misc.getAngleInDegrees(p2, p3);
	//				float diff = Misc.getAngleDiff(p1ToP2, p2ToP3);
	//				float adjustment = Math.min(diff, Math.max(diff * 0.25f, diff - 10f));
	//				adjustment = diff * 0.5f;
	//				//adjustment = diff * 0.25f;
	//				float angle = p1ToP2 + Misc.getClosestTurnDirection(p1ToP2, p2ToP3) * adjustment * 1f + 180f;
	//				//angle = Misc.getAngleInDegrees(p3, p2);
	//				float dist = Misc.getDistance(p2, p1);
	//				Vector2f p1Adjusted = Misc.getUnitVectorAtDegreeAngle(angle);
	//				p1Adjusted.scale(dist);
	//				Vector2f.add(p1Adjusted, p2, p1Adjusted);
					
					//GL11.glVertex2f(p1Adjusted.x, p1Adjusted.y);
					//GL11.glVertex2f(p1.x, p1.y);
					
					p0 = new Vector2f(p0);
					p0.x += curr.normal.x * curr.width * 0.5f * offset;
					p0.y += curr.normal.y * curr.width * 0.5f * offset;
					
					p2 = new Vector2f(p2);
					p2.x += next2.normal.x * next2.width * 0.5f * offset;
					p2.y += next2.normal.y * next2.width * 0.5f * offset;
					
					p1 = new Vector2f(next.locB);
					p1 = new Vector2f(p1);
					p1.x += next.normal.x * next.width * 0.5f * offset;
					p1.y += next.normal.y * next.width * 0.5f * offset;
					
	//				p1ToP2 = Misc.getAngleInDegrees(p1, p2);
	//				p2ToP3 = Misc.getAngleInDegrees(p2, p3);
	//				diff = Misc.getAngleDiff(p1ToP2, p2ToP3);
	//				adjustment = Math.min(diff, Math.max(diff * 0.25f, diff - 10f));
	//				adjustment = diff * 0.5f;
	//				//adjustment = diff * 0.25f;
	//				angle = p1ToP2 + Misc.getClosestTurnDirection(p1ToP2, p2ToP3) * adjustment * 1f + 180f;
	//				//angle = Misc.getAngleInDegrees(p3, p2);
	//				dist = Misc.getDistance(p2, p1);
	//				p1Adjusted = Misc.getUnitVectorAtDegreeAngle(angle);
	//				p1Adjusted.scale(dist);
	//				Vector2f.add(p1Adjusted, p2, p1Adjusted);
					
					incr = 10f;
					for (float len = 0; len < curr.lengthToNext + next.lengthToNext; len += incr) {
						float t = len / (curr.lengthToNext + next.lengthToNext);
						//Vector2f p = Misc.bezier(curr.loc, next.loc, next2.loc, t);
						Vector2f p = Misc.bezier(p0, p1, p2, t);
						
		//				float tPrev = (prev.lengthToNext + len) / (prev.lengthToNext + curr.lengthToNext);
		//				Vector2f pPrev = Misc.bezier(prev.loc, curr.loc, next.loc, tPrev);
						
						float f = len / curr.lengthToNext;
						Vector2f perp;
						if (f < 1f) {
							perp = Misc.interpolateVector(curr.normal, next.normal, f);
						} else {
							f = (len - curr.lengthToNext) / next.lengthToNext;
							perp = Misc.interpolateVector(next.normal, next2.normal, f);
						}
						perp.scale(offset * curr.width * 0.5f);
						perp.set(0, 0);
						
						//p = Misc.interpolateVector(pPrev, p, 0.5f);
						GL11.glVertex2f(p.x, p.y);
						//GL11.glVertex2f(p.x + perp.x, p.y + perp.y);
						//GL11.glVertex2f(pPrev.x, pPrev.y);
					}
					//if (i == 4) break;
				}
			}
			GL11.glEnd();
		}
		
//		GL11.glBegin(GL11.GL_LINES);
//		for (int i = 0; i < segments.size() - 4; i+=2) {
//			//GL11.glBegin(GL11.GL_POINTS);
//			SlipstreamSegment prev = null;
//			if (i > 0) {
//				prev = segments.get(i - 1);
//			}
//			SlipstreamSegment curr = segments.get(i);
//			SlipstreamSegment next = segments.get(i + 1);	
//			SlipstreamSegment next2 = segments.get(i + 2);
//			SlipstreamSegment next3 = segments.get(i + 3);
//			
////			GL11.glVertex2f(curr.loc.x, curr.loc.y);
////			GL11.glVertex2f(next.loc.x, next.loc.y);
////			GL11.glVertex2f(next2.loc.x, next2.loc.y);
//			
//			Vector2f p0 = curr.loc;
//			Vector2f p1 = next.loc;
//			Vector2f p2 = next2.loc;
//			Vector2f p3 = next3.loc;
//			
//			float p1ToP2 = Misc.getAngleInDegrees(p1, p2);
//			float p2ToP3 = Misc.getAngleInDegrees(p2, p3);
//			float diff = Misc.getAngleDiff(p1ToP2, p2ToP3);
//			float adjustment = Math.min(diff, Math.max(diff * 0.25f, diff - 10f));
//			adjustment = diff * 0.5f;
//			//adjustment = diff * 0.25f;
//			float angle = p1ToP2 + Misc.getClosestTurnDirection(p1ToP2, p2ToP3) * adjustment * 1f + 180f;
//			//angle = Misc.getAngleInDegrees(p3, p2);
//			float dist = Misc.getDistance(p2, p1);
//			Vector2f p1Adjusted = Misc.getUnitVectorAtDegreeAngle(angle);
//			p1Adjusted.scale(dist);
//			Vector2f.add(p1Adjusted, p2, p1Adjusted);
//			
//			//skip = diff < 30f;
//			skip = false;
//			if (skip) p1Adjusted.set(p1);
//			skip = !skip;
//			
//			prevAdjustedP1 = p1Adjusted;
//			//GL11.glVertex2f(p1Adjusted.x, p1Adjusted.y);
//			//GL11.glVertex2f(p1.x, p1.y);
//			
//			float incr = 10f;
//			Misc.setColor(new Color(1f, 0.5f, 0f, 1f));
//			for (float len = 0; len < curr.lengthToNext + next.lengthToNext; len += incr) {
//				float t = len / (curr.lengthToNext + next.lengthToNext);
//				//Vector2f p = Misc.bezier(curr.loc, next.loc, next2.loc, t);
//				Vector2f p = Misc.bezier(curr.loc, p1Adjusted, next2.loc, t);
////				float tPrev = (prev.lengthToNext + len) / (prev.lengthToNext + curr.lengthToNext);
////				Vector2f pPrev = Misc.bezier(prev.loc, curr.loc, next.loc, tPrev);
//				
//				float f = len / curr.lengthToNext;
//				Vector2f perp;
//				if (f < 1f) {
//					perp = Misc.interpolateVector(curr.normal, next.normal, f);
//				} else {
//					f = (len - curr.lengthToNext) / next.lengthToNext;
//					perp = Misc.interpolateVector(next.normal, next2.normal, f);
//				}
//				
//				
//				perp.scale(1f * params.width * 0.5f);
//				
//				//p = Misc.interpolateVector(pPrev, p, 0.5f);
//				//GL11.glVertex2f(p.x, p.y);
//				GL11.glVertex2f(p.x + perp.x, p.y + perp.y);
//				GL11.glVertex2f(p.x - perp.x, p.y - perp.y);
//				//GL11.glVertex2f(pPrev.x, pPrev.y);
//			}
//			//if (i == 4) break;
//		}
//		GL11.glEnd();
		
//		GL11.glPointSize(10);
//		GL11.glBegin(GL11.GL_POINTS);
//		for (int i = 0; i < segments.size() - 4; i+=2) {
//			if (i % 4 == 0) {
//				Misc.setColor(Color.red);
//			} else {
//				Misc.setColor(Color.green);
//			}
//			//GL11.glBegin(GL11.GL_POINTS);
//			//SlipstreamSegment prev = segments.get(i);
//			SlipstreamSegment curr = segments.get(i);
//			SlipstreamSegment next = segments.get(i + 1);	
//			SlipstreamSegment next2 = segments.get(i + 2);
//			SlipstreamSegment next3 = segments.get(i + 3);
//			
////			GL11.glVertex2f(curr.loc.x, curr.loc.y);
////			GL11.glVertex2f(next.loc.x, next.loc.y);
////			GL11.glVertex2f(next2.loc.x, next2.loc.y);
//			
//			Vector2f p0 = curr.loc;
//			Vector2f p1 = next.loc;
//			Vector2f p2 = next2.loc;
//			Vector2f p3 = next3.loc;
//			
//			float p1ToP2 = Misc.getAngleInDegrees(p1, p2);
//			float p2ToP3 = Misc.getAngleInDegrees(p2, p3);
//			float diff = Misc.getAngleDiff(p1ToP2, p2ToP3);
//			float angle = p1ToP2 + Misc.getClosestTurnDirection(p1ToP2, p2ToP3) * diff * 1f + 180f;
//			//angle = Misc.getAngleInDegrees(p3, p2);
//			float dist = Misc.getDistance(p2, p1);
//			Vector2f p1Adjusted = Misc.getUnitVectorAtDegreeAngle(angle);
//			p1Adjusted.scale(dist);
//			Vector2f.add(p1Adjusted, p2, p1Adjusted);
//			prevAdjustedP1 = p1Adjusted;
//			//GL11.glVertex2f(p1Adjusted.x, p1Adjusted.y);
//			//GL11.glVertex2f(p1.x, p1.y);
//			
//			GL11.glVertex2f(p0.x, p0.y);
//			GL11.glVertex2f(p1Adjusted.x, p1Adjusted.y);
//			GL11.glVertex2f(p2.x, p2.y);
//		}
//		GL11.glEnd();
		
		if (false) {
			float[] place = getLengthAndWidthFractionWithinStream(Global.getSector().getPlayerFleet().getLocation());
			if (place != null) {
				Misc.setColor(Color.red);
				GL11.glPointSize(40f/zoom);
				GL11.glEnable(GL11.GL_POINT_SMOOTH);
				GL11.glBegin(GL11.GL_POINTS);
				Vector2f p = getPointAt(place[0], place[1]);
				
				GL11.glVertex2f(p.x, p.y);
				
				Misc.setColor(Color.blue);
				p = Global.getSector().getPlayerFleet().getLocation();
				GL11.glVertex2f(p.x, p.y);
				
				SlipstreamSegment seg = getSegmentForDist(place[0]);
				if (seg != null) {
					float withinSeg = place[0] - seg.totalLength;
					Vector2f p2 = new Vector2f(seg.normal.y, -seg.normal.x);
					p2.scale(withinSeg);
					Vector2f.add(p2, seg.loc, p2);
					float width = seg.wobbledWidth;
					if (segments.size() > seg.index + 1) {
						SlipstreamSegment next = segments.get(seg.index + 1);
						width = Misc.interpolate(seg.wobbledWidth, next.wobbledWidth, 
								(place[0] - seg.totalLength) / seg.lengthToNext);
					}
					p2.x += getNormalAt(place[0]).x * place[1] * width * 0.5f;
					p2.y += getNormalAt(place[0]).y * place[1] * width * 0.5f;
					Misc.setColor(Color.green);
					GL11.glVertex2f(p2.x, p2.y);
				}
				GL11.glEnd();
			}
		}
		
//		GL11.glBegin(GL11.GL_LINE_STRIP);
//		for (int i = 1; i < segments.size() - 2; i++) {
//			SlipstreamSegment prev = segments.get(i);
//			SlipstreamSegment curr = segments.get(i);
//			SlipstreamSegment next = segments.get(i + 1);	
//			SlipstreamSegment next2 = segments.get(i + 2);
//			
//			float incr = 5f;
//			for (float len = 0; len < curr.lengthToNext; len += incr) {
//				float t = len / (curr.lengthToNext + next.lengthToNext);
//				Vector2f p = Misc.bezier(curr.loc, next.loc, next2.loc, t);
//				
//				float tPrev = (prev.lengthToNext + len) / (prev.lengthToNext + curr.lengthToNext);
//				Vector2f pPrev = Misc.bezier(prev.loc, curr.loc, next.loc, tPrev);
//				
//				//p = Misc.interpolateVector(pPrev, p, 0.5f);
//				//GL11.glVertex2f(p.x, p.y);
//				GL11.glVertex2f(pPrev.x, pPrev.y);
//			}
//			if (i == 4) break;
//		}
//		GL11.glEnd();
		
		boolean curvedTrails = true;
		boolean useTex = false;
		//if (zoom > 1.25f) useTex = false;
		//useTex = false;
		//System.out.println("USETEX = " + useTex);
		if (!useTex) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glLineWidth(Math.max(1f, Math.min(2f, 2f/zoom)));
			//GL11.glLineWidth(25f);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
		}
		
		//curvedTrails = false;
		if (!curvedTrails) {
			GL11.glBegin(GL11.GL_LINES);
		}
//		GL11.glEnable(GL11.GL_POINT_SMOOTH);
//		GL11.glPointSize(10f);
//		GL11.glBegin(GL11.GL_POINTS);
		//int index = 0;
		
		if (useTex) {
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			
			SpriteAPI line = Global.getSettings().getSprite("graphics/hud/line4x4.png");
			//line = Global.getSettings().getSprite("graphics/hud/line32x32.png");
			line.bindTexture();
		}
		
		for (SlipstreamParticle p : particles) {
			SlipstreamSegment seg = getSegmentForDist(p.dist);
			if (seg == null || !nearSet.contains(seg)) continue; 
			
//			index++;
//			if (index > 1) break;
			//if (true) break;
			float a = viewport.getAlphaMult();
			if (p.remaining <= 0.5f) {
				a = p.remaining / 0.5f;
			} else if (p.elapsed < params.particleFadeInTime) {
				a = p.elapsed / params.particleFadeInTime;
			}
			
			a *= getFaderBrightness(p.dist);
			
			//a *= 0.5f;
			//a *= 0.1f;
			
			//a = 1f;
			
//			SlipstreamSegment seg = getSegmentForDist(p.dist);
//			if (seg == null) continue;
			float yPos = p.yPos;
			//yPos = 0f;
			
			if (curvedTrails) {
				if (useTex) {
					GL11.glBegin(GL11.GL_QUAD_STRIP);
					Vector2f curr = getPointAt(p.dist, yPos);
					if (curr == null || !viewport.isNearViewport(curr, p.speed * params.lineLengthFractionOfSpeed + 50f)) {
						GL11.glEnd();
						continue;
					}
					float iter = 5f;
					float incr = p.speed * params.lineLengthFractionOfSpeed / iter;
					float lw = 1f;
					for (float i = 0; i < iter; i++) {
						float min = incr * 1f;
						float dist = p.dist - i * incr - min;
						Vector2f next = getPointAt(dist, yPos);
						if (next == null) break;
						
						Vector2f perp = getNormalAt(dist);
						if (perp == null) {
							GL11.glEnd();
							break;
						}
						
						float a1 = a * (iter - i) / (iter - 1);
						if (i == 0) a1 = 0f;
						
						Misc.setColor(p.color, a1);
						GL11.glTexCoord2f(0, 0f);
						GL11.glVertex2f(curr.x + perp.x * lw, curr.y + perp.y * lw);
						GL11.glTexCoord2f(0, 1f);
						GL11.glVertex2f(curr.x - perp.x * lw, curr.y - perp.y * lw);
						curr = next;
					}
					GL11.glEnd();
				} else {
					GL11.glBegin(GL11.GL_LINE_STRIP);
					//GL11.glBegin(GL11.GL_LINES);
					Vector2f curr = getPointAt(p.dist, yPos);
					if (curr == null || !viewport.isNearViewport(curr, p.speed * params.lineLengthFractionOfSpeed + 50f)) {
						GL11.glEnd();
						continue;
					}
					float iter = 5f;
					float incr = p.speed * params.lineLengthFractionOfSpeed / iter;
					for (float i = 0; i < iter; i++) {
						
						float min = incr * 0.5f;
						Vector2f next = getPointAt(p.dist - i * incr - min, yPos);
						if (next == null) {
							GL11.glEnd();
							break;
						}
						
						float a1 = a * (iter - i) / (iter - 1);
						//float a2 = a * (iter - i - 1) / (iter - 1);
						if (i == 0) a1 = 0f;
						
						Misc.setColor(p.color, a1);
						GL11.glVertex2f(curr.x, curr.y);
						//Misc.setColor(p.color, a2);
						//GL11.glVertex2f(next.x, next.y);
						curr = next;
					}
					GL11.glEnd();
				}
			} else {
				Vector2f start = getPointAt(p.dist + p.speed * params.lineLengthFractionOfSpeed * 0.1f, yPos);
				if (start == null || !viewport.isNearViewport(start, 500)) continue;
				
				Vector2f mid = getPointAt(p.dist, yPos);
				if (mid == null) continue;
				Vector2f end = getPointAt(p.dist - p.speed * params.lineLengthFractionOfSpeed * 0.9f, yPos);
				if (end == null) continue;
				
				Misc.setColor(p.color, 0f);
				GL11.glVertex2f(start.x, start.y);
				Misc.setColor(p.color, a);
				GL11.glVertex2f(mid.x, mid.y);
				GL11.glVertex2f(mid.x, mid.y);
				Misc.setColor(p.color, 0f);
				GL11.glVertex2f(end.x, end.y);
			}
//			
		}
		if (!curvedTrails) {
			GL11.glEnd();
		}
	}
	
	
	protected FaderUtil fader = new FaderUtil(0f, 0.5f, 0.5f);
	
	public void renderSegments(SpriteAPI sprite, SpriteAPI edge, float alpha, List<SlipstreamSegment> segments) {
		//if (true) return;
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		sprite.bindTexture();
		GL11.glEnable(GL11.GL_BLEND);
		//GL11.glDisable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Color color = sprite.getColor();
		//color = Misc.interpolateColor(color, Color.black, 0.5f);
		//color = Color.black;
		//color = Misc.scaleColorOnly(color, 0.3f);
		//color = Misc.setAlpha(color, 100);
		
		boolean wireframe = false;
		//wireframe = true;
		if (wireframe) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
		}
		
		boolean subtract = false;
		//subtract = true;
		if (subtract) {
			GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
		}
		
		// "channel"
		Color c = Color.black;
		//c = Misc.setAlpha(c, 255);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		int alphaInDeep = 255;
		int alphaOutside = 0;
		HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		
		GL11.glBegin(GL11.GL_QUAD_STRIP);
		
		for (int i = 0; i < segments.size(); i++) {
			SlipstreamSegment curr = segments.get(i);
			float a = curr.fader.getBrightness();
			
			if (entity.isInHyperspace() && plugin.isInClouds(curr.loc, 100f)) {
				c = Misc.setAlpha(c, alphaInDeep);
			} else {
				c = Misc.setAlpha(c, alphaOutside);
			}
			
			Vector2f p1 = new Vector2f(curr.loc);
			p1.x += curr.normal.x * curr.width * 0.5f;
			p1.y += curr.normal.y * curr.width * 0.5f;
			Vector2f p2 = new Vector2f(curr.loc);
			p2.x += curr.normal.x * (curr.width * 0.5f - params.edgeWidth);
			p2.y += curr.normal.y * (curr.width * 0.5f - params.edgeWidth);
			
			p1.x += curr.wobble1.vector.x;
			p1.y += curr.wobble1.vector.y;
			
			Misc.setColor(c, alpha * 0f * a);
			GL11.glVertex2f(p1.x, p1.y);
			Misc.setColor(c, alpha * 1f * a);
			GL11.glVertex2f(p2.x, p2.y);
		}
		GL11.glEnd();
		
		//edge2.bindTexture();
		GL11.glBegin(GL11.GL_QUAD_STRIP);
		
		for (int i = 0; i < segments.size(); i++) {
			SlipstreamSegment curr = segments.get(i);
			float a = curr.fader.getBrightness();
			
			if (entity.isInHyperspace() && plugin.isInClouds(curr.loc, 100f)) {
				c = Misc.setAlpha(c, alphaInDeep);
			} else {
				c = Misc.setAlpha(c, alphaOutside);
			}
			
			Vector2f p1 = new Vector2f(curr.loc);
			p1.x -= curr.normal.x * curr.width * 0.5f;
			p1.y -= curr.normal.y * curr.width * 0.5f;
			Vector2f p2 = new Vector2f(curr.loc);
			p2.x -= curr.normal.x * (curr.width * 0.5f - params.edgeWidth);
			p2.y -= curr.normal.y * (curr.width * 0.5f - params.edgeWidth);
			
			p1.x += curr.wobble2.vector.x;
			p1.y += curr.wobble2.vector.y;
			
			Misc.setColor(c, alpha * 0f * a);
			GL11.glVertex2f(p1.x, p1.y);
			Misc.setColor(c, alpha * 1f * a);
			GL11.glVertex2f(p2.x, p2.y);
		}
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_QUAD_STRIP);
		
		for (int i = 0; i < segments.size(); i++) {
			SlipstreamSegment curr = segments.get(i);
			float a = curr.fader.getBrightness();
			
			if (entity.isInHyperspace() && plugin.isInClouds(curr.loc, 100f)) {
				c = Misc.setAlpha(c, alphaInDeep);
			} else {
				c = Misc.setAlpha(c, alphaOutside);
			}
			
			Vector2f p1 = new Vector2f(curr.loc);
			p1.x += curr.normal.x * (curr.width * 0.5f - params.edgeWidth);
			p1.y += curr.normal.y * (curr.width * 0.5f - params.edgeWidth);
			Vector2f p2 = new Vector2f(curr.loc);
			p2.x -= curr.normal.x * (curr.width * 0.5f - params.edgeWidth);
			p2.y -= curr.normal.y * (curr.width * 0.5f - params.edgeWidth);
			
			Misc.setColor(c, alpha * 1f * a);
			GL11.glVertex2f(p1.x, p1.y);
			GL11.glVertex2f(p2.x, p2.y);
		}
		GL11.glEnd();
		// end "channel"
		
		
		// main background
		if (!wireframe) {
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
		}
		//GL11.glDisable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glBegin(GL11.GL_QUAD_STRIP);
		for (int i = 0; i < segments.size(); i++) {
			SlipstreamSegment curr = segments.get(i);
			float a = curr.fader.getBrightness();
			
			Vector2f p1 = new Vector2f(curr.loc);
			p1.x += curr.normal.x * curr.width * 0.5f;
			p1.y += curr.normal.y * curr.width * 0.5f;
			Vector2f p2 = new Vector2f(curr.loc);
			p2.x -= curr.normal.x * curr.width * 0.5f;
			p2.y -= curr.normal.y * curr.width * 0.5f;
			
			p1.x += curr.wobble1.vector.x;
			p1.y += curr.wobble1.vector.y;
			p2.x += curr.wobble2.vector.x;
			p2.y += curr.wobble2.vector.y;
			
			Misc.setColor(color, alpha * 1f * a);
			GL11.glTexCoord2f(curr.tx, 0f);
			GL11.glVertex2f(p1.x, p1.y);
			GL11.glTexCoord2f(curr.tx, 1f);
			GL11.glVertex2f(p2.x, p2.y);
		}
		GL11.glEnd();
		
		
		// edges
		color = edge.getColor();
		float wobbleMult = 0.5f;
		edge.bindTexture();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glBegin(GL11.GL_QUAD_STRIP);
		
		for (int i = 0; i < segments.size(); i++) {
			SlipstreamSegment curr = segments.get(i);
			float a = curr.fader.getBrightness();
			
			Vector2f p1 = new Vector2f(curr.loc);
			p1.x += curr.normal.x * curr.width * 0.5f;
			p1.y += curr.normal.y * curr.width * 0.5f;
			Vector2f p2 = new Vector2f(curr.loc);
			p2.x += curr.normal.x * (curr.width * 0.5f - params.edgeWidth);
			p2.y += curr.normal.y * (curr.width * 0.5f - params.edgeWidth);
			
			p1.x += curr.wobble1.vector.x * wobbleMult;
			p1.y += curr.wobble1.vector.y * wobbleMult;
			p2.x += curr.wobble1.vector.x * wobbleMult;
			p2.y += curr.wobble1.vector.y * wobbleMult;
			
			Misc.setColor(color, alpha * 1f * a);
			GL11.glTexCoord2f(curr.txe1, 1f);
			GL11.glVertex2f(p1.x, p1.y);
			GL11.glTexCoord2f(curr.txe1, 0f);
			GL11.glVertex2f(p2.x, p2.y);
		}
		GL11.glEnd();
		
		//edge2.bindTexture();
		GL11.glBegin(GL11.GL_QUAD_STRIP);
		
		for (int i = 0; i < segments.size(); i++) {
			SlipstreamSegment curr = segments.get(i);
			float a = curr.fader.getBrightness();
			
			Vector2f p1 = new Vector2f(curr.loc);
			p1.x -= curr.normal.x * curr.width * 0.5f;
			p1.y -= curr.normal.y * curr.width * 0.5f;
			Vector2f p2 = new Vector2f(curr.loc);
			p2.x -= curr.normal.x * (curr.width * 0.5f - params.edgeWidth);
			p2.y -= curr.normal.y * (curr.width * 0.5f - params.edgeWidth);
			
			p1.x += curr.wobble2.vector.x * wobbleMult;
			p1.y += curr.wobble2.vector.y * wobbleMult;
			p2.x += curr.wobble2.vector.x * wobbleMult;
			p2.y += curr.wobble2.vector.y * wobbleMult;
			
			Misc.setColor(color, alpha * 1f * a);
			GL11.glTexCoord2f(curr.txe2, 1f);
			GL11.glVertex2f(p1.x, p1.y);
			GL11.glTexCoord2f(curr.txe2, 0f);
			GL11.glVertex2f(p2.x, p2.y);
		}
		GL11.glEnd();
		
		
		if (subtract) {
			GL14.glBlendEquation(GL14.GL_FUNC_ADD);
		}
		
		if (wireframe) GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}

	
	
	
	
	public Color getRandomColor() {
		return Misc.interpolateColor(params.minColor, params.maxColor, (float) Math.random());
	}
	
	
	/**
	 * result[0] = actual distance along the length of the slipstream 
	 * result[1] = offset along the width of the slipstream, 
	 * 				0 = on center, 1 = on edge along normal, -1 = on edge along negative of normal
	 * null if outside stream
	 * Assumes rectangular, non-tapered stream
	 * @param loc
	 * @return
	 */
	public float [] getLengthAndWidthFractionWithinStream(Vector2f loc) {
		float dist = Misc.getDistance(loc, entity.getLocation());
		if (dist > getRenderRange()) return null;
		
		List<SlipstreamSegment> near = getSegmentsNear(loc, 0f);
		
		for (SlipstreamSegment curr : near) {
			SlipstreamSegment next = null;
			if (segments.size() > curr.index + 1) {
				next = segments.get(curr.index + 1);
			} else {
				next = new SlipstreamSegment();
				//next2.width = next.width;
				next.wobbledWidth = curr.wobbledWidth;
				
				next.normal = curr.normal;
				//next2.dir = next.dir;
				next.loc = new Vector2f(curr.dir);
				next.loc.scale(curr.lengthToPrev);
				Vector2f.add(next.loc, curr.loc, next.loc);
				//next2.locB = next2.loc;
				next.lengthToPrev = curr.lengthToPrev;
				//continue;
			}
				
			Vector2f p3 = loc;
			Vector2f p1 = curr.loc;
			Vector2f p2 = next.loc;
			
			Vector2f currNormalP1 = new Vector2f(curr.loc);
			Vector2f currNormalP2 = new Vector2f(curr.normal);
			currNormalP2.scale(100f);
			Vector2f.add(currNormalP2, currNormalP1, currNormalP2);
			
			Vector2f nextNormalP1 = new Vector2f(next.loc);
			Vector2f nextNormalP2 = new Vector2f(next.normal);
			nextNormalP2.scale(100f);
			Vector2f.add(nextNormalP2, nextNormalP1, nextNormalP2);
			
			//Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(p1, p2));
			Vector2f dir = new Vector2f(curr.dir);
			dir.scale(100f);
			Vector2f p4 = Vector2f.add(p3, dir, new Vector2f());
			
			Vector2f currNormalP = Misc.intersectLines(currNormalP1, currNormalP2, p3, p4);
			if (currNormalP == null) continue;
			Vector2f nextNormalP = Misc.intersectLines(nextNormalP1, nextNormalP2, p3, p4);
			if (nextNormalP == null) continue;
			
			float u = (p3.x - currNormalP.x) * (nextNormalP.x - currNormalP.x) + 
							(p3.y - currNormalP.y) * (nextNormalP.y - currNormalP.y);
			float denom = Vector2f.sub(nextNormalP, currNormalP, new Vector2f()).length();
			denom *= denom;
			if (denom == 0) continue;
			u /= denom;
			
			if (u >= 0 && u <= 1) { // p3 is between the two points on the normals
				Vector2f normalAtP3 = Misc.interpolateVector(curr.normal, next.normal, u);
				normalAtP3.scale(100f);
				Vector2f p3PlusNormal = Vector2f.add(p3, normalAtP3, new Vector2f());
				
				Vector2f intersect = Misc.intersectLines(p1, p2, p3, p3PlusNormal);
				if (intersect == null) continue;
				
				float distFromLine = Vector2f.sub(intersect, p3, new Vector2f()).length();
				float width = Misc.interpolate(curr.wobbledWidth, next.wobbledWidth, u);
				if (distFromLine >= width / 2f) return null;
				
				float [] result = new float[2];
				//result[0] = curr.totalLength + u * curr.lengthToNext;
				result[0] = curr.totalLength + u * next.lengthToPrev;
				result[1] = distFromLine / (width / 2f);
				
				float currToLoc = Misc.getAngleInDegrees(p1, p3);
				float segDir = Misc.getAngleInDegrees(p1, p2);
				if (Misc.getClosestTurnDirection(segDir, currToLoc) < 0) {
					result[1] = -result[1];	
				}
				
				return result;
			
//			float u = (p3.x - p1.x) * (p2.x - p1.x) + (p3.y - p1.y) * (p2.y - p1.y);
//			float denom = Vector2f.sub(p2, p1, new Vector2f()).length();
//			denom *= denom;
//			if (denom == 0) continue;
//			u /= denom;
//			
////			if (u < 0 && u > -0.03f) u = 0f;
////			if (u > 1 && u < 1.03f) u = 1f;
//			
//			if (u >= 0 && u <= 1) { // intersection is between p1 and p2
//				Vector2f intersect = new Vector2f();
//				intersect.x = p1.x + u * (p2.x - p1.x);
//				intersect.y = p1.y + u * (p2.y - p1.y);
//				float distFromLine = Vector2f.sub(intersect, p3, new Vector2f()).length();
//
//				float width = curr.wobbledWidth;
//				if (next != null) {
//					width = Misc.interpolate(curr.wobbledWidth, next.wobbledWidth, u);
//				}
//				
//				if (distFromLine >= width / 2f) return null;
//				
//				float [] result = new float[2];
//				result[0] = curr.totalLength + u * curr.lengthToNext;
//				result[1] = distFromLine / (width / 2f);
//				
//				float currToLoc = Misc.getAngleInDegrees(p1, p3);
//				float segDir = Misc.getAngleInDegrees(p1, p2);
//				if (Misc.getClosestTurnDirection(segDir, currToLoc) < 0) {
//					result[1] = -result[1];	
//				}
//				
//				return result;
			}
			
		}
		
//		Vector2f p3 = new Vector2f(loc);
//		Vector2f p1 = new Vector2f(entity.getLocation());
//		Vector2f p2 = Misc.getUnitVectorAtDegreeAngle(entity.getFacing() + 180f);
//		//p2.scale(params.length);
//		Vector2f.add(p2, p1, p2);
//		
//		float u = (p3.x - p1.x) * (p2.x - p1.x) + (p3.y - p1.y) * (p2.y - p1.y);
//		float denom = Vector2f.sub(p2, p1, new Vector2f()).length();
//		denom *= denom;
//		if (denom == 0) return null;
//		u /= denom;
//		
//		if (u >= 0 && u <= 1) { // intersection is between p1 and p2
//			Vector2f intersect = new Vector2f();
//			intersect.x = p1.x + u * (p2.x - p1.x);
//			intersect.y = p1.y + u * (p2.y - p1.y);
//			float distFromLine = Vector2f.sub(intersect, p3, new Vector2f()).length();
//			//float distAlongLine = u * params.length;
//			if (distFromLine >= params.width/2f) return null;
//			
//			float [] result = new float[2];
//			result[0] = u;
//			result[1] = distFromLine / (params.width / 2f);
//			return result;
//		}
		return null;
	}
	
	public void applyEffectToFleets(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		for (CampaignFleetAPI fleet : entity.getContainingLocation().getFleets()) {
			applyEffect(fleet, days);
		}
	}
	
	//protected boolean playerWasInSlipstream = false;
	protected int playerWasInSlipstreamFramesAgo = 1000;
	public void applyEffect(SectorEntityToken other, float days) {
		if (other instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) other;
			
//			if (fleet.isPlayerFleet()) {
//				if (getLengthAndWidthFractionWithinStream(fleet.getLocation()) == null) {
//					System.out.println("wefwefwefe");
//				}
//				System.out.println("efwefwef");
//			}
			
			float [] offset = getLengthAndWidthFractionWithinStream(fleet.getLocation());
			if (offset == null) {
				if (fleet.isPlayerFleet()) {
					playerWasInSlipstreamFramesAgo++;
					if (playerWasInSlipstreamFramesAgo > 1000) {
						playerWasInSlipstreamFramesAgo = 1000;
					}
				}
				return;
			}
			
//			if (fleet.isPlayerFleet()) {
//				System.out.println("Location in stream: " + offset[0] + ", " + offset[1]);
//			}
			
			//params.burnLevel = 10;
			
			float distAlong = offset[0];
			float yOff = offset[1];
			
//			float intensity = 1f;
//			if (Math.abs(yOff) > 0.5f) {
//				intensity *= (1f - Math.abs(yOff)) / 0.5f;
//			}
			float intensity = getIntensity(yOff);
			float wMult = getWidthBasedSpeedMult(distAlong);
			//System.out.println("wMult: " + wMult);
			intensity *= wMult;
			intensity *= getFaderBrightness(distAlong);
			//intensity *= intensity;
			//System.out.println(intensity);
			
			if (intensity <= 0) {
				if (fleet.isPlayerFleet()) {
					playerWasInSlipstreamFramesAgo++;
					if (playerWasInSlipstreamFramesAgo > 1000) {
						playerWasInSlipstreamFramesAgo = 1000;
					}
				}
				return;
			}
			
			if (fleet.isPlayerFleet()) {
				//if (!playerWasInSlipstream) {
				//	playerWasInSlipstream = true;
				if (playerWasInSlipstreamFramesAgo > 5) {
					fleet.addFloatingText("Entering slipstream", Misc.setAlpha(fleet.getIndicatorColor(), 255), 0.5f);
				}
				playerWasInSlipstreamFramesAgo = 0;
			}
			
			//System.out.println("Intensity: " + intensity);

			// "wind" effect - adjust velocity
			float maxFleetBurn = fleet.getFleetData().getBurnLevel();
			float currFleetBurn = fleet.getCurrBurnLevel();
			
			float maxWindBurn = params.burnLevel * 2f;
			
			float currWindBurn = intensity * maxWindBurn;
			float maxFleetBurnIntoWind = maxFleetBurn - Math.abs(currWindBurn);
			float seconds = days * Global.getSector().getClock().getSecondsPerDay();
			
//			float angle = Misc.getAngleInDegreesStrict(this.entity.getLocation(), fleet.getLocation()) + 180f;
//			Vector2f windDir = Misc.getUnitVectorAtDegreeAngle(angle);
			Vector2f p1 = getPointAt(distAlong, yOff);
			Vector2f p2 = getPointAt(distAlong + 1f, yOff);
			if (p1 == null || p2 == null) {
				if (fleet.isPlayerFleet()) {
					playerWasInSlipstreamFramesAgo++;
					if (playerWasInSlipstreamFramesAgo > 1000) {
						playerWasInSlipstreamFramesAgo = 1000;
					}
				}
				return;
			}
			
			//Vector2f windDir = Misc.getUnitVectorAtDegreeAngle(entity.getFacing());
			Vector2f windDir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(p1, p2));
			if (currWindBurn < 0) {
				windDir.negate();
			}
			Vector2f velDir = Misc.normalise(new Vector2f(fleet.getVelocity()));
			//float baseFleetAccel = Misc.getSpeedForBurnLevel(fleet.getFleetData().getMinBurnLevel());
			float baseFleetAccel = fleet.getTravelSpeed();
			if (baseFleetAccel < 10f) baseFleetAccel = 10f;
			
			boolean fleetTryingToMove = fleet.getMoveDestination() !=  null && 
					Misc.getDistance(fleet.getLocation(), fleet.getMoveDestination()) > fleet.getRadius() + 10f;
			if (fleet.isPlayerFleet()) {
				fleetTryingToMove &= (
						Global.getSector().getCampaignUI().isPlayerFleetFollowingMouse() ||
						fleet.wasSlowMoving());
			}
			float windSpeedReduction = 0f;
			if (!fleetTryingToMove) {
				Vector2f dest = new Vector2f(windDir);
				dest.scale(1000f);
				Vector2f.add(dest, fleet.getLocation(), dest);
				fleet.setMoveDestination(dest.x, dest.y);
			} else {
				Vector2f moveDir = Misc.getUnitVectorAtDegreeAngle(
								Misc.getAngleInDegrees(fleet.getLocation(), fleet.getMoveDestination()));
				float dot = Vector2f.dot(windDir, moveDir);
				if (fleet.wasSlowMoving()) dot = -1f;
				if (dot < 0) {
					float accelBasedMult = fleet.getAcceleration() / baseFleetAccel;
					accelBasedMult *= accelBasedMult;
					if (accelBasedMult > 1f) accelBasedMult = 1f;
					if (accelBasedMult < 0.1f) accelBasedMult = 0.1f;
					windSpeedReduction = -dot * fleet.getFleetData().getBurnLevel() * accelBasedMult;
				}
			}
			
			//float burnBonus = fleet.getFleetData().getBurnLevel() - fleet.getFleetData().getMinBurnLevelUnmodified();
			float burnBonus = fleet.getFleetData().getBurnLevel() - fleet.getFleetData().getMinBurnLevel();
			if (burnBonus < 0) burnBonus = 0;
			//float maxSpeedWithWind = Misc.getSpeedForBurnLevel(params.burnLevel + burnBonus);
			float maxSpeedWithWind = Misc.getSpeedForBurnLevel((params.burnLevel * intensity) + burnBonus);
			if (windSpeedReduction > 0) {
				maxSpeedWithWind = Misc.getSpeedForBurnLevel(
							Math.max(params.burnLevel  * 0.5f * intensity, params.burnLevel * intensity - windSpeedReduction));
			}
			
			float fleetSpeedAlongWind = Vector2f.dot(windDir, fleet.getVelocity());
			if (fleetSpeedAlongWind >= maxSpeedWithWind) {
//				float dotPlayerAndWindVel = Vector2f.dot(windDir, velDir);
//				if (dotPlayerAndWindVel > 0.98f) {
					return;
				//}
			}
			
			velDir.scale(currFleetBurn);
			
			float fleetBurnAgainstWind = -1f * Vector2f.dot(windDir, velDir);
			
			
			float windSpeed = Misc.getSpeedForBurnLevel(currWindBurn);
			//float fleetSpeed = fleet.getTravelSpeed();
			Vector2f windVector = new Vector2f(windDir);
			windVector.scale(windSpeed);
			
			Vector2f vel = fleet.getVelocity();
			Vector2f diff = Vector2f.sub(windVector, vel, new Vector2f());
			//windDir.scale(seconds * fleet.getAcceleration());
			float max = diff.length();
			diff = Misc.normalise(diff);
			//diff.scale(Math.max(windSpeed * seconds, fleet.getAcceleration() * 1f * seconds));
			diff.scale(fleet.getAcceleration() * 3f * seconds);
			//diff.scale(fleet.getTravelSpeed() * 5f * seconds);
			//diff.scale(accelMult);
			if (diff.length() > max) {
				diff.scale(max / diff.length());
			}
			//System.out.println("Applying diff: " + diff);
			//fleet.setVelocity(vel.x + diff.x, vel.y + diff.y);
			
			
//			Vector2f velDir = Misc.normalise(new Vector2f(fleet.getVelocity()));
//			velDir.scale(currFleetBurn);
//			
//			float fleetBurnAgainstWind = -1f * Vector2f.dot(windDir, velDir);
//			
			float accelMult = 0.5f;
			if (fleetBurnAgainstWind > maxFleetBurnIntoWind) {
				accelMult += 0.75f + 0.25f * (fleetBurnAgainstWind - maxFleetBurnIntoWind);
			}
			
			
			//Vector2f vel = fleet.getVelocity();
			//windDir.scale(seconds * fleet.getAcceleration() * accelMult);
			//float baseFleetAccel = Math.max(fleet.getTravelSpeed(), fleet.getAcceleration());
			
			windDir.scale(seconds * baseFleetAccel * accelMult);
			fleet.setVelocity(vel.x + windDir.x, vel.y + windDir.y);
			
			
			boolean withGlow = true;
			//withGlow = false;
			if (withGlow) {
				Color glowColor = params.windGlowColor;
				int alpha = glowColor.getAlpha();
				if (alpha < 75) {
					glowColor = Misc.setAlpha(glowColor, 75);
				}
				// visual effects - glow, tail
				
				p1 = getNoWobblePointAt(distAlong, yOff);
				p2 = getNoWobblePointAt(distAlong + 100f, yOff);
				if (p1 != null && p2 != null) {
					windDir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(p1, p2));
					
//					float fleetSpeedAlongWind = Vector2f.dot(windDir, fleet.getVelocity());
//					//float fleetSpeed = fleet.getVelocity().length();
//					
//					windSpeed = Misc.getSpeedForBurnLevel(params.burnLevel);
//					float matchingWindFraction = fleetSpeedAlongWind/windSpeed;
//					float effectMag = 1f - matchingWindFraction;
//					if (effectMag < 0f)  effectMag = 0f;
					//if (effectMag < 0.25f) effectMag = 0.25f;
					//effectMag = 0.5f;
					
					String modId = "slipstream_" + entity.getId();
					float durIn = 1f;
					float durOut = 3f;
					//durIn = 0.5f;
					//float sizeNormal = (15f + 30f * effectMag * effectMag) * (intensity);
					float sizeNormal = 5f + 10f * intensity;
					for (FleetMemberViewAPI view : fleet.getViews()) {
						view.getWindEffectDirX().shift(modId, windDir.x * sizeNormal, durIn, durOut, 1f);
						view.getWindEffectDirY().shift(modId, windDir.y * sizeNormal, durIn, durOut, 1f);
						view.getWindEffectColor().shift(modId, glowColor, durIn, durOut, 1f);
					}
				}
			}
		}
	}
	
	
	public Vector2f getPointAt(float lengthAlongStream, float offset) {
		SlipstreamSegment curr = getSegmentForDist(lengthAlongStream);
		if (curr == null) return null;
		int index = curr.index;
		
		SlipstreamSegment next = null;
		SlipstreamSegment next2 = null;

		if (index >= segments.size() - 1) return null;
		
		if (index % 2 == 0) {
			next = segments.get(index + 1);
			if (index >= segments.size() - 2) {
				next2 = new SlipstreamSegment();
				//next2.width = next.width;
				next2.wobbledWidth = next.wobbledWidth;
				
				next2.normal = next.normal;
				//next2.dir = next.dir;
				next2.loc = new Vector2f(next.dir);
				next2.loc.scale(next.lengthToPrev);
				Vector2f.add(next2.loc, next.loc, next2.loc);
				//next2.locB = next2.loc;
				next2.lengthToPrev = next.lengthToPrev;
			} else {
				next2 = segments.get(index + 2);
			}
		}
		if (index % 2 != 0) {
			if (index >= segments.size() - 1) return null;
			curr = segments.get(index - 1);
			next = segments.get(index);
			next2 = segments.get(index + 1);
		}
		
		float lenForT = lengthAlongStream - curr.totalLength;
		//float t = lenForT / (curr.lengthToNext + next.lengthToNext);
		float t = lenForT / (curr.lengthToNext + next2.lengthToPrev);
//		if (t < 0) {
//			System.out.println("wefwefe");
//		}
		
		Vector2f p0 = new Vector2f(curr.loc);
		Vector2f p1 = new Vector2f(next.locB);
		Vector2f p2 = new Vector2f(next2.loc);
		
//		offset *= 0.7f;
//		p0.x += curr.normal.x * curr.width * 0.5f * offset;
//		p0.y += curr.normal.y * curr.width * 0.5f * offset;
//		
//		p2.x += next2.normal.x * next2.width * 0.5f * offset;
//		p2.y += next2.normal.y * next2.width * 0.5f * offset;
//		
//		p1.x += next.normal.x * next.width * 0.5f * offset;
//		p1.y += next.normal.y * next.width * 0.5f * offset;
		
		p0.x += curr.normal.x * curr.wobbledWidth * 0.5f * offset;
		p0.y += curr.normal.y * curr.wobbledWidth * 0.5f * offset;
		
		p2.x += next2.normal.x * next2.wobbledWidth * 0.5f * offset;
		p2.y += next2.normal.y * next2.wobbledWidth * 0.5f * offset;
		
		p1.x += next.normal.x * next.wobbledWidth * 0.5f * offset;
		p1.y += next.normal.y * next.wobbledWidth * 0.5f * offset;
		
		//System.out.println("T: " + t);
		Vector2f p = Misc.bezier(p0, p1, p2, t);
		
		return p;
	}
	
	public Vector2f getNoWobblePointAt(float lengthAlongStream, float offset) {
		SlipstreamSegment curr = getSegmentForDist(lengthAlongStream);
		if (curr == null) return null;
		int index = curr.index;
		if (index >= segments.size() - 2) return null;
		
		SlipstreamSegment next = segments.get(index + 1);
		SlipstreamSegment next2 = segments.get(index + 2);
		
		if (index % 2 != 0) {
			curr = segments.get(index - 1);
			next = segments.get(index);
			next2 = segments.get(index + 1);
		}
		
		float lenForT = lengthAlongStream - curr.totalLength;
		float t = lenForT / (curr.lengthToNext + next.lengthToNext);
//		if (t < 0) {
//			System.out.println("wefwefe");
//		}
		
		Vector2f p0 = new Vector2f(curr.loc);
		Vector2f p1 = new Vector2f(next.locB);
		Vector2f p2 = new Vector2f(next2.loc);
		
		float edges = params.edgeWidth * 2f * 0.5f;
		p0.x += curr.normal.x * (curr.width - edges) * 0.5f * offset;
		p0.y += curr.normal.y * (curr.width - edges) * 0.5f * offset;
		
		p2.x += next2.normal.x * (next2.width - edges) * 0.5f * offset;
		p2.y += next2.normal.y * (next2.width - edges) * 0.5f * offset;
		
		p1.x += next.normal.x * (next.width - edges) * 0.5f * offset;
		p1.y += next.normal.y * (next.width - edges) * 0.5f * offset;
		
		Vector2f p = Misc.bezier(p0, p1, p2, t);
		
		return p;
	}
	
	
	public Vector2f getNormalAt(float lengthAlongStream) {
		SlipstreamSegment curr = getSegmentForDist(lengthAlongStream);
		if (curr == null) return null;
		int index = curr.index;
		if (index >= segments.size() - 2) return null;
		
		SlipstreamSegment next = segments.get(index + 1);
		SlipstreamSegment next2 = segments.get(index + 2);
		
		if (index % 2 != 0) {
			curr = segments.get(index - 1);
			next = segments.get(index);
			next2 = segments.get(index + 1);
		}
		
		float lenForT = lengthAlongStream - curr.totalLength;
		
		float f = lenForT / curr.lengthToNext;
		Vector2f perp;
		if (f < 1f) {
			perp = Misc.interpolateVector(curr.normal, next.normal, f);
		} else {
			f = (lenForT - curr.lengthToNext) / next.lengthToNext;
			perp = Misc.interpolateVector(next.normal, next2.normal, f);
		}
		return perp;
	}
	
	public List<SlipstreamSegment> getSegmentsNear(Vector2f loc, float range) {
		//List<SlipstreamSegment> potential = new ArrayList<SlipstreamEntityPlugin2.SlipstreamSegment>();
		List<SlipstreamSegment> result = new ArrayList<SlipstreamEntityPlugin2.SlipstreamSegment>();
		int boxIndex = 0;
		for (BoundingBox box : bounds) {
			if (box.pointNeedsDetailedCheck(loc, range)) {
				int min = boxIndex * segmentsPerBox;
				for (int i = min; i < min + segmentsPerBox && i < segments.size(); i++) {
					SlipstreamSegment curr = segments.get(i);
					float distSq = Misc.getDistanceSq(curr.loc, loc);
					float r = range + curr.width + Math.max(curr.lengthToPrev, curr.lengthToNext);
					if (distSq < r * r) {
						result.add(curr);
					}
				}
			}
			boxIndex++;
		}
		
//		for (SlipstreamSegment curr : potential) {
//			float distSq = Misc.getDistanceSq(curr.loc, loc);
//			float r = range + curr.width + Math.max(curr.lengthToPrev, curr.lengthToNext);
//			if (distSq < r * r) {
//				result.add(curr);
//			}
//		}
//		List<SlipstreamSegment> result = new ArrayList<SlipstreamEntityPlugin2.SlipstreamSegment>();
//		for (SlipstreamSegment curr : segments) {
//			float distSq = Misc.getDistanceSq(curr.loc, loc);
//			float r = range + curr.width + Math.max(curr.lengthToPrev, curr.lengthToNext);
//			if (distSq < r * r) {
//				result.add(curr);
//			}
//		}
		return result;
	}
}





