package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface CommodityTooltipModifier {
	void addSectionAfterPrice(TooltipMakerAPI info, float width, boolean expanded, CargoStackAPI stack); 
}
