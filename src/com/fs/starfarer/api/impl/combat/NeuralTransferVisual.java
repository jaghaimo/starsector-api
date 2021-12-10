package com.fs.starfarer.api.impl.combat;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class NeuralTransferVisual extends BaseCombatLayeredRenderingPlugin {

	public static float TEX_SCROLL_SPEED = 1f;
	
	protected ShipAPI from;
	protected ShipAPI to;
	protected float duration;
	
	protected FaderUtil fader = new FaderUtil(0f, 0.5f, 0.5f);

	protected SpriteAPI sprite;
	protected float texProgress = 0f;
	
	public NeuralTransferVisual(ShipAPI from, ShipAPI to, float duration) {
		this.from = from;
		this.to = to;
		this.duration = duration;
		
		sprite = Global.getSettings().getSprite("misc", "neural_transfer_beam");
		fader.fadeIn();
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		if (Global.getCombatEngine().isPaused()) return;
		
		entity.getLocation().set(to.getShieldCenterEvenIfNoShield());
		fader.advance(amount);
		duration -= amount;
		if (!fader.isFadingOut() && duration <= 0f) {
			fader.fadeOut();
		}
		
		texProgress -= TEX_SCROLL_SPEED * amount * 2f;
	}

	@Override
	public float getRenderRadius() {
		return to.getShieldRadiusEvenIfNoShield() * 3f + 200f;
	}

	@Override
	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		super.render(layer, viewport);
		
		
		//float length = to.getShieldRadiusEvenIfNoShield() * 3f + 100f;
		float length1 = 500f;
		length1 = Math.min(length1, Misc.getDistance(from.getLocation(), to.getLocation()));
		float length2 = to.getShieldRadiusEvenIfNoShield();
		if (length1 < length2) length1 = length2;
		length1 += length2;
		float w1 = to.getShieldRadiusEvenIfNoShield() * 0.3f;
		float w2 = to.getShieldRadiusEvenIfNoShield() * 1.5f;
		float w3 = to.getShieldRadiusEvenIfNoShield() * 2.5f;
		float wMult = 0.33f;
		w1 *= wMult;
		w2 *= wMult;
		w3 *= wMult;
		//w1 = w2 = 400;
		
		float angle = Misc.getAngleInDegrees(from.getLocation(), to.getLocation());
		
		Vector2f dest = new Vector2f(to.getShieldCenterEvenIfNoShield());
		Vector2f src = Misc.getUnitVectorAtDegreeAngle(angle + 180f);
		src.scale(length1 - length2/2f);
		Vector2f.add(src, dest, src);
		
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
		Vector2f dest2 = new Vector2f(dir);
		dest2.scale(length2/3f * 1f);
		Vector2f.add(dest2, dest, dest2);
		Vector2f dest1 = new Vector2f(dir);
		dest1.scale(-length2/3f * 2f);
		Vector2f.add(dest1, dest, dest1);
		
		
		Vector2f perp = Misc.getUnitVectorAtDegreeAngle(angle + 90);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL11.glDisable(GL11.GL_TEXTURE_2D);
		sprite.bindTexture();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		
		Color color = new Color(0,121,216,255);
		
		boolean wireframe = false;
		//wireframe = true;
		if (wireframe) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			//GL11.glDisable(GL11.GL_BLEND);
		}

		float alpha = (float) (viewport.getAlphaMult() * Math.sqrt(fader.getBrightness()));
		alpha *= 0.5f;
		alpha *= (0.5f + Math.min(0.5f, 0.5f * w2 / 360f));
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		Misc.setColor(color, alpha * 1f);
		GL11.glTexCoord2f(0.5f + texProgress, 0.5f);
		GL11.glVertex2f((src.x + dest1.x)/2f, (src.y + dest1.y)/2f);
		
		Misc.setColor(color, alpha * 0f);
		GL11.glTexCoord2f(0f + texProgress, 0f);
		GL11.glVertex2f(src.x + perp.x * w1/2f, src.y + perp.y * w1/2f);
		GL11.glTexCoord2f(0f + texProgress, 1f);
		GL11.glVertex2f(src.x - perp.x * w1/2f, src.y - perp.y * w1/2f);
		
		Misc.setColor(color, alpha * 1f);
		GL11.glTexCoord2f(1f + texProgress, 1f);
		GL11.glVertex2f(dest1.x - perp.x * w2/2f, dest1.y - perp.y * w2/2f);
		GL11.glTexCoord2f(1f + texProgress, 0f);
		GL11.glVertex2f(dest1.x + perp.x * w2/2f, dest1.y + perp.y * w2/2f);
		
		Misc.setColor(color, alpha * 0f);
		GL11.glTexCoord2f(0f + texProgress, 0f);
		GL11.glVertex2f(src.x + perp.x * w1/2f, src.y + perp.y * w1/2f);
		GL11.glEnd();
		
		float th = length2 / length1;
		
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		Misc.setColor(color, alpha * 1f);
		GL11.glTexCoord2f(0.5f + texProgress + th, 0.5f);
		GL11.glVertex2f((dest1.x + dest2.x)/2f, (dest1.y + dest2.y)/2f);
		
		Misc.setColor(color, alpha * 1f);
		GL11.glTexCoord2f(0f + texProgress, 0f);
		GL11.glVertex2f(dest1.x + perp.x * w2/2f, dest1.y + perp.y * w2/2f);
		GL11.glTexCoord2f(0f + texProgress, 1f);
		GL11.glVertex2f(dest1.x - perp.x * w2/2f, dest1.y - perp.y * w2/2f);
		
		Misc.setColor(color, alpha * 0f);
		GL11.glTexCoord2f(1f + texProgress + th, 1f);
		GL11.glVertex2f(dest2.x - perp.x * w3/2f, dest2.y - perp.y * w3/2f);
		GL11.glTexCoord2f(1f + texProgress + th, 0f);
		GL11.glVertex2f(dest2.x + perp.x * w3/2f, dest2.y + perp.y * w3/2f);
		
		Misc.setColor(color, alpha * 1f);
		GL11.glTexCoord2f(0f + texProgress + th, 0f);
		GL11.glVertex2f(dest1.x + perp.x * w2/2f, dest1.y + perp.y * w2/2f);
		GL11.glEnd();
		
		if (wireframe) GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		
	}

	@Override
	public boolean isExpired() {
		return fader.isFadedOut() && duration <= 0f;
	}

	
	
	
	
}
