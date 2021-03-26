package com.fs.starfarer.api.util;

import java.util.Arrays;
import java.util.Random;

public class Noise {
	public static Random random = new Random(Misc.genRandomSeed());
	
	public static float[] genNoise(int size, float spikes) {
		float [] noise = new float[size];
		Arrays.fill(noise, -1);
		//noise[size/2] = 1f;
//		noise[0] = 0f;
//		noise[size - 1] = 0f;
		noise[0] = 1f;
		noise[size - 1] = 1f;
		genNoise(noise, 0, size - 1, 1, spikes);
		normalizeNoise(noise, 0, 1);
		return noise;
	}
	
	
	public static void genNoise(float[] noise, int x1, int x2, int iter, float spikes) {
		if (x1 + 1 >= x2)
			return; // no more values to fill

		int midX = (x1 + x2) / 2;

		fill(noise, midX, x1, x2, iter, spikes);
		
		genNoise(noise, x1, midX, iter + 1, spikes);
		genNoise(noise, midX, x2, iter + 1, spikes);
	}
	
	private static void normalizeNoise(float[] noise, float min, float max) {
		float minNoise = Float.MAX_VALUE;
		float maxNoise = -Float.MAX_VALUE;
		for (int i = 0; i < noise.length; i++) {
			if (noise[i] == -1) continue;
			if (noise[i] > maxNoise) {
				maxNoise = noise[i];
			}
			if (noise[i] < minNoise) {
				minNoise = noise[i];
			}
		}
		if (minNoise >= maxNoise) return;

		float range = maxNoise - minNoise;
		for (int i = 0; i < noise.length; i++) {
			if (noise[i] != -1) {
				noise[i] = min + ((noise[i] - minNoise) / range * (max - min));
			} else {
				if (i > 0) {
					noise[i] = noise[i - 1];
				} else if (i < noise.length - 1) {
					noise[i] = noise[i + 1];
				} else {
					noise[i] = .5f;
				}
			}
		}
	}
	
	private static void fill(float[] noise, int x, int x1, int x2, int iter, float spikes) {
		if (noise[x] == -1) {
			float avg = (noise[x1] + noise[x2]) / 2f;
			noise[x] = avg + ((float) Math.pow(spikes, (iter)) * (float) (random.nextDouble() - .5f));
		}
	}
}
