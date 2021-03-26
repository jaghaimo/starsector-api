package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;
import java.util.EnumSet;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TerrainAIFlags;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.impl.combat.BattleCreationPluginImpl.NebulaTextureProvider;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class NebulaTerrainPlugin extends BaseTiledTerrain implements NebulaTextureProvider {
//	public static float MIN_BURN_PENALTY = 0.1f;
//	public static float BURN_PENALTY_RANGE = 0.4f;
	
	
	public static final float TILE_SIZE = 400;
	public static final float VISIBLITY_MULT = 0.5f;
	
//	public static Map<HullSize, Float> burnPenalty = new HashMap<HullSize, Float>();
//	static {
//		burnPenalty.put(HullSize.FIGHTER, 3f);
//		burnPenalty.put(HullSize.FRIGATE, 3f);
//		burnPenalty.put(HullSize.DESTROYER, 2f);
//		burnPenalty.put(HullSize.CRUISER, 1f);
//		burnPenalty.put(HullSize.CAPITAL_SHIP, 0f);
//	}
	
	
	public void init(String terrainId, SectorEntityToken entity, Object param) {
		super.init(terrainId, entity, param);
		if (name == null || name.equals("Unknown")) name = "Nebula";
	}
	
	public String getNameForTooltip() {
		return "Nebula";
	}
	
	protected Object readResolve() {
		super.readResolve();
		layers = EnumSet.of(CampaignEngineLayers.TERRAIN_2, CampaignEngineLayers.TERRAIN_8);
		return this;
	}
	transient private EnumSet<CampaignEngineLayers> layers = EnumSet.of(CampaignEngineLayers.TERRAIN_2, CampaignEngineLayers.TERRAIN_8);
	public EnumSet<CampaignEngineLayers> getActiveLayers() {
		return layers;
	}
	

	public String getNebulaMapTex() {
		return Global.getSettings().getSpriteName(params.cat, params.key + "_map");
	}

	public String getNebulaTex() {
		return Global.getSettings().getSpriteName(params.cat, params.key);
	}
	
	public void advance(float amount) {
		super.advance(amount);
	}
		

	private transient CampaignEngineLayers currLayer = null;
	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		currLayer = layer;
		super.render(layer, viewport);

		
//		float x = entity.getLocation().x;
//		float y = entity.getLocation().y;
//		float size = getTileSize();
//		float renderSize = getTileRenderSize();
//		
//		float w = tiles.length * size;
//		float h = tiles[0].length * size;
//		
//		x -= w / 2f;
//		y -= h / 2f;
//		
//		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		GL11.glEnable(GL11.GL_BLEND);
//		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//
//		Color color = Color.white;
//		GL11.glColor4ub((byte)color.getRed(),
//						(byte)color.getGreen(),
//						(byte)color.getBlue(),
//						(byte)((float)color.getAlpha() * 0.25f));
//		
//		GL11.glBegin(GL11.GL_QUADS);
//		{
//			GL11.glVertex2f(x, y);
//			GL11.glVertex2f(x, y + h);
//			GL11.glVertex2f(x + w, y + h);
//			GL11.glVertex2f(x + w, y);
//		}
//		GL11.glEnd();
	}

	@Override
	public float getTileRenderSize() {
		//return TILE_SIZE + 300f;
		//return TILE_SIZE + 600f;
		return TILE_SIZE * 2.5f;
	}
	
	@Override
	public float getTileContainsSize() {
		//return TILE_SIZE + 200f;
		return TILE_SIZE * 1.5f;
	}

	@Override
	public float getTileSize() {
		return TILE_SIZE;
	}
	
	@Override
	protected void renderSubArea(float startColumn, float endColumn,
			float startRow, float endRow, float factor, int samples,
			float alphaMult) {
		
//		if (currLayer != CampaignEngineLayers.TERRAIN_8) {
//			float size = getTileSize();
//			float renderSize = getTileRenderSize();
//			float x = entity.getLocation().x - renderSize / 2f;
//			float y = entity.getLocation().y - renderSize / 2f;
//			
//			float w = tiles.length * size + renderSize;
//			float h = tiles[0].length * size + renderSize;
//			
//
//			GL11.glColorMask(false, false, false, true);
//			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);
//			GL11.glColor4ub((byte)0,
//					(byte)0,
//					(byte)0,
//					(byte)255);
//			GL11.glBegin(GL11.GL_QUADS);
//			{
//				GL11.glVertex2f(x, y);
//				GL11.glVertex2f(x, y + h);
//				GL11.glVertex2f(x + w, y + h);
//				GL11.glVertex2f(x + w, y);
//			}
//			GL11.glEnd();
//			
//			GL11.glColorMask(true, true, true, false);
//			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
//			Color color = getRenderColor();
//			GL11.glColor4ub((byte)color.getRed(),
//					(byte)color.getGreen(),
//					(byte)color.getBlue(),
//					(byte)((float)color.getAlpha() * alphaMult));
//		}
		
		super.renderSubArea(startColumn, endColumn, startRow, endRow, factor, samples, alphaMult);
		
		
//		if (currLayer != CampaignEngineLayers.TERRAIN_8) {
//			float size = getTileSize();
//			float renderSize = getTileRenderSize();
//			float x = entity.getLocation().x - renderSize / 2f;
//			float y = entity.getLocation().y - renderSize / 2f;
//			float w = tiles.length * size + renderSize;
//			float h = tiles[0].length * size + renderSize;
//			
//			x -= w/2f;
//			y -= h/2f;
//			
//			
//			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_DST_COLOR);
//			GL11.glColor4ub((byte)0,
//					(byte)0,
//					(byte)0,
//					(byte)255);
//			GL11.glBegin(GL11.GL_QUADS);
//			{
//				GL11.glVertex2f(x, y);
//				GL11.glVertex2f(x, y + h);
//				GL11.glVertex2f(x + w, y + h);
//				GL11.glVertex2f(x + w, y);
//			}
//			GL11.glEnd();
//			
//		}
	}

	@Override
	public void preRender(CampaignEngineLayers layer, float alphaMult) {
		GL11.glEnable(GL11.GL_BLEND);
		if (entity.isInHyperspace()) {
//			if (layer == CampaignEngineLayers.TERRAIN_8) {
//				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//			} else {
//				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//			}
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		} else {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}
		//GL11.glDisable(GL11.GL_BLEND);
		
		if (layer == CampaignEngineLayers.TERRAIN_8) {
			alphaMult *= 0.20f;
		}
		
		Color color = getRenderColor();
		GL11.glColor4ub((byte)color.getRed(),
				(byte)color.getGreen(),
				(byte)color.getBlue(),
				(byte)((float)color.getAlpha() * alphaMult));
	}
	
	@Override
	public void preMapRender(float alphaMult) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		
		//Color color = new Color(125,125,200,255);
		//Color color = new Color(100,100,150,255);
		Color color = getRenderColor();
		GL11.glColor4ub((byte)color.getRed(),
				(byte)color.getGreen(),
				(byte)color.getBlue(),
				(byte)((float)color.getAlpha() * alphaMult));
	}

	@Override
	public Color getRenderColor() {
		return Color.white;
		//return Color.black;
		//return Misc.interpolateColor(Color.black, Color.white, 0.1f);
		//return new Color(255,255,255,150);
	}
	
	@Override
	public void applyEffect(SectorEntityToken entity, float days) {
		if (entity instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
			fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_1",
								"Inside nebula", VISIBLITY_MULT, 
								fleet.getStats().getDetectedRangeMod());
			
			float penalty = Misc.getBurnMultForTerrain(fleet);
			//float penalty = getBurnPenalty(fleet);
			fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_2",
								"Inside nebula", penalty, 
								fleet.getStats().getFleetwideMaxBurnMod());
