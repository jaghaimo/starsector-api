package com.fs.starfarer.api.impl.campaign.plog;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class PLIntel extends BaseIntelPlugin {
	//public static Logger log = Global.getLogger(PLIntel.class);
	
	public PLIntel() {
		//Global.getSector().getIntelManager().addIntel(this);
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
		//boolean isUpdate = getListInfoParam() != null;
		
//		info.addPara("Factions: ", tc, initPad);
//		indent(info);
//		LabelAPI label = info.addPara("%s and %s", 0f, tc,
//				 h, one.getDisplayName(), two.getDisplayName());
//		label.setHighlight(one.getDisplayName(), two.getDisplayName());
//		label.setHighlightColors(one.getBaseUIColor(), two.getBaseUIColor());
//		
//		info.addPara(one.getDisplayName(), 0f, tc,
//					 one.getBaseUIColor(), one.getDisplayName());
//		info.addPara(two.getDisplayName(), 0f, tc,
//					 two.getBaseUIColor(), two.getDisplayName());
			
		unindent(info);
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color c = getTitleColor(mode);
		float pad = 3f;
		float opad = 10f;
		
		info.addPara(getName(), c, 0f);
		
		addBulletPoints(info, mode);
	}
	
	public String getSortString() {
		return "History";
	}
	
	public String getName() {
		if (Misc.isPlayerFactionSetUp()) {
			return Global.getSector().getPlayerFaction().getDisplayName() + " - History";
		}
		return Global.getSector().getPlayerPerson().getNameString() + " - History";
		//return "Fleet Log";
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return Global.getSector().getPlayerFaction();
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}
	
	public void createLargeDescription(CustomPanelAPI panel, float width, float height) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		TooltipMakerAPI info = panel.createUIElement(width, height, false);
		panel.addUIElement(info).inTL(0, 0);
		
		// sorry
		info.addPlaythroughDataPanel(width, height);
	}
	
	@Override
	public boolean hasLargeDescription() {
		return true;
	}

	@Override
	public boolean hasSmallDescription() {
		return false;
	}

	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "fleet_log");
	}
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_FLEET_LOG);
		tags.add(Tags.INTEL_STORY);
		return tags;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return null;
	}

}



