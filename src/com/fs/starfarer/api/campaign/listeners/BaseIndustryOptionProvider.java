package com.fs.starfarer.api.campaign.listeners;

import java.util.List;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.Industry.IndustryTooltipMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class BaseIndustryOptionProvider implements IndustryOptionProvider {

	public List<IndustryOptionData> getIndustryOptions(Industry ind) {
		return null;
	}

	
	public boolean isUnsuitable(Industry ind, boolean allowUnderConstruction) {
		if (ind == null) return true;
		if (ind.getMarket() == null) return true;
		if (!allowUnderConstruction && ind.isBuilding() && !ind.isUpgrading()) return true;
		return false;
		
	}


	public void createTooltip(IndustryOptionData opt, TooltipMakerAPI tooltip, float width) {
		
	}

	public void optionSelected(IndustryOptionData opt, DialogCreatorUI ui) {
		
	}

	public void addToIndustryTooltip(Industry ind, IndustryTooltipMode mode, TooltipMakerAPI tooltip, float width, boolean expanded) {
		
	}
}
