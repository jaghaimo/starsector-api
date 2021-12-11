package com.fs.starfarer.api.impl.campaign.rulecmd.missions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.DevMenuOptions;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.intel.bar.BarEventDialogPlugin;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionBarEventWrapper;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.DumpMemory;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.ShowDefaultVisual;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.AddBarEvent;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.AddBarEvent.BarEventData;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 *	BarCMD
 */
public class BarCMD extends BaseCommandPlugin implements InteractionDialogPlugin {

	public static float BAR_EVENT_MIN_TIME_BEFORE_CHANGING = 20f;
	public static float BAR_EVENT_MAX_TIME_BEFORE_CHANGING = 40f;
	
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
			if (!Global.getSector().isPaused()) {
				stop();
			}
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
		
		
		if (command.equals("returnFromEvent")) {
			MemoryAPI mem = getEntityMemory(memoryMap);
			BarCMD cmd = (BarCMD) mem.get("$BarCMD");
			mem.unset("$BarCMD");
			//mem.unset("$currMission_ref");
			
			dialog.setPlugin(cmd);
			abortMissions(null);

			dialog.getInteractionTarget().setActivePerson(null);
			((RuleBasedInteractionDialogPluginImpl)cmd.originalPlugin).updateMemory();
//			memoryMap.put(MemKeys.LOCAL, dialog.getInteractionTarget().getMemoryWithoutUpdate());
//			memoryMap.remove(MemKeys.ENTITY);

			
			boolean withContinue = params.size() > 1 && params.get(1).getBoolean(memoryMap);
			cmd.returningFromEvent(withContinue);
			
			return true;
		} else if (command.equals("accept")) {
			//MemoryAPI mem = getEntityMemory(memoryMap);
			//Object ref = mem.get("$currMission_ref");
			
			String missionId = params.get(1).getString(memoryMap);
			HubMissionBarEventWrapper w = getWrapperFor(missionId);
			if (w != null && w.getMission() != null) {
				BarEventManager.getInstance().notifyWasInteractedWith(w);
				w.getMission().accept(dialog, memoryMap);
			}
			return true;
		} else if (command.equals("leaveBar")) {
			leaveBar();
			return true;
		} else if (command.equals("playAmbience")) {
			entity = dialog.getInteractionTarget();
			if (!Global.getSector().hasTransientScript(BarAmbiencePlayer.class)) {
				Global.getSector().addTransientScript(new BarAmbiencePlayer(entity.getMarket()));
			}
			return true;
		}
		
//		else if (command.equals("addContact")) {
//			ContactIntel.addPotentialContact(contact, text)
//		}
		
		entity = dialog.getInteractionTarget();
		originalPlugin = dialog.getPlugin();
		
		//FireBest.fire(null, dialog, memoryMap, "SalvageSpecialFinishedNoContinue");
		
