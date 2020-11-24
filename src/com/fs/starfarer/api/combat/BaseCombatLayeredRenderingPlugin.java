package com.fs.starfarer.api.combat;

import java.util.EnumSet;

public class BaseCombatLayeredRenderingPlugin implements CombatLayeredRenderingPlugin {
	
	protected CombatEngineLayers layer = CombatEngineLayers.BELOW_INDICATORS_LAYER;
	
	public BaseCombatLayeredRenderingPlugin() {
		super();
	}
	
	public BaseCombatLayeredRenderingPlugin(CombatEngineLayers layer) {
		this.layer = layer;
	}


	public void advance(float amount) {
	}

	public void cleanup() {
	}

	public EnumSet<CombatEngineLayers> getActiveLayers() {
		return EnumSet.of(layer);
	}

	public float getRenderRadius() {
		return 100;
	}

	public void init() {
		
	}

	public boolean isExpired() {
		return false;
	}

	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
//		float x = 0;
//		float y = 0f;
//		float w = 100;
//		float h = 100;
//		Color color = Color.cyan;
//		float a = 0.25f;
//		if (layer == CombatEngineLayers.BELOW_INDICATORS_LAYER) {
//			x = 50;
//			y = 50;
//			w = 100;
//			h = 100;
//			color = Color.cyan;
//		} else if (layer == CombatEngineLayers.BELOW_PHASED_SHIPS_LAYER) {
//			x = -50;
//			y = 120;
//			w = 50;
//			h = 50;
//			color = new Color(150, 150, 0, 255);
//			a = 1f;
//		} else if (layer == CombatEngineLayers.BELOW_SHIPS_LAYER) {
//			x = -100;
//			y = -100;
//			w = 200;
//			h = 200;
//			color = Color.ORANGE;
//		}
//		
//		float alphaMult = viewport.getAlphaMult();
//		
//		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		GL11.glEnable(GL11.GL_BLEND);
//		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//
//		
//		GL11.glColor4ub((byte)color.getRed(),
//						(byte)color.getGreen(),
//						(byte)color.getBlue(),
//						(byte)(color.getAlpha() * alphaMult * a));
//		
//		GL11.glBegin(GL11.GL_QUADS);
//		{
//			GL11.glVertex2f(x, y);
//			GL11.glVertex2f(x, y + h);
//			GL11.glVertex2f(x + w, y + h);
//			GL11.glVertex2f(x + w, y);
//		}
//		GL11.glEnd();
	}

}



