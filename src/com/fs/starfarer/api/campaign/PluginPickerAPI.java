package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.campaign.econ.ImmigrationPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;


public interface PluginPickerAPI {

	FleetStubConverterPlugin pickFleetStubConverter(CampaignFleetAPI fleet);
	FleetStubConverterPlugin pickFleetStubConverter(FleetStubAPI stub);
	ImmigrationPlugin pickImmigrationPlugin(MarketAPI market);
	AICoreAdminPlugin pickAICoreAdminPlugin(String commodityId);
	FleetInflater pickFleetInflater(CampaignFleetAPI fleet, Object params);

}
