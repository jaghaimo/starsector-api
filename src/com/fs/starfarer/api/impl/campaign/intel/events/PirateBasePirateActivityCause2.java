package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.enc.SlipstreamPirateEPEC;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.HA_CMD;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.MapParams;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Range;

public class PirateBasePirateActivityCause2 extends BaseHostileActivityCause2 {

	public static float MAX_MAG = 0.5f;
	
	
	public static List<MarketAPI> getColoniesAffectedBy(PirateBaseIntel base) {
		List<MarketAPI> result = new ArrayList<MarketAPI>();
		for (StarSystemAPI system : getSystemsAffectedBy(base)) {
			result.addAll(Misc.getMarketsInLocation(system, Factions.PLAYER));
		}
		return result;
	}
	
	public static List<StarSystemAPI> getSystemsAffectedBy(PirateBaseIntel base) {
		List<StarSystemAPI> result = new ArrayList<StarSystemAPI>();
//		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(HostileActivityIntel.class)) {
//			HostileActivityIntel curr = (HostileActivityIntel) intel;
		for (StarSystemAPI system : Misc.getSystemsWithPlayerColonies(false)) {
			if (getBaseIntel(system) == base) {
				result.add(system);
			}
		}
		return result;
	}
	

	public static PirateBaseIntel getBaseIntel(StarSystemAPI system) {
		if (system == null) return null;
		PirateBaseIntel base = SlipstreamPirateEPEC.getClosestPirateBase(system.getLocation());
		return base; 
	}
	
	transient boolean ignoreDeal = false;
	
	public PirateBasePirateActivityCause2(HostileActivityEventIntel intel) {
		super(intel);
	}
	
