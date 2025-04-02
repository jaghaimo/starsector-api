package com.fs.starfarer.api.impl.campaign;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.RefitScreenListener;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.ListMap;
import com.fs.starfarer.api.util.Misc;

public class HullModItemManager implements RefitScreenListener {

	public static class HullModInstallData {
		public String modId;
		public CargoStackAPI item;
	}
	
	public static class HullModDiff {
		public List<String> needItems = new ArrayList<>();
		public List<String> noLongerNeedItems = new ArrayList<>();
	}
	
	public static HullModItemManager getInstance() {
		String key = "$hullModItemManager";
		HullModItemManager manager = (HullModItemManager) Global.getSector().getMemoryWithoutUpdate().get(key);
		if (manager == null) {
			manager = new HullModItemManager();
			Global.getSector().getMemoryWithoutUpdate().set(key, manager);
		}
		return manager;
	}
	
	protected ListMap<HullModInstallData> map = new ListMap<>();
	
	public HullModItemManager() {
		Global.getSector().getListenerManager().addListener(this);
	}
	
	protected Object readResolve() {
		return this;
	}
	
	public boolean isRequiredItemAvailable(String modId, FleetMemberAPI member, ShipVariantAPI currentVariant, MarketAPI dockedAt) {
		if (Global.getCurrentState() == GameState.TITLE) return true;
		
		if (member == null || currentVariant == null) return true;
		
		HullModSpecAPI mod = Global.getSettings().getHullModSpec(modId);
		CargoStackAPI req = mod.getEffect().getRequiredItem();
		if (req == null) return true;
		
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		if (fleet == null) return true;
		
		String memberId = member.getId();
		List<HullModInstallData> installed = map.getList(memberId);
		List<String> installedIds = new ArrayList<>();
		for (HullModInstallData data : installed) {
			installedIds.add(data.modId);
		}
		
		if (installedIds.contains(modId)) return true;
		
		int available = getNumAvailableMinusUnconfirmed(req, member, currentVariant, dockedAt);
		return available >= 1;
	}
	
	public int getNumAvailableMinusUnconfirmed(CargoStackAPI req, FleetMemberAPI member, ShipVariantAPI currentVariant, MarketAPI dockedAt) {
		int requiredAlreadyByUnconfirmed = getNumUnconfirmed(req, member, currentVariant);
		int available = getNumAvailable(req, dockedAt);
		return available - requiredAlreadyByUnconfirmed;
	}
	public int getNumUnconfirmed(CargoStackAPI req, FleetMemberAPI member, ShipVariantAPI currentVariant) {
		if (member == null || currentVariant == null) return 0;
		
		HullModDiff diff = getHullmodDiff(member, currentVariant);
		int requiredAlreadyByUnconfirmed = 0;
		for (String otherModId : diff.needItems) {
			HullModSpecAPI otherMod = Global.getSettings().getHullModSpec(otherModId);
			CargoStackAPI otherReq = otherMod.getEffect().getRequiredItem();
			if (otherReq == null) continue;
			
			if (req.getType() == otherReq.getType() && req.getData() != null &&
					req.getData().equals(otherReq.getData())) {
				requiredAlreadyByUnconfirmed++;
			}
		}
		for (String otherModId : diff.noLongerNeedItems) {
			HullModSpecAPI otherMod = Global.getSettings().getHullModSpec(otherModId);
			CargoStackAPI otherReq = otherMod.getEffect().getRequiredItem();
			if (otherReq == null) continue;
			
			if (req.getType() == otherReq.getType() && req.getData() != null &&
					req.getData().equals(otherReq.getData())) {
				requiredAlreadyByUnconfirmed--;
			}
		}
		return requiredAlreadyByUnconfirmed;
	}
	
	public int getNumAvailable(CargoStackAPI req, MarketAPI dockedAt) {
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		if (fleet == null) return 0;
		
		CargoAPI cargo = fleet.getCargo();
		
		float quantity = (int) Math.round(cargo.getQuantity(req.getType(), req.getData()));
		if (dockedAt != null) {
			cargo = Misc.getStorageCargo(dockedAt);
			if (cargo != null) {
				quantity += (int) Math.round(cargo.getQuantity(req.getType(), req.getData()));
			}
		}
		return (int) Math.round(quantity);
	}
	
