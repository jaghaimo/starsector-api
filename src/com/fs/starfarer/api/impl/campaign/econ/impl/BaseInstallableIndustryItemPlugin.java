package com.fs.starfarer.api.impl.campaign.econ.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class BaseInstallableIndustryItemPlugin implements InstallableIndustryItemPlugin {
	
	public BaseInstallableIndustryItemPlugin() {
	}

	public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode) {
		
	}

	public void createMenuItemTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		
	}

	public SpecialItemData getCurrentlyInstalledItemData() {
		return null;
	}
	
	public void setCurrentlyInstalledItemData(SpecialItemData data) {
		
	}

	public String getMenuItemTitle() {
		return null;
	}

	public String getNoItemCurrentlyInstalledText() {
		return null;
	}

	public String getNoItemsAvailableText() {
		return null;
	}

	public String getSelectItemToAssignToIndustryText() {
		return null;
	}

	public String getSelectedItemInDialogSoundId(SpecialItemData data) {
		if (data == null) return null;
		return Global.getSettings().getSpecialItemSpec(data.getId()).getSoundIdDrop();
	}


	public boolean isInstallableItem(CargoStackAPI stack) {
		return false;
	}
	
	public boolean hasMenuItemTooltip() {
		return true;
	}

	public boolean isMenuItemTooltipExpandable() {
		return false;
	}
	
	public float getMenuItemTooltipWidth() {
		return 450;
	}

	public String getNoItemsAvailableTextRemote() {
		return null;
	}

	public String getUninstallButtonText() {
		return null;
	}
	
	public boolean isMenuItemEnabled() {
		return true;
	}

	public boolean canBeInstalled(SpecialItemData data) {
		return true;
	}

}
