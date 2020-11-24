package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetStubAPI;
import com.fs.starfarer.api.campaign.FleetStubConverterPlugin;

public class FleetStubConverterPluginImpl implements FleetStubConverterPlugin {

	public CampaignFleetAPI convertToFleet(FleetStubAPI stub) {
//		if (stub == null || !(stub.getParams() instanceof FleetParams)) {
//			throw new RuntimeException("Trying to convert invalid fleet stub with FleetStubConverterPluginImpl");
//		}
//		
//		FleetParams params = (FleetParams) stub.getParams();
//		CampaignFleetAPI fleet = FleetFactoryV2.createFleet(params);
//		
//		MemoryAPI memory = fleet.getMemoryWithoutUpdate();
//		fleet.setMemory(stub.getMemoryWithoutUpdate());
//		
//		String sourceMarket = memory.getString(MemFlags.MEMORY_KEY_SOURCE_MARKET);
//		if (sourceMarket != null) {
//			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SOURCE_MARKET, sourceMarket);
//		}
//		
//		fleet.setId(stub.getId());
//		
//		
//		for (FleetAssignmentDataAPI curr : stub.getAssignmentsCopy()) {
//			fleet.addAssignment(curr.getAssignment(), curr.getTarget(), curr.getMaxDurationInDays(),
//								curr.getActionText(), curr.getOnCompletion());
//		}
//		
//		if (stub.getContainingLocation() != null) {
//			stub.getContainingLocation().addEntity(fleet);
//			stub.getContainingLocation().removeFleetStub(stub);
//		}
//		
//		fleet.setLocation(stub.getLocation().x, stub.getLocation().y);
//		
//		
//		if (stub.getScripts() != null) {
//			for (EveryFrameScript script : stub.getScripts()) {
//				fleet.addScript(script);
//			}
//			//stub.getScripts().clear();
//		}
//		
//		if (stub.getEventListeners() != null) {
//			for (FleetEventListener listener : stub.getEventListeners()) {
//				fleet.addEventListener(listener);
//			}
//			stub.getEventListeners().clear();
//		}
//		
//		stub.setFleet(fleet);
//		stub.setMemory(null);
//		stub.clearAssignments();
//		
//		fleet.setStub(stub);
//		
//		return fleet;
		return null;
	}

	
	public FleetStubAPI convertToStub(CampaignFleetAPI fleet) {
//		MemoryAPI memory = fleet.getMemoryWithoutUpdate();
//		memory.advance(10000f); // flush out anything temporary
//		
//		FleetStubAPI stub = fleet.getStub();
//		stub.setMemory(memory);
//		stub.setFleet(null);
//		
//		stub.clearAssignments();
//		for (FleetAssignmentDataAPI curr : fleet.getAssignmentsCopy()) {
//			stub.addAssignment(curr.getAssignment(), curr.getTarget(), curr.getMaxDurationInDays(),
//								curr.getActionText(), curr.getOnCompletion());
//		}
//		
////		if (fleet.getScripts() != null) {
////			if (stub.getScripts() != null) {
////				stub.getScripts().clear();
////			}
////			for (EveryFrameScript script : fleet.getScripts()) {
////				stub.addScript(script);
////			}
////		}
//		
//		if (fleet.getEventListeners() != null) {
//			if (stub.getEventListeners() != null) {
//				stub.getEventListeners().clear();
//			}
//			for (FleetEventListener listener : fleet.getEventListeners()) {
//				stub.addEventListener(listener);
//			}
//		}
//		
//		if (fleet.getContainingLocation() != null) {
//			fleet.getContainingLocation().removeEntity(fleet);
//			fleet.getContainingLocation().addFleetStub(stub);
//			stub.setContainingLocation(fleet.getContainingLocation());
//		}
//		
//		stub.getLocation().set(fleet.getLocation().x, fleet.getLocation().y);
		
		
		return null;
	}

	
	public boolean shouldConvertFromStub(FleetStubAPI stub) {
		return true;
		//return Misc.shouldConvertFromStub(stub);
	}
	
	public boolean shouldConvertToStub(CampaignFleetAPI fleet) {
		return false;
		//return Misc.shouldConvertToStub(fleet);
	}
}











