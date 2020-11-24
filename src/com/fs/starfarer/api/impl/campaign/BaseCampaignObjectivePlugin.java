package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BaseCampaignObjectivePlugin extends BaseCustomEntityPlugin implements CampaignObjective {

	public static final String HACKED = "$cob_hacked";
	
	public static final float HACK_DURATION_DAYS = 90f;
	
	public void printEffect(TooltipMakerAPI text, float pad) {
		
	}
	
	public void addHackStatusToTooltip(TooltipMakerAPI text, float pad) {
		if (isHacked()) {
			text.addPara("Hacked", Misc.getTextColor(), pad);
		}
	}
	
	public void printNonFunctionalAndHackDescription(TextPanelAPI text) {

	}
	
	
	public Boolean isHacked() {
		return entity != null && entity.getMemoryWithoutUpdate().getBoolean(HACKED);
	}

	public void setHacked(boolean hacked) {
		setHacked(hacked, HACK_DURATION_DAYS + (float) Math.random() * 0.5f * HACK_DURATION_DAYS);
	}
	
	public void setHacked(boolean hacked, float days) {
		if (hacked) {
			entity.getMemoryWithoutUpdate().set(HACKED, hacked, days);
		} else {
			entity.getMemoryWithoutUpdate().unset(HACKED);
		}
	}
	
	

}




