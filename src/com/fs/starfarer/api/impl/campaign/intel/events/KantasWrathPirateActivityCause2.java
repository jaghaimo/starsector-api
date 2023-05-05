package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.rulecmd.KantaCMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Range;

public class KantasWrathPirateActivityCause2 extends BaseHostileActivityCause2 {

	public static float MAX_MAG = 0.3f;
	
	public static float MAX_MAG_FOR_REL = 0.15f;
	public static float MAX_MAG_FOR_BLUFFS = 0.15f;
	public static float MAG_PER_BLUFF = 0.05f;
	
	public KantasWrathPirateActivityCause2(HostileActivityEventIntel intel) {
		super(intel);
	}

	@Override
	public TooltipCreator getTooltip() {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("Warlord Kanta, the feared pirate queen, is not pleased with you. Lesser "
						+ "pirates see attacking your interests as a way to curry favor with her.", 0f);
			}
		};
	}

	public int getProgress() {
		float mag = getMagnitudeContribution(null);
		if (mag <= 0) return 0;
		
		mag /= MAX_MAG;
		if (mag > 1f) mag = 1f;
		
		Range r = new Range("kantasWrathPoints");
		return r.interpInt(mag);
//		int progress = 5 + (int) Math.round(mag * 10f);
//		return progress;
	}
	
	public String getDesc() {
		return "Kanta's wrath";
	}	


	public float getMagnitudeContribution(StarSystemAPI system) {
		if (KantaCMD.playerHasProtection()) return 0f;
		
		PersonAPI kanta = People.getPerson(People.KANTA);
		if (kanta == null) return 0f;
		
		float rep = kanta.getRelToPlayer().getRel();
		
		float mag = -1f * rep * MAX_MAG_FOR_REL;
		
		int bluffs = Global.getSector().getCharacterData().getMemoryWithoutUpdate().getInt(MemFlags.KANTA_BLUFFS);
		
		mag += MAG_PER_BLUFF * bluffs;
		
		if (mag > MAX_MAG) mag = MAX_MAG;

		//mag = 0.6f;
		mag = Math.round(mag * 100f) / 100f;
		return mag;
	}

}
