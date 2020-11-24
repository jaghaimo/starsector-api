package com.fs.starfarer.api.plugins.impl;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI.GenericPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.plugins.BuildObjectiveTypePicker;

public class CoreBuildObjectiveTypePicker implements BuildObjectiveTypePicker, GenericPlugin {

	public String pickObjectiveToBuild(BuildObjectiveParams params) {
		CampaignFleetAPI fleet = params.fleet;
		FactionAPI faction = params.faction;
		SectorEntityToken stableLoc = params.stableLoc;
		
		boolean hasComm = false;
		boolean hasSensor = false;
		boolean hasNav = false;
		for (SectorEntityToken curr : stableLoc.getContainingLocation().getEntitiesWithTag(Tags.OBJECTIVE)) {
			// don't own this, and not hostile, so won't be retaking either - ok to build duplicate
			if (curr.getFaction() != faction && !curr.getFaction().isHostileTo(faction)) {
				continue;
			}
			hasComm |= curr.hasTag(Tags.COMM_RELAY);
			hasSensor |= curr.hasTag(Tags.SENSOR_ARRAY);
			hasNav |= curr.hasTag(Tags.NAV_BUOY);
		}
		
		if (faction.getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR)) {
			if (!hasSensor && !hasNav) {
				if ((float) Math.random() > 0.5f) {
					return Entities.NAV_BUOY_MAKESHIFT;
				}
				return Entities.SENSOR_ARRAY_MAKESHIFT;
			}
			if (!hasSensor) {
				return Entities.SENSOR_ARRAY_MAKESHIFT;
			}
			if (!hasNav) {
				return Entities.NAV_BUOY_MAKESHIFT;
			}
			if (!hasComm) {
				return Entities.COMM_RELAY_MAKESHIFT;
			}
		} else {
			if (!hasComm) {
				return Entities.COMM_RELAY_MAKESHIFT;
			}
			if (!hasNav) {
				return Entities.NAV_BUOY_MAKESHIFT;
			}
			if (!hasSensor) {
				return Entities.SENSOR_ARRAY_MAKESHIFT;
			}
		}
		return null;
	}

	
	
	public int getHandlingPriority(Object params) {
		if (params instanceof BuildObjectiveParams) {
			return 0;
		}
		return -1;
	}

}
