package com.fs.starfarer.api.impl.campaign.abilities;

import java.awt.Color;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class GoDarkAbility extends BaseToggleAbility {

	public static final float MAX_BURN_MULT = 0.5f;
	public static final float DETECTABILITY_MULT = 0.5f;
	
//	public String getSpriteName() {
//		return Global.getSettings().getSpriteName("abilities", Abilities.GO_DARK);
//	}
	
	
	
	@Override
	protected String getActivationText() {
		return "Going dark";
	}
	
	@Override
	protected String getDeactivationText() {
		//return "Restoring power";
		return null;
	}


	@Override
	protected void activateImpl() {
//		if (entity.isInCurrentLocation()) {
//			entity.addFloatingText("Going dark", entity.getFaction().getBaseUIColor(), 0.5f);
//		}
		
//		AbilityPlugin transponder = entity.getAbility(Abilities.TRANSPONDER);
//		if (transponder != null && transponder.isActive()) {
//			transponder.deactivate();
//		}
//		AbilityPlugin sb = entity.getAbility(Abilities.SUSTAINED_BURN);
//		if (sb != null && sb.isActive()) {
//			sb.deactivate();
//		}
	}

	@Override
	protected void applyEffect(float amount, float level) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		if (level < 1) level = 0;
		
		float d = fleet.getStats().getDynamic().getValue(Stats.GO_DARK_DETECTED_AT_MULT);
		float b = fleet.getStats().getDynamic().getValue(Stats.GO_DARK_BURN_PENALTY_MULT);
		
		fleet.getStats().getDetectedRangeMod().modifyMult(getModId(), 1f + (DETECTABILITY_MULT * d - 1f) * level, "Going dark");
		fleet.getStats().getFleetwideMaxBurnMod().modifyMult(getModId(), 1f + (MAX_BURN_MULT - 1f) * level * b, "Going dark");
		
		for (FleetMemberViewAPI view : fleet.getViews()) {
			view.getContrailColor().shift(getModId(), new Color(0,0,0,0), 1f, 1f, 1f);
			view.getContrailDurMult().shift(getModId(), 0f, 1f, 1f, 1f);
			//view.getContrailWidthMult().shift(getModId(), 0.25f, 1f, 1f, 1f);
			view.getEngineGlowSizeMult().shift(getModId(), 0.5f, 1f, 1f, 1f);
			view.getEngineHeightMult().shift(getModId(), 0.5f, 1f, 1f, 1f);
			//view.getEngineWidthMult().shift(getModId(), 3f, 1f, 1f, 1f);
		}
		
		
//		if (level > 0) {
//			SlipstreamTerrainPlugin slipstream = SlipstreamTerrainPlugin.getSlipstreamPlugin(fleet.getContainingLocation());
//			if (slipstream != null) {
//				slipstream.disrupt(fleet, 0.1f);
//			}
//		}
	}
	
	@Override
	protected void deactivateImpl() {
		cleanupImpl();
	}
	
	@Override
	protected void cleanupImpl() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		fleet.getStats().getDetectedRangeMod().unmodify(getModId());
		fleet.getStats().getFleetwideMaxBurnMod().unmodifyMult(getModId());		
	}
	
//	@Override
//	public float getActivationDays() {
//		return 0.1f;
//	}
//
//	@Override
//	public float getDeactivationDays() {
//		return 0f;
//	}

	@Override
	public boolean showProgressIndicator() {
		return false;
	}
	
	@Override
	public boolean showActiveIndicator() {
		return isActive();
	}

	
	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		
		String status = " (off)";
		if (turnedOn) {
			status = " (on)";
		}
		
		LabelAPI title = tooltip.addTitle(spec.getName() + status);
		title.highlightLast(status);
		title.setHighlightColor(gray);

		float pad = 10f;
		
		
		tooltip.addPara("Turns off all non-essential systems, reducing the range" +
				" at which the fleet can be detected by %s and reducing the maximum burn" +
				" level by %s", pad, 
				highlight,
				"" + (int)((1f - DETECTABILITY_MULT) * 100f) + "%",
				"" + (int)((1f - MAX_BURN_MULT) * 100f) + "%"
		);
		//tooltip.addPara("Disables the transponder when activated.", pad);
		addIncompatibleToTooltip(tooltip, expanded);
	}

	public boolean hasTooltip() {
		return true;
	}
	

}





