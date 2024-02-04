package com.fs.starfarer.api.impl.campaign.intel.group;

import java.util.ArrayList;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class FGBlockadePlanetAction extends FGBlockadeAction {

	protected float successFractionOverride = -1;
	
	public FGBlockadePlanetAction(FGBlockadeParams params, float duration) {
		super(params, duration);
	}

	@Override
	public void computeInitial() {
		computedInitial = true;
		
		primary = params.specificMarket.getPrimaryEntity();
		
		blockadePoints = new ArrayList<SectorEntityToken>();
		blockadePoints.add(primary);
		
		// otherwise, WasSimScript adds extra MilitaryResponseScripts for objectives and
		// attacking fleets go there almost to the exclusion of other targets
		for (SectorEntityToken objective : params.where.getEntitiesWithTag(Tags.OBJECTIVE)) {
			WarSimScript.setNoFightingForObjective(objective, intel.getFaction(), 1000f);
		}
		
//		origDuration = 3f;
//		setDurDays(3f);
	}

	@Override
	public float getSuccessFraction() {
		if (successFractionOverride >= 0) {
			return successFractionOverride;
		}
		return super.getSuccessFraction();
	}

	public float getSuccessFractionOverride() {
		return successFractionOverride;
	}

	public void setSuccessFractionOverride(float successFractionOverride) {
		this.successFractionOverride = successFractionOverride;
	}
	
}



