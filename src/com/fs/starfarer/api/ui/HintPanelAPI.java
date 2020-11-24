package com.fs.starfarer.api.ui;

import java.awt.Color;

public interface HintPanelAPI {

	LabelAPI setHint(int index, String string);
	LabelAPI setHint(int index, String string, Color color);
//	LabelAPI setHint(int index, String format, Color hl, String ... highlights);
//	LabelAPI setHint(int index, String format, Color color, Color hl, String ... highlights);
	boolean hasHint(int index);
	void fadeOutHint(int index);
	void clearHints(boolean withFade);
	void setOpacity(int index, float opacity);
	void makeDim(int index);
	void makeNormal(int index);
	void clearHints();
	LabelAPI setHint(int index, String format, boolean flash, Color hl, String... highlights);
	LabelAPI setHint(int index, String format, boolean flash, Color color, Color hl, String... highlights);
	
}
