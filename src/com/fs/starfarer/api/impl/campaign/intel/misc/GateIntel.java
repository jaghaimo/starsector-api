package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class GateIntel extends BaseIntelPlugin {

	protected SectorEntityToken gate;
	
	public GateIntel(SectorEntityToken gate) {
		this.gate = gate;
		//Global.getSector().getIntelManager().addIntel(this);
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		
		boolean active = GateEntityPlugin.isActive(gate);
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		boolean isUpdate = getListInfoParam() != null;
		
//		if (mode == ListInfoMode.INTEL) {
//			info.addPara("Located in the " + gate.getContainingLocation().getNameWithLowercaseTypeShort(), tc, initPad);
//			initPad = 0f;
//		}
		
		if (GateEntityPlugin.isScanned(gate)) {
			info.addPara("Scanned", tc, initPad);
			initPad = 0f;
		}
		
		unindent(info);
	}
	
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		String pre = "";
		String post = "";
		if (mode == ListInfoMode.MESSAGES) {
			pre = "Discovered: ";
		}
		
		if (mode == ListInfoMode.INTEL) {
			post = " - " + gate.getContainingLocation().getNameWithTypeShort();
		}
		
		
		Color c = getTitleColor(mode);
		info.addPara(pre + getName() + post, c, 0f);
		addBulletPoints(info, mode);
	}
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		if (gate.getCustomInteractionDialogImageVisual() != null) {
			info.addImage(gate.getCustomInteractionDialogImageVisual().getSpriteName(), width, opad);
		}
		
		Description desc = Global.getSettings().getDescription(gate.getCustomDescriptionId(), Type.CUSTOM);
		info.addPara(desc.getText1(), opad);
		
		if (GateEntityPlugin.isScanned(gate)) {
			if (GateEntityPlugin.canUseGates()) {
				info.addPara("You've scanned this gate and are able to transit it.", opad);
			} else {
				info.addPara("You've scanned this gate.", opad);
			}
		}
		
		//addBulletPoints(info, ListInfoMode.IN_DESC);
		
	}
	
	protected boolean isActive() {
		return GateEntityPlugin.isActive(gate);
	}
	
	@Override
	public String getIcon() {
		if (isActive()) {
			return Global.getSettings().getSpriteName("intel", "gate_active");
		}
		return Global.getSettings().getSpriteName("intel", "gate_inactive");
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_GATES);
		return tags;
	}
	
	public String getSortString() {
		if (isActive()) {
			return "Active Gate " + gate.getName();
		}
		return "Inactive Gate " + gate.getName();
	}
	
	
	
	public String getName() {
		return gate.getName();
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return super.getFactionForUIColors();
	}

	public String getSmallDescriptionTitle() {
		return getName() + " - " + gate.getContainingLocation().getNameWithTypeShort();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return gate;
	}
	
	@Override
	public boolean shouldRemoveIntel() {
		return false;
	}

	@Override
	public String getCommMessageSound() {
		return "ui_discovered_entity";
	}

	public SectorEntityToken getGate() {
		return gate;
	}
	
	
}







