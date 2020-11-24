package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class OverheadTooltipCreator implements TooltipCreator {

	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
//		FDNode node = (FDNode) tooltipParam;
//
//		float pad = 3f;
//		float opad = 10f;
//		Color h = Misc.getHighlightColor();
//		Color g = Misc.getGrayColor();
//	
//		MarketAPI market = (MarketAPI) node.custom;
//		
//		OverheadData overhead = CoreScript.computeOverhead(market);
//		
//		tooltip.addPara("Overhead due to scaling up operations to meet demand from multiple sources.", opad);
//		
//		if (overhead.max != null) { // shouldn't be possible, but just in case
//			SupplierData sd = overhead.max;
//			CommodityOnMarketAPI com = sd.getCommodity();
//			String units = "units";
//			if (sd.getQuantity() == 1) units = "unit";
//			tooltip.addPara("The highest-value export from " + market.getName() + " is %s " + units + 
//					" of " + com.getCommodity().getName() + " sold to " + sd.getMarket().getName() + 
//					" for %s per month.",
//					opad,
//					h,
//					"" + sd.getQuantity(), Misc.getDGSCredits(sd.getExportValue(market)));
//			
//			tooltip.addPara("The total value of exports from " + market.getName() + " is %s, " +
//					"resulting in approximately %s overhead.",
//					opad, h,
//					Misc.getDGSCredits(market.getExportIncome(false)),
//					"" + (int)Math.round(overhead.fraction * 100f) + "%");
//		}
//		
//		tooltip.addPara("More exports always result in more income, but the gains decrease " +
//				"exponentially with more export destinations being shipped to.", g, opad);
//		tooltip.addPara("The overhead amount is based on the highest single export; the higher it is, the lower " +
//				"the overall amount of overhead.", g, opad);
	}
	
	

	public float getTooltipWidth(Object tooltipParam) {
		return 450;
	}

	public boolean isTooltipExpandable(Object tooltipParam) {
		return false;
	}

}
