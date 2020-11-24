package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;


public class Farming extends BaseIndustry implements MarketImmigrationModifier {

	public static Set<String> AQUA_PLANETS = new HashSet<String>();
	
	static {
		AQUA_PLANETS.add(StarTypes.PLANET_WATER);
	}
	
	public void apply() {
		super.apply(true);
		
		int size = market.getSize();
		boolean aquaculture = Industries.AQUACULTURE.equals(getId());
		
		if (aquaculture) {
			demand(0, Commodities.HEAVY_MACHINERY, size, BASE_VALUE_TEXT);
		} else {
			demand(0, Commodities.HEAVY_MACHINERY, size - 3, BASE_VALUE_TEXT);
		}
		
		//supply(3, Commodities.LOBSTER, 5, "Hack");

		// ResourceDepositsCondition sets base value
		// makes more sense for Mining where mining doesn't have to check for existence of resource conditions
		
//		int deficit = getMaxDeficit(Commodities.HEAVY_MACHINERY);
//		supply(1, Commodities.FOOD, -deficit, getDeficitText(Commodities.HEAVY_MACHINERY));
//		supply(1, Commodities.ORGANICS, -deficit, getDeficitText(Commodities.HEAVY_MACHINERY));
		
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.HEAVY_MACHINERY);
		//applyDeficitToProduction(0, deficit, Commodities.FOOD, Commodities.ORGANICS);
		applyDeficitToProduction(0, deficit, Commodities.FOOD);
		
		
		if (!isFunctional()) {
			supply.clear();
		}
	}

	
	@Override
	public void unapply() {
		super.unapply();
	}

	
	@Override
	public boolean isAvailableToBuild() {
		if (!super.isAvailableToBuild()) return false;
		boolean aquaculture = Industries.AQUACULTURE.equals(getId());
		boolean canAquaculture = market.getPlanetEntity() != null && 
								 AQUA_PLANETS.contains(market.getPlanetEntity().getTypeId());
		if (aquaculture != canAquaculture) return false;
		
		for (MarketConditionAPI mc : market.getConditions()) {
			String commodity = ResourceDepositsCondition.COMMODITY.get(mc.getId());
			if (commodity != null) {
				String industry = ResourceDepositsCondition.INDUSTRY.get(commodity);
				if (Industries.FARMING.equals(industry)) return true;
			}
		}
		return false;
	}


	@Override
	public boolean showWhenUnavailable() {
		boolean aquaculture = Industries.AQUACULTURE.equals(getId());
		boolean canAquaculture = market.getPlanetEntity() != null && 
								 AQUA_PLANETS.contains(market.getPlanetEntity().getTypeId());
		if (aquaculture != canAquaculture) return false;
		
		return super.showWhenUnavailable();
	}
	

	@Override
	public String getUnavailableReason() {
		if (!super.isAvailableToBuild()) return super.getUnavailableReason();
		return "Requires farmland";
	}


	@Override
	public void createTooltip(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltip(mode, tooltip, expanded);
//
//		int size = market.getSize();
//		boolean aquaculture = Industries.AQUACULTURE.equals(getId());
//		
//		int machinery = size - 3;
//		if (aquaculture) {
//			machinery = size;
//		}
//		if (machinery < 0) machinery = 0;
//		
//		float pad = 3f;
//		float opad = 10f;
//		
//		FactionAPI faction = market.getFaction();
//		Color color = faction.getBaseUIColor();
//		Color dark = faction.getDarkUIColor();
//		Color grid = faction.getGridUIColor();
//		Color bright = faction.getBrightUIColor();
//		
//		tooltip.addSectionHeading("Produces", color, dark, Alignment.MID, opad);
//		tooltip.beginIconGroup();
//		tooltip.setIconSpacingMedium();
//		for (MutableCommodityQuantity curr : supply.values()) {
//			int qty = curr.getQuantity().getModifiedInt();
//			if (qty <= 0) continue;
//			tooltip.addIcons(market.getCommodityData(curr.getCommodityId()), qty, IconRenderMode.NORMAL);
//		}
//		tooltip.addIconGroup(opad);
//		
//		tooltip.addSectionHeading("Requires", color, dark, Alignment.MID, opad);
//		tooltip.beginIconGroup();
//		tooltip.setIconSpacingMedium();
//		for (MutableCommodityQuantity curr : demand.values()) {
//			int qty = curr.getQuantity().getModifiedInt();
//			if (qty <= 0) continue;
//			tooltip.addIcons(market.getCommodityData(curr.getCommodityId()), qty, IconRenderMode.NORMAL);
//		}
//		tooltip.addIconGroup(opad);
		
	}


	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		incoming.add(Factions.LUDDIC_CHURCH, 10f);
	}
	
	
	@Override
	public String getCurrentImage() {
		boolean aquaculture = Industries.AQUACULTURE.equals(getId());
		if (aquaculture) {
			return super.getCurrentImage();
		}
		float size = market.getSize();
		PlanetAPI planet = market.getPlanetEntity();
		if (size <= 3) {
			return Global.getSettings().getSpriteName("industry", "farming_low");
		}
		if (size <= 5) {
			return Global.getSettings().getSpriteName("industry", "farming_med");
		}
		if (size >= 7) {
			return Global.getSettings().getSpriteName("industry", "farming_high");
		}
	
		return super.getCurrentImage();
	}
	
}
