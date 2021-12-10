package com.fs.starfarer.api.ui;

public interface ButtonAPI extends UIComponentAPI {
	
	public static enum UICheckboxSize {
		TINY,
		SMALL,
		LARGE,
	}
	
	void setShortcut(int key, boolean putLast);
	void setEnabled(boolean enabled);
	boolean isEnabled();
	void setButtonPressedSound(String buttonPressedSound);
	void setMouseOverSound(String mouseOverSound);
	void setButtonDisabledPressedSound(String buttonDisabledPressedSound);
	boolean isChecked();
	void setChecked(boolean checked);
	void highlight();
	void unhighlight();
	boolean isHighlighted();
	void setHighlightBrightness(float highlightBrightness);
	float getHighlightBrightness();
}
