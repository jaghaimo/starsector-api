package com.fs.starfarer.api.impl.campaign.abilities;

import java.awt.Color;
import java.util.EnumSet;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class GraviticScanAbility extends BaseToggleAbility {

	public static float SLIPSTREAM_DETECTION_RANGE = 20000f;
	
	public static String COMMODITY_ID = Commodities.VOLATILES;
	public static float COMMODITY_PER_DAY = 1f;
	
	public static float DETECTABILITY_PERCENT = 50f;
	
	
	@Override
	protected String getActivationText() {
		if (COMMODITY_ID != null && getFleet() != null && getFleet().getCargo().getCommodityQuantity(COMMODITY_ID) <= 0 &&
				(!Global.getSettings().isDevMode() || Global.getSettings().getBoolean("playtestingMode"))) {
			return null;
		}
		return "Neutrino detector activated";
	}
	
	@Override
	protected String getDeactivationText() {
		return null;
	}


	@Override
	protected void activateImpl() {

	}

	@Override
	public boolean showProgressIndicator() {
		return false;
	}
	
	@Override
	public boolean showActiveIndicator() {
		return isActive();
	}

	
	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		Color bad = Misc.getNegativeHighlightColor();
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		
		String status = " (off)";
		if (turnedOn) {
			status = " (on)";
		}
		
		LabelAPI title = tooltip.addTitle(spec.getName() + status);
		title.highlightLast(status);
		title.setHighlightColor(gray);

		float pad = 10f;
		
		
		tooltip.addPara("Reconfigures the fleet's drive field to act as a neutrino detector, " +
				"allowing detection of human-made artifacts - and occasionally fleets - at extreme ranges. ", pad);
		
		tooltip.addSectionHeading("Normal space", Alignment.MID, pad);
		tooltip.addPara("High-emission sources such as stars, planets, jump-points, or space stations produce constant streams. " +
				"Average sources produce periodic bursts. Low-emission sources produce occasional bursts.", pad);
		
		tooltip.addPara("Notoriously unreliable and almost guaranteed to produce numerous false readings.", pad);

		
		if (COMMODITY_ID != null) {
			String unit = "unit";
			if (COMMODITY_PER_DAY != 1) unit = "units";
			CommoditySpecAPI spec = getCommodity();
			unit += " of " + spec.getName().toLowerCase();
			
			tooltip.addPara("Increases the range at which the fleet can be detected by %s and consumes %s " + unit + " per day.",
					pad, highlight,
					"" + (int)DETECTABILITY_PERCENT + "%",
					"" + Misc.getRoundedValueMaxOneAfterDecimal(COMMODITY_PER_DAY)
			);
		} else {
			tooltip.addPara("Increases the range at which the fleet can be detected by %s.",
					pad, highlight,
					"" + (int)DETECTABILITY_PERCENT + "%"
			);
		}
		
		int maxRange = (int) Math.round(SLIPSTREAM_DETECTION_RANGE / Misc.getUnitsPerLightYear());
		tooltip.addSectionHeading("Hyperspace", Alignment.MID, pad);
		tooltip.addPara("Reliably detects the presence of slipstreams out to a range of %s light-years. "
				+ "The background noise levels are such that it is unable to detect any other neutrino sources. "
				+ "When the fleet is traversing a slipstream, the detector is overwhelmed and shuts down.",
				pad, highlight, "" + maxRange);
		if (Misc.isInsideSlipstream(getFleet())) {
			tooltip.addPara("Cannot activate while inside slipstream.", bad, pad);
		}
//		if (getFleet() != null && getFleet().isInHyperspace()) {
//			tooltip.addPara("Can not function in hyperspace.", bad, pad);
//		} else {
//			tooltip.addPara("Can not function in hyperspace.", pad);
//		}
		
		//tooltip.addPara("Disables the transponder when activated.", pad);
		addIncompatibleToTooltip(tooltip, expanded);
	}

	public boolean hasTooltip() {
		return true;
	}
	
	@Override
	public EnumSet<CampaignEngineLayers> getActiveLayers() {
		return EnumSet.of(CampaignEngineLayers.ABOVE);
	}


	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		if (data != null && !isActive() && getProgressFraction() <= 0f) {
			data = null;
		}
	}
	

	protected float phaseAngle;
	protected GraviticScanData data = null;
	@Override
	protected void applyEffect(float amount, float level) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		//if (level < 1) level = 0;
		
		fleet.getStats().getDetectedRangeMod().modifyPercent(getModId(), DETECTABILITY_PERCENT * level, "Gravimetric scan");

		float days = Global.getSector().getClock().convertToDays(amount);
		phaseAngle += days * 360f * 10f;
		phaseAngle = Misc.normalizeAngle(phaseAngle);
		
		if (data == null) {
			data = new GraviticScanData(this);
		}
		data.advance(days);
		
		if (COMMODITY_ID != null) {
			float cost = days * COMMODITY_PER_DAY;
			if (fleet.getCargo().getCommodityQuantity(COMMODITY_ID) > 0 || (Global.getSettings().isDevMode() && !Global.getSettings().getBoolean("playtestingMode"))) {
				fleet.getCargo().removeCommodity(COMMODITY_ID, cost);
			} else {
				CommoditySpecAPI spec = getCommodity();
				fleet.addFloatingText("Out of " + spec.getName().toLowerCase(), Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
				deactivate();
			}
		}
		
		if (Misc.isInsideSlipstream(fleet)) {
			deactivate();
		}
//		if (fleet.isInHyperspace()) {
//			deactivate();
//		}
	}
	
	public CommoditySpecAPI getCommodity() {
		return Global.getSettings().getCommoditySpec(COMMODITY_ID);
	}
	
	@Override
	public boolean isUsable() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return false;
		
		return !Misc.isInsideSlipstream(fleet);
		//return isActive() || !fleet.isInHyperspace();
	}
	

	@Override
	protected void deactivateImpl() {
		cleanupImpl();
	}
	
	@Override
	protected void cleanupImpl() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		fleet.getStats().getDetectedRangeMod().unmodify(getModId());
		//data = null;
	}




	public float getRingRadius() {
		return getFleet().getRadius() + 75f;	
		//return getFleet().getRadius() + 25f;	
	}
	
	transient protected SpriteAPI texture;
	@Override
	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		
		if (data == null) return;
		
		float level = getProgressFraction();
		if (level <= 0) return;
		if (getFleet() == null) return;
		if (!getFleet().isPlayerFleet()) return;
		
		float alphaMult = viewport.getAlphaMult() * level;
		
