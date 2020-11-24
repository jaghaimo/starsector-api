package com.fs.starfarer.api.ui;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;

public interface CustomPanelAPI extends UIPanelAPI {

	CustomPanelAPI createCustomPanel(float width, float height, CustomUIPanelPlugin plugin);

	TooltipMakerAPI createUIElement(float width, float height, boolean withScroller);
	PositionAPI addUIElement(TooltipMakerAPI element);

}
