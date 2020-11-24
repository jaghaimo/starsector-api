package com.fs.starfarer.api.fleet;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface FleetAPI {
	void addFleetMember(FleetMemberType type, String variantId, String optionalName);
}
