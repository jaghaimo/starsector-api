package com.fs.starfarer.api.impl.campaign.missions.hub;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class IntelMarkerIntel extends BaseIntelPlugin {
	
	protected SectorEntityToken loc;
	protected String title;
	protected String icon;
	protected String text;
	protected LinkedHashSet<String> tags = new LinkedHashSet<String>();
	protected FactionAPI faction;

	public IntelMarkerIntel(FactionAPI faction, SectorEntityToken loc,
			String icon, String title, String text,
			Set<String> tags) {
		this.faction = faction;
		this.loc = loc;
		this.icon = icon;
		this.title = title;
		this.text = text;
		this.tags.addAll(tags);
	}

//	@Override
//	public void advance(float amount) {
//		super.advance(amount);
//		if (!isEnded() && !Global.getSector().isPaused()) {
//			endImmediately();
//		}
//	}

	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color c = getTitleColor(mode);
		float pad = 3f;
		float opad = 10f;
		
		c = Misc.getHighlightColor();
		
		info.addPara(title, c, 0f);
		
//		if (subtitle != null) {
//			float initPad = pad;
//			bullet(info);
//			//info.addPara(subtitle, c, initPad);
//			info.addPara(subtitle, g, initPad);
//			unindent(info);
//		}
	}

	public String getSortString() {
		return title;
	}
	
	@Override
	public IntelSortTier getSortTier() {
		return IntelSortTier.TIER_0;
	}

	public String getName() {
		return title;
	}
	
	public String getSmallDescriptionTitle() {
		return getName();
	}
	
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

//		if (image != null) {
//			info.addImage(image, width, 128, opad);
//		}
		if (text != null && !text.isEmpty()) {
			info.addPara(text, tc, opad);
		}
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		//return faction;
		return super.getFactionForUIColors();
	}

	public String getIcon() {
		return icon;
	}
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		//Set<String> tags = super.getIntelTags(map);
		Set<String> tags = new LinkedHashSet<String>(this.tags);
		//tags.add(Tags.INTEL_MISSIONS);
		tags.add(Tags.INTEL_NEW);
		return tags;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return loc;
	}

	@Override
	public List<ArrowData> getArrowData(SectorMapAPI map) {
		//return super.getArrowData(map);
		List<ArrowData> result = new ArrayList<ArrowData>();
		ArrowData d = new ArrowData(Global.getSector().getPlayerFleet(), loc);
		result.add(d);
		return result;
	}
	
	public Color getCircleBorderColorOverride() {
		return Misc.getHighlightColor();
	}
	
}


