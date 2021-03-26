package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TerrainAIFlags;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.abilities.EmergencyBurnAbility;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.FlickerUtilV2;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class HyperspaceTerrainPlugin extends BaseTiledTerrain { // implements NebulaTextureProvider {
	
	
//	public static float MIN_BURN_PENALTY = 0.1f;
//	public static float BURN_PENALTY_RANGE = 0.4f;
	public static final float VISIBLITY_MULT = 0.5f;
	
	
	public static final float STORM_STRIKE_SOUND_RANGE = 1500f;
	
	public static float STORM_MIN_TIMEOUT = 0.4f;
	public static float STORM_MAX_TIMEOUT = 0.6f;
	//public static float STORM_STRIKE_CHANCE = 0.5f;
	public static float STORM_DAMAGE_FRACTION = 0.3f;
	public static float STORM_MIN_STRIKE_DAMAGE = 0.05f;
	public static float STORM_MAX_STRIKE_DAMAGE = 0.95f;
	
	//public static final float STORM_CR_LOSS_MULT = 0.25f;
	
	public static final float FUEL_USE_FRACTION = 1f;
	//public static final float SPEED_MULT = 1f;
	//public static final float STORM_SPEED_MULT = 0.2f;
	public static final float STORM_SPEED_MULT = 1f;
	public static final float STORM_SENSOR_RANGE_MULT = 1f;
	public static final float STORM_VISIBILITY_FLAT = 0f;
	
	
	public static float TILE_SIZE = 200;
	
	public static final CampaignEngineLayers FLASH = CampaignEngineLayers.TERRAIN_6A;
	public static final CampaignEngineLayers FLASH_OVER = CampaignEngineLayers.TERRAIN_9;
	public static final CampaignEngineLayers GLOW = CampaignEngineLayers.TERRAIN_8;
	public static final CampaignEngineLayers BASE = CampaignEngineLayers.TERRAIN_6;
	//public static final CampaignEngineLayers OVER = CampaignEngineLayers.TERRAIN_6B;
	public static final CampaignEngineLayers SHIVER = CampaignEngineLayers.TERRAIN_9;
	public static final CampaignEngineLayers BASE_OVER = CampaignEngineLayers.TERRAIN_7;

	public static enum LocationState {
		OPEN,
		DEEP,
		DEEP_STORM,
	}
	
	public static enum CellState {
		OFF,
		WAIT,
		SIGNAL,
		STORM,
		STORM_WANE,
	}
	public static class CellStateTracker {
		public int i, j;
		public CellState state;
		public float wait, signal, wane;
		private float maxSignal, maxWane;
		public FlickerUtilV2 flicker = null;
		public CellStateTracker(int i, int j, float wait, float signal) {
			this.i = i;
			this.j = j;
			this.wait = wait;
			this.signal = signal;
			this.maxSignal = signal;
			state = CellState.WAIT;
		}

		
		public void advance(float days) {
			if (state == CellState.OFF) return;
			
			if (state == CellState.WAIT  && days > 0) {
				wait -= days;
				if (wait <= 0) {
					days = -wait;
					wait = 0;
					state = CellState.SIGNAL;
				}
			}
			
			if (state == CellState.SIGNAL && days > 0) {
				signal -= days;
				if (signal <= 0) {
					days = -signal;
					signal = 0;
					state = CellState.STORM;
					flicker = new FlickerUtilV2();
					flicker.newBurst();
				}
			}
			
			if (state == CellState.STORM || state == CellState.STORM_WANE) {
				signal -= days; // needed for signal brightness to fade
			}

			if (state == CellState.STORM_WANE && days > 0) {
				wane -= days;
				if (wane <= 0) {
					days = -wane;
					wane = 0;
					if (flicker == null || flicker.getBrightness() <= 0) {
						state = CellState.OFF;
					} else {
						flicker.stop();
					}
				}
			}
			
			if (flicker != null) {
				flicker.advance(days * 7f);
			}
		}
		
		public float getSignalBrightness() {
			if (state == CellState.SIGNAL) {
				return 1f - signal / maxSignal;
			}
//			if (state == CellState.STORM || state == CellState.STORM_WANE) {
//				//System.out.println(Math.max(0, 1 + signal * 10));
//				return Math.max(0, 1 + signal * 10); // fade out over 1 second, signal is negative here
//			}
			if (state == CellState.STORM) {
				return 1f;
			}
			if (state == CellState.STORM_WANE) {
				float fade = maxWane * 0.25f;
				if (wane > fade) {
					return 1f;
				}
				return Math.max(0, wane / fade);
			}
			return 0f;
		}

		public void wane(float wane) {
			this.wane = wane;
			this.maxWane = wane;
			state = CellState.STORM_WANE;
		}
		
		public boolean isOff() {
			return state == CellState.OFF;
		}
		
		public boolean isStorming() {
			return state == CellState.STORM || state == CellState.STORM_WANE;
		}
		
		public boolean isWaning() {
			return state == CellState.STORM_WANE;
		}
		
		public boolean isSignaling() {
			return state == CellState.SIGNAL;
		}
	}
	
	protected transient SpriteAPI flickerTexture;
	
	protected transient CellStateTracker [][] activeCells;
	protected List<CellStateTracker> savedActiveCells = new ArrayList<CellStateTracker>();
	
	protected HyperspaceAutomaton auto;
	
	protected transient String stormSoundId = null;
	
	public void init(String terrainId, SectorEntityToken entity, Object param) {
		super.init(terrainId, entity, param);
	}
	
	protected Object readResolve() {
		super.readResolve();
		layers = EnumSet.of(BASE, FLASH, GLOW, SHIVER, BASE_OVER, FLASH_OVER);
		
		if (auto == null) {
			//auto = new HyperspaceAutomaton(params.w, params.h, 0.75f, 1.25f);
			auto = new HyperspaceAutomaton(params.w, params.h, 1.5f, 2.5f);
		}
		
		flickerTexture = Global.getSettings().getSprite(params.cat, params.key + "_glow");
		if (activeCells == null) {
			activeCells = new CellStateTracker[params.w][params.h];
			
			if (savedActiveCells != null) {
				for (CellStateTracker curr : savedActiveCells) {
					activeCells[curr.i][curr.j] = curr; 
				}
			}
		}
		
		// init cells to random mid-storm state where appropriate
		int [][] cells = auto.getCells();
		
		for (int i = 0; i < activeCells.length; i++) {
			for (int j = 0; j < activeCells[0].length; j++) {
				if (tiles[i][j] < 0) continue;
				
				CellStateTracker curr = activeCells[i][j];
				int val = cells[i][j];
				float interval = auto.getInterval().getIntervalDuration();
				
				if (val == 1 && curr == null) {
					curr = activeCells[i][j] = new CellStateTracker(i, j, 
							interval * 0f + interval * 1.5f * (float) Math.random(),
							interval * 0.5f + interval * 0.5f * (float) Math.random());
					
					float dur = (float) Math.random() * interval * 2.5f;
					curr.advance(dur);
				}
			}
		}
			
		stormSoundId = getSpec().getCustom().optString("stormSound", null);
		return this;
	}
	
	public CellStateTracker[][] getActiveCells() {
		return activeCells;
	}
	

	Object writeReplace() {
		HyperspaceTerrainPlugin copy = (HyperspaceTerrainPlugin) super.writeReplace();
		
		copy.savedActiveCells = new ArrayList<CellStateTracker>();
		for (int i = 0; i < copy.activeCells.length; i++) {
			for (int j = 0; j < copy.activeCells[0].length; j++) {
				CellStateTracker curr = copy.activeCells[i][j];
				if (curr != null && isTileVisible(i, j)) {
					copy.savedActiveCells.add(curr);
				}
			}
		}
		return copy;
	}
	
	transient private EnumSet<CampaignEngineLayers> layers = EnumSet.of(BASE, FLASH, GLOW, SHIVER, BASE_OVER, FLASH_OVER);
	public EnumSet<CampaignEngineLayers> getActiveLayers() {
		return layers;
	}
	
	
	protected transient float [] temp = new float[2];
	protected float[] getThetaAndRadius(Random rand, float width, float height) {
		if (temp == null) temp = new float[2];
		
		//if (true) return temp;
		float speedFactor = 0.5f;
		
		float time = elapsed * Global.getSector().getClock().getSecondsPerDay();
		float min = -360f * (rand.nextFloat() * 3f + 1f) * Misc.RAD_PER_DEG;
		float max = 360f * (rand.nextFloat() * 3f + 1f) * Misc.RAD_PER_DEG;
		float rate = (30f + 70f * rand.nextFloat()) * Misc.RAD_PER_DEG;
		rate *= speedFactor;
		float period = 2f * (max - min) / rate;
		float progress = rand.nextFloat() + time / period;
		progress = progress - (int) progress;
		
		float theta, radius;
		if (progress < 0.5f) {
			theta = min + (max - min) * progress * 2f;
		} else {
			theta = min + (max - min) * (1f - progress) * 2f;
		}
		temp[0] = theta;
		
		min = 0f;
		max = (width + height) * 0.025f;
		rate = max * 0.5f;
		rate *= speedFactor;
		
		period = 2f * (max - min) / rate;
		progress = rand.nextFloat() + time / period;
		progress = progress - (int) progress;
		if (progress < 0.5f) {
			radius = min + (max - min) * progress * 2f;
		} else {
			radius = min + (max - min) * (1f - progress) * 2f;
		}
		temp[1] = radius;
		
		return temp;
		
//		float twoPI = (float) Math.PI * 2f;
//		float maxRad = (width + height) * 0.025f;
//		float speedFactor = 0.5f;
//		float sign1 = rand.nextFloat() > 0.5f ? 1f : -1f;
//		float speed1 = (0.5f + rand.nextFloat()) * twoPI * speedFactor; 
//		float theta1 = rand.nextFloat() * twoPI + speed1 * elapsed * sign1;
//		float radius1 = (0.5f + rand.nextFloat()) * maxRad;
//		temp[0] = theta1;
//		temp[1] = radius1;
//		return temp;
	}
	
	@Override
	protected void renderQuad(int i, int j, float x, float y, float width, float height,
							  float texX, float texY, float texW, float texH, float angle) {
		
		if (currLayer == null) {
			super.renderQuad(i, j, x, y, width, height, texX, texY, texW, texH, angle);
			return;
		}
		
		if (currLayer == FLASH_OVER) return;
		if (currLayer == SHIVER) return;
		//if (currLayer == BASE) return;
		//if (currLayer == BASE_OVER) return;
		//if (currLayer == FLASH) return;
		//if (currLayer == GLOW) return;
		

		CellStateTracker tracker = activeCells[i][j];
		float signal = 0f;
		if (tracker != null) {
			signal = tracker.getSignalBrightness();
		}
		if (currLayer == FLASH && (tracker == null || tracker.flicker == null || tracker.flicker.getBrightness() <= 0)) {
			return;
		}
		
		if (currLayer == GLOW && signal <= 0) {
			return;
		}
		
		
//		if (currLayer != HIGHLIGHT) return;
//		if (currLayer != UNDER) return;
//		if (currLayer == HIGHLIGHT) return;
//		if (currLayer != GLOW) return;
//		if (currLayer != OVER) return;
		
		//if (currLayer != BASE) return;
		//if (currLayer != BASE && currLayer != BASE_OVER) return;
		
		long seed = (long) (x + y * tiles.length) * 1000000;
//		if (currLayer == BASE_OVER) {
//			seed /= (long) 4123;
//		}
		
		Random rand = new Random(seed);
		angle = rand.nextFloat() * 360f;
		
		Color color = getRenderColor();
		
		float [] tr = getThetaAndRadius(rand, width, height);
		float theta1 = tr[0];
		float radius1 = tr[1];
		float sin1 = (float) Math.sin(theta1);
		float cos1 = (float) Math.cos(theta1);
		
		tr = getThetaAndRadius(rand, width, height);
		float theta2 = tr[0];
		float radius2 = tr[1];
		float sin2 = (float) Math.sin(theta2);
		float cos2 = (float) Math.cos(theta2);
		
		tr = getThetaAndRadius(rand, width, height);
		float theta3 = tr[0];
		float radius3 = tr[1];
		float sin3 = (float) Math.sin(theta3);
		float cos3 = (float) Math.cos(theta3);
		
		tr = getThetaAndRadius(rand, width, height);
		float theta4 = tr[0];
		float radius4 = tr[1];
		float sin4 = (float) Math.sin(theta4);
		float cos4 = (float) Math.cos(theta4);
		
		
		
		float vw = width / 2f;
		float vh = height / 2f;
		
		
		float cx = x + vw;
		float cy = y + vh;
		
//		vw *= 0.5f;
//		vh *= 0.5f;
		
		float cos = (float) Math.cos(angle * Misc.RAD_PER_DEG);
		float sin = (float) Math.sin(angle * Misc.RAD_PER_DEG);

		float shiverThreshold = 0.75f;
		
		boolean shiver = false;
		boolean flicker = false;
		
		//System.out.println("Layer: " + currLayer);
		if (currLayer == FLASH || currLayer == FLASH_OVER) {
			if (tracker != null && tracker.flicker != null && tracker.flicker.getBrightness() > 0) {
				flicker = true;
			}
		} else if (currLayer == BASE) {
			if (!currLayerColorSet) {
				currLayerColorSet = true;
				GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)((float)color.getAlpha() * currAlpha * 1f));
			}
		} else if (currLayer == BASE_OVER) {
			if (!currLayerColorSet) {
				currLayerColorSet = true;
				GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)((float)color.getAlpha() * currAlpha * 1f));			
			}
		} else if (currLayer == GLOW) {
			if (tracker != null && signal > 0) {
				GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)((float)color.getAlpha() * currAlpha * 1f * signal));
			} else {
				return;
			}
		} else if (currLayer == SHIVER) {
			if (signal > shiverThreshold && tracker != null && tracker.flicker == null) {
				shiver = true;
			}
		} else {
			return; // under layer, but not "live"
		}
		
		if (currLayer == GLOW || currLayer == BASE || currLayer == BASE_OVER) {
			//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			//if (true) return;
			int iter = 1;
			if (currLayer == GLOW) iter = 1;
			for (int k = 0; k < iter; k++) {
				GL11.glTexCoord2f(texX, texY);
				GL11.glVertex2f(cx + (-vw * cos + vh * sin) + sin1 * radius1,
								cy + (-vw * sin - vh * cos) + cos1 * radius1);
		
				GL11.glTexCoord2f(texX, texY + texH);
				GL11.glVertex2f(cx + (-vw * cos - vh * sin) + sin2 * radius2, 
								cy + (-vw * sin + vh * cos) + cos2 * radius2);
				
				GL11.glTexCoord2f(texX + texW, texY + texH);
				GL11.glVertex2f(cx + (vw * cos - vh * sin) + sin3 * radius3,
								cy + (vw * sin + vh * cos) + cos3 * radius3);
		
				GL11.glTexCoord2f(texX + texW, texY);
				GL11.glVertex2f(cx + (vw * cos + vh * sin) + sin4 * radius4,
								cy + (vw * sin - vh * cos) + cos4 * radius4);
			}
		}
		

		if (flicker || shiver) {
			if (tracker == null) return;
			if (shiver) return;
			//float shiverBrightness = tracker.getBrightness();
			float shiverBrightness = (signal - shiverThreshold) / (1f - shiverThreshold);
			if (shiverBrightness > 0.9f) {
				shiverBrightness = (1f - shiverBrightness) / 0.1f;
			} else {
				shiverBrightness /= 0.9f;
			}
			//shiverBrightness *= shiverBrightness;
			//shiverBrightness = 1f;
			float ox = cx;
			float oy = cy;
			//float maxJitter = 0f + 30f * shiverBrightness * shiverBrightness;
			float maxJitter = 0f + 30f;
			//maxJitter = 0f;
			if (shiver) {
				rand.setSeed((long) (x + y * tiles.length) * 1000000 + Global.getSector().getClock().getTimestamp());
				maxJitter = 0f + 30f * shiverBrightness * shiverBrightness;
			} else {
				rand.setSeed((long) (x + y * tiles.length) * 1000000 + 
						(long) (tracker.flicker.getAngle() * 1000));
			}
			maxJitter *= 5f;
			if (shiver) {
//				vw *= 0.75f;
//				vh *= 0.75f;
			}
			if (flicker) {
				//maxJitter = 0f;
				//maxJitter *= 0.5f;
				vw *= 1.5f;
				vh *= 1.5f;
			}
	
			//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			//flickerTexture.bindTexture();
			if (flicker) {
				float alpha = currAlpha;
				if (currLayer == FLASH_OVER) {
					alpha *= 0.25f;
				}
				GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						//(byte)((float)color.getAlpha() * currAlpha * shiverBrightness * 0.5f));
						(byte)((float)color.getAlpha() * alpha * tracker.flicker.getBrightness() * 1f));
				//System.out.println(tracker.flicker.getBrightness());
			} else if (shiver) {
				GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)((float)color.getAlpha() * currAlpha * shiverBrightness * 0.075f));
			}
			
			int maxIter = 1;
			if (shiver) maxIter = 5;
			for (int iter = 0; iter < maxIter; iter++) {
				cx = ox + rand.nextFloat() * maxJitter - maxJitter/2f;
				cy = oy + rand.nextFloat() * maxJitter - maxJitter/2f;
				
				GL11.glTexCoord2f(texX, texY);
				GL11.glVertex2f(cx + (-vw * cos + vh * sin) + sin1 * radius1,
								cy + (-vw * sin - vh * cos) + cos1 * radius1);
				
				cx = ox + rand.nextFloat() * maxJitter - maxJitter/2f;
				cy = oy + rand.nextFloat() * maxJitter - maxJitter/2f;
		
				GL11.glTexCoord2f(texX, texY + texH);
				GL11.glVertex2f(cx + (-vw * cos - vh * sin) + sin2 * radius2, 
								cy + (-vw * sin + vh * cos) + cos2 * radius2);
				
				cx = ox + rand.nextFloat() * maxJitter - maxJitter/2f;
				cy = oy + rand.nextFloat() * maxJitter - maxJitter/2f;
		
				GL11.glTexCoord2f(texX + texW, texY + texH);
				GL11.glVertex2f(cx + (vw * cos - vh * sin) + sin3 * radius3,
								cy + (vw * sin + vh * cos) + cos3 * radius3);
				
				cx = ox + rand.nextFloat() * maxJitter - maxJitter/2f;
				cy = oy + rand.nextFloat() * maxJitter - maxJitter/2f;
		
				GL11.glTexCoord2f(texX + texW, texY);
				GL11.glVertex2f(cx + (vw * cos + vh * sin) + sin4 * radius4,
								cy + (vw * sin - vh * cos) + cos4 * radius4);
			}
		}
	}
	
	

	public String getNebulaMapTex() {
		return Global.getSettings().getSpriteName(params.cat, params.key + "_map");
	}

	public String getNebulaTex() {
		return Global.getSettings().getSpriteName(params.cat, params.key);
	}
	
	public void advance(float amount) {
		//if (true) return;
		super.advance(amount);

		playStormStrikeSoundsIfNeeded();
		
		float days = Global.getSector().getClock().convertToDays(amount);
//		for (int i = 0; i < 100; i++) {
//			auto.advance(days * 10f);
//		}
		auto.advance(days * 1f);
		
		int [][] cells = auto.getCells();
		
//		int count = 0;
//		for (int i = 0; i < activeCells.length; i++) {
//			for (int j = 0; j < activeCells[0].length; j++) {
//				if (tiles[i][j] < 0) continue;
//				CellStateTracker curr = activeCells[i][j];
//				if (curr != null) {
//					count++;
//				}
//			}
//		}	
//		System.out.println("Count: " + count + "(out of " + (activeCells.length * activeCells[0].length));
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		Vector2f test = new Vector2f();
		if (playerFleet != null) {
			test = playerFleet.getLocation();
		}
		
		float x = this.entity.getLocation().x;
		float y = this.entity.getLocation().y;
		float size = getTileSize();
		
		float w = tiles.length * size;
		float h = tiles[0].length * size;
		x -= w/2f;
		y -= h/2f;
		int xIndex = (int) ((test.x - x) / size);
		int yIndex = (int) ((test.y - y) / size);
		if (xIndex < 0) xIndex = 0;
		if (yIndex < 0) yIndex = 0;
		if (xIndex >= tiles.length) xIndex = tiles.length - 1;
		if (yIndex >= tiles[0].length) yIndex = tiles[0].length - 1;
		
		int subgridSize = (int) ((10000 / size + 1) * 2f);
		
		int minX = Math.max(0, xIndex - subgridSize/2);
		int maxX = xIndex + subgridSize/2 ;
		int minY = Math.max(0, yIndex - subgridSize/2);
		int maxY = yIndex + subgridSize/2;
		
		// clean up area around the "active" area so that as the player moves around,
		// they don't leave frozen storm cells behind (which would then make it into the savefile)
		int pad = 4;
		for (int i = minX - pad; i <= maxX + pad && i < tiles.length; i++) {
			for (int j = minY - pad; j <= maxY + pad && j < tiles[0].length; j++) {
				if (i < minX || j < minY || i > maxX || j > maxY) {
					if (i >= 0 && j >= 0) {
						activeCells[i][j] = null;
					}
				}
			}
		}
		
		for (int i = minX; i <= maxX && i < tiles.length; i++) {
			for (int j = minY; j <= maxY && j < tiles[0].length; j++) {
//		for (int i = 0; i < activeCells.length; i++) {
//			for (int j = 0; j < activeCells[0].length; j++) {
				if (tiles[i][j] < 0) continue;
				
				CellStateTracker curr = activeCells[i][j];
				int val = cells[i][j];
				float interval = auto.getInterval().getIntervalDuration();
				
				if (val == 1 && curr == null) {
					curr = activeCells[i][j] = new CellStateTracker(i, j, 
							interval * 0f + interval * 1.5f * (float) Math.random(),
							interval * 0.5f + interval * 0.5f * (float) Math.random());
//							interval * 0f + interval * 0.5f * (float) Math.random(),
//							interval * 0.25f + interval * 0.25f * (float) Math.random());
				}
				
				if (curr != null) {
					if (val != 1 && curr.isStorming() && !curr.isWaning()) {
						//curr.wane(interval * 0.25f + interval * 0.25f * (float) Math.random());
						curr.wane(interval * 0.5f + interval * 0.5f * (float) Math.random());
//						curr.wane(interval * 0.5f * (float) Math.random() + 
//								  interval * 0.25f + interval * 0.25f * (float) Math.random());
					}
					
					curr.advance(days);
					if (curr.isOff()) {
						activeCells[i][j] = null;
					}
				}
			}
		}
		
	}
	
	
	protected void playStormStrikeSoundsIfNeeded() {
		if (stormSoundId == null) return;
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		if (playerFleet.getContainingLocation() != entity.getContainingLocation()) return;
		
		Vector2f test = playerFleet.getLocation();
		
		float x = this.entity.getLocation().x;
		float y = this.entity.getLocation().y;
		float size = getTileSize();
		
		float w = tiles.length * size;
		float h = tiles[0].length * size;

		x -= w/2f;
		y -= h/2f;
		
		int xIndex = (int) ((test.x - x) / size);
		int yIndex = (int) ((test.y - y) / size);
		
		if (xIndex < 0) xIndex = 0;
		if (yIndex < 0) yIndex = 0;
		
		if (xIndex >= tiles.length) xIndex = tiles.length - 1;
		if (yIndex >= tiles[0].length) yIndex = tiles[0].length - 1;
		
		int subgridSize = (int) ((STORM_STRIKE_SOUND_RANGE / size + 1) * 2f);
		
		for (float i = Math.max(0, xIndex - subgridSize/2); i <= xIndex + subgridSize/2 && i < tiles.length; i++) {
			for (float j = Math.max(0, yIndex - subgridSize/2); j <= yIndex + subgridSize/2 && j < tiles[0].length; j++) {
				int texIndex = tiles[(int) i][(int) j];
				if (texIndex >= 0) {
					float tcx = x + i * size + size/2f;
					float tcy = y + j * size + size/2f;
					Vector2f tileLoc = new Vector2f(tcx, tcy);
				
					CellStateTracker curr = activeCells[(int)i][(int)j];
					if (curr == null || curr.flicker == null || !curr.isStorming() || !curr.flicker.isPeakFrame() || curr.flicker.getNumBursts() > 1) continue;
					
					float dist = Misc.getDistance(test, tileLoc);
					if (dist > STORM_STRIKE_SOUND_RANGE) continue;

					// will be attenuated without this, but there's "too much" lightning sound without
					// this additional attenuation
					float volumeMult = 1f - (dist / STORM_STRIKE_SOUND_RANGE);
					volumeMult = (float) Math.sqrt(volumeMult);
					if (volumeMult <= 0) continue;
					//float volumeMult = 1f;
					//volumeMult *= 0.67f;
					Global.getSoundPlayer().playSound(stormSoundId, 1f, 1f * volumeMult, tileLoc, Misc.ZERO);
				}
			}
		}
		
	}
	
