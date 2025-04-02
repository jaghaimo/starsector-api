package com.fs.starfarer.api.util;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.graphics.SpriteAPI;

public class WarpingSpriteRendererUtil  {
	
	public static class MutatingValue {

		public float value;
		
		public float min;
		public float max;
		
		public float rate;
		public float rateSign;
		
		public float sign = 0;

		public MutatingValue() {
			
		}
		public MutatingValue(float min, float max, float rate) {
			this.min = min;
			this.max = max;
			this.rate = Math.abs(rate);
			
			value = min + (float) Math.random() * (max - min);
			rateSign = Math.signum(rate);
		}
		
		public void set(float min, float max) {
			this.min = min;
			this.max = max;
			value = min + (float) Math.random() * (max - min);
		}

		public void advance(float amount) {
			if (rateSign != 0) {
				value += amount * rate * rateSign;
			} else {
				value += amount * rate;
			}
			if (value > max) {
				value = max;
				rateSign = -1f;
			} else if (value < min) {
				value = min;
				rateSign = 1f;
			}
		}

		public float getValue() {
			if (sign != 0) return value * sign;
			return value;
		}

		public void setValue(float value) {
			this.value = value;
		}

		public float getMin() {
			return min;
		}

		public void setMin(float min) {
			this.min = min;
		}

		public float getMax() {
			return max;
		}

		public void setMax(float max) {
			this.max = max;
		}

		public float getRate() {
			return rate;
		}

		public void setRate(float rate) {
			//System.out.println("RATE: " + rate);
			this.rate = Math.abs(rate);
			//rateSign = Math.signum(rate);
		}

		public float getSign() {
			return sign;
		}

		public void setSign(float sign) {
			this.sign = Math.signum(sign);
		}
		
		public void setRandomSign() {
			sign = (float) Math.signum(Math.random() - 0.5f);
			if (sign == 0) sign = 1;
		}
		public void setRandomRateSign() {
			rateSign = (float) Math.signum(Math.random() - 0.5f);
			if (rateSign == 0) rateSign = 1;
		}

		public float getRateSign() {
			return rateSign;
		}

		public void setRateSign(float rateSign) {
			this.rateSign = rateSign;
		}
		
	}
	
	
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
	
	private int verticesWide, verticesTall;
	private WSVertex [] [] vertices;
	
	public WarpingSpriteRendererUtil(int verticesWide, int verticesTall, float minWarpRadius, float maxWarpRadius, 
								float warpRateMult) {
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
		
		readResolve();
	}
	
	Object readResolve() {
		return this;
	}
	
	public void advance(float amount) {
		for (int i = 0; i < verticesWide; i++) {
			for (int j = 0; j < verticesTall; j++) {
				vertices[i][j].advance(amount);
			}
		}
	}
	
	public void renderNoBlendOrRotate(SpriteAPI sprite, float x, float y) {
		renderNoBlendOrRotate(sprite, x, y, true);	
	}
	
	public void renderNoBlendOrRotate(SpriteAPI sprite, float xOff, float yOff, boolean disableBlend) { 
		
		sprite.bindTexture();
		GL11.glPushMatrix();
		
		Color color = sprite.getColor();
		GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(),
						(byte) color.getBlue(), (byte)(color.getAlpha() * sprite.getAlphaMult()));
		
		// translate to the right location and prepare to draw
		GL11.glTranslatef(xOff, yOff, 0);

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		if (disableBlend) {
			GL11.glDisable(GL11.GL_BLEND);
		}

		float w = sprite.getWidth();
		float h = sprite.getHeight();
		float tw = sprite.getTextureWidth() - 0.001f;
		float th = sprite.getTextureHeight() - 0.001f;
		
		float cw = w / (float) (verticesWide - 1);
		float ch = h / (float) (verticesTall- 1);
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
//		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		GL11.glPointSize(32f);
//		GLUtils.setColor(Color.white);
//		GL11.glBegin(GL11.GL_POINTS);
//		for (float i = 0; i < verticesWide - 1; i++) {
//			for (float j = 0; j < verticesTall; j++) {
//				float x1 = cw * i;
//				float y1 = ch * j;
//				float x2 = cw * (i + 1f);
//				float y2 = ch * j;
//				
//				if (i != 0 && i != verticesWide - 1 && j != 0 && j != verticesTall - 1) {
//					float theta = (float) Math.toRadians(vertices[(int)i][(int)j].theta.getValue());
//					float radius = vertices[(int)i][(int)j].radius.getValue();
//					float sin = (float) Math.sin(theta);
//					float cos = (float) Math.cos(theta);
//					
//					x1 += cos * radius; 
//					y1 += sin * radius;
//					//System.out.println("Radius: " + radius);
//				}
//				
//				
//				GL11.glVertex2f(x1, y1);
//			}
//		}
//		GL11.glEnd();
		
		GL11.glPopMatrix();
	}
	
}














