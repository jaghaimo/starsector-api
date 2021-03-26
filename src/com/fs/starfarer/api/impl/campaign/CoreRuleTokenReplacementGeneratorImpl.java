package com.fs.starfarer.api.impl.campaign;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.rules.RuleTokenReplacementGeneratorPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;

public class CoreRuleTokenReplacementGeneratorImpl implements RuleTokenReplacementGeneratorPlugin {

	public Map<String, String> getTokenReplacements(String ruleId, Object entity, Map<String, MemoryAPI> memoryMap) {
		
		SectorEntityToken target = null;
		PersonAPI person = null;
		MarketAPI market = null;
		FactionAPI faction = null;
		FactionAPI personFaction = null;
		if (entity instanceof SectorEntityToken) {
			target = (SectorEntityToken) entity;
			market = target.getMarket();
			faction = target.getFaction();
		}
		
		if (entity instanceof CampaignFleetAPI && market == null) {
			market = Misc.getSourceMarket((CampaignFleetAPI) entity);
		}
		
		if (entity instanceof SectorEntityToken) {
			person = ((SectorEntityToken) entity).getActivePerson();
		}
		
		if (person == null) {
			if (entity instanceof CampaignFleetAPI) {
				person = ((CampaignFleetAPI) entity).getCommander();
			} else if (entity instanceof PersonAPI ){
				person = (PersonAPI) entity; // can't actually happen as entity is always a SectorEntityToken
			}
		}
		
		if (person != null) {
			personFaction = person.getFaction();
		}
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		Map<String, String> map = new HashMap<String, String>();

		map.put("$Playername", Global.getSector().getCharacterData().getName());
		map.put("$playername", Global.getSector().getCharacterData().getName());
		map.put("$playerName", Global.getSector().getCharacterData().getName());
		map.put("$PlayerName", Global.getSector().getCharacterData().getName());
		
		map.put("$playerFirstName", Global.getSector().getCharacterData().getPerson().getName().getFirst());
		map.put("$playerFirstname", Global.getSector().getCharacterData().getPerson().getName().getFirst());
		map.put("$playerLastName", Global.getSector().getCharacterData().getPerson().getName().getLast());
		map.put("$playerLastname", Global.getSector().getCharacterData().getPerson().getName().getLast());
		
		PersonAPI playerPerson = Global.getSector().getPlayerPerson();
		if (playerPerson != null) {
			if (playerPerson.isMale()) {
				map.put("$playerSirOrMadam", "sir");
				map.put("$PlayerSirOrMadam", "Sir");
				
				map.put("$playerBrotherOrSister", "brother");
				map.put("$PlayerBrotherOrSister", "Brother");
				
				map.put("$playerHisOrHer", "his");
				map.put("$PlayerHisOrHer", "His");
				map.put("$playerHimOrHer", "him");
				map.put("$PlayerHimOrHer", "Him");
				map.put("$playerHeOrShe", "he");
				map.put("$PlayerHeOrShe", "He");
			} else {
				map.put("$playerSirOrMadam", "ma'am");
				map.put("$PlayerSirOrMadam", "Ma'am");
				
				map.put("$playerBrotherOrSister", "sister");
				map.put("$PlayerBrotherOrSister", "Sister");
				
				map.put("$playerHisOrHer", "her");
				map.put("$PlayerHisOrHer", "Her");
				map.put("$playerHimOrHer", "her");
				map.put("$PlayerHimOrHer", "Her");
				map.put("$playerHeOrShe", "she");
				map.put("$PlayerHeOrShe", "She");
			}
		}
		
		if (market != null) {
			map.put("$market", market.getName());
			
			if (target.getLocation() instanceof StarSystemAPI) {
				map.put("$marketSystem", ((StarSystemAPI)target.getLocation()).getBaseName() + " star system");
			} else {
				map.put("$marketSystem", "hyperspace");
			}
			
			MemoryAPI mem = market.getMemoryWithoutUpdate();
			if (mem.contains(MemFlags.MEMORY_KEY_PLAYER_HOSTILE_ACTIVITY_NEAR_MARKET)) {
				float expire = mem.getExpire(MemFlags.MEMORY_KEY_PLAYER_HOSTILE_ACTIVITY_NEAR_MARKET);
				String days = Misc.getAtLeastStringForDays((int) expire);
				map.put("$playerHostileTimeoutStr", days.toLowerCase());
				mem.set("$playerHostileTimeoutStr", days.toLowerCase(), 0);
			}
		}
		
		if (target != null) {
			map.put("$entityName", target.getName());
			map.put("$fleetName", target.getName().toLowerCase());
			map.put("$relayName", target.getName());
			
			
			if (target.getCustomEntityType() != null) {
				if (target.getCustomEntitySpec() != null) {
					CustomEntitySpecAPI spec = target.getCustomEntitySpec();
					map.put("$nameInText", spec.getNameInText());
					map.put("$shortName", spec.getShortName());
					map.put("$isOrAre", spec.getIsOrAre());
					map.put("$aOrAn", spec.getAOrAn());
				}
			}
			
//			map.put("$factionEntityPrefix", target.getFaction().getEntityNamePrefix());
//			map.put("$FactionEntityPrefix", Misc.ucFirst(target.getFaction().getEntityNamePrefix()));
			if (target.getFaction() != null) {
				String factionName = target.getFaction().getEntityNamePrefix();
				if (factionName == null || factionName.isEmpty()) {
					factionName = target.getFaction().getDisplayName();
				}

				map.put("$factionAOrAn", target.getFaction().getPersonNamePrefixAOrAn());
				
				map.put("$factionIsOrAre", target.getFaction().getDisplayNameIsOrAre());
				
				map.put("$faction", factionName);
				map.put("$ownerFaction", factionName);
				map.put("$marketFaction", factionName);
				map.put("$Faction", Misc.ucFirst(factionName));
				map.put("$OwnerFaction", Misc.ucFirst(factionName));
				map.put("$MarketFaction", Misc.ucFirst(factionName));
				map.put("$theFaction", target.getFaction().getDisplayNameWithArticle());
				map.put("$theOwnerFaction", target.getFaction().getDisplayNameWithArticle());
				map.put("$theMarketFaction", target.getFaction().getDisplayNameWithArticle());
				map.put("$TheFaction", Misc.ucFirst(target.getFaction().getDisplayNameWithArticle()));
				map.put("$TheOwnerFaction", Misc.ucFirst(target.getFaction().getDisplayNameWithArticle()));
				map.put("$TheMarketFaction", Misc.ucFirst(target.getFaction().getDisplayNameWithArticle()));
				
				map.put("$factionLong", target.getFaction().getDisplayNameLong());
				map.put("$FactionLong", Misc.ucFirst(target.getFaction().getDisplayNameLong()));
				map.put("$theFactionLong", target.getFaction().getDisplayNameLongWithArticle());
				map.put("$TheFactionLong", Misc.ucFirst(target.getFaction().getDisplayNameLongWithArticle()));
			}
		}
		
		if (target != null) {
			map.put("$entityName", target.getName());
			map.put("$fleetName", target.getName().toLowerCase());
			map.put("$relayName", target.getName());
			
//			map.put("$factionEntityPrefix", target.getFaction().getEntityNamePrefix());
//			map.put("$FactionEntityPrefix", Misc.ucFirst(target.getFaction().getEntityNamePrefix()));
			if (target.getFaction() != null) {
				String factionName = target.getFaction().getEntityNamePrefix();
				if (factionName == null || factionName.isEmpty()) {
					factionName = target.getFaction().getDisplayName();
				}

				map.put("$factionIsOrAre", target.getFaction().getDisplayNameIsOrAre());
				
				map.put("$faction", factionName);
				map.put("$ownerFaction", factionName);
				map.put("$marketFaction", factionName);
				map.put("$Faction", Misc.ucFirst(factionName));
				map.put("$OwnerFaction", Misc.ucFirst(factionName));
				map.put("$MarketFaction", Misc.ucFirst(factionName));
				map.put("$theFaction", target.getFaction().getDisplayNameWithArticle());
				map.put("$theOwnerFaction", target.getFaction().getDisplayNameWithArticle());
				map.put("$theMarketFaction", target.getFaction().getDisplayNameWithArticle());
				map.put("$TheFaction", Misc.ucFirst(target.getFaction().getDisplayNameWithArticle()));
				map.put("$TheOwnerFaction", Misc.ucFirst(target.getFaction().getDisplayNameWithArticle()));
				map.put("$TheMarketFaction", Misc.ucFirst(target.getFaction().getDisplayNameWithArticle()));
				
				map.put("$factionLong", target.getFaction().getDisplayNameLong());
				map.put("$FactionLong", Misc.ucFirst(target.getFaction().getDisplayNameLong()));
				map.put("$theFactionLong", target.getFaction().getDisplayNameLongWithArticle());
				map.put("$TheFactionLong", Misc.ucFirst(target.getFaction().getDisplayNameLongWithArticle()));
			}
		}
		
		
		String shipOrFleet = "fleet";
		if (playerFleet.getFleetData().getMembersListCopy().size() == 1) {
			shipOrFleet = "ship";
			if (playerFleet.getFleetData().getMembersListCopy().get(0).isFighterWing()) {
				shipOrFleet = "fighter wing";
			}
		}
		map.put("$shipOrFleet", shipOrFleet);
		map.put("$fleetOrShip", shipOrFleet);
		map.put("$ShipOrFleet", Misc.ucFirst(shipOrFleet));
		map.put("$FleetOrShip", Misc.ucFirst(shipOrFleet));
		
		if (target instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) target;
			String otherShipOrFleet = "fleet";
			if (fleet.getFleetData().getMembersListCopy().size() == 1) {
				shipOrFleet = "ship";
				if (fleet.getFleetData().getMembersListCopy().get(0).isFighterWing()) {
					shipOrFleet = "fighter wing";
				}
			}
			map.put("$otherShipOrFleet", otherShipOrFleet);
			
			map.put("$otherFleetName", fleet.getName().toLowerCase());
		}
		
