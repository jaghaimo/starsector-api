package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTNonASBScanFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTPoints;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HyperspaceTopographyEventIntel;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.DumpMemory;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.util.Misc;

public class PlanetInteractionDialogPluginImpl implements InteractionDialogPlugin {

	public static int STABLE_FUEL_REQ = 500;
	public static int STABLE_MACHINERY_REQ = 200;
	
	
	//public static String BLACK_HOLE_SCANNED = "$blackHoleScanned";
	public static String ADDED_KEY = "$core_starAddedStable";
	
	private static enum OptionId {
		INIT,
		ADD_STABLE_CONFIRM,
		ADD_STABLE_DESCRIBE,
		//SCAN_BlACK_HOLE,
		DUMP_PLANETKILLER,
		DUMP_PLANETKILLER_ON_SECOND_THOUGHT,
		DUMP_PLANETKILLER_CONT_1,
		ADD_STABLE_NEVER_MIND,
		LEAVE,
	}
	
	private InteractionDialogAPI dialog;
	private TextPanelAPI textPanel;
	private OptionPanelAPI options;
	private VisualPanelAPI visual;
	
	private CampaignFleetAPI playerFleet;
	private PlanetAPI planet;
	private boolean unpauseOnExit = true;
	
	public boolean isUnpauseOnExit() {
		return unpauseOnExit;
	}

	public void setUnpauseOnExit(boolean unpauseOnExit) {
		this.unpauseOnExit = unpauseOnExit;
	}


	private static final Color HIGHLIGHT_COLOR = Global.getSettings().getColor("buttonShortcut");
	
	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		
//		dialog.hideVisualPanel();
//		dialog.setTextWidth(700);
		
		textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		playerFleet = Global.getSector().getPlayerFleet();
		planet = (PlanetAPI) dialog.getInteractionTarget();
		
		visual.setVisualFade(0.25f, 0.25f);
		
		if (planet.getCustomInteractionDialogImageVisual() != null) {
			visual.showImageVisual(planet.getCustomInteractionDialogImageVisual());
		} else {
			if (!Global.getSettings().getBoolean("3dPlanetBGInInteractionDialog")) {
				visual.showPlanetInfo(planet);
			}
		}
	
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
		
		if (optionData == DumpMemory.OPTION_ID) {
			Map<String, MemoryAPI> memoryMap = new HashMap<String, MemoryAPI>();
			MemoryAPI memory = dialog.getInteractionTarget().getMemory();
			
			memoryMap.put(MemKeys.LOCAL, memory);
			if (dialog.getInteractionTarget().getFaction() != null) {
				memoryMap.put(MemKeys.FACTION, dialog.getInteractionTarget().getFaction().getMemory());
			} else {
				memoryMap.put(MemKeys.FACTION, Global.getFactory().createMemory());
			}
			memoryMap.put(MemKeys.GLOBAL, Global.getSector().getMemory());
			memoryMap.put(MemKeys.PLAYER, Global.getSector().getCharacterData().getMemory());
			
			if (dialog.getInteractionTarget().getMarket() != null) {
				memoryMap.put(MemKeys.MARKET, dialog.getInteractionTarget().getMarket().getMemory());
			}
			
			new DumpMemory().execute(null, dialog, null, memoryMap);
			
			return;
		} else if (DevMenuOptions.isDevOption(optionData)) {
			DevMenuOptions.execute(dialog, (String) optionData);
			return;
		}
		
		OptionId option = (OptionId) optionData;
		
		if (text != null) {
			//textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
			dialog.addOptionSelectedText(option);
		}
		
		String type = "star";
		String corona = "star's corona";
		String corona2 = "in the star's corona";
		if (planet.getSpec().isBlackHole()) {
			type = "black hole";
			corona = "event horizon";
			corona2 = "near the event horizon";
		}
		
