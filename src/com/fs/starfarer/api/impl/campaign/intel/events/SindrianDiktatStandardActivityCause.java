package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.events.TriTachyonStandardActivityCause.CompetitorData;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.MapParams;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;

public class SindrianDiktatStandardActivityCause extends BaseHostileActivityCause2 {

	public static int MIN_DIKTAT_PRODUCTION = 7;
	public static int MIN_COMPETITOR_PRODUCTION = 4;
	
	public static int MIN_COMPETITOR_MARKET_SIZE = 4;
	
	public static float PROD_PROGRESS_MULT = Global.getSettings().getFloat("diktatProgressPerUnitProdMult");
	
	public static CompetitorData computeCompetitorData(String factionId, String competitorId, String commodityId) {
		CompetitorData data = new CompetitorData(commodityId);
		
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			CommodityOnMarketAPI com = market.getCommodityData(commodityId);
			if (com == null) continue;

			int prod = com.getMaxSupply();
			if (prod <= 0) continue;
			
			if (factionId.equals(market.getFactionId())) {
				data.factionProdTotal += prod;
				data.factionProducers.add(market);
				data.factionMaxProd = Math.max(data.factionMaxProd, prod);
			} else if (competitorId.equals(market.getFactionId())) {
				data.competitorProdTotal += prod;
				data.competitorMaxProd = Math.max(data.competitorMaxProd, prod);
				data.competitorProducers.add(market);
				
				data.competitorMaxMarketSize = Math.max(data.competitorMaxMarketSize, market.getSize());
			}
			
			data.allProdTotal += prod;
		}
		
		return data;
	}
	
	public static List<CompetitorData> computePlayerCompetitionData() {
		String factionId = Factions.DIKTAT;
		
		List<String> commodities = new ArrayList<String>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (!factionId.equals(market.getFactionId())) continue;
			
			for (CommodityOnMarketAPI com : market.getCommoditiesCopy()) {
				if (!com.isFuel()) continue;
				int prod = com.getMaxSupply();
				if (prod >= MIN_DIKTAT_PRODUCTION) {
					commodities.add(com.getId());
				}
			}
		}
		
		List<CompetitorData> result = new ArrayList<CompetitorData>();
		
		for (String commodityId : commodities) {
			CompetitorData data = computeCompetitorData(factionId, Factions.PLAYER, commodityId);
			if (data.competitorMaxProd < MIN_COMPETITOR_PRODUCTION) continue;
			if (data.competitorMaxMarketSize < MIN_COMPETITOR_MARKET_SIZE) continue;
			result.add(data);
		}
		
		return result;
	}
	
	public static float MAX_MAG = 0.5f;
	
	
	public SindrianDiktatStandardActivityCause(HostileActivityEventIntel intel) {
		super(intel);
	}
	
	
	@Override
	public TooltipCreator getTooltip() {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
		
				Color h = Misc.getHighlightColor();
				Color tc = Misc.getTextColor();
				
				tooltip.addPara("Your colony production of fuel is high enough for the Sindrian Diktat - "
						+ "which depends on its own fuel production for much of its economy - to take notice.", 0f);
				
				List<CompetitorData> comp = computePlayerCompetitionData();
				FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
				
				tooltip.beginTable(player, 20f, "Commodity", getTooltipWidth(tooltipParam) - 150f, "Production", 150f);
				for (final CompetitorData data : comp) {
					tooltip.addRow(Alignment.LMID, tc, Misc.ucFirst(data.spec.getLowerCaseName()),
							   Alignment.MID, h, "" + (int) data.competitorMaxProd);
				}
				tooltip.addTable("", 0, opad);
				tooltip.addSpacer(5f);
				
				tooltip.addPara("Event progress is based on maximum fuel production. "
						+ "%s below %s per colony should be enough to divert "
						+ "the Diktat's attention.", opad, 
						h, "Reducing production levels ", "" + MIN_COMPETITOR_PRODUCTION);
				
				tooltip.addPara("Knocking the Diktat out of the fuel production game is also an option. Much of "
						+ "their fuel production depends on a Domain-era Synchrotron Core installed in "
						+ "their production facilities on Sindria.", opad,
						h, "Synchrotron Core");

				MarketAPI sindria = SindrianDiktatHostileActivityFactor.getSindria(false);
				if (sindria != null && sindria.getStarSystem() != null) {
					MapParams params = new MapParams();
					params.showSystem(sindria.getStarSystem());
					float w = tooltip.getWidthSoFar();
					float ht = Math.round(w / 1.6f);
					params.positionToShowAllMarkersAndSystems(true, Math.min(w, ht));
					UIPanelAPI map = tooltip.createSectorMap(w, ht, params, sindria.getName() + " (" + sindria.getStarSystem().getNameWithLowercaseTypeShort() + ")");
					tooltip.addCustom(map, opad);
				}
			}
		};
	}

	@Override
	public boolean shouldShow() {
		return getProgress() != 0;
	}

	public int getProgress() {
		if (SindrianDiktatHostileActivityFactor.isMadeDeal() ||
				SindrianDiktatHostileActivityFactor.isPlayerDefeatedDiktatAttack()) {
			return 0;
		}
		
		int total = 0;
		
		List<CompetitorData> comp = computePlayerCompetitionData();
		for (CompetitorData data : comp) {
			total += data.getProgress(PROD_PROGRESS_MULT);
		}
		
		return total;
	}
	
	public String getDesc() {
		return "Competing fuel production";
	}
	

	public float getMagnitudeContribution(StarSystemAPI system) {
		if (getProgress() <= 0) return 0f;
		
		List<CompetitorData> comp = computePlayerCompetitionData();
		float mag = 0f;
		for (CompetitorData data : comp) {
			for (MarketAPI market : data.competitorProducers) {
				if (market.getContainingLocation() == system) {
					CommodityOnMarketAPI com = market.getCommodityData(data.commodityId);
					float prod = com.getMaxSupply();
					mag += prod * 0.1f;
				}
			}
		}
		if (mag > MAX_MAG) mag = MAX_MAG;
		
		mag = Math.round(mag * 100f) / 100f;
		
		return mag;
	}
	
}






