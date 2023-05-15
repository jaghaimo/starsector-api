package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.GroundRaidObjectivesListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.graid.BaseGroundRaidObjectivePluginImpl;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidType;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Icon id is under raidObjectives in settings.json.
 * 
 * Actually it seems to work with both; never mind the below -Alex
 * 
 * Note: this method ONLY works when option id "mktRaidNonMarket" is used as the option entering the raid menu.
 * A standard raid will not include the options added with these. In particular, this is because this command
 * specifies a trigger to call when going back from the raid menu, and that doesn't make sense in the context of 
 * a "normal" raid.
 * 
 * AddRaidObjective <icon id> <name to show> <danger> <xp gained> <trigger to run when successful> 
 * 				<optional:show in custom raid menu only> <optional: tooltip>
 */
public class AddRaidObjective extends BaseCommandPlugin {

	public static class CustomRaidObjective extends BaseGroundRaidObjectivePluginImpl {
		public CustomRaidObjectiveAdder adder;
		
		public CustomRaidObjective(CustomRaidObjectiveAdder adder) {
			super(adder.market, null);
			this.adder = adder;
			int marines = adder.danger.marineTokens;
			setMarinesRequired(marines);
		}
		
		public boolean withContinueBeforeResult() {
			return true;
		}

		@Override
		public String getQuantityString(int marines) {
			return "";
		}
		@Override
		public String getValueString(int marines) {
			return "";
		}
		public float getValueSortValue() {
			return super.getValueSortValue();
		}
		public int getCargoSpaceNeeded() {
			return 0;
		}
		public int getFuelSpaceNeeded() {
			return 0;
		}
		public int getProjectedCreditsValue() {
			return 0;
		}
		public RaidDangerLevel getDangerLevel() {
			return adder.danger;
		}
		@Override
		public int getValue(int marines) {
			return 0;
		}
		public float getQuantitySortValue() {
			float add = adder.name.hashCode();
			return QUANTITY_SORT_TIER_0 + add; 
		}
		public String getName() {
			return adder.name;
		}
		@Override
		public String getIconName() {
			return Global.getSettings().getSpriteName("raidObjectives", adder.icon);
		}
		@Override
		public float getQuantity(int marines) {
			return 0;
		}
		public int performRaid(CargoAPI loot, Random random, float lootMult, TextPanelAPI text) {
			return adder.xp;
		}
		
		@Override
		public boolean hasTooltip() {
			return adder.tooltip != null;
		}

		@Override
		public void createTooltip(TooltipMakerAPI t, boolean expanded) {
			t.addPara(adder.tooltip, 0f);
		}
	}

	public static class CustomRaidObjectiveAdder implements EveryFrameScript, GroundRaidObjectivesListener {
		protected boolean done = false;
		
		public String icon;
		public String name;
		public String trigger;
		public String tooltip;
		public int xp = 0;
		public boolean showInCustomOnly;
		public RaidDangerLevel danger;
		public MarketAPI market;
		public SectorEntityToken entity;
		
		public CustomRaidObjectiveAdder(MarketAPI market, SectorEntityToken entity, String icon, String name, String trigger, int xp, RaidDangerLevel danger, boolean showInCustomOnly, String tooltip) {
			this.market = market;
			this.entity = entity;
			this.icon = icon;
			this.name = name;
			this.trigger = trigger;
			this.tooltip = tooltip;
			this.xp = xp;
			this.danger = danger;
			this.showInCustomOnly = showInCustomOnly;
			for (CustomRaidObjectiveAdder adder : Global.getSector().getListenerManager().getListeners(CustomRaidObjectiveAdder.class)) {
				if (adder.name.equals(name) && adder.trigger.equals(trigger)) {
					return;
				}
			}
			Global.getSector().getListenerManager().addListener(this);
			Global.getSector().addScript(this);
		}
		public void advance(float amount) {
			if (amount > 0 && !done) {
				for (CustomRaidObjectiveAdder adder : Global.getSector().getListenerManager().getListeners(CustomRaidObjectiveAdder.class)) {
					if (adder.name.equals(name) && adder.trigger.equals(trigger)) {
						Global.getSector().getListenerManager().removeListener(this);
						done = true;
						break;
					}
				}
			}
		}
		public boolean isDone() {
			return done;
		}
		public boolean runWhilePaused() {
			return false;
		}
		public void modifyRaidObjectives(MarketAPI market, SectorEntityToken entity, List<GroundRaidObjectivePlugin> objectives, RaidType type, int marineTokens, int priority) {
			if (priority != 0) return;
			
			if (type == RaidType.DISRUPT) return;
			if (type != RaidType.CUSTOM_ONLY && showInCustomOnly) return;
			
			if (type == RaidType.CUSTOM_ONLY && entity != null &&
					entity.getMemoryWithoutUpdate().contains("$raidRestrictToTrigger")) {
				String restrict = entity.getMemoryWithoutUpdate().getString("$raidRestrictToTrigger");
				if (restrict != null && !restrict.isEmpty() && !restrict.equals(trigger)) {
					return;
				}
			}
			
			if ((this.market != null && market == this.market) ||
					(this.entity != null && entity == this.entity)) {
				CustomRaidObjective custom = new CustomRaidObjective(this);
				objectives.add(custom);
			}
		}

		public void reportRaidObjectivesAchieved(RaidResultData data, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
			boolean found = false;
			for (GroundRaidObjectivePlugin obj : data.objectives) {
				if (obj instanceof CustomRaidObjective) {
					CustomRaidObjective custom = (CustomRaidObjective) obj;
					if (custom.adder == this) {
						found = true;
						break;
					}
				}
			}
			if (found) {
				advance(0.1f); // triggers removal of objective
				dialog.getInteractionTarget().getMemoryWithoutUpdate().set("$raidMarinesLost", data.marinesLost, 0);
				FireAll.fire(null, dialog, memoryMap, trigger);
			}
		}
	}
	
	
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String icon = params.get(0).getString(memoryMap);
		String name = params.get(1).getStringWithTokenReplacement(ruleId, dialog, memoryMap);
		RaidDangerLevel danger = RaidDangerLevel.valueOf(RaidDangerLevel.class, params.get(2).getString(memoryMap));
		int xp = (int) params.get(3).getFloat(memoryMap);
		String trigger = params.get(4).getString(memoryMap);
		
		boolean showInCustomOnly = false;
		
		String tooltip = null;
		if (params.size() > 5) {
			if (params.size() > 6) {
				showInCustomOnly = params.get(5).getBoolean(memoryMap);
				tooltip = params.get(6).getStringWithTokenReplacement(params.get(5).getString(memoryMap), dialog, memoryMap);
			} else {
				String str = params.get(5).getString(memoryMap);
				if (str != null) {
					if (str.toLowerCase().equals("true") || str.toLowerCase().equals("false")) {
						showInCustomOnly = params.get(5).getBoolean(memoryMap);
					} else {
						tooltip = params.get(5).getStringWithTokenReplacement(params.get(5).getString(memoryMap), dialog, memoryMap);
					}
				}
			}
		}

		SectorEntityToken entity = dialog.getInteractionTarget();
		MarketAPI market = entity.getMarket();
		
		new CustomRaidObjectiveAdder(market, entity, icon, name, trigger, xp, danger, showInCustomOnly, tooltip);
		
		return true;
	}
	
}




