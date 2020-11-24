package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.Set;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator.ConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenContext;

public class HotConditionGenerator implements ConditionGenerator {
	
	public void addConditions(Set<String> conditionsSoFar, GenContext context, PlanetAPI planet) {

		int orbitIndex = context.orbitIndex;
		if (context.parent != null) orbitIndex = context.parentOrbitIndex;
		
		float normalIndex = context.starData.getHabZoneStart() - 1;
		float veryIndex = context.starData.getHabZoneStart() - 3;
		float eitherIndex = context.starData.getHabZoneStart() - 2;
		
		boolean matchVery = orbitIndex <= veryIndex;
		boolean matchNormal = !matchVery && orbitIndex <= normalIndex;
		boolean matchEither = orbitIndex == eitherIndex;
		if (matchEither) {
			matchNormal = StarSystemGenerator.random.nextFloat() < 0.5f;
			matchVery = !matchNormal;
		}

		boolean hasNormal = conditionsSoFar.contains(Conditions.HOT);
		boolean hasVery = conditionsSoFar.contains(Conditions.VERY_HOT);
		
		
		// if there's a baked-in "very hot" condition, don't downgrade it to "hot"
		if (matchNormal && !hasVery && !hasNormal &&
				 PlanetConditionGenerator.preconditionsMet(Conditions.HOT, conditionsSoFar)) {
			conditionsSoFar.add(Conditions.HOT);
			return;
		}
		
		// can, howeved, upgrade a "hot" to a "very hot" provided preconditions are met
		// (i.e. can't upgrade a desert/arid/jungle to "very hot" because "very hot" requires not habitable)
		if (matchVery && !hasVery && 
				PlanetConditionGenerator.preconditionsMet(Conditions.VERY_HOT, conditionsSoFar)) {
			conditionsSoFar.remove(Conditions.HOT);
			conditionsSoFar.add(Conditions.VERY_HOT);
			return;
		}
	}

}
