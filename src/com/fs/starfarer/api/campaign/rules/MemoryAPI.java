package com.fs.starfarer.api.campaign.rules;

import java.util.Collection;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;


public interface MemoryAPI {
	void unset(String key);
	void expire(String key, float days);
	
	boolean contains(String key);
	boolean is(String key, Object value);
	boolean is(String key, float value);
	boolean is(String key, boolean value);
	/**
	 * Never expires.
	 * @param key
	 * @param value
	 */
	void set(String key, Object value);
	
	/**
	 * With expiration.
	 * @param key
	 * @param value
	 * @param expire
	 */
	void set(String key, Object value, float expire);
	Object get(String key);
	String getString(String key);
	float getFloat(String key);
	boolean getBoolean(String key);
	long getLong(String key);
	
	Vector2f getVector2f(String key);
	SectorEntityToken getEntity(String key);
	CampaignFleetAPI getFleet(String key);
	
	/**
	 * Includes both endpoints.
	 * @param key
	 * @param from
	 * @param to
	 * @return
	 */
	boolean between(String key, float min, float max);
	
	
	Collection<String> getKeys();
	float getExpire(String key);
	
	
	void advance(float amount);
	
	
	/**
	 * Can be called multiple times for a key.
	 * 
	 * If this is called, then key will be removed from memory when NONE of the
	 * requiredKeys are left in memory.
	 * 
	 * @param key
	 * @param requiredKey
	 */
	void addRequired(String key, String requiredKey);
	void removeRequired(String key, String requiredKey);
	boolean isEmpty();
	Set<String> getRequired(String key);
	void removeAllRequired(String key);
	void clear();
	int getInt(String key);
}



