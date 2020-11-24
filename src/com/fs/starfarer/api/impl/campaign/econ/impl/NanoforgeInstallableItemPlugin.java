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
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class NanoforgeInstallableItemPlugin extends BaseInstallableIndustryItemPlugin {

	public static float CORRUPTED_NANOFORGE_BONUS = 0.2f;
	public static float PRISTINE_NANOFORGE_BONUS = 0.5f;
	
	public static int CORRUPTED_NANOFORGE_PROD = 1;
	public static int PRISTINE_NANOFORGE_PROD = 3;


	/**
	 * Very important for implemenations of this to not store *any* references to campaign data in data members, since
	 * this is stored in a static map and persists between game loads etc.
	 */
	public static interface NanoforgeEffect {
		void apply(Industry industry);
		void unapply(Industry industry);
		void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode);
	}
	
	public static Map<String, NanoforgeEffect> NANOFORGE_EFFECTS = new HashMap<String, NanoforgeEffect>() {{
		put(Items.CORRUPTED_NANOFORGE, new NanoforgeEffect() {
			public void apply(Industry industry) {
				SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(Items.CORRUPTED_NANOFORGE);
				industry.getMarket().getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD)
						.modifyFlat("nanoforge", CORRUPTED_NANOFORGE_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
				
				if (industry instanceof BaseIndustry) {
					BaseIndustry b = (BaseIndustry) industry;
					int bonus = CORRUPTED_NANOFORGE_PROD;
					b.supply(3, Commodities.HEAVY_MACHINERY, bonus, Misc.ucFirst(spec.getName().toLowerCase()));
					b.supply(3, Commodities.SUPPLIES, bonus, Misc.ucFirst(spec.getName().toLowerCase()));
					b.supply(3, Commodities.HAND_WEAPONS, bonus, Misc.ucFirst(spec.getName().toLowerCase()));
					b.supply(3, Commodities.SHIPS, bonus, Misc.ucFirst(spec.getName().toLowerCase()));
				}
			}
			public void unapply(Industry industry) {
				industry.getMarket().getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD)
						.unmodifyFlat("nanoforge");
				
				if (industry instanceof BaseIndustry) {
					BaseIndustry b = (BaseIndustry) industry;
					b.supply(3, Commodities.HEAVY_MACHINERY, 0, null);
					b.supply(3, Commodities.SUPPLIES, 0, null);
					b.supply(3, Commodities.HAND_WEAPONS, 0, null);
					b.supply(3, Commodities.SHIPS, 0, null);
				}
			}
			public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode) {
				SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(Items.CORRUPTED_NANOFORGE);
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
				text.addPara(pre + "Increases ship and weapon production quality by %s. " +
						"Increases production by %s unit.",
						pad, Misc.getHighlightColor(), 
						"" + (int) Math.round(CORRUPTED_NANOFORGE_BONUS * 100f) + "%",
						"" + (int) CORRUPTED_NANOFORGE_PROD);
			}
		});
		put(Items.PRISTINE_NANOFORGE, new NanoforgeEffect() {
			public void apply(Industry industry) {
				SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(Items.PRISTINE_NANOFORGE);
				industry.getMarket().getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD)
						.modifyFlat("nanoforge", PRISTINE_NANOFORGE_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
				
				if (industry instanceof BaseIndustry) {
					BaseIndustry b = (BaseIndustry) industry;
					int bonus = PRISTINE_NANOFORGE_PROD;
					b.supply(3, Commodities.HEAVY_MACHINERY, bonus, Misc.ucFirst(spec.getName().toLowerCase()));
					b.supply(3, Commodities.SUPPLIES, bonus, Misc.ucFirst(spec.getName().toLowerCase()));
					b.supply(3, Commodities.HAND_WEAPONS, bonus, Misc.ucFirst(spec.getName().toLowerCase()));
					b.supply(3, Commodities.SHIPS, bonus, Misc.ucFirst(spec.getName().toLowerCase()));
				}
			}
			public void unapply(Industry industry) {
				industry.getMarket().getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD)
						.unmodifyFlat("nanoforge");
				
				if (industry instanceof BaseIndustry) {
					BaseIndustry b = (BaseIndustry) industry;
					b.supply(3, Commodities.HEAVY_MACHINERY, 0, null);
					b.supply(3, Commodities.SUPPLIES, 0, null);
					b.supply(3, Commodities.HAND_WEAPONS, 0, null);
					b.supply(3, Commodities.SHIPS, 0, null);
				}
			}
			public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode) {
				SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(Items.PRISTINE_NANOFORGE);
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
				text.addPara(pre + "Increases ship and weapon production quality by %s. " +
						"Increases production by %s units.",
						pad, Misc.getHighlightColor(), 
						"" + (int) Math.round(PRISTINE_NANOFORGE_BONUS * 100f) + "%",
						"" + (int) PRISTINE_NANOFORGE_PROD);
			}
		});
	}};
	
	
	private HeavyIndustry industry;
	
	public NanoforgeInstallableItemPlugin(HeavyIndustry industry) {
		this.industry = industry;
	}


	@Override
	public String getMenuItemTitle() {
		if (getCurrentlyInstalledItemData() == null) {
			return "Install nanoforge...";
		}
		return "Manage nanoforge...";
	}
	
	@Override
	public String getUninstallButtonText() {
		return "Uninstall nanoforge";
	}


	@Override
	public boolean isInstallableItem(CargoStackAPI stack) {
		if (!stack.isSpecialStack()) return false;
		
		return NANOFORGE_EFFECTS.containsKey(stack.getSpecialDataIfSpecial().getId());
	}
	
	
	@Override
	public SpecialItemData getCurrentlyInstalledItemData() {
		return industry.getNanoforge();
	}
	
	@Override
	public void setCurrentlyInstalledItemData(SpecialItemData data) {
		industry.setNanoforge(data);
	}
	
	@Override
	public String getNoItemCurrentlyInstalledText() {
		return "No nanoforge currently installed";
	}

	@Override
	public String getNoItemsAvailableText() {
		return "No nanoforges available";
	}
	
	@Override
	public String getNoItemsAvailableTextRemote() {
		return "No nanoforges available in storage";
	}

	@Override
	public String getSelectItemToAssignToIndustryText() {
		return "Select nanoforge to install for " + industry.getCurrentName();
	}

	@Override
	public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, 
								   InstallableItemDescriptionMode mode) {
		NanoforgeEffect effect = NANOFORGE_EFFECTS.get(data.getId());
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
						"a nanoforge improves the quality of ship production by reducing the number of manufacturing defects. It also makes the construction of higher-tier weapons easier.", 0f);

		SpecialItemData data = industry.getNanoforge();
		if (data == null) {
			tooltip.addPara(getNoItemCurrentlyInstalledText() + ".", opad);
		} else {
			NanoforgeEffect effect = NANOFORGE_EFFECTS.get(data.getId());
			effect.addItemDescription(tooltip, data, InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP);
		}
	}
	
	
}




