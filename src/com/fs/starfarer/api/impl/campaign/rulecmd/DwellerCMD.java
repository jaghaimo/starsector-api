package com.fs.starfarer.api.impl.campaign.rulecmd;

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
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams;
import com.fs.starfarer.api.campaign.FactionProductionAPI;
import com.fs.starfarer.api.campaign.FactionProductionAPI.ItemInProductionAPI;
import com.fs.starfarer.api.campaign.FactionProductionAPI.ProductionItemType;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.DataForEncounterSide;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.FleetMemberData;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemPlugin.RightClickActionHelper;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.impl.items.ShroudedHullmodItemPlugin;
import com.fs.starfarer.api.campaign.impl.items.ShroudedSubstratePlugin;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.AbyssalLightEntityPlugin;
import com.fs.starfarer.api.impl.campaign.AbyssalLightEntityPlugin.DespawnType;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.ListMap;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 */
public class DwellerCMD extends BaseCommandPlugin {
	
	public static enum DwellerStrength {
		LOW,
		MEDIUM,
		HIGH,
		EXTREME,
	}
	
	public static String SHROUDED_TENDRIL = "shrouded_tendril";
	public static String SHROUDED_EYE = "shrouded_eye";
	public static String SHROUDED_MAELSTROM = "shrouded_maelstrom";
	public static String SHROUDED_MAW = "shrouded_maw";
	

	public static ListMap<String> GUARANTEED_FIRST_TIME_ITEMS = new ListMap<>();
	static {
		GUARANTEED_FIRST_TIME_ITEMS.add(SHROUDED_EYE, Items.SHROUDED_LENS);
		GUARANTEED_FIRST_TIME_ITEMS.add(SHROUDED_MAELSTROM, Items.SHROUDED_THUNDERHEAD);
		GUARANTEED_FIRST_TIME_ITEMS.add(SHROUDED_MAW, Items.SHROUDED_MANTLE);
	}
	
	public static ListMap<String> DROP_GROUPS = new ListMap<>();
	static {
		DROP_GROUPS.add(SHROUDED_EYE, "drops_shrouded_eye");
		DROP_GROUPS.add(SHROUDED_MAELSTROM, "drops_shrouded_maelstrom");
		DROP_GROUPS.add(SHROUDED_MAW, "drops_shrouded_maw");
	}
	
	

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		CargoAPI cargo = pf.getCargo();
		
		final SectorEntityToken entity = dialog.getInteractionTarget();
		long seed = Misc.getSalvageSeed(entity);
		Random random = Misc.getRandom(seed, 11);
		//random = new Random();
		
