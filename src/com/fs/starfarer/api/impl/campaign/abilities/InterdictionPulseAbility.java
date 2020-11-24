package com.fs.starfarer.api.impl.campaign.abilities;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.ai.FleetAIFlags;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.loading.CampaignPingSpec;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class InterdictionPulseAbility extends BaseDurationAbility {

	public static class IPReactionScript implements EveryFrameScript {
		float delay;
		boolean done;
		CampaignFleetAPI other;
		CampaignFleetAPI fleet;
		float activationDays;
		/**
		 * fleet is using IP, other is reacting.
		 * @param fleet
		 * @param other
		 * @param activationDays
		 */
		public IPReactionScript(CampaignFleetAPI fleet, CampaignFleetAPI other, float activationDays) {
			this.fleet = fleet;
			this.other = other;
			this.activationDays = activationDays;
			delay = 0.3f + 0.3f * (float) Math.random();
			//delay = 0f;
		}
		public void advance(float amount) {
			if (done) return;
			
			delay -= amount;
			if (delay > 0) return;
			
			VisibilityLevel level = fleet.getVisibilityLevelTo(other);
			if (level == VisibilityLevel.NONE || level == VisibilityLevel.SENSOR_CONTACT) {
				done = true;
				return;
			}
			
			if (!(other.getAI() instanceof ModularFleetAIAPI)) {
				done = true;
				return;
			}
			ModularFleetAIAPI ai = (ModularFleetAIAPI) other.getAI();
			
			
			float dist = Misc.getDistance(fleet.getLocation(), other.getLocation());
			float speed = Math.max(1f, other.getTravelSpeed());
			float eta = dist / speed;
			
			float rushTime = activationDays * Global.getSector().getClock().getSecondsPerDay();
			rushTime += 0.5f + 0.5f * (float) Math.random();
			
			MemoryAPI mem = other.getMemoryWithoutUpdate();
			CampaignFleetAPI pursueTarget = mem.getFleet(FleetAIFlags.PURSUIT_TARGET);
			
			if (eta < rushTime && pursueTarget == fleet) {
				done = true;
				return;
			}
			
			float range = InterdictionPulseAbility.getRange(fleet);
			float getAwayTime = 1f + (range - dist) / speed;
			AbilityPlugin sb = other.getAbility(Abilities.SENSOR_BURST);
			if (getAwayTime > rushTime && sb != null && sb.isUsable() && (float) Math.random() > 0.67f) {
				sb.activate();
				done = true;
				return;
			}
			
			//float avoidRange = Math.min(dist, getRange(other));
			float avoidRange = getRange(other) + 100f;
			ai.getNavModule().avoidLocation(fleet.getContainingLocation(), 
											fleet.getLocation(), avoidRange, avoidRange + 50f, activationDays + 0.01f);
			
			ai.getNavModule().avoidLocation(fleet.getContainingLocation(), 
											//fleet.getLocation(), dist, dist + 50f, activationDays + 0.01f);
					Misc.getPointAtRadius(fleet.getLocation(), avoidRange * 0.5f), avoidRange, avoidRange * 1.5f + 50f, activationDays + 0.05f);
			
			done = true;
		}

		public boolean isDone() {
			return done;
		}
		public boolean runWhilePaused() {
			return false;
		}
	}
	
	public static final float MAX_EFFECT = 1f;
	//public static final float RANGE = 1000f;
	public static final float BASE_RANGE = 500f;
	public static final float BASE_SECONDS = 6f;
	public static final float STRENGTH_PER_SECOND = 200f;
	
	//public static final float CR_COST_MULT = 0.5f;
	public static final float DETECTABILITY_PERCENT = 100f;
	
//	public String getSpriteName() {
//		return Global.getSettings().getSpriteName("abilities", Abilities.EMERGENCY_BURN);
//	}
	

	public static float getRange(CampaignFleetAPI fleet) {
		return BASE_RANGE + fleet.getSensorRangeMod().computeEffective(fleet.getSensorStrength()) / 2f;
	}
	
	@Override
	protected String getActivationText() {
		//return Misc.ucFirst(spec.getName().toLowerCase());
		return "Interdiction pulse";
	}


	protected Boolean primed = null;
	protected Float elapsed = null;
	protected Integer numFired = null;
	
	@Override
	protected void activateImpl() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		Global.getSector().addPing(fleet, Pings.INTERDICT);
		
		float range = getRange(fleet);
		for (CampaignFleetAPI other : fleet.getContainingLocation().getFleets()) {
			if (other == fleet) continue;
			
			float dist = Misc.getDistance(fleet.getLocation(), other.getLocation());
			if (dist > range + 500f) continue;

			other.addScript(new IPReactionScript(fleet, other, getActivationDays()));
		}
		
		primed = true;
		
	}
	
	protected void showRangePing(float amount) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		VisibilityLevel vis = fleet.getVisibilityLevelToPlayerFleet();
		if (vis == VisibilityLevel.NONE || vis == VisibilityLevel.SENSOR_CONTACT) return;
		
		
		boolean fire = false;
		if (elapsed == null) {
			elapsed = 0f;
			numFired = 0;
			fire = true;
		}
		elapsed += amount;
		if (elapsed > 0.5f && numFired < 4) {
			elapsed -= 0.5f;
			fire = true;
		}
		
		if (fire) {
			numFired++;
			
			float range = getRange(fleet);
			CampaignPingSpec custom = new CampaignPingSpec();
			custom.setUseFactionColor(true);
			custom.setWidth(7);
			custom.setMinRange(range - 100f);
			custom.setRange(200);
			custom.setDuration(2f);
			custom.setAlphaMult(0.25f);
			custom.setInFraction(0.2f);
			custom.setNum(1);
			
			Global.getSector().addPing(fleet, custom);
		}
		
	}

	@Override
	protected void applyEffect(float amount, float level) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		fleet.getStats().getDetectedRangeMod().modifyPercent(getModId(), DETECTABILITY_PERCENT * level, "Interdiction pulse");
		
		//System.out.println("Level: " + level);
		
		if (level > 0 && level < 1 && amount > 0) {
			showRangePing(amount);
			
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
		
		float range = getRange(fleet);
		
		boolean playedHit = !(entity.isInCurrentLocation() && entity.isVisibleToPlayerFleet());
		if (level == 1 && primed != null) {
			
			CampaignPingSpec custom = new CampaignPingSpec();
			custom.setUseFactionColor(true);
			custom.setWidth(15);
			custom.setRange(range * 1.3f);
			custom.setDuration(0.5f);
			custom.setAlphaMult(1f);
			custom.setInFraction(0.1f);
			custom.setNum(1);
			Global.getSector().addPing(fleet, custom);

			
			for (CampaignFleetAPI other : fleet.getContainingLocation().getFleets()) {
				if (other == fleet) continue;
				if (other.getFaction() == fleet.getFaction()) continue;
				if (other.isInHyperspaceTransition()) continue;
				
				float dist = Misc.getDistance(fleet.getLocation(), other.getLocation());
				if (dist > range) continue;
				
				
				float interdictSeconds = getInterdictSeconds(fleet, other);
				if (interdictSeconds > 0 && interdictSeconds < 1f) interdictSeconds = 1f;
				
				VisibilityLevel vis = other.getVisibilityLevelToPlayerFleet();
				if (vis == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS ||
						vis == VisibilityLevel.COMPOSITION_DETAILS ||
						(vis == VisibilityLevel.SENSOR_CONTACT && fleet.isPlayerFleet())) {
					if (interdictSeconds <= 0) {
						other.addFloatingText("Interdict avoided!" , fleet.getFaction().getBaseUIColor(), 1f, true);
						continue;
					} else {
						other.addFloatingText("Interdict! (" + (int) Math.round(interdictSeconds) + "s)" , fleet.getFaction().getBaseUIColor(), 1f, true);
					}
				}
				
				float interdictDays = interdictSeconds / Global.getSector().getClock().getSecondsPerDay();
				
				for (AbilityPlugin ability : other.getAbilities().values()) {
					if (!ability.getSpec().hasTag(Abilities.TAG_BURN + "+") &&
							!ability.getId().equals(Abilities.INTERDICTION_PULSE)) continue;
					
					float origCooldown = ability.getCooldownLeft();
					float extra = 0;
					if (ability.isActiveOrInProgress()) {
						extra += ability.getSpec().getDeactivationCooldown() * ability.getProgressFraction();
						ability.deactivate();
						
					}
					
					if (!ability.getSpec().hasTag(Abilities.TAG_BURN + "+")) continue;
					
					float cooldown = interdictDays;
					//cooldown = Math.max(cooldown, origCooldown);
					cooldown += origCooldown;
					cooldown += extra;
					float max = Math.max(ability.getSpec().getDeactivationCooldown(), 2f);
					if (cooldown > max) cooldown = max;
					ability.setCooldownLeft(cooldown);
				}
				
				if (fleet.isPlayerFleet() && other.knowsWhoPlayerIs() && fleet.getFaction() != other.getFaction()) {
					Global.getSector().adjustPlayerReputation(
										new RepActionEnvelope(RepActions.INTERDICTED, null, null, false), 
										other.getFaction().getId());
				}
				
				if (!playedHit) {
					Global.getSoundPlayer().playSound("world_interdict_hit", 1f, 1f, other.getLocation(), other.getVelocity());
					//playedHit = true;
				}
			}
			
			primed = null;
			elapsed = null;
			numFired = null;
		}
		
	}
	
	public static float getInterdictSeconds(CampaignFleetAPI fleet, CampaignFleetAPI other) {
		float offense = fleet.getSensorRangeMod().computeEffective(fleet.getSensorStrength());
		float defense = other.getSensorRangeMod().computeEffective(other.getSensorStrength());
		float diff = offense - defense;
		
		float extra = diff / STRENGTH_PER_SECOND;
		
		float total = BASE_SECONDS + extra;
		if (total < 0f) total = 0f;
		return total;// / Global.getSector().getClock().getSecondsPerDay();
	}
	
	