//		float x = getFleet().getLocation().x;
//		float y = getFleet().getLocation().y;
//		
//		GL11.glPushMatrix();
//		GL11.glTranslatef(x, y, 0);
//		
//		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		Misc.renderQuad(30, 30, 100, 100, Color.green, alphaMult * level);
//		
//		
//		GL11.glPopMatrix();
	
		
		//float noiseLevel = data.getNoiseLevel();
		
		float bandWidthInTexture = 256;
		float bandIndex;
		
		float radStart = getRingRadius();
		float radEnd = radStart + 75f;
		
		float circ = (float) (Math.PI * 2f * (radStart + radEnd) / 2f);
		//float pixelsPerSegment = 10f;
		float pixelsPerSegment = circ / 360f;
		//float pixelsPerSegment = circ / 720;
		float segments = Math.round(circ / pixelsPerSegment);
		
//		segments = 360;
//		pixelsPerSegment = circ / segments;
		//pixelsPerSegment = 10f;
		
		float startRad = (float) Math.toRadians(0);
		float endRad = (float) Math.toRadians(360f);
		float spanRad = Math.abs(endRad - startRad);
		float anglePerSegment = spanRad / segments;
		
		Vector2f loc = getFleet().getLocation();
		float x = loc.x;
		float y = loc.y;

		
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0);
		
		//float zoom = viewport.getViewMult();
		//GL11.glScalef(zoom, zoom, 1);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		if (texture == null) texture = Global.getSettings().getSprite("abilities", "neutrino_detector");
		texture.bindTexture();
		
		GL11.glEnable(GL11.GL_BLEND);
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		boolean outlineMode = false;
		//outlineMode = true;
		if (outlineMode) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			//GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		}
		
		float thickness = (radEnd - radStart) * 1f;
		float radius = radStart;

		float texProgress = 0f;
		float texHeight = texture.getTextureHeight();
		float imageHeight = texture.getHeight();
		float texPerSegment = pixelsPerSegment * texHeight / imageHeight * bandWidthInTexture / thickness;
		
		texPerSegment *= 1f;
		
		float totalTex = Math.max(1f, Math.round(texPerSegment * segments));
		texPerSegment = totalTex / segments;
		
		float texWidth = texture.getTextureWidth();
		float imageWidth = texture.getWidth();
		
		
		
		Color color = new Color(25,215,255,255);
		//Color color = new Color(255,25,255,155);
		
		
		for (int iter = 0; iter < 2; iter++) {
			if (iter == 0) {
				bandIndex = 1;
			} else {
				//color = new Color(255,215,25,255);
				//color = new Color(25,255,215,255);
				bandIndex = 0;
				texProgress = segments/2f * texPerSegment;
				//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			}
			if (iter == 1) {
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			}
			//bandIndex = 1;
			
			float leftTX = (float) bandIndex * texWidth * bandWidthInTexture / imageWidth;
			float rightTX = (float) (bandIndex + 1f) * texWidth * bandWidthInTexture / imageWidth - 0.001f;
			
			GL11.glBegin(GL11.GL_QUAD_STRIP);
			for (float i = 0; i < segments + 1; i++) {
				
				float segIndex = i % (int) segments;
				
				//float phaseAngleRad = (float) Math.toRadians(phaseAngle + segIndex * 10) + (segIndex * anglePerSegment * 10f);
				float phaseAngleRad;
				if (iter == 0) {
					phaseAngleRad = (float) Math.toRadians(phaseAngle) + (segIndex * anglePerSegment * 29f);
				} else { //if (iter == 1) { 
					phaseAngleRad = (float) Math.toRadians(-phaseAngle) + (segIndex * anglePerSegment * 17f);
				}
				
				
				float angle = (float) Math.toDegrees(segIndex * anglePerSegment);
				//if (iter == 1) angle += 180;
				
				
				float pulseSin = (float) Math.sin(phaseAngleRad);
				float pulseMax = thickness * 0.5f;

				pulseMax = thickness * 0.2f;
				pulseMax = 10f;
				
				//pulseMax *= 0.25f + 0.75f * noiseLevel;
				
				float pulseAmount = pulseSin * pulseMax;
				//float pulseInner = pulseAmount * 0.1f;
				float pulseInner = pulseAmount * 0.1f;
				
				float r = radius;

//				float thicknessMult = delegate.getAuroraThicknessMult(angle);
//				float thicknessFlat = delegate.getAuroraThicknessFlat(angle);
				
				float theta = anglePerSegment * segIndex;;
				float cos = (float) Math.cos(theta);
				float sin = (float) Math.sin(theta);
				
				float rInner = r - pulseInner;
				//if (rInner < r * 0.9f) rInner = r * 0.9f;
				
				//float rOuter = (r + thickness * thicknessMult - pulseAmount + thicknessFlat);
				float rOuter = r + thickness - pulseAmount;
				
				
				//rOuter += noiseLevel * 25f;
				
				float grav = data.getDataAt(angle);
				//if (grav > 500) System.out.println(grav);
				//if (grav > 300) grav = 300;
				if (grav > 750) grav = 750;
				grav *= 250f / 750f;
				grav *= level;
				//grav *= 0.5f;
				//rInner -= grav * 0.25f;
				
				//rInner -= grav * 0.1f;
				rOuter += grav;
//				rInner -= grav * 3f;
//				rOuter -= grav * 3f;
				//System.out.println(grav);
				
				float alpha = alphaMult;
				alpha *= 0.25f + Math.min(grav / 100, 0.75f);
				//alpha *= 0.75f;
				
//			
//				
//				
//				phaseAngleWarp = (float) Math.toRadians(phaseAngle - 180 * iter) + (segIndex * anglePerSegment * 1f);
//				float warpSin = (float) Math.sin(phaseAngleWarp);
//				rInner += thickness * 0.5f * warpSin;
//				rOuter += thickness * 0.5f * warpSin;
				
				
				
				float x1 = cos * rInner;
				float y1 = sin * rInner;
				float x2 = cos * rOuter;
				float y2 = sin * rOuter;
				
				x2 += (float) (Math.cos(phaseAngleRad) * pixelsPerSegment * 0.33f);
				y2 += (float) (Math.sin(phaseAngleRad) * pixelsPerSegment * 0.33f);
				
				
				GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)((float) color.getAlpha() * alphaMult * alpha));
				
				GL11.glTexCoord2f(leftTX, texProgress);
				GL11.glVertex2f(x1, y1);
				GL11.glTexCoord2f(rightTX, texProgress);
				GL11.glVertex2f(x2, y2);
				
				texProgress += texPerSegment * 1f;
			}
			GL11.glEnd();
			
			//GL11.glRotatef(180, 0, 0, 1);
		}
		GL11.glPopMatrix();
		
		if (outlineMode) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		}
	}
	


	
	
	
	
}





