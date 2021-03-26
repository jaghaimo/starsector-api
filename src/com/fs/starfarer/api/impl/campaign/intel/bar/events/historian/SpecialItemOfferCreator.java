package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.historian.HistorianData.HistorianOffer;

public class SpecialItemOfferCreator extends BlueprintOfferCreator {

	public static int BASE_POINTS = Global.getSettings().getInt("historianSpecialItemBase");
//	public static Map<String, Integer> POINTS_MAP = new HashMap<String, Integer>();
//	static {
//		POINTS_MAP.put(Items.PRISTINE_NANOFORGE, 5);
//		POINTS_MAP.put(Items.SYNCHROTRON, 5);
//	}
	
	
	protected int numAccepted = 0;
	
	public SpecialItemOfferCreator(float frequency) {
		super(frequency);
	}
	
	public void notifyAccepted(HistorianOffer offer) {
		numAccepted++;
	}

	@Override
	protected BaseHistorianOfferWithLocation createOffer(SectorEntityToken entity, String data) {
//		Integer points = POINTS_MAP.get(data);
//		if (points == null) points = DEFAULT_POINTS;
		int points = (int)Math.round(BASE_POINTS * Math.pow(2, numAccepted));
		return new SpecialItemOffer(entity, points, data);
	}

	@Override
	protected List<Object> getAllSpecs() {
		return new ArrayList<Object>(Global.getSettings().getAllSpecialItemSpecs());
	}

	@Override
	protected String getAlreadyUsedIdFromOffer(HistorianOffer offer) {
		if (offer instanceof SpecialItemOffer) {
			SpecialItemOffer sio = (SpecialItemOffer) offer;
			return sio.getData();
		}
		return null;
	}

	@Override
	protected String getIdForSpec(Object spec) {
		return ((SpecialItemSpecAPI) spec).getId();
	}

	@Override
	protected Set<String> getTagsForSpec(Object spec) {
		return ((SpecialItemSpecAPI) spec).getTags();
	}
	
	@Override
	protected float getRarityForSpec(Object spec) {
		return ((SpecialItemSpecAPI) spec).getRarity();
	}

	@Override
	protected boolean playerKnowsSpecAlready(String id) {
		return false;
	}

	protected float getProbabilityRuins() {
		return 0.67f;
	}
	
	public static String PREFIX = "spec_";
	@Override
	public String getOfferId(BaseHistorianOffer offer) {
		if (offer instanceof SpecialItemOffer) {
			return PREFIX + ((SpecialItemOffer)offer).getData();
		}
		return null;
	}
	
	@Override
	public String getBlueprintIdFromOfferId(String offerId) {
		if (offerId.startsWith(PREFIX)) {
			return offerId.replaceAll(PREFIX, "");
		}
		return null;
	}

}







