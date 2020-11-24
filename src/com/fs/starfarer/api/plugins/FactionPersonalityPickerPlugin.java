package com.fs.starfarer.api.plugins;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public interface FactionPersonalityPickerPlugin {
	WeightedRandomPicker<String> createPersonalityPicker(FactionAPI faction);
	void addDescToTooltip(TooltipMakerAPI tooltip, int level);
}
