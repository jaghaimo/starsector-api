package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.fleets.AutoDespawnScript;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.raid.AssembleStage;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.TransmitterTrapSpecial;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class ScientistAICoreIntel extends BaseIntelPlugin {

	protected SectorEntityToken cache;
	protected StarSystemAPI cacheSystem;
	protected ScientistAICoreBarEvent event;
	protected boolean keptPromise = true;
	
	public ScientistAICoreIntel(SectorEntityToken cache, ScientistAICoreBarEvent event) {
		this.cache = cache;
		cacheSystem = cache.getStarSystem();
		this.event = event;
		Misc.makeImportant(cache, "saci");
		
		cache.getMemoryWithoutUpdate().set("$saic_eventRef", this);
	}
	
	@Override
	public boolean callEvent(String ruleId, InteractionDialogAPI dialog,
							 List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		MemoryAPI memory = cache.getMemoryWithoutUpdate();
		
		if (action.equals("putValuesInMemory")) {
			memory.set("$saic_marketOnOrAt", event.getShownAt().getOnOrAt(), 0);
			memory.set("$saic_marketName", event.getShownAt().getName(), 0);
			memory.set("$saic_heOrShe", event.getHeOrShe(), 0);
		} else if (action.equals("genLootWithCore")) {
			genLoot(dialog, memoryMap, true);
		} else if (action.equals("genLootNoCore")) {
			genLoot(dialog, memoryMap, false);
		}
		
		return true;
	}
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		// actually, probably makes more sense as a one-off
//		float timeout = 120f + (float) Math.random() * 120f;
//		BarEventManager.getInstance().setTimeout(ScientistAICoreBarEventCreator.class, timeout);
	}

	protected void genLoot(final InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, boolean withCore) {
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		
		MemoryAPI memory = cache.getMemoryWithoutUpdate();
		long seed = memory.getLong(MemFlags.SALVAGE_SEED);
		Random random = Misc.getRandom(seed, 100);
		
		DropData d = new DropData();
		d.chances = 10;
		d.group = "blueprints";
		cache.addDropRandom(d);
		
		d = new DropData();
		d.chances = 1;
		d.group = "rare_tech";
		cache.addDropRandom(d);
		
		CargoAPI salvage = SalvageEntity.generateSalvage(random,
				1f, 1f, 1f, 1f, cache.getDropValue(), cache.getDropRandom());
		CargoAPI extra = BaseSalvageSpecial.getCombinedExtraSalvage(memoryMap);
		salvage.addAll(extra);
		BaseSalvageSpecial.clearExtraSalvage(memoryMap);
		if (!extra.isEmpty()) {
			ListenerUtil.reportExtraSalvageShown(cache);
		}
		
		if (withCore) {
			salvage.addCommodity(Commodities.ALPHA_CORE, 1);
			keptPromise = false;
		} else {
			keptPromise = true;
		}
		
		
		dialog.getVisualPanel().showLoot("Salvaged", salvage, false, true, true, new CoreInteractionListener() {
			public void coreUIDismissed() {
				dialog.dismiss();
				dialog.hideTextPanel();
				dialog.hideVisualPanel();
				Misc.fadeAndExpire(cache);
				
				Global.getSector().addScript(ScientistAICoreIntel.this);
				endAfterDelay();
				cache = null;
			}
		});
		options.clearOptions();
		dialog.setPromptText("");
		
		if (keptPromise) {
			if (random.nextFloat() > 0.5f) {
				SectorEntityToken loc = cache.getContainingLocation().createToken(cache.getLocation());
				spawnPiratesToInvestigate(loc, 50f + random.nextFloat() * 50f);
				if (random.nextFloat() > 0.5f) {
					spawnPiratesToInvestigate(loc, 50f + random.nextFloat() * 50f);
				}
			}
		}
	}


	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		boolean isUpdate = getListInfoParam() != null;
		
		initPad = 0f;
		
		unindent(info);
	}
	
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
		addBulletPoints(info, mode);
		
	}
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		

		CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(Commodities.ALPHA_CORE);
		if (spec != null) {
			info.addImage(spec.getIconName(), width, 80, opad);
		}
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
		
		if (cacheSystem == null) {
			return; // hack to make old save work...
		}
		
		if (isEnding()) {
			info.addPara("You've found and looted the cache of pre-Collapse technology in the " +
					cacheSystem.getNameWithLowercaseType() + ".", opad);
			if (keptPromise) {
				info.addPara("You've kept your promise to the scientist and placed the AI core in a secure " +
						"container, to be shipped to " + event.getHimOrHer() + " as soon as you're back " + 
						"in civilized space.", opad);
			} else {
				info.addPara("You've taken the AI core promised to the scientist for yourself. " +
						"Surely, " + event.getHeOrShe() + " was no-one important.", opad);
			}
		} else {
			info.addPara("You've learned that there is a cache of pre-Collapse technology to be found in the " +
					cacheSystem.getNameWithLowercaseType() + ".", opad);
			
			info.addPara("The scientist that informed you of this wants the Alpha Core from this " +
						 "cache as payment for " + event.getHisOrHer() + " services.", opad);
		}
		
	}
	
	@Override
	public String getIcon() {
		//CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(Commodities.AI_CORES);
		CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(Commodities.ALPHA_CORE);
		return spec.getIconName();
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_MISSIONS);
		tags.add(Tags.INTEL_ACCEPTED);
		tags.add(Tags.INTEL_EXPLORATION);
		return tags;
	}
	
	public String getSortString() {
		return "Technology Cache";
	}
	
	public String getName() {
		if (isEnded() || isEnding()) {
			return "Technology Cache - Looted";
		}
		return "Technology Cache";
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return super.getFactionForUIColors();
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return cache;
	}
	
	@Override
	public boolean shouldRemoveIntel() {
		return super.shouldRemoveIntel();
		//return false;
	}

	@Override
	public String getCommMessageSound() {
		return "ui_discovered_entity";
	}
	
	
	
	public static void spawnPiratesToInvestigate(SectorEntityToken locToken, float fp) {
		
		PatrolType type;
		if (fp < AssembleStage.FP_SMALL * 1.5f) {
			type = PatrolType.FAST;
		} else if (fp < AssembleStage.FP_MEDIUM * 1.5f) {
			type = PatrolType.COMBAT;
		} else {
			type = PatrolType.HEAVY;
		}
		
		FleetParamsV3 params = new FleetParamsV3(
				null,
				Global.getSector().getPlayerFleet().getLocationInHyperspace(),
				Factions.PIRATES,
				null, 
				type.getFleetType(),
				fp, // combatPts
				0f, // freighterPts 
				fp * 0.1f, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
		);

		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet.isEmpty()) fleet = null;
		
		if (fleet != null) {
			fleet.addScript(new AutoDespawnScript(fleet));
			
			fleet.setTransponderOn(false);
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
			
			float range = 2000f + (float) Math.random() * 2000f;
			Vector2f loc = Misc.getPointAtRadius(locToken.getLocation(), range);
			
			locToken.getContainingLocation().addEntity(fleet);
			fleet.setLocation(loc.x, loc.y);
			
			TransmitterTrapSpecial.makeFleetInterceptPlayer(fleet, false, true, 30f);
			
			fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, locToken, 1000f);
			//fleet.addDropRandom("blueprints_guaranteed", 1);
		}
		
	}
	
}







