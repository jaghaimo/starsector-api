package com.fs.starfarer.api.impl.campaign.missions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.FleetAIFlags;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.GateTransitListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepRewards;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch.RequiredSystemTags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerAction;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerActionContext;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class DelayedFleetEncounter extends HubMissionWithTriggers implements GateTransitListener {
	
	public static String TRIGGER_REP_LOSS_MINOR = "DFEFWTRepLossMinor";
	public static String TRIGGER_REP_LOSS_MEDIUM = "DFEFWTRepLossMedium";
	public static String TRIGGER_REP_LOSS_HIGH = "DFEFWTRepLossHigh";
	
	public static enum EncounterType {
		OUTSIDE_SYSTEM,
		IN_HYPER_EN_ROUTE,
		JUMP_IN_NEAR_PLAYER,
		FROM_SOMEWHERE_IN_SYSTEM,
	}
	
	public static enum EncounterLocation {
		ANYWHERE,
		POPULATED_SYSTEM,
		NEAR_CORE,
		MIDRANGE,
		FRINGE,
	}
	
	public static float RADIUS_FROM_CORE = 30000f; // may send fleet when within this radius from core
	public static float BASE_DAYS_IN_SYSTEM_BEFORE_AMBUSH_IN_HYPER = 5f;
	public static float BASE_DAYS_IN_SYSTEM_BEFORE_IN_SYSTEM_ATTACK = 10f;
	public static float BASE_TIMEOUT = 10f;
	
	public static float BASE_DELAY_VERY_SHORT = 365f * 0.25f; 
	public static float BASE_DELAY_SHORT = 365f * 0.67f; 
	public static float BASE_DELAY_MEDIUM = 365f * 2f; 
	public static float BASE_DELAY_LONG = 365f * 5f;
	
	public static float BASE_ONLY_CHECK_IN_SYSTEM_DAYS = 15f; 
	
	public enum Stage {
		WAITING,
		SPAWN_FLEET,
		ENDED,
	}
	
	public static float getRandomValue(float base) {
		return StarSystemGenerator.getNormalRandom(Misc.random, base * 0.75f, base * 1.25f);
	}
	
	public static String TIMEOUT_KEY = "$core_dfe_timeout";
	public static boolean isInTimeout() {
		return Global.getSector().getMemoryWithoutUpdate().getBoolean(TIMEOUT_KEY);
	}
	public static void setTimeout() {
		Global.getSector().getMemoryWithoutUpdate().set(TIMEOUT_KEY, true, getRandomValue(BASE_TIMEOUT));
	}
	
	public class CanSpawnFleetConditionChecker implements ConditionChecker {
		protected StarSystemAPI lastSystemPlayerWasIn = null;
		protected float daysInSystem = 0f;
		protected boolean conditionsMet = false;
		protected EncounterType typePicked = null;
		protected Vector2f location;
		protected SectorEntityToken foundEntity;
		
		protected float daysBeforeInHyper = getRandomValue(BASE_DAYS_IN_SYSTEM_BEFORE_AMBUSH_IN_HYPER);
		protected float daysBeforeInSystem = getRandomValue(BASE_DAYS_IN_SYSTEM_BEFORE_IN_SYSTEM_ATTACK);
		
		
		public boolean conditionsMet() {
			doCheck();
			return conditionsMet;
		}
		
		public void advance(float amount) {
			if (conditionsMet) return;
			
			float days = Global.getSector().getClock().convertToDays(amount);
			CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
			
			
			StarSystemAPI curr = playerFleet.getStarSystem();
			if (curr != null) {
				if (curr != lastSystemPlayerWasIn) {
					daysInSystem = 0f;
				}
				lastSystemPlayerWasIn = curr;
				daysInSystem += days;
			}
		}
		
		public void doCheck() {
			if (isInTimeout()) return;
			if (conditionsMet) return;
			
			CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
			if (playerFleet.getFleetPoints() > estimatedFleetPoints * playerFleetSizeAbortMult) {
				return;
			}
			
			if (madeGateTransit && initialTransitFrom != null && 
					Misc.getDistanceLY(initialTransitFrom.getLocation(), 
									   playerFleet.getLocationInHyperspace()) > 2f) {
				return;
			}
			
			boolean onlyCheckInSystem = true;
			StageData stage = getData(currentStage);
			if (stage != null && stage.elapsed > onlyCheckForSpawnInSystemDays) {
				onlyCheckInSystem = false;
			}
			
			
			if (isPlayerInRightRangeBand(lastSystemPlayerWasIn)) {
				if (allowedTypes.contains(EncounterType.IN_HYPER_EN_ROUTE) && !onlyCheckInSystem) {
					if (playerFleet.isInHyperspace()) {
						float maxSpeed = Misc.getSpeedForBurnLevel(playerFleet.getFleetData().getBurnLevel());
						//float threshold = Misc.getSpeedForBurnLevel(15);
						float currSpeed = playerFleet.getVelocity().length();
						if (currSpeed >= maxSpeed * 0.9f) {
							//Misc.findNearestJumpPointThatCouldBeExitedFrom(playerFleet);
							float dist = getRandomValue(2500f);
							float dir = Misc.getAngleInDegrees(playerFleet.getVelocity());
							dir += 75f - 150f * genRandom.nextFloat();
							location = Misc.getUnitVectorAtDegreeAngle(dir);
							location.scale(dist);
							Vector2f.add(location, playerFleet.getLocation(), location);
							conditionsMet = true;
							typePicked = EncounterType.IN_HYPER_EN_ROUTE;
							//getPreviousCreateFleetAction().params.locInHyper = playerFleet.getLocationInHyperspace();
							return;
						}
					}
				}
				if (allowedTypes.contains(EncounterType.OUTSIDE_SYSTEM)) {
					if (playerFleet.isInHyperspace() && daysInSystem > daysBeforeInHyper && lastSystemPlayerWasIn != null) {
						float dist = Misc.getDistance(lastSystemPlayerWasIn.getLocation(), playerFleet.getLocationInHyperspace());
						if (dist < 3000f) {
							conditionsMet = true;
							typePicked = EncounterType.OUTSIDE_SYSTEM;
							return;
						}
					}
				}
				if (allowedTypes.contains(EncounterType.FROM_SOMEWHERE_IN_SYSTEM)) {
					if (playerFleet.getStarSystem() == lastSystemPlayerWasIn && daysInSystem > daysBeforeInSystem && 
							lastSystemPlayerWasIn != null) {
						conditionsMet = true;
						typePicked = EncounterType.FROM_SOMEWHERE_IN_SYSTEM;
						return;
					}
				}
				if (allowedTypes.contains(EncounterType.JUMP_IN_NEAR_PLAYER)) {
					if (playerFleet.getStarSystem() == lastSystemPlayerWasIn && daysInSystem > daysBeforeInSystem && 
									lastSystemPlayerWasIn != null) {
						// spawn from:
						// a nearby jump-point
						// a gas giant gravity well
						// a planet gravity well (transverse jump)
						// a star gravity well
						SectorEntityToken entity = Misc.findNearestJumpPointTo(playerFleet);
						if (entity != null) {
							float dist = Misc.getDistance(playerFleet, entity);
							if (dist < 3000f) {
								conditionsMet = true;
							}
						}
						if (!conditionsMet) {
							entity = Misc.findNearestPlanetTo(playerFleet, true, false);
							if (entity != null) {
								float dist = Misc.getDistance(playerFleet, entity);
								if (dist < 3000f) {
									conditionsMet = true;
								}
							}
						}
						// only jump in near gas giants; don't fall back to other planets/stars
						// it feels too weird/forced if the fleet can just jump in anywhere
//						if (!conditionsMet) {
//							entity = Misc.findNearestPlanetTo(playerFleet, false, false);
//							if (entity != null) {
//								float dist = Misc.getDistance(playerFleet, entity);
//								if (dist < 3000f) {
//									conditionsMet = true;
//								}
//							}
//						}
//						if (!conditionsMet) {
//							entity = Misc.findNearestPlanetTo(playerFleet, false, true);
//							if (entity != null) {
//								float dist = Misc.getDistance(playerFleet, entity);
//								if (dist < 3000f) {
//									conditionsMet = true;
//								}
//							}
//						}
						
						if (conditionsMet) {
							foundEntity = entity;
							typePicked = EncounterType.JUMP_IN_NEAR_PLAYER;
							return;
						}
					}
				}
			}
		}
		
		protected boolean isPlayerInRightRangeBand(LocationAPI system) {
			//if (allowedLocations.contains(EncounterLocation.ANYWHERE)) return true;
			CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
			
			if (system instanceof StarSystemAPI && system == playerFleet.getContainingLocation()) {
				if (requiredTags != null) {
					for (RequiredSystemTags req : requiredTags) {
						if (!req.systemMatchesRequirement((StarSystemAPI) system)) {
							return false;
						}
					}
				}
				
				List<MarketAPI> markets = Misc.getMarketsInLocation(system);
				if (requireLargestMarketNotHostileToFaction != null) {
					MarketAPI largest = null;
					MarketAPI largestHostile = null;
					int maxSize = 0;
					int maxHostileSize = 0;
					for (MarketAPI market : markets) {
						if (market.getSize() > maxSize) {
							largest = market;
							maxSize = market.getSize();
						}
						if (market.getFaction().isHostileTo(requireLargestMarketNotHostileToFaction)) {
							if (market.getSize() > maxHostileSize) {
								largestHostile = market;
								maxHostileSize = market.getSize();
							}
						}
					}
					if (largestHostile != null && maxHostileSize > maxSize) {
						return false;
					}
				}
				if (requiredFactionPresence != null) {
					boolean found = false;
					for (MarketAPI market : markets) {
						if (requiredFactionPresence.contains(market.getFactionId())) {
							found = true;
							break;
						}
					}
					if (!found) {
						return false;
					}
				}
			}
			
			Vector2f coreCenter = new Vector2f();
			
			float fringeRange = 46000;
			
			float nearMarketRange = 5000f;
			boolean nearCoreMarket = false;
			boolean nearAnyMarket = false;
			MarketAPI nearest = null;
			float minDist = Float.MAX_VALUE;
			
			float count = 0f;
			for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
				if (market.isHidden()) continue;
				if (market.getContainingLocation().hasTag(Tags.THEME_CORE_POPULATED)) {
					Vector2f.add(coreCenter, market.getLocationInHyperspace(), coreCenter);
					count++;
					
					if (!nearCoreMarket) {
						float dist = Misc.getDistance(market.getLocation(), playerFleet.getLocationInHyperspace());
						nearCoreMarket = dist < nearMarketRange;
						if (dist < minDist) {
							nearest = market;
							minDist = dist;
						}
					}
				} else if (!nearAnyMarket) {
					float dist = Misc.getDistance(market.getLocation(), playerFleet.getLocationInHyperspace());
					nearAnyMarket = dist < nearMarketRange;
					if (dist < minDist) {
						nearest = market;
						minDist = dist;
					}
				}
			}
			
			if ((nearCoreMarket || nearAnyMarket) && !allowInsidePopulatedSystems && nearest != null) {
				if (!Global.getSector().getPlayerFleet().isInHyperspace() && 
						system == nearest.getStarSystem()) {
					return false;
				}
			}
				
			
			if (count > 0) {
				coreCenter.scale(1f / count);
			}
			
			if (nearCoreMarket && allowedLocations.contains(EncounterLocation.NEAR_CORE)) {
				return true;
			}
			if (nearAnyMarket && allowedLocations.contains(EncounterLocation.POPULATED_SYSTEM)) {
				return true;
			}
			
			for (EncounterLocation location : allowedLocations) {
				if (location == EncounterLocation.NEAR_CORE) continue;
				if (location == EncounterLocation.POPULATED_SYSTEM) continue;
				
				//location = EncounterLocation.FRINGE;
				
				float distFromCore = Misc.getDistance(coreCenter, playerFleet.getLocationInHyperspace());
				
				if (location == EncounterLocation.MIDRANGE) {
					if (distFromCore > fringeRange || nearCoreMarket) {
						continue;
					}
				}
				
				if (location == EncounterLocation.FRINGE) {
					if (distFromCore < fringeRange || nearCoreMarket) {
						continue;
					}
				}
				
				return true;
			}
			return false;
		}
	}
	
	public class DFEPlaceFleetAction implements TriggerAction {
		public DFEPlaceFleetAction() {
		}
		
		public void doAction(TriggerActionContext context) {
			setTimeout();
			
//			protected EncounterType typePicked = null;
//			protected Vector2f location;
//			protected SectorEntityToken foundEntity;
			CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
			playerFleet.getContainingLocation().addEntity(context.fleet);
			
			if (checker.typePicked == EncounterType.OUTSIDE_SYSTEM) {
				Vector2f loc = Misc.getPointAtRadius(playerFleet.getLocationInHyperspace(), 1000f);
				context.fleet.setLocation(loc.x, loc.y);
			} else if (checker.typePicked == EncounterType.FROM_SOMEWHERE_IN_SYSTEM) {
				WeightedRandomPicker<MarketAPI> from = new WeightedRandomPicker<MarketAPI>(genRandom);
				for (MarketAPI curr : Global.getSector().getEconomy().getMarkets(playerFleet.getContainingLocation())) {
					float w = 0f;
					if (curr.getFaction() == context.fleet.getFaction()) {
						w = curr.getSize() * 10000f;
					} else if (!curr.getFaction().isHostileTo(context.fleet.getFaction())) {
						w = curr.getSize();
					}
					if (w > 0f) {
						from.add(curr, w);
					}
				}
				MarketAPI market = from.pick();
				if (market != null) {
					float dir = Misc.getAngleInDegrees(playerFleet.getLocation(), market.getPrimaryEntity().getLocation());
					Vector2f loc = HubMissionWithTriggers.pickLocationWithinArc(genRandom, playerFleet,
								dir, 30f, 3000f, 3000f, 3000f);
					context.fleet.setLocation(loc.x, loc.y);
				} else {
					Vector2f loc = Misc.getPointAtRadius(playerFleet.getLocation(), 3000f);
					context.fleet.setLocation(loc.x, loc.y);
				}
			} else if (checker.typePicked == EncounterType.JUMP_IN_NEAR_PLAYER) {
				JumpDestination dest = new JumpDestination(checker.foundEntity, null);
				if (checker.foundEntity instanceof JumpPointAPI) {
					JumpPointAPI jp = (JumpPointAPI) checker.foundEntity;
					jp.open();
					context.fleet.setLocation(jp.getLocation().x, jp.getLocation().y);
				} else if (checker.foundEntity instanceof PlanetAPI) {
					dest.setMinDistFromToken(checker.foundEntity.getRadius() + 50f);
					dest.setMaxDistFromToken(checker.foundEntity.getRadius() + 400f);
				}
				context.fleet.updateFleetView(); // so that ship views exist and can do the jump-in warping animation
				context.fleet.getContainingLocation().removeEntity(context.fleet);
				Global.getSector().getHyperspace().addEntity(context.fleet);
				context.fleet.setLocation(1000000000, 0);
				Global.getSector().doHyperspaceTransition(context.fleet, null, dest);
			} else if (checker.typePicked == EncounterType.IN_HYPER_EN_ROUTE) {
				context.fleet.setLocation(checker.location.x, checker.location.y);
			}
			
			
			// this is only relevant if an intercept isn't ordered; the triggerIntercept method sets this
			// as well.
			float radius = 2000f * (0.5f + 0.5f * genRandom.nextFloat());
			Vector2f approximatePlayerLoc = Misc.getPointAtRadius(playerFleet.getLocation(), radius);
			context.fleet.getMemoryWithoutUpdate().set(FleetAIFlags.PLACE_TO_LOOK_FOR_TARGET, approximatePlayerLoc, 2f);
		}
	}

	
	protected float minDelay;
	protected float maxDelay;
	protected float onlyCheckForSpawnInSystemDays;
	protected String globalEndFlag;
	
	protected List<EncounterType> allowedTypes = new ArrayList<EncounterType>();
	protected List<EncounterLocation> allowedLocations = new ArrayList<EncounterLocation>();
	protected boolean allowInsidePopulatedSystems = true;
	protected String requireLargestMarketNotHostileToFaction = null;
	protected List<String> requiredFactionPresence = null;
	protected List<RequiredSystemTags> requiredTags = null;
	
	protected boolean canBeAvoidedByGateTransit = true;
	protected boolean madeGateTransit = false;
	protected LocationAPI initialTransitFrom = null;
	
	protected CanSpawnFleetConditionChecker checker;
	
	public DelayedFleetEncounter(Random random, String missionId) {
		if (random == null) random = new Random(Misc.genRandomSeed());
		setGenRandom(random);
		setNoRepChanges();
		globalEndFlag = "$" + "dfe"+ "_" + missionId + "_" + Misc.genUID();
		setMissionId(missionId);
		
		setTypes(EncounterType.OUTSIDE_SYSTEM, EncounterType.JUMP_IN_NEAR_PLAYER, EncounterType.IN_HYPER_EN_ROUTE);
		// don't, since in-system attacks are a jump-in near the player, so it's probably fine anyway
		// and possibly interesting if not fine
		//requireDFESystemTags(ReqMode.NOT_ANY, Tags.THEME_UNSAFE);
		
		onlyCheckForSpawnInSystemDays = BASE_ONLY_CHECK_IN_SYSTEM_DAYS * (0.5f + genRandom.nextFloat());
		Global.getSector().getListenerManager().addListener(this);
	}
	
	public void setCanNotBeAvoidedByGateTransit() {
		canBeAvoidedByGateTransit = false;
	}
	
	public void reportFleetTransitingGate(CampaignFleetAPI fleet, SectorEntityToken gateFrom, SectorEntityToken gateTo) {
		if (!fleet.isPlayerFleet() || gateFrom == null) return;
		
		if (getCurrentStage() == Stage.WAITING && 
				getElapsedInCurrentStage() < waitDays * 0.9f) {
			return;
		}
		
		float dist = Misc.getDistanceLY(gateFrom, gateTo);
		if (dist > 2f) {
			madeGateTransit = true;
		}
		if (initialTransitFrom == null) {
			initialTransitFrom = gateFrom.getContainingLocation();
		}
	}
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		Global.getSector().getListenerManager().removeListener(this);
	}
	
	public void setAllowInsidePopulatedSystems(boolean allowInsidePopulatedSystems) {
		this.allowInsidePopulatedSystems = allowInsidePopulatedSystems;
	}
	public void setRequireLargestMarketNotHostileToFaction(String requireLargestMarketNotHostileToFaction) {
		this.requireLargestMarketNotHostileToFaction = requireLargestMarketNotHostileToFaction;
	}
	public void setRequireFactionPresence(String ... factions) {
		if (factions == null || factions.length <= 0) {
			requiredFactionPresence = null;
		} else {
			requiredFactionPresence = new ArrayList<String>();
			requiredFactionPresence.addAll(Arrays.asList(factions));
		}
	}
	
	public void clearDFESystemTagRequirements() {
		requiredTags = null;
	}
	public void requireDFESystemTags(ReqMode mode, String ... tags) {
		if (requiredTags == null) {
			requiredTags = new ArrayList<HubMissionWithSearch.RequiredSystemTags>();
		}
		RequiredSystemTags req = new RequiredSystemTags(mode, tags);
		requiredTags.add(req);
	}
	
	public void setEncounterInHyper() {
		setTypes(EncounterType.OUTSIDE_SYSTEM, EncounterType.IN_HYPER_EN_ROUTE);
	}
	public void setEncounterOutsideSystem() {
		setTypes(EncounterType.OUTSIDE_SYSTEM);
	}
	public void setEncounterInSystemFromJumpPoint() {
		setTypes(EncounterType.JUMP_IN_NEAR_PLAYER);
	}
	public void setEncounterFromSomewhereInSystem() {
		setTypes(EncounterType.FROM_SOMEWHERE_IN_SYSTEM);
	}
	public void setEncounterInHyperEnRoute() {
		setTypes(EncounterType.IN_HYPER_EN_ROUTE);
	}
	
	public void setTypes(EncounterType ... types) {
		allowedTypes.clear();
		allowedTypes.addAll(Arrays.asList(types));
	}
	public void setLocations(boolean allowInsidePopulatedSystems, String requireLargestMarketNotHostileToFaction,
								EncounterLocation ... locations) {
		allowedLocations.clear();
		allowedLocations.addAll(Arrays.asList(locations));
		setAllowInsidePopulatedSystems(allowInsidePopulatedSystems);
		setRequireLargestMarketNotHostileToFaction(requireLargestMarketNotHostileToFaction);
	}

	public void setDelay(float minDays, float maxDays) {
		this.minDelay = minDays;
		this.maxDelay = maxDays;
	}
	
	public void setDelay(float base) {
		this.minDelay = base * 0.5f;
		this.maxDelay = base * 1.5f;
	}
	
	public void setLocationAnyPopulated(boolean allowInsidePopulatedSystems, String requireLargestMarketNotHostileToFaction) {
		setLocations(allowInsidePopulatedSystems, requireLargestMarketNotHostileToFaction,
				EncounterLocation.POPULATED_SYSTEM);
	}
	
	public void setLocationCoreOnly(boolean allowInsidePopulatedSystems, String requireLargestMarketNotHostileToFaction) {
		setLocations(allowInsidePopulatedSystems, requireLargestMarketNotHostileToFaction,
				EncounterLocation.NEAR_CORE);
	}
	
	public void setLocationOuterSector(boolean allowInsidePopulatedSystems, String requireLargestMarketNotHostileToFaction) {
		setLocations(allowInsidePopulatedSystems, requireLargestMarketNotHostileToFaction,
				EncounterLocation.FRINGE, EncounterLocation.MIDRANGE);
	}
	
	public void setLocationAnywhere(boolean allowInsidePopulatedSystems, String requireLargestMarketNotHostileToFaction) {
		setLocations(allowInsidePopulatedSystems, requireLargestMarketNotHostileToFaction,
				EncounterLocation.ANYWHERE);
	}
	
	public void setLocationFringeOnly(boolean allowInsidePopulatedSystems, String requireLargestMarketNotHostileToFaction) {
		setLocations(allowInsidePopulatedSystems, requireLargestMarketNotHostileToFaction,
				EncounterLocation.FRINGE);
	}
	
	public void setLocationInnerSector(boolean allowInsidePopulatedSystems, String requireLargestMarketNotHostileToFaction) {
		setLocations(allowInsidePopulatedSystems, requireLargestMarketNotHostileToFaction,
				EncounterLocation.NEAR_CORE, EncounterLocation.MIDRANGE);
	}
	
	
	public void setDelayNone() {
		setDelay(0f);
	}
	
	public void setDelayVeryShort() {
		setDelay(BASE_DELAY_VERY_SHORT);
	}
	
	public void setDelayShort() {
		setDelay(BASE_DELAY_SHORT);
	}
	
	public void setDelayMedium() {
		setDelay(BASE_DELAY_MEDIUM);
	}
	
	public void setDelayLong() {
		setDelay(BASE_DELAY_LONG);
	}
	
	public void triggerSetStandardAggroInterceptFlags() {
		triggerMakeHostileAndAggressive();
		triggerFleetAllowLongPursuit();
		triggerSetFleetAlwaysPursue();
		triggerOrderFleetInterceptPlayer();
		triggerOrderFleetMaybeEBurn();
	}
	
	
	@Override
	protected void advanceImpl(float amount) {
		super.advanceImpl(amount);
		
		//System.out.println("Wait: " + waitDays);
		//System.out.println("Elapsed: " + getElapsedInCurrentStage());

		if (getCurrentStage() == Stage.SPAWN_FLEET && checker != null) {
			checker.advance(amount);
		}
		
	}

	protected float waitDays;
	public void beginCreate() {
		checker = new CanSpawnFleetConditionChecker();
		
		if (minDelay <= 0 && maxDelay <= 0) {
			// this check does NOT apply to in hyper in route
			// so, if "immediate" spawn, assuming all encounter types are allowed:
			// - wait a bit before spawning when player exits system
			// - wait a bit longer before it jumps into the system
			// - and wait a little bit (i.e. only check in-system spawn) before spawning in-hyper-en-route 
			checker.daysBeforeInHyper = (0.5f + (0.5f + genRandom.nextFloat())) * 2f;
			checker.daysBeforeInSystem = (0.5f + (0.5f + genRandom.nextFloat())) * 3f;
			onlyCheckForSpawnInSystemDays = 0.5f + (0.5f + genRandom.nextFloat());
		}
		
		waitDays = minDelay + (maxDelay - minDelay) * genRandom.nextFloat();
		connectWithDaysElapsed(Stage.WAITING, Stage.SPAWN_FLEET, waitDays);
		setStartingStage(Stage.WAITING);
		setSuccessStage(Stage.ENDED);
		setStageOnGlobalFlag(Stage.ENDED, globalEndFlag);
		
		setStageOnCustomCondition(Stage.ENDED, new ConditionChecker() {
			public boolean conditionsMet() {
				CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
				// abort if player is too strong
				if (playerFleet.getFleetPoints() > estimatedFleetPoints * playerFleetSizeAbortMult) {
					if ((getCurrentStage() == Stage.WAITING && 
							getElapsedInCurrentStage() > waitDays * 0.9f) || 
							getCurrentStage() == Stage.SPAWN_FLEET) {
						return true;
					}
					return false;
				}
				
				// abort if player made a gate transit shortly before the encounter would trigger
				// the "shortly before" part is handled in reportFleetTransitingGate()
				// and also check that they didn't transit back to their original location
				if (madeGateTransit && initialTransitFrom != null && 
						Misc.getDistanceLY(initialTransitFrom.getLocation(), 
										   playerFleet.getLocationInHyperspace()) > 2f) {
					return true;
				}
				return false;
			}
		});
		
		beginCustomTrigger(checker, Stage.SPAWN_FLEET);
		triggerMakeAllFleetFlagsPermanent();
	}
	
	public void endCreate() {
		triggerSetGlobalMemoryValue(globalEndFlag, true);
		triggerCustomAction(new DFEPlaceFleetAction());
		endTrigger();
		accept(null, null);
	}

	
	public void triggerSetAdjustStrengthBasedOnQuality(boolean randomize, float quality) {
		if (randomize) {
			triggerRandomizeFleetProperties();
		}
		setQuality(quality);
		setUseQualityInsteadOfQualityFraction(true);
		triggerAutoAdjustFleetStrengthMajor();
		setUseQualityInsteadOfQualityFraction(false);
	}
	
	protected PersonAPI personForRepLoss = null;
	public void setFleetWantsThing(String originalFactionId,
								   String thing,
								   String thingItOrThey,
								   String thingDesc,
								   int paymentOffered,
								   boolean aggressiveIfDeclined,
								   ComplicationRepImpact repImpact,
								   String failTrigger,
								   PersonAPI personForRepLoss) {
		
		this.personForRepLoss = personForRepLoss;
		
		triggerSetFleetMissionRef("$" + getMissionId() + "_ref");
		triggerSetFleetMissionRef("$fwt_ref");
		
		if (aggressiveIfDeclined) {
			triggerSetPirateFleet();
			triggerMakeHostileAndAggressive();
		}
		
		if (repImpact == ComplicationRepImpact.LOW) {
			triggerMakeLowRepImpact();
		} else if (repImpact == ComplicationRepImpact.NONE) {
			triggerMakeNoRepImpact();
		}
		
		triggerFleetAllowLongPursuit();
		triggerSetFleetAlwaysPursue();
		
		FactionAPI faction = Global.getSector().getFaction(originalFactionId);
		if (faction.getCustomBoolean(Factions.CUSTOM_SPAWNS_AS_INDEPENDENT)) {
			triggerSetFleetFaction(Factions.INDEPENDENT);
			triggerSetFleetMemoryValue("$fwt_originalFaction", originalFactionId);
		}
		
		triggerSetFleetMemoryValue("$fwt_wantsThing", true);
		triggerSetFleetMemoryValue("$fwt_aggressive", aggressiveIfDeclined);
		triggerSetFleetMemoryValue("$fwt_thing", getWithoutArticle(thing));
		triggerSetFleetMemoryValue("$fwt_Thing", Misc.ucFirst(getWithoutArticle(thing)));
		triggerSetFleetMemoryValue("$fwt_theThing", thing);
		triggerSetFleetMemoryValue("$fwt_TheThing", Misc.ucFirst(thing));
		triggerSetFleetMemoryValue("$fwt_payment", Misc.getWithDGS(paymentOffered));
		triggerSetFleetMemoryValue("$fwt_itOrThey", thingItOrThey);
		triggerSetFleetMemoryValue("$fwt_ItOrThey", Misc.ucFirst(thingItOrThey));
		
		String thingItOrThem = "them";
		if ("it".equals(thingItOrThey)) thingItOrThem = "it";
		triggerSetFleetMemoryValue("$fwt_itOrThem", thingItOrThem);
		triggerSetFleetMemoryValue("$fwt_ItOrThem", Misc.ucFirst(thingItOrThem));
		
		triggerSetFleetMemoryValue("$fwt_thingDesc", thingDesc);
		triggerSetFleetMemoryValue("$fwt_ThingDesc", Misc.ucFirst(thingDesc));
		
		if (failTrigger == null) {
			failTrigger = "FWTDefaultFailTrigger";
		}
		triggerSetFleetMemoryValue("$fwt_missionFailTrigger", failTrigger);
	}
	
	
