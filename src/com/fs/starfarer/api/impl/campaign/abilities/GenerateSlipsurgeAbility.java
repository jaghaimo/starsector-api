package com.fs.starfarer.api.impl.campaign.abilities;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2.SlipstreamParams2;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2.SlipstreamSegment;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FleetMemberDamageLevel;

public class GenerateSlipsurgeAbility extends DurationAbilityWithCost2 {

	public static boolean REQUIRE_GIANT_STARS_OR_STRONGER = true;
	
	public static Map<String, Float> SLIPSURGE_STRENGTH = new LinkedHashMap<String, Float>();
	static {
		// I'm aware that these mappings do not reflect actual star mass, which varies
		// wildly in any case please don't @ me thank you -am
		
		SLIPSURGE_STRENGTH.put(StarTypes.BLACK_HOLE, 1f);
		SLIPSURGE_STRENGTH.put(StarTypes.NEUTRON_STAR, 0.9f);
		
		SLIPSURGE_STRENGTH.put(StarTypes.BLUE_SUPERGIANT, 0.8f);
		SLIPSURGE_STRENGTH.put(StarTypes.RED_SUPERGIANT, 0.8f);
		
		SLIPSURGE_STRENGTH.put(StarTypes.RED_GIANT, 0.6f);
		SLIPSURGE_STRENGTH.put(StarTypes.BLUE_GIANT, 0.6f);
		SLIPSURGE_STRENGTH.put(StarTypes.ORANGE_GIANT, 0.6f);
		
		SLIPSURGE_STRENGTH.put(StarTypes.ORANGE, 0.4f);
		SLIPSURGE_STRENGTH.put(StarTypes.YELLOW, 0.4f);
		
		SLIPSURGE_STRENGTH.put(StarTypes.WHITE_DWARF, 0.25f);
		SLIPSURGE_STRENGTH.put(StarTypes.RED_DWARF, 0.2f);
		
		SLIPSURGE_STRENGTH.put(StarTypes.BROWN_DWARF, 0.1f);
		
		SLIPSURGE_STRENGTH.put(StarTypes.GAS_GIANT, 0f);
		SLIPSURGE_STRENGTH.put(StarTypes.ICE_GIANT, 0f);
	}
	
	public static float SLIPSURGE_STRENGTH_MULT = 1.3f;
	
	public static float TRANSIT_MUSIC_SUPPRESSION = 1f;
	public static String TRANSIT_SOUND_LOOP = "ui_slipsurge_travel_loop";
	
	public static float FUEL_COST_MULT = 5f;
	public static float CR_COST_MULT = 0.25f;
	public static float SENSOR_RANGE_MULT = 0.1f;
	
	public static String SENSOR_MOD_ID = "slipsurge_sensor_penalty";
	
	public static class SlipsurgeFadeInScript implements EveryFrameScript {
		protected SlipstreamTerrainPlugin2 plugin;
		protected boolean done = false;
		protected int index = 0;
		protected float elapsed = 0f;
		protected float maxElapsed = 0f;
		public SlipsurgeFadeInScript(SlipstreamTerrainPlugin2 plugin) {
			this.plugin = plugin;

			float fadeInLength = 300f;
			float lengthSoFar = 0f;
			float durIn = 0.2f;
			maxElapsed = durIn * plugin.getSegments().size() * 0.34f + 3f;
			for (SlipstreamSegment seg : plugin.getSegments()) {
				seg.fader.setDurationIn(durIn);
				seg.fader.forceOut();
				
				seg.bMult = Math.min(1f, lengthSoFar / fadeInLength);
				lengthSoFar += seg.lengthToNext;
			}
		}
		public void advance(float amount) {
			if (done) return;
			
			// amount == 1 when not current location
			// which isn't fine-grained enough to fade the segments in
			// one at a time since each takes <1s
			if (!plugin.getEntity().isInCurrentLocation()) {
				for (SlipstreamSegment curr : plugin.getSegments()) {
					curr.fader.fadeIn();
				}
				done = true;
				return;
			}
			
			
			elapsed += amount;
			if (index >= plugin.getSegments().size() || elapsed > maxElapsed) {
				done = true;
				return;
			}
			SlipstreamSegment curr = plugin.getSegments().get(index);
			if (curr.fader.isFadedIn()) {
				index++;
				return;
			}
			curr.fader.fadeIn();
			if (curr.fader.getBrightness() > 0.33f && index < plugin.getSegments().size() - 1) {
				plugin.getSegments().get(index + 1).fader.fadeIn();
			}
			if (curr.fader.getBrightness() > 0.67f && index < plugin.getSegments().size() - 2) {
				plugin.getSegments().get(index + 2).fader.fadeIn();
			}
		}

