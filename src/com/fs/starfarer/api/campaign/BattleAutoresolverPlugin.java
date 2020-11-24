package com.fs.starfarer.api.campaign;

public interface BattleAutoresolverPlugin {
	void resolve();
	
	FleetEncounterContextPlugin getContext();
}
