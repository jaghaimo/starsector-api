package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SynchrotronInstallableItemPlugin extends BaseInstallableIndustryItemPlugin {

	public static int FUEL_BONUS = 3;
	
	/**
	 * Very important for implemenations of this to not store *any* references to campaign data in data members, since
	 * this is stored in a static map and persists between game loads etc.
	 */
	public static interface SynchrotronEffect {
		void apply(Industry industry);
		void unapply(Industry industry);
		void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode);
	}
	
	public static Map<String, SynchrotronEffect> SYNCHROTRON_EFFECTS = new HashMap<String, SynchrotronEffect>() {{
		put(Items.SYNCHROTRON, new SynchrotronEffect() {
			public void apply(Industry industry) {
				SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(Items.SYNCHROTRON);
				if (industry instanceof BaseIndustry) {
					BaseIndustry b = (BaseIndustry) industry;
					//b.demand(1, Commodities.VOLATILES, FUEL_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
					b.supply(2, Commodities.FUEL, FUEL_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
					
				}
			}
			public void unapply(Industry industry) {
				if (industry instanceof BaseIndustry) {
					BaseIndustry b = (BaseIndustry) industry;
					//b.demand(1, Commodities.VOLATILES, 0, null);
					b.supply(2, Commodities.FUEL, 0, null);
				}
			}
			public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode) {
				SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(Items.SYNCHROTRON);
				String name = Misc.ucFirst(spec.getName().toLowerCase());
				String pre = "";
				float pad = 0f;
				if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_LIST ||
						mode == InstallableItemDescriptionMode.INDUSTRY_TOOLTIP) {
					pre = name + ". ";
				} else if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_INSTALLED || 
						mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP) {
					pre = name + " currently installed. ";
				}
				if (mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP ||
						mode == InstallableItemDescriptionMode.CARGO_TOOLTIP) {
					pad = 10f;
				}
				//text.addPara(pre + "Increases fuel production and demand for volatiles by %s.",
				text.addPara(pre + "Increases fuel production by %s units.",
						pad, Misc.getHighlightColor(), "" + FUEL_BONUS);
			}
		});
	}};
	
	
	private FuelProduction industry;
	
	public SynchrotronInstallableItemPlugin(FuelProduction industry) {
		this.industry = industry;
	}


	@Override
	public String getMenuItemTitle() {
		if (getCurrentlyInstalledItemData() == null) {
			return "Install synchrotron core...";
		}
		return "Manage synchrotron core...";
	}
	
	@Override
	public String getUninstallButtonText() {
		return "Uninstall synchrotron core";
	}


	@Override
	public boolean isInstallableItem(CargoStackAPI stack) {
		if (!stack.isSpecialStack()) return false;
		
		return SYNCHROTRON_EFFECTS.containsKey(stack.getSpecialDataIfSpecial().getId());
	}
	
	
	@Override
	public SpecialItemData getCurrentlyInstalledItemData() {
		return industry.getSynchrotron();
	}
	
	@Override
	public void setCurrentlyInstalledItemData(SpecialItemData data) {
		industry.setSynchrotron(data);
	}
	
	@Override
	public String getNoItemCurrentlyInstalledText() {
		return "No synchrotron core currently installed";
	}

	@Override
	public String getNoItemsAvailableText() {
		return "No synchrotron core available";
	}
	
	@Override
	public String getNoItemsAvailableTextRemote() {
		return "No synchrotron core available in storage";
	}

	@Override
	public String getSelectItemToAssignToIndustryText() {
		return "Select synchrotron core to install for " + industry.getCurrentName();
	}

	@Override
	public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, 
								   InstallableItemDescriptionMode mode) {
		SynchrotronEffect effect = SYNCHROTRON_EFFECTS.get(data.getId());
		if (effect != null) {
			effect.addItemDescription(text, data, mode);
		}
	}

	@Override
	public boolean isMenuItemTooltipExpandable() {
		return false;
	}

	@Override
	public float getMenuItemTooltipWidth() {
		return super.getMenuItemTooltipWidth();
	}
	
	@Override
	public boolean hasMenuItemTooltip() {
		return super.hasMenuItemTooltip();
	}

	@Override
	public void createMenuItemTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		float pad = 3f;
		float opad = 10f;
		
		tooltip.addPara("An irreplaceable piece of Domain technology, " +
						"a synchrotron core enables the mass production of fuel.", 0f);

		SpecialItemData data = industry.getSynchrotron();
		if (data == null) {
			tooltip.addPara(getNoItemCurrentlyInstalledText() + ".", opad);
		} else {
			SynchrotronEffect effect = SYNCHROTRON_EFFECTS.get(data.getId());
			effect.addItemDescription(tooltip, data, InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP);
		}
	}
	
	
}




