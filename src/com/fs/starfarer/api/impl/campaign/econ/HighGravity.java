package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;



public class HighGravity extends BaseHazardCondition {

	public static final float ACCESS_BONUS = 10f;
	
	public void apply(String id) {
		super.apply(id);
		market.getAccessibilityMod().modifyFlat(id, -ACCESS_BONUS/100f, "High gravity");
	}
	
	public void unapply(String id) {
		super.unapply(id);
		market.getAccessibilityMod().unmodifyFlat(id);
	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		
		tooltip.addPara("%s accessibility", 
						10f, Misc.getHighlightColor(),
						"-" + (int)ACCESS_BONUS + "%");
	}
}





