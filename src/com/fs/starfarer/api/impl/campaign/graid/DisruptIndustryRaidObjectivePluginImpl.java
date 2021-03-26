package com.fs.starfarer.api.impl.campaign.graid;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry.IndustryTooltipMode;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class DisruptIndustryRaidObjectivePluginImpl extends BaseGroundRaidObjectivePluginImpl {

	public static float DISRUPTION_DAYS_XP_MULT = 300;
	
	public DisruptIndustryRaidObjectivePluginImpl(MarketAPI market, Industry target) {
		super(market, target.getId());
		setSource(target);
	}
	
	@Override
	public void setSource(Industry source) {
		super.setSource(source);
//		RaidDangerLevel level = getDangerLevel();
//		int marines = level.marineTokens;
//		if (source != null) {
//			marines = source.adjustMarineTokensToRaidItem(id, data, marines); 
//		}
//		setMarinesRequired(marines);
	}
	
	@Override
	public String getQuantityString(int marines) {
		float days = source.getDisruptedDays();
		if (days > 0 && days < 1) days = 1;
		days = Math.round(days);
		if (days > 0) {
			return "" + (int) days;
		}
		return "";
	}
	
	@Override
	public Color getQuantityColor(int marines) {
		//if (getQuantityString(marines).isEmpty()) return Misc.getGrayColor();
		return Misc.getHighlightColor();
	}

	public int getDisruptionDaysSort(int marines) {
		marines = Math.max(1, marines);
		return (int) getBaseDisruptDuration(marines);
	}
	
	public String getDisruptionDaysString(int marines) {
		marines = Math.max(1, marines);
		return "" + (int) getBaseDisruptDuration(marines);
	}

	public float getQuantity(int marines) {
		return 0;
	}
	
	public int getValue(int marines) {
		return 0;
	}
	
	public int getProjectedCreditsValue() {
		return 0;
	}
	
	public IndustrySpecAPI getSpec() {
		return getSource().getSpec();
	}
	
	public RaidDangerLevel getDangerLevel() {
		RaidDangerLevel level = getSpec().getDisruptDanger();
		return level;
	}

	public float getQuantitySortValue() {
		IndustrySpecAPI spec = getSpec();
		float add = spec.getOrder();
		return QUANTITY_SORT_TIER_4 + add + 1000;
	}
	
	public String getName() {
		return getSource().getCurrentName();
	}

	@Override
	public String getIconName() {
		return getSource().getCurrentImage();
	}
	
	protected float addedDisruptionDays = 0f;
	public float getAddedDisruptionDays() {
		return addedDisruptionDays;
	}

	public void setAddedDisruptionDays(float addedDisruptionDays) {
		this.addedDisruptionDays = addedDisruptionDays;
	}

	public int performRaid(CargoAPI loot, Random random, float lootMult, TextPanelAPI text) {
		if (marinesAssigned <= 0) return 0;
		
		float dur = getBaseDisruptDuration(marinesAssigned);
		dur *= lootMult;
		dur *= StarSystemGenerator.getNormalRandom(random, 1f, 1.1f);
		if (dur < 2) dur = 2;
		float already = source.getDisruptedDays();
		source.setDisrupted(already + dur);
		addedDisruptionDays = dur;
		
		text.addPara("The raid was successful in disrupting " + source.getNameForModifier() + " operations." +
				" It will take at least %s days for normal operations to resume.",
				Misc.getHighlightColor(), "" + (int) Math.round(source.getDisruptedDays()));
		
		int xpGained = (int) (dur * DISRUPTION_DAYS_XP_MULT);
		return xpGained;
	}
	
	public float getBaseDisruptDuration(int marines) {
		if (marines <= 0) return 0f;
		float already = source.getDisruptedDays();
		//float dur = marines * Global.getSettings().getFloat("raidDisruptDurationPerMarineToken");
		float dur = marines * source.getSpec().getDisruptDanger().disruptionDays;
		dur *= dur / (dur + already); 
		return dur;
	}
	
	@Override
	public boolean hasTooltip() {
		return true;
	}
	
	@Override
	public float getTooltipWidth() {
		return getSource().getTooltipWidth();
	}

	@Override
	public void createTooltip(TooltipMakerAPI t, boolean expanded) {
		getSource().createTooltip(IndustryTooltipMode.NORMAL, t, expanded);
//		float opad = 10f;
//		float pad = 3f;
//		Color h = Misc.getHighlightColor();
//		Color bad = Misc.getNegativeHighlightColor();
//		Color good = Misc.getPositiveHighlightColor();
//
//		//Description desc = Global.getSettings().getDescription(id, Type.RESOURCE);
//		
//		t.addPara(getItemSpec().getDescFirstPara(), 0f);
//		
//		t.addPara("Base value: %s per unit", opad, h, Misc.getDGSCredits(getItemSpec().getBasePrice()));
	}
}




