package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.ui.HintPanelAPI;

public interface CoreUIAPI {
	CoreUITradeMode getTradeMode();
	HintPanelAPI getHintPanel();
}
