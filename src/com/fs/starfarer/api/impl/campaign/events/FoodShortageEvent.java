package com.fs.starfarer.api.impl.campaign.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.BaseOnMessageDeliveryScript;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.comm.MessagePriority;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventManagerAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.campaign.events.EventProbabilityAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Events;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;

public class FoodShortageEvent extends BaseEventPlugin {
	
	public static Logger log = Global.getLogger(FoodShortageEvent.class);
	
	public static String PRICE_MOD_ID = "fse_priceMod";
	
	public static float FOOD_PRICE_MULT = 1.5f;
	public static float FOOD_PRICE_FLAT = 50f;
	
	public static float MAX_POSSIBLE_DURATION = 60f;
	public static float MIN_FRACTION_FOR_PLAYER_ENDING_EVENT = 0.5f;
	
	
	public static enum Ending {
		DURATION_EXPIRED,
		RELIEF_ARRIVED,
		PLAYER_ENDED,
		PLAYER_ENDED_BLACK,
	}
	
	public static class PlayerFoodTransaction {
		public long timestamp;
		
		/**
		 * Positive for player-bought, negative for player-sold. 
		 */
		public float quantity;
	}
	
	private boolean ended = false;
	
	private float maxDurationDays;
	private float daysBeforeReliefCheck;
	private float daysBeforeReliefSend;
	private float elapsedDays = 0f;
	private int stage = 0;

	//private List<PlayerFoodTransaction> preEventTransactions = new ArrayList<PlayerFoodTransaction>();
	private float preEventFoodLevel;
	
	//private float foodToMeetShortage = 0;
	private float baseFoodToMeetShortage = 0;
	//private float playerBlameAmount = 0;
	private float netDeliveredByPlayerBlack = 0;
	private float netDeliveredByPlayerOther = 0;
	
	//private float originalReliefFleetCargoCapacity = 0;
	private float originalReliefFleetPoints = 0;
	private CampaignFleetAPI reliefFleet;
	
	private MessagePriority messagePriority = MessagePriority.CLUSTER;
	private String foodShortageConditionToken = null;
	
	private MarketAPI reliefMarket;

	
	public void init(String type, CampaignEventTarget eventTarget) {
		super.init(type, eventTarget, false);
	}
	
