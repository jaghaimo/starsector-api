package com.fs.starfarer.api.impl.campaign.abilities;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BuffManagerAPI.Buff;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.CRRecoveryBuff;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FleetMemberDamageLevel;

public class ToggleAbilityWithCost extends BaseToggleAbility {

	public float getFuelCostMult(boolean forTooltip) {
		return 1f;
	}
	public float getCRCostMult(boolean forTooltip) {
		return 0.25f;
	}
	
	public float getActivationAtLowCRShipDamageProbability(boolean forTooltip) {
		return 0.33f;
	}
	
	public FleetMemberDamageLevel getActivationDamageLevel(boolean forTooltip) {
		return FleetMemberDamageLevel.LOW;
	}
	
	public boolean canRecoverCRWhileActive(boolean forTooltip) {
		return true;
	}
	
	public boolean shouldApplyCostWhenDeactivating(boolean forTooltip) {
		return true;
	}
	
	@Override
	protected void activateImpl() {
		deductCost();
	}
	
	protected void deductCost() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		float crCostMult = getCRCostMult(false);
		if (crCostMult > 0) {
			for (FleetMemberAPI member : getNonReadyShips(false)) {
				if ((float) Math.random() < getActivationAtLowCRShipDamageProbability(false)) {
					Misc.applyDamage(member, null, getActivationDamageLevel(false), false, null, null,
							true, null, member.getShipName() + " suffers damage from " + spec.getName() + " activation");
				}
			}
			for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
				float crLoss = getCRCost(member, false);
				member.getRepairTracker().applyCREvent(-crLoss, Misc.ucFirst(spec.getName().toLowerCase()));
			}
		}
		
		float cost = computeFuelCost(false);
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

		if (!canRecoverCRWhileActive(false)) {
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
		if (shouldApplyCostWhenDeactivating(false)) {
			deductCost();
		}
	}
	
	@Override
	protected void cleanupImpl() {
		unapplyStatsEffect();
	}
	
	protected List<FleetMemberAPI> getNonReadyShips(boolean forTooltip) {
		List<FleetMemberAPI> result = new ArrayList<FleetMemberAPI>();
		if (getCRCostMult(forTooltip) <= 0f) return result;
		
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return result;
		
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			float crLoss = getCRCost(member, forTooltip);
			if (Math.round(member.getRepairTracker().getCR() * 100) < Math.round(crLoss * 100)) {
				result.add(member);
			}
		}
		return result;
	}
	
	public float getCRCost(FleetMemberAPI member, boolean forTooltip) {
		float crCostMult = getCRCostMult(forTooltip);
		float crLoss = member.getDeployCost() * crCostMult;
		return Math.round(crLoss * 100f) / 100f;
	}

	protected float computeFuelCost(boolean forTooltip) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return 0f;
		
		float cost = fleet.getLogistics().getFuelCostPerLightYear() * getFuelCostMult(forTooltip);
		return cost;
	}
	
	protected float computeSupplyCost(boolean forTooltip) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return 0f;
		
		float crCostMult = getCRCostMult(forTooltip);
		
		float cost = 0f;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			cost += member.getDeploymentCostSupplies() * crCostMult;
		}
		return cost;
	}

	
	public void addOtherNotUsableReason(TooltipMakerAPI tooltip, boolean expanded) {
		
	}
	public void addCostTooltipSection(TooltipMakerAPI tooltip, boolean expanded, String prefix) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		Color highlight = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		float pad = 10f;
		
		
		String preventsRecovery = "";
		if (!canRecoverCRWhileActive(true)) {
			preventsRecovery = " Prevents combat readiness recovery while active.";
		}
		
		String deactivateCost = "";
		if (shouldApplyCostWhenDeactivating(true)) {
			deactivateCost = " The cost is incurred both when activating and deactivating the ability.";
		}
		preventsRecovery += deactivateCost;
		
		if (Global.CODEX_TOOLTIP_MODE) {
			String years = "year's";
			if (getFuelCostMult(true) != 1) years = "years'";
			
			if (getFuelCostMult(true) > 0 && getCRCostMult(true) > 0) {
				if (prefix == null) {
					prefix = "C";
				} else {
					prefix += " c";
				}
				tooltip.addPara(prefix + "onsumes %s light " + years + " worth of fuel and reduces the combat readiness "
						+ "of all ships by %s of a combat deployment." + preventsRecovery,
						pad, 
						highlight,
						"" + Misc.getRoundedValue(getFuelCostMult(true)),
						"" + (int) Math.round(getCRCostMult(true) * 100f) + "%");
			} else if (getCRCostMult(true) > 0) {
				if (prefix == null) {
					prefix = "R";
				} else {
					prefix += " r";
				}
				tooltip.addPara(prefix + "educes the combat readiness "
						+ "of all ships by %s of a combat deployment." + preventsRecovery,
						pad, 
						highlight,
						"" + Misc.getRoundedValue(getFuelCostMult(true)),
						"" + (int) Math.round(getCRCostMult(true) * 100f) + "%");
			} else if (getFuelCostMult(true) > 0) {
				if (prefix == null) {
					prefix = "C";
				} else {
					prefix += " c";
				}
				tooltip.addPara(prefix + "onsumes %s light " + years + " worth of fuel." + preventsRecovery,
						pad, 
						highlight,
						"" + Misc.getRoundedValue(getFuelCostMult(true)));
			}
			
			if (getCRCostMult(true) > 0) {
				tooltip.addPara("Ships with insufficient combat readiness may suffer damage when the ability is activated.", pad);
			}
		} else {		
			float fuelCost = computeFuelCost(true);
			float supplyCost = computeSupplyCost(true);
			if (supplyCost > 0 && fuelCost > 0) {
				if (prefix == null) {
					prefix = "C";
				} else {
					prefix += " c";
				}
				tooltip.addPara(prefix + "onsumes %s fuel and reduces the combat readiness" +
							" of all ships, costing up to %s supplies to recover." + preventsRecovery, pad, 
							highlight,
							Misc.getRoundedValueMaxOneAfterDecimal(fuelCost),
							Misc.getRoundedValueMaxOneAfterDecimal(supplyCost));
			} else if (supplyCost > 0) {
				if (prefix == null) {
					prefix = "R";
				} else {
					prefix += " r";
				}
				tooltip.addPara(prefix + "educes the combat readiness" +
						" of all ships, costing up to %s supplies to recover." + preventsRecovery, pad, 
						highlight,
						Misc.getRoundedValueMaxOneAfterDecimal(supplyCost));
			} else if (fuelCost > 0) {
				if (prefix == null) {
					prefix = "C";
				} else {
					prefix += " c";
				}
				tooltip.addPara(prefix + "onsumes %s fuel." + preventsRecovery, pad, 
						highlight,
						Misc.getRoundedValueMaxOneAfterDecimal(fuelCost));
			}
			
			if (fuelCost > 0 && fuelCost > fleet.getCargo().getFuel()) {
				tooltip.addPara("Not enough fuel.", bad, pad);
			} else {
				addOtherNotUsableReason(tooltip, expanded);
			}
			
			List<FleetMemberAPI> nonReady = getNonReadyShips(true);
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
	}

	public boolean hasTooltip() {
		return true;
	}
	

//	@Override
//	public void fleetLeftBattle(BattleAPI battle, boolean engagedInHostilities) {
//		if (engagedInHostilities) {
//			deactivate();
//		}
//	}
//
//	@Override
//	public void fleetOpenedMarket(MarketAPI market) {
//		deactivate();
//	}
	
	@Override
	public boolean isOnCooldown() {
		return super.getCooldownFraction() < 1f;
	}
	
	@Override
	public boolean isUsable() {
		return super.isUsable() && 
					getFleet() != null && 
					(getFleet().isAIMode() || computeFuelCost(false) <= getFleet().getCargo().getFuel());
	}
	
	protected boolean showAlarm() {
		return !getNonReadyShips(false).isEmpty() && !isOnCooldown() && !isActiveOrInProgress() && isUsable();
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
}





