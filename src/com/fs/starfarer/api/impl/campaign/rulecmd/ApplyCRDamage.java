package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 *
 *	ApplyCRDamage <fleet points> <quantity multiplier> <cr multiplier> <description string>
 *
 *  ApplyCRDamage $entity.fleetPoints 0.2 1 "Vindictive search"
 *  The first param is the base number of fleet points for the amount of CR damage to do - 
 *  so, for an inspection it's based on the fleet points of the inspecting fleet, but it 
 *  could also just be a flat number, or based on the player fleet if 
 *  we wanted something like "half your ships take CR damage"
 *  
 *  The second param is the multiplier for that number (basically, making up for the fact that you 
 *  can't say "$fleetPoints * 0.2" in rules - so the multiplication happens inside the command. 
 *  So you could have a more or less vindictive inspection by tuning this param
 *  
 *  The third param is the multiplier for the CR damage each selected ship takes; 
 *  with it == 1 it's "half the recovery cost plus 1-10%"
 *  
 *  Fourth param is the description for this CR loss event in the ship's CR tooltip 
 */
public class ApplyCRDamage extends BaseCommandPlugin {

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		//CampaignFleetAPI other = (CampaignFleetAPI) dialog.getInteractionTarget();
		
		TextPanelAPI text = dialog.getTextPanel();
		//Color red = Misc.getNegativeHighlightColor();
		
		float baseFP = params.get(0).getFloat(memoryMap);
		float fpMult = params.get(1).getFloat(memoryMap);
		float crMult = params.get(2).getFloat(memoryMap);
		String desc = params.get(3).getString(memoryMap);
		
		long seed;
		if (dialog.getInteractionTarget() != null) {
			seed = Misc.getSalvageSeed(dialog.getInteractionTarget());
			seed += (ruleId == null ? 0 : ruleId.hashCode());
			seed /= 321L;
			seed *= (Global.getSector().getClock().getMonth() + 10 + (baseFP * fpMult * crMult) * 10f);
		} else {
			seed = Misc.genRandomSeed();
		}
		
		Random random = Misc.getRandom(seed, 7);
		
		applyCRDamage(playerFleet, baseFP * fpMult, crMult, desc, text, random);
		
		return true;
	}
	
	public static void applyCRDamage(CampaignFleetAPI fleet, float damageFP, float crMult, String desc, TextPanelAPI text, Random random) {
		List<FleetMemberAPI> shipsToDamage = new ArrayList<FleetMemberAPI>();
		WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<FleetMemberAPI>(random);
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.isMothballed() && member.getRepairTracker().getBaseCR() < 0.2f) continue;
			picker.add(member, member.getFleetPointCost());
		}

		float totalDamage = damageFP;
		float picked = 0f;
		while (picked < totalDamage && !picker.isEmpty()) {
			FleetMemberAPI pick = picker.pickAndRemove();
			shipsToDamage.add(pick);
			picked += pick.getFleetPointCost();
		}
		
		
		for (FleetMemberAPI member : shipsToDamage) {
			float crLost = Math.min(member.getRepairTracker().getBaseCR(), member.getDeployCost() * 0.5f);
			crLost += 0.01f * (float)random.nextInt(10);
			crLost *= crMult;
			if (crLost > 0) {
				member.getRepairTracker().applyCREvent(-crLost, desc);
				if (text != null) {
					AddRemoveCommodity.addCRLossText(member, text, crLost);
				}
			}
		}
	}

}