		if (command.equals("showOptions")) {
			showOptions(false);
			if (!Global.getSector().hasTransientScript(BarAmbiencePlayer.class)) {
				Global.getSector().addTransientScript(new BarAmbiencePlayer(entity.getMarket()));
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
		
		Random random = new Random(BarEventManager.getInstance().getSeed(entity, null, null));
		
		int min = Global.getSettings().getInt("minBarEvents");
		int max = Global.getSettings().getInt("maxBarEvents");
		float pMult = Global.getSettings().getFloat("barEventProbOneMore");
		//int num = min + random.nextInt(max - min + 1);
		
		WeightedRandomPicker<Integer> numPicker = new WeightedRandomPicker<Integer>(random);
		float p = 1f;
		for (int i = min; i <= max; i++) {
			numPicker.add(i, p);
			p *= pMult;
		}
		int num = numPicker.pick();
		
		if (DebugFlags.BAR_DEBUG) {
			max = 8;
			num = 7;
			//num = max;
		}
		
		//num = 1000;
		
		List<PortsideBarEvent> events = new ArrayList<PortsideBarEvent>();
		String key = "$BarCMD_shownEvents";
		MemoryAPI mem = market.getMemoryWithoutUpdate();
		boolean needToSaveShown = false;
		if (mem.contains(key)) {
			List<String> eventIds = (List<String>) mem.get(key);
			for (String id : eventIds) {
				OUTER: for (PortsideBarEvent event : data.getEvents()) {
//					for (GenericBarEventCreator c : BarEventManager.getInstance().getTimeout().getItems()) {
//						if (c.getBarEventId() != null && c.getBarEventId().equals(id)) {
//							continue OUTER;
//						}
//					}
					if (id.equals(event.getBarEventId())) {
						events.add(event);
					}
				}
			}
			num = Math.max(eventIds.size(), num);
		} else {
			events.addAll(data.getEvents());
			Collections.shuffle(events, random);
			needToSaveShown = true;
		}
		
		boolean addedSomething = false;
		
		AddBarEvent.clearTempEvents(market);
		FireAll.fire(null, dialog, memoryMap, "AddBarEvents");
		
		List<BarEventData> temp = new ArrayList<BarEventData>(AddBarEvent.getTempEvents(market).events.values());
		for (int i = 0; i < max && i < temp.size(); i++) {
			BarEventData b = temp.get(i);
			if (!b.blurb.isEmpty()) {
				dialog.getTextPanel().addPara(b.blurb);
			}
			dialog.getOptionPanel().addOption(b.option, b.optionId);
			if (b.optionColor != null) {
				dialog.setOptionColor(b.optionId, b.optionColor);
			}
			addedSomething = true;
		}
		
		List<String> shown = new ArrayList<String>();
		
		// used alongside with ip.excludeFromGetPerson() to ensure that the same person isn't picked
		// for multiple concurrent bar events
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		ip.resetExcludeFromGetPerson();
		
		// existing contacts don't show up at the bar, since the bar encounter dialogue is written
		// assuming you're meeting them for the first time
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(ContactIntel.class)) {
			ip.excludeFromGetPerson(((ContactIntel)intel).getPerson());
		}
		
//		BaseMissionHub.resetMissionAngle(null, market);
//		BaseMissionHub.getMissionAngle(null, market, random);
		
		Collections.sort(events, new Comparator<PortsideBarEvent>() {
			public int compare(PortsideBarEvent o1, PortsideBarEvent o2) {
				boolean p1 = o1.isAlwaysShow();
				boolean p2 = o2.isAlwaysShow();
				if (p1 && !p2) return -1;
				if (p2 && !p1) return 1;
				return 0;
			}
		});
		
		int curr = 0;
		//for (PortsideBarEvent event : data.getEvents()) {
		for (PortsideBarEvent event : events) {
			if (TutorialMissionIntel.isTutorialInProgress()) continue;
			if (curr + temp.size() >= max) break;
			
//			if (curr < 16) {
//				curr++;
//				continue;
//			}
			
			if (event.shouldRemoveEvent()) continue;
			if (!event.shouldShowAtMarket(market)) continue;
			
			event.addPromptAndOption(dialog, memoryMap);
			if (event instanceof HubMissionBarEventWrapper) {
				HubMissionBarEventWrapper w = (HubMissionBarEventWrapper) event;
				if (w.getMission() == null) { // aborted during creation
					continue;
				}
			}
			event.wasShownAtMarket(market);
			
			if (event.getBarEventId() != null) {
				shown.add(event.getBarEventId());
			}
			
			addedSomething = true;
			
			if (!event.isAlwaysShow()) {
				curr++;
			}
			if (curr >= num) break;
		}
		
		//BaseMissionHub.clearCreatedMissionsList(null, market);
		ip.resetExcludeFromGetPerson();
		
		if (needToSaveShown) {
			float time = BAR_EVENT_MIN_TIME_BEFORE_CHANGING + 
						 (BAR_EVENT_MAX_TIME_BEFORE_CHANGING - BAR_EVENT_MIN_TIME_BEFORE_CHANGING) * random.nextFloat();
			mem.set(key, shown, time);
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
			//dialog.getTextPanel().addParagraph(optionText, Global.getSettings().getColor("buttonText"));
			dialog.addOptionSelectedText(optionData);
		}
		if (optionData == DumpMemory.OPTION_ID) {
			new DumpMemory().execute(null, dialog, null, getMemoryMap());
			return;
		} else if (DevMenuOptions.isDevOption(optionData)) {
			DevMenuOptions.execute(dialog, (String) optionData);
			return;
		}
		
		// need to abort any HubMissionBarEventWrapper missions that are *not* the current selection
		String optionId = null;
		if (optionData instanceof String) {
			optionId = (String) optionData;
		}
		
		abortMissions(optionId);
		
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
		} else if (optionData instanceof String) {
			// a HubMissionBarEventWrapper option
//			HubMissionBarEventWrapper w = getWrapperFor(optionId);
//			if (w != null && w.getMission() != null) {
//				mem.set("$currMission_ref", w.getMission(), 0f);
//			} else {
//				mem.unset("$currMission_ref");
//			}
			
			MemoryAPI eMem = getEntityMemory(memoryMap);
			eMem.set("$BarCMD", this, 0f);
			
			HubMissionBarEventWrapper w = getWrapperFor(optionId);
			if (w != null && w.getMission() != null) {
				PersonAPI person = w.getMission().getPerson();
				if (person != null) {
					dialog.getInteractionTarget().setActivePerson(person);
					((RuleBasedInteractionDialogPluginImpl)originalPlugin).updateMemory();
//					memoryMap.put(MemKeys.ENTITY, memoryMap.get(MemKeys.LOCAL));
//					memoryMap.put(MemKeys.LOCAL, person.getMemoryWithoutUpdate());
					dialog.getVisualPanel().showPersonInfo(person, true);
				}
			}
			
			MemoryAPI mem = memoryMap.get(MemKeys.LOCAL);
			dialog.setPlugin(originalPlugin);
			mem.set("$option", (String) optionData, 0f);
			
			FireBest.fire(null, dialog, memoryMap, "DialogOptionSelected");
		}
	}
	
	public void returningFromEvent(PortsideBarEvent event) {
		returningFromEvent(event.endWithContinue());
	}
	public void returningFromEvent(boolean withContinue) {
		if (withContinue) {
			dialog.getOptionPanel().clearOptions();
			dialog.getOptionPanel().addOption("Continue", "barContinue");
		} else {
			showOptions(true);
		}
	}
	
	public HubMissionBarEventWrapper getWrapperFor(String optionId) {
		PortsideBarData data = PortsideBarData.getInstance();
		for (PortsideBarEvent event : data.getEvents()) {
			if (event instanceof HubMissionBarEventWrapper) {
				HubMissionBarEventWrapper w = (HubMissionBarEventWrapper) event;
				if (w.getMission() == null) continue;
				if (optionId != null && optionId.startsWith(w.getMission().getTriggerPrefix())) {
					return w;
				}
			}
		}
		return null;
	}
	public void abortMissions(String optionId) {
		PortsideBarData data = PortsideBarData.getInstance();
		for (PortsideBarEvent event : data.getEvents()) {
			if (event instanceof HubMissionBarEventWrapper) {
				HubMissionBarEventWrapper w = (HubMissionBarEventWrapper) event;
				if (w.getMission() == null) continue;
				if (optionId == null || !optionId.startsWith(w.getMission().getTriggerPrefix())) {
					w.abortMission();
				}
			}
		}
	}
	
	
	public static BarAmbiencePlayer getAmbiencePlayer() {
		for (EveryFrameScript script : Global.getSector().getTransientScripts()) {
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
		
		if (originalPlugin != null) {
			dialog.setPlugin(originalPlugin);
		}
		
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


















