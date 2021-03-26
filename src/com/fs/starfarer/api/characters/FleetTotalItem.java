package com.fs.starfarer.api.characters;

import java.awt.Color;

import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class FleetTotalItem {
	public String label;
	public String value;
	public Color valueColor = Misc.getHighlightColor();
	public Color labelColor = Misc.getBasePlayerColor();
	public float sortOrder;
	public TooltipCreator tooltipCreator;
}
