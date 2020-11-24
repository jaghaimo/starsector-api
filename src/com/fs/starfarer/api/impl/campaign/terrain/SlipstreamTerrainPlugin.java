package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

public class SlipstreamTerrainPlugin extends BaseTerrain {
	
	public static final String LOCATION_SLIPSTREAM_KEY = "local_slipstream_terrain";
	
	public static final float MIN_POINT_DIST = 20f;
	public static final float MAX_POINT_DIST = 250f;
	
	public static final float WIDTH_GROWTH_PER_DAY = 200f;
	
	
	public static CampaignTerrainAPI getSlipstream(LocationAPI location) {
		if (location == null) return null;
		return (CampaignTerrainAPI) location.getPersistentData().get(LOCATION_SLIPSTREAM_KEY);
	}
	
	public static SlipstreamTerrainPlugin getSlipstreamPlugin(LocationAPI location) {
		CampaignTerrainAPI slipstream = getSlipstream(location);
		if (slipstream != null) {
			return (SlipstreamTerrainPlugin)slipstream.getPlugin();
		}
		return null;
	}
	
	public static class StreamPoint {
		public float x, y;
		public float daysLeft;
		public float burn;
		public float width;
		public StreamPoint(float x, float y, float daysLeft, float burn, float width) {
			this.x = x;
			this.y = y;
			this.daysLeft = daysLeft;
			this.burn = burn;
			this.width = width;
		}
	}
	
	public static class Stream {
		transient public float minX, minY, maxX, maxY;
		public List<StreamPoint> points = new ArrayList<StreamPoint>();
		//public List<StreamParticle> particles = new ArrayList<StreamParticle>();
		public SectorEntityToken key;
		public int particlesPerPoint;

		public Stream(SectorEntityToken key, int particlesPerPoint) {
			this.key = key;
			this.particlesPerPoint = particlesPerPoint;
			readResolve();
		}

		Object readResolve() {
			minX = Float.MAX_VALUE;
			minY = Float.MAX_VALUE;
			maxX = -Float.MAX_VALUE;
			maxY = -Float.MAX_VALUE;
			for (StreamPoint p : points) {
				updateMinMax(p);
			}
			return this;
		}
		
		public boolean isEmpty() {
			return points.isEmpty();
		}
		
		public StreamPoint getLastPoint() {
			if (isEmpty()) return null;
			return points.get(points.size() - 1);
		}
		
		public StreamPoint getFirstPoint() {
			if (isEmpty()) return null;
			return points.get(0);
		}
		
		private void updateMinMax(StreamPoint p) {
			if (p.x < minX) minX = p.x;
			if (p.y < minY) minY = p.y;
			if (p.x > maxX) maxX = p.x;
			if (p.y > maxY) maxY = p.y;
		}
		
		public void addPoint(StreamPoint p) {
			updateMinMax(p);
			points.add(p);
		}
		
		public boolean isNearViewport(ViewportAPI v, float pad) {
			float x = v.getLLX();
			float y = v.getLLY();
			float w = v.getVisibleWidth();
			float h = v.getVisibleHeight();
			
			if (minX > x + w + pad) return false;
			if (minY > y + h + pad) return false;
			if (maxX < x - pad) return false;
			if (maxY < y - pad) return false;
			
			return true;
		}
		
		public boolean couldContainLocation(Vector2f loc, float radius) {
			if (minX > loc.x + radius) return false;
			if (minY > loc.y + radius) return false;
			if (maxX < loc.x - radius) return false;
			if (maxY < loc.y - radius) return false;
			return true;
		}
	}
	
	
	protected Map<SectorEntityToken, Stream> streams = new LinkedHashMap<SectorEntityToken, Stream>();
	transient protected Map<SectorEntityToken, StreamPoint> containsCache = new HashMap<SectorEntityToken, StreamPoint>();
	//transient protected SpriteAPI pointTexture;
	
	protected TimeoutTracker<SectorEntityToken> disrupted = new TimeoutTracker<SectorEntityToken>();
	
