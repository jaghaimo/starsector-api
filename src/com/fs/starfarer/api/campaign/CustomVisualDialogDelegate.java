package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.util.FaderUtil;

public interface CustomVisualDialogDelegate {
	public interface DialogCallbacks {
		void dismissDialog();
		FaderUtil getPanelFader();
	}
	void init(CustomPanelAPI panel, DialogCallbacks callbacks);
	CustomUIPanelPlugin getCustomPanelPlugin();
	float getNoiseAlpha();
	void advance(float amount);
	void reportDismissed(int option);
}
