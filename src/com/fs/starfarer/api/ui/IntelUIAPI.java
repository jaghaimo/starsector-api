package com.fs.starfarer.api.ui;

import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;

public interface IntelUIAPI {

	void updateUIForItem(IntelInfoPlugin plugin);
	void recreateIntelUI();
	void showDialog(SectorEntityToken target, String ruleId);
	void showDialog(SectorEntityToken target, InteractionDialogPlugin plugin);
}
