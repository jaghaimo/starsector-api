package com.fs.starfarer.api.impl.campaign.enc;

public class AbyssalNoEPEC extends BaseEPEncounterCreator {
	public float getFrequencyForPoint(EncounterManager manager, EncounterPoint point) {
//		if (!HyperspaceAbyssPluginImpl.EP_TYPE_ABYSSAL.equals(point.type)) return 0f;
//		return 10f;
		return AbyssalFrequencies.getNoAbyssalEncounterFrequency(manager, point);
	}
	
}







