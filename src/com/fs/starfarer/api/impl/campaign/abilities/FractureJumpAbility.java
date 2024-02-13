package com.fs.starfarer.api.impl.campaign.abilities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.NascentGravityWellAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FleetMemberDamageLevel;

public class FractureJumpAbility extends BaseDurationAbility {

	public static float CR_COST_MULT = 0.1f;
	public static float FUEL_USE_MULT = 1f;
	
	
	public static float NASCENT_JUMP_DIST = 50f;

	protected boolean canUseToJumpToHyper() {
		return true;
	}
	
	protected boolean canUseToJumpToSystem() {
		return true;
	}
	
	protected Boolean primed = null;
	protected NascentGravityWellAPI well = null;
	protected EveryFrameScript ping = null;
	
	@Override
	protected void activateImpl() {
		//if (Global.getSector().isPaused()) return;
		
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		if (fleet.isInHyperspaceTransition()) return;
		
		if (fleet.isInHyperspace() && canUseToJumpToSystem()) { 
			NascentGravityWellAPI well = getNearestWell(NASCENT_JUMP_DIST);
			if (well == null || well.getTarget() == null) return;
			
			this.well = well;
			ping = Global.getSector().addPing(fleet, Pings.TRANSVERSE_JUMP);
			primed = true;
		} else if (!fleet.isInHyperspace() && canUseToJumpToHyper() &&
				fleet.getContainingLocation() instanceof StarSystemAPI) {
			ping = Global.getSector().addPing(fleet, Pings.TRANSVERSE_JUMP);
			primed = true;
		} else {
			deactivate();
		}
	}

	@Override
	public void deactivate() {
		if (ping != null) {
			Global.getSector().removeScript(ping);
			ping = null;
		}
		super.deactivate();
	}

