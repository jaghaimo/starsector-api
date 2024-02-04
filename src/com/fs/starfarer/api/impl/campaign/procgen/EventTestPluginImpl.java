package com.fs.starfarer.api.impl.campaign.procgen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.impl.campaign.AbyssalLightEntityPlugin.AbyssalLightParams;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.eventide.DuelDialogDelegate;
import com.fs.starfarer.api.impl.campaign.eventide.DuelPanel;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTScanFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HyperspaceTopographyEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.inspection.HegemonyInspectionManager;
import com.fs.starfarer.api.impl.campaign.intel.misc.LuddicShrineIntel;
import com.fs.starfarer.api.impl.campaign.intel.punitive.PunitiveExpeditionManager;
import com.fs.starfarer.api.impl.campaign.intel.punitive.PunitiveExpeditionManager.PunExData;
import com.fs.starfarer.api.impl.campaign.plog.PLEntry;
import com.fs.starfarer.api.impl.campaign.plog.PLIntel;
import com.fs.starfarer.api.impl.campaign.plog.PlaythroughLog;
import com.fs.starfarer.api.impl.campaign.population.CoreImmigrationPluginImpl;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;

public class EventTestPluginImpl implements InteractionDialogPlugin {

	protected static enum OptionId {
		INIT,
		PIRATE_RAID,
		PUNITIVE_EXPEDITION,
		INSPECTION,
		PICK_STRENGTH,
		PRINT_LOG,
		TOPOGRAPHY_POINTS,
		HAEI_POINTS,
		ADD_LOG_INTEL,
		INCREASE_COLONY_SIZE,
		FINISH_CONSTRUCTION,
		FIGHT,
		TUTORIAL,
		LEAVE,
	}
	
	protected InteractionDialogAPI dialog;
	protected TextPanelAPI textPanel;
	protected OptionPanelAPI options;
	protected VisualPanelAPI visual;
	
	protected CampaignFleetAPI playerFleet;
	protected PlanetAPI planet;
	
	protected PunExData punExData = null;
	protected boolean sendInspection = false;
	
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
	
		visual.showLargePlanet(Global.getSector().getEntityById("mazalot"));
		
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
		
		if (optionData instanceof Integer) {
			DebugFlags.FAST_RAIDS = true;
			Integer str = (Integer) optionData;
			if (punExData != null) {
				PunitiveExpeditionManager.getInstance().createExpedition(punExData, str);
			} else if (sendInspection) {
				HegemonyInspectionManager.getInstance().createInspection(str);
			}
			optionSelected(null, OptionId.LEAVE);
			return;
		}
		
		if (optionData instanceof PunExData) {
			punExData = (PunExData) optionData;
			optionSelected(null, OptionId.PICK_STRENGTH);
			return;
		}
		
		OptionId option = (OptionId) optionData;
		
		if (text != null) {
			//textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
			dialog.addOptionSelectedText(option);
			//textPanel.addParagraph("");
		}
		
