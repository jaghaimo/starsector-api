package com.fs.starfarer.api.fleet;

import com.fs.starfarer.api.campaign.CargoAPI;

public interface CrewCompositionAPI {
	float getCrew();

	
	/**
	 * Generally not set for most crews, but useful to have here for use during boarding/loss calculation/etc.
	 * @param marines
	 */
	void setMarines(float marines);
	/**
	 * Generally not set for most crews, but useful to have here for use during boarding/loss calculation/etc.
	 */
	float getMarines();
	
	
	/**
	 * Generally not set for most crews, but useful to have here for use during boarding/loss calculation/etc.
	 * @param marines
	 */
	void addMarines(float marines);
	
	void removeAllCrew();
	
	void transfer(float quantity, CrewCompositionAPI dest);
	void addCrew(float quantity);
	
	void addAll(CrewCompositionAPI other);
	void removeAll(CrewCompositionAPI other);
	
	void multiplyBy(float mult);
	
	void addToCargo(CargoAPI cargo);
	void removeFromCargo(CargoAPI cargo);
	
	void clear();
	void setCrew(float quantity);
	int getCrewInt();
}
