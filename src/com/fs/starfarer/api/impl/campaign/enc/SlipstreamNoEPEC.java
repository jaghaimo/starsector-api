package com.fs.starfarer.api.impl.campaign.enc;



public class SlipstreamNoEPEC extends BaseEPEncounterCreator {
	public float getFrequencyForPoint(EncounterManager manager, EncounterPoint point) {
		if (!EncounterManager.EP_TYPE_SLIPSTREAM.equals(point.type)) return 0f;
		return 10f;
	}
}





