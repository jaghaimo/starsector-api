package com.fs.starfarer.api.ui;

public class MapFilterData {
	public boolean starscape;
	public boolean names = true;
	public boolean constellations;
	public boolean factions;
	public boolean missions;
	public boolean fuel;
	public boolean exploration;
	public boolean legend = true;
	public float fuelColorAlphaMult = 1f;
	public float fuelRangeMult = 1f;
	
	public MapFilterData(boolean forNewGame) {
		super();
		
		if (forNewGame) {
			starscape = true;
			names = true;
			factions = true;
			
			missions = false;
			fuel = false;
			exploration = false;
			legend = true;
		}
	}
	
}