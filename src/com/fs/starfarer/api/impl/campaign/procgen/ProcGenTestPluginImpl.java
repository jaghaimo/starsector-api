package com.fs.starfarer.api.impl.campaign.procgen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.CustomConstellationParams;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext;
import com.fs.starfarer.api.util.Misc;

public class ProcGenTestPluginImpl implements InteractionDialogPlugin {

	protected static enum OptionId {
		INIT,
		GEN_YOUNG,
		GEN_AVERAGE,
		GEN_OLD,
		GEN_CUSTOM,
		GEN_SALVAGE,
		PRINT_STATS,
		LEAVE,
	}
	
	protected InteractionDialogAPI dialog;
	protected TextPanelAPI textPanel;
	protected OptionPanelAPI options;
	protected VisualPanelAPI visual;
	
	protected CampaignFleetAPI playerFleet;
	protected PlanetAPI planet;
	
	protected static final Color HIGHLIGHT_COLOR = Global.getSettings().getColor("buttonShortcut");
	
	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		
		textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		playerFleet = Global.getSector().getPlayerFleet();
		planet = (PlanetAPI) dialog.getInteractionTarget();
		
		visual.setVisualFade(0.25f, 0.25f);
		
		//visual.showImageVisual(planet.getCustomInteractionDialogImageVisual());
	
		dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
		optionSelected(null, OptionId.INIT);
	}
	
	public Map<String, MemoryAPI> getMemoryMap() {
		return null;
	}
	
	public void backFromEngagement(EngagementResultAPI result) {
		// no combat here, so this won't get called
	}
	
	public void optionSelected(String text, Object optionData) {
		if (optionData == null) return;
		
		OptionId option = (OptionId) optionData;
		
		if (text != null) {
			textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
		}
		
		// make >1 for faster stats-gathering
		int genCount = 1;
		
		Constellation constellation = null;
		switch (option) {
		case INIT:
			createInitialOptions();
			break;
		case GEN_YOUNG:
			for (int i = 0; i < genCount; i++) {
				constellation = new StarSystemGenerator(new CustomConstellationParams(StarAge.YOUNG)).generate();
			}
			addText("Generated star system.");
			optionSelected(null, OptionId.LEAVE);
			break;			
		case GEN_AVERAGE:
			for (int i = 0; i < genCount; i++) {
				constellation = new StarSystemGenerator(new CustomConstellationParams(StarAge.AVERAGE)).generate();
			}
			addText("Generated star system.");
			optionSelected(null, OptionId.LEAVE);
			break;			
		case GEN_OLD:
			for (int i = 0; i < genCount; i++) {
				constellation = new StarSystemGenerator(new CustomConstellationParams(StarAge.OLD)).generate();
			}
			addText("Generated star system.");
			optionSelected(null, OptionId.LEAVE);
			break;		
		case GEN_SALVAGE:
			ThemeGenContext context = new ThemeGenContext();
			Set<Constellation> c = new HashSet<Constellation>();
			for (StarSystemAPI system : Global.getSector().getStarSystems()) {
				if (system.getConstellation() == null) continue;
				for (StarSystemAPI curr : system.getConstellation().getSystems()) {
					if (curr.isProcgen()) {
						c.add(system.getConstellation());
						break;
					}
				}
			}
			context.constellations = new ArrayList<Constellation>(c);
			//SectorThemeGenerator.generate(context);
			new RemnantThemeGenerator().generateForSector(context, 1f);
			break;
		case GEN_CUSTOM:
			CustomConstellationParams params = new CustomConstellationParams(StarAge.YOUNG);
			
			params.numStars = 7;
			params.forceNebula = true;
			
			params.systemTypes.add(StarSystemType.TRINARY_2CLOSE);
			params.systemTypes.add(StarSystemType.SINGLE);
			params.systemTypes.add(StarSystemType.TRINARY_1CLOSE_1FAR);
			params.systemTypes.add(StarSystemType.NEBULA);
			params.systemTypes.add(StarSystemType.SINGLE);
			params.systemTypes.add(StarSystemType.BINARY_CLOSE);
			params.systemTypes.add(StarSystemType.BINARY_CLOSE);
			//params.systemTypes.add(StarSystemType.TRINARY_2FAR);
			
			params.starTypes.add("black_hole");
			params.starTypes.add("star_blue_giant");
			params.starTypes.add("star_orange");
			
			
			params.starTypes.add("star_neutron");
			params.starTypes.add("star_neutron");
			params.starTypes.add("star_neutron");
			params.starTypes.add("star_neutron");
			params.starTypes.add("nebula_center_average");
			params.starTypes.add("black_hole");
			params.starTypes.add("black_hole");
			params.starTypes.add("black_hole");
			params.starTypes.add("star_blue_giant");
			//params.starTypes.add("black_hole");
			
			constellation = new StarSystemGenerator(params).generate();
			addText("Generated star system.");
			optionSelected(null, OptionId.LEAVE);
			break;
		case PRINT_STATS:
			printStats();
			break;
		case LEAVE:
			//Global.getSector().setPaused(false);
			dialog.dismiss();
			break;
		}
		
//		if (constellation != null) {
//			DerelictThemeGenerator gen = new DerelictThemeGenerator();
//			for (StarSystemAPI system : constellation.systems) {
//				gen.generateForSystem(system, null);
//			}
//		}
	}
	
	protected void printStats() {
		
		final Map<String, Integer> counts = new HashMap<String, Integer>();
		final Map<String, Integer> hab = new HashMap<String, Integer>();
		
		int totalPlanets = 0;
		int totalSystems = 0;
		int totalHab = 0;
		
		int totalPlanetsInSystemsWithTerran = 0;
		int maxPlanetsInSystemsWithTerran = 0;
		
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (!system.isProcgen()) continue;
			
			String starType = null;
			if (system.getStar() != null) {
				starType = system.getStar().getSpec().getName();
			}
			
			totalSystems++;

			int planets = 0;
			Set<String> seen = new HashSet<String>();
			for (PlanetAPI planet : system.getPlanets()) {
				if (planet.getMarket() == null) continue;
				if (!planet.getMarket().isPlanetConditionMarketOnly()) continue;
				
				//String type = planet.getSpec().getPlanetType();
				String type = planet.getSpec().getName();
				
				seen.add(planet.getSpec().getPlanetType());
				planets++;
				
				Integer count = 0;
				if (counts.containsKey(type)) {
					count = counts.get(type);
				}
				count++;
				counts.put(type, count);
				
				if (planet.getMarket().hasCondition(Conditions.HABITABLE)) {
					totalHab++;
					
					if (starType != null) {
						count = 0;
						if (hab.containsKey(starType)) {
							count = hab.get(starType);
						}
						count++;
						hab.put(starType, count);
						
					}
				}
				
				totalPlanets++;
			}
			
			if (seen.contains(StarTypes.PLANET_TERRAN)) {
				if (planets > maxPlanetsInSystemsWithTerran) {
					maxPlanetsInSystemsWithTerran = planets;
				}
				totalPlanetsInSystemsWithTerran += planets;
			}
		}
		
		List<String> list = new ArrayList<String>(counts.keySet());
		Collections.sort(list, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return counts.get(o2).compareTo(counts.get(o1));
			}
		});
		List<String> habList = new ArrayList<String>(hab.keySet());
		Collections.sort(habList, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return hab.get(o2).compareTo(hab.get(o1));
			}
		});
		
		textPanel.addParagraph("");
		print(String.format("Star systems: %4d", totalSystems));
		print(String.format("Planets:      %4d", totalPlanets));
		print(String.format("Habitable     %4d", totalHab));
		print(String.format("Planets in systems with terran worlds: %4d", totalPlanetsInSystemsWithTerran));
		print(String.format("Max planets in system with terran world: %4d", maxPlanetsInSystemsWithTerran));
		if (totalPlanets > 0) {
			print("Planet totals:");
			for (String type : list) {
				Integer count = counts.get(type);
				String value = Misc.getRoundedValueMaxOneAfterDecimal((count * 100f) / totalPlanets) + "%";
				value += " (" + count + ")";
				print(String.format("  %-20s%10s", type, value));
			}
			print("");
		}
		
		if (totalHab > 0) {
			print("Habitable totals by star:");
			for (String type : habList) {
				Integer count = hab.get(type);
				String value = Misc.getRoundedValueMaxOneAfterDecimal((count * 100f) / totalHab) + "%";
				value += " (" + count + ")";
				print(String.format("  %-20s%10s", type, value));
			}
			print("");
		}
	}
	
	protected void print(String str) {
		textPanel.appendToLastParagraph("\n" + str);
		System.out.println(str);
	}
	
	protected void createInitialOptions() {
		options.clearOptions();
		options.addOption("Generate young constellation", OptionId.GEN_YOUNG, null);
		options.addOption("Generate average constellation", OptionId.GEN_AVERAGE, null);
		options.addOption("Generate old constellation", OptionId.GEN_OLD, null);
		options.addOption("Generate preset constellation", OptionId.GEN_CUSTOM, null);
		options.addOption("Generate salvage entities", OptionId.GEN_SALVAGE, null);
		options.addOption("Print stats", OptionId.PRINT_STATS, null);
		options.addOption("Leave", OptionId.LEAVE, null);
	}
	
	
	protected OptionId lastOptionMousedOver = null;
	public void optionMousedOver(String optionText, Object optionData) {

	}
	
	public void advance(float amount) {
		
	}
	
	protected void addText(String text) {
		textPanel.addParagraph(text);
	}
	
	protected void appendText(String text) {
		textPanel.appendToLastParagraph(" " + text);
	}
	
	protected String getString(String id) {
		String str = Global.getSettings().getString("planetInteractionDialog", id);

		String fleetOrShip = "fleet";
		if (playerFleet.getFleetData().getMembersListCopy().size() == 1) {
			fleetOrShip = "ship";
			if (playerFleet.getFleetData().getMembersListCopy().get(0).isFighterWing()) {
				fleetOrShip = "fighter wing";
			}
		}
		str = str.replaceAll("\\$fleetOrShip", fleetOrShip);
		str = str.replaceAll("\\$planetName", planet.getName());
		
		return str;
	}
	

	public Object getContext() {
		return null;
	}
}



