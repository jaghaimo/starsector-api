package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AsteroidAPI;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TerrainAIFlags;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class AsteroidBeltTerrainPlugin extends BaseRingTerrain implements AsteroidSource {
	
//	public static float MIN_BURN_PENALTY = 0.1f;
//	public static float BURN_PENALTY_RANGE = 0.4f;
	
	public static class AsteroidBeltParams extends RingParams {
		public int numAsteroids;
		//public float orbitRadius;
		//public float width;
		public float minOrbitDays;
		public float maxOrbitDays;
		public float minSize;
		public float maxSize;
		public AsteroidBeltParams(int numAsteroids, float orbitRadius,
				float width, float minOrbitDays, float maxOrbitDays,
				float minSize, float maxSize, String name) {
			super(width, orbitRadius, null, name);
			this.numAsteroids = numAsteroids;
			//this.orbitRadius = orbitRadius;
			//this.width = width;
			this.minOrbitDays = minOrbitDays;
			this.maxOrbitDays = maxOrbitDays;
			this.minSize = minSize;
			this.maxSize = maxSize;
		}
	}

	
	@Override
	protected Object readResolve() {
		super.readResolve();
		return this;
	}
	
	private transient RingRenderer rr;
	public void renderOnMap(float factor, float alphaMult) {
		if (params == null) return;
		if (rr == null) {
			rr = new RingRenderer("systemMap", "map_asteroid_belt");
		}
		Color color = Global.getSettings().getColor("asteroidBeltMapColor");
		float bandWidth = params.bandWidthInEngine;
		bandWidth = 300f;
		rr.render(entity.getLocation(),
				  params.middleRadius - bandWidth * 0.5f,
				  params.middleRadius + bandWidth * 0.5f,
				  color,
				  false, factor, alphaMult);
	}

	public void regenerateAsteroids() {
		createAsteroids();
	}

	protected boolean needToCreateAsteroids = true;
	protected void createAsteroids() {
		if (!(params instanceof AsteroidBeltParams)) return;
		
		Random rand = new Random(Global.getSector().getClock().getTimestamp() + entity.getId().hashCode());
		
		LocationAPI location = entity.getContainingLocation();
		for (int i = 0; i < params.numAsteroids; i++) {
			//float size = 8f + (float) Math.random() * 25f;
			float size = params.minSize + rand.nextFloat() * (params.maxSize - params.minSize);
			AsteroidAPI asteroid = location.addAsteroid(size);

			asteroid.setFacing(rand.nextFloat() * 360f);
			float currRadius = params.middleRadius - params.bandWidthInEngine/2f + rand.nextFloat() * params.bandWidthInEngine;
			float angle = rand.nextFloat() * 360f;
			float orbitDays = params.minOrbitDays + rand.nextFloat() * (params.maxOrbitDays - params.minOrbitDays);
			asteroid.setCircularOrbit(this.entity, angle, currRadius, orbitDays);
			Misc.setAsteroidSource(asteroid, this);
		}
		needToCreateAsteroids = false;
	}
	
	public void advance(float amount) {
		if (needToCreateAsteroids) {
			createAsteroids();
		}
		super.advance(amount);
		
//		if (entity.isInCurrentLocation()) {
//			System.out.println("Params: " + params + ", name: " + getNameForTooltip());
//			if (params == null) {
//				System.out.println("efwefwe");
//			}
//		}
	}
	
	
	
//	public static Map<HullSize, Float> burnPenalty = new HashMap<HullSize, Float>();
//	static {
//		burnPenalty.put(HullSize.FIGHTER, 0f);
//		burnPenalty.put(HullSize.FRIGATE, 0f);
//		burnPenalty.put(HullSize.DESTROYER, 1f);
//		burnPenalty.put(HullSize.CRUISER, 2f);
//		burnPenalty.put(HullSize.CAPITAL_SHIP, 3f);
//	}
	
	
//	public void init(String terrainId, SectorEntityToken entity, Object param) {
//		super.init(terrainId, entity, param);
//		if (params.name == null) {
//			params.name = "Asteroid Belt";
//		}
//	}
	
	public AsteroidBeltParams params;
	public void init(String terrainId, SectorEntityToken entity, Object param) {
		super.init(terrainId, entity, param);
		if (param instanceof AsteroidBeltParams) {
			params = (AsteroidBeltParams) param;
			name = params.name;
			if (name == null) {
				name = "Asteroid Belt";
			}
		}
	}
	
	
	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		super.render(layer, viewport);
	}

	@Override
	public void applyEffect(SectorEntityToken entity, float days) {
		if (entity instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
			
//			float penalty = getBurnPenalty(fleet);
//			fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_1",
//								"Inside " + getNameForTooltip().toLowerCase(), 1f - penalty, 
//								fleet.getStats().getFleetwideMaxBurnMod());
			
			if (fleet.getCurrBurnLevel() <= RingSystemTerrainPlugin.MAX_SNEAK_BURN_LEVEL) {
				fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_2",
									"Hiding inside " + getNameForTooltip().toLowerCase(), RingSystemTerrainPlugin.VISIBLITY_MULT, 
									fleet.getStats().getDetectedRangeMod());
			}
//			if (fleet.isPlayerFleet()) {
//				System.out.println("efwefwe");
//			}
			String key = "$asteroidImpactTimeout";
			String sKey = "$skippedImpacts";
			float probPerSkip = 0.15f;
			float maxProb = 1f;
			float maxSkipsToTrack = 7;
			float durPerSkip = 0.2f;
			MemoryAPI mem = fleet.getMemoryWithoutUpdate();
			if (!mem.contains(key)) {
				float expire = mem.getExpire(sKey);
				if (expire < 0) expire = 0;
				
				float hitProb = Misc.getFleetRadiusTerrainEffectMult(fleet) * 0.5f;
				//hitProb = 0.33f;
				hitProb = 0.5f;
				//hitProb = 1f;
				hitProb = expire / durPerSkip * probPerSkip;
				if (hitProb > maxProb) hitProb = maxProb;
				if ((float) Math.random() < hitProb) {
					fleet.addScript(new AsteroidImpact(fleet));
					mem.set(sKey, true, 0);
				} else {
					mem.set(sKey, true, Math.min(expire + durPerSkip, maxSkipsToTrack * durPerSkip));
				}
				mem.set(key, true, (float) (0.05f + 0.1f * Math.random()));
				//mem.set(key, true, (float) (0.01f + 0.02f * Math.random()));
			}
		}
	}
	