		String action = params.get(0).getString(memoryMap);
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memory == null) return false; // should not be possible unless there are other big problems already
				
		if ("smallFleet".equals(action)) {
			return engageFleet(dialog, memoryMap, memory, DwellerStrength.LOW, random);
		} else if ("mediumFleet".equals(action)) {
			return engageFleet(dialog, memoryMap, memory, DwellerStrength.MEDIUM, random);
		} else if ("largeFleet".equals(action)) {
			return engageFleet(dialog, memoryMap, memory, DwellerStrength.HIGH, random);
		} else if ("hugeFleet".equals(action)) {
			return engageFleet(dialog, memoryMap, memory, DwellerStrength.EXTREME, random);
		} else if ("showWeaponPicker".equals(action)) {
			showWeaponPicker(dialog, memoryMap);
			return true;
		} else if ("unlockHullmod".equals(action)) {
			unlockHullmod(dialog, memoryMap);
			return true;
		}
		return false;
	}

	protected void unlockHullmod(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		String modId = Global.getSector().getPlayerMemoryWithoutUpdate().getString(
							ShroudedHullmodItemPlugin.SHROUDED_HULLMOD_ID);
		HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
		
		Global.getSoundPlayer().playUISound("ui_acquired_hullmod", 1, 1);
		TextPanelAPI text = dialog.getTextPanel();
		text.setFontSmallInsignia();
		String str = modSpec.getDisplayName(); 
		text.addParagraph("Acquired hull mod: " + str + "", Misc.getPositiveHighlightColor());
		text.highlightInLastPara(Misc.getHighlightColor(), str);
		text.setFontInsignia();
		
//		Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
//				"Acquired hull mod: " + modSpec.getDisplayName() + "");
		
		Global.getSector().getPlayerFaction().addKnownHullMod(modId);;
	}
	
	public static int getSubstrateCost(WeaponSpecAPI spec) {
		if (!spec.hasTag(Tags.DWELLER)) return 0;
		String substrate = "substrate_";
		for (String tag : spec.getTags()) {
			if (tag.startsWith(substrate)) {
				String num = tag.replaceFirst(substrate, "");
				return Integer.parseInt(num);
			}
		}
		return 0;
	}
	
	protected void showWeaponPicker(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		
		int substrate = Global.getSector().getPlayerMemoryWithoutUpdate().getInt(ShroudedSubstratePlugin.SHROUDED_SUBSTRATE_AVAILABLE);
		
		Set<String> weapons = new LinkedHashSet<>();
		for (WeaponSpecAPI spec : Global.getSettings().getAllWeaponSpecs()) {
			int cost = getSubstrateCost(spec);
			if (cost > 0 && cost <= substrate) {
				weapons.add(spec.getWeaponId());
			}
		}
		
		dialog.showCustomProductionPicker(new BaseCustomProductionPickerDelegateImpl() {
			@Override
			public Set<String> getAvailableFighters() {
				return new LinkedHashSet<>();
			}
			@Override
			public Set<String> getAvailableShipHulls() {
				return new LinkedHashSet<>();
			}
			@Override
			public Set<String> getAvailableWeapons() {
				return weapons;
			}
			@Override
			public float getCostMult() {
				return 1f;
			}
			@Override
			public float getMaximumValue() {
				return substrate;
			}
			
			@Override
			public String getWeaponColumnNameOverride() {
				return "Weapon";
			}

			@Override
			public String getNoMatchingBlueprintsLabelOverride() {
				return "No matching weapons";
			}

			@Override
			public String getMaximumOrderValueLabelOverride() {
				return "Shrouded Substrate available";
			}

			@Override
			public String getCurrentOrderValueLabelOverride() {
				return "Shrouded Substrate required";
			}
			@Override
			public String getItemGoesOverMaxValueStringOverride() {
				return "Not enough Shrouded Substrate";
			}
			@Override
			public String getCustomOrderLabelOverride() {
				return "Weapon assembly";
			}
			@Override
			public String getNoProductionOrdersLabelOverride() {
				return "No assembly orders";
			}
			@Override
			public boolean withQuantityLimits() {
				return false;
			}
			@Override
			public boolean isUseCreditSign() {
				return false;
			}

			@Override
			public int getCostOverride(Object item) {
				if (item instanceof WeaponSpecAPI) {
					return getSubstrateCost((WeaponSpecAPI) item);
				}
				return -1;
			}
			
			@Override
			public void notifyProductionSelected(FactionProductionAPI production) {
				if (!(dialog.getPlugin() instanceof RuleBasedInteractionDialogPluginImpl)) return;
				RuleBasedInteractionDialogPluginImpl plugin = (RuleBasedInteractionDialogPluginImpl) dialog.getPlugin();
				if (!(plugin.getCustom1() instanceof RightClickActionHelper)) return;
				RightClickActionHelper helper = (RightClickActionHelper) plugin.getCustom1();
				
				int cost = production.getTotalCurrentCost();
				helper.removeFromClickedStackFirst(cost);
				int substrate = (int) helper.getNumItems(CargoItemType.SPECIAL, new SpecialItemData(Items.SHROUDED_SUBSTRATE, null));
				Global.getSector().getPlayerMemoryWithoutUpdate().set(ShroudedSubstratePlugin.SHROUDED_SUBSTRATE_AVAILABLE, substrate);
				
				for (ItemInProductionAPI item : production.getCurrent()) {
					if (item.getType() == ProductionItemType.WEAPON) {
						helper.addItems(CargoItemType.WEAPONS, item.getSpecId(), item.getQuantity());
						AddRemoveCommodity.addWeaponGainText(item.getSpecId(), item.getQuantity(), dialog.getTextPanel());
					}
				}
				
				FireBest.fire(null, dialog, memoryMap, "SubstrateWeaponsPicked");
				
				Global.getSoundPlayer().playUISound("ui_cargo_machinery_drop", 1f, 1f);
			}
		});
	}

	
	protected boolean engageFleet(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, MemoryAPI memory, DwellerStrength str, Random random) {
		CampaignFleetAPI fleet = createDwellerFleet(str, random);
		if (fleet == null) return false;
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		fleet.setContainingLocation(pf.getContainingLocation());
		
		final SectorEntityToken entity = dialog.getInteractionTarget();
		
		dialog.setInteractionTarget(fleet);
		
		Global.getSector().getCampaignUI().restartEncounterMusic(fleet);
		
		FIDConfig config = new FIDConfig();
		
		config.delegate = new BaseFIDDelegate() {
			public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
				if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return;
				
				float mult = context.computePlayerContribFraction();
				
				CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();

				DataForEncounterSide data = context.getDataFor(fleet);
				List<FleetMemberAPI> losses = new ArrayList<FleetMemberAPI>();
				for (FleetMemberData fmd : data.getOwnCasualties()) {
					losses.add(fmd.getMember());
				}
				
				float min = 0f;
				float max = 0f;
				boolean gotGuaranteed = false;
				for (FleetMemberAPI member : losses) {
					if (member.getHullSpec().hasTag(Tags.DWELLER)) {
						String key = "substrate_";
						float [] sDrops = Misc.getFloatArray(key + member.getHullSpec().getHullId());
						if (sDrops == null) {
							sDrops = Misc.getFloatArray(key + member.getHullSpec().getHullSize().name());
						}
						if (sDrops == null) continue;
						
						min += sDrops[0];
						max += sDrops[1];
						
						String hullId = member.getHullSpec().getRestoredToHullId();
						String defeatedKey = "$defeatedDweller_" + hullId;
						boolean firstTime = !Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(defeatedKey);
						Global.getSector().getPlayerMemoryWithoutUpdate().set(defeatedKey, true);
						if (firstTime && !gotGuaranteed) {
							List<String> drops = GUARANTEED_FIRST_TIME_ITEMS.get(hullId);
							for (String itemId : drops) {
								SpecialItemData sid = new SpecialItemData(itemId, null);
								boolean add = firstTime && salvage.getQuantity(CargoItemType.SPECIAL, sid) <= 0;
								if (add) {
									salvage.addItems(CargoItemType.SPECIAL, sid, 1);
									gotGuaranteed = true;
								}
							}
						}
					}
				}
				
				long seed = Misc.getSalvageSeed(entity);
				Random random = Misc.getRandom(seed, 50);
				int substrate = 0;
				if (min + max < 1f) {
					if (random.nextFloat() < (min + max) / 2f) {
						substrate = 1;
					}
				} else {
					substrate = (int) Math.round(min + (max - min) * random.nextFloat());
				}
				
				if (substrate > 0) {
					salvage.addItems(CargoItemType.SPECIAL, new SpecialItemData(Items.SHROUDED_SUBSTRATE, null), substrate);
				}
			}
			
			public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
				bcc.aiRetreatAllowed = false;
				bcc.fightToTheLast = true;
				bcc.objectivesAllowed = false;
				bcc.enemyDeployAll = true;
				
				// despawn the light here - the salvage gen method is only called if the player won
				// but want to despawn the light after any fight, regardless
				if (entity.getCustomPlugin() instanceof AbyssalLightEntityPlugin) {
					AbyssalLightEntityPlugin plugin = (AbyssalLightEntityPlugin) entity.getCustomPlugin();
					plugin.despawn(DespawnType.FADE_OUT);
				}
			}
		};
		
		config.alwaysAttackVsAttack = true;
		//config.alwaysPursue = true;
		config.alwaysHarry = true;
		config.showTransponderStatus = false;
		//config.showEngageText = false;
		config.lootCredits = false;		
		
		config.showCommLinkOption = false;
		config.showEngageText = false;
		config.showFleetAttitude = false;
		config.showTransponderStatus = false;
		config.showWarningDialogWhenNotHostile = false;
		config.impactsAllyReputation = false;
		config.impactsEnemyReputation = false;
		config.pullInAllies = false;
		config.pullInEnemies = false;
		config.pullInStations = false;
		
		config.showCrRecoveryText = false;
		config.firstTimeEngageOptionText = "\"Battle stations!\"";
		config.afterFirstTimeEngageOptionText = "Move in to re-engage";
		
		if (str == DwellerStrength.LOW) {
			config.firstTimeEngageOptionText = null;
			config.leaveAlwaysAvailable = true;
		} else {
			config.leaveAlwaysAvailable = true; // except for first engagement
			config.noLeaveOptionOnFirstEngagement = true;
		}
		//config.noLeaveOption = true;
		
