package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.listeners.CoreDiscoverEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.PlanetInfoParams;
import com.fs.starfarer.api.util.Misc;

public class SurveyDataForPlanetIntel extends FleetLogIntel {

	protected String longText;
	protected PlanetAPI planet;
	
	public SurveyDataForPlanetIntel(PlanetAPI planet, String longText, TextPanelAPI textPanel) {
		this.planet = planet;
		this.longText = longText;
		
		setSound("ui_discovered_entity");
		setIconId("found_planet_data");
		
		setRemoveTrigger(planet);
		
		setListInfoParam(DISCOVERED_PARAM);
		Global.getSector().getIntelManager().addIntel(this, false, textPanel);
		setListInfoParam(null);
		
		if (planet.getStarSystem() != null && Misc.hasUnexploredRuins(planet.getMarket())) {
			CoreDiscoverEntityPlugin.addSalvorsTallyIfNeeded(planet.getStarSystem());
		}
	}
	
	@Override
	protected String getName() {
		//String name = "Survey Data: " + planet.getName() + ", " + planet.getTypeNameWithWorld();
		String classStr = Misc.getPlanetSurveyClass(planet);
		String name = "Survey Data: " + classStr + " " + planet.getTypeNameWithWorld(); 
		//name = "Survey Data: Class IV Cryovolcanic World";
		return name;
	}


	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
		addBulletPoints(info, mode);
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		//boolean isUpdate = getListInfoParam() != null;
		
		bullet(info);
		//info.addPara(str, tc, initPad);
		initPad = 0f;
		unindent(info);
	}	
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		PlanetInfoParams params = new PlanetInfoParams();
		params.showConditions = true;
		params.showName = true;
		params.withClass = true;
		params.scaleEvenWhenShowingName = true;
		params.conditionsYOffset = 32f;
		params.showHazardRating = true;
		info.showPlanetInfo(planet, width, width / 1.62f, params, opad + params.conditionsYOffset);
		info.addPara(longText, opad + 18f);

		addBulletPoints(info, ListInfoMode.IN_DESC);
		
		addLogTimestamp(info, tc, opad);
		addDeleteButton(info, width);
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.remove(Tags.INTEL_FLEET_LOG);
		tags.add(Tags.INTEL_EXPLORATION);
		return tags;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return planet;
	}
	
	
	
}




