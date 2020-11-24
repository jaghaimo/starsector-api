package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.characters.PersonAPI;



public interface ReputationActionResponsePlugin {
	public static class ReputationAdjustmentResult {
		public float delta;
		public ReputationAdjustmentResult(float delta) {
			this.delta = delta;
		}
		
	}
	
	ReputationAdjustmentResult handlePlayerReputationAction(Object action, String factionId);
	ReputationAdjustmentResult handlePlayerReputationAction(Object action, PersonAPI person);
}
