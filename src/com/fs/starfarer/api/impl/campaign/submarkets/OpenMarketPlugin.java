package com.fs.starfarer.api.impl.campaign.submarkets;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;

public class OpenMarketPlugin extends BaseSubmarketPlugin {
	
	public void init(SubmarketAPI submarket) {
		super.init(submarket);
	}


	public void updateCargoPrePlayerInteraction() {
		float seconds = Global.getSector().getClock().convertToSeconds(sinceLastCargoUpdate);
		addAndRemoveStockpiledResources(seconds, false, true, true);
		sinceLastCargoUpdate = 0f;

		if (okToUpdateShipsAndWeapons()) {
			sinceSWUpdate = 0f;

			boolean military = Misc.isMilitary(market);
			boolean hiddenBase = market.getMemoryWithoutUpdate().getBoolean(MemFlags.HIDDEN_BASE_MEM_FLAG);
			
			float extraShips = 0f;
			//int extraShipSize = 0;
			if (military && hiddenBase && !market.hasSubmarket(Submarkets.GENERIC_MILITARY)) {
				extraShips = 150f;
				//extraShipSize = 1;
			}
			
			pruneWeapons(0f);
			
			int weapons = 5 + Math.max(0, market.getSize() - 1) + (Misc.isMilitary(market) ? 5 : 0);
			int fighters = 1 + Math.max(0, (market.getSize() - 3) / 2) + (Misc.isMilitary(market) ? 2 : 0);
			
			addWeapons(weapons, weapons + 2, 0, market.getFactionId());
			addFighters(fighters, fighters + 2, 0, market.getFactionId());
			
			
			getCargo().getMothballedShips().clear();
			
			float freighters = 10f;
			CommodityOnMarketAPI com = market.getCommodityData(Commodities.SHIPS);
			freighters += com.getMaxSupply() * 2f;
			if (freighters > 30) freighters = 30;
			
			addShips(market.getFactionId(),
					10f + extraShips, // combat
					freighters, // freighter 
					0f, // tanker
					10f, // transport
					10f, // liner
					5f, // utilityPts
					null, // qualityOverride
					0f, // qualityMod
					ShipPickMode.PRIORITY_THEN_ALL,
					null);
			
			addShips(market.getFactionId(),
					40f, // combat
					0f, // freighter 
					0f, // tanker
					0f, // transport
					0f, // liner
					0f, // utilityPts
					null, // qualityOverride
					-1f, // qualityMod
					null,
					null,
					4);
			
			float tankers = 20f;
			com = market.getCommodityData(Commodities.FUEL);
			tankers += com.getMaxSupply() * 3f;
			if (tankers > 40) tankers = 40;
			//tankers = 40;
			addShips(market.getFactionId(),
					0f, // combat
					0f, // freighter 
					tankers, // tanker
					0, // transport
					0f, // liner
					0f, // utilityPts
					null, // qualityOverride
					0f, // qualityMod
					ShipPickMode.PRIORITY_THEN_ALL,
					null);
			
			
			addHullMods(1, 1 + itemGenRandom.nextInt(3), market.getFactionId());
		}
		
		getCargo().sort();
	}
	
	protected Object writeReplace() {
		if (okToUpdateShipsAndWeapons()) {
			pruneWeapons(0f);
			getCargo().getMothballedShips().clear();
		}
		return this;
	}
	
	
	public boolean shouldHaveCommodity(CommodityOnMarketAPI com) {
		return !market.isIllegal(com);
	}
	
	@Override
	public int getStockpileLimit(CommodityOnMarketAPI com) {
//		int demand = com.getMaxDemand();
//		int available = com.getAvailable();
//		
//		float limit = BaseIndustry.getSizeMult(available) - BaseIndustry.getSizeMult(Math.max(0, demand - 2));
//		limit *= com.getCommodity().getEconUnit();
		
		//limit *= com.getMarket().getStockpileMult().getModifiedValue();
		
		float limit = OpenMarketPlugin.getBaseStockpileLimit(com);
		
		Random random = new Random(market.getId().hashCode() + submarket.getSpecId().hashCode() + Global.getSector().getClock().getMonth() * 170000);
		limit *= 0.9f + 0.2f * random.nextFloat();
		
		float sm = market.getStabilityValue() / 10f;
		limit *= (0.25f + 0.75f * sm);
		
		if (limit < 0) limit = 0;
		
		return (int) limit;
	}
	
