package com.fs.starfarer.api.impl.combat.dweller;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.WarpingSpriteRendererUtil.MutatingValue;

public class WarpingSpriteRendererUtilV2  {
	
	public static class WSVertex {
		public MutatingValue theta;
		public MutatingValue radius;
		
		public WSVertex() {
			//theta = new MutatingValue(-360f * ((float) Math.random() * 3f + 1f), 360f * ((float) Math.random() * 3f + 1f), 30f + 70f * (float) Math.random());
			theta = new MutatingValue(-360f * ((float) Math.random() * 30f + 1f), 360f * ((float) Math.random() * 30f + 1f), 30f + 70f * (float) Math.random());
			radius = new MutatingValue(0, 10f + 15f * (float) Math.random(), 3f + 7f * (float) Math.random());
		}
		
		public void advance(float amount) {
			theta.advance(amount);
			radius.advance(amount);
		}
		
		Object writeReplace() {
			theta.setMax((int)theta.getMax());
			theta.setMin((int)theta.getMin());
			theta.setRate((int)theta.getRate());
			theta.setValue((int)theta.getValue());
			
			radius.setMax((int)radius.getMax());
			radius.setMin((int)radius.getMin());
			radius.setRate((int)radius.getRate());
			radius.setValue((int)radius.getValue());
			return this;
		}
	}
	
	protected int verticesWide, verticesTall;
	protected WSVertex [] [] vertices;
	protected SpriteAPI sprite;
	protected boolean mirror = false;
	
	public WarpingSpriteRendererUtilV2(SpriteAPI sprite, int verticesWide, int verticesTall, 
								float minWarpRadius, float maxWarpRadius, float warpRateMult) {
		
		this.sprite = sprite;
		this.verticesWide = verticesWide;
		this.verticesTall = verticesTall;
		
		vertices = new WSVertex[verticesWide][verticesTall];
		for (int i = 0; i < verticesWide; i++) {
			for (int j = 0; j < verticesTall; j++) {
				vertices[i][j] = new WSVertex();
				
				vertices[i][j].radius.set(minWarpRadius, maxWarpRadius);
				vertices[i][j].radius.rate *= warpRateMult;
				vertices[i][j].theta.rate *= warpRateMult;
				
			}
		}
	}
	
	/**
	 * Only works once, if the original mult was 1f - original rate values are not retained.
	 * @param mult
	 */
	public void setWarpRateMult(float mult) {
		for (int i = 0; i < verticesWide; i++) {
			for (int j = 0; j < verticesTall; j++) {
				vertices[i][j].radius.rate *= mult;
				vertices[i][j].theta.rate *= mult;
			}
		}
	}
	
	public void advance(float amount) {
		for (int i = 0; i < verticesWide; i++) {
			for (int j = 0; j < verticesTall; j++) {
				vertices[i][j].advance(amount);
			}
		}
	}
	
	public void renderAtCenter(float x, float y) { 
		float w = sprite.getWidth();
		float h = sprite.getHeight();
		
		x -= w/2f;
		y -= h/2f;
		
		sprite.bindTexture();
		GL11.glPushMatrix();
		
		Color color = sprite.getColor();
		GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(),
						(byte) color.getBlue(), (byte)(color.getAlpha() * sprite.getAlphaMult()));
		
		// translate to the right location and prepare to draw
		GL11.glTranslatef(x, y, 0);

		float centerX = sprite.getCenterX();
		float centerY = sprite.getCenterY();
		float angle = sprite.getAngle();
		// translate to center, rotate, translate back
		if (centerX != -1 && centerY != -1) {
			GL11.glTranslatef(w / 2, h / 2, 0);
			GL11.glRotatef(angle, 0, 0, 1);
			GL11.glTranslatef(- centerX, - centerY, 0);
		} else {
			GL11.glTranslatef(w / 2, h / 2, 0);
			GL11.glRotatef(angle, 0, 0, 1);
			GL11.glTranslatef(-w / 2, -h / 2, 0);
		}
		
		int blendSrc = sprite.getBlendSrc();
		int blendDest = sprite.getBlendDest();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(blendSrc, blendDest);
		
		float tw = sprite.getTextureWidth() - 0.001f;
		float th = sprite.getTextureHeight() - 0.001f;
		
		float cw = w / (float) (verticesWide - 1);
		float ch = h / (float) (verticesTall - 1);
		float ctw = tw / (float) (verticesWide - 1);
		float cth = th / (float) (verticesTall- 1);
	
		for (float i = 0; i < verticesWide - 1; i++) {
		//for (float i = 5; i < 7; i++) {
			GL11.glBegin(GL11.GL_QUAD_STRIP);
			{
				for (float j = 0; j < verticesTall; j++) {
					float x1 = cw * i;
					float y1 = ch * j;
					float x2 = cw * (i + 1f);
					float y2 = ch * j;
					
					float tx1 = ctw * i;
					float ty1 = cth * j;
					float tx2 = ctw * (i + 1f);
					float ty2 = cth * j;
					
					if (mirror) {
						tx1 = tw - tx1;
						tx2 = th - tx2;
					}
					
					if (i != 0 && i != verticesWide - 1 && j != 0 && j != verticesTall - 1) {
						float theta = (float) Math.toRadians(vertices[(int)i][(int)j].theta.getValue());
						float radius = vertices[(int)i][(int)j].radius.getValue();
						float sin = (float) Math.sin(theta);
						float cos = (float) Math.cos(theta);
						
						x1 += cos * radius; 
						y1 += sin * radius;
						//System.out.println("Radius: " + radius);
					}
					
					if (i + 1 != 0 && i + 1 != verticesWide - 1 && j != 0 && j != verticesTall - 1) {
						float theta = (float) Math.toRadians(vertices[(int)i + 1][(int)j].theta.getValue());
						float radius = vertices[(int)i + 1][(int)j].radius.getValue();
						float sin = (float) Math.sin(theta);
						float cos = (float) Math.cos(theta);
						
						x2 += cos * radius; 
						y2 += sin * radius;
						//System.out.println("Radius: " + radius);
					}

					GL11.glTexCoord2f(tx1, ty1);
					GL11.glVertex2f(x1, y1);
					
					GL11.glTexCoord2f(tx2, ty2);
					GL11.glVertex2f(x2, y2);
				}
			}
			GL11.glEnd();
			
		}
		
		GL11.glPopMatrix();
	}

	public int getVerticesWide() {
		return verticesWide;
	}

	public int getVerticesTall() {
		return verticesTall;
	}

	public SpriteAPI getSprite() {
		return sprite;
	}

	public boolean isMirror() {
		return mirror;
	}

	public void setMirror(boolean mirror) {
		this.mirror = mirror;
	}
	
}














