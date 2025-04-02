package com.fs.starfarer.api.campaign;

import java.util.List;

import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;

public interface IntelDataAPI {
	List<SectorEntityToken> getCommSnifferLocations();

	boolean isInShowMap();
	void setInShowMap(boolean inShowMap);

	IntelInfoPlugin getSelectedIntel();
	void setSelectedIntel(IntelInfoPlugin selectedIntel);
}
