package com.fs.starfarer.api.campaign;

import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ArrowData;
import com.fs.starfarer.api.ui.MarkerData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface CampaignEntityPickerListener {
	
	String getMenuItemNameOverrideFor(SectorEntityToken entity);
	void pickedEntity(SectorEntityToken entity);
	void cancelledEntityPicking();
	String getSelectedTextOverrideFor(SectorEntityToken entity);
	void createInfoText(TooltipMakerAPI info, SectorEntityToken entity);
	boolean canConfirmSelection(SectorEntityToken entity);
	float getFuelColorAlphaMult();
	float getFuelRangeMult();
	
	List<ArrowData> getArrows();
	
	/**
	 * If null: shows player location.
	 * @return
	 */
	List<MarkerData> getMarkers();
	
	
	/**
	 * If null: shows systems the entities are in.
	 * @return
	 */
	Set<StarSystemAPI> getStarSystemsToShow();
}
