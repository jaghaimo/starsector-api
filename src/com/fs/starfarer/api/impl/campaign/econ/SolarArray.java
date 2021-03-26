package com.fs.starfarer.api.impl.campaign.econ;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class SolarArray extends BaseMarketConditionPlugin {
	
	public static int FARMING_BONUS = 2;
	
	public static List<String> SUPPRESSED_CONDITIONS = new ArrayList<String>();
	static {
		SUPPRESSED_CONDITIONS.add(Conditions.HOT);
		SUPPRESSED_CONDITIONS.add(Conditions.POOR_LIGHT);
	}
	
	public void apply(String id) {
		for (String cid : SUPPRESSED_CONDITIONS) {
			market.suppressCondition(cid);
		}
		
		Industry industry = getIndustry();
		if (industry != null) {
			industry.getSupplyBonusFromOther().modifyFlat(id, FARMING_BONUS, Misc.ucFirst(condition.getName().toLowerCase()));
		}
		//industry.supply(id + "_0", commodityId, base, BaseIndustry.BASE_VALUE_TEXT);
	}

	public void unapply(String id) {
		for (String cid : SUPPRESSED_CONDITIONS) {
			market.unsuppressCondition(cid);
		}
		
		Industry industry = getIndustry();
		if (industry != null) {
			industry.getSupplyBonusFromOther().unmodifyFlat(id);
		}
	}
	
	protected Industry getIndustry() {
		Industry industry = market.getIndustry(Industries.FARMING);
		if (industry == null) {
			industry = market.getIndustry(Industries.AQUACULTURE);
		}
		return industry;
	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		float opad = 10f;
		
		List<String> conds = new ArrayList<String>();
		for (String id : SUPPRESSED_CONDITIONS) {
			MarketConditionSpecAPI mc = Global.getSettings().getMarketConditionSpec(id);
			conds.add(mc.getName());
		}
		
		tooltip.addPara("Counters the effects of " + Misc.getAndJoined(conds) + ".\n\nIncreases food production by %s (Farming).",  
				opad, Misc.getHighlightColor(), 
				"" + FARMING_BONUS);
	}
}