	public void init(String terrainId, SectorEntityToken entity, Object param) {
		super.init(terrainId, entity, param);
		readResolve();
	}
	
	public void advance(float amount) {
		super.advance(amount);
		if (true) return;
		
		containsCache.clear();
		
		float days = Global.getSector().getClock().convertToDays(amount);
		
		disrupted.advance(days);
		
		for (CampaignFleetAPI fleet : entity.getContainingLocation().getFleets()) {
			//if (!fleet.isPlayerFleet()) continue;
			if (disrupted.contains(fleet)) continue;
			
			float burnLevel = fleet.getCurrBurnLevel();
			if (burnLevel >= 1) {
				Stream s = getStream(fleet);
				Vector2f loc = fleet.getLocation();
				boolean addPoint = false;
				if (s.isEmpty()) {
					addPoint = true;
				} else {
					StreamPoint last = s.getLastPoint();
					float dist = Misc.getDistance(loc.x, loc.y, last.x, last.y);
					if (dist > MIN_POINT_DIST) {
						addPoint = true;
					}
				}
				if (addPoint) {
					StreamPoint p = new StreamPoint(loc.x, loc.y, 0.25f,
												    burnLevel, 
												    Math.max(MIN_POINT_DIST * 2f, fleet.getRadius()));
					s.addPoint(p);
				}
			}
		}
		
		Iterator<Stream> iter1 = streams.values().iterator();
		while (iter1.hasNext()) {
			Stream s = iter1.next();

			advancePoints(s, days);
			//advanceParticles(s, days);
			//addNewParticles(s, days);
			
			if (s.isEmpty()) {// && s.particles.isEmpty()) {
				iter1.remove();
			} 
		}
	}
	
	public TimeoutTracker<SectorEntityToken> getDisrupted() {
		return disrupted;
	}
	
	public void disrupt(CampaignFleetAPI fleet, float dur) {
		disrupted.set(fleet, dur);
	}

	private void advancePoints(Stream s, float days) {
		Iterator<StreamPoint> iter2 = s.points.iterator();
		while (iter2.hasNext()) {
			StreamPoint p = iter2.next();
			p.daysLeft -= days;
			p.width += WIDTH_GROWTH_PER_DAY * days;
			if (p.daysLeft <= 0) {
				iter2.remove();
			}
		}
	}
	
	protected Stream getStream(SectorEntityToken key) {
		Stream s = streams.get(key);
		if (s == null) {
			s = new Stream(key, 50);
			streams.put(key, s);
		}
		return s;
	}
		
	@Override
	public void applyEffect(SectorEntityToken entity, float days) {
		if (entity instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
			containsPointCaching(fleet, fleet.getLocation(), fleet.getRadius());
			if (containsCache.containsKey(fleet)) {
				StreamPoint point = containsCache.get(fleet);
				float fleetBurn = fleet.getFleetData().getBurnLevel();
				if (point.burn > fleetBurn || true) {
					float diff = point.burn - fleetBurn;
					if (diff > 2) diff = 2;
					diff = (int) diff;
					diff = 2;
					fleet.getStats().addTemporaryModFlat(0.1f, getModId(), "In slipstream", diff, fleet.getStats().getFleetwideMaxBurnMod());
				}
			}
		}
	}

	@Override
	public boolean containsEntity(SectorEntityToken other) {
		return containsPointCaching(other, other.getLocation(), other.getRadius());
	}

	@Override
	public boolean containsPoint(Vector2f point, float radius) {
		return containsPointCaching(null, point, radius);
	}
	