//	public static float getFleetRadiusTerrainEffectMult(CampaignFleetAPI fleet) {
//		float min = Global.getSettings().getBaseFleetSelectionRadius() + Global.getSettings().getFleetSelectionRadiusPerUnitSize();
//		float max = Global.getSettings().getMaxFleetSelectionRadius();
//		float radius = fleet.getRadius();
//		
//		//radius = 1000;
//
//		float mult = (radius - min) / (max - min);
//		if (mult > 1) mult = 1;
//		//if (mult < 0) mult = 0;
//		if (mult < 0.1f) mult = 0.1f;
//		//mult = MIN_BURN_PENALTY + mult * BURN_PENALTY_RANGE;
//
//		float skillMod = fleet.getCommanderStats().getDynamic().getValue(Stats.NAVIGATION_PENALTY_MULT);
//		mult *= skillMod;
//		
//		return mult;
//	}
	
//	protected float getBurnPenalty(CampaignFleetAPI fleet) {
//		float min = Global.getSettings().getBaseFleetSelectionRadius() + Global.getSettings().getFleetSelectionRadiusPerUnitSize();
//		float max = Global.getSettings().getMaxFleetSelectionRadius();
//		float radius = fleet.getRadius();
//
//		float penalty = (radius - min) / (max - min);
//		if (penalty > 1) penalty = 1;
//		if (penalty < 0) penalty = 0;
//		penalty = MIN_BURN_PENALTY + penalty * BURN_PENALTY_RANGE;
//
//		float skillMod = fleet.getCommanderStats().getDynamic().getValue(Stats.NAVIGATION_PENALTY_MULT);
//		penalty *= skillMod;
//		
//		return penalty;
//	}

	public boolean hasTooltip() {
		return true;
	}
	
	protected String getNameForTooltip() {
		return "Asteroid Belt";
	}
	
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		float pad = 10f;
		float small = 5f;
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color fuel = Global.getSettings().getColor("progressBarFuelColor");
		Color bad = Misc.getNegativeHighlightColor();
		
		//tooltip.addTitle(params.name);
		tooltip.addTitle(getNameForTooltip());
		tooltip.addPara(Global.getSettings().getDescription(getTerrainId(), Type.TERRAIN).getText1(), pad);
		
		float nextPad = pad;
		if (expanded) {
			tooltip.addSectionHeading("Travel", Alignment.MID, pad);
			nextPad = small;
		}
		
