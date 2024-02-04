package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionProductionAPI;
import com.fs.starfarer.api.campaign.FactionProductionAPI.ItemInProductionAPI;
import com.fs.starfarer.api.campaign.FactionProductionAPI.ProductionItemType;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction.ShipSaleInfo;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AdminData;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillsChangeRemoveExcessOPEffect;
import com.fs.starfarer.api.characters.SkillsChangeRemoveVentsCapsEffect;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.econ.impl.InstallableItemEffect;
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import com.fs.starfarer.api.impl.campaign.events.BaseEventPlugin.MarketFilter;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Drops;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.ProductionReportIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.ProductionReportIntel.ProductionData;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class CoreScript extends BaseCampaignEventListener implements EveryFrameScript {

	public static Logger log = Global.getLogger(CoreScript.class);
	
	public static final String SHARED_DATA_KEY = "core_CEFSSharedDataKey";
	
	private SharedData shared;
	
	private IntervalUtil timer = new IntervalUtil(0.5f, 1.5f);
	//private Set<String> marketsWithAssignedPatrolScripts = new HashSet<String>();
	
	public CoreScript() {
		super(true);
//		shared = new SharedData();
		shared = SharedData.getData();
	}

	private boolean firstFrame = true;
	public void advance(float amount) {
		SectorAPI sector = Global.getSector();
		playRepChangeSoundsIfNeeded();
		
		if (sector.isPaused()) {
			if (Global.getSettings().isDevMode()) {
				//RouteManager.getInstance().advance(amount);
				RouteManager.getInstance().advance(0f);
			}
			return;
		}
		
		if (firstFrame) {
			firstFrame = false;
		}
		
		float days = sector.getClock().convertToDays(amount);
		shared.advance(amount);
		
		timer.advance(days);
		if (timer.intervalElapsed()) {
		}
		
		RouteManager.getInstance().advance(amount);
		
		Misc.computeCoreWorldsExtent();
		
		//updateSlipstreamVisibility(amount);
	}
	
//	protected transient CampaignTerrainAPI currentStream = null;
//	public void updateSlipstreamVisibility(float amount) {
//		float sw = Global.getSettings().getFloat("sectorWidth");
//		float sh = Global.getSettings().getFloat("sectorHeight");
//		float minCellSize = 12000f;
//		float cellSize = Math.max(minCellSize, sw * 0.05f);
//		CollisionGridUtil grid = new CollisionGridUtil(-sw/2f, sw/2f, -sh/2f, sh/2f, cellSize);
//		Set<String> seenSystems = new LinkedHashSet<String>();
//		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
//			if (market.isHidden()) continue;
//			if (market.getContainingLocation() == null) continue;
//			if (!market.getContainingLocation().isHyperspace()) {
//				String systemId = market.getContainingLocation().getId();
//				if (seenSystems.contains(systemId)) continue;
//				seenSystems.add(systemId);
//			}
//			//if (market.hasIndustry(Industries.SPACEPORT)) continue;
//			Industry spaceport = market.getIndustry(Industries.SPACEPORT);
//			if (spaceport == null || !spaceport.isFunctional()) continue;
//			
//			Vector2f loc = market.getLocationInHyperspace();
//			float size = 10000f;
//			if (!market.getContainingLocation().hasTag(Tags.THEME_CORE)) {
//				size = 10000f;
//			}
//			//size = 200000;
////			if (market.getName().equals("Tartessus")) {
////				System.out.println("ewfwefe");
////			}
//			CustomStreamRevealer revealer = new CustomStreamRevealer(loc, size);
//			grid.addObject(revealer, loc, size * 2f, size * 2f);
//		}
//		
//		//System.out.println("BEGIN");
//		float maxDist = 0f;
//		List<CampaignTerrainAPI> terrainList = Global.getSector().getHyperspace().getTerrainCopy();
//		boolean processNext = false;
//		for (CampaignTerrainAPI terrain : terrainList) {
//			if (terrain.getPlugin() instanceof SlipstreamTerrainPlugin2) {
//				boolean process = false;
//				if (currentStream == null || processNext) {
//					process = true;
//				} else if (currentStream == terrain) {
//					processNext = true;
//				}
//				
//				if (!process) continue;
//				
//				currentStream = terrain;
//				if (terrainList.indexOf(terrain) == terrainList.size() - 1) {
//					currentStream = null;
//				}
//				//System.out.println("Processing: " + terrain.getId());
//				SlipstreamTerrainPlugin2 stream = (SlipstreamTerrainPlugin2) terrain.getPlugin();
//				for (SlipstreamSegment curr : stream.getSegments()) {
//					if (curr.discovered) continue;
//					Iterator<Object> iter = grid.getCheckIterator(curr.loc, curr.width / 2f, curr.width / 2f);
//					//Iterator<Object> iter = grid.getCheckIterator(curr.loc, 100f, 100f);
//					while (iter.hasNext()) {
//						Object obj = iter.next();
//						if (obj instanceof CustomStreamRevealer) {
//							CustomStreamRevealer rev = (CustomStreamRevealer) obj;
//							Vector2f loc = rev.loc;
//							float radius = rev.radius;
//							
//							float dist = Misc.getDistance(loc, curr.loc);
//							if (dist > maxDist) {
//								maxDist = dist;
////								if (dist >= 32500) {
////									System.out.println("Rev loc: " + rev.loc);
////									//grid.getCheckIterator(curr.loc, 100f, 100f);
////								}
//							}
//							if (dist < radius) {
//								curr.discovered = true;
//								break;
//							}
//						}
//					}
//				}
//				break;
//			}
//		}
//		//System.out.println("Max dist: " + maxDist);
//	}
	
	
	private void playRepChangeSoundsIfNeeded() {
		if (deltaFaction != null) {
			if (highestDelta > 0) {
				Global.getSoundPlayer().playUISound("ui_rep_raise", 1, 1);
			} else if (highestDelta < 0) {
				Global.getSoundPlayer().playUISound("ui_rep_drop", 1, 1);
			}
		}
		
		highestDelta = 0f;
		deltaFaction = null;
	}

	
	private float highestDelta = 0f;
	private String deltaFaction = null;
	@Override
	public void reportPlayerReputationChange(String faction, float delta) {
		super.reportPlayerReputationChange(faction, delta);
		if (Math.abs(delta) > Math.abs(highestDelta)) {
			highestDelta = delta;
			deltaFaction = faction;
		}
	}
	

	@Override
	public void reportPlayerReputationChange(PersonAPI person, float delta) {
		super.reportPlayerReputationChange(person, delta);
		if (Math.abs(delta) > Math.abs(highestDelta)) {
			highestDelta = delta;
			deltaFaction = person.getFaction().getId();
		}
	}


	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return true;
	}


	@Override
	public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
		super.reportPlayerMarketTransaction(transaction);
		
		for (ShipSaleInfo info : transaction.getShipsBought()) {
			FleetMemberAPI member = info.getMember();
			if (!member.getVariant().hasTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC)){ 
				SkillsChangeRemoveExcessOPEffect.clampOP(member, Global.getSector().getPlayerStats());
				SkillsChangeRemoveVentsCapsEffect.clampNumVentsAndCaps(member, Global.getSector().getPlayerStats());
			}
		}

		
		
		SubmarketAPI submarket = transaction.getSubmarket();
		MarketAPI market = transaction.getMarket();
		
		if (!market.isPlayerOwned() && submarket.getPlugin().isParticipatesInEconomy() &&
				!submarket.getPlugin().isBlackMarket() && submarket.getFaction() == market.getFaction()) {
			CargoAPI cargo = transaction.getSubmarket().getCargo();
			boolean didAnything = false;
			OUTER: for (CargoStackAPI stack : transaction.getSold().getStacksCopy()) {
				SpecialItemPlugin plugin = stack.getPlugin();
				if (plugin == null) continue;
				
				SpecialItemData data = stack.getSpecialDataIfSpecial();
				
				InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(data.getId());
				for (Industry ind : market.getIndustries()) {
					if (ind.wantsToUseSpecialItem(data)) {
						if (effect != null) {
							List<String> unmet = effect.getUnmetRequirements(ind);
							if (unmet != null && !unmet.isEmpty()) {
								continue;
							}
						}
						
						if (ind.getSpecialItem() != null) { // upgrade, put item into cargo
							cargo.addItems(CargoItemType.SPECIAL, ind.getSpecialItem(), 1);
						}
						cargo.removeItems(CargoItemType.SPECIAL, data, 1);
						ind.setSpecialItem(data);
						didAnything = true;
						continue OUTER;
					}
				}
			}
			
			if (didAnything) {
				cargo.sort();
			}
			
			// not sure about doing this for major factions:
			// - it can mess with their flavor, unintentionally if the player just sells blueprints
			// - it can be a way to make their fleets weaker, by selling loads of blueprints for poor ships
			//BlackMarketPlugin.delayedLearnBlueprintsFromTransaction(submarket.getFaction(), cargo, transaction);
		}
		
		//SharedData.getData().getPlayerActivityTracker().updateLastVisit(transaction.getMarket());

		// moved below code to BaseSubmarketPlugin
