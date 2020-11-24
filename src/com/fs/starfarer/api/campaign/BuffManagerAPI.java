package com.fs.starfarer.api.campaign;

import java.util.List;

import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface BuffManagerAPI {
	public static interface Buff {
		void apply(FleetMemberAPI member);
		//void unapply(MutableShipStatsAPI stats);
		String getId();
		boolean isExpired();
		void advance(float days);
	}
	
	public static class TempBuff implements Buff {
		private Buff buff;
		private float dur;
		public TempBuff(Buff buff, float dur) {
			this.buff = buff;
			this.dur = dur;
		}
		public void advance(float days) {
			dur -= days;
		}
		public void apply(FleetMemberAPI member) {
			buff.apply(member);
		}
		public String getId() {
			return buff.getId();
		}
		public boolean isExpired() {
			return buff.isExpired() || dur <= 0;
		}
	}
	
	Buff getBuff(String id);
	void addBuff(Buff b);
	void removeBuff(String id);
	
	
	/**
	 * Won't trigger an update of other possibly-related fleet stats.
	 * Useful when modifying a stat that has no effect beyond itself (i.e. a combat stat in campaign).
	 * @param b
	 */
	void addBuffOnlyUpdateStat(Buff b);
	List<Buff> getBuffs();
	void advance(float days);
}




