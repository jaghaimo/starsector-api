package com.fs.starfarer.api.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.MusicPlayerPlugin;
import com.fs.starfarer.api.campaign.AICoreAdminPlugin;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ResourceCostPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI.EntryType;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.SubmarketPlugin.OnClickAction;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.econ.AbandonMarketPlugin;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.ImmigrationPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.StabilizeMarketPlugin;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.events.CampaignEventManagerAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventPlugin;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.WarningBeaconEntityPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.econ.impl.ShipQuality;
import com.fs.starfarer.api.impl.campaign.econ.impl.ConstructionQueue.ConstructionQueueItem;
import com.fs.starfarer.api.impl.campaign.econ.impl.ShipQuality.QualityData;
import com.fs.starfarer.api.impl.campaign.events.BaseEventPlugin.MarketFilter;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Difficulties;
import com.fs.starfarer.api.impl.campaign.ids.Drops;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.intel.FactionCommissionIntel;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.population.CoreImmigrationPluginImpl;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.OrbitGap;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.unsetAll;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidSource;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.PulsarBeamTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain.TileParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.plugins.FactionPersonalityPickerPlugin;
import com.fs.starfarer.api.plugins.SurveyPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;


public class Misc {
	
//	public static final String SUPPLY_ACCESSIBILITY = "Supply Accessibility";
	
	public static final float FP_TO_BOMBARD_COST_APPROX_MULT = 12f;
	public static final float FP_TO_GROUND_RAID_STR_APPROX_MULT = 6f;
	
	public static final String UNKNOWN = " ";
	public static final String UNSURVEYED = "??";
	public static final String PRELIMINARY = "?";
	public static final String FULL = "X";
	
	public static Random random = new Random();

	public static enum TokenType {
		VARIABLE,
		LITERAL,
		OPERATOR,
	}
	
	public static final Vector2f ZERO = new Vector2f(0, 0);
	
	public static class VarAndMemory {
		public String name;
		public MemoryAPI memory;
	}
	public static class Token {
		public String string;
		public TokenType type;
		public String varNameWithoutMemoryKeyIfKeyIsValid = null;
		public String varMemoryKey = null;
		public Token(String string, TokenType type) {
			this.string = string;
			this.type = type;
			
			if (isVariable()) {
				int index = string.indexOf(".");
				if (index > 0 && index < string.length() - 1) {
					varMemoryKey = string.substring(1, index);
					varNameWithoutMemoryKeyIfKeyIsValid = "$" + string.substring(index + 1);
				}
			}
		}

		public VarAndMemory getVarNameAndMemory(Map<String, MemoryAPI> memoryMap) {
			String varName = varNameWithoutMemoryKeyIfKeyIsValid;
			MemoryAPI memory = memoryMap.get(varMemoryKey);
			if (memory == null) {
				varName = string;
				memory = memoryMap.get(MemKeys.LOCAL);
			}
			if (memory == null) {
				throw new RuleException("No memory found for keys: " + varMemoryKey + ", " + MemKeys.LOCAL);
			}
			
			VarAndMemory result = new VarAndMemory();
			result.name = varName;
			result.memory = memory;
			return result;
		}
		
		public String getStringWithTokenReplacement(String ruleId, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
			String text = getString(memoryMap);
			if (text == null) return null;
			text = Global.getSector().getRules().performTokenReplacement(ruleId, text, dialog.getInteractionTarget(), memoryMap);
//			Map<String, String> tokens = Global.getSector().getRules().getTokenReplacements(ruleId, dialog.getInteractionTarget(), memoryMap);
//			for (String token : tokens.keySet()) {
//				String value = tokens.get(token);
//				text = text.replaceAll("(?s)\\" + token, value);
//			}
//			text = Misc.replaceTokensFromMemory(text, memoryMap);
			return text;
		}
		public String getString(Map<String, MemoryAPI> memoryMap) {
			String string = null;
			if (isVariable()) {
				VarAndMemory var = getVarNameAndMemory(memoryMap);
				string = var.memory.getString(var.name);
			} else {
				string = this.string;
			}
			return string;
		}
		
		public boolean getBoolean(Map<String, MemoryAPI> memoryMap) {
			String str = getString(memoryMap);
			return Boolean.parseBoolean(str);
		}
		
		public float getFloat(Map<String, MemoryAPI> memoryMap) {
			float result = 0f;
			if (isVariable()) {
				VarAndMemory var = getVarNameAndMemory(memoryMap);
				result = var.memory.getFloat(var.name);
			} else {
				result = Float.parseFloat(string);
			}
			return result;
		}
		
		public Color getColor(Map<String, MemoryAPI> memoryMap) {
			String string = getString(memoryMap);
			try {
				String [] parts = string.split(Pattern.quote(","));
				return new Color(Integer.parseInt(parts[0]),
						Integer.parseInt(parts[1]),
						Integer.parseInt(parts[2]),
						Integer.parseInt(parts[3]));
			} catch (Exception e) {
				if ("bad".equals(string)) {
					string = "textEnemyColor";
				} else if ("good".equals(string)) {
					string = "textFriendColor";
				} else if ("highlight".equals(string)) {
					string = "buttonShortcut";
				}
				return Global.getSettings().getColor(string);
			}
		}
		
		public boolean isLiteral() {
			return type == TokenType.LITERAL;
		}
		public boolean isVariable() {
			return type == TokenType.VARIABLE;
		}
		public boolean isOperator() {
			return type == TokenType.OPERATOR;
		}
		@Override
		public String toString() {
			if (isVariable()) {
				return string + " (" + type.name() + ", memkey: " + varMemoryKey + ", name: " + varNameWithoutMemoryKeyIfKeyIsValid + ")";
			} else {
				return string + " (" + type.name() + ")";
			}
		}
	}
	
	public static List<Token> tokenize(String string) {
		List<Token> result = new ArrayList<Token>();
		boolean inQuote = false;
		boolean inOperator = false;
		
		StringBuffer currToken = new StringBuffer();
		for (int i = 0; i < string.length(); i++) {
			char curr = string.charAt(i);
			char next = 0;
			if (i + 1 < string.length()) next = string.charAt(i + 1);
			boolean charEscaped = false;
			if (curr == '\\') {
				i++;
				if (i >= string.length()) {
					throw new RuleException("Escape character at end of string in: [" + string + "]");
				}
				curr = string.charAt(i);
				if (i + 1 < string.length()) next = string.charAt(i + 1);
				charEscaped = true;
			}
//			if (charEscaped) {
//				System.out.println("dfsdfs");
//			}
			
			if (curr == '"' && !charEscaped) {
				inQuote = !inQuote;
				if (!inQuote && currToken.length() <= 0) {
					result.add(new Token("", TokenType.LITERAL));
				} else if (currToken.length() > 0) {
					String str = currToken.toString();
					if (!inQuote) {
						result.add(new Token(str, TokenType.LITERAL));
					} else {
						if (str.startsWith("$")) {
							result.add(new Token(str, TokenType.VARIABLE));
						} else if (inOperator) {
							result.add(new Token(str, TokenType.OPERATOR));
						} else {
							result.add(new Token(str, TokenType.LITERAL));
						}
					}
				}
				inOperator = false;
				currToken.delete(0, 1000000);
				continue;
			}
			
			if (!inQuote && (curr == ' ' || curr == '\t')) {
				if (currToken.length() > 0) {
					String str = currToken.toString();
					if (str.startsWith("$")) {
						result.add(new Token(str, TokenType.VARIABLE));
					} else if (inOperator) {
						result.add(new Token(str, TokenType.OPERATOR));
					} else {
						result.add(new Token(str, TokenType.LITERAL));
					}
				}
				inOperator = false;
				currToken.delete(0, 1000000);
				continue;
			}
			
			if (!inQuote && !inOperator && isOperatorChar(curr) && (curr != '-' || !isDigit(next))) {
				if (currToken.length() > 0) {
					String str = currToken.toString();
					if (str.startsWith("$")) {
						result.add(new Token(str, TokenType.VARIABLE));
					} else {
						result.add(new Token(str, TokenType.LITERAL));
					}
				}
				currToken.delete(0, 1000000);
				inOperator = true;
				if (charEscaped && curr == 'n') {
					currToken.append("\n");
				} else {
					currToken.append(curr);
				}
				continue;
			}
			
			if (!inQuote && inOperator && !isOperatorChar(curr)) {
				if (currToken.length() > 0) {
					String str = currToken.toString();
					result.add(new Token(str, TokenType.OPERATOR));
				}
				currToken.delete(0, 1000000);
				inOperator = false;
				if (charEscaped && curr == 'n') {
					currToken.append("\n");
				} else {
					currToken.append(curr);
				}
				continue;
			}
			
			if (charEscaped && curr == 'n') {
				currToken.append("\n");
			} else {
				currToken.append(curr);
			}
		}
		
		if (inQuote) {
			throw new RuleException("Unmatched quotes in string: " + string + "]");
		}
		
		if (currToken.length() > 0) {
			String str = currToken.toString();
			if (str.startsWith("$")) {
				result.add(new Token(str, TokenType.VARIABLE));
			} else if (inOperator) {
				result.add(new Token(str, TokenType.OPERATOR));
			} else {
				result.add(new Token(str, TokenType.LITERAL));
			}
		}
	
		return result;
	}
	
	
	private static boolean isDigit(char c) {
		if (c == 0) return false;
		String digits = "1234567890";
		return digits.contains("" + c);
	}
	private static boolean isOperatorChar(char c) {
		String operatorChars = "=<>!+-";
		return operatorChars.contains("" + c);
	}
	
	
	public static String ucFirst(String str) {
		if (str == null) return "Null";
		if (str.isEmpty()) return "";
		return ("" + str.charAt(0)).toUpperCase() + str.substring(1);
	}
	
	public static String lcFirst(String str) {
		if (str == null) return "Null";
		if (str.isEmpty()) return "";
		return ("" + str.charAt(0)).toLowerCase() + str.substring(1);
	}
	
	
	public static String replaceTokensFromMemory(String text, Map<String, MemoryAPI> memoryMap) {
		for (String key : memoryMap.keySet()) {
			MemoryAPI memory = memoryMap.get(key);
			List<String> keys = new ArrayList<String>(memory.getKeys());
			Collections.sort(keys, new Comparator<String>() {
				public int compare(String o1, String o2) {
					return o2.length() - o1.length();
				}
			});
			for (String token : keys) {
				Object value = memory.get(token);
				if (value == null) value = "null";
				if (value instanceof String || value instanceof Boolean || value instanceof Float || value instanceof Integer) {
					text = text.replaceAll("(?s)\\$" + Pattern.quote(key) + "\\." + Pattern.quote(token.substring(1)), value.toString());
					text = text.replaceAll("(?s)\\$" + Pattern.quote(token.substring(1)), value.toString());
				}
			}
		}
		return text;
	}
	
	
	public static float getDistance(SectorEntityToken from, SectorEntityToken to) {
		return getDistance(from.getLocation(), to.getLocation());
	}
	public static float getDistanceLY(SectorEntityToken from, SectorEntityToken to) {
		return getDistanceLY(from.getLocation(), to.getLocation());
	}
	
	
	private static Vector2f temp3 = new Vector2f();
	public static float getDistance(Vector2f v1, Vector2f v2) {
		//return Vector2f.sub(v1, v2, temp3).length();
		return (float) Math.sqrt((v1.x - v2.x) * (v1.x - v2.x) + (v1.y - v2.y) * (v1.y - v2.y));
	}
	
	public static float getDistanceSq(Vector2f v1, Vector2f v2) {
		//return Vector2f.sub(v1, v2, temp3).lengthSquared();
		return (v1.x - v2.x) * (v1.x - v2.x) + (v1.y - v2.y) * (v1.y - v2.y);
	}
	
	public static float getDistance(float x1, float y1, float x2, float y2) {
//		float xDiff = Math.abs(x1 - x2);
//		float yDiff = Math.abs(y1 - y2);
//		return (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
		return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		
	}
	
	public static float getDistanceToPlayerLY(Vector2f locInHyper) {
		if (Global.getSector().getPlayerFleet() == null) return 100000f;
		return getDistanceLY(Global.getSector().getPlayerFleet().getLocationInHyperspace(), locInHyper);
	}
	public static float getDistanceToPlayerLY(SectorEntityToken other) {
		if (Global.getSector().getPlayerFleet() == null) return 100000f;
		return getDistanceLY(Global.getSector().getPlayerFleet().getLocationInHyperspace(), other.getLocationInHyperspace());
	}
	
	public static float getDistanceLY(Vector2f v1, Vector2f v2) {
		return Vector2f.sub(v1, v2, temp3).length() / getUnitsPerLightYear();
	}
	
	public static float getRounded(float in) {
		if (in <= 10) return Math.max(1, (int) in);
		float pow = (int) Math.log10(in);
		float div = (float) Math.pow(10, Math.max(0, pow - 1));
		if (pow == 1) div = 10;
		return (int) Math.round(in / div) * div;
	}
	
	public static String getRoundedValue(float value) {
		if (Math.abs((float)Math.round(value) - value) < 0.0001f) {
			//if (value > 10 || value < -10) {
				return String.format("%d", (int) Math.round(value));
			//} else {
				//return String.format("%.1f", value);
			//}
		} else if ((int) Math.round((value * 100f)) == (int) Math.round((value * 10f)) * 10) {
			return (value > 10 || value < -10) ? "" + (int) Math.round(value) : String.format("%.1f", value);
		} else {
			return (value > 10 || value < -10) ? "" + (int) Math.round(value) : String.format("%.2f", value);
		}
	}
	
	public static float getRoundedValueFloat(float value) {
		if (Math.abs((float)Math.round(value) - value) < 0.0001f) {
			return (int) Math.round(value);
		} else if ((int) Math.round((value * 100f)) == (int) Math.round((value * 10f)) * 10) {
			return (value > 10 || value < -10) ? (int) Math.round(value) :
						(Math.round(value * 10f) / 10f);
		} else {
			return (value > 10 || value < -10) ? (int) Math.round(value) :
						(Math.round(value * 100f) / 100f);
		}
	}
	
	public static String getRoundedValueMaxOneAfterDecimal(float value) {
		if (Math.abs((float)Math.round(value) - value) < 0.0001f) {
			return String.format("%d", (int) Math.round(value));
		} else if ((int) Math.round((value * 100f)) == (int) Math.round((value * 10f)) * 10) {
			return (value >= 10 || value <= -10) ? "" + (int) Math.round(value) : String.format("%.1f", value);
		} else {
			return (value >= 10 || value <= -10) ? "" + (int) Math.round(value) : String.format("%.1f", value);
		}
	}
	
	

	public static float logOfBase(float base, float num) {
	    return (float) (Math.log(num) / Math.log(base));
	}

	public static Vector2f getPointAtRadius(Vector2f from, float r) {
		float angle = (float) ((float) Math.random() * Math.PI * 2f);
		float x = (float) (Math.cos(angle) * r) + from.x;
		float y = (float) (Math.sin(angle) * r) + from.y;
		return new Vector2f(x, y);
	}
	
	public static Vector2f getPointAtRadius(Vector2f from, float r, Random random) {
		float angle = (float) (random.nextFloat() * Math.PI * 2f);
		float x = (float) (Math.cos(angle) * r) + from.x;
		float y = (float) (Math.sin(angle) * r) + from.y;
		return new Vector2f(x, y);
	}
	
	public static Vector2f getPointWithinRadius(Vector2f from, float r) {
		return getPointWithinRadius(from, r, new Random());
	}
	public static Vector2f getPointWithinRadius(Vector2f from, float r, Random random) {
		r = r * random.nextFloat();
		float angle = (float) (random.nextFloat() * Math.PI * 2f);
		float x = (float) (Math.cos(angle) * r) + from.x;
		float y = (float) (Math.sin(angle) * r) + from.y;
		return new Vector2f(x, y);
	}
	
	public static float getSnapshotFPLost(CampaignFleetAPI fleet) {
		float fp = fleet.getFleetPoints();
		float before = 0;
		for (FleetMemberAPI member : fleet.getFleetData().getSnapshot()) {
			before += member.getFleetPointCost();
		}
		
		return before - fp;
	}
	
	public static List<FleetMemberAPI> getSnapshotMembersLost(CampaignFleetAPI fleet) {
		List<FleetMemberAPI> lost = new ArrayList<FleetMemberAPI>();
		List<FleetMemberAPI> curr = fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI member : fleet.getFleetData().getSnapshot()) {
			if (!curr.contains(member)) {
				lost.add(member);
			}
		}
		