	public CargoAPI getItemsInUseBy(FleetMemberAPI member) {
		CargoAPI cargo = Global.getFactory().createCargo(true);
		
		String memberId = member.getId();
		List<HullModInstallData> installed = map.getList(memberId);
		for (HullModInstallData data : installed) {
			cargo.addItems(data.item.getType(), data.item.getData(), 1f);
		}
		
//		cargo.addSpecial(new SpecialItemData(Items.SYNCHROTRON, null), 1);
//		cargo.addSpecial(new SpecialItemData(Items.CATALYTIC_CORE, null), 1);
//		cargo.addSpecial(new SpecialItemData(Items.BIOFACTORY_EMBRYO, null), 1);
//		cargo.addSpecial(new SpecialItemData(Items.ORBITAL_FUSION_LAMP, null), 1);
//		cargo.addSpecial(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE, null), 1);
//		cargo.addSpecial(new SpecialItemData(Items.DEALMAKER_HOLOSUITE, null), 1);
		
		cargo.sort();
		return cargo;
	}
	
	public void giveBackAllItems(FleetMemberAPI member) {
		giveBackAllItems(member, null);
	}
	
	public void giveBackAllItems(FleetMemberAPI member, CargoAPI cargo) {
		String memberId = member.getId();
		List<HullModInstallData> installed = map.getList(memberId);
		List<String> installedIds = new ArrayList<>();
		for (HullModInstallData data : installed) {
			installedIds.add(data.modId);
		}
		for (String modId : installedIds) {
			giveBackRequiredItems(modId, member, cargo);
		}
		
		map.cleanupEmptyLists();
	}

	@Override
	public void reportFleetMemberVariantSaved(FleetMemberAPI member, MarketAPI dockedAt) {
		HullModDiff diff = getHullmodDiff(member, member.getVariant());
		for (String modId : diff.needItems) {
			takeRequiredItems(modId, member, dockedAt);
		}
		for (String modId : diff.noLongerNeedItems) {
			giveBackRequiredItems(modId, member, null);
		}
		
		map.cleanupEmptyLists();
	}
	
	public HullModDiff getHullmodDiff(FleetMemberAPI member, ShipVariantAPI currentVariant) {
		HullModDiff diff = new HullModDiff();
		
		String memberId = member.getId();
		List<HullModInstallData> installed = map.getList(memberId);
		List<String> installedIds = new ArrayList<>();
		for (HullModInstallData data : installed) {
			installedIds.add(data.modId);
		}
		
		for (String modId : currentVariant.getHullMods()) {
			if (member.getHullSpec().isBuiltInMod(modId)) continue;
			HullModSpecAPI mod = Global.getSettings().getHullModSpec(modId);
			CargoStackAPI req = mod.getEffect().getRequiredItem();
			if (req == null) continue;
			
			if (!installedIds.contains(modId)) {
				diff.needItems.add(modId);
			}
			installedIds.remove(modId);
		}
		
		diff.noLongerNeedItems.addAll(installedIds);
		
		return diff;
	}

	public void takeRequiredItems(String modId, FleetMemberAPI member, MarketAPI dockedAt) {
		HullModSpecAPI mod = Global.getSettings().getHullModSpec(modId);
		CargoStackAPI req = mod.getEffect().getRequiredItem();
		if (req == null) return;
		
		String memberId = member.getId();
		List<HullModInstallData> installed = map.getList(memberId);
		for (HullModInstallData data : installed) {
			if (data.modId.equals(modId)) return;
		}
		
		
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		if (fleet == null) return;
		CargoAPI cargo = fleet.getCargo();
		
		boolean removed = false;
		if (cargo.getQuantity(req.getType(), req.getData()) >= 1f) {
			cargo.removeItems(req.getType(), req.getData(), 1);
			removed = true;
		}
		
		if (!removed && dockedAt != null) {
			cargo = Misc.getStorageCargo(dockedAt);
			if (cargo != null && cargo.getQuantity(req.getType(), req.getData()) >= 1f) {
				cargo.removeItems(req.getType(), req.getData(), 1);
				removed = true;
			}
		}
		
		if (!removed) return;
		
		HullModInstallData data = new HullModInstallData();
		data.modId = modId;
		data.item = req;
		map.add(memberId, data);
	}
	
	public void giveBackRequiredItems(String modId, FleetMemberAPI member, CargoAPI cargo) {
		HullModSpecAPI mod = Global.getSettings().getHullModSpec(modId);
		CargoStackAPI req = mod.getEffect().getRequiredItem();
		if (req == null) return;
		
		if (cargo == null) {
			CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
			if (fleet == null) return;
			cargo = fleet.getCargo();
		}
		
		String memberId = member.getId();
		List<HullModInstallData> installed = map.getList(memberId);
		for (HullModInstallData data : installed) {
			if (data.modId.equals(modId)) {
				map.remove(memberId, data);
				cargo.addItems(data.item.getType(), data.item.getData(), 1);
				break;
			}
		}
	}
}
















