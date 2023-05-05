package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;

public class SectorThemeGenerator {

	public static List<ThemeGenerator> generators = new ArrayList<ThemeGenerator>();
	
	static {
		//generators.add(new SpecialThemeGenerator());
		generators.add(new DerelictThemeGenerator());
		generators.add(new RemnantThemeGenerator());
		generators.add(new RuinsThemeGenerator());
		generators.add(new MiscellaneousThemeGenerator());
	}
	
	public static void generate(ThemeGenContext context) {
		Collections.sort(generators, new Comparator<ThemeGenerator>() {
			public int compare(ThemeGenerator o1, ThemeGenerator o2) {
				int result = o1.getOrder() - o2.getOrder();
				if (result == 0) return o1.getThemeId().compareTo(o2.getThemeId());
				return result;
			}
		});
		
		float totalWeight = 0f;
		for (ThemeGenerator g : generators) {
			totalWeight += g.getWeight();
			g.setRandom(StarSystemGenerator.random);
		}
		
		for (ThemeGenerator g : generators) {
			float w = g.getWeight();
			
			float f = 0f;
			if (totalWeight > 0) {
				f = w / totalWeight; 
			} else {
				if (w > 0) f = 1f;
			}
			//g.setRandom(StarSystemGenerator.random);
			g.generateForSector(context, f);
			
			//float used = context.majorThemes.size();
			totalWeight -= w;

		}
	}
	
	
	
	
}
