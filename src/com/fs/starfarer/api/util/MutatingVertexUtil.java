package com.fs.starfarer.api.util;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.util.WarpingSpriteRendererUtil.MutatingValue;

public class MutatingVertexUtil {
		public MutatingValue theta;
		public MutatingValue radius;
		public Vector2f vector = new Vector2f();
		
		public MutatingVertexUtil(float minRadius, float maxRadius, float rate, float angleRate) {
			theta = new MutatingValue(-360f * ((float) Math.random() * 3f + 1f), 360f * ((float) Math.random() * 3f + 1f), angleRate);
			radius = new MutatingValue(minRadius, maxRadius, rate);
		}
		
		public void advance(float amount) {
			theta.advance(amount);
			radius.advance(amount);
			
			vector = Misc.getUnitVectorAtDegreeAngle(theta.getValue());
			vector.scale(radius.getValue());
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