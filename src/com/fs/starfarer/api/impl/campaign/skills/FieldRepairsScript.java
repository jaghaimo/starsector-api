package com.fs.starfarer.api.impl.campaign.skills;

import java.util.LinkedHashSet;
import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 * Used for the Hull Restoration skill, but keeping the name for save compatibility.
 * 
 * @author Alex
 *
 * Copyright 2021 Fractal Softworks, LLC
 */
public class FieldRepairsScript implements EveryFrameScript {

	public static int MONTHS_PER_DMOD_REMOVAL = 1;
	
	public static float RATE_DP_MAX = 40f;
	public static float RATE_DP_MIN = 4f;
	public static float MAX_RATE_MULT = 3f;
	
	public static boolean REMOVE_DMOD_FROM_NEW_SHIPS = true;
	public static float MIN_NEW_REMOVE_PROB = 0.2f;
	public static float NEW_REMOVE_PROB_PER_DMOD = 0.2f;
	
	protected IntervalUtil tracker = new IntervalUtil(10f, 20f);
	protected IntervalUtil tracker2 = new IntervalUtil(3f, 5f);
	
	protected FleetMemberAPI pickedNew = null;
	protected String dmodNew = null;
	protected Random newRandom = new Random(Misc.genRandomSeed());
	protected LinkedHashSet<String> seen = new LinkedHashSet<String>();
	
	protected FleetMemberAPI picked = null;
	protected String dmod = null;
	
	Object readResolve() {
		if (seen == null) {
			seen =  new LinkedHashSet<String>();
		}
		if (tracker2 == null) {
			tracker2 = new IntervalUtil(3f, 5f);
		}
		if (newRandom == null) {
			newRandom = new Random(Misc.genRandomSeed());
		}
		return this;
	}
	
	public void advance(float amount) {
		
//		System.out.println(RecoverAPlanetkiller.getNexus().getContainingLocation().getName() +
//				"   " + RecoverAPlanetkiller.getNexus().getContainingLocation().getLocation());
		
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		if (fleet == null) return;
		
		if (Global.getSector().getPlayerStats().getSkillLevel(Skills.HULL_RESTORATION) <= 0) {
			picked = null;
			dmod = null;
			return;
		}
		
		float days = Global.getSector().getClock().convertToDays(amount);
		float rateMult = 1f / (float) MONTHS_PER_DMOD_REMOVAL;
		//days *= 100f;
		if (picked != null) {
			float dp = picked.getDeploymentPointsCost();
			float f = (dp - RATE_DP_MIN) / (RATE_DP_MAX - RATE_DP_MIN);
			f = 1 - f;
			if (f > 1f) f = 1f;
			if (f < 0f) f = 0f;
			
			rateMult *= 1f + (MAX_RATE_MULT - 1f) * f;
		}
		tracker.advance(days * rateMult * 0.5f); // * 0.5f since the tracker interval averages 15 days
		if (tracker.intervalElapsed()) {
			// if picked ship is no longer present in the fleet when it's time to remove the d-mod,
			// don't remove a d-mod at all
			if (picked == null || dmod == null) {
				pickNext();
			} else {
				if (fleet.getFleetData().getMembersListCopy().contains(picked) &&
						DModManager.getNumNonBuiltInDMods(picked.getVariant()) > 0) {
					
					DModManager.removeDMod(picked.getVariant(), dmod);
					
					HullModSpecAPI spec = DModManager.getMod(dmod);
					MessageIntel intel = new MessageIntel(picked.getShipName() + " - repaired " + spec.getDisplayName(), Misc.getBasePlayerColor());
					intel.setIcon(Global.getSettings().getSpriteName("intel", "repairs_finished"));
					Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.REFIT_TAB, picked);
					
					int dmods = DModManager.getNumNonBuiltInDMods(picked.getVariant());
					if (dmods <= 0) {
						restoreToNonDHull(picked.getVariant());
					}
				}
				picked = null;
				pickNext();
			}
		}
		
