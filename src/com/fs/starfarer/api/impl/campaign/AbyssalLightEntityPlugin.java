package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.abilities.InterdictionPulseAbility;
import com.fs.starfarer.api.impl.campaign.enc.EncounterManager;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTPoints;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.FlickerUtilV2;
import com.fs.starfarer.api.util.Misc;

public class AbyssalLightEntityPlugin extends BaseCustomEntityPlugin {

	public static enum DespawnType {
		FADE_OUT,
		EXPAND,
	}
	
	public static float SENSOR_BURST_TRIGGER_RANGE = 500f;
	public static float DWELLER_TRIGGER_RANGE = 200f;
	
	public static float MIN_DURATION = 20f;
	public static float MAX_DURATION = 30f;
	
	public static float MIN_SIZE = 600f;
	public static float MAX_SIZE = 1200f;
	
	public static float DESPAWN_POOF_THRESHOLD = 0.7f;
	
	public static float GLOW_FREQUENCY = 0.2f; // on/off cycles per second
	
	public static float PLAYER_PROXIMITY_MIN_BRIGHTNESS_AT = 2000f;
	public static float PLAYER_PROXIMITY_MAX_BRIGHTNESS_AT = 300f;
	public static float PLAYER_PROXIMITY_MIN_BRIGHTNESS = 0.25f;
	
	
	public static class DwellerEncounterScript implements EveryFrameScript {
		protected float elapsed = 0f;
		protected String trigger;
		protected boolean done = false;
		protected float triggerDelay;
		protected SectorEntityToken light;
		protected CampaignFleetAPI fleet;
		public DwellerEncounterScript(String trigger, CampaignFleetAPI fleet, SectorEntityToken light) {
			this.trigger = trigger;
			this.fleet = fleet;
			this.light = light;
			triggerDelay = 1f + (float) Math.random() * 1f;
			
		}
		public boolean isDone() {
			return done;
		}
		public boolean runWhilePaused() {
			return false;
		}
		public void advance(float amount) {
			if (done) return;
			
			if (Global.getSector().isPaused() || Global.getSector().getCampaignUI().isShowingDialog()) {
				return;
			}
			
			elapsed += amount;
			
			if (fleet == null || elapsed > 3f || !light.isAlive() || light.hasTag(Tags.FADING_OUT_AND_EXPIRING) ||
					fleet.getContainingLocation() != light.getContainingLocation()) {
				done = true;
				return;
			}
			
			float dist = Misc.getDistance(fleet, light) - fleet.getRadius() - light.getRadius();
			if (dist > DWELLER_TRIGGER_RANGE) {
				done = true;
				return;
			}
			
			
			if (elapsed > triggerDelay) {
				if (fleet.isPlayerFleet()) {
					Misc.showRuleDialog(light, trigger);
				} else {
					fleet.despawn(FleetDespawnReason.DESTROYED_BY_BATTLE, light);
					if (light.getCustomPlugin() instanceof AbyssalLightEntityPlugin) {
						AbyssalLightEntityPlugin plugin = (AbyssalLightEntityPlugin) light.getCustomPlugin();
						plugin.despawn(DespawnType.FADE_OUT);
					}
				}
				done = true;
				return;
			}
		}
	}
	public static class AbyssalLightSoundStopper implements EveryFrameScript {
		protected transient SoundAPI sound = null;
		protected float elapsed = 0f;
		public AbyssalLightSoundStopper(SoundAPI sound) {
			this.sound = sound;
		}
		public boolean isDone() {
			return sound == null;
		}
		public boolean runWhilePaused() {
			return true;
		}
		public void advance(float amount) {
			elapsed += amount;
			if (sound == null) return;
			if (!sound.isPlaying() && elapsed > 1f) {
				sound = null;
				return;
			}
			
			if (Global.getSector().isPaused() && sound != null) {
				sound.stop();
				sound = null;
			}
		}
	}
	public static class AbyssalLightParams {
		public Color color = new Color(200,200,255,255);
		public float frequencyMultMin = 0.5f;
		public float frequencyMultMax = 1.5f;
		public float frequencyChangeMult = 1f;
		public float size;
		public float detectedRange;
		public float bonusMult;
		public float durationDays;
		public int pointsOverride = 0;
		