//		SubmarketAPI sub = transaction.getSubmarket();
//		if (sub.getPlugin().isParticipatesInEconomy()) {
//			SharedData.getData().getPlayerActivityTracker().getPlayerTradeData(sub).addTransaction(transaction);
//		}
	}

	@Override
	public void reportPlayerOpenedMarket(MarketAPI market) {
		super.reportPlayerOpenedMarket(market);
		SharedData.getData().getPlayerActivityTracker().updateLastVisit(market);
	}

	

	@Override
	public void reportBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle) {
		
		generateOrAddToDebrisFieldFromBattle(primaryWinner, battle);
		
		if (!battle.isPlayerInvolved()) return;

		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		if (!playerFleet.isValidPlayerFleet()) {
			float fp = 0;
			float crew = 0;
			for (FleetMemberAPI member : Misc.getSnapshotMembersLost(playerFleet)) {
				fp += member.getFleetPointCost();
				crew = member.getMinCrew();
			}
			shared.setPlayerPreLosingBattleFP(fp);
			shared.setPlayerPreLosingBattleCrew(crew);
			shared.setPlayerLosingBattleTimestamp(Global.getSector().getClock().getTimestamp());
		}
		
		
		for (final CampaignFleetAPI otherFleet : battle.getNonPlayerSideSnapshot()) {
			if (otherFleet.hasScriptOfClass(TOffAlarm.class)) continue;
			MemoryAPI memory = otherFleet.getMemoryWithoutUpdate();
			//if (!playerFleet.isTransponderOn()) {
			//if (!memory.getBoolean(MemFlags.MEMORY_KEY_LOW_REP_IMPACT)) {
				Misc.setFlagWithReason(memory, MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF, "battle", true, 7f + (float) Math.random() * 7f);
			//}
			//}
				
			if (!otherFleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_LOW_REP_IMPACT) ||
					otherFleet.getMemoryWithoutUpdate().getBoolean(MemFlags.SPREAD_TOFF_HOSTILITY_IF_LOW_IMPACT)) {
				otherFleet.addScript(new TOffAlarm(otherFleet));
			}
			
			
			float fpLost = Misc.getSnapshotFPLost(otherFleet);
	
			List<MarketAPI> markets = Misc.findNearbyLocalMarkets(otherFleet,
					Global.getSettings().getFloat("sensorRangeMax") + 500f,
				new MarketFilter() {
					public boolean acceptMarket(MarketAPI market) {
						//return market.getFaction().isAtWorst(otherFleet.getFaction(), RepLevel.COOPERATIVE);
						return market.getFaction() != null && market.getFaction() == otherFleet.getFaction();
					}
				});
			
			for (MarketAPI market : markets) {
				MemoryAPI mem = market.getMemoryWithoutUpdate();
				float expire = fpLost;
				if (mem.contains(MemFlags.MEMORY_KEY_PLAYER_HOSTILE_ACTIVITY_NEAR_MARKET)) {
					expire += mem.getExpire(MemFlags.MEMORY_KEY_PLAYER_HOSTILE_ACTIVITY_NEAR_MARKET); 
				}
				if (expire > 180) expire = 180;
				if (expire > 0) {
					mem.set(MemFlags.MEMORY_KEY_PLAYER_HOSTILE_ACTIVITY_NEAR_MARKET, true, expire);
				}
			}
		}
	}
	

	@Override
	public void reportFleetJumped(CampaignFleetAPI fleet, SectorEntityToken from, JumpDestination to) {
		super.reportFleetJumped(fleet, from, to);
		
		if (!fleet.isPlayerFleet()) return;

		FactionAPI faction = Global.getSector().getPlayerFaction();
		Color color = faction.getBaseUIColor();
		Color dark = faction.getDarkUIColor();
		Color grid = faction.getGridUIColor();
		Color bright = faction.getBrightUIColor();
		
		if (fleet.getContainingLocation() instanceof StarSystemAPI) {
			StarSystemAPI system = (StarSystemAPI) fleet.getContainingLocation();
			markSystemAsEntered(system, true);
		}
	}
	
	public static void markSystemAsEntered(StarSystemAPI system, boolean withMessages) {
		system.setEnteredByPlayer(true);
		
		for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isStar()) continue;
			
			MarketAPI market = planet.getMarket();
			if (market == null) continue;
			if (market.getSurveyLevel() == SurveyLevel.NONE) {
				market.setSurveyLevel(SurveyLevel.SEEN);
				String type = planet.getSpec().getName();
				if (!planet.isGasGiant()) type += " World";
//					Global.getSector().getCampaignUI().addMessage(
//							"New planet data: " + planet.getName() + ", " + type,
//							color);
				
				
				if (withMessages) {
//					CommMessageAPI message = Global.getFactory().createMessage();
//					message.setSubject("New planet data: " + planet.getName() + ", " + type);
//					message.setAction(MessageClickAction.INTEL_TAB);
//					message.setCustomData(planet);
//					message.setAddToIntelTab(false);
//					message.setSmallIcon(Global.getSettings().getSpriteName("intel_categories", "star_systems"));
//					Global.getSector().getCampaignUI().addMessage(message);
					
					MessageIntel intel = new MessageIntel("New planet data: " + planet.getName() + ", " + type,
							Misc.getBasePlayerColor());//, new String[] {"" + points}, Misc.getHighlightColor());
					intel.setIcon(Global.getSettings().getSpriteName("intel", "new_planet_info"));
					Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.INTEL_TAB, planet);
				}
			}
		}
	}

	
	public static void addMiscToDropData(DropData data, FleetMemberAPI member,
										  boolean weapons, boolean mods, boolean fighters) {
		ShipVariantAPI variant = member.getVariant();
		
		if (weapons) {
			float p = Global.getSettings().getFloat("salvageWeaponProb");
			for (String slotId : variant.getNonBuiltInWeaponSlots()) {
				String weaponId = variant.getWeaponId(slotId);
				data.addWeapon(weaponId, 1f * p);
			}
		}
		
		if (mods) {
			float p = Global.getSettings().getFloat("salvageHullmodProb");
			for (String id : member.getVariant().getHullMods()) {
				HullModSpecAPI spec = Global.getSettings().getHullModSpec(id);
				if (spec.isHidden() || spec.isHiddenEverywhere()) continue;
				if (spec.hasTag(Tags.HULLMOD_NO_DROP)) continue;
				data.addHullMod(id, 1f * p);
			}
		}
		
		if (fighters) {
			float p = Global.getSettings().getFloat("salvageWingProb");
			
			for (String id : member.getVariant().getFittedWings()) {
				FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(id);
				if (spec.hasTag(Tags.WING_NO_DROP)) continue;
				data.addFighterChip(id, 1f * p);
			}
		}
		
		data.valueMult = Global.getSettings().getFloat("salvageDebrisFieldFraction");
	}

	public static Set<String> getCargoCommodities(CargoAPI cargo) {
		Set<String> result = new HashSet<String>();
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			if (stack.isCommodityStack()) {
				result.add(stack.getCommodityId());
			}
		}
		return result;
	}
	
	public static void generateOrAddToDebrisFieldFromBattle(CampaignFleetAPI primaryWinner, BattleAPI battle) {
		
		if (primaryWinner == null) return;
		LocationAPI location = primaryWinner.getContainingLocation();
		if (location == null) return;
		
		//if (location.isHyperspace()) return;
		
		boolean allowDebris = !location.isHyperspace(); 
		
		
		boolean playerInvolved = battle.isPlayerInvolved();
		
		DropData misc = new DropData();
		int miscChances = 0;
		
		List<DropData> cargoList = new ArrayList<DropData>();
		
		WeightedRandomPicker<FleetMemberAPI> recoverySpecialChoices = new WeightedRandomPicker<FleetMemberAPI>();
		
		Vector2f com = new Vector2f();
		float count = 0f;
		float fpDestroyed = 0;
		for (CampaignFleetAPI fleet : battle.getSnapshotSideOne()) {
			count++;
			com.x += fleet.getLocation().x;
			com.y += fleet.getLocation().y;
			
			float fpForThisFleet = 0;
			for (FleetMemberAPI loss : Misc.getSnapshotMembersLost(fleet)) {
				//if (loss.getVariant().isStockVariant()) {
				if (!loss.getVariant().hasTag(Tags.SHIP_RECOVERABLE)) { // was not recoverable by player
					if (!loss.isStation() && !fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY)) {
						recoverySpecialChoices.add(loss);
					}
				}
				fpDestroyed += loss.getFleetPointCost();
				fpForThisFleet += loss.getFleetPointCost();
				if (allowDebris && !fleet.isPlayerFleet()) {
					addMiscToDropData(misc, loss, true, true, true);
					miscChances++;
				}
			}
			if (allowDebris && !fleet.isPlayerFleet()) {
				DropData cargo = new DropData();
				float cargoValue = fpForThisFleet * Global.getSettings().getFloat("salvageValuePerFP");
				cargoValue *= Global.getSettings().getFloat("salvageDebrisFieldFraction");
				if (cargoValue >= 1) {
					for (String cid : getCargoCommodities(fleet.getCargo())) {
						cargo.addCommodity(cid, 1f);
					}
					cargo.value = (int) cargoValue;
					cargo.chances = 1;
					cargoList.add(cargo);
				}
			}
		}
		for (CampaignFleetAPI fleet : battle.getSnapshotSideTwo()) {
			count++;
			com.x += fleet.getLocation().x;
			com.y += fleet.getLocation().y;
			
			float fpForThisFleet = 0;
			for (FleetMemberAPI loss : Misc.getSnapshotMembersLost(fleet)) {
				//if (loss.getVariant().isStockVariant()) {
				if (!loss.getVariant().hasTag(Tags.SHIP_RECOVERABLE)) { // was not recoverable by player
					if (!loss.isStation() && !fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY)) {
						recoverySpecialChoices.add(loss);
					}
				} else {
					loss.getVariant().removeTag(Tags.SHIP_RECOVERABLE);
				}
				
				fpDestroyed += loss.getFleetPointCost();
				fpForThisFleet += loss.getFleetPointCost();
				if (allowDebris && !fleet.isPlayerFleet()) {
					addMiscToDropData(misc, loss, true, true, true);
					miscChances++;
				}
			}
			if (allowDebris && !fleet.isPlayerFleet()) {
				DropData cargo = new DropData();
				float cargoValue = fpForThisFleet * Global.getSettings().getFloat("salvageValuePerFP");
				cargoValue *= Global.getSettings().getFloat("salvageDebrisFieldFraction");
				if (cargoValue >= 1) {
					for (String cid : getCargoCommodities(fleet.getCargo())) {
						cargo.addCommodity(cid, 1f);
					}
					cargo.value = (int) cargoValue;
					cargo.chances = 1;
					cargoList.add(cargo);
				}
			}
		}
		//Global.getSector().getPlayerFleet().getLocation()
		if (count <= 0) return;
		
		com.scale(1f / count);
		
		// spawn some derelict ships, maybe. do this here, regardless of whether the value is enough to
		// warrant a debris field
		float numShips = recoverySpecialChoices.getItems().size();
		float chanceDerelict = 1f - 10f / (numShips + 10f);
		//chanceNothing = 0f;
		//Vector2f com = battle.computeCenterOfMass();
		
		// in a battle that involves the player, recoverable ships are non-stock variants
		// (due to being prepared for recovery; dmods etc) and so don't show up as possible recovery choices here
		// which is good! since 1) they could've been actually recovered, and 2) they already contributed to player salvage
		// replaced this with Tags.SHIP_RECOVERABLE tag in recoverable variant for cleanness
		int max = 3;
		if (playerInvolved) {
			max = 2;
			chanceDerelict *= 0.25f;
		}
		for (int i = 0; i < max && !recoverySpecialChoices.isEmpty(); i++) {
			boolean spawnShip = Math.random() < chanceDerelict;
			if (spawnShip) {
				FleetMemberAPI member = recoverySpecialChoices.pickAndRemove();
				String variantId = member.getVariant().getHullVariantId();
				if (!member.getVariant().isStockVariant()) variantId = member.getVariant().getOriginalVariant();
				if (variantId == null) continue;
				DerelictShipData params = new DerelictShipData(new PerShipData(variantId,
										DerelictShipEntityPlugin.pickBadCondition(null), 0f), false);
				params.durationDays = DerelictShipEntityPlugin.getBaseDuration(member.getHullSpec().getHullSize());
				CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(
												 primaryWinner.getContainingLocation(),
												 Entities.WRECK, Factions.NEUTRAL, params);
				entity.addTag(Tags.EXPIRES);
				SalvageSpecialAssigner.assignSpecialForBattleWreck(entity);
				
//				entity.getLocation().x = com.x + (50f - (float) Math.random() * 100f);
//				entity.getLocation().y = com.y + (50f - (float) Math.random() * 100f);
				
				float angle = (float) Math.random() * 360f;
				float speed = 10f + 10f * (float) Math.random();
				Vector2f vel = Misc.getUnitVectorAtDegreeAngle(angle);
				vel.scale(speed);
				entity.getVelocity().set(vel);
				
				entity.getLocation().x = com.x + vel.x * 3f;
				entity.getLocation().y = com.y + vel.y * 3f;
			}
		}
		
		
		
		float salvageValue = fpDestroyed * Global.getSettings().getFloat("salvageValuePerFP");
		if (Misc.isEasy()) {
			salvageValue *= Global.getSettings().getFloat("easySalvageMult");
		}
		
		
		salvageValue *= Global.getSettings().getFloat("salvageDebrisFieldFraction");
		float salvageXP = salvageValue * 0.1f;
		
		float minForField = Global.getSettings().getFloat("minSalvageValueForDebrisField");
		//if (playerInvolved) minForField *= 6f;
		if (playerInvolved) minForField = 2500f + (float) Math.random() * 1000f;
		
		if (salvageValue < minForField || !allowDebris) return;
		
		
		CampaignTerrainAPI debris = null;
		for (CampaignTerrainAPI curr : primaryWinner.getContainingLocation().getTerrainCopy()) {
			if (curr.getPlugin() instanceof DebrisFieldTerrainPlugin) {
				DebrisFieldTerrainPlugin plugin = (DebrisFieldTerrainPlugin) curr.getPlugin();
				if (plugin.params.source == DebrisFieldSource.BATTLE && 
						plugin.params.density >= 1f &&
						plugin.containsPoint(com, 100f)) {
					debris = curr;
					break;
				}
			}
		}
		
		if (debris == null) {
			DebrisFieldParams params = new DebrisFieldParams(
					200f, // field radius - should not go above 1000 for performance reasons
					-1f, // density, visual - affects number of debris pieces
					1f, // duration in days 
					1f); // days the field will keep generating glowing pieces
			params.source = DebrisFieldSource.BATTLE;
			params.baseSalvageXP = (long) salvageXP; // base XP for scavenging in field
			
			debris = (CampaignTerrainAPI) Misc.addDebrisField(location, params, null);
			
			// makes the debris field always visible on map/sensors and not give any xp or notification on being discovered
			//debris.setSensorProfile(null);
			//debris.setDiscoverable(null);
			
			// makes it discoverable and give 200 xp on being found
			// sets the range at which it can be detected (as a sensor contact) to 2000 units
			//debris.setDiscoverable(true);
			//debris.setDiscoveryXP(200f);
			//debris.setSensorProfile(1f);
			//debris.getDetectedRangeMod().modifyFlat("gen", 2000);
			
			debris.setDiscoverable(null);
			debris.setDiscoveryXP(null);
			//debris.setSensorProfile(1f);
			//debris.getDetectedRangeMod().modifyFlat("gen", 1000);
			
			debris.getLocation().set(com);
			
			debris.getDropValue().clear();
			debris.getDropRandom().clear();
		}
		
		
		DebrisFieldTerrainPlugin plugin = (DebrisFieldTerrainPlugin) debris.getPlugin();
		DropData basicDrop = null;
		for (DropData data : debris.getDropValue()) {
			if (Drops.BASIC.equals(data.group)) {
				basicDrop = data;
				break;
			}
		}
		
		// since we're only adding to fields with density 1 (i.e. ones the player hasn't salvaged)
		// no need to worry about how density would affect the salvage value of what we're adding
		if (basicDrop == null) {
			basicDrop = new DropData();
			basicDrop.group = Drops.BASIC;
			debris.addDropValue(basicDrop);
		}
		basicDrop.value += salvageValue;
		
		if (misc.getCustom() != null) {
			misc.chances = miscChances;
			
			float total = misc.getCustom().getTotal();
			if (total > 0) {
				misc.addNothing(Math.max(1f, total));
			}
			debris.addDropRandom(misc);
			//misc.getCustom().print("MISC DROP");
		}
		
		for (DropData cargo : cargoList) {
			debris.addDropRandom(cargo);
		}
		
		
		//if (!battle.isPlayerInvolved()) {
			ShipRecoverySpecialData data = ShipRecoverySpecial.getSpecialData(debris, null, true, false);
			if (data != null && data.ships.size() < 3) {
				float items = recoverySpecialChoices.getTotal();
				float total = items + 25f;
				for (int i = 0; i < 3; i++) {
					if ((float) Math.random() * total < items) {
						FleetMemberAPI pick = recoverySpecialChoices.pick();
						if (pick != null) {
							String variantId = pick.getVariant().getHullVariantId();
							if (!pick.getVariant().isStockVariant()) variantId = pick.getVariant().getOriginalVariant();
							data.addShip(variantId, ShipCondition.WRECKED, 0f);
						}
					}
				}
			}
		//}
		
