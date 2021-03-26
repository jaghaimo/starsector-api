package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface ColonyOtherFactorsListener {
	void printOtherFactors(TooltipMakerAPI text, SectorEntityToken entity);
	boolean isActiveFactorFor(SectorEntityToken entity);
}
