package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.historian.HistorianData.HistorianOffer;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public abstract class BlueprintOfferCreator extends BaseHistorianOfferCreator {

	public BlueprintOfferCreator(float frequency) {
		setFrequency(frequency);
	}
	
	public String getOfferId(BaseHistorianOffer offer) {
		return null;
	}
	
	public String getBlueprintIdFromOfferId(String offerId) {
		return null;
	}
	
	@Override
	public HistorianOffer createOffer(Random random, List<HistorianOffer> soFar) {
		HistorianData hd = HistorianData.getInstance();
		
		SectorEntityToken entity = pickEntity(random, false);
		if (entity == null || random.nextFloat() < getProbabilityRuins()) {
			PlanetAPI planet = pickUnexploredRuins(random);
			if (planet != null) entity = planet;
		}
		if (entity == null) {
			entity = createEntity(random);
		}
		
		if (entity == null) return null;
		
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);

		Set<String> already = new HashSet<String>();
		for (HistorianOffer offer : soFar) {
			String id = getAlreadyUsedIdFromOffer(offer);
			if (id != null) already.add(id);
		}
		
		for (String id : hd.getGivenOffers()) {
			String bpId = getBlueprintIdFromOfferId(id);
			if (bpId != null) {
				already.add(bpId);
			}
		}
		
		for (Object spec : getAllSpecs()) {
			String id = getIdForSpec(spec);
			if (already.contains(id)) continue;
			if (playerKnowsSpecAlready(id)) continue;
			
			float w = hd.getWeightForTags(getTagsForSpec(spec));
			if (w <= 0) continue;
			
			w *= getRarityForSpec(spec);
			picker.add(id, w);
		}
		
		String data = picker.pick();
		if (data == null) return null;
		
		HistorianOffer offer = createOffer(entity, data); 
		return offer;
	}
	
	protected abstract BaseHistorianOfferWithLocation createOffer(SectorEntityToken entity, String data);
	
	protected abstract String getAlreadyUsedIdFromOffer(HistorianOffer offer);
	protected abstract List<Object> getAllSpecs();
	protected abstract Set<String> getTagsForSpec(Object spec);
	protected abstract String getIdForSpec(Object spec);
	protected abstract boolean playerKnowsSpecAlready(String id);
	
	protected float getRarityForSpec(Object spec) {
		return 1f;
	}
	
	protected float getProbabilityRuins() {
		return 0.33f;
	}

}