		switch (option) {
		case INIT:
			createInitialOptions();
			
			
			PersonAPI player = Global.getSector().getPlayerPerson();
			MutableCharacterStatsAPI stats = player.getStats();
//			stats.addXP((long) (6000f * (float) Math.random() + 100f), textPanel, true);
//			stats.spendStoryPoints(2, true, textPanel, false, 1f, null);
			
			break;
		case TUTORIAL:
			final DuelPanel duelPanel = DuelPanel.createTutorial(true, "soe_ambience");
			dialog.showCustomVisualDialog(1024, 700, new DuelDialogDelegate(null, duelPanel, dialog, null, true));
			break;
		case FIGHT:
			final DuelPanel duelPanel2 = DuelPanel.createDefault(true, true, "soe_ambience");
			dialog.showCustomVisualDialog(1024, 700, new DuelDialogDelegate("music_soe_fight", duelPanel2, dialog, null, true));
			
			//Global.getSector().getIntelManager().addIntel(new TestFleetGroupIntel());
			
			//new PerseanLeagueBlockade(params, blockadeParams)
			
//			new GensHannanMachinations(dialog);
			
//			dialog.showCustomVisualDialog(1024, 700, new CustomVisualDialogDelegate() {
//				public CustomUIPanelPlugin getCustomPanelPlugin() {
//					return duelPanel2;
//				}
//				public void init(CustomPanelAPI panel, DialogCallbacks callbacks) {
//					duelPanel2.init(panel, callbacks, dialog);
//				}
//				public float getNoiseAlpha() {
//					return 0;
//				}
//				public void advance(float amount) {
//					
//				}
//				public void reportDismissed(int option) {
//				}
//			});
			//dialog.hideTextPanel();
			break;
		case PIRATE_RAID:
			MarketAPI market = getNearestMarket(false);
			PirateBaseIntel base = findPirateBase();
			if (base != null && market != null && market.getStarSystem() != null) {
				base.startRaid(market.getStarSystem(), 500f);
				base.makeKnown(textPanel);
				//print("Attempted to start raid; likely succeeded, see if there's new intel.");
				optionSelected(null, OptionId.LEAVE);
			}
			//addText("")
			break;	
		case INCREASE_COLONY_SIZE:
			market = getNearestMarket(false);
			if (market != null) {
				int was = market.getSize();
				CoreImmigrationPluginImpl plugin = new CoreImmigrationPluginImpl(market);
				plugin.increaseMarketSize();
				textPanel.addPara("Size of " + market.getName() + " increased from " + was + " to " + market.getSize());
			}
			break;
		case FINISH_CONSTRUCTION:
			market = getNearestMarket(false);
			if (market != null) {
				for (Industry curr : new ArrayList<Industry>(market.getIndustries())) {
					if (curr.isBuilding()) {
						curr.finishBuildingOrUpgrading();
						textPanel.addPara("Finished building or upgrading " + curr.getCurrentName());
					}
				}
			}
			break;
		case PUNITIVE_EXPEDITION:
			options.clearOptions();
			for (PunExData data : PunitiveExpeditionManager.getInstance().getData().values()) {
				if (!PunitiveExpeditionManager.getInstance().getExpeditionReasons(data).isEmpty()) {
					options.addOption("Punitive expedition: " + data.faction.getDisplayName(), data);
				}
			}
			options.addOption("Leave", OptionId.LEAVE, null);
			break;
		case INSPECTION:
			sendInspection = true;
			optionSelected(null, OptionId.PICK_STRENGTH);
			break;
		case PICK_STRENGTH:
			textPanel.addPara("Select strength");
			options.clearOptions();
			options.addOption("100", 100);
			options.addOption("200", 200);
			options.addOption("300", 300);
			options.addOption("400", 400);
			options.addOption("500", 500);
			options.addOption("600", 600);
			options.addOption("800", 800);
			options.addOption("1000", 1000);
			options.addOption("Leave", OptionId.LEAVE, null);
			break;
		case TOPOGRAPHY_POINTS:
			HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(
					new HTScanFactor("Dev mode point increase", 50), dialog);
			break;
		case HAEI_POINTS:
			if (HostileActivityEventIntel.get() != null) {
				HostileActivityEventIntel intel = HostileActivityEventIntel.get();
				intel.setRandom(new Random());
				int p = intel.getProgress();
				if (p < 400 || p == 499) p = 400;
				else if (p < 450) p = 450;
				else p = 499;
				intel.setProgress(p);
				textPanel.addPara("Progress set to " + p);
			}
			//HostileActivityEventIntel.get().addFactor(new BaseOneTimeFactor(50), dialog);
			break;
		case PRINT_LOG:
			
			
			for (int i = 0; i < 10; i++) {
				Vector2f loc = Misc.getPointWithinRadius(playerFleet.getLocation(), 10000f);
				if (Misc.getAbyssalDepth(loc) >= 1f) {
					AbyssalLightParams params = new AbyssalLightParams();
					SectorEntityToken e2 = Global.getSector().getHyperspace().addCustomEntity(Misc.genUID(), null, Entities.ABYSSAL_LIGHT, Factions.NEUTRAL, params);
					e2.setLocation(loc.x, loc.y);
				}
			}
			
//			SectorEntityToken e2 = Global.getSector().getHyperspace().addCustomEntity(Misc.genUID(), null, Entities.ABYSSAL_LIGHT, Factions.NEUTRAL);
//			e2.setLocation(playerFleet.getLocation().x, playerFleet.getLocation().y);
			
			
			//new GensHannanMachinations(dialog);
			
//			if (Global.getSector().getCurrentLocation() instanceof StarSystemAPI) {
//				new HostileActivityIntel((StarSystemAPI) Global.getSector().getCurrentLocation());
//			}
			
			//BaseEventIntel event = new BaseEventIntel();
			//HyperspaceTopographyEventIntel event = new HyperspaceTopographyEventIntel(dialog.getTextPanel(), true);
			//Global.getSector().addScript(this);
			//Global.getSector().getIntelManager().addIntel(event);
			//Global.getSector().getListenerManager().addListener(this);
			
			checkFactionUseOfStuff();
			
			textPanel.addPara("Player log:");
			String log = "";
			for (PLEntry e : PlaythroughLog.getInstance().getEntries()) {
				CampaignClockAPI clock = Global.getSector().getClock().createClock(e.getTimestamp());
				log += clock.getShortDate() + " " + e.getText() + "\n";
			}
			textPanel.setFontVictor();
			textPanel.addPara(log);
			textPanel.setFontInsignia();
			
			LocationAPI loc = Global.getSector().getCurrentLocation();
			String tags = "";
			for (String tag : Global.getSector().getCurrentLocation().getTags()) {
				tags += "    " + tag + "\n";
			}
			textPanel.addPara("\nTags for " + loc.getName() + ":\n" + tags);
			
			break;
		case ADD_LOG_INTEL:
			PLIntel intel = new PLIntel();
			Global.getSector().getIntelManager().addIntel(intel, false, textPanel);
			
			LuddicShrineIntel.addShrineIntelIfNeeded("beholder_station", textPanel);
			LuddicShrineIntel.addShrineIntelIfNeeded("chicomoztoc", textPanel);
			LuddicShrineIntel.addShrineIntelIfNeeded("gilead", textPanel);
			LuddicShrineIntel.addShrineIntelIfNeeded("jangala", textPanel);
			LuddicShrineIntel.addShrineIntelIfNeeded("killa", textPanel);
			LuddicShrineIntel.addShrineIntelIfNeeded("volturn", textPanel);
			
//			PromoteOfficerIntel intel = new PromoteOfficerIntel(textPanel);
//			Global.getSector().getIntelManager().addIntel(intel, false, textPanel);
			
//			dialog.showCustomProductionPicker(new BaseCustomProductionPickerDelegateImpl());
			
			//Global.getSector().getIntelManager().addIntel(intel, false, textPanel);
			
//			for (int i = 0; i < 12 * 3; i++) {
//				for (int j = 0; j < 10; j++) {
//					PlaythroughLog.getInstance().reportEconomyTick(i);
//				}
//				PlaythroughLog.getInstance().reportEconomyMonthEnd();
//			}
			break;
		case LEAVE:
			//Global.getSector().setPaused(false);
			dialog.dismiss();
			break;
		}
	}
	
	protected MarketAPI getNearestMarket(boolean playerOnly) {
		MarketAPI nearest = null;
		float minDist = Float.MAX_VALUE;
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		for (MarketAPI curr : Global.getSector().getEconomy().getMarketsCopy()) {
			if (curr.isHidden()) continue;
			if (playerOnly && !curr.isPlayerOwned()) continue;
			
			float dist = Misc.getDistanceLY(pf, curr.getPrimaryEntity());
			boolean nearer = dist < minDist;
			if (dist == minDist && dist == 0 && nearest != null) {
				float d1 = Misc.getDistance(pf, curr.getPrimaryEntity());
				float d2 = Misc.getDistance(pf, nearest.getPrimaryEntity());
				nearer = d1 < d2;
			}
			if (nearer) {
				nearest = curr;
				minDist = dist;
			}
		}
		return nearest;
	}
	
	protected void print(String str) {
		textPanel.appendToLastParagraph("\n" + str);
		System.out.println(str);
	}
	
	protected void createInitialOptions() {
		options.clearOptions();
		
		options.addOption("Fight!", OptionId.FIGHT);
//		options.addOption("Fight tutorial", OptionId.TUTORIAL);
		
		MarketAPI market = getNearestMarket(false);
		if (market != null) {
			options.addOption("Send pirate raid to " + market.getContainingLocation().getName(), OptionId.PIRATE_RAID, null);
		}
		options.addOption("Send a punitive expedition", OptionId.PUNITIVE_EXPEDITION);
		options.addOption("Send an AI inspection", OptionId.INSPECTION);
		options.addOption("Hyperspace Topography +50 points", OptionId.TOPOGRAPHY_POINTS);
		options.addOption("Hostile Activity: reseed RNG and cycle progress through 400/450/499", OptionId.HAEI_POINTS);
		options.addOption("Print player log", OptionId.PRINT_LOG);
		options.addOption("Add player log intel", OptionId.ADD_LOG_INTEL);
		
		if (market != null) {
			options.addOption("Increase size of " + market.getName() + " to " + (market.getSize() + 1), OptionId.INCREASE_COLONY_SIZE);
			options.addOption("Finish construction on " + market.getName(), OptionId.FINISH_CONSTRUCTION);
		}
		
		options.addOption("Leave", OptionId.LEAVE, null);
	}
	
	
	protected OptionId lastOptionMousedOver = null;
	public void optionMousedOver(String optionText, Object optionData) {

	}
	
	public void advance(float amount) {
		
	}
	
	public Object getContext() {
		return null;
	}
	
	public PirateBaseIntel findPirateBase() {
		for (IntelInfoPlugin p : Global.getSector().getIntelManager().getIntel(PirateBaseIntel.class)) {
			PirateBaseIntel intel = (PirateBaseIntel) p;
			if (intel.isEnded() || intel.isEnding()) continue;
			return intel;
		}
		return null;
	}
	
	
	public void checkFactionUseOfStuff() {
		List<FactionAPI> factions = Global.getSector().getAllFactions();
		
		System.out.println();
		System.out.println("----------------------- FIGHTERS -----------------------");
		System.out.println();
		
		Map<String, String> oneFactionFighters = new LinkedHashMap<String, String>();  
		for (FighterWingSpecAPI spec : Global.getSettings().getAllFighterWingSpecs()) {
			if (spec.hasTag(Tags.RESTRICTED)) continue;
			int count = 0;
			String id = spec.getId();
			String fId = null;
			List<String> all = new ArrayList<String>();
			for (FactionAPI f : factions) {
				if (f.isPlayerFaction()) continue;
				if (f.getKnownFighters().contains(id)) {
					count++;
					fId = f.getId();
					all.add(fId);
				}
			}
			if (count == 0) {
				//System.out.println("Fighter wing [" + id + "] has no increased sell frequency anywhere");
				System.out.println("FIGHTER WING [" + id + "] IS NOT USED BY ANY FACTION");
			}
			if (count == 1) {
				oneFactionFighters.put(id, fId);
			}
		
			if (count != 0) {
				System.out.println("Fighter wing [" + id + "] is known by: [" + Misc.getAndJoined(all) + "]");
			}
		}
		
		System.out.println();
		System.out.println("----------------------- WEAPONS -----------------------");
		System.out.println();
		
		for (WeaponSpecAPI spec : Global.getSettings().getAllWeaponSpecs()) {
			if (spec.hasTag(Tags.RESTRICTED)) continue;
			if (spec.hasTag(Tags.NO_SELL)) continue;
			if (spec.getAIHints().contains(AIHints.SYSTEM)) continue;
			String id = spec.getWeaponId();
			int count = 0;
			List<String> all = new ArrayList<String>();
			for (FactionAPI f : factions) {
				if (f.isPlayerFaction()) continue;
				Float p = f.getWeaponSellFrequency().get(id);
				if (p != null && p > 1f) {
					count++;
				}
				if (f.knowsWeapon(id)) {
					all.add(f.getId());
				}
			}
			if (count <= 0) {
				System.out.println("Weapon [" + id + "] is not sold with higher frequency; known by: [" + Misc.getAndJoined(all) + "]");
			}
		}
		
		
		System.out.println();
		System.out.println("----------------------- SHIPS -----------------------");
		System.out.println();
		
		Map<String, String> oneFactionShips = new LinkedHashMap<String, String>();
		for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
			if (spec.hasTag(Tags.RESTRICTED)) continue;
			if (spec.hasTag(Tags.NO_SELL)) continue;
			if (spec.getHullSize() == HullSize.FIGHTER) continue;
			String id = spec.getHullId();
			if (id.endsWith("_default_D")) continue;
			if (id.endsWith("_default_D")) continue;
			if (id.startsWith("module_")) continue;
			int count = 0;
			String fId = null;
			List<String> all = new ArrayList<String>();
			for (FactionAPI f : factions) {
				if (f.isPlayerFaction()) continue;
				if (f.getKnownShips().contains(id)) {
					count++;
					fId = f.getId();
					all.add(fId);
				}
			}
//			if (count <= 0) {
//				System.out.println("SHIP [" + id + "] IS NOT USED BY ANY FACTION");
//			}
			
			if (count == 1) {
				oneFactionShips.put(id, fId);
			}
			
			if (count > 0) {
				System.out.println("Ship [" + id + "] is known by: [" + Misc.getAndJoined(all) + "]");
			}
		}
		
//		System.out.println();
//		
//		for (String id : oneFactionShips.keySet()) {
//			System.out.println("Ship [" + id + "] is only known by [" + oneFactionShips.get(id) + "]");
//		}
	}

}



















