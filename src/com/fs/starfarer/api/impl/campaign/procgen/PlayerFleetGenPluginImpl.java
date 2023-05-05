package com.fs.starfarer.api.impl.campaign.procgen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomProductionPickerDelegateImpl;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionProductionAPI;
import com.fs.starfarer.api.campaign.FactionProductionAPI.ItemInProductionAPI;
import com.fs.starfarer.api.campaign.FactionProductionAPI.ProductionItemType;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI.SkillLevelAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.util.Misc;

public class PlayerFleetGenPluginImpl implements InteractionDialogPlugin {

	protected static enum FleetQuality {
		MAX_DMODS,
		NO_DMODS,
		ONE_SMOD,
		TWO_SMODS,
		THREE_SMODS,
	}
	
	protected static enum OptionId {
		INIT,
		SELECT_SHIPS,
		ADD_UNSELECTED_OFFICERS,
		OFFICER_LEVEL_5,
		OFFICER_LEVEL_6,
		OFFICER_LEVEL_7,
		OFFICERS_GO_BACK,
		
		QUALITY_MAX_DMODS,
		QUALITY_NO_DMODS,
		QUALITY_1_SMODS,
		QUALITY_2_SMODS,
		QUALITY_2_SMODS_OMEGA,
		QUALITY_3_SMODS,
		QUALITY_3_SMODS_OMEGA,
		
//		SMODS_OFFICER_SHIPS_ONLY,
//		SMODS_ALL_SHIPS,
//		NORMAL_LEVEL_OFFICERS,
//		HIGH_LEVEL_OFFICERS,
		
		ADD_OFFICERS,
		ADD_OFFICERS_NO_SKILLS,
		NO_OFFICERS,
		
		CREATE_REPLACE,
		CREATE_ADD,
		
		LEAVE,
	}
	
	
	public static class FleetGenData {
		public FactionProductionAPI prod;
		public FleetQuality quality;
		public boolean smodsOnAllShips = true;
		public boolean allowOmega = false;
		//public boolean eliteOfficers = false;
		public boolean addOfficers = false;
		public boolean keepOfficerPointsFree = false;
		public boolean append = false;
		
		
		public FleetGenData() {
			prod = Global.getSector().getPlayerFaction().getProduction().clone();
			prod.clear();
		}
	}
	
	
	protected InteractionDialogAPI dialog;
	protected TextPanelAPI textPanel;
	protected OptionPanelAPI options;
	protected VisualPanelAPI visual;
	
	protected FleetGenData data = new FleetGenData();
	protected CampaignFleetAPI playerFleet;
	
	protected Set<String> ships = new LinkedHashSet<String>();
	protected Set<String> fighters = new LinkedHashSet<String>();
	protected Set<String> weapons = new LinkedHashSet<String>();
	protected Set<String> unrestrictedFighters = new LinkedHashSet<String>();
	protected Set<String> unrestrictedWeapons = new LinkedHashSet<String>();
	protected Set<String> hullmods = new LinkedHashSet<String>();
	
	protected static final Color HIGHLIGHT_COLOR = Global.getSettings().getColor("buttonShortcut");
	
	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		
		
		textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		playerFleet = Global.getSector().getPlayerFleet();
		
		visual.setVisualFade(0.25f, 0.25f);
		
		initAvailable();
		
		dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
		optionSelected(null, OptionId.INIT);
	}
	
	public Map<String, MemoryAPI> getMemoryMap() {
		return null;
	}
	
	public void backFromEngagement(EngagementResultAPI result) {
		// no combat here, so this won't get called
	}
	
	
	public void optionSelected(String text, Object optionData) {
		if (optionData == null) return;
		
		OptionId option = (OptionId) optionData;
		
		if (text != null) {
			//textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
			dialog.addOptionSelectedText(option);
			//textPanel.addParagraph("");
		}
		
		switch (option) {
		case INIT:
			selectShipsAndWeapons();
			break;
		case SELECT_SHIPS:
			showBlueprintPicker();
			break;
		case ADD_UNSELECTED_OFFICERS:
			showOfficerSelector();
			break;
		case OFFICERS_GO_BACK:
			selectShipsAndWeapons();
			break;
		case OFFICER_LEVEL_5:
			addNoSkillOfficer(5);
			break;
		case OFFICER_LEVEL_6:
			addNoSkillOfficer(6);
			break;
		case OFFICER_LEVEL_7:
			addNoSkillOfficer(7);
			break;
		case QUALITY_MAX_DMODS:
			data.quality = FleetQuality.MAX_DMODS;
			selectOfficerLevel();
			break;
		case QUALITY_NO_DMODS:
			data.quality = FleetQuality.NO_DMODS;
			selectOfficerLevel();
			break;
		case QUALITY_1_SMODS:
			data.quality = FleetQuality.ONE_SMOD;
			selectOfficerLevel();
			break;
		case QUALITY_2_SMODS:
			data.quality = FleetQuality.TWO_SMODS;
			selectOfficerLevel();
			break;
		case QUALITY_2_SMODS_OMEGA:
			data.quality = FleetQuality.TWO_SMODS;
			data.allowOmega = true;
			selectOfficerLevel();
			break;
		case QUALITY_3_SMODS:
			data.quality = FleetQuality.THREE_SMODS;
			selectOfficerLevel();
			break;
		case QUALITY_3_SMODS_OMEGA:
			data.quality = FleetQuality.THREE_SMODS;
			data.allowOmega = true;
			selectOfficerLevel();
			break;
		case NO_OFFICERS:
			data.addOfficers = false;
			selectAppendOrReplace();
			break;
		case ADD_OFFICERS:
			data.addOfficers = true;
			selectAppendOrReplace();
			break;
		case ADD_OFFICERS_NO_SKILLS:
			data.addOfficers = true;
			data.keepOfficerPointsFree = true;
			selectAppendOrReplace();
			break;
//		case NORMAL_LEVEL_OFFICERS:
//			data.eliteOfficers = false;
//			selectAppendOrReplace();
//			break;
//		case HIGH_LEVEL_OFFICERS:
//			data.eliteOfficers = true;
//			selectAppendOrReplace();
//			break;
		case CREATE_ADD:
			data.append = true;
			createFleet();
			dialog.dismiss();
			break;
		case CREATE_REPLACE:
			data.append = false;
			createFleet();
			dialog.dismiss();
			break;
		case LEAVE:
			dialog.dismiss();
			break;
		default:
			break;
		}
	}
	
	protected MarketAPI getNearestMarket(boolean playerOnly) {
		MarketAPI nearest = null;
		float minDist = Float.MAX_VALUE;
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		for (MarketAPI curr : Global.getSector().getEconomy().getMarketsCopy()) {
			if (curr.isHidden()) continue;
			if (playerOnly && !curr.isPlayerOwned()) continue;
			
			float dist = Misc.getDistanceLY(pf, curr.getPrimaryEntity());
			boolean nearer = dist < minDist;
			if (dist == minDist && dist == 0 && nearest != null) {
				float d1 = Misc.getDistance(pf, curr.getPrimaryEntity());
				float d2 = Misc.getDistance(pf, nearest.getPrimaryEntity());
				nearer = d1 < d2;
			}
			if (nearer) {
				nearest = curr;
				minDist = dist;
			}
		}
		return nearest;
	}
	
	protected void print(String str) {
		textPanel.appendToLastParagraph("\n" + str);
		System.out.println(str);
	}
	
	protected void selectShipsAndWeapons() {
		textPanel.addPara("--- QUICK PLAYER FLEET CREATOR ---");
		textPanel.addPara("Ship variants will be randomly picked from those available for a given hull.");
		textPanel.addPara("Selected weapons and fighters will be added to your cargo.");
		
		options.clearOptions();
		options.addOption("Select ships and weapons", OptionId.SELECT_SHIPS);
		options.addOption("Add officers with unselected skills", OptionId.ADD_UNSELECTED_OFFICERS);
		options.addOption("Leave", OptionId.LEAVE, null);
	}
	
	protected void showOfficerSelector() {
		textPanel.addPara("Select option to instantly add an officer of that level with all unspent skills. The officer "
				+ "level will be 1 higher than selected and they will not have an initial skill.");
		options.clearOptions();
		options.addOption("Level 5", OptionId.OFFICER_LEVEL_5);
		options.addOption("Level 6", OptionId.OFFICER_LEVEL_6);
		options.addOption("Level 7", OptionId.OFFICER_LEVEL_7);
		options.addOption("Go back", OptionId.OFFICERS_GO_BACK);
	}
	
	protected void selectQuality() {
		textPanel.addPara("Select ship quality.");
		options.clearOptions();
		options.addOption("Maximum number of d-mods", OptionId.QUALITY_MAX_DMODS);
		options.addOption("No d-mods", OptionId.QUALITY_NO_DMODS);
		options.addOption("Average of 1 s-mod", OptionId.QUALITY_1_SMODS);
		options.addOption("Average of 2 s-mods", OptionId.QUALITY_2_SMODS);
		options.addOption("Average of 2 s-mods; allow some Omega weapons to be mounted", OptionId.QUALITY_2_SMODS_OMEGA);
		options.addOption("Average of 3 s-mods", OptionId.QUALITY_3_SMODS);
		options.addOption("Average of 3 s-mods; allow some Omega weapons to be mounted", OptionId.QUALITY_3_SMODS_OMEGA);
		options.addOption("Leave", OptionId.LEAVE, null);
	}
	
	protected void selectOfficerLevel() {
		textPanel.addPara("The total number of officers and their level and elite skills will be based on your character's skills.");
		options.clearOptions();
		options.addOption("Add officers (number / level / elite skills based on your character's skills)", OptionId.ADD_OFFICERS);
		//options.addOption("Add officers, but do not spend their skill points", OptionId.ADD_OFFICERS_NO_SKILLS);
		options.addOption("Do not add officers", OptionId.NO_OFFICERS);
		options.addOption("Leave", OptionId.LEAVE, null);
	}
	
	protected void selectAppendOrReplace() {
		textPanel.addPara("Select fleet creation mode.");
		options.clearOptions();
		options.addOption("Replace current fleet with new ships", OptionId.CREATE_REPLACE);
		options.addOption("Add new ships to current fleet", OptionId.CREATE_ADD);
		options.addOption("Leave", OptionId.LEAVE, null);
	}
	
	protected OptionId lastOptionMousedOver = null;
	public void optionMousedOver(String optionText, Object optionData) {

	}
	
	public void advance(float amount) {
		
	}
	
	public Object getContext() {
		return null;
	}
	
	public void showBlueprintPicker() {
		dialog.showCustomProductionPicker(new BaseCustomProductionPickerDelegateImpl() {
			@Override
			public Set<String> getAvailableFighters() {
				return fighters;
			}
			@Override
			public Set<String> getAvailableShipHulls() {
				return ships;
			}
			@Override
			public Set<String> getAvailableWeapons() {
				return weapons;
			}
			@Override
			public float getCostMult() {
				return 0.001f;
			}
			@Override
			public float getMaximumValue() {
				return 1000000;
			}
			public boolean withQuantityLimits() {
				return false;
			}
			@Override
			public void notifyProductionSelected(FactionProductionAPI production) {
				//convertProdToCargo(production);
				data.prod = production;
				selectQuality();
			}
		});
	}
	
	protected void createFleet() {
		
		FactionProductionAPI prod = data.prod;
		CargoAPI cargo = Global.getFactory().createCargo(true);
		

		Random random = Misc.random;
		
		float quality = 2f;
		if (data.quality == FleetQuality.MAX_DMODS) {
			quality = -2f;
		}
		
		CampaignFleetAPI ships = Global.getFactory().createEmptyFleet(Factions.PLAYER, "temp", true);
		ships.setCommander(Global.getSector().getPlayerPerson());
		DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
		p.quality = quality;
		p.mode = ShipPickMode.PRIORITY_THEN_ALL;
		p.persistent = false;
		p.seed = random.nextLong();
		p.timestamp = null;
		p.rProb = 0f;
		p.allWeapons = true;
		
		if (data.quality == FleetQuality.ONE_SMOD) {
			p.averageSMods = 1;
		} else if (data.quality == FleetQuality.TWO_SMODS) {
			p.averageSMods = 2;
		} else if (data.quality == FleetQuality.THREE_SMODS) {
			p.averageSMods = 3;
		}
		
		FleetInflater inflater = Misc.getInflater(ships, p);
		ships.setInflater(inflater);
		
		for (ItemInProductionAPI pick : prod.getCurrent()) {
			int count = pick.getQuantity();

			if (pick.getType() == ProductionItemType.SHIP) {
				List<String> variants = Global.getSettings().getHullIdToVariantListMap().get(pick.getSpecId());
				if (variants.isEmpty()) {
					variants.add(pick.getSpecId() + "_Hull");
				}
				
				for (int i = 0; i < count; i++) {
					int index = random.nextInt(variants.size());
					ships.getFleetData().addFleetMember(variants.get(index));
				}
			} else if (pick.getType() == ProductionItemType.FIGHTER) {
				cargo.addFighters(pick.getSpecId(), count);
			} else if (pick.getType() == ProductionItemType.WEAPON) {
				cargo.addWeapons(pick.getSpecId(), count);
			}
			prod.removeItem(pick.getType(), pick.getSpecId(), count);
		}
		
		FactionAPI pf = Global.getSector().getPlayerFaction();
		List<String> addedWeapons = new ArrayList<String>();
		List<String> addedFighters = new ArrayList<String>();
		List<String> addedHullmods = new ArrayList<String>();
		
		if (data.allowOmega) {
			for (String id : weapons) {
				if (unrestrictedWeapons.contains(id)) continue;
				WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(id);
				if (spec.hasTag(Tags.OMEGA)) {
					unrestrictedWeapons.add(id);
				}
			}
		}
		
		for (String id : unrestrictedWeapons) {
			if (!pf.knowsWeapon(id)) {
				addedWeapons.add(id);
				pf.addKnownWeapon(id, false);
			}
		}
		for (String id : unrestrictedFighters) {
			if (!pf.knowsFighter(id)) {
				addedFighters.add(id);
				pf.addKnownFighter(id, false);
			}
		}
		for (String id : hullmods) {
			if (!pf.knowsHullMod(id)) {
				addedHullmods.add(id);
				pf.addKnownHullMod(id);
			}
		}
		
		ships.inflateIfNeeded();
		
		for (String id : addedWeapons) {
			pf.removeKnownWeapon(id);
		}
		for (String id : addedFighters) {
			pf.removeKnownFighter(id);
		}
		for (String id : addedHullmods) {
			pf.removeKnownHullMod(id);
		}
		
		if (!data.append && !ships.isEmpty()) {
			playerFleet.getFleetData().clear();
			if (data.addOfficers) {
				for (OfficerDataAPI od : playerFleet.getFleetData().getOfficersCopy()) {
					playerFleet.getFleetData().removeOfficer(od.getPerson());
				}
			}
		}
		
		playerFleet.getCargo().addAll(cargo);
		
		for (FleetMemberAPI member : ships.getFleetData().getMembersListCopy()) {
			playerFleet.getFleetData().addFleetMember(member);
		}

		if (data.addOfficers) {
			int maxOfficers = Misc.getMaxOfficers(playerFleet);
			int add = maxOfficers - playerFleet.getFleetData().getOfficersCopy().size();
			if (add > 0) {
				FleetParamsV3 fp = new FleetParamsV3();
				fp.commander = playerFleet.getCommander();
				fp.maxOfficersToAdd = add;
				FleetFactoryV3.addCommanderAndOfficersV2(playerFleet, fp, random);
			}
		}
		
		
		playerFleet.forceSync();
		
		cargo = playerFleet.getCargo();
		
		int neededCrew = (int) ((playerFleet.getFleetData().getMinCrew() + cargo.getMaxPersonnel()) / 2f 
									- cargo.getCrew());
		if (neededCrew > 0) {
			cargo.addCrew(neededCrew);
		}
		cargo.addFuel(cargo.getMaxFuel() - cargo.getFuel());
		cargo.addSupplies(cargo.getSpaceLeft() * 0.5f);
		
		
		playerFleet.forceSync();
		
		for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
			float max = member.getRepairTracker().getMaxCR();
			member.getRepairTracker().setCR(max);
		}
		
		
		
	}
	
	protected void initAvailable() {
		for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
			//if (spec.isDefaultDHull() || spec.isDHull()) continue;
			if (spec.isDefaultDHull()) continue;
			if (spec.getHullSize() == HullSize.FIGHTER) continue;
			if (!spec.hasHullName()) continue;
			if (spec.getHints().contains(ShipTypeHints.STATION)) continue;
//			if (spec.getHints().contains(ShipTypeHints.HIDE_IN_CODEX)) continue;
//			if (spec.getHints().contains(ShipTypeHints.UNBOARDABLE)) continue;
			if ("shuttlepod".equals(spec.getHullId())) continue;
			ships.add(spec.getHullId());
		}
		
		for (WeaponSpecAPI spec : Global.getSettings().getAllWeaponSpecs()) {
			//if (!spec.hasTag(Items.TAG_RARE_BP) && !spec.hasTag(Items.TAG_DEALER)) continue;
			if (spec.getAIHints().contains(AIHints.SYSTEM)) continue;
			if (!spec.hasTag(Tags.RESTRICTED)) {
				unrestrictedWeapons.add(spec.getWeaponId());
			}
			weapons.add(spec.getWeaponId());
		}
		
		for (FighterWingSpecAPI spec : Global.getSettings().getAllFighterWingSpecs()) {
			if (!spec.hasTag(Tags.RESTRICTED)) {
				unrestrictedFighters.add(spec.getId());
			}
			fighters.add(spec.getId());
		}
		
		for (HullModSpecAPI spec : Global.getSettings().getAllHullModSpecs()) {
			hullmods.add(spec.getId());
		}
	}
	
	protected void addNoSkillOfficer(int level) {
		PersonAPI p = Global.getSector().getPlayerFaction().createRandomPerson();
		p.getMemoryWithoutUpdate().set(MemFlags.OFFICER_SKILL_PICKS_PER_LEVEL, 10);
		p.getMemoryWithoutUpdate().set(MemFlags.OFFICER_MAX_LEVEL, level + 1);
		if (level >= 7) {
			p.getMemoryWithoutUpdate().set(MemFlags.OFFICER_MAX_ELITE_SKILLS, 4);
		}
//		p.getStats().setLevel(level + 1);
		for (SkillLevelAPI sl : p.getStats().getSkillsCopy()) {
			p.getStats().setSkillLevel(sl.getSkill().getId(), 0);
		}
		//p.setPersonality(Personalities.AGGRESSIVE);
		OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
		long xp = plugin.getXPForLevel(level + 1);
		OfficerDataAPI od = Global.getFactory().createOfficerData(p);
		od.addXP(xp);
		Global.getSector().getPlayerFleet().getFleetData().addOfficer(od);
		if (level >= 7) {
			textPanel.addPara("Added level " + (level + 1) + " officer with NO initial skill and 4 elite skills max.");	
		} else {
			textPanel.addPara("Added level " + (level + 1) + " officer with NO initial skill.");
		}
	}
}




