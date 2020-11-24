/**
 * 
 */
package com.fs.starfarer.api.fleet;

public class ShipRolePick {
	public String variantId;
	public float weight = 1f;
	public ShipRolePick(String variantId) {
		this.variantId = variantId;
	}
	public boolean isFighterWing() {
		return variantId != null && variantId.endsWith("_wing");
	}
}