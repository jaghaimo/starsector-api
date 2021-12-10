package com.fs.starfarer.api.combat;

import java.awt.Color;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

public interface ShipHullSpecAPI {
	
	public interface ShieldSpecAPI {
		float getPhaseCost();
		float getPhaseUpkeep();
		float getFluxPerDamageAbsorbed();
		ShieldType getType();
		Color getRingColor();
		Color getInnerColor();
		float getUpkeepCost();
		float getArc();
		float getRadius();
		float getCenterX();
		float getCenterY();
	}
	
	public interface EngineSpecAPI {
		float getTurnAcceleration();
		void setTurnAcceleration(float turnAcceleration);
		float getMaxTurnRate();
		void setMaxTurnRate(float maxTurnRate);
		float getAcceleration();
		void setAcceleration(float acceleration);
		float getDeceleration();
		void setDeceleration(float deceleration);
		float getMaxSpeed();
		void setMaxSpeed(float maxSpeed);
		String getManeuverabilityDisplayName(MutableShipStatsAPI stats);
	}
	
	
	public static enum ShipTypeHints {
		FREIGHTER,
		TANKER,
		LINER,
		TRANSPORT,
		CIVILIAN,
		CARRIER,
		COMBAT,
		NO_AUTO_ESCORT,
		UNBOARDABLE,
		STATION,
		SHIP_WITH_MODULES,
		HIDE_IN_CODEX,
		UNDER_PARENT,
		INDEPENDENT_ROTATION,
		ALWAYS_PANIC,
		WEAPONS_FRONT_TO_BACK,
		WEAPONS_BACK_TO_FRONT,
		DO_NOT_SHOW_MODULES_IN_FLEET_LIST,
		RENDER_ENGINES_BELOW_HULL,
		
		NO_NEURAL_LINK,
		
		/** for phase ships w/ a different type of phase system to show up under 
		 * the "Phase" tag in doctrine/production
		 *  */
		PHASE,
		PLAY_FIGHTER_OVERLOAD_SOUNDS, /** by default, fighters don't play overload sounds */
	}
	
	
	public ShieldSpecAPI getShieldSpec();
	
	ShieldType getDefenseType();
	String getHullId();
	String getHullName();
	
	EnumSet<ShipTypeHints> getHints();
	
	float getNoCRLossTime();
	float getCRToDeploy();
	float getCRLossPerSecond();
	
	float getBaseValue();
	
	int getOrdnancePoints(MutableCharacterStatsAPI stats);
	HullSize getHullSize();
	float getHitpoints();
	float getArmorRating();
	float getFluxCapacity();
	float getFluxDissipation();
	
	ShieldType getShieldType();
	
	List<WeaponSlotAPI> getAllWeaponSlotsCopy();
	
	String getSpriteName();
	boolean isCompatibleWithBase();
	String getBaseHullId();
	float getBaseShieldFluxPerDamageAbsorbed();
	String getHullNameWithDashClass();
	boolean hasHullName();
	float getBreakProb();
	float getMinPieces();
	float getMaxPieces();

	int getFighterBays();
	float getMinCrew();
	float getMaxCrew();
	float getCargo();
	float getFuel();
	float getFuelPerLY();

	boolean isDHull();
	boolean isDefaultDHull();

	void setDParentHullId(String dParentHullId);
	String getDParentHullId();

	ShipHullSpecAPI getDParentHull();
	ShipHullSpecAPI getBaseHull();

	List<String> getBuiltInWings();

	boolean isBuiltInWing(int index);

	String getDesignation();

	boolean hasDesignation();

	boolean isRestoreToBase();
	void setRestoreToBase(boolean restoreToBase);

	Vector2f getModuleAnchor();
	void setModuleAnchor(Vector2f moduleAnchor);
	void setCompatibleWithBase(boolean compatibleWithBase);

	Set<String> getTags();
	void addTag(String tag);
	boolean hasTag(String tag);

	float getRarity();

	String getNameWithDesignationWithDashClass();

	String getDescriptionId();

	boolean isBaseHull();

	void setManufacturer(String manufacturer);

	String getManufacturer();

	int getFleetPoints();

	List<String> getBuiltInMods();

	WeaponSlotAPI getWeaponSlotAPI(String slotId);

	String getDescriptionPrefix();

	boolean isBuiltInMod(String modId);

	void addBuiltInMod(String modId);

	boolean isCivilianNonCarrier();

	void setHullName(String hullName);
	void setDesignation(String designation);

	boolean isPhase();

	String getShipFilePath();

	String getTravelDriveId();
	void setTravelDriveId(String travelDriveId);

	EngineSpecAPI getEngineSpec();

	float getSuppliesToRecover();

	void setSuppliesToRecover(float suppliesToRecover);

	float getSuppliesPerMonth();

	void setSuppliesPerMonth(float suppliesPerMonth);

	void setRepairPercentPerDay(float repairPercentPerDay);

	void setCRToDeploy(float crToDeploy);

	float getNoCRLossSeconds();

	void setNoCRLossSeconds(float noCRLossSeconds);

	void setCRLossPerSecond(float crLossPerSecond);

	HashMap<String, String> getBuiltInWeapons();

	boolean isBuiltIn(String slotId);

	void addBuiltInWeapon(String slotId, String weaponId);

	String getShipDefenseId();

	void setShipDefenseId(String shipDefenseId);

	String getShipSystemId();

	void setShipSystemId(String shipSystemId);

}