		return lost;
	}
	
	
	public static CampaignEventPlugin startEvent(CampaignEventTarget eventTarget, String eventId, Object params) {
		CampaignEventManagerAPI manager = Global.getSector().getEventManager();
		CampaignEventPlugin event = manager.getOngoingEvent(eventTarget, eventId);
		if (event == null) {
			event = manager.startEvent(eventTarget, eventId, params);
		}
		return event;
	}
	
	public static Color getStoryOptionColor() {
		//return Misc.interpolateColor(Misc.getButtonTextColor(), Misc.getPositiveHighlightColor(), 0.5f);
		return Global.getSettings().getColor("storyOptionColor");
	}
	
	public static Color getHighlightColor() {
		return Global.getSettings().getColor("buttonShortcut");
	}
	public static Color getTooltipTitleAndLightHighlightColor() {
		return Global.getSettings().getColor("tooltipTitleAndLightHighlightColor");
	}
	public static Color getNegativeHighlightColor() {
		if (Global.getSettings().getBoolean("colorblindMode")) {
			return new Color(0, 100, 255);
		}
		return Global.getSettings().getColor("textEnemyColor");
	}
	
	public static Color getBallisticMountColor() {
		return Global.getSettings().getColor("mountYellowColor");
	}
	public static Color getMissileMountColor() {
		return Global.getSettings().getColor("mountGreenColor");
	}
	public static Color getEnergyMountColor() {
		if (Global.getSettings().getBoolean("colorblindMode")) {
			return new Color(155,155,155,255);
		}
		return Global.getSettings().getColor("mountBlueColor");
	}
	
	public static Color getPositiveHighlightColor() {
		return Global.getSettings().getColor("textFriendColor");
	}
	
	public static Color getGrayColor() {
		return Global.getSettings().getColor("textGrayColor");
	}
	
	public static Color getBrightPlayerColor() {
		return Global.getSector().getPlayerFaction().getBrightUIColor();
	}
	public static Color getBasePlayerColor() {
		return Global.getSector().getPlayerFaction().getBaseUIColor();
	}
	public static Color getDarkPlayerColor() {
		return Global.getSector().getPlayerFaction().getDarkUIColor();
	}
	public static Color getTextColor() {
		return Global.getSettings().getColor("standardTextColor");
	}
	public static Color getButtonTextColor() {
		return Global.getSettings().getColor("buttonText");
	}
	
	public static float getUnitsPerLightYear() {
		return Global.getSettings().getFloat("unitsPerLightYear");
	}
	
	public static float getProfitMarginFlat() {
		return Global.getSettings().getFloat("profitMarginFlat");
	}
	
	public static float getProfitMarginMult() {
		return Global.getSettings().getFloat("profitMarginMult");
	}
	
	public static float getEconomyInterval() {
		return Global.getSettings().getFloat("economyIntervalnGameDays");
	}
	
	public static float getGenericRollingAverageFactor() {
		return Global.getSettings().getFloat("genericRollingAverageFactor");
	}
	
	public static IntervalUtil createEconIntervalTracker() {
		float interval = getEconomyInterval();
		return new IntervalUtil(interval * 0.75f, interval * 1.25f);
	}
	
	public static String getAndJoined(List<String> strings) {
		return getAndJoined(strings.toArray(new String [0]));
	}
	
	public static String getAndJoined(String ... strings) {
		if (strings.length == 1) return strings[0];
		
		String result = "";
		for (int i = 0; i < strings.length - 1; i++) {
			result += strings[i] + ", ";
		}
		if (!result.isEmpty()) {
			result = result.substring(0, result.length() - 2);
		}
		if (strings.length > 2) {
			result += ", and " + strings[strings.length - 1];
		} else if (strings.length == 2) {
			result += " and " + strings[strings.length - 1];
		}
		return result;
	}
	
	public static interface FleetFilter {
		boolean accept(CampaignFleetAPI curr);
	}
	
	public static List<CampaignFleetAPI> findNearbyFleets(SectorEntityToken from, float maxRange, FleetFilter filter) {
		List<CampaignFleetAPI> result = new ArrayList<CampaignFleetAPI>();
		for (CampaignFleetAPI fleet : from.getContainingLocation().getFleets()) {
			if (fleet == from) continue;
			float dist = Misc.getDistance(fleet.getLocation(), from.getLocation());
			if (dist > maxRange) continue;
			
			if (filter == null || filter.accept(fleet)) {
				result.add(fleet);
			}
		}
		return result;
	}
	
	public static List<CampaignFleetAPI> getFleetsInOrNearSystem(StarSystemAPI system) {
		List<CampaignFleetAPI> result = new ArrayList<CampaignFleetAPI>(system.getFleets());
		for (CampaignFleetAPI fleet : Global.getSector().getHyperspace().getFleets()) {
			if (!fleet.isInOrNearSystem(system)) continue;
			result.add(fleet);
		}
		return result;
	}
	
	
	public static List<MarketAPI> getMarketsInLocation(LocationAPI location, String factionId) {
		List<MarketAPI> result = new ArrayList<MarketAPI>();
		for (MarketAPI curr : getMarketsInLocation(location)) {
			if (curr.getFactionId().equals(factionId)) {
				result.add(curr);
			}
		}
		return result;
	}
	
	public static MarketAPI getBiggestMarketInLocation(LocationAPI location) {
		int max = 0;
		MarketAPI best = null;
		for (MarketAPI curr : getMarketsInLocation(location)) {
			int size = curr.getSize();
			if (size > max || (size == max && curr.getFaction().isPlayerFaction())) {
				max = size;
				best = curr;
			}
		}
		return best;
	}
	
	
	public static List<MarketAPI> getMarketsInLocation(LocationAPI location) {
		return Global.getSector().getEconomy().getMarkets(location);
//		List<MarketAPI> result = new ArrayList<MarketAPI>();
//		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
//			if (market.getContainingLocation() != location) continue;
//			result.add(market);
//		}
//		return result;
	}
	
	public static List<MarketAPI> getFactionMarkets(FactionAPI faction, String econGroup) {
		List<MarketAPI> result = new ArrayList<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsInGroup(econGroup)) {
			if (market.getFaction() == faction) {
				result.add(market);
			}
		}
		return result;
	}
	public static List<MarketAPI> getFactionMarkets(FactionAPI faction) {
		//Global.getSector().getEconomy().get
		List<MarketAPI> result = new ArrayList<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.getFaction() == faction) {
				result.add(market);
			}
		}
		return result;
	}
	
	public static List<MarketAPI> getNearbyMarkets(Vector2f locInHyper, float distLY) {
		List<MarketAPI> result = new ArrayList<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			float dist = getDistanceLY(market.getLocationInHyperspace(), locInHyper);
			if (dist > distLY) continue;
			result.add(market);
		}
		return result;
	}
	
	public static int getNumHostileMarkets(FactionAPI faction, SectorEntityToken from, float maxDist) {
		int hostileMarketsNearPoint = 0;
		for (MarketAPI market : Misc.getMarketsInLocation(from.getContainingLocation())) {
			SectorEntityToken primary = market.getPrimaryEntity();
			float dist = getDistance(primary.getLocation(), from.getLocation());
			if (dist > maxDist) continue;
			if (market.getFaction() != null && market.getFaction().isHostileTo(faction)) {
				hostileMarketsNearPoint ++;
			}
		}
		return hostileMarketsNearPoint;
	}
	
	public static List<StarSystemAPI> getNearbyStarSystems(SectorEntityToken token, float maxRangeLY) {
		List<StarSystemAPI> result = new ArrayList<StarSystemAPI>();
		
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			float dist = Misc.getDistanceLY(token.getLocationInHyperspace(), system.getLocation());
			if (dist > maxRangeLY) continue;
			result.add(system);
		}
		return result;
	}
	
	public static StarSystemAPI getNearbyStarSystem(SectorEntityToken token, float maxRangeLY) {
		if (token.getContainingLocation() instanceof StarSystemAPI) {
			return (StarSystemAPI) token.getContainingLocation();
		}
		
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			float dist = Misc.getDistanceLY(token.getLocationInHyperspace(), system.getLocation());
			if (dist > maxRangeLY) continue;
			return system;
		}
		return null;
	}
	
	public static StarSystemAPI getNearestStarSystem(SectorEntityToken token) {
		if (token.getContainingLocation() instanceof StarSystemAPI) {
			return (StarSystemAPI) token.getContainingLocation();
		}
		
		float minDist = Float.MAX_VALUE;
		StarSystemAPI closest = null;
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			float dist = Misc.getDistanceLY(token.getLocationInHyperspace(), system.getLocation());
			if (dist < minDist) {
				minDist = dist;
				closest = system;
			}
		}
		return closest;
	}
	
	public static StarSystemAPI getNearbyStarSystem(SectorEntityToken token) {
		if (token.getContainingLocation() instanceof StarSystemAPI) {
			return (StarSystemAPI) token.getContainingLocation();
		}
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (token.isInOrNearSystem(system)) return system;
		}
		return null;
	}
	
	
	public static boolean showRuleDialog(SectorEntityToken entity, String initialTrigger) {
		RuleBasedInteractionDialogPluginImpl plugin;
		if (initialTrigger != null) {
			plugin = new RuleBasedInteractionDialogPluginImpl(initialTrigger);
		} else {
			plugin = new RuleBasedInteractionDialogPluginImpl();
		}
		return Global.getSector().getCampaignUI().showInteractionDialog(plugin, entity);
	}
	
	public static float DEG_PER_RAD = 180f / 3.1415926f;
	
	public static float getAngleInDegreesStrict(Vector2f v) {
		float angle = (float) Math.atan2(v.y, v.x) * DEG_PER_RAD;
		return angle;
	}
	
	public static float getAngleInDegreesStrict(Vector2f from, Vector2f to) {
		float dx = to.x - from.x;
		float dy = to.y - from.y;
		float angle = (float) Math.atan2(dy, dx) * DEG_PER_RAD;
		return angle;
	}
	public static float getAngleInDegrees(Vector2f v) {
		return Global.getSettings().getAngleInDegreesFast(v);
	}
	
	public static float getAngleInDegrees(Vector2f from, Vector2f to) {
		return Global.getSettings().getAngleInDegreesFast(from, to);
	}
	
	public static Vector2f normalise(Vector2f v) {
		if (v.lengthSquared() > Float.MIN_VALUE) {
			return (Vector2f)v.normalise();
		}
		//return v;
		return new Vector2f(1, 0);
	}
	
	public static float normalizeAngle(float angleDeg) {
		return (angleDeg % 360f + 360f) % 360f;
	}
	
	public static MarketAPI findNearestLocalMarket(SectorEntityToken token, float maxDist, MarketFilter filter) {
		List<MarketAPI> localMarkets = getMarketsInLocation(token.getContainingLocation());
		float distToLocalMarket = Float.MAX_VALUE;
		MarketAPI closest = null;
		for (MarketAPI market : localMarkets) {
			if (filter != null && !filter.acceptMarket(market)) continue;
			
			if (market.getPrimaryEntity() == null) continue;
			if (market.getPrimaryEntity().getContainingLocation() != token.getContainingLocation()) continue;
			
			float currDist = Misc.getDistance(market.getPrimaryEntity().getLocation(), token.getLocation());
			if (currDist > maxDist) continue;
			if (currDist < distToLocalMarket) {
				distToLocalMarket = currDist;
				closest = market;
			}
		}
		return closest;
	}
	public static List<MarketAPI> findNearbyLocalMarkets(SectorEntityToken token, float maxDist, MarketFilter filter) {
		List<MarketAPI> localMarkets = getMarketsInLocation(token.getContainingLocation());
		List<MarketAPI> result = new ArrayList<MarketAPI>();
		
		for (MarketAPI market : localMarkets) {
			if (filter != null && !filter.acceptMarket(market)) continue;
			if (market.getPrimaryEntity() == null) continue;
			if (market.getPrimaryEntity().getContainingLocation() != token.getContainingLocation()) continue;
			
			float currDist = Misc.getDistance(market.getPrimaryEntity().getLocation(), token.getLocation());
			if (currDist > maxDist) continue;
			
			result.add(market);
			
		}
		return result;
	}
	
	public static MarketAPI findNearestLocalMarketWithSameFaction(final SectorEntityToken token, float maxDist) {
		return findNearestLocalMarket(token, maxDist, new MarketFilter() {
			public boolean acceptMarket(MarketAPI curr) {
				return curr.getFaction() == token.getFaction();
			}
		});
	}
	
	
	public static float RAD_PER_DEG = 0.01745329251f;
	public static Vector2f getUnitVectorAtDegreeAngle(float degrees) {
		Vector2f result = new Vector2f();
		float radians = degrees * RAD_PER_DEG;
		result.x = (float)Math.cos(radians);
		result.y = (float)Math.sin(radians);
		
		return result;
	}
	
	/**
	 * Angles.
	 * @param one
	 * @param two
	 * @param check
	 * @return
	 */
	public static boolean isBetween(float one, float two, float check) {
		one = normalizeAngle(one);
		two = normalizeAngle(two);
		check = normalizeAngle(check);

		//System.out.println(one + "," + two + "," + check);
		if (check >= one && check <= two) return true;
		
		if (one > two) {
			if (check <= two) return true;
			if (check >= one) return true;
		}
		return false;
	}
	
	public static float getShieldedCargoFraction(CampaignFleetAPI fleet) {
		float shielded = 0f;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.isMothballed()) continue;
			if (member.getVariant().hasHullMod(HullMods.SHIELDED_CARGO_HOLDS)) {
				shielded += member.getCargoCapacity();
			}
		}
		float max = fleet.getCargo().getMaxCapacity();
		if (max < 1) return 0f;
		return shielded / max;
	}
	
	
	public static Color interpolateColor(Color from, Color to, float progress) {
		float red = (float)from.getRed() + ((float)to.getRed() - (float)from.getRed()) * progress;
		float green = (float)from.getGreen() + ((float)to.getGreen() - (float)from.getGreen()) * progress;
		float blue = (float)from.getBlue() + ((float)to.getBlue() - (float)from.getBlue()) * progress;
		float alpha = (float)from.getAlpha() + ((float)to.getAlpha() - (float)from.getAlpha()) * progress;
		red = Math.round(red);
		green = Math.round(green);
		blue = Math.round(blue);
		alpha = Math.round(alpha);
		return new Color((int)red, (int)green, (int)blue, (int)alpha);
	}
	
	public static Vector2f interpolateVector(Vector2f from, Vector2f to, float progress) {
		Vector2f v = new Vector2f(from);
		
		v.x += (to.x - from.x) * progress;
		v.y += (to.y - from.y) * progress;
		
		return v;
	}
	
	public static float interpolate(float from, float to, float progress) {
		to = from + (to - from) * progress;
		return to;
	}
	
	public static Color scaleColor(Color color, float factor) {
		return new Color((int) (color.getRed() * factor),
						 (int) (color.getGreen() * factor),
						 (int) (color.getBlue() * factor),
						 (int) (color.getAlpha() * factor));
	}
	public static Color scaleColorOnly(Color color, float factor) {
		return new Color((int) (color.getRed() * factor),
						 (int) (color.getGreen() * factor),
						 (int) (color.getBlue() * factor),
						 (int) (color.getAlpha()));
	}
	
	public static Color scaleAlpha(Color color, float factor) {
		return new Color((int) (color.getRed() * 1f),
				(int) (color.getGreen() * 1f),
				(int) (color.getBlue() * 1f),
				(int) (color.getAlpha() * factor));
	}
	
	public static Color setAlpha(Color color, int alpha) {
		if (alpha < 0) alpha = 0;
		if (alpha > 255) alpha = 255;
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
	
	public static float getSizeNum(HullSize size) {
		if (size == null) {
			return 1;
		}
		switch (size) {
		case CAPITAL_SHIP:
			return 5;
		case CRUISER:
			return 3;
		case DESTROYER:
			return 2;
		case FIGHTER:
		case FRIGATE:
		case DEFAULT:
			return 1;
		}
		return 1;
	}
	
	public static void unsetAll(String prefix, String memKey, MemoryAPI memory) {
		Map<String, MemoryAPI> memoryMap = new HashMap<String, MemoryAPI>();
		memoryMap.put(memKey, memory);
		new unsetAll().execute(null, null, Misc.tokenize(prefix), memoryMap);
	}
	
	
	
	public static float getTargetingRadius(Vector2f from, CombatEntityAPI target, boolean considerShield) {
		return Global.getSettings().getTargetingRadius(from, target, considerShield);
	}
	
	
	public static float getClosingSpeed(Vector2f p1, Vector2f p2, Vector2f v1, Vector2f v2) {		
		// direction from target to shooter
		Vector2f dir = Vector2f.sub(p1, p2, new Vector2f());
		normalise(dir);
		// velocity of target relative to shooter
		Vector2f relVel = Vector2f.sub(v2, v1, new Vector2f());
		float closingSpeed = Vector2f.dot(dir, relVel);
		return closingSpeed;
	}
	
	
	protected static DecimalFormat format = null;
	public static DecimalFormat getFormat() {
		if (format == null) {
			DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
//			symbols.setDecimalSeparator('.');
//			symbols.setGroupingSeparator(','); 
			format = new DecimalFormat("###,###,###,###,###", symbols);
		}
		return format;
	}
	
	/**
	 * DGS = digit group separator, i.e.: 1000000 -> 1,000,000
	 * @param num
	 * @return
	 */
	public static String getWithDGS(float num) {
		return getFormat().format(num);
	}
	
	/**
	 * DGS = digit group separator, i.e.: 1000000 -> 1,000,000
	 * @param num
	 * @return
	 */
	public static String getDGSCredits(float num) {
		return getFormat().format((int)num) + Strings.C;
	}
	

	
	public static Vector2f getInterceptPointBasic(SectorEntityToken from, SectorEntityToken to) {
		float dist = getDistance(from.getLocation(), to.getLocation()) - from.getRadius() - to.getRadius();
		if (dist <= 0) return new Vector2f(to.getLocation());
		
		float closingSpeed = getClosingSpeed(from.getLocation(), to.getLocation(), from.getVelocity(), to.getVelocity());
		if (closingSpeed <= 10) return new Vector2f(to.getLocation());
		
		Vector2f toTarget = getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(from.getLocation(), to.getLocation()));
		Vector2f vel = new Vector2f(from.getVelocity());
		normalise(vel);
		float dot = Vector2f.dot(toTarget, vel);
		if (dot < 0) return new Vector2f(to.getLocation());
//		if (to.isPlayerFleet()) {
//			System.out.println("23rwefe");
//		}
		float time = dist / closingSpeed;
		
		Vector2f point = new Vector2f(to.getVelocity());
		point.scale(time);
		Vector2f.add(point, to.getLocation(), point);
		return point;
	}
	
	
	
	/**
	 * A flag can be set to true for several "reasons". As long as it hasn't been set
	 * back to false for all of the "reasons", it will remain set to true.
	 * 
	 * For example, a fleet may be hostile because it's responding to comm relay interference,
	 * and because the player is running with the transponder off. Until both are resolved,
	 * the "hostile" flag will remain set to true.
	 * 
	 * Note: a flag can not be "set" to false. If it's set to false for all the current reasons,
	 * the key is removed from memory. 
	 * 
	 * Returns whether the flag is still set after this method does its work.
	 * @param memory
	 * @param flagKey
	 * @param reason
	 * @param value
	 * @return
	 */
	public static boolean setFlagWithReason(MemoryAPI memory, String flagKey, String reason, boolean value, float expire) {
		String requiredKey = flagKey + "_" + reason;

		if (value) {
			memory.set(flagKey, true);
			memory.set(requiredKey, value, expire);
			memory.addRequired(flagKey, requiredKey);
		} else {
			memory.unset(requiredKey);
		}
		
		return memory.contains(flagKey);
	}
	
	public static boolean flagHasReason(MemoryAPI memory, String flagKey, String reason) {
		String requiredKey = flagKey + "_" + reason;
		
		return memory.getBoolean(requiredKey);
	}
	
	public static void clearFlag(MemoryAPI memory, String flagKey) {
		for (String req : memory.getRequired(flagKey)) {
			memory.unset(req);
		}
	}
	
	public static void makeLowRepImpact(CampaignFleetAPI fleet, String reason) {
		setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_LOW_REP_IMPACT, reason, true, -1);
	}
	public static void makeNoRepImpact(CampaignFleetAPI fleet, String reason) {
		setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_LOW_REP_IMPACT, reason, true, -1);
		setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_NO_REP_IMPACT, reason, true, -1);
	}
	
	public static void makeHostile(CampaignFleetAPI fleet) {
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
	}
	
	public static void makeNotLowRepImpact(CampaignFleetAPI fleet, String reason) {
		setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_LOW_REP_IMPACT, reason, false, -1);
		setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_NO_REP_IMPACT, reason, false, -1);
	}
	
	
	public static String getAgoStringForTimestamp(long timestamp) {
		CampaignClockAPI clock = Global.getSector().getClock();
		float days = clock.getElapsedDaysSince(timestamp);
		
		if (days <= 1f) {
			return "Today";
		} else if (days <= 6f) {
			return (int)Math.ceil(days) + " days ago";
		} else if (days <= 7) {
			return "1 week ago";
		} else if (days <= 14) {
			return "2 weeks ago";
		} else if (days <= 21) {
			return "3 weeks ago";
		} else if (days <= 30 + 14) {
			return "1 month ago";
		} else if (days < 30 * 2 + 14) {
			return "2 months ago";
		} else if (days < 30 * 3 + 14) {
			return "3 months ago";
		} else {
			return "Over 3 months ago";
		}
	}
	
	public static String getAtLeastStringForDays(int days) {
		if (days <= 1f) {
			return "at least a day";
		} else if (days <= 6f) {
			return "at least a few days";
		} else if (days <= 7 + 6) {
			return "at least a week";
		} else if (days <= 14 + 6) {
			return "at least two weeks";
		} else if (days <= 21 + 8) {
			return "at least three weeks";
		} else if (days <= 30 + 29) {
			return "at least a month";
		} else if (days < 30 * 2 + 29) {
			return "at least two months";
		} else if (days < 30 * 3 + 29) {
			return "at least three months";
		} else {
			return "many months";
		}
	}
	
	public static String getStringForDays(int days) {
		if (days <= 1f) {
			return "a day";
		} else if (days <= 6f) {
			return "a few days";
		} else if (days <= 7 + 6) {
			return "a week";
		} else if (days <= 14 + 6) {
			return "two weeks";
		} else if (days <= 21 + 8) {
			return "three weeks";
		} else if (days <= 30 + 29) {
			return "a month";
		} else if (days < 30 * 2 + 29) {
			return "two months";
		} else if (days < 30 * 3 + 29) {
			return "three months";
		} else {
			return "many months";
		}
	}
	
