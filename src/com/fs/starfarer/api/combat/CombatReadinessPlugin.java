package com.fs.starfarer.api.combat;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface CombatReadinessPlugin {
	
	public static enum CREffectDetailType {
		PENALTY,
		BONUS,
		NEUTRAL,
	}
	
	public static class CREffectDetail {
		private String desc;
		private String value;
		private CREffectDetailType type;
		
		public CREffectDetail(String desc, String value, CREffectDetailType type) {
			this.desc = desc;
			this.value = value;
			this.type = type;
		}
		public String getDesc() {
			return desc;
		}
		public String getValue() {
			return value;
		}
		public CREffectDetailType getType() {
			return type;
		}
	}
	
	public static class CREffectDescriptionForTooltip {
		private String string;
		private List<String> highlights = new ArrayList<String>();
		public String getString() {
			return string;
		}
		public void setString(String string) {
			this.string = string;
		}
		public List<String> getHighlights() {
			return highlights;
		}
	}
	
	
	public static class CRStatusItemData {
		private String title;
		private String text;
		private String iconName;
		private boolean isDebuff;
		private Object idToken;
		public CRStatusItemData(Object idToken, String iconName, 
								String title, String text, boolean isDebuff) {
			this.title = title;
			this.text = text;
			this.iconName = iconName;
			this.isDebuff = isDebuff;
			this.idToken = idToken;
		}
		public String getTitle() {
			return title;
		}
		public String getText() {
			return text;
		}
		public String getIconName() {
			return iconName;
		}
		public boolean isDebuff() {
			return isDebuff;
		}
		public Object getIdToken() {
			return idToken;
		}
	}
	
	void applyMaxCRCrewModifiers(FleetMemberAPI member);
	
	
	void applyCRToStats(float cr, MutableShipStatsAPI stats, HullSize hullSize);
	void applyCRToShip(float cr, ShipAPI ship);
	
	List<CRStatusItemData> getCRStatusDataForShip(ShipAPI ship);
	
	float getMalfunctionThreshold(MutableShipStatsAPI stats);
	float getCriticalMalfunctionThreshold(MutableShipStatsAPI stats);
	
	
	/**
	 * Used to construct part of the CR tooltip in the fleet view.
	 * 
	 * @param cr from 0 to 1
	 * @param shipOrWing "ship" or "fighter wing".
	 * @return
	 */
	CREffectDescriptionForTooltip getCREffectDescription(float cr, String shipOrWing, FleetMemberAPI member);
	
	List<CREffectDetail> getCREffectDetails(float cr, FleetMemberAPI member);
	float getMissileLoadedFraction(MutableShipStatsAPI stats, float cr);
	
	
	boolean isOkToPermanentlyDisable(ShipAPI ship, Object module);
}



