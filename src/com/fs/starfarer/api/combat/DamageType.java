/**
 * 
 */
package com.fs.starfarer.api.combat;



public enum DamageType {
	KINETIC(2.0f,0.5f,1.0f, "Kinetic", "200% vs shields, 50% vs armor"),
	HIGH_EXPLOSIVE(0.5f,2.0f,1.0f, "High Explosive", "200% vs armor, 50% vs shields"),
	FRAGMENTATION(0.25f,0.25f,1.0f, "Fragmentation", "25% vs shields and armor, 100% vs hull"),
	ENERGY(1.0f,1.0f,1.0f, "Energy", "100% vs shields, armor, and hull"),
	OTHER(1f,1f,1f, "Other", "Other"); // fighter launchers, etc - where damage type doesn't apply
	

	private DamageType(float shieldMult, float armorMult, float hullMult, String displayName, String description) {
		this.shieldMult = shieldMult;
		this.armorMult = armorMult;
		this.hullMult = hullMult;
		this.displayName = displayName;
		this.description = description;
	}
	
	private final String displayName;
	private final String description;
	private float shieldMult;
	private float armorMult;
	private float hullMult;
	
	public float getShieldMult() {
		return shieldMult;
	}
	public float getArmorMult() {
		return armorMult;
	}
	public float getHullMult() {
		return hullMult;
	}
	public String getDisplayName() {
		return displayName;
	}
	public String getDescription() {
		return description;
	}
	
}