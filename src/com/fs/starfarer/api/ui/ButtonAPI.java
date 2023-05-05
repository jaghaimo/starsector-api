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
	void setQuickMode(boolean quickMode);
	void setClickable(boolean clickable);
	float getGlowBrightness();
	void setGlowBrightness(float glowBrightness);
	
	/**
	 * Only works for certain types of basic buttons.
	 * @param text
	 */
	void setText(String text);
	/**
	 * Only works for certain types of basic buttons.
	 */
	String getText();
	
	
	void setSkipPlayingPressedSoundOnce(boolean skipPlayingPressedSoundOnce);
	void setHighlightBounceDown(boolean b);
	void setShowTooltipWhileInactive(boolean showTooltipWhileInactive);
	void setRightClicksOkWhenDisabled(boolean rightClicksOkWhenDisabled);
	void setFlashBrightness(float flashBrightness);
	void flash(boolean withSound, float in, float out);
	void flash(boolean withSound);
	void flash();
	void setPerformActionWhenDisabled(boolean performActionWhenDisabled);
	boolean isPerformActionWhenDisabled();
	boolean isSkipPlayingPressedSoundOnce();
	Object getCustomData();
	void setCustomData(Object customData);
}
