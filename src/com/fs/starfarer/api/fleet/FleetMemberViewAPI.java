package com.fs.starfarer.api.fleet;

import com.fs.starfarer.api.util.ColorShifterAPI;
import com.fs.starfarer.api.util.ValueShifterAPI;

public interface FleetMemberViewAPI {

	ColorShifterAPI getEngineColor();
	ValueShifterAPI getEngineWidthMult();
	ValueShifterAPI getEngineHeightMult();
	ColorShifterAPI getEngineGlowColor();
	ValueShifterAPI getEngineGlowSizeMult();
	ColorShifterAPI getContrailColor();
	ValueShifterAPI getContrailWidthMult();
	ValueShifterAPI getContrailDurMult();
	ColorShifterAPI getGlowColor();
	FleetMemberAPI getMember();
	ColorShifterAPI getWindEffectColor();
	//ValueShifterAPI getWindEffectLengthMult();
	ValueShifterAPI getWindEffectDirX();
	ValueShifterAPI getWindEffectDirY();

}
