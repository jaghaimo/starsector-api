package com.fs.starfarer.api.impl.campaign.velfield;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2.SlipstreamParams2;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2.SlipstreamSegment;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SlipstreamBuilder {
	
	public static enum StreamType {
		NORMAL,
		WIDE,
		NARROW,
	}
	
	public static float MIN_SPACING = 200f;
	public static float MAX_SPACING = 400f;
	public static float WIDTH_TO_SPACING_MULT = 0.2f;
	
	public static float MIN_NARROW = 0.5f;
	public static float MAX_NARROW = 0.7f;
	public static float MIN_NORMAL = 0.9f;
	public static float MAX_NORMAL = 1.1f;
	public static float MIN_WIDE = 1.3f;
	public static float MAX_WIDE = 1.6f;
	public static float MIN_VERY_WIDE = 1.8f;
	public static float MAX_VERY_WIDE = 2.1f;
	
	public static float WCHANGE_SLOW_T_MULT_MIN = 1f;
	public static float WCHANGE_SLOW_T_MULT_MIN_MAX = 1.2f;
	public static float WCHANGE_MEDIUM_T_MULT_MIN = 1.8f;
	public static float WCHANGE_MEDIUM_T_MULT_MAX = 2.2f;
	public static float WCHANGE_FAST_T_MULT_MIN = 2.8f;
	public static float WCHANGE_FAST_T_MULT_MAX = 3.2f;
	
	public static float WRAND_NONE_MULT = 0f;
	public static float WRAND_LOW_MULT = 0.1f;
	public static float WRAND_MEDIUM_MULT = 0.17f;
	public static float WRAND_HIGH_MULT = 0.25f;
	
	public static float FLUCT_LENGTH_MIN = 2500f;
	public static float FLUCT_LENGTH_MAX = 4000f;
	public static float FLUCT_MAG_MIN = 350f;
	public static float FLUCT_MAG_MAX = 500f;
	
	public static float FLUCT_NONE_MULT = 0f;
	public static float FLUCT_LOW_MULT = 0.33f;
	public static float FLUCT_MEDIUM_MULT = 0.67f;
	public static float FLUCT_HIGH_MULT = 1f;
	
	
	// width
	public static final int WIDTH_NARROW =		 0;
	public static final int WIDTH_NORMAL = 	 	 1;
	public static final int WIDTH_WIDE =		 2;
	public static final int WIDTH_VERY_WIDE =  	 3;
	
	// width randomness
	public static final int WRAND_NONE = 		 0;
	public static final int WRAND_LOW = 		 1;
	public static final int WRAND_MEDIUM = 	 	 2;
	public static final int WRAND_HIGH = 		 3;

	// how quickly width changes
	public static final int WCHANGE_SLOW =	 	 0;
	public static final int WCHANGE_MEDIUM =	 1;
	public static final int WCHANGE_FAST =	 	 2;
	
	// path fluctuations within sections
	public static final int FLUCT_NONE = 		 0;
	public static final int FLUCT_LOW = 		 1;
	public static final int FLUCT_MEDIUM =	 	 2;
	public static final int FLUCT_HIGH = 		 3;
	
	
	public void initTransitionMatrices() {
		widthTM = new float[4][4];
		if (type == StreamType.NORMAL) {
			widthTM[WIDTH_NARROW]  	 = new float [] {0.5f,   1f, 0.4f, 0.3f};
			widthTM[WIDTH_NORMAL]    = new float [] {  1f,   1f, 0.5f, 0.3f};
			widthTM[WIDTH_WIDE]  	 = new float [] {0.7f,   1f, 0.2f, 0.5f};
			widthTM[WIDTH_VERY_WIDE] = new float [] {  1f,   1f,   1f, 0.1f};
		} else if (type == StreamType.WIDE) {
			widthTM[WIDTH_NARROW]  	 = new float [] {0.1f,   1f, 0.6f, 0.4f};
			widthTM[WIDTH_NORMAL]    = new float [] {0.2f, 0.5f,   1f, 0.5f};
			widthTM[WIDTH_WIDE]  	 = new float [] {0.1f, 0.5f,   1f, 0.5f};
			widthTM[WIDTH_VERY_WIDE] = new float [] {  0f, 0.5f,   1f, 0.3f};
		} else if (type == StreamType.NARROW) {
			widthTM[WIDTH_NARROW]  	 = new float [] {0.8f,   1f, 0.3f, 0.1f};
			widthTM[WIDTH_NORMAL]    = new float [] {  1f,   1f, 0.3f, 0.1f};
			widthTM[WIDTH_WIDE]  	 = new float [] {0.7f,   1f, 0.2f, 0.2f};
			widthTM[WIDTH_VERY_WIDE] = new float [] {  1f,   1f,   1f, 0.1f};
		}
		
		wrandTM = new float[4][4];
		wrandTM[WRAND_NONE]		 = new float [] {  1f,   1f, 0.1f, 0.1f};
		wrandTM[WRAND_LOW]		 = new float [] {  1f,   1f,   1f, 0.1f};
		wrandTM[WRAND_MEDIUM]	 = new float [] {0.1f,   1f, 0.5f, 0.2f};
		wrandTM[WRAND_HIGH]		 = new float [] {0.1f, 0.1f,   1f, 0.1f};
		
		wchangeTM = new float[3][3];
		wchangeTM[WCHANGE_SLOW]		 = new float [] {  1f,   1f, 0.1f};
		wchangeTM[WCHANGE_MEDIUM]	 = new float [] {  1f,   1f,   1f};
		wchangeTM[WCHANGE_FAST]		 = new float [] {0.1f,   1f,   1f};
		
		fluctTM = new float[4][4];
		fluctTM[FLUCT_NONE]		 = new float [] {  1f,   1f, 0.1f, 0.1f};
		fluctTM[FLUCT_LOW]		 = new float [] {  1f,   1f,   1f, 0.1f};
		fluctTM[FLUCT_MEDIUM]	 = new float [] {0.1f,   1f, 0.5f, 0.2f};
		fluctTM[FLUCT_HIGH]		 = new float [] {0.1f, 0.1f,   1f, 0.1f};
	}
	
	
	public static class SlipstreamSection {
		public Vector2f from = new Vector2f();
		public Vector2f to = new Vector2f();
		public Vector2f control = new Vector2f();
		
		public int width;
		public int wrand;
		public int wchange;
		public int fluct;
		
		public boolean subdivision = false;
		public float approxCurveLength;
	}
	
	public static class AddedSegment {
		public SlipstreamSegment segment;
		public Vector2f dir;
		public Vector2f perp;
	}
	
	protected float [][] widthTM; 
	protected float [][] wrandTM; 
	protected float [][] wchangeTM; 
	protected float [][] fluctTM;
	
	protected Random random;
	protected SlipstreamParams2 params;
	protected SlipstreamTerrainPlugin2 plugin;
	protected List<SlipstreamSection> sections = new ArrayList<SlipstreamSection>();
	
	protected int currWidth = 0;
	protected int currWRand = 0;
	protected int currWChange = 0;
	protected int currFluct = 0;
	
	protected Vector2f start;
	protected StreamType type;
	
	public SlipstreamBuilder(Vector2f start, SlipstreamTerrainPlugin2 plugin, StreamType type, Random random) {
		this.plugin = plugin;
		this.type = type != null ? type : StreamType.NORMAL;
		this.params = plugin.getParams();
		
		if (random == null) random = Misc.random;
		this.random = random;
		this.start = start;
		initTransitionMatrices();
		for (int i = 0; i < 10; i++) {
			pickAllNext();
		}
	}
	
	protected float maxAngleVarianceForCurve = 30f;
	protected float maxAngleVariance = 60f;
	public float getMaxAngleVariance() {
		return maxAngleVariance;
	}

	public void setMaxAngleVariance(float maxAngleVariance) {
		this.maxAngleVariance = maxAngleVariance;
	}
	public float getMaxAngleVarianceForCurve() {
		return maxAngleVarianceForCurve;
	}
	public void setMaxAngleVarianceForCurve(float maxAngleVarianceForCurve) {
		this.maxAngleVarianceForCurve = maxAngleVarianceForCurve;
	}

	public void buildToDestination(Vector2f control, Vector2f control2, Vector2f to) {
		Vector2f p0 = new Vector2f(start);
		Vector2f p1 = new Vector2f(control);
		Vector2f p2 = new Vector2f(control2);
		Vector2f p3 = new Vector2f(to);
		
		float len = getApproximateBezierLength(p0, p1, p2, p3);
		
		float t = 0f;
		Vector2f prev = new Vector2f(p0);
		while (true) {
			float length = 3000f + random.nextInt(2000);
			float tIncr = length / len;
			t += tIncr;
			if (t > 1f) t = 1f;
			
			Vector2f loc = Misc.bezierCubic(p0, p1, p2, p3, t);
			boolean wide = currWidth == WIDTH_WIDE || currWidth == WIDTH_VERY_WIDE;
			pickAllNext();
			wide |= currWidth == WIDTH_WIDE || currWidth == WIDTH_VERY_WIDE;
			float angle = random.nextFloat() * maxAngleVarianceForCurve - maxAngleVarianceForCurve/2f;
			if (wide) angle *= 0.5f;
			if (t >= 1f) angle = 0f;
			
			loc = Misc.rotateAroundOrigin(loc, angle, prev);
			float actualDist = Misc.getDistance(prev, loc);
			if (actualDist > length * 0.5f) { 
				prev.set(loc);
				addSection(new Vector2f(loc), true);
			}
			
			if (t >= 1f) break;
		}
		
		generate();
	}
	
	public void buildToDestination(Vector2f control, Vector2f to) {
		Vector2f p0 = new Vector2f(start);
		Vector2f p1 = new Vector2f(control);
		Vector2f p2 = new Vector2f(to);
		
		float len = getApproximateBezierLength(p0, p1, p2);
		
		float t = 0f;
		Vector2f prev = new Vector2f(p0);
		while (true) {
			float length = 3000f + random.nextInt(2000);
			float tIncr = length / len;
			t += tIncr;
			if (t > 1f) t = 1f;
			
			Vector2f loc = Misc.bezier(p0, p1, p2, t);
			boolean wide = currWidth == WIDTH_WIDE || currWidth == WIDTH_VERY_WIDE;
			pickAllNext();
			wide |= currWidth == WIDTH_WIDE || currWidth == WIDTH_VERY_WIDE;
			
			float angle = random.nextFloat() * maxAngleVarianceForCurve - maxAngleVarianceForCurve/2f;
			if (wide) angle *= 0.5f;
			if (t >= 1f) angle = 0f;
			
			loc = Misc.rotateAroundOrigin(loc, angle, prev);
			float actualDist = Misc.getDistance(prev, loc);
			if (actualDist > length * 0.5f) { 
				prev.set(loc);
				addSection(new Vector2f(loc), true);
			}
			
			if (t >= 1f) break;
		}
		
		generate();
	}
	
	public void buildToDestination(Vector2f to) {
		Vector2f loc = new Vector2f(start);
		Vector2f p0 = new Vector2f(loc);
		Vector2f p1 = new Vector2f(to);
		
		float len = Misc.getDistance(p0, p1);
		Vector2f dir = Misc.getUnitVector(p0, p1);
		float dirAngle = Misc.getAngleInDegrees(dir);
		
		float distSoFar = 0f;
		float prevAngle = 0f;
		for (int i = 0; distSoFar < len; i++) {
			float angle = random.nextFloat() * maxAngleVariance - maxAngleVariance/2f;

			boolean wide = currWidth == WIDTH_WIDE || currWidth == WIDTH_VERY_WIDE;
			pickAllNext();
			wide |= currWidth == WIDTH_WIDE || currWidth == WIDTH_VERY_WIDE;
			//if (wide) angle *= 0.5f;
			
			angle += dirAngle;
			if (i % 2 == 1) angle = prevAngle;
			prevAngle = angle;
			//if (i == 0) angle = 90f;
			Vector2f add = Misc.getUnitVectorAtDegreeAngle(angle);
			float length = 3000f + random.nextInt(2000);
			distSoFar += length;
			//length *= 0.25f;
			add.scale(length);
			Vector2f.add(loc, add, loc);
			addSection(new Vector2f(loc), true);
		}
		Vector2f end = sections.get(sections.size() - 1).to;
		
		
		float actualAngle = Misc.getAngleInDegrees(p0, end);
		float angleDiff = Misc.getAngleDiff(dirAngle, actualAngle);
		float turnDir = Misc.getClosestTurnDirection(actualAngle, dirAngle);
		
		for (SlipstreamSection section : sections) {
			section.from = Misc.rotateAroundOrigin(section.from, angleDiff * turnDir, p0);
			section.to = Misc.rotateAroundOrigin(section.to, angleDiff * turnDir, p0);
		}
		
		end = sections.get(sections.size() - 1).to;
		float actualDist = Misc.getDistance(p0, end);
		float distToAdd = len - actualDist;
		
		//System.out.println("Distance short: " + distToAdd);
		if (distToAdd > 1000) {
			angleDiff = Misc.getAngleDiff(dirAngle, prevAngle);
			turnDir = Misc.getClosestTurnDirection(actualAngle, dirAngle);
			float turnAmount = Math.min(30f, angleDiff) * turnDir;
			//float turnAmount = angleDiff * turnDir;
			
			Vector2f add = Misc.getUnitVectorAtDegreeAngle(prevAngle + turnAmount);
			float length = distToAdd;
			add.scale(length);
			loc = Vector2f.add(end, add, loc);
			addSection(new Vector2f(loc));
		}
		
		generate();
	}
	
	
	protected float [] buildNoise;
	public void buildTest() {
		
		if (false) {
			Vector2f loc = new Vector2f(start);
			
			Vector2f p0 = loc;
			Vector2f p1 = new Vector2f(loc);
			float w = 15000;
			float h = 15000;
			h = 0f;
			w = 25000f;
			w = 20000f;
			p1.x += w;
			p1.y += h;
			float len = Misc.getDistance(p0, p1);
			Vector2f dir = Misc.getUnitVector(p0, p1);
			Vector2f perp = new Vector2f(-dir.y, dir.x);
			
			float averageSection = 4000;
			
			int numNoisePoints = 32;
			while (numNoisePoints < len / averageSection) {
				numNoisePoints *= 2f;
			}
			if (numNoisePoints > 2048) numNoisePoints = 2048;
			float spikes = 0.75f;
			float [] noise = initNoise1D(random, numNoisePoints, spikes); 
			noise[0] = 0.5f;
			noise[noise.length - 1] = 0.5f;
			genNoise1D(random, noise, numNoisePoints, spikes);
			normalizeNoise1D(noise);
			buildNoise = noise;
			
			float startLength = 1500 + random.nextFloat() * 1500f;
			float endLength = 1500 + random.nextFloat() * 1500f;
			Vector2f curr = new Vector2f(dir);
			curr.scale(startLength);
			Vector2f.add(curr, p0, curr);
			addSection(curr);
			
			
			float mid = len - startLength - endLength;
			float remaining = mid;
			float distSoFar = startLength;
			
			//float maxMag = 500f + mid * 0.025f + random.nextFloat() * (2000f + mid * 0.05f);
			//float maxMag = 500f + mid * 0.025f + 1f * (2000f + mid * 0.05f);
			float maxMag = 500f + mid * 0.01f + 1f * (1000f + mid * 0.025f);
			
			while (remaining > 0) {
				float segLen = 3000f + random.nextFloat() * 2000f;
				segLen = Math.min(segLen, remaining);
				remaining -= segLen;
				if (remaining < segLen * 0.5f) {
					segLen += remaining;
					remaining = 0f;
				}
				distSoFar += segLen;
				
				float t = (distSoFar - startLength) / mid;
				float n = getInterpNoise(noise, t) - 0.5f;
				
				if (t < 0.25f) {
					n *= t / 0.25f;
				} else if (t > 0.75f) {
					n *= (1f - t) / 0.25f;
				}
				
				n *= 2f;
				
				curr = new Vector2f(dir);
				curr.scale(distSoFar);
				Vector2f.add(curr, p0, curr);
				curr.x += perp.x * maxMag * n;
				curr.y += perp.y * maxMag * n;
				addSection(curr);
			}
			
			addSection(p1);
			
			generate();
			return;
		}
		
		if (true) {
			Vector2f loc = new Vector2f(start);
			Vector2f p0 = new Vector2f(loc);
			Vector2f p1 = new Vector2f(loc);
			float w = 15000;
			float h = 15000;
			h = 0f;
			w = 25000f;
			//w = 80000f;
			w = 50000f;
//			h = 50000f;
//			w = 164000f;
//			h = 104000f;
			p1.x += w;
			p1.y += h;
			float len = Misc.getDistance(p0, p1);
			Vector2f dir = Misc.getUnitVector(p0, p1);
			Vector2f perp = new Vector2f(-dir.y, dir.x);
			float dirAngle = Misc.getAngleInDegrees(dir);
			
			float distSoFar = 0f;
			float prevAngle = 0f;
			for (int i = 0; distSoFar < len; i++) {
				float angle = random.nextFloat() * 60f - 30f;
				//float angle = StarSystemGenerator.getNormalRandom(random, -30f, 30f);
				angle += dirAngle;
				if (i % 2 == 1) angle = prevAngle;
				prevAngle = angle;
				//if (i == 0) angle = 90f;
				Vector2f add = Misc.getUnitVectorAtDegreeAngle(angle);
				float length = 3000f + random.nextInt(2000);
				distSoFar += length;
				//length *= 0.25f;
				add.scale(length);
				Vector2f.add(loc, add, loc);
				addSection(new Vector2f(loc));
			}
			Vector2f end = sections.get(sections.size() - 1).to;
			
			
			float actualAngle = Misc.getAngleInDegrees(p0, end);
			float angleDiff = Misc.getAngleDiff(dirAngle, actualAngle);
			float turnDir = Misc.getClosestTurnDirection(actualAngle, dirAngle);
			
			for (SlipstreamSection section : sections) {
				section.from = Misc.rotateAroundOrigin(section.from, angleDiff * turnDir, p0);
				section.to = Misc.rotateAroundOrigin(section.to, angleDiff * turnDir, p0);
			}
			
			end = sections.get(sections.size() - 1).to;
			float actualDist = Misc.getDistance(p0, end);
			float distToAdd = len - actualDist;
			//System.out.println("Distance short: " + distToAdd);
			if (distToAdd > 1000) {
				angleDiff = Misc.getAngleDiff(dirAngle, prevAngle);
				turnDir = Misc.getClosestTurnDirection(actualAngle, dirAngle);
				float turnAmount = Math.min(30f, angleDiff) * turnDir;
				//float turnAmount = angleDiff * turnDir;
				
				Vector2f add = Misc.getUnitVectorAtDegreeAngle(prevAngle + turnAmount);
				float length = distToAdd;
				add.scale(length);
				loc = Vector2f.add(end, add, loc);
				addSection(new Vector2f(loc));
			}
			
			generate();
					
			//System.out.println("Segments: " + plugin.getSegments().size());
			return;
		}

		if (true) {
			Vector2f loc = new Vector2f(start);
			float prevAngle = 0f;
			for (int i = 0; i < 20; i++) {
				float angle = random.nextFloat() * 60f - 30f;
				if (i % 2 == 1) angle = prevAngle;
				prevAngle = angle;
				//if (i == 0) angle = 90f;
				Vector2f add = Misc.getUnitVectorAtDegreeAngle(angle);
				float length = 3000f + random.nextInt(2000);
				//length *= 0.25f;
				add.scale(length);
				Vector2f.add(loc, add, loc);
				addSection(new Vector2f(loc));
			}
			generate();
			return;
		}
		
		if (true) {
			Vector2f loc = new Vector2f(start);
			
			Vector2f p0 = loc;
			Vector2f p1 = new Vector2f(loc);
			float w = 15000;
			float h = 15000;
			h = 0f;
			w = 25000f;
			w = 80000f;
			p1.x += w;
			p1.y += h;
			float len = Misc.getDistance(p0, p1);
			Vector2f dir = Misc.getUnitVector(p0, p1);
			Vector2f perp = new Vector2f(-dir.y, dir.x);
			
			//System.out.println("LEN: " + len);
			
			if (len <= 7000f) {
				addSection(p1);
				generate();
				return;
			}
			if (len <= 15000f) {
				float midLength = len * (0.4f + random.nextFloat() * 0.2f);
				
				Vector2f curr = new Vector2f(dir);
				curr.scale(1000f + random.nextFloat() * 1000f);
				Vector2f.add(curr, p0, curr);
				addSection(new Vector2f(curr));
				
				curr = new Vector2f(dir);
				curr.scale(midLength);
				
				float mag = 0f + random.nextFloat() * 1000f + len * 0.05f;
				if (random.nextFloat() < 0.75f) {
					if (random.nextBoolean()) {
						perp.negate();
					}
					curr.x += perp.x * mag;
					curr.y += perp.y * mag;
				}
				
				Vector2f.add(curr, p0, curr);
				addSection(curr);
				
				float endLength = 1000f + random.nextFloat() * 1000f;
				curr = new Vector2f(dir);
				curr.scale(len - endLength);
				Vector2f.add(curr, p0, curr);
				addSection(curr);
				
				addSection(p1);
				
				generate();
				return;
			}
			
			
			
			float startLength = 1500 + random.nextFloat() * 1500f;
			float endLength = 1500 + random.nextFloat() * 1500f;
			Vector2f curr = new Vector2f(dir);
			curr.scale(startLength);
			Vector2f.add(curr, p0, curr);
			addSection(curr);
			
			float remaining = len - startLength - endLength;
			float distSoFar = startLength;
			while (remaining > 0) {
				float lengthForArc = 10000f + random.nextFloat() * 20000f;
				lengthForArc = Math.min(lengthForArc, remaining);
				remaining -= lengthForArc;
				if (remaining < 10000f) {
					lengthForArc += remaining;
					remaining = 0f;
				}
				
				if (random.nextBoolean()) {
					perp.negate();
				}
				
				float maxMag = 500f + random.nextFloat() * (2000f + lengthForArc * 0.1f);
				maxMag = 4000; 
				float t = 0f;
				while (t < 1f) {
					float perIter = 3000f + random.nextFloat() * 2000f;
					float tForIter = perIter / lengthForArc;
					t += tForIter;
					if (t > 1f - tForIter * 0.5f) {
						tForIter += 1f - t;
						t = 1f;
					}
					perIter = tForIter * lengthForArc;
					distSoFar += perIter;
					
					float mag = getFluctuationFunc(t) * maxMag;
					curr = new Vector2f(dir);
					curr.scale(distSoFar);
					Vector2f.add(curr, p0, curr);
					curr.x += perp.x * mag;
					curr.y += perp.y * mag;
					addSection(new Vector2f(curr));
				}
			}
			
			addSection(p1);
			generate();
			return;
		}
		
		
		if (false) {
			Vector2f loc = new Vector2f(start);
			
			Vector2f p0 = loc;
			Vector2f p3 = new Vector2f(loc);
			float w = 30000;
			float h = 30000;
			p3.x += w;
			p3.y += h;
			float len = Misc.getDistance(p0, p3);
			
//			Vector2f p1 = new Vector2f(p0);
//			Vector2f p2 = new Vector2f(p3);
			
			Vector2f p1 = Vector2f.add(p0, p3, new Vector2f());
			p1.scale(0.5f);
			Vector2f p2 = new Vector2f(p1);
			
			p1.x -= len * 0.2f;
			p1.y += len * 0.2f;
			p2.x += len * 0.2f;
			p2.y -= len * 0.2f;
			
			float dist1 = len * 0.5f + random.nextFloat() * len * 0.5f;
			float dist2 = len * 0.5f + random.nextFloat() * len * 0.5f;
			
			p1 = Misc.getPointAtRadius(p0, dist1, random);
			p2 = Misc.getPointAtRadius(p3, dist2, random);
			
			p1 = new Vector2f(p0.x, p3.y);
			p2 = new Vector2f(p3.x, p0.y);
			
			if (random.nextBoolean()) {
				Vector2f temp = p1;
				p1 = p2;
				p2 = temp;
			}
			
			float veryApproxPathLength = len * 1.5f;
			float tPerIter = 8000f / (veryApproxPathLength + 1f);
			
			float t = 0f;
			while (t < 1f) {
				t += tPerIter * (0.8f + 0.4f * random.nextFloat());
				if (t > 1f - tPerIter * 0.5f) t = 1f;
				
				Vector2f curr = Misc.bezierCubic(p0, p1, p2, p3, t); 
				addSection(new Vector2f(curr));
			}
			generate();
			return;
		}
		
		if (false) {
			Vector2f loc = new Vector2f(start);
			
			Vector2f p0 = loc;
			Vector2f p2 = new Vector2f(loc);
			float w = 30000;
			float h = 30000;
			p2.x += w;
			p2.y += h;
			float len = Misc.getDistance(p0, p2);
			
			Vector2f p1 = Vector2f.add(p0, p2, new Vector2f());
			p1.scale(0.5f);
			p1.x -= len * 0.25f;
			p1.y += len * 0.25f;
			
			float approxPathLength = getApproximateBezierLength(p0, p1, p2);
			float tPerIter = 8000f / (approxPathLength + 1f);
			
			float t = 0f;
			while (t < 1f) {
				t += tPerIter * (0.8f + 0.4f * random.nextFloat());
				if (t > 1f - tPerIter * 0.5f) t = 1f;
				
				Vector2f curr = Misc.bezier(p0, p1, p2, t); 
				addSection(new Vector2f(curr));
			}
			generate();
			return;
		}
		
		
		if (false) {
			Vector2f loc = new Vector2f(start);
			loc.x += 5000;
			addSection(loc, 1, 0, 0, 3);
			loc.x += 5000;
			addSection(loc, 1, 0, 0, 3);
			loc.x += 5000;
			addSection(loc, 1, 0, 0, 3);
			loc.x += 5000;
			addSection(loc, 1, 0, 0, 3);
			
			loc.x += 5000;
			loc.y += 5000;
			addSection(loc, 0, 0, 0, 3);
			loc.y += 2000;
			addSection(loc, 0, 0, 0, 3);
			loc.x -= 5000;
			loc.y += 5000;
			addSection(loc, 0, 0, 0, 3);
			loc.x -= 5000;
			addSection(loc, 0, 0, 0, 3);
			generate();
			return;
		}
		
		Vector2f loc = new Vector2f(start);
		float prevAngle = 0f;
		for (int i = 0; i < 20; i++) {
			float angle = random.nextFloat() * 60f - 30f;
			if (i % 2 == 1) angle = prevAngle;
			prevAngle = angle;
			//if (i == 0) angle = 90f;
			Vector2f add = Misc.getUnitVectorAtDegreeAngle(angle);
			float length = 3000f + random.nextInt(2000);
			//length *= 0.25f;
			add.scale(length);
			Vector2f.add(loc, add, loc);
			addSection(new Vector2f(loc));
		}
		
		generate();
	}
	
	public void generate() {
		
//		MIN_SPACING = 200f;
//		MAX_SPACING = 400f;
//		WIDTH_TO_SPACING_MULT = 0.2f;
//		MIN_SPACING = 300f;
//		MAX_SPACING = 600f;
//		WIDTH_TO_SPACING_MULT = 0.4f;
//		long seed = 23895464576452L + 4384357483229348234L;
//		seed = 1181783497276652981L ^ seed;
//		this.random = new Random(seed);
		
		computeControlPoints();
		buildStream();
	}
	
	
	public void buildStream() {
		plugin.setBuilder(this);
		if (sections.isEmpty()) return;
		
		
		float totalCurveLength = 0f;
		for (SlipstreamSection curr : sections) {
			totalCurveLength += curr.approxCurveLength;
		}
		
		int numNoisePoints = 32;
		while (numNoisePoints * (MIN_SPACING + MAX_SPACING) / 2f < totalCurveLength) {
			numNoisePoints *= 2f;
		}
		if (numNoisePoints > 2048) numNoisePoints = 2048;
		
		float spikes = 0.67f;
		float [] noiseForWidth = initNoise1D(random, numNoisePoints, spikes); 
		noiseForWidth[0] = 0.5f;
		noiseForWidth[noiseForWidth.length - 1] = 0.5f;
		genNoise1D(random, noiseForWidth, numNoisePoints, spikes);
		normalizeNoise1D(noiseForWidth);
		
		float width = getGoalWidth(sections.get(0));
		//width *= 3f;
		//width *= .5f;
		
		
		float curveLengthSoFar = 0f;
		for (SlipstreamSection curr : sections) {
			//curr.fluct = FLUCT_NONE;
			//curr.fluct = FLUCT_LOW;
			//curr.fluct = FLUCT_MEDIUM;
			//curr.fluct = FLUCT_HIGH;
			
			float startingWidth = width;
			float goalWidth = getGoalWidth(curr);
			float changeRate = getWChangeMult(curr);
			float wrandMult = getWRandMult(curr);
			float fluctMult = getFluctMult(curr);
			
			
			float desiredSpacing = Math.max(MIN_SPACING, width * WIDTH_TO_SPACING_MULT);
			if (desiredSpacing > MAX_SPACING) desiredSpacing = MAX_SPACING;
			
			int segments = (int) (curr.approxCurveLength / desiredSpacing);
			if (segments < 2) segments = 2;
			float spacing = curr.approxCurveLength / segments;
			
			List<AddedSegment> added = new ArrayList<AddedSegment>(); 
			
			int startIndex = 1;
			if (getPrev(curr) == null) startIndex = 0;
			for (int i = startIndex; i < segments; i++) {
				float f = (float)i / ((float)segments - 1f);
				float f2 = (float)(i + 0.1f) / ((float)segments - 1f);
				
				width = Misc.interpolate(startingWidth, goalWidth, Math.min(1f, f * f * changeRate));
				float t = (curveLengthSoFar + f * curr.approxCurveLength) / totalCurveLength;
				float wNoise = (getInterpNoise(noiseForWidth, t) - 0.5f) * 2f;
				wNoise *= wrandMult;
				//wNoise *= 3f;
				width *= (1f + wNoise);
				
				Vector2f loc = Misc.bezier(curr.from, curr.control, curr.to, f);
				Vector2f loc2 = Misc.bezier(curr.from, curr.control, curr.to, f2);
				Vector2f dir = Vector2f.sub(loc2, loc, new Vector2f());
				Misc.normalise(dir);
				Vector2f perp = new Vector2f(-dir.y, dir.x);
				
				plugin.addSegment(loc, width);
				AddedSegment seg = new AddedSegment();
				seg.segment = plugin.getSegments().get(plugin.getSegments().size() - 1);
				seg.dir = new Vector2f(dir);
				seg.perp = new Vector2f(perp);
				added.add(seg);
			}
			
			float distPerFluct = FLUCT_LENGTH_MIN + random.nextFloat() * (FLUCT_LENGTH_MAX - FLUCT_LENGTH_MIN);
			float fluctAmount = FLUCT_MAG_MIN + random.nextFloat() * (FLUCT_MAG_MAX - FLUCT_MAG_MIN);
			float fluctDir = Math.signum(random.nextFloat() - 0.5f);
			float distSoFar = 0f;
			List<AddedSegment> temp = new ArrayList<AddedSegment>();
			AddedSegment prev = null;
			for (AddedSegment seg : added) {
				if (prev != null) {
					distSoFar += Misc.getDistance(seg.segment.loc, prev.segment.loc);
				}
				temp.add(seg);
				prev = seg;
				
				if (distSoFar > distPerFluct && temp.size() >= 4) {
					for (int i = 0; i < temp.size(); i++) {
						float t = i / (temp.size() - 1f);
						AddedSegment seg2 = temp.get(i);
						
						float fluctMag = getFluctuationFunc(t);
						fluctMag *= fluctMult;
						//fluctDir = 1f;
						
						fluctMag *= fluctAmount * fluctDir;
						
						seg2.segment.loc.x += seg2.perp.x * fluctMag;
						seg2.segment.loc.y += seg2.perp.y * fluctMag;

					}
					temp.clear();
					distSoFar = 0f;
					distPerFluct = FLUCT_LENGTH_MIN + random.nextFloat() * (FLUCT_LENGTH_MAX - FLUCT_LENGTH_MIN);
					fluctAmount = FLUCT_MAG_MIN + random.nextFloat() * (FLUCT_MAG_MAX - FLUCT_MAG_MIN);
					fluctDir = Math.signum(random.nextFloat() - 0.5f);
				}
			}
			
			curveLengthSoFar += curr.approxCurveLength;
		}
		
		adjustSharpInflectionPoints();
		
		float fadeDist = 500f;
		float distSoFar = 0f;
		SlipstreamSegment prev = null;
		for (int i = 0; i < plugin.getSegments().size(); i++) {
			SlipstreamSegment curr = plugin.getSegments().get(i);
			if (prev != null) {
				distSoFar += Misc.getDistance(prev.loc, curr.loc);
			}
			if (distSoFar >= fadeDist) {
				break;
			}
			
			float b = distSoFar / fadeDist;
			if (b < 0f) b = 0f;
			if (b > 1f) b = 1f;
			curr.bMult = b;
			prev = curr;
		}
		
		distSoFar = 0f;
		prev = null;
		for (int i = plugin.getSegments().size() - 1; i >= 0; i--) {
			SlipstreamSegment curr = plugin.getSegments().get(i);
			if (prev != null) {
				distSoFar += Misc.getDistance(prev.loc, curr.loc);
			}
			if (distSoFar >= fadeDist) {
				break;
			}
			
			float b = distSoFar / fadeDist;
			if (b < 0f) b = 0f;
			if (b > 1f) b = 1f;
			curr.bMult = b;
			prev = curr;
		}
	}
	
	protected float getFluctuationFunc(float t) {
		float pi = (float) Math.PI;
		return ((float)Math.cos(pi + 2f * pi * t) + 1f) * 0.5f;
	}
	
	protected void adjustSharpInflectionPoints() {
		for (int i = 1; i < plugin.getSegments().size() - 1; i++) {
			SlipstreamSegment prev = plugin.getSegments().get(i - 1);			
			SlipstreamSegment curr = plugin.getSegments().get(i);			
			SlipstreamSegment next = plugin.getSegments().get(i + 1);
			
			float dir1 = Misc.getAngleInDegrees(prev.loc, curr.loc);
			float dir2 = Misc.getAngleInDegrees(curr.loc, next.loc);
			float diff = Misc.getAngleDiff(dir1, dir2);
			if (diff > 5f) {
				Vector2f avg = Vector2f.add(prev.loc, next.loc, new Vector2f());
				avg.scale(0.5f);
				curr.loc.set(Misc.interpolateVector(curr.loc, avg, 0.67f));
				//i++;
			}
		}
	}
	
	protected float getWRandMult(SlipstreamSection curr) {
		float mult = 0f;
		if (curr.wrand == WRAND_NONE) {
			mult = WRAND_NONE_MULT;
		} else if (curr.wrand == WRAND_LOW) {
			mult = WRAND_LOW_MULT;
		} else if (curr.wrand == WRAND_MEDIUM) {
			mult = WRAND_MEDIUM_MULT;
		} else if (curr.wrand == WRAND_HIGH) {
			mult = WRAND_HIGH_MULT;
		}
		//mult *= 0.9f + 0.2f * random.nextFloat(); 
		return mult;
	}
	
	protected float getFluctMult(SlipstreamSection curr) {
		float mult = 0f;
		if (curr.fluct == FLUCT_NONE) {
			mult = FLUCT_NONE_MULT;
		} else if (curr.fluct == WRAND_LOW) {
			mult = FLUCT_LOW_MULT;
		} else if (curr.fluct == FLUCT_MEDIUM) {
			mult = FLUCT_MEDIUM_MULT;
		} else if (curr.fluct == FLUCT_HIGH) {
			mult = FLUCT_HIGH_MULT;
		}
		mult *= 0.9f + 0.2f * random.nextFloat();
		return mult;
	}
	
	protected float getWChangeMult(SlipstreamSection curr) {
		float mult = 1f;
		if (curr.wchange == WCHANGE_SLOW) {
			mult = WCHANGE_SLOW_T_MULT_MIN + random.nextFloat() * (WCHANGE_SLOW_T_MULT_MIN_MAX - WCHANGE_SLOW_T_MULT_MIN);
		} else if (curr.wchange == WCHANGE_MEDIUM) {
			mult = WCHANGE_MEDIUM_T_MULT_MIN + random.nextFloat() * (WCHANGE_MEDIUM_T_MULT_MAX - WCHANGE_MEDIUM_T_MULT_MIN);
		} else if (curr.wchange == WCHANGE_FAST) {
			mult = WCHANGE_FAST_T_MULT_MIN + random.nextFloat() * (WCHANGE_FAST_T_MULT_MAX - WCHANGE_FAST_T_MULT_MIN);
		}
		return mult;
	}
	
	protected float getGoalWidth(SlipstreamSection curr) {
		float goalWidth = params.baseWidth;
		float mult = 1f;
		if (curr.width == WIDTH_NARROW) {
			mult = MIN_NARROW + random.nextFloat() * (MAX_NARROW - MIN_NARROW);
		} else if (curr.width == WIDTH_NORMAL) {
			mult = MIN_NORMAL + random.nextFloat() * (MAX_NORMAL - MIN_NORMAL);
		} else if (curr.width == WIDTH_WIDE) {
			mult = MIN_WIDE + random.nextFloat() * (MAX_WIDE - MIN_WIDE);
		} else if (curr.width == WIDTH_VERY_WIDE) {
			mult = MIN_VERY_WIDE + random.nextFloat() * (MAX_VERY_WIDE - MIN_VERY_WIDE);
		} 
		return goalWidth * mult;
	}
	
	protected void computeControlPoints() {
		float angleLimit = 30f;
		for (SlipstreamSection curr : sections) {
			SlipstreamSection prev = getPrev(curr);
			SlipstreamSection next = getNext(curr);

			
			if (prev == null && next == null) {
				curr.control = Vector2f.add(curr.from, curr.to, new Vector2f());
				curr.control.scale(0.5f);
			} else if (prev == null && next != null) {
				Vector2f p1 = curr.from;
				Vector2f p2 = curr.to;
				Vector2f p3 = next.to;
				float angleP2ToControl = Misc.getAngleInDegrees(p3, p2);
				Vector2f dirP2ToControl = Misc.getUnitVectorAtDegreeAngle(angleP2ToControl);
				Vector2f p2ToP1 = Vector2f.sub(p1, p2, new Vector2f());
				float angleP2toP1 = Misc.getAngleInDegrees(p2ToP1);
				float angleToControlAndP2toP1 = Vector2f.angle(dirP2ToControl, p2ToP1) * Misc.DEG_PER_RAD;
				if (angleToControlAndP2toP1 > angleLimit) {
					angleToControlAndP2toP1 = angleLimit;
					float turnDir = Misc.getClosestTurnDirection(angleP2toP1, angleP2ToControl);
					angleP2ToControl = angleP2toP1 + turnDir * angleToControlAndP2toP1;
					dirP2ToControl = Misc.getUnitVectorAtDegreeAngle(angleP2ToControl);
				}
				float dist = Misc.getDistance(p1, p2);
				dist /= 2f;
				float h = dist * (float) Math.tan(angleToControlAndP2toP1 * Misc.RAD_PER_DEG);
				float b = (float) Math.sqrt(dist * dist + h * h);
				dirP2ToControl.scale(b);
				Vector2f.add(dirP2ToControl, p2, curr.control);
			} else {
				Vector2f p1 = prev.control;
				Vector2f p2 = curr.from;
				Vector2f p3 = curr.to;
				float angleP2ToControl = Misc.getAngleInDegrees(p1, p2);
				Vector2f dirP2ToControl = Misc.getUnitVectorAtDegreeAngle(angleP2ToControl);
				Vector2f p2ToP3 = Vector2f.sub(p3, p2, new Vector2f());
				float angleP2ToP3 = Misc.getAngleInDegrees(p2ToP3);
				float angleToControlAndP2toP3 = Vector2f.angle(dirP2ToControl, p2ToP3) * Misc.DEG_PER_RAD;
				if (angleToControlAndP2toP3 > angleLimit) {
					angleToControlAndP2toP3 = angleLimit;
					float turnDir = Misc.getClosestTurnDirection(angleP2ToP3, angleP2ToControl);
					angleP2ToControl = angleP2ToP3 + turnDir * angleToControlAndP2toP3;
					dirP2ToControl = Misc.getUnitVectorAtDegreeAngle(angleP2ToControl);
				}
				
				float dist = Misc.getDistance(p2, p3);
				dist /= 2f;
				float h = dist * (float) Math.tan(angleToControlAndP2toP3 * Misc.RAD_PER_DEG);
				float b = (float) Math.sqrt(dist * dist + h * h);
				dirP2ToControl.scale(b);
				Vector2f.add(dirP2ToControl, p2, curr.control);
			}
			
			curr.approxCurveLength = getApproximateBezierLength(curr.from, curr.control, curr.to);
		}
	}
	
	public void addSection(Vector2f to) {
		addSection(to, false);
	}
	public void addSection(Vector2f to, boolean alreadyPicked) {
		if (!alreadyPicked) {
			pickAllNext();
		}
		addSection(to, currWidth, currWRand, currWChange, currFluct);
	}
	
	public void addSection(Vector2f to, int width, int wrand, int wchange, int fluct) {
		addSection(to, width, wrand, wchange, fluct, -1);
	}
	
	public void addSection(Vector2f to, int width, int wrand, int wchange, int fluct, int insertIndex) {
		SlipstreamSection s = new SlipstreamSection();
		s.to.set(to);
		s.width = width;
		s.wrand = wrand;
		s.wchange = wchange;
		s.fluct = fluct;
		
		if (insertIndex < 0) {
			sections.add(s);
		} else {
			sections.add(insertIndex, s);
		}
		
		SlipstreamSection p = getPrev(s);
		if (p != null) {
			s.from.set(p.to);
		} else {
			s.from.set(start);
		}
	}
	
	
	public SlipstreamSection getPrev(SlipstreamSection s) {
		int index = sections.indexOf(s);
		if (index < 1) return null;
		return sections.get(index - 1);
	}
	public SlipstreamSection getNext(SlipstreamSection s) {
		int index = sections.indexOf(s);
		if (index < 0 || index >= sections.size() - 1) return null;
		return sections.get(index + 1);
	}
	
	public void pickAllNext() {
		currWidth = pickWidth(currWidth);
		currWRand = pickWRand(currWRand);
		currWChange = pickWChange(currWChange);
		currFluct = pickFluct(currFluct);
		
		if (currWidth == WIDTH_NARROW) {
			if (currFluct == FLUCT_HIGH) {
				currFluct = FLUCT_LOW;
			} else if (currFluct == FLUCT_MEDIUM) {
				currFluct = FLUCT_NONE;
			}
		}
	}
	
	public int pickWidth(int curr) {
		return pickNext(curr, widthTM);
	}
	
	public int pickWRand(int curr) {
		return pickNext(curr, wrandTM);
	}
	
	public int pickWChange(int curr) {
		return pickNext(curr, wchangeTM);
	}
	
	public int pickFluct(int curr) {
		return pickNext(curr, fluctTM);
	}
	
	public int pickNext(int curr, float [][] matrix) {
		float [] weights = matrix[curr];
		WeightedRandomPicker<Integer> picker = new WeightedRandomPicker<Integer>(random);
		for (int i = 0; i < weights.length; i++) {
			picker.add(i, weights[i]);
		}
		return picker.pick();
	}
	
	
	public void renderDebug(float alpha) {
		//if (true) return;
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
//		float scale = 0.25f;
//		//scale = 1f;
//		GL11.glPushMatrix();
//		GL11.glTranslatef(sections.get(0).from.x, sections.get(0).from.y, 0f);
//		GL11.glScalef(scale, scale, 1f);
//		GL11.glTranslatef(sections.get(0).from.x * -scale, sections.get(0).from.y * -scale, 0f);
		
		if (false) {
			Misc.setColor(Color.white);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glLineWidth(3f);
			GL11.glBegin(GL11.GL_LINE_STRIP);
			Vector2f p0 = new Vector2f(0, 0);
			Vector2f p1 = new Vector2f(10000, 0);
			Vector2f p2 = new Vector2f(0, 10000);
			Vector2f p3 = new Vector2f(10000, 10000);
			for (float t = 0f; t <= 1f; t += 0.02f) {
				Vector2f p = Misc.bezierCubic(p0, p1, p2, p3, t);
				GL11.glVertex2f(p.x, p.y);
			}
			GL11.glEnd();
		}
		
		if (false) {
			GL11.glEnable(GL11.GL_POINT_SMOOTH);
			GL11.glPointSize(10f);
			GL11.glBegin(GL11.GL_POINTS);
			Misc.setColor(Color.yellow);
			for (SlipstreamSection curr : sections) {
				Misc.setColor(Color.yellow);
				if (curr.subdivision) {
					Misc.setColor(Color.green);
				}
				Vector2f p = curr.from;
				GL11.glVertex2f(p.x, p.y);
				p = curr.to;
				GL11.glVertex2f(p.x, p.y);
				
				Misc.setColor(Color.cyan);
				p = curr.control;
				GL11.glVertex2f(p.x, p.y);
			}
			GL11.glEnd();
			
			Misc.setColor(Color.white);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glLineWidth(3f);
			GL11.glBegin(GL11.GL_LINE_STRIP);
			for (SlipstreamSection curr : sections) {
				for (float t = 0f; t <= 1f; t += 0.02f) {
					Vector2f p = Misc.bezier(curr.from, curr.control, curr.to, t);
					GL11.glVertex2f(p.x, p.y);
				}
			}
			GL11.glEnd();
		}
		
		for (BoundingBox box : plugin.getBounds()) {
			Misc.setColor(Color.cyan);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glLineWidth(3f);
			GL11.glBegin(GL11.GL_LINE_LOOP);
			if (box != null) {
				for (Vector2f p : box.box) {
					GL11.glVertex2f(p.x, p.y);
				}
			}
			GL11.glEnd();
		}
		
		GL11.glPopMatrix();
		
		if (false) {
		GL11.glPushMatrix();
		Vector2f loc = Global.getSector().getPlayerFleet().getLocation();
		GL11.glTranslatef(loc.x - 1000, loc.y + 100, 0f);
//		
//		long seed = 23895464576452L + 4384357483229348234L;
//		//seed += System.nanoTime() / 1000000000L;
//		seed = 1181783497276652981L ^ seed;
//		Random random = new Random(seed);
//		float spikes = 0.67f;
//		float [] noise = initNoise1D(random, 128, spikes); 
//		noise[0] = 0.5f;
//		noise[noise.length - 1] = 0.5f;
//		genNoise1D(random, noise, 128, spikes);
//		normalizeNoise1D(noise);
		
		Misc.setColor(Color.orange);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(3f);
		GL11.glBegin(GL11.GL_LINE_STRIP);
		float horz = 50f;
		float vert = 500f;
		for (int i = 0; i < buildNoise.length; i++) {
			float f = buildNoise[i];
			GL11.glVertex2f(i * horz, f * vert); 
		}
		GL11.glEnd();
//		GL11.glBegin(GL11.GL_LINE_STRIP);
//		float iter = 2000f;
//		for (int i = 0; i < iter; i++) {
//			float f = getInterpNoise(noise, i / iter);
//			GL11.glVertex2f(i, f * vert); 
//		}
//		GL11.glEnd();
		Misc.setColor(Color.white);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(0, 0);
		GL11.glVertex2f(buildNoise.length * horz, 0);
		GL11.glVertex2f(0, 0);
		GL11.glVertex2f(0, vert);
		GL11.glEnd();
		
		GL11.glPopMatrix();
		}
	}
	
	public static float getInterpNoise(float [] noise, float t) {
		t *= noise.length;
		int index = (int) t;
		if (index >= noise.length) index = noise.length - 1;
		if (index < 0) index = 0;
		
		int index2 = index + 1;
		if (index2 >= noise.length) index2 = index - 1; 
		
		float f = t - index;
		
		return Misc.interpolate(noise[index], noise[index2], f);
	}
	
	
	/**
	 * To [0, 1]
	 * @param noise
	 */
	public static void normalizeNoise1D(float [] noise) {
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;
		for (float f : noise) {
			if (f < min) min = f;
			if (f > max) max = f;
		}
		
		if (max <= min) {
			for (int i = 0; i < noise.length; i++) {
				noise[i] = 0.5f;
			}
			return;
		}
		
		float range = max - min;
		for (int i = 0; i < noise.length; i++) {
			noise[i] = (noise[i] - min) / range;
		}
	}
	
	public static float [] initNoise1D(Random random, int size, float spikes) {
		float [] noise = new float[size];
		for (int i = 0; i < noise.length; i++) {
			noise[i] = -1f;
		}
		noise[0] = random.nextFloat() * spikes;
		noise[noise.length - 1] = random.nextFloat() * spikes;
		return noise;
	}
	public static void genNoise1D(Random random, float [] noise, int size, float spikes) {
		genNoise1DFill(random, noise, 0, noise.length - 1, 1, spikes);
	}
	
	public static void genNoise1DFill(Random random, float [] noise, int x1, int x2, int iter, float spikes) {
		if (x1 + 1 >= x2) return;

		int midX = (x1 + x2) / 2;

		float avg = (noise[x1] + noise[x2]) / 2f;
		noise[midX] = avg + ((float) Math.pow(spikes, (iter)) * (float) (random.nextFloat() - .5f));
		
		genNoise1DFill(random, noise, x1, midX, iter + 1, spikes);
		genNoise1DFill(random, noise, midX, x2, iter + 1, spikes);
	}

	public static float getApproximateBezierLength(Vector2f p0, Vector2f p1, Vector2f p2) {
		float total = 0f;
		Vector2f prev = p0;
		for (float f = 0; f <= 1.01f; f += 0.1f) {
			Vector2f curr = Misc.bezier(p0, p1, p2, f);
			total += Misc.getDistance(prev, curr);
			prev = curr;
		}
		return total;
	}
	public static float getApproximateBezierLength(Vector2f p0, Vector2f p1, Vector2f p2, Vector2f p3) {
		float total = 0f;
		Vector2f prev = p0;
		for (float f = 0; f <= 1.01f; f += 0.05f) {
			Vector2f curr = Misc.bezierCubic(p0, p1, p2, p3, f);
			total += Misc.getDistance(prev, curr);
			prev = curr;
		}
		return total;
	}
}





