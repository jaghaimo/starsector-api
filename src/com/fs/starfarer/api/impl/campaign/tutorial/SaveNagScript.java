package com.fs.starfarer.api.impl.campaign.tutorial;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.HintPanelAPI;
import com.fs.starfarer.api.util.Misc;

public class SaveNagScript implements EveryFrameScript {

	protected float duration;
	protected boolean playerSaved;
	
	protected Object writeReplace() {
		playerSaved = true;
		HintPanelAPI hints = Global.getSector().getCampaignUI().getHintPanel();
		if (hints != null) {
			hints.clearHints(false);
		}
		return this;
	}
	
	
	public SaveNagScript(float duration) {
		this.duration = duration;
		HintPanelAPI hints = Global.getSector().getCampaignUI().getHintPanel();
		hints.clearHints();
		String control = Global.getSettings().getControlStringForEnumName("QUICK_SAVE");
		hints.setHint(0, "- Press %s to quick-save", true, Misc.getHighlightColor(), control);
	}

	public void advance(float amount) {
		if (Global.getSector().getCampaignUI().isShowingDialog()) return;
		
		duration -= amount;
		if (duration <= 0) {
			HintPanelAPI hints = Global.getSector().getCampaignUI().getHintPanel();
			hints.clearHints();
		}
	}

	public boolean isDone() {
		return duration <= 0;
	}

	public boolean runWhilePaused() {
		return true;
	}

}
