package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.SeededFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class RemnantSeededFleetManager extends SeededFleetManager {

//	public static class DerelictFleetInteractionConfigGen implements FIDConfigGen {
//		public FIDConfig createConfig() {
//			FIDConfig config = new FIDConfig();
//			config.showTransponderStatus = false;
//			config.delegate = new BaseFIDDelegate() {
//				public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
//					if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return;
//					
//					CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
//					
//					DataForEncounterSide data = context.getDataFor(fleet);
//					List<FleetMemberAPI> losses = new ArrayList<FleetMemberAPI>();
//					for (FleetMemberData fmd : data.getOwnCasualties()) {
//						losses.add(fmd.getMember());
//					}
//					
//					List<DropData> dropRandom = new ArrayList<DropData>();
//					
//					int [] counts = new int[5];
//					String [] groups = new String [] {Drops.REM_FRIGATE, Drops.REM_DESTROYER, 
//													  Drops.REM_CRUISER, Drops.REM_CAPITAL,
//													  Drops.GUARANTEED_ALPHA};
//					
//					//for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
//					for (FleetMemberAPI member : losses) {
//						if (member.isStation()) {
//							counts[4] += 1;
//							counts[3] += 1;
//						} else if (member.isCapital()) {
//							counts[3] += 1;
//						} else if (member.isCruiser()) {
//							counts[2] += 1;
//						} else if (member.isDestroyer()) {
//							counts[1] += 1;
//						} else if (member.isFrigate()) {
//							counts[0] += 1;
//						}
//					}
//					
////					if (fleet.isStationMode()) {
////						counts[2] += 10;
////					}
//
//					for (int i = 0; i < counts.length; i++) {
//						int count = counts[i];
//						if (count <= 0) continue;
//						
//						DropData d = new DropData();
//						d.group = groups[i];
//						d.chances = (int) Math.ceil(count * 1f);
//						dropRandom.add(d);
//					}
//					
//					Random salvageRandom = new Random(Misc.getSalvageSeed(fleet));
//					//salvageRandom = new Random();
//					CargoAPI extra = SalvageEntity.generateSalvage(salvageRandom, 1f, 1f, 1f, 1f, null, dropRandom);
//					for (CargoStackAPI stack : extra.getStacksCopy()) {
//						salvage.addFromStack(stack);
//					}
//				}
//				public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
//					bcc.aiRetreatAllowed = false;
//					bcc.objectivesAllowed = false;
//				}
//			};
//			return config;
//		}
//	}
	
	
	public static class RemnantFleetInteractionConfigGen implements FIDConfigGen {
		public FIDConfig createConfig() {
			FIDConfig config = new FIDConfig();
			config.showTransponderStatus = false;
			config.delegate = new BaseFIDDelegate() {
//				public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
//					if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return;
//					
//					CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
//					
//					DataForEncounterSide data = context.getDataFor(fleet);
//					List<FleetMemberAPI> losses = new ArrayList<FleetMemberAPI>();
//					for (FleetMemberData fmd : data.getOwnCasualties()) {
//						losses.add(fmd.getMember());
//					}
//					
//					List<DropData> dropRandom = new ArrayList<DropData>();
//					
//					int [] counts = new int[5];
//					String [] groups = new String [] {Drops.REM_FRIGATE, Drops.REM_DESTROYER, 
//													  Drops.REM_CRUISER, Drops.REM_CAPITAL,
//													  Drops.GUARANTEED_ALPHA};
//					
//					//for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
//					for (FleetMemberAPI member : losses) {
//						if (member.isStation()) {
//							counts[4] += 1;
//							counts[3] += 1;
//						} else if (member.isCapital()) {
//							counts[3] += 1;
//						} else if (member.isCruiser()) {
//							counts[2] += 1;
//						} else if (member.isDestroyer()) {
//							counts[1] += 1;
//						} else if (member.isFrigate()) {
//							counts[0] += 1;
//						}
//					}
//					
////					if (fleet.isStationMode()) {
////						counts[2] += 10;
////					}
//
//					for (int i = 0; i < counts.length; i++) {
//						int count = counts[i];
//						if (count <= 0) continue;
//						
//						DropData d = new DropData();
//						d.group = groups[i];
//						d.chances = (int) Math.ceil(count * 1f);
//						dropRandom.add(d);
//					}
//					
//					Random salvageRandom = new Random(Misc.getSalvageSeed(fleet));
//					//salvageRandom = new Random();
//					CargoAPI extra = SalvageEntity.generateSalvage(salvageRandom, 1f, 1f, 1f, 1f, null, dropRandom);
//					for (CargoStackAPI stack : extra.getStacksCopy()) {
//						salvage.addFromStack(stack);
//					}
//				}
				public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
					bcc.aiRetreatAllowed = false;
					//bcc.objectivesAllowed = false;
				}
			};
			return config;
		}
	}
	
	
	protected int minPts;
	protected int maxPts;
	protected float activeChance;

	public RemnantSeededFleetManager(StarSystemAPI system, int minFleets, int maxFleets, int minPts, int maxPts, float activeChance) {
		super(system, 1f);
		this.minPts = minPts;
		this.maxPts = maxPts;
		this.activeChance = activeChance;
		
		int num = minFleets + StarSystemGenerator.random.nextInt(maxFleets - minFleets + 1);
		for (int i = 0; i < num; i++) {
			long seed = StarSystemGenerator.random.nextLong();
			addSeed(seed);
		}
	}

	@Override
	protected CampaignFleetAPI spawnFleet(long seed) {
		Random random = new Random(seed);
		
		int combatPoints = minPts + random.nextInt(maxPts - minPts + 1);
		
		String type = FleetTypes.PATROL_SMALL;
		if (combatPoints > 8) type = FleetTypes.PATROL_MEDIUM;
		if (combatPoints > 16) type = FleetTypes.PATROL_LARGE;
		
		combatPoints *= 8f; // 8 is fp cost of remnant frigate
		
		FleetParamsV3 params = new FleetParamsV3(
				system.getLocation(),
				Factions.REMNANTS,
				1f,
				type,
				combatPoints, // combatPts
				0f, // freighterPts 
				0f, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
		);
		params.withOfficers = false;
		params.random = random;
		
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet == null) return null;
		
		system.addEntity(fleet);
		fleet.setFacing(random.nextFloat() * 360f);
		
		
		boolean dormant = random.nextFloat() >= activeChance;
		//dormant = false;
		int numActive = 0;
		for (SeededFleet f : fleets) {
			if (f.fleet != null) numActive++;
		}
		if (numActive == 0 && activeChance > 0) { // first fleet is not dormant, to ensure one active fleet always
			dormant = false;
		}
		initRemnantFleetProperties(random, fleet, dormant);
		
		if (dormant) {
			SectorEntityToken target = pickEntityToGuard(random, system, fleet);
			if (target != null) {
//				Vector2f loc = Misc.getPointAtRadius(target.getLocation(), 300f, random);
//				fleet.setLocation(loc.x, loc.y);
				
				fleet.setCircularOrbit(target, 
						random.nextFloat() * 360f, 
						fleet.getRadius() + target.getRadius() + 100f + 100f * random.nextFloat(),
						25f + 5f * random.nextFloat());
			} else {
				Vector2f loc = Misc.getPointAtRadius(new Vector2f(), 4000f, random);
				fleet.setLocation(loc.x, loc.y);
			}
		} else {
			fleet.addScript(new RemnantAssignmentAI(fleet, system, null));
		}
		
		return fleet;
	}
	
	
	public static SectorEntityToken pickEntityToGuard(Random random, StarSystemAPI system, CampaignFleetAPI fleet) {
		WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>(random);
		
		for (SectorEntityToken entity : system.getEntitiesWithTag(Tags.SALVAGEABLE)) {
			float w = 1f;
			if (entity.hasTag(Tags.NEUTRINO_HIGH)) w = 3f;
			if (entity.hasTag(Tags.NEUTRINO_LOW)) w = 0.33f;
			picker.add(entity, w);
		}
		
		for (SectorEntityToken entity : system.getJumpPoints()) {
			picker.add(entity, 1f);
		}
		
		return picker.pick();
	}
	
	
	
	public static void initRemnantFleetProperties(Random random, CampaignFleetAPI fleet, boolean dormant) {
		if (random == null) random = new Random();
		
		fleet.removeAbility(Abilities.EMERGENCY_BURN);
		fleet.removeAbility(Abilities.SENSOR_BURST);
		fleet.removeAbility(Abilities.GO_DARK);
		
		// to make sure they attack the player on sight when player's transponder is off
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER, true);
		
		// to make dormant fleets not try to retreat and get harried repeatedly for CR loss 
		if (dormant) {
			//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
		}
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
		
		if (dormant) {
			fleet.setTransponderOn(false);
//			fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_PATROL_FLEET);
//			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true); // so they don't turn transponder on
//			fleet.addAssignment(FleetAssignment.HOLD, null, 1000000f, "dormant");
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
			fleet.setAI(null);
			fleet.setNullAIActionText("dormant");
		}
		
		addRemnantInteractionConfig(fleet);
		//addRemnantAICoreDrops(random, fleet, 1f);
		
		long salvageSeed = random.nextLong();
		fleet.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, salvageSeed);
	}
	
	public static void addRemnantInteractionConfig(CampaignFleetAPI fleet) {
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, 
				   new RemnantFleetInteractionConfigGen());		
	}
	
//	public static void addRemnantAICoreDrops(Random random, CampaignFleetAPI fleet, float mult) {
//		if (random == null) random = new Random();
//		long salvageSeed = random.nextLong();
//		fleet.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, salvageSeed);
//		
//		int [] counts = new int[3];
//		String [] groups = new String [] {Drops.AI_CORES1, Drops.AI_CORES2, Drops.AI_CORES3};
//		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
//			if (member.isCapital()) {
//				counts[2] += 2;
//			} else if (member.isCruiser()) {
//				counts[2] += 1;
//			} else if (member.isDestroyer()) {
//				counts[1] += 1;
//			} else if (member.isFrigate()) {
//				counts[0] += 1;
//			}
//		}
//		
//		if (fleet.isStationMode()) {
//			counts[2] += 10;
//		}
//
//		for (int i = 0; i < counts.length; i++) {
//			int count = counts[i];
//			if (count <= 0) continue;
//			
//			DropData d = new DropData();
//			d.group = groups[i];
//			d.chances = (int) Math.ceil(count * mult);
//			fleet.addDropRandom(d);
//		}
//		
//	}

}