	@Override
	protected void applyEffect(float amount, float level) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		if (level > 0 && level < 1 && amount > 0) {
			float activateSeconds = getActivationDays() * Global.getSector().getClock().getSecondsPerDay();
			float speed = fleet.getVelocity().length();
			float acc = Math.max(speed, 200f)/activateSeconds + fleet.getAcceleration();
			float ds = acc * amount;
			if (ds > speed) ds = speed;
			Vector2f dv = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(fleet.getVelocity()));
			dv.scale(ds);
			fleet.setVelocity(fleet.getVelocity().x - dv.x, fleet.getVelocity().y - dv.y);
			return;
		}
		
		if (level == 1 && primed != null) {
			float dist = Float.MAX_VALUE;
			if (well != null) {
				dist = Misc.getDistance(fleet, well) - well.getRadius() - fleet.getRadius();
			}
			if (well != null && fleet.isInHyperspace() && canUseToJumpToSystem() && dist < 500f) { 
				SectorEntityToken planet = well.getTarget();
				Vector2f loc = Misc.getPointAtRadius(planet.getLocation(), planet.getRadius() + 200f + fleet.getRadius());
				SectorEntityToken token = planet.getContainingLocation().createToken(loc.x, loc.y);
				
				JumpDestination dest = new JumpDestination(token, null);
				Global.getSector().doHyperspaceTransition(fleet, fleet, dest);
			} else if (!fleet.isInHyperspace() && canUseToJumpToHyper() &&
					fleet.getContainingLocation() instanceof StarSystemAPI) {
				float crCostFleetMult = getCRCostMult(fleet);
				if (crCostFleetMult > 0) {
					for (FleetMemberAPI member : getNonReadyShips()) {
						if ((float) Math.random() < EmergencyBurnAbility.ACTIVATION_DAMAGE_PROB) {
							Misc.applyDamage(member, null, FleetMemberDamageLevel.LOW, false, null, null,
									true, null, member.getShipName() + " suffers damage from Transverse Jump activation");
						}
					}
					for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
						float crLoss = member.getDeployCost() * CR_COST_MULT * crCostFleetMult;
						member.getRepairTracker().applyCREvent(-crLoss, "Transverse jump");
					}
					String key = "$makeTranverseJumpCostMoreCROnce";
					fleet.getMemoryWithoutUpdate().unset(key);
				}
				
				float cost = computeFuelCost();
				fleet.getCargo().removeFuel(cost);
				
				
				StarSystemAPI system = (StarSystemAPI) fleet.getContainingLocation();
				
				Vector2f offset = Vector2f.sub(fleet.getLocation(), system.getCenter().getLocation(), new Vector2f());
				float maxInSystem = 20000f;
				float maxInHyper = 2000f;
				float f = offset.length() / maxInSystem;
				//if (f > 1) f = 1;
				if (f > 0.5f) f = 0.5f;
				
				float angle = Misc.getAngleInDegreesStrict(offset);
				
				Vector2f destOffset = Misc.getUnitVectorAtDegreeAngle(angle);
				destOffset.scale(f * maxInHyper);
				
				Vector2f.add(system.getLocation(), destOffset, destOffset);
				SectorEntityToken token = Global.getSector().getHyperspace().createToken(destOffset.x, destOffset.y);
				
				JumpDestination dest = new JumpDestination(token, null);
				Global.getSector().doHyperspaceTransition(fleet, fleet, dest);
			}
			
			primed = null;
			well = null;
		}
	}
	
	@Override
	protected String getActivationText() {
		return super.getActivationText();
		//return "Initiating jump";
	}


	@Override
	protected void deactivateImpl() {
		cleanupImpl();
	}
	
	@Override
	protected void cleanupImpl() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
	}
	
	@Override
	public boolean isUsable() {
		if (!super.isUsable()) return false;
		if (getFleet() == null) return false;
		
		CampaignFleetAPI fleet = getFleet();
		
		if (fleet.isInHyperspaceTransition()) return false;
		
		if (TutorialMissionIntel.isTutorialInProgress()) return false;
		
		if (canUseToJumpToSystem() && fleet.isInHyperspace() && getNearestWell(NASCENT_JUMP_DIST) != null) {
			return true;
		}
		
		if (canUseToJumpToHyper() && !fleet.isInHyperspace()) {
			//if (getNonReadyShips().isEmpty() &&
			if ((getFleet().isAIMode() || computeFuelCost() <= getFleet().getCargo().getFuel())) {
				return true;
			}
		}
		
		return false;
	}
	
	public NascentGravityWellAPI getNearestWell(float maxDist) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return null;
		if (!fleet.isInHyperspace()) return null;
		
		float minDist = Float.MAX_VALUE;
		NascentGravityWellAPI closest = null;
		List<Object> wells = fleet.getContainingLocation().getEntities(NascentGravityWellAPI.class);
		for (Object o : wells) {
			NascentGravityWellAPI well = (NascentGravityWellAPI) o;
			float dist = Misc.getDistance(well.getLocation(), fleet.getLocation());
			dist -= well.getRadius() + fleet.getRadius();
			if (dist > maxDist) continue;
			if (dist < minDist) {
				minDist = dist;
				closest = well;
			}
		}
		return closest;
	}

	
	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color fuel = Global.getSettings().getColor("progressBarFuelColor");
		Color bad = Misc.getNegativeHighlightColor();
		
		LabelAPI title = tooltip.addTitle("Transverse Jump");

		float pad = 10f;

		NascentGravityWellAPI well = getNearestWell(NASCENT_JUMP_DIST);
		
		tooltip.addPara("Jump into hyperspace without the use of a jump-point, or " +
						"jump into a star system across the hyperspace boundary near a nascent gravity well, " +
						"emerging near the entity corresponding to the gravity well.", pad);
		
		float fuelCost = computeFuelCost();
		float supplyCost = computeSupplyCost();
		
		if (supplyCost > 0) {
			tooltip.addPara("Jumping into hyperspace consumes %s fuel and slightly reduces the combat readiness" +
						" of all ships, costing up to %s supplies to recover. Jumping into a star system is free.", pad, 
						highlight,
						Misc.getRoundedValueMaxOneAfterDecimal(fuelCost),
						Misc.getRoundedValueMaxOneAfterDecimal(supplyCost));
		} else {
			tooltip.addPara("Jumping into hyperspace consumes %s fuel. Jumping into a star system is free.", pad, 
					highlight,
					Misc.getRoundedValueMaxOneAfterDecimal(fuelCost));
		}
		
		
		if (TutorialMissionIntel.isTutorialInProgress()) { 
			tooltip.addPara("Can not be used right now.", bad, pad);
		}
		
		if (!fleet.isInHyperspace()) {
			if (fuelCost > fleet.getCargo().getFuel()) {
				tooltip.addPara("Not enough fuel.", bad, pad);
			}
		
			List<FleetMemberAPI> nonReady = getNonReadyShips();
			if (!nonReady.isEmpty()) {
				//tooltip.addPara("Not all ships have enough combat readiness to initiate an emergency burn. Ships that require higher CR:", pad);
				tooltip.addPara("Some ships don't have enough combat readiness to safely initiate a transverse jump " +
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
			
//			List<FleetMemberAPI> nonReady = getNonReadyShips();
//			if (!nonReady.isEmpty()) {
//				tooltip.addPara("Not all ships have enough combat readiness to initiate a transverse jump. Ships that require higher CR:", pad);
//				tooltip.beginGridFlipped(getTooltipWidth(), 1, 30, pad);
//				//tooltip.setGridLabelColor(bad);
//				int j = 0;
//				int max = 7;
//				for (FleetMemberAPI member : nonReady) {
//					if (j >= max) {
//						if (nonReady.size() > max + 1) {
//							tooltip.addToGrid(0, j++, "... and several other ships", "", bad);
//							break;
//						}
//					}
//					float crLoss = member.getDeployCost() * CR_COST_MULT;
//					String cost = "" + Math.round(crLoss * 100) + "%";
//					String str = "";
//					if (!member.isFighterWing()) {
//						str += member.getShipName() + ", ";
//						str += member.getHullSpec().getHullNameWithDashClass();
//					} else {
//						str += member.getVariant().getFullDesignationWithHullName();
//					}
//					tooltip.addToGrid(0, j++, str, cost, bad);
//				}
//				tooltip.addGrid(3f);
//			}
		}

		if (fleet.isInHyperspace()) {
			if (well == null) {
				tooltip.addPara("Must be near a nascent gravity well.", bad, pad);
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
	
	
	protected List<FleetMemberAPI> getNonReadyShips() {
		List<FleetMemberAPI> result = new ArrayList<FleetMemberAPI>();
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return result;
		
		//float crCostFleetMult = fleet.getStats().getDynamic().getValue(Stats.EMERGENCY_BURN_CR_MULT);
		//float crCostFleetMult = 1f;
		float crCostFleetMult = getCRCostMult(fleet);
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			//if (member.isMothballed()) continue;
			float crLoss = member.getDeployCost() * CR_COST_MULT * crCostFleetMult;
			if (Math.round(member.getRepairTracker().getCR() * 100) < Math.round(crLoss * 100)) {
				result.add(member);
			}
		}
		return result;
	}

	protected float computeFuelCost() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return 0f;
		
		float cost = fleet.getLogistics().getFuelCostPerLightYear() * FUEL_USE_MULT;
		return cost;
	}
	
	protected float getCRCostMult(CampaignFleetAPI fleet) {
		float crCostFleetMult = fleet.getStats().getDynamic().getValue(Stats.DIRECT_JUMP_CR_MULT);
		String key = "$makeTranverseJumpCostMoreCROnce";
		if (fleet.getMemoryWithoutUpdate().contains(key)) {
			crCostFleetMult = 20f;
		}
		return crCostFleetMult;
	}
	
	protected float computeSupplyCost() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return 0f;
		
		//float crCostFleetMult = fleet.getStats().getDynamic().getValue(Stats.EMERGENCY_BURN_CR_MULT);
		//float crCostFleetMult = 1f;
		float crCostFleetMult = getCRCostMult(fleet);
		
		float cost = 0f;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			cost += member.getDeploymentCostSupplies() * CR_COST_MULT * crCostFleetMult;
		}
		return cost;
	}
	
	
	
	protected boolean showAlarm() {
		if (getFleet() != null && getFleet().isInHyperspace()) return false;
		return !getNonReadyShips().isEmpty() && !isOnCooldown() && !isActiveOrInProgress() && isUsable();
	}
	
//	@Override
//	public boolean isUsable() {
//		return super.isUsable() && 
//					getFleet() != null && 
//					//getNonReadyShips().isEmpty() &&
//					(getFleet().isAIMode() || computeFuelCost() <= getFleet().getCargo().getFuel());
//	}
	
	@Override
	public float getCooldownFraction() {
		if (showAlarm()) {
			return 0f;
		}
		return super.getCooldownFraction();
	}
	@Override
	public boolean showCooldownIndicator() {
		return super.showCooldownIndicator();
	}
	@Override
	public boolean isOnCooldown() {
		return super.getCooldownFraction() < 1f;
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





