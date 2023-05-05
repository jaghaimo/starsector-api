package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.CustomCampaignEntityPlugin;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CampaignObjective;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTNeutrinoBurstFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTPoints;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HyperspaceTopographyEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.CommSnifferIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamVisibilityManager;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * NotifyEvent $eventHandle <params> 
 * 
 */
public class Objectives extends BaseCommandPlugin {

	public static String BURST_RANGE = "$COB_burstRange";
	
	public static float BURST_RANGE_MAKESHIFT = 10;
	public static float BURST_RANGE_DOMAIN = 15;
	public static float BURST_RANGE_SCAVENGER_MIN = 5; // used by HT_CMD
	public static float BURST_RANGE_SCAVENGER_MAX = 10; // used by HT_CMD
	
	public static float SALVAGE_FRACTION = 0.5f;
	
	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected CargoAPI playerCargo;
	protected MemoryAPI memory;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	protected FactionAPI faction;
	
	public Objectives() {
		
	}
	
	public Objectives(SectorEntityToken entity) {
		init(entity);
	}
	
	protected void init(SectorEntityToken entity) {
		memory = entity.getMemoryWithoutUpdate();
		this.entity = entity;
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		
		faction = entity.getFaction();
		
		if (entity.hasTag(Tags.MAKESHIFT)) {
			memory.set(BURST_RANGE, (int)BURST_RANGE_MAKESHIFT, 0);
		} else {
			memory.set(BURST_RANGE, (int)BURST_RANGE_DOMAIN, 0);
		}
		
		//DebugFlags.OBJECTIVES_DEBUG = false;
	}

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		entity = dialog.getInteractionTarget();
		init(entity);
		
		memory = getEntityMemory(memoryMap);
		
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();

