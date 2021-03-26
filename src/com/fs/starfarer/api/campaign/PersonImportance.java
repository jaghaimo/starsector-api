package com.fs.starfarer.api.campaign;


public enum PersonImportance {

	VERY_LOW("Very Low", 0f),
	LOW("Low", 0.25f),
	MEDIUM("Medium", 0.5f),
	HIGH("High", 0.75f),
	VERY_HIGH("Very High", 1f)
	;
	
	
	private final String displayName;
	private final float value;
	private PersonImportance(String displayName, float value) {
		this.displayName = displayName;
		this.value = value;
	}
	public String getDisplayName() {
		return displayName;
	}
	public float getValue() {
		return value;
	}
	
	private static PersonImportance [] vals = values();
	public PersonImportance next() {
		int index = this.ordinal() + 1;
		if (index >= vals.length) index = vals.length - 1;
		return vals[index];
	}
	public PersonImportance prev() {
		int index = this.ordinal() - 1;
		if (index < 0) index = 0;
		return vals[index];
	}
	
}
