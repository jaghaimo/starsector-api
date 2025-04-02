package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class HypershuntIntel extends MapMarkerIntel {

	public HypershuntIntel(SectorEntityToken entity, TextPanelAPI textPanel) {
		
		//String icon = Global.getSettings().getSpriteName("intel", "hypershunt");
		String title = entity.getName();
		
		String text = null;
//		if (entity.getStarSystem() != null) {
//			text = "Located in the " + entity.getStarSystem().getNameWithLowercaseTypeShort();
//		}
		setSound("ui_discovered_entity");
		
		setWithDeleteButton(false);
		//setWithTimestamp(false);
		
		init(entity, title, text, null, true, textPanel);
	}
	
	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "hypershunt");
	}

	@Override
	protected boolean withTextInDesc() {
		return false;
	}
	
	@Override
	protected void addExtraBulletPoints(TooltipMakerAPI info, Color tc, float initPad, ListInfoMode mode) {
		if (entity.getMemoryWithoutUpdate().getBoolean("$usable")) {
			info.addPara("Active", tc, initPad);
		} else {
			info.addPara("Inactive", tc, initPad);
		}
	}
	
	public boolean defendersDefeated() {
		return entity.getMemoryWithoutUpdate().getBoolean("$defenderFleetDefeated");
	}
	
	@Override
	protected void addPostDescriptionSection(TooltipMakerAPI info, float width, float height, float opad) {
		//if (!entity.getMemoryWithoutUpdate().getBoolean("$hasDefenders")) {
		if (defendersDefeated()) {
			SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(Items.CORONAL_PORTAL);
			info.addPara("Allows colonies within %s light-years to build %s additional industry, "
					+ "provided they have a %s installed and have a steady supply of transplutonics.", 
					opad, Misc.getHighlightColor(), 
					"" + (int)ItemEffectsRepo.CORONAL_TAP_LIGHT_YEARS, 
					"" + (int)ItemEffectsRepo.CORONAL_TAP_INDUSTRIES, 
					spec.getName());
			
			if (!entity.getMemoryWithoutUpdate().getBoolean("$usable")) {
				// these must match the quantities specified in the rule "cTap_infoText"
				int crew = 1000;
				int metals = 20000;
				int transplutonics = 5000;
				info.showCost("Resources required to activate", false, (int)((width - opad) / 3f), 
						Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), opad, 
						new String [] {Commodities.CREW, Commodities.METALS, Commodities.RARE_METALS},
						new int [] {crew, metals, transplutonics}, new boolean [] {false, false, false});
			}
		}
	}

	public static HypershuntIntel getHypershuntIntel(SectorEntityToken entity) {
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(HypershuntIntel.class)) {
			if (((HypershuntIntel)intel).getEntity() == entity) return (HypershuntIntel)intel;
		}
		return null;
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		//tags.add(Tags.INTEL_);
		return tags;
	}
	
	
}