		if (command.equals("printCost")) {
			String type = params.get(1).getString(memoryMap);
			printCost(type);
		} else if (command.equals("showSalvage")) {
			printSalvage();
		} else if (command.equals("hasRepImpact")) {
			return hasRepImpact();
		} else if (command.equals("showRepairCost")) {
			printRepairCost(true);
		} else if (command.equals("showBurstCost")) {
			boolean hasRecent = HyperspaceTopographyEventIntel.hasRecentReadingsNearPlayer();
			printBurstCost(canBurst() && !hasRecent);
		} else if (command.equals("showRepairCostNoPrompt")) {
			printRepairCost(false);
		} else if (command.equals("printHackDesc")) {
			printHackDesc();
		} else if (command.equals("canBuild")) {
			String type = params.get(1).getString(memoryMap);
			return canBuild(type);
		} else if (command.equals("canActivate")) {
			return canActivate(entity.getCustomEntityType());
		} else if (command.equals("canBurst")) {
			return canBurst();
		} else if (command.equals("build")) {
			String type = params.get(1).getString(memoryMap);
			build(type, Factions.PLAYER);
		} else if (command.equals("printDescription")) {
			updateMemory();
			String type = entity.getCustomEntityType();
			printDescription(type);
		} else if (command.equals("isHacked")) {
			return isHacked();
		} else if (command.equals("doAction")) {
			String action = params.get(1).getString(memoryMap);
			if (action.equals("hack")) {
				hack();
			} else if (action.equals("reset")) {
				reset();
			} else if (action.equals("unhack")) {
				unhack();
			} else if (action.equals("control")) {
				control(Factions.PLAYER);
			} else if (action.equals("salvage")) {
				salvage(Factions.PLAYER);
			} else if (action.equals("burst")) {
				doBurst();
			}
		}
		return true;
	}
	
	protected void doBurst() {
		CargoAPI cargo = playerCargo;
		String [] res = getBurstResources();
		int [] quantities = getBurstQuantities();
		for (int i = 0; i < res.length; i++) {
			String commodityId = res[i];
			int quantity = quantities[i];
			cargo.removeCommodity(commodityId, quantity);
			AddRemoveCommodity.addCommodityLossText(commodityId, quantity, text);
		}
		
		float range = memory.getFloat(BURST_RANGE);
		SlipstreamVisibilityManager.updateSlipstreamVisibility(entity.getLocationInHyperspace(), range);
		
		int points = 0;
		if (entity.hasTag(Tags.MAKESHIFT)) {
			points = HTPoints.NEUTRINO_BURST_MAKESHIFT;
		} else {
			points = HTPoints.NEUTRINO_BURST_DOMAIN;
		}
		
		
		boolean hasRecent = HyperspaceTopographyEventIntel.hasRecentReadingsNearPlayer();
		if (!hasRecent && points > 0) {
			HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(new HTNeutrinoBurstFactor(points), dialog);
			if (HyperspaceTopographyEventIntel.get() != null) {
				HyperspaceTopographyEventIntel.get().addRecentReadings(entity.getLocationInHyperspace());
			}
		}
	}

	protected boolean hasRepImpact() {
		for (MarketAPI curr : Global.getSector().getEconomy().getMarkets(entity.getContainingLocation())) {
			if (curr.getFaction() == entity.getFaction() && 
					!curr.getFaction().isNeutralFaction() &&
					!curr.getFaction().isPlayerFaction()) {
				return true;
			}
		}
		return false;
	}

	public void salvage(final String factionId) {
		
		CargoAPI salvage = Global.getFactory().createCargo(true);
		String [] r = getResources();
		int [] q = getSalvageQuantities();
		
		for (int i = 0; i < r.length; i++) {
			salvage.addCommodity(r[i], q[i]);
		}
		
		dialog.getVisualPanel().showLoot("Salvaged", salvage, false, true, true, new CoreInteractionListener() {
			public void coreUIDismissed() {
				dialog.dismiss();
				dialog.hideTextPanel();
				dialog.hideVisualPanel();
				
				LocationAPI loc = entity.getContainingLocation();
				SectorEntityToken built = loc.addCustomEntity(null,
						 									 null,
						 Entities.STABLE_LOCATION, // type of object, defined in custom_entities.json
						 Factions.NEUTRAL); // faction
				if (entity.getOrbit() != null) {
					built.setOrbit(entity.getOrbit().makeCopy());
				}
				loc.removeEntity(entity);
				
				built.getMemoryWithoutUpdate().set(MemFlags.RECENTLY_SALVAGED, true, 30f);
				
				if (Factions.PLAYER.equals(factionId) && hasRepImpact() &&
						!entity.getFaction().isPlayerFaction() && 
						!entity.getFaction().isNeutralFaction()) {
					RepActions action = RepActions.COMBAT_AGGRESSIVE;
					if (entity.hasTag(Tags.MAKESHIFT)) {
						action = RepActions.COMBAT_AGGRESSIVE_TOFF;
					}
					Global.getSector().adjustPlayerReputation(
							new RepActionEnvelope(action, null, null, null, false, true, "Change caused by destruction of " + entity.getCustomEntitySpec().getDefaultName().toLowerCase()), 
							faction.getId());
				}
				
				ListenerUtil.reportObjectiveDestroyed(entity, built, Global.getSector().getFaction(factionId));
			}
		});
		options.clearOptions();
		dialog.setPromptText("");
		
	}
	
	public boolean isNonFunctional() {
		return entity.getMemoryWithoutUpdate().getBoolean(MemFlags.OBJECTIVE_NON_FUNCTIONAL);
	}
	
	public void control(String factionId) {
		if (dialog != null) {
			if (Factions.PLAYER.equals(factionId) && isNonFunctional()) {
				removeRepairCosts(text);
			}
			if (Factions.PLAYER.equals(factionId) && hasRepImpact() &&
					!entity.getFaction().isPlayerFaction() && !entity.getFaction().isNeutralFaction()) {
				RepActions action = RepActions.COMBAT_AGGRESSIVE;
				//action = RepActions.COMBAT_AGGRESSIVE_TOFF;
				Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(action, null, null, text, false, true), 
						faction.getId());
			}
		}
		FactionAPI prev = entity.getFaction();
		entity.setFaction(factionId);
		faction = entity.getFaction();

		if (!entity.hasTag(Tags.COMM_RELAY) && faction.isPlayerFaction()) {
			unhack();
		}
		
		entity.getMemoryWithoutUpdate().unset(MemFlags.OBJECTIVE_NON_FUNCTIONAL);
		
		if (dialog != null) {
			((RuleBasedDialog) dialog.getPlugin()).updateMemory();
			updateMemory();
			
			printOwner();
		}
		
		
		ListenerUtil.reportObjectiveChangedHands(entity, prev, faction);
	}
	
	public void unhack() {
		CommSnifferIntel intel = CommSnifferIntel.getExistingSnifferIntelForRelay(entity);
		if (intel != null) {
			intel.uninstall();
			updateMemory();
		} else {
			CustomCampaignEntityPlugin plugin = entity.getCustomPlugin();
			if (plugin instanceof CampaignObjective) {
				CampaignObjective o = (CampaignObjective) plugin;
				o.setHacked(false);
			}
			updateMemory();
		}
	}
	
	public void hack() {
		CustomCampaignEntityPlugin plugin = entity.getCustomPlugin();
		if (plugin instanceof CampaignObjective) {
			CampaignObjective o = (CampaignObjective) plugin;
			o.setHacked(true);
		}
		updateMemory();
	}
	
	public void reset() {
		CustomCampaignEntityPlugin plugin = entity.getCustomPlugin();
		if (plugin instanceof CampaignObjective) {
			CampaignObjective o = (CampaignObjective) plugin;
			o.setReset(true);
		}
		// so that a false sensor reading doesn't spawn immediately after "introducing false readings"
		Global.getSector().getPlayerFleet().getMemoryWithoutUpdate().set(MemFlags.FLEET_NOT_CHASING_GHOST, true,
								0.5f +Misc.random.nextFloat() * 1f);
		updateMemory();
	}
	
	public boolean isHacked() {
		CustomCampaignEntityPlugin plugin = entity.getCustomPlugin();
		if (plugin instanceof CampaignObjective) {
			CampaignObjective o = (CampaignObjective) plugin;
			return o.isHacked();
		}
		return false;
	}
	
	public void build(String type, String factionId) {
		LocationAPI loc = entity.getContainingLocation();
		SectorEntityToken built = loc.addCustomEntity(null,
				 									 null,
				 type, // type of object, defined in custom_entities.json
				 factionId); // faction
		if (entity.getOrbit() != null) {
			built.setOrbit(entity.getOrbit().makeCopy());
		}
		built.setLocation(entity.getLocation().x, entity.getLocation().y);
		loc.removeEntity(entity);
		
		//entity.setContainingLocation(null);
		built.getMemoryWithoutUpdate().set("$originalStableLocation", entity);
		
		if (text != null) {
			removeBuildCosts();
			Global.getSoundPlayer().playUISound("ui_objective_constructed", 1f, 1f);
		}
	}

	
	public boolean canBuild(String type) {
		if (DebugFlags.OBJECTIVES_DEBUG) {
			return true;
		}
		
		CargoAPI cargo = playerCargo;
		String [] res = getResources();
		int [] quantities = getQuantities();
		for (int i = 0; i < res.length; i++) {
			String commodityId = res[i];
			int quantity = quantities[i];
			if (quantity > cargo.getQuantity(CargoItemType.RESOURCES, commodityId)) {
				return false;
			}
		}
		return true;
	}
	
	public void removeBuildCosts() {
		if (DebugFlags.OBJECTIVES_DEBUG) {
			return;
		}
		
		CargoAPI cargo = playerCargo;
		String [] res = getResources();
		int [] quantities = getQuantities();
		for (int i = 0; i < res.length; i++) {
			String commodityId = res[i];
			int quantity = quantities[i];
			cargo.removeCommodity(commodityId, quantity);
		}
	}
	
	public void removeRepairCosts(TextPanelAPI text) {
		if (DebugFlags.OBJECTIVES_DEBUG) {
			return;
		}
		
		CargoAPI cargo = playerCargo;
		String [] res = getRepairResources();
		int [] quantities = getRepairQuantities();
		for (int i = 0; i < res.length; i++) {
			String commodityId = res[i];
			int quantity = quantities[i];
			cargo.removeCommodity(commodityId, quantity);
			AddRemoveCommodity.addCommodityLossText(commodityId, quantity, text);
		}
	}
	
	public boolean canActivate(String type) {
		if (DebugFlags.OBJECTIVES_DEBUG) {
			return true;
		}
		
		CargoAPI cargo = playerCargo;
		String [] res = getRepairResources();
		int [] quantities = getRepairQuantities();
		for (int i = 0; i < res.length; i++) {
			String commodityId = res[i];
			int quantity = quantities[i];
			if (quantity > cargo.getQuantity(CargoItemType.RESOURCES, commodityId)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean canBurst() {
		if (DebugFlags.OBJECTIVES_DEBUG) {
			return true;
		}
		
		CargoAPI cargo = playerCargo;
		String [] res = getBurstResources();
		int [] quantities = getBurstQuantities();
		for (int i = 0; i < res.length; i++) {
			String commodityId = res[i];
			int quantity = quantities[i];
			if (quantity > cargo.getQuantity(CargoItemType.RESOURCES, commodityId)) {
				return false;
			}
		}
		return true;
	}
	
	
	public void updateMemory() {
		//memory.set("$cob_hacked", isHacked(), 0f);
		//memory.set(BaseCampaignObjectivePlugin.HACKED, isHacked(), 0f);
	}
	
	public void printDescription(String type) {
		Description desc = Global.getSettings().getDescription(type, Type.CUSTOM);
		if (desc != null) {
			text.addParagraph(desc.getText1());
		}
		
		CustomEntitySpecAPI spec = Global.getSettings().getCustomEntitySpec(type);
		CustomCampaignEntityPlugin plugin = spec.getPlugin();
		SectorEntityToken temp = entity.getContainingLocation().createToken(0, 0);
		for (String tag : spec.getTags()) {
			temp.addTag(tag);
		}
		plugin.init(temp, null);
		
		boolean objective = entity.hasTag(Tags.OBJECTIVE);
		if (objective) {
			plugin = entity.getCustomPlugin();
		}
		
		Class c = null;
		if (plugin instanceof CampaignObjective) {
			CampaignObjective o = (CampaignObjective) plugin;
			c = o.getClass();
			
			TooltipMakerAPI info = text.beginTooltip();
			o.printEffect(info, 0f);
			text.addTooltip();
			
			o.printNonFunctionalAndHackDescription(text);
		}
		
		printOwner();
		
		for (SectorEntityToken curr : entity.getContainingLocation().getEntitiesWithTag(Tags.OBJECTIVE)) {
			if (curr.hasTag(Tags.OBJECTIVE)) {
				if (curr.getFaction() == null || !curr.getFaction().isPlayerFaction() ||
						curr.getCustomEntitySpec() == null) {
					continue;
				}
				
				CustomCampaignEntityPlugin ccep = curr.getCustomPlugin();
				if (ccep instanceof CampaignObjective) {
					CampaignObjective o = (CampaignObjective) ccep;
					if (c == o.getClass()) {
						if (entity == curr) {
							text.addPara("Another one in this star system would have no effect " +
											"beyond providing redundancy in case this one is lost.");
						} else {
							text.addPara("There's already " +
									curr.getCustomEntitySpec().getAOrAn() + " " + 
									curr.getCustomEntitySpec().getNameInText() + " under your control " +
											"in this star system. Another one would have no effect " +
											"beyond providing redundancy if one is lost.");
						}
						break;
					}
				}
			}
		}
		
	}
	
	public void printOwner() {
		boolean objective = entity.hasTag(Tags.OBJECTIVE);
		if (objective) {
			if (!faction.isNeutralFaction()) {
				if (entity.getFaction().isPlayerFaction() && !Misc.isPlayerFactionSetUp()) {
					text.addPara("This " + entity.getCustomEntitySpec().getShortName() + " is under your control.", 
							entity.getFaction().getBaseUIColor(), "your");
				} else {
					text.addPara("This " + entity.getCustomEntitySpec().getShortName() + " is under %s control.",
							entity.getFaction().getBaseUIColor(), entity.getFaction().getPersonNamePrefix());
				}
			} else {
				text.addPara("This " + entity.getCustomEntitySpec().getShortName() + " is not claimed by any faction.");
			}
		}
	}
	
	
	
	public void printHackDesc() {
//		if (entity.hasTag(Tags.COMM_RELAY)) {
//			text.addPara("The comm sniffer will remain active until it is detected and cleared out by the " +
//					"maintenance subroutines. The odds of this happening increase drastically when " +
//					"multiple comm sniffers are installed on different relays in the comm network.");
//		} else {
//			text.addPara("The hack will eventually be picked up and cleared out by the maintenance subroutines, but should " +
//					"remain effective for at least three months.");
//		}
	}
	
	public void printRepairCost(boolean withPrompt) {
		Misc.showCost(text, null, null, getRepairResources(), getRepairQuantities());
		if (withPrompt) {
			text.addPara("Proceed with reactivation?");
		}
	}
	
	public void printBurstCost(boolean withPrompt) {
		Misc.showCost(text, null, null, getBurstResources(), getBurstQuantities());
		boolean hasRecent = HyperspaceTopographyEventIntel.hasRecentReadingsNearPlayer();
		if (hasRecent) {
			LabelAPI label = text.addPara("You've recently acquired topographic data within %s light-years of your current location,"
					+ " and a neutrino burst here "
					+ "will not meaningfully contribute to your understanding of "
					+ "hyperspace topology. It will, however, still "
					+ "reveal all nearby slipstreams.", Misc.getHighlightColor(),
					"" + (int)HyperspaceTopographyEventIntel.RECENT_READINGS_RANGE_LY);
			label.setHighlightColors(Misc.getHighlightColor(), Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
			label.setHighlight("" + (int)HyperspaceTopographyEventIntel.RECENT_READINGS_RANGE_LY,
							"will not meaningfully contribute", "hyperspace topology");
		}
		//RECENT_READINGS_RANGE_LY
		if (withPrompt) {
			text.addPara("Proceed with neutrino burst?");
		}
	}
	
	public void printSalvage() {
		Misc.showCost(text, "Potential salvage", false, null, null, getResources(), getSalvageQuantities());
		
		text.addPara("Proceed with salvage operation?");
	}
	
	public void printCost(String type) {
		printDescription(type);
		
		Misc.showCost(text, null, null, getResources(), getQuantities());
		
		if (canBuild(type)) {
			text.addPara("Proceed with construction?");
		} else {
			text.addPara("You do not have the necessary resources to build this structure.");
		}
	}
	
	public String [] getResources() {
		if (entity.hasTag(Tags.MAKESHIFT) || entity.hasTag(Tags.STABLE_LOCATION)) {
			return new String[] {Commodities.HEAVY_MACHINERY, Commodities.METALS, Commodities.RARE_METALS};
		}
		return new String[] {Commodities.HEAVY_MACHINERY, Commodities.METALS, Commodities.RARE_METALS, Commodities.VOLATILES};
	}


	public int [] getSalvageQuantities() {
		int [] q = getQuantities();
		int [] result = new int [q.length];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = (int) (q[i] * SALVAGE_FRACTION);
		}
		return result;
	}
	public int [] getQuantities() {
		if (entity.hasTag(Tags.MAKESHIFT) || entity.hasTag(Tags.STABLE_LOCATION)) {
			return new int[] {15, 30, 5};
		}
		return new int[] {50, 200, 20, 20};
	}
	
	public String [] getRepairResources() {
		return new String[] {Commodities.HEAVY_MACHINERY };
	}


	public int [] getRepairQuantities() {
		return new int[] {5};
	}
	
	
	public int [] getBurstQuantities() {
		return new int[] {HTPoints.NEUTRINO_BURST_VOLATILES_COST};
	}
	
	public String [] getBurstResources() {
		return new String[] {Commodities.VOLATILES};
	}
}















