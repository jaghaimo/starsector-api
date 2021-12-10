package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.eventide.DuelDialogDelegate;
import com.fs.starfarer.api.impl.campaign.eventide.DuelPanel;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class SwordOfEventide extends HubMissionWithSearch {

//	public void init(CustomPanelAPI panel, DialogCallbacks callbacks) {
//		this.callbacks = callbacks;
//		callbacks.getPanelFader().setDurationOut(2f);
//		duelPanel.init(panel, callbacks, dialog);
//		if (musicId != null && !musicId.isEmpty()) {
//			Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
//			Global.getSoundPlayer().playCustomMusic(1, 1, musicId);
//		} else {
//			Global.getSoundPlayer().pauseMusic();
//			Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
//		}
//	}
//	public void reportDismissed(int option) {
//		Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
//		Global.getSoundPlayer().restartCurrentMusic();
		
	public static String SOE_MUSIC_STATE = "$soe_musicState";
	public static String SOE_STATE_PARTY = "party";
	public static String SOE_STATE_PRE_DUEL = "pre_duel";
	public static String SOE_STATE_DUEL = "duel";
	public static String SOE_STATE_POST_DUEL = "post_duel";
	
	public static String PARTY_CHATTER = "soe_party_chatter";
	public static String PARTY_MUSIC = "music_soe_party";
	public static String DUEL_MUSIC = "music_soe_fight";
	
	public static class SOEMusicPlayer implements EveryFrameScript {
		public boolean done = false;
		public String currState = null;
		
		public SOEMusicPlayer() {
		}
		public void advance(float amount) {
			String state = Global.getSector().getMemoryWithoutUpdate().getString(SOE_MUSIC_STATE);
			if (state != null && !state.equals(currState)) {
				if (SOE_STATE_PARTY.equals(state)) {
					Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
					Global.getSoundPlayer().playCustomMusic(1, 1, PARTY_MUSIC, true);
				} else if (SOE_STATE_PRE_DUEL.equals(state)) {
					Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
					Global.getSoundPlayer().pauseCustomMusic();
				} else if (SOE_STATE_DUEL.equals(state)) {
					Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
					Global.getSoundPlayer().playCustomMusic(1, 1, DUEL_MUSIC, true);
				} else if (SOE_STATE_POST_DUEL.equals(state)) {
					Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
					Global.getSoundPlayer().pauseCustomMusic();
				}
				currState = state;
			}
			
			if (SOE_STATE_PARTY.equals(state)) {
				Global.getSoundPlayer().playUILoop(PARTY_CHATTER, 1f, 1f);
			} else if (SOE_STATE_PRE_DUEL.equals(state)) {
				Global.getSoundPlayer().playUILoop(PARTY_CHATTER, 1f, 0.5f);
			}
			
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
			Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
			Global.getSoundPlayer().restartCurrentMusic();
		}
	}
	
	
	public static enum Stage {
		GO_TO_EVENTIDE,
		COMPLETED,
	}
	
	protected PersonAPI neriene_rao; // neriene rao
	protected PersonAPI caspian_sang; // caspian sang
	protected PersonAPI orcus_rao; // orcus rao
	
	protected MarketAPI eventide;
	//protected MarketAPI chicomoztoc;
	
	public static float MISSION_DAYS = 120f;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$soe_ref","$soe_inProgress")) {
			return false;
		}
		
		neriene_rao = getImportantPerson(People.NERIENE_RAO);
		if (neriene_rao == null) return false;
		caspian_sang = getImportantPerson(People.CASPIAN);
		if (caspian_sang == null) return false;
		orcus_rao = getImportantPerson(People.RAO);
		if (orcus_rao == null) return false;
		
		eventide = Global.getSector().getEconomy().getMarket("eventide");
		if (eventide == null) return false;
		
		setStartingStage(Stage.GO_TO_EVENTIDE);
		addSuccessStages(Stage.COMPLETED);
		
		setStoryMission();
		
		makeImportant(eventide, "$soe_invitedToBall", Stage.GO_TO_EVENTIDE);
		//setStageOnMemoryFlag(Stage.COMPLETED, baird.getMarket(), "$gaTTB_completed");
		
		setStageOnGlobalFlag(Stage.COMPLETED, "$soe_completed");
		
		setRepFactionChangesNone();
		setRepPersonChangesNone();
		
		beginStageTrigger(Stage.GO_TO_EVENTIDE);
		triggerSetGlobalMemoryValuePermanent("$didEventideRaoBall", true);
		endTrigger();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
	
	}

	protected SOEMusicPlayer player = null;
	@Override
	protected boolean callAction(String action, String ruleId, final InteractionDialogAPI dialog,
								 List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		if ("THEDUEL".equals(action)) {
			TextPanelAPI text = dialog.getTextPanel();
//			text.setFontOrbitronUnnecessarilyLarge();
//			Color color = Misc.getBasePlayerColor();
//			color = Global.getSector().getFaction(Factions.HEGEMONY).getBaseUIColor();
//			text.addPara("THE DUEL", color);
//			text.setFontInsignia();
			text.addImage("misc", "THEDUEL");
			return true;
		} else if ("addMusicPlayer".equals(action)) {
			if (!Global.getSector().hasTransientScript(SOEMusicPlayer.class)) {
				player = new SOEMusicPlayer();
				Global.getSector().addTransientScript(player);
				Global.getSector().getMemoryWithoutUpdate().set(SOE_MUSIC_STATE, SOE_STATE_PARTY);
			}
			return true;
		} else if ("cleanUpMusicPlayer".equals(action)) {
			if (player != null) {
				player.stop();;
			}
			return true;
		} else if ("crowdGasp".equals(action)) {
			Global.getSoundPlayer().playUISound("soe_crowd_gasp", 1f, 1f);
			Global.getSector().getMemoryWithoutUpdate().set(SOE_MUSIC_STATE, SOE_STATE_PRE_DUEL);
			return true;
		} else if ("postDuel".equals(action)) {
			Global.getSector().getMemoryWithoutUpdate().set(SOE_MUSIC_STATE, SOE_STATE_POST_DUEL);
			return true;
		} else if ("beginDuel".equals(action)) {
			Global.getSector().getMemoryWithoutUpdate().set(SOE_MUSIC_STATE, SOE_STATE_DUEL);
			
			boolean playerSkilled = false;
			if (params.size() >= 2) {
				playerSkilled = params.get(1).getBoolean(memoryMap);
			}
			final DuelPanel duelPanel = DuelPanel.createDefault(playerSkilled, true, "soe_ambience");
			dialog.showCustomVisualDialog(1024, 700, new DuelDialogDelegate(null, duelPanel, dialog, memoryMap, false));
			return true;
		} else if ("beginTutorial".equals(action)) {
			boolean playerSkilled = false;
			if (params.size() >= 2) {
				playerSkilled = params.get(1).getBoolean(memoryMap);
			}
			final DuelPanel duelPanel = DuelPanel.createTutorial(playerSkilled, "soe_ambience");
			dialog.showCustomVisualDialog(1024, 700, new DuelDialogDelegate(null, duelPanel, dialog, memoryMap, true));
			return true;
		}
		return super.callAction(action, ruleId, dialog, params, memoryMap);
	}

	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_EVENTIDE) {
			info.addPara("Go to Eventide in the Samarra system.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_EVENTIDE) {
			info.addPara("Go to Eventide in the Samarra system", tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Princess of Persea";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
}





