package com.fs.starfarer.api.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.opengl.ATIMeminfo;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.NVXGpuMemoryInfo;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.MusicPlayerPlugin;
import com.fs.starfarer.api.campaign.AICoreAdminPlugin;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI.EntryType;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.ParticleControllerAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.ResourceCostPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.SubmarketPlugin.OnClickAction;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption;
import com.fs.starfarer.api.campaign.ai.FleetAIFlags;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.econ.AbandonMarketPlugin;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.ImmigrationPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.StabilizeMarketPlugin;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventManagerAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventPlugin;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI.SkillLevelAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.CombatListenerUtil;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.SharedUnlockData;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.JumpPointInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.WarningBeaconEntityPlugin;
import com.fs.starfarer.api.impl.campaign.abilities.ReversePolarityToggle;
import com.fs.starfarer.api.impl.campaign.econ.impl.ConstructionQueue.ConstructionQueueItem;
import com.fs.starfarer.api.impl.campaign.econ.impl.ShipQuality;
import com.fs.starfarer.api.impl.campaign.econ.impl.ShipQuality.QualityData;
import com.fs.starfarer.api.impl.campaign.events.BaseEventPlugin.MarketFilter;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Difficulties;
import com.fs.starfarer.api.impl.campaign.ids.Drops;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.intel.FactionCommissionIntel;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.plog.PlaythroughLog;
import com.fs.starfarer.api.impl.campaign.plog.SModRecord;
import com.fs.starfarer.api.impl.campaign.population.CoreImmigrationPluginImpl;
import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.OrbitGap;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.unsetAll;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidSource;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain.TileParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.PulsarBeamTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2.SlipstreamSegment;
import com.fs.starfarer.api.impl.codex.CodexUnlocker;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.plugins.FactionPersonalityPickerPlugin;
import com.fs.starfarer.api.plugins.SimulatorPlugin;
import com.fs.starfarer.api.plugins.SurveyPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;


public class Misc {
	
	public static boolean CAN_SMOD_BUILT_IN = true;
	
	public static String SIR = "Sir";
	public static String MAAM = "Ma'am";
	public static String CAPTAIN = "Captain";
	
	public static float FLUX_PER_CAPACITOR = Global.getSettings().getFloat("fluxPerCapacitor");
	public static float DISSIPATION_PER_VENT = Global.getSettings().getFloat("dissipationPerVent");
	
	private static boolean cbMode = Global.getSettings().getBoolean("colorblindMode");
	
	public static Color MOUNT_BALLISTIC = Global.getSettings().getColor("mountYellowColor");
	public static Color MOUNT_MISSILE = Global.getSettings().getColor("mountGreenColor");
	public static Color MOUNT_ENERGY = cbMode ? new Color(155,155,155,255) : Global.getSettings().getColor("mountBlueColor");
	public static Color MOUNT_UNIVERSAL = Global.getSettings().getColor("mountGrayColor");
	public static Color MOUNT_HYBRID = Global.getSettings().getColor("mountOrangeColor");
	public static Color MOUNT_SYNERGY = Global.getSettings().getColor("mountCyanColor");
	public static Color MOUNT_COMPOSITE = Global.getSettings().getColor("mountCompositeColor");
	
	// for combat entities
	public static final int OWNER_NEUTRAL = 100;
	public static final int OWNER_PLAYER = 0;
	
	public static Color FLOATY_EMP_DAMAGE_COLOR = new Color(255,255,255,255);
	public static Color FLOATY_ARMOR_DAMAGE_COLOR = new Color(255,255,0,220);
	public static Color FLOATY_SHIELD_DAMAGE_COLOR = new Color(200,200,255,220);
	public static Color FLOATY_HULL_DAMAGE_COLOR = new Color(255,50,0,220);
	
//	public static final String SUPPLY_ACCESSIBILITY = "Supply Accessibility";
	
	public static float GATE_FUEL_COST_MULT = Global.getSettings().getFloat("gateTransitFuelCostMult");
	
	public static int MAX_COLONY_SIZE = Global.getSettings().getInt("maxColonySize");
	public static int OVER_MAX_INDUSTRIES_PENALTY = Global.getSettings().getInt("overMaxIndustriesPenalty");
	
	
	public static float FP_TO_BOMBARD_COST_APPROX_MULT = 12f;
	public static float FP_TO_GROUND_RAID_STR_APPROX_MULT = 6f;
	
	public static String UNKNOWN = " ";
	public static String UNSURVEYED = "??";
	public static String PRELIMINARY = "?";
	public static String FULL = "X";
	
	/**
	 * Name of "story points".
	 */
	public static String STORY = "story";
	
	public static float MAX_OFFICER_LEVEL = Global.getSettings().getFloat("officerMaxLevel");
	
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
		
		public Object getObject(Map<String, MemoryAPI> memoryMap) {
			Object o = null;
			if (isVariable()) {
				VarAndMemory var = getVarNameAndMemory(memoryMap);
				o = var.memory.get(var.name);
			}
			return o;
		}
		
		public boolean getBoolean(Map<String, MemoryAPI> memoryMap) {
			String str = getString(memoryMap);
			return Boolean.parseBoolean(str);
		}
		
		public boolean isBoolean(Map<String, MemoryAPI> memoryMap) {
			String str = getString(memoryMap);
			return str.toLowerCase().equals("true") || str.toLowerCase().equals("false");
		}
		
