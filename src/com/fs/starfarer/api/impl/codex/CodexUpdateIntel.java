package com.fs.starfarer.api.impl.codex;

import java.util.LinkedHashSet;
import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.misc.FleetLogIntel;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class CodexUpdateIntel extends FleetLogIntel {

	protected LinkedHashSet<String> unlockedEntries;

	public CodexUpdateIntel(LinkedHashSet<String> unlockedEntries) {
		this.unlockedEntries = unlockedEntries;
		Global.getSector().getIntelManager().addIntel(this);
	}

	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		bullet(info);
		
		if (!unlockedEntries.isEmpty()) {
			String entries = unlockedEntries.size() == 1 ? "entry" : "entries";
			info.addPara("%s new Codex " + entries + "", initPad, tc, h, "" + unlockedEntries.size());
			initPad = 0f;
		}
		unindent(info);
	}

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float small = 3f;
		float opad = 10f;

		if (!unlockedEntries.isEmpty()) {
			info.addPara("New Codex entries available.", opad);
			info.addCodexEntries("New entries", unlockedEntries, true, opad);
			float ew = info.getPrev().getPosition().getWidth();
			float xOff = (int)((width - ew)/2f);
			info.getPrev().getPosition().setXAlignOffset(xOff);
			info.addSpacer(0f).getPosition().setXAlignOffset(-xOff);
		}
		
		addLogTimestamp(info, tc, opad);
		
		addDeleteButton(info, width);
	}

	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "codex_update");
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		return tags;
	}

	public String getName() {
		return "Codex Update";
	}

}
