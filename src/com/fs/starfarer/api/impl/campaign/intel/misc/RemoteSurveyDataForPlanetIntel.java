package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.util.Random;
import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.plugins.SurveyPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.PlanetInfoParams;
import com.fs.starfarer.api.util.Misc;

public class RemoteSurveyDataForPlanetIntel extends FleetLogIntel {

	protected PlanetAPI planet;
	protected String minClass;
	
	public RemoteSurveyDataForPlanetIntel(PlanetAPI planet) {
		this.planet = planet;
		
		setSound("ui_discovered_entity");
		setIconId("remote_survey_data");
		
		setRemoveTrigger(planet);
		setRemoveSurveyedPlanet(true);
		
		minClass = pickMinClass();
		
		setListInfoParam(DISCOVERED_PARAM);
		Global.getSector().getIntelManager().addIntel(this, false, null);
		setListInfoParam(null);
	}
	
	public String pickMinClass() {
		SurveyPlugin plugin = (SurveyPlugin) Global.getSettings().getNewPluginInstance("surveyPlugin");
		String type = plugin.getSurveyDataType(planet);
		int typeNum = 0;
		if (Commodities.SURVEY_DATA_1.equals(type)) {
			typeNum = 1;
		} else if (Commodities.SURVEY_DATA_2.equals(type)) {
			typeNum = 2;
		} else if (Commodities.SURVEY_DATA_3.equals(type)) {
			typeNum = 3;
		} else if (Commodities.SURVEY_DATA_4.equals(type)) {
			typeNum = 4;
		} else if (Commodities.SURVEY_DATA_5.equals(type)) {
			typeNum = 5;
		}
		
		Random random = Misc.getRandom(Misc.getSalvageSeed(planet, true), 0);
		if (random.nextBoolean()) {
			typeNum--;
		}
		if (typeNum < 1) typeNum = 1;
		
		switch (typeNum) {
		case 1: return "Class I";
		case 2: return "Class II";
		case 3: return "Class III";
		case 4: return "Class IV";
		}
		return "Class V";
	}
	
	@Override
	protected String getName() {
		String name = "Remote Survey: " + minClass + "+ " + planet.getTypeNameWithWorld(); 
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
		params.classStrOverride = minClass + "+";
		params.scaleEvenWhenShowingName = true;
		params.conditionsYOffset = 32f;
		params.showHazardRating = true;
		info.showPlanetInfo(planet, width, width / 1.62f, params, opad + params.conditionsYOffset);
		info.addPara("A remote survey has indicated that " + planet.getName() + " is " + minClass + " or higher "
				+ "and is likely the most promising candidate "
				+ "for a full survey operation in the " + planet.getStarSystem().getNameWithLowercaseTypeShort() + ".", opad + 18f,
				h, minClass + " or higher", "most promising candidate");

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




