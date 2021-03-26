package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.impl.campaign.events.FoodShortageEvent;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class FoodShortage extends BaseMarketConditionPlugin {
	
	private FoodShortageEvent event = null;
	
	
	public FoodShortage() {
	}

	public void apply(String id) {
		market.getStability().modifyFlat(id, -1f * event.getStabilityImpact(), "Food shortage");
		
		String sellId = Stats.getPlayerSellRepImpactMultId(Commodities.FOOD);
		market.getStats().getDynamic().getStat(sellId).modifyMult(id, 4f);
	}

	public void unapply(String id) {
		market.getCommodityData(Commodities.FOOD).getPlayerSupplyPriceMod().unmodify(id);
		market.getStability().unmodify(id);
		
		String sellId = Stats.getPlayerSellRepImpactMultId(Commodities.FOOD);
		market.getStats().getDynamic().getStat(sellId).unmodify(id);
	}

	
	public List<String> getRelatedCommodities() {
		return event.getRelatedCommodities();
	}

	@Override
	public void setParam(Object param) {
		event = (FoodShortageEvent) param;
	}
	
	
	public Map<String, String> getTokenReplacements() {
		// LinkedHashMap because order matters in getHighlights()
		//Map<String, String> tokens = new LinkedHashMap<String, String>();
		Map<String, String> tokens = super.getTokenReplacements();
		
		int penalty = (int) event.getStabilityImpact();
		tokens.put("$stabilityPenalty", "" + penalty);
		
		//int increase = (int) ((event.getFoodPriceFlat() - 1f) * 100f); 
		//tokens.put("$foodPriceIncrease", "" + increase + "%");
		//tokens.put("$foodPriceIncrease", "~" + (int) Misc.getRounded(event.getFoodPriceFlat()) + Strings.C);
		
		int remaining = (int) Misc.getRounded(event.getRemainingFoodToMeetShortage());
		tokens.put("$neededFood", "" + remaining);
		
		return tokens;
	}

	@Override
	public String[] getHighlights() {
		List<String> highlights = new ArrayList<String>();
		//addTokensToList(highlights, "$stabilityPenalty", "$foodPriceIncrease", "$neededFood");
		addTokensToList(highlights, "$stabilityPenalty", "$neededFood");
		return highlights.toArray(new String [0]);
		//return new String[] {"" + (int) event.getStabilityImpact() };
	}

	@Override
	public Color[] getHighlightColors() {
		return super.getHighlightColors();
	}
	
	@Override
	public boolean isTransient() {
		return false;
	}
	
}