//	protected void spawnWavefront(int i, int j) {
//		if (true) return;
//		float [] center = getTileCenter(i, j);
//		
//		float angle;
//		float spread = 90f;
//		int yLoc = (int) center[1];
//		yLoc = yLoc / 4000;
//		if (yLoc % 2 == 0) {
//			angle = 0 - spread / 2f + (float) Math.random() * spread; 
//		} else {
//			angle = 180 - spread / 2f + (float) Math.random() * spread;
//		}
//	
//		float initialRange = 400f;
//		Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
//		loc.scale(50f);
//		loc.x += center[0];
//		loc.y += center[1];
//		
//		float burnLevel = (float) (7f + 8f * Math.random());
//		burnLevel = Math.round(burnLevel);
//		
//		float r = (float) Math.random();
//		r *= r;
//		float durDays = 1f + r * 2f;
//		float width = 300f + 500f * (float) Math.random();
//		SectorEntityToken wave = entity.getContainingLocation().addTerrain(
//				Terrain.WAVEFRONT,
//				new WavefrontParams(burnLevel, // burn level
//						1f, // CR loss multiplier
//						initialRange, // "origin" range, controls curve of wave
//						width, 100, // width and width expansion
//						200f, 10, // thickness and thickness expansion
//						durDays, // duration days
//						angle // angle
//				) {
//
//				});
//		wave.getLocation().set(loc.x, loc.y);
//	}
	
	
		

	private transient CampaignEngineLayers currLayer = null;
	private transient boolean currLayerColorSet = false;
	private transient float currAlpha = 1f;
	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		//if (true) return;
		currLayer = layer;
		//currLayerColorSet = false;
		super.render(layer, viewport);
	}
	
	@Override
	public void renderOnMap(float factor, float alphaMult) {
		currLayer = null;
		//currLayerColorSet = false;
		super.renderOnMap(factor, alphaMult);
	}

	

	@Override
	public float getTileRenderSize() {
		//return TILE_SIZE + 300f;
		//return TILE_SIZE + 600f;
		return TILE_SIZE * 2.5f;
	}
	
	@Override
	public float getTileContainsSize() {
		//return TILE_SIZE + 200f;
		return TILE_SIZE * 1.5f;
	}

	@Override
	public float getTileSize() {
		return TILE_SIZE;
	}
	
	@Override
	protected void renderSubArea(float startColumn, float endColumn,
			float startRow, float endRow, float factor, int samples,
			float alphaMult) {
		super.renderSubArea(startColumn, endColumn, startRow, endRow, factor, samples, alphaMult);
	}

	@Override
	public void preRender(CampaignEngineLayers layer, float alphaMult) {
		GL11.glEnable(GL11.GL_BLEND);
		
		//System.out.println("Layer: " + layer);
		
		if (layer == FLASH || layer == FLASH_OVER) {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			flickerTexture.bindTexture();
		} else {
			if (layer == GLOW || layer == SHIVER || layer == BASE) {
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			} else {
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			}
			if (layer == SHIVER) {
				flickerTexture.bindTexture();
			}
		}
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
//		if (layer == UPPER) {
//			alphaMult *= 0.30f;
//		}
		
		currAlpha = alphaMult;
		currLayerColorSet = false;
//		Color color = getRenderColor();
//		GL11.glColor4ub((byte)color.getRed(),
//				(byte)color.getGreen(),
//				(byte)color.getBlue(),
//				(byte)((float)color.getAlpha() * alphaMult));
	}
	
	@Override
	public void preMapRender(float alphaMult) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		
		//Color color = new Color(125,125,200,255);
		//Color color = new Color(100,100,150,255);
		currAlpha = alphaMult;
		currLayerColorSet = false;
		
		Color color = getRenderColor();
		GL11.glColor4ub((byte)color.getRed(),
				(byte)color.getGreen(),
				(byte)color.getBlue(),
				(byte)((float)color.getAlpha() * alphaMult));
	}
	
	
	public void renderOnRadar(Vector2f radarCenter, float factor, float alphaMult) {
		currLayer = null;
		//if (true) return;
		
		float radius = Global.getSettings().getFloat("campaignRadarRadius") + 2000;
		
		GL11.glPushMatrix();
		GL11.glTranslatef(-radarCenter.x * factor, -radarCenter.y * factor, 0);
		//super.renderOnMap(factor, alphaMult);
		
		preMapRender(alphaMult);
		//GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		int samples = 10;
		
		float x = this.entity.getLocation().x;
		float y = this.entity.getLocation().y;
		float size = getTileSize();
		float renderSize = getTileRenderSize();
		
		float w = tiles.length * size;
		float h = tiles[0].length * size;
		x -= w/2f;
		y -= h/2f;
		float extra = (renderSize - size) / 2f + 100f;
		
		float llx = radarCenter.x - radius;
		float lly = radarCenter.y - radius;
		float vw = radius * 2f;
		float vh = radius * 2f;
		
		if (llx > x + w + extra) {
			GL11.glPopMatrix();
			return;
		}
		if (lly > y + h + extra) {
			GL11.glPopMatrix();
			return;
		}
		if (llx + vw + extra < x) {
			GL11.glPopMatrix();
			return;
		}
		if (lly + vh + extra < y) {
			GL11.glPopMatrix();
			return;
		}
		
		float xStart = (int)((llx - x - extra) / size);
		if (xStart < 0) xStart = 0;
		float yStart = (int)((lly - y - extra) / size);
		if (yStart < 0) yStart = 0;
		
		float xEnd = (int)((llx + vw - x + extra) / size) + 1;
		if (xEnd >= tiles.length) xEnd = tiles.length - 1;
		float yEnd = (int)((lly + vw - y + extra) / size) + 1;
		if (yEnd >= tiles.length) yEnd = tiles[0].length - 1;
		
		xStart = (int) Math.floor(xStart / samples) * samples;
		xEnd = (int) Math.floor(xEnd / samples) * samples;
		yStart = (int) Math.ceil(yStart / samples) * samples;
		yEnd = (int) Math.ceil(yEnd / samples) * samples;
		
		mapTexture.bindTexture();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		renderSubArea(xStart, xEnd, yStart, yEnd, factor, samples, alphaMult);
		
		GL11.glPopMatrix();
	}

	
	@Override
	public Color getRenderColor() {
		return Color.white;
		//return Misc.scaleColorOnly(Color.white, 0.67f);
	}
	
	@Override
	public boolean containsEntity(SectorEntityToken other) {
		return true;
	}

	@Override
	public boolean containsPoint(Vector2f test, float r) {
		return true;
	}
	
	public boolean isInClouds(SectorEntityToken other) {
		if (other.getContainingLocation() != this.entity.getContainingLocation()) return false;
		return super.containsPoint(other.getLocation(), other.getRadius());
	}
	
	public boolean isInClouds(Vector2f test, float r) {
		return super.containsPoint(test, r);
	}

	public int [] getTilePreferStorm(Vector2f test, float r) {
		// tiles exist outside render range now
		//float dist = Misc.getDistance(this.entity.getLocation(), test) - r;
		//if (dist > getRenderRange()) return null;
		
		float x = this.entity.getLocation().x;
		float y = this.entity.getLocation().y;
		float size = getTileSize();
		float containsSize = getTileContainsSize();
		
		float w = tiles.length * size;
		float h = tiles[0].length * size;

		x -= w/2f;
		y -= h/2f;
		
		float extra = (containsSize - size) / 2f;
		
		if (test.x + r + extra < x) return null;
		if (test.y + r + extra < y) return null;
		if (test.x > x + w + r + extra) return null;
		if (test.y > y + h + r + extra) return null;
		
		int xIndex = (int) ((test.x - x) / size);
		int yIndex = (int) ((test.y - y) / size);
		
		if (xIndex < 0) xIndex = 0;
		if (yIndex < 0) yIndex = 0;
		
		if (xIndex >= tiles.length) xIndex = tiles.length - 1;
		if (yIndex >= tiles[0].length) yIndex = tiles[0].length - 1;
		
		int [] found = null;
		for (float i = Math.max(0, xIndex - 1); i <= xIndex + 1 && i < tiles.length; i++) {
			for (float j = Math.max(0, yIndex - 1); j <= yIndex + 1 && j < tiles[0].length; j++) {
				int texIndex = tiles[(int) i][(int) j];
				if (texIndex >= 0) {
					float tx = x + i * size + size/2f - containsSize/2f;
					float ty = y + j * size + size/2f - containsSize/2f;
					 
					if (test.x + r < tx) continue;
					if (test.y + r < ty) continue;
					if (test.x > tx + containsSize + r) continue;
					if (test.y > ty + containsSize + r) continue;
					//return true;
					int [] curr = new int[] {(int)i, (int)j};
					//int val = auto.getCells()[(int) i][(int) j];
					//if (val == 1) {
					CellStateTracker cell = activeCells[(int) i][(int) j];
					if (cell != null && cell.isStorming()) {
						return curr;
					}
					if (found == null || (cell != null && cell.isSignaling())) {
						found = curr;
					}
				}
			}
		}
		return found;
	}
	
	public LocationState getStateAt(SectorEntityToken entity, float extraRadius) {
		boolean inCloud = isInClouds(entity);
		int [] tile = getTilePreferStorm(entity.getLocation(), entity.getRadius() + extraRadius);
		CellStateTracker cell = null; 
		if (tile != null) {
			cell = activeCells[tile[0]][tile[1]];
		}
		if (!inCloud) {
			return LocationState.OPEN;
		} else if (cell == null || !cell.isStorming()) {
			return LocationState.DEEP;
		} else { //if (cell.isStorming()) {
			return LocationState.DEEP_STORM;
		}
	}
	
	public CellStateTracker getCellAt(Vector2f location, float radius) {
		int [] tile = getTilePreferStorm(location, radius);
		CellStateTracker cell = null; 
		if (tile != null) {
			cell = activeCells[tile[0]][tile[1]];
		}
		return cell;
	}
	
	public CellStateTracker getCellAt(SectorEntityToken entity, float extraRadius) {
		int [] tile = getTilePreferStorm(entity.getLocation(), entity.getRadius() + extraRadius);
		CellStateTracker cell = null; 
		if (tile != null) {
			cell = activeCells[tile[0]][tile[1]];
		}
		return cell;
	}
	
	@Override
	protected boolean shouldPlayLoopOne() {
		LocationState state = getStateAt(Global.getSector().getPlayerFleet(), getExtraSoundRadius());
		return super.shouldPlayLoopOne() && state == LocationState.OPEN;
	}

	@Override
	protected boolean shouldPlayLoopTwo() {
		LocationState state = getStateAt(Global.getSector().getPlayerFleet(), getExtraSoundRadius());
		//System.out.println("Two: " + (super.shouldPlayLoopTwo() && state == LocationState.DEEP));
		return super.shouldPlayLoopTwo() && state == LocationState.DEEP;
	}
	
	@Override
	protected boolean shouldPlayLoopThree() {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		int [] tile = getTilePreferStorm(playerFleet.getLocation(), playerFleet.getRadius() + getExtraSoundRadius());
		CellStateTracker cell = null; 
		if (tile != null) {
			cell = activeCells[tile[0]][tile[1]];
		}
		return super.shouldPlayLoopThree() && cell != null && cell.isSignaling();
	}
	
	@Override
	protected boolean shouldPlayLoopFour() {
		LocationState state = getStateAt(Global.getSector().getPlayerFleet(), getExtraSoundRadius());
		//System.out.println("Four: " + (super.shouldPlayLoopFour() && state == LocationState.DEEP_STORM));
		return super.shouldPlayLoopFour() && state == LocationState.DEEP_STORM;
	}


	@Override
	public void applyEffect(SectorEntityToken entity, float days) {
		if (entity instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
			
			boolean inCloud = isInClouds(fleet);
			int [] tile = getTilePreferStorm(fleet.getLocation(), fleet.getRadius());
			CellStateTracker cell = null; 
			if (tile != null) {
				cell = activeCells[tile[0]][tile[1]];
			}
			
//			if (cell == null) {
//				inCloud = false;
//			}
			
//			fleet.getStats().addTemporaryModFlat(0.1f, getModId() + "_fuel",
//					"In hyperspace", FUEL_USE_FRACTION, 
//					fleet.getStats().getFuelUseHyperMult());
			
			
			if (!inCloud || fleet.isInHyperspaceTransition()) {
				// open, do nothing
			//} else if (cell == null || !cell.isStorming()) {
			} else {
				fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_1",
						"In deep hyperspace", VISIBLITY_MULT, 
						fleet.getStats().getDetectedRangeMod());
	
				//float penalty = getBurnPenalty(fleet);
				float penalty = Misc.getBurnMultForTerrain(fleet);
				fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_2",
						"In deep hyperspace", penalty, 
						fleet.getStats().getFleetwideMaxBurnMod());
				if (cell != null && cell.isSignaling() && cell.signal < 0.2f) {
					cell.signal = 0; // go to storm as soon as a fleet enters, if it's close to storming already
				}
				if (cell != null && cell.isStorming() && !Misc.isSlowMoving(fleet)) {
					// storm
					if (STORM_SENSOR_RANGE_MULT != 1) {
						fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_storm_sensor",
								"In deep hyperspace (storm)", STORM_SENSOR_RANGE_MULT, 
								fleet.getStats().getSensorRangeMod());
					}
					
					if (STORM_VISIBILITY_FLAT != 0) {
						fleet.getStats().addTemporaryModFlat(0.1f, getModId() + "_storm_visibility",
								"In deep hyperspace (storm)", STORM_VISIBILITY_FLAT,
								fleet.getStats().getDetectedRangeMod());
					}
					
					if (STORM_SPEED_MULT != 1) {
						fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_storm_speed",
								"In deep hyperspace (storm)", getAdjustedSpeedMult(fleet, STORM_SPEED_MULT), 
								fleet.getStats().getFleetwideMaxBurnMod());				
					}
					applyStormStrikes(cell, fleet, days);
				}
			}
		}
	}
	
	protected void applyStormStrikes(CellStateTracker cell, CampaignFleetAPI fleet, float days) {
		
		if (cell.flicker != null && cell.flicker.getWait() > 0) {
			cell.flicker.setNumBursts(0);
			cell.flicker.setWait(0);
			cell.flicker.newBurst();
		}
		
		if (cell.flicker == null || !cell.flicker.isPeakFrame()) return;

		
		fleet.addScript(new HyperStormBoost(cell, fleet));
		
		String key = "$stormStrikeTimeout";
		MemoryAPI mem = fleet.getMemoryWithoutUpdate();
		if (mem.contains(key)) return;
		//boolean canDamage = !mem.contains(key);
		mem.set(key, true, (float) (STORM_MIN_TIMEOUT + (STORM_MAX_TIMEOUT - STORM_MIN_TIMEOUT) * Math.random()));
		
		//if ((float) Math.random() > STORM_STRIKE_CHANCE && false) return;
		
		List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
		if (members.isEmpty()) return;
		
		float totalValue = 0;
		for (FleetMemberAPI member : members) {
			totalValue += member.getStats().getSuppliesToRecover().getModifiedValue();
		}
		if (totalValue <= 0) return;
		
		float strikeValue = totalValue * STORM_DAMAGE_FRACTION * (0.5f + (float) Math.random() * 0.5f);
		
//		int index = Misc.random.nextInt(members.size());
//		FleetMemberAPI member = members.get(index);
		
		float ebCostThresholdMult = 4f;
		
		WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<FleetMemberAPI>();
		WeightedRandomPicker<FleetMemberAPI> preferNotTo = new WeightedRandomPicker<FleetMemberAPI>();
		for (FleetMemberAPI member : members) {
			float w = 1f;
			if (member.isMothballed()) w *= 0.1f;
			
			
			float ebCost = EmergencyBurnAbility.getCRCost(member, fleet);
			if (ebCost * ebCostThresholdMult > member.getRepairTracker().getCR()) {
				preferNotTo.add(member, w);
			} else {
				picker.add(member, w);
			}
		}
		if (picker.isEmpty()) {
			picker.addAll(preferNotTo);
		}
		
		FleetMemberAPI member = picker.pick();
		if (member == null) return;
		
		float crPerDep = member.getDeployCost();
		float suppliesPerDep = member.getStats().getSuppliesToRecover().getModifiedValue();
		if (suppliesPerDep <= 0 || crPerDep <= 0) return;
		
		float strikeDamage = crPerDep * strikeValue / suppliesPerDep;
		if (strikeDamage < STORM_MIN_STRIKE_DAMAGE) strikeDamage = STORM_MIN_STRIKE_DAMAGE;
		
		float resistance = member.getStats().getDynamic().getValue(Stats.CORONA_EFFECT_MULT);
		strikeDamage *= resistance;
		
		if (strikeDamage > STORM_MAX_STRIKE_DAMAGE) strikeDamage = STORM_MAX_STRIKE_DAMAGE;
		
//		if (fleet.isPlayerFleet()) {
//			System.out.println("wefw34gerg");
//		}
		
		float currCR = member.getRepairTracker().getBaseCR();
		float crDamage = Math.min(currCR, strikeDamage);
		
		float ebCost = EmergencyBurnAbility.getCRCost(member, fleet);
		if (currCR >= ebCost * ebCostThresholdMult) {
			crDamage = Math.min(currCR - ebCost * 1.5f, crDamage);
		}
		
		if (crDamage > 0) {
			member.getRepairTracker().applyCREvent(-crDamage, "hyperstorm", "Hyperspace storm strike");
		}
		
		float hitStrength = member.getStats().getArmorBonus().computeEffective(member.getHullSpec().getArmorRating());
		hitStrength *= strikeDamage / crPerDep; 
		member.getStatus().applyDamage(hitStrength);
		if (member.getStatus().getHullFraction() < 0.01f) {
			member.getStatus().setHullFraction(0.01f);
		}
		
		if (fleet.isPlayerFleet()) {
			Global.getSector().getCampaignUI().addMessage(
					member.getShipName() + " suffers damage from the storm", Misc.getNegativeHighlightColor());
			
			Global.getSector().getCampaignUI().showHelpPopupIfPossible("chmHyperStorm");
		}
	}

	public String getStormSoundId() {
		return stormSoundId;
	}

	public boolean hasTooltip() {
		return true;
	}
	
	public String getNameForTooltip() {
		return getTerrainName();
	}
	
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		float pad = 10f;
		float small = 5f;
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color fuel = Global.getSettings().getColor("progressBarFuelColor");
		Color bad = Misc.getNegativeHighlightColor();
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		boolean inCloud = isInClouds(player);
		int [] tile = getTilePreferStorm(player.getLocation(), player.getRadius());
		CellStateTracker cell = null; 
		if (tile != null) {
			cell = activeCells[tile[0]][tile[1]];
		}
