package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.CustomSkillDescription;
import com.fs.starfarer.api.characters.FleetTotalItem;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI.SkillLevelAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class BaseSkillEffectDescription implements CustomSkillDescription {

	public static float TOOLTIP_WIDTH = 450;
	
	public static float FIGHTER_BAYS_THRESHOLD = 8;
	public static float OP_THRESHOLD = 1000;
	public static float OP_LOW_THRESHOLD = 500;
	public static float OP_ALL_LOW_THRESHOLD = 500;
	public static float PHASE_OP_THRESHOLD = 150;
	public static float MILITARIZED_OP_THRESHOLD = 50;
	
	public static float AUTOMATED_POINTS_THRESHOLD = 200;
	
	public static boolean USE_RECOVERY_COST = true;
	
	static {
		if (USE_RECOVERY_COST) {
			// working off 60% of the default battle size of 300 for these, which is 180
			OP_THRESHOLD = 240;
			OP_LOW_THRESHOLD = 120;
			OP_ALL_LOW_THRESHOLD = 60;
//			OP_THRESHOLD = 150;
//			OP_LOW_THRESHOLD = 75;
//			OP_ALL_LOW_THRESHOLD = 50;
			
			
			PHASE_OP_THRESHOLD = 30;
			MILITARIZED_OP_THRESHOLD = 5;
			AUTOMATED_POINTS_THRESHOLD = 30;
		}
	}
	
	
	public static enum ThresholdBonusType {
		OP,
		OP_LOW,
		OP_ALL_LOW,
		MILITARIZED_OP,
		PHASE_OP,
		FIGHTER_BAYS,
		AUTOMATED_POINTS,
	}
	
	protected Color tc;
	protected Color dtc;
	protected Color hc;
	protected Color dhc;
	protected String indent;
	protected int alpha;

	public void init(MutableCharacterStatsAPI stats, SkillSpecAPI skill) {
		indent = BaseIntelPlugin.BULLET;
		tc = Misc.getTextColor();
		hc = Misc.getHighlightColor();
		dhc = Misc.setAlpha(hc, 155);
		alpha = 255;
		float level = stats.getSkillLevel(skill.getId());
		if (level <= 0) {
			tc = Misc.getGrayColor();
			hc = dhc;
			alpha = 155;
		}
		dtc = Misc.getGrayColor();
	}
	
	public void addFighterBayThresholdInfo(TooltipMakerAPI info, FleetDataAPI data) {
		if (isInCampaign()) {
			int bays = Math.round(getNumFighterBays(data));
			String baysStr = "fighter bays";
			if (bays == 1) baysStr = "fighter bay";
			//baysStr = "";
			info.addPara(indent + "Maximum at %s or less fighter bays in fleet, your fleet has %s " + baysStr,
					0f, tc, hc, 
					"" + (int) FIGHTER_BAYS_THRESHOLD,
					"" + bays);
		} else {
			info.addPara(indent + "Maximum at %s or less fighter bays in fleet",
					0f, tc, hc, 
					"" + (int) FIGHTER_BAYS_THRESHOLD);
		}
	}
	
	public void addOPThresholdInfo(TooltipMakerAPI info, FleetDataAPI data, MutableCharacterStatsAPI cStats) {
		addOPThresholdInfo(info, data, cStats, OP_THRESHOLD);
	}
	public void addOPThresholdInfo(TooltipMakerAPI info, FleetDataAPI data, MutableCharacterStatsAPI cStats, float threshold) {
		if (USE_RECOVERY_COST) {
			if (isInCampaign()) {
				float op = getTotalCombatOP(data, cStats);
				info.addPara(indent + "Maximum at %s or less total combat ship recovery cost, your fleet's total is %s",
						0f, tc, hc, 
						"" + (int) threshold,
						"" + (int)Math.round(op));
			} else {
				info.addPara(indent + "Maximum at %s or less total combat ship recovery cost for fleet",
						0f, tc, hc, 
						"" + (int) threshold);
			}
			return;
		}
		if (isInCampaign()) {
			float op = getTotalCombatOP(data, cStats);
			String opStr = "points";
			if (op == 1) opStr = "point";
			info.addPara(indent + "Maximum at %s or less total combat ship ordnance points in fleet, your fleet has %s " + opStr,
					0f, tc, hc, 
					"" + (int) threshold,
					"" + (int)Math.round(op));
		} else {
			info.addPara(indent + "Maximum at %s or less total combat ship ordnance points in fleet",
					0f, tc, hc, 
					"" + (int) threshold);
		}
	}
	
	public void addOPThresholdAll(TooltipMakerAPI info, FleetDataAPI data, MutableCharacterStatsAPI cStats, float threshold) {
		if (USE_RECOVERY_COST) {
			if (isInCampaign()) {
				float op = getTotalOP(data, cStats);
				info.addPara(indent + "Maximum at %s or less total deployment recovery cost, your fleet's total is %s",
						0f, tc, hc, 
						"" + (int) threshold,
						"" + (int)Math.round(op));
			} else {
				info.addPara(indent + "Maximum at %s or less total deployment recovery cost for fleet",
						0f, tc, hc, 
						"" + (int) threshold);
			}
			return;
		}
		if (isInCampaign()) {
			float op = getTotalOP(data, cStats);
//			String opStr = "combat ship ordnance points";
//			if (op == 1) opStr = "combat ship ordnance point";
			String opStr = "points";
			if (op == 1) opStr = "point";
			info.addPara(indent + "Maximum at %s or less total ordnance points in fleet, your fleet has %s " + opStr,
					0f, tc, hc, 
					"" + (int) threshold,
					"" + (int)Math.round(op));
		} else {
			info.addPara(indent + "Maximum at %s or less total ordnance points in fleet",
					0f, tc, hc, 
					"" + (int) threshold);
		}
	}
	
	public void addPhaseOPThresholdInfo(TooltipMakerAPI info, FleetDataAPI data, MutableCharacterStatsAPI cStats) {
		if (USE_RECOVERY_COST) {
			if (isInCampaign()) {
				float op = getPhaseOP(data, cStats);
				info.addPara(indent + "Maximum at %s or less total combat phase ship recovery cost, your fleet's total is %s",
						0f, tc, hc, 
						"" + (int) PHASE_OP_THRESHOLD,
						"" + (int)Math.round(op));
			} else {
				info.addPara(indent + "Maximum at %s or less total combat phase ship recovery cost for fleet",
						0f, tc, hc, 
						"" + (int) PHASE_OP_THRESHOLD);
			}
			
			return;
		}
		if (isInCampaign()) {
			float op = getPhaseOP(data, cStats);
			String opStr = "points";
			if (op == 1) opStr = "point";
			info.addPara(indent + "Maximum at %s or less total combat phase ship ordnance points in fleet, your fleet has %s " + opStr,
					0f, tc, hc, 
					"" + (int) PHASE_OP_THRESHOLD,
					"" + (int)Math.round(op));
		} else {
			info.addPara(indent + "Maximum at %s or less total combat phase ship ordnance points in fleet",
					0f, tc, hc, 
					"" + (int) PHASE_OP_THRESHOLD);
		}
	}
	
	public void addAutomatedThresholdInfo(TooltipMakerAPI info, FleetDataAPI data, MutableCharacterStatsAPI cStats) {
		if (USE_RECOVERY_COST) {
			if (isInCampaign()) {
				float op = getAutomatedPoints(data, cStats);
				info.addPara(indent + "Maximum at %s or less total automated ship points*, your fleet's total is %s ",
						0f, tc, hc, 
						"" + (int) AUTOMATED_POINTS_THRESHOLD,
						"" + (int)Math.round(op));
			} else {
				info.addPara(indent + "Maximum at %s or less total automated ship points* for fleet",
						0f, tc, hc, 
						"" + (int) AUTOMATED_POINTS_THRESHOLD);
			}
			return;
		}
		if (isInCampaign()) {
			float op = getAutomatedPoints(data, cStats);
			String opStr = "points";
			if (op == 1) opStr = "point";
			info.addPara(indent + "Maximum at %s or less total automated ship points* in fleet, your fleet has %s " + opStr,
					0f, tc, hc, 
					"" + (int) AUTOMATED_POINTS_THRESHOLD,
					"" + (int)Math.round(op));
		} else {
			info.addPara(indent + "Maximum at %s or less total automated ship points* in fleet",
					0f, tc, hc, 
					"" + (int) AUTOMATED_POINTS_THRESHOLD);
		}
	}
	
	public void addMilitarizedOPThresholdInfo(TooltipMakerAPI info, FleetDataAPI data, MutableCharacterStatsAPI cStats) {
		if (USE_RECOVERY_COST) {
			if (isInCampaign()) {
				float op = getMilitarizedOP(data, cStats);
				info.addPara(indent + "Maximum at %s or less total deployment recovery cost for ships with Militarized Subsystems, your fleet's total is %s",
						0f, tc, hc, 
						"" + (int) MILITARIZED_OP_THRESHOLD,
						"" + (int)Math.round(op));
			} else {
				info.addPara(indent + "Maximum at %s or less total deployment recovery cost for ships with Militarized Subsystems for fleet",
						0f, tc, hc, 
						"" + (int) MILITARIZED_OP_THRESHOLD);
			}
			return;
		} else {
			if (isInCampaign()) {
				float op = getMilitarizedOP(data, cStats);
				String opStr = "points";
				if (op == 1) opStr = "point";
				info.addPara(indent + "Maximum at %s or less total ordnance points for ships with Militarized Subsystems, your fleet has %s " + opStr,
						0f, tc, hc, 
						"" + (int) MILITARIZED_OP_THRESHOLD,
						"" + (int)Math.round(op));
			} else {
				info.addPara(indent + "Maximum at %s or less total ordnance points for ships with Militarized Subsystems",
						0f, tc, hc, 
						"" + (int) MILITARIZED_OP_THRESHOLD);
			}
		}
	}
	
	protected float computeAndCacheThresholdBonus(MutableShipStatsAPI stats,
			String key, float maxBonus, ThresholdBonusType type) {
		FleetDataAPI data = getFleetData(stats);
		MutableCharacterStatsAPI cStats = getCommanderStats(stats);
		return computeAndCacheThresholdBonus(data, cStats, key, maxBonus, type);
	}
	protected float computeAndCacheThresholdBonus(FleetDataAPI data, MutableCharacterStatsAPI cStats,
			String key, float maxBonus, ThresholdBonusType type) {
		if (data == null) return maxBonus;
		if (cStats.getFleet() == null) return maxBonus;

		Float bonus = (Float) data.getCacheClearedOnSync().get(key);
		if (bonus != null) return bonus;

		float currValue = 0f;
		float threshold = 1f;
		
		if (type == ThresholdBonusType.FIGHTER_BAYS) {
			currValue = getNumFighterBays(data);
			threshold = FIGHTER_BAYS_THRESHOLD;
		} else if (type == ThresholdBonusType.OP) {
			currValue = getTotalCombatOP(data, cStats);
			threshold = OP_THRESHOLD;
		} else if (type == ThresholdBonusType.OP_LOW) {
			currValue = getTotalCombatOP(data, cStats);
			threshold = OP_LOW_THRESHOLD;
		} else if (type == ThresholdBonusType.OP_ALL_LOW) {
			currValue = getTotalOP(data, cStats);
			threshold = OP_ALL_LOW_THRESHOLD;
		} else if (type == ThresholdBonusType.MILITARIZED_OP) {
			currValue = getMilitarizedOP(data, cStats);
			threshold = MILITARIZED_OP_THRESHOLD;
		} else if (type == ThresholdBonusType.PHASE_OP) {
			currValue = getPhaseOP(data, cStats);
			threshold = PHASE_OP_THRESHOLD;
		} else if (type == ThresholdBonusType.AUTOMATED_POINTS) {
			currValue = getAutomatedPoints(data, cStats);
			threshold = AUTOMATED_POINTS_THRESHOLD;
		}
		
		bonus = getThresholdBasedRoundedBonus(maxBonus, currValue, threshold);

		data.getCacheClearedOnSync().put(key, bonus);
		return bonus;
	}
//	refitData.addOrUpdate(0, 0, indent + "Combat ship ordnance points", "" + (int)totalOP);
//	refitData.addOrUpdate(0, 1, indent + "Phase ship ordnance points", "" + (int)phaseOP);
//	refitData.addOrUpdate(0, 2, indent + "Fighter bays", "" + (int)bays);
//	float totalOP = BaseSkillEffectDescription.getTotalOP(fleet.getFleetData(), Global.getSector().getPlayerStats());
//	float phaseOP = BaseSkillEffectDescription.getPhaseOP(fleet.getFleetData(), Global.getSector().getPlayerStats());
	
	public FleetTotalItem getOPTotal() {
		final CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		final MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
		FleetTotalItem item = new FleetTotalItem();
		item.label = "Total ordnance points";
		if (USE_RECOVERY_COST) {
			item.label = "All ships";
		}
		item.value = "" + (int) getTotalOP(fleet.getFleetData(), stats);
		item.sortOrder = 50;
		
		item.tooltipCreator = getTooltipCreator(new TooltipCreatorSkillEffectPlugin() {
			public void addDescription(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("The total deployment points of all the ships in your fleet.", 0f);
			}
			public List<FleetMemberPointContrib> getContributors() {
				return getTotalOPDetail(fleet.getFleetData(), stats);
			}
		});
		
		return item;
	}
	
	public FleetTotalItem getCombatOPTotal() {
		final CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		final MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
		FleetTotalItem item = new FleetTotalItem();
		item.label = "Combat ship ordnance points";
		if (USE_RECOVERY_COST) {
			item.label = "Combat ships";
		}
		item.value = "" + (int) BaseSkillEffectDescription.getTotalCombatOP(fleet.getFleetData(), stats);
		item.sortOrder = 100;
		
		item.tooltipCreator = getTooltipCreator(new TooltipCreatorSkillEffectPlugin() {
			public void addDescription(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("The total deployment points of all the combat ships in your fleet.", 0f);
			}
			public List<FleetMemberPointContrib> getContributors() {
				return getTotalCombatOPDetail(fleet.getFleetData(), stats);
			}
		});
		
		return item;
	}
	
	public FleetTotalItem getAutomatedPointsTotal() {
		final CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		final MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
		FleetTotalItem item = new FleetTotalItem();
		item.label = "Automated ships";
		item.value = "" + (int) BaseSkillEffectDescription.getAutomatedPoints(fleet.getFleetData(), stats);
		item.sortOrder = 350;
		
		item.tooltipCreator = getTooltipCreator(new TooltipCreatorSkillEffectPlugin() {
			public void addDescription(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("The total deployment points of all the automated ships in your fleet, "
						+ "with additional points for ships controlled by AI cores.", 0f);
			}
			public List<FleetMemberPointContrib> getContributors() {
				return getAutomatedPointsDetail(fleet.getFleetData(), stats);
			}
		});
		
		return item;
	}
	
	public FleetTotalItem getPhaseOPTotal() {
		final CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		final MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
		FleetTotalItem item = new FleetTotalItem();
		item.label = "Phase ship ordnance points";
		if (USE_RECOVERY_COST) {
			item.label = "Phase ships";
		}
		item.value = "" + (int) BaseSkillEffectDescription.getPhaseOP(fleet.getFleetData(), stats);
		item.sortOrder = 200;
		
		item.tooltipCreator = getTooltipCreator(new TooltipCreatorSkillEffectPlugin() {
			public void addDescription(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("The total deployment points of all the non-civilian phase ships in your fleet.", 0f);
			}
			public List<FleetMemberPointContrib> getContributors() {
				return getPhaseOPDetail(fleet.getFleetData(), stats);
			}
		});
		return item;
	}
	
	public FleetTotalItem getMilitarizedOPTotal() {
		final CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		final MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
		FleetTotalItem item = new FleetTotalItem();
		item.label = "Militarized ship ordnance points";
		if (USE_RECOVERY_COST) {
			item.label = "Militarized ships";
		}
		item.value = "" + (int) BaseSkillEffectDescription.getMilitarizedOP(fleet.getFleetData(), stats);
		item.sortOrder = 300;
		
		item.tooltipCreator = getTooltipCreator(new TooltipCreatorSkillEffectPlugin() {
			public void addDescription(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("The total deployment points of all the ships in your fleet that have "
						+ "the \"Militarized Subsystems\" hullmod.", 0f);
			}
			public List<FleetMemberPointContrib> getContributors() {
				return getMilitarizedOPDetail(fleet.getFleetData(), stats);
			}
		});
		
		return item;
	}
	
	public FleetTotalItem getFighterBaysTotal() {
		final CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		FleetTotalItem item = new FleetTotalItem();
		item.label = "Fighter bays";
		item.value = "" + (int) BaseSkillEffectDescription.getNumFighterBays(fleet.getFleetData());
		item.sortOrder = 400;
		
		item.tooltipCreator = getTooltipCreator(new TooltipCreatorSkillEffectPlugin() {
			public void addDescription(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("The total number of fighter bays in your fleet. Both empty and filled "
						+ "fighter bays are counted. Built-in fighter bays can be removed from a ship by "
						+ "installing the \"Converted Fighter Bay\" hullmod.", 0f);
			}
			public List<FleetMemberPointContrib> getContributors() {
				return getNumFighterBaysDetail(fleet.getFleetData());
			}
		});
		
		return item;
	}
	
	public interface TooltipCreatorSkillEffectPlugin {
		void addDescription(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam);
		List<FleetMemberPointContrib> getContributors();
	}
	public TooltipCreator getTooltipCreator(final TooltipCreatorSkillEffectPlugin plugin) {
		return new BaseTooltipCreator() {
			public float getTooltipWidth(Object tooltipParam) {
				return TOOLTIP_WIDTH;
			}
			@SuppressWarnings("unchecked")
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float pad = 3f;
				float opad = 10f;
				
				plugin.addDescription(tooltip, expanded, tooltipParam);
				
				tooltip.addPara("Certain skills will have reduced effects if this value exceeds a "
						+ "skill-specific threshold; see the specific skill for details. Mothballed "
						+ "ships are not counted towards the total.", opad);
				
				tooltip.addPara("Skills using this value:", opad);
				tooltip.setBulletedListMode(BaseIntelPlugin.INDENT);
				List<SkillLevelAPI> skills = (List<SkillLevelAPI>) tooltipParam;
				Set<SkillLevelAPI> seen = new HashSet<MutableCharacterStatsAPI.SkillLevelAPI>();
				
				float initPad = opad;
				//initPad = pad;
				for (SkillLevelAPI skill : skills) {
					if (seen.contains(skill)) continue;
					seen.add(skill);
					String label = skill.getSkill().getName() + " (" + skill.getSkill().getGoverningAptitudeName() + ")";
					label = skill.getSkill().getGoverningAptitudeName() + " - " + skill.getSkill().getName();
					tooltip.addPara(label, 
							skill.getSkill().getGoverningAptitudeColor(), initPad);
					initPad = pad;
				}
				tooltip.setBulletedListMode(null);
				
				List<FleetMemberPointContrib> members = plugin.getContributors();
				Collections.sort(members, new Comparator<FleetMemberPointContrib>() {
					public int compare(FleetMemberPointContrib o1, FleetMemberPointContrib o2) {
						return o2.points - o1.points;
					}
				});
				
				tooltip.addPara("Ships contributing to this value:", opad);
				initPad = opad;
				//initPad = pad;
				if (members.isEmpty()) {
					tooltip.setBulletedListMode(BaseIntelPlugin.INDENT);
					tooltip.addPara("None", initPad);
					tooltip.setBulletedListMode(null);
				}
				float gridWidth = 450f;
				//tooltip.beginGrid(gridWidth, 1);
				tooltip.beginGridFlipped(gridWidth, 1, 40f, opad);
				int i = 0;
				int max = 20;
				if (members.size() == max + 1) {
					max = members.size();
				}
				for (FleetMemberPointContrib member : members) {
//					String label = tooltip.shortenString(BaseIntelPlugin.INDENT + getValueLabelForMember(member.member),
//													gridWidth);
					String label = tooltip.shortenString(getValueLabelForMember(member.member),
							gridWidth - 60f);
					tooltip.addToGrid(0, i++, label, "+" + member.points);
					if (i >= max) break;
				}
				tooltip.addGrid(opad);
				if (members.size() > max) {
					tooltip.addPara("And %s other ships with a smaller contribution.", opad, 
							Misc.getHighlightColor(), "" + (members.size() - max));
				}
				
			}
		};
	}
	
	public static String getValueLabelForMember(FleetMemberAPI member) {
		String str = "";
		if (!member.isFighterWing()) {
			str += member.getShipName() + ", ";
			str += member.getHullSpec().getHullNameWithDashClass();
		} else {
			str += member.getVariant().getFullDesignationWithHullName();
		}
		if (member.isMothballed()) {
			str += " (mothballed)";
		}
		return str;
	}
	
	
	public float getThresholdBasedBonus(float maxBonus, float value, float threshold) {
		float bonus = maxBonus * threshold / Math.max(value, threshold);
		return bonus;
	}
	public float getThresholdBasedRoundedBonus(float maxBonus, float value, float threshold) {
		float bonus = maxBonus * threshold / Math.max(value, threshold);
		if (bonus > 0 && bonus < 1) bonus = 1;
		if (maxBonus > 1f) {
			if (bonus < maxBonus) {
				bonus = Math.min(bonus, maxBonus - 1f);
			}
			bonus = (float) Math.round(bonus);
		}
		return bonus;
	}
	
	public static boolean isMilitarized(MutableShipStatsAPI stats) {
		if (stats == null || stats.getFleetMember() == null) return false;
		return isMilitarized(stats.getFleetMember());
	}
	public static boolean isMilitarized(FleetMemberAPI member) {
		if (member == null) return false;
		MutableShipStatsAPI stats = member.getStats();
		return stats != null && stats.getVariant() != null && 
			   stats.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS);
	}
	
	public static boolean isCivilian(MutableShipStatsAPI stats) {
		if (stats == null || stats.getFleetMember() == null) return false;
		return isCivilian(stats.getFleetMember());
	}
	
	public static boolean isCivilian(FleetMemberAPI member) {
		if (member == null) return false;
		MutableShipStatsAPI stats = member.getStats();
		return stats != null && stats.getVariant() != null && 
				stats.getVariant().hasHullMod(HullMods.CIVGRADE) && 
				!stats.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS);// &&
				//!stats.getVariant().getHullSpec().getHints().contains(ShipTypeHints.CARRIER);
	}
	
	public static boolean hasFighterBays(MutableShipStatsAPI stats) {
		if (stats == null || stats.getFleetMember() == null) return false;
		return hasFighterBays(stats.getFleetMember());
	}
	
	public static boolean hasFighterBays(FleetMemberAPI member) {
		if (member == null) return false;
		MutableShipStatsAPI stats = member.getStats();
		return stats != null && stats.getNumFighterBays().getModifiedInt() > 0;
	}
	
