package com.fs.starfarer.api.impl.campaign.abilities;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SensorBurstAbility extends BaseDurationAbility {

	public static final float SENSOR_RANGE_BONUS = 3000f;
	public static final float DETECTABILITY_RANGE_BONUS = 5000f;
	//public static final float ACCELERATION_MULT = 4f;
	
//	public String getSpriteName() {
//		return Global.getSettings().getSpriteName("abilities", Abilities.SENSOR_BURST);
//	}
	
//	@Override
//	protected String getActivationText() {
//		//return "Active sensor burst";
//		return Misc.ucFirst(spec.getName().toLowerCase());
//	}

	@Override
	protected void activateImpl() {
		if (entity.isInCurrentLocation()) {
			//entity.addFloatingText("Active sensor burst", entity.getFaction().getBaseUIColor(), 0.5f);
			
			VisibilityLevel level = entity.getVisibilityLevelToPlayerFleet();
			if (level != VisibilityLevel.NONE) {
				Global.getSector().addPing(entity, Pings.SENSOR_BURST);
			}
		}
		
//		AbilityPlugin goDark = entity.getAbility(Abilities.GO_DARK);
//		if (goDark != null && goDark.isActive()) {
//			goDark.deactivate();
//		}
//		AbilityPlugin sb = entity.getAbility(Abilities.SUSTAINED_BURN);
//		if (sb != null && sb.isActive()) {
//			sb.deactivate();
//		}
	}

	@Override
	protected void applyEffect(float amount, float level) {
//		if (level > 0) {
//			AbilityPlugin goDark = entity.getAbility(Abilities.GO_DARK);
//			if (goDark != null) goDark.forceDisable();
//			AbilityPlugin eb = entity.getAbility(Abilities.EMERGENCY_BURN);
//			if (eb != null) eb.forceDisable();
//			AbilityPlugin sb = entity.getAbility(Abilities.SUSTAINED_BURN);
//			if (sb != null) sb.forceDisable();
//		}
		
		
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
//		if (fleet.isPlayerFleet()) {
//			System.out.println("Level: " + level);
//		}
		
		//float b = fleet.getStats().getDynamic().getValue(Stats.SENSOR_BURST_BURN_PENALTY_MULT);
		
		//fleet.getStats().getFleetwideMaxBurnMod().modifyMult(getModId(), 1f + (0f - 1f * level) * b, "Active sensor burst");
		//fleet.getStats().getFleetwideMaxBurnMod().modifyMult(getModId(), 1f + (0f - 1f * 1f) * b, "Active sensor burst");
		//fleet.getStats().getFleetwideMaxBurnMod().modifyMult(getModId(), 0, "Active sensor burst");
		
		fleet.getStats().getSensorRangeMod().modifyFlat(getModId(), SENSOR_RANGE_BONUS * level, "Active sensor burst");
		fleet.getStats().getDetectedRangeMod().modifyFlat(getModId(), DETECTABILITY_RANGE_BONUS * level, "Active sensor burst");
		
		//fleet.getStats().getAccelerationMult().modifyMult(getModId(), 1f + (ACCELERATION_MULT - 1f) * level);
		
		fleet.goSlowOneFrame();
	}

	@Override
	protected void deactivateImpl() {
		cleanupImpl();
	}
	
	@Override
	protected void cleanupImpl() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		fleet.getStats().getSensorRangeMod().unmodify(getModId());
		fleet.getStats().getDetectedRangeMod().unmodify(getModId());
		//fleet.getStats().getFleetwideMaxBurnMod().unmodify(getModId());
		//fleet.getStats().getAccelerationMult().unmodify(getModId());
	}
	
//	@Override
//	public float getActivationDays() {
//		return 0.2f;
//	}
//
//	@Override
//	public float getCooldownDays() {
//		return 1f;
//	}
//
//	@Override
//	public float getDeactivationDays() {
//		return 0.2f;
//	}
//
//	@Override
//	public float getDurationDays() {
//		return 0.5f;
//	}

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		
		LabelAPI title = tooltip.addTitle(spec.getName());
//		title.highlightLast(status);
//		title.setHighlightColor(gray);

		float pad = 10f;
		tooltip.addPara("Turn off engines to reduce interference and link all sensors in the fleet into a single network.", pad);
		tooltip.addPara("Increases sensor range by %s* units and" +
				" increases the range at which the fleet can be detected by %s* units." +
				" The fleet is only able to %s** while the ability is active.",
				pad, highlight,
				"" + (int)SENSOR_RANGE_BONUS,
				"" + (int)DETECTABILITY_RANGE_BONUS,
				"move slowly"
		);
		//tooltip.addPara("Disables \"Go Dark\" when activated.", pad);
		tooltip.addPara("*2000 units = 1 map grid cell", gray, pad);
		tooltip.addPara("**A fleet is considered slow-moving at a burn level of half that of its slowest ship.", gray, 0f);
//		tooltip.addPara("**Maximum burn level of %s", 0f, gray, 
//				Misc.getDarkHighlightColor(), 
//				"" + Misc.getGoSlowBurnLevel(getFleet()));
		
		addIncompatibleToTooltip(tooltip, expanded);
		
	}

	public boolean hasTooltip() {
		return true;
	}
	
}





