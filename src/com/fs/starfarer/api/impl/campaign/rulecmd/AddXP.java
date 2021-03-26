package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	AddXP <xp>
 */
public class AddXP extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		long xp = (long) params.get(0).getFloat(memoryMap);
		
		Global.getSector().getPlayerStats().addXP(xp, dialog.getTextPanel());
		
		/*
		StarSystemAPI galatia = Global.getSector().getStarSystem("galatia");
		SectorEntityToken academy = galatia.getEntityById("station_galatia_academy");
		MarketAPI market = academy.getMarket();
		
		
		PersonAPI person2 = Global.getFactory().createPerson();
		person2.setId("assistant_academician");
		person2.setFaction(Factions.INDEPENDENT);
		person2.setGender(Gender.MALE);
		person2.setRankId(Ranks.CITIZEN);
		person2.setPostId(Ranks.POST_ACADEMICIAN);
		person2.getName().setFirst("Alviss");
		person2.getName().setLast("Sebestyen");
		person2.setPortraitSprite(Global.getSettings().getSpriteName("characters", "assistant_academician"));
		
		market.getCommDirectory().addPerson(person2, 1);
		market.addPerson(person2);
		
		CommDirectoryEntryAPI entry = market.getCommDirectory().getEntryForPerson("assistant_academician");
		if (entry != null) {
			entry.setHidden(true);
		}
		*/
		return true;
	}

}
