package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamManager;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public interface SlipstreamConfigUpdater {
	void updateSlipstreamConfig(String prevConfig, WeightedRandomPicker<String> nextConfigPicker, SlipstreamManager manager);
}
