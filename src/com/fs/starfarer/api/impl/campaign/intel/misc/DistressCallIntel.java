package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class DistressCallIntel extends BreadcrumbIntelV2 {

	protected StarSystemAPI system;

	public DistressCallIntel(StarSystemAPI system) {
		super(system.getCenter());
		this.system = system;
		Global.getSector().addScript(this);
		
		//setIcon(Global.getSettings().getSpriteName("intel", "distress_call"));
		setIconId("distress_call");
		setSound("ui_intel_distress_call");
		setDuration(60f);
		setTitle("Distress Call");
		setText("You receive a distress call from the nearby " + system.getNameWithLowercaseType() + ". " + 
				"There's no additional information, but that's not surprising - " +
				"a typical fleet doesn't carry the equipment to broadcast a full-fledged data " +
				"stream into hyperspace.");
	}
	
	@Override
	public void advance(float amount) {
		super.advance(amount);
		if (system == Global.getSector().getCurrentLocation()) {
			endAfterDelay();
		}
	}



	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
		
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		if (system != null) {
			bullet(info);
			info.addPara("Originating in the " + system.getNameWithLowercaseType() + "", tc, initPad);
			unindent(info);
		}
	}

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		info.addPara(text, opad);
		
//		float days = getDaysSincePlayerVisible();
//		if (days >= 1) {
//			addDays(info, "ago.", days, tc, opad);
//		}
		
		if (isEnding()) {
			info.addPara("You've investigated the system this distress call came from.", opad);
		}
		
		addLogTimestamp(info, tc, opad);
		
		addDeleteButton(info, width);
	}

	public String getSortString() {
		//return "Distress Call";
		return super.getSortString();
	}

	public String getName() {
		if (isEnding()) return title + " - Investigated";
		return title;
	}

	@Override
	public boolean shouldRemoveIntel() {
		if (isEnded()) return true;
		return super.shouldRemoveIntel();
	}
	

}
