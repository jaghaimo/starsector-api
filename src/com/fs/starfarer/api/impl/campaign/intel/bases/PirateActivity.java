package com.fs.starfarer.api.impl.campaign.intel.bases;

import java.awt.Color;
import java.util.Map;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class PirateActivity extends BaseMarketConditionPlugin {

	protected PirateBaseIntel intel;
	
	public PirateActivity() {
	}

	@Override
	public void setParam(Object param) {
		intel = (PirateBaseIntel) param;
	}


	public void apply(String id) {
		float accessibility = intel.getAccessibilityPenalty();
		float stability = intel.getStabilityPenalty();
		String name = "Pirate activity";
		if (accessibility != 0) {
			market.getAccessibilityMod().modifyFlat(id, -accessibility, name);
		}
		if (stability != 0) {
			market.getStability().modifyFlat(id, -stability, name);
		}
	}

	public void unapply(String id) {
		market.getAccessibilityMod().unmodifyFlat(id);
		market.getStability().unmodifyFlat(id);
	}
	
	
	@Override
	public void advance(float amount) {
		if (!intel.getMarket().isInEconomy()) {
			market.removeSpecificCondition(condition.getIdForPluginModifications());
		}
	}

	public Map<String, String> getTokenReplacements() {
		return super.getTokenReplacements();
		//return event.getTokenReplacements();
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	public void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		Color h = Misc.getHighlightColor();
		Color n = Misc.getNegativeHighlightColor();
		
		float pad = 3f;
		float small = 5f;
		float opad = 10f;
		
		float accessibility = intel.getAccessibilityPenalty();
		float stability = intel.getStabilityPenalty();
		
		if (stability != 0 && accessibility != 0) {
			tooltip.addPara("%s stability, %s accessibility.", 
					opad, h,
					"-" + (int)stability, "-" + (int)Math.round(accessibility * 100f) + "%");
		} else if (stability != 0) {
			tooltip.addPara("%s stability.", 
					opad, h,
					"-" + (int)stability);
		} else if (accessibility != 0) {
			tooltip.addPara("%s accessibility.", 
					opad, h,
					"-" + (int)Math.round(accessibility * 100f) + "%");
		} else {
			tooltip.addPara("No perceptible impact on operations as of yet.", opad);
		}
	}

	@Override
	public float getTooltipWidth() {
		return super.getTooltipWidth();
	}

	@Override
	public boolean hasCustomTooltip() {
		return true;
	}

	@Override
	public boolean isTooltipExpandable() {
		return super.isTooltipExpandable();
	}

	public PirateBaseIntel getIntel() {
		return intel;
	}
	
}