		if (person != null) {
			if (person.isMale()) {
				map.put("$hisOrHer", "his");
				map.put("$HisOrHer", "His");
				map.put("$himOrHer", "him");
				map.put("$HimOrHer", "Him");
				map.put("$heOrShe", "he");
				map.put("$HeOrShe", "He");
				map.put("$himOrHerself", "himself");
				map.put("$HimOrHerself", "Himself");
				map.put("$manOrWoman", "man");
				map.put("$ManOrWoman", "Man");
				map.put("$brotherOrSister", "brother");
				map.put("$BrotherOrSister", "Brother");
				map.put("$sirOrMadam", "sir");
				map.put("$SirOrMadam", "Sir");
			} else {
				map.put("$hisOrHer", "her");
				map.put("$HisOrHer", "Her");
				map.put("$himOrHer", "her");
				map.put("$HimOrHer", "Her");
				map.put("$heOrShe", "she");
				map.put("$HeOrShe", "She");
				map.put("$himOrHerself", "herself");
				map.put("$HimOrHerself", "Herself");
				map.put("$manOrWoman", "woman");
				map.put("$ManOrWoman", "Woman");
				map.put("$brotherOrSister", "sister");
				map.put("$BrotherOrSister", "Sister");
				map.put("$sirOrMadam", "ma'am");
				map.put("$SirOrMadam", "Ma'am");
			}
			
			if (person.getRank() != null) {
				map.put("$personRank", person.getRank().toLowerCase());
				map.put("$PersonRank", Misc.ucFirst(person.getRank()));
			}
			
			if (person.getPost() != null) {
				map.put("$personPost", person.getPost().toLowerCase());
				map.put("$PersonPost", Misc.ucFirst(person.getPost()));
			}
			
			map.put("$PersonName", person.getName().getFullName());
			map.put("$personName", person.getName().getFullName());
			map.put("$personFirstName", person.getName().getFirst());
			map.put("$personLastName", person.getName().getLast());
		}
		
		
		if (faction != null) {
			float rel = faction.getRelationship(Factions.PLAYER);
			RepLevel level = RepLevel.getLevelFor(rel);
			map.put("$relAdjective", level.getDisplayName().toLowerCase());
		}
		
		if (personFaction != null) {
			String factionName = personFaction.getEntityNamePrefix();
			if (factionName == null || factionName.isEmpty()) {
				factionName = personFaction.getDisplayName();
			}

			map.put("$personFactionIsOrAre", personFaction.getDisplayNameIsOrAre());
			
			map.put("$personFaction", factionName);
			map.put("$PersonFaction", Misc.ucFirst(factionName));
			map.put("$thePersonFaction", personFaction.getDisplayNameWithArticle());
			map.put("$ThePersonFaction", Misc.ucFirst(personFaction.getDisplayNameWithArticle()));
			
			map.put("$personFactionLong", personFaction.getDisplayNameLong());
			map.put("$PersonFactionLong", Misc.ucFirst(personFaction.getDisplayNameLong()));
			map.put("$thePersonFactionLong", personFaction.getDisplayNameLongWithArticle());
			map.put("$ThePersonFactionLong", Misc.ucFirst(personFaction.getDisplayNameLongWithArticle()));
		}
		
		return map;
	}

}









