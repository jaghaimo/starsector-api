package com.fs.starfarer.api.loading;

import java.awt.Color;

public class MuzzleFlashSpec implements Cloneable {

	private float length = 40f;
	private float spread = 20f; // in degrees
	private float particleSizeMin = 10f;
	private float particleSizeRange = 10f;
	private float particleDuration = 0.5f;
	private int particleCount = 10;
	
	private Color particleColor = new Color(255,255,255,255);

	
	@Override
	public MuzzleFlashSpec clone() {
		try {
			MuzzleFlashSpec copy = (MuzzleFlashSpec) super.clone();
			return copy;
		} catch (CloneNotSupportedException e) {
			return null; // should not happen
		}
	}

	public MuzzleFlashSpec(float length, float spread, float particleSizeMin,
						   float particleSizeRange, float particleDuration, int particleCount,
						   Color particleColor) {
		this.length = length;
		this.spread = spread;
		this.particleSizeMin = particleSizeMin;
		this.particleSizeRange = particleSizeRange;
		this.particleDuration = particleDuration;
		this.particleCount = particleCount;
		this.particleColor = particleColor;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public float getSpread() {
		return spread;
	}

	public void setSpread(float spread) {
		this.spread = spread;
	}

	public float getParticleSizeMin() {
		return particleSizeMin;
	}

	public void setParticleSizeMin(float particleSizeMin) {
		this.particleSizeMin = particleSizeMin;
	}

	public float getParticleSizeRange() {
		return particleSizeRange;
	}

	public void setParticleSizeRange(float particleSizeRange) {
		this.particleSizeRange = particleSizeRange;
	}

	public float getParticleDuration() {
		return particleDuration;
	}

	public void setParticleDuration(float particleDuration) {
		this.particleDuration = particleDuration;
	}

	public int getParticleCount() {
		return particleCount;
	}

	public void setParticleCount(int particleCount) {
		this.particleCount = particleCount;
	}

	public Color getParticleColor() {
		return particleColor;
	}

	public void setParticleColor(Color particleColor) {
		this.particleColor = particleColor;
	}
	
}
