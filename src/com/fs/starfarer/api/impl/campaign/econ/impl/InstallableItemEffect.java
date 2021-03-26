package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.util.List;

import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin.InstallableItemDescriptionMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

/**
 * Very important for implemenations of this to not store *any* references to campaign data in data members, since
 * this is stored in a static map and persists between game loads etc.
 */
public interface InstallableItemEffect {
	void apply(Industry industry);
	void unapply(Industry industry);
	void addItemDescription(Industry industry, TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode);
	
	
	/**
	 * Only called for the industry type this item is potentially installable in. So, if
	 * there are no colony-properties-based restrictions, safe to always return true.
	 * @param industry
	 * @return
	 */
	List<String> getUnmetRequirements(Industry industry);
	List<String> getRequirements(Industry industry);
	
//	boolean canBeInstalledIn(Industry industry);
//	String getRequirementsText(Industry industry);
}