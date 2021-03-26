package com.fs.starfarer.api.util;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.graphics.SpriteAPI;

public class JitterUtil {

	public static void renderWithJitter(SpriteAPI s, float x, float y, float maxJitter, int numCopies) {
		for (int i = 0; i < numCopies; i++) {
			Vector2f jv = new Vector2f();
			jv.x = (float) Math.random() * maxJitter - maxJitter/2f;
			jv.y = (float) Math.random() * maxJitter - maxJitter/2f;
			//if (jv.lengthSquared() != 0) jv.normalise();
			s.renderAtCenter(x + jv.x, y + jv.y);
		}
	}
	
	private long seed = Misc.genRandomSeed();
	private Random random = new Random(seed);
	
	public void updateSeed() {
		//seed = (long)(Math.random() * 1000000000f);
		seed = Misc.genRandomSeed();
	}
	
	public Random getRandom() {
		random.setSeed(seed);
		return random;
	}

	public void render(SpriteAPI s, float x, float y, float maxJitter, int numCopies) {
		render(s, x, y, 0f, maxJitter, numCopies);
	}
	
	private boolean setSeedOnRender = true;
	private boolean circular;
	public boolean isSetSeedOnRender() {
		return setSeedOnRender;
	}

	public void setSetSeedOnRender(boolean resetSeedOnRender) {
		this.setSeedOnRender = resetSeedOnRender;
	}
	
	public void setUseCircularJitter(boolean circular) {
		this.circular = circular;
	}

	public void render(SpriteAPI s, float x, float y, float minJitter, float maxJitter, int numCopies) {
		if (setSeedOnRender) {
			random.setSeed(seed);
		}
		for (int i = 0; i < numCopies; i++) {
			Vector2f jv = new Vector2f();
			
//			if (true) {
//				float r = minJitter + (maxJitter - minJitter) * (float) random.nextFloat();
//				r *= 0.5f;
//				float angle = (float) ((float) random.nextFloat() * Math.PI * 2f);
//				float jx = (float) Math.cos(angle) * r;
//				float jy = (float) Math.sin(angle) * r;
//				s.renderAtCenter(x + jx, y + jy);
//				continue;
//			}
			
			if (circular) {
				float r = minJitter + (maxJitter - minJitter) * (float) random.nextFloat();
				jv = Misc.getPointAtRadius(jv, r, random);
			} else {
				if (minJitter <= 0) {
					jv.x = random.nextFloat() * maxJitter - maxJitter/2f;
					jv.y = random.nextFloat() * maxJitter - maxJitter/2f;
				} else {
					jv.x = random.nextFloat() * (maxJitter - minJitter) + minJitter;
					jv.y = random.nextFloat() * (maxJitter - minJitter) + minJitter;
					if (jv.x < minJitter) jv.x = minJitter;
					if (jv.y < minJitter) jv.y = minJitter;
					jv.x *= Math.signum(random.nextFloat() - 0.5f);
					jv.y *= Math.signum(random.nextFloat() - 0.5f);
				}
			}
			//if (jv.lengthSquared() != 0) jv.normalise();
			s.renderAtCenter(x + jv.x, y + jv.y);
		}
	}
}