		switch (option) {
		case INIT:
			//boolean scannedAlready = planet.getMemoryWithoutUpdate().getBoolean(BLACK_HOLE_SCANNED);
			boolean didAlready = planet.getMemoryWithoutUpdate().getBoolean(ADDED_KEY);
			addText(getString("approach"));
			
			if (planet.getMemoryWithoutUpdate().getBoolean("$abyssalBlackHoleReadings")) {
				planet.getMemoryWithoutUpdate().unset("$abyssalBlackHoleReadings");
				planet.getMemoryWithoutUpdate().set("$abyssalBlackHoleReadingsRevisit", true);
				
				addText("Your sensors officer hesitates, then calls for your attention. \"Captain, there's a... pattern. "
						+ "From the black hole. Or, rather,\" they pause, looking almost embarrassed. \"-The energy radiated"
						+ " by the accretion disc."
						+ "\n\n"
						+ "They pull a collated sensor output map into the primary holo. \"It's almost a signal. See, if we "
						+ "chart these fluctuations in energy output over time..."
						+ "\n\n"
						+ "You see it now, an orderly series. Not quite a sequence of prime numbers, "
						+ "unless you jig the math. Statistically this is nearly impossible. Possible explanations "
						+ "are as unlikely: a series of planets - large moons? - with specific mass-ratio relationships, "
						+ "all pulled into the accretion disc at just such an angle, like an intentional "
						+ "message... or orderly annihilation of a constructed planetary system at a scale beyond the wildest "
						+ "dreams of the most bloody-minded war-planners of the Domain."
						+ "\n\n"
						+ "It can't be known. The pattern disappears as quickly as it arose.");
				
				int points = HTPoints.ABYSSAL_BLACK_HOLE_UNUSUAL_READINGS;
				if (points > 0) {
					HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(
						new HTNonASBScanFactor("Picked up unusual readings from abyssal black hole", points), dialog);
				}
			} else if (planet.getMemoryWithoutUpdate().getBoolean("$abyssalBlackHoleReadingsRevisit")) {
				addText("Your sensors officer detects no more unusual energy patterns from the inner rim of the accretion disc, just noise,"
						+ " as mindless as the background radiation of the cosmos itself.");
			}
			
			if (didAlready) {
				addText("The " + corona + " exhibits fluctuations indicative of recent antimatter application.");
			}
//			if (scannedAlready) {
//				addText("You've scanned this black hole.");
//			}
			
			Description desc = Global.getSettings().getDescription(planet.getCustomDescriptionId(), Type.CUSTOM);
			if (desc != null && desc.hasText3()) {
				addText(desc.getText3());
			}
			createInitialOptions();
			break;
		case DUMP_PLANETKILLER:
			addText("Your officers respond promptly to the order, and move to the task with all alacrity. There is an edge to their call-and-response,"
					+ " however, as if they cannot help but acknowledge the deep sense of the gravity in this act.\n"
					+ "\"Package ready to drop, captain,\" your ops chief says. \"On your order.\"");
			options.clearOptions();
			options.addOption("\"Destroy it!\"", OptionId.DUMP_PLANETKILLER_CONT_1, null);
			options.addOption("\"No... I will keep it.\"", OptionId.DUMP_PLANETKILLER_ON_SECOND_THOUGHT, null); // Isildur, nooo!!!
			break;
		case DUMP_PLANETKILLER_ON_SECOND_THOUGHT:
			createInitialOptions();
			break;
		case DUMP_PLANETKILLER_CONT_1:
			addText("At your command the planetkiller, locked in its cradle, is boosted toward the very center of the black hole, up and over the plane of the accretion disc.\n\n"
					+ "With a flash only a little more than noise in the sensor telemetry, it is gone."); //, like tears in rain"); - OMG Alex, you're killing me -dgb
			AddRemoveCommodity.addItemLossText(new SpecialItemData(Items.PLANETKILLER, null), 1, dialog.getTextPanel());
			Global.getSector().getPlayerStats().addStoryPoints(1, dialog.getTextPanel(), false);
			removePK();
			options.clearOptions();
			options.addOption("Leave", OptionId.LEAVE, null);
			options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
			break;
//		case SCAN_BlACK_HOLE:
//			planet.getMemoryWithoutUpdate().set(BLACK_HOLE_SCANNED, true);
//			addText("TODO TODO TODO Your sensors officer works quickly, initiating a multi-wave scan of the black hole - or, rather, its event horizon. "
//					+ "A few minutes later, you have the data; "
//					+ "not terribly useful on its own, but gradually reaching a critical mass "
//					+ "when combined with other readings taken elsewhere.");
//			HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(new HTBlackHoleFactor(), dialog);
//			createInitialOptions();
//			break;
		case ADD_STABLE_CONFIRM:
			StarSystemAPI system = planet.getStarSystem();
			if (system != null) {
				
				CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
				cargo.removeFuel(STABLE_FUEL_REQ);
				AddRemoveCommodity.addCommodityLossText(Commodities.FUEL, STABLE_FUEL_REQ, dialog.getTextPanel());
				StarSystemGenerator.addStableLocations(system, 1);
				planet.getMemoryWithoutUpdate().set(ADDED_KEY, true);
				addText("Preparations are made, and you give the go-ahead. " +
						"A few tense minutes later, the chief engineer reports success. " +
						"The resulting stable location won't last for millennia, like " +
						"naturally-occurring ones - but it'll do for your purposes.");
			}
			createInitialOptions();
			break;
		case ADD_STABLE_DESCRIBE:
			addText("The procedure requires spreading prodigious amounts of antimatter " + corona2 + ", " +
					"according to calculations far beyond the ability of anything on the right side of the " +
					"treaty that ended the Second AI War.");
			boolean canAfford = dialog.getTextPanel().addCostPanel("Resources required (available)", 
					Commodities.ALPHA_CORE, 1, false,
					Commodities.HEAVY_MACHINERY, STABLE_MACHINERY_REQ, false,
					Commodities.FUEL, STABLE_FUEL_REQ, true
					);
			
			options.clearOptions();
			
			int num = Misc.getNumStableLocations(planet.getStarSystem());
			boolean alreadyCant = false;
			if (num <= 0) {
				options.addOption("Proceed with the operation", OptionId.ADD_STABLE_CONFIRM, null);
			} else if (num < 2) {
				addText("Normally, this procedure can only be performed in a star system without any " +
						"stable locations. However, your chief engineer suggests an unorthodox workaround.");
				options.addOption("Proceed with the operation", OptionId.ADD_STABLE_CONFIRM, null);
				SetStoryOption.set(dialog, Global.getSettings().getInt("createStableLocation"), 
						OptionId.ADD_STABLE_CONFIRM, "createStableLocation", Sounds.STORY_POINT_SPEND_TECHNOLOGY,
						"Created additional stable location in " + planet.getStarSystem().getNameWithLowercaseType() + "");
			} else {
				alreadyCant = true;
				
				String reason = "This procedure can not performed in a star system that already has " +
								"numerous stable locations.";
				options.addOption("Proceed with the operation", OptionId.ADD_STABLE_CONFIRM, null);
				options.setEnabled(OptionId.ADD_STABLE_CONFIRM, false);
				addText(reason);
				options.setTooltip(OptionId.ADD_STABLE_CONFIRM, reason);
			}
			
			if (!canAfford && !alreadyCant) {
				String reason = "You do not have the necessary resources to carry out this procedure.";
				options.setEnabled(OptionId.ADD_STABLE_CONFIRM, false);
				addText(reason);
				options.setTooltip(OptionId.ADD_STABLE_CONFIRM, reason);
			}
			
			
			options.addOption("Never mind", OptionId.ADD_STABLE_NEVER_MIND, null);
			//createInitialOptions();
			break;
		case ADD_STABLE_NEVER_MIND:
			createInitialOptions();
			break;
		case LEAVE:
			if (unpauseOnExit) {
				Global.getSector().setPaused(false);
			}
			dialog.dismiss();
			break;
		}
	}
	
	
	protected void createInitialOptions() {
		options.clearOptions();
		
		MemoryAPI memory = dialog.getInteractionTarget().getMemory();
		
		String type = "star";
		String corona = "star's corona";
		String corona2 = "in the star's corona";
		boolean blackHole = false;
		if (planet.getSpec().isBlackHole()) {
			blackHole = true;
			type = "black hole";
			corona = "event horizon";
			corona2 = "near the event horizon";
		}
		
		StarSystemAPI system = planet.getStarSystem();
		//boolean scannedAlready = planet.getMemoryWithoutUpdate().getBoolean(BLACK_HOLE_SCANNED);
		boolean didAlready = planet.getMemoryWithoutUpdate().getBoolean(ADDED_KEY);
		boolean deepSpace = system.isDeepSpace();
		if (system != null && planet == system.getStar() && !didAlready && !deepSpace) {
//			int num = Misc.getNumStableLocations(planet.getStarSystem());
			//options.addOption("Induce a resonance cascade in the star's hyperfield, creating a stable location", OptionId.ADD_STABLE_DESCRIBE, null);
			options.addOption("Consider inducing a resonance cascade in the " + type + "'s hyperfield, creating a stable location", OptionId.ADD_STABLE_DESCRIBE, null);
//			SetStoryOption.set(dialog, Global.getSettings().getInt("createStableLocation"), 
//					OptionId.ADD_STABLE, "createStableLocation", Sounds.STORY_POINT_SPEND_TECHNOLOGY);
//			if (num >= 3) {
//				options.setEnabled(OptionId.ADD_STABLE, false);
//				options.setTooltip(OptionId.ADD_STABLE, "This star system can't have any more stable locations.");
//			}
//			if (num >= 0) {
//				options.setEnabled(OptionId.ADD_STABLE, false);
//				options.setTooltip(OptionId.ADD_STABLE, "This procedure can only be performed in star systems " +
//														"without any stable locations.");
//			}
		}
		
//		if (blackHole && !scannedAlready) {
//			options.addOption("Scan the black hole to assess its impact on local hyperspace topography",
//					OptionId.SCAN_BlACK_HOLE, null);
//		}
		
		
		if (hasPK() && blackHole == true) {
			options.addOption("Dump the planetkiller weapon into the black hole", OptionId.DUMP_PLANETKILLER, null);
		}
		
		options.addOption("Leave", OptionId.LEAVE, null);
		options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
		
		if (Global.getSettings().isDevMode()) {
			DevMenuOptions.addOptions(dialog);
		}
	}
	
	public void removePK() {
		Global.getSector().getPlayerFleet().getCargo().
				removeItems(CargoItemType.SPECIAL, new SpecialItemData(Items.PLANETKILLER, null), 1);
	}
	public boolean hasPK() {
		return Global.getSector().getPlayerFleet().getCargo().
				getQuantity(CargoItemType.SPECIAL, new SpecialItemData(Items.PLANETKILLER, null)) > 0;
	}
	
	
	private OptionId lastOptionMousedOver = null;
	public void optionMousedOver(String optionText, Object optionData) {

	}
	
	public void advance(float amount) {
		
	}
	
	private void addText(String text) {
		textPanel.addParagraph(text);
	}
	
	private void appendText(String text) {
		textPanel.appendToLastParagraph(" " + text);
	}
	
	private String getString(String id) {
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



