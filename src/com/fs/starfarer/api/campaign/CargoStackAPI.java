package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;


public interface CargoStackAPI {
	
	public boolean isWeaponStack();
	/**
	 * Use isCommodityStack() instead.
	 * @return
	 */
	@Deprecated public boolean isResourceStack();
	boolean isCommodityStack();
	boolean isMarineStack();
	boolean isFuelStack();
	boolean isSupplyStack();
	boolean isCrewStack();
	boolean isPersonnelStack();
	
	/**
	 * Returns null if it's not a commodity stack.
	 * @return
	 */
	String getCommodityId();
	
	public float getCargoSpace();
	public float getCargoSpacePerUnit();

	
	public float getSize();
	public float getFree();
	public void setSize(float size);
	public void add(float quantity);
	public void subtract(float quantity);
	
	public float getMaxSize();
	public boolean isFull();

	public CargoAPI.CargoItemType getType();
	public void setType(CargoAPI.CargoItemType type);
	
	/**
	 * If true, it's an empty cargo stack. These get created for spacing, a result of the player moving cargo around.
	 * @return
	 */
	public boolean isNull();
	
	
	/**
	 * Usually a String. Its contents (i.e. the resource id) is how you can tell apart different types of resources.
	 * @return
	 */
	public Object getData();

	// these concepts might not survive an actual economy implementation
//	public int getBaseValue();
//	public int getBaseValuePerUnit();
	
	public String getDisplayName();
	
	/**
	 * @return CargoAPI that contains this stack.
	 */
	public CargoAPI getCargo();
	
	int getBaseValuePerUnit();
	WeaponSpecAPI getWeaponSpecIfWeapon();
	
	/**
	 * Call isSpecialStack() and check specialData.getId().equals(Items.MODSPEC) instead.
	 * @return
	 */
	@Deprecated boolean isModSpecStack();
	boolean isFighterWingStack();
	FighterWingSpecAPI getFighterWingSpecIfWing();
	HullModSpecAPI getHullModSpecIfHullMod();
	CommoditySpecAPI getResourceIfResource();
	
	boolean isSpecialStack();
	SpecialItemData getSpecialDataIfSpecial();
	SpecialItemSpecAPI getSpecialItemSpecIfSpecial();
	
	/**
	 * Returns a new instance of the plugin. The special item plugin is transient and many instances may be
	 * in existence at any time - i.e. one for rendering the item in cargo, one for creating the tooltip,
	 * one for executing a right-click action, etc. 
	 * @return
	 */
	SpecialItemPlugin getPlugin();
	void setCargo(CargoAPI cargo);
	boolean isInPlayerCargo();
	boolean isPickedUp();
	void setPickedUp(boolean isPickedUp);


}
