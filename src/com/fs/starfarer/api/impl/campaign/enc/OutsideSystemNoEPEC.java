package com.fs.starfarer.api.impl.campaign.enc;



public class OutsideSystemNoEPEC extends BaseEPEncounterCreator {
	public float getFrequencyForPoint(EncounterManager manager, EncounterPoint point) {
		if (!EncounterManager.EP_TYPE_OUTSIDE_SYSTEM.equals(point.type)) return 0f;
		return 10f;
	}
}





