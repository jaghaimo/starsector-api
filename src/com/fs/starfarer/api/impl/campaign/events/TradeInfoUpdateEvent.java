package com.fs.starfarer.api.impl.campaign.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.campaign.events.CampaignEventPlugin.PriceUpdatePlugin.PriceType;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.SaveableIterator;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class TradeInfoUpdateEvent extends BaseEventPlugin {
	
	public static final float TIMEOUT = 30f;
	
	public static Logger log = Global.getLogger(TradeInfoUpdateEvent.class);
	
	private IntervalUtil remoteTracker;
	private IntervalUtil localTracker;
	private TimeoutTracker<String> sinceLastLocalReport = new TimeoutTracker<String>();
	private TimeoutTracker<String> sinceLastRemoteReport = new TimeoutTracker<String>();
	
	private List<PriceUpdatePlugin> updatesForNextReport = new ArrayList<PriceUpdatePlugin>();
	private SectorEntityToken commRelayForNextReport = null;
	
	public void init(String type, CampaignEventTarget eventTarget) {
		super.init(type, eventTarget);
		remoteTracker = new IntervalUtil(0.5f, 1.5f);
		localTracker = new IntervalUtil(0.5f, 1.5f);
	}
	
	public void startEvent() {
		super.startEvent();
	}
	
	public void advance(float amount) {
		//if (true) return;
		
		if (!isEventStarted()) return;
		if (isDone()) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		
		sinceLastLocalReport.advance(days);
		sinceLastRemoteReport.advance(days);
		
		localTracker.advance(days);
		if (localTracker.intervalElapsed()) {
			checkLocalPrices();
		}
		
		remoteTracker.advance(days);
		if (remoteTracker.intervalElapsed()) {
			checkRemotePrices();
		}
	}


	//private SaveableIterator<SectorEntityToken> relayIter = null;
	private SaveableIterator<StarSystemAPI> starSystemIter = null;

	private CampaignEventTarget tempTarget;

	private void checkRemotePrices() {
		
		if (starSystemIter == null || !starSystemIter.hasNext()) {
//			List<SectorEntityToken> relays = Global.getSector().getIntel().getCommSnifferLocations();
//			if (Global.getSettings().isDevMode()) {
//				relays = Global.getSector().getEntitiesWithTag(Tags.COMM_RELAY);
//			}
//			relayIter = new SaveableIterator<SectorEntityToken>(relays);
			List<StarSystemAPI> systems = new ArrayList<StarSystemAPI>(Global.getSector().getStarSystems());
			Collections.shuffle(systems);
			starSystemIter = new SaveableIterator<StarSystemAPI>(systems);
			
			float size = systems.size();
			float interval = 1.5f * TIMEOUT / size;
			remoteTracker.setInterval(interval * 0.75f,  interval * 1.25f);
		}
		if(!starSystemIter.hasNext()) return;

		
		final StarSystemAPI system = starSystemIter.next();
		//if (Global.getSector().getCurrentLocation() == system) return;
		
		
		List<SectorEntityToken> relays = system.getEntitiesWithTag(Tags.COMM_RELAY);
		List<SectorEntityToken> withIntel = Global.getSector().getIntel().getCommSnifferLocations();
		
		SectorEntityToken relay = null;
		for (SectorEntityToken curr : relays) {
			if (withIntel.contains(curr)) {
				relay = curr;
				break;
			}
		}
		
		boolean hasIntel = relay != null;
		if (relay == null && relays.size() > 0) {
			relay = relays.get(new Random().nextInt(relays.size()));
		}

		if (relay == null) return;
		
		String id = relay.getContainingLocation().getId();
		if (sinceLastRemoteReport.contains(id)) return;
		
		
		List<PriceUpdate> list = getPriceUpdatesFor(system);
		if (!list.isEmpty()) {
			
			commRelayForNextReport = relay;
			if (hasIntel) {
				pickUpdatesFrom(system, list, PickMode.REMOTE_WITH_INTEL);
				if (!updatesForNextReport.isEmpty()) {
					sinceLastRemoteReport.set(id, TIMEOUT);
//					Global.getSector().reportEventStage(this, "prices_sniffer", relay, MessagePriority.SECTOR, new BaseOnMessageDeliveryScript() {
//						public void beforeDelivery(CommMessageAPI message) {
//							if (system != Global.getSector().getPlayerFleet().getContainingLocation()) {
//								message.setShowInCampaignList(false);
//							}
//						}
//					});
				}
			} else {
				pickUpdatesFrom(system, list, PickMode.REMOTE);
				if (!updatesForNextReport.isEmpty()) {
					sinceLastRemoteReport.set(id, TIMEOUT);
//					Global.getSector().reportEventStage(this, "prices_remote", relay, MessagePriority.SECTOR, new BaseOnMessageDeliveryScript() {
//						public void beforeDelivery(CommMessageAPI message) {
//							if (system != Global.getSector().getPlayerFleet().getContainingLocation()) {
//								message.setShowInCampaignList(false);
//							}
//						}
//					});
				}
			}
		}
	}


	private void checkLocalPrices() {
		//if (Global.getSector().isInNewGameAdvance()) return;
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		if (playerFleet.isInHyperspace() || playerFleet.getContainingLocation() == null) return;
		String id = playerFleet.getContainingLocation().getId();
		if (sinceLastLocalReport.contains(id)) return;
		
		List<PriceUpdate> list = getPriceUpdatesFor(playerFleet.getContainingLocation());
		
		if (!list.isEmpty()) {
			pickUpdatesFrom(playerFleet.getContainingLocation(), list, PickMode.LOCAL);
			if (!updatesForNextReport.isEmpty()) {
				sinceLastLocalReport.set(id, TIMEOUT);
				//Global.getSector().reportEventStage(this, "prices_local", playerFleet, MessagePriority.SECTOR);
			}
		}
	}
	
	private static enum PickMode {
		LOCAL,
		REMOTE,
		REMOTE_WITH_INTEL,
	}
	
	private void pickUpdatesFrom(LocationAPI system, List<PriceUpdate> updates, PickMode mode) {
		
		float numMarkets = 0;
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.getContainingLocation() != system) continue;
			//numMarkets += market.getSize();
			numMarkets++;
			
		}
		int max = 0;
		switch (mode) {
		case LOCAL:
			//max = (int) Math.max(2, numMarkets - 2);
			max = (int) Math.max(2, numMarkets) + 1;
			break;
		case REMOTE:
			max = (int) Math.max(1, numMarkets - 2);
			break;
		case REMOTE_WITH_INTEL:
			max = (int) Math.max(1, numMarkets);
			break;
		}
		
		if (max > 5) max = 5;
		if (max < 1) max = 1;
		
		
		log.info("");
		log.info("");
		log.info("Picking " + max + " updates");
		
		WeightedRandomPicker<PriceUpdate> picker = new WeightedRandomPicker<PriceUpdate>();
		List<PriceUpdatePlugin> known = getPlayerKnownUpdates();
		for (PriceUpdate pu : updates) {
			float weight = getWeightFor(pu, known);
			if (weight <= 0) continue;
			
			log.info(pu.getCommodity().getCommodity().getName() + "(" + pu.getCommodity().getMarket().getName() + "): weight " + weight); 
			picker.add(pu, weight);
		}
		
		log.info("");
		updatesForNextReport.clear();
		for (int i = 0; i < max; i++) {
			PriceUpdate update = picker.pick();
			if (update != null) {
				log.info("Picked " + update.getCommodity().getCommodity().getName() + "(" + update.getCommodity().getMarket().getName() + ")");
				updatesForNextReport.add(update);
				picker.remove(update);
			}
		}
	}
	
	
	private void pickAllRelevantFromMarket(MarketAPI market, List<PriceUpdate> updates) {
		log.info("Picking market updates");
		
		updatesForNextReport.clear();
		List<PriceUpdatePlugin> known = getPlayerKnownUpdates();
		for (PriceUpdate update : updates) {
//			System.out.println("Checking for local update: " + update.getCommodity().getCommodity().getName());
//			if (!update.isSignificant()) continue;
//			float weight = getWeightFor(update, known);
//			log.info(update.getCommodity().getCommodity().getName() + ": weight " + (int) weight);
			//if (update.getType() != PriceType.NORMAL || weight > 100) {
			if (shouldUpdateLocally(update, known)) {
				log.info("Adding " + update.getCommodity().getCommodity().getName() + "(" + update.getCommodity().getMarket().getName() + ")");
				updatesForNextReport.add(update);
			}
		}
	}
	
	
	private List<PriceUpdate> getPriceUpdatesFor(LocationAPI system) {
		List<PriceUpdate> updates = new ArrayList<PriceUpdate>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.getContainingLocation() != system) continue;
			updates.addAll(getUpdatesFor(market));
		}
		return updates;
	}
	
	private List<PriceUpdate> getUpdatesFor(MarketAPI market) {
		List<PriceUpdate> updates = new ArrayList<PriceUpdate>();
		if (!market.isInEconomy()) {
			return updates;
		}
		for (CommodityOnMarketAPI com : market.getAllCommodities()) {
			if (com.isNonEcon()) continue;
			if (com.isPersonnel()) continue;
			float volumeFactor = com.getStockpile() + com.getDemand().getDemandValue();
			if (volumeFactor < 50) continue;
			PriceUpdate update = new PriceUpdate(com);
			if (update.isSignificant()) {
				updates.add(update);
			}
		}
		return updates;
	}
	
	private float getWeightFor(PriceUpdatePlugin update, List<PriceUpdatePlugin> known) {
		CommodityOnMarketAPI com = update.getCommodity();
		MarketAPI market = update.getMarket();
		
//		if (market.getId().contains("achaman") && com.getId().equals("food")) {
//			System.out.println("dfsdfefw2");
//		}
//		if (com.getId().equals("food")) {
//			System.out.println("dfsdfefw2");
//		}
		
		float volumeFactor = com.getStockpile() + com.getDemand().getDemandValue();
		if (volumeFactor == 0) return 0f;
		
		volumeFactor = (float) Math.sqrt(volumeFactor);
		
		//volumeFactor *= com.getCommodity().getBasePrice();
		
		//volumeFactor *= (float) market.getSize();
		
		if (update.getType() == PriceType.NORMAL) {
			//volumeFactor = (float) Math.sqrt(volumeFactor);
			volumeFactor *= 0.25f;
		}
		
		float numCheap = 0;
		float numExpensive = 0;
		float numNormal = 0;
		
		
		CampaignClockAPI clock = Global.getSector().getClock();
		float daysSinceLastSame = Float.MAX_VALUE;
		
		int numSeenSkipped = 0;
		Set<CommodityOnMarketAPI> seen = new HashSet<CommodityOnMarketAPI>();
		for (PriceUpdatePlugin curr : known) {
			CommodityOnMarketAPI currCom = curr.getCommodity();
			if (currCom == null) continue;
			if (seen.contains(currCom)) {
				numSeenSkipped++;
				continue;
			}
			seen.add(currCom);
			if (!currCom.getId().equals(com.getId())) continue;
			
			if (currCom == com) {
				float priceDiff = Math.abs(curr.getDemandPrice() + curr.getSupplyPrice() - update.getDemandPrice() - update.getSupplyPrice());
				if (priceDiff < 0.2f * (curr.getDemandPrice() + curr.getSupplyPrice())) {
					daysSinceLastSame = clock.getElapsedDaysSince(curr.getTimestamp());
				}
			}
			//clock.getElapsedDaysSince(-55661070348000L)
			switch (curr.getType()) {
			case CHEAP:
				numCheap++;
				break;
			case EXPENSIVE:
				numExpensive++;
				break;
			case NORMAL:
				numNormal++;
				break;
			}
		}
		
		if (daysSinceLastSame < 30) return 0f;
		
		if (update.getType() == PriceType.NORMAL) {
			if (numExpensive + numCheap == 0) {
				return 0f;
			}
			if (numCheap == 0 && update.getAvailable() <= 10) {
				return 0f;
			}
			if (numExpensive == 0 && update.getDemand() <= 10) {
				return 0f;
			}
		}
		
		float total = numCheap + numExpensive + numNormal;
		
		float weightMult = 1f;
		if (total <= 0) total = 1f;
		switch (update.getType()) {
		case CHEAP:
			weightMult = 1f + 3f * Math.max(0, numNormal + numExpensive - numCheap) / total;
			break;
		case EXPENSIVE:
			weightMult = 1f + 3f * Math.max(0, numNormal + numCheap - numExpensive) / total;
			
			if (!com.isFuel() && !com.isPersonnel() && !com.getId().equals(Commodities.SUPPLIES)) {
				CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
				float f = playerFleet.getCargo().getQuantity(CargoItemType.RESOURCES, com.getId()) / Math.max(playerFleet.getCargo().getMaxCapacity(), 1);
				weightMult *= (1f + 2f * f);
			}
			
			break;
		case NORMAL:
			weightMult = 1f + 1f * Math.max(0, numCheap + numExpensive - numNormal) / total;
			break;
		}
		
		return volumeFactor * weightMult;
	}
	
	
	
	private boolean shouldUpdateLocally(PriceUpdate update, List<PriceUpdatePlugin> known) {
//		if (update.getCommodity().getId().equals(Commodities.LOBSTER)) {
//			System.out.println("wfwefweew");
//		}
		if (!update.isSignificant()) return false;
		
		CommodityOnMarketAPI com = update.getCommodity();
		MarketAPI market = com.getMarket();
		StarSystemAPI system = market.getStarSystem();
		
//		float numCheap = 0;
//		float numExpensive = 0;
//		float numNormal = 0;
//		float numLocalNormal = 0;
		
		CampaignClockAPI clock = Global.getSector().getClock();
		float daysSinceLastSame = Float.MAX_VALUE;
		
		int numSeenSkipped = 0;
		Set<CommodityOnMarketAPI> seen = new HashSet<CommodityOnMarketAPI>();
		PriceUpdatePlugin mostRecent = null;
		for (PriceUpdatePlugin curr : known) {
			CommodityOnMarketAPI currCom = curr.getCommodity();
			if (currCom == null) continue;
			if (seen.contains(currCom)) {
				numSeenSkipped++;
				continue;
			}
			seen.add(currCom);
			if (!currCom.getId().equals(com.getId())) continue;
			
			if (currCom == com) {
				mostRecent = curr;
				float priceDiff = Math.abs(curr.getDemandPrice() + curr.getSupplyPrice() - update.getDemandPrice() - update.getSupplyPrice());
				if (priceDiff < 0.2f * (curr.getDemandPrice() + curr.getSupplyPrice())) {
					daysSinceLastSame = clock.getElapsedDaysSince(curr.getTimestamp());
				}
				break;
			}
			//clock.getElapsedDaysSince(-55661070348000L)
//			switch (curr.getType()) {
//			case CHEAP:
//				numCheap++;
//				break;
//			case EXPENSIVE:
//				numExpensive++;
//				break;
//			case NORMAL:
//				numNormal++;
//				if (system != null && system == curr.getMarket().getStarSystem()) {
//					numLocalNormal++;
//				}
//				break;
//			}
		}
		
		if (daysSinceLastSame < 5) return false;
		
		//boolean canSell = (int) Misc.getRounded(update.getAvailable()) >= 5;
		
		if (update.getType() != PriceType.NORMAL) {
			return true;
		}
		
		//if (mostRecent != null && mostRecent.getType() != PriceType.NORMAL && update.getType() == PriceType.NORMAL) {
		if (mostRecent != null) {
//			if (mostRecent.getType() != PriceType.NORMAL && update.getType() == PriceType.NORMAL) {
//				update.get
//			}
			return true;
		}
		
		return false;
		
//		CommodityStatTracker stats = SharedData.getData().getActivityTracker().getCommodityTracker();
//		CommodityStats cs = stats.getStats(update.getCommodity().getId());
//		
//		float numMarkets = Global.getSector().getEconomy().getMarketsCopy().size();
//		if (numMarkets < 1) return false; // ??? no markets
//		
//		if (com.getAverageStockpileAfterDemand() > cs.getTotalStockpiles() * 4f / numMarkets) {
//			return true;
//		}
//		if (com.getDemand().getDemandValue() > cs.getTotalDemand() * 1f / numMarkets) {
//			return true;
//		}
		
//		if (update.getType() == PriceType.NORMAL) {
//			if (numExpensive + numCheap == 0) return false;
//			if (numExpensive > 0 && (numCheap > 0 || numLocalNormal > 0)) return false;
//		}
//		
//		return true;
	}
	
	
	/**
	 * Some of the updates may be contradictory, but since this is used for computing
	 * weights when picking which updates to send, it's probably good enough.
	 * @return
	 */
	private List<PriceUpdatePlugin> getPlayerKnownUpdates() {
		
		CampaignClockAPI clock = Global.getSector().getClock();
		
		List<PriceUpdatePlugin> updates = new ArrayList<PriceUpdatePlugin>();
//		for (CommMessageAPI message : Global.getSector().getIntel().getMessagesCopy()) {
//			if (clock.getElapsedDaysSince(message.getTimeSent()) > 30) continue;
//			if (!message.hasTag(Tags.REPORT_PRICES)) continue;
//			
//			List<PriceUpdatePlugin> list = message.getPriceUpdates();
//			if (list == null || list.isEmpty()) continue;
//			
////			if (message.getMarket() != null && message.getMarket().getId().contains("skathi")) {
////				System.out.println("e23edfsdf");
////			}
//			for (PriceUpdatePlugin curr : list) {
//				updates.add(curr);
//			}
//		}
//		
//		Collections.sort(updates, new Comparator<PriceUpdatePlugin>() {
//			public int compare(PriceUpdatePlugin o1, PriceUpdatePlugin o2) {
//				long result = (o2.getTimestamp() - o1.getTimestamp());
//				if (result > 0)
//					return 1;
//				if (result < 0)
//					return -1;
//				return 0;
//			}
//		});
		
		
		return updates;
	}
	
	
	@Override
	public void reportPlayerOpenedMarket(MarketAPI market) {
		getLocalUpdates(market);
	}

	@Override
	public void reportPlayerClosedMarket(MarketAPI market) {
		//market.removeCondition(Conditions.EVENT_TRADE_DISRUPTION);
		//market.getCommodityData(Commodities.HEAVY_MACHINERY).getPlayerPriceMod().modifyMult("sdfsdfsd", 0.1f);
		getLocalUpdates(market);
	}
	
	protected void getLocalUpdates(MarketAPI market) {
//		float days = SharedData.getData().getPlayerActivityTracker().getDaysSinceLastVisitTo(market);
//		if (days < 3) return;
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
//		if (playerFleet.isInHyperspace()) return;
		
		List<PriceUpdate> list = getUpdatesFor(market);
		tempTarget = new CampaignEventTarget(market);
		this.market = market;
		if (!list.isEmpty()) {
			pickAllRelevantFromMarket(market, list);
			if (!updatesForNextReport.isEmpty()) {
				//Global.getSector().reportEventStage(this, "prices_market", playerFleet, MessagePriority.DELIVER_IMMEDIATELY);
			}
		}
		tempTarget = null;
		this.market = null;
	}
	


	@Override
	public List<PriceUpdatePlugin> getPriceUpdates() {
		return updatesForNextReport;
	}
	
	@Override
	public List<String> getRelatedCommodities() {
		return super.getRelatedCommodities();
	}
	

	public Map<String, String> getTokenReplacements() {
		Map<String, String> map = super.getTokenReplacements();
//		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
//		if (playerFleet.isInHyperspace()) {
//			map.put("$fromSystem", "hyperspace");
//		} else {
//			map.put("$fromSystem", ((StarSystemAPI)playerFleet.getContainingLocation()).getBaseName());
//		}
		
		if (commRelayForNextReport != null) {
			map.put("$relayName", commRelayForNextReport.getName());
			if (commRelayForNextReport.isInHyperspace()) {
				map.put("$fromSystem", "hyperspace");
			} else {
				map.put("$fromSystem", ((StarSystemAPI)commRelayForNextReport.getContainingLocation()).getBaseName());
			}
		}
		
		List<PriceUpdatePlugin> updates = getPriceUpdates();
		if (updates != null && !updates.isEmpty()) {
			String priceList = "Price information updated for: ";
			for (PriceUpdatePlugin update : updates) {
				CommodityOnMarketAPI com = update.getCommodity();
				priceList += com.getCommodity().getName()  + " (" + com.getMarket().getName() + "), ";
			}
			priceList = priceList.substring(0, priceList.length() - 2);
			priceList += ".";
			map.put("$priceList", priceList);
		}
		return map;
	}

	@Override
	public String[] getHighlights(String stageId) {
//		List<String> result = new ArrayList<String>();
//		addTokensToList(result, "$neededFood");
//		return result.toArray(new String[0]);
		return null;
	}
	
	@Override
	public Color[] getHighlightColors(String stageId) {
		return super.getHighlightColors(stageId);
	}
	
	@Override
	public CampaignEventTarget getEventTarget() {
		if (tempTarget != null) return tempTarget;
		return super.getEventTarget();
	}

	public boolean isDone() {
		return false;
	}
	
	
	@Override
	public String getEventName() {
		return "Trade info update"; // not used anywhere
	}

	@Override
	public CampaignEventCategory getEventCategory() {
		return CampaignEventCategory.DO_NOT_SHOW_IN_MESSAGE_FILTER;
	}
	
	public boolean showAllMessagesIfOngoing() {
		return false;
	}
}










