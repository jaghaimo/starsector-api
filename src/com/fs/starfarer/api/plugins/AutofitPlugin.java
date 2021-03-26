package com.fs.starfarer.api.plugins;

import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;




public interface AutofitPlugin {
	public interface AvailableWeapon {
		String getId();
		WeaponSpecAPI getSpec();
		float getPrice();
		CargoAPI getSource();
		SubmarketAPI getSubmarket();
		int getQuantity();
		void setQuantity(int quantity);
		float getOPCost(MutableCharacterStatsAPI stats, MutableShipStatsAPI shipStats);
	}
	
	public interface AvailableFighter {
		String getId();
		FighterWingSpecAPI getWingSpec();
		float getPrice();
		CargoAPI getSource();
		SubmarketAPI getSubmarket();
		int getQuantity();
		void setQuantity(int quantity);
	}
	
	public interface AutofitPluginDelegate {
		void fitFighterInSlot(int index, AvailableFighter fighter, ShipVariantAPI variant);
		void clearFighterSlot(int index, ShipVariantAPI variant);
		void fitWeaponInSlot(WeaponSlotAPI slot, AvailableWeapon weapon, ShipVariantAPI variant);
		void clearWeaponSlot(WeaponSlotAPI slot, ShipVariantAPI variant);
		
		List<AvailableWeapon> getAvailableWeapons();
		List<AvailableFighter> getAvailableFighters();
		
		boolean isPriority(WeaponSpecAPI weapon);
		boolean isPriority(FighterWingSpecAPI wing);
		
		List<String> getAvailableHullmods();
		void syncUIWithVariant(ShipVariantAPI variant);
		
		//void syncUIWithVariant();
		
		ShipAPI getShip();
		
		FactionAPI getFaction();
		
		boolean isPlayerCampaignRefit();
		boolean canAddRemoveHullmodInPlayerCampaignRefit(String modId);
	}
	

	
	
	public static class AutofitOption {
		public String id;
		public String text;
		public boolean checked;
		public String tooltip;
		public AutofitOption(String id, String text, boolean checked, String tooltip) {
			this.id = id;
			this.text = text;
			this.checked = checked;
			this.tooltip = tooltip;
		}
	}
	
	
	//void init(AutofitPluginDelegate delegate);
	List<AutofitOption> getOptions();
	void doFit(ShipVariantAPI current, ShipVariantAPI target, int maxSMods, AutofitPluginDelegate delegate);
	float getRating(ShipVariantAPI current, ShipVariantAPI target, AutofitPluginDelegate delegate);
	int getCreditCost();
	

	String getQuickActionText();
	void doQuickAction(ShipVariantAPI current, AutofitPluginDelegate delegate);
	String getQuickActionTooltip();
	
	
	void autoAssignOfficers(CampaignFleetAPI fleet);
	boolean isQuickActionEnabled(ShipVariantAPI currentVariant);
	void setRandom(Random random);
}

















