package com.fs.starfarer.api.combat.listeners;

import com.fs.starfarer.api.combat.CombatEntityAPI;

public interface DamageListener {
	void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result);
}
