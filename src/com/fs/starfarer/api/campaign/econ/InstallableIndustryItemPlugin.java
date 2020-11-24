package com.fs.starfarer.api.campaign.econ;

import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface InstallableIndustryItemPlugin {

	public static enum InstallableItemDescriptionMode {
		INDUSTRY_TOOLTIP,
		INDUSTRY_MENU_TOOLTIP,
		MANAGE_ITEM_DIALOG_LIST,
		MANAGE_ITEM_DIALOG_INSTALLED,
		CARGO_TOOLTIP,
	}
	
	public SpecialItemData getCurrentlyInstalledItemData();
	public void setCurrentlyInstalledItemData(SpecialItemData data);

	public void addItemDescription(TooltipMakerAPI text, SpecialItemData data,
								   InstallableItemDescriptionMode mode);

	public String getUninstallButtonText();
	public String getMenuItemTitle();
	
	public String getNoItemCurrentlyInstalledText();
	public String getSelectItemToAssignToIndustryText();
	public String getNoItemsAvailableText();
	public String getNoItemsAvailableTextRemote();

	public boolean isInstallableItem(CargoStackAPI stack);

	public String getSelectedItemInDialogSoundId(SpecialItemData data);
	public void createMenuItemTooltip(TooltipMakerAPI tooltip, boolean expanded);
	public boolean isMenuItemTooltipExpandable();
	public boolean hasMenuItemTooltip();
	public float getMenuItemTooltipWidth();



}
