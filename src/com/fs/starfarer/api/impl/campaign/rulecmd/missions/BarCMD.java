package com.fs.starfarer.api.impl.campaign.rulecmd.missions;

import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.DevMenuOptions;
import com.fs.starfarer.api.impl.campaign.intel.bar.BarEventDialogPlugin;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.DumpMemory;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.ShowDefaultVisual;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	BarCMD
 */
public class BarCMD extends BaseCommandPlugin implements InteractionDialogPlugin {

	protected SectorEntityToken entity;
	protected InteractionDialogPlugin originalPlugin;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;

	public static class BarAmbiencePlayer implements EveryFrameScript {
		public MarketAPI market;
		public String soundId = "bar_ambience";
		public float pitch = 1f;
		public float volume = 1f;
		public float musicSuppression = 0.95f;
		public boolean done = false;
		
		public FaderUtil fader = new FaderUtil(0f, 0.5f);
		public BarAmbiencePlayer(MarketAPI market) {
			this.market = market;
			if (market.getFaction() != null) {
				soundId = market.getFaction().getBarSound();
			}
			fader.fadeIn();
		}
		public void advance(float amount) {
			fader.advance(amount);
			Global.getSector().getCampaignUI().suppressMusic(fader.getBrightness() * musicSuppression);
			Global.getSoundPlayer().playUILoop(soundId, pitch, volume * fader.getBrightness());
		}
		public boolean isDone() {
			return done;
		}
		public boolean runWhilePaused() {
			return true;
		}
		public void stop() {
			done = true;
		}
	}
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		if (dialog == null) return false;

		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		entity = dialog.getInteractionTarget();
		originalPlugin = dialog.getPlugin();
		
		//FireBest.fire(null, dialog, memoryMap, "SalvageSpecialFinishedNoContinue");
		
		if (command.equals("showOptions")) {
			showOptions(false);
			if (!Global.getSector().hasScript(BarAmbiencePlayer.class)) {
				Global.getSector().addScript(new BarAmbiencePlayer(entity.getMarket()));
			}
		}
		
		return true;
	}
	
	public void showOptions(boolean returningFromEvent) {
		dialog.getVisualPanel().restoreSavedVisual();
		dialog.getVisualPanel().saveCurrentVisual();
		
		PortsideBarData data = PortsideBarData.getInstance();
		
		MarketAPI market = entity.getMarket();
		
		dialog.getOptionPanel().clearOptions();
		
		int max = Global.getSettings().getInt("maxBarEvents");
		int curr = 0;
		boolean addedSomething = false;
		for (PortsideBarEvent event : data.getEvents()) {
			if (TutorialMissionIntel.isTutorialInProgress()) continue;
			
			if (event.shouldRemoveEvent()) continue;
			if (!event.shouldShowAtMarket(market)) continue;
			
			event.addPromptAndOption(dialog);
			event.wasShownAtMarket(market);
			
			addedSomething = true;
			
			curr++;
			if (curr >= max) break;
		}
		
		if (returningFromEvent) {
			if (!addedSomething) {
				dialog.getTextPanel().addPara("Nothing of note is going on at the bar.");
			} else {
				dialog.getTextPanel().addPara("You unobtrusively watch the patrons of the bar for " +
											  "a few minutes before deciding what to do next.");
			}
		}
		
		dialog.getOptionPanel().addOption("Leave the bar", "barLeave");
		dialog.getOptionPanel().setShortcut("barLeave", Keyboard.KEY_ESCAPE, false, false, false, true);
		
		
		if (Global.getSettings().isDevMode()) {
			DevMenuOptions.addOptions(dialog);
		}
		
		dialog.setPlugin(this);
		init(dialog);
	}

	public void optionSelected(String optionText, Object optionData) {
		if (optionText != null) {
			dialog.getTextPanel().addParagraph(optionText, Global.getSettings().getColor("buttonText"));
		}
		if (optionData == DumpMemory.OPTION_ID) {
			new DumpMemory().execute(null, dialog, null, getMemoryMap());
			return;
		} else if (DevMenuOptions.isDevOption(optionData)) {
			DevMenuOptions.execute(dialog, (String) optionData);
			return;
		}
		
		if (optionData instanceof PortsideBarEvent) {
			PortsideBarEvent event = (PortsideBarEvent) optionData;
			BarEventDialogPlugin plugin = new BarEventDialogPlugin(this, this, event, memoryMap);
			dialog.setPlugin(plugin);
			plugin.init(dialog);
			return;
		} else if ("barLeave".equals(optionData)) {
			leaveBar();
		} else if ("barContinue".equals(optionData)) {
			showOptions(true);
		}
	}
	
	public void returningFromEvent(PortsideBarEvent event) {
		if (event.endWithContinue()) {
			dialog.getOptionPanel().clearOptions();
			dialog.getOptionPanel().addOption("Continue", "barContinue");
		} else {
			showOptions(true);
		}
	}
	
	
	public static BarAmbiencePlayer getAmbiencePlayer() {
		for (EveryFrameScript script : Global.getSector().getScripts()) {
			if (script instanceof BarAmbiencePlayer) {
				return (BarAmbiencePlayer) script;
			}
		}
		return null;		
	}
	
	public void leaveBar() {
		
		BarAmbiencePlayer player = getAmbiencePlayer();
		if (player != null) {
			player.stop();
		}
		
		dialog.setPlugin(originalPlugin);
		
		new ShowDefaultVisual().execute(null, dialog, Misc.tokenize(""), memoryMap);
		FireBest.fire(null, dialog, memoryMap, "ReturnFromBar");
	}
	
	public void advance(float amount) {
	}
	public void backFromEngagement(EngagementResultAPI battleResult) {
	}
	public Object getContext() {
		return null;
	}
	public Map<String, MemoryAPI> getMemoryMap() {
		return memoryMap;
	}
	public void optionMousedOver(String optionText, Object optionData) {
	}
	
	public void init(InteractionDialogAPI dialog) {
	}
	
}


















