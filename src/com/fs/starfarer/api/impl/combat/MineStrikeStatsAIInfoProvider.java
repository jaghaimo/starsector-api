package com.fs.starfarer.api.impl.combat;

import com.fs.starfarer.api.combat.ShipAPI;

public interface MineStrikeStatsAIInfoProvider {
	float getFuseTime();
	float getMineRange(ShipAPI ship);
	//float getMineRange();
}
