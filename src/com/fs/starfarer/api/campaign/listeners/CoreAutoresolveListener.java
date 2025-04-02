package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.impl.campaign.BattleAutoresolverPluginImpl;
import com.fs.starfarer.api.impl.campaign.BattleAutoresolverPluginImpl.FleetAutoresolveData;

/**
 * Called from {@link BattleAutoresolverPluginImpl}, a different implementation of that plugin is not obligated 
 * to call these methods or use the same data structures. 
 * @author Alex
 *
 */
public interface CoreAutoresolveListener {
	void modifyDataForFleet(FleetAutoresolveData data);
}
