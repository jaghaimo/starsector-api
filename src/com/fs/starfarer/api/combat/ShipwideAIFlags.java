package com.fs.starfarer.api.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipwideAIFlags {
	public static enum AIFlags {
		//IS_ENGAGED,
		
		HARASS_MOVE_IN,
		HARASS_MOVE_IN_COOLDOWN,
		
		AVOIDING_BORDER,
		DO_NOT_AVOID_BORDER,
		CAMP_LOCATION,
		FACING_OVERRIDE_FOR_MOVE_AND_ESCORT_MANEUVERS,
		ESCORT_OTHER_SHIP,
		//PULL_BACK_FIGHTERS, // can do ship.setForceCarrierPullBackTime()
		
		
		
		MANEUVER_TARGET,
		MOVEMENT_DEST_WHILE_SIDETRACKED,
		CARRIER_FIGHTER_TARGET,
		MAINTAINING_STRIKE_RANGE,
		//CARRIER_DEFEND_TARGET,
		DRONE_MOTHERSHIP,
		
		DO_NOT_USE_SHIELDS,
		DO_NOT_USE_FLUX,
		DO_NOT_VENT,
		DO_NOT_AUTOFIRE_NON_ESSENTIAL_GROUPS,
		RUN_QUICKLY,
		TURN_QUICKLY,
		PURSUING,
		HAS_INCOMING_DAMAGE,
		KEEP_SHIELDS_ON,
		STAY_PHASED, // will not re-phase but should stay phased if cloak is on
		PHASE_BRAWLER_DUMPING_FLUX, // will not re-phase but should stay phased if cloak is on
		DO_NOT_BACK_OFF,
		
		/**
		 * To function reliably, requires DO_NOT_BACK_OFF to be set, also.
		 */
		DO_NOT_BACK_OFF_EVEN_WHILE_VENTING,
		BACK_OFF,
		BACK_OFF_MIN_RANGE,
		BACK_OFF_MAX_RANGE,
		STANDING_OFF_VS_SHIP_ON_MAP_BORDER,
		BACKING_OFF,
		SAFE_VENT,
		OK_TO_CANCEL_SYSTEM_USE_TO_VENT,
		
		MANEUVER_RANGE_FROM_TARGET,
		
		SAFE_FROM_DANGER_TIME,
		
		PREFER_LEFT_BROADSIDE,
		PREFER_RIGHT_BROADSIDE,
		
		AUTO_FIRING_AT_PHASE_SHIP,
		AUTO_BEAM_FIRING_AT_PHASE_SHIP,
		
		DELAY_STRIKE_FIRE,
		
		PHASE_ATTACK_RUN,
		PHASE_ATTACK_RUN_FROM_BEHIND_DIST_CRITICAL,
		PHASE_ATTACK_RUN_IN_GOOD_SPOT,
		PHASE_ATTACK_RUN_TIMEOUT,
		
		DO_NOT_PURSUE,
		
		TIMID_ESCORT,
		ESCORT_RANGE_MODIFIER,
		IGNORES_ORDERS,
		
		
		/**
		 * Only set for phase ships using the default phase cloak AI. 
		 */
		IN_CRITICAL_DPS_DANGER,
		//MANEUVER_TARGET,
		
		
		/**
		 * Fighters only, attack run related. 
		 */
		IN_ATTACK_RUN,
		POST_ATTACK_RUN,
		WANTED_TO_SLOW_DOWN,
		FINISHED_SPREADING,
		WING_NEAR_ENEMY,
		WING_WAS_NEAR_ENEMY,
		WING_SHOULD_GET_SOME_DISTANCE,

		
		REACHED_WAYPOINT,
		//ATTACK_RUN_WAYPOINT,
		
		
		/**
		 * Whether the ship wants to be escorted by nearby friendlies. 
		 */
		NEEDS_HELP,
		
		SYSTEM_TARGET_COORDS,
		
		BIGGEST_THREAT,
		
		MOVEMENT_DEST,
		
		HAS_POTENTIAL_MINE_TRIGGER_NEARBY,
		
		TARGET_FOR_SHIP_SYSTEM,
		
		CUSTOM1,
		CUSTOM2,
		CUSTOM3,
		CUSTOM4,
		CUSTOM5,
	}
	
	public static final float FLAG_DURATION = 0.5f;
	
	private class FlagData {
		AIFlags flag;
		float elapsed;
		float durationOverride = 0;
		Object custom;
		private FlagData(AIFlags flag) {
			super();
			this.flag = flag;
		}
	}
	

	private Map<AIFlags, FlagData> flags = new HashMap<AIFlags, FlagData>();
	
	public void unsetFlag(AIFlags flag) {
//		if (flag == AIFlags.BACK_OFF) {
//			System.out.println("wefwefwefe");
//		}
		flags.remove(flag);
	}
	
	public void setFlag(AIFlags flag) {
//		if (flag == AIFlags.BACK_OFF) {
//			System.out.println("fwfwefew");
//		}
		FlagData data = flags.get(flag);
		if (data != null) {
			data.elapsed = 0;
		} else {
			flags.put(flag, new FlagData(flag));
		}
	}
	
	public void setFlag(AIFlags flag, float duration) {
		setFlag(flag, duration, null);
	}
	
	public void setFlag(AIFlags flag, float duration, Object custom) {
//		if (flag == AIFlags.DO_NOT_BACK_OFF) {
//			System.out.println("fwfwefew");
//		}
		//if (flag == AIFlags.BACK_OFF) {
//		if (flag == AIFlags.CARRIER_FIGHTER_TARGET) {
//			System.out.println("fwfwefew");
//		}
//		if (flag == AIFlags.BACK_OFF) {
//			System.out.println("fwfwefew");
//		}
//		if (flag == AIFlags.DO_NOT_USE_SHIELDS) {
//			System.out.println("fwfwefew");
//		}
		FlagData data = flags.get(flag);
		if (data != null) {
			data.elapsed = 0;
			data.custom = custom;
		} else {
			data = new FlagData(flag);
			data.durationOverride = duration;
			data.custom = custom;
			flags.put(flag, data);
		}
	}
	
	public Object getCustom(AIFlags flag) {
		FlagData data = flags.get(flag);
		if (data != null) {
			return data.custom;
		}
		return null;
	}
	
	public void removeFlag(AIFlags flag) {
//		if (flag == AIFlags.BACK_OFF) {
//			System.out.println("wefwefwefe");
//		}
		flags.remove(flag);
	}
	
	public void advance(float amount) {
		List<AIFlags> remove = new ArrayList<AIFlags>();
		for (AIFlags flag : flags.keySet()) {
			FlagData data = flags.get(flag);
			data.elapsed += amount;
			//if (data.elapsed > FLAG_DURATION && data.elapsed > data.durationOverride) {
			if ((data.durationOverride <= 0 && data.elapsed > FLAG_DURATION) ||
					(data.durationOverride > 0 && data.elapsed > data.durationOverride)) {
				remove.add(flag);
			}
		}
		for (AIFlags flag : remove) {
//			if (flag == AIFlags.BACK_OFF) {
//				System.out.println("wefwefwefe");
//			}
			flags.remove(flag);
		}
	}
	
	/**
	 * Checks whether a specific AI flag is set.
	 * This is how different ship AI modules communicate with each other, when they need to.
	 * @param flag
	 * @return
	 */
	public boolean hasFlag(AIFlags flag) {
		return flags.containsKey(flag);
	}
}








