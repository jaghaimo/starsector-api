package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.historian.HistorianData.HistorianOffer;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

public class WeaponBlueprintOfferCreator extends BlueprintOfferCreator {

	public WeaponBlueprintOfferCreator(float frequency) {
		super(frequency);
	}

	@Override
	protected BaseHistorianOfferWithLocation createOffer(SectorEntityToken entity, String data) {
		return new WeaponBlueprintOffer(entity, data);
	}

	@Override
	protected List<Object> getAllSpecs() {
		return new ArrayList<Object>(Global.getSettings().getAllWeaponSpecs());
	}

	@Override
	protected String getAlreadyUsedIdFromOffer(HistorianOffer offer) {
		if (offer instanceof WeaponBlueprintOffer) {
			WeaponBlueprintOffer wbo = (WeaponBlueprintOffer) offer;
			return wbo.getData();
		}
		return null;
	}

	@Override
	protected String getIdForSpec(Object spec) {
		return ((WeaponSpecAPI) spec).getWeaponId();
	}

	@Override
	protected Set<String> getTagsForSpec(Object spec) {
		return ((WeaponSpecAPI) spec).getTags();
	}
	
	@Override
	protected float getRarityForSpec(Object spec) {
		return ((WeaponSpecAPI) spec).getRarity();
	}

	@Override
	protected boolean playerKnowsSpecAlready(String id) {
		return Global.getSector().getPlayerFaction().knowsWeapon(id);
	}

	protected float getProbabilityRuins() {
		return super.getProbabilityRuins();
	}

	
	public static String PREFIX = "wpn_";
	@Override
	public String getOfferId(BaseHistorianOffer offer) {
		if (offer instanceof WeaponBlueprintOffer) {
			return PREFIX + ((WeaponBlueprintOffer)offer).getData();
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







