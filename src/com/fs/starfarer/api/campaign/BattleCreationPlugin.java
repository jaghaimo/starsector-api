package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;

public interface BattleCreationPlugin {
	void initBattle(BattleCreationContext context, MissionDefinitionAPI api);
	void afterDefinitionLoad(CombatEngineAPI engine);
}
