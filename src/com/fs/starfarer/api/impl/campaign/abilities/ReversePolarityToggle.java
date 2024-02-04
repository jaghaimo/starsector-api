package com.fs.starfarer.api.impl.campaign.abilities;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ReversePolarityToggle extends ToggleAbilityWithCost {

	public static String REVERSED_POLARITY = "$reversedPolarity";
	public static String POLARITY_SPEED_MULT = "$polaritySpeedMult";
	public static String POLARITY_WIND_GLOW_COLOR_KEY = "$polarityWindGlowColor";
	public static Color POLARITY_WIND_GLOW_COLOR = new Color(1f, 0.25f, 1f, 0.75f);

	public static float SLIPSTREAM_SPEED_MULT = 0.75f;
	
	public static float CR_COST_MULT = 0.25f;
	public static float FUEL_COST_MULT = 1f;
	
	public static float ACTIVATION_DAMAGE_PROB = 0.33f;
	
	@Override
	public float getFuelCostMult(boolean forTooltip) {
		if (!forTooltip && !isFleetInSlipstream()) return 0f;
		return FUEL_COST_MULT;
	}

	@Override
	public float getCRCostMult(boolean forTooltip) {
		if (!forTooltip && !isFleetInSlipstream()) return 0f;
		return CR_COST_MULT;
	}

	@Override
	public boolean canRecoverCRWhileActive(boolean forTooltip) {
		return true;
	}	
	
	@Override
	protected String getActivationText() {
		return "Drive field polarity reversed";
	}
	
	
	@Override
	public boolean isUsable() {
		if (!super.isUsable()) return false;
		if (getFleet() == null) return false;
		
		CampaignFleetAPI fleet = getFleet();
		
		if (!fleet.isInHyperspace()) return false;
		
		return true;
	}

	@Override
	protected void applyStatsEffect(float amount, float level) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		if (fleet.getContainingLocation() == null || !fleet.getContainingLocation().isHyperspace()) {
			deactivate();
			return;
		}
		
		if (level >= 1f && turnedOn) {
			fleet.getMemoryWithoutUpdate().set(REVERSED_POLARITY, true, getDeactivationDays());
			float speedMult = SLIPSTREAM_SPEED_MULT;
			fleet.getMemoryWithoutUpdate().set(POLARITY_SPEED_MULT, speedMult, getDeactivationDays());
			fleet.getMemoryWithoutUpdate().set(POLARITY_WIND_GLOW_COLOR_KEY, POLARITY_WIND_GLOW_COLOR, getDeactivationDays());
		}
		if (level <= 0) {
			cleanupImpl();
		}
	}


	@Override
	protected void applyFleetVisual(float amount, float level) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		Color c = new Color(255,0,255,255);
		Color cDim = new Color(255,0,255,50);
		Color cDim2 = new Color(255,0,255,120);
		for (FleetMemberViewAPI view : fleet.getViews()) {
			//view.getContrailColor().shift(getModId(), view.getEngineColor().getBase(), 1f, 1f, 0.25f);
			view.getContrailColor().shift(getModId(), cDim2, 1f, 1f, .75f);
			view.getEngineGlowColor().shift(getModId(), cDim, 1f, 1f, .5f);
			view.getEngineGlowSizeMult().shift(getModId(), 3f, 1f, 1f, 1f);
			//view.getEngineHeightMult().shift(getModId(), 5f, 1f, 1f, 1f);
			//view.getEngineWidthMult().shift(getModId(), 10f, 1f, 1f, 1f);
		}
	}

	
	public boolean isFleetInSlipstream() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return false;
		return Misc.isInsideSlipstream(fleet);
	}

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		
		String status = " (off)";
		if (turnedOn) {
			status = " (on)";
		}
		
		LabelAPI title = tooltip.addTitle("Reverse Polarity" + status);
		title.highlightLast(status);
		title.setHighlightColor(gray);

		float pad = 10f;
		
		
		tooltip.addPara("Reverse the polarity of the drive field, causing the fleet to travel "
				+ "against the current of slipstreams.", pad);

		if (SLIPSTREAM_SPEED_MULT != 1f) {
			tooltip.addPara("Going against the current is less efficient and results in "
					+ "the slipstream current's effect being reduced by %s.", pad,
					highlight,
					"" + Math.round(100f * (1f - SLIPSTREAM_SPEED_MULT))+ "%"
			);
		}
		
		tooltip.addPara("When used outside a slipstream, incurs no cost, penalty, or risk of ship damage.", pad);
		
		tooltip.addSectionHeading("Use inside slipstreams", Alignment.MID, pad);
		addCostTooltipSection(tooltip, expanded, "An emergency maneuver when performed inside a slipstream, "
				+ "reversing drive field polarity");
		

		addIncompatibleToTooltip(tooltip, expanded);
	}
	

	@Override
	public void addOtherNotUsableReason(TooltipMakerAPI tooltip, boolean expanded) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		Color bad = Misc.getNegativeHighlightColor();
		if (!fleet.isInHyperspace()) {
			tooltip.addPara("Can only be used in hyperspace.", bad, 10f);
		}
	}

	protected boolean showAlarm() {
		if (!isFleetInSlipstream()) return false;
		return super.showAlarm() || isFleetInSlipstream();
	}
	

	@Override
	public Color getCooldownColor() {
		if (showAlarm()) {
			Color color = Misc.getNegativeHighlightColor();
			if (!super.showAlarm()) { // fleet is inside slipstream, but good CR
				color = Misc.getHighlightColor();
			}
			return Misc.scaleAlpha(color, Global.getSector().getCampaignUI().getSharedFader().getBrightness() * 0.5f);
		}
		return super.getCooldownColor();
	}
//	
	
//	public void setFuelUseModifier(CampaignFleetAPI fleet, boolean on) {
//		if (!WITH_FUEL_USE_MULT) return;
//		
//		String id1 = "reverse_polarity_1";
//		
//		MutableStat stat = fleet.getStats().getDynamic().getStat(Stats.FUEL_USE_NOT_SHOWN_ON_MAP_MULT);
//		stat.unmodifyMult(id1);
//		
//		for (StatMod mod : stat.getMultMods().values()) {
//			if (SlipstreamTerrainPlugin2.FUEL_USE_MODIFIER_DESC.equals(mod.desc)) {
//				if (on) {
//					stat.modifyMult(id1, FUEL_USE_MULT, 
//							SlipstreamTerrainPlugin2.FUEL_USE_MODIFIER_DESC + " (reversed polarity)");
//				} else {
//					stat.unmodifyMult(id1);
//				}
//				break;
//			}
//		}
//	}
}





