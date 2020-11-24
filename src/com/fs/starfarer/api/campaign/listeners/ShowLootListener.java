package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;

public interface ShowLootListener {
	void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog);
}
