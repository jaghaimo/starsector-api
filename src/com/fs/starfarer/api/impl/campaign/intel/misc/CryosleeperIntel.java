package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.Cryorevival;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class CryosleeperIntel extends MapMarkerIntel {

	public CryosleeperIntel(SectorEntityToken entity, TextPanelAPI textPanel) {
		
		//String icon = Global.getSettings().getSpriteName("intel", "cryosleeper");
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
		return Global.getSettings().getSpriteName("intel", "cryosleeper");
	}

	@Override
	public FactionAPI getFactionForUIColors() {
		return entity.getFaction();
	}
	
	@Override
	protected boolean withTextInDesc() {
		return false;
	}
	
	@Override
	protected void addPostDescriptionSection(TooltipMakerAPI info, float width, float height, float opad) {
		if (!entity.getMemoryWithoutUpdate().getBoolean("$hasDefenders")) {
			IndustrySpecAPI spec = Global.getSettings().getIndustrySpec(Industries.CRYOREVIVAL);
			info.addPara("Allows colonies within %s light-years to build a %s, "
					+ "greatly increasing their population growth.", 
					opad, Misc.getHighlightColor(), "" + (int)Cryorevival.MAX_BONUS_DIST_LY, spec.getName());
		}
	}

	public static CryosleeperIntel getCryosleeperIntel(SectorEntityToken entity) {
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(CryosleeperIntel.class)) {
			if (((CryosleeperIntel)intel).getEntity() == entity) return (CryosleeperIntel)intel;
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




