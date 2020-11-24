package com.fs.starfarer.api.ui;

public interface ButtonAPI {
	void setShortcut(int key, boolean putLast);
	void setEnabled(boolean enabled);
	boolean isEnabled();
}
