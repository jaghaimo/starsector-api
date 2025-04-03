package com.fs.starfarer.api.combat;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.HullModItemManager;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BaseHullMod implements HullModEffect {

	protected HullModSpecAPI spec;
	
	public void init(HullModSpecAPI spec) {
		this.spec = spec;
		
	}
	
	public boolean isSMod(MutableShipStatsAPI stats) {
		if (stats == null || stats.getVariant() == null || spec == null) return false;
		return stats.getVariant().getSMods().contains(spec.getId()) ||
				stats.getVariant().getSModdedBuiltIns().contains(spec.getId());
	}
	public boolean isSMod(ShipAPI ship) {
		if (ship == null || ship.getVariant() == null || spec == null) return false;
		return ship.getVariant().getSMods().contains(spec.getId()) ||
				ship.getVariant().getSModdedBuiltIns().contains(spec.getId());
	}
	public boolean isBuiltIn(ShipAPI ship) {
		if (ship == null || ship.getVariant() == null || spec == null) return false;
		return ship.getHullSpec().getBuiltInMods().contains(spec.getId());
	}
	
	
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
	}

	public void applyEffectsBeforeShipCreation(HullSize hullSize,
											   MutableShipStatsAPI stats, String id) {
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		return getDescriptionParam(index, hullSize);
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		return getSModDescriptionParam(index, hullSize);
	}
	

	public boolean isApplicableToShip(ShipAPI ship) {
		return true;
	}

	
	public void advanceInCampaign(FleetMemberAPI member, float amount) {
		
	}
	
	public void advanceInCombat(ShipAPI ship, float amount) {
		
	}

	public String getUnapplicableReason(ShipAPI ship) {
		return null;
	}

	public boolean affectsOPCosts() {
		return false;
	}

	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (spec == null) return true;
		
		boolean reqSpaceport = spec.hasTag(HullMods.TAG_REQ_SPACEPORT);
		if (!reqSpaceport) return true;
		
		if (marketOrNull == null) return false;
		if (mode == CoreUITradeMode.NONE || mode == null) return false;
		
		for (Industry ind : marketOrNull.getIndustries()) {
			if (ind.getSpec().hasTag(Industries.TAG_STATION)) return true;
			if (ind.getSpec().hasTag(Industries.TAG_SPACEPORT)) return true;
		}
		
		return false;
	}

	public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (spec == null) return null;
		
		boolean reqSpaceport = spec.hasTag(HullMods.TAG_REQ_SPACEPORT);
		if (!reqSpaceport) return null;
		
		boolean has = ship.getVariant().hasHullMod(spec.getId());
		
		String verb = "installed";
		
		// I think "or modified" was when you couldn't build in logistics mods while not at dock
		//if (has) verb = "removed or modified";
		if (has) verb = "removed";
		
		return "Can only be " + verb + " at a colony with a spaceport or an orbital station";
	}
	

	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}
	
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		
	}
	
	public boolean hasSModEffectSection(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		if (!hasSModEffect()) return false;
		// hope commenting this out doesn't make it crash
		//GameState state = Global.getCurrentState();
		//if (state == GameState.TITLE && !Global.getSettings().isDevMode()) return false;
		if (Misc.CAN_SMOD_BUILT_IN) {
			return !isBuiltIn(ship) || !isSModEffectAPenalty();
		}
		return !isBuiltIn(ship);
	}
	
	public boolean isSModEffectAPenalty() {
		return false;
	}
	
	public boolean hasSModEffect() {
		return spec != null && 
				spec.getSModEffectFormat() != null &&
				!spec.getSModEffectFormat().trim().isEmpty() &&
				!spec.getSModEffectFormat().startsWith("#");
	}
	
	public void addSModEffectSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec, boolean isForBuildInList) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		final String [] params = new String [] { 
				getSModDescriptionParam(0, hullSize, null),
				getSModDescriptionParam(1, hullSize, null),
				getSModDescriptionParam(2, hullSize, null),
				getSModDescriptionParam(3, hullSize, null),
				getSModDescriptionParam(4, hullSize, null),
				getSModDescriptionParam(5, hullSize, null),
				getSModDescriptionParam(6, hullSize, null),
				getSModDescriptionParam(7, hullSize, null),
				getSModDescriptionParam(8, hullSize, null),
				getSModDescriptionParam(9, hullSize, null)
			};
		tooltip.addPara(spec.getSModDescription(hullSize).replaceAll("\\%", "%%"), opad, h, params);		
	}
	
	
	public void addSModSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec, boolean isForBuildInList) {
		float opad = 10f;
		boolean sMod = isSMod(ship);
		Color s = Misc.getStoryOptionColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color darkBad = Misc.setAlpha(Misc.scaleColorOnly(bad, 0.4f), 175);
		if (!sMod || hasSModEffect()) {
//			if (isForModSpec) {
//				tooltip.addSpacer(opad);
//			}
			if (!isSModEffectAPenalty()) {
//				Color c = Misc.getStoryOptionColor();
//				if (!sMod) c = Misc.getStoryDarkColor()
				if (isForBuildInList) {
					tooltip.addSectionHeading(spec.getDisplayName() + " bonus", Misc.getStoryOptionColor(), Misc.getStoryDarkColor(), Alignment.MID, opad);
				} else {
					tooltip.addSectionHeading("S-mod bonus", Misc.getStoryOptionColor(), Misc.getStoryDarkColor(), Alignment.MID, opad);
				}
			} else {
				if (isForBuildInList) {
					tooltip.addSectionHeading(spec.getDisplayName() + " penalty", bad, darkBad, Alignment.MID, opad);
				} else {
					tooltip.addSectionHeading("S-mod penalty", bad, darkBad, Alignment.MID, opad);
				}
			}
		}
		
		if (hasSModEffect()) {
			if (isForBuildInList) {
				tooltip.addSpacer(-5f);
				//tooltip.setHeightSoFar(tooltip.getHeightSoFar() - 5f);
			}
			addSModEffectSection(tooltip, hullSize, ship, width, isForModSpec, false);
			if (!sMod && !isForBuildInList) {
				
				boolean builtIn = isBuiltIn(ship);
				if (builtIn) {
//					tooltip.addPara("This effect only applies if this built-in hullmod is enhanced using a %s. "
//							+ "Doing this graints %s bonus experience - more than building in a regular hullmod.",
//							opad, s, "story point", "100%");
//					tooltip.addPara("This effect only applies if this hullmod is enhanced using a %s. "
//							+ "Since it's is already built in, this graints "
//							+ "%s bonus experience - more than building in a regular hullmod.",
//							opad, s, "story point", "100%");
					tooltip.addPara("This effect only applies if this built-in hullmod is enhanced using a %s. Doing this does not count against the maximum number of s-mods a ship can have.",
							opad, s, "story point");
				} else {
					String cheap = "Cheap hullmods have stronger effects."; 
					if (Global.CODEX_TOOLTIP_MODE) {
						cheap = "Cheaper hullmods have stronger effects, more expensive hullmods may have penalties.";
					}
					tooltip.addPara("This effect only applies if this hullmod is built into the hull using a story point. " + cheap,
							opad, s, "story point");
				}
//				tooltip.addPara("This hullmod has the following effect when built into the hull using a story point:",
//						opad, s, "story point");
			} else {
//				tooltip.addPara("This hullmod has the following effect from being built into the hull using a story point:",
//						opad, s, "story point");
			}
		} else { // no section in this case, but leaving this in case that changes
			if (!sMod) {
				tooltip.addPara("Aside from removing its ordnance point cost, "
						+ "this hullmod gains no extra effect from being built into the hull using "
						+ "a story point.", opad, s, "story point");
			}
		}
		
		
		if (!sMod || hasSModEffect()) {
			if (isForModSpec) {
				tooltip.addSpacer(opad);
			}
		}
		
//		tooltip.addPara("Some hullmods have an additional effect"
//		+ " when they're built into the hull using a story point. "
//		+ "Hullmods that cost fewer ordnance points have a bonus, while "
//		+ "more expensive hullmods usually have a penalty.", opad, s, "story point");
//
//tooltip.addPara("Hullmods that are built into the base hull do not have this effect, and neither do "
//		+ "hullmods with a midrange cost.", opad);
		
	}
	
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		
	}
	
	
	public boolean shipHasOtherModInCategory(ShipAPI ship, String currMod, String category) {
		for (String id : ship.getVariant().getHullMods()) {
			HullModSpecAPI mod = Global.getSettings().getHullModSpec(id);
			if (!mod.hasTag(category)) continue;
			if (id.equals(currMod)) continue;
			return true;
		}
		return false;
	}
	

	public boolean isInPlayerFleet(MutableShipStatsAPI stats) {
		if (stats == null) return false;
		FleetMemberAPI member = stats.getFleetMember();
		if (member == null) return false;
		PersonAPI fc = member.getFleetCommanderForStats();
		if (fc == null) fc = member.getFleetCommander();
		if (fc == null) return false;
		return fc.isPlayer();
	}
	
	public boolean isInPlayerFleet(ShipAPI ship) {
		if (ship == null) return false;
		FleetMemberAPI member = ship.getFleetMember();
		if (member == null) return false;
		PersonAPI fc = member.getFleetCommanderForStats();
		if (fc == null) fc = member.getFleetCommander();
		if (fc == null) return false;
		return fc.isPlayer();
	}

	public Color getBorderColor() {
		return null;
		//return Color.red;
	}

	public Color getNameColor() {
		return null;
		//return Color.red;
	}

	public int getDisplaySortOrder() {
//		if (spec.getId().equals("hiressensors")) {
//			return 200;
//		}
		return 100;
	}

	public int getDisplayCategoryIndex() {
//		if (spec.getId().equals("hiressensors")) {
//			return 4;
//		}
		return -1;
	}

	public float getTooltipWidth() {
		return 369f;
	}
	
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		return true;
	}
	
	public void addRequiredItemSection(TooltipMakerAPI tooltip, 
								FleetMemberAPI member, ShipVariantAPI currentVariant, MarketAPI dockedAt,
								float width, boolean isForModSpec) {
		
		CargoStackAPI req = getRequiredItem();
		if (req != null) {
			float opad = 10f;
			if (isForModSpec || Global.CODEX_TOOLTIP_MODE) {
				Color color = Misc.getBasePlayerColor();
				if (isForModSpec) {
					color = Misc.getHighlightColor();
				}
				String name = req.getDisplayName();
				String aOrAn = Misc.getAOrAnFor(name);
				tooltip.addPara("Requires " + aOrAn + " %s to install.", 
									opad, color, name);
			} else if (currentVariant != null && member != null) {
				if (currentVariant.hasHullMod(spec.getId())) {
					if (!currentVariant.getHullSpec().getBuiltInMods().contains(spec.getId())) {
						Color color = Misc.getPositiveHighlightColor();
						tooltip.addPara("Using item: " + req.getDisplayName(), 
											color, opad);
					}
				} else {
					int available = HullModItemManager.getInstance().getNumAvailableMinusUnconfirmed(req, 
																member, currentVariant, dockedAt);
					Color color = Misc.getPositiveHighlightColor();
					if (available < 1) color = Misc.getNegativeHighlightColor();
					if (available < 0) available = 0;
					tooltip.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", 
										color, opad);
				}
			}
		}		
		
	}
	
	
}

















