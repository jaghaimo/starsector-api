package com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.intel.misc.BreadcrumbIntel;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageSpecialInteraction.SalvageSpecialPlugin;

public class DomainSurveyDerelictSpecial extends BaseSalvageSpecial {

	public static enum SpecialType {
		SCRAMBLED,
		LOCATION_SURVEY_SHIP,
		LOCATION_MOTHERSHIP,
		
//		PLANET_INTERESTING_PROPERTY,
//		PLANET_SURVEY_DATA,
//		SYSTEM_PRELIMINARY_SURVEY,
		
		ACTIVATE_PROBE,
		ACTIVATE_SURVEY_SHIP,
	}
	
	
	public static class DomainSurveyDerelictSpecialData implements SalvageSpecialData {
		public SpecialType type = SpecialType.SCRAMBLED;
		public String entityId = null;
		public String secondaryId = null;
		public DomainSurveyDerelictSpecialData(SpecialType type) {
			this.type = type;
		}
		public SalvageSpecialPlugin createSpecialPlugin() {
			return new DomainSurveyDerelictSpecial();
		}
	}
	
	private DomainSurveyDerelictSpecialData data;
	
	public DomainSurveyDerelictSpecial() {
	}

	@Override
	public void init(InteractionDialogAPI dialog, Object specialData) {
		super.init(dialog, specialData);
		
		data = (DomainSurveyDerelictSpecialData) specialData;
		
		if (data.entityId != null) {
			SectorEntityToken entity = Global.getSector().getEntityById(data.entityId);
			if (entity == null) {// || 
//					(entity != null && entity instanceof CustomCampaignEntityAPI && 
//							!((CustomCampaignEntityAPI)entity).isDiscoverable())) {
				data.entityId = null;
				data.type = SpecialType.SCRAMBLED;
			}
		}
		
		switch (data.type) {
		case SCRAMBLED:
			initNothing();
			break;
//		case PLANET_INTERESTING_PROPERTY:
//			initInterestingProperty();
//			break;
//		case PLANET_SURVEY_DATA:
//			initPlanetSurveyData();
//			break;
//		case SYSTEM_PRELIMINARY_SURVEY:
////			PlanetAPI planet = (PlanetAPI) Global.getSector().getEntityById(data.entityId);
////			data.entityId = planet.getContainingLocation().getId();
//			initPreliminarySystemSurvey();
//			break;
		case LOCATION_SURVEY_SHIP:
			initSurveyParentEntity();
			break;
		case LOCATION_MOTHERSHIP:
			initSurveyParentEntity();
			break;
//		case CONSTELLATION_PRELIMINARY_SURVEY:
//			break;
		}
		
//		for (PlanetAPI curr : entity.getContainingLocation().getPlanets()) {
//			if (!curr.isStar()) {
//				planet = curr;
//				break;
//			}
//		}
//		
//		dialog.getTextPanel().addParagraph("Survey data for " + planet.getName() + " gained.");
//		//Misc.setFullySurveyed(planet.getMarket());
//		planet.getMarket().setSurveyLevel(SurveyLevel.PRELIMINARY);
//		
//		setDone(true);
//		dialog.getOptionPanel().clearOptions();
//		dialog.getOptionPanel().addOption("Continue", "continue");
	}

	public boolean shouldShowAgain() {
		return data != null && (
			data.type == SpecialType.ACTIVATE_PROBE || 
			data.type == SpecialType.ACTIVATE_SURVEY_SHIP
		);
	}
	

