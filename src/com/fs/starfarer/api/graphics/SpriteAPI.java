package com.fs.starfarer.api.graphics;

import java.awt.Color;

public interface SpriteAPI {

	void setBlendFunc(int src, int dest);
	void setNormalBlend();
	void setAdditiveBlend();
	void setCenter(float x, float y);
	void setSize(float width, float height);
	float getAngle();
	void setAngle(float angle);

	Color getColor();
	void setColor(Color color);
	void setHeight(float height);
	void setWidth(float width);
	float getHeight();
	float getWidth();
	void bindTexture();
	int getTextureId();

	void renderAtCenter(float x, float y);
	void render(float x, float y);
	void renderRegionAtCenter(float x, float y, float tx, float ty, float tw, float th);
	void renderRegion(float x, float y, float tx, float ty, float tw, float th);

	float getCenterX();
	float getCenterY();

	float getAlphaMult();
	void setAlphaMult(float alphaMult);

	
	/**
	 * Fraction of the OpenGL texture's width taken up by the image.
	 * OpenGL textures have width and height that are powers of 2, the image may not.
	 * @return
	 */
	float getTextureWidth();
	
	/**
	 * Fraction of the OpenGL texture's height taken up by the image.
	 * OpenGL textures have width and height that are powers of 2, the image may not.
	 * @return
	 */
	float getTextureHeight();
	
	
	void setCenterY(float cy);
	void setCenterX(float cx);
	
	Color getAverageColor();
	
	void setTexX(float texX);
	void setTexY(float texY);
	void setTexWidth(float texWidth);
	void setTexHeight(float texHeight);
	void renderWithCorners(float blX, float blY, float tlX, float tlY, float trX, float trY, float brX, float brY);
	Color getAverageBrightColor();
	void renderNoBind(float x, float y);
	void renderAtCenterNoBind(float x, float y);
	int getBlendDest();
	int getBlendSrc();
	
	float getTexX();
	float getTexY();
	float getTexWidth();
	float getTexHeight();
}
