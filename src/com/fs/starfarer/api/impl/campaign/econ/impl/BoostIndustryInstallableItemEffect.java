/**
 * 
 */
package com.fs.starfarer.api.impl.campaign.econ.impl;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.util.Misc;

public abstract class BoostIndustryInstallableItemEffect extends BaseInstallableItemEffect {

	protected int supplyIncrease = 0;
	protected int demandIncrease = 0;

	public BoostIndustryInstallableItemEffect(String id, int supplyIncrease, int demandIncrease) {
		super(id);
		this.supplyIncrease = supplyIncrease;
		this.demandIncrease = demandIncrease;
	}

	public void apply(Industry industry) {
		if (supplyIncrease != 0) {
			industry.getSupplyBonus().modifyFlat(spec.getId(), supplyIncrease,
					Misc.ucFirst(spec.getName().toLowerCase()));
		}
		if (demandIncrease != 0) {
			industry.getDemandReduction().modifyFlat(spec.getId(), -demandIncrease,
					Misc.ucFirst(spec.getName().toLowerCase()));
		}
	}
	public void unapply(Industry industry) {
		if (supplyIncrease != 0) {
			industry.getSupplyBonus().modifyFlat(spec.getId(), 0,
					Misc.ucFirst(spec.getName().toLowerCase()));
		}
		if (demandIncrease != 0) {
			industry.getDemandReduction().modifyFlat(spec.getId(), 0,
					Misc.ucFirst(spec.getName().toLowerCase()));
		}
	}

	public int getSupplyIncrease() {
		return supplyIncrease;
	}

	public void setSupplyIncrease(int supplyIncrease) {
		this.supplyIncrease = supplyIncrease;
	}

	public int getDemandIncrease() {
		return demandIncrease;
	}

	public void setDemandIncrease(int demandIncrease) {
		this.demandIncrease = demandIncrease;
	}
	
	
//	public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode) {
//	}
	
}






