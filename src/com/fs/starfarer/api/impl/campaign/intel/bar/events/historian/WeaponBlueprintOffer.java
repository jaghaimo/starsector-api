package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

public class WeaponBlueprintOffer extends BaseHistorianOfferWithLocation {

	protected String data;
	
	public WeaponBlueprintOffer(SectorEntityToken entity, String data) {
		super(entity);
		this.data = data;
		
	}

	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog) {
		WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(data);
		dialog.getOptionPanel().addOption("... the possible location of a blueprint (" + 
								spec.getWeaponName() + ")",
										this);
		SetStoryOption.set(dialog, 1, this, "historianBP", Sounds.STORY_POINT_SPEND_TECHNOLOGY,
				"Learned location of " + spec.getWeaponName() + " blueprint");
	}
	
	public String getSortString() {
		return "Weapon Blueprint";
	}
	
	public String getName() {
		WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(data);
		if (isEnding()) {
			return spec.getWeaponName() + " Blueprint - Recovered";
		} else {
			return spec.getWeaponName() + " Blueprint Location";
		}
	}

	@Override
	protected void addItemToCargo(CargoAPI loot) {
		loot.addSpecial(new SpecialItemData(Items.WEAPON_BP, data), 1);		
	}

	public String getData() {
		return data;
	}
	
}










