package com.fs.starfarer.api;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignProgressIndicatorAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FleetStubAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.CrewCompositionAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface FactoryAPI {
	FleetMemberAPI createFleetMember(FleetMemberType type, String variantOrWingId);
	CargoAPI createCargo(boolean unlimitedStacks);
	CrewCompositionAPI createCrewComposition();

	JumpPointAPI createJumpPoint(String id, String name);
	OrbitAPI createCircularOrbit(SectorEntityToken focus, float angle, float orbitRadius, float orbitDays);
	CampaignProgressIndicatorAPI createProgressIndicator(String text, SectorEntityToken target, float durationDays);
	
	MemoryAPI createMemory();
	MarketAPI createMarket(String id, String name, int size);
	//MarketAPI createConditionMarket(String id, String name, int size);
	
	
//	/**
//	 * Convert a stub "market conditions only" market used for uninhabited planets into
//	 * a full-featured market used by the economy. Used when, for example, establishing an outpost
//	 * on a planet.
//	 * @param market
//	 * @return
//	 */
//	MarketAPI convertToRegularMarket(MarketAPI market);
	
	CampaignFleetAPI createEmptyFleet(String factionId, String name, boolean aiMode);
	
	PersonAPI createPerson();
	
	OfficerDataAPI createOfficerData(PersonAPI person);
	BattleAPI createBattle(CampaignFleetAPI one, CampaignFleetAPI two);
	CargoStackAPI createCargoStack(CargoItemType type, Object data, CargoAPI cargo);
	CommMessageAPI createMessage();
	FleetStubAPI createStub();
	OrbitAPI createCircularOrbitWithSpin(SectorEntityToken focus, float angle, float orbitRadius, float orbitDays, float spin);
	FleetMemberAPI createFleetMember(FleetMemberType type, ShipVariantAPI variant);
	CampaignFleetAIAPI createFleetAI(CampaignFleetAPI fleet);
	OrbitAPI createCircularOrbitPointingDown(SectorEntityToken focus, float angle, float orbitRadius, float orbitDays);
	//CargoStackAPI createCargoStack(CargoItemType type, Object data, Object data2, CargoAPI cargo);
}
