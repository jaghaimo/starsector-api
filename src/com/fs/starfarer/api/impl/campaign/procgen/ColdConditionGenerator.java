package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.Set;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator.ConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenContext;

public class ColdConditionGenerator implements ConditionGenerator {
	
	public void addConditions(Set<String> conditionsSoFar, GenContext context, PlanetAPI planet) {

		int orbitIndex = context.orbitIndex;
		if (context.parent != null) orbitIndex = context.parentOrbitIndex;
		
		float normalIndex = context.starData.getHabZoneStart() + 2;
		float veryIndex = context.starData.getHabZoneStart() + 4;
		float eitherIndex = context.starData.getHabZoneStart() + 3;
		
		boolean matchVery = orbitIndex >= veryIndex;
		boolean matchNormal = !matchVery && orbitIndex >= normalIndex;
		boolean matchEither = orbitIndex == eitherIndex;
		if (matchEither) {
			matchNormal = StarSystemGenerator.random.nextFloat() < 0.5f;
			matchVery = !matchNormal;
		}

		boolean hasNormal = conditionsSoFar.contains(Conditions.COLD);
		boolean hasVery = conditionsSoFar.contains(Conditions.VERY_COLD);
		
		
		// if there's a baked-in "very cold" condition, don't downgrade it to "cold"
		if (matchNormal && !hasVery && !hasNormal &&
				 PlanetConditionGenerator.preconditionsMet(Conditions.COLD, conditionsSoFar)) {
			conditionsSoFar.add(Conditions.COLD);
			return;
		}
		
		// can, howeved, upgrade a "cold" to a "very cold" provided preconditions are met
		// (i.e. can't upgrade a tundra to "very cold" because "very cold" requires not habitable)
		if (matchVery && !hasVery && 
				PlanetConditionGenerator.preconditionsMet(Conditions.VERY_COLD, conditionsSoFar)) {
			conditionsSoFar.remove(Conditions.COLD);
			conditionsSoFar.add(Conditions.VERY_COLD);
			return;
		}
	}

}
