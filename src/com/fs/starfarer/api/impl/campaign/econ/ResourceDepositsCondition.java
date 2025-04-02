package com.fs.starfarer.api.impl.campaign.econ;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class ResourceDepositsCondition extends BaseHazardCondition implements MarketImmigrationModifier {
	
	public static Map<String, String> COMMODITY = new HashMap<String, String>();
	public static Map<String, Integer> MODIFIER = new HashMap<String, Integer>();
	public static Map<String, String> INDUSTRY = new HashMap<String, String>();
	public static Map<String, Integer> BASE_MODIFIER = new HashMap<String, Integer>();
	public static Set<String> BASE_ZERO  = new HashSet<String>();
	static {
		COMMODITY.put(Conditions.ORE_SPARSE, Commodities.ORE);
		COMMODITY.put(Conditions.ORE_MODERATE, Commodities.ORE);
		COMMODITY.put(Conditions.ORE_ABUNDANT, Commodities.ORE);
		COMMODITY.put(Conditions.ORE_RICH, Commodities.ORE);
		COMMODITY.put(Conditions.ORE_ULTRARICH, Commodities.ORE);
		
		COMMODITY.put(Conditions.RARE_ORE_SPARSE, Commodities.RARE_ORE);
		COMMODITY.put(Conditions.RARE_ORE_MODERATE, Commodities.RARE_ORE);
		COMMODITY.put(Conditions.RARE_ORE_ABUNDANT, Commodities.RARE_ORE);
		COMMODITY.put(Conditions.RARE_ORE_RICH, Commodities.RARE_ORE);
		COMMODITY.put(Conditions.RARE_ORE_ULTRARICH, Commodities.RARE_ORE);
		
		COMMODITY.put(Conditions.ORGANICS_TRACE, Commodities.ORGANICS);
		COMMODITY.put(Conditions.ORGANICS_COMMON, Commodities.ORGANICS);
		COMMODITY.put(Conditions.ORGANICS_ABUNDANT, Commodities.ORGANICS);
		COMMODITY.put(Conditions.ORGANICS_PLENTIFUL, Commodities.ORGANICS);
		
		COMMODITY.put(Conditions.VOLATILES_TRACE, Commodities.VOLATILES);
		COMMODITY.put(Conditions.VOLATILES_DIFFUSE, Commodities.VOLATILES);
		COMMODITY.put(Conditions.VOLATILES_ABUNDANT, Commodities.VOLATILES);
		COMMODITY.put(Conditions.VOLATILES_PLENTIFUL, Commodities.VOLATILES);
		
		COMMODITY.put(Conditions.FARMLAND_POOR, Commodities.FOOD);
		COMMODITY.put(Conditions.FARMLAND_ADEQUATE, Commodities.FOOD);
		COMMODITY.put(Conditions.FARMLAND_RICH, Commodities.FOOD);
		COMMODITY.put(Conditions.FARMLAND_BOUNTIFUL, Commodities.FOOD);
		
		COMMODITY.put(Conditions.VOLTURNIAN_LOBSTER_PENS, Commodities.LOBSTER);
		
		COMMODITY.put(Conditions.WATER_SURFACE, Commodities.FOOD);
		//COMMODITY.put(Conditions.POLLUTION, Commodities.FOOD);
		
		
		MODIFIER.put(Conditions.ORE_SPARSE, -1);
		MODIFIER.put(Conditions.ORE_MODERATE, 0);
		MODIFIER.put(Conditions.ORE_ABUNDANT, 1);
		MODIFIER.put(Conditions.ORE_RICH, 2);
		MODIFIER.put(Conditions.ORE_ULTRARICH, 3);
		
		
		MODIFIER.put(Conditions.RARE_ORE_SPARSE, -1);
		MODIFIER.put(Conditions.RARE_ORE_MODERATE, 0);
		MODIFIER.put(Conditions.RARE_ORE_ABUNDANT, 1);
		MODIFIER.put(Conditions.RARE_ORE_RICH, 2);
		MODIFIER.put(Conditions.RARE_ORE_ULTRARICH, 3);
		
		MODIFIER.put(Conditions.ORGANICS_TRACE, -1);
		MODIFIER.put(Conditions.ORGANICS_COMMON, 0);
		MODIFIER.put(Conditions.ORGANICS_ABUNDANT, 1);
		MODIFIER.put(Conditions.ORGANICS_PLENTIFUL, 2);
		
		MODIFIER.put(Conditions.VOLATILES_TRACE, -1);
		MODIFIER.put(Conditions.VOLATILES_DIFFUSE, 0);
		MODIFIER.put(Conditions.VOLATILES_ABUNDANT, 1);
		MODIFIER.put(Conditions.VOLATILES_PLENTIFUL, 2);
		
		MODIFIER.put(Conditions.FARMLAND_POOR, -1);
		MODIFIER.put(Conditions.FARMLAND_ADEQUATE, 0);
		MODIFIER.put(Conditions.FARMLAND_RICH, 1);
		MODIFIER.put(Conditions.FARMLAND_BOUNTIFUL, 2);
		
		MODIFIER.put(Conditions.WATER_SURFACE, 0);
		//MODIFIER.put(Conditions.POLLUTION, -2);
		MODIFIER.put(Conditions.VOLTURNIAN_LOBSTER_PENS, 1);
		
		
		INDUSTRY.put(Commodities.ORE, Industries.MINING);
		INDUSTRY.put(Commodities.RARE_ORE, Industries.MINING);
		INDUSTRY.put(Commodities.VOLATILES, Industries.MINING);
		INDUSTRY.put(Commodities.ORGANICS, Industries.MINING);
		
		INDUSTRY.put(Commodities.FOOD, Industries.FARMING);
		INDUSTRY.put(Commodities.LOBSTER, Industries.FARMING);
		
		BASE_MODIFIER.put(Commodities.ORE, 0);
		BASE_MODIFIER.put(Commodities.RARE_ORE, -2);
		BASE_MODIFIER.put(Commodities.VOLATILES, -2);
		BASE_MODIFIER.put(Commodities.ORGANICS, 0);
		BASE_MODIFIER.put(Commodities.FOOD, 0);
		
		BASE_MODIFIER.put(Commodities.LOBSTER, 0);
		BASE_ZERO.add(Commodities.LOBSTER);
	}
	
//	public static int getProduction(MarketAPI market, String industryId, String commodityId) {
//		for (MarketConditionAPI mc : market.getConditions()) {
//			String currCommodity = COMMODITY.get(mc.getId());
//			if (currCommodity == null) continue;
//			if (!currCommodity.equals(commodityId)) continue;
//			
//			Integer mod = MODIFIER.get(mc.getId());
//			if (mod == null) continue;
//			
//			Integer baseMod = BASE_MODIFIER.get(currCommodity);
//			if (baseMod == null) continue;
//			
//			String currIndustry = INDUSTRY.get(currCommodity);
//			if (currIndustry == null) continue;
//			if (!industryId.equals(currIndustry)) {
//				if (Industries.FARMING.equals(currIndustry)) {
//					if (!industryId.equals(Industries.AQUACULTURE)) {
//						continue;
//					}
//				} else {
//					continue;
//				}
//			}
//			
//			int size = market.getSize();
//			if (BASE_ZERO.contains(commodityId)) {
//				size = 0;
//			}
//			
//			int base = size + baseMod;
//
//			return base + mod;
//		}
//		return 0;
//	}
	
	
	
	public void apply(String id) {
		super.apply(id);
		
		String commodityId = COMMODITY.get(condition.getId());
		if (commodityId == null) return;
		
//		if (commodityId.equals(Commodities.LOBSTER)) {
//			System.out.println("ewfwefwe");
//		}
		
		Integer mod = MODIFIER.get(condition.getId());
		if (mod == null) return;
		
		Integer baseMod = BASE_MODIFIER.get(commodityId);
		if (baseMod == null) return;
		
		String industryId = INDUSTRY.get(commodityId);
		if (industryId == null) return;
		
		Industry industry = market.getIndustry(industryId);
		if (industry == null) {
			if (Industries.FARMING.equals(industryId)) {
				industryId = Industries.AQUACULTURE;
				industry = market.getIndustry(industryId);
			}
			if (industry == null) return;
		}

		int size = market.getSize();
		if (BASE_ZERO.contains(commodityId)) {
			size = 0;
		}
		
		int base = size + baseMod;

		if (industry.isFunctional()) {
//			industry.getSupply(commodityId).getQuantity().modifyFlat(id + "_0", base, BaseIndustry.BASE_VALUE_TEXT);
//			industry.getSupply(commodityId).getQuantity().modifyFlat(id + "_1", mod, Misc.ucFirst(condition.getName().toLowerCase()));
			
			industry.supply(id + "_0", commodityId, base, BaseIndustry.BASE_VALUE_TEXT);
			industry.supply(id + "_1", commodityId, mod, Misc.ucFirst(condition.getName().toLowerCase()));
		} else {
			industry.getSupply(commodityId).getQuantity().unmodifyFlat(id + "_0");
			industry.getSupply(commodityId).getQuantity().unmodifyFlat(id + "_1");
		}
		
		if (Commodities.FOOD.equals(commodityId)) {
			market.addImmigrationModifier(this);
		}
		
// uncomment to make farming provide organics
// also need to adjust Farming to apply machinery deficit penalty
//		if ((Industries.FARMING.equals(industryId) ||
//				Industries.AQUACULTURE.equals(industryId) && Commodities.FOOD.equals(commodityId))) {
//			industry.getSupply(Commodities.ORGANICS).getQuantity().modifyFlat(id + "_0", size - 2, BaseIndustry.BASE_VALUE_TEXT);
//			industry.getSupply(Commodities.ORGANICS).getQuantity().modifyFlat(id + "_1", mod, Misc.ucFirst(condition.getName().toLowerCase()));
//		}
	}
	
	public void unapply(String id) {
		super.unapply(id);
		market.removeImmigrationModifier(this);
	}

	@Override
	public Map<String, String> getTokenReplacements() {
		if (true) return super.getTokenReplacements();
		
		Map<String, String> map = super.getTokenReplacements();
		
		String commodityId = COMMODITY.get(condition.getId());
		if (commodityId == null) return map;
		
		Integer mod = MODIFIER.get(condition.getId());
		if (mod == null) return map;
		
		CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(commodityId);
		
		String str = "" + mod;
		if (mod > 0) str = "+" + mod;
		if (mod == 0) {
			map.put("$resourceModText", "No bonuses or penalties to " + spec.getName().toLowerCase() + " production.");
		} else {
			map.put("$resourceModText", "" + str + " to " + spec.getName().toLowerCase() + " production.");
		}
		
		//map.put("$resourceMod", Misc.ucFirst(spec.getName().toLowerCase()) )
		
		
		return map;
	}

	@Override
	public String[] getHighlights() {
		if (true) return super.getHighlights();
		
		String commodityId = COMMODITY.get(condition.getId());
		if (commodityId == null) return super.getHighlights();
		
		Integer mod = MODIFIER.get(condition.getId());
		if (mod == null) return super.getHighlights();
		
		String str = "" + mod;
		if (mod > 0) str = "+" + mod;
		
		if (mod == 0) {
			return super.getHighlights();
		}
		
		return new String[] {str};
	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		
		String commodityId = COMMODITY.get(condition.getId());
		if (commodityId != null) {
			
//			boolean hasHazard = false;
//			Object test = Global.getSettings().getSpec(ConditionGenDataSpec.class, condition.getId(), true);
//			if (test instanceof ConditionGenDataSpec) {
//				ConditionGenDataSpec spec = (ConditionGenDataSpec) test;
//				float hazard = spec.getHazard();
//				//hazard = 0.25f;
//				hasHazard = hazard != 0;
//			}
			
			Integer mod = MODIFIER.get(condition.getId());
			if (mod != null) {
				CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(commodityId);
				
				String industryId = INDUSTRY.get(commodityId);
				if (commodityId.equals(Commodities.FOOD) && condition.getId().equals(Conditions.WATER_SURFACE) &&
						industryId.equals(Industries.FARMING)) {
					industryId = Industries.AQUACULTURE;
				} else if (commodityId.equals(Commodities.LOBSTER) && condition.getId().equals(Conditions.VOLTURNIAN_LOBSTER_PENS) &&
						industryId.equals(Industries.FARMING)) {
					industryId = Industries.AQUACULTURE;
				}
				IndustrySpecAPI ind = Global.getSettings().getIndustrySpec(industryId);
				
				
				String str = "" + mod;
				if (mod > 0) str = "+" + mod;
				String text = "";
				if (mod == 0) {
					text = "No bonuses or penalties to " + spec.getName().toLowerCase() + " production (" + ind.getName() + ")";
				} else {
					//text = "" + str + " to " + spec.getName().toLowerCase() + " production.";
					text = "" + str + " " + spec.getName().toLowerCase() + " production (" + ind.getName() + ")";
				}
				float pad = 10f;
				tooltip.addPara(text, pad, Misc.getHighlightColor(), str);
			}
		}
	}
	
	
	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		float qty = 0f;
		if (Conditions.FARMLAND_POOR.equals(condition.getId())) {
			qty = 5f;
		} else if (Conditions.FARMLAND_ADEQUATE.equals(condition.getId())) {
			qty = 10f;
		} else if (Conditions.FARMLAND_RICH.equals(condition.getId())) {
			qty = 20f;
		} else if (Conditions.FARMLAND_BOUNTIFUL.equals(condition.getId())) {
			qty = 30f;
		} else if (Conditions.WATER_SURFACE.equals(condition.getId())) {
			qty = 10f;
		}
		if (qty > 0) {
			incoming.add(Factions.LUDDIC_CHURCH, qty);
		}
	}


	
	
}




