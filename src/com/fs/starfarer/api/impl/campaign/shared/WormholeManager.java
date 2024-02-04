package com.fs.starfarer.api.impl.campaign.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.impl.campaign.JumpPointInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;

public class WormholeManager {
	
	public static String KEY = "$core_wormholeManager";
	
	public static String WORMHOLE = "$wormhole";
	public static String LIMBO_STABLE_LOCATION = "$limboStableLocation";
	public static String GOT_WORMHOLE_CALIBRATION_DATA = "$gotWormholeCalibrationData";
	
	
//	public static float UNSTABLE_DURATION_MIN = 365f * 0.75f;
//	public static float UNSTABLE_DURATION_MAX = 365 * 1.25f;
	public static float UNSTABLE_DURATION_MIN = 180f * 0.75f;
	public static float UNSTABLE_DURATION_MAX = 180f * 1.25f;
	
	public static WormholeManager get() {
		Object obj = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		if (obj != null) return (WormholeManager) obj;
		
		WormholeManager data = new WormholeManager();
		Global.getSector().getMemoryWithoutUpdate().set(KEY, data);
		return data;
	}
	
	public static class WormholeItemData {
		public String category;
		public String id;
		public String name;
		public WormholeItemData(String jsonStr) {
			try {
				JSONObject json = new JSONObject(jsonStr);
				category = json.getString("cat");
				id = json.getString("id");
				name = json.getString("name");
			} catch (JSONException e) {
				throw new RuntimeException("Unable to parse Wormhole Anchor data [" + jsonStr + "]", e);
			}
		}
		public WormholeItemData(String category, String id, String name) {
			this.category = category;
			this.id = id;
			this.name = name;
		}
		
		public String toJsonStr() {
			try {
				JSONObject json = new JSONObject();
				json.put("cat", category);
				json.put("id", id);
				json.put("name", name);
				return json.toString();
			} catch (JSONException e) {
				return null;
			}
		}
	}
	
	public static class WormholeData {
		public SpecialItemData item;
		public JumpPointAPI jumpPoint;
		public WormholeItemData itemData;
		public WormholeData(SpecialItemData item, JumpPointAPI jumpPoint, WormholeItemData itemData) {
			this.item = item;
			this.jumpPoint = jumpPoint;
			this.itemData = itemData;
		}
	}
	
	public static boolean willWormholeBecomeUnstable(SectorEntityToken stableLocation) {
		boolean makeUnstable = true;
		if (stableLocation.getMemoryWithoutUpdate().getBoolean(LIMBO_STABLE_LOCATION) &&
				Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(GOT_WORMHOLE_CALIBRATION_DATA)) {
			makeUnstable = false;
		}
		return makeUnstable;
	}
	
	
	protected List<WormholeData> deployed = new ArrayList<WormholeData>();
	

	public void updateWormholeDestinations() {
		List<WormholeData> sorted = new ArrayList<WormholeData>(deployed);
		Collections.sort(sorted, new Comparator<WormholeData>() {
			public int compare(WormholeData o1, WormholeData o2) {
				return o1.itemData.name.compareTo(o2.itemData.name);
			}
		});
		
		for (WormholeData data : deployed) {
			JumpPointAPI jp = data.jumpPoint;
			jp.clearDestinations();
			boolean added = false;
			for (WormholeData other : sorted) {
				if (other == data) continue;
				if (!data.itemData.category.equals(other.itemData.category)) continue;

				jp.addDestination(new JumpDestination(other.jumpPoint, "wormhole terminus " + other.itemData.name));
				added = true;
			}
			if (added) {
				jp.setStandardWormholeToStarfieldVisual();
			} else {
				jp.setStandardWormholeToNothingVisual();
			}
		}
	}
	