//	public static String getTimeStringForDays(int days) {
//		if (days <= 1f) {
//			return "1 day";
//		} else if (days <= 6f) {
//			return "at least a few days";
//		} else if (days <= 7) {
//			return "at least a week";
//		} else if (days <= 14) {
//			return "at least 2 weeks";
//		} else if (days <= 21) {
//			return "at least 3 weeks";
//		} else if (days <= 30 + 14) {
//			return "at least a month";
//		} else if (days < 30 * 2 + 14) {
//			return "at least 2 months";
//		} else if (days < 30 * 3 + 14) {
//			return "at least 3 months";
//		} else {
//			return "many months";
//		}
//	}
	
	public static float getBurnLevelForSpeed(float speed) {
		speed -= Global.getSettings().getBaseTravelSpeed();
		if (speed < 0 || speed <= Global.getSettings().getFloat("minTravelSpeed") + 1f) speed = 0;
		float currBurn = speed / Global.getSettings().getSpeedPerBurnLevel();
		//System.out.println("ADFSDF: " +Math.round(currBurn));
		return Math.round(currBurn + 0.01f);
	}
	
	public static float getFractionalBurnLevelForSpeed(float speed) {
//		System.out.println("Speed: " + Global.getSector().getPlayerFleet().getVelocity().length());
//		System.out.println("Max: " + Global.getSector().getPlayerFleet().getTravelSpeed());
		speed -= Global.getSettings().getBaseTravelSpeed();
		if (speed < 0 || speed <= Global.getSettings().getFloat("minTravelSpeed") + 1f) speed = 0;
		float currBurn = speed / Global.getSettings().getSpeedPerBurnLevel();
		//System.out.println("ADFSDF: " +Math.round(currBurn));
		return currBurn;
	}
	
	public static float getSpeedForBurnLevel(float burnLevel) {
		float speed = Global.getSettings().getBaseTravelSpeed() + burnLevel * Global.getSettings().getSpeedPerBurnLevel();
		return speed;
	}
	
	public static float getFuelPerDay(CampaignFleetAPI fleet, float burnLevel) {
		float speed = Global.getSettings().getBaseTravelSpeed() + Global.getSettings().getSpeedPerBurnLevel() * burnLevel;
		return getFuelPerDayAtSpeed(fleet, speed);
	}
	
	public static float getFuelPerDayAtSpeed(CampaignFleetAPI fleet, float speed) {
		float perLY = fleet.getLogistics().getFuelCostPerLightYear();

		// this is potentially evil - currently, the velocity is in units per SECOND, not per day
		speed = speed * Global.getSector().getClock().getSecondsPerDay();
		// now, speed is in units per day
		
		speed = speed / Global.getSettings().getUnitsPerLightYear();
		// ly/day now
		
		
		return speed * perLY;
	}
	
	public static float getLYPerDayAtBurn(CampaignFleetAPI fleet, float burnLevel) {
		float speed = Global.getSettings().getBaseTravelSpeed() + Global.getSettings().getSpeedPerBurnLevel() * burnLevel;
		return getLYPerDayAtSpeed(fleet, speed);
	}
	public static float getLYPerDayAtSpeed(CampaignFleetAPI fleet, float speed) {
		// this is potentially evil - currently, the velocity is in units per SECOND, not per day
		speed = speed * Global.getSector().getClock().getSecondsPerDay();
		// now, speed is in units per day
		speed = speed / Global.getSettings().getUnitsPerLightYear();
		// ly/day now
		
		return speed * 1f; // 1f days
	}
	
	private static Vector3f temp4 = new Vector3f();
	public static Color zeroColor = new Color(0,0,0,0);
	public static float getDistance(Vector3f v1, Vector3f v2)
	{
		return Vector3f.sub(v1, v2, temp4).length();
	}
	
	public static float getAngleDiff(float from, float to) {
		float diff = normalizeAngle(from - to);
		if (diff > 180) return 360 - diff;
		else return diff;
	}
	
	public static boolean isInArc(float direction, float arc, Vector2f from, Vector2f to) {
		direction = normalizeAngle(direction);
		if (arc >= 360) return true;
		if (direction < 0) direction = 360 + direction;
		Vector2f towardsTo = new Vector2f(to.x - from.x, to.y - from.y);
		if (towardsTo.lengthSquared() == 0) return false;
		float dir = Misc.getAngleInDegrees(towardsTo);
		if (dir < 0) dir = 360 + dir;
		float arcFrom = direction - arc/2f;
		if (arcFrom < 0) arcFrom = 360 + arcFrom;
		if (arcFrom > 360) arcFrom -= 360;
		float arcTo = direction + arc/2f;
		if (arcTo < 0) arcTo = 360 + arcTo;
		if (arcTo > 360) arcTo -= 360;
		
		if (dir >= arcFrom && dir <= arcTo) return true;
		if (dir >= arcFrom && arcFrom > arcTo) return true;
		if (dir <= arcTo && arcFrom > arcTo) return true;
		return false;
	}
	
	public static boolean isInArc(float direction, float arc, float test) {
		test = normalizeAngle(test);
		
		if (arc >= 360) return true;
		if (direction < 0) direction = 360 + direction;
		float dir = test;
		if (dir < 0) dir = 360 + dir;
		float arcFrom = direction - arc/2f;
		if (arcFrom < 0) arcFrom = 360 + arcFrom;
		if (arcFrom > 360) arcFrom -= 360;
		float arcTo = direction + arc/2f;
		if (arcTo < 0) arcTo = 360 + arcTo;
		if (arcTo > 360) arcTo -= 360;
		
		if (dir >= arcFrom && dir <= arcTo) return true;
		if (dir >= arcFrom && arcFrom > arcTo) return true;
		if (dir <= arcTo && arcFrom > arcTo) return true;
		return false;
	}
	
	
	public static SectorEntityToken addNebulaFromPNG(String image, float centerX, float centerY, LocationAPI location,
			String category, String key, int tilesWide, int tilesHigh, StarAge age) {
		return addNebulaFromPNG(image, centerX, centerY, location, category, key, tilesWide, tilesHigh, Terrain.NEBULA, age);
	}
	
	
	public static SectorEntityToken addNebulaFromPNG(String image, float centerX, float centerY, LocationAPI location,
										String category, String key, int tilesWide, int tilesHigh,
										String terrainType, StarAge age) {
		try {
			BufferedImage img = null;
		    //img = ImageIO.read(new File("../starfarer.res/res/data/campaign/terrain/nebula_test.png"));
		    img = ImageIO.read(Global.getSettings().openStream(image));
		    
		    int chunkSize = 10000;
		    int w = img.getWidth();
		    int h = img.getHeight();
		    Raster data = img.getData();
		    for (int i = 0; i < w; i += chunkSize) {
		    	for (int j = 0; j < h; j += chunkSize) {
		    		
		    		int chunkWidth = chunkSize;
		    		if (i + chunkSize > w) chunkWidth = w - i;
		    		int chunkHeight = chunkSize;
		    		if (j + chunkSize > h) chunkHeight = h - i;
		    		
//		    		boolean hasAny = false;
//		    		for (int x = i; x < i + chunkWidth; x++) {
//		    			for (int y = j; y < j + chunkHeight; y++) {
//		    				int [] pixel = data.getPixel(i, h - j - 1, (int []) null);
//		    				int total = pixel[0] + pixel[1] + pixel[2];
//		    				if (total > 0) {
//		    					hasAny = true;
//		    					break;
//		    				}
//		    			}
//		    		}
//		    		if (!hasAny) continue;
		    		
		    		StringBuilder string = new StringBuilder();
		    		for (int y = j + chunkHeight - 1; y >= j; y--) {
		    			for (int x = i; x < i + chunkWidth; x++) {
		    				int [] pixel = data.getPixel(x, h - y - 1, (int []) null);
		    				int total = pixel[0] + pixel[1] + pixel[2];
		    				if (total > 0) {
		    					string.append("x");
		    				} else {
		    					string.append(" ");
		    				}
		    			}
		    		}
		    		
		    		float tileSize = NebulaTerrainPlugin.TILE_SIZE;
		    		float x = centerX - tileSize * (float) w / 2f + (float) i * tileSize + chunkWidth / 2f * tileSize;
		    		float y = centerY - tileSize * (float) h / 2f + (float) j * tileSize + chunkHeight / 2f * tileSize;
		    		
		    		SectorEntityToken curr = location.addTerrain(terrainType, new TileParams(string.toString(),
		    							chunkWidth, chunkHeight,
		    							category, key, tilesWide, tilesHigh, null));
		    		curr.getLocation().set(x, y);
		    		
		    		if (location instanceof StarSystemAPI) {
						StarSystemAPI system = (StarSystemAPI) location;
						
						system.setAge(age);
						system.setHasSystemwideNebula(true);
		    		}
		    		
		    		return curr;
		    	}
		    }
		    return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public static void renderQuad(float x, float y, float width, float height, Color color, float alphaMult) {
		GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)((float)color.getAlpha() * alphaMult));
		
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glVertex2f(x, y);
			GL11.glVertex2f(x, y + height);
			GL11.glVertex2f(x + width, y + height);
			GL11.glVertex2f(x + width, y);
		}
		GL11.glEnd();
	}
	
	/**
	 * Shortest distance from line to a point.
	 * @param p1 line1
	 * @param p2 line2
	 * @param p3 point
	 * @return
	 */
	public static float distanceFromLineToPoint(Vector2f p1, Vector2f p2, Vector2f p3) {
		float u = (p3.x - p1.x) * (p2.x - p1.x) + (p3.y - p1.y) * (p2.y - p1.y);
		float denom = Vector2f.sub(p2, p1, new Vector2f()).length();
		denom *= denom;
		//if (denom == 0) return 0;
		u /= denom;
		Vector2f i = new Vector2f();
		i.x = p1.x + u * (p2.x - p1.x);
		i.y = p1.y + u * (p2.y - p1.y);
		return Vector2f.sub(i, p3, new Vector2f()).length();
	}
	
	public static Vector2f closestPointOnLineToPoint(Vector2f p1, Vector2f p2, Vector2f p3) {
		float u = (p3.x - p1.x) * (p2.x - p1.x) + (p3.y - p1.y) * (p2.y - p1.y);
		float denom = Vector2f.sub(p2, p1, new Vector2f()).length();
		denom *= denom;
		//if (denom == 0) return 0;
		u /= denom;
		Vector2f i = new Vector2f();
		i.x = p1.x + u * (p2.x - p1.x);
		i.y = p1.y + u * (p2.y - p1.y);
		return i;
	}
	
	public static Vector2f closestPointOnSegmentToPoint(Vector2f p1, Vector2f p2, Vector2f p3) {
		float u = (p3.x - p1.x) * (p2.x - p1.x) + (p3.y - p1.y) * (p2.y - p1.y);
		float denom = Vector2f.sub(p2, p1, new Vector2f()).length();
		denom *= denom;
		
		u /= denom;
		
		// if closest point on line is outside the segment, clamp to on the segment
		if (u < 0) u = 0;
		if (u > 1) u = 1;
		
		Vector2f i = new Vector2f();
		i.x = p1.x + u * (p2.x - p1.x);
		i.y = p1.y + u * (p2.y - p1.y);
		return i;
	}
	
	public static boolean areSegmentsCoincident(Vector2f a1, Vector2f a2, Vector2f b1, Vector2f b2) {
		float denom = (b2.y - b1.y) * (a2.x - a1.x) - (b2.x - b1.x) * (a2.y - a1.y);
		float numUa = (b2.x - b1.x) * (a1.y - b1.y) - (b2.y - b1.y) * (a1.x - b1.x);
		float numUb = (a2.x - a1.x) * (a1.y - b1.y) - (a2.y - a1.y) *(a1.x - b1.x);

		if (denom == 0 && !(numUa == 0 && numUb == 0)) { // parallel, not coincident
			return false;
		}

		if (denom == 0 && numUa == 0 && numUb == 0) { // coincident
			return true;
		}
		
		return false;
	}
	
	public static Vector2f getPerp(Vector2f v) {
		Vector2f perp = new Vector2f();
		perp.x = v.y;
		perp.y = -v.x;
		return perp;
	}
	
	public static float getClosestTurnDirection(float facing, Vector2f from, Vector2f to) {
		Vector2f desired = getDiff(to, from);
		if (desired.lengthSquared() == 0) return 0;
		//float angle = getAngleInDegrees(desired);
		Vector2f more = getUnitVectorAtDegreeAngle(facing + 1);
		Vector2f less = getUnitVectorAtDegreeAngle(facing - 1);
		
		float fromMore = Vector2f.angle(more, desired);
		float fromLess = Vector2f.angle(less, desired);
		if (fromMore == fromLess) return 0f;
		if (fromMore > fromLess) return -1f;
		return 1f;
	}
	
	public static float getClosestTurnDirection(Vector2f one, Vector2f two) {
		return getClosestTurnDirection(getAngleInDegrees(one), new Vector2f(0, 0), two);
	}
	
	public static Vector2f getDiff(Vector2f v1, Vector2f v2)
	{
		//Vector2f result = new Vector2f();
		return Vector2f.sub(v1, v2, new Vector2f());
	}
	
	public static MarketAPI getSourceMarket(CampaignFleetAPI fleet) {
		String id = fleet.getMemoryWithoutUpdate().getString(MemFlags.MEMORY_KEY_SOURCE_MARKET);
		if (id == null) return null;
		MarketAPI market = Global.getSector().getEconomy().getMarket(id);
		return market;
	}
	
	public static float getSpawnChanceMult(Vector2f locInHyper) {
		if (Global.getSector().getPlayerFleet() == null) return 1f;
		
		float min = Global.getSettings().getFloat("minFleetSpawnChanceMult");
		float range = Global.getSettings().getFloat("minFleetSpawnChanceRangeLY");
		
		Vector2f playerLoc = Global.getSector().getPlayerFleet().getLocationInHyperspace();
		float distLY = getDistanceLY(playerLoc, locInHyper);
		
		float f = (1f - Math.min(1f, distLY/range));
		return min + (1f - min) * f * f;
	}
	
	public static Vector2f pickHyperLocationNotNearPlayer(Vector2f from, float minDist) {
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		float r = 2000f;
		if (player == null || !player.isInHyperspace()) {
			return getPointWithinRadius(from, r);
		}
		float dist = Misc.getDistance(player.getLocation(), from);
		if (dist > minDist + r) {
			return getPointWithinRadius(from, r);
		}
		float dir = Misc.getAngleInDegrees(player.getLocation(), from);
		Vector2f v = Misc.getUnitVectorAtDegreeAngle(dir);
		v.scale(minDist + 2000 - dist);
		Vector2f.add(v, from, v);
		return getPointWithinRadius(v, r);
	}
	
	public static Vector2f pickLocationNotNearPlayer(LocationAPI where, Vector2f from, float minDist) {
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		float r = 2000f;
		if (player == null || player.getContainingLocation() != where) {
			return getPointWithinRadius(from, r);
		}
		float dist = Misc.getDistance(player.getLocation(), from);
		if (dist > minDist + r) {
			return getPointWithinRadius(from, r);
		}
		float dir = Misc.getAngleInDegrees(player.getLocation(), from);
		Vector2f v = Misc.getUnitVectorAtDegreeAngle(dir);
		v.scale(minDist + 2000 - dist);
		Vector2f.add(v, from, v);
		return getPointWithinRadius(v, r);
	}
	
	public static float getBattleJoinRange() {
		return Global.getSettings().getFloat("battleJoinRange");
	}
	
	public static void wiggle(Vector2f v, float max) {
		v.x += (max * 2f * ((float) Math.random() - 0.5f));
		v.y += (max * 2f * ((float) Math.random() - 0.5f));
	}
	
	
	public static boolean isPlayerOrCombinedPlayerPrimary(CampaignFleetAPI fleet) {
		if (fleet.isPlayerFleet()) return true;
		
		if (fleet.getBattle() != null && fleet.getBattle().isOnPlayerSide(fleet) &&
				fleet.getBattle().isPlayerPrimary()) {
					return true;
		}
		return false;
	}
	
	public static boolean isPlayerOrCombinedContainingPlayer(CampaignFleetAPI fleet) {
		if (fleet.isPlayerFleet()) return true;
		
		if (fleet.getBattle() != null && fleet.getBattle().isOnPlayerSide(fleet)) {
			return true;
		}
		return false;
	}

	public static final String ASTEROID_SOURCE = "misc_astrdSource";
	
	public static AsteroidSource getAsteroidSource(SectorEntityToken asteroid) {
		if (asteroid.getCustomData().containsKey(ASTEROID_SOURCE)) {
			return (AsteroidSource) asteroid.getCustomData().get(ASTEROID_SOURCE);
		}
		return null;
	}
	public static void setAsteroidSource(SectorEntityToken asteroid, AsteroidSource source) {
		asteroid.getCustomData().put(ASTEROID_SOURCE, source);
	}
	public static void clearAsteroidSource(SectorEntityToken asteroid) {
		asteroid.getCustomData().remove(ASTEROID_SOURCE);
	}
	
	public static boolean isFastStart() {
		return Global.getSector().getMemoryWithoutUpdate().getBoolean("$fastStart");
	}
	public static boolean isFastStartExplorer() {
		return Global.getSector().getMemoryWithoutUpdate().getBoolean("$fastStartExplorer");
	}
	public static boolean isFastStartMerc() {
		return Global.getSector().getMemoryWithoutUpdate().getBoolean("$fastStartMerc");
	}
	
	public static boolean isEasy() {
		return Difficulties.EASY.equals(Global.getSector().getDifficulty());
	}
	
	public static boolean isNormal() {
		return Difficulties.NORMAL.equals(Global.getSector().getDifficulty());
	}
	
	
	public static void main(String[] args) {
//		System.out.println("TEST");
//		String s = "te\\\"st==$global.one $player.a $>=$sdfdsf\"++++dfdsf0---\"\"quoted \\\"=+!<>param\" one two 1234 1  2342 24  \t\t234234";
//		//String s = "menuState sdfsdf sdfsdf \"\"= \"main \\\" 1234\"";
//		//s = "!sdfsdAKJKFHD";
//		List<Token> result = tokenize(s);
//		for (Token str : result) {
//			System.out.println(str);
//		}
		//addNebulaFromPNG();
//		String s = "ffff0000";
//		System.out.println(Long.parseLong(s, 16));
		//System.out.println(Long.parseLong("aa0F245C", 16));
	}

	public static CampaignTerrainAPI getHyperspaceTerrain() {
		for (CampaignTerrainAPI curr : Global.getSector().getHyperspace().getTerrainCopy()) {
			if (curr.getPlugin() instanceof HyperspaceTerrainPlugin) {
				return curr;
			}
		}
		return null;
	}
	
	public static StarCoronaTerrainPlugin getCoronaFor(PlanetAPI star) {
		for (CampaignTerrainAPI curr : star.getContainingLocation().getTerrainCopy()) {
			if (curr.getPlugin() instanceof StarCoronaTerrainPlugin) {
				StarCoronaTerrainPlugin corona = (StarCoronaTerrainPlugin) curr.getPlugin();
				if (corona.getRelatedEntity() == star) return corona;
			}
		}
		return null;
	}
	
	public static PulsarBeamTerrainPlugin getPulsarFor(PlanetAPI star) {
		for (CampaignTerrainAPI curr : star.getContainingLocation().getTerrainCopy()) {
			if (curr.getPlugin() instanceof PulsarBeamTerrainPlugin) {
				PulsarBeamTerrainPlugin corona = (PulsarBeamTerrainPlugin) curr.getPlugin();
				if (corona.getRelatedEntity() == star) return corona;
			}
		}
		return null;
	}
	
	public static boolean hasPulsar(StarSystemAPI system) {
		return system != null && system.hasPulsar();
//		if (system.getStar() != null && system.getStar().getSpec().isPulsar()) return true;
//		if (system.getSecondary() != null && system.getSecondary().getSpec().isPulsar()) return true;
//		if (system.getTertiary() != null && system.getTertiary().getSpec().isPulsar()) return true;
//		return false;
	}
	
	public static String getCommissionFactionId() {
		String str = Global.getSector().getCharacterData().getMemoryWithoutUpdate().getString(MemFlags.FCM_FACTION);
		return str;
	}
	
	public static FactionAPI getCommissionFaction() {
		String id = getCommissionFactionId();
		if (id != null) {
			return Global.getSector().getFaction(id);
		}
		return null;
	}
	
	public static FactionCommissionIntel getCommissionIntel() {
		Object obj = Global.getSector().getCharacterData().getMemoryWithoutUpdate().get(MemFlags.FCM_EVENT);
		if (obj instanceof FactionCommissionIntel) {
			return (FactionCommissionIntel) obj;
		}
		return null;
	}
	
	
	public static boolean caresAboutPlayerTransponder(CampaignFleetAPI fleet) {
//		if (fleet.isInCurrentLocation()) {
//			System.out.println("efwefew");
//		}
		if (fleet.getFaction().isPlayerFaction()) return false;
		
		boolean caresAboutTransponder = true;
		if (fleet.getFaction().getCustomBoolean(Factions.CUSTOM_ALLOWS_TRANSPONDER_OFF_TRADE)) {
			caresAboutTransponder = false;
		}
		MarketAPI source = Misc.getSourceMarket(fleet);
		if (source != null && source.hasCondition(Conditions.FREE_PORT)) {
			caresAboutTransponder = false;
		}
		
		if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_PATROL_ALLOW_TOFF)) {
			caresAboutTransponder = false;
		}
		
		if (caresAboutTransponder && source != null && source.getPrimaryEntity() != null) {
			final CampaignFleetAPI player = Global.getSector().getPlayerFleet();
			if (player == null || player.isInHyperspace()) {
				caresAboutTransponder = false;
			} else {
				caresAboutTransponder = source.getPrimaryEntity().getContainingLocation() == player.getContainingLocation();
//				boolean alreadyTargetingPlayer = false;
//				if (fleet.getAI() instanceof ModularFleetAIAPI) {
//					ModularFleetAIAPI ai = (ModularFleetAIAPI) fleet.getAI();
//					SectorEntityToken target = ai.getTacticalModule().getTarget();
//					alreadyTargetingPlayer = target == player;
//				}
//				if (!alreadyTargetingPlayer) {
////					float max = Global.getSettings().getFloat("maxTransponderRequiredRangeAroundMarket");
////					float dist = getDistance(player.getLocation(), source.getPrimaryEntity().getLocation());
//					float max = Global.getSettings().getFloat("maxTransponderRequiredRangeAroundMarketSystem");
//					float dist = getDistanceLY(player.getLocationInHyperspace(), source.getLocationInHyperspace());
//					if (dist > max) {
//						caresAboutTransponder = false;
//					}
//				}
			}
		}
		
		return caresAboutTransponder;
	}
	

	public static interface FindShipFilter {
		public boolean matches(ShipAPI ship);
	}
	
	public static ShipAPI findClosestShipEnemyOf(ShipAPI ship, Vector2f locFromForSorting, HullSize smallestToNote, float maxRange, boolean considerShipRadius) {
		return findClosestShipEnemyOf(ship, locFromForSorting, smallestToNote, maxRange, considerShipRadius, null);
	}
	public static ShipAPI findClosestShipEnemyOf(ShipAPI ship, Vector2f locFromForSorting, HullSize smallestToNote, float maxRange, boolean considerShipRadius, FindShipFilter filter) {
		CombatEngineAPI engine = Global.getCombatEngine();
		List<ShipAPI> ships = engine.getShips();
		float minDist = Float.MAX_VALUE;
		ShipAPI closest = null;
		for (ShipAPI other : ships) {
			if (other.getHullSize().ordinal() < smallestToNote.ordinal()) continue;
			if (other.isShuttlePod()) continue;
			if (other.isHulk()) continue;
			if (ship.getOwner() != other.getOwner() && other.getOwner() != 100) {
				if (filter != null && !filter.matches(other)) continue;
				
				float dist = getDistance(ship.getLocation(), other.getLocation());
				float distSort = getDistance(locFromForSorting, other.getLocation());
				float radSum = ship.getCollisionRadius() + other.getCollisionRadius();
				if (!considerShipRadius) radSum = 0;
				if (dist > maxRange + radSum) continue;
				if (distSort < minDist) {
					closest = other;
					minDist = distSort;
				}
			}
		}
		return closest;
	}
	
	
	public static <T extends Enum<T>> T mapToEnum(JSONObject json, String key, Class<T> enumType, T defaultOption) throws JSONException {
		String val = json.optString(key);
		if (val == null || val.equals("")) {
			if (defaultOption == null) {
				throw new RuntimeException("Key [" + key + "] is required");
			}
			return defaultOption;
		}
		try {
			return (T) Enum.valueOf(enumType, val);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Key [" + key + "] has invalid value [" + val + "] in [" + json.toString() + "]");
		}
	}

	public static Color getColor(JSONObject json, String key) throws JSONException {
		if (!json.has(key)) return Color.white;
		JSONArray arr = json.getJSONArray(key);
		return new Color(arr.getInt(0), arr.getInt(1), arr.getInt(2), arr.getInt(3));
	}
	
	public static Color optColor(JSONObject json, String key, Color defaultValue) throws JSONException {
		if (!json.has(key)) return defaultValue;
		JSONArray arr = json.getJSONArray(key);
		return new Color(arr.getInt(0), arr.getInt(1), arr.getInt(2), arr.getInt(3));
	}
	
	
	public static void normalizeNoise(float[][] noise) {
		float minNoise = 1;
		float maxNoise = 0;
		for (int i = 0; i < noise.length; i++) {
			for (int j = 0; j < noise[0].length; j++) {
				if (noise[i][j] != -1) {
					if (noise[i][j] > maxNoise)
						maxNoise = noise[i][j];
					if (noise[i][j] < minNoise)
						minNoise = noise[i][j];
				}
			}
		}
		
		if (minNoise >= maxNoise) return;

		float range = maxNoise - minNoise;

		for (int i = 0; i < noise.length; i++) {
			for (int j = 0; j < noise[0].length; j++) {
				if (noise[i][j] != -1) {
					float newNoise = (noise[i][j] - minNoise) / range;
					noise[i][j] = newNoise;
				} else {
					if (i > 0)
						noise[i][j] = noise[i - 1][j];
					else if (i < noise.length - 1)
						noise[i][j] = noise[i + 1][j];
					else
						noise[i][j] = .5f;
				}
			}
		}
	}

	public static float [][] initNoise(Random random, int w, int h, float spikes) {
		if (random == null) random = new Random();
		float [][] noise = new float [w][h];
		for (int i = 0; i < noise.length; i++) {
			for (int j = 0; j < noise[0].length; j++) {
				noise[i][j] = -1f;
			}
		}
		noise[0][0] = random.nextFloat() * spikes;
		noise[0][noise[0].length - 1] = random.nextFloat() * spikes;
		noise[noise.length - 1][0] = random.nextFloat() * spikes;
		noise[noise.length - 1][noise[0].length - 1] = random.nextFloat() * spikes;
		return noise;
	}
	
	public static void genFractalNoise(Random random, float[][] noise, int x1, int y1,
			int x2, int y2, int iter, float spikes) {
		if (x1 + 1 >= x2 || y1 + 1 >= y2) return; // no more values to fill

		int midX = (x1 + x2) / 2;
		int midY = (y1 + y2) / 2;

		fill(random, noise, midX, y1, x1, y1, x2, y1, iter, spikes);
		fill(random, noise, midX, y2, x1, y2, x2, y2, iter, spikes);
		fill(random, noise, x1, midY, x1, y1, x1, y2, iter, spikes);
		fill(random, noise, x2, midY, x2, y1, x2, y2, iter, spikes);
		
		// averaging 4 neighboring values
		fill(random, noise, midX, midY, midX, y1, midX, y2, iter, spikes);
		float midValue1 = noise[midX][midY];
		fill(random, noise, midX, midY, x1, midY, x2, midY, iter, spikes);
		float midValue2 = noise[midX][midY];
		noise[midX][midY] = (midValue1 + midValue2)/2f;

		genFractalNoise(random, noise, x1, y1, midX, midY, iter + 1, spikes);
		genFractalNoise(random, noise, x1, midY, midX, y2, iter + 1, spikes);
		genFractalNoise(random, noise, midX, y1, x2, midY, iter + 1, spikes);
		genFractalNoise(random, noise, midX, midY, x2, y2, iter + 1, spikes);
	}

	private static void fill(Random random, float[][] noise, int x, int y, int x1, int y1,
			int x2, int y2, int iter, float spikes) {
		if (noise[x][y] == -1) {
			float avg = (noise[x1][y1] + noise[x2][y2]) / 2f;
			noise[x][y] = avg + ((float) Math.pow(spikes, (iter)) * (float) (random.nextFloat() - .5));
		}
	}
	
	
	public static float getClosestTurnDirection(float facing, float desired) {
		facing = normalizeAngle(facing);
		desired = normalizeAngle(desired);
		if (facing == desired) return 0;
		
		Vector2f desiredVec = getUnitVectorAtDegreeAngle(desired);
		//if (desiredVec.lengthSquared() == 0) return 0;
		Vector2f more = getUnitVectorAtDegreeAngle(facing + 1);
		Vector2f less = getUnitVectorAtDegreeAngle(facing - 1);
		
		float fromMore = Vector2f.angle(more, desiredVec);
		float fromLess = Vector2f.angle(less, desiredVec);
		if (fromMore > fromLess) return -1f;
		return 1f;
	}
	
	public static float computeAngleSpan(float radius, float range) {
		if (range <= 1) return 180f;
		return (2f * radius) / (2f * (float) Math.PI * range) * 360f;
	}
	
	public static float computeAngleRadius(float angle, float range) {
		float rad = (float) Math.toRadians(angle);
		return rad * range;
	}
	
	public static float approach(float curr, float dest, float minSpeed,  float diffSpeedMult, float amount) {
		float diff = dest - curr;
		float delta = (Math.signum(diff) * minSpeed + (diff * diffSpeedMult)) * amount;
		
		if (Math.abs(delta) > Math.abs(diff)) delta = diff;
		return curr + delta;
	}
	
	public static void cleanBuffer(Buffer toBeDestroyed) {
    	try {
    		if (toBeDestroyed instanceof DirectBuffer) {
    			Cleaner cleaner = ((DirectBuffer) toBeDestroyed).cleaner();
    			if (cleaner != null) cleaner.clean();
    			Global.getLogger(Misc.class).info(String.format("Cleaned buffer (using cast)"));
    			return;
    		} else {
    			
    		}
    		
	    	Method cleanerMethod = toBeDestroyed.getClass().getMethod("cleaner");
	    	cleanerMethod.setAccessible(true);
	    	Object cleaner = cleanerMethod.invoke(toBeDestroyed);
	    	if (cleaner != null) {
	    		Method cleanMethod = cleaner.getClass().getMethod("clean");
	    		cleanMethod.setAccessible(true);
	    		cleanMethod.invoke(cleaner);
	    		Global.getLogger(Misc.class).info(String.format("Cleaned buffer (using reflection)"));
	    	} else {
	    		Global.getLogger(Misc.class).warn(String.format("Buffer can not be cleaned"));
	    	}
    	} catch (Exception e) {
    		Global.getLogger(Misc.class).warn(e.getMessage(), e);
    	}
    }
	
	
	public static float getFleetwideTotalStat(CampaignFleetAPI fleet, String dynamicMemberStatId) {
		float total = 0;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.isMothballed()) continue;
			total += member.getStats().getDynamic().getValue(dynamicMemberStatId);
		}
		return total;
	}
	
	public static float getFleetwideTotalMod(CampaignFleetAPI fleet, String dynamicMemberStatId, float base) {
		float total = 0;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.isMothballed()) continue;
			total += member.getStats().getDynamic().getValue(dynamicMemberStatId, base);
		}
		return total;
	}
	
	public static String getStarId(PlanetAPI planet) {
		String starId = planet.getContainingLocation().getId();
		if (planet.getContainingLocation() instanceof StarSystemAPI) {
			StarSystemAPI system = (StarSystemAPI) planet.getContainingLocation();
			if (system.getStar() != null) {
				starId = system.getStar().getId();
			}
		}
		if (planet.getOrbitFocus() instanceof PlanetAPI) {
			PlanetAPI parent = (PlanetAPI) planet.getOrbitFocus();
			if (parent.isStar()) {
				starId = parent.getId();
			} else {
				if (parent.getOrbitFocus() instanceof PlanetAPI) {
					parent = (PlanetAPI) parent.getOrbitFocus();
					if (parent.isStar()) {
						starId = parent.getId();
					}
				}
			}
		}
		return starId;
	}
	
	
