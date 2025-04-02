package com.fs.starfarer.api.impl.codex;

import java.util.LinkedHashSet;
import java.util.List;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.listeners.CargoScreenListener;
import com.fs.starfarer.api.campaign.listeners.CodexEventListener;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI.SkillLevelAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.SharedUnlockData;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * For the codex entries that require a listener of some type to unlock.
 * 
 * @author Alex
 *
 */
public class CodexUnlocker implements FleetEventListener, CodexEventListener, CargoScreenListener {

	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
	}

	@Override
	public void reportBattleOccurred(CampaignFleetAPI nullHere, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		if (battle == null || !battle.isPlayerInvolved()) return;
		
		LinkedHashSet<String> hulls = new LinkedHashSet<String>(); 
		
		for (CampaignFleetAPI fleet : battle.getNonPlayerSideSnapshot()) {
			List<FleetMemberAPI> members = Misc.getSnapshotMembersLost(fleet);
			// unlock everything, not just the destroyed ships
			//if (primaryWinner != null && primaryWinner.isPlayerFleet()) {
				for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
					if (!members.contains(member)) {
						members.add(member);
					}
				}
			//}
			for (FleetMemberAPI member : members) {
				if (member.getHullSpec().hasTag(Tags.CODEX_UNLOCKABLE)) {
					String hullId = CodexDataV2.getFleetMemberBaseHullId(member);
					if (SharedUnlockData.get().isPlayerAwareOfShip(hullId)) continue;
					hulls.add(hullId);
				}
			}
		}
		
		if (!hulls.isEmpty()) {
			for (String hullId : hulls) {
				SharedUnlockData.get().reportPlayerAwareOfShip(hullId, false);
			}
			SharedUnlockData.get().saveIfNeeded();
		}
	}

	@Override
	public void reportCargoScreenOpened() {
		unlockStuff();
	}
	
	@Override
	public void reportAboutToOpenCodex() {
		unlockStuff();
	}
	
	public void unlockStuff() {
		if (Global.getCurrentState() != GameState.CAMPAIGN) return;
		
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		if (fleet == null || fleet.getFleetData() == null) return;
		CargoAPI cargo = fleet.getCargo();
		if (cargo == null) return;
		
		boolean save = false; 
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			FighterWingSpecAPI wing = stack.getFighterWingSpecIfWing();
			if (wing != null && wing.hasTag(Tags.CODEX_UNLOCKABLE)) {
				save |= SharedUnlockData.get().reportPlayerAwareOfFighter(wing.getId(), false);
			}
			
			WeaponSpecAPI weapon = stack.getWeaponSpecIfWeapon();
			if (weapon != null && weapon.hasTag(Tags.CODEX_UNLOCKABLE)) {
				save |= SharedUnlockData.get().reportPlayerAwareOfWeapon(weapon.getWeaponId(), false);
			}
			
			HullModSpecAPI hullmod = stack.getHullModSpecIfHullMod();
			if (hullmod != null && hullmod.hasTag(Tags.CODEX_UNLOCKABLE)) {
				save |= SharedUnlockData.get().reportPlayerAwareOfHullmod(hullmod.getId(), false);
			}
			
			SpecialItemSpecAPI item = stack.getSpecialItemSpecIfSpecial();
			if (item != null && item.hasTag(Tags.CODEX_UNLOCKABLE)) {
				save |= SharedUnlockData.get().reportPlayerAwareOfSpecialItem(item.getId(), false);
			}
			
			CommoditySpecAPI commodity = stack.getResourceIfResource();
			if (commodity != null && commodity.hasTag(Tags.CODEX_UNLOCKABLE)) {
				save |= SharedUnlockData.get().reportPlayerAwareOfCommodity(commodity.getId(), false);
			}
		}
		
		
		for (MarketAPI market : Misc.getPlayerMarkets(true)) {
			for (Industry ind : market.getIndustries()) {
				if (ind.getSpec().hasTag(Tags.CODEX_UNLOCKABLE)) {
					save |= SharedUnlockData.get().reportPlayerAwareOfIndustry(ind.getSpec().getId(), false);
				}
			}
		}
		
		MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
		if (stats != null) {
			for (SkillLevelAPI sl : stats.getSkillsCopy()) {
				if (sl.getLevel() > 0 && sl.getSkill().hasTag(Tags.CODEX_UNLOCKABLE)) {
					save |= SharedUnlockData.get().reportPlayerAwareOfSkill(sl.getSkill().getId(), false);
				}	
			}
		}
		
		for (AbilityPlugin ability : fleet.getAbilities().values()) {
			if (ability.getSpec().hasTag(Tags.CODEX_UNLOCKABLE)) {
				save |= SharedUnlockData.get().reportPlayerAwareOfAbility(ability.getSpec().getId(), false);
			}
		}
		
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.getHullSpec().hasTag(Tags.CODEX_UNLOCKABLE)) {
				String hullId = CodexDataV2.getFleetMemberBaseHullId(member);
				save |= SharedUnlockData.get().reportPlayerAwareOfShip(hullId, true);
			}
		}
		
		if (save) SharedUnlockData.get().saveIfNeeded();
	}
	
	
	
	@Override
	public void reportClosedCodex() {
		
	}

	
	public static void makeAwareOfConditionsOn(MarketAPI market) {
		if (market == null) return;
		
		boolean save = false;
		for (MarketConditionAPI mc : market.getConditions()) {
			if (mc.getSpec().hasTag(Tags.CODEX_UNLOCKABLE)) {
				save |= SharedUnlockData.get().reportPlayerAwareOfCondition(mc.getSpec().getId(), false);
			}
		}
		if (save) SharedUnlockData.get().saveIfNeeded();
	}


	@Override
	public void reportPlayerLeftCargoPods(SectorEntityToken entity) {}
	@Override
	public void reportPlayerNonMarketTransaction(PlayerMarketTransaction transaction, InteractionDialogAPI dialog) {}
	@Override
	public void reportSubmarketOpened(SubmarketAPI submarket) {}
	
	
	
	

}
