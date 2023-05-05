package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class EconomyFleetAssignmentAI extends RouteFleetAssignmentAI {

	public static class CargoQuantityData {
		public String cargo;
		public int units;
		public CargoQuantityData(String cargo, int units) {
			this.cargo = cargo;
			this.units = units;
		}
		
		public CommoditySpecAPI getCommodity() {
			CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(cargo);
			return spec;
		}
	}
	
	public static class EconomyRouteData {
		public float cargoCap, fuelCap, personnelCap;
		public float size;
		public boolean smuggling = false;
		public MarketAPI from;
		public MarketAPI to;
		
		public List<CargoQuantityData> cargoDeliver = new ArrayList<CargoQuantityData>();
		public List<CargoQuantityData> cargoReturn = new ArrayList<CargoQuantityData>();
		
		public void addDeliver(String id, int qty) {
			cargoDeliver.add(new CargoQuantityData(id, qty));
		}
		public void addReturn(String id, int qty) {
			cargoReturn.add(new CargoQuantityData(id, qty));
		}
		
		public static String getCargoList(List<CargoQuantityData> cargo) {
			List<String> strings = new ArrayList<String>();
			
			List<CargoQuantityData> sorted = new ArrayList<CargoQuantityData>(cargo);
			Collections.sort(sorted, new Comparator<CargoQuantityData>() {
				public int compare(CargoQuantityData o1, CargoQuantityData o2) {
					if (o1.getCommodity().isPersonnel() && !o2.getCommodity().isPersonnel()) {
						return 1;
					}
					if (o2.getCommodity().isPersonnel() && !o1.getCommodity().isPersonnel()) {
						return -1;
					}
					return o2.units - o1.units;
				}
			});
			
			for (CargoQuantityData curr : sorted) {
				CommoditySpecAPI spec = curr.getCommodity();
				//CommodityOnMarketAPI com = from.getCommodityData(curr.cargo);
				//if (com.getId().equals(Commodities.SHIPS)) {
				if (spec.getId().equals(Commodities.SHIPS)) {
					strings.add("ship hulls");
					continue;
				}
				//if (com.getCommodity().isMeta()) continue;
				//strings.add(com.getCommodity().getName().toLowerCase());
				if (spec.isMeta()) continue;
				strings.add(spec.getName().toLowerCase());
			}
			if (strings.size() > 4) {
				List<String> copy = new ArrayList<String>();
				copy.add(strings.get(0));
				copy.add(strings.get(1));
				copy.add("other commodities");
				strings = copy;
			}
			return Misc.getAndJoined(strings);
		}
	}
	
	
	private String origFaction;
	private IntervalUtil factionChangeTracker = new IntervalUtil(0.1f, 0.3f);
	public EconomyFleetAssignmentAI(CampaignFleetAPI fleet, RouteData route) {
		super(fleet, route);
		//origFaction = fleet.getFaction().getId();
		origFaction = route.getFactionId();
		if (!getData().smuggling) {
			origFaction = null;
			factionChangeTracker = null;
		} else {
			factionChangeTracker.forceIntervalElapsed();
			doSmugglingFactionChangeCheck(0.1f);
		}
	}
	
	public static String getCargoListDeliver(RouteData route) {
		return getCargoList(route, route.getSegments().get(0));
	}
	public static String getCargoListReturn(RouteData route) {
		return getCargoList(route, route.getSegments().get(3));
	}
	public static String getCargoList(RouteData route, RouteSegment segment) {
//		int index = route.getSegments().indexOf(segment);
		EconomyRouteData data = (EconomyRouteData) route.getCustom();
		
		Integer id = segment.getId();
		
		if (id <= EconomyFleetRouteManager.ROUTE_DST_UNLOAD) {
			return EconomyRouteData.getCargoList(data.cargoDeliver);
		}
		return EconomyRouteData.getCargoList(data.cargoReturn);
	}
	protected String getCargoList(RouteSegment segment) {
		return getCargoList(route, segment);
	}
	
	protected void updateCargo(RouteSegment segment) {
		//int index = route.getSegments().indexOf(segment);
		
		// 0: loading from
		// 1: moving to
		// 2: unloading to
		// 3: loading to
		// 4: moving from
		// 5: unloading from
		
		Integer id = segment.getId();
		
		if (route.isExpired() || id == EconomyFleetRouteManager.ROUTE_SRC_LOAD || 
								 id == EconomyFleetRouteManager.ROUTE_DST_LOAD) { 
			fleet.getCargo().clear();
			syncMothballedShips(0f, null);
			return;
		}
		
		EconomyRouteData data = getData();
		MarketAPI cargoSource = data.from;
		List<CargoQuantityData> list = data.cargoDeliver;
		if (id > EconomyFleetRouteManager.ROUTE_DST_LOAD) {
			cargoSource = data.to;
			list = data.cargoReturn;
		}
		
		CargoAPI cargo = fleet.getCargo();
		cargo.clear();
		
		float total = 0f;
		Map<String, Float> target = new LinkedHashMap<String, Float>();
		float ships = 0f;
		for (CargoQuantityData curr : list) {
			CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(curr.cargo);
			float qty = (int) (BaseIndustry.getSizeMult(curr.units) * spec.getEconUnit());
			
			if (curr.cargo.equals(Commodities.SHIPS)) {
				ships = Math.max(ships, curr.units);
				continue;
			}
			
			if (curr.cargo.equals(Commodities.FUEL)) {
				cargo.addFuel(Math.min(qty, cargo.getMaxFuel()));
				continue;
			}
			
			if (curr.cargo.equals(Commodities.CREW)) continue;
			if (curr.cargo.equals(Commodities.MARINES)) continue;

			total += qty;
			target.put(curr.cargo, qty);
		}
		
		syncMothballedShips(ships, cargoSource);
		
		if (total <= 0) return;
		
		float maxCargo = cargo.getMaxCapacity();
		for (String cid : target.keySet()) {
			float qty = target.get(cid);
			
			cargo.addCommodity(cid, ((int) qty * Math.min(1f, maxCargo / total)));
		}
	}
	
	protected void syncMothballedShips(float units, MarketAPI market) {
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.isMothballed()) {
				fleet.getFleetData().removeFleetMember(member);
			}
		}
		
		if (units <= 0) return;
		
		
		Random random = new Random();
		if (route.getSeed() != null) {
			random = new Random(route.getSeed());
		}

		float add = units * 1.5f + random.nextInt(3);
		
		EconomyRouteData data = getData();
		boolean sameFaction = data.from.getFaction() == data.to.getFaction();
		for (int i = 0; i < add; i++) {
			WeightedRandomPicker<String> roles = new WeightedRandomPicker<String>(random);
			roles.add(ShipRoles.COMBAT_FREIGHTER_SMALL, 20f);
			roles.add(ShipRoles.FREIGHTER_SMALL, 20f);
			roles.add(ShipRoles.TANKER_SMALL, 10f);
			if (i >= 2) {
				roles.add(ShipRoles.COMBAT_FREIGHTER_MEDIUM, 20f * (i - 1));
				roles.add(ShipRoles.FREIGHTER_MEDIUM, 20f * (i - 1));
				roles.add(ShipRoles.TANKER_MEDIUM, 10f * (i - 1));
			}
			if (i >= 5) {
				roles.add(ShipRoles.COMBAT_FREIGHTER_LARGE, 20f * (i - 2));
				roles.add(ShipRoles.FREIGHTER_LARGE, 20f * (i - 2));
				roles.add(ShipRoles.TANKER_LARGE, 10f * (i - 2));
			}
			
			String role = roles.pick();
			ShipPickParams params = ShipPickParams.priority();
			if (!sameFaction) params = ShipPickParams.imported();
			List<ShipRolePick> picks = market.pickShipsForRole(role, params, random, null);
			for (ShipRolePick pick : picks) {
				FleetMemberAPI member = fleet.getFleetData().addFleetMember(pick.variantId);
				member.getRepairTracker().setMothballed(true);
			}
		}
		fleet.getFleetData().sort();
	}
	
	
	@Override
	protected String getStartingActionText(RouteSegment segment) {
		String list = getCargoList(segment);
		if (list.isEmpty()) {
			return "preparing for a voyage to " + getData().to.getName();
		}
		return "loading " + list + " at " + getData().from.getName();
	}
	@Override
	protected String getEndingActionText(RouteSegment segment) {
		String list = getCargoList(segment);
		if (list.isEmpty()) {
			return "orbiting " + getData().from.getName();
		}
		return "unloading " + list + " at " + getData().from.getName();
	}
	
	@Override
	protected String getTravelActionText(RouteSegment segment) {
		String list = getCargoList(segment);
		
		//int index = route.getSegments().indexOf(segment);
		Integer id = segment.getId();
		if (id == EconomyFleetRouteManager.ROUTE_TRAVEL_DST || id == EconomyFleetRouteManager.ROUTE_TRAVEL_WS) {
			if (list.isEmpty()) {
				return "traveling to " + getData().to.getName();
			}
			return "delivering " + list + " to " + getData().to.getName(); 
		} else if (id == EconomyFleetRouteManager.ROUTE_TRAVEL_SRC || id == EconomyFleetRouteManager.ROUTE_TRAVEL_BACK_WS) {
			if (list.isEmpty()) {
				return "returning to " + getData().from.getName();
			}
			return "returning to " + getData().from.getName() + " with " + list;
		}
		return super.getTravelActionText(segment);
	}
	
	@Override
	protected String getInSystemActionText(RouteSegment segment) {
		String list = getCargoList(segment);
		//int index = route.getSegments().indexOf(segment);
		Integer id = segment.getId();
		
		if (id == EconomyFleetRouteManager.ROUTE_DST_UNLOAD) {
			if (list.isEmpty()) {
				return "orbiting " + getData().to.getName();
			}
			return "unloading " + list + " at " + getData().to.getName();
		} else if (id == EconomyFleetRouteManager.ROUTE_DST_LOAD) {
			if (list.isEmpty()) {
				return "orbiting " + getData().to.getName();
			}
			return "loading " + list + " at " + getData().to.getName();
		} else if (id == EconomyFleetRouteManager.ROUTE_RESUPPLY_WS || id == EconomyFleetRouteManager.ROUTE_RESUPPLY_BACK_WS) {
			return "resupplying";
		}
		
		return super.getInSystemActionText(segment);
	}

	
	@Override
	protected void addEndingAssignment(RouteSegment current, boolean justSpawned) {
		super.addEndingAssignment(current, justSpawned);
		updateCargo(current);
	}

	@Override
	protected void addLocalAssignment(RouteSegment current, boolean justSpawned) {
		super.addLocalAssignment(current, justSpawned);
		updateCargo(current);
	}

	@Override
	protected void addStartingAssignment(RouteSegment current, boolean justSpawned) {
		super.addStartingAssignment(current, justSpawned);
		updateCargo(current);
	}

	@Override
	protected void addTravelAssignment(RouteSegment current, boolean justSpawned) {
		super.addTravelAssignment(current, justSpawned);
		updateCargo(current);
	}

	
	protected EconomyRouteData getData() {
		EconomyRouteData data = (EconomyRouteData) route.getCustom();
		return data;
	}
	
	
	@Override
	public void advance(float amount) {
		super.advance(amount);
		doSmugglingFactionChangeCheck(amount);
	}
	
	
	public void doSmugglingFactionChangeCheck(float amount) {
		EconomyRouteData data = getData();
		if (!data.smuggling) return;
		float days = Global.getSector().getClock().convertToDays(amount);
		
//		if (fleet.isInCurrentLocation()) {
//			System.out.println("23wefwf23");
//			days *= 100000f;
//		}
		
		factionChangeTracker.advance(days);
		if (factionChangeTracker.intervalElapsed() && fleet.getAI() != null) {
			MarketAPI align = null;
			if (data.from.getStarSystem() == fleet.getContainingLocation()) {
				align = data.from;
			} else if (data.to.getStarSystem() == fleet.getContainingLocation()) {
				align = data.to;
			}
			
			if (align != null) {
				String targetFac = origFaction;
				boolean hostile = align.getFaction().isHostileTo(targetFac);
				if (hostile) {
					targetFac = Factions.INDEPENDENT;
					hostile = align.getFaction().isHostileTo(targetFac);
				}
				if (hostile) {
					targetFac = align.getFactionId();
				}
				if (!fleet.getFaction().getId().equals(targetFac)) {
					fleet.setFaction(targetFac, true);
				}
			} else {
				String targetFac = origFaction;
				if (fleet.isInHyperspace()) {
					targetFac = Factions.INDEPENDENT;
				}
				if (!fleet.getFaction().getId().equals(targetFac)) {
					fleet.setFaction(targetFac, true);
				}
			}
			
//			SectorEntityToken target = route.getMarket().getPrimaryEntity();
//			FleetAssignmentDataAPI assignment = fleet.getAI().getCurrentAssignment();
//			if (assignment != null && assignment.getAssignment() != FleetAssignment.STANDING_DOWN) {
//				target = assignment.getTarget();
//			}
//			if (target != null && target.getFaction() != null) {
//				boolean targetHostile = target.getFaction().isHostileTo(origFaction);
//				boolean mathchesTarget = fleet.getFaction().getId().equals(target.getFaction().getId());
//				boolean mathchesOrig = fleet.getFaction().getId().equals(origFaction);
//				float dist = Misc.getDistance(fleet.getLocation(), target.getLocation());
//				if (dist < target.getRadius() + fleet.getRadius() + 1000) {
//					if (targetHostile && !mathchesTarget) {
//						fleet.setFaction(target.getFaction().getId(), true);
//					}
//				} else {
//					if (!mathchesOrig) {
//						fleet.setFaction(origFaction, true);
//					}
//				}
//			}
		}
	}
	
}










