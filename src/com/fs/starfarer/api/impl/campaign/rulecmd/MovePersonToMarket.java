package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI.PersonDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Can pass in an invalid person ID to use the active person instead.
 *  
 * Method *should* handle the case when the person is a player contact.
 *  
 * MovePersonToMarket <person id> <optional market id>
 */
public class MovePersonToMarket extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		String personId = params.get(0).getString(memoryMap);
		String marketId = null;
		if (params.size() > 1) {
			marketId = params.get(1).getString(memoryMap);
		}
		
		SectorEntityToken entity = dialog.getInteractionTarget();
		
		MarketAPI market = null;
		if (entity != null) market = entity.getMarket();
		if (marketId != null) {
			market = Global.getSector().getEconomy().getMarket(marketId);
		}
		if (market == null && "ga_market".equals(marketId)) { // horrible hack time
			market = Global.getSector().getImportantPeople().getPerson(People.BAIRD).getMarket();
		}
		
		if (market == null) return false;
		
		PersonAPI person = null;
		PersonDataAPI data = Global.getSector().getImportantPeople().getData(personId);
		if (data != null) person = data.getPerson();
		
		if (person == null && entity != null) {
			person = entity.getActivePerson();
		}
		if (person == null) return false;
		
		Misc.moveToMarket(person, market, false);
		return true;
	}

}








