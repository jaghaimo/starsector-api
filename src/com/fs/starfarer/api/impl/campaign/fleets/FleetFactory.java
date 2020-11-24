package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.loading.AbilitySpecAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class FleetFactory {

	public static Logger log = Global.getLogger(FleetFactory.class);
	
	public static enum PatrolType {
		FAST(FleetTypes.PATROL_SMALL),
		COMBAT(FleetTypes.PATROL_MEDIUM),
		HEAVY(FleetTypes.PATROL_LARGE);
		
		private String fleetType;
		private PatrolType(String fleetType) {
			this.fleetType = fleetType;
		}
		public String getFleetType() {
			return fleetType;
		}
	}
	
	public static CampaignFleetAPI createPatrol(MarketAPI market, PatrolType type) {
		float stability = market.getStabilityValue();
		String factionId = market.getFactionId();
		FactionAPI faction = Global.getSector().getFaction(factionId);
		float qf = market.getShipQualityFactor();
		return createPatrol(type, faction, stability, qf, market); 
		
	}
	
	public static CampaignFleetAPI createPatrol(PatrolType type, FactionAPI faction, float stability, float qf, MarketAPI market) {
		String fleetType = FleetTypes.PATROL_SMALL;
		switch (type) {
		case FAST:
			fleetType = FleetTypes.PATROL_SMALL;
			break;
		case COMBAT:
			fleetType = FleetTypes.PATROL_MEDIUM;
			break;
		case HEAVY:
			fleetType = FleetTypes.PATROL_LARGE;
			break;
		}
		CampaignFleetAPI fleet = createEmptyFleet(faction.getId(), fleetType, market);

		switch (type) {
		case FAST:
			faction.pickShipAndAddToFleet(ShipRoles.COMBAT_SMALL, ShipPickParams.all(), fleet);
			faction.pickShipAndAddToFleet(ShipRoles.FAST_ATTACK, ShipPickParams.all(), fleet);
			faction.pickShipAndAddToFleet(ShipRoles.FAST_ATTACK, ShipPickParams.all(), fleet);
			if (stability > (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_SMALL, ShipPickParams.all(), fleet);
			}
			if (stability > (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.FAST_ATTACK, ShipPickParams.all(), fleet);
			}
			break;
		case COMBAT:
			faction.pickShipAndAddToFleet(ShipRoles.COMBAT_SMALL, ShipPickParams.all(), fleet);
			faction.pickShipAndAddToFleet(ShipRoles.FAST_ATTACK, ShipPickParams.all(), fleet);
			
			faction.pickShipAndAddToFleet(ShipRoles.COMBAT_SMALL, ShipPickParams.all(), fleet);
			faction.pickShipAndAddToFleet(ShipRoles.COMBAT_MEDIUM, ShipPickParams.all(), fleet);
			faction.pickShipAndAddToFleet(ShipRoles.COMBAT_MEDIUM, ShipPickParams.all(), fleet);
			
			if (stability > (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.FREIGHTER_SMALL, ShipPickParams.all(), fleet);
			}
			if (stability > (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.TANKER_SMALL, ShipPickParams.all(), fleet);
			}
			if (stability > (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_SMALL, ShipPickParams.all(), fleet);
			}
			if (stability > (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_SMALL, ShipPickParams.all(), fleet);
			}
			if (stability > (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_MEDIUM, ShipPickParams.all(), fleet);
			}
			break;
		case HEAVY:
			faction.pickShipAndAddToFleet(ShipRoles.FAST_ATTACK, ShipPickParams.all(), fleet);
			
			if ((float) Math.random() > 0.5f) {
				faction.pickShipAndAddToFleet(ShipRoles.CARRIER_MEDIUM, ShipPickParams.all(), fleet);
			} else {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_SMALL, ShipPickParams.all(), fleet);
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_LARGE, ShipPickParams.all(), fleet);
			}
			
			faction.pickShipAndAddToFleet(ShipRoles.COMBAT_SMALL, ShipPickParams.all(), fleet);
			faction.pickShipAndAddToFleet(ShipRoles.COMBAT_MEDIUM, ShipPickParams.all(), fleet);
			faction.pickShipAndAddToFleet(ShipRoles.COMBAT_LARGE, ShipPickParams.all(), fleet);

			faction.pickShipAndAddToFleet(ShipRoles.FREIGHTER_MEDIUM, ShipPickParams.all(), fleet);
			faction.pickShipAndAddToFleet(ShipRoles.TANKER_MEDIUM, ShipPickParams.all(), fleet);
			
			if (stability > (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_SMALL, ShipPickParams.all(), fleet);
			}
			if (stability > (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_MEDIUM, ShipPickParams.all(), fleet);
			}
			if (stability > (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_LARGE, ShipPickParams.all(), fleet);
			}
			break;
		}
	
		setAIMode(fleet, true);
		
		return fleet;
	}
	
	
	
	
	public static enum MercType {
		SCOUT(FleetTypes.MERC_SCOUT),
		BOUNTY_HUNTER(FleetTypes.MERC_BOUNTY_HUNTER),
		PRIVATEER(FleetTypes.MERC_PRIVATEER),
		PATROL(FleetTypes.MERC_PATROL),
		ARMADA(FleetTypes.MERC_ARMADA);
		
		public String fleetType;
		private MercType(String fleetType) {
			this.fleetType = fleetType;
		}
		
	}
	
	public static CampaignFleetAPI createMerc(MarketAPI market, float qf, MercType type, String factionIdForShipPicking) {
		float stability = market.getStabilityValue();
		
		String factionId = Factions.INDEPENDENT;
		FactionAPI faction = Global.getSector().getFaction(factionIdForShipPicking);
//		FactionAPI factionForInitialName = Global.getSector().getFaction(factionId);
//		//float qf = market.getShipQualityFactor();
//		String fleetName = factionForInitialName.getFleetTypeName(type.fleetType); 
//		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(factionId, fleetName, true);
		//CampaignFleetAPI fleet = createEmptyFleet(factionId, type.fleetType);
		CampaignFleetAPI fleet = createEmptyFleet(factionIdForShipPicking, type.fleetType, market);
		switch (type) {
		case SCOUT:
			faction.pickShipAndAddToFleet(ShipRoles.FAST_ATTACK, ShipPickParams.all(), fleet);
			if ((float) Math.random() > 0.5f) {
				faction.pickShipAndAddToFleet(ShipRoles.FAST_ATTACK, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.FAST_ATTACK, ShipPickParams.all(), fleet);
			}
			break;
		case PRIVATEER:
			faction.pickShipAndAddToFleet(ShipRoles.COMBAT_SMALL, ShipPickParams.all(), fleet);
			faction.pickShipAndAddToFleet(ShipRoles.COMBAT_MEDIUM, ShipPickParams.all(), fleet);
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.FAST_ATTACK, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_MEDIUM, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				if ((float) Math.random() > 0.5f) {
					faction.pickShipAndAddToFleet(ShipRoles.COMBAT_FREIGHTER_MEDIUM, ShipPickParams.all(), fleet);
				} else {
					faction.pickShipAndAddToFleet(ShipRoles.FREIGHTER_MEDIUM, ShipPickParams.all(), fleet);
				}
			}
			break;
		case BOUNTY_HUNTER:
			faction.pickShipAndAddToFleet(ShipRoles.FAST_ATTACK, ShipPickParams.all(), fleet);
			faction.pickShipAndAddToFleet(ShipRoles.FAST_ATTACK, ShipPickParams.all(), fleet);
			if ((float) Math.random() > 0.5f) {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_SMALL, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.FAST_ATTACK, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.FAST_ATTACK, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.TANKER_SMALL, ShipPickParams.all(), fleet);
			}
			break;
		case PATROL:
			if ((float) Math.random() > 0.33f) {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_SMALL, ShipPickParams.all(), fleet);
				faction.pickShipAndAddToFleet(ShipRoles.ESCORT_SMALL, ShipPickParams.all(), fleet);
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_MEDIUM, ShipPickParams.all(), fleet);
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_MEDIUM, ShipPickParams.all(), fleet);
			} else {
				faction.pickShipAndAddToFleet(ShipRoles.CARRIER_SMALL, ShipPickParams.all(), fleet);
				if (stability < (float) Math.random() * 20f) {
					faction.pickShipAndAddToFleet(ShipRoles.CARRIER_SMALL, ShipPickParams.all(), fleet);
				}
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.ESCORT_SMALL, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_MEDIUM, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.ESCORT_SMALL, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_MEDIUM, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.FREIGHTER_MEDIUM, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.TANKER_SMALL, ShipPickParams.all(), fleet);
			}
			break;
		case ARMADA:
			if ((float) Math.random() > 0.33f) {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_SMALL, ShipPickParams.all(), fleet);
				faction.pickShipAndAddToFleet(ShipRoles.ESCORT_SMALL, ShipPickParams.all(), fleet);
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_MEDIUM, ShipPickParams.all(), fleet);
				faction.pickShipAndAddToFleet(ShipRoles.ESCORT_MEDIUM, ShipPickParams.all(), fleet);
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_LARGE, ShipPickParams.all(), fleet);
			} else {
				faction.pickShipAndAddToFleet(ShipRoles.CARRIER_MEDIUM, ShipPickParams.all(), fleet);
				if (stability < (float) Math.random() * 20f) {
					faction.pickShipAndAddToFleet(ShipRoles.CARRIER_SMALL, ShipPickParams.all(), fleet);
				}
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.ESCORT_MEDIUM, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_LARGE, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.ESCORT_SMALL, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.COMBAT_MEDIUM, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.FREIGHTER_MEDIUM, ShipPickParams.all(), fleet);
			}
			if (stability < (float) Math.random() * 20f) {
				faction.pickShipAndAddToFleet(ShipRoles.TANKER_MEDIUM, ShipPickParams.all(), fleet);
			}
			break;
		}
	
		setAIMode(fleet, true);
		return fleet;
	}
	
	private static List<String> startingAbilities = null;
	public static CampaignFleetAPI createEmptyFleet(String factionId, String fleetType, MarketAPI market) {
		FactionAPI faction = Global.getSector().getFaction(factionId);
		String fleetName = null;
		if (fleetType != null) fleetName = faction.getFleetTypeName(fleetType);
		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(factionId, fleetName, true);
		if (fleetType != null) {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_TYPE, fleetType);
		}
		
		if (market != null) {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SOURCE_MARKET, market.getId());
		}
		
		if (startingAbilities == null) {
			startingAbilities = new ArrayList<String>();
			for (String id : Global.getSettings().getSortedAbilityIds()) {
				AbilitySpecAPI spec = Global.getSettings().getAbilitySpec(id);
				if (spec.isAIDefault()) {
					startingAbilities.add(id);
				}
			}
		}
		
		for (String id : startingAbilities) {
			fleet.addAbility(id);
		}
		
		return fleet;
	}
	
	
