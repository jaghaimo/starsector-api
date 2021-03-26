package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.historian.HistorianData.HistorianOffer;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;

public class FighterBlueprintOfferCreator extends BlueprintOfferCreator {

	public FighterBlueprintOfferCreator(float frequency) {
		super(frequency);
	}

	@Override
	protected BaseHistorianOfferWithLocation createOffer(SectorEntityToken entity, String data) {
		return new FighterBlueprintOffer(entity, data);
	}

	@Override
	protected List<Object> getAllSpecs() {
		return new ArrayList<Object>(Global.getSettings().getAllFighterWingSpecs());
	}

	@Override
	protected String getAlreadyUsedIdFromOffer(HistorianOffer offer) {
		if (offer instanceof FighterBlueprintOffer) {
			FighterBlueprintOffer fbo = (FighterBlueprintOffer) offer;
			return fbo.getData();
		}
		return null;
	}

	@Override
	protected String getIdForSpec(Object spec) {
		return ((FighterWingSpecAPI) spec).getId();
	}

	@Override
	protected Set<String> getTagsForSpec(Object spec) {
		return ((FighterWingSpecAPI) spec).getTags();
	}
	
	@Override
	protected float getRarityForSpec(Object spec) {
		return ((FighterWingSpecAPI) spec).getRarity();
	}

	@Override
	protected boolean playerKnowsSpecAlready(String id) {
		return Global.getSector().getPlayerFaction().knowsFighter(id);
	}

	protected float getProbabilityRuins() {
		return super.getProbabilityRuins();
	}

	
	public static String PREFIX = "ftr_";
	@Override
	public String getOfferId(BaseHistorianOffer offer) {
		if (offer instanceof FighterBlueprintOffer) {
			return PREFIX + ((FighterBlueprintOffer)offer).getData();
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







