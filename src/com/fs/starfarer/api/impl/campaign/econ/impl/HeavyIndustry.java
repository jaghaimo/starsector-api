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
import com.fs.starfarer.api.impl.campaign.econ.impl.NanoforgeInstallableItemPlugin.NanoforgeEffect;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;


public class HeavyIndustry extends BaseIndustry {

	public static float ORBITAL_WORKS_QUALITY_BONUS = 0.2f;
	
	public void apply() {
		super.apply(true);
		
		int size = market.getSize();
		
		boolean works = Industries.ORBITALWORKS.equals(getId());
		int shipBonus = 0;
		float qualityBonus = 0;
		if (works) {
			//shipBonus = 2;
			qualityBonus = ORBITAL_WORKS_QUALITY_BONUS;
		}
		
		demand(Commodities.METALS, size);
		demand(Commodities.RARE_METALS, size - 2);
		
		supply(Commodities.HEAVY_MACHINERY, size - 2);
		supply(Commodities.SUPPLIES, size - 2);
		supply(Commodities.HAND_WEAPONS, size - 2);
		supply(Commodities.SHIPS, size - 2);
		if (shipBonus > 0) {
			supply(1, Commodities.SHIPS, shipBonus, "Orbital works");
		}
		
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.METALS, Commodities.RARE_METALS);
		int maxDeficit = size - 3; // to allow *some* production so economy doesn't get into an unrecoverable state
		if (deficit.two > maxDeficit) deficit.two = maxDeficit;
		
		applyDeficitToProduction(2, deficit,
					Commodities.HEAVY_MACHINERY,
					Commodities.SUPPLIES,
					Commodities.HAND_WEAPONS,
					Commodities.SHIPS);
		
//		if (market.getId().equals("chicomoztoc")) {
//			System.out.println("efwefwe");
//		}
		
		applyNanoforgeEffects();

		if (qualityBonus > 0) {
			market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(1), qualityBonus, "Orbital works");
		}
		
		float stability = market.getPrevStability();
		if (stability < 5) {
			float stabilityMod = (stability - 5f) / 5f;
			stabilityMod *= 0.5f;
			//market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(0), stabilityMod, "Low stability at production source");
			market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(0), stabilityMod, getNameForModifier() + " - low stability");
		}
		
		if (!isFunctional()) {
			supply.clear();
			unapply();
		}
	}
	
	@Override
	public void unapply() {
		super.unapply();
		
		if (nanoforge != null) {
			NanoforgeEffect effect = NanoforgeInstallableItemPlugin.NANOFORGE_EFFECTS.get(nanoforge.getId());
			if (effect != null) {
				effect.unapply(this);
			}
		}
		
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId(0));
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId(1));
	}

	
	
	@Override
	protected void upgradeFinished(Industry previous) {
		super.upgradeFinished(previous);
		
		if (previous instanceof HeavyIndustry) {
			setNanoforge(((HeavyIndustry) previous).getNanoforge());
		}
	}

	protected void applyNanoforgeEffects() {
//		if (Global.getSector().getEconomy().isSimMode()) {
//			return;
//		}
		
		if (nanoforge != null) {
			NanoforgeEffect effect = NanoforgeInstallableItemPlugin.NANOFORGE_EFFECTS.get(nanoforge.getId());
			if (effect != null) {
				effect.apply(this);
			}
		}
	}

	protected SpecialItemData nanoforge = null;
	public void setNanoforge(SpecialItemData nanoforge) {
		if (nanoforge == null && this.nanoforge != null) {
			NanoforgeEffect effect = NanoforgeInstallableItemPlugin.NANOFORGE_EFFECTS.get(this.nanoforge.getId());
			if (effect != null) {
				effect.unapply(this);
			}
		}
		this.nanoforge = nanoforge;
	}

	public SpecialItemData getNanoforge() {
		return nanoforge;
	}
	
	public SpecialItemData getSpecialItem() {
		return nanoforge;
	}
	
	public void setSpecialItem(SpecialItemData special) {
		nanoforge = special;
	}
	
	@Override
	public boolean wantsToUseSpecialItem(SpecialItemData data) {
		if (nanoforge != null && Items.CORRUPTED_NANOFORGE.equals(nanoforge.getId()) &&
				data != null && Items.PRISTINE_NANOFORGE.equals(data.getId())) {
			return true;
		}
		
		return nanoforge == null && 
				data != null &&
				NanoforgeInstallableItemPlugin.NANOFORGE_EFFECTS.containsKey(data.getId());
	}
	
	@Override
	protected void addPostSupplySection(TooltipMakerAPI tooltip, boolean hasSupply, IndustryTooltipMode mode) {
		super.addPostSupplySection(tooltip, hasSupply, mode);
	}
	
	@Override
	public void notifyBeingRemoved(MarketInteractionMode mode, boolean forUpgrade) {
		super.notifyBeingRemoved(mode, forUpgrade);
		if (nanoforge != null && !forUpgrade) {
			CargoAPI cargo = getCargoForInteractionMode(mode);
			if (cargo != null) {
				cargo.addSpecial(nanoforge, 1);
			}
		}
	}

	@Override
	protected boolean addNonAICoreInstalledItems(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {
		if (nanoforge == null) return false;

		float opad = 10f;

		FactionAPI faction = market.getFaction();
		Color color = faction.getBaseUIColor();
		Color dark = faction.getDarkUIColor();
		
		
		SpecialItemSpecAPI nanoforgeSpec = Global.getSettings().getSpecialItemSpec(nanoforge.getId());
		
		TooltipMakerAPI text = tooltip.beginImageWithText(nanoforgeSpec.getIconName(), 48);
		NanoforgeEffect effect = NanoforgeInstallableItemPlugin.NANOFORGE_EFFECTS.get(nanoforge.getId());
		effect.addItemDescription(text, nanoforge, InstallableItemDescriptionMode.INDUSTRY_TOOLTIP);
		tooltip.addImageWithText(opad);
		
		return true;
	}

	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		//if (mode == IndustryTooltipMode.NORMAL && isFunctional()) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {			
			boolean works = Industries.ORBITALWORKS.equals(getId());
			if (works) {
				float total = ORBITAL_WORKS_QUALITY_BONUS;
				String totalStr = "+" + (int)Math.round(total * 100f) + "%";
				Color h = Misc.getHighlightColor();
				if (total < 0) {
					h = Misc.getNegativeHighlightColor();
					totalStr = "" + (int)Math.round(total * 100f) + "%";
				}
				float opad = 10f;
				if (total >= 0) {
					tooltip.addPara("Ship quality: %s", opad, h, totalStr);
					tooltip.addPara("*Quality bonus only applies for the largest ship producer in the faction.", 
							Misc.getGrayColor(), opad);
				}
			}
		}
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
		list.add(new NanoforgeInstallableItemPlugin(this));
		return list;
	}

	@Override
	public void initWithParams(List<String> params) {
		super.initWithParams(params);
		
		for (String str : params) {
			if (NanoforgeInstallableItemPlugin.NANOFORGE_EFFECTS.containsKey(str)) {
				setNanoforge(new SpecialItemData(str, null));
				break;
			}
		}
	}

	@Override
	public List<SpecialItemData> getVisibleInstalledItems() {
		List<SpecialItemData> result = super.getVisibleInstalledItems();
		
		if (nanoforge != null) {
			result.add(nanoforge);
		}
		
		return result;
	}
	
	public float getPatherInterest() {
		float base = 2f;
		if (nanoforge != null) base += 4f;
		return base + super.getPatherInterest();
	}
	
}









