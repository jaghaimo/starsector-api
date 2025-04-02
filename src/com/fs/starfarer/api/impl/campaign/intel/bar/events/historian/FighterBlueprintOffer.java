package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;

public class FighterBlueprintOffer extends BaseHistorianOfferWithLocation {

	protected String data;
	
	public FighterBlueprintOffer(SectorEntityToken entity, String data) {
		super(entity);
		this.data = data;
		
	}

	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog) {
		FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(data);
		dialog.getOptionPanel().addOption("... the possible location of a blueprint (" + 
								spec.getVariant().getFullDesignationWithHullName() + ")",
								//spec.getWingName() + ")",
										this);
		SetStoryOption.set(dialog, 1, this, "historianBP", Sounds.STORY_POINT_SPEND_TECHNOLOGY,
				"Learned location of " + spec.getVariant().getFullDesignationWithHullName() + " blueprint");
	}
	
	public String getSortString() {
		if (getTagsForSort().contains(Tags.INTEL_FLEET_LOG) || getTagsForSort().contains(Tags.INTEL_EXPLORATION)) {
			return getSortStringNewestFirst();
		}
		return "Weapon Blueprint";
	}
	
	public String getName() {
		FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(data);
		if (isEnding()) {
			return spec.getWingName() + " Blueprint - Recovered";
		} else {
			return spec.getWingName() + " Blueprint Location";
		}
	}

	@Override
	protected void addItemToCargo(CargoAPI loot) {
		loot.addSpecial(new SpecialItemData(Items.FIGHTER_BP, data), 1);		
	}

	public String getData() {
		return data;
	}

	
}