		public AbyssalLightParams() {
			this(MIN_SIZE, MAX_SIZE);
		}
		public AbyssalLightParams(float minSize, float maxSize) {
			frequencyMultMin = 0.5f;
			frequencyMultMax = 1.5f;
			
			float sizeRange = maxSize - minSize;
			float avgSize = MIN_SIZE + (MAX_SIZE - MIN_SIZE) * 0.5f;
			
			
			size = minSize + (float) Math.random() * sizeRange;
			detectedRange = (size * 3f) / HyperspaceTerrainPlugin.ABYSS_SENSOR_RANGE_MULT;
			
			bonusMult = size / avgSize;
			
			durationDays = MIN_DURATION + (MAX_DURATION - MIN_DURATION) * (float) Math.random();
		}
		
		public int getTopographyPoints() {
			if (pointsOverride > 0) return pointsOverride;
			
			return Math.round(HTPoints.ABYSSAL_LIGHT_MIN +
					(HTPoints.ABYSSAL_LIGHT_AVG - HTPoints.ABYSSAL_LIGHT_MIN) * bonusMult);
		}
	}
	
	
	transient private SpriteAPI glow;
	
	
	protected float phase = 0f;
	protected float frequencyMult = 1f;
	protected FlickerUtilV2 flicker = new FlickerUtilV2();
	protected FaderUtil fader = new FaderUtil(0f, 0.5f, 0.5f);
	protected AbyssalLightParams params;
	protected float untilFrequencyChange = 0f;
	protected float untilSoundPlayed = 0f;
	protected float flickerDur = 0f;
	protected float abilityResponseFlickerTimeout = 0f;
	protected DespawnType despawnType = null;
	protected boolean playedDespawnSound = false;
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		params = (AbyssalLightParams) pluginParams;
		
		float radiusMult = params.bonusMult;
		if (radiusMult > 1f) {
			radiusMult = 1f + (radiusMult - 1f) * 0.33f;
		} else if (radiusMult < 1f) {
			radiusMult = 1f - (1f - radiusMult) * 0.33f; 
		}
		((CustomCampaignEntityAPI) entity).setRadius(entity.getRadius() * radiusMult);
		entity.setSensorProfile(1f);
		entity.setDiscoverable(false);
		//entity.setDiscoveryXP(spec.getDiscoveryXP());
		
		entity.getDetectedRangeMod().modifyFlat("gen", params.detectedRange);
		entity.setExtendedDetectedAtRange(2000f);
		entity.setDetectionRangeDetailsOverrideMult(0.5f);
		entity.getMemoryWithoutUpdate().set(MemFlags.EXTRA_SENSOR_INDICATORS, 3);
		
