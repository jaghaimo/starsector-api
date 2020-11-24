package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.campaign.GenericPluginManagerAPI.GenericPlugin;

public class BaseGenericPlugin implements GenericPlugin {
	public int getHandlingPriority(Object params) {
		return 0;
	}
}
