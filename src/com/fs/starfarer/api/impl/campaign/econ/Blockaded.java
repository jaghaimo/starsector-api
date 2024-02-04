package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;

import com.fs.starfarer.api.impl.campaign.intel.group.BlockadeFGI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class Blockaded extends BaseMarketConditionPlugin {

	//public static float ACCESSIBILITY_PENALTY = 0.5f;
	
	protected BlockadeFGI blockade;
	
	public Blockaded() {
	}
	
	@Override
	public void setParam(Object param) {
		blockade = (BlockadeFGI) param;
	}




	public void apply(String id) {
		if (blockade == null) return;
		market.getAccessibilityMod().modifyFlat(id, -blockade.getAccessibilityPenalty(), Misc.ucFirst(getName().toLowerCase()));
	}

	public void unapply(String id) {
		market.getAccessibilityMod().unmodifyFlat(id);
	}
	
	@Override
	public void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		if (blockade == null) return;
		
		Color h = Misc.getHighlightColor();
		float opad = 10f;
		
		tooltip.addPara("%s accessibility.", 
				opad, h,
				"-" + (int)Math.round(blockade.getAccessibilityPenalty() * 100f) + "%");
	}

	@Override
	public boolean hasCustomTooltip() {
		return true;
	}


	@Override
	public boolean isTransient() {
		return false;
	}
	
	

}





