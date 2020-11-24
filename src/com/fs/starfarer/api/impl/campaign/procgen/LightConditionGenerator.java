package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.Set;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator.ConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenContext;

public class LightConditionGenerator implements ConditionGenerator {
	
	public void addConditions(Set<String> conditionsSoFar, GenContext context, PlanetAPI planet) {

		int orbitIndex = context.orbitIndex;
		if (context.parent != null) orbitIndex = context.parentOrbitIndex;
		
		float hab = context.starData.getHabZoneStart();
		
		// want low-light conditions in neutron star systems
//		if (context.starData.getId().equals(StarTypes.NEUTRON_STAR)) {
//			hab = 0;
//		}
		
		float normalIndex = hab + 4;
		float veryIndex = hab + 8;
		float eitherIndex = hab + 5;
		
		boolean matchVery = orbitIndex >= veryIndex;
		boolean matchNormal = !matchVery && orbitIndex >= normalIndex;
		boolean matchEither = orbitIndex == eitherIndex;
		if (matchEither) {
			matchNormal = StarSystemGenerator.random.nextFloat() < 0.5f;
			matchVery = !matchNormal;
		}

		boolean hasNormal = conditionsSoFar.contains(Conditions.POOR_LIGHT);
		boolean hasVery = conditionsSoFar.contains(Conditions.DARK);
		
		
		// if there's a baked-in "darkness" condition, don't downgrade it to "poor light"
		if (matchNormal && !hasVery && !hasNormal &&
				 PlanetConditionGenerator.preconditionsMet(Conditions.POOR_LIGHT, conditionsSoFar)) {
			conditionsSoFar.add(Conditions.POOR_LIGHT);
			return;
		}
		
		// can, howeved, upgrade a "poor light" to a "darkness" provided preconditions are met
		if (matchVery && !hasVery && 
				PlanetConditionGenerator.preconditionsMet(Conditions.DARK, conditionsSoFar)) {
			conditionsSoFar.remove(Conditions.POOR_LIGHT);
			conditionsSoFar.add(Conditions.DARK);
			return;
		}
	}

}