		updateFrequency(0f);
		updateSoundDelayAndPlaySound(0f);
		readResolve();
	}
	
	public boolean isDespawning() {
		return despawnType != null || entity.hasTag(Tags.FADING_OUT_AND_EXPIRING);
	}
	
	public void despawn(DespawnType type) {
		if (isDespawning()) return;
		
		float dur = 1f;
		if (type == DespawnType.EXPAND) dur = 2f;
		Misc.fadeAndExpire(entity, dur);
		despawnType = type;
		
		if (type == DespawnType.EXPAND && entity.isInCurrentLocation()) {
			VisibilityLevel level = entity.getVisibilityLevelToPlayerFleet();
			if (level == VisibilityLevel.COMPOSITION_DETAILS || level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS) {
				Global.getSoundPlayer().playSound("abyssal_light_expand_despawn_windup", 1f, 1f, entity.getLocation(), Misc.ZERO);
			}
		}
	}
	
	public void updateSoundDelayAndPlaySound(float amount) {
		untilSoundPlayed -= amount;
		if (untilSoundPlayed <= 0f) {
			//untilSoundPlayed = 0.5f + (float) Math.random() * 20f;
			untilSoundPlayed = 5f + (float) Math.random() * 30f;
		
			// don't play sound when called from the constructor with amount == 0f
			if (amount > 0 && entity.isInCurrentLocation()) {
				VisibilityLevel level = entity.getVisibilityLevelToPlayerFleet();
				if (level == VisibilityLevel.COMPOSITION_DETAILS || level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS) {
					float volume = 1f;
					if (params.bonusMult < 1f) {
						volume = 1f - (1f - params.bonusMult) * 0.6f; 
					}
					if (volume > 1f) volume = 1f;
					if (volume < 0f) volume = 0f;
					
					float pitchMult = 1f;
					if (isDweller()) {
						pitchMult = 0.5f;
					}
					
					SoundAPI sound = Global.getSoundPlayer().playSound("abyssal_light_random_sound", 1f * pitchMult, volume, entity.getLocation(), Misc.ZERO);
					Global.getSector().addScript(new AbyssalLightSoundStopper(sound));
					//flickerDur = 3f + 2f * (float) Math.random();
					flickerDur = 0.5f + 0.5f * (float) Math.random();
					
// too random			
//					if (isDweller()) {
//						CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
//						if (fleet != null && abilityResponseFlickerTimeout <= 0f) {
//							flickerDur = 0.5f + 0.5f * (float) Math.random();
//							abilityResponseFlickerTimeout = 3f;
//							float dist = Misc.getDistance(fleet, entity);
//							if (dist < DWELLER_TRIGGER_RANGE + fleet.getRadius() + entity.getRadius()) {
//								Global.getSector().addScript(new DwellerEncounterScript("DwellerAttackAfterAbilityUse", entity));
//							}						
//						}
//					}
				}
			}
		}
	}
	public void updateFrequency(float amount) {
		untilFrequencyChange -= amount * params.frequencyChangeMult;
		if (untilFrequencyChange <= 0f) {
			untilFrequencyChange = 1f + (float) Math.random() * 3f;
			
			frequencyMult = params.frequencyMultMin * 
					(float) Math.random() * (params.frequencyMultMax - params.frequencyMultMin);
		}
	}
	
	Object readResolve() {
		glow = Global.getSettings().getSprite("campaignEntities", "abyssal_light_glow");
		return this;
	}
	
	public AbyssalLightParams getParams() {
		return params;
	}
	
	public boolean isDweller() {
		return entity != null && entity.hasTag(Tags.DWELLER_LIGHT);
	}
	
	public void advance(float amount) {
		if (isDespawning()) {
			if (!playedDespawnSound && 
					(getDespawnProgress() > DESPAWN_POOF_THRESHOLD || despawnType == DespawnType.FADE_OUT)) {
				playedDespawnSound = true;
				if (entity.isInCurrentLocation()) {
					VisibilityLevel level = entity.getVisibilityLevelToPlayerFleet();
					if (level == VisibilityLevel.COMPOSITION_DETAILS || level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS) {
						String soundId = "abyssal_light_despawn_disrupted";
						if (despawnType == DespawnType.FADE_OUT) {
							soundId = "abyssal_light_despawn_expired";
						}
						Global.getSoundPlayer().playSound(soundId, 1f, 1f, entity.getLocation(), Misc.ZERO);
					}
				}
			}
			
			return;
		}
		
		//boolean dweller = isDespawning(); 
		
		float days = Misc.getDays(amount);
		params.durationDays -= days;
		if (params.durationDays <= 0f || Misc.getAbyssalDepth(entity) < 1f) {
			despawn(DespawnType.FADE_OUT);
			return;
		}
		
		updateFrequency(amount);
		updateSoundDelayAndPlaySound(amount);
		
		phase += amount * GLOW_FREQUENCY * frequencyMult;
		while (phase > 1) phase --;
		
		if (flickerDur > 0) {
			flickerDur -= amount;
			if (flickerDur < 0) flickerDur = 0;
		}
		if (abilityResponseFlickerTimeout > 0) {
			abilityResponseFlickerTimeout -= amount;
			if (abilityResponseFlickerTimeout < 0) abilityResponseFlickerTimeout = 0;
			//System.out.println("Timeout: " + abilityResponseFlickerTimeout);
		}
		
		flicker.advance(amount * 10f);
		fader.advance(amount);
		
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean(MemFlags.GLOBAL_INTERDICTION_PULSE_JUST_USED_IN_CURRENT_LOCATION)) {
			if (entity.getContainingLocation() != null) {
				for (CampaignFleetAPI fleet : entity.getContainingLocation().getFleets()) {
					float range = InterdictionPulseAbility.getRange(fleet);
					float dist = Misc.getDistance(fleet.getLocation(), entity.getLocation());
					if (dist > range) continue;
					
					
					if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.JUST_DID_INTERDICTION_PULSE)) {
						if (isDweller()) {
							if (abilityResponseFlickerTimeout <= 0f) {
								flickerDur = 0.5f + 0.5f * (float) Math.random();
								abilityResponseFlickerTimeout = 6f;
								if (dist < DWELLER_TRIGGER_RANGE + fleet.getRadius() + entity.getRadius()) {
									Global.getSector().addScript(new DwellerEncounterScript("DwellerAttackAfterAbilityUse", fleet, entity));
								}						
							}
						} else {
							AbyssalLightBonus bonus = AbyssalLightBonus.get(fleet);
							
							float brightness = getProximityBasedBrightnessFactor(fleet, entity.getLocation());
							bonus.addBurnBonus(params.bonusMult * brightness);
							
							if (fleet.isPlayerFleet()) {
								bonus.addTopographyPoints(params.getTopographyPoints());
							}
							
							despawn(DespawnType.EXPAND);
						}
					}
				}
			}
			if (isDespawning()) return;
		}
		
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean(MemFlags.GLOBAL_SENSOR_BURST_JUST_USED_IN_CURRENT_LOCATION)) {
			if (entity.getContainingLocation() != null) {
				for (CampaignFleetAPI fleet : entity.getContainingLocation().getFleets()) {
					float range = SENSOR_BURST_TRIGGER_RANGE + fleet.getRadius() + entity.getRadius();
					float dist = Misc.getDistance(fleet.getLocation(), entity.getLocation());
					if (dist > range) continue;
					
					
					if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.JUST_DID_SENSOR_BURST)) {
						if (isDweller()) {
							if (abilityResponseFlickerTimeout <= 0f) {
								flickerDur = 0.5f + 0.5f * (float) Math.random();
								abilityResponseFlickerTimeout = 6f;
								if (dist < DWELLER_TRIGGER_RANGE + fleet.getRadius() + entity.getRadius()) {
									Global.getSector().addScript(new DwellerEncounterScript("DwellerAttackAfterAbilityUse", fleet, entity));
								}						
							}
						} else {
							AbyssalLightBonus bonus = AbyssalLightBonus.get(fleet);
							
							float brightness = getProximityBasedBrightnessFactor(fleet, entity.getLocation());
							bonus.addSensorBonus(params.bonusMult * brightness);
							
							if (fleet.isPlayerFleet()) {
								bonus.addTopographyPoints(params.getTopographyPoints());
								
								// can cause overlapping gravity wells etc
								//EncounterManager.getInstance().getPointTimeout().clear();
								EncounterManager.getInstance().getCreatorTimeout().clear();
							}
							
							despawn(DespawnType.EXPAND);
						}
					}
				}
			}
			if (isDespawning()) return;
		}
	}
	
	public float getFlickerBasedMult() {
		if (flickerDur <= 0f) return 1f;
		
		float mult = 1f;
		if (flickerDur < 1f) mult = flickerDur;
		
		if (isDweller()) {
			mult *= 0.75f;
		} else {
			mult *= 0.25f;
		}
		
		float f = 1f - flicker.getBrightness() * mult;
		return f;
	}
	
	public float getGlowAlpha() {
		float glowAlpha = 0f;
		if (phase < 0.5f) glowAlpha = phase * 2f;
		if (phase >= 0.5f) glowAlpha = (1f - (phase - 0.5f) * 2f);
		glowAlpha = 0.75f + glowAlpha * 0.25f;
		
		if (glowAlpha < 0) glowAlpha = 0;
		if (glowAlpha > 1) glowAlpha = 1;
		return glowAlpha;
	}
	
	

	public float getRenderRange() {
		return entity.getRadius() + params.size * 0.75f;
	}

	public float getDespawnProgress() {
		if (!isDespawning()) return 0f;
		float f = entity.getSensorFaderBrightness();
		return 1 - f;
	}
	
	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		float alphaMult = viewport.getAlphaMult();
		
		if (!isDespawning() || despawnType == DespawnType.FADE_OUT) {
			alphaMult *= entity.getSensorFaderBrightness();
		}
		
		if (alphaMult <= 0) return;
		
		float f = getDespawnProgress();
		if (despawnType == DespawnType.FADE_OUT) {
			f = 0f;
		}
		
		if (f > DESPAWN_POOF_THRESHOLD) {
			float fadeOut = (1f - f) / (1f - DESPAWN_POOF_THRESHOLD);
			alphaMult *= fadeOut;
		}
		
		VisibilityLevel level = entity.getVisibilityLevelToPlayerFleet();
		if (level == VisibilityLevel.COMPOSITION_DETAILS || level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS) {
			fader.fadeIn();
		} else {
			fader.fadeOut();
		}
		
		alphaMult *= fader.getBrightness();
		
		float b = getPlayerProximityBasedBrightnessFactor(entity);
		alphaMult *= b;
		
		