	private void initSurveyParentEntity() {
		if (data.entityId == null) {
			initNothing();
			return;
		}
		
		SectorEntityToken parent = Global.getSector().getEntityById(data.entityId);
		if (parent == null || !parent.hasSensorProfile()) {
			initNothing();
			return;
		}
		
		String text1 = "The $shortName's memory banks are partially accessible, and ";
		String text1ForIntel = "While exploring $aOrAn $nameInText, your crews found " +
		   						"partially accessible memory banks that ";
		//entity.getCustomEntitySpec().getAOrAn() + " " + 
		String name = parent.getCustomEntitySpec().getNameInText();
		String nameForTitle = parent.getCustomEntitySpec().getDefaultName();
		//String loc = BreadcrumbSpecial.getLocatedString(parent);
		
		String subject = getString("Derelict " + nameForTitle + " Location");
		
		text1 += "contain information pointing to the location of a " + name + ". ";
		text1ForIntel += "contain information pointing to the location of a " + name + ". ";
		//text1 += "It was last seen by this $shortName " + orbiting + loc + ".";
		
		String located = BreadcrumbSpecial.getLocatedString(parent, true);
		//located = located.replaceFirst("located ", "");
		text1 += "It was last seen by this $shortName " + located + ".";
		text1ForIntel += "It was last seen by this $shortName " + located + ".";
		
		text1 = getString(text1);
		
		addText(text1);
		
		BreadcrumbIntel intel = new BreadcrumbIntel(entity, parent);
		intel.setTitle(getString(subject));
		intel.setText(getString(text1ForIntel));
		Global.getSector().getIntelManager().addIntel(intel, false, text);
		
//		CommMessageAPI message = FleetLog.beginEntry(subject, parent);
//		message.getSection1().addPara(getString(text1));
//		FleetLog.addToLog(message, text);
		
		//unsetData();
		setDone(true);
	}

