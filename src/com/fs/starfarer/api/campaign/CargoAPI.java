package com.fs.starfarer.api.campaign;

import java.util.List;

import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.util.MutableValue;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface CargoAPI {

	public static class CargoItemQuantity<T> {
		T item;
		int count;
		
		public CargoItemQuantity(T item, int count) {
			this.item = item;
			this.count = count;
		}
		
		public int getCount() {
			return count;
		}
		
		public T getItem() {
			return item;
		}
	}

//	public static enum CrewXPLevel {
//		GREEN("green_crew", "icon_crew_green", "Green"),
//		REGULAR("regular_crew", "icon_crew_regular", "Regular"),
//		VETERAN("veteran_crew", "icon_crew_veteran", "Veteran"),
//		ELITE("elite_crew", "icon_crew_elite", "Elite");
//		
//		private String id;
//		private String rankIconKey;
//		private String prefix;
//		private CrewXPLevel(String id, String iconKey, String prefix) {
//			this.id = id;
//			this.rankIconKey = iconKey;
//			this.prefix = prefix;
//		}
//		public String getPrefix() {
//			return prefix;
//		}
//		public String getId() {
//			return id;
//		}
//		public String getRankIconKey() {
//			return rankIconKey;
//		}
//	}
	
	
	public static enum CargoItemType {
		RESOURCES,
		WEAPONS,
		FIGHTER_CHIP,
		//MOD_SPEC, // replaced with SPECIAL and SpecialItemData.id for type; see Items class for possible values.
		//BLUEPRINTS,
		SPECIAL,
		NULL,
	}
	
	
	List<CargoItemQuantity<String>> getWeapons();
	
	int getNumWeapons(String id);
	void removeWeapons(String id, int count);
	void addWeapons(String id, int count);
	
	float getSupplies();
	float getFuel();
	
	int getTotalCrew();
	void addCrew(int quantity);
	int getCrew();
	int getMarines();
	void addMarines(int quantity);
	void removeMarines(int quantity);
	
	void addFuel(float quantity);
	void removeFuel(float quantity);
	void addSupplies(float quantity);
	void removeSupplies(float quantity);
	public void removeCrew(int quantity);
	
	public void addItems(CargoAPI.CargoItemType itemType, Object data, float quantity);
	public boolean removeItems(CargoAPI.CargoItemType itemType, Object data, float quantity);
	public float getQuantity(CargoAPI.CargoItemType type, Object data);
	
	
	
	public void addMothballedShip(FleetMemberType type, String variantOrWingId, String optionalName);
	
	void initMothballedShips(String factionId);
	/**
	 * Call initMothballedShips(String factionId) before using this method.
	 * @return
	 */
	FleetDataAPI getMothballedShips();
	
	
	public void clear();
	
	/**
	 * Use SectorEntityToken.setFreeTransfer() instead.
	 * Whether moving items to and from this entity has a cost.
	 * @param freeTransfer
	 */
	@Deprecated public void setFreeTransfer(boolean freeTransfer);
	@Deprecated public boolean isFreeTransfer();
	
	public MutableValue getCredits();
	
	public List<CargoStackAPI> getStacksCopy();

	void gainCrewXP(float xp);

	void sort();

	float getMaxFuel();
	float getMaxCapacity();
	float getMaxPersonnel();

	float getSpaceUsed();
	float getSpaceLeft();

	boolean isEmpty();

	void removeEmptyStacks();

	void addFromStack(CargoStackAPI stack);

	void addCommodity(String commodityId, float quantity);

	float getCommodityQuantity(String id);

	int getTotalPersonnel();

	void removeCommodity(String id, float quantity);

	void removeStack(CargoStackAPI stack);

	List<CargoItemQuantity<String>> getFighters();

	int getNumFighters(String id);

	void addAll(CargoAPI other);

	void addFighters(String id, int count);

	void addHullmods(String id, int count);

	int getFreeCrewSpace();

	int getFreeFuelSpace();

	void addSpecial(SpecialItemData data, float quantity);

	CargoAPI createCopy();

	void initPartialsIfNeeded();

	void removeAll(CargoAPI other);

	void addAll(CargoAPI other, boolean includeMothballedShips);

	CargoAPI getOrigSource();
	void setOrigSource(CargoAPI origSource);

	FleetDataAPI getFleetData();

	void updateSpaceUsed();

	void removeFighters(String id, int count);

	boolean isUnlimitedStacks();
	void setUnlimitedStacks(boolean unlimitedStacks);	
}












