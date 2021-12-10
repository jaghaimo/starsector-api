package com.fs.starfarer.api.impl.campaign.velfield;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.util.Misc;

public class TurbulenceCalc2 {

	public static class TurbulenceParams {
		public VelocityField field;
		public float effectWidth;
		public float effectLength;
		public float propagationAmount;
		
		public float maxDispersionAngle = 120f;
		public float energyTransferMult = 5f; // should be 2 for proper energy conservation, but something's off
		public float dampenFactor = 0.2f;
		public float maxVelocity = 1000f;
	}
	
	public static class DeltaData {
		public Vector2f delta;
		public Vector2f velocity;
		public Vector2f dir;
		public float weight;
		public DeltaData(Vector2f delta, Vector2f velocity, Vector2f dir, float weight) {
			this.delta = delta;
			this.velocity = velocity;
			this.dir = dir;
			this.weight = weight;
		}
	}
	

	public static void advance(TurbulenceParams params) {
		
		if (params.propagationAmount > 1f) params.propagationAmount = 1f;
		
		Vector2f[][] f = params.field.getField();
		float s = params.field.getCellSize();
		
		float effectWidth = params.effectWidth;
		float effectLength = params.effectLength;
		
		Vector2f[][] delta = new Vector2f[f.length][f[0].length];
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[0].length; j++) {
				delta[i][j] = new Vector2f();
			}
		}

		
		
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[0].length; j++) {
				
				float cx = i * s;
				float cy = j * s;
				Vector2f v = f[i][j];
				
				Vector2f dir = Misc.normalise(new Vector2f(v));
				
				Vector2f p1 = new Vector2f(dir);
				p1.scale(-effectLength * 0.5f);
				p1.x += cx;
				p1.y += cy;
				
				Vector2f p2 = new Vector2f(dir);
				p2.scale(effectLength * 0.5f);
				//p2.scale(effectLength);
				p2.x += cx;
				p2.y += cy;
				
				float minX = Math.min(p1.x - effectWidth * 0.5f, p2.x - effectWidth * 0.5f);
				float maxX = Math.max(p1.x + effectWidth * 0.5f, p2.x + effectWidth * 0.5f);
				float minY = Math.min(p1.y - effectLength * 0.5f, p2.y - effectLength * 0.5f);
				float maxY = Math.max(p1.y + effectLength * 0.5f, p2.y + effectLength * 0.5f);
				
				int cellX1 = (int) (minX / s);
				int cellY1 = (int) (minY / s);
				if (minX < 0) cellX1 = (int) (-1f * Math.abs(minX) / s) - 1;
				if (minY < 0) cellY1 = (int) (-1f * Math.abs(minY) / s) - 1;
				
				int cellX2 = (int) (maxX / s);
				int cellY2 = (int) (maxY / s);
				if (maxX < 0) cellX2 = (int) (-1f * Math.abs(maxX) / s) - 1;
				if (maxY < 0) cellY2 = (int) (-1f * Math.abs(maxY) / s) - 1;
				maxX += 1;
				maxY += 1;
				
				float velDir = Misc.getAngleInDegrees(dir);
				velDir = Misc.normalizeAngle(velDir);
				
//				if (speed > 0) {
//					System.out.println("efwefwef");
//				}

				//if (true) continue;
				List<DeltaData> deltaData = new ArrayList<TurbulenceCalc2.DeltaData>();
				for (int a = cellX1; a <= cellX2; a++) {
					for (int b = cellY1; b <= cellY2; b++) {
						if (a == i && b == j) continue;
						
						Vector2f p3 = new Vector2f(a * s, b * s);
						
						float u = (p3.x - p1.x) * (p2.x - p1.x) + (p3.y - p1.y) * (p2.y - p1.y);
						float denom = Vector2f.sub(p2, p1, new Vector2f()).length();
						denom *= denom;
						if (denom == 0) continue;
						u /= denom;
						
						if (u >= 0 && u <= 1) { // intersection is between p1 and p2
							Vector2f intersect = new Vector2f();
							intersect.x = p1.x + u * (p2.x - p1.x);
							intersect.y = p1.y + u * (p2.y - p1.y);
							float distFromLine = Vector2f.sub(intersect, p3, new Vector2f()).length();
							float distAlongLine = Math.abs((u - 0.5f) * effectLength);
							if (distFromLine > effectWidth * 0.5f) continue;
							if (distAlongLine > effectLength * 0.5f) continue;
							
							float rateMult = (0.5f * (1f - distFromLine / (effectWidth * 0.5f))) +
											 (0.5f * (1f - distAlongLine / (effectLength * 0.5f)));
//							if (distFromLine <= 0f) {
//								System.out.println("efwfwefwe");
//							}
							float offsetMult = (distFromLine / (effectWidth * 0.5f)) *
											   (0.5f + 0.5f * distAlongLine / (effectLength * 0.5f));
							float deltaAngleOffset = offsetMult * params.maxDispersionAngle;
							//float offsetDir = Misc.getClosestTurnDirection(velDir, Misc.getAngleInDegrees(p1, p3));
							
							float offsetDir = 0f;
							float diff = Misc.normalizeAngle(Misc.getAngleInDegrees(p1, p3)) - velDir;
							//diff = Misc.normalizeAngle(diff);
						    if (diff < 0) diff += 360;
						    if (diff == 0 || diff == 360f) {
						    	offsetDir = 0f;
						    } else if (diff > 180) {
						    	offsetDir = -1f;
						    } else {
						    	offsetDir = 1f;
						    }
						    //offsetDir = Misc.getClosestTurnDirection(velDir, Misc.getAngleInDegrees(p1, p3));
//						    float offsetDir2 = Misc.getClosestTurnDirection(velDir, Misc.getAngleInDegrees(p1, p3));
//						    if (offsetDir != offsetDir2) {
//						    	System.out.println("NOT THE SAME");
//						    }
							
							
							Vector2f dv = Misc.getUnitVectorAtDegreeAngle(velDir + deltaAngleOffset * offsetDir);
							Vector2f d = getCell(delta, a, b);
							Vector2f destVel = getCell(f, a, b);
							
							DeltaData data = new DeltaData(d, destVel, dv, rateMult);
							deltaData.add(data);
						}
					}
				}
				
				float totalWeight = 0f;
				for (DeltaData data : deltaData) {
					totalWeight += data.weight;
				}
				
				float speed = v.length();
				float energy = 0.5f * speed * speed;
				float energyToTransfer = energy * params.propagationAmount;
				
				if (totalWeight > 0) {
					for (DeltaData data : deltaData) {
						float mult = data.weight / totalWeight;
						float energyToAdd = energyToTransfer * mult;
						
						// 0.5 Vold^2 + energyToAdd = 0.5 Vnew^2
						// Vold^2 + 2 energyToAdd = Vnew^2
						float speedOther = data.velocity.length();
						float speedOtherNew = (float) Math.sqrt(speedOther * speedOther + params.energyTransferMult * energyToAdd);
						float speedAdd = speedOtherNew - speedOther;
						data.delta.x += data.dir.x * speedAdd;
						data.delta.y += data.dir.y * speedAdd;
					}
//					if (speedToTransfer > 0) {
//						System.out.println("To transfer: " + speedToTransfer + ", transferred: " + totalTransferred);
//					}
					float speedNew = (float) Math.sqrt(speed * speed - 2f * energyToTransfer);
					float speedAdd = speedNew - speed; // should be a negative number
					Vector2f deltaForCurrCell = getCell(delta, i, j);
					deltaForCurrCell.x += dir.x * speedAdd;
					deltaForCurrCell.y += dir.y * speedAdd;
				}
			}
		}
		
		float maxVel = params.maxVelocity;
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[0].length; j++) {
				Vector2f.add(f[i][j], delta[i][j], f[i][j]);
				float len = f[i][j].length();
				if (len > maxVel) {
					f[i][j].scale(maxVel/len);
				}
			}
		}

		// dapmen by a fraction of the propagation rate
		float dampenFraction = params.dampenFactor;
		//dampenFraction = 0f;
		if (dampenFraction > 0) {
			for (int i = 0; i < f.length; i++) {
				for (int j = 0; j < f[0].length; j++) {
					Vector2f v = f[i][j];
					//Vector2f dv = getCell(delta, i, j);
					
					float speed = v.length();
					float dampen = speed * params.propagationAmount * dampenFraction;
					if (speed > 0f) {
						v.scale((speed - dampen) / speed);
					}
				}
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