	public void initNothing() {
		addText("The $shortName's memory banks have been scrubbed clean by hard radiation, and the systems are largely inert and non-functional.");
		
		//unsetData();
		setDone(true);
	}
	
	
//	protected void initInterestingProperty() {
//		if (data.entityId == null || data.secondaryId == null) {
//			initNothing();
//			return;
//		}
//		
//		PlanetAPI planet = (PlanetAPI) Global.getSector().getEntityById(data.entityId);
//		if (planet.getMarket() != null && planet.getMarket().getSurveyLevel() == SurveyLevel.FULL) {
//			initNothing();
//			return;
//		}
//		
//		String text1 = getString("The $shortName's memory banks are partially accessible, and ");
//		String text1ForIntel = "While exploring $aOrAn $nameInText, your crews found " +
//		   						"partially accessible memory banks that ";
//		
//		String desc = "";
//		String world = planet.getSpec().getAOrAn() + " " + planet.getTypeNameWithWorld().toLowerCase();
//		//String loc = getLocationDescription(planet);
//		
//		String loc = BreadcrumbSpecial.getLocatedString(planet);
//		loc = loc.replaceFirst("located ", "");
//		
//		String subject = "";
//		MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(data.secondaryId);
//		if (spec.getId().equals(Conditions.HABITABLE)) {
//			subject = "Habitable planet location";
//			desc = "point to the existence of " + world + " with a low hazard rating " + loc;
//			
//		} else {
//			subject = Misc.ucFirst(spec.getName().toLowerCase()) + " location";
//			desc = "contain information about " + spec.getName().toLowerCase() + " on " + world + " " + loc;
//		}
//		
//		desc += ".";
//		
//		addText(text1 + desc);
//		
//		BreadcrumbIntel intel = new BreadcrumbIntel(entity, planet);
//		intel.setTitle(getString(subject));
//		intel.setText(getString(text1ForIntel));
//		Global.getSector().getIntelManager().addIntel(intel, false, text);
//		
////		CommMessageAPI message = FleetLog.beginEntry(subject, planet);
////		message.getSection1().addPara(getString(text1 + desc));
////		FleetLog.addToLog(message, text);
//		
//		//unsetData();
//		setDone(true);
//	}
	
	
//	protected void initPlanetSurveyData() {
//		if (data.entityId == null) {
//			initNothing();
//			return;
//		}
//		
//		PlanetAPI planet = (PlanetAPI) Global.getSector().getEntityById(data.entityId);
//		if (planet.getMarket() != null && planet.getMarket().getSurveyLevel() == SurveyLevel.FULL) {
//			initNothing();
//			return;
//		}
//		
//
//		String name = planet.getName();
//		String world = planet.getSpec().getAOrAn() + " " + planet.getTypeNameWithWorld().toLowerCase();
//		//String loc = getLocationName(planet);
//		String loc = BreadcrumbSpecial.getLocatedString(planet);
//		loc = loc.replaceFirst("located ", "");
//		
//		String text1 = getString("The $shortName's memory banks are partially accessible, " +
//				"and contain full survey data for " + name + ", " + world + " located " + loc + ".");
//		
//		//planet.getMarket().setSurveyLevel(SurveyLevel.PRELIMINARY);
//		
//		String subject = "Full survey data for " + name;
//		
//		addText(text1);
//		Misc.setFullySurveyed(planet.getMarket(), null, false);
//		Misc.addSurveyDataFor(planet, text);
////		text.setFontSmallInsignia();
////		text.addParagraph("Acquired full survey data for " + name + ", " + planet.getTypeNameWithWorld().toLowerCase(),
////								planet.getSpec().getIconColor());
////		text.setFontInsignia();
//		
//		SurveyPlugin plugin = (SurveyPlugin) Global.getSettings().getPlugin("surveyPlugin");
//		plugin.init(Global.getSector().getPlayerFleet(), planet);
//		long xp = plugin.getXP();
//		if (xp > 0) {
//			Global.getSector().getPlayerPerson().getStats().addXP(xp, text);
//		}
//		
//		CommMessageAPI message = FleetLog.beginEntry(subject, planet);
//		message.getSection1().addPara(getString(text1));
//		FleetLog.addToLog(message, text);
//		
//		//unsetData();
//		setDone(true);
//	}
	
	
//	protected void initPreliminarySystemSurvey() {
//		if (data.entityId == null) {
//			initNothing();
//			return;
//		}
//		
//		StarSystemAPI system = Global.getSector().getStarSystem(data.entityId);
//		if (system == null) {
//			initNothing();
//			return;
//		}
//		
//		String name = system.getNameWithLowercaseType();
//		String text1 = getString("The $shortName's memory banks are partially accessible, " +
//				"and contain complete preliminary survey data for the " + name + ".");
//		
//		String subject = "Acquired complete preliminary survey data for the " + name;
//		
//		addText(text1);
//		
//		String data = "";
//		Highlights h = new Highlights();
//		for (PlanetAPI planet : system.getPlanets()) {
//			if (planet.isStar()) continue;
//			if (planet.getMarket() == null) continue;
//			if (!planet.getMarket().isPlanetConditionMarketOnly()) continue;
//			if (planet.getMarket().getSurveyLevel().ordinal() > SurveyLevel.PRELIMINARY.ordinal()) continue;
//			
//			String curr = planet.getName() + ", " + planet.getTypeNameWithWorld().toLowerCase();
//			data += "    " + curr + "\n";
//			h.append(curr, planet.getSpec().getIconColor());
//			
////			text.addParagraph("    " + planet.getName() + ", " + planet.getTypeNameWithWorld().toLowerCase(),
////								planet.getSpec().getIconColor());
//			planet.getMarket().setSurveyLevel(SurveyLevel.PRELIMINARY);
//			
//			//Misc.setPreliminarySurveyed(planet.getMarket(), text, true);
//		}
//		//data = "";
//		if (!data.isEmpty()) {
//			text.setFontSmallInsignia();
//			text.addParagraph("Preliminary survey data for:", Misc.getTooltipTitleAndLightHighlightColor());
//			//data = data.substring(0, data.length() - 2);
//			data = "    " + data.trim();
//			text.addParagraph(data);
//			text.setHighlightsInLastPara(h);
//			text.setFontInsignia();
//			
//			CommMessageAPI message = FleetLog.beginEntry(subject, system.getCenter());
//			message.getSection1().addPara(getString(text1));
//			FleetLog.addToLog(message, text);
//		} else {
//			text.addParagraph("However, you've already acquired this data through other means.");
//		}
//		
//		//unsetData();
//		setDone(true);
//	}

	
//	protected void unsetData() {
//		BaseCommandPlugin.getEntityMemory(memoryMap).unset(MemFlags.SALVAGE_SPECIAL_DATA);
//	}
	