//		basicDrop = new DropData();
//		basicDrop.group = "misc_test";
//		basicDrop.value = 100000;
//		existing.addDropValue(basicDrop);
		
		// resize and adjust duration here
		
		float radius = 100f + (float) Math.min(900, Math.sqrt(basicDrop.value));
		float durationExtra = (float) Math.sqrt(salvageValue) * 0.1f;
		
		float minDays = DebrisFieldTerrainPlugin.DISSIPATE_DAYS + 1f;
		if (durationExtra < minDays) durationExtra = minDays;
		
		float time = durationExtra + plugin.params.lastsDays;
		if (time > 30f) time = 30f;
		
		plugin.params.lastsDays = time;
		plugin.params.glowsDays = time;
		
		plugin.params.bandWidthInEngine = radius;
		plugin.params.middleRadius = plugin.params.bandWidthInEngine / 2f;
		
		float range = DebrisFieldTerrainPlugin.computeDetectionRange(plugin.params.bandWidthInEngine);
		debris.getDetectedRangeMod().modifyFlat("gen", range);
	}


	
	
	@Override
	public void reportPlayerDumpedCargo(CargoAPI cargo) {
		super.reportPlayerDumpedCargo(cargo);
		
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		CustomCampaignEntityAPI pods = Misc.addCargoPods(playerFleet.getContainingLocation(), playerFleet.getLocation());
		pods.getCargo().addAll(cargo);
		
		CargoPodsResponse script = new CargoPodsResponse(pods);
		pods.getContainingLocation().addScript(script);
		ListenerUtil.reportPlayerLeftCargoPods(pods);
//		pods.getMemoryWithoutUpdate().set(CargoPods.LOCKED, true);
//		pods.getMemoryWithoutUpdate().set(CargoPods.CAN_UNLOCK, true);
		//pods.getMemoryWithoutUpdate().set(CargoPods.TRAPPED, true);
		
		//pods.getDetectedRangeMod().modifyFlat("gen", 1000);
	}
	
	@Override
	public void reportPlayerDidNotTakeCargo(CargoAPI cargo) {
		super.reportPlayerDumpedCargo(cargo);
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		CustomCampaignEntityAPI pods = Misc.addCargoPods(playerFleet.getContainingLocation(), playerFleet.getLocation());
		pods.getCargo().addAll(cargo);
		
		ListenerUtil.reportPlayerLeftCargoPods(pods);
	}

	protected Random prodRandom = new Random();
	public void doCustomProduction() {
		FactionAPI pf = Global.getSector().getPlayerFaction();
		FactionProductionAPI prod = pf.getProduction();
		
		MarketAPI gatheringPoint = prod.getGatheringPoint();
		if (gatheringPoint == null) return;

		//CargoAPI local = Misc.getLocalResourcesCargo(gatheringPoint);
		CargoAPI local = Misc.getStorageCargo(gatheringPoint);
		if (local == null) return;
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		MonthlyReport report = SharedData.getData().getCurrentReport();
		report.computeTotals();
		
		// limit production capacity by available credits - the net coming in this month plus what the player has
		int total = (int) (report.getRoot().totalIncome - report.getRoot().totalUpkeep);
		int credits = (int) playerFleet.getCargo().getCredits().get();
		credits += total;
		if (credits < 0) credits = 0;
		
		int capacity = prod.getMonthlyProductionCapacity();
		capacity = Math.min(capacity, credits);
		//capacity = 1000000;
		
		int remainingValue = capacity + prod.getAccruedProduction();
		//if (remainingValue <= 0) return;
		
		
		
		//Random random = new Random();
		if (prodRandom == null) prodRandom = new Random();
		Random random = prodRandom;
		
		WeightedRandomPicker<ItemInProductionAPI> picker = new WeightedRandomPicker<ItemInProductionAPI>(random);
		for (ItemInProductionAPI item : prod.getCurrent()) {
			if (item.getBuildDelay() > 0 && !Global.getSettings().isDevMode()) continue;
			picker.add(item, item.getQuantity());
		}
		int accrued = 0;
		
		boolean wantedToDoProduction = !picker.isEmpty();
		boolean unableToDoProduction = capacity <= 0;

		ProductionData data = new ProductionData();
		//CargoAPI cargo = Global.getFactory().createCargo(true);
		//cargo.initMothballedShips(Factions.PLAYER);
		CargoAPI cargo = data.getCargo("Heavy Industry - Custom Production");
		
		float quality = -1f;
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (!market.isPlayerOwned()) continue;
			//quality = Math.max(quality, ShipQuality.getShipQuality(market, Factions.PLAYER));
			float currQuality = market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).computeEffective(0f);
			currQuality += market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).computeEffective(0f);
			quality = Math.max(quality, currQuality);
		}
		quality -= Global.getSector().getFaction(Factions.PLAYER).getDoctrine().getShipQualityContribution();
		quality += 4f * Global.getSettings().getFloat("doctrineFleetQualityPerPoint");
		
		CampaignFleetAPI ships = Global.getFactory().createEmptyFleet(Factions.PLAYER, "temp", true);
		ships.setCommander(Global.getSector().getPlayerPerson());
		DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
		p.quality = quality;
		p.mode = ShipPickMode.PRIORITY_THEN_ALL;
		p.persistent = false;
		p.seed = random.nextLong();
		p.timestamp = null;
		
		FleetInflater inflater = Misc.getInflater(ships, p);
		ships.setInflater(inflater);
		
		int totalCost = 0;
		while (remainingValue > 0 && !picker.isEmpty()) {
			ItemInProductionAPI pick = picker.pick();
			int baseCost = pick.getBaseCost();
			
			int count = Math.min(pick.getQuantity(), remainingValue / Math.max(1, baseCost));
			if (count > 0) {
				count = random.nextInt(count) + 1;
			}
			if (count <= 0) {
				accrued = remainingValue;
				remainingValue = 0;
			} else {
				int currCost = count * baseCost;
				totalCost += currCost;
				remainingValue -= currCost;
				
				if (pick.getType() == ProductionItemType.SHIP) {
					List<String> variants = Global.getSettings().getHullIdToVariantListMap().get(pick.getSpecId());
					if (variants.isEmpty()) {
						variants.add(pick.getSpecId() + "_Hull");
						continue;
					}
					
					int index = random.nextInt(variants.size());
					//cargo.addMothballedShip(FleetMemberType.SHIP, variants.get(index), null);
					for (int i = 0; i < count; i++) {
						ships.getFleetData().addFleetMember(variants.get(index));
					}
				} else if (pick.getType() == ProductionItemType.FIGHTER) {
					cargo.addFighters(pick.getSpecId(), count);
				} else if (pick.getType() == ProductionItemType.WEAPON) {
					cargo.addWeapons(pick.getSpecId(), count);
				}
				
				prod.removeItem(pick.getType(), pick.getSpecId(), count);
				if (pick.getQuantity() <= 0) {
					picker.remove(pick);
				}
			}
		}
		
		int weaponCost = 0;
		ships.inflateIfNeeded();
		for (FleetMemberAPI member : ships.getFleetData().getMembersListCopy()) {
			cargo.getMothballedShips().addFleetMember(member);
			for (String wingId : member.getVariant().getNonBuiltInWings()) {
				FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(wingId);
				weaponCost += spec.getBaseValue();
			}
			for (String slotId : member.getVariant().getNonBuiltInWeaponSlots()) {
				WeaponSpecAPI spec = member.getVariant().getWeaponSpec(slotId);
				weaponCost += spec.getBaseValue();
			}
		}
		if (!DebugFlags.WEAPONS_HAVE_COST) {
			weaponCost = 0;
		}
		
		// add some supplies, fuel, and crew
		int addedValue = (int) (totalCost * Global.getSettings().getFloat("productionSuppliesBonusFraction"));
		int sCost = (int) Global.getSettings().getCommoditySpec(Commodities.SUPPLIES).getBasePrice();
		int fCost = (int) Global.getSettings().getCommoditySpec(Commodities.FUEL).getBasePrice();
		int cCost = (int) Global.getSettings().getCommoditySpec(Commodities.CREW).getBasePrice();
		
		int supplies = (int) (addedValue * (0.5f * (0.5f + random.nextFloat() * 0.5f))) / sCost;
		int fuel = (int) (addedValue * (0.3f * (0.5f + random.nextFloat() * 0.5f))) / fCost;
		int crew = (addedValue - sCost * supplies - fCost * fuel) / cCost;
		
		supplies = supplies / 10 * 10;
		fuel = fuel / 10 * 10;
		crew = crew / 10 * 10;
		
		cargo.addSupplies(supplies);
		cargo.addFuel(fuel);
		cargo.addCrew(crew);
		
		//accrued += remainingValue; // don't do that, only want to accrue when trying to produce something

		totalCost -= prod.getAccruedProduction();
		totalCost += accrued;
		if (totalCost < 0) totalCost = 0;
		prod.setAccruedProduction(accrued);
		
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (!market.isPlayerOwned()) continue;
		
			for (Industry ind : market.getIndustries()) {
				Random curr = Misc.getRandom(random.nextLong(), 11);
				CargoAPI added = ind.generateCargoForGatheringPoint(curr);
				if (added != null && (!added.isEmpty() || 
						(added.getMothballedShips() != null && !added.getMothballedShips().getMembersListCopy().isEmpty()))) {
					String title = ind.getCargoTitleForGatheringPoint();
					data.getCargo(title).addAll(added, true);
				}
			}
		}
		
		
		// done with production
		
		
		
		if (!data.isEmpty() || totalCost > 0 || (wantedToDoProduction && unableToDoProduction)) {
			if (totalCost > 0) {
				//MonthlyReport report = SharedData.getData().getCurrentReport();
				
				FDNode marketsNode = report.getNode(MonthlyReport.OUTPOSTS);
				if (marketsNode.name == null) {
					marketsNode.name = "Colonies";
					marketsNode.custom = MonthlyReport.OUTPOSTS;
					marketsNode.tooltipCreator = report.getMonthlyReportTooltip();
				}
				
				FDNode production = report.getNode(marketsNode, MonthlyReport.PRODUCTION);
				production.name = "Custom production orders";
				production.custom = MonthlyReport.PRODUCTION;
				production.custom2 = cargo;
				production.tooltipCreator = report.getMonthlyReportTooltip();
				
				production.upkeep += totalCost;
				
				if (weaponCost > 0) {
					FDNode productionWeapons = report.getNode(marketsNode, MonthlyReport.PRODUCTION_WEAPONS);
					productionWeapons.name = "Weapons & fighter LPCs for produced ships";
					productionWeapons.custom = MonthlyReport.PRODUCTION_WEAPONS;
					productionWeapons.tooltipCreator = report.getMonthlyReportTooltip();
					productionWeapons.upkeep += weaponCost;
				}
			}
		
			for (CargoAPI curr : data.data.values()) {
				local.addAll(curr);
				local.initMothballedShips(Factions.PLAYER);
				for (FleetMemberAPI member : curr.getMothballedShips().getMembersListCopy()) {
//					member.getRepairTracker().setCR(0f);
//					member.getRepairTracker().setMothballed(true);
					member.getRepairTracker().setMothballed(false);
					member.getRepairTracker().setCR(0.5f);
					local.getMothballedShips().addFleetMember(member);
				}
			}
			local.sort();
			
			ProductionReportIntel intel = new ProductionReportIntel(gatheringPoint, data, 
													totalCost + weaponCost, prod.getAccruedProduction(),
													wantedToDoProduction && unableToDoProduction);
			Global.getSector().getIntelManager().addIntel(intel);
		}
		
	}
	

	@Override
	public void reportEconomyMonthEnd() {
		super.reportEconomyMonthEnd();

		if (TutorialMissionIntel.isTutorialInProgress()) {
			return;
		}
		
		MonthlyReport report = SharedData.getData().getCurrentReport();
		FDNode marketsNode = report.getNode(MonthlyReport.OUTPOSTS);
		if (marketsNode.custom != null) {
			for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
				if (!market.isPlayerOwned()) continue;
				
				float incentive = market.getIncentiveCredits();
				if (incentive > 0) {
					FDNode mNode = report.getNode(marketsNode, market.getId());
					if (mNode.custom != null) {
						FDNode incNode = report.getNode(mNode, "incentives"); 
						incNode.name = "Hazard pay";
						incNode.custom = MonthlyReport.INCENTIVES;
						incNode.mapEntity = market.getPrimaryEntity();
						incNode.tooltipCreator = report.getMonthlyReportTooltip();
						incNode.upkeep += incentive;
					}
					market.setIncentiveCredits(0);
				}
			}
		}
		
		
		MonthlyReport previous = SharedData.getData().getPreviousReport();
		float debt = previous.getDebt();
		if (debt > 0) {
			MonthlyReport current = SharedData.getData().getCurrentReport();
			current.getDebtNode().upkeep = debt;
		}
		
		doCustomProduction();
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		//MonthlyReport previous = SharedData.getData().getPreviousReport();
		SharedData.getData().rollOverReport();
		report = SharedData.getData().getPreviousReport();
		report.setPreviousDebt(previous.getDebt());
		report.setTimestamp(Global.getSector().getClock().getTimestamp());
		
		report.computeTotals();
		int total = (int) (report.getRoot().totalIncome - report.getRoot().totalUpkeep);
		float credits = (int) playerFleet.getCargo().getCredits().get();
		
		float newCredits = credits + total;
		if (newCredits < 0) {
			report.setDebt((int) Math.abs(newCredits));
			newCredits = 0;
		}
		playerFleet.getCargo().getCredits().set(newCredits);
		
		
		String totalStr = Misc.getDGSCredits(Math.abs(total));
		//Global.getSector().getCampaignUI().showCoreUITab(CoreUITabId.OUTPOSTS);
		
		String title = "Monthly income: " + totalStr;
		Color highlight = Misc.getHighlightColor();
		if (total < 0) {
			title = "Monthly expenses: " + totalStr;
			highlight = Misc.getNegativeHighlightColor();
		}
		
		MessageIntel intel = new MessageIntel(title,
				Misc.getBasePlayerColor(), new String[] {totalStr}, highlight);
		intel.setIcon(Global.getSettings().getSpriteName("intel", "monthly_income_report"));
		
		if (total >= 0) {
			intel.setSound("ui_intel_monthly_income_positive");
		} else {
			intel.setSound("ui_intel_monthly_income_negative");
		}
		
		Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.INCOME_TAB, Tags.INCOME_REPORT);
		
		
