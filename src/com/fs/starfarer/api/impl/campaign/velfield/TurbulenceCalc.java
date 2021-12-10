package com.fs.starfarer.api.impl.campaign.velfield;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.util.Misc;

public class TurbulenceCalc {
	
	public static Vector2f [] DIRECTIONS = new Vector2f[8];
	static {
		for (int i = 0; i < 8; i++) {
			float angle = i * 45f + 180f;
			DIRECTIONS[i] = Misc.getUnitVectorAtDegreeAngle(angle);
		}
	}

//	protected VelocityField field;
//	protected float propagationSpeed;
//
//	public TurbulenceCalc(VelocityField field, float propagationSpeed) {
//		this.field = field;
//		this.propagationSpeed = propagationSpeed;
//	}
	
	
	public static void advance(VelocityField field, float propagationSpeed, float amount) {
		
		Vector2f[][] f = field.getField();
		
		Vector2f[][] delta = new Vector2f[f.length][f[0].length];
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[0].length; j++) {
				delta[i][j] = new Vector2f();
			}
		}
		Vector2f[][] delta2 = new Vector2f[f.length][f[0].length];
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[0].length; j++) {
				delta2[i][j] = new Vector2f();
			}
		}
		
		//Vector2f[][] d = field.getDelta();
		
		float mult = propagationSpeed * amount;
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[0].length; j++) {
				
				addCell(f, delta, i + 1, j + 0, i, j, DIRECTIONS[0], mult);
				addCell(f, delta, i + 1, j + 1, i, j, DIRECTIONS[1], mult);
				addCell(f, delta, i + 0, j + 1, i, j, DIRECTIONS[2], mult);
				addCell(f, delta, i - 1, j + 1, i, j, DIRECTIONS[3], mult);
				addCell(f, delta, i - 1, j + 0, i, j, DIRECTIONS[4], mult);
				addCell(f, delta, i - 1, j - 1, i, j, DIRECTIONS[5], mult);
				addCell(f, delta, i - 0, j - 1, i, j, DIRECTIONS[6], mult);
				addCell(f, delta, i + 1, j - 1, i, j, DIRECTIONS[7], mult);
				//Misc.wiggle(d[i][j], d[i][j].length() * 0.1f);
			}
		}
		
		mult = -1f;
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[0].length; j++) {
				addCell(delta, delta2, i + 1, j + 0, i, j, DIRECTIONS[0], mult);
				addCell(delta, delta2, i + 1, j + 1, i, j, DIRECTIONS[1], mult);
				addCell(delta, delta2, i + 0, j + 1, i, j, DIRECTIONS[2], mult);
				addCell(delta, delta2, i - 1, j + 1, i, j, DIRECTIONS[3], mult);
				addCell(delta, delta2, i - 1, j + 0, i, j, DIRECTIONS[4], mult);
				addCell(delta, delta2, i - 1, j - 1, i, j, DIRECTIONS[5], mult);
				addCell(delta, delta2, i - 0, j - 1, i, j, DIRECTIONS[6], mult);
				addCell(delta, delta2, i + 1, j - 1, i, j, DIRECTIONS[7], mult);
				//Misc.wiggle(d[i][j], d[i][j].length() * 0.1f);
			}
		}

//		for (int i = 0; i < f.length; i++) {
//			for (int j = 0; j < f[0].length; j++) {
//				Misc.wiggle(delta[i][j], delta[i][j].length() * 0.1f);
//				Misc.wiggle(delta2[i][j], delta2[i][j].length() * 0.1f);
//			}
//		}
		
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[0].length; j++) {
				Vector2f.add(f[i][j], delta[i][j], f[i][j]);
//				Vector2f.add(f[i][j], delta2[i][j], f[i][j]);
//				float len = f[i][j].length();
//				if (len > 70f) {
//					f[i][j].scale(70f/len);
//				}
			}
		}
		
	}
	
	public static Vector2f getCell(Vector2f [][] data, int i, int j) {
		if (i < 0 || j < 0) return new Vector2f();
		if (i >= data.length || j >= data[0].length) return new Vector2f();
		return data[i][j];
	}
	
	public static void addCell(Vector2f[][] field, Vector2f[][] delta, int fromX, int fromY, int x, int y, Vector2f dir, float propagationMult) {
		Vector2f cell = getCell(field, fromX, fromY);
		Vector2f d = getCell(delta, x, y);
		Vector2f dFrom = getCell(delta, fromX, fromY);
		float dot = Vector2f.dot(cell, dir);
		
		d.x += dir.x * dot * propagationMult;
		d.y += dir.y * dot * propagationMult;
		
		dFrom.x -= dir.x * dot * propagationMult;
		dFrom.y -= dir.y * dot * propagationMult;
	}
	
	public static void addCell(Vector2f cell, Vector2f dir, Vector2f delta) {
		float dot = Vector2f.dot(cell, dir);
		delta.x += dir.x * dot;
		delta.y += dir.y * dot;
	}
}







