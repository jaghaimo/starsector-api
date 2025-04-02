package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;

public class SpecialItemOffer extends BaseHistorianOfferWithLocation {
	
	protected int points;
	protected String data;
	
	public SpecialItemOffer(SectorEntityToken entity, int points, String id) {
		super(entity);
		this.points = points;
		this.data = id;
		
	}

	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog) {
		SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(data);
		dialog.getOptionPanel().addOption("... the possible location of an item (" + 
								spec.getName() + ")",
										this);
		SetStoryOption.set(dialog, points, this, "historianBP", Sounds.STORY_POINT_SPEND_TECHNOLOGY,
				"Learned location of " + spec.getName());				
	}
	
	public String getSortString() {
		if (getTagsForSort().contains(Tags.INTEL_FLEET_LOG) || getTagsForSort().contains(Tags.INTEL_EXPLORATION)) {
			return getSortStringNewestFirst();
		}
		return "AAA";
	}
	
	public String getName() {
		SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(data);
		if (isEnding()) {
			return spec.getName() + " - Recovered";
		} else {
			return spec.getName() + " Location";
		}
	}

	@Override
	protected void addItemToCargo(CargoAPI loot) {
		loot.addSpecial(new SpecialItemData(data, null), 1);		
	}

	public String getData() {
		return data;
	}
	
}