//		config.noSalvageLeaveOptionText = "Continue";
		
//		config.dismissOnLeave = false;
//		config.printXPToDialog = true;
		
		long seed = Misc.getSalvageSeed(entity);
		config.salvageRandom = Misc.getRandom(seed, 75);
		
		Global.getSector().getPlayerMemoryWithoutUpdate().set("$encounteredDweller", true);
		Global.getSector().getPlayerMemoryWithoutUpdate().set("$encounteredMonster", true);
		Global.getSector().getPlayerMemoryWithoutUpdate().set("$encounteredWeird", true);
		
		final FleetInteractionDialogPluginImpl plugin = new FleetInteractionDialogPluginImpl(config);
		
		//final InteractionDialogPlugin originalPlugin = dialog.getPlugin();
		
		dialog.setPlugin(plugin);
		plugin.init(dialog);
		
		
		return true;
	}



	public static CampaignFleetAPI createDwellerFleet(DwellerStrength str, Random random) {
		CampaignFleetAPI f = Global.getFactory().createEmptyFleet(Factions.DWELLER, "Manifestation", true);
		
		FactionAPI faction = Global.getSector().getFaction(Factions.DWELLER);
		String typeKey = FleetTypes.PATROL_SMALL;
		if (str == DwellerStrength.MEDIUM) typeKey = FleetTypes.PATROL_MEDIUM; 
		if (str == DwellerStrength.HIGH) typeKey = FleetTypes.PATROL_LARGE; 
		if (str == DwellerStrength.EXTREME) typeKey = FleetTypes.PATROL_LARGE; 
		f.setName(faction.getFleetTypeName(typeKey));
		
		f.setInflater(null);
		
		if (str == DwellerStrength.LOW) {
			addShips(f, 6, 8, random, ShipRoles.DWELLER_TENDRIL);
			addShips(f, 1, 1, random, ShipRoles.DWELLER_EYE);
			addShips(f, 1, 2, random, ShipRoles.DWELLER_MAELSTROM);
		} else if (str == DwellerStrength.MEDIUM) {
			addShips(f, 9, 12, random, ShipRoles.DWELLER_TENDRIL);
			int eyes = addShips(f, 1, 1, random, ShipRoles.DWELLER_EYE);
			addShips(f, 2 - eyes, 3 - eyes, random, ShipRoles.DWELLER_MAELSTROM);
			addShips(f, 1, 1, random, ShipRoles.DWELLER_MAW);
		} else if (str == DwellerStrength.HIGH) {
			addShips(f, 11, 14, random, ShipRoles.DWELLER_TENDRIL);
			int eyes = addShips(f, 2, 3, random, ShipRoles.DWELLER_EYE);
			addShips(f, 3 - eyes, 5 - eyes, random, ShipRoles.DWELLER_MAELSTROM);
			addShips(f, 1, 1, random, ShipRoles.DWELLER_MAW);
		} else if (str == DwellerStrength.EXTREME) {
			addShips(f, 12, 15, random, ShipRoles.DWELLER_TENDRIL);
			int eyes = addShips(f, 2, 3, random, ShipRoles.DWELLER_EYE);
			addShips(f, 3 - eyes, 5 - eyes, random, ShipRoles.DWELLER_MAELSTROM);
			addShips(f, 2, 2, random, ShipRoles.DWELLER_MAW);
		}
		
		f.getFleetData().setSyncNeeded();
		f.getFleetData().syncIfNeeded();
		f.getFleetData().sort();
		
		for (FleetMemberAPI curr : f.getFleetData().getMembersListCopy()) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
			
			// tag is added to ships now
//			ShipVariantAPI v = curr.getVariant().clone();
//			v.addTag(Tags.LIMITED_TOOLTIP_IF_LOCKED);
//			curr.setVariant(v, false, false);
		}
		
		
