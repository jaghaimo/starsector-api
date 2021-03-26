package com.fs.starfarer.api.campaign;

import java.awt.Color;

public interface ParticleControllerAPI {

	float getBrightness();
	float getNewness();
	boolean isExpired();
	void advance(float elapsed);
	void setVel(float dx, float dy);
	void setPos(float x, float y);
	float getAge();
	void setAge(float age);
	float getDx();
	void setDx(float dx);
	float getDy();
	void setDy(float dy);
	float getMaxAge();
	void setMaxAge(float maxAge);
	float getX();
	void setX(float x);
	float getY();
	void setY(float y);
	float getAngle();
	void setAngle(float angle);
	float getRotationSpeed();
	void setRotationSpeed(float rotationSpeed);
	float getRampUpPeriod();
	void setRampUpPeriod(float rampUpPeriod);
	void setColor(Color color);
	Color getColor();

}
