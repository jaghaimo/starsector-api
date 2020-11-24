package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * ShowImageVisual <category> <key>
 */
public class ShowImageVisual extends BaseCommandPlugin {

	public ShowImageVisual() {
		
	}
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		
		String category = "illustrations";
		String key = null;
		
		if (params.size() <= 1) {
			key = params.get(0).string;
		} else {
			category = params.get(0).string;
			key = params.get(1).string;
		}
		
		SpriteAPI sprite = Global.getSettings().getSprite(category, key);
		dialog.getVisualPanel().showImagePortion(category, key, sprite.getWidth(), sprite.getHeight(), 0, 0, 480, 300);
		
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


