/**
 * 
 */
package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin.InstallableItemDescriptionMode;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

public abstract class BaseInstallableItemEffect implements InstallableItemEffect {
	public String id;
	public SpecialItemSpecAPI spec;
	public BaseInstallableItemEffect(String id) {
		this.id = id;
		spec = Global.getSettings().getSpecialItemSpec(id);
	}
	
	public float getItemDescPad(InstallableItemDescriptionMode mode) {
		float pad = 0f;
		if (mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP ||
				mode == InstallableItemDescriptionMode.CARGO_TOOLTIP) {
			pad = 10f;
		}
		return pad;
	}
	public String getItemInstalledText(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode) {
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
		return pre;
	}
	
	public void addItemDescription(Industry industry, TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode) {
		String pre = getItemInstalledText(text, data, mode);
		float pad = getItemDescPad(mode);
		addItemDescriptionImpl(industry, text, data, mode, pre, pad);
	}
	
	protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode, String pre, float pad) {
		
	}
	
	public String [] getSimpleReqs(Industry industry) {
		return new String [0];
	}
	
	public List<String> getRequirements(Industry industry) {
		List<String> reqs = new ArrayList<String>();
		for (String curr : getSimpleReqs(industry)) {
			reqs.add(curr);
		}
		return reqs;
	}
	
	public List<String> getUnmetRequirements(Industry industry) {
		List<String> unmet = new ArrayList<String>();
		if (industry == null) return unmet;
		
		MarketAPI market = industry.getMarket();
		
		for (String curr : getRequirements(industry)) {
			if (ItemEffectsRepo.NO_ATMOSPHERE.equals(curr)) {
				if (!market.hasCondition(Conditions.NO_ATMOSPHERE)) {
					unmet.add(curr);
				}
			} else if (ItemEffectsRepo.HABITABLE.equals(curr)) {
				if (!market.hasCondition(Conditions.HABITABLE)) {
					unmet.add(curr);
				}
			} else if (ItemEffectsRepo.NOT_HABITABLE.equals(curr)) {
				if (market.hasCondition(Conditions.HABITABLE)) {
					unmet.add(curr);
				}
			} else if (ItemEffectsRepo.GAS_GIANT.equals(curr)) {
				if (market.getPlanetEntity() != null && !market.getPlanetEntity().isGasGiant()) {
					unmet.add(curr);
				}
			} else if (ItemEffectsRepo.NOT_A_GAS_GIANT.equals(curr)) {
				if (market.getPlanetEntity() != null && market.getPlanetEntity().isGasGiant()) {
					unmet.add(curr);
				}
			} else if (ItemEffectsRepo.NOT_EXTREME_WEATHER.equals(curr)) {
				if (market.hasCondition(Conditions.EXTREME_WEATHER)) {
					unmet.add(curr);
				}
			} else if (ItemEffectsRepo.NOT_EXTREME_TECTONIC_ACTIVITY.equals(curr)) {
				if (market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
					unmet.add(curr);
				}
			} else if (ItemEffectsRepo.NO_TRANSPLUTONIC_ORE_DEPOSITS.equals(curr)) {
				if (market.hasCondition(Conditions.RARE_ORE_SPARSE) ||
						market.hasCondition(Conditions.RARE_ORE_MODERATE) ||
						market.hasCondition(Conditions.RARE_ORE_ABUNDANT) ||
						market.hasCondition(Conditions.RARE_ORE_RICH) ||
						market.hasCondition(Conditions.RARE_ORE_ULTRARICH)) {
					unmet.add(curr);
				}
			} else if (ItemEffectsRepo.NO_VOLATILES_DEPOSITS.equals(curr)) {
				if (market.hasCondition(Conditions.VOLATILES_TRACE) ||
						market.hasCondition(Conditions.VOLATILES_DIFFUSE) ||
						market.hasCondition(Conditions.VOLATILES_ABUNDANT) ||
						market.hasCondition(Conditions.VOLATILES_PLENTIFUL)) {
					unmet.add(curr);
				}
			} else if (ItemEffectsRepo.HOT_OR_EXTREME_HEAT.equals(curr)) {
				if (!market.hasCondition(Conditions.HOT) &&
						!market.hasCondition(Conditions.VERY_HOT)) {
					unmet.add(curr);
				}
			} else if (ItemEffectsRepo.COLD_OR_EXTREME_COLD.equals(curr)) {
				if (!market.hasCondition(Conditions.COLD) &&
						!market.hasCondition(Conditions.VERY_COLD)) {
					unmet.add(curr);
				}
			} else if (ItemEffectsRepo.CORONAL_TAP_RANGE.equals(curr)) {
				Pair<SectorEntityToken, Float> p = PopulationAndInfrastructure.getNearestCoronalTap(
														market.getLocationInHyperspace(), true);
				float dist = Float.MAX_VALUE;
				if (p != null) dist = p.two;
				if (dist > ItemEffectsRepo.CORONAL_TAP_LIGHT_YEARS) {
					unmet.add(curr);
				}
			}
		}
		
		return unmet;
	}
	
	
	protected void addRequirements(TooltipMakerAPI text, boolean canInstall, 
								InstallableItemDescriptionMode mode, String ... reqs) {
		if (canInstall) return;
		
		float opad = 10f;
		String list = "";
		for (String curr : reqs) {
			curr = curr.trim();
			list += curr + ", ";
		}
		
		Color reqColor = Misc.getBasePlayerColor();
		if (mode != InstallableItemDescriptionMode.CARGO_TOOLTIP) {
			reqColor = Misc.getNegativeHighlightColor();
			opad = 0f;
		}
		
		if (!list.isEmpty()) list = list.substring(0, list.length() - 2);
		if (!list.isEmpty()) {
			text.addPara("Requires: " + list, opad, 
					Misc.getGrayColor(), reqColor, reqs);
		}
	}
	
	

//	public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode) {
//	}
//	public void apply(Industry industry) {
//	}
//	public void unapply(Industry industry) {
//	}
	
//	public boolean canBeInstalledIn(Industry industry) {
//		return true;
//	}
//	public String getRequirementsText(Industry industry) {
//		return null;
//	}
	
}






