package com.fs.starfarer.api.impl.campaign.eventide;

import java.io.IOException;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;

public class BaseDuelBackground implements DuelBackground {

	protected SpriteAPI loadTex(String tex) {
		try {
			Global.getSettings().loadTexture(tex);
			return Global.getSettings().getSprite(tex);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void advance(float amount) {
		
	}

	public void render(DuelPanel panel, float xOffset, float yOffset, float alphaMult) {
		
	}

	public float getStageWidth() {
		return 1024f;
	}

	public void renderForeground(DuelPanel panel, float xOffset, float yOffset, float alphaMult) {
		
	}

}
