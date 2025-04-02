package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SimUpdateIntel extends FleetLogIntel {

	protected LinkedHashSet<String> addedFactions;
	protected LinkedHashSet<String> addedVariants;

	public SimUpdateIntel(LinkedHashSet<String> addedFactions, LinkedHashSet<String> addedVariants) {
		this.addedFactions = addedFactions;
		this.addedVariants = addedVariants;
		
		Global.getSector().getIntelManager().addIntel(this);
	}

	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		bullet(info);
		
		if (!addedFactions.isEmpty()) {
			String factions = addedFactions.size() == 1 ? "faction" : "factions";
			info.addPara("%s new " + factions + "", initPad, tc, h, "" + addedFactions.size());
			initPad = 0f;
		}
		if (!addedVariants.isEmpty()) {
			String variants = addedVariants.size() == 1 ? "variant" : "variants";
			info.addPara("%s new hull " + variants + "", initPad, tc, h, "" + addedVariants.size());
			initPad = 0f;
		}
		unindent(info);
	}

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float small = 3f;
		float opad = 10f;

//		info.addPara("New simulator opponents and factions unlocked. New ships "
//				+ "are unlocked by encountering and destroying them in battle.", 0f);
		
		if (!addedFactions.isEmpty()) {
			info.addSpacer(0f);
			info.addSectionHeading("Factions", Alignment.MID, opad);
			info.addPara("New simulator factions available. New factions "
					+ "are are made available by encountering their fleets, or deserters from the faction, in battle.", opad);
			//bullet(info);
			float initPad = opad;
			for (String fid : addedFactions) {
				FactionSpecAPI spec = Global.getSettings().getFactionSpec(fid);
				TooltipMakerAPI para = info.beginImageWithText(spec.getCrest(), 32);
				para.addPara(Misc.ucFirst(spec.getDisplayName()), spec.getBaseUIColor(), 0f);
				//para.getPosition().setXAlignOffset(100f);
				info.addImageWithText(initPad);
				//info.addPara(Misc.ucFirst(spec.getDisplayName()), spec.getBaseUIColor(), initPad);
				initPad = 3f;
			}
			//unindent(info);
		}
		
		if (!addedVariants.isEmpty()) {
			info.addSpacer(0f);
			float extra = 0f;
			if (!addedFactions.isEmpty()) extra = 2f;
			info.addSectionHeading("Ship variants", Alignment.MID, opad + extra);
			info.addPara("New simulator opponents available. New ships "
						+ "are made available by encountering and destroying them in battle.", opad);
			
			List<FleetMemberAPI> members = new ArrayList<FleetMemberAPI>();
			for (String vid : addedVariants) {
				FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, vid);
				members.add(member);
			}
			
			int cols = 7;
			float iconSize = width / cols;
			int rows = members.size() / cols;
			if (rows * cols < members.size()) rows++;
			info.addShipList(cols, rows, iconSize, Misc.getBasePlayerColor(), members, opad);
			
			//info.showShips(members, members.size(), true, opad);
//			bullet(info);
//			float initPad = opad;
//			for (String vid : addedVariants) {
//				ShipVariantAPI v = Global.getSettings().getVariant(vid);
//				if (v == null) continue;
//				info.addPara(v.getFullDesignationWithHullName(), initPad);
//				initPad = 0f;
//			}
//			unindent(info);
		}
		
		addLogTimestamp(info, tc, opad);
		
		addDeleteButton(info, width);
	}

	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "simulator_update");
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		return tags;
	}

	public String getName() {
		return "Simulator Update";
	}

}
