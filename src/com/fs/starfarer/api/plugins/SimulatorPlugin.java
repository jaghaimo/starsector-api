package com.fs.starfarer.api.plugins;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.awt.Color;

import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

/**
 * Implementations of this need to handle some campaign events but to also work outside the campaign,
 * e.g. for the devMode "edit variants" simulator, and for the mission refit simulator.
 * 
 * @author Alex
 *
 * Copyright 2024 Fractal Softworks, LLC
 */
public interface SimulatorPlugin {

	public static boolean ENABLE_OPTION_CHECKBOX_ICONS = true;
	
	public static float DEFAULT_PAD_AFTER = 20f;
	
	public static interface AdvancedSimOption {
		public String getId();
		public float getPadAfter();
	}
	
	public static class SimOptionData {
		public String id;
		public String text;
		public String tooltip;
		public String iconKey = null;
		public boolean enabled = true;
		public String unmetReq = null;
		public float extraPad = 0f;
		public SimOptionData(String id, String text, String tooltip, String iconKey) {
			this.id = id;
			this.text = text;
			this.tooltip = tooltip;
			if (ENABLE_OPTION_CHECKBOX_ICONS) {
				this.iconKey = iconKey;
			}
		}
		public SimOptionData(String id, String text, String tooltip, boolean enabled, String unmetReq, String iconKey) {
			super();
			this.id = id;
			this.text = text;
			this.tooltip = tooltip;
			this.enabled = enabled;
			this.unmetReq = unmetReq;
			if (ENABLE_OPTION_CHECKBOX_ICONS) {
				this.iconKey = iconKey;
			}
		}
	}
	
	public static class SimOptionSelectorData implements AdvancedSimOption {
		public String id;
		public String text;
		public List<SimOptionData> options = new ArrayList<SimOptionData>();
		public boolean compact = true;
		public float padAfter = DEFAULT_PAD_AFTER;

		public SimOptionSelectorData(String id, String text, boolean compact) {
			this.id = id;
			this.text = text;
			this.compact = compact;
		}
		public String getId() {
			return id;
		}
		public float getPadAfter() {
			return padAfter;
		}
	}
	
	public static class SimOptionCheckboxData implements AdvancedSimOption {
		public String id;
		public String text;
		public String tooltip;
		public boolean showOnOffState = true;
		public float padAfter = DEFAULT_PAD_AFTER;
		
		public boolean enabled = true;
		public String unmetReq = null;
		
		public SimOptionCheckboxData(String id, String text, String tooltip) {
			this.id = id;
			this.text = text;
			this.tooltip = tooltip;
		}
		public SimOptionCheckboxData(String id, String text, String tooltip, boolean enabled, String unmetReq) {
			this.id = id;
			this.text = text;
			this.tooltip = tooltip;
			this.enabled = enabled;
			this.unmetReq = unmetReq;
		}
		public String getId() {
			return id;
		}
		public float getPadAfter() {
			return padAfter;
		}
	}

	
	public static class SimCategoryData {
		public String id;
		public String name;
		public Color nameColor;
		public String iconName;
		public Object data;
		public boolean custom = false;
		public boolean nonFactionCategory = false; 
		public FactionAPI faction; // not a real faction, a faked-up copy
		
		public List<String> variants;
		public int maxVariants;
	}
	
	
	public static class SimUIStateData {
		public String selectedCategory = null;
		public boolean showAdvanced = false;
		public int groupSize;
		public Map<String, String> settings = new LinkedHashMap<String, String>();
		
		public void fromJSON(JSONObject json) {
			selectedCategory = json.optString("selected", null);
			showAdvanced = json.optBoolean("advanced", false);
			groupSize = json.optInt("groupSize", 0);
			settings = new LinkedHashMap<String, String>();
			JSONObject map = json.optJSONObject("settings");
			if (map != null) {
				for (String id : JSONObject.getNames(map)) {
					String value = map.optString(id, null);
					if (value != null) {
						settings.put(id, value);
					}
				}
			}
		}
		
		public JSONObject toJSON() throws JSONException {
			JSONObject json = new JSONObject();
			if (selectedCategory != null) {
				json.put("selected", selectedCategory);
			}
			json.put("advanced", showAdvanced);
			json.put("groupSize", groupSize);
			JSONObject map = new JSONObject();
			json.put("settings", map);
			for (String id : settings.keySet()) {
				map.put(id, (String) settings.get(id));
			}
			return json;
		}
	}
	
	public void applySettingsToFleetMembers(List<FleetMemberAPI> members, SimCategoryData category, Map<String, String> settings);
	public void applySettingsToDeployed(List<DeployedFleetMemberAPI> deployed, Map<String, String> settings);

	public List<SimCategoryData> getCategories();
	public SimCategoryData getCustomCategory();
	
	public List<AdvancedSimOption> getSimOptions(SimCategoryData category);
	public boolean showGroupDeploymentWidget(SimCategoryData category);
	
	SimUIStateData getUIStateData();
	void loadUIStateData();
	void saveUIStateData();
	

	void addCustomOpponents(List<String> variants);
	void removeCustomOpponents(List<String> variants);
	//void resetCustomOpponents();
	void loadCustomOpponents();
	void saveCustomOpponents();

	List<String> generateSelection(SimCategoryData category, int deploymentPoints);

	
	void reportPlayerBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle);

	void appendToTooltip(TooltipMakerAPI info, float initPad, float width, AdvancedSimOption option, Object extra);
	void resetToDefaults(boolean withSave);

}



