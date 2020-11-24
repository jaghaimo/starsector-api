package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.characters.CharacterCreationData;


/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface SectorProcGenPlugin {
	void prepare(CharacterCreationData data);
	
	void generate(CharacterCreationData data, SectorGenProgress progress);
	
//	void begin(CharacterCreationData data);
//	void nextStep();
//	boolean isDone();
//	
//	float getProgress();
}
