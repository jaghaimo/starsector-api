package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BuffManagerAPI.Buff;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class TowCable implements HullModEffect {
	
	public static final String HULLMOD_ID = "tow_cable";
	
	public void init(HullModSpecAPI spec) {
		
	}
	
	public static class TowCableBuff implements Buff {
		//public boolean wasApplied = false;
		private String buffId;
		private int frames = 0;
		
		public TowCableBuff(String buffId) {
			this.buffId = buffId; 
		}
		
		public boolean isExpired() {
			//return wasApplied;
			return frames >= 2;
		}
		public String getId() {
			return buffId;
		}
		public void apply(FleetMemberAPI member) {
			// this ensures the buff lasts for exactly one frame unless wasApplied is reset (as it is later)
			//wasApplied = true;
			member.getStats().getMaxBurnLevel().modifyFlat(buffId, 1);
		}
		public void advance(float days) {
			frames++;
		}
	};
	
	
	public TowCable() {

	}
	
	public void advanceInCampaign(FleetMemberAPI member, float amount) {
		if (member.getFleetData() == null) return;
		if (member.getFleetData().getFleet() == null) return;
		if (!member.getFleetData().getFleet().isPlayerFleet()) return;
		
		
		if (!member.getVariant().getHullMods().contains(HULLMOD_ID)) {
			cleanUpTowCableBuffBy(member);
			return;
		}
		
		if (!member.canBeDeployedForCombat()) {
			cleanUpTowCableBuffBy(member);
			return;
		}
		
		FleetDataAPI data = member.getFleetData();
		List<FleetMemberAPI> all = data.getMembersListCopy();
		
		int numCables = 0;
		int thisCableIndex = -1;
		for (FleetMemberAPI curr : all) {
			if (!curr.canBeDeployedForCombat()) continue;
			if (curr.getVariant().getHullMods().contains(HULLMOD_ID)) {
				if (curr == member) {
					thisCableIndex = numCables;
				}
				numCables++;
			}
		}
		if (numCables <= 0 || thisCableIndex == -1) {
			cleanUpTowCableBuffBy(member);
			return;
		}
		
		TowCableBuff buff = getTowCableBuffBy(member, true);
		Map<FleetMemberAPI, Integer> cables = new HashMap<FleetMemberAPI, Integer>();
		float towSpeed = member.getStats().getMaxBurnLevel().getModifiedValue();
		FleetMemberAPI thisCableTarget = null;
		
		for (int cableIndex = 0; cableIndex < numCables; cableIndex++) {
			FleetMemberAPI slowest = getSlowest(all, towSpeed, cables);
			if (slowest == null) break;
			//slowest.getStats().getMaxBurnLevel().getModifiedValue()
			Integer bonus = cables.get(slowest);
			if (bonus == null) bonus = new Integer(0);
			bonus++;
			cables.put(slowest, bonus);
			
			if (cableIndex == thisCableIndex) {
				thisCableTarget = slowest;
				Buff existing = slowest.getBuffManager().getBuff(buff.getId());
				if (existing == buff) {
					// make sure it's using the same buff rather than reapplying it,
					// which would trigger a full stat recompute for the entire FLEET every frame
					//buff.wasApplied = false;
					buff.frames = 0;
					//System.out.println("renewed on " + slowest);
				} else {
					//buff.wasApplied = false;
					buff.frames = 0;
					slowest.getBuffManager().addBuff(buff);
					//System.out.println("Num: " + slowest.getBuffManager().getBuffs().size());
					//System.out.println("added to " + slowest);
				}
				break;
			}
		}
		
		for (FleetMemberAPI curr : all) {
			if (curr != thisCableTarget) {
				curr.getBuffManager().removeBuff(buff.getId());
			}
		}
	}
	
	private FleetMemberAPI getSlowest(List<FleetMemberAPI> all, float speedCutoff, Map<FleetMemberAPI, Integer> cables) {
		FleetMemberAPI slowest = null;
		float minLevel = Float.MAX_VALUE;
		for (FleetMemberAPI curr : all) {
			if (!isSuitable(curr)) continue;
			
			float baseBurn = getMaxBurnWithoutCables(curr);
			Integer bonus = cables.get(curr);
			if (bonus == null) bonus = new Integer(0);
			
			if (bonus >= getMaxCablesFor(curr)) continue;
			
			float burnLevel = baseBurn + bonus;
			
			if (burnLevel >= speedCutoff) continue;
		
			if (burnLevel < minLevel) {
				minLevel = burnLevel;
				slowest = curr;
			}
		}
		return slowest;		
	}
	
	private int getMaxCablesFor(FleetMemberAPI member) {
//		switch (member.getHullSpec().getHullSize()) {
//		case CAPITAL_SHIP: return 4;
//		case CRUISER: return 3;
//		case DESTROYER: return 2;
//		case FRIGATE: return 1;
//		}
		return 1;
	}
	
	private float getMaxBurnWithoutCables(FleetMemberAPI member) {
		MutableStat burn = member.getStats().getMaxBurnLevel();
		float val = burn.getModifiedValue();
		float sub = 0;
		for (StatMod mod : burn.getFlatMods().values()) {
			if (mod.getSource().startsWith(HULLMOD_ID)) sub++;
		}
		return Math.max(0, val - sub);
	}
	
	private boolean isSuitable(FleetMemberAPI member) {
		if (member.isFighterWing()) return false;
		return true;
	}
	
	private void cleanUpTowCableBuffBy(FleetMemberAPI member) {
		if (member.getFleetData() == null) return;
		FleetDataAPI data = member.getFleetData();
		TowCableBuff buff = getTowCableBuffBy(member, false);
		if (buff != null) {
			for (FleetMemberAPI curr : data.getMembersListCopy()) {
				curr.getBuffManager().removeBuff(buff.getId());
			}
		}
	}
	
	/**
	 * One instance of the buff object per ship with a Tow Cable.
	 */
	public static final String TOW_CABLE_KEY = "TowCable_PersistentBuffs";
	
	@SuppressWarnings("unchecked")
	private TowCableBuff getTowCableBuffBy(FleetMemberAPI member, boolean createIfMissing) {
		Map<FleetMemberAPI, TowCableBuff> buffs;
		if (Global.getSector().getPersistentData().containsKey(TOW_CABLE_KEY)) {
			buffs = (Map<FleetMemberAPI, TowCableBuff>) Global.getSector().getPersistentData().get(TOW_CABLE_KEY);
		} else {
			buffs = new HashMap<FleetMemberAPI, TowCableBuff>();
			Global.getSector().getPersistentData().put(TOW_CABLE_KEY, buffs);
		}
		
		//new HashMap<FleetMemberAPI, TowCableBuff>();
		TowCableBuff buff = buffs.get(member);
		if (buff == null && createIfMissing) {
			String id = HULLMOD_ID + "_" + member.getId();
			buff = new TowCableBuff(id);
			buffs.put(member, buff);
		}
		return buff;
	}
	

	public void advanceInCombat(ShipAPI ship, float amount) {
	}
	
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
	}
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
	public boolean isApplicableToShip(ShipAPI ship) {
		return true;
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

	public String getUnapplicableReason(ShipAPI ship) {
		return null;
	}

	public boolean affectsOPCosts() {
		return false;
	}

	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		return getDescriptionParam(index, hullSize);
	}

	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		return true;
	}

	public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		return null;
	}

	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		
	}

	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		
	}

	public Color getBorderColor() {
		// TODO Auto-generated method stub
		return null;
	}

	public Color getNameColor() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getDisplaySortOrder() {
		return 100;
	}
	
	public int getDisplayCategoryIndex() {
		return -1;
	}
}
