package com.fs.starfarer.api.combat;

import java.util.List;

public interface BattleObjectiveEffect {
	
	public static class ShipStatusItem {
		private String title;
		private String description;
		private boolean isDebuff;
		private Object key = null;
		public ShipStatusItem(String title, String description, boolean isDebuff) {
			this.title = title;
			this.description = description;
			this.isDebuff = isDebuff;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public boolean isDebuff() {
			return isDebuff;
		}
		public void setDebuff(boolean isDebuff) {
			this.isDebuff = isDebuff;
		}
		public Object getKey() {
			return key;
		}
		public void setKey(Object key) {
			this.key = key;
		}
	}
	
	void init(CombatEngineAPI engine, BattleObjectiveAPI objective);
	
	/**
	 * Apply/unapply effects here.
	 * @param amount
	 */
	void advance(float amount);
	
	/**
	 * Must return null if this objective has no effect on the passed in ship.
	 * @param ship
	 * @return
	 */
	List<ShipStatusItem> getStatusItemsFor(ShipAPI ship);
	String getLongDescription();
}