//	public static float getEffectMagnitude(CampaignFleetAPI fleet, CampaignFleetAPI other) {
//		float burn = Misc.getBurnLevelForSpeed(other.getVelocity().length());
//		
//		Vector2f velDir = Misc.normalise(new Vector2f(other.getVelocity()));
//		Vector2f toFleet = Misc.normalise(Vector2f.sub(fleet.getLocation(), other.getLocation(), new Vector2f()));
//		float dot = Vector2f.dot(velDir, toFleet);
//		if (dot <= 0.05f || burn <= 1f) return 0f;
//		
//		float effect = dot;
//		if (effect < 0) effect = 0;
//		if (effect > 1) effect = 1;
//		
//		//effect *= Math.min(1f, burn / 10f);
//		
//		//return effect;
//		return Math.max(0.1f, effect);
//	}

	@Override
	protected void deactivateImpl() {
		cleanupImpl();
	}
	
	@Override
	protected void cleanupImpl() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		fleet.getStats().getDetectedRangeMod().unmodify(getModId());
		//fleet.getStats().getSensorRangeMod().unmodify(getModId());
		//fleet.getStats().getFleetwideMaxBurnMod().unmodify(getModId());
		//fleet.getStats().getAccelerationMult().unmodify(getModId());
		//fleet.getCommanderStats().getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).unmodify(getModId());
		
		primed = null;
	}
	

	@Override
	public boolean isUsable() {
		return super.isUsable() && 
					getFleet() != null;// && 
					//getNonReadyShips().isEmpty();
	}
	