//	public static float getTotalOP(MutableShipStatsAPI stats) {
//		FleetDataAPI data = getFleetData(stats);
//		MutableCharacterStatsAPI cStats = getCommanderStats(stats);
//		if (data == null) return 0;
//		return getTotalOP(data, cStats);
//	}
	
	protected static float getPoints(FleetMemberAPI member, MutableCharacterStatsAPI stats) {
		if (USE_RECOVERY_COST) {
			return member.getDeploymentPointsCost();
		}
		return member.getHullSpec().getOrdnancePoints(stats);
	}
	
	public static class FleetMemberPointContrib {
		public FleetMemberAPI member;
		public int points;
		public FleetMemberPointContrib(FleetMemberAPI member, int points) {
			super();
			this.member = member;
			this.points = points;
		}
	}
	
	
	public static float getTotalOP(FleetDataAPI data, MutableCharacterStatsAPI stats) {
		float op = 0;
		for (FleetMemberAPI curr : data.getMembersListCopy()) {
			if (curr.isMothballed()) continue;
			op += getPoints(curr, stats);
		}
		return Math.round(op);
	}
	
	public static List<FleetMemberPointContrib> getTotalOPDetail(FleetDataAPI data, MutableCharacterStatsAPI stats) {
		List<FleetMemberPointContrib> result = new ArrayList<BaseSkillEffectDescription.FleetMemberPointContrib>();
		for (FleetMemberAPI curr : data.getMembersListCopy()) {
			if (curr.isMothballed()) continue;
			int pts = (int) Math.round(getPoints(curr, stats));
			result.add(new FleetMemberPointContrib(curr, pts));
		}
		return result;
	}
	
	public static float getTotalCombatOP(FleetDataAPI data, MutableCharacterStatsAPI stats) {
		float op = 0;
		for (FleetMemberAPI curr : data.getMembersListCopy()) {
			if (curr.isMothballed()) continue;
			if (isCivilian(curr)) continue;
			op += getPoints(curr, stats);
		}
		return Math.round(op);
	}
	
	public static List<FleetMemberPointContrib> getTotalCombatOPDetail(FleetDataAPI data, MutableCharacterStatsAPI stats) {
		List<FleetMemberPointContrib> result = new ArrayList<BaseSkillEffectDescription.FleetMemberPointContrib>();
		for (FleetMemberAPI curr : data.getMembersListCopy()) {
			if (curr.isMothballed()) continue;
			if (isCivilian(curr)) continue;
			int pts = (int) Math.round(getPoints(curr, stats));
			result.add(new FleetMemberPointContrib(curr, pts));
		}
		return result;
	}
	
	public static float getPhaseOP(FleetDataAPI data, MutableCharacterStatsAPI stats) {
		float op = 0;
		for (FleetMemberAPI curr : data.getMembersListCopy()) {
			if (curr.isMothballed()) continue;
			if (curr.isPhaseShip()) {
				if (isCivilian(curr)) continue;
				op += getPoints(curr, stats);
			}
		}
		return Math.round(op);
	}
	
	public static List<FleetMemberPointContrib> getPhaseOPDetail(FleetDataAPI data, MutableCharacterStatsAPI stats) {
		List<FleetMemberPointContrib> result = new ArrayList<BaseSkillEffectDescription.FleetMemberPointContrib>();
		for (FleetMemberAPI curr : data.getMembersListCopy()) {
			if (curr.isMothballed()) continue;
			if (curr.isPhaseShip()) {
				if (isCivilian(curr)) continue;
				int pts = (int) Math.round(getPoints(curr, stats));
				result.add(new FleetMemberPointContrib(curr, pts));
			}
		}
		return result;
	}
	
	public static float getMilitarizedOP(FleetDataAPI data, MutableCharacterStatsAPI stats) {
		float op = 0;
		for (FleetMemberAPI curr : data.getMembersListCopy()) {
			if (curr.isMothballed()) continue;
			if (!isMilitarized(curr)) continue;
			op += getPoints(curr, stats);
		}
		return Math.round(op);
	}
	
	public static List<FleetMemberPointContrib> getMilitarizedOPDetail(FleetDataAPI data, MutableCharacterStatsAPI stats) {
		List<FleetMemberPointContrib> result = new ArrayList<BaseSkillEffectDescription.FleetMemberPointContrib>();
		for (FleetMemberAPI curr : data.getMembersListCopy()) {
			if (curr.isMothballed()) continue;
			if (!isMilitarized(curr)) continue;
			int pts = (int) Math.round(getPoints(curr, stats));
			result.add(new FleetMemberPointContrib(curr, pts));
		}
		return result;
	}
	
	public static float getNumFighterBays(FleetDataAPI data) {
		if (data ==  null) return FIGHTER_BAYS_THRESHOLD;
		float bays = 0;
		for (FleetMemberAPI curr : data.getMembersListCopy()) {
			if (curr.isMothballed()) continue;
			bays += curr.getNumFlightDecks();
		}
		return bays;
	}
	
	public static List<FleetMemberPointContrib> getNumFighterBaysDetail(FleetDataAPI data) {
		List<FleetMemberPointContrib> result = new ArrayList<BaseSkillEffectDescription.FleetMemberPointContrib>();
		for (FleetMemberAPI curr : data.getMembersListCopy()) {
			if (curr.isMothballed()) continue;
			int pts = curr.getNumFlightDecks();
			if (pts <= 0) continue;
			result.add(new FleetMemberPointContrib(curr, pts));
		}
		return result;
	}
	
	public static float getAutomatedPoints(FleetDataAPI data, MutableCharacterStatsAPI stats) {
		float points = 0;
		for (FleetMemberAPI curr : data.getMembersListCopy()) {
			if (curr.isMothballed()) continue;
			if (!Misc.isAutomated(curr)) continue;
			if (curr.getCaptain().isAICore()) {
				points += curr.getCaptain().getMemoryWithoutUpdate().getFloat(AICoreOfficerPlugin.AUTOMATED_POINTS_VALUE);
			}
			points += getPoints(curr, stats);
		}
		return Math.round(points);
	}
	
	public static List<FleetMemberPointContrib> getAutomatedPointsDetail(FleetDataAPI data, MutableCharacterStatsAPI stats) {
		List<FleetMemberPointContrib> result = new ArrayList<BaseSkillEffectDescription.FleetMemberPointContrib>();
		for (FleetMemberAPI curr : data.getMembersListCopy()) {
			if (curr.isMothballed()) continue;
			if (!Misc.isAutomated(curr)) continue;
			
			int pts = (int) Math.round(getPoints(curr, stats));
			if (curr.getCaptain().isAICore()) {
				pts += (int) Math.round(curr.getCaptain().getMemoryWithoutUpdate().getFloat(AICoreOfficerPlugin.AUTOMATED_POINTS_VALUE));
			}
			result.add(new FleetMemberPointContrib(curr, pts));
		}
		return result;
	}
	
	public static boolean isInCampaign() {
		return Global.getCurrentState() == GameState.CAMPAIGN && 
			   Global.getSector() != null && 
			   Global.getSector().getPlayerFleet() != null;
	}

	public static MutableCharacterStatsAPI getCommanderStats(MutableShipStatsAPI stats) {
		if (stats == null) {
			if (isInCampaign()) {
				return Global.getSector().getPlayerStats();
			}
			return null;
		}
		
		FleetMemberAPI member = stats.getFleetMember();
		if (member == null) return null;
		PersonAPI commander = member.getFleetCommanderForStats();
		if (commander == null) commander = member.getFleetCommander();
		if (commander != null) {
			return commander.getStats();
		}
		return null;
	}
	
	public static FleetDataAPI getFleetData(MutableShipStatsAPI stats) {
		if (stats == null) {
			if (isInCampaign()) {
				return Global.getSector().getPlayerFleet().getFleetData();
			}
			return null;
		}
		FleetMemberAPI member = stats.getFleetMember();
		if (member == null) return null;
		FleetDataAPI data = member.getFleetDataForStats();
		if (data == null) data = member.getFleetData();
		return data;
	}
	
	
	public boolean hasCustomDescription() {
		return true;
	}
	
	public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
										TooltipMakerAPI info, float width) {
	}

	public String getEffectDescription(float level) {
		return null;
	}

	public String getEffectPerLevelDescription() {
		return null;
	}

	public ScopeDescription getScopeDescription() {
		return null;
	}

}
