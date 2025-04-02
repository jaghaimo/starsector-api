package com.fs.starfarer.api.combat;

public class ShipAIConfig implements Cloneable {

	
	@Override
	public ShipAIConfig clone() {
		try {
			return (ShipAIConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			return null; // should not happen
		}
	}

	public boolean alwaysStrafeOffensively = false;
	public boolean backingOffWhileNotVentingAllowed = true;
	public boolean turnToFaceWithUndamagedArmor = true;
	
	public boolean burnDriveIgnoreEnemies = false;
	
	public String personalityOverride = null;

	public ShipAIConfig() {
	}
	
	public void copyFrom(ShipAIConfig other) {
		if (other == null) {
			return;
		}
		alwaysStrafeOffensively = other.alwaysStrafeOffensively;
		backingOffWhileNotVentingAllowed = other.backingOffWhileNotVentingAllowed;
		turnToFaceWithUndamagedArmor = other.turnToFaceWithUndamagedArmor;
		burnDriveIgnoreEnemies = other.burnDriveIgnoreEnemies;
		personalityOverride = other.personalityOverride;
	}
}
