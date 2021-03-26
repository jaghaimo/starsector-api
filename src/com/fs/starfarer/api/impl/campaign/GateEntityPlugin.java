package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;
import java.util.LinkedHashSet;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.intel.misc.GateIntel;
import com.fs.starfarer.api.impl.campaign.world.ZigLeashAssignmentAI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.JitterUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WarpingSpriteRendererUtil;

public class GateEntityPlugin extends BaseCustomEntityPlugin {

	public static float ACCUMULATED_TRANSIT_DIST_DECAY_RATE = 20f;
	public static float MOTE_SPAWN_MULT_BASELINE_DIST_LY = 25f;
	
	public static String GATE_DATA = "gateData";
	
	public static String GATE_SCANNED = "$gateScanned"; // in gate memory
	public static String CAN_SCAN_GATES = "$canScanGates"; // in global memory
	public static String GATES_ACTIVE = "$gatesActive"; // in global memory
	public static String PLAYER_CAN_USE_GATES = "$playerCanUseGates"; // in global memory
	public static String NUM_GATES_SCANNED = "$numGatesScanned"; // in global memory
	
	public static int getNumGatesScanned() {
		return Global.getSector().getMemoryWithoutUpdate().getInt(NUM_GATES_SCANNED);
	}
	public static void addGateScanned() {
		int num = getNumGatesScanned();
		num++;
		Global.getSector().getMemoryWithoutUpdate().set(NUM_GATES_SCANNED, num);
	}
	
	public static class GateData {
		public LinkedHashSet<SectorEntityToken> scanned = new LinkedHashSet<SectorEntityToken>();
	}
	
	public static GateData getGateData() {
		Object data = Global.getSector().getPersistentData().get(GATE_DATA);
		if (!(data instanceof GateData)) {
			data = new GateData();
			Global.getSector().getPersistentData().put(GATE_DATA, data);
		}
		return (GateData) data;
	}
	
	public static boolean isScanned(SectorEntityToken gate) {
		return gate.getMemoryWithoutUpdate().getBoolean(GATE_SCANNED);
	}
	
	public static boolean isActive(SectorEntityToken gate) {
		return gate.getCustomPlugin() instanceof GateEntityPlugin &&
				((GateEntityPlugin)gate.getCustomPlugin()).isActive();
	}
	
