package com.fs.starfarer.api.impl.campaign.procgen;

import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenContext;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenResult;

public interface TerrainGenPlugin {

	boolean wantsToHandle(TerrainGenDataSpec terrainData, GenContext context);
	GenResult generate(TerrainGenDataSpec terrainData, GenContext context);
}
