package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.Set;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator.ConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenContext;

public class GravityConditionGenerator implements ConditionGenerator {
	
	public void addConditions(Set<String> conditionsSoFar, GenContext context, PlanetAPI planet) {
		
		if (conditionsSoFar.contains(Conditions.LOW_GRAVITY)) return;
		if (conditionsSoFar.contains(Conditions.HIGH_GRAVITY)) return;
		
		float radius = planet.getRadius();
		
		float min = StarSystemGenerator.MIN_MOON_RADIUS;
		float max = 250f;
		
		if (radius < min) radius = min;
		if (radius > max) radius = max;
		
		if (radius < 100 && PlanetConditionGenerator.preconditionsMet(Conditions.LOW_GRAVITY, conditionsSoFar)) {
			float range = 100 - min;
			float chance = 0f;
			if (range > 0) {
				chance = 0.2f + 0.8f * ((100f - radius) / range);
			}
			if (StarSystemGenerator.random.nextFloat() < chance) {
				conditionsSoFar.add(Conditions.LOW_GRAVITY);
			}
			return;
		}
		
		if (radius > 140 && PlanetConditionGenerator.preconditionsMet(Conditions.HIGH_GRAVITY, conditionsSoFar)) {
			float range = max - 140;
			float chance = 0f;
			if (range > 0) {
				chance = 0.1f + 0.9f * ((max - radius) / range);
			}
			if (StarSystemGenerator.random.nextFloat() < chance) {
				conditionsSoFar.add(Conditions.HIGH_GRAVITY);
			}
			return;
		}
		
		
		
		
		
		
	}

}