//		if (Misc.getDistanceToPlayerLY(entity) > 0.1f) {
//			Misc.fadeAndExpire(entity);
//			return;
//		}
//		if (!Global.getSector().isPaused()) {
//			System.out.println("Alpha: " + alphaMult);
//		}

		CustomEntitySpecAPI spec = entity.getCustomEntitySpec();
		if (spec == null) return;
		
		float w = spec.getSpriteWidth();
		float h = spec.getSpriteHeight();
		
		Vector2f loc = entity.getLocation();
		
		float glowAlpha = getGlowAlpha();
		
		glow.setColor(params.color);
		
		w = params.size;
		h = params.size;
		
		float scale = 0.25f + alphaMult * 0.75f;
		if (f > DESPAWN_POOF_THRESHOLD) {
			scale *= 1.5f;
		}
		
		float fringeScale = 1f;
		//fringeScale = 1.5f;
		
		glow.setAdditiveBlend();
		
		if (layer == CampaignEngineLayers.TERRAIN_8) {
			//float flicker = getFlickerBasedMult();
			glow.setAlphaMult(alphaMult * glowAlpha * 0.5f);
			glow.setSize(w * scale * fringeScale, h * scale * fringeScale);
			
			glow.renderAtCenter(loc.x, loc.y);
		}
		
		if (layer == CampaignEngineLayers.STATIONS) {
			if (f > 0f) {
				float extra = 1f + f * 1.33f;
				if (f > DESPAWN_POOF_THRESHOLD) extra = 0f;
				scale *= extra;
			}
			float flicker = getFlickerBasedMult();
			for (int i = 0; i < 5; i++) {
				if (i != 0) flicker = 1f;
				
				w *= 0.3f;
				h *= 0.3f;
				glow.setSize(w * scale, h * scale);
				glow.setAlphaMult(alphaMult * glowAlpha * 0.67f * flicker);
				glow.renderAtCenter(loc.x, loc.y);
			}
		}
	}
	
	
	public static float getPlayerProximityBasedBrightnessFactor(SectorEntityToken entity) {
		return getPlayerProximityBasedBrightnessFactor(entity.getLocation());
	}
	public static float getPlayerProximityBasedBrightnessFactor(Vector2f loc) {
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player == null) {
			return PLAYER_PROXIMITY_MIN_BRIGHTNESS;
		}
		return getProximityBasedBrightnessFactor(player, loc);
	}
	
	public static float getProximityBasedBrightnessFactor(CampaignFleetAPI from, Vector2f loc) {
		
		float dist = Misc.getDistance(from.getLocation(), loc) - from.getRadius();
		
		if (dist <= PLAYER_PROXIMITY_MAX_BRIGHTNESS_AT) {
			return 1f;
			//return Math.max(PLAYER_PROXIMITY_MIN_BRIGHTNESS, dist / PLAYER_PROXIMITY_MAX_BRIGHTNESS_AT);
		}
		dist -= PLAYER_PROXIMITY_MAX_BRIGHTNESS_AT;
		
		float f = 1f - dist / (PLAYER_PROXIMITY_MIN_BRIGHTNESS_AT - PLAYER_PROXIMITY_MAX_BRIGHTNESS_AT);
		if (f < 0f) f = 0f;
		
		float b = PLAYER_PROXIMITY_MIN_BRIGHTNESS + f * (1f - PLAYER_PROXIMITY_MIN_BRIGHTNESS);
		
		return b;
	}
	

