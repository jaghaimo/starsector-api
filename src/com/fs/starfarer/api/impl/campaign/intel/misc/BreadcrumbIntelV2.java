package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BreadcrumbIntelV2 extends FleetLogIntel {

	protected SectorEntityToken target;
	
	protected String title;
	protected String text;
	
	protected Boolean showSpecificEntity = null;
	
	public BreadcrumbIntelV2(SectorEntityToken target) {
		this.target = target;
		setRemoveTrigger(target);
	}
	
	@Override
	public void reportRemovedIntel() {
		super.reportRemovedIntel();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
	}

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		info.addImage(Global.getSettings().getSpriteName("illustrations", "space_wreckage"), width, opad);
		
		info.addPara(text, opad);
		
		//target.getOrbit().updateLocation();
		
//		float days = getDaysSincePlayerVisible();
//		if (days >= 1) {
//			addDays(info, "ago.", days, tc, opad);
//		}
		addLogTimestamp(info, tc, opad);
		
//		LuddicPathBaseIntel base = new LuddicPathBaseIntel((StarSystemAPI) Global.getSector().getCurrentLocation(), Factions.LUDDIC_PATH);
//		Global.getSector().addScript(base);
//		MemoryAPI mem = base.getEntity().getMemoryWithoutUpdate();
//		mem.set("$blah", t
		
		addDeleteButton(info, width);
	}

	@Override
	public String getIcon() {
		return super.getIcon();
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.remove(Tags.INTEL_FLEET_LOG);
		tags.add(Tags.INTEL_EXPLORATION);
		return tags;
	}

	public String getSortString() {
		//return "Location";
		return super.getSortString();
	}

	public String getName() {
		return title;
	}

	@Override
	public FactionAPI getFactionForUIColors() {
		return super.getFactionForUIColors();
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		if (target.getStarSystem() != null && showSpecificEntity == null) {
			return target.getStarSystem().createToken(0, 0);
			//return target.getStarSystem().getCenter();
		}
		return target;
	}

	@Override
	public boolean shouldRemoveIntel() {
		return super.shouldRemoveIntel();
	}

	@Override
	public String getCommMessageSound() {
		if (sound != null) return sound;
		return getSoundMinorMessage();
	}
	
	public Boolean getShowSpecificEntity() {
		return showSpecificEntity;
	}

	public void setShowSpecificEntity(Boolean showPlanet) {
		if (showPlanet != null && !showPlanet) showPlanet = null;
		
		this.showSpecificEntity = showPlanet;
	}

}
