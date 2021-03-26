package com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialPlugin;

public class CargoManifestSpecial extends BaseSalvageSpecial {

	
	public static class CargoManifestSpecialData implements SalvageSpecialData {
		public String commodityId = null;
		public float min;
		public float max;
		public CargoManifestSpecialData(String commodityId, float min, float max) {
			this.commodityId = commodityId;
			this.min = min;
			this.max = max;
		}
		
		public SalvageSpecialPlugin createSpecialPlugin() {
			return new CargoManifestSpecial();
		}
	}
	
	private CargoManifestSpecialData data;
	
	public CargoManifestSpecial() {
	}
	

	@Override
	public void init(InteractionDialogAPI dialog, Object specialData) {
		super.init(dialog, specialData);
		
		data = (CargoManifestSpecialData) specialData;
		
		initManifest();
	}

	private void initManifest() {
		
		CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(data.commodityId);
		
		if (spec == null) {
			initNothing();
			return;
		}
		
		boolean debris = Entities.DEBRIS_FIELD_SHARED.equals(entity.getCustomEntityType());
		String name = "sent to the $shortName";
		if (debris) name = "searching through the debris";
		if (entity instanceof PlanetAPI) {
			name = "sent to the surface";
		}
		
		String text1 = "A cargo manifest found by the salvage crews " + name + " indicates " +
						"the presence of a quantity of " + spec.getName().toLowerCase() + ", " +
						"likely to be found if proper salvage operations are conducted.";
		addText(text1);
		
		CargoAPI extra = Global.getFactory().createCargo(true); 
		float quantity = data.min + (data.max - data.min) * random.nextFloat();
		quantity = Math.round(quantity);
		extra.addCommodity(data.commodityId, quantity);
		addTempExtraSalvage(extra);
		
		setDone(true);
		setShowAgain(true);
	}
	
	
}



