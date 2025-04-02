package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.JumpPointInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class WormholeIntel extends MapMarkerIntel {

	public WormholeIntel(SectorEntityToken entity, TextPanelAPI textPanel, boolean deployed) {
		
		//String icon = Global.getSettings().getSpriteName("intel", "wormhole");
		String title = entity.getName();
		
		String text = null;
//		if (entity.getStarSystem() != null) {
//			if (entity.getStarSystem().isDeepSpace()) {
//				text = "Located in deep space";
//			} else {
//				text = "Located in the " + entity.getStarSystem().getNameWithLowercaseTypeShort();
//			}
//		}
		setSound("ui_discovered_entity");
		if (deployed) {
			setDiscoveredPrefixOverride("Deployed: ");
		}
		setWithDeleteButton(false);
		//setWithTimestamp(false);
		
		init(entity, title, text, null, true, textPanel);
	}
	
	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "wormhole");
	}



	@Override
	protected boolean withTextInDesc() {
		return true;
	}
	
	@Override
	protected boolean withCustomVisual() {
		return true;
	}

	@Override
	protected boolean withCustomDescription() {
		return true;
	}
	
	@Override
	protected void addCustomVisual(TooltipMakerAPI info, float width, float height) {
		info.addImage(Global.getSettings().getSpriteName("illustrations", "jump_point_wormhole"), width, 10f);
	}

	@Override
	protected void addCustomDescription(TooltipMakerAPI info, float width, float height) {
		//setWithTimestamp(true);
		float opad = 10f;
		info.addPara("Wormholes can be used for rapid transit between termini located many "
				   + "light-years apart.", opad);
	}
	
	@Override
	protected void addExtraBulletPoints(TooltipMakerAPI info, Color tc, float initPad, ListInfoMode mode) {
		if (!(entity instanceof JumpPointAPI)) return;
		
		JumpPointAPI jumpPoint = (JumpPointAPI) entity;
		float dur = jumpPoint.getMemoryWithoutUpdate().getExpire(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY);
		if (dur > 0) {
			String durStr = "" + (int) dur;
			String days = "days";
			if ((int)dur == 1) {
				days = "day";
			}
			if ((int)dur <= 0) {
				days = "day";
				durStr = "1";
			}
			info.addPara("%s " + days + " until stabilized", initPad, tc, Misc.getHighlightColor(), durStr);
		}
	}

	@Override
	protected void addPostDescriptionSection(TooltipMakerAPI info, float width, float height, float opad) {
		
	}

	public static WormholeIntel getWormholeIntel(SectorEntityToken entity) {
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(WormholeIntel.class)) {
			if (((WormholeIntel)intel).getEntity() == entity) return (WormholeIntel)intel;
		}
		return null;
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		//tags.remove(Tags.INTEL_FLEET_LOG);
		tags.add(Tags.INTEL_GATES);
		return tags;
	}

	@Override
	public String getSortString() {
		if (getTagsForSort().contains(Tags.INTEL_FLEET_LOG) || getTagsForSort().contains(Tags.INTEL_EXPLORATION)) {
			return super.getSortString();
		}
		//return super.getSortString();
		return "AAAA";
	}
	
	@Override
	public List<ArrowData> getArrowData(SectorMapAPI map) {

		if (!(entity instanceof JumpPointAPI)) return null;
		
		JumpPointAPI jp = (JumpPointAPI) entity;
		
		List<ArrowData> result = new ArrayList<ArrowData>();
		for (JumpDestination dest : jp.getDestinations()) {
			SectorEntityToken target = dest.getDestination();
			if (getWormholeIntel(target) == null) continue;
			
			ArrowData arrow = new ArrowData(entity, target);
			arrow.color = new Color(255,75,255,255);
			arrow.width = 7f;
			result.add(arrow);
		}
		
		return result;
	}

	
}




