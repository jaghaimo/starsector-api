package com.fs.starfarer.api.impl.campaign.eventide;

public interface DuelBackground {
	
	void advance(float amount);
	void render(DuelPanel panel, float xOffset, float yOffset, float alphaMult);
	void renderForeground(DuelPanel panel, float xOffset, float yOffset, float alphaMult);

	float getStageWidth();
}