//		f.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, 
//				   			new DwellerFIDConfig());
//		f.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
		
		// required for proper music track to play, see: DwellerCMD
		f.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
		
//		//f.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, true);
//		f.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true);
		f.getMemoryWithoutUpdate().set(MemFlags.MAY_GO_INTO_ABYSS, true);
		
		return f;
	}
	
	
	public static int addShips(CampaignFleetAPI fleet, int min, int max, Random random, Object ... roles) {
		if (min < 0) min = 0;
		if (max < 0) max = 0;
		
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
		if (roles.length == 1) {
			picker.add((String) roles[0], 1f);
		} else {
			for (int i = 0; i < roles.length; i += 2) {
				picker.add((String) roles[i], (float) roles[i + 1]);
			}
		}
		int num = min + random.nextInt(max - min + 1);
		FactionAPI faction = Global.getSector().getFaction(Factions.DWELLER);
		
		ShipPickParams p = new ShipPickParams(ShipPickMode.ALL);
		p.blockFallback = true;
		p.maxFP = 1000000;
		for (int i = 0; i < num; i++) {
			String role = picker.pick();
			List<ShipRolePick> picks = faction.pickShip(role, p, null, random);
			for (ShipRolePick pick : picks) {
				fleet.getFleetData().addFleetMember(pick.variantId);
				
				ShipVariantAPI variant = Global.getSettings().getVariant(pick.variantId);
				if (variant != null) {
					String hullId = variant.getHullSpec().getRestoredToHullId();
					List<String> dropGroups = DROP_GROUPS.get(hullId);
					for (String group : dropGroups) {
						fleet.addDropRandom(group, 1);
					}
				}
			}
		}
		return num;
	}
}