//	@Override
//	public void createMapTooltip(TooltipMakerAPI tooltip, boolean expanded) {
//		String post = "";
//		Color color = entity.getFaction().getBaseUIColor();
//		Color postColor = color;
//		if (entity.getMemoryWithoutUpdate().getBoolean(RemnantSystemType.DESTROYED.getBeaconFlag())) {
//			post = " - Low";
//			postColor = Misc.getPositiveHighlightColor();
//		} else if (entity.getMemoryWithoutUpdate().getBoolean(RemnantSystemType.SUPPRESSED.getBeaconFlag())) {
//			post = " - Medium";
//			postColor = Misc.getHighlightColor();
//		} else if (entity.getMemoryWithoutUpdate().getBoolean(RemnantSystemType.RESURGENT.getBeaconFlag())) {
//			post = " - High";
//			postColor = Misc.getNegativeHighlightColor();
//		}
//		
//		tooltip.addPara(entity.getName() + post, 0f, color, postColor, post.replaceFirst(" - ", ""));
//	}
//
//	@Override
//	public boolean hasCustomMapTooltip() {
//		return true;
//	}
//	
//	@Override
//	public void appendToCampaignTooltip(TooltipMakerAPI tooltip, VisibilityLevel level) {
//		if (level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS || 
//				level == VisibilityLevel.COMPOSITION_DETAILS) {
//			
//			String post = "";
//			Color color = Misc.getTextColor();
//			Color postColor = color;
//			if (entity.getMemoryWithoutUpdate().getBoolean(RemnantSystemType.DESTROYED.getBeaconFlag())) {
//				post = "low";
//				postColor = Misc.getPositiveHighlightColor();
//			} else if (entity.getMemoryWithoutUpdate().getBoolean(RemnantSystemType.SUPPRESSED.getBeaconFlag())) {
//				post = "medium";
//				postColor = Misc.getHighlightColor();
//			} else if (entity.getMemoryWithoutUpdate().getBoolean(RemnantSystemType.RESURGENT.getBeaconFlag())) {
//				post = "high";
//				postColor = Misc.getNegativeHighlightColor();
//			}
//			if (!post.isEmpty()) {
//				tooltip.setParaFontDefault();
//				tooltip.addPara(BaseIntelPlugin.BULLET + "Danger level: " + post, 10f, color, postColor, post);
//			}
//		}
//		
//	}
}









