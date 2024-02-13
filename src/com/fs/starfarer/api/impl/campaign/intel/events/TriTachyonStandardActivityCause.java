package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.MapParams;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;

public class TriTachyonStandardActivityCause extends BaseHostileActivityCause2 {

	public static int MIN_TRITACH_PRODUCTION = 5;
	public static int MIN_COMPETITOR_PRODUCTION = 4;
	
	public static int MIN_COMPETITOR_MARKET_SIZE = 4;
	
	public static float PROD_PROGRESS_MULT = Global.getSettings().getFloat("triTachyonProgressPerUnitProdMult");
	
	public static Set<String> COMPETING_COMMODITIES = new LinkedHashSet<String>();
	static {
		COMPETING_COMMODITIES.add(Commodities.LUXURY_GOODS);
		COMPETING_COMMODITIES.add(Commodities.FUEL);
		COMPETING_COMMODITIES.add(Commodities.DRUGS);
		COMPETING_COMMODITIES.add(Commodities.ORGANS);
		COMPETING_COMMODITIES.add(Commodities.HAND_WEAPONS);
		COMPETING_COMMODITIES.add(Commodities.RARE_ORE);
		COMPETING_COMMODITIES.add(Commodities.RARE_METALS);
		COMPETING_COMMODITIES.add(Commodities.HEAVY_MACHINERY);
		COMPETING_COMMODITIES.add(Commodities.SUPPLIES);
		COMPETING_COMMODITIES.add(Commodities.VOLATILES);
		COMPETING_COMMODITIES.add(Commodities.ORGANICS);
	}
	
	public static class CompetitorData {
		public String commodityId;
		public CommoditySpecAPI spec;
		public int factionProdTotal;
		public int factionMaxProd;
		public int competitorProdTotal;
		public int competitorMaxProd;
		public int competitorMaxMarketSize;
		public int allProdTotal;
		public List<MarketAPI> factionProducers = new ArrayList<MarketAPI>();
		public List<MarketAPI> competitorProducers = new ArrayList<MarketAPI>();
		
		public CompetitorData(String commodityId) {
			this.commodityId = commodityId;
			spec = Global.getSettings().getCommoditySpec(commodityId);
		}

		public int getProgress(float progMult) {
			return (int) Math.round(competitorMaxProd * progMult);
		}
	}
	

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
		String factionId = Factions.TRITACHYON;
		
		Set<String> commodities = new LinkedHashSet<String>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (!factionId.equals(market.getFactionId())) continue;
			
			for (CommodityOnMarketAPI com : market.getCommoditiesCopy()) {
				if (com.isPersonnel()) continue;
				if (com.getId().equals(Commodities.SHIPS)) continue;
				if (com.getId().equals(Commodities.SHIP_WEAPONS)) continue;
				if (!COMPETING_COMMODITIES.contains(com.getId())) continue;
				int prod = com.getMaxSupply();
				
				if (prod >= MIN_TRITACH_PRODUCTION) {
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
	
	
	public TriTachyonStandardActivityCause(HostileActivityEventIntel intel) {
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
				
				tooltip.addPara("Your colony production of certain commodities that the Tri-Tachyon corporation "
						+ "has an interest in is high enough "
						+ "to be viewed as competition.", 0f);
				
				TriTachyonHostileActivityFactor.addDealtSectionToTooltip(tooltip, opad);
				
//				tooltip.addPara("Independent-flagged commerce raiders have been sighted in your space, "
//						+ "attacking trade fleets regardless of their factional allegiance.", 0f);
				
				List<CompetitorData> comp = computePlayerCompetitionData();
				FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
				
				tooltip.beginTable(player, 20f, "Commodity", getTooltipWidth(tooltipParam) - 150f, "Production", 150f);
				for (final CompetitorData data : comp) {
					tooltip.addRow(Alignment.LMID, tc, Misc.ucFirst(data.spec.getLowerCaseName()),
							   Alignment.MID, h, "" + (int) data.competitorMaxProd);
				}
				tooltip.addTable("", 0, opad);
				tooltip.addSpacer(5f);
				
				tooltip.addPara("Event progress is based on the maximum production of each commodity. "
						+ "%s below %s per colony, or reducing the scope of the competition to a single commodity,"
						+ " should be enough to divert "
						+ "Tri-Tachyon's attention.", opad, 
						h,
						"Reducing production levels",
						"" + MIN_COMPETITOR_PRODUCTION,
						"single commodity");
				
				tooltip.addPara("%s are also possible.", opad,
						h, "Reciprocal solutions");
				
				StarSystemAPI system = TriTachyonHostileActivityFactor.getPrimaryTriTachyonSystem();
				if (system != null) {
					tooltip.addPara("The Tri-Tachyon Corporation's base of power is in the " + 
							system.getNameWithLowercaseType() + ".", opad);

					MapParams params = new MapParams();
					params.showSystem(system);
					float width = tooltip.getWidthSoFar();
					float height = Math.round(width / 1.6f);
					params.positionToShowAllMarkersAndSystems(true, Math.min(width, height));
					UIPanelAPI map = tooltip.createSectorMap(width, height, params, system.getNameWithLowercaseTypeShort());
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
		if (TriTachyonHostileActivityFactor.isPlayerCounterRaidedTriTach() || 
				TriTachyonHostileActivityFactor.getPrimaryTriTachyonSystem() == null) {
			return 0;
		}
		
		int total = 0;
		List<CompetitorData> comp = computePlayerCompetitionData();
		if (comp.size() <= 1) return 0;
		
		for (CompetitorData data : comp) {
			total += data.getProgress(PROD_PROGRESS_MULT);
		}
		
		if (TriTachyonHostileActivityFactor.isDealtWithMercAttack()) {
			total = (int) Math.round(total * TriTachyonHostileActivityFactor.DEALT_WITH_MERC_PROGRESS_MULT); 
		}
		
		return total;
	}
	
	public String getDesc() {
		return "Competing exports";
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
					mag += prod * 0.05f;
				}
			}
		}
		if (mag > MAX_MAG) mag = MAX_MAG;
		
		mag = Math.round(mag * 100f) / 100f;
		
		return mag;
	}
	
}