//		if (cell == null) {
//			inCloud = false;
//		}
		
		tooltip.addTitle(getTerrainName());
		if (!inCloud) {
			// open
			tooltip.addPara(Global.getSettings().getDescription(getTerrainId() + "_normal", Type.TERRAIN).getText1(), pad);
		} else if (cell == null || !cell.isStorming()) {
			// deep
			tooltip.addPara(Global.getSettings().getDescription(getTerrainId() + "_deep", Type.TERRAIN).getText1(), pad);
		} else if (cell.isStorming()) {
			// storm
			tooltip.addPara(Global.getSettings().getDescription(getTerrainId() + "_storm", Type.TERRAIN).getText1(), pad);
		}
		
		String fuelCost = Misc.getRoundedValueMaxOneAfterDecimal(player.getLogistics().getFuelCostPerLightYear());
		
		float nextPad = pad;
		if (expanded) {
			tooltip.addSectionHeading("Travel", Alignment.MID, pad);
			nextPad = small;
		}
		tooltip.addPara("Traveling through hyperspace consumes fuel based on the distance travelled. " +
				"Your fleet requires %s fuel per light-year.*", nextPad,
				highlight, fuelCost);

		if (inCloud) {
			tooltip.addPara("Reduces the range at which fleets inside can be detected by %s.",
					pad,
					highlight,
					"" + (int) ((1f - VISIBLITY_MULT) * 100) + "%"
			);
			
			tooltip.addPara("Reduces the speed of fleets inside by up to %s. Larger fleets are slowed down more.", 
					nextPad,
					highlight, 
					"" + (int) ((Misc.BURN_PENALTY_MULT) * 100f) + "%"
			);
		
			float penalty = Misc.getBurnMultForTerrain(Global.getSector().getPlayerFleet());
			tooltip.addPara("Your fleet's speed is reduced by %s.", pad,
					highlight,
					"" + (int) Math.round((1f - penalty) * 100) + "%"
					//Strings.X + penaltyStr
			);
			
			tooltip.addSectionHeading("Hyperspace storms", Alignment.MID, pad);
			
			Color stormDescColor = Misc.getTextColor();
			if (cell != null && cell.isStorming()) {
				stormDescColor = bad;
			}
			tooltip.addPara("Being caught in a storm causes storm strikes to damage ships " +
						    "and reduce their combat readiness. " +
						    "Larger fleets attract more damaging strikes.", stormDescColor, pad);
			
			tooltip.addPara("In addition, storm strikes toss the fleet's drive bubble about " +
						    "with great violence, often causing a loss of control. " +
						    "Some commanders are known to use these to gain additional " +
						    "speed, and to save fuel - a practice known as \"storm riding\".", Misc.getTextColor(), pad);
			
			tooltip.addPara("\"Slow-moving\" fleets do not attract storm strikes.", Misc.getTextColor(), pad);
		}
		
		if (expanded) {
			tooltip.addSectionHeading("Combat", Alignment.MID, pad);
//			if (inCloud) {
//				tooltip.addPara("Numerous patches of nebula-like hyperfragments present on the battlefield, slowing ships down to a percentage of their top speed.", small);
//			} else {
//				tooltip.addPara("No effect.", small);
//			}
			tooltip.addPara("No combat effects.", nextPad);
		}
		
		tooltip.addPara("*1 light-year = 2000 units = 1 map grid cell", gray, pad);
	}
	
	protected float getAdjustedSpeedMult(CampaignFleetAPI fleet, float baseMult) {
		float skillMod = fleet.getCommanderStats().getDynamic().getValue(Stats.NAVIGATION_PENALTY_MULT);
		if (skillMod < 0) skillMod = 0;
		if (skillMod > 1) skillMod = 1;
		
		float penalty = 1f - baseMult;
		penalty *= skillMod;
		
		return 1f - penalty;
	}
	
	public boolean isTooltipExpandable() {
//		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
//		boolean inCloud = isInClouds(player);
//		return inCloud;
		return true;
	}
	
	public float getTooltipWidth() {
		return 375f;
	}
	
	public String getTerrainName() {
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		boolean inCloud = isInClouds(player);
		int [] tile = getTilePreferStorm(player.getLocation(), player.getRadius());
		int val = 0;
		CellStateTracker cell = null;
		if (tile != null) {
			cell = activeCells[tile[0]][tile[1]];
		}
		
		String name = "Hyperspace";
		if (!inCloud) {
		} else if (cell == null || !cell.isStorming()) {
			name = "Hyperspace (Deep)";
		} else if (cell.isStorming()) {
			name = "Hyperspace (Storm)";
		}
		return name;
	}
	
	
	public String getEffectCategory() {
		return "dark-hyper-like";
	}

	public boolean hasAIFlag(Object flag) {
		return flag == TerrainAIFlags.REDUCES_SENSOR_RANGE;
	}
	
	public boolean hasAIFlag(Object flag, CampaignFleetAPI fleet) {
		if (flag == TerrainAIFlags.DANGEROUS_UNLESS_GO_SLOW) {
			int [] tile = getTilePreferStorm(fleet.getLocation(), fleet.getRadius() + 100f);
			CellStateTracker cell = null; 
			if (tile != null) {
				cell = activeCells[tile[0]][tile[1]];
			}
			if (cell != null) {
				return cell.isStorming() || cell.isSignaling();
			}
		}
		return hasAIFlag(flag);
	}
	
	@Override
	public int getNumMapSamples() {
		return 10;
	}
	
	
	
//	public static float getBurnPenalty(CampaignFleetAPI fleet) {
//		AsteroidBeltTerrainPlugin.getFleetRadiusTerrainEffectMult(fleet);
//		
//		float min = Global.getSettings().getBaseFleetSelectionRadius() + Global.getSettings().getFleetSelectionRadiusPerUnitSize();
//		float max = Global.getSettings().getMaxFleetSelectionRadius();
//		float radius = fleet.getRadius();
//
//		float penalty = 1f - (radius - min) / (max - min);
//		if (penalty > 1) penalty = 1;
//		if (penalty < 0) penalty = 0;
//		penalty = MIN_BURN_PENALTY + penalty * BURN_PENALTY_RANGE;
//
//		float skillMod = fleet.getCommanderStats().getDynamic().getValue(Stats.NAVIGATION_PENALTY_MULT);
//		penalty *= skillMod;
//		
//		return penalty;
//	}

	public static void main(String[] args) {
		System.out.println(1.5f - (int) 1.5f);
	}
}








