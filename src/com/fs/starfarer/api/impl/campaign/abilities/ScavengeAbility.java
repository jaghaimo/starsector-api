package com.fs.starfarer.api.impl.campaign.abilities;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageEntityGeneratorOld;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class ScavengeAbility extends BaseDurationAbility {

	private IntervalUtil interval = null;
	
	@Override
	protected String getActivationText() {
		return "Scavenging";
	}

	@Override
	protected void activateImpl() {
		if (entity.isPlayerFleet()) {
			CampaignFleetAPI fleet = getFleet();
			DebrisFieldTerrainPlugin debris = getDebrisField();
			if (debris != null) {
				SectorEntityToken target = debris.getEntity().getContainingLocation().addCustomEntity(
									null, null, Entities.DEBRIS_FIELD_SHARED, debris.getEntity().getFaction().getId());
				debris.getEntity().getContainingLocation().removeEntity(target);
				
				//target.getDropRandom()
				target.getLocation().set(debris.getEntity().getLocation());
				
				MemoryAPI debrisMemory = debris.getEntity().getMemory();
				debrisMemory.set(MemFlags.SALVAGE_DEBRIS_FIELD, debris, 0);
				target.setMemory(debrisMemory);
				
//				if (debrisMemory.contains(MemFlags.SALVAGE_SEED)) {
//					target.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, debrisMemory.getLong(MemFlags.SALVAGE_SEED));
//				}
//				
//				if (debrisMemory.contains(MemFlags.SALVAGE_SPECIAL_DATA)) {
//					target.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPECIAL_DATA, debrisMemory.get(MemFlags.SALVAGE_SPECIAL_DATA));
//				}
				
				// the debris field spec is shared between all debris fields and all the relevant values in it
				// are set here, before it's actually used for any given instance of a debris field
				SalvageEntityGenDataSpec spec = SalvageEntityGeneratorOld.getSalvageSpec(target.getCustomEntityType());
				
				spec.getDropValue().clear();
				spec.getDropRandom().clear();
				
				spec.getDropValue().addAll(debris.getEntity().getDropValue());
				spec.getDropRandom().addAll(debris.getEntity().getDropRandom());
				
				spec.setProbDefenders(debris.getParams().defenderProb * debris.getParams().density);
				spec.setMinStr(debris.getParams().minStr * debris.getParams().density);
				spec.setMaxStr(debris.getParams().maxStr * debris.getParams().density);
				spec.setMaxDefenderSize(debris.getParams().maxDefenderSize);
				spec.setDefFaction(debris.getParams().defFaction);
				
				float xp = debris.getParams().baseSalvageXP * debris.getParams().density;
				spec.setXpSalvage(xp);
				
//				DropData data = new DropData();
//				data.group = Drops.AI_CORES3;
//				data.value = 10000000;
//				spec.getDropValue().add(data);
				
				Global.getSector().getCampaignUI().showInteractionDialog(
												new RuleBasedInteractionDialogPluginImpl(),
												target);
			}
		}
//		if (entity.isInCurrentLocation()) {
//			VisibilityLevel level = entity.getVisibilityLevelToPlayerFleet();
//			if (level != VisibilityLevel.NONE) {
//				Global.getSector().addPing(entity, Pings.SENSOR_BURST);
//			}
//		}
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
//		if (level > 0) {
//			
//		}
		
		if (interval != null && getFleet() != null && getFleet().isPlayerFleet()) {
			float days = Global.getSector().getClock().convertToDays(amount);
			interval.advance(days);
			if (interval.intervalElapsed()) {
//				WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>();
//				
//				picker.add(Commodities.CREW, 1);
//				picker.add(Commodities.CREW, 1);
				
//				Color color = Misc.setAlpha(entity.getIndicatorColor(), 255);
//				
//				CargoAPI cargo = getFleet().getCargo();
//				float r = (float) Math.random();
//				if (r > 0.9f) {
//					cargo.addCommodity(Commodities.BETA_CORE, 1);
//					entity.addFloatingText("Found beta core", color, 0.5f);
//					Global.getSoundPlayer().playUISound("ui_cargo_metals_drop", 1f, 1f);
//				} else if (r > 0.5f) {
//					int qty = (int) (5f + 5f * (float) Math.random());
//					cargo.addCommodity(Commodities.METALS, qty);
//					entity.addFloatingText("Found " + qty + " metal", color, 0.5f);
//					Global.getSoundPlayer().playUISound("ui_cargo_metals_drop", 1f, 1f);
//				} else if (r > 0.25f) {
//					int qty = (int) (1f + 3f * (float) Math.random());
//					cargo.removeCommodity(Commodities.CREW, qty);
//					entity.addFloatingText("Lost " + qty + " crew", color, 0.5f);
//					Global.getSoundPlayer().playUISound("ui_cargo_crew_drop", 1f, 1f);
//				} else if (r >= 0) {
//					int qty = (int) (1f + 2f * (float) Math.random());
//					cargo.removeCommodity(Commodities.HEAVY_MACHINERY, qty);
//					entity.addFloatingText("Lost " + qty + " heavy machinery", color, 0.5f);
//					Global.getSoundPlayer().playUISound("ui_cargo_machinery_drop", 1f, 1f);
//				}
				
			}
		}
		
	}

	@Override
	protected void applyEffect(float amount, float level) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
//		if (interval == null && level > 0) {
//			interval = new IntervalUtil(0.1f, 0.15f);
//		} else if (level <= 0) {
//			if (interval != null) {
//				entity.addFloatingText("Finished", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
//			}
//			interval = null;
//		}
		
//		fleet.getStats().getSensorRangeMod().modifyFlat(getModId(), SENSOR_RANGE_BONUS * level, "Active sensor burst");
//		fleet.getStats().getDetectedRangeMod().modifyFlat(getModId(), DETECTABILITY_RANGE_BONUS * level, "Active sensor burst");
//		fleet.getStats().getFleetwideMaxBurnMod().modifyMult(getModId(), 0, "Active sensor burst");
//		fleet.getStats().getAccelerationMult().modifyMult(getModId(), 1f + (ACCELERATION_MULT - 1f) * level);
	}

	@Override
	protected void deactivateImpl() {
		cleanupImpl();
	}
	
	@Override
	protected void cleanupImpl() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
