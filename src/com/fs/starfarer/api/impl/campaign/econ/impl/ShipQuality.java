package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI.EconomyUpdateListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class ShipQuality implements EconomyUpdateListener {

	public static final String KEY = "$core_shipQualityManager";
	
	public static ShipQuality getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		if (test == null) {
			test = new ShipQuality();
			Global.getSector().getMemoryWithoutUpdate().set(KEY, test);
		}
		return (ShipQuality) test; 
	}
	
	
	public static class QualityData {
		public String econGroup;
		public FactionAPI faction;
		public StatBonus quality = new StatBonus();
		
		
		public MarketAPI market;
		public int prod;
		public float qMod = 0f;
		public QualityData(String econGroup, FactionAPI faction) {
			this.econGroup = econGroup;
			this.faction = faction;
		}
	}
	
	protected transient Map<String, QualityData> data = new HashMap<String, QualityData>();
	
	protected Object readResolve() {
		data = new HashMap<String, QualityData>();
		return this;
	}
	
	public ShipQuality() {
		Global.getSector().getEconomy().addUpdateListener(this);
	}
	
	public String getKey(MarketAPI market) {
		return market.getFactionId() + "_" + market.getEconGroup();
	}
	
	public void economyUpdated() {
		// use highest in-faction export, tie-break with quality
		// need to consider different econ groups separately CommodityMarketDataAPI
		// even a ship production of 1 unit in-faction is enough 
		// to get in-faction rather than imported hulls and quality
		
		data.clear();
		
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
//			if (market.getName().equals("Stral")) {
//				System.out.println("wefwef");
//			}
			
			QualityData d = getQualityData(market);
			
			CommodityOnMarketAPI com = market.getCommodityData(Commodities.SHIPS);
			int prod = Math.min(com.getAvailable(), com.getMaxSupply());
//			if (com.getCommodityMarketData() == null) {
//				System.out.println("wfwefew");
//			}
//			if (market.isPlayerOwned()) {
//				System.out.println("wefwefwe");
//			}
//			if (market.getName().contains("Kapteyn")) {
//				System.out.println("wefwefwe");
//			}
			int inFactionShipping = com.getCommodityMarketData().getMaxShipping(market, true);
			prod = Math.min(prod, inFactionShipping);
			prod = Math.max(Math.min(com.getAvailable(), com.getMaxSupply()), prod);
			if (prod >= d.prod && prod > 0) {
				float q = market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).computeEffective(0f);
				if (q >= d.qMod || prod > d.prod) {
					d.prod = prod;
					d.qMod = q;
					d.market = market;
					d.quality = market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD);
				}
			}
		}
	}
	
	public static float IMPORT_PENALTY = Global.getSettings().getFloat("qualityPenaltyForImports");
	
	public QualityData getQualityData(MarketAPI market) {
		String key = getKey(market);
		QualityData d = data.get(key);
		if (d == null) {
//			if (market.getName().equals("Rosas")) {
//				System.out.println("wefwef");
//			}
			d = new QualityData(market.getEconGroup(), market.getFaction());
			if (!market.isHidden()) {
				d.quality.modifyFlat("no_prod_penalty", IMPORT_PENALTY, "Cross-faction imports");
			}
			//d.market = market; // doesn't matter which market, just needs to have a market to show up in tooltip
			d.prod = -1;
			d.qMod = -1;
			data.put(key, d);
		}
		return d;
	}
	
	public void commodityUpdated(String commodityId) {
		
	}

	public boolean isEconomyListenerExpired() {
		return false;
	}
	
	
	public static float getShipQuality(MarketAPI market, String factionId) {
		float quality = 0f;
		
		if (market != null) {
			QualityData d = getInstance().getQualityData(market);
			quality = d.quality.computeEffective(0f);
			quality += market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).computeEffective(0f);
		}
		
		if (factionId != null) {
			if (market != null && market.getFaction() != null) {
				quality -= market.getFaction().getDoctrine().getShipQualityContribution();
			}
			quality += Global.getSector().getFaction(factionId).getDoctrine().getShipQualityContribution();
		}
		
		return quality;
	}
}















