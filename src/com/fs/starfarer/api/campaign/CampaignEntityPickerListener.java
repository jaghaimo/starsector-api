package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface CampaignEntityPickerListener {
	
//	public interface CustomMapMenuCreator {
//		void addItem(String text, Object data);
//	}
//	
//	void createLeftClickMenu(CustomMapMenuCreator menu, SectorEntityToken targetIconEntity);
//	void menuItemClicked(Object data);
	
	String getMenuItemNameOverrideFor(SectorEntityToken entity);
	void pickedEntity(SectorEntityToken entity);
	void cancelledEntityPicking();
	String getSelectedTextOverrideFor(SectorEntityToken entity);
	void createInfoText(TooltipMakerAPI info, SectorEntityToken entity);
	boolean canConfirmSelection(SectorEntityToken entity);
	float getFuelColorAlphaMult();
	float getFuelRangeMult();
}
