package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.PlanetaryShield;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.PlanetaryShieldIntel;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.PlanetaryShieldIntel.PSIStage;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * 
 */
public class RedPlanet extends BaseCommandPlugin {

	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected PlanetAPI planet;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected CargoAPI playerCargo;
	protected MemoryAPI memory;
	protected MarketAPI market;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	protected FactionAPI faction;

	
	public RedPlanet() {
	}
	
	public RedPlanet(SectorEntityToken entity) {
		init(entity);
	}
	
	protected void init(SectorEntityToken entity) {
		memory = entity.getMemoryWithoutUpdate();
		this.entity = entity;
		planet = (PlanetAPI) entity;
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		
		faction = entity.getFaction();
		
		market = entity.getMarket();
		
		
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
		
		if (command.equals("genLoot")) {
			genLoot();
		}
		
		return true;
	}

	protected void genLoot() {
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		
		MemoryAPI memory = planet.getMemoryWithoutUpdate();
		long seed = memory.getLong(MemFlags.SALVAGE_SEED);
		Random random = Misc.getRandom(seed, 100);
		
		DropData d = new DropData();
		d.chances = 5;
		d.group = "blueprints";
		planet.addDropRandom(d);
		
		d = new DropData();
		d.chances = 1;
		d.group = "rare_tech";
		planet.addDropRandom(d);
		
		CargoAPI salvage = SalvageEntity.generateSalvage(random, 1f, 1f, 1f, 1f, planet.getDropValue(), planet.getDropRandom());
		CargoAPI extra = BaseSalvageSpecial.getCombinedExtraSalvage(memoryMap);
		salvage.addAll(extra);
		BaseSalvageSpecial.clearExtraSalvage(memoryMap);
		if (!extra.isEmpty()) {
			ListenerUtil.reportExtraSalvageShown(planet);
		}
		salvage.addSpecial(new SpecialItemData("industry_bp", "planetaryshield"), 1);
		salvage.sort();
		
		dialog.getVisualPanel().showLoot("Salvaged", salvage, false, true, true, new CoreInteractionListener() {
			public void coreUIDismissed() {
				dialog.dismiss();
				dialog.hideTextPanel();
				dialog.hideVisualPanel();
				
				PlanetaryShieldIntel intel = (PlanetaryShieldIntel) Global.getSector().getIntelManager().getFirstIntel(PlanetaryShieldIntel.class);
				if (intel != null) {
					Global.getSector().addScript(intel);
					intel.endAfterDelay();
					//intel.sendUpdate(PSIStage.DONE, textPanel);
					intel.sendUpdateIfPlayerHasIntel(PSIStage.DONE, false);
				}
				long xp = PlanetaryShieldIntel.FINISHED_XP;
				Global.getSector().getPlayerPerson().getStats().addXP(xp);
			}
		});
		options.clearOptions();
		dialog.setPromptText("");
		
		
		planet.getMemoryWithoutUpdate().unset(MiscellaneousThemeGenerator.PLANETARY_SHIELD_PLANET);
		Global.getSector().getMemoryWithoutUpdate().unset(MiscellaneousThemeGenerator.PLANETARY_SHIELD_PLANET_KEY);
		PlanetaryShield.unapplyVisuals(planet);
		
//		if (keptPromise) {
//			if (random.nextFloat() > 0.5f) {
//				SectorEntityToken loc = planet.getContainingLocation().createToken(planet.getLocation());
//				spawnPiratesToInvestigate(loc, 50f + random.nextFloat() * 50f);
//				if (random.nextFloat() > 0.5f) {
//					spawnPiratesToInvestigate(loc, 50f + random.nextFloat() * 50f);
//				}
//			}
//		}
	}
	
}




