	private boolean containsPointCaching(SectorEntityToken key, Vector2f point, float radius) {
		if (key != null && containsCache.containsKey(key)) {
			return true;
		}
		if (true) return false;
		
		float maxBurn = 0f;
		StreamPoint result = null;
		for (Stream s : streams.values()) {
			if (s.key == key) continue;
			
			if (!s.couldContainLocation(point, radius)) continue;
			for (StreamPoint p : s.points) {
//				if (key instanceof CampaignFleetAPI) {
//					float fleetBurn = ((CampaignFleetAPI)key).getFleetData().getBurnLevel();
//					if (p.burn <= fleetBurn) {
//						continue;
//					}
//				}
				
				float dist = Misc.getDistance(p.x, p.y, point.x, point.y);
				if (dist < p.width && maxBurn < p.burn) {
					maxBurn = p.burn;
					result = p;
				}
			}
		}
		
		if (result != null && key != null) {
			containsCache.put(key, result);
		}
		
		return result != null;
	}

	
	
	Object readResolve() {
		layers = EnumSet.of(CampaignEngineLayers.TERRAIN_7);
		//pointTexture = Global.getSettings().getSprite("terrain", "slipstream_point");
		if (containsCache == null) {
			containsCache = new HashMap<SectorEntityToken, StreamPoint>();
		}
		return this;
	}
	
	Object writeReplace() {
		return this;
	}
	
	transient private EnumSet<CampaignEngineLayers> layers = EnumSet.of(CampaignEngineLayers.TERRAIN_7);
	public EnumSet<CampaignEngineLayers> getActiveLayers() {
		return layers;
	}
	
	@Override
	public boolean stacksWithSelf() {
		return super.stacksWithSelf();
	}

	@Override
	public float getRenderRange() {
		return Float.MAX_VALUE;
	}

	public boolean hasTooltip() {
		return true;
	}
	
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		float pad = 10f;
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color fuel = Global.getSettings().getColor("progressBarFuelColor");
		Color bad = Misc.getNegativeHighlightColor();
		
		tooltip.addTitle(getTerrainName());
		tooltip.addPara("Reduces the range at which fleets inside it can be detected by %s.", pad,
				highlight, 
				"50%"
		);
		tooltip.addPara("Does not stack with other similar terrain effects.", pad);
	}
	
	public boolean isTooltipExpandable() {
		return false;
	}
	
	public float getTooltipWidth() {
		return 350f;
	}
	
	public String getTerrainName() {
		return "Slipstream";
	}
	
	public String getEffectCategory() {
		return "slipstream";
	}
}



//public static class StreamParticle {
//	public float x, y;
//	public float age, maxAge;
//	public StreamPoint point;
//	//public Vector2f lastVel = null;
//	//public LinkedList<Vector2f> prevLocs = new LinkedList<Vector2f>();
//	public Color color;
//}

