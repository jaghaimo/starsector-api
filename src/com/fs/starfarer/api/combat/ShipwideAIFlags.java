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
		
		MANEUVER_TARGET,
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
		DO_NOT_BACK_OFF,
		BACK_OFF,
		BACK_OFF_MIN_RANGE,
		STANDING_OFF_VS_SHIP_ON_MAP_BORDER,
		BACKING_OFF,
		SAFE_VENT,
		OK_TO_CANCEL_SYSTEM_USE_TO_VENT,
		
		SAFE_FROM_DANGER_TIME,
		
		PREFER_LEFT_BROADSIDE,
		PREFER_RIGHT_BROADSIDE,
		
		AUTO_FIRING_AT_PHASE_SHIP,
		AUTO_BEAM_FIRING_AT_PHASE_SHIP,
		
		DELAY_STRIKE_FIRE,
		
		PHASE_ATTACK_RUN,
		//PHASE_ATTACK_RUN_END,
		PHASE_ATTACK_RUN_FROM_BEHIND_DIST_CRITICAL,
		PHASE_ATTACK_RUN_IN_GOOD_SPOT,
		PHASE_ATTACK_RUN_TIMEOUT,
		
		DO_NOT_PURSUE,
		
		IN_CRITICAL_DPS_DANGER,
		//MANEUVER_TARGET,
		
		
		/**
		 * Fighters only, attack run related. 
		 */
		IN_ATTACK_RUN,
		POST_ATTACK_RUN,
		WANTED_TO_SLOW_DOWN,
		FINISHED_SPREADING,

		
		REACHED_WAYPOINT,
		//ATTACK_RUN_WAYPOINT,
		
		
		/**
		 * Whether the ship wants to be escorted by nearby friendlies. 
		 */
		NEEDS_HELP,
		
		SYSTEM_TARGET_COORDS,
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
		//if (flag == AIFlags.BACK_OFF) {
//		if (flag == AIFlags.CARRIER_FIGHTER_TARGET) {
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
			if (data.elapsed > FLAG_DURATION && data.elapsed > data.durationOverride) {
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








