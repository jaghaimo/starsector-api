package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;

public class ExampleCustomUIPanel extends BaseCustomUIPanelPlugin {

	private PositionAPI p;
	private SpriteAPI sprite;

	private float mouseX, mouseY;
	
	public ExampleCustomUIPanel() {
		sprite = Global.getSettings().getSprite("graphics/ships/wolf/wolf_base.png");
	}

	public void positionChanged(PositionAPI position) {
		p = position;
		mouseX = p.getX() + p.getWidth() / 2f;
		mouseY = p.getY() + p.getHeight() / 2f;
	}
	
	public void advance(float amount) {
		if (p == null) return;
		
	}

	public void processInput(List<InputEventAPI> events) {
		if (p == null) return;
		
		for (InputEventAPI event : events) {
			if (event.isConsumed()) continue;
			
			if (event.isMouseMoveEvent()) {
				if (p.containsEvent(event)) {
					mouseX = event.getX();
					mouseY = event.getY();
					//System.out.println("x,y: " + x + "," + y);
				} else {
					mouseX = p.getX() + p.getWidth() / 2f;
					mouseY = p.getY() + p.getHeight() / 2f;
				}
			}
		}
	}

	public void render(float alphaMult) {
		if (p == null) return;
		
		float x = p.getX();
		float y = p.getY();
		float w = p.getWidth();
		float h = p.getHeight();
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		Color color = Color.cyan;
		
		GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)(color.getAlpha() * alphaMult * 0.25f));
		
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glVertex2f(x, y);
			GL11.glVertex2f(x, y + h);
			GL11.glVertex2f(x + w, y + h);
			GL11.glVertex2f(x + w, y);
		}
		GL11.glEnd();
		
//		mouseX = Mouse.getX();
//		mouseY = Mouse.getY();
		sprite.setAlphaMult(alphaMult);
		sprite.renderAtCenter(mouseX, mouseY);
		
	}

	public void renderBelow(float alphaMult) {
		
	}

	@Override
	public void buttonPressed(Object buttonId) {
		super.buttonPressed(buttonId);
	}
	
	

}
