package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI.GenericPlugin;

public interface CommodityDescriptionProvider extends GenericPlugin {
	public String getTooltipTitle(CargoStackAPI stack);
	public String getTooltipDescription(CargoStackAPI stack);
}