//	public static enum PlanetDataForSystem {
//		NONE,
//		SEEN,
//		PRELIMINARY,
//		//PARTIAL,
//		FULL,
//	}
	
	public static SurveyLevel getMinSystemSurveyLevel(StarSystemAPI system) {
		//boolean some = false, all = true;
		SurveyLevel minLevel = SurveyLevel.FULL;
		boolean empty = true;
		for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isStar()) continue;
			MarketAPI market = planet.getMarket();
			if (market == null) continue;
			
			empty = false;
			SurveyLevel level = market.getSurveyLevel();
			if (level.ordinal() < minLevel.ordinal()) {
				minLevel = level;
			}
		}
		
		if (!system.isEnteredByPlayer() && empty) minLevel = SurveyLevel.NONE;
		if (system.isEnteredByPlayer() && empty) minLevel = SurveyLevel.FULL;
		
		return minLevel;
		
			
//		if (all && system.isEnteredByPlayer()) return PlanetDataForSystem.FULL;
//		if (some) return PlanetDataForSystem.PARTIAL;
//		return PlanetDataForSystem.NONE;
	}
	
	public static boolean hasAnySurveyDataFor(StarSystemAPI system) {
		for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isStar()) continue;
			MarketAPI market = planet.getMarket();
			if (market == null) continue;
			
			SurveyLevel level = market.getSurveyLevel();
			if (level != SurveyLevel.NONE) return true;
		}
		return false;
	}
	
	
	
	public static void setAllPlanetsKnown(String systemName) {
		StarSystemAPI system = Global.getSector().getStarSystem(systemName);
		if (system != null) {
			setAllPlanetsKnown(system);
		} else {
			throw new RuntimeException("Star system [" + systemName + "] not found");
		}
	}
	
	public static void setAllPlanetsKnown(StarSystemAPI system) {
		for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isStar()) continue;
			
			MarketAPI market = planet.getMarket();
			if (market == null) continue;
			if (!market.isPlanetConditionMarketOnly()) {
				market.setSurveyLevel(SurveyLevel.FULL);
			} else if (market.getSurveyLevel() == SurveyLevel.NONE) {
				market.setSurveyLevel(SurveyLevel.SEEN);
			}
		}
	}
	
	public static void setAllPlanetsSurveyed(StarSystemAPI system) {
		for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isStar()) continue;
			
			MarketAPI market = planet.getMarket();
			if (market == null) continue;
			
			market.setSurveyLevel(SurveyLevel.FULL);
			for (MarketConditionAPI mc : market.getConditions()) {
				mc.setSurveyed(true);
			}
		}
	}
	
	public static void generatePlanetConditions(String systemName, StarAge age) {
		StarSystemAPI system = Global.getSector().getStarSystem(systemName);
		if (system != null) {
			generatePlanetConditions(system, age);
		} else {
			throw new RuntimeException("Star system [" + systemName + "] not found");
		}
	}
	
	public static void generatePlanetConditions(StarSystemAPI system, StarAge age) {
		for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isStar()) continue;
			
			if (planet.getMarket() != null && !planet.getMarket().getConditions().isEmpty()) continue;
			
			PlanetConditionGenerator.generateConditionsForPlanet(planet, age);
		}
	}
	
	
	public static int getEstimatedOrbitIndex(PlanetAPI planet) {
		Vector2f centerLoc = new Vector2f();
		float centerRadius = 0;

//		if (planet.getId().toLowerCase().equals("asharu")) {
//			System.out.println("sdfwefe");
//		}
		
		float planetRadius = planet.getRadius();
		PlanetAPI parent = null;
		PlanetAPI parentParent = null;
		if (planet.getOrbitFocus() instanceof PlanetAPI) {
			parent = (PlanetAPI) planet.getOrbitFocus();
			if (parent.getOrbitFocus() instanceof PlanetAPI) {
				parentParent = (PlanetAPI) parent.getOrbitFocus();
			}
			if (parent.isStar()) {
				centerLoc = parent.getLocation();
				centerRadius = parent.getRadius();
			} else if (parentParent != null && parentParent.isStar()) {
				centerLoc = parentParent.getLocation();
				centerRadius = parentParent.getRadius();
				planetRadius = parent.getRadius();
			}
		}
		
		float approximateExtraRadiusPerOrbit = 400f; 
		
		float dist = Misc.getDistance(centerLoc, planet.getLocation());
		int orbitIndex = (int) ((dist - centerRadius - planetRadius - 
								StarSystemGenerator.STARTING_RADIUS_STAR_BASE - StarSystemGenerator.STARTING_RADIUS_STAR_RANGE * 0.5f) /
									(StarSystemGenerator.BASE_INCR * 1.25f + approximateExtraRadiusPerOrbit));
		if (orbitIndex == 0) {
			orbitIndex = (int) ((dist - centerRadius - planetRadius - 
					StarSystemGenerator.STARTING_RADIUS_STAR_BASE - StarSystemGenerator.STARTING_RADIUS_STAR_RANGE * 0.5f) /
						(StarSystemGenerator.BASE_INCR * 1.25f));
		}
		if (orbitIndex < 0) orbitIndex = 0;
		
		return orbitIndex;
	}
	
	
	public static Random getRandom(long seed, int level) {
		if (seed == 0) return new Random();
		
		Random r = new Random(seed);
		for (int i = 0; i < level; i++) {
			r.nextLong();
		}
		return new Random(r.nextLong());
	}
	
	
	public static void addSurveyDataFor(PlanetAPI planet, TextPanelAPI text) {
		SurveyPlugin plugin = (SurveyPlugin) Global.getSettings().getNewPluginInstance("surveyPlugin");
		plugin.init(Global.getSector().getPlayerFleet(), planet);
		
		String dataType = plugin.getSurveyDataType(planet);
		if (dataType != null) {
			Global.getSector().getPlayerFleet().getCargo().addCommodity(dataType, 1);
			if (text != null) {
				AddRemoveCommodity.addCommodityGainText(dataType, 1, text);
			}
		}
	}
	
	public static void setFullySurveyed(MarketAPI market, TextPanelAPI text, boolean withNotification) {
		//if (true) return;
		
		for (MarketConditionAPI mc : market.getConditions()) {
			mc.setSurveyed(true);
		}
		market.setSurveyLevel(SurveyLevel.FULL);
		
		if (withNotification && market.getPrimaryEntity() instanceof PlanetAPI) {
			PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
			String string = "Acquired full survey data for " + planet.getName() + ", " + planet.getTypeNameWithWorld().toLowerCase();
			if (text != null) {
				text.setFontSmallInsignia();
				text.addParagraph(string, planet.getSpec().getIconColor());
				text.setFontInsignia();
			} else {
				//Global.getSector().getCampaignUI().addMessage(string, planet.getSpec().getIconColor());

				MessageIntel intel = new MessageIntel("Full survey data: " + planet.getName() + ", " + planet.getTypeNameWithWorld(),
						Misc.getBasePlayerColor());//, new String[] {"" + points}, Misc.getHighlightColor());
				intel.setIcon(Global.getSettings().getSpriteName("intel", "new_planet_info"));
				Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.INTEL_TAB, planet);
				
//				CommMessageAPI message = Global.getFactory().createMessage();
//				message.setSubject(string);
//				//message.setSubjectColor(planet.getSpec().getIconColor());
//				message.setAction(MessageClickAction.INTEL_TAB);
//				message.setCustomData(planet);
//				message.setAddToIntelTab(false);
//				message.setSmallIcon(Global.getSettings().getSpriteName("intel_categories", "star_systems"));
//				Global.getSector().getCampaignUI().addMessage(message);
			}
		}
	}
	
	public static void setPreliminarySurveyed(MarketAPI market, TextPanelAPI text, boolean withNotification) {
		market.setSurveyLevel(SurveyLevel.PRELIMINARY);
		
		if (withNotification && market.getPrimaryEntity() instanceof PlanetAPI) {
			PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
			String string = "Acquired preliminary survey data for " + planet.getName() + ", " + planet.getTypeNameWithWorld().toLowerCase();
			if (text != null) {
				text.setFontSmallInsignia();
				text.addParagraph(string, planet.getSpec().getIconColor());
				text.setFontInsignia();
			} else {
				//Global.getSector().getCampaignUI().addMessage(string, planet.getSpec().getIconColor());
				
				MessageIntel intel = new MessageIntel("Preliminary survey data: " + planet.getName() + ", " + planet.getTypeNameWithWorld(),
						Misc.getBasePlayerColor());//, new String[] {"" + points}, Misc.getHighlightColor());
				intel.setIcon(Global.getSettings().getSpriteName("intel", "new_planet_info"));
				Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.INTEL_TAB, planet);
				
//				CommMessageAPI message = Global.getFactory().createMessage();
//				message.setSubject(string);
//				//message.setSubjectColor(planet.getSpec().getIconColor());
//				message.setAction(MessageClickAction.INTEL_TAB);
//				message.setCustomData(planet);
//				message.setAddToIntelTab(false);
//				message.setSmallIcon(Global.getSettings().getSpriteName("intel_categories", "star_systems"));
//				Global.getSector().getCampaignUI().addMessage(message);
				//Global.getSector().getCampaignUI().addMessage(string, planet.getSpec().getIconColor());
			}
		}
	}
	
	public static void setSeen(MarketAPI market, TextPanelAPI text, boolean withNotification) {
		market.setSurveyLevel(SurveyLevel.SEEN);
		
		if (withNotification && market.getPrimaryEntity() instanceof PlanetAPI) {
			PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
			//String string = "Acquired preliminary survey data for " + planet.getName() + ", " + planet.getTypeNameWithWorld().toLowerCase();
			String type = planet.getSpec().getName();
			if (!planet.isGasGiant()) type += " World";
			String string = "New planet data: " + planet.getName() + ", " + type;
			if (text != null) {
				text.setFontSmallInsignia();
				text.addParagraph(string, planet.getSpec().getIconColor());
				text.setFontInsignia();
			} else {
				
				MessageIntel intel = new MessageIntel(string,
						Misc.getBasePlayerColor());//, new String[] {"" + points}, Misc.getHighlightColor());
				intel.setIcon(Global.getSettings().getSpriteName("intel", "new_planet_info"));
				Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.INTEL_TAB, planet);
				
//				CommMessageAPI message = Global.getFactory().createMessage();
//				message.setSubject(string);
//				message.setSubjectColor(planet.getSpec().getIconColor());
//				message.setAction(MessageClickAction.INTEL_TAB);
//				message.setCustomData(planet);
//				message.setAddToIntelTab(false);
//				message.setSmallIcon(Global.getSettings().getSpriteName("intel_categories", "star_systems"));
//				Global.getSector().getCampaignUI().addMessage(message);
				//Global.getSector().getCampaignUI().addMessage(string, planet.getSpec().getIconColor());
			}
		}
	}
	
	
	
	public static String getStringWithTokenReplacement(String format, SectorEntityToken entity, Map<String, MemoryAPI> memoryMap) {
		return Global.getSector().getRules().performTokenReplacement(
				null, format,
				entity, memoryMap);
	}
	
	
	public static void renderQuadAlpha(float x, float y, float width, float height, Color color, float alphaMult) {
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ZERO);

		GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)(color.getAlpha() * alphaMult));
		
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glVertex2f(x, y);
			GL11.glVertex2f(x, y + height);
			GL11.glVertex2f(x + width, y + height);
			GL11.glVertex2f(x + width, y);
		}
		GL11.glEnd();
	}
	
	
	public static void fadeAndExpire(SectorEntityToken entity) {
		fadeAndExpire(entity, 1f);
	}
	public static void fadeAndExpire(final SectorEntityToken entity, final float seconds) {
		entity.addTag(Tags.NON_CLICKABLE);
		//entity.getContainingLocation().addScript(new EveryFrameScript() {
		entity.addScript(new EveryFrameScript() {
			float elapsed = 0f;
			public boolean runWhilePaused() {
				return false;
			}
			public boolean isDone() {
				return entity.isExpired();
			}
			public void advance(float amount) {
				elapsed += amount;
				if (elapsed > seconds) {
					entity.setExpired(true);
				}
				float b = 1f - elapsed / seconds;
				if (b < 0) b = 0;
				if (b > 1) b = 1;
				entity.forceSensorFaderBrightness(Math.min(entity.getSensorFaderBrightness(), b));
				entity.setAlwaysUseSensorFaderBrightness(true);
			}
		});
	}
	public static void fadeInOutAndExpire(final SectorEntityToken entity, final float in, final float dur, final float out) {
		entity.addTag(Tags.NON_CLICKABLE);
		entity.forceSensorFaderBrightness(0f);
		entity.setAlwaysUseSensorFaderBrightness(true);
		//entity.getContainingLocation().addScript(new EveryFrameScript() {
		entity.addScript(new EveryFrameScript() {
			float elapsed = 0f;
			public boolean runWhilePaused() {
				return false;
			}
			public boolean isDone() {
				return entity.isExpired();
			}
			public void advance(float amount) {
				elapsed += amount;
				if (elapsed > in + dur + out) {
					entity.setExpired(true);
				}
				float b = 1f;
				if (elapsed < in) {
					b = elapsed / in;
				} else if (elapsed > in + dur) {
					b = 1f - (elapsed - in - dur) / out;
				}
				if (b < 0) b = 0;
				if (b > 1) b = 1;
				entity.forceSensorFaderBrightness(Math.min(entity.getSensorFaderBrightness(), b));
				entity.setAlwaysUseSensorFaderBrightness(true);
			}
		});
	}
	
	public static CustomCampaignEntityAPI addCargoPods(LocationAPI where, Vector2f loc) {
		CustomCampaignEntityAPI pods = where.addCustomEntity(null, null, Entities.CARGO_PODS, Factions.NEUTRAL);
		pods.getLocation().x = loc.x;
		pods.getLocation().y = loc.y;
		
		Vector2f vel = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
		vel.scale(5f + 10f * (float) Math.random());
		pods.getVelocity().set(vel);
		
		pods.setDiscoverable(null);
		pods.setDiscoveryXP(null);
		pods.setSensorProfile(1f);
		
		return pods;
	}
	
	
	public static SectorEntityToken addDebrisField(LocationAPI loc, DebrisFieldParams params, Random random) {
		if (random == null) random = new Random();
		SectorEntityToken debris = loc.addTerrain(Terrain.DEBRIS_FIELD, params);
		debris.setSensorProfile(1f);
		debris.setDiscoverable(true);
		debris.setName(((CampaignTerrainAPI)debris).getPlugin().getTerrainName());
		
//		float range = 300f + params.bandWidthInEngine * 5;
//		if (range > 2000) range = 2000;
//		debris.getDetectedRangeMod().modifyFlat("gen", range);
		
		float range = DebrisFieldTerrainPlugin.computeDetectionRange(params.bandWidthInEngine);
		debris.getDetectedRangeMod().modifyFlat("gen", range);
	
		debris.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, random.nextLong());
		
		// add some default salvage
		// most uses of this will want to clear that out and add something more specific
		DropData data = new DropData();
		data.group = Drops.BASIC;
		data.value = (int) ((1000 + params.bandWidthInEngine) * 5);
		debris.addDropValue(data); 
		
		debris.setDiscoveryXP((float)((int)(params.bandWidthInEngine * 0.2f)));
		if (params.baseSalvageXP <= 0) {
			debris.setSalvageXP((float)((int)(params.bandWidthInEngine * 0.6f)));
		}
		
		return debris;
	}
	

	public static boolean isShipRecoverable(FleetMemberAPI member, CampaignFleetAPI recoverer, boolean own, boolean useOfficerRecovery, long seed, float chanceMult) {
		//Random rand = new Random(1000000 * (member.getId().hashCode() + seed + Global.getSector().getClock().getDay()));
		//Random rand = new Random(1000000 * (member.getId().hashCode() + Global.getSector().getClock().getDay()));
		Random rand = new Random(1000000 * member.getId().hashCode() + Global.getSector().getPlayerBattleSeed());
		//rand = new Random();
		float chance = Global.getSettings().getFloat("baseShipRecoveryChance");
		if (own) {
			chance = Global.getSettings().getFloat("baseOwnShipRecoveryChance");
		}
		chance = member.getStats().getDynamic().getValue(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD, chance);
		if (recoverer != null) {
			chance = recoverer.getStats().getDynamic().getValue(Stats.SHIP_RECOVERY_MOD, chance);
			if (useOfficerRecovery) {
				chance = recoverer.getStats().getDynamic().getValue(Stats.OFFICER_SHIP_RECOVERY_MOD, chance);
			}
		}
		if (chance < 0) chance = 0;
		if (chance > 1f) chance = 1f;
		chance *= chanceMult;
		return rand.nextFloat() < chance;
	}