//	public static final float SUPPLIES_FRACTION = 0.15f;
//	public static final float FUEL_FRACTION = 0.5f;
	public static final float SUPPLIES_FRACTION = 0;
	public static final float FUEL_FRACTION = 0;
	public static void finishAndSync(CampaignFleetAPI fleet) {
		setAIMode(fleet, false);
	}
	private static void setAIMode(CampaignFleetAPI fleet, boolean addSuppliesAndFuel) {
//		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
//			//member.setCrewXPLevel(CrewXPLevel.REGULAR);
//			member.setCrewXPLevel(member.getCrewXPLevel());
//		}
//		fleet.setAIMode(true);
//		if (addSuppliesAndFuel) {
//			float maxCargo = fleet.getCargo().getMaxCapacity();
//			float maxFuel = fleet.getCargo().getMaxFuel();
//			fleet.getCargo().clear();
//			fleet.getCargo().addItems(CargoItemType.RESOURCES, Commodities.SUPPLIES, maxCargo * SUPPLIES_FRACTION);
//			fleet.getCargo().addItems(CargoItemType.RESOURCES, Commodities.FUEL, maxFuel * FUEL_FRACTION);
//		}
		fleet.forceSync();
	}
	
	
	public static CampaignFleetAPI createGenericFleet(String factionId, String name, float qualityFactor, int maxFP) {
		FactionAPI faction = Global.getSector().getFaction(factionId);
		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(factionId, name, true);
		
		WeightedRandomPicker<String> main = new WeightedRandomPicker<String>();
		
		main.add(ShipRoles.COMBAT_SMALL, 5);
		main.add(ShipRoles.COMBAT_SMALL, 5);
		
		main.add(ShipRoles.ESCORT_SMALL, 6);
		main.add(ShipRoles.COMBAT_SMALL, 20);
		main.add(ShipRoles.FAST_ATTACK, 10);

		if (maxFP >= 20) {
			main.add(ShipRoles.COMBAT_MEDIUM, 10);
			main.add(ShipRoles.ESCORT_MEDIUM, 10);
		}
		if (maxFP >= 40) {
			main.add(ShipRoles.COMBAT_LARGE, 5);
			main.add(ShipRoles.COMBAT_SMALL, 5);
		}
		if (maxFP >= 60) {
			main.add(ShipRoles.COMBAT_LARGE, 5);
			main.add(ShipRoles.COMBAT_CAPITAL, 3);
		}
		
		// add ships and/or fighters, leave some headroom for carriers
		int failCount = 0;
		while (fleet.getFleetPoints() < maxFP * 0.75f) {
			String role = main.pick();
			boolean added = faction.pickShipAndAddToFleet(role, ShipPickParams.all(), fleet) > 0;
			if (added) {
				failCount = 0;
			} else {
				failCount++;
				if (failCount >= 10) break;
			}
		}
		
		// add a flagship if all we'd added before this were fighters
		if (fleet.getFlagship() == null) {
			faction.pickShipAndAddToFleet(ShipRoles.COMBAT_SMALL, ShipPickParams.all(), fleet);
			if (fleet.getFlagship() == null) {
				FactoryAPI f = Global.getFactory();
				FleetMemberAPI flagship = f.createFleetMember(FleetMemberType.SHIP, "tempest_Attack");
				fleet.getFleetData().addFleetMember(flagship);
			}
		}
		
		int fighterFP = 0;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.isFighterWing()) {
				fighterFP += member.getFleetPointCost();
			}
		}
		
		float pointsLeft = maxFP - fleet.getFleetPoints();
		float carrierPoints = Math.min(pointsLeft, fighterFP * 0.33f);
		
		WeightedRandomPicker<String> carriers = new WeightedRandomPicker<String>();
		if (fighterFP >= 10) {
			carriers.add(ShipRoles.CARRIER_SMALL, 5);
		} 
		if (fighterFP >= 30) {
			carriers.add(ShipRoles.CARRIER_MEDIUM, 5);
		} 
		if (fighterFP >= 50) {
			carriers.add(ShipRoles.CARRIER_LARGE, 5);
		}
		
		// add carriers, if needed
		failCount = 0;
		int startingFP = fleet.getFleetPoints();
		while (fleet.getFleetPoints() < startingFP + carrierPoints) {
			String role = carriers.pick();
			boolean added = faction.pickShipAndAddToFleet(role, ShipPickParams.all(), fleet) > 0;
			if (added) {
				failCount = 0;
			} else {
				failCount++;
				if (failCount >= 10) break;
			}
		}
		
		// finish out by adding more ships with any leftover points
		failCount = 0;
		while (fleet.getFleetPoints() < maxFP) {
			String role = main.pick();
			boolean added = faction.pickShipAndAddToFleet(role, ShipPickParams.all(), fleet) > 0;
			if (added) {
				failCount = 0;
			} else {
				failCount++;
				if (failCount >= 10) break;
			}
		}
		
		finishAndSync(fleet);
		
		return fleet;
	}
	
	
	public static void addGenericCombatShips(CampaignFleetAPI fleet, FactionAPI faction, MarketAPI market, int extraFP) {
		float qualityFactor = market.getShipQualityFactor();
		
		WeightedRandomPicker<String> main = new WeightedRandomPicker<String>();
		
		main.add(ShipRoles.COMBAT_SMALL, 5);
		main.add(ShipRoles.COMBAT_SMALL, 5);
		
		main.add(ShipRoles.ESCORT_SMALL, 6);
		main.add(ShipRoles.COMBAT_SMALL, 20);
		main.add(ShipRoles.FAST_ATTACK, 10);

		if (extraFP >= 20) {
			main.add(ShipRoles.COMBAT_MEDIUM, 10);
			main.add(ShipRoles.ESCORT_MEDIUM, 10);
		}
		if (extraFP >= 40) {
			main.add(ShipRoles.COMBAT_LARGE, 5);
			main.add(ShipRoles.COMBAT_SMALL, 5);
		}
		if (extraFP >= 60) {
			main.add(ShipRoles.COMBAT_LARGE, 5);
			main.add(ShipRoles.COMBAT_CAPITAL, 3);
		}
		
		// add ships and/or fighters, leave some headroom for carriers
		int failCount = 0;
		int starting = fleet.getFleetPoints();
		while (fleet.getFleetPoints() < starting + extraFP) {
			String role = main.pick();
			boolean added = faction.pickShipAndAddToFleet(role, ShipPickParams.all(), fleet) > 0;
			if (added) {
				failCount = 0;
			} else {
				failCount++;
				if (failCount >= 10) break;
			}
		}
	}
}