		public boolean isDone() {
			return done;
		}
		public boolean runWhilePaused() {
			return false;
		}
	}
	
	public static class ExpandStormRadiusScript implements EveryFrameScript {
		protected float elapsed = 0f;
		protected float extraRadius;
		public ExpandStormRadiusScript(float extraRadius) {
			this.extraRadius = extraRadius;
		}
		public void advance(float amount) {
			elapsed += amount;
			
			CampaignTerrainAPI terrain = Misc.getHyperspaceTerrain();
			if (terrain != null) {
				HyperspaceTerrainPlugin htp = (HyperspaceTerrainPlugin) terrain.getPlugin();
				htp.setExtraDistanceAroundPlayerToAdvanceStormCells(extraRadius);
				htp.setStormCellTimeMultOutsideBaseArea(5f);
			}
		}

		public boolean isDone() {
			return elapsed >= 10f;
		}
		public boolean runWhilePaused() {
			return false;
		}
	}
	
	public static class SlipsurgeEffectScript implements EveryFrameScript {
		protected CampaignFleetAPI fleet;
		protected boolean triggered = false;
		protected boolean done = false;
		protected boolean didTempCleanup = false;
		protected float elapsed = 0f;
		protected SlipstreamTerrainPlugin2 plugin;
		public SlipsurgeEffectScript(CampaignFleetAPI fleet, SlipstreamTerrainPlugin2 plugin) {
			this.fleet = fleet;
			this.plugin = plugin;
		}
		
		public void abort() {
			done = true;
			didTempCleanup = true;
			fleet.fadeInIndicator();
			fleet.setNoEngaging(0f);
			fleet.getStats().getSensorRangeMod().unmodifyMult(SENSOR_MOD_ID);
			for (FleetMemberViewAPI view : fleet.getViews()) {
				view.endJitter();
			}
			if (fleet.isPlayerFleet()) {
				Global.getSector().getCampaignUI().setFollowingDirectCommand(false);
			}
			//fleet.setMoveDestination(dest.x, dest.y)
		}
		
