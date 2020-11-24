package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;

public interface ColonyPlayerHostileActListener {

	void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market, TempData actionData, CargoAPI cargo);
	void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData, Industry industry);
	
	void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData);
	void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData);
}