//	public static float computeDetectionRangeForEntity(float radius) {
//		float range = 300f + radius * 5f;
//		if (range > 2000) range = 2000;
//		return radius;
//	}
	
	
	
	public static JumpPointAPI findNearestJumpPointTo(SectorEntityToken entity) {
		float min = Float.MAX_VALUE;
		JumpPointAPI result = null;
		List<JumpPointAPI> points = entity.getContainingLocation().getEntities(JumpPointAPI.class);
		
		for (JumpPointAPI curr : points) {
			float dist = Misc.getDistance(entity.getLocation(), curr.getLocation());
			if (dist < min) {
				min = dist;
				result = curr;
			}
		}
		return result;
	}
	
	
	
	public static final boolean shouldConvertFromStub(LocationAPI containingLocation, Vector2f location) {
		//if (true) return false;
		if (Global.getSector().getPlayerFleet() == null) return false;
		
//		if (containingLocation == null || 
//				containingLocation != Global.getSector().getPlayerFleet().getContainingLocation()) return false;
		
		Vector2f stubLocInHyper = null;
		if (containingLocation == null || containingLocation.isHyperspace()) {
			stubLocInHyper = location;
		} else {
			stubLocInHyper = containingLocation.getLocation();
		}

		Vector2f playerLoc = Global.getSector().getPlayerFleet().getLocationInHyperspace();
		
		boolean sameLoc = containingLocation != null &&
						  Global.getSector().getPlayerFleet().getContainingLocation() == containingLocation;
		
		float maxDist = 6000;
		if (!sameLoc) maxDist = 3000;
		
		float dist = Misc.getDistance(playerLoc, stubLocInHyper);
		return dist < maxDist;
	}
	
