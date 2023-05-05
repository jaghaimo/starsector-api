package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class ShowDefaultVisual extends BaseCommandPlugin {

	public ShowDefaultVisual() {
		
	}
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		SectorEntityToken target = dialog.getInteractionTarget();
		
		if (target.getCustomInteractionDialogImageVisual() != null) {
			dialog.getVisualPanel().showImageVisual(target.getCustomInteractionDialogImageVisual());
		} else {
			if (target.getMarket() != null) {
				target = target.getMarket().getPlanetEntity();
			}
			if (target instanceof PlanetAPI) {
				//Global.getSettings().setBoolean("3dPlanetBGInInteractionDialog", true);
				if (!Global.getSettings().getBoolean("3dPlanetBGInInteractionDialog")) {
					dialog.getVisualPanel().showPlanetInfo((PlanetAPI) target);
				}
				//dialog.getVisualPanel().showLargePlanet((PlanetAPI) target);
				
			} else if (target instanceof CampaignFleetAPI) {
				CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
				CampaignFleetAPI otherFleet = (CampaignFleetAPI) target;
				//dialog.getVisualPanel().setVisualFade(0.25f, 0.25f);
				//dialog.getVisualPanel().showFleetInfo((String)null, playerFleet, (String)null, otherFleet, null);
				showFleetInfo(dialog, playerFleet, otherFleet);
				
				//if (otherFleet )
			}
//			else if (target instanceof XXXXX) {
//				dialog.getVisualPanel().showXXXXX((XXXXX) target);
//			}
		}
	
		return true;
	}
	
	protected void showFleetInfo(InteractionDialogAPI dialog, CampaignFleetAPI player, CampaignFleetAPI other) {
		BattleAPI b = player.getBattle();
		if (b == null) b = other.getBattle();
		if (b != null && b.isPlayerInvolved()) {
			String titleOne = "Your forces";
			if (b.isPlayerInvolved() && b.getPlayerSide().size() > 1) {
				titleOne += ", with allies";
			}
			if (!Global.getSector().getPlayerFleet().isValidPlayerFleet()) {
				titleOne = "Allied forces";
			}
			String titleTwo = null;
			if (b.getPrimary(b.getNonPlayerSide()) != null) {
				titleTwo = b.getPrimary(b.getNonPlayerSide()).getNameWithFactionKeepCase();
			}
			if (b.getNonPlayerSide().size() > 1) titleTwo += ", with allies";
			dialog.getVisualPanel().showFleetInfo(titleOne, b.getPlayerCombined(), Misc.ucFirst(titleTwo), b.getNonPlayerCombined(), null);
		} else {
			if (b != null) {
				String titleOne = b.getPrimary(b.getSideOne()).getNameWithFactionKeepCase();
				if (b.getSideOne().size() > 1) titleOne += ", with allies";
				String titleTwo = b.getPrimary(b.getSideTwo()).getNameWithFactionKeepCase();
				if (b.getSideTwo().size() > 1) titleTwo += ", with allies";
				
				FleetEncounterContext fake = new FleetEncounterContext();
				fake.setBattle(b);
				dialog.getVisualPanel().showPreBattleJoinInfo(null, player, Misc.ucFirst(titleOne), Misc.ucFirst(titleTwo), fake);
			} else {
				dialog.getVisualPanel().showFleetInfo((String)null, player, (String)null, other, null);
			}
		}
	}

}