//		tooltip.addPara("Reduces the travel speed of fleets inside by up to %s. Smaller fleets are more easily able to maneuver the enclosing drive bubble to avoid collisions and suffer a lower penalty.",
//				nextPad,
//				highlight,
//				"" + (int) ((MIN_BURN_PENALTY + BURN_PENALTY_RANGE) * 100f) + "%"				
//		);
//		
//		float penalty = getBurnPenalty(Global.getSector().getPlayerFleet());
//		String penaltyStr = Misc.getRoundedValue(1f - penalty);
//		tooltip.addPara("Your fleet's speed is reduced by %s.", pad,
//				highlight,
//				"" + (int) Math.round((penalty) * 100) + "%"
//				//Strings.X + penaltyStr
//		);
		
		tooltip.addPara("Chance of asteroid impacts on the drive field bubble. The impacts do not present a " +
				"direct danger to ships but may briefly knock the fleet off course.", nextPad);
		
		tooltip.addPara("Smaller fleets are usually able to avoid the heavier impacts, and fleets traveling at burn %s or below do not risk impacts at all.", pad,
				highlight,
				"" + (int)Math.round(AsteroidImpact.SAFE_BURN_LEVEL)
		);
		
//		tooltip.addPara("Reduces the range at which stationary fleets inside it can be detected by %s.", pad,
//				highlight, 
//				"" + (int) ((1f - RingSystemTerrainPlugin.VISIBLITY_MULT) * 100) + "%"
//		);
		
		String stop = Global.getSettings().getControlStringForEnumName("GO_SLOW");
		tooltip.addPara("Reduces the range at which stationary or slow-moving* fleets inside it can be detected by %s.", nextPad,
				highlight, 
				"" + (int) ((1f - RingSystemTerrainPlugin.VISIBLITY_MULT) * 100) + "%"
		);
		tooltip.addPara("*Press and hold %s to stop; combine with holding the left mouse button down to move slowly.", nextPad,
				Misc.getGrayColor(), highlight, 
				stop
		);
		
//		tooltip.addPara("Reduces the maximum burn level of ships depending on size. Smaller ships are more easily able to manuver to avoid impacts and suffer a smaller penalty.", nextPad);
//		tooltip.beginGrid(150, 1);
//		tooltip.addToGrid(0, 0, "  Frigates", "" + -burnPenalty.get(HullSize.FRIGATE).intValue());
//		tooltip.addToGrid(0, 1, "  Destroyers", "" + -burnPenalty.get(HullSize.DESTROYER).intValue());
//		tooltip.addToGrid(0, 2, "  Cruisers", "" + -burnPenalty.get(HullSize.CRUISER).intValue());
//		tooltip.addToGrid(0, 3, "  Capital ships", "" + -burnPenalty.get(HullSize.CAPITAL_SHIP).intValue());
//		tooltip.addGrid(3f);
		
		if (expanded) {
			tooltip.addSectionHeading("Combat", Alignment.MID, pad);
			tooltip.addPara("Numerous asteroids present on the battlefield. Large enough to be an in-combat navigational hazard.", small);
		}
		
		//tooltip.addPara("Does not stack with other similar terrain effects.", pad);
	}
	
	public boolean isTooltipExpandable() {
		return true;
	}
	
	public float getTooltipWidth() {
		return 350f;
	}
	
	public String getEffectCategory() {
		return "asteroid_belt";
	}
	
	public boolean hasAIFlag(Object flag) {
		return flag == TerrainAIFlags.REDUCES_SPEED_LARGE;
	}

	
	public void reportAsteroidPersisted(SectorEntityToken asteroid) {
		if (Misc.getAsteroidSource(asteroid) == this) {
			params.numAsteroids--;
		}
	}
}
