package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	NGCAddShip <variant id>
 */
public class NGCAddShipSilent extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");
		
//		data.getPerson().setPortraitSprite("graphics/portraits/portrait_ai2b.png");  
//		data.getPerson().getName().setFirst("First");  
//		data.getPerson().getName().setLast("Last");
		
		
		String vid = params.get(0).getString(memoryMap);
		FleetMemberType type = FleetMemberType.SHIP;
		if (vid.endsWith("_wing")) {
			type = FleetMemberType.FIGHTER_WING; 
		}
		data.addStartingFleetMember(vid, type);
		
//		ShipVariantAPI variant = Global.getSettings().getVariant(vid);
//		AddRemoveCommodity.addFleetMemberGainText(variant, dialog.getTextPanel());
		
		return true;
	}

}