		public void advance(float amount) {
			if (done) return;
			
			if (fleet.isInHyperspaceTransition() ||
						plugin.getEntity().getContainingLocation() != fleet.getContainingLocation()) {
				if (!didTempCleanup) {
					abort();
				}
				// fleet could conceivably come back, set done back to false so the script keeps running
				done = false;
				return;
			}
			
			elapsed += amount;
			if (!triggered) {
				if (elapsed >= 30f || 
						!plugin.getEntity().isAlive()) {
					done = true;
					return;
				}
			}
			
			float burn = Misc.getBurnLevelForSpeed(fleet.getVelocity().length());
			if (burn > 50 && plugin.containsEntity(fleet)) {
				triggered = true;
			}
			
			if (triggered) {
				if (burn < 50) {
					plugin.despawn(0, 0.2f, new Random());
					abort();
					return;
				}
				
				AbilityPlugin gs = fleet.getAbility(Abilities.GENERATE_SLIPSURGE);
				if (gs instanceof BaseAbilityPlugin) {
					BaseAbilityPlugin base = (BaseAbilityPlugin) gs;
					for (AbilityPlugin curr : fleet.getAbilities().values()) {
						if (curr == this) continue;
						if (!base.isCompatible(curr)) {
							if (curr.isActiveOrInProgress()) {
								curr.deactivate();
							}
							curr.forceDisable();
						}
					}
				}
				
				float decelMult = Misc.getAbyssalDepth(fleet) * 1f;
				if (fleet.getGoSlowOneFrame()) decelMult = 1f;
				//if (fleet.getGoSlowOneFrame() && !plugin.containsEntity(fleet)) {
				if (decelMult > 0f && !plugin.containsEntity(fleet)) {
					float angle = Misc.getAngleInDegrees(fleet.getVelocity());
					Vector2f decelDir = Misc.getUnitVectorAtDegreeAngle(angle + 180f);
					
					float targetDecelSeconds = 0.5f;
					float speed = fleet.getVelocity().length();
					float decelAmount = amount * speed / targetDecelSeconds;
					decelDir.scale(decelAmount * decelMult);
					
					Vector2f vel = fleet.getVelocity();
					fleet.setVelocity(vel.x + decelDir.x, vel.y + decelDir.y);
				}
				
				
				fleet.fadeOutIndicator();
				fleet.setNoEngaging(0.1f);
				fleet.setInteractionTarget(null);
				if (fleet.isPlayerFleet()) {
					Global.getSector().getCampaignUI().setFollowingDirectCommand(true);
				}
				fleet.getMemoryWithoutUpdate().set(MemFlags.NO_HIGH_BURN_TOPOGRAPHY_READINGS, true, 0.1f);
				fleet.getStats().getSensorRangeMod().modifyMult(SENSOR_MOD_ID, SENSOR_RANGE_MULT, "Extreme burn level");
				didTempCleanup = false;
				
				float angle = Misc.getAngleInDegrees(fleet.getVelocity());
				Vector2f jitterDir = Misc.getUnitVectorAtDegreeAngle(angle + 180f);
				Vector2f windDir = Misc.getUnitVectorAtDegreeAngle(angle);
				float windIntensity = (burn - 50f) / 250f;
				if (windIntensity < 0) windIntensity = 0;
				//if (windIntensity > 1f) windIntensity = 1f;
				
				float b = (burn - 50f) / 450f;
				if (b < 0) b = 0;
				if (b > 1f) b = 1f;
				if (b > 0) {
					
					if (fleet.isPlayerFleet()) {
						float volume = b;
						Global.getSector().getCampaignUI().suppressMusic(TRANSIT_MUSIC_SUPPRESSION * volume);
						Global.getSoundPlayer().setNextLoopFadeInAndOut(0.05f, 0.5f);
						Global.getSoundPlayer().playLoop(TRANSIT_SOUND_LOOP, fleet, 
									1f, volume,
									fleet.getLocation(), fleet.getVelocity());						
					}
					
					
					//String modId = "SlipsurgeEffectScript_" + fleet.getId();
					for (FleetMemberViewAPI view : fleet.getViews()) {
						Color c = view.getMember().getHullSpec().getHyperspaceJitterColor();
//						view.setJitter(1f, 1f, c, 7, 10f);
//						view.setJitterBrightness(b);
//						//view.setJitter(0.25f, 1f, c, 10 + Math.round(40f * b), 20f);
//						view.setUseCircularJitter(true);
						
						c = Misc.setAlpha(c, 60);
						view.setJitter(0.1f, 1f, c, 10 + Math.round(40f * b), 20f);
						view.setUseCircularJitter(true);
						view.setJitterDirection(jitterDir);
						view.setJitterLength(30f * b);
						view.setJitterBrightness(b);
						
					}
				} else {
					for (FleetMemberViewAPI view : fleet.getViews()) {
						view.endJitter();
					}
				}
			}
			
		}

		public boolean isDone() {
			return done;
		}
		public boolean runWhilePaused() {
			return false;
		}
	}
	
	protected Boolean primed = null;
	protected Vector2f startLoc = null;
	protected JumpPointAPI well = null;
	
	
	public float getFuelCostMult() {
		return FUEL_COST_MULT;
	}
	public float getCRCostMult() {
		return CR_COST_MULT;
	}
	public FleetMemberDamageLevel getActivationDamageLevel() {
		return FleetMemberDamageLevel.MEDIUM;
	}
	public boolean canRecoverCRWhileActive() {
		return true;
	}
	
	
	@Override
	protected void activateImpl() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		primed = true;
		well = findGravityWell();
		if (well == null) return;
		