	public static boolean areGatesActive() {
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean(GATES_ACTIVE)) {
			return true;
		}
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		return cargo.getQuantity(CargoItemType.SPECIAL, new SpecialItemData(Items.JANUS, null)) > 0;
	}
	
	public static boolean canUseGates() {
//		if (true) return true;
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean(PLAYER_CAN_USE_GATES)) {
			return true;
		}
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		return cargo.getQuantity(CargoItemType.SPECIAL, new SpecialItemData(Items.JANUS, null)) > 0;
	}
	
	transient protected SpriteAPI baseSprite;
	transient protected SpriteAPI scannedGlow;
	
	transient protected SpriteAPI activeGlow;
	transient protected SpriteAPI whirl1;
	transient protected SpriteAPI whirl2;
	transient protected SpriteAPI starfield;
	transient protected SpriteAPI rays;
	transient protected SpriteAPI concentric;
	
	transient protected WarpingSpriteRendererUtil warp;
	
	protected FaderUtil beingUsedFader = new FaderUtil(0f, 1f, 1f, false, true);
	protected FaderUtil glowFader = new FaderUtil(0f, 1f, 1f, true, true);
	protected boolean madeActive = false;
	protected boolean addedIntel = false;
	protected float showBeingUsedDur = 0f;
	protected float accumulatedTransitDistLY = 0f;
	
	protected Color jitterColor = null; 
	protected JitterUtil jitter;
	protected FaderUtil jitterFader = null;
	
	protected IntervalUtil moteSpawn = null;
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		readResolve();
	}
	
	
	Object readResolve() {
		scannedGlow = Global.getSettings().getSprite("gates", "glow_scanned");
		activeGlow = Global.getSettings().getSprite("gates", "glow_ring_active");
		concentric = Global.getSettings().getSprite("gates", "glow_concentric");
		rays = Global.getSettings().getSprite("gates", "glow_rays");
		whirl1 = Global.getSettings().getSprite("gates", "glow_whirl1");
		whirl2 = Global.getSettings().getSprite("gates", "glow_whirl2");
		starfield = Global.getSettings().getSprite("gates", "starfield");
		
		//warp = new WarpingSpriteRendererUtil(10, 10, 10f, 20f, 2f); 
		
		if (beingUsedFader == null) {
			beingUsedFader = new FaderUtil(0f, 1f, 1f, false, true);
		}
		if (glowFader == null) {
			glowFader = new FaderUtil(0f, 1f, 1f, true, true);
			glowFader.fadeIn();
		}
		inUseAngle = 0;
		return this;
	}
	
	public void jitter() {
		if (jitterFader == null) {
			jitterFader = new FaderUtil(0f, 2f, 2f);
			jitterFader.setBounceDown(true);
		}
		if (jitter == null) {
			jitter = new JitterUtil();
			jitter.updateSeed();
		}
		jitterFader.fadeIn();
	}
	
	public float getJitterLevel() {
		if (jitterFader != null) return jitterFader.getBrightness();
		return 0f;
	}

	public Color getJitterColor() {
		return jitterColor;
	}

	public void setJitterColor(Color jitterColor) {
		this.jitterColor = jitterColor;
	}

	public boolean isActive() {
		return madeActive;
	}
	
	public void showBeingUsed(float transitDistLY) {
		showBeingUsed(10f, transitDistLY);
	}
	
	public void showBeingUsed(float dur, float transitDistLY) {
		beingUsedFader.fadeIn();
		showBeingUsedDur = dur;
		
		accumulatedTransitDistLY += transitDistLY;
		
//		if (withSound && entity.isInCurrentLocation()) {
//			Global.getSoundPlayer().playSound("gate_being_used", 1, 1, entity.getLocation(), entity.getVelocity());
//		}
	}
	
	public float getProximitySoundFactor() {
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		float dist = Misc.getDistance(player.getLocation(), entity.getLocation());
		float radSum = entity.getRadius() + player.getRadius();
		
		//if (dist < radSum) return 1f;
		dist -= radSum;

		float f = 1f;
		if (dist > 300f) {
			f = 1f - (dist - 300f) / 100f;
		}

		//float f = 1f - dist / 300f;
		if (f < 0) f = 0;
		if (f > 1) f = 1;
		return f;
	}
	
	public void playProximityLoop() {
		if (!isActive() || !entity.isInCurrentLocation()) return;
		
		float prox = getProximitySoundFactor();
		float volumeMult = prox;
		float suppressionMult = prox;
		if (volumeMult <= 0) return;
		volumeMult = (float) Math.sqrt(volumeMult);
		//volumeMult = 1f;
		
		Global.getSector().getCampaignUI().suppressMusic(1f * suppressionMult);
		
		float dirToEntity = Misc.getAngleInDegrees(entity.getLocation(), this.entity.getLocation());
		Vector2f playbackLoc = Misc.getUnitVectorAtDegreeAngle(dirToEntity);
		playbackLoc.scale(500f);
		Vector2f.add(entity.getLocation(), playbackLoc, playbackLoc);
		
		Global.getSoundPlayer().playLoop("active_gate_loop", entity, 
				//volume * 0.25f + 0.75f, volume * volumeMult,
				1f, 1f * volumeMult,
				playbackLoc, Misc.ZERO);
		
		if (!beingUsedFader.isIdle()) {
			Global.getSoundPlayer().playLoop("gate_being_used", entity, 
					//volume * 0.25f + 0.75f, volume * volumeMult,
					1f, 1f * volumeMult,
					playbackLoc, Misc.ZERO);
		}
	}

	protected float inUseAngle = 0f;
	public void advance(float amount) {
		if (showBeingUsedDur > 0 || !beingUsedFader.isIdle()) {
			showBeingUsedDur -= amount;
			if (showBeingUsedDur > 0) {
				beingUsedFader.fadeIn();
			} else {
				showBeingUsedDur = 0f;
			}
			inUseAngle += amount * 60f;
			if (warp != null) {
				warp.advance(amount);
			}
		}
		glowFader.advance(amount);
		
//		if (entity.isInCurrentLocation()) {
//			System.out.println("BRIGHTNESS: " + beingUsedFader.getBrightness());
//		}
		
		if (jitterFader != null) {
			jitterFader.advance(amount);
			if (jitterFader.isFadedOut()) {
				jitterFader = null;
			}
		}
		
		if (accumulatedTransitDistLY > 0) {
			float days = Global.getSector().getClock().convertToDays(amount);
			accumulatedTransitDistLY -= days * ACCUMULATED_TRANSIT_DIST_DECAY_RATE;
			if (accumulatedTransitDistLY < 0) accumulatedTransitDistLY = 0;
		}
		
		beingUsedFader.advance(amount);

		if (!madeActive) {
			if (canUseGates() && isScanned(entity)) {
				if (entity.getName().equals(entity.getCustomEntitySpec().getDefaultName())) {
					entity.setName("Active Gate");
				}
				entity.setCustomDescriptionId("active_gate");
				entity.setInteractionImage("illustrations", "active_gate");
				madeActive = true;
			}
		}
		
		
		if (entity.isInCurrentLocation()) {
			if (amount > 0) {
				playProximityLoop();
				
				if (jitter != null) {
					jitter.updateSeed();
				}
				
//				if (isScanned(entity)) {
//					float dist = Misc.getDistance(entity, Global.getSector().getPlayerFleet()) - 
//							entity.getRadius() - Global.getSector().getPlayerFleet().getRadius();
//					if (dist < 300f && canUseGates()) {
//						showBeingUsed();
//					}
//				}
			
				if (!addedIntel && !Global.getSector().isInNewGameAdvance() && 
						!Global.getSector().isInFastAdvance()) {
					boolean alreadyHas = false;
					for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(GateIntel.class)) {
						if (((GateIntel)intel).getGate() == entity) {
							alreadyHas = true;
							break;
						}
					}
					if (!alreadyHas) {
						Global.getSector().getIntelManager().addIntel(new GateIntel(entity));
					}
					addedIntel = true;
				}
				
				if (beingUsedFader.getBrightness() > 0) {
					if (moteSpawn == null) moteSpawn = new IntervalUtil(0.01f, 20f);
					float moteSpawnMult = accumulatedTransitDistLY / MOTE_SPAWN_MULT_BASELINE_DIST_LY; 
					moteSpawn.advance(amount * moteSpawnMult);
					if (moteSpawn.intervalElapsed()) {
						ZigLeashAssignmentAI.spawnMote(entity);
					}
				} else {
					moteSpawn = null;
				}
			} else {
				moteSpawn = null;
			}
		}		
	}

	public float getRenderRange() {
		return entity.getRadius() + 500f;
	}

	@Override
	public void createMapTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		Color color = entity.getFaction().getBaseUIColor();
		
