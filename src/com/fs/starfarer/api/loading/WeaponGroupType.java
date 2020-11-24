/**
 * 
 */
package com.fs.starfarer.api.loading;


public enum WeaponGroupType {
	LINKED("Linked"),
	ALTERNATING("Alternating");
	
	private String displayName;

	private WeaponGroupType(String displayName) {
		this.displayName = displayName;
	}
	public String getDisplayName() {
		return displayName;
	}

}