//	protected List<FleetMemberAPI> getNonReadyShips() {
//		List<FleetMemberAPI> result = new ArrayList<FleetMemberAPI>();
//		CampaignFleetAPI fleet = getFleet();
//		if (fleet == null) return result;
//		
//		float crCostFleetMult = fleet.getStats().getDynamic().getValue(Stats.EMERGENCY_BURN_CR_MULT);
//		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
//			//if (member.isMothballed()) continue;
//			float crLoss = member.getDeployCost() * CR_COST_MULT * crCostFleetMult;
//			if (Math.round(member.getRepairTracker().getCR() * 100) < Math.round(crLoss * 100)) {
//				result.add(member);
//			}
//		}
//		return result;
//	}

//	protected float computeSupplyCost() {
//		CampaignFleetAPI fleet = getFleet();
//		if (fleet == null) return 0f;
//		
//		float crCostFleetMult = fleet.getStats().getDynamic().getValue(Stats.EMERGENCY_BURN_CR_MULT);
//		
//		float cost = 0f;
//		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
//			cost += member.getDeploymentPointsCost() * CR_COST_MULT * crCostFleetMult;
//		}
//		return cost;
//	}

	
	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color fuel = Global.getSettings().getColor("progressBarFuelColor");
		Color bad = Misc.getNegativeHighlightColor();
		
		LabelAPI title = tooltip.addTitle("Interdiction Pulse");

		float pad = 10f;
		
		int range = (int) getRange(fleet);
		
		
		tooltip.addPara("Stops the fleet and uses its active sensor network to charge and release a powerful energy pulse that " +
				"can disrupt the drive fields of nearby fleets.", pad);
		
		Color c = Misc.getTooltipTitleAndLightHighlightColor();
		tooltip.addPara("The disruption interrupts any movement-related abilities (such as %s) " +
				"and prevents their use for some time afterwards. Also interrupts charging interdiction pulses.", pad,
				highlight, "Sustained Burn");

		tooltip.addPara("The disruption lasts for %s seconds, modified by %s second for " +
				"every %s points of difference in the fleets' sensor strengths.", pad, highlight,
				"" + (int) BASE_SECONDS,
				"" + (int) 1,
				"" + (int) STRENGTH_PER_SECOND);
		
		tooltip.addPara("Base range of %s* units, increased by half your fleet's sensor strength, " +
				"for a total of %s units. While the pulse is charging, the range at which the fleet can be detected will " +
				"gradually increase by up to %s.", pad, highlight, 
				"" + (int) BASE_RANGE,
				"" + range,
				"" + (int) DETECTABILITY_PERCENT + "%");
		
		tooltip.addPara("A successful interdict is considered a hostile act, though not on the same level as " +
						"open warfare.", pad);
		
		tooltip.addPara("*2000 units = 1 map grid cell", gray, pad);
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
	
}





