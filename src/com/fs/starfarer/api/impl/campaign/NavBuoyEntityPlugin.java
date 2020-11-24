package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class NavBuoyEntityPlugin extends BaseCampaignObjectivePlugin {

	public static float NAV_BONUS = 3f;
	public static float NAV_BONUS_MAKESHIFT = 2f;
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		readResolve();
	}
	
	Object readResolve() {
		return this;
	}
	
	public void advance(float amount) {
		if (entity.getContainingLocation() == null || entity.isInHyperspace()) return;
		
		String id = getModId();
		for (CampaignFleetAPI fleet : entity.getContainingLocation().getFleets()) {
			if (fleet.isInHyperspaceTransition()) continue;
			
			if (fleet.getFaction() == entity.getFaction() || (isHacked() && fleet.getFaction().isPlayerFaction())) {
				
				String desc = "Nav buoy";
				float bonus = NAV_BONUS;
				if (isMakeshift()) {
					desc = "Makeshift nav buoy";
					bonus = NAV_BONUS_MAKESHIFT;
				}
				
				StatMod curr = fleet.getStats().getFleetwideMaxBurnMod().getFlatBonus(id);
				if (curr == null || curr.value <= bonus) {
					fleet.getStats().addTemporaryModFlat(0.1f, id,
							desc, bonus, 
							fleet.getStats().getFleetwideMaxBurnMod());
				}
			}
		}
		
	}
	
	protected boolean isMakeshift() {
		return entity.hasTag(Tags.MAKESHIFT);
	}
	
	public void printEffect(TooltipMakerAPI text, float pad) {
		int bonus = (int) NAV_BONUS;
		if (isMakeshift()) {
			bonus = (int) NAV_BONUS_MAKESHIFT;
		}
		text.addPara(BaseIntelPlugin.INDENT + "%s burn level for all same-faction fleets in system",
				pad, Misc.getHighlightColor(), "+" + bonus);
	}
	
	public void printNonFunctionalAndHackDescription(TextPanelAPI text) {
		if (entity.getMemoryWithoutUpdate().getBoolean(MemFlags.OBJECTIVE_NON_FUNCTIONAL)) {
			text.addPara("This one, however, does not appear to be transmitting a navigation telemetry broadcast. The cause of its lack of function is unknown.");
		}
		if (isHacked()) {
			text.addPara("You have a hack running on this sensor array.");
		}
	}
	
	

	@Override
	public void addHackStatusToTooltip(TooltipMakerAPI text, float pad) {
		int bonus = (int) NAV_BONUS;
		if (isMakeshift()) {
			bonus = (int) NAV_BONUS_MAKESHIFT;
		}
		text.addPara("%s burn level for in-system fleets",
				pad, Misc.getHighlightColor(), "+" + bonus);
		
		super.addHackStatusToTooltip(text, pad);
	}

	protected String getModId() {
		return "nav_buoy";
	}
	
	
}



