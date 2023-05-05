/**
 * 
 */
package com.fs.starfarer.api.impl.campaign.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction.ShipSaleInfo;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin.ShipSalesData;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class PlayerTradeDataForSubmarket {
	
	private Map<String, MutableStatWithTempMods> tx = new HashMap<String, MutableStatWithTempMods>();
	
	private CargoAPI playerBought, playerSold;
	private float accumulatedPlayerTradeValueForPositive = 0;
	private float accumulatedPlayerTradeValueForNegative = 0;
	private float totalPlayerTradeValue = 0;
	
	private IntervalUtil tracker;
	
	private Map<String, ShipSalesData> playerBoughtShips = new LinkedHashMap<String, ShipSalesData>();
	private Map<String, ShipSalesData> playerSoldShips = new LinkedHashMap<String, ShipSalesData>();
	private MarketAPI market;
	private SubmarketAPI submarket;
	
	public PlayerTradeDataForSubmarket(SubmarketAPI submarket) {
		this.market = submarket.getMarket();
		this.submarket = submarket;
		
		playerBought = Global.getFactory().createCargo(true);
		playerSold = Global.getFactory().createCargo(true);
		
		tracker = Misc.createEconIntervalTracker();
	}
	
	protected Object readResolve() {
		if (tx == null) {
			tx = new HashMap<String, MutableStatWithTempMods>();
		}
		return this;
	}
	
	public static String getTXId(CargoStackAPI stack) {
		return stack.getType().name() + "_" + stack.getData().toString();
	}
	
	public static String getTXId(ShipSalesData data) {
		return data.getVariantId();
	}
	
	public MutableStatWithTempMods getStat(String id) {
		MutableStatWithTempMods stat = tx.get(id);
		if (stat == null) {
			stat = new MutableStatWithTempMods(0);
			tx.put(id, stat);
		}
		return stat;
	}
	
	public void advance(float days) {
		tracker.advance(days);
		if (tracker.intervalElapsed()) {
			//float factor = 1f - Misc.getGenericRollingAverageFactor();
			float factor = 0.5f;
			
			for (CargoStackAPI stack : playerBought.getStacksCopy()) {
				stack.setSize(stack.getSize() * factor);
				if (stack.getSize() < 10) {
					stack.setSize(0);
				}
			}
			playerBought.removeEmptyStacks();
			for (CargoStackAPI stack : playerSold.getStacksCopy()) {
				stack.setSize(stack.getSize() * factor);
				if (stack.getSize() < 10) {
					stack.setSize(0);
				}
			}
			playerSold.removeEmptyStacks();
			
			//System.out.println("Ships: " + (playerBoughtShips.size() + playerSoldShips.size()));
//			if (playerSold.getMothballedShips() != null) {
//				System.out.println("Ships: " + playerSold.getMothballedShips().getNumMembers());
//			}
			
			List<String> remove = new ArrayList<String>();
			for (ShipSalesData data : playerBoughtShips.values()) {
				data.setNumShips(data.getNumShips() * factor);
				data.setTotalValue(data.getTotalValue() * factor);
				if (data.getNumShips() < 0.2f) remove.add(data.getVariantId());
			}
			for (String vid : remove) {
				playerBoughtShips.remove(vid);
			}
			remove.clear();
			
			for (ShipSalesData data : playerSoldShips.values()) {
				data.setNumShips(data.getNumShips() * factor);
				data.setTotalValue(data.getTotalValue() * factor);
				if (data.getNumShips() < 0.2f) remove.add(data.getVariantId());
			}
			for (String vid : remove) {
				playerSoldShips.remove(vid);
			}
			remove.clear();
			
			
			accumulatedPlayerTradeValueForPositive *= factor;
			accumulatedPlayerTradeValueForNegative *= factor;
			totalPlayerTradeValue *= factor;
		}
	}

	
	public void addTransaction(PlayerMarketTransaction transaction) {
		for (CargoStackAPI stack : transaction.getSold().getStacksCopy()) {
			addToTrackedPlayerSold(stack);
		}
		for (CargoStackAPI stack : transaction.getBought().getStacksCopy()) {
			addToTrackedPlayerBought(stack);
		}
		for (ShipSaleInfo info : transaction.getShipsBought()) {
			addToTrackedPlayerBought(info);
		}
		for (ShipSaleInfo info : transaction.getShipsSold()) {
			addToTrackedPlayerSold(info);
		}
	}
	
	private float getTransponderMult() {
		boolean tOn = Global.getSector().getPlayerFleet().isTransponderOn();
		float mult = 1f;
		if (!tOn) {
			//mult = 0.25f;
			mult = Global.getSettings().getFloat("transponderOffMarketAwarenessMult");
		}
		return mult;
	}
	
	public void addToTrackedPlayerBought(ShipSaleInfo info) {
		String vid = info.getMember().getVariant().getHullSpec().getHullId();
		ShipSalesData bought = getBoughtShipData(vid);
		ShipSalesData sold = getSoldShipData(vid);
		
		float playerImpactMult = Global.getSettings().getFloat("economyPlayerTradeImpactMult");
		
		float fractionBought = 1f;
		if (sold.getNumShips() > 0) {
			fractionBought = Math.max(0, fractionBought - sold.getNumShips());
			sold.setNumShips(sold.getNumShips() - 1f);
			sold.setTotalValue(sold.getTotalValue() - (1f - fractionBought) * info.getPrice() * playerImpactMult);
			if (sold.getNumShips() < 0) sold.setNumShips(0);
			if (sold.getTotalValue() < 0) sold.setTotalValue(0);
		}
		
		if (fractionBought > 0) {
			accumulatedPlayerTradeValueForPositive += info.getPrice() * playerImpactMult * fractionBought * getTransponderMult();
			accumulatedPlayerTradeValueForNegative += info.getPrice() * playerImpactMult * fractionBought * getTransponderMult();
			totalPlayerTradeValue += info.getPrice() * playerImpactMult * fractionBought * getTransponderMult();
			bought.setNumShips(bought.getNumShips() + 1f * fractionBought);
			bought.setTotalValue(bought.getTotalValue() + info.getPrice() * playerImpactMult * fractionBought);
		}
	}
	
	public void addToTrackedPlayerSold(ShipSaleInfo info) {
		String vid = info.getMember().getVariant().getHullSpec().getHullId();
		ShipSalesData bought = getBoughtShipData(vid);
		ShipSalesData sold = getSoldShipData(vid);
		
		float playerImpactMult = Global.getSettings().getFloat("economyPlayerTradeImpactMult");
		
		float fractionSold = 1f;
		if (bought.getNumShips() > 0) {
			fractionSold = Math.max(0, fractionSold - bought.getNumShips());
			bought.setNumShips(bought.getNumShips() - 1f);
			bought.setTotalValue(bought.getTotalValue() - (1f - fractionSold) * info.getPrice() * playerImpactMult);
			if (bought.getNumShips() < 0) bought.setNumShips(0);
			if (bought.getTotalValue() < 0) bought.setTotalValue(0);
		}
		
		if (fractionSold > 0) {
			accumulatedPlayerTradeValueForPositive += info.getPrice() * playerImpactMult * fractionSold * getTransponderMult();
			accumulatedPlayerTradeValueForNegative += info.getPrice() * playerImpactMult * fractionSold * getTransponderMult();
			totalPlayerTradeValue += info.getPrice() * playerImpactMult * fractionSold * getTransponderMult();
			sold.setNumShips(sold.getNumShips() + 1f * fractionSold);
			sold.setTotalValue(sold.getTotalValue() + info.getPrice() * playerImpactMult * fractionSold);
		}
	}
	
	public void addToTrackedPlayerBought(CargoStackAPI stack) {
		float qty = stack.getSize() - playerSold.getQuantity(stack.getType(), stack.getData());
		float impact = computeImpactOfHavingAlreadyBought(market, 
				stack.getType(), stack.getData(), stack.getBaseValuePerUnit(), qty);
		accumulatedPlayerTradeValueForPositive += impact * getTransponderMult();
		accumulatedPlayerTradeValueForNegative += impact * getTransponderMult();
		totalPlayerTradeValue += impact * getTransponderMult();
		
		playerBought.addItems(stack.getType(), stack.getData(), stack.getSize());
		playerSold.removeItems(stack.getType(), stack.getData(), stack.getSize());
		
		if (qty >= 1 && stack.getType() == CargoItemType.RESOURCES) {
			PlayerTradeProfitabilityData data = SharedData.getData().getPlayerActivityTracker().getProfitabilityData();
			float price = computePriceOfHavingAlreadyBought(market, 
						stack.getType(), stack.getData(), stack.getBaseValuePerUnit(), qty);
			data.reportNetBought((String)stack.getData(), qty, price);
		}
	}

	public void addToTrackedPlayerSold(CargoStackAPI stack) {
		addToTrackedPlayerSold(stack, -1);
	}
	
	public void addToTrackedPlayerSold(CargoStackAPI stack, float totalPriceOverride) {
		float qty = stack.getSize() - playerBought.getQuantity(stack.getType(), stack.getData());
		float impact = computeImpactOfHavingAlreadySold(market, 
				stack.getType(), stack.getData(), stack.getBaseValuePerUnit(), qty);
		
		playerSold.addItems(stack.getType(), stack.getData(), stack.getSize());
		playerBought.removeItems(stack.getType(), stack.getData(), stack.getSize());
		
		float overrideImpactMult = 1f;
		if (qty >= 1 && stack.getType() == CargoItemType.RESOURCES) {
			PlayerTradeProfitabilityData data = SharedData.getData().getPlayerActivityTracker().getProfitabilityData();
			float price = computePriceOfHavingAlreadySold(market, 
						stack.getType(), stack.getData(), stack.getBaseValuePerUnit(), qty);
			if (totalPriceOverride > 0) {
				if (price > 0) {
					overrideImpactMult = totalPriceOverride / price;
				}
				price = totalPriceOverride;
			}
			
//			String multId = Stats.getPlayerTradeImpactMultId(commodityId);
//			val *= market.getStats().getDynamic().getValue(multId);
//			multId = Stats.getPlayerSellImpactMultId(commodityId);
//			val *= market.getStats().getDynamic().getValue(multId);
			
			data.reportNetSold((String)stack.getData(), qty, price);
		}
		
		accumulatedPlayerTradeValueForPositive += impact * getTransponderMult() * overrideImpactMult;
		accumulatedPlayerTradeValueForNegative += impact * getTransponderMult() * overrideImpactMult;
		totalPlayerTradeValue += impact * getTransponderMult() * overrideImpactMult;
	}
	
	public static float computeImpactOfHavingAlreadySold(MarketAPI market, CargoItemType type, Object data, float baseValue, float qty) {
		if (qty < 1) return 0;
		
		float playerImpactMult = Global.getSettings().getFloat("economyPlayerTradeImpactMult");
		float illegalImpactMult = Global.getSettings().getFloat("economyPlayerSellIllegalImpactMult");
		float militaryImpactMult = Global.getSettings().getFloat("economyPlayerSellMilitaryImpactMult");
		
		if (type == CargoItemType.RESOURCES) {
			String commodityId = (String) data;
			CommodityOnMarketAPI com = market.getCommodityData(commodityId);
			float val = market.getDemandPriceAssumingExistingTransaction(commodityId, qty, -qty * com.getUtilityOnMarket(), true);
			if (!market.hasCondition(Conditions.FREE_PORT) &&
					market.isIllegal(commodityId)) {
				val *= illegalImpactMult;
			}
			val *= playerImpactMult;
			
			String multId = Stats.getPlayerTradeRepImpactMultId(commodityId);
			val *= market.getStats().getDynamic().getValue(multId);
			multId = Stats.getPlayerSellRepImpactMultId(commodityId);
			val *= market.getStats().getDynamic().getValue(multId);
			
			if (com.getCommodity().hasTag(Commodities.TAG_MILITARY)) {
				val *= militaryImpactMult;
			}
			return val;
		} else {
			float val = (float) baseValue * qty * playerImpactMult;
			return val;
		}
	}
	
	public static float computePriceOfHavingAlreadySold(MarketAPI market, CargoItemType type, Object data, float baseValue, float qty) {
		if (qty < 1) return 0;
		
		if (type == CargoItemType.RESOURCES) {
			String commodityId = (String) data;
			CommodityOnMarketAPI com = market.getCommodityData(commodityId);
			float val = market.getDemandPriceAssumingExistingTransaction(commodityId, qty, -qty * com.getUtilityOnMarket(), true);
			return val;
		} else {
			float val = (float) baseValue * qty;
			return val;
		}
	}
	
	public static float computeImpactOfHavingAlreadyBought(MarketAPI market, CargoItemType type, Object data, float baseValue, float qty) {
		if (qty < 1) return 0;
		
		float playerImpactMult = Global.getSettings().getFloat("economyPlayerTradeImpactMult");
//		market.getSupplyPriceAssumingExistingTransaction(commodityId, qty, qty * com.getUtilityOnMarket(), true);
//		market.getSupplyPriceAssumingExistingTransaction(commodityId, qty, 1, true);
		if (type == CargoItemType.RESOURCES) {
			String commodityId = (String) data;
			CommodityOnMarketAPI com = market.getCommodityData(commodityId);
			float val = market.getSupplyPriceAssumingExistingTransaction(commodityId, qty, qty * com.getUtilityOnMarket(), true);
			val *= playerImpactMult;
			
			String multId = Stats.getPlayerTradeRepImpactMultId(commodityId);
			val *= market.getStats().getDynamic().getValue(multId);
			multId = Stats.getPlayerBuyRepImpactMultId(commodityId);
			val *= market.getStats().getDynamic().getValue(multId);
			return val;
		} else {
			float val = (float) baseValue * qty * playerImpactMult;
			return val;
		}
	}
	
	public static float computePriceOfHavingAlreadyBought(MarketAPI market, CargoItemType type, Object data, float baseValue, float qty) {
		if (qty < 1) return 0;
		
		if (type == CargoItemType.RESOURCES) {
			String commodityId = (String) data;
			CommodityOnMarketAPI com = market.getCommodityData(commodityId);
			float val = market.getSupplyPriceAssumingExistingTransaction(commodityId, qty, qty * com.getUtilityOnMarket(), true);
			return val;
		} else {
			float val = (float) baseValue * qty;
			return val;
		}
	}

	
	public float getTotalPlayerTradeValue() {
		return totalPlayerTradeValue;
	}

	public void setTotalPlayerTradeValue(float totalPlayerTradeValue) {
		this.totalPlayerTradeValue = totalPlayerTradeValue;
	}

	public CargoAPI getRecentPlayerBought() {
		return playerBought;
	}

	public CargoAPI getRecentPlayerSold() {
		return playerSold;
	}

	public float getAccumulatedPlayerTradeValueForPositive() {
		return accumulatedPlayerTradeValueForPositive;
	}
	
	public void setAccumulatedPlayerTradeValueForPositive(float accumulatedPlayerTradeValue) {
		this.accumulatedPlayerTradeValueForPositive = accumulatedPlayerTradeValue;
	}

	public float getAccumulatedPlayerTradeValueForNegative() {
		return accumulatedPlayerTradeValueForNegative;
	}

	public void setAccumulatedPlayerTradeValueForNegative(
			float accumulatedPlayerTradeValueForNegative) {
		this.accumulatedPlayerTradeValueForNegative = accumulatedPlayerTradeValueForNegative;
	}

	public IntervalUtil getTracker() {
		return tracker;
	}

	public Collection<ShipSalesData> getRecentlyPlayerBoughtShips() {
		return playerBoughtShips.values();
	}

	public Collection<ShipSalesData> getRecentlyPlayerSoldShips() {
		return playerSoldShips.values();
	}

	public MarketAPI getMarket() {
		return market;
	}

	public SubmarketAPI getSubmarket() {
		return submarket;
	}
	
	
	protected ShipSalesData getSoldShipData(String vid) {
		ShipSalesData sold = playerSoldShips.get(vid);
		if (sold == null) {
			sold = new ShipSalesData();
			sold.setVariantId(vid);
			playerSoldShips.put(vid, sold);
		}
		return sold;
	}
	
	protected ShipSalesData getBoughtShipData(String vid) {
		ShipSalesData bought = playerBoughtShips.get(vid);
		if (bought == null) {
			bought = new ShipSalesData();
			bought.setVariantId(vid);
			playerBoughtShips.put(vid, bought);
		}
		return bought;
	}
	
	public float getRecentBaseTradeValueImpact() {
//		float playerImpactMult = Global.getSettings().getFloat("economyPlayerTradeImpactMult");
//		float illegalImpactMult = Global.getSettings().getFloat("economyPlayerSellIllegalImpactMult");
		
		float total = 0;
		for (CargoStackAPI stack : playerBought.getStacksCopy()) {
			float qty = stack.getSize();
//			if (qty < 1) continue;
//			float val = (float) stack.getBaseValuePerUnit() * qty * playerImpactMult;
			float val = computeImpactOfHavingAlreadyBought(market, 
									stack.getType(), stack.getData(), stack.getBaseValuePerUnit(), qty);
			total += val;
		}
		for (CargoStackAPI stack : playerSold.getStacksCopy()) {
			float qty = stack.getSize();
			if (qty < 1) continue;
//			float val = (float) stack.getBaseValuePerUnit() * qty * playerImpactMult;
//			if (!market.hasCondition(Conditions.FREE_PORT) &&
//					stack.isCommodityStack() && market.isIllegal(stack.getCommodityId())) {
//				val *= illegalImpactMult;
//			}
			float val = computeImpactOfHavingAlreadySold(market, 
										stack.getType(), stack.getData(), stack.getBaseValuePerUnit(), qty);
			total += val;
		}
		return total;
	}

	public void setSubmarket(SubmarketAPI submarket) {
		this.submarket = submarket;
	}
	
}






