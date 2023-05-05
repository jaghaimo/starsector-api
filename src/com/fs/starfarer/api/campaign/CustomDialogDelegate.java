package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.ui.CustomPanelAPI;

public interface CustomDialogDelegate {
	
	public interface CustomDialogCallback {
		/**
		 * 0 for confirm, 1 for cancel.
		 */
		void dismissCustomDialog(int option);
	}
	
	void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback);
	
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
