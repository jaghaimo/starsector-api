package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.impl.items.GenericSpecialItemPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class GenericInstallableItemPlugin extends BaseInstallableIndustryItemPlugin {
	
	protected Industry industry;
	
	public GenericInstallableItemPlugin(Industry industry) {
		this.industry = industry;
	}

	@Override
	public String getMenuItemTitle() {
		if (getCurrentlyInstalledItemData() == null) {
			return "Install item...";
		}
		return "Manage item...";
	}
	
	@Override
	public String getUninstallButtonText() {
		return "Uninstall item";
	}


	@Override
	public boolean isInstallableItem(CargoStackAPI stack) {
		if (!stack.isSpecialStack()) return false;
		
		//String industryId = ItemEffectsRepo.ITEM_TO_INDUSTRY.get(stack.getSpecialDataIfSpecial().getId());
//		String industryId = stack.getPlugin().getSpec().getParams();
//		if (industryId == null || industryId.isEmpty() || !industryId.equals(industry.getId())) {
//			return false;
//		}
		
		String [] industries = stack.getPlugin().getSpec().getParams().split(",");
		Set<String> all = new HashSet<String>();
		for (String ind: industries) all.add(ind.trim());
		if (!all.contains(industry.getId())) return false;
		
		return ItemEffectsRepo.ITEM_EFFECTS.containsKey(stack.getSpecialDataIfSpecial().getId());
	}
	
	@Override
	public SpecialItemData getCurrentlyInstalledItemData() {
		return industry.getSpecialItem();
	}
	
	@Override
	public void setCurrentlyInstalledItemData(SpecialItemData data) {
		industry.setSpecialItem(data);
	}
	
	@Override
	public String getNoItemCurrentlyInstalledText() {
		return "No item currently installed";
	}

	@Override
	public String getNoItemsAvailableText() {
		return "No suitable items available";
	}
	
	@Override
	public String getNoItemsAvailableTextRemote() {
		return "No suitable items available in storage";
	}

	@Override
	public String getSelectItemToAssignToIndustryText() {
		return "Select item to install for " + industry.getCurrentName();
	}
	
	public boolean canBeInstalled(SpecialItemData data) {
		InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(data.getId());
		if (effect != null) {
			//return effect.canBeInstalledIn(industry);
			List<String> unmet = effect.getUnmetRequirements(industry);
			return unmet == null || unmet.isEmpty();
		}
		return true;
	}


	@Override
	public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, 
								   InstallableItemDescriptionMode mode) {
		InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(data.getId());
		if (effect != null) {
			List<String> unmet = effect.getUnmetRequirements(industry);
			boolean canInstall = unmet == null || unmet.isEmpty();
			if (!canInstall) {
				GenericSpecialItemPlugin.addReqsSection(industry, effect, text, true, 0f);
			} else {
				effect.addItemDescription(industry, text, data, mode);
			}
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
		
//		tooltip.addPara("A likely Domain-era item improving the colony's infrastructure. " +
//						"Only one such item may be installed at a colony at a time.", 0f);
		tooltip.addPara("Certain Domain-era artifacts might be installed here to improve the colony. " +
						"Only one such artifact may be installed at an industry at a time.", 0f);

		SpecialItemData data = industry.getSpecialItem();
		if (data == null) {
			tooltip.addPara(getNoItemCurrentlyInstalledText() + ".", opad);
		} else {
			InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(data.getId());
//			effect.addItemDescription(industry, tooltip, data, InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP);
			
			SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(data.getId());
			TooltipMakerAPI text = tooltip.beginImageWithText(spec.getIconName(), 48);
			effect.addItemDescription(industry, text, data, InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP);
			tooltip.addImageWithText(opad);
		}
	}
	
	
}










