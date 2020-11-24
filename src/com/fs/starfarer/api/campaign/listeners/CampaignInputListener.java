package com.fs.starfarer.api.campaign.listeners;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;

public interface CampaignInputListener {
	/**
	 * Higher number = higher priority, i.e. gets to process input first.
	 * @return
	 */
	int getListenerInputPriority();
	void processCampaignInputPreCore(List<InputEventAPI> events);
	void processCampaignInputPreFleetControl(List<InputEventAPI> events);
	void processCampaignInputPostCore(List<InputEventAPI> events);
}
