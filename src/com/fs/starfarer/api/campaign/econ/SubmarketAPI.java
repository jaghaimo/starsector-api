package com.fs.starfarer.api.campaign.econ;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.SubmarketPlugin.TransferAction;

public interface SubmarketAPI {
	MarketAPI getMarket();
	SubmarketSpecAPI getSpec();
	
	SubmarketPlugin getPlugin();
	
	FactionAPI getFaction();
	void setFaction(FactionAPI faction);
	
	String getSpecId();
	String getName();
	String getNameOneLine();
	
	
	CargoAPI getCargo();
	
	/**
	 * Fraction of the price.
	 * @return
	 */
	float getTariff();
	CargoAPI getCargoNullOk();
	boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action);
	
//	boolean isBlackMarket();
//	boolean isOpenMarket();
	
}
