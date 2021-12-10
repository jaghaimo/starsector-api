package com.fs.starfarer.api.impl.campaign.eventide;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;

public class EventideDuelBackground extends BaseDuelBackground {

	protected SpriteAPI bg;
	protected SpriteAPI column;
	protected SpriteAPI platform;
	protected SpriteAPI tower;
	protected SpriteAPI conduits;
	protected SpriteAPI foreground_side;
	
	public EventideDuelBackground() {
		bg = loadTex("graphics/misc/eventide_bg.jpg");
		column = loadTex("graphics/misc/column.png");
		platform = loadTex("graphics/misc/fight_platform.png");
		tower = loadTex("graphics/misc/midground_tower1.png");
		conduits = loadTex("graphics/misc/under_conduits.png");
		foreground_side = loadTex("graphics/misc/foreground_side.png");
	}
	
	
	public void advance(float amount) {
		
	}

	public void render(DuelPanel panel, float xOffset, float yOffset, float alphaMult) {
		PositionAPI p = panel.getPosition();
		float x = p.getX();
		float y = p.getY();
		float w = p.getWidth();
		
		//bg.render(p.getX(), p.getY() + (p.getHeight() - bg.getHeight()));
		bg.render(x, y);
		
		float columnXOffset = xOffset * 0.5f; //0.25f;
		float columnYOffset = yOffset * 0.5f; //0.25f;
		float towerXOffset = xOffset * 0.25f; //0.5f;
		float towerYOffset = yOffset * 0.25f; //0.5f;
		
		
		float platformY = panel.getFloorLevel() -
						  platform.getHeight() + 40 + //64 + //92 +  
						  yOffset;
		float columnY = platformY + 92f + columnYOffset;
		float columnSpacing = column.getWidth() - 60f;
		float columnX = x + columnXOffset - 375f; //140f;
		
		tower.render(x + w - tower.getWidth() + towerXOffset, y + towerYOffset);
		
		column.render(columnX + columnSpacing * 3f, columnY);
		column.render(columnX + columnSpacing * 2f, columnY);
		column.render(columnX + columnSpacing, columnY);
		column.render(columnX, columnY);
		
		conduits.setWidth(getStageWidth());
		conduits.render(p.getX() - (getStageWidth() - p.getWidth()) / 2f + xOffset, platformY - conduits.getHeight() + 130);
		
		platform.setWidth(getStageWidth());
		platform.render(p.getX() - (getStageWidth() - p.getWidth()) / 2f + xOffset, platformY);
		//platform.render(p.getX() + xOffset, platformY);
		
		
	}
	
	public void renderForeground(DuelPanel panel, float xOffset, float yOffset, float alphaMult) {
		PositionAPI p = panel.getPosition();
		float x = p.getX();
		float y = p.getY();
		foreground_side.render( getStageWidth() -foreground_side.getWidth() + xOffset, y);
	}


	public float getStageWidth() {
		//return 1024f + 512f;
		return 2048f;
	}

}
