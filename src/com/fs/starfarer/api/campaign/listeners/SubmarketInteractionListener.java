package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.econ.SubmarketAPI;

public interface SubmarketInteractionListener {
	public static enum SubmarketInteractionType {
		SHIPS,
		CARGO,
	}
	void reportPlayerOpenedSubmarket(SubmarketAPI submarket, SubmarketInteractionType type);
}
