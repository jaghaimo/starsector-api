package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.SectorEntityToken;

/**
 * See: BaseSalvageSpecial.setExtraSalvage() etc.
 */
public interface ExtraSalvageShownListener {
	void reportExtraSalvageShown(SectorEntityToken entity);
}