		public boolean isFloat(Map<String, MemoryAPI> memoryMap) {
			String str = null;
			if (isVariable()) {
				VarAndMemory var = getVarNameAndMemory(memoryMap);
				str = var.memory.getString(var.name);
			} else {
				str = string;
			}
			try {
				Float.parseFloat(str);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
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
		
		public int getInt(Map<String, MemoryAPI> memoryMap) {
			return (int) Math.round(getFloat(memoryMap));
		}
		
		public Color getColor(Map<String, MemoryAPI> memoryMap) {
			Object object = null;
			if (isVariable()) {
				VarAndMemory var = getVarNameAndMemory(memoryMap);
				object = var.memory.get(var.name);
			}
			if (object instanceof Color) {
				return (Color) object;
			}
					
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
				} else if ("h".equals(string)) {
					string = "buttonShortcut";
				} else if ("story".equals(string)) {
					return Misc.getStoryOptionColor();
				} else if ("gray".equals(string)) {
					return Misc.getGrayColor();
				} else if ("grey".equals(string)) {
					return Misc.getGrayColor();
				} else {
					FactionAPI faction = Global.getSector().getFaction(string);
					if (faction != null) {
						return faction.getBaseUIColor();
					}
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
		List<String> keySet = new ArrayList<String>(memoryMap.keySet());
		if (keySet.contains(MemKeys.LOCAL)) {
			keySet.remove(MemKeys.LOCAL);
			keySet.add(0, MemKeys.LOCAL);
		}
		for (String key : keySet) {
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
		return getDistanceLY(from.getLocationInHyperspace(), to.getLocationInHyperspace());
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
	
	public static String getRoundedValueOneAfterDecimalIfNotWhole(float value) {
		if (Math.abs((float)Math.round(value) - value) < 0.0001f) {
			return String.format("%d", (int) Math.round(value));
		} else {
			return String.format("%.1f", value);
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
		return getPointWithinRadius(from, r, random);
	}
	public static Vector2f getPointWithinRadius(Vector2f from, float r, Random random) {
		r = r * random.nextFloat();
		float angle = (float) (random.nextFloat() * Math.PI * 2f);
		float x = (float) (Math.cos(angle) * r) + from.x;
		float y = (float) (Math.sin(angle) * r) + from.y;
		return new Vector2f(x, y);
	}
	
	public static Vector2f getPointWithinRadiusUniform(Vector2f from, float r, Random random) {
		r = (float) (r * Math.sqrt(random.nextFloat()));
		float angle = (float) (random.nextFloat() * Math.PI * 2f);
		float x = (float) (Math.cos(angle) * r) + from.x;
		float y = (float) (Math.sin(angle) * r) + from.y;
		return new Vector2f(x, y);
	}
	
	public static Vector2f getPointWithinRadiusUniform(Vector2f from, float minR, float maxR, Random random) {
		float r = (float) (minR + (maxR - minR) * Math.sqrt(random.nextFloat()));
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
	
	public static Color getStoryDarkBrighterColor() {
		return setAlpha(scaleColorOnly(getStoryOptionColor(), 0.65f), 255);
	}
	public static Color getStoryDarkColor() {
		return setAlpha(scaleColorOnly(getStoryOptionColor(), 0.4f), 175);
	}
	public static Color getStoryBrightColor() {
		Color bright = interpolateColor(getStoryOptionColor(), 
		   		setAlpha(Color.white, 255),
		   		0.35f);
		return bright;
	}
	public static Color getStoryOptionColor() {
		//return Misc.interpolateColor(Misc.getButtonTextColor(), Misc.getPositiveHighlightColor(), 0.5f);
		return Global.getSettings().getColor("storyOptionColor");
		//return Global.getSettings().getColor("tooltipTitleAndLightHighlightColor");
	}
	
	public static Color getHighlightedOptionColor() {
		return Global.getSettings().getColor("buttonShortcut");
	}
	
	public static Color getHighlightColor() {
		return Global.getSettings().getColor("buttonShortcut");
	}
	public static Color getDarkHighlightColor() {
		Color hc = Misc.getHighlightColor();
		return Misc.setAlpha(hc, 255);
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
		return getJoined("and", strings);
	}
	
	public static String getJoined(String joiner, List<String> strings) {
		return getJoined(joiner, strings.toArray(new String [0]));
	}
	public static String getJoined(String joiner, String ... strings) {
		if (strings.length == 1) return strings[0];
		
		String result = "";
		for (int i = 0; i < strings.length - 1; i++) {
			result += strings[i] + ", ";
		}
		if (!result.isEmpty()) {
			result = result.substring(0, result.length() - 2);
		}
		if (strings.length > 2) {
			if (joiner.isEmpty()) {
				result += ", " + strings[strings.length - 1];
			} else {
				result += ", " + joiner + " " + strings[strings.length - 1];
			}
		} else if (strings.length == 2) {
			if (joiner.isEmpty()) {
				result += ", " + strings[strings.length - 1];
			} else {
				result += " " + joiner + " " + strings[strings.length - 1];
			}
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
		if (location == null) return new ArrayList<MarketAPI>();
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
	public static List<MarketAPI> getPlayerMarkets(boolean includeNonPlayerFaction) {
		FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
		List<MarketAPI> result = new ArrayList<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.getFaction() == player) {
				result.add(market);
			} else if (includeNonPlayerFaction && market.isPlayerOwned()) {
				result.add(market);
			}
		}
		return result;
	}
	
	public static List<StarSystemAPI> getPlayerSystems(boolean includeNonPlayerFaction) {
		return getSystemsWithPlayerColonies(includeNonPlayerFaction);
	}
	public static List<StarSystemAPI> getSystemsWithPlayerColonies(boolean includeNonPlayerFaction) {
		List<MarketAPI> markets = Misc.getPlayerMarkets(includeNonPlayerFaction);
		List<StarSystemAPI> systems = new ArrayList<StarSystemAPI>();
		for (MarketAPI market : markets) {
			StarSystemAPI system = market.getStarSystem();
			if (system != null && !systems.contains(system)) {
				systems.add(system);
			}
		}
		return systems;
	}
	
	public static List<MarketAPI> getFactionMarkets(String factionId) {
		return getFactionMarkets(Global.getSector().getFaction(factionId));
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
		
		StarSystemAPI closest = null;
		float minDist = Float.MAX_VALUE;
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			float dist = Misc.getDistanceLY(token.getLocationInHyperspace(), system.getLocation());
			if (dist > maxRangeLY) continue;
			if (dist < minDist) {
				minDist = dist;
				closest = system;
			}
			//return system;
		}
		return closest;
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
	
	public static Vector2f getUnitVector(Vector2f from, Vector2f to) {
		return getUnitVectorAtDegreeAngle(getAngleInDegrees(from, to));
	}
	
	public static float RAD_PER_DEG = 0.01745329251f;
	public static Vector2f getUnitVectorAtDegreeAngle(float degrees) {
		Vector2f result = new Vector2f();
		float radians = degrees * RAD_PER_DEG;
		result.x = (float)Math.cos(radians);
		result.y = (float)Math.sin(radians);
		
		return result;
	}
	
	public static Vector2f rotateAroundOrigin(Vector2f v, float angle) {
		float cos = (float) Math.cos(angle * RAD_PER_DEG);
		float sin = (float) Math.sin(angle * RAD_PER_DEG);
		Vector2f r = new Vector2f();
		r.x = v.x * cos - v.y * sin;
		r.y = v.x * sin + v.y * cos;
		return r;
	}
	
	public static Vector2f rotateAroundOrigin(Vector2f v, float angle, Vector2f origin) {
		float cos = (float) Math.cos(angle * RAD_PER_DEG);
		float sin = (float) Math.sin(angle * RAD_PER_DEG);
		Vector2f r = Vector2f.sub(v, origin, new Vector2f());
		Vector2f r2 = new Vector2f();
		r2.x = r.x * cos - r.y * sin;
		r2.y = r.x * sin + r.y * cos;
		Vector2f.add(r2, origin, r2);
		return r2;
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
	
	public static Color genColor(Color min, Color max, Random random) {
		Color color = new Color((int) (min.getRed() + (max.getRed() - min.getRed()) * random.nextDouble()),
				(int) (min.getGreen() + (max.getGreen() - min.getGreen()) * random.nextDouble()),
				(int) (min.getBlue() + (max.getBlue() - min.getBlue()) * random.nextDouble()),
				255);
		
		return color;
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
	
	public static void makeHostileToPlayerTradeFleets(CampaignFleetAPI fleet) {
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE_TO_PLAYER_TRADE_FLEETS, true);
	}
	
	public static void makeHostileToAllTradeFleets(CampaignFleetAPI fleet) {
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE_TO_ALL_TRADE_FLEETS, true);
	}
	
	public static void makeNonHostileToFaction(CampaignFleetAPI fleet, String factionId, float dur) {
		makeNonHostileToFaction(fleet, factionId, true, dur);
	}
	public static void makeNonHostileToFaction(CampaignFleetAPI fleet, String factionId, boolean nonHostile, float dur) {
		String flag = MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE + "_" + factionId;
		if (!nonHostile) {
			fleet.getMemoryWithoutUpdate().unset(flag);
		} else {
			fleet.getMemoryWithoutUpdate().set(flag, true, dur);
		}
	}
	public static void makeHostileToFaction(CampaignFleetAPI fleet, String factionId, float dur) {
		makeHostileToFaction(fleet, factionId, true, dur);
	}
	public static void makeHostileToFaction(CampaignFleetAPI fleet, String factionId, boolean hostile, float dur) {
		String flag = MemFlags.MEMORY_KEY_MAKE_HOSTILE + "_" + factionId;
		if (!hostile) {
			fleet.getMemoryWithoutUpdate().unset(flag);
		} else {
			fleet.getMemoryWithoutUpdate().set(flag, true, dur);
		}
	}
	
	public static boolean isFleetMadeHostileToFaction(CampaignFleetAPI fleet, FactionAPI faction) {
		return isFleetMadeHostileToFaction(fleet, faction.getId());
	}
	public static boolean isFleetMadeHostileToFaction(CampaignFleetAPI fleet, String factionId) {
		if (Factions.PLAYER.equals(factionId) && 
				fleet.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MAKE_HOSTILE)) {
			return true;
		}
		String flag = MemFlags.MEMORY_KEY_MAKE_HOSTILE + "_" + factionId;
		return fleet.getMemoryWithoutUpdate().getBoolean(flag);
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
	
	public static String getDetailedAgoString(long timestamp) {
		CampaignClockAPI clock = Global.getSector().getClock();
		int days = (int) clock.getElapsedDaysSince(timestamp);
		
		if (days == 0) {
			return "0 days ago";
		} else if (days == 1) {
			return "1 day ago";
		} else if (days <= 6) {
			return (int)Math.ceil(days) + " days ago";
		} else if (days <= 7) {
			return "1 week ago";
		} else if (days <= 14) {
			return "2 weeks ago";
		} else if (days <= 21) {
			return "3 weeks ago";
		} else {
			int months = days / 30;
			if (months <= 12) {
				if (months <= 1) {
					return "1 month ago";
				} else {
					return "" + months + " months ago";
				}
			} else {
				int years = months / 12;
				if (years <= 1) {
					return "1 cycle ago";
				} else {
					return "" + years + " cycles ago";
				}
			}
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
		// 1/1/20: changed to not add +0.01f; not sure why it was there but could cause issues w/ isSlowMoving(), maybe?
		//return Math.round(currBurn + 0.01f);
		return Math.round(currBurn);
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
		if (denom == 0) return p1;
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
	
	public static boolean isPointInBounds(Vector2f p1, List<Vector2f> bounds) {
		Vector2f p2 = new Vector2f(p1);
		p2.x += 10000;
		int count = 0;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < bounds.size() - 1; j++) {
				Vector2f s1 = bounds.get(j);
				Vector2f s2 = bounds.get(j + 1);
				Vector2f p = intersectSegments(p1, p2, s1, s2);
				if (p != null) {
					if (Math.abs(p.x - s1.x) < 0.001f && 
						Math.abs(p.y - s1.y) < 0.001f) {
						continue; // JUST the first point, (p1, p2]
					}
					if (areSegmentsCoincident(p1, p2, s1, s2)) {
						continue;
					}
					count++;
				}
			}
			if (i == 0 && count % 2 == 1) {
				count = 0;
				p2.y += 100;
			} else {
				break;
			}
		}
		return count % 2 == 1;
	}
	
	public static Vector2f intersectSegments(Vector2f a1, Vector2f a2, Vector2f b1, Vector2f b2) {
		float denom = (b2.y - b1.y) * (a2.x - a1.x) - (b2.x - b1.x) * (a2.y - a1.y);
		float numUa = (b2.x - b1.x) * (a1.y - b1.y) - (b2.y - b1.y) * (a1.x - b1.x);
		float numUb = (a2.x - a1.x) * (a1.y - b1.y) - (a2.y - a1.y) * (a1.x - b1.x);

		if (denom == 0 && !(numUa == 0 && numUb == 0)) { // parallel, not coincident
			return null;
		}

		if (denom == 0 && numUa == 0 && numUb == 0) { // coincident
			float minX, minY, maxX, maxY;
			if (a1.x < a2.x) {
				minX = a1.x;
				maxX = a2.x;
			} else {
				minX = a2.x;
				maxX = a1.x;
			}
			if (a1.y < a2.y) {
				minY = a1.y;
				maxY = a2.y;
			} else {
				minY = a2.y;
				maxY = a1.y;
			}
			// if either one of the endpoints in segment b is between the points in segment a,
			// return that endpoint as the intersection.  Otherwise, no intersection.
			if (b1.x >= minX && b1.x <= maxX && b1.y >= minY && b1.y <= maxY) {
				return new Vector2f(b1);
			} else if (b2.x >= minX && b2.x <= maxX && b2.y >= minY && b2.y <= maxY) {
				return new Vector2f(b2);
			} else {
				return null;
			}
		}

		float Ua = numUa / denom;
		float Ub = numUb / denom;
		if (Ua >=0 && Ua <= 1 && Ub >= 0 && Ub <= 1) { // segments intersect
			Vector2f result = new Vector2f();
//			if (Ua <= 0.001f) {
//				result.x = a1.x;
//				result.y = a1.y;
//			} else if (Ua >= 0.999f) {
//				result.x = a2.x;
//				result.y = a2.y;
//			} else {
				result.x = a1.x + Ua * (a2.x - a1.x);
				result.y = a1.y + Ua * (a2.y - a1.y);
//			}
			return result;
		} else { // lines intersect, but segments do not
			return null;
		}

	}
	
	
	public static Vector2f intersectLines(Vector2f a1, Vector2f a2, Vector2f b1, Vector2f b2) {
		float denom = (b2.y - b1.y) * (a2.x - a1.x) - (b2.x - b1.x) * (a2.y - a1.y);
		float numUa = (b2.x - b1.x) * (a1.y - b1.y) - (b2.y - b1.y) * (a1.x - b1.x);
		float numUb = (a2.x - a1.x) * (a1.y - b1.y) - (a2.y - a1.y) * (a1.x - b1.x);

		if (denom == 0 && !(numUa == 0 && numUb == 0)) { // parallel, not coincident
			return null;
		}

		if (denom == 0 && numUa == 0 && numUb == 0) { // coincident
			return new Vector2f(a1);
		}

		float Ua = numUa / denom;
		float Ub = numUb / denom;
		Vector2f result = new Vector2f();
		result.x = a1.x + Ua * (a2.x - a1.x);
		result.y = a1.y + Ua * (a2.y - a1.y);
		return result;
	}
	
	
	
	
	/**
	 * Going from p1 to p2.  Returns the closer intersection.
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param r
	 * @return
	 */
	public static Vector2f intersectSegmentAndCircle(Vector2f p1, Vector2f p2, Vector2f p3, float r) {

		float uNom = (p3.x - p1.x) * (p2.x - p1.x) + (p3.y - p1.y) * (p2.y - p1.y);
		float uDenom = (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y);

		Vector2f closest = new Vector2f();
		if (uDenom == 0) { // p1 and p2 are coincident
			closest.set(p1);
		} else {
			float u = uNom / uDenom;
			closest.x = p1.x + u * (p2.x - p1.x);
			closest.y = p1.y + u * (p2.y - p1.y);
		}

		float distSq = (closest.x - p3.x) * (closest.x - p3.x) + (closest.y - p3.y) * (closest.y - p3.y);
		if (distSq > r * r) { // closest point is farther than radius
			//System.out.println("shorted");
			return null;
		} else if (uDenom == 0) {
			return closest; // in the case where p1==p2 and they're inside the circle, return p1.
		}

		float a = (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y);
		float b = 2f * ( (p2.x - p1.x) * (p1.x - p3.x) + (p2.y - p1.y) *
				(p1.y - p3.y) );
		float c = p3.x * p3.x + p3.y * p3.y + p1.x * p1.x + p1.y * p1.y - 2f
				* (p3.x * p1.x + p3.y * p1.y) - r * r;

		float bb4ac = b * b - 4f * a * c;

		if (bb4ac < 0) return null;

		float mu1 = (-b + (float) Math.sqrt(bb4ac)) / (2 * a);
		float mu2 = (-b - (float) Math.sqrt(bb4ac)) / (2 * a);

		float minMu = mu1;
		if ((mu2 < minMu && mu2 >= 0) || minMu < 0) minMu = mu2;

		if (minMu < 0 || minMu > 1) {
			float p2DistSq = (p2.x - p3.x) * (p2.x - p3.x) + (p2.y - p3.y) * (p2.y - p3.y);
			if (p2DistSq <= r * r) return p2;
			else return null;
		}
		//System.out.println("mu1: " + mu1 + ", mu2: " + mu2);

		Vector2f result = new Vector2f();
		result.x = p1.x + minMu * (p2.x - p1.x);
		result.y = p1.y + minMu * (p2.y - p1.y);

		return result;
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
	
	public static float getClosestTurnDirection(float facing, float desired) {
		float diff = Misc.normalizeAngle(desired) - Misc.normalizeAngle(facing);
		if (diff < 0) diff += 360;
		
		if (diff == 0 || diff == 360f) {
			return 0f;
		} else if (diff > 180) {
			return -1f;
		} else {
			return 1f;
		}
//		facing = normalizeAngle(facing);
//		desired = normalizeAngle(desired);
//		if (facing == desired) return 0;
//		
//		Vector2f desiredVec = getUnitVectorAtDegreeAngle(desired);
//		//if (desiredVec.lengthSquared() == 0) return 0;
//		Vector2f more = getUnitVectorAtDegreeAngle(facing + 1);
//		Vector2f less = getUnitVectorAtDegreeAngle(facing - 1);
//		
//		float fromMore = Vector2f.angle(more, desiredVec);
//		float fromLess = Vector2f.angle(less, desiredVec);
//		if (fromMore > fromLess) return -1f;
//		return 1f;
	}
	
	public static float getClosestTurnDirection(float facing, Vector2f from, Vector2f to) {
		float diff = Misc.normalizeAngle(getAngleInDegrees(from, to)) - Misc.normalizeAngle(facing);
		if (diff < 0) diff += 360;
		
		if (diff == 0 || diff == 360f) {
			return 0f;
		} else if (diff > 180) {
			return -1f;
		} else {
			return 1f;
		}
//		Vector2f desired = getDiff(to, from);
//		if (desired.lengthSquared() == 0) return 0;
//		//float angle = getAngleInDegrees(desired);
//		Vector2f more = getUnitVectorAtDegreeAngle(facing + 1);
//		Vector2f less = getUnitVectorAtDegreeAngle(facing - 1);
//		
//		float fromMore = Vector2f.angle(more, desired);
//		float fromLess = Vector2f.angle(less, desired);
//		if (fromMore == fromLess) return 0f;
//		if (fromMore > fromLess) return -1f;
//		return 1f;
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
	
	public static SectorEntityToken getSourceEntity(CampaignFleetAPI fleet) {
		String id = fleet.getMemoryWithoutUpdate().getString(MemFlags.MEMORY_KEY_SOURCE_MARKET);
		if (id == null) return null;
		MarketAPI market = Global.getSector().getEconomy().getMarket(id);
		if (market != null && market.getPrimaryEntity() != null) {
			return market.getPrimaryEntity();
		}
		SectorEntityToken entity = Global.getSector().getEntityById(id);
		return entity;
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
		if (v.length() == 0 || v.lengthSquared() == 0) {
			v.x += max * 0.25f;
		}
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
	
	
//	public static void main(String[] args) {
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
//	}

	public static CampaignTerrainAPI getHyperspaceTerrain() {
		for (CampaignTerrainAPI curr : Global.getSector().getHyperspace().getTerrainCopy()) {
			if (curr.getPlugin() instanceof HyperspaceTerrainPlugin) {
				return curr;
			}
		}
		return null;
	}
	public static HyperspaceTerrainPlugin getHyperspaceTerrainPlugin() {
		CampaignTerrainAPI hyper = getHyperspaceTerrain();
		if (hyper != null) {
			return (HyperspaceTerrainPlugin) hyper.getPlugin();
		}
		return null;
	}
	
	public static boolean isInAbyss(Vector2f loc) {
		return getAbyssalDepth(loc) > 0;
	}
	public static boolean isInAbyss(SectorEntityToken entity) {
		return getAbyssalDepth(entity) > 0;
	}
	public static float getAbyssalDepth(Vector2f loc) {
		return getAbyssalDepth(loc, false);
	}
	public static float getAbyssalDepth(Vector2f loc, boolean uncapped) {
		HyperspaceTerrainPlugin plugin = getHyperspaceTerrainPlugin();
		if (plugin == null) return 0f;
		return plugin.getAbyssalDepth(loc, uncapped);
	}
	
	public static List<StarSystemAPI> getAbyssalSystems() {
		HyperspaceTerrainPlugin plugin = getHyperspaceTerrainPlugin();
		if (plugin == null) return new ArrayList<StarSystemAPI>();
		return plugin.getAbyssalSystems();
	}
	
	public static float getAbyssalDepthOfPlayer() {
		return getAbyssalDepth(Global.getSector().getPlayerFleet());
	}
	public static float getAbyssalDepthOfPlayer(boolean uncapped) {
		return getAbyssalDepth(Global.getSector().getPlayerFleet(), uncapped);
	}
	public static float getAbyssalDepth(SectorEntityToken entity) {
		return getAbyssalDepth(entity, false);
	}
	public static float getAbyssalDepth(SectorEntityToken entity, boolean uncapped) {
		if (entity == null || !entity.isInHyperspace()) return 0f;
		return getAbyssalDepth(entity.getLocation());
	}
	
	public static boolean isInsideBlackHole(CampaignFleetAPI fleet, boolean includeEventHorizon) {
		for (PlanetAPI planet : fleet.getContainingLocation().getPlanets()) {
			if (planet.isStar() && planet.getSpec() != null && planet.getSpec().isBlackHole()) {
				float dist = Misc.getDistance(fleet, planet);
				if (dist < planet.getRadius() + fleet.getRadius()) {
					return true;
				} else if (includeEventHorizon) {
					StarCoronaTerrainPlugin corona = getCoronaFor(planet);
					if (corona != null && corona.containsEntity(fleet)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	public static StarCoronaTerrainPlugin getCoronaFor(PlanetAPI star) {
		if (star == null) return null;
		
		for (CampaignTerrainAPI curr : star.getContainingLocation().getTerrainCopy()) {
			if (curr.getPlugin() instanceof StarCoronaTerrainPlugin) {
				StarCoronaTerrainPlugin corona = (StarCoronaTerrainPlugin) curr.getPlugin();
				if (corona.getRelatedEntity() == star) return corona;
			}
		}
		return null;
	}
	
	public static MagneticFieldTerrainPlugin getMagneticFieldFor(PlanetAPI planet) {
		if (planet == null || planet.getContainingLocation() == null) return null;
		
		for (CampaignTerrainAPI curr : planet.getContainingLocation().getTerrainCopy()) {
			if (curr.getPlugin() instanceof MagneticFieldTerrainPlugin) {
				MagneticFieldTerrainPlugin field = (MagneticFieldTerrainPlugin) curr.getPlugin();
				if (field.getRelatedEntity() == planet) return field;
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
		
		// prevents "infinitely chase player, re-interact, and don't demand anything or act hostile" scenario
		if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE)) {
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
	
	public static ShipAPI findClosestShipTo(ShipAPI ship, Vector2f locFromForSorting, HullSize smallestToNote, float maxRange, boolean considerShipRadius, boolean allowHulks, FindShipFilter filter) {
		CombatEngineAPI engine = Global.getCombatEngine();
		List<ShipAPI> ships = engine.getShips();
		float minDist = Float.MAX_VALUE;
		ShipAPI closest = null;
		for (ShipAPI other : ships) {
			if (other == ship) continue;
			if (other.getHullSize().ordinal() < smallestToNote.ordinal()) continue;
			if (other.isShuttlePod()) continue;
			if (other.isHulk()) continue;
			if (allowHulks || other.getOwner() != 100) {
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
		return mapToEnum(json, key, enumType, defaultOption, true);
	}
	public static <T extends Enum<T>> T mapToEnum(JSONObject json, String key, Class<T> enumType, T defaultOption, boolean required) throws JSONException {
		String val = json.optString(key);
		if (val == null || val.equals("")) {
			if (defaultOption == null && required) {
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
	
	public static Vector2f getVector(JSONObject json, String arrayKey, Vector2f def) throws JSONException {
		if (!json.has(arrayKey)) return def;
		return getVector(json, arrayKey);
	}
	public static Vector2f getVector(JSONObject json, String arrayKey) throws JSONException {
		Vector2f v = new Vector2f();
		JSONArray arr = json.getJSONArray(arrayKey);
		v.set((float) arr.getDouble(0), (float) arr.getDouble(1));
		return v;
	}
	
	public static Vector3f getVector3f(JSONObject json, String arrayKey) throws JSONException {
		Vector3f v = new Vector3f();
		JSONArray arr = json.getJSONArray(arrayKey);
		v.set((float) arr.getDouble(0), (float) arr.getDouble(1), (float) arr.getDouble(1));
		return v;
	}
	
	public static Vector2f optVector(JSONObject json, String arrayKey) {
		Vector2f v = new Vector2f();
		JSONArray arr = json.optJSONArray(arrayKey);
		if (arr == null) return null;
		v.set((float) arr.optDouble(0), (float) arr.optDouble(1));
		return v;
	}
	
	public static Vector3f optVector3f(JSONObject json, String arrayKey) throws JSONException {
		Vector3f v = new Vector3f();
		JSONArray arr = json.optJSONArray(arrayKey);
		if (arr == null) return new Vector3f();
		v.set((float) arr.getDouble(0), (float) arr.getDouble(1), (float) arr.getDouble(2));
		return v;
	}	
	
	public static Vector2f getVector(JSONObject json, String arrayKey, int index) throws JSONException {
		Vector2f v = new Vector2f();
		JSONArray arr = json.getJSONArray(arrayKey);
		v.set((float) arr.getDouble(index * 2 + 0), (float) arr.getDouble(index * 2 + 1));
		return v;
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
		if (random == null) random = Misc.random;
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

	public static Map<Class, Method> cleanerCache = new LinkedHashMap<>();
	public static Map<Class, Method> cleanCache = new LinkedHashMap<>();
    
	public static void cleanBuffer(Buffer toBeDestroyed) {
    	try {
    		
    		Method cleanerMethod = cleanerCache.get(toBeDestroyed.getClass());
    		if (cleanerMethod == null) {
    			cleanerMethod = toBeDestroyed.getClass().getMethod("cleaner");
    			if (cleanerMethod != null) {
	    			cleanerMethod.setAccessible(true);
	    			cleanerCache.put(toBeDestroyed.getClass(), cleanerMethod);
    			}
    		}
    		if (cleanerMethod != null) {
		    	Object cleaner = cleanerMethod.invoke(toBeDestroyed);
		    	if (cleaner != null) {
		    		Method cleanMethod = cleanCache.get(cleaner.getClass());
		    		if (cleanMethod == null) {
		    			cleanMethod = cleaner.getClass().getMethod("clean");
		    			if (cleanMethod != null) {
			    			cleanMethod.setAccessible(true);
			    			cleanCache.put(cleaner.getClass(), cleanMethod);
		    			}
		    		}
		    		if (cleanMethod != null) {
			    		cleanMethod.invoke(cleaner);
			    		Global.getLogger(Misc.class).info(String.format("Cleaned buffer (using reflection)"));
		    		} else {
		    			Global.getLogger(Misc.class).warn(String.format("Buffer can not be cleaned"));
		    		}
		    	} else {
		    		Global.getLogger(Misc.class).warn(String.format("Buffer can not be cleaned"));
		    	}
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
		return getFleetwideTotalMod(fleet, dynamicMemberStatId, base, null);
	}
	public static float getFleetwideTotalMod(CampaignFleetAPI fleet, String dynamicMemberStatId, float base, ShipAPI ship) {
		float total = 0;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.isMothballed()) continue;
			if (ship != null && ship.getFleetMember() == member) {
				total += ship.getMutableStats().getDynamic().getValue(dynamicMemberStatId, base);
			} else {
				total += member.getStats().getDynamic().getValue(dynamicMemberStatId, base);
			}
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
	
	public static void setAllPlanetsSurveyed(StarSystemAPI system, boolean setRuinsExplored) {
		for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isStar()) continue;
			
			MarketAPI market = planet.getMarket();
			if (market == null) continue;
			
			market.setSurveyLevel(SurveyLevel.FULL);
			for (MarketConditionAPI mc : market.getConditions()) {
				mc.setSurveyed(true);
			}
			
			if (setRuinsExplored && Misc.hasRuins(market)) {
				market.getMemoryWithoutUpdate().set("$ruinsExplored", true);
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
		if (seed == 0) return random;
		
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
		
		if (planet.getSpec().hasTag(Tags.CODEX_UNLOCKABLE)) {
			SharedUnlockData.get().reportPlayerAwareOfPlanet(planet.getSpec().getPlanetType(), true);
		}
		
		CodexUnlocker.makeAwareOfConditionsOn(planet.getMarket());
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
		if (entity.hasTag(Tags.FADING_OUT_AND_EXPIRING)) return;
		
		entity.addTag(Tags.NON_CLICKABLE);
		entity.addTag(Tags.FADING_OUT_AND_EXPIRING);
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
	
	public static void fadeIn(final SectorEntityToken entity, final float in) {
		entity.forceSensorFaderBrightness(0f);
		entity.setAlwaysUseSensorFaderBrightness(true);
		entity.addScript(new EveryFrameScript() {
			float elapsed = 0f;
			public boolean runWhilePaused() {
				return false;
			}
			public boolean isDone() {
				return elapsed > in;
			}
			public void advance(float amount) {
				elapsed += amount;
				if (elapsed > in) {
					entity.setAlwaysUseSensorFaderBrightness(false);
					return;
				}
				float b = elapsed / in;
				if (b < 0) b = 0;
				if (b > 1) b = 1;
				entity.forceSensorFaderBrightness(Math.min(entity.getSensorFaderBrightness(), b));
				entity.setAlwaysUseSensorFaderBrightness(true);
			}
		});
	}
//	public static void fadeSensorContactAndExpire(final SectorEntityToken entity, final float seconds) {
//		entity.addTag(Tags.NON_CLICKABLE);
//		entity.addTag(Tags.FADING_OUT_AND_EXPIRING);
//		//entity.getContainingLocation().addScript(new EveryFrameScript() {
//		entity.addScript(new EveryFrameScript() {
//			float elapsed = 0f;
//			public boolean runWhilePaused() {
//				return false;
//			}
//			public boolean isDone() {
//				return entity.isExpired();
//			}
//			public void advance(float amount) {
//				elapsed += amount;
//				if (elapsed > seconds) {
//					entity.setExpired(true);
//				}
//				float b = 1f - elapsed / seconds;
//				if (b < 0) b = 0;
//				if (b > 1) b = 1;
//				entity.forceSensorContactFaderBrightness(Math.min(entity.getSensorContactFaderBrightness(), b));
//			}
//		});
//	}
	
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
		if (random == null) random = Misc.random;
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

	public static boolean isUnboardable(FleetMemberAPI member) {
		if (member.getVariant() != null && member.getVariant().hasTag(Tags.VARIANT_UNBOARDABLE)) {
			return true;
		}
		return isUnboardable(member.getHullSpec());
	}
	
	public static boolean isUnboardable(ShipHullSpecAPI hullSpec) {
		if (hullSpec.getHints().contains(ShipTypeHints.UNBOARDABLE)) {
			for (String tag : getAllowedRecoveryTags()) {
				if (hullSpec.hasTag(tag)) return false;
			}
			if (hullSpec.isDefaultDHull()) {
				ShipHullSpecAPI parent = hullSpec.getDParentHull();
				for (String tag : getAllowedRecoveryTags()) {
					if (parent.hasTag(tag)) return false;
				}
			}
			return true;
		}
		return false;
	}
	

	public static boolean isShipRecoverable(FleetMemberAPI member, CampaignFleetAPI recoverer, boolean own, boolean useOfficerRecovery, float chanceMult) {
		//Random rand = new Random(1000000 * (member.getId().hashCode() + seed + Global.getSector().getClock().getDay()));
		//Random rand = new Random(1000000 * (member.getId().hashCode() + Global.getSector().getClock().getDay()));
		if (own) {
			if (!member.getVariant().getSMods().isEmpty()) {
				return true;
			}
			if (!member.getVariant().getSModdedBuiltIns().isEmpty()) {
				return true;
			}
			if (member.getCaptain() != null && !member.getCaptain().isDefault()) {
				return true;
			}
		}
		if (member.getVariant().hasTag(Tags.VARIANT_ALWAYS_RECOVERABLE)) {
			return true;
		}
		Random rand = new Random(1000000 * member.getId().hashCode() + Global.getSector().getPlayerBattleSeed());
		//rand = new Random();
		float chance = Global.getSettings().getFloat("baseShipRecoveryChance");
		if (own) {
			chance = Global.getSettings().getFloat("baseOwnShipRecoveryChance");
		}
		chance = member.getStats().getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).computeEffective(chance);
		if (recoverer != null) {
			chance = recoverer.getStats().getDynamic().getMod(Stats.SHIP_RECOVERY_MOD).computeEffective(chance);
			if (useOfficerRecovery) {
				chance = recoverer.getStats().getDynamic().getMod(Stats.OFFICER_SHIP_RECOVERY_MOD).computeEffective(chance);
			}
		}
		chance *= chanceMult;
		
		if (chance < 0) chance = 0;
		if (chance > 1f) chance = 1f;
		boolean recoverable = rand.nextFloat() < chance; 
		
//		System.out.println("Recovery for " + member.getHullSpec().getHullId() + 
//				"(" + member.getId().hashCode() + "): " + chance + " (" + recoverable + ")");
		return recoverable;
	}

//	public static float computeDetectionRangeForEntity(float radius) {
//		float range = 300f + radius * 5f;
//		if (range > 2000) range = 2000;
//		return radius;
//	}
	
	
	
	public static JumpPointAPI findNearestJumpPointTo(SectorEntityToken entity) {
		return findNearestJumpPointTo(entity, false);
	}
	public static JumpPointAPI findNearestJumpPointTo(SectorEntityToken entity, boolean allowWormhole) {
		float min = Float.MAX_VALUE;
		JumpPointAPI result = null;
		List<JumpPointAPI> points = entity.getContainingLocation().getEntities(JumpPointAPI.class);
		
		for (JumpPointAPI curr : points) {
			if (!allowWormhole && curr.isWormhole()) continue;
			if (curr.getMemoryWithoutUpdate().getBoolean(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY)) {
				continue;
			}
			float dist = Misc.getDistance(entity.getLocation(), curr.getLocation());
			if (dist < min) {
				min = dist;
				result = curr;
			}
		}
		return result;
	}
	
	public static JumpPointAPI findNearestJumpPointThatCouldBeExitedFrom(SectorEntityToken entity) {
		float min = Float.MAX_VALUE;
		JumpPointAPI result = null;
		List<JumpPointAPI> points = entity.getContainingLocation().getEntities(JumpPointAPI.class);
		
		for (JumpPointAPI curr : points) {
			if (curr.isGasGiantAnchor() || curr.isStarAnchor()) continue;
			float dist = Misc.getDistance(entity.getLocation(), curr.getLocation());
			if (dist < min) {
				min = dist;
				result = curr;
			}
		}
		return result;
	}
	
	public static SectorEntityToken findNearestPlanetTo(SectorEntityToken entity, boolean requireGasGiant, boolean allowStars) {
		float min = Float.MAX_VALUE;
		SectorEntityToken result = null;
		List<PlanetAPI> planets = entity.getContainingLocation().getPlanets();
		
		for (PlanetAPI curr : planets) {
			if (requireGasGiant && !curr.isGasGiant()) continue;
			if (!allowStars && curr.isStar()) continue;
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
	
	@SuppressWarnings("unchecked")
	public static JumpPointAPI findNearestJumpPoint(SectorEntityToken from) {
		float min = Float.MAX_VALUE;
		JumpPointAPI result = null;
		LocationAPI location = from.getContainingLocation();
		List<JumpPointAPI> points = location.getEntities(JumpPointAPI.class);
		for (JumpPointAPI curr : points) {
			float dist = Misc.getDistance(from.getLocation(), curr.getLocation());
			if (dist < min) {
				min = dist;
				result = curr;
			}
		}
		return result;
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
	
	public static String getPlanetSurveyClass(PlanetAPI planet) {
		SurveyPlugin plugin = (SurveyPlugin) Global.getSettings().getNewPluginInstance("surveyPlugin");
		String type = plugin.getSurveyDataType(planet);
		if (type != null) {
			CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(type);
			String classStr = spec.getName().replaceFirst(" Survey Data", "");
			return classStr;
		}
		return "Class N";
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
	
	public static void setPrevSalvageSpecial(SectorEntityToken entity, Object data) {
		entity.getMemoryWithoutUpdate().set(MemFlags.PREV_SALVAGE_SPECIAL_DATA, data);
	}
	
	public static Object getSalvageSpecial(SectorEntityToken entity) {
		return entity.getMemoryWithoutUpdate().get(MemFlags.SALVAGE_SPECIAL_DATA);
	}
	public static Object getPrevSalvageSpecial(SectorEntityToken entity) {
		return entity.getMemoryWithoutUpdate().get(MemFlags.PREV_SALVAGE_SPECIAL_DATA);
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

//	public static boolean systemHasPulsar(StarSystemAPI system) {
//		return hasPulsar(system);
////		boolean result = (system.getStar() != null && system.getStar().getSpec().isPulsar()) ||
////		   (system.getSecondary() != null && system.getSecondary().getSpec().isPulsar()) ||
////		   (system.getTertiary() != null && system.getTertiary().getSpec().isPulsar());
////		return result;
//	}
	public static PlanetAPI getPulsarInSystem(StarSystemAPI system) {
		if (system.getStar() != null && system.getStar().getSpec().isPulsar()) {
			return system.getStar();
		}
		if (system.getSecondary() != null && system.getSecondary().getSpec().isPulsar()) {
			return system.getSecondary();
		}
		if (system.getTertiary() != null && system.getTertiary().getSpec().isPulsar()) {
			return system.getTertiary();
		}
		return null;
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
			if (curr instanceof JumpPointAPI && ((JumpPointAPI)curr).isWormhole()) {
				continue;
			}
			
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
	
	public static String FLEET_RETURNING_TO_DESPAWN = "$core_fleetReturningToDespawn";
	
	public static boolean isFleetReturningToDespawn(CampaignFleetAPI fleet) {
		return fleet.getMemoryWithoutUpdate().getBoolean(FLEET_RETURNING_TO_DESPAWN);
	}
	
	public static void giveStandardReturnToSourceAssignments(CampaignFleetAPI fleet, boolean withClear) {
		if (withClear) {
			fleet.clearAssignments();
		}
		fleet.getMemoryWithoutUpdate().set(FLEET_RETURNING_TO_DESPAWN, true);
		MarketAPI source = Misc.getSourceMarket(fleet);
		if (source != null) {
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, source.getPrimaryEntity(), 1000f, "returning to " + source.getName());
			fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, source.getPrimaryEntity(), 1f + 1f * (float) Math.random());
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, source.getPrimaryEntity(), 1000f);
		} else {
			SectorEntityToken entity = getSourceEntity(fleet);
			if (entity != null) {
				fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, entity, 1000f, "returning to " + entity.getName());
				fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, entity, 1f + 1f * (float) Math.random());
				fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, entity, 1000f);
			} else {
				SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
				fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, token, 1000f);
			}
		}
	}
	
	public static void giveStandardReturnAssignments(CampaignFleetAPI fleet, SectorEntityToken where, String text, boolean withClear) {
		if (withClear) {
			fleet.clearAssignments();
		}
		fleet.getMemoryWithoutUpdate().set(FLEET_RETURNING_TO_DESPAWN, true);
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
	
	public static Vector2f getInterceptPoint(SectorEntityToken from, SectorEntityToken to, float maxSpeedFrom) {
		
		//if (true) return new Vector2f(to.getLocation());
		//Vector2f v1 = new Vector2f(from.getVelocity());
		//Vector2f v2 = new Vector2f(to.getVelocity());
		Vector2f v2 = Vector2f.sub(to.getVelocity(), from.getVelocity(), new Vector2f());
		
		float s1 = maxSpeedFrom;
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
	
	
	public static boolean doesMarketHaveMissionImportantPeopleOrIsMarketMissionImportant(SectorEntityToken entity) {
		MarketAPI market = entity.getMarket();
		if (market == null) return false;
		if (market.getPrimaryEntity() != entity) return false;
		
		if (market.getMemoryWithoutUpdate().getBoolean(MemFlags.ENTITY_MISSION_IMPORTANT)) return true;
		
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
	
	public static boolean isImportantForReason(MemoryAPI memory, String reason) {
		String flagKey = MemFlags.ENTITY_MISSION_IMPORTANT;
		return flagHasReason(memory, flagKey, reason);
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
		return getSalvageSeed(entity, false);
	}
	public static long getSalvageSeed(SectorEntityToken entity, boolean nonRandom) {
		long seed = entity.getMemoryWithoutUpdate().getLong(MemFlags.SALVAGE_SEED);
		if (seed == 0) {
			//seed = new Random().nextLong();
			String id = entity.getId();
			if (id == null) id = genUID();
			if (nonRandom) {
				seed = (entity.getId().hashCode() * 17000) * 1181783497276652981L;
			} else {
				seed = seedUniquifier() ^ (entity.getId().hashCode() * 17000);
			}
			Random r = new Random(seed);
			for (int i = 0; i < 5; i++) {
				r.nextLong();
			}
			long result = r.nextLong();
			entity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, result);
			return result;
		}
		return seed;
	}
	
	
	public static long getNameBasedSeed(SectorEntityToken entity) {
		String id = entity.getName();
		if (id == null) id = genUID();
		
		long seed = (entity.getId().hashCode() * 17000);
		Random r = new Random(seed);
		for (int i = 0; i < 53; i++) {
			r.nextLong();
		}
		long result = r.nextLong();
		return result;
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
		
		if (fleet.wasSlowMoving()) {
			Vector2f vel = fleet.getVelocity();
			Vector2f neg = new Vector2f(vel);
			neg.negate();
			return getAngleInDegrees(neg);
		}
		
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

	public static SimulatorPlugin getSimulatorPlugin() {
		return (SimulatorPlugin) Global.getSettings().getPlugin("simulatorPlugin");
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
	
	public static AICoreOfficerPlugin getAICoreOfficerPlugin(String commodityId) {
		AICoreOfficerPlugin plugin = Global.getSector().getPluginPicker().pickAICoreOfficerPlugin(commodityId);
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
		if (market == null) return null;
		SubmarketAPI submarket = market.getSubmarket(Submarkets.SUBMARKET_STORAGE);
		if (submarket == null) return null;
		return (StoragePlugin) submarket.getPlugin();
	}
	
	public static SubmarketPlugin getLocalResources(MarketAPI market) {
		SubmarketAPI submarket = market.getSubmarket(Submarkets.LOCAL_RESOURCES);
		if (submarket == null) return null;
		return submarket.getPlugin();
	}
	public static CargoAPI getStorageCargo(MarketAPI market) {
		if (market == null) return null;
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
		return getOfficerSalary(officer, Misc.isMercenary(officer));
	}
	public static float getOfficerSalary(PersonAPI officer, boolean mercenary) {
		int officerBase = Global.getSettings().getInt("officerSalaryBase");
		int officerPerLevel = Global.getSettings().getInt("officerSalaryPerLevel");
		
		float payMult = 1f;
		if (mercenary) {
			payMult = Global.getSettings().getFloat("officerMercPayMult");
		}
		
		float salary = (officerBase + officer.getStats().getLevel() * officerPerLevel) * payMult;
		return salary;
	}
	
//	public static int getAccessibilityPercent(float a) {
//		int result = (int) Math.round(a * 100f);
//		if (a < 0 && result == 0) result = -1; // shipping penalty at "below zero"
//		return result;
//	}
	
	public static Map<String, Integer> variantToFPCache = new HashMap<String, Integer>();
	//public static Map<String, Boolean> variantToIsBaseCache = new HashMap<String, Boolean>();
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
	
//	public static boolean getIsBaseForVariantId(String variantId) {
//		Boolean isBase = variantToIsBaseCache.get(variantId);
//		if (isBase != null) return isBase;
////		System.out.println(variantId);
////		if (variantId.equals("buffalo2_FS")) {
////			System.out.println("wefwef");
////		}
//		ShipVariantAPI variant = Global.getSettings().getVariant(variantId);
//		isBase = variant.getHullSpec().hasTag(Items.TAG_BASE_BP);
//		variantToIsBaseCache.put(variantId, isBase);
//		
//		return isBase;
//	}
	
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
	
	public static SectorEntityToken getStationEntity(MarketAPI market, CampaignFleetAPI fleet) {
		for (SectorEntityToken entity : market.getConnectedEntities()) {
			if (entity.hasTag(Tags.STATION)) {
				CampaignFleetAPI curr = getStationFleet(entity);
				if (curr != null && curr == fleet) return entity;
			}
		}
		return null;
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
		return entity != null && entity.hasTag(Tags.SYSTEM_ANCHOR);
	}
	
	public static StarSystemAPI getStarSystemForAnchor(SectorEntityToken anchor) {
		return (StarSystemAPI) anchor.getMemoryWithoutUpdate().get(MemFlags.STAR_SYSTEM_IN_ANCHOR_MEMORY);
	}
	
	public static void showCost(TextPanelAPI text, Color color, Color dark, String [] res, int [] quantities) {
		showCost(text, "Resources: consumed (available)", true, color, dark, res, quantities);
	}
	public static void showCost(TextPanelAPI text, String title, boolean withAvailable, Color color, Color dark, String [] res, int [] quantities) {
		showCost(text, title, withAvailable, -1f, color, dark, res, quantities, null);
	}
	public static void showCost(TextPanelAPI text, String title, boolean withAvailable, float widthOverride, Color color, Color dark, String [] res, int [] quantities, boolean [] consumed) {
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
		
		if (widthOverride > 0) {
			cost.setComWidthOverride(widthOverride);
		}
		
		boolean dgs = true;
		for (int i = 0; i < res.length; i++) {
			String commodityId = res[i];
			int required = quantities[i];
			int available = (int) cargo.getCommodityQuantity(commodityId);
			Color curr = color;
			if (withAvailable && required > cargo.getQuantity(CargoItemType.RESOURCES, commodityId)) {
				curr = Misc.getNegativeHighlightColor();
			}
			if (dgs) {
				if (withAvailable) {
					cost.addCost(commodityId, Misc.getWithDGS(required) + " (" + Misc.getWithDGS(available) + ")", curr);
				} else {
					cost.addCost(commodityId, Misc.getWithDGS(required), curr);
				}
				if (consumed != null && consumed[i]) {
					cost.setLastCostConsumed(true);
				}
			} else {
				if (withAvailable) {
					cost.addCost(commodityId, "" + required + " (" + available + ")", curr);
				} else {
					cost.addCost(commodityId, "" + required, curr);
				}
				if (consumed != null && consumed[i]) {
					cost.setLastCostConsumed(true);
				}
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

	public static String getFleetType(CampaignFleetAPI fleet) {
		return fleet.getMemoryWithoutUpdate().getString(MemFlags.MEMORY_KEY_FLEET_TYPE);
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
		float sMods = 0f;
		if (member.getFleetData() != null && member.getFleetData().getFleet() != null) {
			CampaignFleetAPI fleet = member.getFleetData().getFleet();
			if (fleet.getInflater() != null && !fleet.isInflated()) {
				quality = fleet.getInflater().getQuality();
				sMods = fleet.getInflater().getAverageNumSMods();
			} else {
				BattleAPI battle = fleet.getBattle();
				CampaignFleetAPI source = battle == null ? null : battle.getSourceFleet(member);
				if (source != null && source.getInflater() != null &&
						!source.isInflated()) {
					quality = source.getInflater().getQuality();
					sMods = source.getInflater().getAverageNumSMods();
				} else {
					float dmods = DModManager.getNumDMods(member.getVariant());
					quality = 1f - Global.getSettings().getFloat("qualityPerDMod") * dmods;
					if (quality < 0) quality = 0f;
					
					sMods = member.getVariant().getSMods().size();
				}
			}
		}
		
		if (member.isStation()) {
			quality = 1f;
		}
		
		if (sMods > 0) {
			quality += sMods * Global.getSettings().getFloat("qualityPerSMod");
		}
		
		float captainMult = 1f;
		if (member.getCaptain() != null) {
			float captainLevel = (member.getCaptain().getStats().getLevel() - 1f);
			if (member.isStation()) {
				captainMult += captainLevel / (MAX_OFFICER_LEVEL * 2f);
			} else {
				captainMult += captainLevel / MAX_OFFICER_LEVEL;
			}
		}
		
		if (withQuality) {
			//str *= Math.max(0.25f, 0.5f + quality);
			str *= Math.max(0.25f, 0.8f + quality * 0.4f);
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

	
	public static LabelAPI addDesignTypePara(TooltipMakerAPI tooltip, String design, float pad) {
		if (design != null && !design.isEmpty()) {
			return tooltip.addPara("Design type: %s", pad, Misc.getGrayColor(), Global.getSettings().getDesignTypeColor(design), design);
		}
		return null;
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
		addHitGlow(location, loc, vel, size, dur, color);
	}
	public static void addHitGlow(LocationAPI location, Vector2f loc, Vector2f vel, float size, float dur, Color color) {
		location.addHitParticle(loc, vel,
				size, 0.4f, dur, color);
		location.addHitParticle(loc, vel,
				size * 0.25f, 0.4f, dur, color);
		location.addHitParticle(loc, vel,
				size * 0.15f, 1f, dur, Color.white);
	}
	
	public static ParticleControllerAPI [] addGlowyParticle(LocationAPI location, Vector2f loc, Vector2f vel, float size, float rampUp, float dur, Color color) {
		//rampUp = 0f;
		//dur = 3f;
		
		ParticleControllerAPI [] result = new ParticleControllerAPI[3];
		
		result[0] = location.addParticle(loc, vel,
				size, 0.4f, rampUp, dur, color);
		result[1] = location.addParticle(loc, vel,
				size * 0.25f, 0.4f, rampUp, dur, color);
		result[2] = location.addParticle(loc, vel,
				size * 0.15f, 1f, rampUp, dur, Color.white);
		
		return result;
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
		return market != null && market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_MILITARY);
	}
	
	public static boolean hasHeavyIndustry(MarketAPI market) {
		boolean heavyIndustry = false;
		for (Industry curr : market.getIndustries()) {
			if (curr.getSpec().hasTag(Industries.TAG_HEAVYINDUSTRY)) {
				heavyIndustry = true;
			}
		}
		return heavyIndustry;
	}
	
	public static boolean hasOrbitalStation(MarketAPI market) {
		for (Industry curr : market.getIndustries()) {
			if (curr.getSpec().hasTag(Industries.TAG_STATION)) {
				return true;
			}
		}
		return false;
	}
	
	public static FactionAPI getClaimingFaction(SectorEntityToken planet) {
		if (planet.getStarSystem() != null) {
			String claimedBy = planet.getStarSystem().getMemoryWithoutUpdate().getString(MemFlags.CLAIMING_FACTION);
			if (claimedBy != null) {
				return Global.getSector().getFaction(claimedBy);
			}
		}
		
		int max = 0;
		MarketAPI result = null;
		List<MarketAPI> markets = Global.getSector().getEconomy().getMarkets(planet.getContainingLocation());
		for (MarketAPI curr : markets) {
			if (curr.isHidden()) continue;
			if (curr.getFaction().isPlayerFaction()) continue;
			
			int score = curr.getSize();
			for (MarketAPI other : markets) {
				if (other != curr && other.getFaction() == curr.getFaction()) score++;
			}
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
	
		// since incentives no longer work this way...
//		float refundFraction = Global.getSettings().getFloat("industryRefundFraction");
//		float incentives = market.getIncentiveCredits() * refundFraction;
//		total += incentives;
		
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

	public static int getMaxOfficers(CampaignFleetAPI fleet) {
		int max = (int) fleet.getCommander().getStats().getOfficerNumber().getModifiedValue();
		return max;
	}
	public static int getNumNonMercOfficers(CampaignFleetAPI fleet) {
		int count = 0;
		for (OfficerDataAPI od : fleet.getFleetData().getOfficersCopy()) {
			if (!isMercenary(od.getPerson())) {
				count++;
			}
		}
		return count;
	}
	
	public static List<OfficerDataAPI> getMercs(CampaignFleetAPI fleet) {
		List<OfficerDataAPI> mercs = new ArrayList<OfficerDataAPI>();
		for (OfficerDataAPI od : fleet.getFleetData().getOfficersCopy()) {
			if (isMercenary(od.getPerson())) {
				mercs.add(od);
			}
		}
		return mercs;
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
	
	public static int getNumImprovedIndustries(MarketAPI market) {
		int count = 0;
		for (Industry curr : market.getIndustries()) {
			if (curr.isImproved()) {
				count++;
			}
		}
		return count;
	}
	
	public static int getNumStableLocations(StarSystemAPI system) {
		if (system == null) return 0;
		int count = system.getEntitiesWithTag(Tags.STABLE_LOCATION).size();
		count += system.getEntitiesWithTag(Tags.OBJECTIVE).size();
		for (SectorEntityToken e : system.getJumpPoints()) {
			if (e instanceof JumpPointAPI) {
				JumpPointAPI jp = (JumpPointAPI) e;
				if (jp.isWormhole()) count++;
			}
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
	
	
	public static float getHitGlowSize(float baseSize, float baseDamage, ApplyDamageResultAPI result) {
		if (result == null || baseDamage <= 0) return baseSize;
		
		float sd = result.getDamageToShields() + result.getOverMaxDamageToShields();
		float ad = result.getTotalDamageToArmor();
		float hd = result.getDamageToHull();
		float ed = result.getEmpDamage();
		DamageType type = result.getType();
		return getHitGlowSize(baseSize, baseDamage, type, sd, ad, hd, ed);
	}
	
	
	public static float getHitGlowSize(float baseSize, float baseDamage, DamageType type, float sd, float ad, float hd, float ed) {
		
		float minBonus = 0f;
		if (type == DamageType.KINETIC) {
			sd *= 0.5f;
			//if (sd > 0) minBonus = 0.1f;
		} else if (type == DamageType.HIGH_EXPLOSIVE) {
			ad *= 0.5f;
			if (ad > 0) minBonus = 0.1f;
		} else if (type == DamageType.FRAGMENTATION) {
			if (hd > 0) {
				minBonus = 0.2f * (hd / (hd + ad));
			}
			sd *= 2f;
			ad *= 2f;
			hd *= 2f;
		}
		
		float totalDamage = sd + ad + hd;
		if (totalDamage <= 0) return baseSize;
		
		// emp damage makes the hitglow normal-sized, but not bigger
		if (totalDamage < baseDamage) {
			totalDamage += ed;
			if (totalDamage > baseDamage) {
				totalDamage = baseDamage;
			}
		}
		
		float minSize = 15f;
		float minMult = minSize / baseSize;
		minMult = Math.max(minMult, 0.67f + minBonus);
		if (minMult > 1) minMult = 1;
		
		float mult = totalDamage / baseDamage;
		if (mult < minMult) mult = minMult;
		
		float maxMult = 1.5f;
		if (mult > maxMult) mult = maxMult;
		//mult = maxMult;
		//mult = 1f;
		//System.out.println("Mult: " + mult);
		return baseSize * mult;
	}
	
	public static int getNumEliteSkills(PersonAPI person) {
		int count = 0;
		for (SkillLevelAPI sl : person.getStats().getSkillsCopy()) {
			if (sl.getLevel() >= 2) count++;
		}
		return count;
	}
	
	
//	public static void spawnExtraHitGlow(Object param, Vector2f loc, Vector2f vel, float intensity) {
//		if (param instanceof DamagingProjectileAPI) {
//			DamagingProjectileAPI proj = (DamagingProjectileAPI) param;
//			ProjectileSpecAPI spec = proj.getProjectileSpec();
//			if (spec != null) {
//				CombatEngineAPI engine = Global.getCombatEngine();
//				float base = spec.getHitGlowRadius();
//				if (base == 0) base = spec.getLength() * 1.0f;
//				float size = base * (1f + 1f * intensity) * 0.5f;
//				if (size < 20) return;
//				
//				Color color = Misc.interpolateColor(spec.getFringeColor(), spec.getCoreColor(), 0.25f);
//				engine.addHitParticle(loc, vel, size, 0.75f * intensity, color);
//			}
//		} else if (param instanceof BeamAPI) {
//			BeamAPI beam = (BeamAPI) param;
//			CombatEngineAPI engine = Global.getCombatEngine();
//			float size = beam.getHitGlowRadius() * (1f + intensity) * 0.5f;
//			if (size < 20) return;
//			
//			Color color = Misc.interpolateColor(beam.getFringeColor(), beam.getCoreColor(), 0.25f);
//			engine.addHitParticle(loc, vel, size, 0.75f * intensity, color);
//		}
//	}
	
	public static String MENTORED = "$mentored";
	public static boolean isMentored(PersonAPI person) {
		return person.getMemoryWithoutUpdate().is(MENTORED, true);
	}
	
	public static void setMentored(PersonAPI person, boolean mentored) {
		person.getMemoryWithoutUpdate().set(MENTORED, mentored);
	}
	
	public static final String IS_MERCENARY = "$isMercenary";
	public static boolean isMercenary(PersonAPI person) {
		return person != null && person.getMemoryWithoutUpdate().is(IS_MERCENARY, true);
	}
	
	public static final String MERCENARY_HIRE_TIMESTAMP = "$mercHireTS";
	public static void setMercHiredNow(PersonAPI person) {
		person.getMemoryWithoutUpdate().set(MERCENARY_HIRE_TIMESTAMP, Global.getSector().getClock().getTimestamp());
	}
	
	public static float getMercDaysSinceHired(PersonAPI person) {
		long ts = person.getMemoryWithoutUpdate().getLong(MERCENARY_HIRE_TIMESTAMP);
		return Global.getSector().getClock().getElapsedDaysSince(ts);
	}
	
	public static void setMercenary(PersonAPI person, boolean mercenary) {
		person.getMemoryWithoutUpdate().set(IS_MERCENARY, mercenary);
	}
	
	public static final String CAPTAIN_UNREMOVABLE = "$captain_unremovable";
	public static boolean isUnremovable(PersonAPI person) {
		//if (true) return true;
		return person != null && person.getMemoryWithoutUpdate().is(CAPTAIN_UNREMOVABLE, true);
	}
	
	public static void setUnremovable(PersonAPI person, boolean unremovable) {
		person.getMemoryWithoutUpdate().set(CAPTAIN_UNREMOVABLE, unremovable);
	}
	
	public static final String KEEP_CAPTAIN_ON_SHIP_RECOVERY = "$keep_captain_on_ship_recovery";
	public static boolean isKeepOnShipRecovery(PersonAPI person) {
		return person != null && person.getMemoryWithoutUpdate().is(KEEP_CAPTAIN_ON_SHIP_RECOVERY, true);
	}
	
	public static void setKeepOnShipRecovery(PersonAPI person, boolean keepOnRecovery) {
		person.getMemoryWithoutUpdate().set(KEEP_CAPTAIN_ON_SHIP_RECOVERY, keepOnRecovery);
	}
	
	public static boolean isAutomated(MutableShipStatsAPI stats) {
		if (stats == null) return false;
		return isAutomated(stats.getFleetMember());
		
	}
	public static boolean isAutomated(FleetMemberAPI member) {
		return member != null && member.getVariant() != null && isAutomated(member.getVariant());
	}
	public static boolean isAutomated(ShipVariantAPI variant) {
		return variant != null && (variant.hasHullMod(HullMods.AUTOMATED) ||
				variant.hasTag(Tags.AUTOMATED) ||
				(variant.getHullSpec() != null && variant.getHullSpec().hasTag(Tags.AUTOMATED)));
	}
	public static boolean isAutomated(ShipAPI ship) {
		if (ship == null) return false;
		return isAutomated(ship.getVariant()); 
	}
	
	
	public static String RECOVERY_TAGS_KEY = "$core_recoveryTags";
	@SuppressWarnings("unchecked")
	public static Set<String> getAllowedRecoveryTags() {
		Set<String> tags = (Set<String>) Global.getSector().getMemoryWithoutUpdate().get(RECOVERY_TAGS_KEY);
		if (tags == null) {
			tags = new HashSet<String>();
			Global.getSector().getMemoryWithoutUpdate().set(RECOVERY_TAGS_KEY, tags);
		}
		return tags;
	}
	
	public static int MAX_PERMA_MODS = Global.getSettings().getInt("maxPermanentHullmods");
	
	public static int getMaxPermanentMods(ShipAPI ship) {
		if (ship == null) return 0;
		return (int) Math.round(ship.getMutableStats().getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).computeEffective(MAX_PERMA_MODS));
	}
	
	public static int getMaxPermanentMods(FleetMemberAPI member, MutableCharacterStatsAPI stats) {
		if (member == null) return 0;
		PersonAPI prev = member.getFleetCommanderForStats();
		PersonAPI fake = Global.getFactory().createPerson();
		fake.setStats(stats);
		member.setFleetCommanderForStats(fake, null);
		int num = (int) Math.round(member.getStats().getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).computeEffective(MAX_PERMA_MODS));
		member.setFleetCommanderForStats(prev, null);
		return num;
	}

	
	public static float getBuildInBonusXP(HullModSpecAPI mod, HullSize size) {
		//float threshold = Global.getSettings().getBonusXP("permModNoBonusXPOPThreshold");
		
		float fraction = 0f;
		//float cost = 0f;
		switch (size) {
		case CAPITAL_SHIP:
			fraction = Global.getSettings().getBonusXP("permModCapital");
			//cost = mod.getCapitalCost();
			break;
		case CRUISER:
			fraction = Global.getSettings().getBonusXP("permModCruiser");
			//cost = mod.getCruiserCost();
			break;
		case DESTROYER:
			fraction = Global.getSettings().getBonusXP("permModDestroyer");
			//cost = mod.getDestroyerCost();
			break;
		case FRIGATE:
			fraction = Global.getSettings().getBonusXP("permModFrigate");
			//cost = mod.getFrigateCost();
			break;
		}
		
		//float max = Global.getSettings().getBonusXP("permModMaxBonusXP");
		
//		float fraction = 0f;
//		if (threshold > 0) {
//			fraction = max * (1f - cost / threshold);
//			if (fraction < 0f) fraction = 0f;
//			if (fraction > 1f) fraction = 1f;
//		}
		
//		MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
//		fraction += stats.getDynamic().getMod(Stats.BUILD_IN_BONUS_XP_MOD).computeEffective(0);
		if (fraction < 0f) fraction = 0f;
		if (fraction > 1f) fraction = 1f;
		return fraction;
	}
	
	public static int getOPCost(HullModSpecAPI mod, HullSize size) {
		switch (size) {
		case CAPITAL_SHIP:
			return mod.getCapitalCost();
		case CRUISER:
			return mod.getCruiserCost();
		case DESTROYER:
			return mod.getDestroyerCost();
		case FRIGATE:
			return mod.getFrigateCost();
		}
		return mod.getFrigateCost();
	}
	
	public static boolean isSpecialMod(ShipVariantAPI variant, HullModSpecAPI spec) {
//		if (spec.getId().equals(HullMods.ANDRADA_MODS)) {
//			return true;fwewefwefe
//		}
		if (spec.isHidden()) return false;
		if (spec.isHiddenEverywhere()) return false;
		if (spec.hasTag(Tags.HULLMOD_DMOD)) return false;
		if (!variant.getPermaMods().contains(spec.getId())) return false;
		if (variant.getHullSpec().getBuiltInMods().contains(spec.getId())) return false;
		if (!variant.getSMods().contains(spec.getId())) return false;
		
		return true;
	}
	
	public static boolean hasSModdableBuiltIns(ShipVariantAPI variant) {
		if (!CAN_SMOD_BUILT_IN || variant == null) return false;
		int num = 0;
		for (String id : variant.getHullMods()) {
			HullModSpecAPI spec = Global.getSettings().getHullModSpec(id);
			if (spec.isHidden()) continue;
			if (spec.isHiddenEverywhere()) continue;
			if (spec.hasTag(Tags.HULLMOD_DMOD)) continue;
			if (variant.getHullSpec().isBuiltInMod(id) &&
					spec.getEffect().hasSModEffect() && !spec.getEffect().isSModEffectAPenalty() &&
					!variant.getSModdedBuiltIns().contains(id)) {
				num++;
			}
		}
		return num > 0;
	}
	public static int getCurrSpecialMods(ShipVariantAPI variant) {
		if (variant == null) return 0;
		int num = 0;
		for (String id : variant.getHullMods()) {
			HullModSpecAPI spec = Global.getSettings().getHullModSpec(id);
			if (!isSpecialMod(variant, spec)) continue;
//			if (spec.isHidden()) continue;
//			if (spec.isHiddenEverywhere()) continue;
//			if (spec.hasTag(Tags.HULLMOD_DMOD)) continue;
//			if (!variant.getPermaMods().contains(spec.getId())) continue;
			num++;
		}
		return num;
	}
	
	public static List<HullModSpecAPI> getCurrSpecialModsList(ShipVariantAPI variant) {
		List<HullModSpecAPI> result = new ArrayList<HullModSpecAPI>();
		if (variant == null) return result;
		int num = 0;
		for (String id : variant.getHullMods()) {
			HullModSpecAPI spec = Global.getSettings().getHullModSpec(id);
			if (!isSpecialMod(variant, spec)) continue;
			result.add(spec);
		}
		return result;
	}
	
	public static boolean isSlowMoving(CampaignFleetAPI fleet) {
		return fleet.getCurrBurnLevel() <= getGoSlowBurnLevel(fleet);
	}
	
	
	
	//public static float MAX_SNEAK_BURN_LEVEL = Global.getSettings().getFloat("maxSneakBurnLevel");
	public static float SNEAK_BURN_MULT = Global.getSettings().getFloat("sneakBurnMult");
	
	public static int getGoSlowBurnLevel(CampaignFleetAPI fleet) {
//		if (fleet.isPlayerFleet()) {
//			System.out.println("fewfewfe");
//		}
		float bonus = fleet.getStats().getDynamic().getMod(Stats.MOVE_SLOW_SPEED_BONUS_MOD).computeEffective(0);
		//int burn = (int)Math.round(MAX_SNEAK_BURN_LEVEL + bonus);
		//int burn = (int)Math.round(fleet.getFleetData().getMinBurnLevelUnmodified() * SNEAK_BURN_MULT);
		int burn = (int)Math.round(fleet.getFleetData().getMinBurnLevel() * SNEAK_BURN_MULT);
		burn += bonus;
		//burn = (int) Math.min(burn, fleet.getFleetData().getBurnLevel() - 1);
		return burn;
	}
	
	
	public static enum FleetMemberDamageLevel {
		LOW,
		MEDIUM,
		HIGH,
	}
	
	public static void applyDamage(FleetMemberAPI member, Random random, FleetMemberDamageLevel level, 
								   boolean withCRDamage, String crDamageId, String crDamageReason,
								   boolean withMessage, TextPanelAPI textPanel, 
								   String messageText) {
		float damageMult = 1f;
		switch (level) {
		case LOW:
			damageMult = 3f;
			break;
		case MEDIUM:
			damageMult = 10f;
			break;
		case HIGH:
			damageMult = 20f;
			break;
		}
		applyDamage(member, random, damageMult, withCRDamage, crDamageId, crDamageReason, 
					withMessage, textPanel, messageText);
	}
	
	public static void applyDamage(FleetMemberAPI member, Random random, float damageMult, 
				boolean withCRDamage, String crDamageId, String crDamageReason,
				boolean withMessage, TextPanelAPI textPanel, 
				String messageText) {
		if (random == null) random = Misc.random;
		damageMult *= 0.75f + random.nextFloat() * 0.5f;
		
//		float hitStrength = 0f; 
//		hitStrength += member.getHullSpec().getArmorRating() * 0.1f;
//		hitStrength *= damageMult;
		
		// hull damage going to be overridden by hullDamageFraction, anyway
		// so just want enough hitStrength to visibly damage the armor
		float hitStrength = member.getHullSpec().getArmorRating() * 2f;
		
		float hullDamageFraction = 0.025f * damageMult;
		float max = 0.5f + random.nextFloat() * 0.1f;
		float min = 0.01f + random.nextFloat() * 0.04f;
		if (hullDamageFraction > max) hullDamageFraction = max;
		if (hullDamageFraction < min) hullDamageFraction = min;
		
		if (hitStrength > 0) {
			float numHits = 3f;
			for (int i = 0; i < numHits; i++) {
				member.getStatus().applyDamage(hitStrength / numHits, hullDamageFraction / numHits);
			}	
			if (member.getStatus().getHullFraction() < 0.01f) {
				member.getStatus().setHullFraction(0.01f);
			}
			
			boolean isPF = member != null && member.getFleetData() != null &&
					member.getFleetData().getFleet() != null && member.getFleetData().getFleet().isPlayerFleet();
			
			if (withCRDamage) {
				float crPerDep = member.getDeployCost();
				float currCR = member.getRepairTracker().getBaseCR();
				float crDamage = Math.min(currCR, crPerDep * 0.1f * damageMult);
				if (crDamage > 0) {
					if (isPF) {
						member.getRepairTracker().applyCREvent(-crDamage, crDamageId, crDamageReason);
					} else {
						member.getRepairTracker().applyCREvent(-crDamage, null, null);
					}
				}
			}
			
			if (withMessage && isPF) {
				MessageIntel intel = new MessageIntel(messageText,
										Misc.getNegativeHighlightColor());
				intel.setIcon(Global.getSettings().getSpriteName("intel", "damage_report"));
				
				if (textPanel != null) {
					Global.getSector().getIntelManager().addIntelToTextPanel(intel, textPanel);
				} else {
					Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.REFIT_TAB, member);
				}
			}
		}
	}
	
	public static float getBonusXPForRecovering(FleetMemberAPI member) {
		float ownedShip = Global.getSettings().getBonusXP("recoverOwnedShip");
		float threshold = Global.getSettings().getBonusXP("recoverNoBonusXPDeploymentPoints");
		
		if (member.getOwner() == 0) {
			return ownedShip;
		}
		
		float f = 1f - member.getDeploymentPointsCost() / threshold;
		if (f < 0) f = 0;
		if (f > 1) f = 1;
		
		return f;
	}
	
	public static float [] getBonusXPForScuttling(FleetMemberAPI member) {
		float points = 0f;
		float xp = 0f;
		for (SModRecord record : PlaythroughLog.getInstance().getSModsInstalled()) {
			//if (member.getId() != null && member.getId().equals(record.getMemberId())) {
			if (member == record.getMember() && record.getMember() != null) {
				points += record.getSPSpent();
				xp += record.getBonusXPFractionGained() * record.getSPSpent();
			}
		}
		if (points > 0) {
			return new float[] {points, 1f - xp/points};
		}
		return new float[] {0f, 0f};
	}
	
	public static float getSpawnFPMult(CampaignFleetAPI fleet) {
		float mult = fleet.getMemoryWithoutUpdate().getFloat(FleetFactoryV3.KEY_SPAWN_FP_MULT);
		if (mult == 0) mult = 1f;
		return mult;
	}
	
	public static void setSpawnFPMult(CampaignFleetAPI fleet, float mult) {
		fleet.getMemoryWithoutUpdate().set(FleetFactoryV3.KEY_SPAWN_FP_MULT, mult);
	}
	
	public static boolean isDecentralized(FactionAPI faction) {
		return faction != null && faction.getCustomBoolean(Factions.CUSTOM_DECENTRALIZED);
	}
	
	public static String getPersonalityName(PersonAPI person) {
		String personalityName = person.getPersonalityAPI().getDisplayName();
		if (person.isAICore()) {
			if (Personalities.RECKLESS.equals(person.getPersonalityAPI().getId())) {
				personalityName = "Fearless";
			}
		}
		return personalityName;
	}
	
	public static String LAST_RAIDED_AT = "$lastRaidedAt";
	public static void setRaidedTimestamp(MarketAPI market) {
		market.getMemoryWithoutUpdate().set(LAST_RAIDED_AT, Global.getSector().getClock().getTimestamp());
	}
	
	public static float getDaysSinceLastRaided(MarketAPI market) {
		Long ts = market.getMemoryWithoutUpdate().getLong(LAST_RAIDED_AT);
		if (ts == null) return Float.MAX_VALUE;
		return Global.getSector().getClock().getElapsedDaysSince(ts);
	}
	
	public static int computeEconUnitChangeFromTradeModChange(CommodityOnMarketAPI com, int quantity) {
		float currQty = com.getCombinedTradeModQuantity();
		int currMod = (int) com.getModValueForQuantity(currQty);
		
		float quantityWithTX = com.getTradeMod().getModifiedValue() + quantity + 
								Math.max(com.getTradeModPlus().getModifiedValue(), 0) +
								Math.min(com.getTradeModMinus().getModifiedValue(), 0);
		
		int newMod = (int) com.getModValueForQuantity(quantityWithTX);
		
		int diff = newMod - currMod;
		
		return diff;
	}
	
	public static void affectAvailabilityWithinReason(CommodityOnMarketAPI com, int quantity) {
		int units = computeEconUnitChangeFromTradeModChange(com, quantity);
		int maxUnits = Math.min(3, Math.max(com.getMaxDemand(), com.getMaxSupply()));
		if (Math.abs(units) > maxUnits) {
			int sign = (int) Math.signum(quantity);
			quantity = (int) Math.round(com.getQuantityForModValue(maxUnits));
			quantity *= sign;
		}
		com.addTradeMod("mod_" + Misc.genUID(), quantity, BaseSubmarketPlugin.TRADE_IMPACT_DAYS);
	}
	
	
	public static boolean isOpenlyPopulated(StarSystemAPI system) {
		for (MarketAPI market : Misc.getMarketsInLocation(system)) {
			if (!market.isHidden()) return true;
		}
		return false;
	}
	
	
	public static boolean hasAtLeastOneOfTags(Collection<String> tags, String ... other) {
		for (String tag : other) {
			if (tags.contains(tag)) return true;
		}
		return false;
	}
	
	
	
//	public static boolean isUnpopulatedPlanet(PlanetAPI planet) {
//		if (planet.isStar() || 
//				planet.getMarket() == null || 
//				!planet.getMarket().isPlanetConditionMarketOnly()) {
//			return false;
//		}
//		return true;
//	}
	
	public static boolean hasUnexploredRuins(MarketAPI market) {
		return market != null && market.isPlanetConditionMarketOnly() &&
			hasRuins(market) && !market.getMemoryWithoutUpdate().getBoolean("$ruinsExplored");
	}
	public static boolean hasRuins(MarketAPI market) {
		return market != null && 
			   (market.hasCondition(Conditions.RUINS_SCATTERED) || 
			   market.hasCondition(Conditions.RUINS_WIDESPREAD) ||
			   market.hasCondition(Conditions.RUINS_EXTENSIVE) ||
			   market.hasCondition(Conditions.RUINS_VAST));
	}
	
	public static MarketConditionSpecAPI getRuinsSpec(MarketAPI market) {
		String id = getRuinsType(market);
		return Global.getSettings().getMarketConditionSpec(id);
	}
	
	/**
	 * Assumes the market *does* have ruins.
	 * @param market
	 * @return
	 */
	public static String getRuinsType(MarketAPI market) {
		if (market == null) return Conditions.RUINS_SCATTERED;
		if (market.hasCondition(Conditions.RUINS_SCATTERED)) return Conditions.RUINS_SCATTERED; 
		if (market.hasCondition(Conditions.RUINS_WIDESPREAD)) return Conditions.RUINS_WIDESPREAD; 
		if (market.hasCondition(Conditions.RUINS_EXTENSIVE)) return Conditions.RUINS_EXTENSIVE; 
		if (market.hasCondition(Conditions.RUINS_VAST)) return Conditions.RUINS_VAST;
		return Conditions.RUINS_SCATTERED;
	}
	
	public static boolean hasFarmland(MarketAPI market) {
		return market != null && 
				(market.hasCondition(Conditions.FARMLAND_POOR) || 
				market.hasCondition(Conditions.FARMLAND_ADEQUATE) ||
				market.hasCondition(Conditions.FARMLAND_RICH) ||
				market.hasCondition(Conditions.FARMLAND_BOUNTIFUL));
	}
	
	
	public static String DEFEAT_TRIGGERS = "$defeatTriggers";
	public static void addDefeatTrigger(CampaignFleetAPI fleet, String trigger) {
		List<String> triggers = getDefeatTriggers(fleet, true);
		triggers.add(trigger);
	}
	
	public static void removeDefeatTrigger(CampaignFleetAPI fleet, String trigger) {
		List<String> triggers = getDefeatTriggers(fleet, false);
		if (triggers != null) {
			triggers.remove(trigger);
			clearDefeatTriggersIfNeeded(fleet);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> getDefeatTriggers(CampaignFleetAPI fleet, boolean createIfNecessary) {
		MemoryAPI mem = fleet.getMemoryWithoutUpdate();
		List<String> triggers = null;
		if (!mem.contains(DEFEAT_TRIGGERS)) {
			if (!createIfNecessary) return null;
			triggers = new ArrayList<String>();
			mem.set(DEFEAT_TRIGGERS, triggers);
		} else {
			triggers = (List<String>) mem.get(DEFEAT_TRIGGERS);
		}
		return triggers;
	}
	
	public static void clearDefeatTriggersIfNeeded(CampaignFleetAPI fleet) {
		List<String> triggers = getDefeatTriggers(fleet, false);
		if (triggers != null && triggers.isEmpty()) {
			MemoryAPI mem = fleet.getMemoryWithoutUpdate();
			mem.unset(DEFEAT_TRIGGERS);
		}
	}
	
	public static boolean shouldShowDamageFloaty(ShipAPI source, ShipAPI target) {
		if (target == null || !Global.getCombatEngine().getShips().contains(target)) {
			return false;
		}
		CombatEngineAPI engine = Global.getCombatEngine();
		ShipAPI playerShip = engine.getPlayerShip();
		
		boolean sourceIsPlayerShipWing = false;
		sourceIsPlayerShipWing = source != null && source.getWing() != null && 
								 source.getWing().getSourceShip() == playerShip;
		
		CombatEntityAPI followedEntity = engine.getCombatUI().getEntityToFollowV2();
		boolean showFloaty = target == playerShip || // this is the player's ship
		target == followedEntity || // getting video feed from this ship
							 source == playerShip || // the damage came from the player's ship
							 sourceIsPlayerShipWing ||
							 target == playerShip.getShipTarget() || // the ship is the player ship's target
							 engine.hasAttachedFloaty(target); // the ship already has a floaty on it, meaning the player is likely looking at it
		showFloaty = showFloaty && !target.isFighter(); // no floaties on fighters
		showFloaty = showFloaty && Global.getSettings().isShowDamageFloaties();
		return showFloaty;
	}

//	
//	 public static Vector2f cubeBezier(Vector2f p0, Vector2f p1, Vector2f p2, Vector2f p3, float t)
//	 {
//	     float r = 1f - t;
//	     float f0 = r * r * r;
//	     float f1 = r * r * t * 3;
//	     float f2 = r * t * t * 3;
//	     float f3 = t * t * t;
//	     return f0*p0 + f1*p1 + f2*p2 + f3*p3;
//	 }
	
	
//	long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory
//	        .getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
	protected static Boolean canCheckVramNVIDIA = null;
	public static boolean canCheckVram() {
		if (canCheckVramNVIDIA == null) {
			String str = GL11.glGetString(GL11.GL_EXTENSIONS);
			if (str != null) {
				List<String> extensions = Arrays.asList(str.split(" "));
				canCheckVramNVIDIA = extensions.contains("GL_NVX_gpu_memory_info");
				//canCheckVramATI = extensions.contains("GL_ATI_meminfo");
			} else {
				canCheckVramNVIDIA = false;
			}
		}
		return true;
	}
	/**
	 * Reminder: call this on startup to see what the max is.
	 * @return
	 */
	public static int getVramFreeKB() {
		if (canCheckVramNVIDIA) {
			return GL11.glGetInteger(NVXGpuMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX);
		} else {
			return GL11.glGetInteger(ATIMeminfo.GL_TEXTURE_FREE_MEMORY_ATI);
		}
	}
	
	public static int getVramMaximumKB() {
		return GL11.glGetInteger(NVXGpuMemoryInfo.GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX);
	}
	public static int getVramDedicatedKB() {
		return GL11.glGetInteger(NVXGpuMemoryInfo.GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX);
	}
	public static int getVramUsedKB() {
		return getVramMaximumKB() - getVramFreeKB();
	}

//	public static void printExtensions() {
//		String str = GL11.glGetString(GL11.GL_EXTENSIONS);
//		System.out.println(str);
//	}
	
	
	public static float IMPACT_VOLUME_MULT = Global.getSettings().getFloat("impactSoundVolumeMult");
	
	public static void playSound(ApplyDamageResultAPI result, Vector2f loc, Vector2f vel,
								String lightShields, String solidShields, String heavyShields,
								String lightHull, String solidHull, String heavyHull
								) {
		float shieldDam = result.getDamageToShields();
		float armorDam = result.getTotalDamageToArmor();
		float hullDam = result.getDamageToHull();
		float fluxDam = result.getEmpDamage();
		
		
		float totalDam = shieldDam + armorDam + hullDam;
		// if no damage, don't play sounds
		if (totalDam + fluxDam <= 0) return;
		
		float vol = 1f;
		
		float volMult = IMPACT_VOLUME_MULT;
		
		// if shields were damaged, then ONLY shields were damaged
		if (shieldDam > 0) {
			String soundId = null;
			if (shieldDam < 70) {
				vol = shieldDam / 20f;
				if (vol > 1) vol = 1;
				soundId = lightShields;
			} else if (shieldDam < 200) {
				soundId = solidShields;
			} else {
				soundId = heavyShields;
			}
			if (soundId != null) {
				Global.getSoundPlayer().playSound(soundId, 1f, vol * volMult, loc, vel);
			}
			return;
		}
		
		String soundId = null;
		
		float physicalDam = armorDam + hullDam + fluxDam;
		//System.out.println(physicalDam);
		if (physicalDam < 5) {
			vol = physicalDam / 5f;
			if (vol > 1) vol = 1;
			soundId = lightHull;
		} else if (physicalDam < 40) {
			soundId = lightHull;
		} else if (physicalDam < 150) {
			soundId = solidHull;
		} else {
			soundId = heavyHull;
		}

		if (soundId != null) {
			Global.getSoundPlayer().playSound(soundId, 1f, vol * volMult, loc, vel);
		}
		return;
	}
	
	
	public static float getShipWeight(ShipAPI ship) {
		return getShipWeight(ship, true);
	}
	
	public static float getShipWeight(ShipAPI ship, boolean adjustForNonCombat) {
		if (ship.isDrone()) return 0.1f;
		boolean nonCombat = ship.isNonCombat(false);
		float weight = 0;
		switch (ship.getHullSize()) {
		case CAPITAL_SHIP: weight += 8; break;
		case CRUISER: weight += 4; break;
		case DESTROYER: weight += 2; break;
		case FRIGATE: weight += 1; break;
		case FIGHTER: weight += 1; break;
		}
		if (ship.getHullSpec().isPhase() && (ship.isFrigate() || ship.isDestroyer())) {
			weight += 2f;
		}
		if (nonCombat && adjustForNonCombat) weight *= 0.25f;
		if (ship.isDrone()) weight *= 0.1f;
		return weight;
	}
	
	public static float getIncapacitatedTime(ShipAPI ship) {
		float incapTime = 0f;
		if (ship.getFluxTracker().isVenting()){
			incapTime = ship.getFluxTracker().getTimeToVent();
		} else if (ship.getFluxTracker().isOverloaded()) {
			incapTime = ship.getFluxTracker().getOverloadTimeRemaining();
		}
		return incapTime;
	}
	
	public static boolean isAvoidingPlayerHalfheartedly(CampaignFleetAPI fleet) {
		if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_NEVER_AVOID_PLAYER_SLOWLY)) {
			return false;
		}
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		boolean avoidingPlayer = fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_AVOID_PLAYER_SLOWLY);
		if (avoidingPlayer && !fleet.isHostileTo(player)) return true;
		
		CampaignFleetAPI fleeingFrom = fleet.getMemoryWithoutUpdate().getFleet(FleetAIFlags.NEAREST_FLEEING_FROM);
		if (fleeingFrom != null && fleeingFrom.isPlayerFleet()) {
			if (Misc.shouldNotWantRunFromPlayerEvenIfWeaker(fleet) && fleet.isHostileTo(player)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * In vanilla, pirates and Luddic Path.
	 * @param faction
	 * @return
	 */
	public static boolean isPirateFaction(FactionAPI faction) {
		return faction != null &&
			   faction.getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR);// &&
					//faction.getCustomBoolean(Factions.CUSTOM_MAKES_PIRATE_BASES);
	}
	
	/**
	 * Probably wrong sometimes...
	 * @return "a" or "an" for word.
	 */
	public static String getAOrAnFor(String word) {
		word = word.toLowerCase();
	    for (String other : new String[] {"euler", "heir", "honest", "hono"}) {
	        if (word.startsWith(other)) {
	            return "an";
	        }
	    }

	    if (word.startsWith("hour") && !word.startsWith("houri")) {
	        return "an";
	    }

	    Pattern p;
	    Matcher m;

	    for (String regex : new String[] { "^e[uw]", "^onc?e\b", "^uni([^nmd]|mo)", "^u[bcfhjkqrst][aeiou]"}) {
	    	p = Pattern.compile("(?is)" + regex + ".*");
		    m = p.matcher(word);
		    if (m.matches()) {
	            return "a";
		    }
	    }

	    p = Pattern.compile("(?is)" + "^U[NK][AIEO]");
	    m = p.matcher(word);
	    if (m.matches()) {
	        return "a";
	    }

	    for (String letter : new String[] { "a", "e", "i", "o", "u" }) {
	    	if (word.startsWith(letter)) {
	    		return "an";
	    	}
	    }
	    
	    p = Pattern.compile("(?is)" + "^y(b[lor]|cl[ea]|fere|gg|p[ios]|rou|tt)" + ".*");
	    m = p.matcher(word);
	    if (m.matches()) {
	        return "an";
	    }

	    return "a";
	}
	
	
	public static void moveToMarket(PersonAPI person, MarketAPI destination, boolean alwaysAddToCommDirectory) {
		ContactIntel intel = ContactIntel.getContactIntel(person);
		if (intel != null) {
			intel.relocateToMarket(destination, false);
		} else {
			boolean addToComms = alwaysAddToCommDirectory;
			boolean hidden = false;
			if (person.getMarket() != null) {
				MarketAPI market = person.getMarket();
				CommDirectoryEntryAPI entry = market.getCommDirectory().getEntryForPerson(person);
				if (entry != null) {
					addToComms = true;
					hidden = entry.isHidden();
				}
				market.removePerson(person);
				market.getCommDirectory().removePerson(person);
			}
			
			if (!destination.getPeopleCopy().contains(person)) {
				destination.addPerson(person);
			}
			person.setMarket(destination);
			
			if (addToComms) {
				if (destination.getCommDirectory() != null && 
						destination.getCommDirectory().getEntryForPerson(person) == null) {
					destination.getCommDirectory().addPerson(person);
					if (hidden) {
						CommDirectoryEntryAPI entry = destination.getCommDirectory().getEntryForPerson(person);
						if (entry != null) {
							entry.setHidden(true);
						}
					}
				}
			}
		}
	}

	public static void makeStoryCritical(String marketId, String reason) {
		makeStoryCritical(Global.getSector().getEconomy().getMarket(marketId), reason);
	}
	public static void makeStoryCritical(MarketAPI market, String reason) {
		makeStoryCritical(market.getMemoryWithoutUpdate(), reason);
	}
	public static void makeStoryCritical(MemoryAPI memory, String reason) {
		setFlagWithReason(memory, MemFlags.STORY_CRITICAL, reason, true, -1f);
	}
	public static void makeNonStoryCritical(MarketAPI market, String reason) {
		makeNonStoryCritical(market.getMemoryWithoutUpdate(), reason);
	}
	public static void makeNonStoryCritical(MemoryAPI memory, String reason) {
		setFlagWithReason(memory, MemFlags.STORY_CRITICAL, reason, false, -1f);
	}
	public static boolean isStoryCritical(MarketAPI market) {
		return isStoryCritical(market.getMemoryWithoutUpdate());
	}
	public static boolean isStoryCritical(MemoryAPI memory) {
		return memory.getBoolean(MemFlags.STORY_CRITICAL);
	}
	
	
	/**
	 * Whether it prevents salvage, surveying, etc. But NOT things that require only being
	 * seen to ruin them, such as SpySat deployments.
	 * @param fleet
	 * @return
	 */
	public static boolean isInsignificant(CampaignFleetAPI fleet) {
		boolean recentlyBeaten = fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_RECENTLY_DEFEATED_BY_PLAYER);
		if (recentlyBeaten) return true;
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		if (pf == null) return true; // ??
		if (fleet.getAI() != null) {
			EncounterOption opt = fleet.getAI().pickEncounterOption(null, pf);
			if (opt == EncounterOption.DISENGAGE || opt == EncounterOption.HOLD_VS_STRONGER) {
				return true;
			}
			if (opt == EncounterOption.ENGAGE) {
				return false;
			}
		}
		int pfCount = pf.getFleetSizeCount();
		int otherCount = fleet.getFleetSizeCount();
		
		return otherCount <= pfCount / 4;
	}
	
	/**
	 * Mainly for avoiding stuff like "pirate fleet with 4 rustbuckets will run away from the player's
	 * 4 regular-quality frigates". Fleets that this evaluates to true for will avoid the player slowly.
	 * @param fleet
	 * @return
	 */
	public static boolean shouldNotWantRunFromPlayerEvenIfWeaker(CampaignFleetAPI fleet) {
		boolean recentlyBeaten = fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_RECENTLY_DEFEATED_BY_PLAYER);
		if (recentlyBeaten) return true;
		if (fleet.getFleetData() == null) return false;
		
		float count = 0;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (!member.isCivilian() && member.getHullSpec() != null) {
				switch (member.getHullSpec().getHullSize()) {
				case CAPITAL_SHIP: count += 4; break;
				case CRUISER: count += 3; break;
				case DESTROYER: count += 2; break;
				case FRIGATE: count += 1; break;
				}
			}
		}
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		float pfCount = 0;
		for (FleetMemberAPI member : pf.getFleetData().getMembersListCopy()) {
			if (!member.isCivilian() && member.getHullSpec() != null) {
				switch (member.getHullSpec().getHullSize()) {
				case CAPITAL_SHIP: pfCount += 4; break;
				case CRUISER: pfCount += 3; break;
				case DESTROYER: pfCount += 2; break;
				case FRIGATE: pfCount += 1; break;
				}
			}
		}
		
		if (count > pfCount * 0.67f) {
			return true;
		}
		
		if (isInsignificant(fleet) && count <= 6) return true;
		
		return false;
	}
	
	public static float findKth(float[] arr, int k) {
		if (arr == null || arr.length <= k || k < 0) {
			return -1;
		}

		int from = 0;
		int to = arr.length - 1;

		while (from < to) {
			int r = from;
			int w = to;
			float mid = arr[(r + w) / 2];

			while (r < w) {
				if (arr[r] >= mid) {
					float tmp = arr[w];
					arr[w] = arr[r];
					arr[r] = tmp;
					w--;
				} else {
					r++;
				}
			}

			if (arr[r] > mid) r--;

			if (k <= r) {
				to = r;
			} else {
				from = r + 1;
			}
		}

		return arr[k];
	}

	public static float getAdjustedBaseRange(float base, ShipAPI ship, WeaponAPI weapon) {
		if (ship == null || weapon == null) return base;
		float flat = CombatListenerUtil.getWeaponBaseRangeFlatMod(ship, weapon);
		float percent = CombatListenerUtil.getWeaponBaseRangePercentMod(ship, weapon);
		float mult = CombatListenerUtil.getWeaponBaseRangeMultMod(ship, weapon);
		return (base * (1f + percent/100f) + flat) * mult;
	}
	
	
	public static Vector2f bezier(Vector2f p0, Vector2f p1, Vector2f p2, float t) {
		if (t < 0) t = 0;
		if (t > 1) t = 1;
		Vector2f r = new Vector2f();
		r.x = (1f - t) * (1f - t) * p0.x + 2f * (1f - t) * t * p1.x + t * t * p2.x;
		r.y = (1f - t) * (1f - t) * p0.y + 2f * (1f - t) * t * p1.y + t * t * p2.y;
		return r;
	}
	
	public static Vector2f bezierCubic(Vector2f p0, Vector2f p1, Vector2f p2, Vector2f p3, float t) {
		if (t < 0) t = 0;
		if (t > 1) t = 1;
		Vector2f r = new Vector2f();
		
		r.x = (1f - t) * (1f - t) * (1f -t) * p0.x + 
				3f * (1f - t) * (1f - t) * t * p1.x +
				3f * (1f - t) * t * t * p2.x +
				t * t * t * p3.x;
		r.y = (1f - t) * (1f - t) * (1f -t) * p0.y + 
				3f * (1f - t) * (1f - t) * t * p1.y +
				3f * (1f - t) * t * t * p2.y +
				t * t * t * p3.y;
		return r;
	}
	
	public static boolean isInsideSlipstream(Vector2f loc, float radius) {
		return isInsideSlipstream(loc, radius, Global.getSector().getHyperspace());
	}
	public static boolean isInsideSlipstream(Vector2f loc, float radius, LocationAPI location) {
		if (location == null) return false;
		for (CampaignTerrainAPI ter : location.getTerrainCopy()) {
			if (ter.getPlugin() instanceof SlipstreamTerrainPlugin2) {
				SlipstreamTerrainPlugin2 plugin = (SlipstreamTerrainPlugin2) ter.getPlugin();
				if (plugin.containsPoint(loc, radius)) {
					return true;
				}
			}
		}
		return false;
	}
	public static boolean isInsideSlipstream(SectorEntityToken entity) {
		if (entity == null || entity.getContainingLocation() == null) return false;
		for (CampaignTerrainAPI ter : entity.getContainingLocation().getTerrainCopy()) {
			if (ter.getPlugin() instanceof SlipstreamTerrainPlugin2) {
				SlipstreamTerrainPlugin2 plugin = (SlipstreamTerrainPlugin2) ter.getPlugin();
				if (plugin.containsEntity(entity)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isOutsideSector(Vector2f loc) {
		float sw = Global.getSettings().getFloat("sectorWidth");
		float sh = Global.getSettings().getFloat("sectorHeight");
		return loc.x < -sw/2f || loc.x > sw/2f || loc.y < -sh/2f || loc.y > sh/2f;
	}
	
	public static boolean crossesAnySlipstream(LocationAPI location, Vector2f from, Vector2f to) {
		for (CampaignTerrainAPI ter : location.getTerrainCopy()) {
			if (ter.getPlugin() instanceof SlipstreamTerrainPlugin2) {
				SlipstreamTerrainPlugin2 plugin = (SlipstreamTerrainPlugin2) ter.getPlugin();
				List<SlipstreamSegment> segments = plugin.getSegments();
				int skip = Math.max(20, segments.size() / 10);
				for (int i = 0; i < segments.size(); i += skip) {
					int i2 = i + skip;
					if (i2 > segments.size() - skip/2) i2 = segments.size() - 1;
					if (i2 >= segments.size()) i2 = segments.size() - 1;
					
					if (i2 <= i) break;
					
					Vector2f p = intersectSegments(segments.get(i).loc, segments.get(i2).loc, from, to);
					if (p != null) return true;
				}
			}
		}
		return false;
	}
	
	public static void computeCoreWorldsExtent() {
		Vector2f min = new Vector2f();
		Vector2f max = new Vector2f();
		for (StarSystemAPI curr : Global.getSector().getStarSystems()) {
			if (curr.hasTag(Tags.THEME_CORE)) {
				Vector2f loc = curr.getLocation();
				min.x = Math.min(min.x, loc.x);
				min.y = Math.min(min.y, loc.y);
				max.x = Math.max(max.x, loc.x);
				max.y = Math.max(max.y, loc.y);
			}
		}
		
		Vector2f core = Vector2f.add(min, max, new Vector2f());
		core.scale(0.5f);
		
		Global.getSector().getMemoryWithoutUpdate().set("$coreWorldsMin", min);
		Global.getSector().getMemoryWithoutUpdate().set("$coreWorldsMax", max);
		Global.getSector().getMemoryWithoutUpdate().set("$coreWorldsCenter", core);
	}

	public static Vector2f getCoreMin() {
		Vector2f v = (Vector2f) Global.getSector().getMemoryWithoutUpdate().get("$coreWorldsMin");
		if (v == null) {
			computeCoreWorldsExtent();
			v = (Vector2f) Global.getSector().getMemoryWithoutUpdate().get("$coreWorldsMin");
		}
		return v;
	}
	public static Vector2f getCoreMax() {
		Vector2f v = (Vector2f) Global.getSector().getMemoryWithoutUpdate().get("$coreWorldsMax");
		if (v == null) {
			computeCoreWorldsExtent();
			v = (Vector2f) Global.getSector().getMemoryWithoutUpdate().get("$coreWorldsMax");
		}
		return v;
	}
	public static Vector2f getCoreCenter() {
		Vector2f v = (Vector2f) Global.getSector().getMemoryWithoutUpdate().get("$coreWorldsCenter");
		if (v == null) {
			computeCoreWorldsExtent();
			v = (Vector2f) Global.getSector().getMemoryWithoutUpdate().get("$coreWorldsCenter");
		}
		return v;
	}
	
	
//	public static void createColonyStatic(MarketAPI market) 
//	{
//		String factionId = Factions.PLAYER;
//		
//		market.setSize(3);
//		market.addCondition("population_3");
//		market.setFactionId(factionId);
//		market.setPlanetConditionMarketOnly(false);
//		
//		if (market.hasCondition(Conditions.DECIVILIZED))
//		{
//			market.removeCondition(Conditions.DECIVILIZED);
//			market.addCondition(Conditions.DECIVILIZED_SUBPOP);
//		}
//		market.addIndustry(Industries.POPULATION);
//		
//		market.addSubmarket(Submarkets.LOCAL_RESOURCES);
//		market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
//		
//		market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
//		for (MarketConditionAPI cond : market.getConditions())
//		{
//			cond.setSurveyed(true);
//		}
//		
//		Global.getSector().getEconomy().addMarket(market, true);
//		market.getPrimaryEntity().setFaction(factionId);
//		
//		market.setPlayerOwned(true);
//		market.addIndustry(Industries.SPACEPORT);
//		SubmarketAPI storage = market.getSubmarket(Submarkets.SUBMARKET_STORAGE);
//		if (storage != null)
//			((StoragePlugin)storage.getPlugin()).setPlayerPaidToUnlock(true);
//	}
	

	public static boolean turnTowardsPointV2(MissileAPI missile, Vector2f point, float angVel) {
		float desiredFacing = getAngleInDegrees(missile.getLocation(), point);
		return turnTowardsFacingV2(missile, desiredFacing, angVel);
	}
	
	public static boolean turnTowardsFacingV2(MissileAPI missile, float desiredFacing, float relativeAngVel) {		
		
		float turnVel = missile.getAngularVelocity() - relativeAngVel;
		float absTurnVel = Math.abs(turnVel);
		
		float turnDecel = missile.getEngineController().getTurnDeceleration();
		// v t - 0.5 a t t = dist
		// dv = a t;  t = v / a
		float decelTime = absTurnVel / turnDecel; 
		float decelDistance = absTurnVel * decelTime - 0.5f * turnDecel * decelTime * decelTime;
		
		float facingAfterNaturalDecel = missile.getFacing() + Math.signum(turnVel) * decelDistance;
		float diffWithEventualFacing = getAngleDiff(facingAfterNaturalDecel, desiredFacing);
		float diffWithCurrFacing = getAngleDiff(missile.getFacing(), desiredFacing);
		
		if (diffWithEventualFacing > 1f) {
			float turnDir = getClosestTurnDirection(missile.getFacing(), desiredFacing);
			if (Math.signum(turnVel) == Math.signum(turnDir)) {
				if (decelDistance > diffWithCurrFacing) {
					turnDir = -turnDir;
				}
			}
			if (turnDir < 0) {
				missile.giveCommand(ShipCommand.TURN_RIGHT);
			} else if (turnDir >= 0) {
				missile.giveCommand(ShipCommand.TURN_LEFT);
			} else {
				return false;
			}
		}
		return false;
	}
	
	public static boolean turnTowardsFacingV2(ShipAPI ship, float desiredFacing, float relativeAngVel) {		
		
		float turnVel = ship.getAngularVelocity() - relativeAngVel;
		float absTurnVel = Math.abs(turnVel);
		
		float turnDecel = ship.getEngineController().getTurnDeceleration();
		// v t - 0.5 a t t = dist
		// dv = a t;  t = v / a
		float decelTime = absTurnVel / turnDecel; 
		float decelDistance = absTurnVel * decelTime - 0.5f * turnDecel * decelTime * decelTime;
		
		float facingAfterNaturalDecel = ship.getFacing() + Math.signum(turnVel) * decelDistance;
		float diffWithEventualFacing = getAngleDiff(facingAfterNaturalDecel, desiredFacing);
		float diffWithCurrFacing = getAngleDiff(ship.getFacing(), desiredFacing);
		
		if (diffWithEventualFacing > 1f) {
			float turnDir = getClosestTurnDirection(ship.getFacing(), desiredFacing);
			if (Math.signum(turnVel) == Math.signum(turnDir)) {
				if (decelDistance > diffWithCurrFacing) {
					turnDir = -turnDir;
				}
			}
			if (turnDir < 0) {
				ship.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
			} else if (turnDir >= 0) {
				ship.giveCommand(ShipCommand.TURN_LEFT, null, 0);
			} else {
				return false;
			}
		}
		return false;
	}
	
	public static int getUntrustwortyCount() {
		int count = Global.getSector().getPlayerMemoryWithoutUpdate().getInt(MemFlags.PLAYER_UNTRUSTWORTHY);
		return count;
	}
	
	public static void incrUntrustwortyCount() {
		int count = getUntrustwortyCount();
		Global.getSector().getPlayerMemoryWithoutUpdate().set(MemFlags.PLAYER_UNTRUSTWORTHY, count + 1);
	}
	
	public static ReputationAdjustmentResult adjustRep(PersonAPI person, float delta, TextPanelAPI text) {
		return adjustRep(person, delta, null, text);
	}
	public static ReputationAdjustmentResult adjustRep(PersonAPI person, float delta, RepLevel limit, TextPanelAPI text) {
		return adjustRep(person, delta, limit, text, true, true);
	}
	public static ReputationAdjustmentResult adjustRep(PersonAPI person, float delta, RepLevel limit, TextPanelAPI text,
			boolean addMessageOnNoChange, boolean withMessage) {
		CustomRepImpact impact = new CustomRepImpact();
		impact.delta = delta;
		if (limit != null) {
			impact.limit = limit;
		}
		return Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.CUSTOM, 
						impact, null, text, addMessageOnNoChange, withMessage),
						person);
	}
	
	public static ReputationAdjustmentResult adjustRep(String factionId, float delta, TextPanelAPI text) {
		return adjustRep(factionId, delta, null, text);
	}
	public static ReputationAdjustmentResult adjustRep(String factionId, float delta, RepLevel limit, TextPanelAPI text) {
		return adjustRep(factionId, delta, limit, text, true, true);
	}
	public static ReputationAdjustmentResult adjustRep(String factionId, float delta, RepLevel limit, TextPanelAPI text,
			boolean addMessageOnNoChange, boolean withMessage) {
		CustomRepImpact impact = new CustomRepImpact();
		impact.delta = delta;
		if (limit != null) {
			impact.limit = limit;
		}
		return Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.CUSTOM, 
						impact, null, text, addMessageOnNoChange, withMessage),
				factionId);
	}
	
	public static String getHullSizeStr(HullSize size) {
		switch (size) {
		case CAPITAL_SHIP: return "Capital";
		case CRUISER: return "Cruiser";
		case DESTROYER: return "Destroyer";
		case FIGHTER: return "Fighter";
		case FRIGATE: return "Frigate";
		}
		return "Unknown";
	}
	
	public static float getColorDist(Color one, Color two) {
		float r = Math.abs(one.getRed() - two.getRed());
		float g = Math.abs(one.getGreen() - two.getGreen());
		float b = Math.abs(one.getBlue() - two.getBlue());
		float a = Math.abs(one.getAlpha() - two.getAlpha());
		
		return (float) Math.sqrt(r * r + g * g + b * b + a * a);
	}
	
	
	public static float FRINGE_THRESHOLD = 0.7f;
	
	public static boolean isFringe(SectorEntityToken entity) {
		return isFringe(entity.getLocationInHyperspace());
	}
	public static boolean isFringe(StarSystemAPI system) {
		return isFringe(system.getLocation());
	}
	public static boolean isFringe(Vector2f loc) {
		return getFringeFactor(loc) > FRINGE_THRESHOLD;
	}
	public static float getFringeFactor(Vector2f loc) {
		float sw = Global.getSettings().getFloat("sectorWidth");
		float sh = Global.getSettings().getFloat("sectorHeight");
		float mult = 0.8f;
		//float mult = 1f;
		float a = sw * 0.5f * mult;
		float b = sh * 0.5f * mult;
		float x = loc.x;
		float y = loc.y;
		
		float f = (x * x) / (a * a) + (y * y)/ (b * b);
		if (f < 0) f = 0;
		if (f > 1) f = 1;
		return f;
	}
	
	public static boolean isHiddenBase(MarketAPI market) {
		return market.getMemoryWithoutUpdate().getBoolean(MemFlags.HIDDEN_BASE_MEM_FLAG);
	}
	
	
	public static boolean isReversePolarity(SectorEntityToken entity) {
		return entity.getMemoryWithoutUpdate().getBoolean(ReversePolarityToggle.REVERSED_POLARITY);
	}
	
	
	
	public static enum CatalogEntryType {
		PLANET("P"),
		GIANT("G"),
		STAR("S"),
		BLACK_HOLE("B");
		
		public String suffix;
		private CatalogEntryType(String suffix) {
			this.suffix = suffix;
		}
		
	}
	public static String genEntityCatalogId(CatalogEntryType type) {
		return genEntityCatalogId(-1, -1, -1, type);
	}
	public static String genEntityCatalogId(int cycleOverride, int monthOverride, int dayOverride, CatalogEntryType type) {
		return genEntityCatalogId(null, cycleOverride, monthOverride, dayOverride, type);
	}
	public static String genEntityCatalogId(String firstChar, int cycleOverride, int monthOverride, int dayOverride, CatalogEntryType type) {
		
		String base = "Perseus";
		
		int cycle = Global.getSector().getClock().getCycle();
		cycle += 3000;
		if (cycleOverride > 0) cycle = cycleOverride; 
		
		int month = Global.getSector().getClock().getMonth();
		if (monthOverride > 0) month = monthOverride;
		int day = Global.getSector().getClock().getDay();
		if (dayOverride > 0) day = dayOverride;
		
		String s1 = Integer.toHexString(cycle).toUpperCase();
		
		Random r = StarSystemGenerator.random;
		
		String s0 = Integer.toHexString(r.nextInt(16)).toUpperCase();
		if (firstChar != null) s0 = firstChar;
		
		String s2 = Integer.toHexString(month).toUpperCase();
		String s3 = Integer.toHexString(day).toUpperCase();
		
//		s1 = "" + cycle;
//		s0 = "";
		
		while (s1.length() < 3) s1 = "0" + s1;
		while (s3.length() < 2) s3 = "0" + s3;
		
		return base + " " + s0 + s1 + "-" + s2 + s3 + type.suffix;
	}
	
	public static float getAveragePlanetRadius(PlanetSpecAPI spec) {
		if (spec.isStar()) {
			StarGenDataSpec starData = (StarGenDataSpec) 
					Global.getSettings().getSpec(StarGenDataSpec.class, spec.getPlanetType(), true);
			if (starData != null) { 
				return (starData.getMinRadius() + starData.getMaxRadius()) * 0.5f;
			}
		}
		
		PlanetGenDataSpec planetData = (PlanetGenDataSpec) 
				Global.getSettings().getSpec(PlanetGenDataSpec.class, spec.getPlanetType(), true);
		if (planetData != null) {
			return (planetData.getMinRadius() + planetData.getMaxRadius()) * 0.5f;
		}
		
		return 200f;
	}
	
	public static boolean canPlanetTypeRollHabitable(PlanetSpecAPI spec) {
		return canPlanetTypeRollCondition(spec, Conditions.HABITABLE);
	}
	
	public static boolean canPlanetTypeRollCondition(PlanetSpecAPI spec, String id) {
		ConditionGenDataSpec hab = (ConditionGenDataSpec) 
				Global.getSettings().getSpec(ConditionGenDataSpec.class, id, true);

		PlanetGenDataSpec genData = (PlanetGenDataSpec) 
				Global.getSettings().getSpec(PlanetGenDataSpec.class, spec.getPlanetType(), true);

		if (genData != null && hab != null) {
			String planetCat = genData.getCategory();
			if (hab.hasMultiplier(planetCat) && hab.getMultiplier(planetCat) > 0) {
				return true;
			}
		}
		return false;
	}
	
	public static int getMaxMarketSize(MarketAPI market) {
		return (int)Math.round(market.getStats().getDynamic().getMod(
							Stats.MAX_MARKET_SIZE).computeEffective(Misc.MAX_COLONY_SIZE));
	}
	
	public static float countEnemyWeightInArc(ShipAPI ship, float dir, float arc, float maxRange, boolean ignoreFightersAndModules) {
		return countEnemyWeightInArcAroundLocation(ship, ship.getLocation(), dir, arc, maxRange, null, ignoreFightersAndModules);
	}
	public static float countEnemyWeightInArcAroundLocation(ShipAPI ship, Vector2f loc, float dir, float arc, float maxRange,
								ShipAPI ignore, boolean ignoreFightersAndModules) {
		return countEnemyWeightInArcAroundLocation(ship.getOwner(), loc, dir, arc, maxRange, ignore, ignoreFightersAndModules, false);
	}
	public static float countEnemyWeightInArcAroundLocation(int owner, Vector2f loc, 
				float dir, float arc, float maxRange,
				ShipAPI ignore, boolean ignoreFightersAndModules, boolean awareOnly) {
		CombatEngineAPI engine = Global.getCombatEngine();
		List<ShipAPI> ships = engine.getAllShips();
		
		float weight = 0;
		for (ShipAPI other : ships) {
			if (ignoreFightersAndModules) {
				if (other.isFighter()) continue;
				if (other.isStationModule()) continue;
			}
			if (other.isFighter() && other.getWing() != null && !other.getWing().isLeader(other)) continue;
			if (other.isHulk()) continue;
			if (other.isDrone()) continue;
			if (other.isShuttlePod()) continue;
			if (other.getOwner() == 100) continue;
			if (owner == other.getOwner()) continue;
			//if (other.isRetreating()) continue;
			if (other.controlsLocked()) continue;
			if (other == ignore) continue;
			if (awareOnly && !engine.isAwareOf(owner, other)) continue;
			
			float dist = getDistance(loc, other.getLocation());
			if (dist > maxRange) continue;

			if (arc >= 360f || isInArc(dir, arc, loc, other.getLocation())) {
				weight += getShipWeight(other);
				//weight += other.getHullSize().ordinal();
			}
		}
		return weight;
	}
	
	public static float [] getFloatArray(String key) {
		try {
			JSONArray arr = Global.getSettings().getJSONArray(key);
			float [] result = new float [arr.length()];
			for (int i = 0; i < arr.length(); i++) {
				result[i] = (float) arr.optDouble(i, 0f);
			}
			return result;
		} catch (JSONException e) {
			return null;
		}
	}
	
	public static enum WeaponSkinType {
		UNDER,
		TURRET,
		HARDPOINT,
		TURRET_GLOW,
		HARDPOINT_GLOW,
		TURRET_BARRELS,
		HARDPOINT_BARRELS,
	}
	
	public static SpriteAPI getWeaponSkin(ShipAPI ship, String weaponId, WeaponSkinType type) {
		String cat = null;
		SpriteAPI skin = null;
		if (ship.getOwner() == 0 || ship.getOriginalOwner() == 0) {
			cat = "weaponSkinsPlayerOnly";
			skin = getWeaponSkin(cat, weaponId, ship, type);
		}
		if (skin != null) return skin;
		
		cat = "weaponSkinsPlayerAndNPC";
		skin = getWeaponSkin(cat, weaponId, ship, type);
		return skin;
	}
	
	
	public static SpriteAPI getWeaponSkin(String cat, String weaponId, ShipAPI ship, WeaponSkinType type) {
		
		String exclude = "weaponSkinsExcludeFromSharing";
		String style = ship.getHullStyleId();
		
		List<String> skins = Global.getSettings().getSpriteKeys(cat);
		Set<String> noSharing = new LinkedHashSet<String>(Global.getSettings().getSpriteKeys(exclude));
		
		List<SpriteAPI> matching = new ArrayList<SpriteAPI>();
		String keyForHull = weaponId + ":" + style + ":" + type.name();
		for (String key : skins) {
			if (key.equals(keyForHull)) {
				return Global.getSettings().getSprite(cat, key);
			}
			if (key.startsWith(weaponId) && key.endsWith(type.name()) && !noSharing.contains(key)) {
				matching.add(Global.getSettings().getSprite(cat, key));
			}
		}
		
		if (!matching.isEmpty()) {
			SpriteAPI best = null;
			float minDist = Float.MAX_VALUE;
			
			for (SpriteAPI curr : matching) {
				float dist = Misc.getColorDist(ship.getSpriteAPI().getAverageBrightColor(), curr.getAverageBrightColor());
				if (dist < minDist) {
					best = curr;
					minDist = dist;
				}
			}
			return best;
		}
		return null;
	}
}
















