package com.fs.starfarer.api.impl.campaign.velfield;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.NascentGravityWellAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamBuilder.StreamType;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2.SlipstreamParams2;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2.SlipstreamSegment;
import com.fs.starfarer.api.impl.campaign.world.TTBlackSite;
import com.fs.starfarer.api.util.CollisionGridUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SlipstreamManager implements EveryFrameScript {
	
	public static int WIDTH = 21;
	public static int HEIGHT = 11;
	public static float MAP_WIDTH_PADDING = 8000;
	public static float MAP_HEIGHT_PADDING = 5000;
	/**
	 * 21x11 characters
	 * Capital letter: endpoints
	 * Lowercase letter, optional: control point for 3-point stream (if 2: cubic bezier curve instead of quadratic)
	 * Direction of flow is determined by time of cycle and relative location of endpoints
	 * X is ignored; just marks the center to make it easier to edit
	 * After a capital letter:
	 * < makes the stream go in the opposite-to-standard direction
	 * +-<single digit number> adjusts the stream's burn level; 0 means +10
	 * ~ makes the stream narrower, on average
	 * = makes it wider
	 * | makes it much straighter than usual
	 * ! priority, spawned before other streams (and thus other streams will fade when intersecting it, not this one)
	 * ^ only keep longest segment - but, from intersections with streams ONLY, not with stars/other blockers
	 * * randomize: up to +10 burn, reverse, straightness, wide/narrow 
	 * ? randomize: same as above, but no reverse and only up to +5 burn
	 */
	public static Map<String, Float> STREAM_CONFIGS = new LinkedHashMap<String, Float>();
	
	static {
		loadConfigs();
	}
	
	public static void loadConfigs() {
		// begin "finished"
		STREAM_CONFIGS.put(
				"aD?                 B" +
				"C?                 EA" +
				"                  c  " +
				"                     " +
				"                     " +
				"          X          " +
				"                     " +
				"                     " +
				"  d                  " +
				"A!E?                C" +
				"B!                 Db"
				, 10f);
		STREAM_CONFIGS.put(
				"Ba   C         D   aB" +
				"                     " +
				"                     " +
				"                     " +
				"                     " +
				"         CXD         " +
				"          e          " +
				"                     " +
				"                     " +
				"                     " +
				"A!bE              EbA"
		, 10f);		
		STREAM_CONFIGS.put(
				"A! D?            F? A" +
				"                     " +
				"                     " +
				"a                   a" +
				"                     " +
				"C!   DE   X   FG    C" +
				"                     " +
				"b                   b" +
				"                     " +
				"                     " +
				"B! E?            G? B"
				, 10f);		
		STREAM_CONFIGS.put(
				"C!   D!            cA" +
				"                     " +
				"                     " +
				"                     " +
				"A?                 aa" +
				"          X         c" +
				"B?                 bb" +
				"                     " +
				"                     " +
				"d             C      " +
				"d                  DB"
				, 10f);		
		STREAM_CONFIGS.put(
				"B^!     I^?   H^? a G" +
				"                     " +
				"   i         h       " +
				"                    g" +
				"    A!|  H^G^?       " +
				"   C^<I   X          " +
				"    D^    E^?F^?    F" +
				"c  AB                " +
				"                     " +
				"b    d               " +
				"C?          D?e   a E"
				, 10f);
		STREAM_CONFIGS.put(
				"       b  a          " +
				"A|~+0               A" +
				"                     " +
				"B|~+0               B" +
				"      c          a   " +
				"          X          " +
				"                     " +
				"C|!+0 d             C" +
				"               b     " +
				"D|~+0               D" +
				"          d   c      "
				, 10f);
		
		// highly randomized and mirrored
		STREAM_CONFIGS.put(
				"A*       B*         G" +
				"    H*         g     " +
				"           h      H  " +
				"    b                " +
				" C*     A        G*  " +
				"          X         F" +
				"     c       D       " +
				"  B     E*      e    " +
				"          d       f  " +
				"                     " +
				"D*     C    F*      E"
				, 5f);
		String prev = (String) STREAM_CONFIGS.keySet().toArray()[STREAM_CONFIGS.size() - 1];
		STREAM_CONFIGS.put(mirrorHorz(prev), 5f);
		STREAM_CONFIGS.put(mirrorVert(prev), 5f);
		STREAM_CONFIGS.put(mirrorHorz(mirrorVert(prev)), 5f);
		
		// end "finished"
		
		
		// maybe?
//		STREAM_CONFIGS.put(
//				"A|!~+0C   D    FE   A" +
//				"c    d              B" +
//				"                     " +
//				"                     " +
//				"                     " +
//				"        f X      e   " +
//				"                     " +
//				"                     " +
//				"                     " +
//				"B|!<-5D^             " +
//				"  C^       F^E<^   bb"
//				, 10f);		
//		STREAM_CONFIGS.put( // also maybe, very similar to another one though
//				"A        C D        A" +
//				"  c               d  " +
//				"                     " +
//				"                     " +
//				"          e          " +
//				"          X          " +
//				"          b          " +
//				"E         a         E" +
//				"                     " +
//				"                     " +
//				"B  C            D   B"
//				, 10f);

		// blank
//		STREAM_CONFIGS.put(
//				"                     " +
//				"                     " +
//				"                     " +
//				"                     " +
//				"                     " +
//				"          X          " +
//				"                     " +
//				"                     " +
//				"                     " +
//				"                     " +
//				"                     "
//				, 10f);
		
	}
	
	public static void mirrorPrevVert(float weight) {
		String prev = (String) STREAM_CONFIGS.keySet().toArray()[STREAM_CONFIGS.size() - 1];
		STREAM_CONFIGS.put(mirrorVert(prev), weight);
	}
	public static String mirrorVert(String in) {
		String out = "";
		for (int j = 0; j < HEIGHT; j++) {
			out = in.substring(j * WIDTH, (j + 1) * WIDTH) + out;
		}
		return out;
	}
	public static void mirrorPrevHorz(float weight) {
		String prev = (String) STREAM_CONFIGS.keySet().toArray()[STREAM_CONFIGS.size() - 1];
		STREAM_CONFIGS.put(mirrorHorz(prev), weight);
	}
	public static String mirrorHorz(String in) {
		String out = "";
		for (int j = 0; j < HEIGHT; j++) {
			out = out + new StringBuilder(in.substring(j * WIDTH, (j + 1) * WIDTH)).reverse();
		}
		return out;
	}
	
	public static void validateConfigs() {
		// make sure they all parse
		for (String key : STREAM_CONFIGS.keySet()) {
			if (key.length() != WIDTH * HEIGHT) {
				throw new RuntimeException("Length of slipstream config not WIDTHxHEIGHT [" + key + "]");
			}
			try {
				new StreamConfig(key, null);
			} catch (Throwable t) {
				throw new RuntimeException("Error parsing slipstream config [" + key + "]", t);
			}
		}
		
	}
	
	public static class StreamData {
		public String id;
		public int p0x = -1;
		public int p0y = -1;
		public int p1x = -1;
		public int p1y = -1;
		public int controlX = -1;
		public int controlY = -1;
		public int control2X = -1;
		public int control2Y = -1;
		public int burnMod = 0;
		public boolean reverse = false;
		public StreamType type = StreamType.NORMAL;
		public boolean wasUsed = false;
		public boolean straight = false;
		public boolean priority = false;
		public boolean onlyKeepLongestSegment = false;
		public boolean randomize = false;
		public boolean minorRandomize = false;
		
		public StreamData(String id) {
			this.id = id;
		}
		public Vector2f generateP0(Random random) {
			if (p0x < 0) return null;
			return generate(p0x, p0y, random);
		}
		public Vector2f generateP1(Random random) {
			if (p1x < 0) return null;
			return generate(p1x, p1y, random);
		}
		public Vector2f generateControl(Random random) {
			if (controlX < 0) return null;
			return generate(controlX, controlY, random);
		}
		public Vector2f generateControl2(Random random) {
			if (control2X < 0) return null;
			return generate(control2X, control2Y, random);
		}
		public Vector2f generate(int cellX, int cellY, Random random) {
			float sw = Global.getSettings().getFloat("sectorWidth") - MAP_WIDTH_PADDING;
			float sh = Global.getSettings().getFloat("sectorHeight") - MAP_HEIGHT_PADDING;
			float cellWidth = sw / WIDTH;
			float cellHeight = sh / HEIGHT;
			Vector2f p = new Vector2f();
//			if (cellX == 20) {
//				System.out.println("efwefwef");
//			}
			float minX = -sw/2f + cellWidth * cellX;
			float maxX = -sw/2f + cellWidth * (cellX + 1);
			float minY = -sh/2f + cellHeight * cellY;
			float maxY = -sh/2f + cellHeight * (cellY + 1);
			
			p.x = minX + (maxX - minX) * random.nextFloat();
			p.y = minY + (maxY - minY) * random.nextFloat();
//			p.x = minX + (maxX - minX) * 0f;
//			p.y = minY + (maxY - minY) * 0f;
//			p.x = minX + (maxX - minX) * 1f;
//			p.y = minY + (maxY - minY) * 1f;
			
			return p;
		}
	}
	
	public static class StreamConfig {
		
		public List<StreamData> streams = new ArrayList<StreamData>();
		public String data;
		public StreamConfig(String data, Random random) {
			this.data = data;
			Set<String> ids = new LinkedHashSet<String>();
			for (int i = 0; i < data.length(); i++) {
				char c = data.charAt(i);
				if (c == 'X') continue;
				if (Character.isUpperCase(c)) {
					ids.add("" + c);
				}
			}
			
			for (String id : ids) {
				StreamData curr = new StreamData(id);
				for (int i = 0; i < data.length(); i++) {
					char c = data.charAt(i);
					if (c == 'X') continue;
					
					int cellX = i % WIDTH;
					int cellY = HEIGHT - i / WIDTH - 1;
					if (id.equals("" + c)) {
						if (curr.p0x < 0) {
							curr.p0x = cellX;
							curr.p0y = cellY;
						} else if (curr.p1x < 0) {
							curr.p1x = cellX;
							curr.p1y = cellY;
						}
					} else if (id.toLowerCase().equals("" + c)) {
						if (curr.controlX < 0) {
							curr.controlX = cellX;
							curr.controlY = cellY;
						} else {
							curr.control2X = cellX;
							curr.control2Y = cellY;
						}
					}
					
					if (id.equals("" + c)) {
						for (int j = i + 1; j < data.length() && j < i + 10; j++) {
							char c2 = data.charAt(j);
							if (c2 == ' ' || Character.isAlphabetic(c2)) break;
							if (c2 == '<') {
								curr.reverse = true;
							} else if (c2 == '~') {
								curr.type = StreamType.NARROW;
							} else if (c2 == '=') {
								curr.type = StreamType.WIDE;
							} else if (c2 == '-') {
								int burnMod = data.charAt(j + 1) - '0';
								if (burnMod == 0) burnMod = 10;
								curr.burnMod = -1 * burnMod;
							} else if (c2 == '+') {
								int burnMod = data.charAt(j + 1) - '0';
								if (burnMod == 0) burnMod = 10;
								curr.burnMod = burnMod;
							} else if (c2 == '|') {
								curr.straight = true;
							} else if (c2 == '!') {
								curr.priority = true;
							} else if (c2 == '^') {
								curr.onlyKeepLongestSegment = true;
							} else if (c2 == '*') {
								curr.randomize = true;
							} else if (c2 == '?') {
								curr.minorRandomize = true;
							}
						}
					}
				}
				if (curr.p0x >= 0 || curr.p1x >= 0) {
					if ((curr.randomize || curr.minorRandomize) && random != null) {
						if (!curr.reverse && !curr.minorRandomize) {
							curr.reverse = random.nextFloat() < 0.5f;
						}
						if (!curr.straight) {
							curr.straight = random.nextFloat() < 0.25f;
						}
						if (curr.burnMod == 0) {
							//curr.burnMod = (random.nextBoolean() ? 1 : -1) * random.nextInt(6);
							if (curr.minorRandomize) {
								curr.burnMod = random.nextInt(6);
							} else {
								curr.burnMod = random.nextInt(11);
							}
						}
						if (curr.type == StreamType.NORMAL) {
							float r = random.nextFloat();
							if (r < 0.2f) {
								curr.type = StreamType.NARROW;
							} else if (r < 0.4f) {
								curr.type = StreamType.WIDE;
							}
						}
					}
					streams.add(curr);
				}
			}
		}
	}
	

	public static class AddedStream {
		public CampaignTerrainAPI terrain;
		public SlipstreamTerrainPlugin2 plugin;
		public Vector2f from;
		public Vector2f to;
		public Vector2f control;
		public long timestamp;
		public AddedStream(SlipstreamTerrainPlugin2 plugin) {
			this.plugin = plugin;
			terrain = (CampaignTerrainAPI) plugin.getEntity();
			from = new Vector2f(plugin.getSegments().get(0).loc);
			to = new Vector2f(plugin.getSegments().get(plugin.getSegments().size() - 1).loc);
			timestamp = Global.getSector().getClock().getTimestamp();
		}
	}
	
	
	protected transient CollisionGridUtil grid;
	
	protected IntervalUtil interval = new IntervalUtil(1f, 2f);
	protected Random random = new Random();
	protected int prevMonth = -1;
	protected int desiredNumStreams = 0;
	protected List<AddedStream> active = new ArrayList<SlipstreamManager.AddedStream>();
	protected StreamConfig config;
	protected String prevConfig;
	
	protected Object readResolve() {
		if (active == null) {
			active = new ArrayList<SlipstreamManager.AddedStream>();
		}
		return this;
	}
	
	
	public void advance(float amount) {
		//if (true) return;
		if (DebugFlags.SLIPSTREAM_DEBUG) {
			random = Misc.random;
		}
		
//		int total = 0;
//		for (AddedStream curr : active) {
//			total += curr.plugin.getSegments().size();
//		}
//		System.out.println("TOTAL SEGMENTS: " + total + ", streams: " + active.size());
		
		float days = Global.getSector().getClock().convertToDays(amount);
		if (DebugFlags.SLIPSTREAM_DEBUG) {
			days *= 100f;
		}
		interval.advance(days);
		//DebugFlags.SLIPSTREAM_DEBUG = true;
		if (interval.intervalElapsed()) {
			Iterator<AddedStream> iter = active.iterator();
			while (iter.hasNext()) {
				AddedStream curr = iter.next();
				if (!curr.terrain.isAlive()) {
					iter.remove();
				}
			}
			
			int month = Global.getSector().getClock().getMonth();
			//month = 6;
			if (month == 6 || month == 12) {
				for (AddedStream curr : active) {
					if (curr.plugin.isDespawning()) continue;
					float despawnDelay = 0f + random.nextFloat() * 20f;
					float timeMinusDelay = 27f - despawnDelay;
					float despawnDays = timeMinusDelay * 0.5f + random.nextFloat() * timeMinusDelay * 0.5f;
					curr.plugin.despawn(despawnDelay, despawnDays, random);
				}
				if (config != null) {
					prevConfig = config.data;
				}
				config = null;
			} else if (month != 6 && month != 12) {
				if (config == null) {
					if (DebugFlags.SLIPSTREAM_DEBUG) {
						STREAM_CONFIGS.clear();
						loadConfigs();
					}
					
					WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
					for (String key : STREAM_CONFIGS.keySet()) {
						if (prevConfig != null && prevConfig.equals(key) && STREAM_CONFIGS.size() > 1) {
							continue;
						}
						picker.add(key, STREAM_CONFIGS.get(key));
					}
					if (picker.isEmpty() && prevConfig != null) {
						picker.add(prevConfig, 1f);
					}
					ListenerUtil.updateSlipstreamConfig(prevConfig, picker, this);
					String data = picker.pick();
					if (data != null) {
						config = new StreamConfig(data, random);
					}
				}
				addStream(month);
			}
			
			
			prevMonth = month;
		}
		grid = null;
	}
	
	
	public void addStream(int month) {
		if (config == null) return;
		
		//random = new Random();
//		long seed = 23895464576452L + 4384357483229348234L + 4343253L;
//		seed = 1181783497276652981L ^ seed;
//		Random random = new Random(seed);
		
		WeightedRandomPicker<StreamData> picker = new WeightedRandomPicker<StreamData>(random);
		for (StreamData data : config.streams) {
			if (data.wasUsed) continue;
			if (!data.priority) continue;
			picker.add(data);
		}
		if (picker.isEmpty()) {
			// add non-priority if all priority ones are already used
			for (StreamData data : config.streams) {
				if (data.wasUsed) continue;
				picker.add(data);
			}
		}
		
		StreamData data = picker.pick();
		if (data == null) return;
		
		SlipstreamParams2 params = new SlipstreamParams2();
		params.burnLevel = 30 + data.burnMod;
		params.minSpeed = Misc.getSpeedForBurnLevel(params.burnLevel - 5);
		params.maxSpeed = Misc.getSpeedForBurnLevel(params.burnLevel + 5);
		params.lineLengthFractionOfSpeed = 0.25f * Math.max(0.25f, Math.min(1f, 30f / (float) params.burnLevel));
		
		
		Vector2f from = data.generateP0(random);
		Vector2f to = data.generateP1(random);
		Vector2f control = data.generateControl(random);
		Vector2f control2 = data.generateControl2(random);
		if (from == null || to == null) return;

		// default direction is east in first half of the cycle, west in the second half
		// months 6 and 12 don't really matter since the slipstreams despawn during those
		if (month == 12 || month < 6) {
		//if (!(month == 12 || month < 6)) {
			if ((!data.reverse && from.x > to.x) || (data.reverse && from.x < to.x)) {
				Vector2f temp = to;
				to = from;
				from = temp;
			}
		} else {
			if ((!data.reverse && from.x < to.x) || (data.reverse && from.x > to.x)) {
				Vector2f temp = to;
				to = from;
				from = temp;
			}
		}
		
		
		LocationAPI hyperspace = Global.getSector().getHyperspace();
		CampaignTerrainAPI slipstream = (CampaignTerrainAPI) hyperspace.addTerrain(Terrain.SLIPSTREAM, params);
		
		slipstream.setLocation(from.x, from.y);
		SlipstreamTerrainPlugin2 plugin = (SlipstreamTerrainPlugin2) slipstream.getPlugin();
		SlipstreamBuilder builder = new SlipstreamBuilder(slipstream.getLocation(), plugin, data.type, random);
		
		if (data.straight) {
			float mult = 0.25f;
			builder.setMaxAngleVariance(builder.getMaxAngleVariance() * mult);
			builder.setMaxAngleVarianceForCurve(builder.getMaxAngleVarianceForCurve() * mult);
		}
		
		if (control2 != null) {
			float dist1 = Misc.getDistance(from, control);
			float dist2 = Misc.getDistance(from, control2);
			if (dist2 < dist1) {
				Vector2f temp = control2;
				control2 = control;
				control = temp;
			}
			builder.buildToDestination(control, control2, to);
		} else if (control != null) {
			builder.buildToDestination(control, to);
		} else {
			builder.buildToDestination(to);
		}
		
		checkIntersectionsAndFadeSections(plugin, data.onlyKeepLongestSegment);
		
		if (plugin.getSegments().size() < 3) {
			hyperspace.removeEntity(slipstream);
			return;
		}
		
		float spawnDays = 1f + 2f * random.nextFloat();
		if (DebugFlags.SLIPSTREAM_DEBUG) {
			spawnDays = 0f;
		}
		plugin.spawn(spawnDays, random);
		
		plugin.recomputeEncounterPoints();
		
		AddedStream added = new AddedStream(plugin);
		active.add(added);
		data.wasUsed = true;
	}
	
	public void checkIntersectionsAndFadeSections(SlipstreamTerrainPlugin2 plugin, boolean onlyKeepLongestBetweenStreams) {
		updateGrid();
		
		plugin.recomputeIfNeeded();
		
		List<SlipstreamSegment> segments = plugin.getSegments();
		
		Set<SlipstreamSegment> otherStreamCuts = new HashSet<SlipstreamSegment>();
		
		for (SlipstreamSegment curr : segments) {
			
			Iterator<Object> iter = grid.getCheckIterator(curr.loc, curr.width / 2f, curr.width / 2f);
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof JumpPointAPI) {
					JumpPointAPI jp = (JumpPointAPI) obj;
					Vector2f loc = jp.getLocation();
					float radius = jp.getRadius();
					if (jp.getOrbitFocus() != null) {
						loc = jp.getOrbitFocus().getLocation();
						radius = Misc.getDistance(jp.getOrbitFocus(), jp) + jp.getRadius();
					}
					
					Vector2f diff = Vector2f.sub(loc, curr.loc, new Vector2f());
					
					float distPerp = Math.abs(Vector2f.dot(curr.normal, diff));
					float distAlong = Math.abs(Vector2f.dot(curr.dir, diff));
					
					distPerp -= radius;
					distAlong -= radius;
					
					float minDistAlong = Math.max(curr.lengthToNext, curr.lengthToPrev);
					float fadeDistAlong = 500f + minDistAlong;
					if (distPerp < curr.width / 2f && 
							distAlong < fadeDistAlong) {
						if (distAlong < minDistAlong) {
							curr.fader.forceOut();
							curr.bMult = 0f;
						} else {
							curr.bMult = Math.min(curr.bMult, 
												 (distAlong - minDistAlong) / (fadeDistAlong - minDistAlong));
						}
					}
				} else if (obj instanceof CampaignTerrainAPI) {
					CampaignTerrainAPI terrain = (CampaignTerrainAPI) obj;
					SlipstreamTerrainPlugin2 otherPlugin = (SlipstreamTerrainPlugin2) terrain.getPlugin();
					if (otherPlugin == plugin) continue;
					
					for (SlipstreamSegment other : otherPlugin.getSegmentsNear(curr.loc, curr.width / 2f)) {
						//if (other.fader.getBrightness() == 0 || other.bMult <= 0) continue;
						if (other.bMult <= 0) continue;
						
						float dist = Misc.getDistance(curr.loc, other.loc);
						float minDist = curr.width / 2f + other.width / 2f;
						float fadeDist = minDist + 500f;
						if (dist < fadeDist) {
							if (dist < minDist) {
								curr.fader.forceOut();
								curr.bMult = 0f;
								otherStreamCuts.add(curr);
							} else {
								curr.bMult = Math.min(curr.bMult, 
													 (dist - minDist) / (fadeDist - minDist));
							}
						}
					}
				} else if (obj instanceof CustomStreamBlocker) {
					CustomStreamBlocker blocker = (CustomStreamBlocker) obj;
					Vector2f loc = blocker.loc;
					float radius = blocker.radius;
					
					Vector2f diff = Vector2f.sub(loc, curr.loc, new Vector2f());
					float distPerp = Math.abs(Vector2f.dot(curr.normal, diff));
					float distAlong = Math.abs(Vector2f.dot(curr.dir, diff));
					
					distPerp -= radius;
					distAlong -= radius;
					
					float minDistAlong = Math.max(curr.lengthToNext, curr.lengthToPrev);
					float fadeDistAlong = 500f + minDistAlong;
					if (distPerp < curr.width / 2f && 
							distAlong < fadeDistAlong) {
						if (distAlong < minDistAlong) {
							curr.fader.forceOut();
							curr.bMult = 0f;
						} else {
							curr.bMult = Math.min(curr.bMult, 
												 (distAlong - minDistAlong) / (fadeDistAlong - minDistAlong));
						}
					}
				}
			}
		}

		fadeOutSectionsShorterThan(segments, 5000f);
		
		// unrelated to the above - for proximity-to-something fades that
		// were unnecessary because there wasn't an actual intersection with that something
		removedFadesThatDoNotReachZero(segments);
		
		
		
		if (onlyKeepLongestBetweenStreams) {
			List<SlipstreamSegment> longest = new ArrayList<SlipstreamTerrainPlugin2.SlipstreamSegment>();
			
			List<SlipstreamSegment> currList = new ArrayList<SlipstreamTerrainPlugin2.SlipstreamSegment>();
			
			for (SlipstreamSegment curr : segments) {
				if (otherStreamCuts.contains(curr)) {
					if (currList.size() > longest.size()) {
						longest = currList;
					}
					currList = new ArrayList<SlipstreamSegment>();
				}
				if (curr.bMult > 0f) {
					currList.add(curr);
				}
			}
			if (currList.size() > longest.size()) {
				longest = currList;
			}
			for (SlipstreamSegment curr : segments) {
				if (!longest.contains(curr)) {
					curr.bMult = 0f;
					curr.fader.forceOut();
				}
			}
			
		}
	}
	
	
	public static void fadeOutSectionsShorterThan(List<SlipstreamSegment> segments, float minLength) {
		float minRunLength = minLength;
		List<SlipstreamSegment> currRun = new ArrayList<SlipstreamSegment>();
		for (SlipstreamSegment curr : segments) {
			if (curr.bMult <= 0f) {
				float runLength = 0f;
				for (SlipstreamSegment inRun : currRun) {
					runLength += inRun.lengthToNext; // counts one more than it should; meh 
				}
				if (runLength < minRunLength) {
					for (SlipstreamSegment inRun : currRun) {
						inRun.fader.forceOut();
						inRun.bMult = 0f;
					}
				}
				currRun.clear();
			} else {
				currRun.add(curr);
			}
		}
	}
	
	public static void removedFadesThatDoNotReachZero(List<SlipstreamSegment> segments) {
		List<SlipstreamSegment> currRun = new ArrayList<SlipstreamSegment>();
		boolean currRunReachedZero = false;
		for (SlipstreamSegment curr : segments) {
			if (curr.bMult < 1f) {
				currRun.add(curr);
				if (curr.bMult <= 0f || (curr.fader.getBrightness() == 0 && !curr.fader.isFadingIn())) {
					currRunReachedZero = true;
				}
			} else {
				if (!currRunReachedZero) {
					for (SlipstreamSegment inRun : currRun) {
						inRun.fader.fadeIn();
						inRun.bMult = 1f;
					}
				}
				currRun.clear();
				currRunReachedZero = false;
			} 
		}
	}
	
	
	
	public static class CustomStreamRevealer extends CustomStreamBlocker {
		public CustomStreamRevealer(Vector2f loc, float radius) {
			super(loc, radius);
		}
	}
	
	public static class CustomStreamBlocker {
		public Vector2f loc;
		public float radius;
		public CustomStreamBlocker(Vector2f loc, float radius) {
			this.loc = new Vector2f(loc);
			this.radius = radius;
		}
	}
	
	public CollisionGridUtil getGrid() {
		return grid;
	}

	public void updateGrid() {
		if (grid != null) return;
		
		float sw = Global.getSettings().getFloat("sectorWidth");
		float sh = Global.getSettings().getFloat("sectorHeight");
		float minCellSize = 12000f;
		float cellSize = Math.max(minCellSize, sw * 0.05f);
		
		grid = new CollisionGridUtil(-sw/2f, sw/2f, -sh/2f, sh/2f, cellSize);
		
		LocationAPI hyperspace = Global.getSector().getHyperspace();
		for (SectorEntityToken jp : hyperspace.getJumpPoints()) {
			float size = jp.getRadius() * 2f + 100f;
			grid.addObject(jp, jp.getLocation(), size * 2f, size * 2f);
		}
		
//		for (NascentGravityWellAPI well : hyperspace.getGravityWells()) {
//			float size = 1000f + well.getRadius();
//			CustomStreamBlocker blocker = new CustomStreamBlocker(well.getLocation(), size);
//			grid.addObject(blocker, well.getLocation(), size * 2f, size * 2f);
//		}
		Object alphaSiteWell = Global.getSector().getMemoryWithoutUpdate().get(TTBlackSite.NASCENT_WELL_KEY);
		if (alphaSiteWell instanceof NascentGravityWellAPI) {
			NascentGravityWellAPI well = (NascentGravityWellAPI) alphaSiteWell;
			float size = 1000f + well.getRadius();
			CustomStreamBlocker blocker = new CustomStreamBlocker(well.getLocation(), size);
			grid.addObject(blocker, well.getLocation(), size * 2f, size * 2f);
		}
		
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (system.hasTag(Tags.THEME_CORE)) {
				Vector2f loc = system.getLocation();
				float size = 4000f;
				CustomStreamBlocker blocker = new CustomStreamBlocker(loc, size);
				grid.addObject(blocker, loc, size * 2f, size * 2f);
			}
		}
		
