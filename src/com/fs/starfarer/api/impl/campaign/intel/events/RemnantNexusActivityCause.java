package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.MapParams;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;

public class RemnantNexusActivityCause extends BaseHostileActivityCause2 {

	public static float MAX_MAG = 0.5f;
	
	public static int PROGRESS_NEXUS_DAMAGED = Global.getSettings().getInt("remnantNexusPointsDamaged");
	public static int PROGRESS_NEXUS_NORMAL = Global.getSettings().getInt("remnantNexusPointsNormal");
	
	public RemnantNexusActivityCause(HostileActivityEventIntel intel) {
		super(intel);
	}
	
	@Override
	public void addExtraRows(TooltipMakerAPI info, BaseEventIntel intel) {
		Set<CampaignFleetAPI> seen = new LinkedHashSet<CampaignFleetAPI>();
		for (final StarSystemAPI system : Misc.getSystemsWithPlayerColonies(false)) {
			CampaignFleetAPI nexus = RemnantHostileActivityFactor.getRemnantNexus(system);
			if (nexus == null) continue;
			
			if (nexus == null || seen.contains(nexus)) continue;
			
			
			int numColonies = Misc.getMarketsInLocation(system, Factions.PLAYER).size();
			final String colonies = numColonies != 1 ? "colonies" : "colony";
			final String isOrAre = numColonies != 1 ? "are" : "is";
			
			String desc = "Remnant Nexus in the " + system.getNameWithLowercaseTypeShort();

			final int progress = getProgressForNexus(nexus);
			String progressStr = "+" + progress;
			if (progress < 0) progressStr = "" + progress;
			Color descColor = getDescColor(intel);
			Color progressColor = getProgressColor(intel);
			
			info.addRowWithGlow(Alignment.LMID, descColor, "    " + desc,
							    Alignment.RMID, progressColor, progressStr);
			
			TooltipCreator t = new BaseFactorTooltip() {
				@Override
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					float opad = 10f;

					MapParams params = new MapParams();
					params.showSystem(system);

					float w = tooltip.getWidthSoFar();
					float h = Math.round(w / 1.6f);
					params.positionToShowAllMarkersAndSystems(true, Math.min(w, h));
					
					//UIPanelAPI map = tooltip.createSectorMap(w, h, params, aStr + " " + Misc.ucFirst(systems));
					UIPanelAPI map = tooltip.createSectorMap(w, h, params, system.getNameWithLowercaseType());
					
					tooltip.addPara("Your " + colonies + " in the " + system.getNameWithLowercaseTypeShort() + 
							" " + isOrAre + " threatened by a Remnant Nexus located in the system. " +  
							"This results in a greater risk of trade fleets being attacked, and eventual "
							+ "existential danger for your " + colonies + ".", 0f, Misc.getNegativeHighlightColor(), 
							"existential danger for your " + colonies);
					
					tooltip.addPara("Even when relatively dormant, " +
							"the Remnant fleets are a constant thorn in your side, wasting defensive resources and "
							+ "exposing your " + colonies + 
							" to other dangers.", opad);
					
					tooltip.addPara("%s should address all these concerns.", opad,
							Misc.getHighlightColor(), "Destroying the Nexus");
					
					tooltip.addCustom(map, opad);
				}
			};
			info.addTooltipToAddedRow(t, TooltipLocation.RIGHT, false);
		}
	}

	@Override
	public boolean shouldShow() {
		return getProgress() != 0;
	}

	public int getProgress() {
		int total = 0;
		for (final StarSystemAPI system : Misc.getSystemsWithPlayerColonies(false)) {
			CampaignFleetAPI nexus = RemnantHostileActivityFactor.getRemnantNexus(system);
			if (nexus == null) continue;
			total += getProgressForNexus(nexus);
		}
		return total;
	}
	

	protected int getProgressForNexus(CampaignFleetAPI nexus) {
		if (nexus == null) return 0;
		
		boolean damaged = nexus.getMemoryWithoutUpdate().getBoolean("$damagedStation");
		if (damaged) {
			return PROGRESS_NEXUS_DAMAGED;
		}
		return PROGRESS_NEXUS_NORMAL;
	}
	
	
	
	public String getDesc() {
		return null;
	}

	public float getMagnitudeContribution(StarSystemAPI system) {
		if (getProgress() <= 0) return 0f;
		
		CampaignFleetAPI nexus = RemnantHostileActivityFactor.getRemnantNexus(system);
		if (nexus == null) return 0f;
		
		boolean damaged = nexus.getMemoryWithoutUpdate().getBoolean("$damagedStation");
		if (damaged) {
			return MAX_MAG * 0.5f;
		}
		
		return MAX_MAG;
	}
	

}


