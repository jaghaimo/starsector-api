package com.fs.starfarer.api.campaign;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.shared.PlayerTradeDataForSubmarket;

public class PlayerMarketTransaction {

	public static class ShipSaleInfo {
		private FleetMemberAPI member;
		private float price;
		public ShipSaleInfo(FleetMemberAPI member, float price) {
			this.member = member;
			this.price = price;
		}
		public FleetMemberAPI getMember() {
			return member;
		}
		public void setMember(FleetMemberAPI member) {
			this.member = member;
		}
		public float getPrice() {
			return price;
		}
		public void setPrice(float price) {
			this.price = price;
		}
	}
	
	public static enum LineItemType {
		BOUGHT,
		SOLD,
		UPDATE,
	}
	public static class TransactionLineItem {
		private String id;
		private LineItemType itemType;
		private CargoItemType cargoType;
		private SubmarketAPI submarket;
		private float quantity;
		private float price;
		private float tariff;
		private float demandPrice;
		private float demandTariff;
		private long timestamp;
		public TransactionLineItem(String id, LineItemType itemType,
				CargoItemType cargoType, SubmarketAPI submarket,
				float quantity, float price, float tariff, long timestamp) {
			this.id = id;
			this.itemType = itemType;
			this.cargoType = cargoType;
			this.submarket = submarket;
			this.quantity = quantity;
			this.price = price;
			this.tariff = tariff;
			this.timestamp = timestamp;
		}
		/**
		 * Only set for an "UPDATE", which assumes that the price/tariff pair is for the supply prices.
		 * @return
		 */
		public float getDemandPrice() {
			return demandPrice;
		}
		public void setDemandPrice(float price2) {
			this.demandPrice = price2;
		}
		public float getDemandTariff() {
			return demandTariff;
		}
		public void setDemandTariff(float demandTariff) {
			this.demandTariff = demandTariff;
		}
		public float getTariff() {
			return tariff;
		}
		public void setTariff(float tariff) {
			this.tariff = tariff;
		}
		public SubmarketAPI getSubmarket() {
			return submarket;
		}
		public void setSubmarket(SubmarketAPI submarket) {
			this.submarket = submarket;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public LineItemType getItemType() {
			return itemType;
		}
		public void setItemType(LineItemType itemType) {
			this.itemType = itemType;
		}
		public CargoItemType getCargoType() {
			return cargoType;
		}
		public void setCargoType(CargoItemType cargoType) {
			this.cargoType = cargoType;
		}
		public float getQuantity() {
			return quantity;
		}
		public void setQuantity(float quantity) {
			this.quantity = quantity;
		}
		public float getPrice() {
			return price;
		}
		public void setPrice(float price) {
			this.price = price;
		}
		public long getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
	}
	
	private float creditValue;
	private CargoAPI bought = Global.getFactory().createCargo(true);
	private CargoAPI sold = Global.getFactory().createCargo(true);
	
	private List<TransactionLineItem> lineItems = new ArrayList<TransactionLineItem>();
	
	private MarketAPI market;
	private SubmarketAPI submarket;
	
	private List<ShipSaleInfo> shipsBought = new ArrayList<ShipSaleInfo>();
	private List<ShipSaleInfo> shipsSold = new ArrayList<ShipSaleInfo>();
	private final CoreUITradeMode tradeMode;
	
	public PlayerMarketTransaction(MarketAPI market, SubmarketAPI submarket, CoreUITradeMode tradeMode) {
		this.market = market;
		this.submarket = submarket;
		this.tradeMode = tradeMode;
	}
	
	
	public CoreUITradeMode getTradeMode() {
		return tradeMode;
	}
	
	/**
	 * Only set for commodities (and possibly weapons) in non-free transactions.
	 * @return
	 */
	public List<TransactionLineItem> getLineItems() {
		return lineItems;
	}

	public float getBaseTradeValueImpact() {
		float total = 0;
		for (CargoStackAPI stack : bought.getStacksCopy()) {
			float qty = stack.getSize();
//			if (qty < 1) continue;
//			float val = (float) stack.getBaseValuePerUnit() * qty * playerImpactMult;
			float val = PlayerTradeDataForSubmarket.computeImpactOfHavingAlreadyBought(market, 
								stack.getType(), stack.getData(), stack.getBaseValuePerUnit(), qty);
			total += val;
		}
		
		for (CargoStackAPI stack : sold.getStacksCopy()) {
			float qty = stack.getSize();
//			if (qty < 1) continue;
//			float val = (float) stack.getBaseValuePerUnit() * qty * playerImpactMult;
//			if (!market.hasCondition(Conditions.FREE_PORT) &&
//					stack.isCommodityStack() && market.isIllegal(stack.getCommodityId())) {
//				val *= illegalImpactMult;
//			}
			float val = PlayerTradeDataForSubmarket.computeImpactOfHavingAlreadySold(market, 
					stack.getType(), stack.getData(), stack.getBaseValuePerUnit(), qty);			
			total += val;
		}
		
		return total;
	}
	
	
	/**
	 * Positive if the player received money, negative if they paid money.
	 * Only the net, so it's possible to have a very small value for a very large transaction.
	 * @return
	 */
	public float getCreditValue() {
		return creditValue;
	}
	public void setCreditValue(float creditValue) {
		this.creditValue = creditValue;
	}
	public CargoAPI getBought() {
		return bought;
	}
	public void setBought(CargoAPI bought) {
		this.bought = bought;
	}
	public CargoAPI getSold() {
		return sold;
	}
	public void setSold(CargoAPI sold) {
		this.sold = sold;
	}
	public MarketAPI getMarket() {
		return market;
	}
	public SubmarketAPI getSubmarket() {
		return submarket;
	}

	public float getQuantityBought(String commodityId) {
//		float total = 0f;
//		for (CargoStackAPI stack : getBought().getStacksCopy()) {
//			String id = stack.getCommodityId();
//			if (id != null && id.equals(commodityId)) total += stack.getSize();
//		}
//		return total;
		return getBought().getQuantity(CargoItemType.RESOURCES, commodityId);
	}
	public float getQuantitySold(String commodityId) {
//		float total = 0f;
//		for (CargoStackAPI stack : getSold().getStacksCopy()) {
//			String id = stack.getCommodityId();
//			if (id != null && id.equals(commodityId)) total += stack.getSize();
//		}
		return getSold().getQuantity(CargoItemType.RESOURCES, commodityId);
	}

	public List<ShipSaleInfo> getShipsBought() {
		return shipsBought;
	}

	public List<ShipSaleInfo> getShipsSold() {
		return shipsSold;
	}

	
}





