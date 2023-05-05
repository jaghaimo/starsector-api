package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.combat.ViewportAPI;

public interface CampaignUIRenderingListener {
	void renderInUICoordsBelowUI(ViewportAPI viewport);
	void renderInUICoordsAboveUIBelowTooltips(ViewportAPI viewport);
	void renderInUICoordsAboveUIAndTooltips(ViewportAPI viewport);
}
