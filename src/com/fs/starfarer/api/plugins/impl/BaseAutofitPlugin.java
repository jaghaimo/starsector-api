package com.fs.starfarer.api.plugins.impl;

import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.plugins.AutofitPlugin;

public class BaseAutofitPlugin implements AutofitPlugin {

	public void doFit(ShipVariantAPI current, ShipVariantAPI target, int maxSMods, AutofitPluginDelegate delegate) {
		
	}

	public List<AutofitOption> getOptions() {
		return null;
	}

	public float getRating(ShipVariantAPI current, ShipVariantAPI target, AutofitPluginDelegate delegate) {
		return 0;
	}

	public int getCreditCost() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void doQuickAction(ShipVariantAPI current, AutofitPluginDelegate delegate) {
		
	}

	public String getQuickActionText() {
		return null;
	}

	public String getQuickActionTooltip() {
		return null;
	}

	public void autoAssignOfficers(CampaignFleetAPI fleet) {
		
	}

	public boolean isQuickActionEnabled(ShipVariantAPI currentVariant) {
		return false;
	}

	public void setRandom(Random random) {
		
	}

}
