package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;

public class ShipBlueprintOffer extends BaseHistorianOfferWithLocation {

	protected String data;
	
	public ShipBlueprintOffer(SectorEntityToken entity, String data) {
		super(entity);
		this.data = data;
		
	}

	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog) {
		ShipHullSpecAPI spec = Global.getSettings().getHullSpec(data);
		dialog.getOptionPanel().addOption("... the possible location of a blueprint (" + 
								spec.getHullNameWithDashClass() + " " + spec.getDesignation() + ")",
										this);
		SetStoryOption.set(dialog, 1, this, "historianBP", Sounds.STORY_POINT_SPEND_TECHNOLOGY,
				"Learned location of " + spec.getHullNameWithDashClass() + " " + spec.getDesignation() + " blueprint");
	}
	
	public String getSortString() {
		return "Ship Blueprint";
	}
	
	public String getName() {
		ShipHullSpecAPI spec = Global.getSettings().getHullSpec(data);
		if (isEnding()) {
			return spec.getHullName() + " Blueprint - Recovered";
		} else {
			return spec.getHullName() + " Blueprint Location";
		}
	}

	@Override
	protected void addItemToCargo(CargoAPI loot) {
		loot.addSpecial(new SpecialItemData(Items.SHIP_BP, data), 1);		
	}

	public String getData() {
		return data;
	}
	
}










