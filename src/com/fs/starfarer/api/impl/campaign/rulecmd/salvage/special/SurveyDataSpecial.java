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
import com.fs.starfarer.api.impl.campaign.intel.misc.BreadcrumbIntel;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialPlugin;
import com.fs.starfarer.api.plugins.SurveyPlugin;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SurveyDataSpecial extends BaseSalvageSpecial {

	public static float MAX_RANGE = 16000f;
	
	public static enum SurveyDataSpecialType {
		SCRAMBLED, // used when the planet is already surveyed or doesn't exist anymore or nothing is found etc
		PLANET_INTERESTING_PROPERTY,
		PLANET_SURVEY_DATA,
		SYSTEM_PRELIMINARY_SURVEY,
		AUTO_PICK, // generate one of the above automatically, for a nearby planet or system
		AUTO_PICK_NOT_SYSTEM, // pick either property or data, but not full system
	}
	
	
	public static class SurveyDataSpecialData implements SalvageSpecialData {
		public SurveyDataSpecialType type = null;
		public String entityId = null;
		public String secondaryId = null;
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
		
		if (data.type == SurveyDataSpecialType.AUTO_PICK ||
				data.type == SurveyDataSpecialType.AUTO_PICK_NOT_SYSTEM) {
			WeightedRandomPicker<SurveyDataSpecialType> picker = new WeightedRandomPicker<SurveyDataSpecialType>(random);
			picker.add(SurveyDataSpecialType.PLANET_INTERESTING_PROPERTY, 50f);
			picker.add(SurveyDataSpecialType.PLANET_SURVEY_DATA, 20f);
			
			if (data.type == SurveyDataSpecialType.AUTO_PICK) {
				picker.add(SurveyDataSpecialType.SYSTEM_PRELIMINARY_SURVEY, 5f);
			}
			
			data.type = picker.pick();
			data.entityId = null;
			
			if (data.type == SurveyDataSpecialType.PLANET_INTERESTING_PROPERTY) {
				List<StarSystemAPI> systems = Misc.getSystemsInRange(entity, null, true, MAX_RANGE);
				PlanetAPI planet = DerelictThemeGenerator.findInterestingPlanet(systems, null, false, data.includeRuins, random);
				String conditionId = DerelictThemeGenerator.getInterestingCondition(planet, data.includeRuins);
				if (planet != null && conditionId != null) {
					data.entityId = planet.getId();
					data.secondaryId = conditionId;
				}
			} else if (data.type == SurveyDataSpecialType.PLANET_SURVEY_DATA) {
				List<StarSystemAPI> systems = Misc.getSystemsInRange(entity, null, true, MAX_RANGE);
				PlanetAPI planet = DerelictThemeGenerator.findInterestingPlanet(systems, null, false, data.includeRuins, random);
				if (planet != null) {
					data.entityId = planet.getId();
				}
			} else if (data.type == SurveyDataSpecialType.SYSTEM_PRELIMINARY_SURVEY) {
				StarSystemAPI system = DerelictThemeGenerator.findNearbySystem(entity, null, random, MAX_RANGE);
				if (system != null) {
					data.entityId = system.getId();
				}
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
		case PLANET_INTERESTING_PROPERTY:
			initInterestingProperty();
			break;
		case PLANET_SURVEY_DATA:
			initPlanetSurveyData();
			break;
		case SYSTEM_PRELIMINARY_SURVEY:
			initPreliminarySystemSurvey();
			break;
		}

	}

	public void initNothing() {
		addText("The $shortName's memory banks have been scrubbed clean by hard radiation, and the systems are largely inert and non-functional.");
		setDone(true);
	}
	
	
	protected void initInterestingProperty() {
		if (data.entityId == null || data.secondaryId == null) {
			initNothing();
			return;
		}
		
		PlanetAPI planet = (PlanetAPI) Global.getSector().getEntityById(data.entityId);
		if (planet.getMarket() != null && planet.getMarket().getSurveyLevel() == SurveyLevel.FULL) {
			initNothing();
			return;
		}
		
		String text1 = getString("The $shortName's memory banks are partially accessible, and the data therein ");
		boolean debris = Entities.DEBRIS_FIELD_SHARED.equals(entity.getCustomEntityType());
		if (debris) {
			text1 = "Your salvage crews find a memory bank in the debris. The data therein ";
		}
		
		String desc = "";
		String world = planet.getSpec().getAOrAn() + " " + planet.getTypeNameWithWorld().toLowerCase();
		
		String loc = BreadcrumbSpecial.getLocatedString(planet, true);
		loc = loc.replaceFirst("located ", "");
		
		String subject = "";
		MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(data.secondaryId);
		if (spec.getId().equals(Conditions.HABITABLE)) {
			subject = "Habitable World";
			desc = "points to the existence of " + world + " with a low hazard rating " + loc;
			
		} else {
			subject = Misc.ucFirst(spec.getName()) + " Location";
			desc = "contained information about " + spec.getName().toLowerCase() + " on " + world + " " + loc;
		}
		
		desc += ".";
		
		addText(text1 + desc);
		
		
		String text1ForIntel = "While exploring $aOrAn $nameInText, your crews found " +
							   "partially accessible memory banks that ";
		
		BreadcrumbIntel intel = new BreadcrumbIntel(entity, planet);
		intel.setTitle(getString(subject));
		intel.setText(getString(text1ForIntel + desc));
		Global.getSector().getIntelManager().addIntel(intel, false, text);
		
//		CommMessageAPI message = FleetLog.beginEntry(subject, planet);
//		message.getSection1().addPara(getString(text1 + desc));
//		FleetLog.addToLog(message, text);
		
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
		
		String subject = "Survey Data for " + name;
		
		addText(text1);
		Misc.setFullySurveyed(planet.getMarket(), null, false);
		Misc.addSurveyDataFor(planet, text);
//		text.setFontSmallInsignia();
//		text.addParagraph("Acquired full survey data for " + name + ", " + planet.getTypeNameWithWorld().toLowerCase(),
//								planet.getSpec().getIconColor());
//		text.setFontInsignia();
		
		SurveyPlugin plugin = (SurveyPlugin) Global.getSettings().getPlugin("surveyPlugin");
		plugin.init(Global.getSector().getPlayerFleet(), planet);
		long xp = plugin.getXP();
		if (xp > 0) {
			Global.getSector().getPlayerPerson().getStats().addXP(xp, text);
		}
		

		BreadcrumbIntel intel = new BreadcrumbIntel(entity, planet);
		intel.setTitle(getString(subject));
		intel.setText(getString(text1ForIntel));
		intel.setShowSpecificEntity(true);
		Global.getSector().getIntelManager().addIntel(intel, false, text);
		
//		CommMessageAPI message = FleetLog.beginEntry(subject, planet);
//		message.getSection1().addPara(getString(text1));
//		FleetLog.addToLog(message, text);
		
		//unsetData();
		setDone(true);
	}
	
	
	protected void initPreliminarySystemSurvey() {
		if (data.entityId == null) {
			initNothing();
			return;
		}
		
		StarSystemAPI system = Global.getSector().getStarSystem(data.entityId);
		if (system == null) {
			initNothing();
			return;
		}
		
		String name = system.getNameWithLowercaseType();
		String text1 = "The $shortName's memory banks are partially accessible, " +
					   "and contain complete preliminary survey data for the " + name + ".";
		
		String text1ForIntel = "While exploring $aOrAn $nameInText, your crews found " +
		   "partially accessible memory banks that contain complete preliminary survey data for the " + name + ".";
		
		
		boolean debris = Entities.DEBRIS_FIELD_SHARED.equals(entity.getCustomEntityType());
		if (debris) {
			text1 = "Your salvage crews find a functional memory bank in the debris. " +
					"It contains complete preliminary survey data for the " + name + ".";
		}
		
		
		String subject = "Preliminary Survey of the " + system.getName();
		
		addText(text1);
		
		String data = "";
		Highlights h = new Highlights();
		for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isStar()) continue;
			if (planet.getMarket() == null) continue;
			if (!planet.getMarket().isPlanetConditionMarketOnly()) continue;
			if (planet.getMarket().getSurveyLevel().ordinal() > SurveyLevel.PRELIMINARY.ordinal()) continue;
			
			String curr = planet.getName() + ", " + planet.getTypeNameWithWorld().toLowerCase();
			data += "    " + curr + "\n";
			h.append(curr, planet.getSpec().getIconColor());

//			text.addParagraph("    " + planet.getName() + ", " + planet.getTypeNameWithWorld().toLowerCase(),
//								planet.getSpec().getIconColor());
			planet.getMarket().setSurveyLevel(SurveyLevel.PRELIMINARY);
			
			//Misc.setPreliminarySurveyed(planet.getMarket(), text, true);
		}
		
		//data = "";
		
		if (!data.isEmpty()) {
			text.setFontSmallInsignia();
			text.addParagraph("Preliminary survey data for:", Misc.getTooltipTitleAndLightHighlightColor());
			//data = data.substring(0, data.length() - 2);
			data = "    " + data.trim();
			text.addParagraph(data);
			text.setHighlightsInLastPara(h);
			text.setFontInsignia();
			
//			CommMessageAPI message = FleetLog.beginEntry(subject, system.getCenter());
//			message.getSection1().addPara(getString(text1));
//			FleetLog.addToLog(message, text);
			
			
			BreadcrumbIntel intel = new BreadcrumbIntel(entity, system.getCenter());
			intel.setTitle(getString(subject));
			intel.setText(getString(text1ForIntel));
			Global.getSector().getIntelManager().addIntel(intel, false, text);
			
		} else {
			text.addParagraph("However, you've already acquired this data through other means.");
		}
		
		//unsetData();
		setDone(true);
	}

	
	@Override
	public void optionSelected(String optionText, Object optionData) {
		super.optionSelected(optionText, optionData);
	}

	
	
}
