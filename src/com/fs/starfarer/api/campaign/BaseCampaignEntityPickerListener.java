package com.fs.starfarer.api.campaign;

import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ArrowData;
import com.fs.starfarer.api.ui.MarkerData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class BaseCampaignEntityPickerListener implements CampaignEntityPickerListener {

	public String getMenuItemNameOverrideFor(SectorEntityToken entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public void pickedEntity(SectorEntityToken entity) {
		// TODO Auto-generated method stub
		
	}

	public void cancelledEntityPicking() {
		// TODO Auto-generated method stub
		
	}

	public String getSelectedTextOverrideFor(SectorEntityToken entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public void createInfoText(TooltipMakerAPI info, SectorEntityToken entity) {
		// TODO Auto-generated method stub
		
	}

	public boolean canConfirmSelection(SectorEntityToken entity) {
		// TODO Auto-generated method stub
		return false;
	}

	public float getFuelColorAlphaMult() {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getFuelRangeMult() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<ArrowData> getArrows() {
//		List<ArrowData> test = new ArrayList<ArrowData>();
//		ArrowData d = new ArrowData(20f, Global.getSector().getPlayerFleet(), 
//				Global.getSector().getStarSystems().get(10).getHyperspaceAnchor(), Color.ORANGE); 
//		test.add(d);
//		return test;
		return null;
	}

	public List<MarkerData> getMarkers() {
		//ArrayList<MarkerData> test = new ArrayList<MarkerData>();
//		MarkerData d = new MarkerData(new Vector2f(0, 0), Global.getSector().getHyperspace(), Color.cyan, 1f);
//		test.add(d);
		
		// standard player location marker
		//test.add(new MarkerData(Global.getSector().getPlayerFleet().getLocationInHyperspace(), null, null));
		//return test;
		return null;
	}

	public Set<StarSystemAPI> getStarSystemsToShow() {
		
		//return new LinkedHashSet<StarSystemAPI>(Global.getSector().getStarSystems());
		return null;
	}
	
}
