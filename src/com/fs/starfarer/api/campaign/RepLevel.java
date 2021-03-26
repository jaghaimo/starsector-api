package com.fs.starfarer.api.campaign;

public enum RepLevel {

	VENGEFUL("Vengeful", getT4(), 1f),
	HOSTILE("Hostile", getT3(), getT4()),
	INHOSPITABLE("Inhospitable", getT2(), getT3()),
	SUSPICIOUS("Suspicious", getT1(), getT2()),
	NEUTRAL("Neutral", 0, getT1()),
	FAVORABLE("Favorable", getT1(), getT2()),
	WELCOMING("Welcoming", getT2(), getT3()),
	FRIENDLY("Friendly", getT3(), getT4()),
	COOPERATIVE("Cooperative", getT4(), 1f);

	
	private final String displayName;
	private final float max;
	private final float min;
	private RepLevel(String displayName, float min, float max) {
		this.displayName = displayName;
		this.min = min;
		this.max = max;
	}
	public String getDisplayName() {
		return displayName;
	}
	
	public RepLevel getOneBetter() {
		if (this == VENGEFUL) return HOSTILE;
		if (this == HOSTILE) return INHOSPITABLE;
		if (this == INHOSPITABLE) return SUSPICIOUS;
		if (this == SUSPICIOUS) return NEUTRAL;
		if (this == NEUTRAL) return FAVORABLE;
		if (this == FAVORABLE) return WELCOMING;
		if (this == WELCOMING) return FRIENDLY;
		if (this == FRIENDLY) return COOPERATIVE;
		
		return COOPERATIVE;
	}
	
	public RepLevel getOneWorse() {
		if (this == HOSTILE) return VENGEFUL;
		if (this == INHOSPITABLE) return HOSTILE;
		if (this == SUSPICIOUS) return INHOSPITABLE;
		if (this == NEUTRAL) return SUSPICIOUS;
		if (this == FAVORABLE) return NEUTRAL;
		if (this == WELCOMING) return FAVORABLE;
		if (this == FRIENDLY) return WELCOMING;
		if (this == COOPERATIVE) return FRIENDLY;
		
		return VENGEFUL;
	}
	
	/**
	 * Not inclusive.
	 * @return
	 */
	public float getMin() {
		return min;
	}
	/**
	 * Inclusive.
	 * @return
	 */
	public float getMax() {
		return max;
	}
	public boolean isAtWorst(RepLevel level) {
		return ordinal() >= level.ordinal();
	}
	public boolean isAtBest(RepLevel level) {
		return ordinal() <= level.ordinal();
	}
	
	public boolean isNeutral() {
		return this == NEUTRAL;
	}
	
	public boolean isPositive() {
		return isAtWorst(RepLevel.FAVORABLE);
	}
	
	public boolean isNegative() {
		return isAtBest(RepLevel.SUSPICIOUS);
	}
	
	private static final float T1 = 0.09f;
	private static final float T2 = 0.24f;
	private static final float T3 = 0.49f;
	private static final float T4 = 0.74f;
//	private static final float T1 = 0.1f;
//	private static final float T2 = 0.25f;
//	private static final float T3 = 0.5f;
//	private static final float T4 = 0.75f;
	public static RepLevel getLevelFor(float r) {
		if (r >= 0) {
			int rel = getRepInt(r);
			if (rel <= getRepInt(T1)) return NEUTRAL;
			if (rel <= getRepInt(T2)) return FAVORABLE;
			if (rel <= getRepInt(T3)) return WELCOMING;
			if (rel <= getRepInt(T4)) return FRIENDLY;
			return COOPERATIVE;
		} else {
			r = -r;
			int rel = getRepInt(r);
			if (rel <= getRepInt(T1)) return NEUTRAL;
			if (rel <= getRepInt(T2)) return SUSPICIOUS;
			if (rel <= getRepInt(T3)) return INHOSPITABLE;
			if (rel <= getRepInt(T4)) return HOSTILE;
			return VENGEFUL;
		}
	}
	
	public static int getRepInt(float f) {
		return (int) Math.round(f * 100f);
	}
	
	public static float getT1() {
		return T1;
	}
	public static float getT2() {
		return T2;
	}
	public static float getT3() {
		return T3;
	}
	public static float getT4() {
		return T4;
	}
	
	
}