//		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
//			if (market.isHidden()) continue;
//			//if (market.hasIndustry(Industries.SPACEPORT)) continue;
//			Industry spaceport = market.getIndustry(Industries.SPACEPORT);
//			if (spaceport == null || !spaceport.isFunctional()) continue;
//			
//			Vector2f loc = market.getLocationInHyperspace();
//			float size = 5000f;
//			CustomStreamRevealer revealer = new CustomStreamRevealer(loc, size);
//			grid.addObject(revealer, loc, size * 2f, size * 2f);
//		}
		
		
		int segmentsToSkip = (int) ((cellSize - 2000) / 400f);
		float checkSize = minCellSize - 2000f;
		
		for (CampaignTerrainAPI curr : hyperspace.getTerrainCopy()) {
			if (curr.getPlugin() instanceof SlipstreamTerrainPlugin2) {
				SlipstreamTerrainPlugin2 plugin = (SlipstreamTerrainPlugin2) curr.getPlugin();
				List<SlipstreamSegment> segments = plugin.getSegments();
				List<SlipstreamSegment> check = new ArrayList<SlipstreamTerrainPlugin2.SlipstreamSegment>();
				for (int i = 0; i < segments.size(); i += segmentsToSkip) {
					check.add(segments.get(i));
				}
				if (!check.contains(segments.get(segments.size() - 1))) {
					check.add(segments.get(segments.size() - 1));
				}
				
				for (SlipstreamSegment seg : check) {
					grid.addObject(curr, seg.loc, checkSize, checkSize);
				}
			}
		}
		
		ListenerUtil.updateSlipstreamBlockers(grid, this);
	}
	
	
	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
	
	
	
}
