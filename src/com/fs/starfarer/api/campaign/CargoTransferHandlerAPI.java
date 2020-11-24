package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.campaign.econ.SubmarketAPI;

public interface CargoTransferHandlerAPI {
	Object getManifestOne();
	SubmarketAPI getSubmarketTradedWith();
	boolean isNoCost();
	float computeCurrentSingleItemSellCost(CargoStackAPI stack);
	float computeCurrentSingleItemBuyCost(CargoStackAPI stack);

}
