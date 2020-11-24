package com.fs.starfarer.api.impl.campaign.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.comm.MessagePriority;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventManagerAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventPlugin;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Events;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class BaseEventPlugin implements CampaignEventPlugin, CampaignEventListener {
	
	private String id = Misc.genUID();
	
	protected String eventType;
	protected CampaignEventTarget eventTarget;
	protected MarketAPI market;
	protected SectorEntityToken entity;
	protected FactionAPI faction;
	protected String statModId;
	protected boolean started;

	protected MemoryAPI memory = null;

	protected float startProbability;
	
	public void init(String eventType, CampaignEventTarget eventTarget) {
		init(eventType, eventTarget, true);
	}
	public void init(String eventType, CampaignEventTarget eventTarget, boolean addListener) {
		this.eventType = eventType;
		this.eventTarget = eventTarget;

		setTarget(eventTarget);
		
		if (market == null) {
			statModId = eventType + "_" + Misc.genUID();
		} else {
			statModId = eventType + "_" + market.getId() + "_" + Misc.genUID();
		}

		if (addListener) {
			Global.getSector().addListener(this);
		}
	}
	
	public void setTarget(CampaignEventTarget eventTarget) {
		this.eventTarget = eventTarget;
		if (eventTarget.getEntity() != null) {
			market = eventTarget.getEntity().getMarket();
			faction = eventTarget.getFaction();
			entity = eventTarget.getEntity();
		}
	}
	
	protected String getLoggingId() {
		if (market != null) {
			return "[" + market.getName() + "]";
		} else if (eventTarget != null && eventTarget.getEntity() != null) {
			return "[" + eventTarget.getEntity().getName() + "]";
		} else {
			return "[" + eventType + "]";
		}
	}
	
	public void cleanup() {
		Global.getSector().removeListener(this);
	}

	public void startEvent() {
		startEvent(false);
	}
	public void startEvent(boolean addListener) {
		started = true;
		if (addListener) {
			Global.getSector().addListener(this);
		}
	}
	
	protected boolean isEventStarted() {
		return started;
	}
	
	public void advance(float amount) {
		
	}

	public CampaignEventTarget getEventTarget() {
		return eventTarget;
	}

	public String getEventType() {
		return eventType;
	}

	public String getStageIdForLikely() {
		return null;
	}

	public String getStageIdForPossible() {
		return null;
	}

	public Map<String, String> getTokenReplacements() {
		HashMap<String, String> tokens = new HashMap<String, String>();
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		tokens.put("$playerName", Global.getSector().getCharacterData().getName());
		//PersonAPI playerPerson = playerFleet.getCommander();
		PersonAPI playerPerson = Global.getSector().getPlayerPerson();
		if (playerPerson != null) {
			if (playerPerson.isMale()) {
				tokens.put("$playerHisOrHer", "his");
				tokens.put("$PlayerHisOrHer", "His");
				tokens.put("$playerHimOrHer", "him");
				tokens.put("$PlayerHimOrHer", "Him");
				tokens.put("$playerHeOrShe", "he");
				tokens.put("$PlayerHeOrShe", "He");
			} else {
				tokens.put("$playerHisOrHer", "her");
				tokens.put("$PlayerHisOrHer", "Her");
				tokens.put("$playerHimOrHer", "her");
				tokens.put("$PlayerHimOrHer", "Her");
				tokens.put("$playerHeOrShe", "she");
				tokens.put("$PlayerHeOrShe", "She");
			}
		}
		
		MarketAPI market = this.market;
		SectorEntityToken entity = this.entity;
		FactionAPI faction = this.faction;
		if (getEventTarget() != null) { // for special events that can change their targets
			market = getEventTarget().getMarket();
			entity = getEventTarget().getEntity();
			faction = getEventTarget().getFaction();
		}
		
		if (entity != null) {
			tokens.put("$name", entity.getName());
		}
		
		if (market != null) {
			if (market.getPrimaryEntity().hasTag(Tags.STATION)) {
				tokens.put("$onOrAt", "at");
			} else {
				tokens.put("$onOrAt", "on");
			}
			
			tokens.put("$market", market.getName());
			tokens.put("$marketFaction", market.getFaction().getDisplayName());
			tokens.put("$MarketFaction", Misc.ucFirst(market.getFaction().getDisplayName()));
			tokens.put("$TheMarketFaction", Misc.ucFirst(market.getFaction().getDisplayNameWithArticle()));
			tokens.put("$theMarketFaction", market.getFaction().getDisplayNameWithArticle());
			
			if (eventTarget.getLocation() instanceof StarSystemAPI) {
				//tokens.put("$marketSystem", ((StarSystemAPI)eventTarget.getLocation()).getBaseName() + " star system");
				tokens.put("$marketSystem", ((StarSystemAPI)eventTarget.getLocation()).getBaseName());
			} else {
				tokens.put("$marketSystem", "hyperspace");
			}
			
			RepLevel level = playerFleet.getFaction().getRelationshipLevel(market.getFaction());
			tokens.put("$factionStanding", level.getDisplayName().toLowerCase());
			tokens.put("$FactionStanding", Misc.ucFirst(level.getDisplayName()));
		}
		
		if (faction != null) {
			RepLevel level = playerFleet.getFaction().getRelationshipLevel(faction);
			tokens.put("$factionStanding", level.getDisplayName().toLowerCase());
			tokens.put("$FactionStanding", Misc.ucFirst(level.getDisplayName()));
		}
		
		if (playerFleet != null) {
			String fleetOrShip = "fleet";
			if (playerFleet.getFleetData().getMembersListCopy().size() == 1) {
				fleetOrShip = "ship";
				if (playerFleet.getFleetData().getMembersListCopy().get(0).isFighterWing()) {
					fleetOrShip = "fighter wing";
				}
			}
			tokens.put("$playerShipOrFleet", fleetOrShip);
		}
		
		//if (getEventTarget() != null && getEventTarget().getEntity() instanceof CampaignFleetAPI) {
		//if (getEventTarget() != null && getEventTarget().getEntity() != null) {
		//getEventTarget().getEntity().getFaction();
		if (faction != null) {
			//CampaignFleetAPI fleet = (CampaignFleetAPI) getEventTarget().getEntity();
			//FactionAPI faction = getEventTarget().getEntity().getFaction();
			String factionName = faction.getEntityNamePrefix();
			if (factionName == null || factionName.isEmpty()) {
				factionName = faction.getDisplayName();
			}
			
			tokens.put("$factionIsOrAre", faction.getDisplayNameIsOrAre());
			
			tokens.put("$faction", factionName);
			tokens.put("$ownerFaction", factionName);
			tokens.put("$marketFaction", factionName);
			tokens.put("$Faction", Misc.ucFirst(factionName));
			tokens.put("$OwnerFaction", Misc.ucFirst(factionName));
			tokens.put("$MarketFaction", Misc.ucFirst(factionName));
			tokens.put("$theFaction", faction.getDisplayNameWithArticle());
			tokens.put("$theOwnerFaction", faction.getDisplayNameWithArticle());
			tokens.put("$theMarketFaction", faction.getDisplayNameWithArticle());
			tokens.put("$TheFaction", Misc.ucFirst(faction.getDisplayNameWithArticle()));
			tokens.put("$TheOwnerFaction", Misc.ucFirst(faction.getDisplayNameWithArticle()));
			tokens.put("$TheMarketFaction", Misc.ucFirst(faction.getDisplayNameWithArticle()));
			
			tokens.put("$factionLong", faction.getDisplayNameLong());
			tokens.put("$FactionLong", Misc.ucFirst(faction.getDisplayNameLong()));
			tokens.put("$theFactionLong", faction.getDisplayNameLongWithArticle());
			tokens.put("$TheFactionLong", Misc.ucFirst(faction.getDisplayNameLongWithArticle()));
		}
		
		return tokens;
	}
	
	
	public static void addFactionNameTokens(Map<String, String> tokens, String prefix, FactionAPI faction) {
		if (faction != null) {
			String factionName = faction.getEntityNamePrefix();
			if (factionName == null || factionName.isEmpty()) {
				factionName = faction.getDisplayName();
			}
			String prefixUC = Misc.ucFirst(prefix);
			tokens.put("$" + prefix + "Faction", factionName);
			tokens.put("$" + prefixUC + "Faction", Misc.ucFirst(factionName));
			tokens.put("$the" + prefixUC + "Faction", faction.getDisplayNameWithArticle());
			tokens.put("$The" + prefixUC + "Faction", Misc.ucFirst(faction.getDisplayNameWithArticle()));
			
			tokens.put("$" + prefix + "FactionLong", faction.getDisplayNameLong());
			tokens.put("$" + prefixUC + "FactionLong", Misc.ucFirst(faction.getDisplayNameLong()));
			tokens.put("$the" + prefixUC + "FactionLong", faction.getDisplayNameLongWithArticle());
			tokens.put("$The" + prefixUC + "FactionLong", Misc.ucFirst(faction.getDisplayNameLongWithArticle()));
			
			tokens.put("$" + prefix + "FactionIsOrAre", faction.getDisplayNameIsOrAre());
		}
	}
	
	public static void addPersonTokens(Map<String, String> tokens, String prefix, PersonAPI person) {
		if (person != null) {
			tokens.put("$" + prefix + "Name", person.getName().getFullName());
			tokens.put("$" + prefix + "LastName", person.getName().getLast());
			tokens.put("$" + prefix + "FirstName", person.getName().getFirst());
			
			
			tokens.put("$" + prefix + "Rank", person.getRank().toLowerCase());
			tokens.put("$" + Misc.ucFirst(prefix) + "Rank", Misc.ucFirst(person.getRank()));
			
			tokens.put("$" + prefix + "Post", person.getPost().toLowerCase());
			tokens.put("$" + Misc.ucFirst(prefix) + "Post", Misc.ucFirst(person.getPost()));
			
			
			if (person.isMale()) {
				tokens.put("$" + prefix + "HisOrHer", "his");
				tokens.put("$" + Misc.ucFirst(prefix) + "HisOrHer", "His");
				tokens.put("$" + prefix + "HimOrHer", "him");
				tokens.put("$" + Misc.ucFirst(prefix) + "HimOrHer", "Him");
				tokens.put("$" + prefix + "HeOrShe", "he");
				tokens.put("$" + Misc.ucFirst(prefix) + "HeOrShe", "He");
			} else {
				tokens.put("$" + prefix + "HisOrHer", "her");
				tokens.put("$" + Misc.ucFirst(prefix) + "HisOrHer", "Her");
				tokens.put("$" + prefix + "HimOrHer", "her");
				tokens.put("$" + Misc.ucFirst(prefix) + "HimOrHer", "Her");
				tokens.put("$" + prefix + "HeOrShe", "she");
				tokens.put("$" + Misc.ucFirst(prefix) + "HeOrShe", "She");
			}
		}
	}

	public MessagePriority getWarningWhenLikelyPriority() {
		return null;
	}

	public MessagePriority getWarningWhenPossiblePriority() {
		return null;
	}

	public boolean isDone() {
		return false;
	}

	public void setParam(Object param) {
		
	}
	
	public String getTargetName() {
		if (eventTarget.getEntity() != null) {
			return eventTarget.getEntity().getName() + " (" + eventTarget.getLocation().getName() + ")";
		}
		return eventTarget.getLocation().getName();
	}

	public static interface MarketFilter {
		boolean acceptMarket(MarketAPI market);
	}
	
	public static MarketAPI findNearestMarket(MarketAPI from, MarketFilter filter) {
		float minDist = Float.MAX_VALUE;
		MarketAPI result = null;
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market == from) continue;
			float dist = Misc.getDistance(market.getLocationInHyperspace(), from.getLocationInHyperspace());
			if (dist < minDist && (filter == null || filter.acceptMarket(market))) {
				minDist = dist;
				result = market;
			}
		}
		return result;
	}
	
	public static List<MarketAPI> findMatchingMarkets(MarketFilter filter) {
		List<MarketAPI> result = new ArrayList<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (filter == null || filter.acceptMarket(market)) {
				result.add(market);
			}
		}
		return result;
	}
	
	public void increaseRecentUnrest(float stabilityChange) {
		if (stabilityChange <= 0) return;
		CampaignEventManagerAPI manager = Global.getSector().getEventManager();
		RecentUnrestEvent event = (RecentUnrestEvent) manager.getOngoingEvent(eventTarget, Events.RECENT_UNREST);
		if (event == null) {
			event = (RecentUnrestEvent) manager.startEvent(eventTarget, Events.RECENT_UNREST, null);
		}
		event.increaseStabilityPenalty((int) stabilityChange);
	}
	
	public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
		
	}

	public void reportFleetDespawned(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
	}

	public void reportFleetJumped(CampaignFleetAPI fleet, SectorEntityToken from, JumpDestination to) {
	}

	public void reportFleetReachedEntity(CampaignFleetAPI fleet, SectorEntityToken entity) {
	}

	public boolean allowMultipleOngoingForSameTarget() {
		return false;
	}
	
	public String[] getHighlights(String stageId) {
		return null;
	}
	
	public Color[] getHighlightColors(String stageId) {
		String [] highlights = getHighlights(stageId);
		if (highlights != null) {
			Color c = Global.getSettings().getColor("buttonShortcut");
			Color [] colors = new Color[highlights.length];
			Arrays.fill(colors, c);
			return colors;
		}
		return null;
	}
	
	
	public void addTokensToList(List<String> list, String ... keys) {
		Map<String, String> tokens = getTokenReplacements();
		for (String key : keys) {
			if (tokens.containsKey(key)) {
				list.add(tokens.get(key));
			}
		}
	}
	
	public String getEventName() {
		return "BaseEventPlugin.getEventName()";
	}
	
	public CampaignEventCategory getEventCategory() {
		return CampaignEventCategory.EVENT;
	}

	protected MessagePriority getDefaultPriority() {
		MessagePriority priority = MessagePriority.SECTOR;
		switch (market.getSize()) {
		case 1:
		case 2:
		case 3:
			priority = MessagePriority.SYSTEM;
			break;
		case 4:
		case 5:
		case 6:
			priority = MessagePriority.CLUSTER;
			break;
		case 7:
		case 8:
		case 9:
			priority = MessagePriority.SECTOR;
			break;
		}
		return priority;
	}

	public List<String> getRelatedCommodities() {
		return null;
	}
	
	public List<PriceUpdatePlugin> getPriceUpdates() {
		return null;
	}

	public void reportShownInteractionDialog(InteractionDialogAPI dialog) {
		
	}

	public void reportPlayerOpenedMarket(MarketAPI market) {
		
	}

	public String getCurrentMessageIcon() {
		return null;
	}

	public String getCurrentImage() {
		return null;
	}

	public String getEventIcon() {
		return null;
	}

	public boolean showAllMessagesIfOngoing() {
		return true;
	}

	public void reportPlayerReputationChange(String faction, float delta) {
		
	}

	public void reportPlayerEngagement(EngagementResultAPI result) {
		
	}

	public void reportFleetSpawned(CampaignFleetAPI fleet) {
		
	}

	public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {
		
	}

	public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {
		
	}

	public void reportPlayerClosedMarket(MarketAPI market) {
		
	}

	public boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		return true;
	}
	
	public MemoryAPI getMemory() {
		return memory;
	}

	public String getId() {
		if (id == null) {
			id = Misc.genUID();
		}
		return id;
	}
	
	public String getStatModId() {
		return statModId;
	}

	public void reportPlayerReputationChange(PersonAPI person, float delta) {
		
	}
	
	public void reportPlayerActivatedAbility(AbilityPlugin ability, Object param) {
		
	}

	public void reportPlayerDeactivatedAbility(AbilityPlugin ability, Object param) {
		
	}

	public void reportBattleFinished(CampaignFleetAPI primaryWinner, BattleAPI battle) {
		
	}

	public void reportBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle) {
		
	}
	public void setProbability(float p) {
		this.startProbability = p;
		
	}
	public boolean useEventNameAsId() {
		return false;
	}
	
	public boolean showLatestMessageIfOngoing() {
		return true;
	}
	
	public void reportPlayerDumpedCargo(CargoAPI cargo) {
		
	}
	public void reportPlayerDidNotTakeCargo(CargoAPI cargo) {
		
	}
	public void reportEconomyMonthEnd() {
		// TODO Auto-generated method stub
		
	}
	public void reportEconomyTick(int iterIndex) {
		// TODO Auto-generated method stub
		
	}
}






