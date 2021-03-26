package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.ui.CustomPanelAPI;

public interface CustomDialogDelegate {
	void createCustomDialog(CustomPanelAPI panel);
	
	/**
	 * Note: customDialogCancel() will still be called if the Escape key is pressed
	 * @return
	 */
	boolean hasCancelButton();
	String getConfirmText();
	String getCancelText();
	void customDialogConfirm();
	void customDialogCancel();
	CustomUIPanelPlugin getCustomPanelPlugin();
}
