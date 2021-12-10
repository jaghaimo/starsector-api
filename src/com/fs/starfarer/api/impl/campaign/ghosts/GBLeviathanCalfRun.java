package com.fs.starfarer.api.impl.campaign.ghosts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.impl.campaign.ghosts.GBIGenerateSlipstream.GhostBehaviorWithSlipstream;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2;
import com.fs.starfarer.api.util.Misc;


public class GBLeviathanCalfRun extends BaseGhostBehavior implements GhostBehaviorWithSlipstream {
	
	public static float SENSOR_BURST_TURN_RATE = 30f;
	public static float PULSE_BURN_BONUS = 10;
	public static float PULSE_BURN_BONUS_DECAY = 0.25f;
	
	protected boolean dirSet = false;
	protected float dir;
	protected float burnBonus;
	protected int origPluginBurn;
	protected SectorEntityToken from;
	protected float phase = (float) Math.random();
	protected int maxBurn;
	protected SlipstreamTerrainPlugin2 plugin;
	protected boolean affectedByPulse = false;
	protected float wobbleRate;
	protected float maxWobble;
	
	public GBLeviathanCalfRun(float duration, SectorEntityToken from, float wobbleRate, float maxWobble, 
			int maxBurn, boolean affectedByPulse) {
		super(duration);
		this.from = from;
		this.wobbleRate = wobbleRate;
		this.maxWobble = maxWobble;
		this.maxBurn = maxBurn;
		this.affectedByPulse = affectedByPulse;
	}



	@Override
	public void advance(float amount, SensorGhost ghost) {
		if (from.getContainingLocation() != ghost.getEntity().getContainingLocation() ||
				ghost.getEntity().getContainingLocation() == null || !from.isAlive()) {
			end();
			return;
		}
		super.advance(amount, ghost);
		
		if (!dirSet || ghost.getEntity().getVelocity().length() < 10f) {
			dir = Misc.getAngleInDegrees(from.getLocation(), ghost.getEntity().getLocation());
			dirSet = true;
		} else {
			//dir = Misc.getAngleInDegrees(ghost.getEntity().getVelocity());
		}
		
		float pi = (float) Math.PI;
		float sin = (float) Math.sin(phase * pi * 2f);
		phase += amount * wobbleRate;
		
		float maxAngleOffset = maxWobble;
		float angle = dir + sin * maxAngleOffset;
		
//		float maxSteeringOffset = 30f;
//		maxSteeringOffset = 0f;
//		if (maxSteeringOffset > 0f && plugin != null && plugin.containsEntity(from)) {
//			float [] coords = plugin.getLengthAndWidthFractionWithinStream(from.getLocation());
//			if (coords != null) {
//				float dist = Misc.getDistance(ghost.getEntity(), from);
//				if (dist < 2000f) {
//					float mag = (1f - Math.max(0f, dist - 1000f) / 1000f) * 
//								Math.signum(coords[1]) * Math.max(0f, Math.abs(coords[1]) - 0.3f) * 
//								maxSteeringOffset;
//					angle += mag;
//				}
//			}
//		}
		
		if (affectedByPulse) {
			checkInterdictionPulses(ghost);
		}
		//checkSensorBursts(ghost, amount);
		
		int useBonus = 0;
		if (plugin != null) {
			burnBonus -= PULSE_BURN_BONUS_DECAY * amount;
			if (burnBonus < 0) burnBonus = 0;
	
			useBonus = (int) burnBonus;
			plugin.getParams().burnLevel = origPluginBurn + useBonus;
		}
		
		Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
		loc.scale(10000f);
		Vector2f.add(loc, ghost.getEntity().getLocation(), loc);
		ghost.moveTo(loc, maxBurn + useBonus);
		
	}

	public void checkInterdictionPulses(SensorGhost ghost) {
		if (!Global.getSector().getMemoryWithoutUpdate().getBoolean(MemFlags.GLOBAL_INTERDICTION_PULSE_JUST_USED_IN_CURRENT_LOCATION)) {
			return;
		}
		CustomCampaignEntityAPI entity = ghost.getEntity();
		if (entity == null || entity.getContainingLocation() == null || plugin == null) return;
		for (CampaignFleetAPI fleet : entity.getContainingLocation().getFleets()) {
//			float range = InterdictionPulseAbility.getRange(fleet);
//			float dist = Misc.getDistance(fleet.getLocation(), entity.getLocation());
//			if (dist > range) continue;
			// anywhere inside the stream is fine, actually
			if (!plugin.containsEntity(fleet)) continue;
			
			if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.JUST_DID_INTERDICTION_PULSE)) {
				burnBonus = PULSE_BURN_BONUS;
				if (fleet.isPlayerFleet()) {
					String key = "$leviathanCalvesPulsed";
					int count = Global.getSector().getMemoryWithoutUpdate().getInt(key);
					Global.getSector().getMemoryWithoutUpdate().set(key, count + 1);
				}
				return;
			}
		}
	}
	public void checkSensorBursts(SensorGhost ghost, float amount) {
		CustomCampaignEntityAPI entity = ghost.getEntity();
		if (entity == null || entity.getContainingLocation() == null || plugin == null) return;
		for (CampaignFleetAPI fleet : entity.getContainingLocation().getFleets()) {
			// anywhere inside the stream is fine, actually
			if (!plugin.containsEntity(fleet)) continue;
			
			AbilityPlugin asb = fleet.getAbility(Abilities.SENSOR_BURST);
			if (asb.isInProgress()) {// && asb.getLevel() == 1f) {
				float [] coords = plugin.getLengthAndWidthFractionWithinStream(fleet.getLocation());
				if (coords != null) {
					dir += coords[1] * SENSOR_BURST_TURN_RATE * amount;
					((BaseSensorGhost)ghost).setAccelMult(1f);
				}
//				float velDir = Misc.getAngleInDegrees(entity.getVelocity());
//				float toFleet = Misc.getAngleInDegrees(entity.getLocation(), fleet.getLocation());
//				float turnDir = Misc.getClosestTurnDirection(toFleet, velDir);
//				dir += turnDir * SENSOR_BURST_TURN_RATE * amount;
			}
		}
	}


	public void setSlipstream(SlipstreamTerrainPlugin2 plugin) {
		this.plugin = plugin;
		origPluginBurn = plugin.getParams().burnLevel;
	}
	
	
	
}













