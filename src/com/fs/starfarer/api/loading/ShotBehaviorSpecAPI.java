package com.fs.starfarer.api.loading;

import org.json.JSONObject;

import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;

public interface ShotBehaviorSpecAPI {

	String getBehavorString();
	JSONObject getParams();
	String getOnExplosionClassName();
	void setOnExplosionClassName(String onExplosionClassName);
	String getOnHitClassName();
	void setOnHitClassName(String effectClassName);
	OnHitEffectPlugin getOnHitEffect();
	ProximityExplosionEffect getOnProximityExplosionEffect();

}
