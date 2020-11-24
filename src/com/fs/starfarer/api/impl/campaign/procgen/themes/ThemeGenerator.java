package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.Random;


public interface ThemeGenerator {
	String getThemeId();
	
	void generateForSector(ThemeGenContext context, float allowedUnusedFraction);
	
	/**
	 * Themes with lower "order" values get their shot at generating content first.
	 * @return
	 */
	int getOrder();
	
	
	/**
	 * What fraction of the Sector this theme wants to be used for, relative to other themes.
	 * 
	 * The fraction it gets is weight / (total weight of all themes).
	 * 
	 * Base value is 100.
	 * 
	 * @return
	 */
	float getWeight();

	Random getRandom();
	void setRandom(Random random);
}
