package com.fs.starfarer.api.impl.campaign.shared;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class PlayerTradeProfitabilityData {
	public static Logger log = Global.getLogger(PlayerTradeProfitabilityData.class);
	
	public static class CommodityData {
		private String commodityId;
		private float totalPrice;
		private float quantity;
		
		public float getQuantity() {
			return quantity;
		}
		public void setQuantity(float quantity) {
			this.quantity = quantity;
		}
		public CommodityData(String commodityId) {
			this.commodityId = commodityId;
		}
		public String getCommodityId() {
			return commodityId;
		}
		public void setCommodityId(String commodityId) {
			this.commodityId = commodityId;
		}
		public float getTotalPrice() {
			return totalPrice;
		}
		public void setTotalPrice(float totalPrice) {
			this.totalPrice = totalPrice;
		}
	}
	
	private Map<String, CommodityData> dataBought = new LinkedHashMap<String, CommodityData>();
	private IntervalUtil tracker;
	
	private long accruedXP = 0;
	
	public PlayerTradeProfitabilityData() {
		tracker = Misc.createEconIntervalTracker();
	}
	
	public void reportNetBought(String commodityId, float quantity, float totalPrice) {
		CommodityData data = getBoughtDataFor(commodityId);
		data.setQuantity(data.getQuantity() + quantity);
		data.setTotalPrice(data.getTotalPrice() + totalPrice);
	}
	
	public void reportNetSold(String commodityId, float quantity, float totalPrice) {
		CommodityData data = getBoughtDataFor(commodityId);
		if (data.getQuantity() < 1 || quantity < 1) return;
		float avgBuyPrice = data.getTotalPrice() / data.getQuantity();
		
		float net = quantity;
		if (quantity > data.getQuantity()) net = data.getQuantity();
		data.setQuantity(data.getQuantity() - net);

		if (net < 1) return;
		
		float paidForNet = avgBuyPrice * net;
		data.setTotalPrice(Math.max(0, data.getTotalPrice() - paidForNet));
		
		float receivedForNet = net * totalPrice / quantity;
		
		float profit = receivedForNet - paidForNet;
		if (profit <= 0) return;
		
		float xpPerCredit = Global.getSettings().getFloat("economyPlayerXPPerCreditOfProfit");
		
		long xp = (long) (profit * xpPerCredit);
		
		accruedXP += xp;
		
		log.info("Player accrued " + xp + " xp for selling " + commodityId + " (profit per unit: " + (int) (profit / net) + ")");
	}
	


	public void advance(float days) {
		tracker.advance(days);
		if (tracker.intervalElapsed()) {
			float factor = Misc.getGenericRollingAverageFactor();
			for (CommodityData cd : new ArrayList<CommodityData>(dataBought.values())) {
				cd.setQuantity(cd.getQuantity() * factor);
				cd.setTotalPrice(cd.getTotalPrice() * factor);
				if (cd.getQuantity() < 1) dataBought.remove(cd.getCommodityId());
			}
		}
	}
	
	public CommodityData getBoughtDataFor(String commodityId) {
		CommodityData cd = dataBought.get(commodityId);
		if (cd == null) {
			cd = new CommodityData(commodityId);
			dataBought.put(commodityId, cd);
		}
		return cd;
	}

	public long getAccruedXP() {
		return accruedXP;
	}

	public void setAccruedXP(long accruedXP) {
		this.accruedXP = accruedXP;
	}
}