	public void startEvent() {
		super.startEvent(true);
		if (market == null) return;
		
		switch (market.getSize()) {
		case 1:
		case 2:
		case 3:
			messagePriority = MessagePriority.SYSTEM;
			break;
		case 4:
		case 5:
			messagePriority = MessagePriority.CLUSTER;
			break;
		case 6:
		case 7:
		case 8:
		case 9:
			messagePriority = MessagePriority.SECTOR;
			break;
		}
		
		// moved to FoodShortage condition
//		String sellId = Stats.getPlayerSellImpactMultId(Commodities.FOOD);
//		market.getStats().getDynamic().getStat(sellId).modifyFlat(getId() + "_mod", getStabilityImpact());
		
		baseFoodToMeetShortage = getBaseShortageAmount();
		//foodToMeetShortage = baseFoodToMeetShortage;
		
		maxDurationDays = 20f + (float) Math.random() * 20f;
		
		daysBeforeReliefCheck = 7f + (float) Math.random() * 7f;
		daysBeforeReliefSend = daysBeforeReliefCheck + (float) Math.random() * 7f;
		
		foodShortageConditionToken = market.addCondition(Conditions.EVENT_FOOD_SHORTAGE, this);
		CommodityOnMarketAPI com = market.getCommodityData(Commodities.FOOD);
		
		preEventFoodLevel = com.getStockpile();
		com.setStockpile(com.getStockpile() * getStockpileMult());
		com.getPlayerSupplyPriceMod().modifyMult(PRICE_MOD_ID, FOOD_PRICE_MULT);
		com.getPlayerSupplyPriceMod().modifyFlat(PRICE_MOD_ID, FOOD_PRICE_FLAT);
		
		
		SubmarketAPI open = market.getSubmarket("open_market");
		if (open != null) {
			float food = open.getCargo().getQuantity(CargoItemType.RESOURCES, Commodities.FOOD);
			open.getCargo().removeItems(CargoItemType.RESOURCES, Commodities.FOOD, food);
		}
		
		SubmarketAPI black = market.getSubmarket("black_market");
		if (black != null) {
			black.getPlugin().updateCargoPrePlayerInteraction();
			black.getCargo().addItems(CargoItemType.RESOURCES, Commodities.FOOD,
									  (int)Math.max(1, baseFoodToMeetShortage * (0.1f + (float) Math.random() * 0.05f)));
		}
		
		Global.getSector().reportEventStage(this, "start", messagePriority);
		
		log.info(getLoggingId() + " Starting food shortage" + 
				 ", food needed: " + (int) baseFoodToMeetShortage +
				 ", stockpile mult: " + getStockpileMult() + 
				 ", max duration: " + (int) maxDurationDays + " days");
	}
	
	
	private float getBaseShortageAmount() {
		float stockpileMult = getStockpileMult();
		CommodityOnMarketAPI com = market.getCommodityData(Commodities.FOOD);
		float amount = com.getStockpile() * (1f - stockpileMult);
		if (amount < 50) amount = 50;
		return Misc.getRounded(amount);
	}

	
	private float getStockpileMult() {
		switch ((int) market.getSize()) {
		case 0:
		case 1:
			return 0f;
		case 2:
		case 3:
			return 0.2f;
		case 4:
		case 5:
			return 0.3f;
		case 6:
		case 7:
			return 0.5f;
		case 8:
		case 9:
			return 0.9f;
		}
		return 1f;
//		switch ((int) market.getSize()) {
//		case 0:
//		case 1:
//			return 0f;
//		case 2:
//		case 3:
//			return 0.25f;
//		case 4:
//		case 5:
//			return 0.5f;
//		case 6:
//		case 7:
//		case 8:
//		case 9:
//			return 0.9f;
//		}
//		return 1f;
	}
	
	
	public float getStabilityImpact() {
		switch ((int) market.getSize()) {
		case 0:
		case 1:
			return 4;
		case 2:
		case 3:
			return 3;
		case 4:
		case 5:
			return 2;
		case 6:
		case 7:
		case 8:
		case 9:
			return 1;
		}
		return 1f;		
	}
	
	public float getReliefAbortUnrest() {
		return 2;
	}
	
	public float getAddedExpiredUnrest() {
		return 1;
	}
	
	public float getReducedEndedUnrest() {
		return 1;
	}

	public float getEndBlackUnrest() {
		float base = getStabilityImpact();
		float mult = Math.min(1f, netDeliveredByPlayerBlack / baseFoodToMeetShortage);
		return Math.round(base * mult);
	}
	