//	public static final boolean shouldConvertFromStub(FleetStubAPI stub) {
//		if (Global.getSector().getPlayerFleet() == null) return false;
//		
//		return shouldConvertFromStub(stub.getContainingLocation(), stub.getLocation());
//	}
//	
//	
//	public static final boolean shouldConvertToStub(CampaignFleetAPI fleet) {
//		if (fleet.getStub() == null || !fleet.isConvertToStub()) return false;
//		
//		if (Global.getSector().getPlayerFleet() == null) return true;
//		
//		Vector2f fleetLocInHyper = fleet.getLocationInHyperspace();
//		Vector2f playerLoc = Global.getSector().getPlayerFleet().getLocationInHyperspace();
//		
//		boolean sameLoc = fleet.getContainingLocation() != null &&
//						  Global.getSector().getPlayerFleet().getContainingLocation() == fleet.getContainingLocation();
//		
//		float maxDist = 8000;
//		if (!sameLoc) maxDist = 4000;
//		
//		float dist = Misc.getDistance(playerLoc, fleetLocInHyper);
//		return dist > maxDist;
//	}
	
	
	private static final AtomicLong seedUniquifier = new AtomicLong(8682522807148012L);
	
	/**
	 * How Java generates a seed for a new java.util.Random() instance.
	 * @return
	 */
	public static long genRandomSeed() {
		return seedUniquifier() ^ System.nanoTime();
	}
	public static long seedUniquifier() {
		// L'Ecuyer, "Tables of Linear Congruential Generators of
		// Different Sizes and Good Lattice Structure", 1999
		for (;;) {
			long current = seedUniquifier.get();
			long next = current * 181783497276652981L;
			//long next = current * 1181783497276652981L; // actual correct number?
			if (seedUniquifier.compareAndSet(current, next)) {
				return next;
			}
		}
	}
	
	
	public static String genUID() {
		if (Global.getSettings() != null && Global.getSettings().isInGame() &&  
				(Global.getSettings().isInCampaignState() || Global.getSettings().isGeneratingNewGame())) {
			return Global.getSector().genUID(); 
		}
		return UUID.randomUUID().toString();
	}
	
	
	public static String colorsToString(List<Color> colors) {
		String result = "";
		for (Color c : colors) {
			result += Integer.toHexString(c.getRGB()) + "|";
		}
		if (result.length() > 0) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}
	
	public static List<Color> colorsFromString(String in) {
		List<Color> result = new ArrayList<Color>();
		for (String p : in.split("\\|")) {
			//result.add(new Color(Integer.parseInt(p, 16)));
			result.add(new Color((int)Long.parseLong(p, 16)));
		}
		return result;
	}
	
	public static JumpPointAPI getJumpPointTo(PlanetAPI star) {
		for (Object entity : Global.getSector().getHyperspace().getEntities(JumpPointAPI.class)) {
			JumpPointAPI jp = (JumpPointAPI) entity;
			if (jp.getDestinationVisualEntity() == star) return jp;
		}
		return null;
	}
	
	
	public static final String D_HULL_SUFFIX = "_default_D";
	public static String getDHullId(ShipHullSpecAPI spec) {
		String base = spec.getHullId();
		if (base.endsWith(D_HULL_SUFFIX)) return base;
		return base + D_HULL_SUFFIX;
	}
	
	
	public static HullModSpecAPI getMod(String id) {
		return Global.getSettings().getHullModSpec(id);
	}
	
	public static float getDistanceFromArc(float direction, float arc, float angle) {
		direction = normalizeAngle(direction);
		angle = normalizeAngle(angle);
	
		float dist1 = Math.abs(angle - direction) - arc/2f;
		float dist2 = Math.abs(360 - Math.abs(angle - direction)) - arc/2f;
		
		if (dist1 <= 0 || dist2 <= 0) return 0;
		
		return dist1 > dist2 ? dist2 : dist1;
	}
	
	public static void initConditionMarket(PlanetAPI planet) {
		if (planet.getMarket() != null) {
			Global.getSector().getEconomy().removeMarket(planet.getMarket());
		}
		
		MarketAPI market = Global.getFactory().createMarket("market_" + planet.getId(), planet.getName(), 1);
		market.setPlanetConditionMarketOnly(true);
		market.setPrimaryEntity(planet);
		market.setFactionId(Factions.NEUTRAL);
		planet.setMarket(market);
		
		long seed = StarSystemGenerator.random.nextLong();
		planet.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, seed);
	}
	
	public static void initEconomyMarket(PlanetAPI planet) {
		if (planet.getMarket() != null) {
			Global.getSector().getEconomy().removeMarket(planet.getMarket());
		}
		
		MarketAPI market = Global.getFactory().createMarket("market_" + planet.getId(), planet.getName(), 1);
		//market.setPlanetConditionMarketOnly(true);
		market.setPrimaryEntity(planet);
		market.setFactionId(Factions.NEUTRAL);
		planet.setMarket(market);
		Global.getSector().getEconomy().addMarket(market, true);
	}
	
	public static String getSurveyLevelString(SurveyLevel level, boolean withBrackets) {
		String str = " ";
		if (level == SurveyLevel.NONE) str = UNKNOWN;
		else if (level == SurveyLevel.SEEN) str = UNSURVEYED;
		else if (level == SurveyLevel.PRELIMINARY) str = PRELIMINARY;
		else if (level == SurveyLevel.FULL) str = FULL;
		
		if (withBrackets) {
			str = "[" + str + "]";
		}
		return str;
	}
	
	
	public static void setDefenderOverride(SectorEntityToken entity, DefenderDataOverride override) {
		entity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_DEFENDER_OVERRIDE, override);
	}
	
	public static void setSalvageSpecial(SectorEntityToken entity, Object data) {
		entity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPECIAL_DATA, data);
