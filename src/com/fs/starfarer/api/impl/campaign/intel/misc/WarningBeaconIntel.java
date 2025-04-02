package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator.RemnantSystemType;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class WarningBeaconIntel extends BaseIntelPlugin {

	protected SectorEntityToken beacon;
	
	public WarningBeaconIntel(SectorEntityToken beacon) {
		this.beacon = beacon;
		//Global.getSector().getIntelManager().addIntel(this);
	}
	
	protected RemnantSystemType getRemnantType() {
		RemnantSystemType remnantType = null;
		if (beacon.getMemoryWithoutUpdate().contains(RemnantSystemType.DESTROYED.getBeaconFlag())) {
			remnantType = RemnantSystemType.DESTROYED;
		} else if (beacon.getMemoryWithoutUpdate().contains(RemnantSystemType.SUPPRESSED.getBeaconFlag())) {
			remnantType = RemnantSystemType.SUPPRESSED;
		} else if (beacon.getMemoryWithoutUpdate().contains(RemnantSystemType.RESURGENT.getBeaconFlag())) {
			remnantType = RemnantSystemType.RESURGENT;
		}
		return remnantType;
	}
	
	protected boolean isLow() {
		return beacon.hasTag(Tags.BEACON_LOW);
	}
	protected boolean isMedium() {
		return beacon.hasTag(Tags.BEACON_MEDIUM);
	}
	protected boolean isHigh() {
		return beacon.hasTag(Tags.BEACON_HIGH);
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		boolean isUpdate = getListInfoParam() != null;
		
		String danger = null;
		Color dangerColor = null;
		if (isLow()) {
			danger = "low";
			dangerColor = Misc.getPositiveHighlightColor();
		} else if (isMedium()) {
			danger = "medium";
			dangerColor = h;
		} else if (isHigh()) {
			danger = "high";
			dangerColor = Misc.getNegativeHighlightColor();
		}
		
		if (danger != null) {
			info.addPara("Danger level: " + danger, initPad, tc, dangerColor, danger);
			initPad = 0f;
		}
		
		unindent(info);
	}
	
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
		addBulletPoints(info, mode);
	}
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		
		Description desc = Global.getSettings().getDescription("warning_beacon", Type.CUSTOM);
		info.addPara(desc.getText1FirstPara(), opad);
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
		
		if (beacon.isInHyperspace()) {
			StarSystemAPI system = Misc.getNearbyStarSystem(beacon, 1f);
			if (system != null) {
				info.addPara("This beacon is located near the " + system.getNameWithLowercaseType() + 
						", warning of dangers that presumably lie within.", opad);
				
			}
		}
	}
	
	@Override
	public String getIcon() {
		if (isLow()) {
			return Global.getSettings().getSpriteName("intel", "beacon_low");
		} else if (isMedium()) {
			return Global.getSettings().getSpriteName("intel", "beacon_medium");
		} else if (isHigh()) {
			return Global.getSettings().getSpriteName("intel", "beacon_high");
		}
		return Global.getSettings().getSpriteName("intel", "beacon_low");
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_BEACON);
		
//		if (getRemnantType() != null) {
//			tags.add(Factions.REMNANTS);
//		}
		
		return tags;
	}
	
	public String getSortString() {
		if (isLow()) {
			return "Warning Beacon 3";
		} else if (isMedium()) {
			return "Warning Beacon 2";
		} else if (isHigh()) {
			return "Warning Beacon 1";
		}
		return "Warning Beacon 0";
	}
	
	public String getName() {
//		if (isLow()) {
//			return "Warning Beacon (Low)";
//		} else if (isMedium()) {
//			return "Warning Beacon (Medium)";
//		} else if (isHigh()) {
//			return "Warning Beacon (High)";
//		}
		return "Warning Beacon";
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
		if (beacon.isInHyperspace() && false) {
			StarSystemAPI system = Misc.getNearbyStarSystem(beacon, 1f);
			if (system != null) {
				return system.getHyperspaceAnchor();
			}
		}
		return beacon;
	}
	
	@Override
	public boolean shouldRemoveIntel() {
		return false;
	}

	@Override
	public String getCommMessageSound() {
		return "ui_discovered_entity";
	}
	
	
	
}







