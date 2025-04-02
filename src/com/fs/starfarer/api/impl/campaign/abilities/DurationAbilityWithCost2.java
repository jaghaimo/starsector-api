package com.fs.starfarer.api.impl.campaign.abilities;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.BuffManagerAPI.Buff;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.CRRecoveryBuff;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FleetMemberDamageLevel;

public class DurationAbilityWithCost2 extends BaseDurationAbility {

	public float getFuelCostMult() {
		return 1f;
	}
	public float getCRCostMult() {
		return 0.25f;
	}
	
	public float getActivationAtLowCRShipDamageProbability() {
		return 0.33f;
	}
	
	public FleetMemberDamageLevel getActivationDamageLevel() {
		return FleetMemberDamageLevel.LOW;
	}
	
	public boolean canRecoverCRWhileActive() {
		return false;
	}
	
	protected void deductCost() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		float crCostMult = getCRCostMult();
		if (crCostMult > 0) {
			for (FleetMemberAPI member : getNonReadyShips()) {
				if ((float) Math.random() < getActivationAtLowCRShipDamageProbability()) {
					Misc.applyDamage(member, null, getActivationDamageLevel(), false, null, null,
							true, null, member.getShipName() + " suffers damage from " + spec.getName() + " activation");
				}
			}
			for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
				float crLoss = getCRCost(member);
				member.getRepairTracker().applyCREvent(-crLoss, Misc.ucFirst(spec.getName().toLowerCase()));
			}
		}
		
		float cost = computeFuelCost();
		fleet.getCargo().removeFuel(cost);
	}

	
	protected void applyStatsEffect(float amount, float level) {
		
	}
	
	protected void unapplyStatsEffect() {
		
	}
	
	protected void applyFleetVisual(float amount, float level) {
		
	}
	
	@Override
	protected void applyEffect(float amount, float level) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		applyStatsEffect(amount, level);
		applyFleetVisual(amount, level);

		if (!canRecoverCRWhileActive()) {
			String buffId = getModId();
			float buffDur = 0.1f;
			boolean needsSync = false;
			for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
				if (level <= 0) {
					member.getBuffManager().removeBuff(buffId);
					needsSync = true;
				} else {
					Buff test = member.getBuffManager().getBuff(buffId);
					if (test instanceof CRRecoveryBuff) {
						CRRecoveryBuff buff = (CRRecoveryBuff) test;
						buff.setDur(buffDur);
					} else {
						member.getBuffManager().addBuff(new CRRecoveryBuff(buffId, 0f, buffDur));
						needsSync = true;
					}
				}
			}
		
			if (needsSync) {
				fleet.forceSync();
			}
		}
		
	}

	@Override
	protected void deactivateImpl() {
		cleanupImpl();
	}
	
	@Override
	protected void cleanupImpl() {
		unapplyStatsEffect();
	}
	
	protected List<FleetMemberAPI> getNonReadyShips() {
		List<FleetMemberAPI> result = new ArrayList<FleetMemberAPI>();
		if (getCRCostMult() <= 0f) return result;
		
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return result;
		
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			float crLoss = getCRCost(member);
			if (Math.round(member.getRepairTracker().getCR() * 100) < Math.round(crLoss * 100)) {
				result.add(member);
			}
		}
		return result;
	}
	
	public float getCRCost(FleetMemberAPI member) {
		float crCostMult = getCRCostMult();
		float crLoss = member.getDeployCost() * crCostMult;
		return Math.round(crLoss * 100f) / 100f;
	}

	protected float computeFuelCost() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return 0f;
		
		float cost = fleet.getLogistics().getFuelCostPerLightYear() * getFuelCostMult();
		return cost;
	}
	
	protected float computeSupplyCost() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return 0f;
		
		float crCostMult = getCRCostMult();
		
		float cost = 0f;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			cost += member.getDeploymentCostSupplies() * crCostMult;
		}
		return cost;
	}

	
	public void addInitialDescription(TooltipMakerAPI tooltip, boolean expanded) {
		
	}
	
	public boolean addNotUsableReasonBeforeFuelCost(TooltipMakerAPI tooltip, boolean expanded) {
		return false;
	}
	public void addNotUsableReasonAfterFuelCost(TooltipMakerAPI tooltip, boolean expanded) {
		
	}
	
	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color fuel = Global.getSettings().getColor("progressBarFuelColor");
		Color bad = Misc.getNegativeHighlightColor();
		
		if (!Global.CODEX_TOOLTIP_MODE) {
			LabelAPI title = tooltip.addTitle(spec.getName());
		} else {
			tooltip.addSpacer(-10f);
		}

		addInitialDescription(tooltip, expanded);

		float pad = 10f;
		
		
		String preventsRecovery = "";
		if (!canRecoverCRWhileActive()) {
			preventsRecovery = " Prevents combat readiness recovery while active.";
		}

		if (Global.CODEX_TOOLTIP_MODE) {
			String years = "year's";
			if (getFuelCostMult() != 1) years = "years'";
			if (getFuelCostMult() > 0 || getCRCostMult() > 0) {
				tooltip.addPara("Consumes %s light " + years + " worth of fuel and reduces the combat readiness "
						+ "of all ships by %s of a combat deployment." + preventsRecovery,
						pad, 
						highlight,
						"" + Misc.getRoundedValue(getFuelCostMult()),
						"" + (int) Math.round(getCRCostMult() * 100f) + "%");
			} else if (getCRCostMult() > 0) {
				tooltip.addPara("Reduces the combat readiness "
						+ "of all ships by %s of a combat deployment." + preventsRecovery,
						pad, 
						highlight,
						"" + (int) Math.round(getCRCostMult() * 100f) + "%");
			} else if (getFuelCostMult() > 0) {
				tooltip.addPara("Consumes %s light " + years + " worth of fuel." + preventsRecovery,
						pad, 
						highlight,
						"" + Misc.getRoundedValue(getFuelCostMult()));
			}
			
			if (getCRCostMult() > 0) {
				tooltip.addPara("Ships with insufficient combat readiness may suffer damage when the ability is activated.", pad);
			}
		} else {
			float fuelCost = computeFuelCost();
			float supplyCost = computeSupplyCost();
			if (supplyCost > 0 && fuelCost > 0) {
				tooltip.addPara("Consumes %s fuel and reduces the combat readiness" +
							" of all ships, costing up to %s supplies to recover." + preventsRecovery, pad, 
							highlight,
							Misc.getRoundedValueMaxOneAfterDecimal(fuelCost),
							Misc.getRoundedValueMaxOneAfterDecimal(supplyCost));
			} else if (supplyCost > 0) {
				tooltip.addPara("Reduces the combat readiness" +
						" of all ships, costing up to %s supplies to recover." + preventsRecovery, pad, 
						highlight,
						Misc.getRoundedValueMaxOneAfterDecimal(supplyCost));
			} else if (fuelCost > 0) {
				tooltip.addPara("Consumes %s fuel." + preventsRecovery, pad, 
						highlight,
						Misc.getRoundedValueMaxOneAfterDecimal(fuelCost));
			}
			
			boolean addedReason = addNotUsableReasonBeforeFuelCost(tooltip, expanded);
			if (!addedReason && fuelCost > 0 && fuelCost > fleet.getCargo().getFuel()) {
				tooltip.addPara("Not enough fuel.", bad, pad);
			} else {
				addNotUsableReasonAfterFuelCost(tooltip, expanded);
			}
			
			List<FleetMemberAPI> nonReady = getNonReadyShips();
			if (!nonReady.isEmpty()) {
				tooltip.addPara("Some ships don't have enough combat readiness " +
								"and may suffer damage if the ability is activated:", pad, 
								Misc.getNegativeHighlightColor(), "may suffer damage");
				int j = 0;
				int max = 4;
				float initPad = 5f;
				for (FleetMemberAPI member : nonReady) {
					if (j >= max) {
						if (nonReady.size() > max + 1) {
							tooltip.addPara(BaseIntelPlugin.INDENT + "... and several other ships", initPad);
							break;
						}
					}
					String str = "";
					if (!member.isFighterWing()) {
						str += member.getShipName() + ", ";
						str += member.getHullSpec().getHullNameWithDashClass();
					} else {
						str += member.getVariant().getFullDesignationWithHullName();
					}
					
					tooltip.addPara(BaseIntelPlugin.INDENT + str, initPad);
					initPad = 0f;
					j++;
				}
			}
		}
		
		addIncompatibleToTooltip(tooltip, expanded);
	}

	public boolean hasTooltip() {
		return true;
	}
	

	@Override
	public void fleetLeftBattle(BattleAPI battle, boolean engagedInHostilities) {
		if (engagedInHostilities) {
			deactivate();
		}
	}

	@Override
	public void fleetOpenedMarket(MarketAPI market) {
		deactivate();
	}
	
	protected boolean showAlarm() {
		return !getNonReadyShips().isEmpty() && !isOnCooldown() && !isActiveOrInProgress() && isUsable();
	}
	
	@Override
	public boolean isOnCooldown() {
		return super.getCooldownFraction() < 1f;
	}
	
	@Override
	public boolean isUsable() {
		return super.isUsable() && 
					getFleet() != null && 
					(getFleet().isAIMode() || computeFuelCost() <= getFleet().getCargo().getFuel());
	}
	@Override
	public float getCooldownFraction() {
		if (showAlarm()) {
			return 0f;
		}
		return super.getCooldownFraction();
	}

	@Override
	public Color getCooldownColor() {
		if (showAlarm()) {
			Color color = Misc.getNegativeHighlightColor();
			return Misc.scaleAlpha(color, Global.getSector().getCampaignUI().getSharedFader().getBrightness() * 0.5f);
		}
		return super.getCooldownColor();
	}

	@Override
	public boolean isCooldownRenderingAdditive() {
		if (showAlarm()) {
			return true;
		}
		return false;
	}
	
	@Override
	protected void activateImpl() {
		
	}
}