//		if (data instanceof ShipRecoverySpecialData) {
//			BaseSalvageSpecial.clearExtraSalvage(entity);
//		}
	}
	
	public static Object getSalvageSpecial(SectorEntityToken entity) {
		return entity.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA);
	}
	
	
	
	
	public static List<StarSystemAPI> getSystemsInRange(SectorEntityToken from, Set<StarSystemAPI> exclude, boolean nonEmpty, float maxRange) {
		List<StarSystemAPI> systems = new ArrayList<StarSystemAPI>();
		
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (exclude != null && exclude.contains(system)) continue;
			
			float dist = Misc.getDistance(from.getLocationInHyperspace(), system.getLocation());
			if (dist > maxRange) continue;
			if (nonEmpty && !systemHasPlanets(system)) continue;
			
			systems.add(system);
		}
		
		return systems;
	}

	public static boolean systemHasPlanets(StarSystemAPI system) {
		for (PlanetAPI p : system.getPlanets()) {
			if (!p.isStar()) return true;
		}
		return false;
	}
	
	public static float getCampaignShipScaleMult(HullSize size) {
		switch (size) {
		case CAPITAL_SHIP:
			return 0.07f;
		case CRUISER:
			return  0.08f;
		case DESTROYER:
			return 0.09f;
		case FRIGATE:
			return 0.11f;
		case FIGHTER:
			return 0.15f;
		case DEFAULT:
			return 0.1f;
		}
		return 0.1f;
	}
	
	public static WeightedRandomPicker<String> createStringPicker(Object ... params) {
		return createStringPicker(StarSystemGenerator.random, params);
	}
	
	public static WeightedRandomPicker<String> createStringPicker(Random random, Object ... params) {
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		for (int i = 0; i < params.length; i += 2) {
			String item = (String) params[i];
			float weight = 0f;
			if (params[i+1] instanceof Float) {
				weight = (Float) params[i+1];
			} else if (params[i+1] instanceof Integer) {
				weight = (Integer) params[i+1];
			}
			picker.add(item, weight);
		}
		return picker;
	}
	
	public static void setWarningBeaconGlowColor(SectorEntityToken beacon, Color color) {
		beacon.getMemoryWithoutUpdate().set(WarningBeaconEntityPlugin.GLOW_COLOR_KEY, color);
	}
	
	public static void setWarningBeaconPingColor(SectorEntityToken beacon, Color color) {
		beacon.getMemoryWithoutUpdate().set(WarningBeaconEntityPlugin.PING_COLOR_KEY, color);
	}
	
	public static void setWarningBeaconColors(SectorEntityToken beacon, Color glow, Color ping) {
		if (glow != null) setWarningBeaconGlowColor(beacon, glow);
		if (ping != null) setWarningBeaconPingColor(beacon, ping);
	}
	

	public static List<CampaignFleetAPI> getNearbyFleets(SectorEntityToken from, float maxDist) {
		List<CampaignFleetAPI> result = new ArrayList<CampaignFleetAPI>();
		for (CampaignFleetAPI other : from.getContainingLocation().getFleets()) {
			if (from == other) continue;
			float dist = getDistance(from.getLocation(), other.getLocation());
			if (dist <= maxDist) {
				result.add(other);
			}
		}
		return result;
	}
	
	public static List<CampaignFleetAPI> getVisibleFleets(SectorEntityToken from, boolean includeSensorContacts) {
		List<CampaignFleetAPI> result = new ArrayList<CampaignFleetAPI>();
		for (CampaignFleetAPI other : from.getContainingLocation().getFleets()) {
			if (from == other) continue;
			VisibilityLevel level = other.getVisibilityLevelTo(from);
			if (level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS || level == VisibilityLevel.COMPOSITION_DETAILS) {
				result.add(other);
			} else if (level == VisibilityLevel.SENSOR_CONTACT && includeSensorContacts) {
				result.add(other);
			}
		}
		return result;
	}
	
	public static boolean isSameCargo(CargoAPI baseOne, CargoAPI baseTwo) {
		CargoAPI one = Global.getFactory().createCargo(true);
		one.addAll(baseOne);
		one.sort();
		
		CargoAPI two = Global.getFactory().createCargo(true);
		two.addAll(baseTwo);
		two.sort();
	

		if (one.getStacksCopy().size() != two.getStacksCopy().size()) return false;
		
		List<CargoStackAPI> stacks1 = one.getStacksCopy();
		List<CargoStackAPI> stacks2 = two.getStacksCopy();
		for (int i = 0; i < stacks1.size(); i++) {
			CargoStackAPI s1 = stacks1.get(i);
			CargoStackAPI s2 = stacks2.get(i);
			
			if ((s1 == null || s2 == null) && s1 != s2) return false;
			if (s1.getSize() != s2.getSize()) return false;
			if (s1.getType() != s2.getType()) return false;
			if ((s1.getData() == null || s2.getData() == null) && s1.getData() != s2.getData()) return false;
			if (!s1.getData().equals(s2.getData())) return false;
		}
		
		
		return true;
	}
	
	
	public static JumpPointAPI getDistressJumpPoint(StarSystemAPI system) {
		SectorEntityToken jumpPoint = null;
		float minDist = Float.MAX_VALUE;
		for (SectorEntityToken curr : system.getJumpPoints()) {
			float dist = Misc.getDistance(system.getCenter().getLocation(), curr.getLocation());
			if (dist < minDist) {
				jumpPoint = curr;
				minDist = dist;
			}
		}
		if (jumpPoint instanceof JumpPointAPI) {
			return (JumpPointAPI) jumpPoint;
		}
		return null;
	}
	
	
	public static void clearTarget(CampaignFleetAPI fleet, boolean forgetTransponder) {
		fleet.setInteractionTarget(null);
		if (fleet.getAI() instanceof ModularFleetAIAPI) {
			ModularFleetAIAPI ai = (ModularFleetAIAPI) fleet.getAI();
			ai.getTacticalModule().setTarget(null);
			ai.getTacticalModule().setPriorityTarget(null, 0f, false);
		}
		if (forgetTransponder) {
			Misc.forgetAboutTransponder(fleet);
		}
	}
	
	public static void giveStandardReturnToSourceAssignments(CampaignFleetAPI fleet) {
		giveStandardReturnToSourceAssignments(fleet, true);
	}
	public static void giveStandardReturnToSourceAssignments(CampaignFleetAPI fleet, boolean withClear) {
		if (withClear) {
			fleet.clearAssignments();
		}
		MarketAPI source = Misc.getSourceMarket(fleet);
		if (source != null) {
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, source.getPrimaryEntity(), 1000f, "returning to " + source.getName());
			fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, source.getPrimaryEntity(), 1f + 1f * (float) Math.random());
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, source.getPrimaryEntity(), 1000f);
		} else {
			SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, token, 1000f);
		}
	}
	
	public static void giveStandardReturnAssignments(CampaignFleetAPI fleet, SectorEntityToken where, String text, boolean withClear) {
		if (withClear) {
			fleet.clearAssignments();
		}
		if (text == null) {
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, where, 1000f, "returning to " + where.getName());
		} else {
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, where, 1000f, text + " " + where.getName());
		}
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, where, 5f + 5f * (float) Math.random());
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, where, 1000f);
	}
	
	
	public static void adjustRep(float repChangeFaction, RepLevel limit, String factionId,
								 float repChangePerson, RepLevel personLimit, PersonAPI person,
								 TextPanelAPI text) {
		if (repChangeFaction != 0) {
			CustomRepImpact impact = new CustomRepImpact();
			impact.delta = repChangeFaction;
			impact.limit = limit;
			Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.CUSTOM, impact,
										  null, text, true), 
										  factionId);
			
			if (person != null) {
				impact.delta = repChangePerson;
				impact.limit = personLimit;
				Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(RepActions.CUSTOM, impact,
											  null, text, true), person);
			}
		}
	}
	
	
	public static void interruptAbilitiesWithTag(CampaignFleetAPI fleet, String tag) {
		for (AbilityPlugin curr : fleet.getAbilities().values()) {
			if (curr.isActive()) {
				for (String t : curr.getSpec().getTags()) {
					if (t.equals(tag)) {
						curr.deactivate();
						break;
					}
				}
			}
		}
	}
	
	
	public static Vector2f getInterceptPoint(CampaignFleetAPI from, SectorEntityToken to) {
		
		//if (true) return new Vector2f(to.getLocation());
		//Vector2f v1 = new Vector2f(from.getVelocity());
		//Vector2f v2 = new Vector2f(to.getVelocity());
		Vector2f v2 = Vector2f.sub(to.getVelocity(), from.getVelocity(), new Vector2f());
		
		float s1 = from.getTravelSpeed();
		float s2 = v2.length();
		
		if (s1 < 10) s1 = 10;
		if (s2 < 10) s2 = 10;
		
		Vector2f p1 = new Vector2f(from.getLocation());
		Vector2f p2 = new Vector2f(to.getLocation());
		
		float dist = getDistance(p1, p2);
		float time = dist / s1;
		float maxTime = dist / s2 * 0.75f;
		if (time > maxTime) time = maxTime; // to ensure intercept point is never behind the from fleet
		
		Vector2f p3 = getUnitVectorAtDegreeAngle(getAngleInDegrees(v2));
		
		p3.scale(time * s2);
		Vector2f.add(p2, p3, p3);
		
		Vector2f overshoot = getUnitVectorAtDegreeAngle(getAngleInDegrees(p1, p3));
		overshoot.scale(3000f);
		Vector2f.add(p3, overshoot, p3);
		
		return p3;
	}

	public static void stopPlayerFleet() {
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player != null) {
			player.setVelocity(0, 0);
		}
	}
	
	public static String getListOfResources(Map<String, Integer> res, List<String> quantities) {
		List<String> list = new ArrayList<String>();
		for (String con : res.keySet()) {
			CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(con);
			int qty = res.get(con);
			list.add("" + qty + " " + spec.getName().toLowerCase());
			quantities.add("" + qty);
		}
		return Misc.getAndJoined(list);
	}
	
	
	public static void setColor(Color color) {
		GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)color.getAlpha());
	}
	
	public static void setColor(Color color, float alphaMult) {
		GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)((float)color.getAlpha() * alphaMult));
	}
	
	
	public static void setColor(Color color, int alpha) {
		GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)alpha);
	}
	
	
	public static boolean doesMarketHaveMissionImportantPeople(SectorEntityToken entity) {
		MarketAPI market = entity.getMarket();
		if (market == null) return false;
		if (market.getPrimaryEntity() != entity) return false;
		
		if (market != null && market.getCommDirectory() != null) {
			for (CommDirectoryEntryAPI entry : market.getCommDirectory().getEntriesCopy()) {
				if (entry.getType() == EntryType.PERSON && entry.getEntryData() instanceof PersonAPI) {
					PersonAPI person = (PersonAPI) entry.getEntryData();
					if (person.getMemoryWithoutUpdate().getBoolean(MemFlags.ENTITY_MISSION_IMPORTANT)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	public static void makeImportant(SectorEntityToken entity, String reason) {
		makeImportant(entity.getMemoryWithoutUpdate(), reason, -1);
	}
	public static void makeImportant(SectorEntityToken entity, String reason, float dur) {
		makeImportant(entity.getMemoryWithoutUpdate(), reason, dur);
	}
	public static void makeImportant(PersonAPI person, String reason) {
		makeImportant(person.getMemoryWithoutUpdate(), reason, -1);
	}
	public static void makeImportant(PersonAPI person, String reason, float dur) {
		makeImportant(person.getMemoryWithoutUpdate(), reason, dur);
	}
	public static void makeImportant(MemoryAPI memory, String reason) {
		Misc.setFlagWithReason(memory, MemFlags.ENTITY_MISSION_IMPORTANT,
				reason, true, -1);
	}
	public static void makeImportant(MemoryAPI memory, String reason, float dur) {
		Misc.setFlagWithReason(memory, MemFlags.ENTITY_MISSION_IMPORTANT,
				reason, true, dur);
	}
	
	public static void makeUnimportant(SectorEntityToken entity, String reason) {
		makeUnimportant(entity.getMemoryWithoutUpdate(), reason);
	}
	public static void makeUnimportant(PersonAPI person, String reason) {
		makeUnimportant(person.getMemoryWithoutUpdate(), reason);
	}
	public static void makeUnimportant(MemoryAPI memory, String reason) {
		Misc.setFlagWithReason(memory, MemFlags.ENTITY_MISSION_IMPORTANT,
			       			   reason, false, 0);
	}
	
	public static void cleanUpMissionMemory(MemoryAPI memory, String prefix) {
		List<String> unset = new ArrayList<String>();
		for (String key : memory.getKeys()) {
			if (key.startsWith("$" + prefix)) {
				unset.add(key);
			}
		}
		for (String key : unset) {
			memory.unset(key);
		}
		
		if (prefix.endsWith("_")) {
			prefix = prefix.substring(0, prefix.length() - 1);
		}
		
		Misc.setFlagWithReason(memory, MemFlags.ENTITY_MISSION_IMPORTANT,
						       prefix, false, 0f);
	}
	
	
	public static void clearAreaAroundPlayer(float minDist) {
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player == null) return;
		
		for (CampaignFleetAPI other : player.getContainingLocation().getFleets()) {
			if (player == other) continue;
			if (other.getBattle() != null) continue;
			if (other.getOrbit() != null) continue;
			if (!other.isHostileTo(player)) continue;
			
			float dist = Misc.getDistance(player.getLocation(), other.getLocation());
			if (dist < minDist) {
				float angle = Misc.getAngleInDegrees(player.getLocation(), other.getLocation());
				Vector2f v = Misc.getUnitVectorAtDegreeAngle(angle);
				v.scale(minDist);
				Vector2f.add(v, other.getLocation(), v);
				other.setLocation(v.x, v.y);
			}
		}
	}
	
	public static long getSalvageSeed(SectorEntityToken entity) {
		long seed = entity.getMemoryWithoutUpdate().getLong(MemFlags.SALVAGE_SEED);
		if (seed == 0) {
			//seed = new Random().nextLong();
			String id = entity.getId();
			if (id == null) id = genUID();
			seed = seedUniquifier() ^ (entity.getId().hashCode() * 17000);
			Random r = new Random(seed);
			for (int i = 0; i < 5; i++) {
				r.nextLong();
			}
			return r.nextLong();
		}
		return seed;
	}
	
	public static void forgetAboutTransponder(CampaignFleetAPI fleet) {
		MemoryAPI mem = fleet.getMemoryWithoutUpdate();
		if (mem.getBoolean(MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF)) {
			mem.removeAllRequired(MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF);
		}
		mem.unset(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF);
		mem.unset(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
	}
	
	public static void setAbandonedStationMarket(String marketId, SectorEntityToken station) {
		station.getMemoryWithoutUpdate().set("$abandonedStation", true);
		MarketAPI market = Global.getFactory().createMarket(marketId, station.getName(), 0);
		market.setSurveyLevel(SurveyLevel.FULL);
		market.setPrimaryEntity(station);
		market.setFactionId(station.getFaction().getId());
		market.addCondition(Conditions.ABANDONED_STATION);
		market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
		market.setPlanetConditionMarketOnly(false);
		((StoragePlugin)market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
		station.setMarket(market);
		station.getMemoryWithoutUpdate().unset("$tradeMode");
	}
	
	public static float getDesiredMoveDir(CampaignFleetAPI fleet) {
		if (fleet.getMoveDestination() == null) return 0f;
		
		return getAngleInDegrees(fleet.getLocation(), fleet.getMoveDestination());
	}
	
	public static boolean isPermaKnowsWhoPlayerIs(CampaignFleetAPI fleet) {
		MemoryAPI mem = fleet.getMemoryWithoutUpdate();
		if (mem.contains(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON) &&
				mem.getExpire(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON) < 0) {
			return true;
		}
		return false;
	}

	
	public static ImmigrationPlugin getImmigrationPlugin(MarketAPI market) {
		ImmigrationPlugin plugin = Global.getSector().getPluginPicker().pickImmigrationPlugin(market);
		if (plugin == null) {
			plugin = new CoreImmigrationPluginImpl(market);
		}
		return plugin;
	}
	
	public static AICoreAdminPlugin getAICoreAdminPlugin(String commodityId) {
		AICoreAdminPlugin plugin = Global.getSector().getPluginPicker().pickAICoreAdminPlugin(commodityId);
		return plugin;
	}
	
	public static AbandonMarketPlugin getAbandonMarketPlugin(MarketAPI market) {
		AbandonMarketPlugin plugin = Global.getSector().getGenericPlugins().pickPlugin(AbandonMarketPlugin.class, market);
		return plugin;
	}
	
	public static StabilizeMarketPlugin getStabilizeMarketPlugin(MarketAPI market) {
		StabilizeMarketPlugin plugin = Global.getSector().getGenericPlugins().pickPlugin(StabilizeMarketPlugin.class, market);
		return plugin;
	}
	
	
	public static FleetInflater getInflater(CampaignFleetAPI fleet, Object params) {
		FleetInflater plugin = Global.getSector().getPluginPicker().pickFleetInflater(fleet, params);
		return plugin;
	}
	
//	public static float getIncomingRate(MarketAPI market, float weight) {
//		ImmigrationPlugin plugin = getImmigrationPlugin(market);
//		float diff = plugin.getWeightForMarketSize(market.getSize() + 1) -
//					 plugin.getWeightForMarketSize(market.getSize());
//		if (diff <= 0) return 0f;
//		
//		//PopulationComposition incoming = market.getIncoming();
//		return weight / diff;
//		
//	}
	
	public static boolean playerHasStorageAccess(MarketAPI market) {
		SubmarketAPI storage = market.getSubmarket(Submarkets.SUBMARKET_STORAGE);
		if (storage != null && storage.getPlugin().getOnClickAction(null) == OnClickAction.OPEN_SUBMARKET) {
			return true;
		}
		return false;
	}
	
	public static float getMarketSizeProgress(MarketAPI market) {
		ImmigrationPlugin plugin = getImmigrationPlugin(market);
		float min = plugin.getWeightForMarketSize(market.getSize());
		float max = plugin.getWeightForMarketSize(market.getSize() + 1);
		
		float curr = market.getPopulation().getWeightValue();
		
		if (max <= min) return 0f;
		
		float f = (curr - min) / (max - min);
		if (f < 0) f = 0;
		if (f > 1) f = 1;
		return f;
	}
	
	
	public static float getStorageFeeFraction() {
		float storageFreeFraction = Global.getSettings().getFloat("storageFreeFraction");
		return storageFreeFraction;
	}
	
	public static int getStorageCostPerMonth(MarketAPI market) {
		return (int) (getStorageTotalValue(market) * getStorageFeeFraction());
	}
	
	public static SubmarketPlugin getStorage(MarketAPI market) {
		SubmarketAPI submarket = market.getSubmarket(Submarkets.SUBMARKET_STORAGE);
		if (submarket == null) return null;
		return submarket.getPlugin();
	}
	
	public static SubmarketPlugin getLocalResources(MarketAPI market) {
		SubmarketAPI submarket = market.getSubmarket(Submarkets.LOCAL_RESOURCES);
		if (submarket == null) return null;
		return submarket.getPlugin();
	}
	public static CargoAPI getStorageCargo(MarketAPI market) {
		SubmarketAPI submarket = market.getSubmarket(Submarkets.SUBMARKET_STORAGE);
		if (submarket == null) return null;
		return submarket.getCargo();
	}
	
	public static CargoAPI getLocalResourcesCargo(MarketAPI market) {
		SubmarketAPI submarket = market.getSubmarket(Submarkets.LOCAL_RESOURCES);
		if (submarket == null) return null;
		return submarket.getCargo();
	}
	
	public static float getStorageTotalValue(MarketAPI market) {
		return getStorageCargoValue(market) + getStorageShipValue(market);
	}
	public static float getStorageCargoValue(MarketAPI market) {
		SubmarketPlugin plugin = getStorage(market);
		if (plugin == null) return 0f;
		float value = 0f;
		for (CargoStackAPI stack : plugin.getCargo().getStacksCopy()) {
			value += stack.getSize() * stack.getBaseValuePerUnit();
		}
		return value;
	}
	
	public static float getStorageShipValue(MarketAPI market) {
		SubmarketPlugin plugin = getStorage(market);
		if (plugin == null) return 0f;
		float value = 0f;
		
		for (FleetMemberAPI member : plugin.getCargo().getMothballedShips().getMembersListCopy()) {
			value += member.getBaseValue();
		}
		return value;
	}
	
	
	/**
	 * Returns true if it added anything to the tooltip.
	 * @return
	 */
	public static boolean addStorageInfo(TooltipMakerAPI tooltip, Color color, Color dark, MarketAPI market,
										 //boolean showFees,
										 boolean includeLocalResources, boolean addSectionIfEmpty) {
		SubmarketPlugin storage = Misc.getStorage(market);
		SubmarketPlugin local = Misc.getLocalResources(market);
		
		CargoAPI cargo = Global.getFactory().createCargo(true);
		List<FleetMemberAPI> ships = new ArrayList<FleetMemberAPI>();
		if (storage != null) {
			cargo.addAll(storage.getCargo());
			ships.addAll(storage.getCargo().getMothballedShips().getMembersListCopy());
		}
		if (local != null && includeLocalResources) {
			cargo.addAll(local.getCargo());
			ships.addAll(local.getCargo().getMothballedShips().getMembersListCopy());
		}
		
		float opad = 15f;
		if (!cargo.isEmpty() || addSectionIfEmpty) {
			String title = "Cargo in storage";
			if (includeLocalResources && local != null) {
				title = "Cargo in storage and resource stockpiles";
			}
			tooltip.addSectionHeading(title, color, dark, Alignment.MID, opad);
			opad = 10f;
			tooltip.showCargo(cargo, 10, true, opad);
		}
		
		if (!ships.isEmpty() || addSectionIfEmpty) {
			String title = "Ships in storage";
			if (includeLocalResources && local != null) {
				title = "Ships in storage";
			}
			tooltip.addSectionHeading(title, color, dark, Alignment.MID, opad);
			opad = 10f;
			tooltip.showShips(ships, 10, true, opad);
		}
		
		if (!market.isPlayerOwned()) {
			int cost = getStorageCostPerMonth(market);
			if (cost > 0) {
				tooltip.addPara("Monthly storage fee: %s", opad, getHighlightColor(), getDGSCredits(cost));
			}
		}
		
		if (addSectionIfEmpty) return true;
		
		return !cargo.isEmpty() || !ships.isEmpty();
	}
	
	public static String getTokenReplaced(String in, SectorEntityToken entity) {
		in = Global.getSector().getRules().performTokenReplacement(null, in, entity, null);
		return in;
	}
	
	public static float getOutpostPenalty() {
		return Global.getSettings().getFloat("colonyOverMaxPenalty");
	}

	
	public static float getAdminSalary(PersonAPI admin) {
		int tier = (int) admin.getMemoryWithoutUpdate().getFloat("$ome_adminTier");
		String salaryKey = "adminSalaryTier" + tier;
		float s = Global.getSettings().getInt(salaryKey);
		return s;
	}
	
	public static float getOfficerSalary(PersonAPI officer) {
		int officerBase = Global.getSettings().getInt("officerSalaryBase");
		int officerPerLevel = Global.getSettings().getInt("officerSalaryPerLevel");
		
		float salary = officerBase + officer.getStats().getLevel() * officerPerLevel;
		return salary;
	}
	
//	public static int getAccessibilityPercent(float a) {
//		int result = (int) Math.round(a * 100f);
//		if (a < 0 && result == 0) result = -1; // shipping penalty at "below zero"
//		return result;
//	}
	
	public static Map<String, Integer> variantToFPCache = new HashMap<String, Integer>();
	public static Map<String, Boolean> variantToIsBaseCache = new HashMap<String, Boolean>();
	public static Map<String, String> variantToHullCache = new HashMap<String, String>();
	//public static Map<String, String> hullIdToHasTag = new HashMap<String, String>();
	public static String getHullIdForVariantId(String variantId) {
		String hull = variantToHullCache.get(variantId);
		if (hull != null) return hull;
		
		ShipVariantAPI variant = Global.getSettings().getVariant(variantId);
		hull = variant.getHullSpec().getHullId();
		variantToHullCache.put(variantId, hull);
		
		return hull;
	}
	
	public static boolean getIsBaseForVariantId(String variantId) {
		Boolean isBase = variantToIsBaseCache.get(variantId);
		if (isBase != null) return isBase;
//		System.out.println(variantId);
//		if (variantId.equals("buffalo2_FS")) {
//			System.out.println("wefwef");
//		}
		ShipVariantAPI variant = Global.getSettings().getVariant(variantId);
		isBase = variant.getHullSpec().hasTag(Items.TAG_BASE_BP);
		variantToIsBaseCache.put(variantId, isBase);
		
		return isBase;
	}
	
	public static int getFPForVariantId(String variantId) {
		Integer fp = variantToFPCache.get(variantId);
		if (fp != null) return fp;
		
		ShipVariantAPI variant = Global.getSettings().getVariant(variantId);
		fp = variant.getHullSpec().getFleetPoints();
		variantToFPCache.put(variantId, fp);
		
		return fp;
	}
	
	public static FactionPersonalityPickerPlugin getFactionPersonalityPicker() {
		return (FactionPersonalityPickerPlugin) Global.getSettings().getPlugin("factionPersonalityPicker");
	}
	
	
	public static float getAdjustedStrength(float fp, MarketAPI market) {
		fp *= Math.max(0.25f, 0.5f + Math.min(1f, Misc.getShipQuality(market)));
		
		if (market != null) {
			float numShipsMult = market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).computeEffective(0f);
			fp *= numShipsMult;
			
	//		float pts = market.getFaction().getDoctrine().getNumShips() + market.getFaction().getDoctrine().getOfficerQuality();
	//		fp *= 1f + (pts - 2f) / 4f;
			float pts = market.getFaction().getDoctrine().getOfficerQuality();
			fp *= 1f + (pts - 1f) / 4f;
		}
		return fp;
	}
	public static float getAdjustedFP(float fp, MarketAPI market) {
		if (market != null) {
			float numShipsMult = market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).computeEffective(0f);
			fp *= numShipsMult;
		}
		return fp;
	}
	
	public static float getShipQuality(MarketAPI market) {
		return getShipQuality(market, null);
	}
	public static float getShipQuality(MarketAPI market, String factionId) {
		return ShipQuality.getShipQuality(market, factionId);
//		float quality = 0f;
//		
//		if (market != null) {
//			CommodityOnMarketAPI com = market.getCommodityData(Commodities.SHIPS);
//			
//			SupplierData sd = com.getSupplier();
//			if (sd != null && sd.getMarket() != null) {
//				quality = sd.getMarket().getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).computeEffective(0f);
//				if (factionId == null && sd.getMarket().getFaction() != market.getFaction()) {
//					quality -= FleetFactoryV3.IMPORTED_QUALITY_PENALTY;
//				} else if (factionId != null && !factionId.equals(sd.getMarket().getFactionId())) {
//					quality -= FleetFactoryV3.IMPORTED_QUALITY_PENALTY;
//				}
//			}
//			
//			quality += market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).computeEffective(0f);
//		}
//		
//		
//		if (factionId == null) {
//			//quality += market.getFaction().getDoctrine().getShipQualityContribution();
//		} else {
//			if (market != null) {
//				quality -= market.getFaction().getDoctrine().getShipQualityContribution();
//			}
//			quality += Global.getSector().getFaction(factionId).getDoctrine().getShipQualityContribution();
//		}
//		
//		return quality;
	}
	
	
	public static ShipPickMode getShipPickMode(MarketAPI market) {
		return getShipPickMode(market, null);
	}
	public static ShipPickMode getShipPickMode(MarketAPI market, String factionId) {
		QualityData d = ShipQuality.getInstance().getQualityData(market);
		if (d.market != null) {
			if (factionId == null && d.market.getFaction() != market.getFaction()) {
				return ShipPickMode.IMPORTED;
			} else if (factionId != null && !factionId.equals(d.market.getFactionId())) {
				return ShipPickMode.IMPORTED;
			}
			return ShipPickMode.PRIORITY_THEN_ALL;
		}
		return ShipPickMode.IMPORTED;
	}
	
	public static boolean isBusy(CampaignFleetAPI fleet) {
		return fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.FLEET_BUSY);
	}
	
	public static CampaignFleetAPI getStationFleet(MarketAPI market) {
		for (SectorEntityToken entity : market.getConnectedEntities()) {
			if (entity.hasTag(Tags.STATION)) {
				CampaignFleetAPI fleet = getStationFleet(entity);
				if (fleet != null) return fleet;
			}
		}
		return null;
	}
	public static CampaignFleetAPI getStationFleet(SectorEntityToken station) {
		if (station.hasTag(Tags.STATION)) {// && station instanceof CustomCampaignEntityAPI) {
			Object test = station.getMemoryWithoutUpdate().get(MemFlags.STATION_FLEET);
			if (test instanceof CampaignFleetAPI) {
				return (CampaignFleetAPI) test;
			}
		}
		return null;
	}
	
	public static CampaignFleetAPI getStationBaseFleet(MarketAPI market) {
		for (SectorEntityToken entity : market.getConnectedEntities()) {
			if (entity.hasTag(Tags.STATION)) {
				CampaignFleetAPI fleet = getStationBaseFleet(entity);
				if (fleet != null) return fleet;
			}
		}
		return null;
	}
	public static CampaignFleetAPI getStationBaseFleet(SectorEntityToken station) {
		if (station.hasTag(Tags.STATION)) {// && station instanceof CustomCampaignEntityAPI) {
			Object test = station.getMemoryWithoutUpdate().get(MemFlags.STATION_BASE_FLEET);
			if (test instanceof CampaignFleetAPI) {
				return (CampaignFleetAPI) test;
			}
		}
		return null;
	}
	
	public static MarketAPI getStationMarket(CampaignFleetAPI station) {
		Object test = station.getMemoryWithoutUpdate().get(MemFlags.STATION_MARKET);
		if (test instanceof MarketAPI) {
			return (MarketAPI) test;
		}
		return null;
	}
	
	public static Industry getStationIndustry(MarketAPI market) {
		for (Industry ind : market.getIndustries()) {
			if (ind.getSpec().hasTag(Industries.TAG_STATION)) {
				return ind;
			}
		}
		return null;
	}
	
	public static boolean isActiveModule(ShipVariantAPI variant) {
		boolean notActiveModule = variant.getHullSpec().getOrdnancePoints(null) <= 0 &&
								  variant.getWeaponGroups().isEmpty() &&
								  variant.getHullSpec().getFighterBays() <= 0;
		return !notActiveModule;
	}
	public static boolean isActiveModule(ShipAPI ship) {
		boolean notActiveModule = ship.getVariant().getHullSpec().getOrdnancePoints(null) <= 0 &&
								  ship.getVariant().getWeaponGroups().isEmpty() &&
								  ship.getMutableStats().getNumFighterBays().getModifiedValue() <= 0;
		return !notActiveModule;
	}
	
	public static void addCreditsMessage(String format, int credits) {
		Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
				String.format(format, Misc.getDGSCredits(credits)), getTooltipTitleAndLightHighlightColor(), Misc.getDGSCredits(credits), getHighlightColor());
	}
	
	
	public static Vector2f getSystemJumpPointHyperExitLocation(JumpPointAPI jp) {
		for (JumpDestination d : jp.getDestinations()) {
			if (d.getDestination() != null && d.getDestination().getContainingLocation() != null &&
					d.getDestination().getContainingLocation().isHyperspace()) {
				return d.getDestination().getLocation();
			}
		}
		return jp.getLocationInHyperspace();
	}
	
	
	public static boolean isNear(SectorEntityToken entity, Vector2f hyperLoc) {
		float maxRange = Global.getSettings().getFloat("commRelayRangeAroundSystem");
		float dist = Misc.getDistanceLY(entity.getLocationInHyperspace(), hyperLoc);
		if (dist > maxRange) return false;
		return true;
	}
	
	public static float getDays(float amount) {
		return Global.getSector().getClock().convertToDays(amount);
	}
	
	public static float getProbabilityMult(float desired, float current, float deviationMult) {
		float deviation = desired * deviationMult;
		float exponent = (desired - current) / deviation;
		if (exponent > 4) exponent = 4;
		float probMult = (float) Math.pow(10f, exponent);
		return probMult;
	}
	
	public static boolean isHyperspaceAnchor(SectorEntityToken entity) {
		return entity.hasTag(Tags.SYSTEM_ANCHOR);
	}
	
	public static StarSystemAPI getStarSystemForAnchor(SectorEntityToken anchor) {
		return (StarSystemAPI) anchor.getMemoryWithoutUpdate().get(MemFlags.STAR_SYSTEM_IN_ANCHOR_MEMORY);
	}
	
	public static void showCost(TextPanelAPI text, Color color, Color dark, String [] res, int [] quantities) {
		showCost(text, "Resources: consumed (available)", true, color, dark, res, quantities);
	}
	public static void showCost(TextPanelAPI text, String title, boolean withAvailable, Color color, Color dark, String [] res, int [] quantities) {
		if (color == null) color = getBasePlayerColor();
		if (dark == null) dark = getDarkPlayerColor();
		
		Set<String> unmet = new HashSet<String>();
		Set<String> all = new LinkedHashSet<String>();
		
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		
		for (int i = 0; i < res.length; i++) {
			String commodityId = res[i];
			int quantity = quantities[i];
			if (quantity > cargo.getQuantity(CargoItemType.RESOURCES, commodityId)) {
				unmet.add(commodityId);
			}
			all.add(commodityId);
		}
		
		float costHeight = 67;
		ResourceCostPanelAPI cost = text.addCostPanel(title, costHeight,
				color, dark);
		cost.setNumberOnlyMode(true);
		cost.setWithBorder(false);
		cost.setAlignment(Alignment.LMID);
		
		for (int i = 0; i < res.length; i++) {
			String commodityId = res[i];
			int required = quantities[i];
			int available = (int) cargo.getCommodityQuantity(commodityId);
			Color curr = color;
			if (withAvailable && required > cargo.getQuantity(CargoItemType.RESOURCES, commodityId)) {
				curr = Misc.getNegativeHighlightColor();
			}
			if (withAvailable) {
				cost.addCost(commodityId, "" + required + " (" + available + ")", curr);
			} else {
				cost.addCost(commodityId, "" + required, curr);
			}
		}
		cost.update();
	}
	
	public static boolean isPlayerFactionSetUp() {
		String key = "$shownFactionConfigDialog";
		if (Global.getSector().getMemoryWithoutUpdate().contains(key)) {
			return true;
		}
		return false;
	}
	
	public static boolean isPatrol(CampaignFleetAPI fleet) {
		return fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_PATROL_FLEET);
	}
	public static boolean isSmuggler(CampaignFleetAPI fleet) {
		return fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_SMUGGLER);
	}
	public static boolean isTrader(CampaignFleetAPI fleet) {
		return fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_TRADE_FLEET);
	}
	public static boolean isPirate(CampaignFleetAPI fleet) {
		return fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_PIRATE);
	}
	public static boolean isScavenger(CampaignFleetAPI fleet) {
		return fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_SCAVENGER);
	}
	public static boolean isRaider(CampaignFleetAPI fleet) {
		return fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_RAIDER);
	}
	public static boolean isWarFleet(CampaignFleetAPI fleet) {
		return fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_WAR_FLEET);
	}
	
	
	/**
	 * pair.one can be null if a stand-alone, non-market station is being returned in pair.two.
	 * @param from
	 * @return
	 */
	public static Pair<SectorEntityToken, CampaignFleetAPI> getNearestStationInSupportRange(CampaignFleetAPI from) {
		SectorEntityToken closestEntity = null;
		CampaignFleetAPI closest = null;
		float minDist = Float.MAX_VALUE;
		for (SectorEntityToken station : from.getContainingLocation().getCustomEntitiesWithTag(Tags.STATION)) {
			CampaignFleetAPI fleet = Misc.getStationFleet(station);
			if (fleet == null || fleet.isEmpty()) continue;
			
			if (!isStationInSupportRange(from, fleet)) continue;
			float dist = Misc.getDistance(from.getLocation(), station.getLocation());
			
			if (dist < minDist) {
				closest = fleet;
				closestEntity = station;
				minDist = dist;
			}
		}
		
		// remnant stations and other fleets that are in station mode, w/o a related market
		for (CampaignFleetAPI fleet : from.getContainingLocation().getFleets()) {
			if (!fleet.isStationMode()) continue;
			if (fleet.isHidden()) continue;
			
			if (!isStationInSupportRange(from, fleet)) continue;
			float dist = Misc.getDistance(from.getLocation(), fleet.getLocation());
			
			if (dist < minDist) {
				closest = fleet;
				closestEntity = null;
				minDist = dist;
			}
		}
		
		if (closest == null) return null;
		
		return new Pair<SectorEntityToken, CampaignFleetAPI>(closestEntity, closest);
	}
	
	public static boolean isStationInSupportRange(CampaignFleetAPI fleet, CampaignFleetAPI station) {
		float check = Misc.getBattleJoinRange();
		float distPrimary = 10000f;
		MarketAPI market = getStationMarket(station);
		if (market != null) {
			distPrimary = Misc.getDistance(fleet.getLocation(), market.getPrimaryEntity().getLocation());
		}
		float distStation = Misc.getDistance(fleet.getLocation(), station.getLocation());
		
		if (distPrimary > check && distStation > check) {
			return false;
		}
		return true;
		
	}
	
	
	public static float getMemberStrength(FleetMemberAPI member) {
		return getMemberStrength(member, true, true, true);
	}
	
	public static float getMemberStrength(FleetMemberAPI member, boolean withHull, boolean withQuality, boolean withCaptain) {
		float str = member.getMemberStrength();
		float min = 0.25f;
		if (str < min) str = min;
		
		float quality = 0.5f;
		if (member.getFleetData() != null && member.getFleetData().getFleet() != null) {
			CampaignFleetAPI fleet = member.getFleetData().getFleet();
			if (fleet.getInflater() != null && !fleet.isInflated()) {
				quality = fleet.getInflater().getQuality();
			} else {
				BattleAPI battle = fleet.getBattle();
				CampaignFleetAPI source = battle == null ? null : battle.getSourceFleet(member);
				if (source != null && source.getInflater() != null &&
						!source.isInflated()) {
					quality = source.getInflater().getQuality();
				} else {
					float dmods = DModManager.getNumDMods(member.getVariant());
					quality = 1f - Global.getSettings().getFloat("qualityPerDMod") * dmods;
					if (quality < 0) quality = 0f;
				}
			}
		}
		
		if (member.isStation()) {
			quality = 1f;
		}
		
		
		float captainMult = 1f;
		if (member.getCaptain() != null) {
			float captainLevel = (member.getCaptain().getStats().getLevel() - 1f);
			if (member.isStation()) {
				captainMult += captainLevel / 20f;
			} else {
				captainMult += captainLevel / 10f;
			}
		}
		
		if (withQuality) {
			str *= Math.max(0.25f, 0.5f + quality);
		}
		if (withHull) {
			str *= 0.5f + 0.5f * member.getStatus().getHullFraction();
		}
		if (withCaptain) {
			str *= captainMult;
		}
		//System.out.println("Member: " + member + ", str: " + str);
		
		return str;
	}
	
	
	public static void increaseMarketHostileTimeout(MarketAPI market, float days) {
		MemoryAPI mem = market.getMemoryWithoutUpdate();
		float expire = days;
		if (mem.contains(MemFlags.MEMORY_KEY_PLAYER_HOSTILE_ACTIVITY_NEAR_MARKET)) {
			expire += mem.getExpire(MemFlags.MEMORY_KEY_PLAYER_HOSTILE_ACTIVITY_NEAR_MARKET); 
		}
		if (expire > 180) expire = 180;
		if (expire > 0) {
			mem.set(MemFlags.MEMORY_KEY_PLAYER_HOSTILE_ACTIVITY_NEAR_MARKET, true, expire);
		}
	}
	
	public static void removeRadioChatter(MarketAPI market) {
		if (market.getContainingLocation() == null) return;
		for (CampaignTerrainAPI terrain : market.getContainingLocation().getTerrainCopy()) {
			if (Terrain.RADIO_CHATTER.equals(terrain.getType())) {
				float dist = Misc.getDistance(terrain, market.getPrimaryEntity());
				if (dist < 200) {
					market.getContainingLocation().removeEntity(terrain);
				}
			}
		}
	}
	
	
	public static Color getDesignTypeColor(String designType) {
		return Global.getSettings().getDesignTypeColor(designType);
	}
	
	public static Color getDesignTypeColorDim(String designType) {
		Color c = Global.getSettings().getDesignTypeColor(designType);
		return Misc.scaleColorOnly(c, 0.53f);
	}

	
	public static void addDesignTypePara(TooltipMakerAPI tooltip, String design, float pad) {
		if (design != null && !design.isEmpty()) {
			tooltip.addPara("Design type: %s", pad, Misc.getGrayColor(), Global.getSettings().getDesignTypeColor(design), design);
		}
	}
	
	public static float getFleetRadiusTerrainEffectMult(CampaignFleetAPI fleet) {
		float min = Global.getSettings().getBaseFleetSelectionRadius() + Global.getSettings().getFleetSelectionRadiusPerUnitSize();
		float max = Global.getSettings().getMaxFleetSelectionRadius();
		float radius = fleet.getRadius();
		
		//radius = 1000;

		float mult = (radius - min) / (max - min);
		if (mult > 1) mult = 1;
		//if (mult < 0) mult = 0;
		if (mult < MIN_TERRAIN_EFFECT_MULT) mult = MIN_TERRAIN_EFFECT_MULT;
		//mult = MIN_BURN_PENALTY + mult * BURN_PENALTY_RANGE;

		float skillMod = fleet.getCommanderStats().getDynamic().getValue(Stats.NAVIGATION_PENALTY_MULT);
		mult *= skillMod;
		
		mult = Math.round(mult * 100f) / 100f;
		
		return mult;
	}
	
	public static float MIN_TERRAIN_EFFECT_MULT = Global.getSettings().getFloat("minTerrainEffectMult");
	public static float BURN_PENALTY_MULT = Global.getSettings().getFloat("standardBurnPenaltyMult");
	public static float getBurnMultForTerrain(CampaignFleetAPI fleet) {
		float mult = getFleetRadiusTerrainEffectMult(fleet);
		mult = (1f - BURN_PENALTY_MULT * mult);
		mult = Math.round(mult * 100f) / 100f;
		if (mult < 0.1f) mult = 0.1f;
//		if (mult > 1) mult = 1;
		return mult;
	}
		
	
	public static void addHitGlow(LocationAPI location, Vector2f loc, Vector2f vel, float size, Color color) {
		float dur = 1f + (float) Math.random();
		location.addHitParticle(loc, vel,
				size, 0.4f, dur, color);
		location.addHitParticle(loc, vel,
				size * 0.25f, 0.4f, dur, color);
		location.addHitParticle(loc, vel,
				size * 0.15f, 1f, dur, Color.white);
	}
	
	
	public static float SAME_FACTION_BONUS = Global.getSettings().getFloat("accessibilitySameFactionBonus");
	public static float PER_UNIT_SHIPPING = Global.getSettings().getFloat("accessibilityPerUnitShipping");
	
	public static int getShippingCapacity(MarketAPI market, boolean inFaction) {
		float a = Math.round(market.getAccessibilityMod().computeEffective(0f) * 100f) / 100f;
		if (inFaction) {
			a += SAME_FACTION_BONUS;
		}
		return (int) Math.max(0, a / PER_UNIT_SHIPPING);
	}
	
	public static String getStrengthDesc(float strAdjustedFP) {
		String strDesc;
		if (strAdjustedFP < 50) {
			strDesc = "very weak";
		} else if (strAdjustedFP < 150) {
			strDesc = "somewhat weak";
		} else if (strAdjustedFP < 300) {
			strDesc = "fairly capable";
		} else if (strAdjustedFP < 750) {
			strDesc = "fairly strong";
		} else if (strAdjustedFP < 1250) {
			strDesc = "strong";
		} else {
			strDesc = "very strong";
		}
		return strDesc;
	}
	
	public static boolean isMilitary(MarketAPI market) {
		return market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_MILITARY);
	}
	
	public static FactionAPI getClaimingFaction(SectorEntityToken planet) {
		int max = 0;
		MarketAPI result = null;
		for (MarketAPI curr : Global.getSector().getEconomy().getMarkets(planet.getContainingLocation())) {
			if (curr.isHidden()) continue;
			if (curr.getFaction().isPlayerFaction()) continue;
			
			int score = curr.getSize();
			if (isMilitary(curr)) score += 10;
			if (score > max) {
				JSONObject json = curr.getFaction().getCustom().optJSONObject(Factions.CUSTOM_PUNITIVE_EXPEDITION_DATA);
				if (json == null) continue;
				boolean territorial = json.optBoolean("territorial");
				if (!territorial) continue;
				
				max = score;
				result = curr;
			}
		}
		if (result == null) return null;
		
		return result.getFaction();
	}
	
	
	public static int computeTotalShutdownRefund(MarketAPI market) {
		int total = 0;
		for (Industry industry : market.getIndustries()) {
			total += computeShutdownRefund(market, industry);
		}
		return total;
	}
	
	public static int computeShutdownRefund(MarketAPI market, Industry industry) {
		float refund = 0;
		
		Industry upInd = null;
		if (industry.isUpgrading()) {
			String up = industry.getSpec().getUpgrade();
			if (up != null) {
				upInd = market.instantiateIndustry(up);
			}
		}
		if (industry.isUpgrading() && upInd != null) {
			refund += upInd.getBuildCost();
		}
		
		float refundFraction = Global.getSettings().getFloat("industryRefundFraction");
		Industry curr = industry;
		while (curr != null) {
			if (curr.isBuilding() && !curr.isUpgrading()) {
				refund += curr.getBuildCost();
			} else {
				refund += curr.getBuildCost() * refundFraction;
			}
			String down = curr.getSpec().getDowngrade();
			if (down != null) {
				curr = market.instantiateIndustry(down);
			} else {
				curr = null;
			}
		}
		
		return (int) refund;
	}
	
	
	public static SectorEntityToken addWarningBeacon(SectorEntityToken center, OrbitGap gap, String beaconTag) {
		CustomCampaignEntityAPI beacon = center.getContainingLocation().addCustomEntity(null, null, Entities.WARNING_BEACON, Factions.NEUTRAL);
		beacon.addTag(beaconTag);
		
		float radius = (gap.start + gap.end) / 2f;
		float orbitDays = radius / (10f + StarSystemGenerator.random.nextFloat() * 5f);
		beacon.setCircularOrbitPointingDown(center, StarSystemGenerator.random.nextFloat() * 360f, radius, orbitDays);

		Color glowColor = new Color(255,200,0,255);
		Color pingColor = new Color(255,200,0,255);
		if (beaconTag.equals(Tags.BEACON_MEDIUM)) {
			glowColor = new Color(250,155,0,255);
			pingColor = new Color(250,155,0,255);
		} else if (beaconTag.equals(Tags.BEACON_HIGH)) {
			glowColor = new Color(250,55,0,255);
			pingColor = new Color(250,125,0,255);
		}
		Misc.setWarningBeaconColors(beacon, glowColor, pingColor);
		return beacon;
	}

	
	public static CoreUITradeMode getTradeMode(MemoryAPI memory) {
		CoreUITradeMode mode = CoreUITradeMode.OPEN;
		String val = memory.getString("$tradeMode");
		if (val != null && !val.isEmpty()) {
			mode = CoreUITradeMode.valueOf(val);
		}
		return mode;
	}
	
	public static boolean isSpacerStart() {
		return Global.getSector().getMemoryWithoutUpdate().getBoolean("$spacerStart");
	}
	
	public static Industry getSpaceport(MarketAPI market) {
		for (Industry ind : market.getIndustries()) {
			if (ind.getSpec().hasTag(Industries.TAG_SPACEPORT)) {
				return ind;
			}
		}
		return null;
	}
	
	public static Color setBrightness(Color color, int brightness) {
		float max = color.getRed();
		if (color.getGreen() > max) max = color.getGreen();
		if (color.getBlue() > max) max = color.getBlue();
		float f = brightness / max;
		color = scaleColorSaturate(color, f);
		return color;
	}
	
	public static Color scaleColorSaturate(Color color, float factor) {
		int red = (int) (color.getRed() * factor);
		int green = (int) (color.getGreen() * factor);
		int blue = (int) (color.getBlue() * factor);
		int alpha = (int) (color.getAlpha() * factor);
		
		if (red > 255) red = 255;
		if (green > 255) green = 255;
		if (blue > 255) blue = 255;
		if (alpha > 255) alpha = 255;
		
		return new Color(red, green, blue, alpha);
	}
	
	
	public static int getMaxIndustries(MarketAPI market) {
		return (int)Math.round(market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).computeEffective(0));
	}
	
	public static int getNumIndustries(MarketAPI market) {
		int count = 0;
		for (Industry curr : market.getIndustries()) {
			if (curr.isIndustry()) {
				count++;
			} else if (curr.isUpgrading()) {
				String up = curr.getSpec().getUpgrade();
				if (up != null) {
					Industry upInd = market.instantiateIndustry(up);
					if (upInd.isIndustry()) count++;
				}
			}
		}
		for (ConstructionQueueItem item : market.getConstructionQueue().getItems()) {
			IndustrySpecAPI spec = Global.getSettings().getIndustrySpec(item.id);
			if (spec.hasTag(Industries.TAG_INDUSTRY)) count++;
		}
		return count;
	}

	public static Industry getCurrentlyBeingConstructed(MarketAPI market) {
		for (Industry curr : market.getIndustries()) {
			if (curr.getSpec().hasTag(Industries.TAG_POPULATION)) continue;
			
			if (curr.isBuilding() && !curr.isUpgrading()) {
				return curr;
			}
		}
		return null;
	}
	
	public static Color getRelColor(float rel) {  
		Color relColor = new Color(125,125,125,255);  
		if (rel > 1) rel = 1;  
		if (rel < -1) rel = -1;  

		if (rel > 0) {  
			relColor = Misc.interpolateColor(relColor, Misc.getPositiveHighlightColor(), Math.max(0.15f, rel));  
		} else if (rel < 0) {  
			relColor = Misc.interpolateColor(relColor, Misc.getNegativeHighlightColor(), Math.max(0.15f, -rel));  
		}  
		return relColor;  
	} 
	
	public static MusicPlayerPlugin musicPlugin = null;
	public static MusicPlayerPlugin getMusicPlayerPlugin() {
		if (musicPlugin == null) {
			musicPlugin = (MusicPlayerPlugin) Global.getSettings().getNewPluginInstance("musicPlugin");
		}
		return musicPlugin;
	}
	
	
	
	public static String DANGER_LEVEL_OVERRIDE = "$dangerLevelOverride";
	public static int getDangerLevel(CampaignFleetAPI fleet) {
		if (fleet.getMemoryWithoutUpdate().contains(DANGER_LEVEL_OVERRIDE)) {
			return (int) fleet.getMemoryWithoutUpdate().getFloat(DANGER_LEVEL_OVERRIDE);
		}
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		float playerStr = 0f;
		float fleetStr = 0f;
		for (FleetMemberAPI member : pf.getFleetData().getMembersListCopy()) {
			float strength = Misc.getMemberStrength(member, true, true, true);
			playerStr += strength;
		}
		
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			float strength = Misc.getMemberStrength(member, true, true, true);
			fleetStr += strength;
		}
		
		if (playerStr > fleetStr * 3f) return 1;
		if (playerStr > fleetStr * 1.5f) return 2;
		if (playerStr > fleetStr * 0.75f) return 3;
		if (playerStr > fleetStr * 0.33f) return 4;
		return 5;
	}
}
















