package com.fs.starfarer.api.combat.listeners;

import com.fs.starfarer.api.combat.DamageType;

public interface ApplyDamageResultAPI {
	float getDamageToHull();
	float getTotalDamageToArmor();
	float getDamageToPrimaryArmorCell();
	float getDamageToShields();
	void setDamageToHull(float damageToHull);
	void setTotalDamageToArmor(float totalDamageToArmor);
	void setDamageToPrimaryArmorCell(float damageToPrimaryArmorCell);
	void setDamageToShields(float damageToShields);
	float getEmpDamage();
	void setEmpDamage(float empDamage);
	DamageType getType();
	void setType(DamageType type);
	float getOverMaxDamageToShields();
	void setOverMaxDamageToShields(float overMaxDamageToShields);
	
	boolean isDps();
	void setDps(boolean isDps);

}