	public void advance(float amount) {
		if (Global.getSector().isPaused()) return;
		
		//float econInterval = Global.getSettings().getFloat("economyIntervalnGameDays");
		float days = Global.getSector().getClock().convertToDays(amount);
		
		if (!isEventStarted()) {
			return;
		}
		
		if (isDone()) return;
		
		elapsedDays += days;
		
		if (elapsedDays > daysBeforeReliefCheck && stage == 0) {
			stage++;
			
			reliefMarket = findNearestMarket(market, new MarketFilter() {
				public boolean acceptMarket(MarketAPI market) {
					if (market == FoodShortageEvent.this.market) {
						return false;
					}
					if (market.getFaction().isHostileTo(FoodShortageEvent.this.market.getFaction())) {
						return false;
					}
					if (SharedData.getData().getMarketsThatSentRelief().contains(market.getId())) {
						return false;
					}
					if (market.getCommodityData(Commodities.FOOD).getStockpile() < baseFoodToMeetShortage) {
						return false;
					}
					
					CampaignEventManagerAPI manager = Global.getSector().getEventManager();
					EventProbabilityAPI ep = manager.getProbability(Events.FOOD_SHORTAGE, market);
					if (ep.getProbability() > 0.1) {
						return false;
					}
					if (manager.isOngoing(ep)) {
						return false;
					}
					if ((float) Math.random() * 10f > market.getStabilityValue()) return false;
					
					return market.getSize() >= FoodShortageEvent.this.market.getSize();
				}
			});
			//reliefMarket = Global.getSector().getEconomy().getMarket("sindria");
			
			//if ((float) Math.random() > 0.67f || market.getFaction().getId().equals("pirates")) {
			if (market.getFaction().getId().equals("pirates")) {
				reliefMarket = null;
			}
			
			if (reliefMarket != null) {
				SharedData.getData().getMarketsThatSentRelief().add(reliefMarket.getId(), 14f + (float) Math.random() * 14f);
				Global.getSector().reportEventStage(this, "warning_relief", reliefMarket.getPrimaryEntity(), messagePriority);
			} else {
				log.info(getLoggingId() + " No relief market found");
				applyReliefFleetDidNotMakeItConsequences();
				Global.getSector().reportEventStage(this, "relief_unavailable", messagePriority);
			}
		}
		
		if (elapsedDays > daysBeforeReliefSend && stage == 1) {
			stage++;
			if (reliefMarket != null) {
				createReliefFleet();
				if (reliefFleet == null) {
					log.info(getLoggingId() + " Failed to spawn relief fleet");
					applyReliefFleetDidNotMakeItConsequences();
					Global.getSector().reportEventStage(this, "relief_unavailable", messagePriority);
				} else {
					originalReliefFleetPoints = reliefFleet.getFleetPoints();
					
					SectorEntityToken entity = reliefMarket.getPrimaryEntity();
					reliefMarket.getPrimaryEntity().getContainingLocation().addEntity(reliefFleet);
					reliefFleet.setLocation(entity.getLocation().x, entity.getLocation().y);
					
					reliefFleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, reliefMarket.getPrimaryEntity(), 2,
							"loading food from " + reliefMarket.getName(), new Script() {
						public void run() {
							reliefFleet.getCargo().addItems(CargoItemType.RESOURCES, Commodities.FOOD, 
										reliefFleet.getCargo().getMaxCapacity() * (1f - FleetFactory.SUPPLIES_FRACTION));
						}
					});
					reliefFleet.addAssignment(FleetAssignment.DELIVER_RESOURCES, market.getPrimaryEntity(), 1000,
											  "delivering food relief from " + reliefMarket.getName() + " to " + market.getName());
					reliefFleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, market.getPrimaryEntity(), 2,
											  "offloading food at " + market.getName(), new Script() {
						public void run() {
							endEvent(Ending.RELIEF_ARRIVED);
						}
					});				
					reliefFleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, reliefMarket.getPrimaryEntity(), 1000,
											  "returning to " + reliefMarket.getName() + " after delivering food relief to " + market.getName());
					
					Global.getSector().reportEventStage(this, "relief_sent", reliefMarket.getPrimaryEntity(), messagePriority);
					log.info(getLoggingId() + " Sending relief fleet from " + reliefMarket.getName());
				}
			}
		}
		
		if (reliefFleet != null) {
			float currPoints = reliefFleet.getFleetPoints();
			if (currPoints < originalReliefFleetPoints * 0.5f) {
				Global.getSector().reportEventStage(this, "relief_aborted", reliefMarket.getPrimaryEntity(), messagePriority);
				log.info(getLoggingId() + " Relief aborted, fleet returning to " + reliefMarket.getName());
				
				reliefFleet.clearAssignments();
				reliefFleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, reliefMarket.getPrimaryEntity(), 1000);
				applyReliefFleetDidNotMakeItConsequences();
				reliefFleet = null;
			}
		}
		
		if ((elapsedDays > maxDurationDays || elapsedDays > MAX_POSSIBLE_DURATION) && stage == 2) {
			endEvent(Ending.DURATION_EXPIRED);
			stage++;
		}
	}

	
	private void applyReliefFleetDidNotMakeItConsequences() {
		float abortUnrest = getReliefAbortUnrest();
		increaseRecentUnrest((int) abortUnrest);
		log.info(" +" + abortUnrest + " unrest at " + market.getName());
	}

	private void createReliefFleet() {
		reliefFleet = FleetFactory.createEmptyFleet(reliefMarket.getFactionId(), FleetTypes.FOOD_RELIEF_FLEET, reliefMarket);

		int size = market.getSize();
		float combat = size;
		float freighter = size * 1.5f;
		float transport = size * 0.25f;
		combat *= 5;
		freighter *= 3;
		transport *= 3;
		reliefFleet = FleetFactoryV3.createFleet(new FleetParamsV3(
				reliefMarket, 
				FleetTypes.FOOD_RELIEF_FLEET,
				combat, // combatPts
				freighter, // freighterPts 
				0f, // tankerPts
				transport, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
				));
	}
	
	
	
	private void endEvent(Ending ending) {
		if (market != null) {
			market.removeCondition(Conditions.EVENT_FOOD_SHORTAGE);
		}
		ended = true;
		CommodityOnMarketAPI com = market.getCommodityData(Commodities.FOOD);
		com.setStockpile(com.getStockpile() / getStockpileMult());
		com.getPlayerSupplyPriceMod().unmodifyMult(PRICE_MOD_ID);
		com.getPlayerSupplyPriceMod().unmodifyFlat(PRICE_MOD_ID);
		
		// moved to FoodShortage condition
//		String sellId = Stats.getPlayerSellImpactMultId(Commodities.FOOD);
//		market.getStats().getDynamic().getStat(sellId).unmodify(getId() + "_mod");
		
		float ongoingStabilityImpact = getStabilityImpact();
		
		Misc.unsetAll("$market.foodShortage", MemKeys.MARKET, market.getMemoryWithoutUpdate());
		final InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
		switch (ending) {
		case DURATION_EXPIRED:
			Global.getSector().reportEventStage(this, "end_expired", messagePriority);
			log.info(getLoggingId() + " Expired");
			ongoingStabilityImpact += getAddedExpiredUnrest();
			market.getMemoryWithoutUpdate().set("$foodShortageExpired", true, 10);
			break;
		case PLAYER_ENDED:
			if (dialog != null && dialog.getPlugin().getMemoryMap() != null && dialog.getTextPanel() != null) {
				dialog.getTextPanel().addParagraph("Financial transaction confirmed", Global.getSettings().getColor("buttonText"));
			}
			if (elapsedDays < 7) {
				ongoingStabilityImpact = 0;
				Global.getSector().reportEventStage(this, "end_player_fast", null, MessagePriority.DELIVER_IMMEDIATELY,
						new BaseOnMessageDeliveryScript() {
					public void beforeDelivery(CommMessageAPI message) {
						Global.getSector().adjustPlayerReputation(
								new RepActionEnvelope(RepActions.FOOD_SHORTAGE_PLAYER_ENDED_FAST, market, message,
													  dialog.getTextPanel(), true), 
								market.getFaction().getId());
					}
				});
				log.info(getLoggingId() + " Ended by player within 7 days");
				market.getMemoryWithoutUpdate().set("$foodShortageEndedByPlayerFast", true, 10);
				//market.getFaction().adjustRelationship(Factions.PLAYER, RepRewards.HIGH);
//				Global.getSector().adjustPlayerReputation(
//						new RepActionEnvelope(RepActions.FOOD_SHORTAGE_PLAYER_ENDED_FAST, market), 
//						market.getFaction().getId());
			} else {
				ongoingStabilityImpact = Math.max(0, ongoingStabilityImpact - getReducedEndedUnrest());
				Global.getSector().reportEventStage(this, "end_player", null, MessagePriority.DELIVER_IMMEDIATELY,
						new BaseOnMessageDeliveryScript() {
					public void beforeDelivery(CommMessageAPI message) {
						Global.getSector().adjustPlayerReputation(
								new RepActionEnvelope(RepActions.FOOD_SHORTAGE_PLAYER_ENDED_NORMAL, market, message, 
													  dialog.getTextPanel(), true), 
								market.getFaction().getId());
					}
				});
				log.info(getLoggingId() + " Ended by player"); 
				market.getMemoryWithoutUpdate().set("$foodShortageEndedByPlayer", true, 10);
			}
			if (dialog != null && dialog.getPlugin().getMemoryMap() != null) {
				dialog.getVisualPanel().hideCore();
				FireBest.fire(null, dialog, dialog.getPlugin().getMemoryMap(), "FoodShortageEndedByPlayerSale");
			}
			break;
		case PLAYER_ENDED_BLACK:
			if (dialog != null && dialog.getPlugin().getMemoryMap() != null && dialog.getTextPanel() != null) {
				dialog.getTextPanel().addParagraph("Financial transaction confirmed", Global.getSettings().getColor("buttonText"));
			}
			ongoingStabilityImpact = ongoingStabilityImpact + getEndBlackUnrest();
			Global.getSector().reportEventStage(this, "end_player_black", null, MessagePriority.DELIVER_IMMEDIATELY,
					new BaseOnMessageDeliveryScript() {
				public void beforeDelivery(CommMessageAPI message) {
//					Global.getSector().adjustPlayerReputation(
//							new RepActionEnvelope(RepActions.FOOD_SHORTAGE_PLAYER_ENDED_MEDIUM_CONTRIB, market, message, 
//												  dialog.getTextPanel(), true), 
//							market.getFaction().getId());
				}
			});
			log.info(getLoggingId() + " Ended by player black market trade"); 
			market.getMemoryWithoutUpdate().set("$foodShortageEndedByPlayerBlack", true, 10);
			if (dialog != null && dialog.getPlugin().getMemoryMap() != null) {
				dialog.getVisualPanel().hideCore();
				FireBest.fire(null, dialog, dialog.getPlugin().getMemoryMap(), "FoodShortageEndedByPlayerSale");
			}
			break;
		case RELIEF_ARRIVED:
			Global.getSector().reportEventStage(this, "end_relief_arrived", null, messagePriority);
			ongoingStabilityImpact = Math.max(0, ongoingStabilityImpact - getReducedEndedUnrest());
			log.info(getLoggingId() + " Ended by relief fleet arrival");
			market.getMemoryWithoutUpdate().set("$foodShortageEndedByNPC", true, 10);
			break;
		}
		
		for (SubmarketAPI sub : market.getSubmarketsCopy()) {
			if (sub.getPlugin().isFreeTransfer()) continue;
			
			float food = sub.getCargo().getQuantity(CargoItemType.RESOURCES, Commodities.FOOD);
			sub.getCargo().removeItems(CargoItemType.RESOURCES, Commodities.FOOD, food);
		}
		
		increaseRecentUnrest((int) ongoingStabilityImpact);
		log.info(getLoggingId() + " Unrest +" + (int)ongoingStabilityImpact);

	}

	public boolean isDone() {
		return ended;
	}

	public MessagePriority getWarningWhenPossiblePriority() {
		return messagePriority;
	}
	
	public MessagePriority getWarningWhenLikelyPriority() {
		return messagePriority;
	}

	public String getStageIdForLikely() {
		return "likely";
	}

	public String getStageIdForPossible() {
		return "possible";
	}

	
