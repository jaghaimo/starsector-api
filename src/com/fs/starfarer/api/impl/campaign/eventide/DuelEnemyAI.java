package com.fs.starfarer.api.impl.campaign.eventide;

public interface DuelEnemyAI {
	void advance(float amount, DuelPanel duel);
	void render(float alphaMult);
}
