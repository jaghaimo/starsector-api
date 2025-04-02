package com.fs.starfarer.api.impl.combat.dweller;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;

/**
 * Not actually used - see DwellerCMD; that's used instead.
 * 
 * @author Alex
 *
 */
public class DwellerFIDConfig implements FIDConfigGen {
	public FIDConfig createConfig() {
		FIDConfig config = new FIDConfig();
		
		config.alwaysAttackVsAttack = true;
		//config.alwaysPursue = true;
		config.alwaysHarry = true;
		config.showTransponderStatus = false;
		//config.showEngageText = false;
		config.lootCredits = false;
		
		
		config.delegate = new BaseFIDDelegate() {
			public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
				if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return;

//				float mult = context.computePlayerContribFraction();
//				float p = Global.getSettings().getFloat("salvageHullmodProb");
//				float pItem = Global.getSettings().getFloat("salvageHullmodRequiredItemProb");
//				
//				CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
//
//				DataForEncounterSide data = context.getDataFor(fleet);
//				List<FleetMemberAPI> losses = new ArrayList<FleetMemberAPI>();
//				for (FleetMemberData fmd : data.getOwnCasualties()) {
//					losses.add(fmd.getMember());
//				}
//
//				Random random = Misc.getRandom(Misc.getSalvageSeed(fleet), 7);
//				//random = new Random();
//				
//				for (FleetMemberAPI member : losses) {
//					if (member.getHullSpec().hasTag(Tags.THREAT_FABRICATOR)) {
//						int rolls = 0;
//						switch (member.getHullSpec().getHullSize()) {
//						case CAPITAL_SHIP: rolls = 30; break;
//						case CRUISER: rolls = 18; break;
//						case DESTROYER: rolls = 12; break;
//						case FRIGATE: rolls = 6; break;
//						}
//						
//						WeightedRandomPicker<SwarmConstructableVariant> picker = new WeightedRandomPicker<>(random);
//						for (SwarmConstructableVariant curr : ConstructionSwarmSystemScript.CONSTRUCTABLE) {
//							picker.add(curr, curr.dp);
//						}
//						
//						for (int i = 0; i < rolls; i++) {
//							SwarmConstructableVariant pick = picker.pick();
//							ShipVariantAPI variant = Global.getSettings().getVariant(pick.variantId);
//							
//							for (String id : variant.getHullMods()) {
//								if (!variant.getHullSpec().isBuiltInMod(id)) {
//									if (random.nextFloat() < pItem && random.nextFloat() < mult) {
//										HullModSpecAPI spec = Global.getSettings().getHullModSpec(id);
//										CargoStackAPI item = spec.getEffect().getRequiredItem();
//										if (item != null) {
//											boolean addToLoot = true;
//											if (item.getSpecialItemSpecIfSpecial() != null && item.getSpecialItemSpecIfSpecial().hasTag(Tags.NO_DROP)) {
//												addToLoot = false;
//											} else if (item.getResourceIfResource() != null && item.getResourceIfResource().hasTag(Tags.NO_DROP)) {
//												addToLoot = false;
//											} else if (item.getFighterWingSpecIfWing() != null && item.getFighterWingSpecIfWing().hasTag(Tags.NO_DROP)) {
//												addToLoot = false;
//											} else if (item.getWeaponSpecIfWeapon() != null && item.getWeaponSpecIfWeapon().hasTag(Tags.NO_DROP)) {
//												addToLoot = false;
//											}
//											if (addToLoot) {
//												salvage.addItems(item.getType(), item.getData(), 1);
//											}
//										}
//									}
//								}
//								
//								//if (random.nextFloat() > mult) continue;
//								if (random.nextFloat() < p && random.nextFloat() < mult) {
//									HullModSpecAPI spec = Global.getSettings().getHullModSpec(id);
//									boolean known = Global.getSector().getPlayerFaction().knowsHullMod(id);
//									if (DebugFlags.ALLOW_KNOWN_HULLMOD_DROPS) known = false;
//									if (known || spec.isHidden() || spec.isHiddenEverywhere()) continue;
//									//if (spec.isAlwaysUnlocked()) continue;
//									if (spec.hasTag(Tags.HULLMOD_NO_DROP)) continue;
//									
//									salvage.addHullmods(id, 1);
//								}
//							}
//						}
//					}
//				}
			}
			
			public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
				bcc.aiRetreatAllowed = false;
				bcc.fightToTheLast = true;
				bcc.objectivesAllowed = false;
				bcc.enemyDeployAll = true;
			}
		};
		return config;
	}
}





