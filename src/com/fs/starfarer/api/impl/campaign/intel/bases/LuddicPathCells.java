package com.fs.starfarer.api.impl.campaign.intel.bases;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class LuddicPathCells extends BaseMarketConditionPlugin {

	public static int STABLITY_PENALTY = 1;
	
	protected LuddicPathCellsIntel intel;
	
	public LuddicPathCells() {
	}

	@Override
	public void setParam(Object param) {
		intel = (LuddicPathCellsIntel) param;
	}
	
	public LuddicPathCellsIntel getIntel() {
		return intel;
	}

	public void apply(String id) {
		if (!intel.isSleeper()) {
			float stability = STABLITY_PENALTY;
			String name = "Active Luddic Path cells";
			if (stability != 0) {
				market.getStability().modifyFlat(id, -stability, name);
			}
		}
	}

	public void unapply(String id) {
		market.getStability().unmodifyFlat(id);
	}
	
	
	@Override
	public void advance(float amount) {
		
	}

	@Override
	public String getIconName() {
		return intel.getIcon();
	}

	@Override
	public String getName() {
		return intel.getName();
	}


	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		
		//Color color = market.getTextColorForFactionOrPlanet();
		Color color = Global.getSector().getFaction(Factions.LUDDIC_PATH).getBaseUIColor();
		tooltip.addTitle(condition.getName(), color);
		
		intel.createSmallDescription(tooltip, 0, 0);
	}

//	@Override
//	public void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
//		Color h = Misc.getHighlightColor();
//		Color n = Misc.getNegativeHighlightColor();
//		
//		float pad = 3f;
//		float small = 5f;
//		float opad = 10f;
//		
//
//		if (!intel.isSleeper()) {
//			float stability = STABLITY_PENALTY;
//			tooltip.addPara("%s stability. Possibility of various acts of terror and sabotage, " +
//					"if smugglers from a Luddic Path base are able to provide material support.", 
//					opad, h,
//					"-" + (int)stability);
//		} else {
//			tooltip.addPara("No perceptible impact on operations as of yet.", opad);
//		}
//	}

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

	@Override
	public boolean isTransient() {
		return false;
	}

}