	public static String getLocationName(SectorEntityToken entity) {
		LocationAPI loc = entity.getContainingLocation();
		if (loc == null) {
			return "in an unknown location nearby"; 
		}
		if (loc.isHyperspace()) {
			return "in hyperspace nearby";
		}
		StarSystemAPI system = (StarSystemAPI) loc;
		
		if (system == Global.getSector().getCurrentLocation()) {
			if (system.isNebula()) {
				return "inside this nebula";
			}
			return "in this system";
		}
		
		return "in the " + system.getNameWithLowercaseType();
	}
	
	public static String getLocationDescription(SectorEntityToken entity) {
		LocationAPI loc = entity.getContainingLocation();
		if (loc == null) {
			return "in an unknown location nearby"; 
		}
		if (loc.isHyperspace()) {
			return "in hyperspace nearby";
		}
		StarSystemAPI system = (StarSystemAPI) loc;
		
		if (system == Global.getSector().getCurrentLocation()) {
			if (system.isNebula()) {
				return "inside this nebula";
			}
			return "in this system";
		}

		if (entity.getConstellation() != null && entity.getConstellation() != Global.getSector().getCurrentLocation().getConstellation()) {
			Constellation c = entity.getConstellation();
			String cText = "in the " + c.getNameWithLowercaseType();
			if (c.getSystems().size() == 1) {
				return "orbiting " + getStarDescription(system.getStar()) + " nearby";
			}
			
			if (system.isNebula()) {
				return "inside a nebula " + cText;
			}
			
			if (system.getTertiary() != null) {
				return "in a trinary star system " + cText;
			}
			
			if (system.getSecondary() != null) {
				return "in a binary star system " + cText;
			}
			
			//if (system.getType() == StarSystemType.SINGLE) {
				return "orbiting " + getStarDescription(system.getStar()) + " " + cText;
		}
		
		if (system.isNebula()) {
			return "inside a nearby nebula";
		}
		
		if (system.getTertiary() != null) {
			return "in a nearby trinary star system";
		}
		
		if (system.getSecondary() != null) {
			return "in a nearby binary star system";
		}
		
		//if (system.getType() == StarSystemType.SINGLE) {
			return "orbiting " + getStarDescription(system.getStar()) + " nearby";
		//}
	}
	
	
	public static String getStarDescription(PlanetAPI star) {
		String type = star.getTypeId();
		
		if (type.equals(StarTypes.BLACK_HOLE)) return "a black hole";
		if (type.equals(StarTypes.NEUTRON_STAR)) return "a neutron star";
		
		if (type.equals(StarTypes.ORANGE) ||
			type.equals(StarTypes.ORANGE_GIANT)) {
			return "an orange star";
		}
		
		if (type.equals(StarTypes.RED_DWARF) ||
			type.equals(StarTypes.RED_SUPERGIANT) ||
				type.equals(StarTypes.RED_GIANT)) {
			return "a red star";
		}
		
		if (type.equals(StarTypes.BLUE_GIANT) ||
				type.equals(StarTypes.BLUE_SUPERGIANT)) {
			return "a blue star";
		}
		
		if (type.equals(StarTypes.BROWN_DWARF) ||
				type.equals(StarTypes.WHITE_DWARF)) {
			return "a dim star";
		}
		
		if (type.equals(StarTypes.YELLOW)) {
			return "a yellow star";
		}
		
		return "a star of unknown type";
	}
	
	
	@Override
	public void optionSelected(String optionText, Object optionData) {
		super.optionSelected(optionText, optionData);
		
		if ("continue".equals(optionData)) {
			setDone(true);
		}
	}

	@Override
	public boolean endWithContinue() {
		return super.endWithContinue();
	}
	
	
}
