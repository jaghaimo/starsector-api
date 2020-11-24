package com.fs.starfarer.api.impl.campaign.submarkets;

import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
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
	
			pruneWeapons(0f);
			
			int weapons = 2 + Math.max(0, market.getSize() - 3) + (Misc.isMilitary(market) ? 5 : 0);
			int fighters = 1 + Math.max(0, (market.getSize() - 3) / 2) + (Misc.isMilitary(market) ? 2 : 0);
			
			addWeapons(weapons, weapons + 2, 0, market.getFactionId());
			addFighters(fighters, fighters + 2, 0, market.getFactionId());
			
			
			getCargo().getMothballedShips().clear();
			
			addShips(market.getFactionId(),
					20f, // combat
					20f, // freighter 
					0f, // tanker
					10f, // transport
					10f, // liner
					5f, // utilityPts
					null, // qualityOverride
					0f, // qualityMod
					ShipPickMode.IMPORTED,
					null);
			
			addShips(market.getFactionId(),
					30f, // combat
					0f, // freighter 
					0f, // tanker
					0f, // transport
					0f, // liner
					0f, // utilityPts
					null, // qualityOverride
					-0.5f, // qualityMod
					null,
					null);
			
			float tankers = 20f;
			CommodityOnMarketAPI com = market.getCommodityData(Commodities.FUEL);
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
			
			
			addHullMods(1, 1 + itemGenRandom.nextInt(3));
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
		int demand = com.getMaxDemand();
		int available = com.getAvailable();
		
		float limit = BaseIndustry.getSizeMult(available) - BaseIndustry.getSizeMult(Math.max(0, demand - 2));
		limit *= com.getCommodity().getEconUnit();
		
		//limit *= com.getMarket().getStockpileMult().getModifiedValue();
		
		Random random = new Random(market.getId().hashCode() + submarket.getSpecId().hashCode() + Global.getSector().getClock().getMonth() * 170000);
		limit *= 0.9f + 0.2f * random.nextFloat();
		
		float sm = market.getStabilityValue() / 10f;
		limit *= (0.25f + 0.75f * sm);
		
		if (limit < 0) limit = 0;
		
		return (int) limit;
	}
	
	
	public static int getApproximateStockpileLimit(CommodityOnMarketAPI com) {
		int demand = com.getMaxDemand();
		int available = com.getAvailable();
		
		float limit = BaseIndustry.getSizeMult(available) - BaseIndustry.getSizeMult(Math.max(0, demand - 2));
		limit *= com.getCommodity().getEconUnit();
		//limit *= 0.5f;
		
		if (limit < 0) limit = 0;
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
