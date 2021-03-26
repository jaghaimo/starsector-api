package com.fs.starfarer.api.impl.campaign.graid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.GroundRaidObjectivesListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidType;

public class StandardGroundRaidObjectivesCreator implements GroundRaidObjectivesListener {

	public void modifyRaidObjectives(MarketAPI market, SectorEntityToken entity, List<GroundRaidObjectivePlugin> objectives, RaidType type, int marineTokens, int priority) {
		if (priority != 0) return;
		if (market == null) return;
		
		if (type == RaidType.VALUABLE) { 
			Map<CommodityOnMarketAPI, Float> raidValuables = computeRaidValuables(market);
			List<CommodityOnMarketAPI> commodities = new ArrayList<CommodityOnMarketAPI>(raidValuables.keySet());
			for (CommodityOnMarketAPI com : commodities) {
				CommodityGroundRaidObjectivePluginImpl curr = new CommodityGroundRaidObjectivePluginImpl(market, com.getId());
				if (curr.getQuantity(1) <= 0) continue;
				objectives.add(curr);
			}
			
			for (Industry ind : market.getIndustries()) {
				String coreId = ind.getAICoreId();
				if (coreId != null) {
					AICoreGroundRaidObjectivePluginImpl core = new AICoreGroundRaidObjectivePluginImpl(market, coreId, ind);
					objectives.add(core);
				}
				SpecialItemData sid = ind.getSpecialItem();
				if (sid != null) {
					SpecialItemRaidObjectivePluginImpl special = new SpecialItemRaidObjectivePluginImpl(market, 
																		sid.getId(), null, ind);
					objectives.add(special);
					
				}
			}
			
			// a bit confusing, and also hard to balance - either the best option or the worst, not much in-between 
//			CreditsGroundRaidObjectivePluginImpl credits = new CreditsGroundRaidObjectivePluginImpl(market);
//			if (credits.getQuantity(1) > 0) {
//				objectives.add(credits);
//			}
			
			ShipWeaponsGroundRaidObjectivePluginImpl weapons = new ShipWeaponsGroundRaidObjectivePluginImpl(market);
			if (weapons.getQuantity(1) > 0) {
				objectives.add(weapons);
			}
			
			BlueprintGroundRaidObjectivePluginImpl blueprints = new BlueprintGroundRaidObjectivePluginImpl(market);
			if (blueprints.getQuantity(1) > 0) {
				objectives.add(blueprints);
			}
			
			if (market.hasCondition(Conditions.SOLAR_ARRAY)) {
				objectives.add(new SolarArrayGroundRaidObjectivePluginImpl(market));
			}
			
		} else if (type == RaidType.DISRUPT) {
			for (Industry ind : market.getIndustries()) {
				if (ind.getSpec().hasTag(Industries.TAG_UNRAIDABLE)) continue;
				
				DisruptIndustryRaidObjectivePluginImpl curr = new DisruptIndustryRaidObjectivePluginImpl(market, ind);
				if (curr.getBaseDisruptDuration(marineTokens) <= 0) continue;
				objectives.add(curr);
			}
		}
	}

	protected Map<CommodityOnMarketAPI, Float> computeRaidValuables(MarketAPI market) {
		Map<CommodityOnMarketAPI, Float> result = new HashMap<CommodityOnMarketAPI, Float>();
		for (CommodityOnMarketAPI com : market.getAllCommodities()) {
			if (com.isPersonnel()) continue;
			if (com.getCommodity().hasTag(Commodities.TAG_META)) continue;
			if (com.getCommodity().hasTag(Commodities.TAG_NON_ECONOMIC)) continue;
			
			int a = com.getAvailable();
			if (a > 0) {
				float num = BaseIndustry.getSizeMult(a) * com.getCommodity().getEconUnit() * 0.5f;
				result.put(com, num);
			}
		}
		return result;
	}
	
	
	public void reportRaidObjectivesAchieved(RaidResultData data, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		
	}

}