//	public void setAbortWhenPlayerFleetTooStrong() {
//		playerFleetSizeAbortMult = 2f;
//	}
	
	public void setAlwaysAbort() {
		playerFleetSizeAbortMult = 0f;
	}
	public void setDoNotAbortWhenPlayerFleetTooStrong() {
		playerFleetSizeAbortMult = 100000000f;
	}
	
	public void setPlayerFleetSizeAbortMult(float playerFleetSizeAbortMult) {
		this.playerFleetSizeAbortMult = playerFleetSizeAbortMult;
	}

	protected FleetSize fleetSize = FleetSize.MEDIUM;
	protected float estimatedFleetPoints = 0f;
	protected float playerFleetSizeAbortMult = 2f;
	protected void computeThresholdPoints(String factionId) {
		FactionAPI faction = Global.getSector().getFaction(factionId);
		float maxPoints = faction.getApproximateMaxFPPerFleet(ShipPickMode.PRIORITY_THEN_ALL);
		estimatedFleetPoints = fleetSize.maxFPFraction * maxPoints;
	}
	
	public void triggerFleetSetFaction(String factionId) {
		computeThresholdPoints(factionId);
		super.triggerSetFleetFaction(factionId);
	}
	
	@Override
	public void triggerCreateFleet(FleetSize size, FleetQuality quality, String factionId, String type, SectorEntityToken roughlyWhere) {
		fleetSize = size;
		computeThresholdPoints(factionId);
		super.triggerCreateFleet(size, quality, factionId, type, roughlyWhere);
	}
	@Override
	public void triggerCreateFleet(FleetSize size, FleetQuality quality, String factionId, String type, StarSystemAPI roughlyWhere) {
		fleetSize = size;
		computeThresholdPoints(factionId);
		super.triggerCreateFleet(size, quality, factionId, type, roughlyWhere);
	}
	@Override
	public void triggerCreateFleet(FleetSize size, FleetQuality quality, String factionId, String type, Vector2f locInHyper) {
		fleetSize = size;
		computeThresholdPoints(factionId);
		super.triggerCreateFleet(size, quality, factionId, type, locInHyper);
	}
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		return false;
	}
	@Override
	public String getBaseName() {
		return null;
	}

	@Override
	public boolean isHidden() {
		return true;
	}
	
	
	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		float repLossPerson = 0f;
		float repLossFaction = 0f;
		if ("repLossMinor".equals(action)) {
			repLossPerson = -RepRewards.SMALL;
			repLossFaction = -RepRewards.TINY;
		} else if ("repLossMedium".equals(action)) {
			repLossPerson = -RepRewards.HIGH;
			repLossFaction = -RepRewards.SMALL;
		} else if ("repLossHigh".equals(action)) {
			repLossPerson = -RepRewards.EXTREME;
			repLossFaction = -RepRewards.HIGH;
		}
		
		if (repLossPerson != 0 && personForRepLoss != null) {
			CustomRepImpact impact = new CustomRepImpact();
			impact.delta = repLossPerson;
			Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.CUSTOM, impact,
							null, dialog.getTextPanel(), true), personForRepLoss);
			
			impact.delta = repLossFaction;
			Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.CUSTOM, impact,
							null, dialog.getTextPanel(), true), personForRepLoss.getFaction().getId());
			
			return true;
		}
		return super.callAction(action, ruleId, dialog, params, memoryMap);
	}
	
	
}