		tracker2.advance(days);
		if (tracker2.intervalElapsed() && REMOVE_DMOD_FROM_NEW_SHIPS) {
			if (pickedNew == null || dmodNew == null) {
				pickNextNew();
			} else {
				seen.add(pickedNew.getId());
				
				float numDmods = DModManager.getNumNonBuiltInDMods(pickedNew.getVariant());
				if (fleet.getFleetData().getMembersListCopy().contains(pickedNew) && numDmods > 0) {
					float probRemove = MIN_NEW_REMOVE_PROB + numDmods * NEW_REMOVE_PROB_PER_DMOD;
					if (newRandom.nextFloat() < probRemove) {
						DModManager.removeDMod(pickedNew.getVariant(), dmodNew);
						
						HullModSpecAPI spec = DModManager.getMod(dmodNew);
						MessageIntel intel = new MessageIntel(pickedNew.getShipName() + " - repaired " + spec.getDisplayName(), Misc.getBasePlayerColor());
						intel.setIcon(Global.getSettings().getSpriteName("intel", "repairs_finished"));
						Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.REFIT_TAB, pickedNew);
						
						int dmods = DModManager.getNumNonBuiltInDMods(pickedNew.getVariant());
						if (dmods <= 0) {
							restoreToNonDHull(pickedNew.getVariant());
						}
					}
				}
				pickedNew = null;
				pickNextNew();
			}
		}
	}
	
	public void pickNext() {
		picked = null;
		dmod = null;
		
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<FleetMemberAPI>();
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.getVariant().isStockVariant()) continue;
			if (member.isMothballed()) continue;
			if (member.getHullSpec().hasTag(Tags.HULL_UNRESTORABLE) ||
					member.getVariant().hasTag(Tags.VARIANT_UNRESTORABLE)) continue;
			int dmods = DModManager.getNumNonBuiltInDMods(member.getVariant());
			if (dmods > 0) {
				picker.add(member, 1);
			}
		}
		picked = picker.pick();
		
		if (picked != null) {
			ShipVariantAPI variant = picked.getVariant();
			WeightedRandomPicker<String> modPicker = new WeightedRandomPicker<String>();
			for (String id : variant.getHullMods()) {
				if (DModManager.getMod(id).hasTag(Tags.HULLMOD_DMOD)) {
					if (variant.getHullSpec().getBuiltInMods().contains(id)) continue;
					modPicker.add(id);
				}
			}
			dmod = modPicker.pick();
			if (dmod == null) {
				picked = null;
			}
		}
	}
	
	public void pickNextNew() {
		pickedNew = null;
		dmodNew = null;
		
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<FleetMemberAPI>();
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.getVariant().isStockVariant() || member.isMothballed() ||
					member.getHullSpec().hasTag(Tags.HULL_UNRESTORABLE) ||
					member.getVariant().hasTag(Tags.VARIANT_UNRESTORABLE)) {
				seen.add(member.getId());
				continue;
			}
			if (seen.contains(member.getId())) continue;
			int dmods = DModManager.getNumNonBuiltInDMods(member.getVariant());
			if (dmods > 0) {
				picker.add(member, 1);
			} else {
				seen.add(member.getId());
			}
		}
		pickedNew = picker.pick();
		
		if (pickedNew != null) {
			ShipVariantAPI variant = pickedNew.getVariant();
			WeightedRandomPicker<String> modPicker = new WeightedRandomPicker<String>();
			for (String id : variant.getHullMods()) {
				if (DModManager.getMod(id).hasTag(Tags.HULLMOD_DMOD)) {
					if (variant.getHullSpec().getBuiltInMods().contains(id)) continue;
					modPicker.add(id);
				}
			}
			dmodNew = modPicker.pick();
			if (dmodNew == null) {
				pickedNew = null;
			}
		}
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}

	
	public static ShipHullSpecAPI getBaseNonDHullFor(ShipVariantAPI v) {
		ShipHullSpecAPI base = v.getHullSpec().getDParentHull();
		
		// so that a skin with dmods can be "restored" - i.e. just dmods suppressed w/o changing to
		// actual base skin
		//if (!v.getHullSpec().isDHull()) base = v.getHullSpec();
		if (!v.getHullSpec().isDefaultDHull() && !v.getHullSpec().isRestoreToBase()) base = v.getHullSpec();
		
		if (base == null && v.getHullSpec().isRestoreToBase()) {
			base = v.getHullSpec().getBaseHull();
		}
		return base;
	}
	
	public static void restoreToNonDHull(ShipVariantAPI v) {
		ShipHullSpecAPI base = getBaseNonDHullFor(v);
		
		if (base != null) {
			//v.clearPermaMods();
			v.setHullSpecAPI(base);
		}
	}
}


