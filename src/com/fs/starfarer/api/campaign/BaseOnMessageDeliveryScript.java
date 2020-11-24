package com.fs.starfarer.api.campaign;

public abstract class BaseOnMessageDeliveryScript implements OnMessageDeliveryScript {

	public boolean shouldDeliver() {
		return true;
	}

}
