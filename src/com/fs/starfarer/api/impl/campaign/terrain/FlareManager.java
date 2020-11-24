package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class FlareManager {
	
	public static interface FlareManagerDelegate {
		SectorEntityToken getFlareCenterEntity();
		
		List<Color> getFlareColorRange();
		float getFlareProbability();
		float getFlareSkipLargeProbability();
		int getFlareMinSmallCount();
		int getFlareMaxSmallCount();
		
		
		float getFlareOccurrenceAngle();
		float getFlareOccurrenceArc();
		
		float getFlareArcMin();
		float getFlareArcMax();
		float getFlareFadeInMin();
		float getFlareFadeInMax();
		float getFlareFadeOutMin();
		float getFlareFadeOutMax();
		float getFlareExtraLengthMultMin();
		float getFlareExtraLengthMultMax();
		float getFlareExtraLengthFlatMin();
		float getFlareExtraLengthFlatMax();
		float getFlareShortenFlatModMin();
		float getFlareShortenFlatModMax();
		
		float getFlareSmallArcMin();
		float getFlareSmallArcMax();
		float getFlareSmallFadeInMin();
		float getFlareSmallFadeInMax();
		float getFlareSmallFadeOutMin();
		float getFlareSmallFadeOutMax();
		float getFlareSmallExtraLengthMultMin();
		float getFlareSmallExtraLengthMultMax();
		float getFlareSmallExtraLengthFlatMin();
		float getFlareSmallExtraLengthFlatMax();
		float getFlareSmallShortenFlatModMin();
		float getFlareSmallShortenFlatModMax();
	}

	public static class Flare {
		public float direction;
		public float arc;
		public float extraLengthMult;
		public float extraLengthFlat;
		public float shortenFlatMod;
		transient public List<Color> colors = new ArrayList<Color>();
		public String c = null;
		public FaderUtil fader;
		
		Object readResolve() {
			if (c != null) {
				colors = Misc.colorsFromString(c);
			} else {
				colors = new ArrayList<Color>();
			}
			return this;
		}
		
		Object writeReplace() {
			c = Misc.colorsToString(colors);
			return this;
		}
	}
	
	private IntervalUtil flareTracker = new IntervalUtil(0.5f, 1.5f);
	private List<Flare> flares = new ArrayList<Flare>();
	private FlareManagerDelegate delegate;

	
	public FlareManager(FlareManagerDelegate delegate) {
		this.delegate = delegate;
	}

	Object readResolve() {
		return this;
	}
	
	Object writeReplace() {
		if (flares != null && flares.isEmpty()) flares = null;
		return this;
	}

	public List<Flare> getFlares() {
		if (flares == null) {
			flares = new ArrayList<Flare>();
		}
		return flares;
	}

	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		
		Flare curr = null;
		if (!getFlares().isEmpty()) {
			curr = getFlares().get(0);
		}
		if (curr != null) {
			curr.fader.advance(days);
			if (curr.fader.isFadedOut()) {
				curr = null;
				getFlares().remove(0);
				if (!getFlares().isEmpty()) {
					getFlares().get(0).fader.fadeIn();
				}
			}
		}
		
		flareTracker.advance(days);
		if (flareTracker.intervalElapsed() && getFlares().isEmpty()) {
			if (Math.random() < delegate.getFlareProbability()) {
				initNewFlareSequence();
			}
		}
	}
	
	public Flare getActiveFlare() {
		if (getFlares().isEmpty()) return null;
		return getFlares().get(0);
	}
	
	public boolean isInActiveFlareArc(Vector2f point) {
		float angle = Misc.getAngleInDegrees(delegate.getFlareCenterEntity().getLocation(), point);
		return isInActiveFlareArc(angle);
	}
	
	public boolean isInActiveFlareArc(SectorEntityToken other) {
		float angle = Misc.getAngleInDegrees(delegate.getFlareCenterEntity().getLocation(), other.getLocation());
		return isInActiveFlareArc(angle);
	}
	
	public boolean isInActiveFlareArc(float angle) {
		Flare curr = getActiveFlare();
		if (curr == null) return false;
		return Misc.isInArc(curr.direction, curr.arc, angle);
	}
	
	public Color getColorForAngle(Color baseColor, float angle) {
		Flare curr = getActiveFlare();
		if (curr == null) return baseColor;
		
		if (!Misc.isInArc(curr.direction, curr.arc, angle)) return baseColor;
		
		angle = Misc.normalizeAngle(angle);
		
		float arcStart = curr.direction - curr.arc / 2f;
		float arcEnd = curr.direction + curr.arc / 2f;
		
		angle -= arcStart;
		if (angle < 0) angle += 360f;
		
		float progress = angle / (arcEnd - arcStart);
		if (progress < 0) progress = 0;
		if (progress > 1) progress = 1;
		
		float numColors = curr.colors.size();
		
		float fractionalIndex = ((numColors - 1f) * progress);
		int colorOne = (int) fractionalIndex;
		int colorTwo = (int) Math.ceil(fractionalIndex);
		
		float interpProgress = fractionalIndex - (int)fractionalIndex;
		Color one = curr.colors.get(colorOne);
		Color two = curr.colors.get(colorTwo);
		
		Color result = Misc.interpolateColor(one, two, interpProgress);
		result = Misc.interpolateColor(baseColor, result, curr.fader.getBrightness());
		
		return result;
	}
	
	
	public float getExtraLengthFlat(float angle) {
		Flare curr = getActiveFlare();
		if (curr == null) return 0f;
		
		if (!Misc.isInArc(curr.direction, curr.arc, angle)) return 0f;
		
		return curr.extraLengthFlat * (float)Math.sqrt(curr.fader.getBrightness());
	}
	
	public float getExtraLengthMult(float angle) {
		Flare curr = getActiveFlare();
		if (curr == null) return 1f;
		
		if (!Misc.isInArc(curr.direction, curr.arc, angle)) return 1f;
		
		return 1f + (curr.extraLengthMult - 1f) * (float)Math.sqrt(curr.fader.getBrightness());
	}
	
	public float getShortenMod(float angle) {
		Flare curr = getActiveFlare();
		if (curr == null) return 0f;
		
		if (!Misc.isInArc(curr.direction, curr.arc, angle)) return 0f;
		
		return curr.shortenFlatMod * (float)Math.sqrt(curr.fader.getBrightness());
	}
	
	public float getInnerOffsetMult(float angle) {
		Flare curr = getActiveFlare();
		if (curr == null) return 0f;
		
		if (!Misc.isInArc(curr.direction, curr.arc, angle)) return 0f;
		
		//return curr.fader.getBrightness();
		return (float)Math.sqrt(curr.fader.getBrightness());
	}
	
	protected void initNewFlareSequence() {
		getFlares().clear();
		
		int numSmall = delegate.getFlareMinSmallCount() + 
						(int)Math.ceil((float) (delegate.getFlareMaxSmallCount() - delegate.getFlareMinSmallCount()) * (float) Math.random());
		
		Flare large = genLargeFlare();
		for (int i = 0; i < numSmall; i++) {
			getFlares().add(genSmallFlare(large.direction, large.arc));
		}
		
		if (Math.random() > delegate.getFlareSkipLargeProbability()) {
			getFlares().add(large);
		}
		
		if (!getFlares().isEmpty()) {
			getFlares().get(0).fader.fadeIn();
		}
	}
	
	protected Flare genSmallFlare(float dir, float arc) {
		Flare flare = new Flare();
		flare.direction = dir - 
						  arc / 2f + 
						  (float) Math.random() * arc;
		flare.arc = delegate.getFlareSmallArcMin() +
					(delegate.getFlareSmallArcMax() - delegate.getFlareSmallArcMin()) * (float) Math.random();
		flare.extraLengthFlat = delegate.getFlareSmallExtraLengthFlatMin() +
					(delegate.getFlareSmallExtraLengthFlatMax() - delegate.getFlareSmallExtraLengthFlatMin()) * (float) Math.random();
		flare.extraLengthMult = delegate.getFlareSmallExtraLengthMultMin() +
					(delegate.getFlareSmallExtraLengthMultMax() - delegate.getFlareSmallExtraLengthMultMin()) * (float) Math.random();
		flare.shortenFlatMod = delegate.getFlareSmallShortenFlatModMin() +
								(delegate.getFlareSmallShortenFlatModMax() - delegate.getFlareSmallShortenFlatModMin()) * (float) Math.random();
		
		flare.fader = new FaderUtil(0, 
									delegate.getFlareSmallFadeInMin() +
									(delegate.getFlareSmallFadeInMax() - delegate.getFlareSmallFadeInMin()) * (float) Math.random(),
									delegate.getFlareSmallFadeOutMin() +
									(delegate.getFlareSmallFadeOutMax() - delegate.getFlareSmallFadeOutMin()) * (float) Math.random(),
									false, true);
		
		setColors(flare);
		return flare;
	}
	
	protected Flare genLargeFlare() {
		Flare flare = new Flare();			
		flare.direction = delegate.getFlareOccurrenceAngle() - 
						  delegate.getFlareOccurrenceArc() / 2f + 
						  (float) Math.random() * delegate.getFlareOccurrenceArc();
		flare.arc = delegate.getFlareArcMin() +
				    (delegate.getFlareArcMax() - delegate.getFlareArcMin()) * (float) Math.random();
		flare.extraLengthFlat = delegate.getFlareExtraLengthFlatMin() +
					(delegate.getFlareExtraLengthFlatMax() - delegate.getFlareExtraLengthFlatMin()) * (float) Math.random();
		flare.extraLengthMult = delegate.getFlareExtraLengthMultMin() +
								(delegate.getFlareExtraLengthMultMax() - delegate.getFlareExtraLengthMultMin()) * (float) Math.random();
		flare.shortenFlatMod = delegate.getFlareShortenFlatModMin() +
								(delegate.getFlareShortenFlatModMax() - delegate.getFlareShortenFlatModMin()) * (float) Math.random();
		
		flare.fader = new FaderUtil(0, 
					delegate.getFlareFadeInMin() +
					(delegate.getFlareFadeInMax() - delegate.getFlareFadeInMin()) * (float) Math.random(),
					delegate.getFlareFadeOutMin() +
					(delegate.getFlareFadeOutMax() - delegate.getFlareFadeOutMin()) * (float) Math.random(),
					false, true);
		
		flare.direction = Misc.normalizeAngle(flare.direction);
		
		setColors(flare);
		
		return flare;
	}
	
	protected void setColors(Flare flare) {
		float colorRangeFraction = flare.arc / delegate.getFlareArcMax();
		int totalColors = delegate.getFlareColorRange().size();
		int numColors = Math.round(colorRangeFraction * totalColors);
		if (numColors < 1) numColors = 1;
		
		flare.colors.clear();
		Random r = new Random();
		int start = 0;
		if (numColors < totalColors) {
			start = r.nextInt(totalColors - numColors);
		}
		for (int i = start; i < totalColors; i++) {
			flare.colors.add(delegate.getFlareColorRange().get(i));
		}
	}
}













