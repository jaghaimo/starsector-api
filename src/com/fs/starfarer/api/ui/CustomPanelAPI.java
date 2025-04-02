package com.fs.starfarer.api.ui;

import java.awt.Color;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;

public interface CustomPanelAPI extends UIPanelAPI {

	CustomPanelAPI createCustomPanel(float width, float height, CustomUIPanelPlugin plugin);

	TooltipMakerAPI createUIElement(float width, float height, boolean withScroller);
	PositionAPI addUIElement(TooltipMakerAPI element);
	
	CustomUIPanelPlugin getPlugin();
	
	
	/**
	 * Returns the intel UI; only works when creating large intel descriptions.
	 * @return
	 */
	IntelUIAPI getIntelUI();

	UIPanelAPI wrapTooltipWithBox(TooltipMakerAPI tooltip);
	UIPanelAPI wrapTooltipWithBox(TooltipMakerAPI tooltip, Color color);
	UIPanelAPI wrapTooltipWithBox(TooltipMakerAPI tooltip, float padLeft, float padRight, float padBelow, float padAbove, Color color);

	void updateUIElementSizeAndMakeItProcessInput(TooltipMakerAPI element);

}
