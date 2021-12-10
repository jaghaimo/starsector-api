package com.fs.starfarer.api.campaign;

import java.util.List;

public interface IntelDataAPI {
	List<SectorEntityToken> getCommSnifferLocations();

	boolean isInShowMap();
	void setInShowMap(boolean inShowMap);
}
