package com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTTopographicDataFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HyperspaceTopographyEventIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialPlugin;

public class TopographicDataSpecial extends BaseSalvageSpecial {

	public static class TopographicDataSpecialData implements SalvageSpecialData {
		public int points;
		public TopographicDataSpecialData(int points) {
			this.points = points;
		}
		public SalvageSpecialPlugin createSpecialPlugin() {
			return new TopographicDataSpecial();
		}
	}
	
	private TopographicDataSpecialData data;
	
	public TopographicDataSpecial() {
	}

	@Override
	public void init(InteractionDialogAPI dialog, Object specialData) {
		super.init(dialog, specialData);
		
		data = (TopographicDataSpecialData) specialData;
	
		//boolean debris = Entities.DEBRIS_FIELD_SHARED.equals(entity.getCustomEntityType());
		
		String text1 = getString("Your salvage crews find a partially-accessible data core that contains "
				+ "hyperspace topography data. The data is well out of date, but that makes it particularly "
				+ "valuable in extrapolating current trends.");
		addText(text1);
		
		HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(new HTTopographicDataFactor(data.points), dialog);
		
		setDone(true);
	}

	
	
}
