package com.fs.starfarer.api.campaign;

public interface FleetStubConverterPlugin {
	
	CampaignFleetAPI convertToFleet(FleetStubAPI stub);
	FleetStubAPI convertToStub(CampaignFleetAPI fleet);
	
	boolean shouldConvertFromStub(FleetStubAPI stub);
	boolean shouldConvertToStub(CampaignFleetAPI fleet);
}
