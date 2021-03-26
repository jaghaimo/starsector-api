package com.fs.starfarer.api.plugins;

import java.util.Random;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI.GenericPlugin;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public interface DModAdderPlugin extends GenericPlugin {
	public static class DModAdderParams {
		public ShipVariantAPI variant;
		public boolean destroyed;
		public boolean own;
		public boolean canAddDestroyedMods;
		public int num;
		public CampaignFleetAPI recoverer;
		public Random random;
	}

	void addDMods(DModAdderParams params);
}
