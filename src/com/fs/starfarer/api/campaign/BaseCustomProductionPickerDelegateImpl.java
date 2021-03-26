package com.fs.starfarer.api.campaign;

import java.util.Set;

public class BaseCustomProductionPickerDelegateImpl implements CustomProductionPickerDelegate {
	public float getCostMult() {
		return 1f;
	}

	public Set<String> getAvailableFighters() {
//		List<String> result = new ArrayList<String>();
//		result.add("wasp_wing");
//		return result;
		return null;
	}
	
	public Set<String> getAvailableShipHulls() {
//		List<String> result = new ArrayList<String>();
//		result.add("onslaught");
//		return result;
		return null;
	}

	public Set<String> getAvailableWeapons() {
//		List<String> result = new ArrayList<String>();
//		result.add("hellbore");
//		return result;
		return null;
	}

	public float getMaximumValue() {
		return 100000;
	}

	public void notifyProductionSelected(FactionProductionAPI production) {
	}

}
