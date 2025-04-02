package com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.intel.misc.SurveyDataForPlanetIntel;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialPlugin;
import com.fs.starfarer.api.plugins.SurveyPlugin;
import com.fs.starfarer.api.util.Misc;

public class SurveyDataSpecial extends BaseSalvageSpecial {

	public static float MAX_RANGE = 30000f;
	
	public static enum SurveyDataSpecialType {
		SCRAMBLED, // used when the planet is already surveyed or doesn't exist anymore or nothing is found etc
		PLANET_SURVEY_DATA,
		
		// these are not really interesting enough
		@Deprecated PLANET_INTERESTING_PROPERTY,
		@Deprecated SYSTEM_PRELIMINARY_SURVEY,
		@Deprecated AUTO_PICK, // generate one of the above automatically, for a nearby planet or system
		@Deprecated AUTO_PICK_NOT_SYSTEM, // pick either property or data, but not full system
	}
	
	
	public static class SurveyDataSpecialData implements SalvageSpecialData {
		public SurveyDataSpecialType type = null;
		public String entityId = null;
		
		@Deprecated public String secondaryId = null;
		
		public boolean includeRuins = true;
		public SurveyDataSpecialData(SurveyDataSpecialType type) {
			this.type = type;
		}
		
		public SalvageSpecialPlugin createSpecialPlugin() {
			return new SurveyDataSpecial();
		}
	}
	
	private SurveyDataSpecialData data;
	
	public SurveyDataSpecial() {
	}

	@Override
	public void init(InteractionDialogAPI dialog, Object specialData) {
		super.init(dialog, specialData);
		
		data = (SurveyDataSpecialData) specialData;
		
		//random = new Random();
		
		if (data.type == SurveyDataSpecialType.PLANET_SURVEY_DATA && data.entityId == null) {
			List<StarSystemAPI> systems = Misc.getSystemsInRange(entity, null, true, MAX_RANGE);
			PlanetAPI planet = DerelictThemeGenerator.findInterestingPlanet(systems, null, false, data.includeRuins, random);
			if (planet != null) {
				data.entityId = planet.getId();
			}
		}
		
		if (data.entityId != null) {
			SectorEntityToken entity = Global.getSector().getEntityById(data.entityId);
			StarSystemAPI system = Global.getSector().getStarSystem(data.entityId);
			if (entity == null && system == null) { 
				data.entityId = null;
				data.type = SurveyDataSpecialType.SCRAMBLED;
			}
		} else {
			data.type = SurveyDataSpecialType.SCRAMBLED;
		}
		
		
		switch (data.type) {
		case SCRAMBLED:
			initNothing();
			break;
		case PLANET_SURVEY_DATA:
			initPlanetSurveyData();
			break;
		}

	}

	public void initNothing() {
		addText("The $shortName's memory banks have been scrubbed clean by hard radiation, and the systems are largely inert and non-functional.");
		setDone(true);
	}
	

	protected void initPlanetSurveyData() {
		if (data.entityId == null) {
			initNothing();
			return;
		}
		
		PlanetAPI planet = (PlanetAPI) Global.getSector().getEntityById(data.entityId);
		if (planet.getMarket() != null && planet.getMarket().getSurveyLevel() == SurveyLevel.FULL) {
			initNothing();
			return;
		}
		

		String name = planet.getName();
		String world = planet.getSpec().getAOrAn() + " " + planet.getTypeNameWithWorld().toLowerCase();
		//String loc = getLocationName(planet);
		String loc = BreadcrumbSpecial.getLocatedString(planet, true);
		loc = loc.replaceFirst("located ", "");
		
		String text1 = "The $shortName's memory banks are partially accessible, " +
					   "and contain full survey data for " + name + ", " + world + " located " + loc + ".";
		
		String text1ForIntel = "While exploring $aOrAn $nameInText, your crews found " +
		   					   "partially accessible memory banks that contain full survey data for " +
		   					   name + ", " + world + " located " + loc + ".";
		
		boolean debris = Entities.DEBRIS_FIELD_SHARED.equals(entity.getCustomEntityType());
		if (debris) {
			text1 = "Your salvage crews find a functional memory bank in the debris. " +
					"It contains full survey data for " + name + ", " + world + " located " + loc + ".";
		}
		
		String conditionId = DerelictThemeGenerator.getInterestingCondition(planet, data.includeRuins);
		if (conditionId != null) {
			MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(conditionId);
			if (spec != null) {
				text1 += " The world is notable for ";
				text1ForIntel += " The world is notable for ";
				if (conditionId.equals(Conditions.HABITABLE)) {
					text1 += "being habitable.";
					text1ForIntel += "being habitable.";
				} else {
					text1 += "having " + spec.getName().toLowerCase() + ".";
					text1ForIntel += "having " + spec.getName().toLowerCase() + ".";
				}
			}
		}
		
		
		//planet.getMarket().setSurveyLevel(SurveyLevel.PRELIMINARY);
		
		//String subject = "Survey Data: " + name + ", " + planet.getTypeNameWithWorld();
		
		addText(text1);
		Misc.setFullySurveyed(planet.getMarket(), null, false);
		Misc.addSurveyDataFor(planet, text);
//		text.setFontSmallInsignia();
//		text.addParagraph("Acquired full survey data for " + name + ", " + planet.getTypeNameWithWorld().toLowerCase(),
//								planet.getSpec().getIconColor());
//		text.setFontInsignia();
		
		SurveyPlugin plugin = (SurveyPlugin) Global.getSettings().getNewPluginInstance("surveyPlugin");
		plugin.init(Global.getSector().getPlayerFleet(), planet);
		long xp = plugin.getXP();
		if (xp > 0) {
			Global.getSector().getPlayerPerson().getStats().addXP(xp, text);
		}
		
		new SurveyDataForPlanetIntel(planet, getString(text1ForIntel), text);

//		BreadcrumbIntel intel = new BreadcrumbIntel(entity, planet);
//		intel.setTitle(getString(subject));
//		intel.setText(getString(text1ForIntel));
//		intel.setShowSpecificEntity(true);
//		//intel.setIcon(Global.getSettings().getSpriteName("intel", "found_planet_data"));
//		intel.setIconId("found_planet_data");
//		Global.getSector().getIntelManager().addIntel(intel, false, text);
		
//		CommMessageAPI message = FleetLog.beginEntry(subject, planet);
//		message.getSection1().addPara(getString(text1));
//		FleetLog.addToLog(message, text);
		
		//unsetData();
		setDone(true);
	}
	
	
	@Override
	public void optionSelected(String optionText, Object optionData) {
		super.optionSelected(optionText, optionData);
	}

	
	
}
