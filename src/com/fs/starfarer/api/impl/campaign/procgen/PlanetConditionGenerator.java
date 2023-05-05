package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.CustomConstellationParams;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenContext;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class PlanetConditionGenerator {

	public static interface ConditionGenerator {
		void addConditions(Set<String> conditionsSoFar, GenContext context, PlanetAPI planet);
	}
	
	
	/**
	 * Group id to generator map.
	 */
	public static Map<String, ConditionGenerator> generators = new HashMap<String, ConditionGenerator>();

	static {
		generators.put("gravity", new GravityConditionGenerator());
		generators.put("cold", new ColdConditionGenerator());
		generators.put("hot", new HotConditionGenerator());
		generators.put("light", new LightConditionGenerator());
		generators.put("radiation", new RadiationConditionGenerator());
	}
	
	
	
	public static void generateConditionsForPlanet(GenContext context, PlanetAPI planet) {
		generateConditionsForPlanet(context, planet, null);
	}
	
	public static void generateConditionsForPlanet(PlanetAPI planet, StarAge age) {
		generateConditionsForPlanet(null, planet, age);
	}
	
	public static void generateConditionsForPlanet(GenContext context, PlanetAPI planet, StarAge age) {
		
		if (context == null) {
//			if (planet.getId().toLowerCase().equals("tlalocan")) {
//				System.out.println("sdfwefe");
//			}
			context = createContext(planet, age);
		}
		
		Collection<ConditionGenDataSpec> all = Global.getSettings().getAllSpecs(ConditionGenDataSpec.class);
		List<ConditionGenDataSpec> specs = new ArrayList<ConditionGenDataSpec>();
		for (ConditionGenDataSpec spec : all) {
			specs.add(spec);
		}
		
		Collections.sort(specs, new Comparator<ConditionGenDataSpec>() {
			public int compare(ConditionGenDataSpec o1, ConditionGenDataSpec o2) {
				return (int) Math.signum(o1.getOrder() - o2.getOrder());
			}
		});
		
		Map<String, List<String>> groupsInOrder = new LinkedHashMap<String, List<String>>();
		float prevGroup = -100000;
		List<String> currList = null;
		for (ConditionGenDataSpec spec : specs) {
			float currGroup = spec.getOrder();
			if (prevGroup != currGroup) {
				currList = new ArrayList<String>();
				groupsInOrder.put(spec.getGroup(), currList);
			}
			prevGroup = currGroup;
			
			if (!currList.contains(spec.getGroup())) {
				currList.add(spec.getGroup());
			}
		}
		
		
//		List<String> groups = new ArrayList<String>();
//		//String prev = null;
//		for (ConditionGenDataSpec spec : specs) {
//			if (spec.getGroup() == null) continue;
//			//if (!spec.getGroup().equals(prev)) {
//			if (!groups.contains(spec.getGroup())) {
//				groups.add(spec.getGroup());
//			}
//			//prev = spec.getGroup();
//		}
		
		
		Set<String> conditionsSoFar = new HashSet<String>();
		
		// want to add random/fixed conditions for ALL groups with the same order first,
		// then do generators.
		// why: fixed hot/cold need to be added before hot/cold generators so
		// that those generators can process the conditions correctly.
		// this is due to hot/cold circular requirement dependency: hot req !cold, and vice versa
		for (String key : groupsInOrder.keySet()) {
			List<String> groups = groupsInOrder.get(key);
			for (String group : groups) {
				WeightedRandomPicker<String> picker = getGroupPicker(group, conditionsSoFar, context, planet);
				String pick = picker.pick();
				if (pick != null) {
					conditionsSoFar.add(pick);
				}
			}
			
			for (String group : groups) {
				ConditionGenerator generator = generators.get(group);
				if (generator != null) {
					generator.addConditions(conditionsSoFar, context, planet);
				}
			}
		}
		
		
		// add picked conditions to market
		MarketAPI market = planet.getMarket();
		if (market == null) {
			market = Global.getFactory().createMarket("market_" + planet.getId(), planet.getName(), 1);
			//market = Global.getFactory().createConditionMarket("market_" + planet.getId(), planet.getName(), 1);
			market.setPlanetConditionMarketOnly(true);
			market.setPrimaryEntity(planet);
			market.setFactionId(Factions.NEUTRAL);
			planet.setMarket(market);
		}
		
		for (String cid : conditionsSoFar) {
			if (cid.endsWith(ConditionGenDataSpec.NO_PICK_SUFFIX)) continue;
			//planet.getMemory().set("$genCondition:" + condition, true);
			
			MarketConditionAPI mc = market.getSpecificCondition(market.addCondition(cid));
			
			ConditionGenDataSpec spec = (ConditionGenDataSpec) Global.getSettings().getSpec(ConditionGenDataSpec.class, cid, true);
			mc.setSurveyed(!spec.isRequiresSurvey());
		}
		
		market.reapplyConditions();
	}
	
	
	public static WeightedRandomPicker<String> getGroupPicker(String group, Set<String> conditionsSoFar, 
															  GenContext context, PlanetAPI planet) {
		
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(StarSystemGenerator.random);
		
		List<ConditionGenDataSpec> groupData = getDataForGroup(group);

		String planetType = planet.getSpec().getPlanetType();
//		if (planetType.equals("lava")) {
//			System.out.println("sdfwefwe");
//		}
		
		PlanetGenDataSpec planetData = (PlanetGenDataSpec) Global.getSettings().getSpec(PlanetGenDataSpec.class, planetType, false);
		
		String category = planetData.getCategory();
		
		for (ConditionGenDataSpec data : groupData) {
			float weight = 1f;
			if (data.hasMultiplier(planetType)) {
				weight = data.getMultiplier(planetType);
			} else if (data.hasMultiplier(category)) {
				weight = data.getMultiplier(category);
			} else {
				continue;
			}
			
			for (String cid : conditionsSoFar) {
				if (data.hasMultiplier(cid)) {
					weight *= data.getMultiplier(cid);
				}
			}
			
			if (weight <= 0) continue;
			
			if (!preconditionsMet(data.getId(), conditionsSoFar)) continue;
			
			picker.add(data.getId(), weight);
		}
		
		return picker;
	}
	
	
	public static boolean preconditionsMet(String conditionId, Set<String> conditionsSoFar) {
		ConditionGenDataSpec data = (ConditionGenDataSpec) Global.getSettings().getSpec(ConditionGenDataSpec.class, conditionId, true);
		
		boolean foundAll = true;
		for (String cid : data.getRequiresAll()) {
			if (!conditionsSoFar.contains(cid)) {
				foundAll = false;
				break;
			}
		}
		if (!foundAll) return false;
		

		boolean foundOne = false;
		for (String cid : data.getRequiresAny()) {
			if (conditionsSoFar.contains(cid)) {
				foundOne = true;
				break;
			}
		}
		if (!foundOne && !data.getRequiresAny().isEmpty()) return false;
		
		
		foundOne = false;
		for (String cid : data.getRequiresNotAny()) {
			if (conditionsSoFar.contains(cid)) {
				foundOne = true;
				break;
			}
		}
		if (foundOne) return false;
		
		return true;
	}
	
	
	public static List<ConditionGenDataSpec> getDataForGroup(String group) {
		List<ConditionGenDataSpec> result = new ArrayList<ConditionGenDataSpec>();
		Collection<ConditionGenDataSpec> all = Global.getSettings().getAllSpecs(ConditionGenDataSpec.class);
		for (ConditionGenDataSpec spec : all) {
			if (group.equals(spec.getGroup())) {
				result.add(spec);
			}
		}
		return result;
	}
	
	
	public static GenContext createContext(PlanetAPI planet, StarAge age) {
		
		if (!(planet.getContainingLocation() instanceof StarSystemAPI)) return null;
		
		StarSystemAPI system = (StarSystemAPI) planet.getContainingLocation();
		
		CustomConstellationParams p = new CustomConstellationParams(age);
		StarSystemGenerator gen = new StarSystemGenerator(p);
		gen.system = system;
		gen.starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, system.getStar().getSpec().getPlanetType(), false);
		gen.starAge = age;
		gen.constellationAge = age;
		gen.starAgeData = (AgeGenDataSpec) Global.getSettings().getSpec(AgeGenDataSpec.class, age.name(), true);
		gen.star = system.getStar();
		gen.pickNebulaAndBackground();

		gen.systemCenter = system.getCenter();
		
		
		PlanetAPI parentPlanet = null;
		PlanetAPI parentStar = null;
		if (planet.getOrbitFocus() instanceof PlanetAPI) {
			PlanetAPI p1 = (PlanetAPI) planet.getOrbitFocus();
			PlanetAPI p2 = null;
			if (p1.getOrbitFocus() instanceof PlanetAPI) {
				p2 = (PlanetAPI) p1.getOrbitFocus();
			}
			if (p1.isStar()) {
				parentStar = p1;
			} else {
				parentPlanet = p1;
				if (p2 != null && p2.isStar()) {
					parentStar = p2;
				} else {
					parentStar = system.getStar();
				}
			}
		} else {
			parentStar = system.getStar();
		}
		
		StarGenDataSpec starData = gen.starData;
		PlanetGenDataSpec planetData = null;
		if (parentStar != null) {
			starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, parentStar.getSpec().getPlanetType(), false);
		}
		if (parentPlanet != null) {
			planetData = (PlanetGenDataSpec) Global.getSettings().getSpec(PlanetGenDataSpec.class, parentPlanet.getSpec().getPlanetType(), false);
		}

		int parentOrbitIndex = -1;
		int orbitIndex = 0;

		float fromStar = 0;
		if (parentPlanet == null) {
			orbitIndex = Misc.getEstimatedOrbitIndex(planet);
			parentOrbitIndex = -1;
			float dist = Misc.getDistance(parentStar.getLocation(), planet.getLocation());
			fromStar = dist;
		} else {
			parentOrbitIndex = Misc.getEstimatedOrbitIndex(planet);
			float dist = 0f;
			if (parentPlanet.getOrbitFocus() != null) {
				dist = Misc.getDistance(parentPlanet.getLocation(), parentPlanet.getOrbitFocus().getLocation());
				fromStar = dist;
			}
			orbitIndex = 1; // don't care about index of moon's orbit
		}
		
//		if (parentOrbitIndex >= 0) {
//			System.out.println("Orbit index for " + planet.getName() + ": " + parentOrbitIndex);
//		} else {
//			System.out.println("Orbit index for " + planet.getName() + ": " + orbitIndex);
//		}

		GenContext context = new GenContext(gen, system, gen.systemCenter, starData, 
				parentPlanet, orbitIndex, age.name(), fromStar, StarSystemGenerator.MAX_ORBIT_RADIUS,
				planetData != null ? planetData.getCategory() : null, parentOrbitIndex);
		context.orbitIndex = orbitIndex;

		return context;
	}
}













