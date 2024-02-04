package com.fs.starfarer.api.impl.campaign.intel.group;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.group.FGBlockadeAction.FGBlockadeParams;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class BlockadeFGI extends GenericRaidFGI {

	protected FGBlockadeParams blockadeParams;
	protected IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);
	
	public BlockadeFGI(GenericRaidParams params, FGBlockadeParams blockadeParams) {
		super(null);
		if (params.noun == null) {
			params.noun = "blockade";
		}
		if (params.forcesNoun == null) {
			params.forcesNoun = "blockading forces";
		}
		
		
		this.params = params;
		this.blockadeParams = blockadeParams;
		setRandom(params.random);
		
		initActions();
	}
	
	protected GenericPayloadAction createPayloadAction() {
		return new FGBlockadeAction(blockadeParams, params.payloadDays);
	}
	
	public float getAccessibilityPenalty() {
		int str = getRelativeFGStrength(getTargetSystem());
		if (str < 0) {
			return 0f;
		} else if (str == 0) {
			return blockadeParams.accessibilityPenalty * 0.5f;
		} else {
			return blockadeParams.accessibilityPenalty;
		}
	}
	
	
	public void advance(float amount) {
		super.advance(amount);
		
		if (isEnded() || isEnding() || isAborted() || isSpawning()) return;
		
		float days = Misc.getDays(amount);
		interval.advance(days);
		if (interval.intervalElapsed()) {
			
			if (isCurrent(PAYLOAD_ACTION)) {
				applyBlockadeCondition();
			} else {
				unapplyBlockadeCondition();
			}
			
			periodicUpdate();
		}
	}
	
	protected void periodicUpdate() {
		
	}
	
	protected void applyBlockadeCondition() {
		int str = getRelativeFGStrength(getTargetSystem());
		if (str < 0) {
			unapplyBlockadeCondition();
			return;
		}
		
		for (MarketAPI market : Misc.getMarketsInLocation(getTargetSystem(), blockadeParams.targetFaction)) {
			if (!market.hasCondition(Conditions.BLOCKADED)) {
				market.addCondition(Conditions.BLOCKADED, this);
			}
		}
	}
	
	protected void unapplyBlockadeCondition() {
		for (MarketAPI market : Misc.getMarketsInLocation(getTargetSystem(), blockadeParams.targetFaction)) {
			market.removeCondition(Conditions.BLOCKADED);
		}
	}
	

	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		unapplyBlockadeCondition();
	}

	@Override
	protected void addBasicDescription(TooltipMakerAPI info, float width, float height, float opad) {
		info.addImage(getFaction().getLogo(), width, 128, opad);
		
		StarSystemAPI system = raidAction.getWhere();
		
		String noun = getNoun();
		
		info.addPara(Misc.ucFirst(faction.getPersonNamePrefixAOrAn()) + " %s " + noun + " " + getOfString() + " "
				+ "the " + system.getNameWithLowercaseTypeShort() + ".", opad,
				faction.getBaseUIColor(), faction.getPersonNamePrefix());
	}
	
	protected String getOfString() {
		return "of";
	}
	
	protected void addAssessmentSection(TooltipMakerAPI info, float width, float height, float opad) {
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		FactionAPI faction = getFaction();
		
		List<MarketAPI> targets = params.raidParams.allowedTargets;
		
		String noun = getNoun();
		String forcesNoun = getForcesNoun();
		if (!isEnding() && !isSucceeded() && !isFailed()) {
			
			FactionAPI other = Global.getSector().getFaction(blockadeParams.targetFaction);
			boolean hostile = getFaction().isHostileTo(blockadeParams.targetFaction);
			
			info.addSectionHeading("Assessment", 
							faction.getBaseUIColor(), faction.getDarkUIColor(), Alignment.MID, opad);
			
			boolean started = isCurrent(PAYLOAD_ACTION);
			float remaining = getETAUntil(PAYLOAD_ACTION, true) - getETAUntil(TRAVEL_ACTION, true);
			if (remaining > 0 && remaining < 1) remaining = 1;
			String days = (int)remaining == 1 ? "day" : "days";
			
			if (started) days = "more " + days;
			
			LabelAPI label = info.addPara("The " + noun + " will last for approximately %s " + days
					+ ", causing a %s accessibility penalty "
					+ "for all %s colonies in the " + 
					getTargetSystem().getNameWithLowercaseTypeShort() + ".", opad, h,
					"" + (int) remaining,
					"" + (int) Math.round(blockadeParams.accessibilityPenalty * 100f) + "%",
					other.getPersonNamePrefix());
			label.setHighlight("" + (int) remaining, 
							   "" + (int) Math.round(blockadeParams.accessibilityPenalty * 100f) + "%",
							   other.getPersonNamePrefix());
			label.setHighlightColors(h, h, other.getBaseUIColor());
			
			if (!hostile) {
				info.addPara("The " + forcesNoun + " are not nominally hostile, but will harass shipping and "
						+ "attempt to maintain control over the system's jump-points.", opad, 
						Misc.getHighlightColor(), "not nominally hostile");
			} else {
				info.addPara("The " + forcesNoun + " are actively hostile, but will not directly attack colonies "
						+ "in the system and will instead "
						+ "attempt to maintain control over the system's jump-points. If a defended planet "
						+ "happens to be near a jump-point, however, the situation is apt to get hot very quickly.", 
						opad, Misc.getNegativeHighlightColor(), "actively hostile");
			}
			
			addStrengthDesc(info, opad, getTargetSystem(), forcesNoun, 
					"the " + noun + " is unlikely to be effective",
					"the " + noun + " is likely to only be partially effective",
					"the " + noun + " is likely to be fully effective");
			
			addPostAssessmentSection(info, width, height, opad);
		}
	}
	
	protected void addPostAssessmentSection(TooltipMakerAPI info, float width, float height, float opad) {
		
	}

	public FGBlockadeParams getBlockadeParams() {
		return blockadeParams;
	}
	
}









