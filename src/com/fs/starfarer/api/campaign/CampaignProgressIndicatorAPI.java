package com.fs.starfarer.api.campaign;

import java.awt.Color;

public interface CampaignProgressIndicatorAPI extends SectorEntityToken {
	void setProgress(float progress);
	float getProgress();
	void setBarColor(Color color);
	float getDurationDays();
	SectorEntityToken getTarget();
	void setTarget(SectorEntityToken target);
	
	
	
	 /**
	 * Turns red and fades out. Will be removed from the containing location when finished fading out.
	 */
	void interrupt();
	
	/**
	 * Fades out. Will be removed from the containing location when finished fading out.
	 */
	void finish();
}