	public static float ECON_UNIT_MULT_EXTRA = 1f;
	public static float ECON_UNIT_MULT_PRODUCTION = 0.4f;
	public static float ECON_UNIT_MULT_IMPORTS = 0.1f;
	public static float ECON_UNIT_MULT_DEFICIT = -0.2f;
	
	public static Set<String> SPECIAL_COMMODITIES = new HashSet<String>();
	static {
		SPECIAL_COMMODITIES.add(Commodities.SUPPLIES);
		SPECIAL_COMMODITIES.add(Commodities.FUEL);
		SPECIAL_COMMODITIES.add(Commodities.CREW);
		SPECIAL_COMMODITIES.add(Commodities.MARINES);
		SPECIAL_COMMODITIES.add(Commodities.HEAVY_MACHINERY);
	}
	
	public static float getBaseStockpileLimit(CommodityOnMarketAPI com) {
//		if (com.getCommodity().getId().equals(Commodities.LUXURY_GOODS)) {
//			System.out.println("wefwefwef");
//		}
		int shippingGlobal = Global.getSettings().getShippingCapacity(com.getMarket(), false);
		int available = com.getAvailable();
		int production = com.getMaxSupply();
		production = Math.min(production, available);
			
		int demand = com.getMaxDemand();
		int export = (int) Math.min(production, shippingGlobal);
		
		int extra = available - Math.max(export, demand);
		if (extra < 0) extra = 0;
		
		//int inDemand = Math.min(available, demand);
		//int normal = Math.max(0, available - inDemand - extra);
		int deficit = Math.max(0, demand - available);
		
		float unit = com.getCommodity().getEconUnit();
		
		int imports = available - production;
		if (imports < 0) imports = 0;
		
		float limit = 0f;
		limit += imports * unit * ECON_UNIT_MULT_IMPORTS;
		limit += production * unit * ECON_UNIT_MULT_PRODUCTION;
		limit += extra * unit * ECON_UNIT_MULT_EXTRA;
		limit -= deficit * unit * ECON_UNIT_MULT_DEFICIT;
		
		
		//limit += inDemand * unit * ECON_UNIT_MULT_IN_DEMAND;
		//limit += normal * unit * ECON_UNIT_MULT_NORMAL;
		
		if (limit < 0) limit = 0;
		return (int) limit;
	}
	
	
	public static int getApproximateStockpileLimit(CommodityOnMarketAPI com) {
//		int demand = com.getMaxDemand();
//		int available = com.getAvailable();
//		
//		float limit = BaseIndustry.getSizeMult(available) - BaseIndustry.getSizeMult(Math.max(0, demand - 2));
//		limit *= com.getCommodity().getEconUnit();
//		//limit *= 0.5f;
//		
//		if (limit < 0) limit = 0;
//		return (int) limit;
		
		float limit = OpenMarketPlugin.getBaseStockpileLimit(com);
		return (int) limit;
	}
	
	
	
	
	@Override
	public PlayerEconomyImpactMode getPlayerEconomyImpactMode() {
		return PlayerEconomyImpactMode.PLAYER_SELL_ONLY;
	}


	@Override
	public boolean isOpenMarket() {
		return true;
	}


	@Override
	public String getTooltipAppendix(CoreUIAPI ui) {
		if (ui.getTradeMode() == CoreUITradeMode.SNEAK) {
			return "Requires: proper docking authorization (transponder on)";
		}
		return super.getTooltipAppendix(ui);
	}


	@Override
	public Highlights getTooltipAppendixHighlights(CoreUIAPI ui) {
		if (ui.getTradeMode() == CoreUITradeMode.SNEAK) {
			String appendix = getTooltipAppendix(ui);
			if (appendix == null) return null;
			
			Highlights h = new Highlights();
			h.setText(appendix);
			h.setColors(Misc.getNegativeHighlightColor());
			return h;
		}
		return super.getTooltipAppendixHighlights(ui);
	}
	

}