//		CommMessageAPI message = FleetLog.beginEntry(
//				title,
//				playerFleet, 
//				highlight,
//				totalStr
//		);
//		
//		message.setSmallIcon(Global.getSettings().getSpriteName("intel_categories", "bounties"));
//		if (total >= 0) {
//			message.getSection1().addPara("Over the last month, your fleet, outposts, and other ventures have" +
//					" produced an income of " + totalStr + ".");			
//		} else {
//			message.getSection1().addPara("Over the last month, your fleet, outposts, and other ventures have" +
//					" produced expenses of " + totalStr + ".");
//		}
//		
//		if (total < 0 && Math.abs(total) > credits) {
//			message.getSection1().addPara("Your expenses have exceeded your credit balance. If this continues for more than a month, " +
//					"some crew and officers may begin to leave, and other undertakings requiring credit expenditures may fail.");
//			//message.getSection1().addPara(".");
//		}
//		message.getSection1().addPara("See the \"Income\" tab in the command screen for a detailed breakdown.");
//		
//		message.getSection1().setHighlights(totalStr);
//		message.getSection1().setHighlightColors(highlight);
//		
//		message.setShowInCampaignList(true);
//		message.setAddToIntelTab(true);
//		message.setAction(MessageClickAction.INCOME_TAB);
//		message.addTag(Tags.FLEET_LOG);
//		message.addTag(Tags.INCOME_REPORT);
//		
//		Global.getSector().getCampaignUI().addMessage(message);
	}

