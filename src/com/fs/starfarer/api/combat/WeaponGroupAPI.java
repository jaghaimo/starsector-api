package com.fs.starfarer.api.combat;

import java.util.List;

import com.fs.starfarer.api.loading.WeaponGroupType;

public interface WeaponGroupAPI {

	boolean isAutofiring();
	void toggleOn();
	void toggleOff();
	List<WeaponAPI> getWeaponsCopy();
	
	WeaponGroupType getType();
	void setType(WeaponGroupType type);
	ShipAPI getShip();
	WeaponAPI getActiveWeapon();
	List<AutofireAIPlugin> getAIPlugins();
	
	AutofireAIPlugin getAutofirePlugin(WeaponAPI weapon);
	boolean isUsingDefaultAI(WeaponAPI weapon);
	WeaponAPI removeWeapon(int index);
	void addWeaponAPI(WeaponAPI weapon);

}
