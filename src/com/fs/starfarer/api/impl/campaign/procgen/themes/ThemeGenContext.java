package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.impl.campaign.procgen.Constellation;

public class ThemeGenContext {
	public Map<Constellation, String> majorThemes = new HashMap<Constellation, String>();
	public List<Constellation> constellations = new ArrayList<Constellation>();
}