	@Override
	public void addExtraRows(TooltipMakerAPI info, BaseEventIntel intel) {
		Set<PirateBaseIntel> seen = new LinkedHashSet<PirateBaseIntel>();
		for (final StarSystemAPI system : Misc.getSystemsWithPlayerColonies(false)) {
			final PirateBaseIntel base = getBaseIntel(system);
			if (base == null || seen.contains(base)) continue;
			
			
			int numColonies = 0;
			final List<String> affected = new ArrayList<String>();
			for (StarSystemAPI curr : getSystemsAffectedBy(base)) {
				affected.add(curr.getNameWithNoType());
				numColonies += Misc.getMarketsInLocation(curr, Factions.PLAYER).size();
			}
			if (affected.isEmpty()) continue;
			
			seen.add(base);
			
			
			final String colonies = numColonies != 1 ? "colonies" : "colony";
			final String isOrAre = numColonies != 1 ? "are" : "is";
			
			String desc = "Hidden pirate base near your " + colonies;
			if (base.isPlayerVisible()) {
				desc = "Pirate base in the " + base.getSystem().getNameWithLowercaseTypeShort() + "";
			}
			ignoreDeal = true;
			final int progress = getProgressForBase(base);
			ignoreDeal = false;
			String progressStr = "+" + progress;
			if (progress < 0) progressStr = "" + progress;
			Color descColor = getDescColor(intel);
			Color progressColor = getProgressColor(intel);
			
			if (base.playerHasDealWithBaseCommander()) {
				progressStr = EventFactor.NEGATED_FACTOR_PROGRESS;
				descColor = Misc.getGrayColor();
				progressColor = Misc.getPositiveHighlightColor();
			}
			
			info.addRowWithGlow(Alignment.LMID, descColor, "    " + desc,
							    Alignment.RMID, progressColor, progressStr);
			
			TooltipCreator t = new BaseFactorTooltip() {
				@Override
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					float opad = 10f;

					String aStr = Misc.getAndJoined(affected);
					String systems = "systems";
					if (affected.size() == 1) systems = "system";

					MapParams params = new MapParams();
					for (StarSystemAPI curr : getSystemsAffectedBy(base)) {
						params.showSystem(curr);
					}
					if (base.playerHasDealWithBaseCommander() || base.isPlayerVisible()) {
						params.showMarket(base.getMarket(), 1f);
//						params.arrows = new ArrayList<ArrowData>();
//						for (StarSystemAPI curr : getSystemsAffectedBy(base)) {
//							if (curr != base.getEntity().getContainingLocation()) {
//								ArrowData arr = new ArrowData(base.getEntity(), curr.getHyperspaceAnchor());
//								arr.color = Global.getSector().getFaction(Factions.PIRATES).getBrightUIColor();
//								params.arrows.add(arr);
//							}
//						}
					}
					float w = tooltip.getWidthSoFar();
					float h = Math.round(w / 1.6f);
					params.positionToShowAllMarkersAndSystems(true, Math.min(w, h));
					
					//UIPanelAPI map = tooltip.createSectorMap(w, h, params, aStr + " " + Misc.ucFirst(systems));
					UIPanelAPI map = tooltip.createSectorMap(w, h, params, aStr + " " + systems);
					
					if (base.playerHasDealWithBaseCommander()) {
						String systemStr = "in the " + base.getSystem().getNameWithLowercaseTypeShort() + "";
						tooltip.addPara("Your " + colonies + " in the " + aStr + " " + systems + 
								" " + isOrAre + " within range of a pirate base located " + systemStr + ". " +  
								"You have an agreement with "
								+ "the base commander, and fleets from this base do not, as a rule, "
								+ "harass your colonies or shipping.", 0f,
								Misc.getPositiveHighlightColor(), "agreement");
						
						int payment = HA_CMD.computePirateProtectionPaymentPerMonth(base);
						tooltip.addPara("Assuming current colony income levels, this agreement costs "
								+ "you %s per month. If it was not in effect, "
								+ "this base would contribute %s points of event progress per month.", opad,
								Misc.getHighlightColor(),
								Misc.getDGSCredits(payment), "" + progress);
						
					} else {
						String systemStr = "in a nearby system";
						if (base.isPlayerVisible()) {
							systemStr = "in the " + base.getSystem().getNameWithLowercaseTypeShort() + "";
						}
						tooltip.addPara("Your " + colonies + " in the " + aStr + " " + systems + 
								" " + isOrAre + " within range of a pirate base located " + systemStr + ". " +  
								"This results in a greater volume of pirate "
								+ "fleets preying on trade. %s should address this.", 0f,
								Misc.getHighlightColor(), "Dealing with the base");
					}
					
					tooltip.addCustom(map, opad);
				}
			};
			info.addTooltipToAddedRow(t, TooltipLocation.RIGHT, false);
		}
	}

	public boolean playerHasDealWithAnyBases() {
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(PirateBaseIntel.class)) {
			PirateBaseIntel curr = (PirateBaseIntel) intel;
			if (curr.playerHasDealWithBaseCommander()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean shouldShow() {
		return getProgress() != 0 || playerHasDealWithAnyBases();
	}

	public int getProgress() {
		int total = 0;
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(PirateBaseIntel.class)) {
			PirateBaseIntel curr = (PirateBaseIntel) intel;
			total += getProgressForBase(curr);
		}
		return total;
	}
	

	protected int getProgressForBase(PirateBaseIntel base) {
		if (!ignoreDeal && base.playerHasDealWithBaseCommander()) {
			return 0;
		}
		int total = 0;
		for (StarSystemAPI system : getSystemsAffectedBy(base)) {
			total += getProgressForSystem(system);
		}
		return total;
	}
	
	protected int getProgressForSystem(StarSystemAPI system) {
		float mag = getMagnitudeContribution(system);
		if (mag <= 0) return 0;
		mag /= MAX_MAG;
		if (mag > 1f) mag = 1f;
		Range r = new Range("pirateBaseProximityPoints");
		return r.interpInt(mag);
//		int progress = 3 + (int) Math.round(mag * 7f);
//		return progress;
	}
	
	
	public String getDesc() {
		return null;
	}
//	protected float getMaxMag() {
//		float max = 0f;
//		for (final StarSystemAPI system : Misc.getSystemsWithPlayerColonies(false)) {
//			float mag = getMagnitudeContribution(system);
//			if (mag > max) {
//				max = mag;
//			}
//		}
//		return max;
//	}
	

	public float getMagnitudeContribution(StarSystemAPI system) {
		List<MarketAPI> markets = Misc.getMarketsInLocation(system, Factions.PLAYER);
		float maxSize = 0f;
		for (MarketAPI market : markets) {
			maxSize = Math.max(maxSize, market.getSize());
		}
		
		PirateBaseIntel base = getBaseIntel(system);
		float mag = SlipstreamPirateEPEC.getPirateBaseProximityFactor(base, system.getLocation());
		//mag = 0.95f;
		mag *= 0.5f;
		mag *= maxSize / 6f;
		if (base != null) {
			mag *= (float)(base.getTier().ordinal() + 1f) / 5f;
		}
		if (mag > MAX_MAG) mag = MAX_MAG;
		
		mag = Math.round(mag * 100f) / 100f;
		
		if (base != null && base.playerHasDealWithBaseCommander() && !ignoreDeal) {
			mag = 0f;
		}
		
		return mag;
	}
	

}


