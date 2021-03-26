package com.fs.starfarer.api.ui;

public interface ButtonAPI {
	void setShortcut(int key, boolean putLast);
	void setEnabled(boolean enabled);
	boolean isEnabled();
	void setButtonPressedSound(String buttonPressedSound);
	void setMouseOverSound(String mouseOverSound);
	void setButtonDisabledPressedSound(String buttonDisabledPressedSound);
	boolean isChecked();
	void setChecked(boolean checked);
}