//		boolean inactive = !isScanned(entity) || !areGatesActive();
//		String gateTypeName = "Active Gate";
//		if (inactive) gateTypeName = "Inactive Gate";
//		
//		if (gateTypeName.equals(entity.getName())) {
//			gateTypeName = "";
//		} else {
//			//gateTypeName = " - " + gateTypeName;
//		}
		
		tooltip.addPara(entity.getName(), color, 0);
		
		if (isScanned(entity)) {
			if (areGatesActive()) {
				tooltip.addPara("Active", Misc.getGrayColor(), 3f);
			} else {
				tooltip.addPara("Scanned", Misc.getGrayColor(), 3f);
			}
		}
	}

	@Override
	public boolean hasCustomMapTooltip() {
		return true;
	}
	
	@Override
	public void appendToCampaignTooltip(TooltipMakerAPI tooltip, VisibilityLevel level) {
		if (isScanned(entity)) {
			tooltip.setParaFontDefault();
			if (areGatesActive()) {
				tooltip.addPara("This gate is active.", Misc.getGrayColor(), 10f);
			} else {
				tooltip.addPara("You've scanned this gate.", Misc.getGrayColor(), 10f);
			}
		}
	}
	
	
	transient protected boolean scaledSprites = false;
	protected void scaleGlowSprites() {
		if (scaledSprites) return;
		CustomEntitySpecAPI spec = entity.getCustomEntitySpec();
		if (spec != null) {
			baseSprite = Global.getSettings().getSprite(spec.getSpriteName());
			baseSprite.setSize(spec.getSpriteWidth(), spec.getSpriteHeight());
			
			scaledSprites = true;
			float scale = spec.getSpriteWidth() / Global.getSettings().getSprite(spec.getSpriteName()).getWidth();
			scannedGlow.setSize(scannedGlow.getWidth() * scale, scannedGlow.getHeight() * scale);
			activeGlow.setSize(activeGlow.getWidth() * scale, activeGlow.getHeight() * scale);
			
			rays.setSize(rays.getWidth() * scale, rays.getHeight() * scale);
			whirl1.setSize(whirl1.getWidth() * scale, whirl1.getHeight() * scale);
			whirl2.setSize(whirl2.getWidth() * scale, whirl2.getHeight() * scale);
			starfield.setSize(starfield.getWidth() * scale, starfield.getHeight() * scale);
			concentric.setSize(concentric.getWidth() * scale, concentric.getHeight() * scale);
		}
	}
	
	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		if (layer == CampaignEngineLayers.BELOW_STATIONS) {
			boolean beingUsed = !beingUsedFader.isFadedOut();
			if (beingUsed) {
				float alphaMult = viewport.getAlphaMult();
				alphaMult *= entity.getSensorFaderBrightness();
				alphaMult *= entity.getSensorContactFaderBrightness();
				if (alphaMult <= 0f) return;
				
				if (warp == null) {
					int cells = 6;
					float cs = starfield.getWidth() / 10f;
					warp = new WarpingSpriteRendererUtil(cells, cells, cs * 0.2f, cs * 0.2f, 2f);
				}
				
				Vector2f loc = entity.getLocation();
				
				float glowAlpha = 1f;
				scaleGlowSprites();
				
				glowAlpha *= beingUsedFader.getBrightness();
				
				starfield.setAlphaMult(alphaMult * glowAlpha);
				starfield.setAdditiveBlend();
				//starfield.renderAtCenter(loc.x + 1.5f, loc.y);
				
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				warp.renderNoBlendOrRotate(starfield, loc.x + 1.5f - starfield.getWidth() / 2f,
											loc.y - starfield.getHeight() / 2f, false);
			}
		}
		if (layer == CampaignEngineLayers.STATIONS) {
			float alphaMult = viewport.getAlphaMult();
			alphaMult *= entity.getSensorFaderBrightness();
			alphaMult *= entity.getSensorContactFaderBrightness();
			if (alphaMult <= 0f) return;
			
			CustomEntitySpecAPI spec = entity.getCustomEntitySpec();
			if (spec == null) return;
			
			float w = spec.getSpriteWidth();
			float h = spec.getSpriteHeight();
			
			float scale = spec.getSpriteWidth() / Global.getSettings().getSprite(spec.getSpriteName()).getWidth(); 
			
			Vector2f loc = entity.getLocation();
			
			
			Color scannedGlowColor = new Color(255,200,0,255);
			Color activeGlowColor = new Color(200,50,255,255);
			
			scannedGlowColor = Color.white;
			activeGlowColor = Color.white;
			
			float glowAlpha = 1f;
			
			
			float glowMod1 = 0.5f + 0.5f * glowFader.getBrightness();
			float glowMod2 = 0.75f + 0.25f * glowFader.getBrightness();
			
			boolean beingUsed = !beingUsedFader.isFadedOut();
			boolean scanned = isScanned(entity);
			boolean active = areGatesActive();
			
			scaleGlowSprites();
			
			if (jitterFader != null && jitter != null) {
				Color c = jitterColor;
				if (c == null) c = new Color(255,255,255,255);
				baseSprite.setColor(c);
				baseSprite.setAlphaMult(alphaMult * jitterFader.getBrightness());
				baseSprite.setAdditiveBlend();
				jitter.render(baseSprite, loc.x, loc.y, 30f * jitterFader.getBrightness(), 10);
				baseSprite.renderAtCenter(loc.x, loc.y);
			}
			
			
			if (!active && scanned) {
				scannedGlow.setColor(scannedGlowColor);
				//scannedGlow.setSize(w, h);
				scannedGlow.setAlphaMult(alphaMult * glowAlpha * glowMod1);
				scannedGlow.setAdditiveBlend();
				scannedGlow.renderAtCenter(loc.x, loc.y);
			}
			
			if (active && scanned) {
				activeGlow.setColor(activeGlowColor);
				//activeGlow.setSize(w * scale, h * scale);
				activeGlow.setAlphaMult(alphaMult * glowAlpha * glowMod2);
				activeGlow.setAdditiveBlend();
				activeGlow.renderAtCenter(loc.x, loc.y);
			}
			
//			beingUsed = true;
//			showBeingUsedDur = 10f;
			if (beingUsed) {
				if (!active) {
					activeGlow.setColor(activeGlowColor);
					//activeGlow.setSize(w + 20, h + 20);
					activeGlow.setAlphaMult(alphaMult * glowAlpha * beingUsedFader.getBrightness() * glowMod2);
					activeGlow.setAdditiveBlend();
					activeGlow.renderAtCenter(loc.x, loc.y);
				}
				
				glowAlpha *= beingUsedFader.getBrightness();
				float angle;
				
				rays.setAlphaMult(alphaMult * glowAlpha);
				rays.setAdditiveBlend();
				rays.renderAtCenter(loc.x + 1.5f, loc.y);
				
				concentric.setAlphaMult(alphaMult * glowAlpha * 1f);
				concentric.setAdditiveBlend();
				concentric.renderAtCenter(loc.x + 1.5f, loc.y);

				angle = -inUseAngle * 0.25f;
				angle = Misc.normalizeAngle(angle);
				whirl1.setAngle(angle);
				whirl1.setAlphaMult(alphaMult * glowAlpha);
				whirl1.setAdditiveBlend();
				whirl1.renderAtCenter(loc.x + 1.5f, loc.y);
				
				angle = -inUseAngle * 0.33f;
				angle = Misc.normalizeAngle(angle);
				whirl2.setAngle(angle);
				whirl2.setAlphaMult(alphaMult * glowAlpha * 0.5f);
				whirl2.setAdditiveBlend();
				whirl2.renderAtCenter(loc.x + 1.5f, loc.y);
			}
		}
	}
	
	
	
	
	

