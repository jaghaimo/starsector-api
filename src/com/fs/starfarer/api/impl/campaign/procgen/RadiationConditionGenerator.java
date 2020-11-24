package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.Set;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator.ConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenContext;

public class RadiationConditionGenerator implements ConditionGenerator {
	
	public void addConditions(Set<String> conditionsSoFar, GenContext context, PlanetAPI planet) {

		if (conditionsSoFar.contains(Conditions.IRRADIATED)) return;
		if (!PlanetConditionGenerator.preconditionsMet(Conditions.IRRADIATED, conditionsSoFar)) return;
		if (!context.starData.getId().equals(StarTypes.NEUTRON_STAR)) return;
		
		int orbitIndex = context.orbitIndex;
		if (context.parent != null) orbitIndex = context.parentOrbitIndex;

		if (orbitIndex <= 8) {
			conditionsSoFar.add(Conditions.IRRADIATED);
		}
	}

}