//			fleet.getStats().addTemporaryModPercent(0.1f, getModId() + "_2",
//								"Inside nebula", -100f * penalty, 
//								fleet.getStats().getFleetwideMaxBurnMod());

			
//			if (fleet.isPlayerFleet()) {
//				System.out.println("23gfgwefwef");
//			}
//			String buffId = getModId();
//			float buffDur = 0.1f;
//			for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
//				float penalty = burnPenalty.get(member.getHullSpec().getHullSize());
//				
//				Buff test = member.getBuffManager().getBuff(buffId);
//				if (test instanceof MaxBurnBuff) {
//					MaxBurnBuff buff = (MaxBurnBuff) test;
//					buff.setDur(buffDur);
//				} else {
//					member.getBuffManager().addBuff(new MaxBurnBuff(buffId, -penalty, buffDur));
//				}
//			}
		}
	}
	
//	public static float getBurnPenalty(CampaignFleetAPI fleet) {
//		float min = Global.getSettings().getBaseFleetSelectionRadius() + Global.getSettings().getFleetSelectionRadiusPerUnitSize();
//		float max = Global.getSettings().getMaxFleetSelectionRadius();
//		float radius = fleet.getRadius();
//
//		float penalty = 1f - (radius - min) / (max - min);
//		if (penalty > 1) penalty = 1;
//		if (penalty < 0) penalty = 0;
//		penalty = MIN_BURN_PENALTY + penalty * BURN_PENALTY_RANGE;
//
//		float skillMod = fleet.getCommanderStats().getDynamic().getValue(Stats.NAVIGATION_PENALTY_MULT);
//		penalty *= skillMod;
//		
//		return penalty;
//	}

	public boolean hasTooltip() {
		return true;
	}
	
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		float pad = 10f;
		float small = 5f;
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color fuel = Global.getSettings().getColor("progressBarFuelColor");
		Color bad = Misc.getNegativeHighlightColor();
		Color text = Misc.getTextColor();
		
		tooltip.addTitle("Nebula");
		tooltip.addPara(Global.getSettings().getDescription(getTerrainId(), Type.TERRAIN).getText1(), pad);
		float nextPad = pad;
		if (expanded) {
			tooltip.addSectionHeading("Travel", Alignment.MID, pad);
			nextPad = small;
		}
		tooltip.addPara("Reduces the range at which fleets inside can be detected by %s.", nextPad,
				highlight, 
				"" + (int) ((1f - VISIBLITY_MULT) * 100) + "%"
		);
		
		tooltip.addPara("Reduces the travel speed of fleets inside by up to %s. Larger fleets are slowed down more.",
				pad,
				highlight,
				"" + (int) ((Misc.BURN_PENALTY_MULT) * 100f) + "%"
		);
		
		float penalty = Misc.getBurnMultForTerrain(Global.getSector().getPlayerFleet());
		String penaltyStr = Misc.getRoundedValue(1f - penalty);
		tooltip.addPara("Your fleet's speed is reduced by %s.", pad,
				highlight,
				"" + (int) Math.round((1f - penalty) * 100) + "%"
				//Strings.X + penaltyStr
		);
		
