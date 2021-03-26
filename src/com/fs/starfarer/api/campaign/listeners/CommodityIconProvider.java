package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI.GenericPlugin;

public interface CommodityIconProvider extends GenericPlugin {
	public String getRankIconName(CargoStackAPI stack);
	public String getIconName(CargoStackAPI stack);
}
