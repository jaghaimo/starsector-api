package com.fs.starfarer.api.combat;

public class ShipAIConfig {

	public boolean alwaysStrafeOffensively = false;
	public boolean backingOffWhileNotVentingAllowed = true;
	public boolean turnToFaceWithUndamagedArmor = true;
	
	public boolean burnDriveIgnoreEnemies = false;
	
	public String personalityOverride = null;

	public ShipAIConfig() {
	}
	
	
}
