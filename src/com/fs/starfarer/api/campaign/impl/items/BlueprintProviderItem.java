package com.fs.starfarer.api.campaign.impl.items;

import java.util.List;


public interface BlueprintProviderItem {
	List<String> getProvidedShips();
	List<String> getProvidedWeapons();
	List<String> getProvidedFighters();
	List<String> getProvidedIndustries();
}