//	public void advance(float amount) {
//		if (entity.isInCurrentLocation()) {
//			if (rings != null) {
//				for (TempRingData data : rings) {
//					data.advance(amount);
//				}
//			}
//			angleOffset += 30f * amount;
//		}		
//	}	
//	Object readResolve() {
//		glow = Global.getSettings().getSprite("gates", "glow");
//		atmosphereTex = Global.getSettings().getSprite("combat", "corona_hard");
//		
//		rings = new ArrayList<GateEntityPlugin.TempRingData>();
//		float baseRadius = 120 * 0.6f;
//		float thickness = 5;
//		float max = 30f;
//		for (float i = 0; i < max; i++ ) {
//			//TempRingData data = new TempRingData(baseRadius - i * 1.5f, thickness);
//			float mult = 0.5f + 0.5f * (1f - i / max);
//			mult = 1f;
//			TempRingData data = new TempRingData(baseRadius - i * 1.5f * mult, thickness);
//			rings.add(data);
//		}
//		
//		return this;
//	}
//	
//	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
//		if (layer == CampaignEngineLayers.STATIONS) {
//			float alphaMult = viewport.getAlphaMult();
//			alphaMult *= entity.getSensorFaderBrightness();
//			alphaMult *= entity.getSensorContactFaderBrightness();
//			if (alphaMult <= 0f) return;
//			
//			CustomEntitySpecAPI spec = entity.getCustomEntitySpec();
//			if (spec == null) return;
//			
//			float w = spec.getSpriteWidth();
//			float h = spec.getSpriteHeight();
//			
//			Vector2f loc = entity.getLocation();
//			
//			
//			Color glowColor = new Color(255,200,0,255);
//			float glowAlpha = 1f;
//			
//			//glow.setColor(Color.white);
//			glow.setColor(glowColor);
//			
//			glow.setSize(w, h);
//			glow.setAlphaMult(alphaMult * glowAlpha);
//			glow.setAdditiveBlend();
//			
//			glow.setColor(new Color(100, 100, 255));
//			//glow.setAngle(entity.getFacing() - 90f + glowAngle1);
////			glow.renderAtCenter(loc.x, loc.y);
////			glow.renderAtCenter(loc.x, loc.y);
////			glow.renderAtCenter(loc.x, loc.y);
//			//glow.renderAtCenter(loc.x, loc.y);
//			
////			float perSegment = 2f;
////			segments = (int) ((entity.getRadius() * 2f * 3.14f) / perSegment);
////			if (segments < 8) segments = 8;
////			
////			if (noise != null) {
////				float x = loc.x;
////				float y = loc.y;
////				float r = entity.getRadius() * 0.6f;
////				float tSmall = 10f;
////				
////				Color color = new Color(100, 100, 255);
////				noiseMag = 0.7f;
////				float a = 1f;
////				a = 1f;
////				for (int i = 0; i < 3; i++) {
////					renderAtmosphere(x, y, r - i, tSmall, alphaMult * a, segments, atmosphereTex, noise, color, true);
////				}
//////				renderAtmosphere(x, y, r, tSmall, alphaMult * a, segments, atmosphereTex, noise, color, true);
//////				renderAtmosphere(x, y, r - 2f, tSmall, alphaMult * a, segments, atmosphereTex, noise, color, true);
//////				
////				float circleAlpha = 1f;
////				if (alphaMult < 0.5f) {
////					circleAlpha = alphaMult * 2f;
////				}
//////				float tCircleBorder = 1f;
//			
////			float x = loc.x;
////			float y = loc.y;
//			float a = 0.5f;
//			Color color = new Color(100, 100, 255);
//			if (rings != null) {
//				//float i = rings.size();
//				float i = 0f;
//				for (TempRingData data : rings) {
//					float r = i / rings.size() * 15f;
//					float angle = i * 5f + angleOffset;
//					Vector2f loc2 = Misc.getUnitVectorAtDegreeAngle(angle);
//					loc2.scale(r);
//					float x = loc.x + loc2.x;
//					float y = loc.y + loc2.y;
//					i++;
//					float bonusThickness = 0f;
////					if (i == 1) bonusThickness = 20f;
////					if (i <= 5) bonusThickness = 20f - i * 3f;
//					renderAtmosphere(x, y, data.radius, data.thickness + bonusThickness, 
//							alphaMult * a, data.segments, atmosphereTex, data.noise, color, true);
//
//					if ((int)Math.round(i) == rings.size()) {
//						renderCircle(x, y,  data.radius, 1f, data.segments, data.noise, Color.black);
//					}
//				}
//				
//				float x = loc.x;
//				float y = loc.y;
//				//renderCircle(x, y,  rings.get(0).radius, 1f, rings.get(0).segments, rings.get(0).noise, Color.black);
//			}
////				renderAtmosphere(x, y, r, tCircleBorder, circleAlpha, segments, atmosphereTex, noise, Color.black, false);
////			}
//		}
//	}
//	
//	
//	
//	
//	private void renderCircle(float x, float y, float radius, float alphaMult, int segments, float [] noise, Color color) {
//		//if (fader.isFadingIn()) alphaMult = 1f;
//		
//		float startRad = (float) Math.toRadians(0);
//		float endRad = (float) Math.toRadians(360);
//		float spanRad = Misc.normalizeAngle(endRad - startRad);
//		float anglePerSegment = spanRad / segments;
//		
//		GL11.glPushMatrix();
//		GL11.glTranslatef(x, y, 0);
//		GL11.glRotatef(0, 0, 0, 1);
//		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		
//		GL11.glEnable(GL11.GL_BLEND);
//		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		
//		
//		GL11.glColor4ub((byte)color.getRed(),
//						(byte)color.getGreen(),
//						(byte)color.getBlue(),
//						(byte)((float) color.getAlpha() * alphaMult));
//		
//		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
//		GL11.glVertex2f(0, 0);
//		for (float i = 0; i < segments + 1; i++) {
//			boolean last = i == segments;
//			if (last) i = 0;
//			float theta = anglePerSegment * i;
//			float cos = (float) Math.cos(theta);
//			float sin = (float) Math.sin(theta);
//			
//			float m1 = 0.9f + 0.1f * noise[(int)i];
////			if (noiseMag <= 0) {
////				m1 = 1f;
////			}
//			
//			float x1 = cos * radius * m1;
//			float y1 = sin * radius * m1;
//			
//			GL11.glVertex2f(x1, y1);
//			
//			if (last) break;
//		}
//		
//		
//		GL11.glEnd();
//		GL11.glPopMatrix();
//		
//	}
//	
//	
//	private void renderAtmosphere(float x, float y, float radius, float thickness, float alphaMult, int segments, SpriteAPI tex, float [] noise, Color color, boolean additive) {
//		
//		float startRad = (float) Math.toRadians(0);
//		float endRad = (float) Math.toRadians(360);
//		float spanRad = Misc.normalizeAngle(endRad - startRad);
//		float anglePerSegment = spanRad / segments;
//		
//		GL11.glPushMatrix();
//		GL11.glTranslatef(x, y, 0);
//		GL11.glRotatef(0, 0, 0, 1);
//		GL11.glEnable(GL11.GL_TEXTURE_2D);
//		
//		tex.bindTexture();
//
//		GL11.glEnable(GL11.GL_BLEND);
//		if (additive) {
//			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//		} else {
//			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		}
//		
////		float zoom = Global.getSector().getViewport().getViewMult();
//		
//		float noiseMult = 1f;
////		if (zoom > 1) {
////			noiseMult = 1f / zoom;
////			alphaMult *= noiseMult;
////		}
//		
//		GL11.glColor4ub((byte)color.getRed(),
//						(byte)color.getGreen(),
//						(byte)color.getBlue(),
//						(byte)((float) color.getAlpha() * alphaMult));
//		float texX = 0f;
//		float incr = 1f / segments;
//		GL11.glBegin(GL11.GL_QUAD_STRIP);
//		for (float i = 0; i < segments + 1; i++) {
//			boolean last = i == segments;
//			if (last) i = 0;
//			float theta = anglePerSegment * i;
//			float cos = (float) Math.cos(theta);
//			float sin = (float) Math.sin(theta);
//			
//			
//			float m1 = 0.9f + 0.1f * noise[(int)i] * noiseMult;
//			float m2 = m1;
//			//m2 = 1f;
//			
//			float x1 = cos * radius * m1;
//			float y1 = sin * radius * m1;
//			float x2 = cos * (radius + thickness * m2 * 5f);
//			float y2 = sin * (radius + thickness * m2);
//			
//			GL11.glTexCoord2f(0.5f, 0.05f);
//			GL11.glVertex2f(x1, y1);
//			
//			GL11.glTexCoord2f(0.5f, 0.95f);
//			GL11.glVertex2f(x2, y2);
//			
//			texX += incr;
//			if (last) break;
//		}
//		
//		GL11.glEnd();
//		GL11.glPopMatrix();
//	}
//	
//	
//	
//	
//	
//
//	public static class TempRingData {
//		protected float [] noise;
//		protected float [] noise1;
//		protected float [] noise2;
//		protected int segments;
//		protected float noiseElapsed = 0f;
//		protected float noisePeriod = 0f;
//		protected float radius;
//		protected float noiseMag;
//		protected float thickness;
//		public TempRingData(float radius, float thickness) {
//			this.radius = radius;
//			this.thickness = thickness;
//			
//			float perSegment = 2f;
//			segments = (int) ((radius * 2f * 3.14f) / perSegment);
//			if (segments < 8) segments = 8;
//			
//			noiseMag = 0.6f;
//			noiseMag = 1f;
//			noisePeriod = 1f + Misc.random.nextFloat() * 1f;
//			noise1 = Noise.genNoise(segments, noiseMag);
//			noise2 = Noise.genNoise(segments, noiseMag);
//			noise = Arrays.copyOf(noise1, noise1.length);
//		}
//		public void advance(float amount) {
//			noiseElapsed += amount;
//			if (noiseElapsed > noisePeriod) {
//				noiseElapsed = 0;
//				noise1 = Arrays.copyOf(noise2, noise2.length);
//				noise2 = Noise.genNoise(segments, noiseMag);
//			}
//			float f = noiseElapsed / noisePeriod;
//			for (int i = 0; i < noise.length; i++) {
//				float n1 = noise1[i];
//				float n2 = noise2[i];
//				noise[i] = n1 + (n2 - n1) * f;
//			}
//		}
//	}
//	transient protected List<TempRingData> rings;
//	transient protected SpriteAPI atmosphereTex;
	
}