//	private MonthlyReportNodeTooltipCreator monthlyReportTooltip;
//	public static MonthlyReportNodeTooltipCreator getMonthlyReportTooltip() {
//		if (monthlyReportTooltip == null) {
//			monthlyReportTooltip = new MonthlyReportNodeTooltipCreator();
//		}
//		return monthlyReportTooltip;
//	}


	@Override
	public void reportEconomyTick(int iterIndex) {
		super.reportEconomyTick(iterIndex);
		
		if (TutorialMissionIntel.isTutorialInProgress()) {
			return;
		}
		
		//for (int i = 0; i < 100; i++) {
		int crewSalary = Global.getSettings().getInt("crewSalary");
		int marineSalary = Global.getSettings().getInt("marineSalary");
		
		float numIter = Global.getSettings().getFloat("economyIterPerMonth");
		float f = 1f / numIter;
		
		//f = 1f;
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		MonthlyReport report = SharedData.getData().getCurrentReport();
		
		
		FDNode fleetNode = report.getNode(MonthlyReport.FLEET);
		fleetNode.name = "Fleet";
		fleetNode.custom = MonthlyReport.FLEET;
		fleetNode.tooltipCreator = report.getMonthlyReportTooltip();
		
		int crewCost = playerFleet.getCargo().getCrew() * crewSalary;
		FDNode crewNode = report.getNode(fleetNode, MonthlyReport.CREW);
		crewNode.upkeep += crewCost * f;
		crewNode.name = "Crew payroll";
		crewNode.custom = MonthlyReport.CREW;
		crewNode.tooltipCreator = report.getMonthlyReportTooltip();
		
		int marineCost = playerFleet.getCargo().getMarines() * marineSalary;
		if (marineSalary > 0) {
			FDNode marineNode = report.getNode(fleetNode, MonthlyReport.MARINES);
			marineNode.upkeep += marineCost * f;
			marineNode.name = "Marine payroll";
			marineNode.custom = MonthlyReport.MARINES;
			marineNode.tooltipCreator = report.getMonthlyReportTooltip();
		}
		
		
//		List<PersonnelAtEntity> droppedOffMarines = PlayerFleetPersonnelTracker.getInstance().getDroppedOff();
//		for (PersonnelAtEntity curr : droppedOffMarines) {
//			
//		}
//		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
//			if (Misc.playerHasStorageAccess(market)) {
//				
//			}
//		}
		
		
		
		//FDNode officersNode = report.getNode(MonthlyReport.OFFICERS);
		FDNode officersNode = report.getNode(fleetNode, MonthlyReport.OFFICERS);
		officersNode.name = "Officer payroll";
		officersNode.custom = MonthlyReport.OFFICERS;
		officersNode.tooltipCreator = report.getMonthlyReportTooltip();
		
		
		for (OfficerDataAPI od : playerFleet.getFleetData().getOfficersCopy()) {
			float salary = Misc.getOfficerSalary(od.getPerson());
			FDNode oNode = report.getNode(officersNode, od.getPerson().getId()); 
			oNode.name = od.getPerson().getName().getFullName();
			oNode.upkeep += salary * f;
			oNode.custom = od;
		}
		
		FDNode marketsNode = report.getNode(MonthlyReport.OUTPOSTS);
		marketsNode.name = "Colonies";
		marketsNode.custom = MonthlyReport.OUTPOSTS;
		marketsNode.tooltipCreator = report.getMonthlyReportTooltip();
		
		FDNode storageNode = null;
//		storageNode = report.getNode(MonthlyReport.STORAGE);
//		storageNode.name = "Storage";
//		storageNode.custom = MonthlyReport.STORAGE;
//		storageNode.tooltipCreator = report.getMonthlyReportTooltip();
		
		float storageFraction = Global.getSettings().getFloat("storageFreeFraction");
		
		int index = 0;
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
//			if (index % 10 != 3 || index == 0) {
//				index++;
//				continue;
//			}
			
			if (!market.isPlayerOwned() && Misc.playerHasStorageAccess(market)) {
				float vc = Misc.getStorageCargoValue(market);
				float vs = Misc.getStorageShipValue(market);
				
				float fc = (int) (vc * storageFraction);
				float fs = (int) (vs * storageFraction);
				if (fc > 0 || fs > 0) {
					if (storageNode == null) {
						storageNode = report.getNode(MonthlyReport.STORAGE);
						storageNode.name = "Storage";
						storageNode.custom = MonthlyReport.STORAGE;
						storageNode.tooltipCreator = report.getMonthlyReportTooltip();
					}
					FDNode mNode = report.getNode(storageNode, market.getId());
					String desc = "";
					if (fc > 0 && fs > 0) {
						desc = "ships & cargo";
					} else if (fc > 0) {
						desc = "cargo";
					} else {
						desc = "ships";
					}
					mNode.name = market.getName() + " (" + desc + ")";
					mNode.custom = market;
					mNode.custom2 = MonthlyReport.STORAGE;
					//mNode.tooltipCreator = report.getMonthlyReportTooltip();
					
					mNode.upkeep += (fc + fs) * f;
				}
				continue;
			}
			
			
			//if (market.isHidden()) continue;
			//if (!Factions.DIKTAT.equals(market.getFaction().getId()) && !market.isPlayerOwned()) continue;
			if (!market.isPlayerOwned()) continue;
			
			FDNode mNode = report.getNode(marketsNode, market.getId()); 
			mNode.name = market.getName() + " (" + market.getSize() + ")";
			mNode.custom = market;
			
			FDNode indNode = report.getNode(mNode, "industries"); 
			indNode.name = "Industries & structures";
			indNode.custom = MonthlyReport.INDUSTRIES;
			indNode.mapEntity = market.getPrimaryEntity();
			indNode.tooltipCreator = report.getMonthlyReportTooltip();
//			node.income += (int) market.getIndustryIncome();
//			node.upkeep += (int) market.getIndustryUpkeep();
			
			for (Industry curr : market.getIndustries()) {
				FDNode iNode = report.getNode(indNode, curr.getId());
				iNode.name = curr.getCurrentName();
				iNode.income += curr.getIncome().getModifiedInt() * f;
				iNode.upkeep += curr.getUpkeep().getModifiedInt() * f;
				iNode.custom = curr;
				iNode.mapEntity = market.getPrimaryEntity();
			}
			
			FDNode exportNode = report.getNode(mNode, "exports"); 
			exportNode.name = "Exports";
			exportNode.custom = MonthlyReport.EXPORTS;
			exportNode.mapEntity = market.getPrimaryEntity();
			exportNode.tooltipCreator = report.getMonthlyReportTooltip();
			
			addExportsGroupedByCommodity(report, exportNode, market, f);
			//addExportsGroupedByFaction(report, exportNode, market, f);
			
			
//			FDNode overheadNode = report.getNode(exportNode, "overhead");
//			if (overheadNode.name == null) {
//				overheadNode.name = "Overhead";
//				overheadNode.icon = Global.getSettings().getSpriteName("income_report", "overhead");
//				overheadNode.custom = market;
//				overheadNode.mapEntity = market.getPrimaryEntity();
//				overheadNode.tooltipCreator = new OverheadTooltipCreator();
//			}
//			
//			OverheadData overhead = computeOverhead(market);
//			if (overhead.fraction > 0) {
//				float totalIncome = market.getExportIncome(false);
//				overheadNode.upkeep += totalIncome * overhead.fraction * f; 
//			}
			
			index++;
		}
		//}
		
		
		FDNode adminNode = report.getNode(marketsNode, MonthlyReport.ADMIN);
		adminNode.name = "Administrators";
		adminNode.custom = MonthlyReport.ADMIN;
		adminNode.tooltipCreator = report.getMonthlyReportTooltip();
		
		for (AdminData data : Global.getSector().getCharacterData().getAdmins()) {
			float salary = Misc.getAdminSalary(data.getPerson());
			if (salary <= 0) continue;
			
			FDNode aNode = report.getNode(adminNode, data.getPerson().getId()); 
			aNode.name = data.getPerson().getName().getFullName();
			if (data.getMarket() != null) {
				aNode.name += " (" + data.getMarket().getName() + ")";
			} else {
				aNode.name += " (unassigned)";
				salary *= Global.getSettings().getFloat("idleAdminSalaryMult");
			}
			aNode.upkeep += salary * f;
			aNode.custom = data;
		}
		
		//reportEconomyMonthEnd();
	}
	