//		tooltip.addPara("Reduces the maximum burn level of ships depending on size. Larger ship classes are able to exploit a higher mass to cross section ratio and suffer a smaller penalty.", pad);
//		tooltip.beginGrid(150, 1);
//		tooltip.addToGrid(0, 0, "  Frigates", "" + -burnPenalty.get(HullSize.FRIGATE).intValue());
//		tooltip.addToGrid(0, 1, "  Destroyers", "" + -burnPenalty.get(HullSize.DESTROYER).intValue());
//		tooltip.addToGrid(0, 2, "  Cruisers", "" + -burnPenalty.get(HullSize.CRUISER).intValue());
//		tooltip.addToGrid(0, 3, "  Capital ships", "" + -burnPenalty.get(HullSize.CAPITAL_SHIP).intValue());
//		tooltip.addGrid(small);
		
		if (expanded) { 
			tooltip.addSectionHeading("Combat", Alignment.MID, pad);
			tooltip.addPara("Numerous patches of nebula present on the battlefield, but the medium is not dense enough to affect ships moving at combat speeds.", small);
//			tooltip.addPara("Numerous patches of nebula present on the battlefield, slowing ships down to a percentage of their top speed.", small);
//			tooltip.beginGrid(150, 1);
//	//		nebulaSpeedFighter":1,
//	//		"nebulaSpeedFrigate":0.7,
//	//		"nebulaSpeedDestroyer":0.8,
//	//		"nebulaSpeedCruiser":0.9,
//	//		"nebulaSpeedCapital":1,
//			tooltip.addToGrid(0, 0, "  Frigates", "" + (int)(Global.getSettings().getFloat("nebulaSpeedFrigate") * 100f) + "%");
//			tooltip.addToGrid(0, 1, "  Destroyers", "" + (int)(Global.getSettings().getFloat("nebulaSpeedDestroyer") * 100f) + "%");
//			tooltip.addToGrid(0, 2, "  Cruisers", "" + (int)(Global.getSettings().getFloat("nebulaSpeedCruiser") * 100f) + "%");
//			//tooltip.addToGrid(0, 3, "  Capital ships", "" + (int)(Global.getSettings().getFloat("nebulaSpeedCapital") * 100f) + "%");
//			tooltip.addGrid(small);
//			
//			tooltip.addPara("Capital ships and fighters are not affected.", pad);
		}
		
		//tooltip.addPara("Does not stack with other similar terrain effects.", pad);
	}
	
	public boolean isTooltipExpandable() {
		return true;
	}
	
	public float getTooltipWidth() {
		return 350f;
	}
	
	public String getEffectCategory() {
		return "nebula-like";
	}

	public boolean hasAIFlag(Object flag) {
		return flag == TerrainAIFlags.REDUCES_DETECTABILITY || 
				flag == TerrainAIFlags.REDUCES_SPEED_LARGE||
				flag == TerrainAIFlags.TILE_BASED
				;
	}
}