//	private float updatePurchaseListAndReturnTotalBought() {
//		CampaignClockAPI clock = Global.getSector().getClock();
//		float maxDays = 30f;
//		float total = 0f;
//		List<PlayerFoodTransaction> remove = new ArrayList<PlayerFoodTransaction>();
//		for (PlayerFoodTransaction p : preEventTransactions) {
//			float elapsed = clock.getElapsedDaysSince(p.timestamp);
//			if (elapsed > maxDays) {
//				remove.add(p);
//			} else {
//				total += p.quantity;
//			}
//		}
//		preEventTransactions.removeAll(remove);
//		
//		return total;
//	}
	
	@Override
	public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
		if (!isEventStarted()) return;
		
		if (market == null) return;
		if (transaction.getMarket() != market) return;
		if (isDone()) return;
		
//		if (market.getId().equals("jangala")) {
//			System.out.println("123sfe");
//		}
		
		SubmarketPlugin plugin = transaction.getSubmarket().getPlugin();
		if (!plugin.isParticipatesInEconomy()) return;
		
		float bought = transaction.getQuantityBought(Commodities.FOOD);
		float sold = transaction.getQuantitySold(Commodities.FOOD);
		float netBought = bought - sold;
		
		if (netBought == 0) return;
		
//		CampaignEventManagerAPI manager = Global.getSector().getEventManager();
//		EventProbabilityAPI ep = manager.getProbability(getEventType(), getEventTarget());
		
		
		CommodityOnMarketAPI com = market.getCommodityData(Commodities.FOOD);
		float postTransactionLevel = com.getStockpile();
		
		if (plugin.isBlackMarket()) {
			netDeliveredByPlayerBlack -= bought;
			netDeliveredByPlayerBlack += sold;
		} else {
			netDeliveredByPlayerOther -= bought;
			netDeliveredByPlayerOther += sold;
		}
		
		if (postTransactionLevel >= preEventFoodLevel &&
				netDeliveredByPlayerOther + netDeliveredByPlayerBlack >= MIN_FRACTION_FOR_PLAYER_ENDING_EVENT * baseFoodToMeetShortage) {
			if (netDeliveredByPlayerBlack >= MIN_FRACTION_FOR_PLAYER_ENDING_EVENT * baseFoodToMeetShortage) {
				endEvent(Ending.PLAYER_ENDED_BLACK);
			} else {
				endEvent(Ending.PLAYER_ENDED);
			}
			// added in endEvent() before rep gain
//			InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
//			if (dialog != null && dialog.getPlugin().getMemoryMap() != null) {
//				//dialog.getTextPanel().clear();
//				//dialog.getTextPanel().addParagraph("Financial transaction confirmed", Global.getSettings().getColor("buttonText"));
//				dialog.getVisualPanel().hideCore();
//				FireBest.fire(null, dialog, dialog.getPlugin().getMemoryMap(), "FoodShortageEndedByPlayerSale");
//			}
		}
	}

	@Override
	public void reportFleetDespawned(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (fleet == reliefFleet) {
			Global.getSector().reportEventStage(this, "relief_lost", reliefMarket.getPrimaryEntity(), messagePriority);
			applyReliefFleetDidNotMakeItConsequences();
			reliefFleet = null;
		}
	}
	
	public float getRemainingFoodToMeetShortage() {
		return Math.max(0, baseFoodToMeetShortage - netDeliveredByPlayerBlack - netDeliveredByPlayerOther);
	}

	public Map<String, String> getTokenReplacements() {
		Map<String, String> map = super.getTokenReplacements();

		/*
		 $targetFaction
		 $reliefSystem
		 $reliefEntity
		 $playerName
		 */
		map.put("$targetFaction", eventTarget.getEntity().getFaction().getDisplayName());
		
		if (reliefMarket != null) {
			SectorEntityToken primary = reliefMarket.getPrimaryEntity();
			LocationAPI loc = primary.getContainingLocation();
			if (loc instanceof StarSystemAPI) {
				//map.put("$reliefSystem", ((StarSystemAPI)loc).getBaseName() + " star system");
				map.put("$reliefSystem", ((StarSystemAPI)loc).getBaseName());
			} else {
				map.put("$reliefSystem", "hyperspace");
			}
			
			map.put("$reliefMarket", primary.getName());
//			map.put("$reliefSystem", "Askonia");
//			map.put("$reliefEntity", "Volturn");
			if (reliefFleet != null) {
				float dist = Misc.getDistance(primary.getLocationInHyperspace(), market.getPrimaryEntity().getLocationInHyperspace());
				float eta = dist / reliefFleet.getFleetData().getTravelSpeed();
				 // since travel speed is units/second, not per day
				eta /= Global.getSector().getClock().getSecondsPerDay();
				eta += 2f;
				
				String s = null;
				if (eta <= 3f) {
					s = "within a few days";
				} else if (eta <= 7) {
					s = "within a week";
				} else if (eta <= 14) {
					s = "within a couple of weeks";
				} else if (eta <= 30) {
					s = "within a month";
				} else {
					s = "in the coming months";
				}
				map.put("$eta", s);
			}
		}
		
		int needed = (int) Misc.getRounded(getRemainingFoodToMeetShortage());
		map.put("$neededFood", "" + needed);
		
		map.put("$stabilityPenalty", "" + (int) getStabilityImpact());
		map.put("$stabilityPenaltyRelief", "" + (int) getReliefAbortUnrest());
		map.put("$stabilityPenaltyExpire", "" + (int) (getAddedExpiredUnrest() + getStabilityImpact()));
		map.put("$stabilityPenaltyEnd", "" + (int) Math.max(0, getStabilityImpact() - getReducedEndedUnrest()));
		map.put("$stabilityPenaltyEndBlack", "" + (int) (getStabilityImpact() + getEndBlackUnrest()));
		return map;
	}

	@Override
	public String[] getHighlights(String stageId) {
		List<String> result = new ArrayList<String>();
		if ("start".equals(stageId)) {
			addTokensToList(result, "$stabilityPenalty", "$neededFood");
		}
		if ("relief_unavailable".equals(stageId)) {
			addTokensToList(result, "$stabilityPenaltyRelief");
		}
		if ("relief_aborted".equals(stageId)) {
			addTokensToList(result, "$stabilityPenaltyRelief");
		}
		if ("relief_lost".equals(stageId)) {
			addTokensToList(result, "$stabilityPenaltyRelief");
		}
		if ("end_relief_arrived".equals(stageId)) {
			addTokensToList(result, "$stabilityPenaltyEnd");
		}
		if ("end_expired".equals(stageId)) {
			addTokensToList(result, "$stabilityPenaltyExpire");
		}
		if ("end_player".equals(stageId)) {
			addTokensToList(result, "$stabilityPenaltyEnd");
		}
		if ("end_player_black".equals(stageId)) {
			addTokensToList(result, "$stabilityPenaltyEndBlack");
		}
		if ("end_player_fast".equals(stageId)) {
		}
		return result.toArray(new String[0]);
	}
	
	@Override
	public Color[] getHighlightColors(String stageId) {
		return super.getHighlightColors(stageId);
//		String [] highlights = getHighlights(stageId);
//		if (highlights != null) {
//			Color c = Global.getSettings().getColor("buttonShortcut");
//			Color [] colors = new Color[highlights.length];
//			Arrays.fill(colors, c);
//			return colors;
//		}
//		//return null;
//		return null;
	}
	
	@Override
	public List<String> getRelatedCommodities() {
		List<String> commodities = new ArrayList<String>();
		commodities.add(Commodities.FOOD);
		return commodities;
	}
	
	@Override
	public List<PriceUpdatePlugin> getPriceUpdates() {
		List<PriceUpdatePlugin> updates = new ArrayList<PriceUpdatePlugin>();
		updates.add(new PriceUpdate(market.getCommodityData(Commodities.FOOD)));
		return updates;
	}
	
	public String getEventName() {
		if (!isEventStarted()) {
			return "Possible food shortage - " + market.getName() + "";
		}
		if (isDone()) {
			return "Food shortage - " + market.getName() + " (over)";
		}
		return "Food shortage - " + market.getName() + "";
	}
}




