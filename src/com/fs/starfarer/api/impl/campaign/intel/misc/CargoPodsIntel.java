package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.CargoPodsEntityPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class CargoPodsIntel extends FleetLogIntel {
	
	public static String PODS_UPDATE = "pods_update";
	
	protected SectorEntityToken pods;
	
	public CargoPodsIntel(SectorEntityToken pods) {
		this.pods = pods;
		setRemoveTrigger(pods);
	}

	public SectorEntityToken getPods() {
		return pods;
	}

	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		bullet(info);
		CargoPodsEntityPlugin plugin = (CargoPodsEntityPlugin) pods.getCustomPlugin();
		addDays(info, "left until orbit degrades", plugin.getDaysLeft(), tc, initPad);
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
		
		info.addPara("A clutch of cargo pods in a temporarily stable orbit. Once the orbit degrades, " +
					 "the pods will be lost.", opad);	
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
		
		
		CargoAPI cargo = pods.getCargo();
		if (cargo != null && !cargo.getStacksCopy().isEmpty()) {
			info.addSectionHeading("Cargo", Alignment.MID, opad);
			info.showCargo(cargo, 20, true, opad);
		}

		addLogTimestamp(info, tc, opad);
		
		addDeleteButton(info, width);
	}

	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "stabilized_pods");
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		return tags;
	}

	public String getSortString() {
		//return "Stabilized Cargo Pods";
		return super.getSortString();
	}

	public String getName() {
		return "Stabilized Cargo Pods";
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return pods;
	}

}