		//startLoc = new Vector2f(fleet.getLocation());
		float angle = Misc.getAngleInDegrees(well.getLocation(), fleet.getLocation());
		float offset = 100f;
		Vector2f from = Misc.getUnitVectorAtDegreeAngle(angle);
		from.scale(offset + fleet.getRadius());
		Vector2f.add(from, fleet.getLocation(), from);
		startLoc = from;
		
		
		SectorEntityToken token = fleet.getContainingLocation().createToken(startLoc);
		//Global.getSector().addPing(fleet, Pings.SLIPSURGE);
		Global.getSector().addPing(token, Pings.SLIPSURGE);
		
		well.getContainingLocation().addScript(new ExpandStormRadiusScript(16000f));
		
		deductCost();
	}
	
	@Override
	protected void applyStatsEffect(float amount, float level) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		if (level > 0 && level < 1 && amount > 0) {
			fleet.goSlowOneFrame();
			return;
		}
		
		if (level == 1 && primed != null) {
			generateSlipstream();
			primed = null;
			startLoc = null;
			well = null;
		}
		
	}
	
	
	protected void generateSlipstream() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		//JumpPointAPI jp = findGravityWell();
		JumpPointAPI jp = well;
		if (jp ==  null) return;
		
		float strength = getStrengthForGravityWell(jp);
		strength *= SLIPSURGE_STRENGTH_MULT;
		
		float angle = Misc.getAngleInDegrees(jp.getLocation(), startLoc);
		float offset = 100f;
//		Vector2f from = Misc.getUnitVectorAtDegreeAngle(angle);
//		from.scale(offset + fleet.getRadius());
//		Vector2f.add(from, startLoc, from);
		Vector2f from = startLoc;
		
		SlipstreamParams2 params = new SlipstreamParams2();
		
		params.enteringSlipstreamTextOverride = "Entering slipsurge";
		params.enteringSlipstreamTextDurationOverride = 0.1f;
		params.forceNoWindVisualEffectOnFleets = true;
		
		float width = 600f;
		float length = 1000f;

		length += strength * 500f;
		params.burnLevel = Math.round(400f + strength * strength * 500f);
		params.accelerationMult = 20f + strength * strength * 280f; 
		
		//params.accelerationMult = 500f;
		
		params.baseWidth = width;
		params.widthForMaxSpeed = 400f;
		params.widthForMaxSpeedMinMult = 0.34f;
		//params.widthForMaxSpeed = 300f;
		params.slowDownInWiderSections = true;
		//params.edgeWidth = 100f;
		//params.accelerationMult = 100f;
		
		
		params.minSpeed = Misc.getSpeedForBurnLevel(params.burnLevel - params.burnLevel/8);
		params.maxSpeed = Misc.getSpeedForBurnLevel(params.burnLevel + params.burnLevel/8);
		//params.lineLengthFractionOfSpeed = 0.25f * Math.max(0.25f, Math.min(1f, 30f / (float) params.burnLevel));
		params.lineLengthFractionOfSpeed = 2000f / ((params.maxSpeed + params.minSpeed) * 0.5f);
		
		float lineFactor = 0.1f;
		params.minSpeed *= lineFactor;
		params.maxSpeed *= lineFactor;
		//params.lineLengthFractionOfSpeed *= 0.25f;
		//params.lineLengthFractionOfSpeed *= 1f;
		params.maxBurnLevelForTextureScroll = (int) (params.burnLevel * 0.1f);
		
		params.particleFadeInTime = 0.01f;
		params.areaPerParticle = 1000f; 
		
		Vector2f to = Misc.getUnitVectorAtDegreeAngle(angle);
		to.scale(offset + fleet.getRadius() + length);
		Vector2f.add(to, startLoc, to);
		
		CampaignTerrainAPI slipstream = (CampaignTerrainAPI) well.getContainingLocation().addTerrain(Terrain.SLIPSTREAM, params);
		slipstream.addTag(Tags.SLIPSTREAM_VISIBLE_IN_ABYSS);
		slipstream.setLocation(from.x, from.y);
		
		SlipstreamTerrainPlugin2 plugin = (SlipstreamTerrainPlugin2) slipstream.getPlugin();
		
		float spacing = 100f;
		float incr = spacing / length;

		Vector2f diff = Vector2f.sub(to, from, new Vector2f());
		for (float f = 0; f <= 1f; f += incr) {
			Vector2f curr = new Vector2f(diff);
			curr.scale(f);
			Vector2f.add(curr, from, curr);
			plugin.addSegment(curr, width - Math.min(300f, 300f * (float)Math.sqrt(f)));
			//plugin.addSegment(curr, width);
		}
		
		plugin.recomputeIfNeeded();
		
		plugin.despawn(1.5f, 0.2f, new Random());
		
		slipstream.addScript(new SlipsurgeFadeInScript(plugin));
		fleet.addScript(new SlipsurgeEffectScript(fleet, plugin));
		
	}
	
	@Override
	protected void unapplyStatsEffect() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		primed = null;
		startLoc = null;
		well = null;
	}
	

	@Override
	public boolean isUsable() {
		if (!super.isUsable()) return false;
		return super.isUsable() && 
					getFleet() != null &&
					getFleet().isInHyperspace() &&
					findGravityWell() != null;
					//findGravityWell() != null || Misc.isInsideSlipstream(getFleet()));
					//(getFleet().isAIMode() || computeFuelCost(Misc.isInsideSlipstream(getFleet())) <= getFleet().getCargo().getFuel());
	}

	
	@Override
	public void addInitialDescription(TooltipMakerAPI tooltip, boolean expanded) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		Color text = Misc.getTextColor();
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		float pad = 10f;
		
		
		String extra = "";
		if (REQUIRE_GIANT_STARS_OR_STRONGER) {
			extra = "and availability ";
		}
			
			
		// some of the other factors: it has "dwarf" or "giant" in the name
		tooltip.addPara("Overload and modulate the fleet's drive field "
				+ "to induce an extremely powerful, "
				+ "short-duration slipstream flowing away from the nearest gravity well, its strength " + extra
				+ "based on the stellar object's mass, density, and some other poorly-understood factors. "
				+ "Largely ineffective inside abyssal hyperspace.", pad);
		
