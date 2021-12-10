package com.fs.starfarer.api.impl.campaign.eventide;

import java.util.Map;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.ui.CustomPanelAPI;

public class DuelDialogDelegate implements CustomVisualDialogDelegate {
	protected DialogCallbacks callbacks;
	protected float endDelay = 2f;
	protected boolean finished = false;
	
	protected String musicId;
	protected DuelPanel duelPanel;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	protected boolean tutorialMode;
	
	
	public DuelDialogDelegate(@Deprecated String musicId, DuelPanel duelPanel, InteractionDialogAPI dialog,
							  Map<String, MemoryAPI> memoryMap, boolean tutorialMode) {
		this.musicId = musicId;
		this.duelPanel = duelPanel;
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		this.tutorialMode = tutorialMode;
	}
	public CustomUIPanelPlugin getCustomPanelPlugin() {
		return duelPanel;
	}
	public void init(CustomPanelAPI panel, DialogCallbacks callbacks) {
		this.callbacks = callbacks;
		callbacks.getPanelFader().setDurationOut(2f);
		duelPanel.init(panel, callbacks, dialog);
//		if (musicId != null && !musicId.isEmpty()) {
//			Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
//			Global.getSoundPlayer().playCustomMusic(1, 1, musicId);
//		} else {
//			Global.getSoundPlayer().pauseMusic();
//			Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
//		}
	}
	public float getNoiseAlpha() {
		return 0;
	}
	public void advance(float amount) {
		if (!finished && 
				(duelPanel.getPlayer().health <= 0 || duelPanel.getEnemy().health <= 0)) {
			endDelay -= amount;
			if (endDelay <= 0f) {
				callbacks.getPanelFader().fadeOut();
				if (callbacks.getPanelFader().isFadedOut()) {
					callbacks.dismissDialog();
					finished = true;
				}
			}
		}
	}
	public void reportDismissed(int option) {
//		Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
//		Global.getSoundPlayer().restartCurrentMusic();
		
		if (memoryMap != null) { // null when called from the test dialog
			if (!tutorialMode) {
				if (duelPanel.getPlayer().health > 0) {
					memoryMap.get(MemKeys.LOCAL).set("$soe_playerWonDuel", true, 0);
				} else {
					memoryMap.get(MemKeys.LOCAL).set("$soe_playerLostDuel", true, 0);
				}
				FireBest.fire(null, dialog, memoryMap, "SOEDuelFinished");
			} else {
				FireBest.fire(null, dialog, memoryMap, "SOETutorialFinished");
			}
		}
	}
}

