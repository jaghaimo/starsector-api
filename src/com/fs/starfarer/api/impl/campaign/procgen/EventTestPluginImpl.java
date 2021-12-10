package com.fs.starfarer.api.impl.campaign.procgen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.eventide.DuelDialogDelegate;
import com.fs.starfarer.api.impl.campaign.eventide.DuelPanel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.inspection.HegemonyInspectionManager;
import com.fs.starfarer.api.impl.campaign.intel.punitive.PunitiveExpeditionManager;
import com.fs.starfarer.api.impl.campaign.intel.punitive.PunitiveExpeditionManager.PunExData;
import com.fs.starfarer.api.impl.campaign.plog.PLEntry;
import com.fs.starfarer.api.impl.campaign.plog.PLIntel;
import com.fs.starfarer.api.impl.campaign.plog.PlaythroughLog;
import com.fs.starfarer.api.impl.campaign.population.CoreImmigrationPluginImpl;
import com.fs.starfarer.api.util.Misc;

public class EventTestPluginImpl implements InteractionDialogPlugin {

	protected static enum OptionId {
		INIT,
		PIRATE_RAID,
		PUNITIVE_EXPEDITION,
		INSPECTION,
		PICK_STRENGTH,
		PRINT_LOG,
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
		case PRINT_LOG:
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
		
//		options.addOption("Fight!", OptionId.FIGHT);
//		options.addOption("Fight tutorial", OptionId.TUTORIAL);
		
		MarketAPI market = getNearestMarket(false);
		if (market != null) {
			options.addOption("Send pirate raid to " + market.getContainingLocation().getName(), OptionId.PIRATE_RAID, null);
		}
		options.addOption("Send a punitive expedition", OptionId.PUNITIVE_EXPEDITION);
		options.addOption("Send an AI inspection", OptionId.INSPECTION);
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
}



