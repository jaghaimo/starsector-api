package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.People;

public abstract class PersonalFleetScript implements EveryFrameScript, FleetEventListener {
	
	protected String personId;
	
	protected float minRespawnDelayDays = 1f;
	protected float maxRespawnDelayDays = 2f;
	
	protected float minFailedSpawnRespawnDelayDays = 1f;
	protected float maxFailedSpawnRespawnDelayDays = 2f;
	protected float currDelay;
	protected CampaignFleetAPI fleet;
	protected Random random = new Random();
	protected boolean done = false;
	
	public PersonalFleetScript(String personId) {
		this.personId = personId;
		Global.getSector().addScript(this);
	}
	
	public PersonAPI getPerson() {
		return People.getPerson(personId);
	}
	
	public boolean isDone() {
		return done;
	}

	public boolean runWhilePaused() {
		return false;
	}

	public void advance(float amount) {
		if (amount <= 0 || isDone()) return;
		
		if (fleet != null && !fleet.isAlive()) {
			fleet = null;
		}
		
		if (fleet == null) {
			float days = Global.getSector().getClock().convertToDays(amount);
			currDelay -= days;
			if (currDelay <= 0f) {
				currDelay = 0f;
				
				if (shouldScriptBeRemoved() || getPerson() == null) {
					done = true;
					return;
				}
				
				if (canSpawnFleetNow()) {
					fleet = spawnFleet();
					if (fleet != null) {
						fleet.addEventListener(this);
					}
				}
				if (fleet == null) {
					currDelay = minFailedSpawnRespawnDelayDays + 
							   (maxFailedSpawnRespawnDelayDays - minFailedSpawnRespawnDelayDays) * random.nextFloat();
				}
			}
		}
	}

	
	public abstract CampaignFleetAPI spawnFleet();
	public abstract boolean canSpawnFleetNow();
	public abstract boolean shouldScriptBeRemoved(); 

	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (fleet == this.fleet) {
			this.fleet = null;
			currDelay = minRespawnDelayDays + 
					   (maxRespawnDelayDays - minRespawnDelayDays) * random.nextFloat();
		}
	}

	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public float getMinRespawnDelayDays() {
		return minRespawnDelayDays;
	}

	public void setMinRespawnDelayDays(float minRespawnDelayDays) {
		this.minRespawnDelayDays = minRespawnDelayDays;
	}

	public float getMaxRespawnDelayDays() {
		return maxRespawnDelayDays;
	}

	public void setMaxRespawnDelayDays(float maxRespawnDelayDays) {
		this.maxRespawnDelayDays = maxRespawnDelayDays;
	}

	public float getMinFailedSpawnRespawnDelayDays() {
		return minFailedSpawnRespawnDelayDays;
	}

	public void setMinFailedSpawnRespawnDelayDays(float minFailedSpawnRespawnDelayDays) {
		this.minFailedSpawnRespawnDelayDays = minFailedSpawnRespawnDelayDays;
	}

	public float getMaxFailedSpawnRespawnDelayDays() {
		return maxFailedSpawnRespawnDelayDays;
	}

	public void setMaxFailedSpawnRespawnDelayDays(float maxFailedSpawnRespawnDelayDays) {
		this.maxFailedSpawnRespawnDelayDays = maxFailedSpawnRespawnDelayDays;
	}

	public float getCurrDelay() {
		return currDelay;
	}

	public void setCurrDelay(float currDelay) {
		this.currDelay = currDelay;
	}

	public CampaignFleetAPI getFleet() {
		return fleet;
	}

	public void setFleet(CampaignFleetAPI fleet) {
		this.fleet = fleet;
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}
	
	
}

