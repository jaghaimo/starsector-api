package com.fs.starfarer.api.impl.campaign.abilities.ai;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.ai.AbilityAIPlugin;
import com.fs.starfarer.api.characters.AbilityPlugin;

public class BaseAbilityAI implements AbilityAIPlugin {

	protected CampaignFleetAPI fleet;
	protected AbilityPlugin ability;
	//protected final ModularFleetAIAPI ai;
	
	public BaseAbilityAI() {
		
	}
//	public BaseAbilityAI(AbilityPlugin ability, ModularFleetAIAPI ai) {
//		this.ability = ability;
//		this.ai = ai;
//		if (ability.getEntity() instanceof CampaignFleetAPI) {
//			this.fleet = (CampaignFleetAPI) ability.getEntity();
//		} else {
//			this.fleet = null;
//		}
//	}
	public void init(AbilityPlugin ability) {
		if (ability.getEntity() instanceof CampaignFleetAPI) {
			this.fleet = (CampaignFleetAPI) ability.getEntity();
		}
		this.ability = ability;
	}

	public void advance(float days) {
		
	}

}






