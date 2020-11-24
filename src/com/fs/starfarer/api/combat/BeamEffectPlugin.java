package com.fs.starfarer.api.combat;

public interface BeamEffectPlugin {
	void advance(float amount, CombatEngineAPI engine, BeamAPI beam);
}
