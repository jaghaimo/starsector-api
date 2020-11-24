package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;

public class RingRenderer {

	
	private SpriteAPI texture;


	public RingRenderer(String cat, String key) {
		texture = Global.getSettings().getSprite(cat, key);
	}
	
	
	public void render(Vector2f loc, float minR, float maxR, Color color, boolean spiral, float factor, float alphaMult) {
		
		float middleRadius = minR + (maxR - minR) / 2f;
		middleRadius *= factor;
		
		float angle = 0f;
		
		float circ = (float) (Math.PI * 2f * middleRadius);
		float pixelsPerSegment = 5f;
		//float segments = circ / pixelsPerSegment;
		float segments = Math.round(circ / pixelsPerSegment);
		
		float startRad = (float) Math.toRadians(0);
		float endRad = (float) Math.toRadians(360f);
		float spanRad = Misc.normalizeAngle(endRad - startRad);
		float anglePerSegment = spanRad / segments;
		
		float x = loc.x;
		float y = loc.y;
		
		GL11.glPushMatrix();
		GL11.glTranslatef(x * factor, y * factor, 0);
		GL11.glRotatef(angle, 0, 0, 1);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		texture.bindTexture();
		//GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)((float) color.getAlpha() * alphaMult));
		
		
		float texWidth = texture.getTextureWidth();
		float imageWidth = texture.getWidth();
		float bandWidthInTexture = imageWidth;
		
		float thickness = (maxR - minR) * 1f * factor;
		float radius = middleRadius - thickness / 2f;
		if (spiral) {
			float prev = (radius + thickness);
			thickness = Math.min(thickness, (radius + thickness) * 0.25f);
			radius = prev - thickness;
		}
		
		float texProgress = 0f;
		float texHeight = texture.getTextureHeight();
		float imageHeight = texture.getHeight();
		float texPerSegment = pixelsPerSegment * texHeight / imageHeight * bandWidthInTexture / thickness;
		
		//texPerSegment = 0.01f;
		float totalTex = Math.max(1f, Math.round(texPerSegment * segments));
		texPerSegment = totalTex / segments;
		

		float leftTX = 0;
		float rightTX = 1;
//		float bandIndex = 0;
//		float leftTX = (float) bandIndex * texWidth * bandWidthInTexture / imageWidth;
//		float rightTX = (float) (bandIndex + 1f) * texWidth * bandWidthInTexture / imageWidth - 0.001f;

		float minSpiralRadius = radius * 0.01f;
		float spiralFactor = 1.25f;
		//System.out.println(hashCode());
//		if (!GLListManager.callList(token)) {
//			token = GLListManager.beginList();
			GL11.glBegin(GL11.GL_QUAD_STRIP);
			
			if (!spiral) {
				for (float i = 0; i < segments; i++) {
					float theta = anglePerSegment * i;// + (float) Math.toRadians(angle);
					float cos = (float) Math.cos(theta);
					float sin = (float) Math.sin(theta);
					float x1 = cos * radius;
					float y1 = sin * radius;
					float x2 = cos * (radius + thickness);
					float y2 = sin * (radius + thickness);
					
					GL11.glTexCoord2f(leftTX, texProgress);
					GL11.glVertex2f(x1, y1);
					GL11.glTexCoord2f(rightTX, texProgress);
					GL11.glVertex2f(x2, y2);
					
					texProgress += texPerSegment;
				}
				
				{
					int i = 0;
					float theta = anglePerSegment * i; // + (float) Math.toRadians(angle);
					float cos = (float) Math.cos(theta);
					float sin = (float) Math.sin(theta);
					float x1 = cos * radius;
					float y1 = sin * radius;
					float x2 = cos * (radius + thickness);
					float y2 = sin * (radius + thickness);
					
					GL11.glTexCoord2f(leftTX, texProgress);
					GL11.glVertex2f(x1, y1);
					GL11.glTexCoord2f(rightTX, texProgress);
					GL11.glVertex2f(x2, y2);
				}
			} else {
				//System.out.println(radius);
				//minSpiralRadius = 850f;
				float numSpirals = (radius - minSpiralRadius) / (thickness * spiralFactor);
				if (numSpirals < 2) numSpirals = 2;
				float distMult = 1f;
				float thicknessMult = 10f;
				float theta = 0f;
				for (float i = 0; i < segments * numSpirals; i++) {
					distMult = 1f - (i / (segments * numSpirals));
					//distMult *= distMult;
					distMult = (float) Math.sqrt(distMult);
					//thicknessMult = 0.5f + 0.5f * (1f - (i / (segments * numSpirals)));
					thicknessMult = 1f;
					float alpha = i / segments;
					if (i > segments * (numSpirals - 1)) {
						alpha = (segments * numSpirals - i) / segments;
					}
					if (alpha > 1) alpha = 1;
					if (alpha < 0) alpha = 0;
					
					GL11.glColor4ub((byte)color.getRed(),
							(byte)color.getGreen(),
							(byte)color.getBlue(),
							(byte)((float) color.getAlpha() * alpha * alphaMult));
					
					
					theta = anglePerSegment * i;// + (float) Math.toRadians(angle);
					float cos = (float) Math.cos(theta);
					float sin = (float) Math.sin(theta);
					float x1 = cos * ((radius - minSpiralRadius) * distMult + minSpiralRadius);
					float y1 = sin * ((radius - minSpiralRadius) * distMult + minSpiralRadius);
					float x2 = cos * (((radius - minSpiralRadius) * distMult + thickness * thicknessMult) + minSpiralRadius);
					float y2 = sin * (((radius - minSpiralRadius) * distMult + thickness * thicknessMult) + minSpiralRadius);
					
					GL11.glTexCoord2f(leftTX, texProgress);
					GL11.glVertex2f(x1, y1);
					GL11.glTexCoord2f(rightTX, texProgress);
					GL11.glVertex2f(x2, y2);
					
					float circumferenceMult = ((radius - minSpiralRadius) * distMult + minSpiralRadius) / radius;
					texProgress += texPerSegment * circumferenceMult;
					
					if (i == 0) {
						GL11.glColor4ub((byte)color.getRed(),
								(byte)color.getGreen(),
								(byte)color.getBlue(),
								(byte)((float) color.getAlpha() * alphaMult));
					}
				}
			}

			
			GL11.glEnd();
			//GLListManager.endList();
		//}
		GL11.glPopMatrix();
		
	}

}
