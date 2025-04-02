package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * For Tri-Tachyon (inc) Business (c)
 * 
 *	TT_CMD <action> <parameters>
 */
public class TT_CMD extends BaseCommandPlugin {
	
	protected String ARROYO = "arroyo";
	protected String SUN = "sun";
	protected String GLAMOR_ROTANEV = "glamor_rotanev";

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		CargoAPI cargo = pf.getCargo();
		
		String action = params.get(0).getString(memoryMap);
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memory == null) return false; // should not be possible unless there are other big problems already
		
//		MarketAPI market = dialog.getInteractionTarget().getMarket();
//		StarSystemAPI system = null;
//		if (dialog.getInteractionTarget().getContainingLocation() instanceof StarSystemAPI) {
//			system = (StarSystemAPI) dialog.getInteractionTarget().getContainingLocation();
//		}
				
		if ("isArroyoContact".equals(action)) {
			return isArroyoContact();
		} else if ("giveColossus".equals(action)) {
			giveColossus(dialog, params, memoryMap);
			return true;
		} else if ("givePhaeton".equals(action)) {
			givePhaeton(dialog, params, memoryMap);
			return true;
		}
		
		return false;
	}

	protected void giveColossus(InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		ShipVariantAPI v = Global.getSettings().getVariant("colossus_Standard").clone();
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
		
	}

	protected void givePhaeton(InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		//System.out.print("givePhaeton called");
		ShipVariantAPI v = Global.getSettings().getVariant("phaeton_Standard").clone();
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
	}

	protected boolean isArroyoContact() {
		PersonAPI person = People.getPerson(ARROYO); 
		boolean isContact = ContactIntel.playerHasContact(person, true);
		//System.out.print("isArroyoContact? result: " +isContact );
		return isContact; 
	}
	
}
