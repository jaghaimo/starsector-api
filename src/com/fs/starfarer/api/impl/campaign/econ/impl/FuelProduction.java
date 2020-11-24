package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin.InstallableItemDescriptionMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI.MarketInteractionMode;
import com.fs.starfarer.api.impl.campaign.econ.impl.SynchrotronInstallableItemPlugin.SynchrotronEffect;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;


public class FuelProduction extends BaseIndustry {

	public void apply() {
		super.apply(true);
		
		int size = market.getSize();
		
		demand(Commodities.VOLATILES, size);
		
		supply(Commodities.FUEL, size - 2);
		
		applySynchrotronEffects();
		
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.VOLATILES);
		
		applyDeficitToProduction(1, deficit, Commodities.FUEL);
		
		if (!isFunctional()) {
			supply.clear();
		}
	}

	
	@Override
	public void unapply() {
		super.unapply();
		
		if (synchrotron != null) {
			SynchrotronEffect effect = SynchrotronInstallableItemPlugin.SYNCHROTRON_EFFECTS.get(synchrotron.getId());
			if (effect != null) {
				effect.unapply(this);
			}
		}
	}
	

	@Override
	public String getCurrentImage() {
		if (synchrotron != null) {
			return Global.getSettings().getSpriteName("industry", "advanced_fuel_prod");
		}
		return super.getCurrentImage();
	}


	@Override
	protected void upgradeFinished(Industry previous) {
		super.upgradeFinished(previous);
		
		if (previous instanceof FuelProduction) {
			setSynchrotron(((FuelProduction) previous).getSynchrotron());
		}
	}

	protected void applySynchrotronEffects() {
		if (synchrotron != null) {
			SynchrotronEffect effect = SynchrotronInstallableItemPlugin.SYNCHROTRON_EFFECTS.get(synchrotron.getId());
			if (effect != null) {
				effect.apply(this);
			}
		}
	}

	protected SpecialItemData synchrotron = null;
	public void setSynchrotron(SpecialItemData synchrotron) {
		if (synchrotron == null && this.synchrotron != null) {
			SynchrotronEffect effect = SynchrotronInstallableItemPlugin.SYNCHROTRON_EFFECTS.get(this.synchrotron.getId());
			if (effect != null) {
				effect.unapply(this);
			}
		}
		this.synchrotron = synchrotron;
	}

	public SpecialItemData getSynchrotron() {
		return synchrotron;
	}
	
	public SpecialItemData getSpecialItem() {
		return synchrotron;
	}
	
	public void setSpecialItem(SpecialItemData special) {
		synchrotron = special;
	}
	
	@Override
	public boolean wantsToUseSpecialItem(SpecialItemData data) {
		return synchrotron == null && 
				data != null &&
				SynchrotronInstallableItemPlugin.SYNCHROTRON_EFFECTS.containsKey(data.getId());
	}
	
	@Override
	public void notifyBeingRemoved(MarketInteractionMode mode, boolean forUpgrade) {
		super.notifyBeingRemoved(mode, forUpgrade);
		if (synchrotron != null && !forUpgrade) {
			CargoAPI cargo = getCargoForInteractionMode(mode);
			if (cargo != null) {
				cargo.addSpecial(synchrotron, 1);
			}
		}
	}

	@Override
	protected boolean addNonAICoreInstalledItems(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {
		if (synchrotron == null) return false;

		float opad = 10f;

		FactionAPI faction = market.getFaction();
		Color color = faction.getBaseUIColor();
		Color dark = faction.getDarkUIColor();
		
		
		SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(synchrotron.getId());
		
		TooltipMakerAPI text = tooltip.beginImageWithText(spec.getIconName(), 48);
		SynchrotronEffect effect = SynchrotronInstallableItemPlugin.SYNCHROTRON_EFFECTS.get(synchrotron.getId());
		effect.addItemDescription(text, synchrotron, InstallableItemDescriptionMode.INDUSTRY_TOOLTIP);
		tooltip.addImageWithText(opad);
		
		return true;
	}

	public boolean isDemandLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public boolean isSupplyLegal(CommodityOnMarketAPI com) {
		return true;
	}

	@Override
	public List<InstallableIndustryItemPlugin> getInstallableItems() {
		ArrayList<InstallableIndustryItemPlugin> list = new ArrayList<InstallableIndustryItemPlugin>();
		list.add(new SynchrotronInstallableItemPlugin(this));
		return list;
	}

	@Override
	public void initWithParams(List<String> params) {
		super.initWithParams(params);
		
		for (String str : params) {
			if (SynchrotronInstallableItemPlugin.SYNCHROTRON_EFFECTS.containsKey(str)) {
				setSynchrotron(new SpecialItemData(str, null));
				break;
			}
		}
	}

	@Override
	public List<SpecialItemData> getVisibleInstalledItems() {
		List<SpecialItemData> result = super.getVisibleInstalledItems();
		
		if (synchrotron != null) {
			result.add(synchrotron);
		}
		
		return result;
	}
	
	public float getPatherInterest() {
		float base = 2f;
		if (synchrotron != null) base += 4f;
		return base + super.getPatherInterest();
	}
}
