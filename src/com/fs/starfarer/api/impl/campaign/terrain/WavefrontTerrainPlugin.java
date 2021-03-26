package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;
import java.util.EnumSet;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class WavefrontTerrainPlugin extends BaseTerrain {
	
	public static class WavefrontParams {
		public float burnLevel;
		public float crLossMult;
		public float arcOriginRange;
		public float startWidth;
		public float widthExpansionRate;
		public float startThickness;
		public float thicknessExpansionRate;
		public float duration;
		public float angle;
		public WavefrontParams(float burnLevel, float crLossMult,
				float arcOriginRange, float startWidth,
				float widthExpansionRate, float startThickness,
				float thicknessExpansionRate, float duration, float angle) {
			this.burnLevel = burnLevel;
			this.crLossMult = crLossMult;
			this.arcOriginRange = arcOriginRange;
			this.startWidth = startWidth;
			this.widthExpansionRate = widthExpansionRate;
			this.startThickness = startThickness;
			this.thicknessExpansionRate = thicknessExpansionRate;
			this.duration = duration;
			this.angle = angle;
		}
		
	}
	
	transient protected SpriteAPI texture = null;
	protected WavefrontParams params;
	protected Vector2f arcOrigin = new Vector2f();
	protected Vector2f velDir = new Vector2f(), p1 = new Vector2f(), p2 = new Vector2f();
	protected float distanceTravelled = 0f;
	protected float currentWidth;
	protected float currentThickness;
	protected float currentArc = 0f;
	protected float phaseAngle;
	protected int numSegments;
	protected FaderUtil fader = new FaderUtil(0, 0.2f, 1f);
	
	public void init(String terrainId, SectorEntityToken entity, Object param) {
		super.init(terrainId, entity, param);
		params = (WavefrontParams) param;
		fader.fadeIn();
		
		velDir = Misc.getUnitVectorAtDegreeAngle(params.angle);
		
		currentThickness = params.startThickness;
		currentWidth = params.startWidth;
		
		numSegments = (int) ((params.startWidth + params.widthExpansionRate * params.duration) / getPixelsPerSegment());
		if (numSegments < 5) numSegments = 5;
		
		phaseAngle = (float) Math.random() * 360f;
		
		readResolve();
	}
	
	Object readResolve() {
		texture = Global.getSettings().getSprite("terrain", "wavefront");
		layers = EnumSet.of(CampaignEngineLayers.TERRAIN_6, CampaignEngineLayers.TERRAIN_10);
		return this;
	}
	
	protected float getPixelsPerSegment() {
		return 25f;
	}
	
	Object writeReplace() {
		return this;
	}
	
	transient private EnumSet<CampaignEngineLayers> layers = EnumSet.of(CampaignEngineLayers.TERRAIN_6, CampaignEngineLayers.TERRAIN_10);
	public EnumSet<CampaignEngineLayers> getActiveLayers() {
		return layers;
	}

	public WavefrontParams getParams() {
		return params;
	}

	protected void updateArcOrigin() {
		arcOrigin = Misc.getUnitVectorAtDegreeAngle(params.angle);
		arcOrigin.scale(params.arcOriginRange + distanceTravelled);
		arcOrigin.negate();
		Vector2f.add(entity.getLocation(), arcOrigin, arcOrigin);
	}
	
	public void advance(float amount) {
		super.advance(amount);
		
		// doing this every frame in case something manupulates the location directly
		// such as the location being set initially
		updateArcOrigin();
		updateArcOfCurrWidth();
		
		float days = Global.getSector().getClock().convertToDays(amount);
		fader.advance(days);
		params.duration -= days;
		if (params.duration <= 0) {
			fader.fadeOut();
		}
		if (fader.isFadedOut()) {
			entity.getContainingLocation().removeEntity(entity);
			return;
		}
		
		currentWidth += params.widthExpansionRate * days;
		currentThickness += params.thicknessExpansionRate * days;
		
		float speed = Misc.getSpeedForBurnLevel(params.burnLevel);
		speed *= fader.getBrightness();
		distanceTravelled += speed * amount;
		
		entity.getVelocity().set(velDir); 
		entity.getVelocity().scale(speed);
		
//		arcOrigin.x += entity.getVelocity().x * amount;
//		arcOrigin.y += entity.getVelocity().y * amount;
		
//		float angle1 = Misc.getAngleInDegrees(arcOrigin, p1);
//		float angle2 = Misc.getAngleInDegrees(arcOrigin, p2);
//		float startRad = (float) Math.toRadians(angle1);
//		float endRad = (float) Math.toRadians(angle2);
//		float spanRad = Math.abs(endRad - startRad);
//		float periodMult = 3.1415f * 2f / spanRad * 0.25f * currentWidth / 800f;
		
		phaseAngle += days * 360f * 0.2f * 1f * (1f / (currentWidth / 800f));
		//phaseAngle += days * 360f * 0.2f;
		
		//phaseAngle += days * 360f * 0.2f * 1f;
		//phaseAngle = Misc.normalizeAngle(phaseAngle); // not necessary due to wavefront lifetime, also need to normalise phaseAngleRad later before multiplying by periodMult.
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		//if (true) return;
		
		float alphaMult = viewport.getAlphaMult();
		alphaMult *= fader.getBrightness();
		if (alphaMult <= 0) return;
		
		float xOff = 0;
		float yOff = 0;
		//float phaseAngle = this.phaseAngle;
		if (layer == CampaignEngineLayers.TERRAIN_10) {
			//return;
			float offset = 50;
			if (offset > currentThickness * 0.5f) {
				offset = currentThickness * 0.5f;
			}
			xOff = velDir.x * -offset;
			yOff = velDir.y * -offset;
			//phaseAngle = this.phaseAngle + 35f;
			//return;
		}
		
		
		
		float bandWidthInTexture = 256f;
		float bandIndex;
		
		float max = params.arcOriginRange + distanceTravelled;
		float min = max - currentThickness;
		float radStart = min;
		float radEnd = max;
		
		if (radEnd < radStart + 10f) radEnd = radStart + 10f;
		
		float segments = numSegments;
		
		float angle1 = Misc.getAngleInDegrees(arcOrigin, p1);
		float angle2 = Misc.getAngleInDegrees(arcOrigin, p2);
		float turnDir = Misc.getClosestTurnDirection(Misc.getUnitVectorAtDegreeAngle(angle1),
													 Misc.getUnitVectorAtDegreeAngle(angle2));
		
		float startRad = (float) Math.toRadians(angle1);
		float endRad = (float) Math.toRadians(angle2);
		if (startRad > endRad) {
			endRad += Math.PI * 2f;
		}
		float spanRad = Math.abs(endRad - startRad);
		float anglePerSegment = spanRad / segments;
		
		Vector2f loc = arcOrigin;
		float x = loc.x;
		float y = loc.y;

		
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		texture.bindTexture();
		
		GL11.glEnable(GL11.GL_BLEND);
		
//		if (layer == campaignenginelayers.terrain_10) {
//			gl11.glblendfunc(gl11.gl_src_alpha, gl11.gl_one);
//		} else {
//			gl11.glblendfunc(gl11.gl_src_alpha, gl11.gl_one_minus_src_alpha);
//		}
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		
		float thickness = (radEnd - radStart) * 1f;
		float radius = radStart;

		float texProgress = 0f;
		//texProgress = (float) (phaseAngle / 360f) * 10f;
		
		
		float texHeight = texture.getTextureHeight();
		float imageHeight = texture.getHeight();
		//float texPerSegment = getPixelsPerSegment() * texHeight / imageHeight * bandWidthInTexture / thickness * 1f;
		//float texPerSegment = getPixelsPerSegment() * texHeight / imageHeight * thickness / bandWidthInTexture;
		//float texPerSegment = getPixelsPerSegment() * texHeight / imageHeight * params.startThickness / bandWidthInTexture;
		float texPerSegment = getPixelsPerSegment() * texHeight / imageHeight * bandWidthInTexture / params.startThickness;
		texPerSegment *= 200f/256f;
		texPerSegment *= 200f/256f;
		//float texPerSegment = getPixelsPerSegment() * texHeight / imageHeight;
		//float texPerSegment = getPixelsPerSegment() * texHeight / imageHeight;
		//texPerSegment = getPixelsPerSegment() * texHeight / imageHeight;
		//System.out.println("TPS: " + texPerSegment);
		
		texPerSegment *= 1f;
		
		float totalTex = Math.max(1f, Math.round(texPerSegment * segments));
		texPerSegment = totalTex / segments;
		
		float texWidth = texture.getTextureWidth();
		float imageWidth = texture.getWidth();
		
//		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		GL11.glDisable(GL11.GL_BLEND);
//		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
//		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		float fadeDist = getFadeDist();
		
		//float periodMult = ((float) Math.PI * 2f) / (spanRad * 4f);
		//float periodMult = 360f / Misc.getAngleDiff(angle1, angle2) * 0.5f;
		float periodMult = 3.1415f * 2f / spanRad * 0.25f * currentWidth / 800f;
		//float periodMult = 3.1415f * 2f / spanRad * 0.25f * (float) Math.pow(currentWidth / 800f, .75f);
		//float periodMult = 3.1415f * 2f / (float) Math.pow(spanRad, 0.15f) * 0.25f * currentWidth / 800f;
		//System.out.println(periodMult);
		//periodMult = 2f;
		texProgress = (float) (phaseAngle / 360f) * 5f * 0.25f;
		if (layer == CampaignEngineLayers.TERRAIN_10) {
			periodMult *= 1.25f;
			texProgress = (float) (phaseAngle / 360f) * -1f * 0.25f;
		}
		
		//float periodMult = 3.1415f * 2f / (3.1415f * 0.05f) * 0.25f;
		//periodMult = 15f;
		//System.out.println("P: " + periodMult);
		//alphaMult *= 0.75f;
		//periodMult = 20f;
		for (int iter = 0; iter < 4; iter++) {
			if (iter == 0) {
				bandIndex = 1;
			} else {
				if (iter == 2) bandIndex = 2;
				else if (iter == 3) bandIndex = 3;
				else bandIndex = 0;
			}
			
			float leftTX = (float) bandIndex * texWidth * bandWidthInTexture / imageWidth;
			float rightTX = (float) (bandIndex + 1f) * texWidth * bandWidthInTexture / imageWidth - 0.001f;
			
			float temp = leftTX;
			leftTX = rightTX;
			rightTX = temp;
			
			GL11.glBegin(GL11.GL_QUAD_STRIP);
			for (float i = 0; i < segments; i++) {
				
				//float phaseAngleRad = (float) Math.toRadians(phaseAngle + segIndex * 10) + (segIndex * anglePerSegment * 10f);
				float phaseAngleRad;
				float angleIncrementBase = anglePerSegment;
				//angleIncrementBase = currentWidth / segments * 0.00075f;
				if (iter == 0) {
					phaseAngleRad = (float) Math.toRadians(phaseAngle) + (i * angleIncrementBase * 10f);
				} else { //if (iter == 1) {
					if (iter == 2) {
						phaseAngleRad = (float) Math.toRadians(-phaseAngle) + (i * angleIncrementBase * 15f);
					} else if (iter == 3) {
						phaseAngleRad = (float) Math.toRadians(phaseAngle) + (i * angleIncrementBase * 5f);
					} else {
						phaseAngleRad = (float) Math.toRadians(-phaseAngle) + (i * angleIncrementBase * 5f);
					}
					//phaseAngleRad += (float) Math.PI;
				}
				
				
//				float angle = (float) Math.toDegrees(i * anglePerSegment);
//				if (iter == 1) angle += 180;
				
				float pulseSin = (float) Math.sin(phaseAngleRad * periodMult * 1f);
				float pulseAmount = pulseSin * thickness * 0.25f;
				float pulseInner = pulseAmount * 0.5f;
				pulseInner *= 1f;
				
//				pulseInner = 0f;
//				pulseAmount = 0f;
				//pulseInner *= Math.max(0, pulseSin - 0.5f);
				//pulseInner *= 0f;
				
				float r = radius;

				float thicknessMult = 1.25f;
				float thicknessFlat = 0f;
				
				float theta = startRad + turnDir * anglePerSegment * i;
				
				float cos = (float) Math.cos(theta);
				float sin = (float) Math.sin(theta);
				float x1 = cos * (r - pulseInner);
				float y1 = sin * (r - pulseInner);
				float x2 = cos * (r + thickness * thicknessMult - pulseAmount + thicknessFlat);
				float y2 = sin * (r + thickness * thicknessMult - pulseAmount + thicknessFlat);
				
				if (iter == 3) {
					x1 = cos * (r - pulseInner - thickness * 1f);
					y1 = sin * (r - pulseInner - thickness * 1f);
					x2 = cos * (r + thickness * thicknessMult - pulseAmount + thicknessFlat - thickness * 0.0f);
					y2 = sin * (r + thickness * thicknessMult - pulseAmount + thicknessFlat - thickness * 0.0f);
				}
				
				x1 += xOff;
				x2 += xOff;
				y1 += yOff;
				y2 += yOff;
				
//				x2 += (float) (Math.cos(phaseAngleRad) * getPixelsPerSegment() * 0.33f);
//				y2 += (float) (Math.sin(phaseAngleRad) * getPixelsPerSegment() * 0.33f);
				
				//Color color = Color.white;
				//Color color = new Color(255,160,75);
				Color color = Color.white;
				float alpha = alphaMult;
				
				float distFromEdge;
				if (i < segments / 2f) {
					distFromEdge = i * currentWidth / segments; 
				} else {
					distFromEdge = (segments - i - 1f) * currentWidth / segments;
				}
				alpha *= Math.min(1f, distFromEdge / fadeDist);
				
				GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)((float) color.getAlpha() * alpha));
				
				GL11.glTexCoord2f(leftTX, texProgress);
				GL11.glVertex2f(x1, y1);
				GL11.glTexCoord2f(rightTX, texProgress);
				GL11.glVertex2f(x2, y2);
				
				texProgress += texPerSegment;
			}
			GL11.glEnd();
			//GL11.glRotatef(180, 0, 0, 1);
		}
		GL11.glPopMatrix();
		
	}
	
	@Override
	public float getRenderRange() {
		return (currentThickness + currentWidth) * 0.5f + 200f;
	}
	
	protected void updateArcOfCurrWidth() {
		Vector2f perp = Misc.getPerp(velDir);
		
		p1.set(perp);
		p2.set(perp).negate();
		
		p1.scale(currentWidth * 0.5f);
		p2.scale(currentWidth * 0.5f);
		
		Vector2f.add(p1, entity.getLocation(), p1);
		Vector2f.add(p2, entity.getLocation(), p2);
		
		float angle1 = Misc.getAngleInDegrees(arcOrigin, p1);
		float angle2 = Misc.getAngleInDegrees(arcOrigin, p2);
		
		float diff = Misc.getAngleDiff(angle1, angle2);
		currentArc = diff;
	}
	
	protected float getFadeDist() {
		float fadeDist = Math.max(300, currentWidth / numSegments * 4f);
		if (fadeDist > currentWidth / 3f) fadeDist = currentWidth / 3f;
		return fadeDist;
	}
	
	@Override
	public boolean containsPoint(Vector2f point, float radius) {
		if (!Misc.isInArc(params.angle, currentArc, arcOrigin, point)) {
			return false;
		}
		
		float dist = Misc.getDistance(point, arcOrigin);
		
		float max = params.arcOriginRange + distanceTravelled;
		float min = max - currentThickness; 
		
		return dist >= min - radius && dist <= max + radius;
	}
	
	@Override
	public void applyEffect(SectorEntityToken entity, float days) {
		if (entity instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) entity;

			float intensity = getIntensityAtPoint(fleet.getLocation());
			if (intensity <= 0) return;
			
			// "wind" effect - adjust velocity
			
			fleet.getStats().removeTemporaryMod(getModId());
			
			float maxFleetBurn = fleet.getFleetData().getBurnLevel();
			float currFleetBurn = fleet.getCurrBurnLevel();
			
			float maxWindBurn = params.burnLevel;
			
			float currWindBurn = intensity * maxWindBurn;
			float burnDiff = maxFleetBurn - currWindBurn;
			float maxFleetBurnIntoWind = 0;
			if (burnDiff >= 5.9f) {
				maxFleetBurnIntoWind = -params.burnLevel + 4f;
			} else if (burnDiff >= 3.9f) {
				maxFleetBurnIntoWind = -params.burnLevel + 3f;
			} else if (burnDiff >= 2.9f) {
				maxFleetBurnIntoWind = -params.burnLevel + 2f;
			} else if (burnDiff >= 0.9f) {
				maxFleetBurnIntoWind = -params.burnLevel + 1f;
			} else {
				maxFleetBurnIntoWind = -params.burnLevel + 1f;
			}
			
			float angle = getForceDirAtPoint(fleet.getLocation());
			
			Vector2f windDir = Misc.getUnitVectorAtDegreeAngle(angle);
			Vector2f velDir = Misc.normalise(new Vector2f(fleet.getVelocity()));
			float dot = Vector2f.dot(windDir, velDir);
			
			velDir.scale(currFleetBurn);
			float fleetBurnAgainstWind = -1f * Vector2f.dot(windDir, velDir);
			
			float burnBonus = (currWindBurn - maxFleetBurn) * dot;
			if (dot > 0) {
				burnBonus = Math.round(burnBonus);
				if (burnBonus < 1) burnBonus = 1;
				fleet.getStats().addTemporaryModFlat(0.1f, getModId(), "In wavefront", burnBonus, fleet.getStats().getFleetwideMaxBurnMod());
			}
			
			float accelMult = 0.5f;
			if (fleetBurnAgainstWind < maxFleetBurnIntoWind) {
				//accelMult = 0.5f + 0.25f * Math.abs(fleetBurnAgainstWind - maxFleetBurnIntoWind);
				accelMult = 0.5f;
				//accelMult = 0f;
			} else {
				float diff = Math.abs(fleetBurnAgainstWind - maxFleetBurnIntoWind);
				accelMult = 2f + diff * 0.25f;
			}
			
			float maxSpeed = fleet.getTravelSpeed();
			float baseAccel = Math.max(10f, maxSpeed * fleet.getStats().getAccelerationMult().getBaseValue());
			
			float seconds = days * Global.getSector().getClock().getSecondsPerDay();
			
			Vector2f vel = fleet.getVelocity();
			windDir.scale(seconds * baseAccel * accelMult);
			fleet.setVelocity(vel.x + windDir.x, vel.y + windDir.y);
			
			Color glowColor = new Color(100,200,255,75);
			int alpha = glowColor.getAlpha();
			if (alpha < 75) {
				glowColor = Misc.setAlpha(glowColor, 75);
			}
			
			// visual effects - glow, tail
			float durIn = 1f;
			float durOut = 2f;
			Misc.normalise(windDir);
			float sizeNormal = 5f + 10f * intensity;
			for (FleetMemberViewAPI view : fleet.getViews()) {
				view.getWindEffectDirX().shift(getModId(), windDir.x * sizeNormal, durIn, durOut, 1f);
				view.getWindEffectDirY().shift(getModId(), windDir.y * sizeNormal, durIn, durOut, 1f);
				//view.getWindEffectColor().shift(getModId(), glowColor, durIn, durOut, intensity);
				view.getWindEffectColor().shift(getModId(), glowColor, durIn, durOut, intensity);
			}
		}
		
	}
	
	
	
	public float getForceDirAtPoint(Vector2f point) {
		float angle1 = Misc.getAngleInDegrees(arcOrigin, p1);
		float angle2 = Misc.getAngleInDegrees(arcOrigin, p2);
		
		float angle3 = Misc.getAngleInDegrees(arcOrigin, point);
		
		float widthPerDegree = currentWidth / Misc.getAngleDiff(angle1, angle2);
		
		float dist1 = Misc.getAngleDiff(angle1, angle3) * widthPerDegree;
		float dist2 = Misc.getAngleDiff(angle2, angle3) * widthPerDegree;
		
		float fadeDist = getFadeDist();
		
		float turnDir = 0f;
		float brightness = 0f;
		if (dist1 < fadeDist && dist1 <= dist2) {
			brightness = dist1 / fadeDist;
			turnDir = Misc.getClosestTurnDirection(angle3, arcOrigin, p1);
		}
		
		if (dist2 < fadeDist && dist2 <= dist1) {
			brightness = dist2 / fadeDist;
			turnDir = Misc.getClosestTurnDirection(angle3, arcOrigin, p2);
		}
		
		return angle3 + (1f - brightness) * 45f * turnDir;
	}
	
	public float getIntensityAtPoint(Vector2f point) {
		float angle1 = Misc.getAngleInDegrees(arcOrigin, p1);
		float angle2 = Misc.getAngleInDegrees(arcOrigin, p2);
		
		float angle3 = Misc.getAngleInDegrees(arcOrigin, point);
		
		if (!Misc.isBetween(angle1, angle2, angle3)) return 0f;
		
		float widthPerDegree = currentWidth / Misc.getAngleDiff(angle1, angle2);
		
		float dist1 = Misc.getAngleDiff(angle1, angle3) * widthPerDegree;
		float dist2 = Misc.getAngleDiff(angle2, angle3) * widthPerDegree;
		
		float fadeDist = getFadeDist();
		
		float sideMult = 1f;
		if (dist1 < fadeDist && dist1 <= dist2) {
			sideMult = dist1 / fadeDist;
		}
		if (dist2 < fadeDist && dist2 <= dist1) {
			sideMult = dist2 / fadeDist;
		}
		
		float distToPoint = Misc.getDistance(arcOrigin, point);
		float max = params.arcOriginRange + distanceTravelled;
		float min = max - currentThickness;
		
		max = max - min;
		distToPoint -= min;
		min = 0;
		float f = distToPoint / max;
		
		if (f > 0.75f) {
			f = (1f - f) / .25f;
		} else if (f < 0.25f) {
			f = f / 0.25f;
		} else {
			f = 1f;
		}
		
		return sideMult * f * fader.getBrightness();
	}
	
	@Override
	public Color getNameColor() {
		Color bad = Misc.getNegativeHighlightColor();
		Color base = super.getNameColor();
		return Misc.interpolateColor(base, bad, Global.getSector().getCampaignUI().getSharedFader().getBrightness() * 1f);
	}

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
		
		tooltip.addTitle("Wavefront");
		tooltip.addPara(Global.getSettings().getDescription(getTerrainId(), Type.TERRAIN).getText1(), pad);
		
		float nextPad = pad;
		if (expanded) {
			tooltip.addSectionHeading("Travel", Alignment.MID, pad);
			nextPad = small;
		}
		tooltip.addPara("Reduces the combat readiness of " +
				"all ships in the corona at a steady pace.", nextPad);
		tooltip.addPara("The heavy solar wind also makes the star difficult to approach.", pad);
		tooltip.addPara("Occasional solar flare activity takes these effects to even more dangerous levels.", pad);
		
		if (expanded) {
			tooltip.addSectionHeading("Combat", Alignment.MID, pad);
			tooltip.addPara("Reduces the peak performance time of ships and increases the rate of combat readiness degradation in protracted engagements.", small);
		}
		
		//tooltip.addPara("Does not stack with other similar terrain effects.", pad);
	}
	
	public boolean isTooltipExpandable() {
		return true;
	}
	
	public float getTooltipWidth() {
		return 350f;
	}
	
	public String getTerrainName() {
		return "Wavefront";
	}
	
	public String getNameForTooltip() {
		return getTerrainName();
	}
	
	public String getEffectCategory() {
		return null;
	}

	
	public float getMaxEffectRadius(Vector2f locFrom) {
		return getRenderRange();
	}
	public float getMinEffectRadius(Vector2f locFrom) {
		return 0f;
	}
	
	public float getOptimalEffectRadius(Vector2f locFrom) {
		return getMaxEffectRadius(locFrom);
	}
	
	public boolean hasMapIcon() {
		return false;
	}
	
	public boolean canPlayerHoldStationIn() {
		return false;
	}
}