//		fleet.getStats().getSensorRangeMod().unmodify(getModId());
//		fleet.getStats().getDetectedRangeMod().unmodify(getModId());
//		fleet.getStats().getFleetwideMaxBurnMod().unmodify(getModId());
//		fleet.getStats().getAccelerationMult().unmodify(getModId());
	}
	
	@Override
	public boolean isUsable() {
		DebrisFieldTerrainPlugin debris = getDebrisField();
		if (debris == null || debris.isScavenged()) return false;
		return super.isUsable();
	}
	
	protected DebrisFieldTerrainPlugin getDebrisField() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return null;
		
		DebrisFieldTerrainPlugin scavenged = null; 
		DebrisFieldTerrainPlugin usable = null; 
		for (CampaignTerrainAPI curr : fleet.getContainingLocation().getTerrainCopy()) {
			if (curr.getPlugin() instanceof DebrisFieldTerrainPlugin) {
				DebrisFieldTerrainPlugin debris = (DebrisFieldTerrainPlugin) curr.getPlugin();
				if (debris.containsEntity(fleet)) {
					if (debris.isScavenged()) {
						scavenged = debris;
					} else {
						usable = debris;
					}
					//return debris;
				}
			}
		}
		if (usable != null) return usable;
		return scavenged;
	}

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		
		tooltip.addTitle(spec.getName());

		float pad = 10f;
		tooltip.addPara("Pick through a debris field looking for anything of value.", pad);
		
		DebrisFieldTerrainPlugin debris = getDebrisField();
		if (debris == null) {
			tooltip.addPara("Your fleet is not currently inside a debris field.", Misc.getNegativeHighlightColor(), pad);
		} else if (debris.isScavenged()) {
			tooltip.addPara("Your fleet is inside a debris field, but it contains nothing of value.", Misc.getNegativeHighlightColor(), pad);
		} else {
			tooltip.addPara("Your fleet is inside a debris field and can begin scavenging.", Misc.getPositiveHighlightColor(), pad);
		}
//		tooltip.addPara("Increases sensor range by %s* units and" +
//				" increases the range at which the fleet can be detected by %s* units." +
//				" Also brings the fleet to a near-stop.",
//				pad, highlight,
//				"" + (int)SENSOR_RANGE_BONUS,
//				"" + (int)DETECTABILITY_RANGE_BONUS
//		);
		//tooltip.addPara("Disables \"Go Dark\" when activated.", pad);
		addIncompatibleToTooltip(tooltip, expanded);
		
//		tooltip.addPara("*2000 units = 1 map grid cell", gray, pad);
	}

	public boolean hasTooltip() {
		return true;
	}
}