//		tooltip.addPara("A stronger surge can allow the fleet to rapidly travel up to %s light-years. "
//				+ "Attempting to move slowly "
//				+ "during the transit will decelerate the fleet quickly.", pad, highlight, 
//				"10", "move slowly"); 

		FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
		Color starColor = Misc.getBasePlayerColor();
		tooltip.beginTable(player, 20f, "Stellar object type", getTooltipWidth() - 150f, "Surge strength", 150f);
		if (REQUIRE_GIANT_STARS_OR_STRONGER) {
			tooltip.addRow(Alignment.LMID, starColor, "Black holes, neutron stars",
						   Alignment.MID, highlight, "Extreme");
			tooltip.addRow(Alignment.LMID, starColor, "Supergiant stars",
					Alignment.MID, highlight, "High");
			tooltip.addRow(Alignment.LMID, starColor, "Giant stars",
					Alignment.MID, highlight, "Average");
			tooltip.addRow(Alignment.LMID, starColor, "Smaller stars / stellar objects",
					Alignment.MID, highlight, "---");
		} else {
			tooltip.addRow(Alignment.LMID, starColor, "Black holes, neutron stars",
					   Alignment.MID, highlight, "Extreme");
			tooltip.addRow(Alignment.LMID, starColor, "Supergiant stars",
					Alignment.MID, highlight, "Very high");
			tooltip.addRow(Alignment.LMID, starColor, "Giant stars",
					Alignment.MID, highlight, "High");
			tooltip.addRow(Alignment.LMID, starColor, "Sol-like stars",
					Alignment.MID, highlight, "Average");
			tooltip.addRow(Alignment.LMID, starColor, "Dwarf stars",
					Alignment.MID, highlight, "Low");
			tooltip.addRow(Alignment.LMID, starColor, "Gas giants",
					Alignment.MID, highlight, "Very low");
		}
		
		tooltip.addTable("", 0, pad);
		tooltip.addSpacer(5f);
		
		tooltip.addPara("A stronger surge can allow the fleet to rapidly travel up to %s light-years. "
				+ "Attempting to %s "
				+ "during the transit will decelerate the fleet quickly. Fleet sensor range is "
				+ "reduced by %s during the transit.", pad, highlight, 
				"15", "move slowly",
				"" + (int)Math.round((1f - SENSOR_RANGE_MULT) * 100f) + "%"); 
		
	}
	
	@Override
	public boolean addNotUsableReasonBeforeFuelCost(TooltipMakerAPI tooltip, boolean expanded) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return false;
		
		Color bad = Misc.getNegativeHighlightColor();
		
		float pad = 10f;

		if (!fleet.isInHyperspace()) {
			tooltip.addPara("Can only be used in hyperspace.", bad, pad);
			return true;
		//} else if (findGravityWell() == null && !Misc.isInsideSlipstream(getFleet())) {
		} else if (findGravityWell() == null) {
			//tooltip.addPara("Must be near a gravity well or inside a slipstream.", bad, pad);
			if (REQUIRE_GIANT_STARS_OR_STRONGER) {
				tooltip.addPara("Must be near a powerful gravity well.", bad, pad);
			} else {
				tooltip.addPara("Must be near a gravity well.", bad, pad);
			}
			return true;
		}
		
		return false;
	}

	
	public JumpPointAPI findGravityWell() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return null;

		JumpPointAPI closest = null;
		float minDist = Float.MAX_VALUE;
		for (SectorEntityToken curr : fleet.getContainingLocation().getJumpPoints()) {
			JumpPointAPI jp = (JumpPointAPI) curr;
			if (!jp.isStarAnchor() && !jp.isGasGiantAnchor()) continue;
			if (jp.getDestinationVisualEntity() == null) continue;
			
			if (REQUIRE_GIANT_STARS_OR_STRONGER) {
				float str = getStrengthForGravityWell(jp);
				float min = SLIPSURGE_STRENGTH.get(StarTypes.YELLOW);
				if (str <= min) continue;
			}
			
			float dist = Misc.getDistance(fleet, jp) - jp.getRadius();
			if (dist > jp.getRadius() + 150f) continue;
			if (dist < minDist) {
				closest = jp;
				minDist = dist;
			}
		}
		return closest;
	}
	
	public float getStrengthForGravityWell(JumpPointAPI jp) {
		return getStrengthForStellarObject(jp.getDestinationVisualEntity());
	}
	
	public float getStrengthForStellarObject(SectorEntityToken object) {
		if (!(object instanceof PlanetAPI)) return 0f;
		PlanetAPI star = (PlanetAPI) object;
		PlanetSpecAPI spec = star.getSpec();
		Float val = SLIPSURGE_STRENGTH.get(spec.getPlanetType());
		if (val != null) return val;
		
		if (spec.isGasGiant()) return 0f;
		
		// probably a mod-added star of some sort
		// time to make some guesses based on the "poorly understood factors"
		
		String key = StarTypes.YELLOW;
		
		String name = spec.getName().toLowerCase();
		if (name.contains("neutron") || spec.isPulsar()) {
			key = StarTypes.NEUTRON_STAR;
		} else if (name.contains("dwarf")) {
			key = StarTypes.WHITE_DWARF;
		} else if (name.contains("giant")) {
			key = StarTypes.BLUE_GIANT;
		} else if (name.contains("supergiant")) {
			key = StarTypes.BLUE_SUPERGIANT;
		} else  if (name.contains(" hole")) {
			key = StarTypes.BLACK_HOLE;
		} else if (name.contains("brown")) {
			key = StarTypes.BROWN_DWARF;
		}
		
		val = SLIPSURGE_STRENGTH.get(key);
		if (val != null) return val;
		return 0.4f;
	}
	
	
}





