/**
 * 
 */
package com.fs.starfarer.api.campaign.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;

public interface NavigationModulePlugin {
	void clearAvoidList();
	void unavoidEntity(SectorEntityToken entity);
	void avoidEntity(SectorEntityToken entity, float minRange, float maxRange, float duration);
	void avoidLocation(LocationAPI containingLocation, Vector2f loc, float minRange, float maxRange, float duration);
	
	void setPreferredHeading(float heading);
	void setDestination(Vector2f loc);
	float getPreferredHeading(float heading);
	
	float getCalculatedHeading();
	Vector2f getClickToMoveLocation();
	
	
	void advance(float days);
	void doNotAvoid(SectorEntityToken entity, float days);
	
}