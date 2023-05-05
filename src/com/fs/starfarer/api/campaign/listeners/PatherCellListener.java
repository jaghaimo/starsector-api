package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathCellsIntel;

public interface PatherCellListener {
	void reportCellsDisrupted(LuddicPathCellsIntel cell);
}