	public JumpPointAPI addWormhole(SpecialItemData item, SectorEntityToken stableLocation, InteractionDialogAPI dialog) {
		WormholeItemData itemData = new WormholeItemData(item.getData());
		
		WormholeData data = getDeployed(itemData.id);
		if (data != null) return data.jumpPoint;
		
		boolean makeUnstable = willWormholeBecomeUnstable(stableLocation);
		
		LocationAPI loc = stableLocation.getContainingLocation();
		JumpPointAPI wormhole = Global.getFactory().createJumpPoint(null, "Wormhole Terminus " + itemData.name);
		wormhole.setRadius(75f);
		//wormhole.setStandardWormholeToStarfieldVisual();
		
		wormhole.getMemoryWithoutUpdate().set(WORMHOLE, true);
		if (makeUnstable) {
			float dur = UNSTABLE_DURATION_MIN + (UNSTABLE_DURATION_MAX - UNSTABLE_DURATION_MIN) * (float) Math.random();
			if (Global.getSettings().isDevMode() && !Global.getSettings().getBoolean("playtestingMode")) {
				dur *= 0.01f;
			}
			wormhole.getMemoryWithoutUpdate().set(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY, true, dur);
		}
		
		loc.addEntity(wormhole);
		if (stableLocation.getOrbit() != null) {
			wormhole.setOrbit(stableLocation.getOrbit().makeCopy());
		}
		wormhole.setLocation(stableLocation.getLocation().x, stableLocation.getLocation().y);
		loc.removeEntity(stableLocation);
		//Misc.fadeAndExpire(stableLocation);
		
		// DO NOT set this, it causes the original stable location to be shown on the map
		// when the player is not in-system
		//wormhole.getMemoryWithoutUpdate().set("$originalStableLocation", stableLocation);
		
		deployed.add(new WormholeData(item, wormhole, itemData));
		
		if (dialog != null && dialog.getTextPanel() != null) {
			Global.getSector().getPlayerFleet().getCargo().removeItems(CargoItemType.SPECIAL, item, 1);
			AddRemoveCommodity.addItemLossText(item, 1, dialog.getTextPanel());
		}
		
		updateWormholeDestinations();
		
		return wormhole;
	}
	
	public void removeWormhole(JumpPointAPI jp, InteractionDialogAPI dialog) {
		for (WormholeData data : deployed) {
			if (data.jumpPoint == jp) {
				LocationAPI loc = jp.getContainingLocation();
				SectorEntityToken stableLocation = loc.addCustomEntity(null,
						 									 null,
						 Entities.STABLE_LOCATION, // type of object, defined in custom_entities.json
						 Factions.NEUTRAL); // faction
				if (jp.getOrbit() != null) {
					stableLocation.setOrbit(jp.getOrbit().makeCopy());
				}
				//Misc.fadeAndExpire(jp);
				loc.removeEntity(jp);
				
				if (dialog != null && dialog.getTextPanel() != null) {
					Global.getSector().getPlayerFleet().getCargo().addItems(CargoItemType.SPECIAL, data.item, 1);
					AddRemoveCommodity.addItemGainText(data.item, 1, dialog.getTextPanel());
				}
				
				deployed.remove(data);
				
				updateWormholeDestinations();
				break;
			}
		}
	}
	
	public boolean isDeployed(String id) {
		if (id == null) return false;
		for (WormholeData data : deployed) {
			if (id.equals(data.itemData.id)) return true;
		}
		return false;
	}
	public WormholeData getDeployed(String id) {
		if (id == null) return null;
		for (WormholeData data : deployed) {
			if (id.equals(data.itemData.id)) return data;
		}
		return null;
	}
	
	public static SpecialItemData createWormholeAnchor(String id, String name) {
		return createWormholeAnchor("standard", id, name);
	}
	public static SpecialItemData createWormholeAnchor(String category, String id, String name) {
		WormholeItemData itemData = new WormholeItemData(category, id, name);
		SpecialItemData item = new SpecialItemData(Items.WORMHOLE_ANCHOR, itemData.toJsonStr());
		return item;
	}
	
}



















