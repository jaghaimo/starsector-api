package com.fs.starfarer.api.impl.campaign.skills;

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

public class FieldRepairsScript implements EveryFrameScript {

	public static int MONTHS_PER_DMOD_REMOVAL = 1;
	
	protected IntervalUtil tracker = new IntervalUtil(10f, 20f);
	
	protected FleetMemberAPI picked = null;
	protected String dmod = null;
	
	public void advance(float amount) {
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		if (fleet == null) return;
		
		if (Global.getSector().getPlayerStats().getSkillLevel(Skills.FIELD_REPAIRS) <= 0) {
			picked = null;
			dmod = null;
			return;
		}
		
		float days = Global.getSector().getClock().convertToDays(amount);
		float rateMult = 1f / (float) MONTHS_PER_DMOD_REMOVAL;
		//days *= 100f;
		tracker.advance(days * rateMult * 0.5f); // * 0.5f since the tracker interval averages 15 days
		if (tracker.intervalElapsed()) {
			// pick which ship to remove which d-mod from half a month ahead of time
			// if it's no longer present in the fleet when it's time to remove the d-mod,
			// don't remove a d-mod at all
			if (picked == null || dmod == null) {
				pickNext();
			} else {
				if (fleet.getFleetData().getMembersListCopy().contains(picked)) {
					DModManager.removeDMod(picked.getVariant(), dmod);
					
					HullModSpecAPI spec = DModManager.getMod(dmod);
					MessageIntel intel = new MessageIntel(picked.getShipName() + " - repaired " + spec.getDisplayName(), Misc.getBasePlayerColor());
					intel.setIcon(Global.getSettings().getSpriteName("intel", "repairs_finished"));
					Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.REFIT_TAB, picked);
					
					int dmods = DModManager.getNumDMods(picked.getVariant());
					if (dmods <= 0) {
						restoreToNonDHull(picked.getVariant());
					}
				}
				picked = null;
				pickNext();
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

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}

	
	public static void restoreToNonDHull(ShipVariantAPI v) {
		ShipHullSpecAPI base = v.getHullSpec().getDParentHull();
		
		// so that a skin with dmods can be "restored" - i.e. just dmods suppressed w/o changing to
		// actual base skin
		//if (!v.getHullSpec().isDHull()) base = v.getHullSpec();
		if (!v.getHullSpec().isDefaultDHull() && !v.getHullSpec().isRestoreToBase()) base = v.getHullSpec();
		
		if (base == null && v.getHullSpec().isRestoreToBase()) {
			base = v.getHullSpec().getBaseHull();
		}
		
		if (base != null) {
			//v.clearPermaMods();
			v.setHullSpecAPI(base);
		}
	}
}


