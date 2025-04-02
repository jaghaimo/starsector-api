package com.fs.starfarer.api.impl.campaign.abilities;

import java.util.Random;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType;
import com.fs.starfarer.api.impl.campaign.fleets.PirateFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RuinsFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class DistressCallAbility extends BaseDurationAbility implements RouteFleetSpawner {

	public static final float NEARBY_USE_TIMEOUT_DAYS = 20f;
	public static final float NEARBY_USE_RADIUS_LY = 5f;
	
	public static final float DAYS_TO_TRACK_USAGE = 365f;
	
	public static enum DistressCallOutcome {
		NOTHING,
		HELP,
		PIRATES,
	}
	
	public static class DistressResponseData {
		public DistressCallOutcome outcome;
		public JumpPointAPI inner;
		public JumpPointAPI outer;
	}
	
	
	public static class AbilityUseData {
		public long timestamp;
		public Vector2f location;
		public AbilityUseData(long timestamp, Vector2f location) {
			this.timestamp = timestamp;
			this.location = location;
		}
		
	}
	
	protected boolean performed = false;
	protected int numTimesUsed = 0;
	protected long lastUsed = 0;
	
	protected TimeoutTracker<AbilityUseData> uses = new TimeoutTracker<AbilityUseData>();
	
	protected Object readResolve() {
		super.readResolve();
		if (uses == null) {
			uses = new TimeoutTracker<AbilityUseData>();
		}
		return this;
	}
	
	@Override
	protected void activateImpl() {
		if (entity.isInCurrentLocation()) {
			VisibilityLevel level = entity.getVisibilityLevelToPlayerFleet();
			if (level != VisibilityLevel.NONE) {
				Global.getSector().addPing(entity, Pings.DISTRESS_CALL);
			}
			
			performed = false;
		}
		
	}

	@Override
	protected void applyEffect(float amount, float level) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		if (!performed) {
			if (wasUsedNearby(NEARBY_USE_TIMEOUT_DAYS, fleet.getLocationInHyperspace(), NEARBY_USE_RADIUS_LY)) {
				performed = true;
				return;
			}
			
			WeightedRandomPicker<DistressCallOutcome> picker = new WeightedRandomPicker<DistressCallOutcome>();
			picker.add(DistressCallOutcome.HELP, 10f);
			if (numTimesUsed > 2) {
				float uses = getNumUsesInLastPeriod();
				float pirates = 10f + uses * 2f;
				picker.add(DistressCallOutcome.PIRATES, pirates);
				
				float nothing = 10f + uses * 2f;
				picker.add(DistressCallOutcome.NOTHING, nothing);
			}
			
			DistressCallOutcome outcome = picker.pick();
			//outcome = DistressCallOutcome.HELP;
			
			if (outcome != DistressCallOutcome.NOTHING) {
				float delay = 10f + 10f * (float) Math.random();
				if (numTimesUsed == 0) {
					delay = 1f + 2f * (float) Math.random();
				}
				//delay = 0f;
				addResponseScript(delay, outcome);
			}
			
			numTimesUsed++;
			lastUsed = Global.getSector().getClock().getTimestamp();
			performed = true;
			
			AbilityUseData data = new AbilityUseData(lastUsed, fleet.getLocationInHyperspace());
			uses.add(data, DAYS_TO_TRACK_USAGE);
		}
	}
	
	public boolean wasUsedNearby(float withinDays, Vector2f locInHyper, float withinRangeLY) {
		for (AbilityUseData data : uses.getItems()) {
			float daysSinceUse = Global.getSector().getClock().getElapsedDaysSince(data.timestamp);
			if (daysSinceUse <= withinDays) {
				float range = Misc.getDistanceLY(locInHyper, data.location);
				if (range <= withinRangeLY) return true;
			}
		}
		return false;
	}
	
	
	@Override
	public void advance(float amount) {
		super.advance(amount);

		float days = Global.getSector().getClock().convertToDays(amount);
		uses.advance(days);
	}

	public TimeoutTracker<AbilityUseData> getUses() {
		return uses;
	}
	
	public int getNumUsesInLastPeriod() {
		return uses.getItems().size();
	}

	protected void addResponseScript(float delayDays, DistressCallOutcome outcome) {
		final CampaignFleetAPI player = getFleet();
		if (player == null) return;
		if (!(player.getContainingLocation() instanceof StarSystemAPI)) return;
		
		final StarSystemAPI system = (StarSystemAPI) player.getContainingLocation();
		
		final JumpPointAPI inner = Misc.getDistressJumpPoint(system);
		if (inner == null) return;
		
		JumpPointAPI outerTemp = null;
		if (inner.getDestinations().size() >= 1) {
			SectorEntityToken test = inner.getDestinations().get(0).getDestination();
			if (test instanceof JumpPointAPI) {
				outerTemp = (JumpPointAPI) test;
			}
		}
		final JumpPointAPI outer = outerTemp;
		if (outer == null) return;
		
		
		if (outcome == DistressCallOutcome.HELP) {
			addHelpScript(delayDays, system, inner, outer);
		} else if (outcome == DistressCallOutcome.PIRATES) {
			addPiratesScript(delayDays, system, inner, outer);
		}
		
	}
	
	protected void addPiratesScript(float delayDays,
								 final StarSystemAPI system, 
								 final JumpPointAPI inner, 
								 final JumpPointAPI outer) {
		Global.getSector().addScript(new DelayedActionScript(delayDays) {
			@Override
			public void doAction() {
				CampaignFleetAPI player = Global.getSector().getPlayerFleet();
				if (player == null) return;
				
				int numPirates = new Random().nextInt(3) + 1;
				for (int i = 0; i < numPirates; i++) {
					DistressResponseData data = new DistressResponseData();
					data.outcome = DistressCallOutcome.PIRATES;
					data.inner = inner;
					data.outer = outer;
					
					OptionalFleetData extra = new OptionalFleetData();
					extra.factionId = Factions.PIRATES;
					
					RouteData route = RouteManager.getInstance().addRoute("dca_" + getId(), null, 
													Misc.genRandomSeed(), extra, DistressCallAbility.this, data);
					float waitDays = 15f + (float) Math.random() * 10f;
					route.addSegment(new RouteSegment(waitDays, inner));
				}
			}
		});
	}
	
	protected void addHelpScript(float delayDays,
									final StarSystemAPI system, 
									final JumpPointAPI inner, 
									final JumpPointAPI outer) {
		Global.getSector().addScript(new DelayedActionScript(delayDays) {
			@Override
			public void doAction() {
				DistressResponseData data = new DistressResponseData();
				data.outcome = DistressCallOutcome.HELP;
				data.inner = inner;
				data.outer = outer;
				
				RouteData route = RouteManager.getInstance().addRoute("dca_" + getId(), null, 
												Misc.genRandomSeed(), null, DistressCallAbility.this, data);
				float waitDays = 15f + (float) Math.random() * 10f;
				route.addSegment(new RouteSegment(waitDays, inner));
			}
		});
	}
	
	
	
	
	public boolean isUsable() {
		if (!super.isUsable()) return false;
		if (getFleet() == null) return false;
		
		CampaignFleetAPI fleet = getFleet();
		if (fleet.isInHyperspace() || fleet.isInHyperspaceTransition()) return false;
		
		if (fleet.getContainingLocation() != null && fleet.getContainingLocation().hasTag(Tags.SYSTEM_ABYSSAL)) {
			return false;
		}
		
		return true;
	}
	

	@Override
	protected void deactivateImpl() {
		cleanupImpl();
	}
	
	@Override
	protected void cleanupImpl() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
	}

	
	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		if (!Global.CODEX_TOOLTIP_MODE) {
			LabelAPI title = tooltip.addTitle(spec.getName());
		} else {
			tooltip.addSpacer(-10f);
		}

		float pad = 10f;
		
		tooltip.addPara("May be used by a stranded fleet to punch a distress signal through to hyperspace, " +
						"asking nearby fleets to bring aid in the form of fuel and supplies. " +
						"Help may take many days to arrive, if it arrives at all, and taking advantage " +
						"of it will result in a progressively higher reduction in standing with the responders.", pad);
		
		tooltip.addPara("By long-standing convention, the fleet in distress is expected to meet any responders at the " +
						"innermost jump-point inside a star system.", pad, highlight,
						"innermost jump-point");
		
		tooltip.addPara("The signal is non-directional and carries no data, and is therefore not useful for " +
						"calling for help in a tactical situation.", pad);
		
		if (!Global.CODEX_TOOLTIP_MODE) {
			if (fleet.isInHyperspace()) {
				tooltip.addPara("Can not be used in hyperspace.", bad, pad);
			}
			if (fleet.getContainingLocation() != null && fleet.getContainingLocation().hasTag(Tags.SYSTEM_ABYSSAL)) {
				tooltip.addPara("Can not be used in star systems deep within abyssal hyperspace.", bad, pad);
			}
		}
		
		addIncompatibleToTooltip(tooltip, expanded);
		
	}

	public boolean hasTooltip() {
		return true;
	}
	

	public CampaignFleetAPI spawnFleet(RouteData route) {
		
		DistressResponseData data = (DistressResponseData) route.getCustom();
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player == null) return null;
		
		if (data.outcome == DistressCallOutcome.HELP) {
			WeightedRandomPicker<String> factions = SalvageSpecialAssigner.getNearbyFactions(
											null, data.inner,
											10f, 10f, 0f);
			
			String faction = factions.pick();
//			faction = Factions.HEGEMONY;
//			faction = Factions.PIRATES;
			if (faction == null) return null;
			
			//int fuelNeeded = DistressResponse.getNeededFuel(player);
			
			CampaignFleetAPI fleet = null;
			if (Factions.INDEPENDENT.equals(faction)) {
				WeightedRandomPicker<String> typePicker = new WeightedRandomPicker<String>();
//				typePicker.add(FleetTypes.SCAVENGER_SMALL, 5f); // too little fuel to bother
				
				//if (fuelNeeded < 750) {
				typePicker.add(FleetTypes.SCAVENGER_MEDIUM, 10f); // 500+ fuel
				typePicker.add(FleetTypes.SCAVENGER_LARGE, 5f); // 1000+ fuel
				String type = typePicker.pick();
	
				fleet = RuinsFleetRouteManager.createScavenger(
												type, data.inner.getLocationInHyperspace(),
												route, null, false, null);
			} else {
				WeightedRandomPicker<PatrolType> picker = new WeightedRandomPicker<PatrolType>();
//				picker.add(PatrolType.FAST, 5f); 
//				picker.add(PatrolType.COMBAT, 10f); 
				picker.add(PatrolType.HEAVY, 5f);
				PatrolType type = picker.pick();
				
				fleet = MilitaryBase.createPatrol(type, 15f, faction, route, null, data.inner.getLocationInHyperspace(), route.getRandom());
				//fleet = PatrolFleetManager.createPatrolFleet(type, null, faction, data.inner.getLocationInHyperspace(), 0f);
			}
			if (fleet == null) return null;
	
			if (Misc.getSourceMarket(fleet) == null) return null;
	
	
			if (numTimesUsed == 1) {
				FullName name = new FullName("Mel", "Greenish", fleet.getCommander().getGender());
				fleet.getCommander().setName(name);
				fleet.getFlagship().setShipName("IS In All Circumstances");
			}
	
			Misc.makeImportant(fleet, "distressResponse", 30f);
			fleet.getMemoryWithoutUpdate().set("$distressResponse", true);
	
			Global.getSector().getHyperspace().addEntity(fleet);
	
			if (!player.isInHyperspace() && 
					(Global.getSector().getHyperspace().getDaysSinceLastPlayerVisit() > 5 ||
							player.getCargo().getFuel() <= 0)) {
	
				Vector2f loc = data.outer.getLocation();
				fleet.setLocation(loc.x, loc.y + fleet.getRadius() + 100f);
			} else {
				float dir = (float) Math.random() * 360f;
				if (player.isInHyperspace()) {
					dir = Misc.getAngleInDegrees(player.getLocation(), data.inner.getLocationInHyperspace());
					dir += (float) Math.random() * 120f - 60f;
				}
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(dir);
				loc.scale(3000f + 1000f * (float) Math.random());
				Vector2f.add(data.inner.getLocationInHyperspace(), loc, loc);
				fleet.setLocation(loc.x, loc.y + fleet.getRadius() + 100f);
			}
	
			fleet.addScript(new DistressCallResponseAssignmentAI(fleet, data.inner.getStarSystem(), 
																 data.inner, data.outer));
			
			return fleet;
		} else if (data.outcome == DistressCallOutcome.PIRATES) {
			int points = 5 + new Random().nextInt(15);
			
			CampaignFleetAPI fleet = PirateFleetManager.createPirateFleet(points, route, data.inner.getLocationInHyperspace());
			if (fleet == null) return null;
			if (Misc.getSourceMarket(fleet) == null) return null;
			
			Global.getSector().getHyperspace().addEntity(fleet);
		
			if (!player.isInHyperspace() && 
					(Global.getSector().getHyperspace().getDaysSinceLastPlayerVisit() > 5 ||
							player.getCargo().getFuel() <= 0)) {
				
				Vector2f loc = data.outer.getLocation();
				fleet.setLocation(loc.x, loc.y + fleet.getRadius() + 100f);
			} else {
				float dir = (float) Math.random() * 360f;
				if (player.isInHyperspace()) {
					dir = Misc.getAngleInDegrees(player.getLocation(), data.inner.getLocationInHyperspace());
					dir += (float) Math.random() * 120f - 60f;
				}
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(dir);
				loc.scale(3000f + 1000f * (float) Math.random());
				Vector2f.add(data.inner.getLocationInHyperspace(), loc, loc);
				fleet.setLocation(loc.x, loc.y + fleet.getRadius() + 100f);
			}
		
			fleet.addScript(new DistressCallResponsePirateAssignmentAI(fleet, data.inner.getStarSystem(), data.inner, data.outer));
			
			return fleet;
		}
		
		
		return null;
	}

	
	public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
		// don't respawn since the assignment AI is not set up to handle it well, it'll
		// just basically start over
		route.expire(); 
	}

	public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
		return false;
	}

	public boolean shouldRepeat(RouteData route) {
		return false;
	}

}





