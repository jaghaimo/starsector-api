package com.fs.starfarer.api.impl.campaign.velfield;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2.SlipstreamSegment;
import com.fs.starfarer.api.util.Misc;

public class BoundingBox {
	
	public static BoundingBox create(List<SlipstreamSegment> segments) {
		float padding = 0;
		List<Vector2f> points = new ArrayList<Vector2f>();
		SlipstreamSegment prev = null;
		float unitsPerNormals = 2000f;
		float distSoFar = unitsPerNormals;
		//for (SlipstreamSegment seg : segments) {
		for (int i = 0; i < segments.size(); i++) { 
			SlipstreamSegment seg = segments.get(i);
			if (distSoFar >= unitsPerNormals || i == segments.size() - 1) {
				distSoFar -= unitsPerNormals;
				Vector2f n1 = new Vector2f(seg.loc);
				Vector2f n2 = new Vector2f(seg.loc);
				n1.x += seg.normal.x * seg.width * 0.5f;
				n1.y += seg.normal.y * seg.width * 0.5f;
				n2.x -= seg.normal.x * seg.width * 0.5f;
				n2.y -= seg.normal.y * seg.width * 0.5f;
				points.add(n1);
				points.add(n2);
			} else {
				points.add(new Vector2f(seg.loc));
			}
			if (seg.width * 0.5f > padding) {
				padding = seg.width * 0.5f;
			}
			if (prev != null) {
				distSoFar += Misc.getDistance(seg.loc, prev.loc);
			}
			prev = seg;
		}
		padding *= 0.5f;
		BoundingBox box = new BoundingBox(padding);
		box.compute(points);
		return box;
	}

	protected transient List<Vector2f> points;
	protected transient List<Vector2f> convexHull;
	protected List<Vector2f> box;
	protected float padding;
	protected float [] rotatedBox;
	protected float angle;

	protected boolean boxComputed = false;
	protected Vector2f center = new Vector2f();
	protected float radius;
	
	public BoundingBox(float padding) {
		this.padding = padding;
	}

	public boolean pointNeedsDetailedCheck(Vector2f p) {
		return pointNeedsDetailedCheck(p, 0f);
	}
	public boolean pointNeedsDetailedCheck(Vector2f p, float extraRange) {
		float distSq = Misc.getDistanceSq(center, p);
		if (distSq > (radius + extraRange) * (radius + extraRange)) return false;
		
		if (!boxComputed) return true;
		
		Vector2f check = Misc.rotateAroundOrigin(p, angle);
		
		return check.x > rotatedBox[0] - extraRange && check.x < rotatedBox[2] + extraRange &&
			   check.y > rotatedBox[1] - extraRange && check.y < rotatedBox[3] + extraRange;
	}
	
	
	public void compute(List<Vector2f> points) {
		boxComputed = false;
		computeConvexHull(points);
		computeBox();
		computeCenterAndRadius();
	}
	
	public void computeCenterAndRadius() {
		center.set(0, 0);
		for (Vector2f p : points) {
			center.x += p.x;
			center.y += p.y;
		}
		center.scale(1f / (float) Math.max(1f, points.size()));
		
		float maxDistSq = 0f;
		for (Vector2f p : points) {
			float dist = Misc.getDistanceSq(center, p);
			if (dist > maxDistSq) maxDistSq = dist;
		}
		radius = (float) Math.sqrt(maxDistSq) + padding;
		
	}
	
	public void computeBox() {
		if (boxComputed) return;
		
		if (convexHull == null || convexHull.size() < 3) {
			boxComputed = false;
			return;
		}
		
		float minArea = Float.MAX_VALUE;
		float [] best = null;
		float bestAngle = 0f;
		for (int i = 0; i < convexHull.size(); i++) {
			int e1 = i;
			int e2 = i + 1;
			if (i + 1 == convexHull.size()) e2 = 0;
			Vector2f p1 = convexHull.get(e1);
			Vector2f p2 = convexHull.get(e2);
			Vector2f edge = Vector2f.sub(p2, p1, new Vector2f());
			edge = Misc.normalise(edge);
			
		    float angle = (float) Math.acos(edge.y) * Misc.DEG_PER_RAD;
		    List<Vector2f> rotated = rotate(convexHull, angle);
		    float [] box = getBoundingBox(rotated);
		    float area = (box[2] - box[0]) * (box[3] - box[1]);
		    if (area < minArea) {
		    	minArea = area;
		    	best = box;
		    	bestAngle = angle;
		    }
		}
		
		if (best == null) {
			boxComputed = false;
			return;
		}
		
		best[0] -= padding;
		best[1] -= padding;
		best[2] += padding;
		best[3] += padding;
		
		Vector2f p1 = new Vector2f(best[0], best[1]);
		Vector2f p2 = new Vector2f(best[2], best[1]);
		Vector2f p3 = new Vector2f(best[2], best[3]);
		Vector2f p4 = new Vector2f(best[0], best[3]);
		box = new ArrayList<Vector2f>();
		box.add(p1);
		box.add(p2);
		box.add(p3);
		box.add(p4);
		
		rotatedBox = best; 
		this.angle = bestAngle;
		
		box = rotate(box, -bestAngle);
		boxComputed = true;
	}
	