//	public static class OverheadData {
//		public SupplierData max;
//		public float fraction;
//	}
//	
//	public static OverheadData computeOverhead(MarketAPI market) {
//		OverheadData result = new OverheadData();
//		
//		SupplierData max = null;
//		float maxValue = 0;
//		float total = 0f;
//		List<CommodityOnMarketAPI> comList = market.getCommoditiesCopy();
//		for (CommodityOnMarketAPI com : comList) {
//			for (SupplierData sd : com.getExports()) {
//				int income = sd.getExportValue(market);
//				if (income <= 0) continue;
//				
//				total += income;
//				if (income > maxValue) {
//					max = sd;
//					maxValue = income;
//				}
//			}
//		}
//		
//		if (max != null && maxValue > 0) {
//			result.max = max;
//			float units = total / maxValue;
//			float mult = Misc.logOfBase(2f, units) + 1f;
//			result.fraction = 1f - (mult / units);
//			if (result.fraction < 0) result.fraction = 0;
//			if (result.fraction > 0) {
//				result.fraction = Math.round(result.fraction * 100f) / 100f;
//				result.fraction = Math.max(result.fraction, 0.01f);
//			}
//		}
//		return result;
//	}
	
	public static class ExportCommodityGroupData {
		public CommodityOnMarketAPI com;
		public int quantity;
	}
	
	protected void addExportsGroupedByCommodity(MonthlyReport report, FDNode parent, MarketAPI market, float f) {
		for (CommodityOnMarketAPI com : market.getCommoditiesCopy()) {
			FDNode eNode = report.getNode(parent, com.getId());
			eNode.name = com.getCommodity().getName();
			eNode.income += com.getExportIncome() * f;
			eNode.custom = com;
			eNode.mapEntity = market.getPrimaryEntity();
		}
		
//		List<FDNode> sorted = new ArrayList<FDNode>(parent.getChildren().values());
//		Collections.sort(sorted, new Comparator<FDNode>() {
//			public int compare(FDNode o1, FDNode o2) {
//				return o2.income - o1.income;
//			}
//		});
//		parent.getChildren().clear();
//		parent.getChildren().
	}
	
	
	
}


















