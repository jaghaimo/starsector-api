package com.fs.starfarer.api.campaign;

import java.util.Set;


public interface CustomProductionPickerDelegate {

	public Set<String> getAvailableShipHulls();
	public Set<String> getAvailableWeapons();
	public Set<String> getAvailableFighters();
	
	public float getCostMult();
	public float getMaximumValue();
	
	public void notifyProductionSelected(FactionProductionAPI production);
}
