package com.fs.starfarer.api.ui;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;

public interface SectorMapAPI {
	SectorEntityToken getConstellationLabelEntity(Constellation c);
	SectorEntityToken getIntelIconEntity(IntelInfoPlugin plugin);
}