//private void advanceParticles(Stream s, float days) {
//	Iterator<StreamParticle> iter3 = s.particles.iterator();
//	while (iter3.hasNext()) {
//		StreamParticle p = iter3.next();
//		p.age += days;
//		if (p.age >= p.maxAge) {
//			iter3.remove();
//		} else if (p.point != null) {
//			StreamPoint curr = p.point;
//			StreamPoint prev = null, next = null;
//			int index = s.points.indexOf(curr);
//			if (index != -1) {
//				if (index > 0) {
//					prev = s.points.get(index - 1);
//				}
//				if (index < s.points.size() - 1) {
//					next = s.points.get(index + 1);
//				}
//			}
//			Vector2f vel = new Vector2f();
//			Vector2f cv = new Vector2f(curr.x, curr.y);
//			Vector2f pLoc = new Vector2f(p.x, p.y);
//			float maxSpeed = 1000f;
//			float minSpeed = 1000f;
//			float maxDist = 200f;
//			Vector2f unitDirPrev = null;
//			if (prev != null) {
//				Vector2f pv = new Vector2f(prev.x, prev.y);
//				Vector2f dir = Vector2f.sub(cv, pv, new Vector2f());
//				unitDirPrev = new Vector2f(dir);
//				Misc.normalise(dir);
//				float dist = Misc.getDistance(pLoc, pv);
//				float speed = minSpeed;
//				if (dist < maxDist) {
//					speed = minSpeed + (maxSpeed - minSpeed) * (1f - dist / maxDist); 
//				}
//				dir.scale(speed);
//				Vector2f.add(vel, dir, vel);
//			}
//			if (next != null) {
//				Vector2f nv = new Vector2f(next.x, next.y);
//				Vector2f dir = Vector2f.sub(nv, cv, new Vector2f());
//				Misc.normalise(dir);
//				float dist = Misc.getDistance(cv, pLoc);
//				boolean angleOk = unitDirPrev == null || Vector2f.dot(unitDirPrev, dir) > 0;
//				if (dist < MAX_POINT_DIST && angleOk) {
//					float speed = minSpeed;
//					if (dist < maxDist) {
//						speed = minSpeed + (maxSpeed - minSpeed) * (1f - dist / maxDist); 
//					}
//					dir.scale(speed);
//					Vector2f.add(vel, dir, vel);
//					
//					float distNext = Misc.getDistance(nv, pLoc);
//					if (distNext < dist) {
//						p.point = next;
//					}
//				}
//			}
//			
//			p.x += vel.x * days;
//			p.y += vel.y * days;
//			
//			pLoc.set(p.x, p.y);
//			if (p.prevLocs.size() < 2) {
//				p.prevLocs.add(pLoc);
//			} else {
//				float dist = Misc.getDistance(p.prevLocs.get(p.prevLocs.size() - 2), p.prevLocs.getLast());
//				if (dist >= 2) {
//					p.prevLocs.add(pLoc);
//				} else {
//					p.prevLocs.getLast().set(pLoc);
//				}
//			}
//			
//			
//			while (p.prevLocs.size() > 5) {
//				p.prevLocs.removeFirst();
//			}
//		}
//	}
//}
//
//
//private void addNewParticles(Stream s, float days) {
//	if (s.isEmpty()) return;
//	
//	if (!s.key.isInCurrentLocation() || !s.isNearViewport(Global.getSector().getViewport(), 500f)) {
//		return;
//	}
//	
//	int points = s.points.size();
//	int maxParticles = (points - 0) * s.particlesPerPoint;
//
//	WeightedRandomPicker<Color> colorPicker = new WeightedRandomPicker<Color>();
//	if (s.key instanceof CampaignFleetAPI) {
//		CampaignFleetAPI fleet = (CampaignFleetAPI) s.key;
//		for (FleetMemberViewAPI view : fleet.getViews()) {
//			colorPicker.add(view.getContrailColor().getCurr(), view.getMember().getHullSpec().getHullSize().ordinal());
//		}
//	}
//	if (colorPicker.isEmpty()) {
//		colorPicker.add(new Color(100, 150, 255, 255), 1f);
//	}
//	
//	Random r = new Random();
//	for (int i = 0; i < s.particlesPerPoint * 0.1f; i++) {
//		if (s.particles.size() >= maxParticles || s.isEmpty() || s.points.size() <= 1) {
//			break;
//		}
//		
//		int index = r.nextInt(s.points.size() - 1);
//		StreamPoint p = s.points.get(index);
//		if (index < s.points.size() - 1) {
//			StreamPoint next = s.points.get(index + 1);
//			float dist = Misc.getDistance(p.x, p.y, next.x, next.y);
//			if (dist > MAX_POINT_DIST) continue;
//		}
//		
//		StreamParticle particle = new StreamParticle();
//		float spread = 10f + (s.points.size() - index) * 10f;
//		particle.x = p.x + (float) Math.random() * spread - spread/2f;
//		particle.y = p.y + (float) Math.random() * spread - spread/2f;
////		particle.x = p.x;
////		particle.y = p.y;
//		particle.age = 0;
//		particle.maxAge = 0.1f + 0.1f * (float) Math.random();
//		particle.point = p;
//		
//		particle.color = colorPicker.pick();
//		
//		s.particles.add(particle);
//	}
//}
//public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
//	super.render(layer, viewport);
//	
//	float alphaMult = viewport.getAlphaMult();
//	
//	for (Stream s : streams.values()) {
//		if (!s.isNearViewport(viewport, 300f)) continue;
//		if (s.particles.isEmpty()) continue;
//		GL11.glEnable(GL11.GL_BLEND);
//		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		//Color color = new Color(100, 150, 255, 255);
////		GL11.glColor4ub((byte)color.getRed(),
////				(byte)color.getGreen(),
////				(byte)color.getBlue(),
////				(byte)((float)color.getAlpha() * alphaMult));
//		
//		pointTexture.bindTexture();
//		GL11.glBegin(GL11.GL_QUADS);
//		StreamPoint last = null;
//		if (!s.points.isEmpty()) {
//			last = s.points.get(s.points.size() - 1);
//		}
//		for (StreamParticle p : s.particles) {
////			if (p.prevLocs == null || p.prevLocs.size() < 5) continue;
////			float index = 0f;
////			float max = p.prevLocs.size();
////			for (Vector2f loc : p.prevLocs) {
////				float b;
////				float t = Math.min(0.05f, p.maxAge / 2f);
////				if (p.age < t) {
////					b = p.age / t;
////				} else {
////					b = 1f - (p.age - t) / (p.maxAge - t);
////				}
////				b *= 1f - ((index + 1) / max);
////				if (index == 0) b = 0;
////				if (index == max - 1) b = 0;
////				
////				GL11.glColor4ub((byte)color.getRed(),
////						(byte)color.getGreen(),
////						(byte)color.getBlue(),
////						(byte)((float)color.getAlpha() * alphaMult * b));
////				
////				float size = 7f;
////				GL11.glTexCoord2f(0, 0);
////				GL11.glVertex2f(loc.x - size/2f, loc.y - size/2f);
////		
////				GL11.glTexCoord2f(0.01f, 0.99f);
////				GL11.glVertex2f(loc.x - size/2f, loc.y + size/2f);
////		
////				GL11.glTexCoord2f(0.99f, 0.99f);
////				GL11.glVertex2f(loc.x + size/2f, loc.y + size/2f);
////		
////				GL11.glTexCoord2f(0.99f, 0.01f);
////				GL11.glVertex2f(loc.x + size/2f, loc.y - size/2f);
////				
////				index++;
////			}
//			
//			float b;
//			float t = Math.min(0.05f, p.maxAge / 2f);
//			if (p.age < t) {
//				b = p.age / t;
//			} else {
//				b = 1f - (p.age - t) / (p.maxAge - t);
//			}
//			b *= 4f;
//			
//			if (last != null) {
////				if (s.key.isPlayerFleet()) {
////					System.out.println("23rasdf");
////				}
//				float dist = Misc.getDistance(last.x, last.y, p.x, p.y);
//				float max = s.key.getRadius() + 50f;
//				if (dist < max) {
//					b *= Math.max(0, (dist - s.key.getRadius())/ (max - s.key.getRadius()));
//					if (b <= 0) {
//						p.age = p.maxAge + 1f;
//					}
//				}
//			}
//			if (p.age >= p.maxAge) {
//				b = 0f;
//			}
//			
//			Color color = p.color;
//			float alpha = ((float)color.getAlpha() * alphaMult * b);
//			if (alpha > 255f) alpha = 255f;
//			GL11.glColor4ub((byte)color.getRed(),
//					(byte)color.getGreen(),
//					(byte)color.getBlue(),
//					(byte)(alpha));
//			
//			float size = 7f;
//			GL11.glTexCoord2f(0, 0);
//			GL11.glVertex2f(p.x - size/2f, p.y - size/2f);
//	
//			GL11.glTexCoord2f(0.01f, 0.99f);
//			GL11.glVertex2f(p.x - size/2f, p.y + size/2f);
//	
//			GL11.glTexCoord2f(0.99f, 0.99f);
//			GL11.glVertex2f(p.x + size/2f, p.y + size/2f);
//	
//			GL11.glTexCoord2f(0.99f, 0.01f);
//			GL11.glVertex2f(p.x + size/2f, p.y - size/2f);
//		}
//		
//		GL11.glEnd();
//		
//	}
//}
