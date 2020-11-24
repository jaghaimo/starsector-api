package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.campaign.comm.CommMessageAPI;

public interface OnMessageDeliveryScript {
	/**
	 * Called before shouldDeliver(), so can be used to determine whether the message
	 * should be delivered.
	 */
	void beforeDelivery(CommMessageAPI message);
	boolean shouldDeliver();
}
