package com.fs.starfarer.api.ui;

import java.util.List;

import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.intel.misc.MapMarkerIntel;

public interface IntelUIAPI {

	void updateUIForItem(IntelInfoPlugin plugin);
	void recreateIntelUI();
	void showDialog(SectorEntityToken target, String trigger);
	void showDialog(SectorEntityToken target, InteractionDialogPlugin plugin);
	void showEditIntelMarkerDialog(MapMarkerIntel intel);
	void updateIntelList();
	void updateIntelList(boolean retainCurrentSelection);
	void updateIntelList(boolean retainCurrentSelection, List<IntelInfoPlugin> show);
	void selectItem(IntelInfoPlugin plugin);
	boolean isShowingCustomIntelSubset();
	void showOnMap(SectorEntityToken token);
	void saveSmallDescScrollState();
	void restoreSmallDescScrollState();
}
