package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;

public interface CargoScreenListener {
	void reportCargoScreenOpened();
	void reportPlayerLeftCargoPods(SectorEntityToken entity);
	void reportPlayerNonMarketTransaction(PlayerMarketTransaction transaction, InteractionDialogAPI dialog);
	void reportSubmarketOpened(SubmarketAPI submarket);
	
}
