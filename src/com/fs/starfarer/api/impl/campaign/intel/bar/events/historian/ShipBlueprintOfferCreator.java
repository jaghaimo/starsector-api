package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.historian.HistorianData.HistorianOffer;

public class ShipBlueprintOfferCreator extends BlueprintOfferCreator {

	public ShipBlueprintOfferCreator(float frequency) {
		super(frequency);
	}

	@Override
	protected BaseHistorianOfferWithLocation createOffer(SectorEntityToken entity, String data) {
		return new ShipBlueprintOffer(entity, data);
	}

	@Override
	protected List<Object> getAllSpecs() {
		return new ArrayList<Object>(Global.getSettings().getAllShipHullSpecs());
	}

	@Override
	protected String getAlreadyUsedIdFromOffer(HistorianOffer offer) {
		if (offer instanceof ShipBlueprintOffer) {
			ShipBlueprintOffer sbo = (ShipBlueprintOffer) offer;
			return sbo.getData();
		}
		return null;
	}

	@Override
	protected String getIdForSpec(Object spec) {
		return ((ShipHullSpecAPI) spec).getHullId();
	}

	@Override
	protected Set<String> getTagsForSpec(Object spec) {
		return ((ShipHullSpecAPI) spec).getTags();
	}
	
	@Override
	protected float getRarityForSpec(Object spec) {
		return ((ShipHullSpecAPI) spec).getRarity();
	}

	@Override
	protected boolean playerKnowsSpecAlready(String id) {
		return Global.getSector().getPlayerFaction().knowsShip(id);
	}

	protected float getProbabilityRuins() {
		return super.getProbabilityRuins();
	}
	
	public static String PREFIX = "ship_";
	@Override
	public String getOfferId(BaseHistorianOffer offer) {
		if (offer instanceof ShipBlueprintOffer) {
			return PREFIX + ((ShipBlueprintOffer)offer).getData();
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







