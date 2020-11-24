package com.fs.starfarer.api.impl.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * "X new items" or whatnot message. Shown when enough new intel items would be added in a single frame that
 * it would flood the player. 
 * 
 * @author Alex Mosolov
 *
 * Copyright 2018 Fractal Softworks, LLC
 */
public class NewMessagesIntel extends BaseIntelPlugin {
	
	private int num;

	public NewMessagesIntel(int num) {
		this.num = num;
		
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		//info.setParaSmallInsignia();
		info.addPara("New intel (%s)", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "" + num);
		//info.setParaFontDefault();
	}

	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "multipleNew");
	}

	@Override
	public String getCommMessageSound() {
		return super.getCommMessageSound();
	}

	
	
}