	public static List<Vector2f> rotate(List<Vector2f> points, float angle) {
		List<Vector2f> result = new ArrayList<Vector2f>();
		for (Vector2f p : points) {
			result.add(Misc.rotateAroundOrigin(p, angle));
		}
		return result;
	}
	
	public static float [] getBoundingBox(List<Vector2f> points) {
		float minX = Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float maxX = -Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;
		for (Vector2f p : points) {
			if (p.x < minX) minX = p.x;
			if (p.y < minY) minY = p.y;
			if (p.x > maxX) maxX = p.x;
			if (p.y > maxY) maxY = p.y;
		}
		return new float [] {minX, minY, maxX, maxY};
	}
	
	public void computeConvexHull(List<Vector2f> points) {
		this.points = new ArrayList<Vector2f>(points);
		
		if (points.size() < 3) {
			boxComputed = false;
			return;
		}
		
		Vector2f p1 = null;
		float minY = Float.MAX_VALUE;
		for (Vector2f p : points) {
			if (p.y < minY) {
				p1 = p;
				minY = p.y;
			}
		}
		final Map<Vector2f, Float> angles = new HashMap<Vector2f, Float>();
		for (Vector2f p : points) {
			if (p == p1) continue;
			float angle = Misc.getAngleInDegreesStrict(p1, p);
			//float angle = 1f;
			angles.put(p, angle);
		}
		
		List<Vector2f> sorted = new ArrayList<Vector2f>(points);
		sorted.remove(p1);
		Collections.sort(sorted, new Comparator<Vector2f>() {
			public int compare(Vector2f o1, Vector2f o2) {
				float diff = angles.get(o1) - angles.get(o2);
				if (diff < 0) return -1; 
				if (diff > 0) return 1;
				return 0;
			}
		});
		
		convexHull = new ArrayList<Vector2f>();
		convexHull.add(p1);
		for (Vector2f p : sorted) {
			if (convexHull.size() == 1) {
				convexHull.add(p);
				continue;
			}
			while (true) {
				float turnDir = getTurnDir(convexHull.get(convexHull.size() - 2),
										   convexHull.get(convexHull.size() - 1),
										   p);
				if (turnDir <= 0) {
					convexHull.remove(convexHull.size() - 1);
					if (convexHull.size() < 2) break;
				} else {
					convexHull.add(p);
					break;
				}
			}
		}
		
		if (convexHull.size() < 3) {
			boxComputed = false;
			return;
		}
		
	}
	
	public static float getTurnDir(Vector2f p1, Vector2f p2, Vector2f p3) {
		float r = (p2.x - p1.x) * (p3.y - p1.y) - (p2.y  - p1.y) * (p3.x - p1.x);
		if (r > 0) return 1f; // left turn
		if (r < 0) return -1f; // right turn
		return 0; // collinear
	}
	
	
	public void renderDebug(float alpha) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glEnable(GL11.GL_POINT_SMOOTH);
		GL11.glPointSize(10f);
		GL11.glBegin(GL11.GL_POINTS);
		Misc.setColor(Color.yellow);
		if (points != null) {
			for (Vector2f p : points) {
				GL11.glVertex2f(p.x, p.y);
			}
		}
		GL11.glEnd();
		
		Misc.setColor(Color.white);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(3f);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		if (convexHull != null) {
			for (Vector2f p : convexHull) {
				GL11.glVertex2f(p.x, p.y);
			}
		}
		GL11.glEnd();
		
		Misc.setColor(Color.cyan);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(3f);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		if (box != null) {
			for (Vector2f p : box) {
				GL11.glVertex2f(p.x, p.y);
			}
		}
		GL11.glEnd();
		
	}
	
	
	
	
}




