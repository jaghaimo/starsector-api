package com.fs.starfarer.api.impl.campaign.velfield;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class SlipstreamEntityPlugin extends BaseCustomEntityPlugin {
	
	public static float MAX_PARTICLES_ADD_PER_FRAME = 100;
	
	public static float RAD_PER_DEG = 0.01745329251f;
	public static Vector2f rotateAroundOrigin(Vector2f v, float cos, float sin) {
		Vector2f r = new Vector2f();
		r.x = v.x * cos - v.y * sin;
		r.y = v.x * sin + v.y * cos;
		return r;
	}
	
	public static class SlipstreamParticle {
		Vector2f loc = new Vector2f();
		Vector2f vel = new Vector2f();
		Color color;
		float remaining;
		float elapsed;
	}
	
	public static class SlipstreamParams {
		public String spriteKey1 = "slipstream1";
		public Color spriteColor1 = new Color(0.3f, 0.5f, 1f, 0.67f);
		public float width;
		public float length;
		public int numParticles;
		public float minSpeed;
		public float maxSpeed;
		public float maxSpeedForTex;
		public int burnLevel = 30;
		public Color minColor;
		public Color maxColor;
		public float minDur = 0f;
		public float maxDur = 4f;
		public float lineLengthFractionOfSpeed = 0.5f;
	}
	
	protected SlipstreamParams params = new SlipstreamParams();
	
	protected float texelsPerPixel = 1f;
	protected transient List<SlipstreamParticle> particles = new ArrayList<SlipstreamParticle>();
	
	public SlipstreamEntityPlugin() {
	}
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		this.params = (SlipstreamParams) pluginParams;
		fader.fadeIn();
		texProgress1 = (float) Math.random();
		texProgress2 = (float) Math.random();
		texProgress3 = (float) Math.random();
		readResolve();
	}
	
	public float getRenderRange() {
		return Math.max(params.width, params.length) * 1.5f;
	}

	Object readResolve() {
		if (particles == null) {
			particles = new ArrayList<SlipstreamParticle>();
		}
		return this;
	}
	
	public void advance(float amount) {
		if (!entity.isInCurrentLocation()) return;
		
		applyEffectToFleets(amount);
		
		fader.advance(amount);

		
		entity.getLocation().x += Misc.getSpeedForBurnLevel(params.burnLevel) * amount;
		entity.setFacing(0f);
//		entity.getLocation().set(Global.getSector().getPlayerFleet().getLocation().x + 500, 
//				Global.getSector().getPlayerFleet().getLocation().y + 1000f);
//		entity.getLocation().set(Global.getSector().getPlayerFleet().getLocation());
		
		params.minColor = new Color(0.5f, 0.3f, 0.75f, 0.85f);
		params.maxColor = new Color(0.5f, 0.6f, 1f, 1f);
		params.spriteColor1 = new Color(0.3f, 0.5f, 1f, 0.67f);
		params.minDur = 1f;
		params.maxDur = 4f;
		params.minSpeed = 700f;
		params.maxSpeed = 1500f;
		params.minSpeed = Misc.getSpeedForBurnLevel(25f);
		params.maxSpeed = Misc.getSpeedForBurnLevel(35f);
		params.maxSpeedForTex = Misc.getSpeedForBurnLevel(15f);
		params.lineLengthFractionOfSpeed = 0.25f;
		//params.lineLengthFractionOfSpeed = 0.5f;
		//params.lineLengthFractionOfSpeed = 1f;
		//params.lineLengthFractionOfSpeed = 0.15f;
		params.burnLevel = 30;
		params.numParticles = 1000;
		params.width = 512f;
		params.length = 10000f;
		params.minColor = new Color(0.5f, 0.3f, 0.75f, 0.1f);
		params.maxColor = new Color(0.5f, 0.6f, 1f, 0.5f);
		params.maxDur = 6f;
//		params.minSpeed = Misc.getSpeedForBurnLevel(16f);
//		params.maxSpeed = Misc.getSpeedForBurnLevel(16f);
//		params.numParticles = 0;
		//params.spriteKey1 = "graphics/fx/beam_weave_core.png";
		
		SpriteAPI sprite = Global.getSettings().getSprite("misc", params.spriteKey1);
		texelsPerPixel = sprite.getHeight() / params.width;
		
		
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(entity.getFacing());
		float cos = dir.x;
		float sin = dir.y;
//		cos = (float) Math.cos(entity.getFacing() * RAD_PER_DEG);
//		sin = (float) Math.sin(entity.getFacing() * RAD_PER_DEG);
		
		float x = entity.getLocation().x;
		float y = entity.getLocation().y;
		
		int added = 0;
		//MAX_PARTICLES_ADD_PER_FRAME = 2000;
		while (particles.size() < params.numParticles && added < MAX_PARTICLES_ADD_PER_FRAME) {
			added++;
			
			SlipstreamParticle p = new SlipstreamParticle();
			float fLength = (float) Math.random() * 0.8f;
			float sign = Math.signum((float) Math.random() - 0.5f);
			//fWidth = 0.5f + sign * fWidth * fWidth;
			float r = (float) Math.random() * 0.7f;
			float fWidth = 0.5f + sign * (1f - (float)Math.sqrt(r)) * fLength * 0.5f;
			//fWidth = r;
			fWidth = 0.5f + sign * r * (0.5f + 0.5f * fLength) * 0.5f;
			fWidth = (float) Math.random() * 0.8f + 0.1f;
			//fLength *= fLength * fLength * fLength;
			//fLength *= fLength;
			float speed = params.minSpeed + (params.maxSpeed - params.minSpeed) * (float) Math.random();
			//speed *= 0.5f;
			float dur = params.minDur + (params.maxDur - params.minDur) * (float) Math.random(); 
			//float minDistFromSource = speed * dur;
			float minDistFromSource = speed * dur * 0.25f;
			p.loc.set(-fLength * (params.length - minDistFromSource) - minDistFromSource,
					   fWidth * params.width - params.width / 2f);
			p.loc = rotateAroundOrigin(p.loc, cos, sin);
			p.loc.x += x;
			p.loc.y += y;
			
			
			float angleToEntity = Misc.getAngleInDegrees(p.loc, entity.getLocation());
			float turnDir = Misc.getClosestTurnDirection(entity.getFacing(), angleToEntity);
			float diff = Math.min(30f, Misc.getAngleDiff(angleToEntity, entity.getFacing()) * 0.5f);
			diff = 0f;
			//p.vel.set(dir);
			p.vel.set(Misc.getUnitVectorAtDegreeAngle(entity.getFacing() + turnDir * diff));
			p.vel.scale(speed);

			p.remaining = dur;
			p.color = getRandomColor();
			
			particles.add(p);
		}
		
		Iterator<SlipstreamParticle> iter = particles.iterator();
		while (iter.hasNext()) {
			SlipstreamParticle p = iter.next();
			p.remaining -= amount;
			p.elapsed += amount;
			if (p.remaining <= 0) {
				iter.remove();
				continue;
			}
			
			boolean shouldFadeOut = false;
			Vector2f toEntity = Vector2f.sub(entity.getLocation(), p.loc, new Vector2f());
			shouldFadeOut = Vector2f.dot(toEntity, p.vel) < 0f;
			if (shouldFadeOut) {
				if (p.elapsed > 1f) {
					p.remaining = Math.min(p.remaining, 0.5f);
				}
			}
			
			p.loc.x += p.vel.x * amount;
			p.loc.y += p.vel.y * amount;
		}
		
//		float texSpeed = params.maxSpeedForTex;
//		texSpeed = 100f;
		float texSpeed = Misc.getSpeedForBurnLevel(params.burnLevel);
//		texSpeed = 100f;
		//texSpeed = Misc.getSpeedForBurnLevel(7);
		
		float unitsPerOneTexIter = sprite.getWidth();
		float texUnitsPerSecondForSpeed = texSpeed / unitsPerOneTexIter * texelsPerPixel;
		texProgress1 -= texUnitsPerSecondForSpeed * amount;
		texProgress2 += texUnitsPerSecondForSpeed * amount * 1.9f;
		texProgress3 += texUnitsPerSecondForSpeed * amount * 0.7f;
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		
		SpriteAPI sprite = Global.getSettings().getSprite("misc", params.spriteKey1);
		//sprite.setAdditiveBlend();
		sprite.setNormalBlend();
		sprite.setColor(params.spriteColor1);
		//sprite.setColor(Misc.setAlpha(params.spriteColor1, 255));
		//sprite.setColor(Color.blue);
		renderLayer(sprite, texProgress1, viewport.getAlphaMult());
		//sprite.setColor(Color.red);
		//renderLayer(sprite, texProgress2, viewport.getAlphaMult());
		//sprite.setColor(Color.green);
		//renderLayer(sprite, texProgress3, viewport.getAlphaMult());
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		
		float zoom = Global.getSector().getViewport().getViewMult(); 

		//GL11.glLineWidth(2f);
		//GL11.glLineWidth(Math.max(1f, 2f/zoom));
		GL11.glLineWidth(Math.max(1f, Math.min(2f, 2f/zoom)));
		//GL11.glLineWidth(1.5f);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glBegin(GL11.GL_LINES);
		for (SlipstreamParticle p : particles) {
			//if (true) break;
			if (!viewport.isNearViewport(p.loc, 500)) continue;
			float a = viewport.getAlphaMult();
			if (p.remaining <= 0.5f) {
				a = p.remaining / 0.5f;
			} else if (p.elapsed < 1f) {
				a = p.elapsed / 1f;
			}
			
			//a *= 0.5f;
			//a *= 0.1f;
			
			Vector2f start = new Vector2f(p.loc);
			Vector2f end = new Vector2f(p.loc);
			start.x += p.vel.x * params.lineLengthFractionOfSpeed * 0.1f;
			start.y += p.vel.y * params.lineLengthFractionOfSpeed * 0.1f;
			end.x -= p.vel.x * params.lineLengthFractionOfSpeed * 0.9f;
			end.y -= p.vel.y * params.lineLengthFractionOfSpeed * 0.9f;
			
			Misc.setColor(p.color, 0f);
			GL11.glVertex2f(start.x, start.y);
			Misc.setColor(p.color, a);
			GL11.glVertex2f(p.loc.x, p.loc.y);
			GL11.glVertex2f(p.loc.x, p.loc.y);
			Misc.setColor(p.color, 0f);
			GL11.glVertex2f(end.x, end.y);
			
//			a *= 0.5f;
//			float spacing = 0.67f;
//			Vector2f perp = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(p.vel) + 90f);
//			perp.scale(spacing);
//			Misc.setColor(p.color, 0f);
//			GL11.glVertex2f(start.x + perp.x, start.y + perp.y);
//			Misc.setColor(p.color, a);
//			GL11.glVertex2f(p.loc.x + perp.x, p.loc.y + perp.y);
//			GL11.glVertex2f(p.loc.x + perp.x, p.loc.y + perp.y);
//			Misc.setColor(p.color, 0f);
//			GL11.glVertex2f(end.x + perp.x, end.y + perp.y);
//			
//			perp.negate();
//			Misc.setColor(p.color, 0f);
//			GL11.glVertex2f(start.x + perp.x, start.y + perp.y);
//			Misc.setColor(p.color, a);
//			GL11.glVertex2f(p.loc.x + perp.x, p.loc.y + perp.y);
//			GL11.glVertex2f(p.loc.x + perp.x, p.loc.y + perp.y);
//			Misc.setColor(p.color, 0f);
//			GL11.glVertex2f(end.x + perp.x, end.y + perp.y);
			
		}
		GL11.glEnd();
	}
	
	
	protected float texProgress1 = 0f;
	protected float texProgress2 = 0f;
	protected float texProgress3 = 0f;
	protected FaderUtil fader = new FaderUtil(0f, 0.5f, 0.5f);
	
	public void renderLayer(SpriteAPI sprite, float texProgress, float alpha) {
		Vector2f from = new Vector2f(entity.getLocation());
		Vector2f to = Misc.getUnitVectorAtDegreeAngle(entity.getFacing());
		to.scale(-params.length);
		Vector2f.add(to, from, to);
		
		float length1 = 500f;
		length1 = Math.min(length1, Misc.getDistance(from, to));
		length1 = Misc.getDistance(from, to);
		float length2 = params.width / 2f;
		if (length1 < length2) length1 = length2;
		length1 += length2;
		
		//length1 = params.width;
		//length1 = params.length * 0.05f;
		length1 = params.length * 0.15f;
		length2 = params.length - length1;
		
		float w1 = length2 * 0.3f;
		float w2 = length2 * 1.5f;
		float w3 = length2 * 2.5f;
		float wMult = 0.33f;
		wMult = 1f;
		w1 *= wMult;
		w2 *= wMult;
		w3 *= wMult;
		//w1 = w2 = 400;
		
		w1 = w2 = w3 = params.width;
		
		float widthMult = 0.3f;
		w1 = params.width * widthMult;
		w2 = params.width * (1f - (1f - widthMult) * (length2 / (length1 + length2)));
		w3 = params.width * 1f;
		
		w1 = w2 = w3 = params.width;
		
		float angle = entity.getFacing() + 180f;
		
		Vector2f dest = new Vector2f(to);
		Vector2f src = new Vector2f(from);
		
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
		Vector2f dest1 = new Vector2f(dir);
		dest1.scale(length1);
		Vector2f.add(dest1, src, dest1);
		Vector2f dest2 = new Vector2f(dir);
		dest2.scale(length1 + length2);
		Vector2f.add(dest2, src, dest2);
		
		
		Vector2f perp = Misc.getUnitVectorAtDegreeAngle(angle + 90);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL11.glDisable(GL11.GL_TEXTURE_2D);
		sprite.bindTexture();
		GL11.glEnable(GL11.GL_BLEND);
		//GL11.glDisable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		
		Color color = sprite.getColor();
		
		boolean wireframe = false;
		//wireframe = true;
		if (wireframe) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			//GL11.glDisable(GL11.GL_BLEND);
		}
		
//		float texScale = sprite.getWidth() / (length1 + length2);
//		float tx1 = length1 / (length1 + length2) * texScale;
		float tx1 = length1 / sprite.getWidth() * texelsPerPixel;

//		alpha *= Math.sqrt(fader.getBrightness());
//		alpha *= 0.5f;
//		alpha *= (0.5f + Math.min(0.5f, 0.5f * w2 / 360f));
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		Misc.setColor(color, alpha * 1f);
		GL11.glTexCoord2f(tx1 * 0.5f + texProgress, 0.5f);
		GL11.glVertex2f((src.x + dest1.x)/2f, (src.y + dest1.y)/2f);
		
		Misc.setColor(color, alpha * 0f);
		GL11.glTexCoord2f(0f + texProgress, 0f);
		GL11.glVertex2f(src.x + perp.x * w1/2f, src.y + perp.y * w1/2f);
		GL11.glTexCoord2f(0f + texProgress, 1f);
		GL11.glVertex2f(src.x - perp.x * w1/2f, src.y - perp.y * w1/2f);
		
		Misc.setColor(color, alpha * 1f);
		GL11.glTexCoord2f(tx1 + texProgress, 1f);
		GL11.glVertex2f(dest1.x - perp.x * w2/2f, dest1.y - perp.y * w2/2f);
		GL11.glTexCoord2f(tx1 + texProgress, 0f);
		GL11.glVertex2f(dest1.x + perp.x * w2/2f, dest1.y + perp.y * w2/2f);
		
		Misc.setColor(color, alpha * 0f);
		GL11.glTexCoord2f(0f + texProgress, 0f);
		GL11.glVertex2f(src.x + perp.x * w1/2f, src.y + perp.y * w1/2f);
		GL11.glEnd();
		
		//float th = length2 / length1;
		float th = tx1 * length2 / length1;
		//th *= texScale;
		//th = 0.5f;
		
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		Misc.setColor(color, alpha * 1f);
		GL11.glTexCoord2f(tx1 + texProgress + th * 0.5f, 0.5f);
		GL11.glVertex2f((dest1.x + dest2.x)/2f, (dest1.y + dest2.y)/2f);
		
		Misc.setColor(color, alpha * 1f);
		GL11.glTexCoord2f(tx1 + texProgress, 0f);
		GL11.glVertex2f(dest1.x + perp.x * w2/2f, dest1.y + perp.y * w2/2f);
		GL11.glTexCoord2f(tx1 + texProgress, 1f);
		GL11.glVertex2f(dest1.x - perp.x * w2/2f, dest1.y - perp.y * w2/2f);
		
		Misc.setColor(color, alpha * 0f);
		GL11.glTexCoord2f(tx1 + texProgress + th, 1f);
		GL11.glVertex2f(dest2.x - perp.x * w3/2f, dest2.y - perp.y * w3/2f);
		GL11.glTexCoord2f(tx1 + texProgress + th, 0f);
		GL11.glVertex2f(dest2.x + perp.x * w3/2f, dest2.y + perp.y * w3/2f);
		
		Misc.setColor(color, alpha * 1f);
		GL11.glTexCoord2f(tx1 + texProgress, 0f);
		GL11.glVertex2f(dest1.x + perp.x * w2/2f, dest1.y + perp.y * w2/2f);
		GL11.glEnd();
		
		if (wireframe) GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}

	
	
	
	
	public Color getRandomColor() {
		return Misc.interpolateColor(params.minColor, params.maxColor, (float) Math.random());
	}
	
	
	/**
	 * result[0] = along the length of the field, 0 = at start, 1 = at tail 
	 * result[1] = along the width of the field, 0 = on center, 1 = on edge, no directional information
	 * null if outside stream
	 * Assumes rectangular, non-tapered stream
	 * @param loc
	 * @return
	 */
	public float [] getLengthAndWidthFractionWithinStream(Vector2f loc) {
		float dist = Misc.getDistance(loc, entity.getLocation());
		if (dist > getRenderRange()) return null;
		
		Vector2f p3 = new Vector2f(loc);
		Vector2f p1 = new Vector2f(entity.getLocation());
		Vector2f p2 = Misc.getUnitVectorAtDegreeAngle(entity.getFacing() + 180f);
		p2.scale(params.length);
		Vector2f.add(p2, p1, p2);
		
		float u = (p3.x - p1.x) * (p2.x - p1.x) + (p3.y - p1.y) * (p2.y - p1.y);
		float denom = Vector2f.sub(p2, p1, new Vector2f()).length();
		denom *= denom;
		if (denom == 0) return null;
		u /= denom;
		
		if (u >= 0 && u <= 1) { // intersection is between p1 and p2
			Vector2f intersect = new Vector2f();
			intersect.x = p1.x + u * (p2.x - p1.x);
			intersect.y = p1.y + u * (p2.y - p1.y);
			float distFromLine = Vector2f.sub(intersect, p3, new Vector2f()).length();
			//float distAlongLine = u * params.length;
			if (distFromLine >= params.width/2f) return null;
			
			float [] result = new float[2];
			result[0] = u;
			result[1] = distFromLine / (params.width / 2f);
			return result;
		}
		return null;
	}
	
	public void applyEffectToFleets(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		for (CampaignFleetAPI fleet : entity.getContainingLocation().getFleets()) {
			applyEffect(fleet, days);
		}
	}
	
	protected boolean playerWasInSlipstream = false;
	public void applyEffect(SectorEntityToken other, float days) {
		if (other instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) other;
			
//			if (fleet.isPlayerFleet()) {
//				System.out.println("efwefwef");
//			}
			
			float [] offset = getLengthAndWidthFractionWithinStream(fleet.getLocation());
			if (offset == null) {
				if (fleet.isPlayerFleet()) {
					playerWasInSlipstream = false;
				}
				return;
			}
			
			float burnBonus = fleet.getFleetData().getBurnLevel() - fleet.getFleetData().getMinBurnLevelUnmodified();
			if (burnBonus < 0) burnBonus = 0;
			float maxSpeedWithWind = Misc.getSpeedForBurnLevel(params.burnLevel + burnBonus + 1.2f);
			if (fleet.getVelocity().length() >= maxSpeedWithWind) {
				return;
			}
			
			float fL = offset[0];
			float fW = offset[1];
			
			float intensity = 1f;
			if (fL > 0.75f) {
				intensity = (1f - fL) / 0.25f;
			} else if (fL < 0.5f) {
				intensity = fL / 0.5f;
			}
			if (fW > 0.5f) {
				intensity *= (1f - fW) / 0.5f;
			}
			//intensity *= intensity;
			
			if (intensity <= 0) {
				if (fleet.isPlayerFleet()) {
					playerWasInSlipstream = false;
				}
				return;
			}
			
			if (fleet.isPlayerFleet()) {
				if (!playerWasInSlipstream) {
					playerWasInSlipstream = true;
					fleet.addFloatingText("Entering slipstream", Misc.setAlpha(fleet.getIndicatorColor(), 255), 0.5f);
				}
			}
			
			//System.out.println("Intensity: " + intensity);

			// "wind" effect - adjust velocity
			float maxFleetBurn = fleet.getFleetData().getBurnLevel();
			float currFleetBurn = fleet.getCurrBurnLevel();
			
			float maxWindBurn = params.burnLevel * 2f;
			
			float currWindBurn = intensity * maxWindBurn;
			float maxFleetBurnIntoWind = maxFleetBurn - Math.abs(currWindBurn);
			float seconds = days * Global.getSector().getClock().getSecondsPerDay();
			
//			float angle = Misc.getAngleInDegreesStrict(this.entity.getLocation(), fleet.getLocation()) + 180f;
//			Vector2f windDir = Misc.getUnitVectorAtDegreeAngle(angle);
			Vector2f windDir = Misc.getUnitVectorAtDegreeAngle(entity.getFacing());
			if (currWindBurn < 0) {
				windDir.negate();
			}
			Vector2f velDir = Misc.normalise(new Vector2f(fleet.getVelocity()));
			velDir.scale(currFleetBurn);
			
			float fleetBurnAgainstWind = -1f * Vector2f.dot(windDir, velDir);
			
			float accelMult = 0.5f;
//			if (fleetBurnAgainstWind > maxFleetBurnIntoWind) {
//				accelMult += 0.75f + 0.25f * (fleetBurnAgainstWind - maxFleetBurnIntoWind);
//			}
			accelMult *= 2f;
			
			
			
			float windSpeed = Misc.getSpeedForBurnLevel(currWindBurn);
			//float fleetSpeed = fleet.getTravelSpeed();
			Vector2f windVector = new Vector2f(windDir);
			windVector.scale(windSpeed);
			
			Vector2f vel = fleet.getVelocity();
			Vector2f diff = Vector2f.sub(windVector, vel, new Vector2f());
			//windDir.scale(seconds * fleet.getAcceleration());
			float max = diff.length();
			diff = Misc.normalise(diff);
			//diff.scale(Math.max(windSpeed * seconds, fleet.getAcceleration() * 1f * seconds));
			diff.scale(fleet.getAcceleration() * 3f * seconds);
			//diff.scale(fleet.getTravelSpeed() * 5f * seconds);
			//diff.scale(accelMult);
			if (diff.length() > max) {
				diff.scale(max / diff.length());
			}
			//System.out.println("Applying diff: " + diff);
			//fleet.setVelocity(vel.x + diff.x, vel.y + diff.y);
			
			
//			Vector2f velDir = Misc.normalise(new Vector2f(fleet.getVelocity()));
//			velDir.scale(currFleetBurn);
//			
//			float fleetBurnAgainstWind = -1f * Vector2f.dot(windDir, velDir);
//			
			accelMult = 0.5f;
			if (fleetBurnAgainstWind > maxFleetBurnIntoWind) {
				accelMult += 0.75f + 0.25f * (fleetBurnAgainstWind - maxFleetBurnIntoWind);
			}
			
			//Vector2f vel = fleet.getVelocity();
			//windDir.scale(seconds * fleet.getAcceleration() * accelMult);
			windDir.scale(seconds * Math.max(fleet.getTravelSpeed(), fleet.getAcceleration()) * accelMult);
			fleet.setVelocity(vel.x + windDir.x, vel.y + windDir.y);
			
			
			Color glowColor = params.spriteColor1;
			int alpha = glowColor.getAlpha();
			if (alpha < 75) {
				glowColor = Misc.setAlpha(glowColor, 75);
			}
			// visual effects - glow, tail
			
			float fleetSpeedAlongWind = Vector2f.dot(windDir, fleet.getVelocity());
			float fleetSpeed = fleet.getVelocity().length();
			
			float matchingWindFraction = fleetSpeedAlongWind/windSpeed;
			float effectMag = 1f - matchingWindFraction * 0.5f;
			if (effectMag > 0f)  effectMag = 0f;
			if (effectMag < 0.5f) effectMag = 0.5f;
			
			String modId = "slipstream_" + entity.getId();
			float durIn = 1f;
			float durOut = 3f;
			Misc.normalise(windDir);
			float sizeNormal = 10f + 25f * intensity * effectMag;
			for (FleetMemberViewAPI view : fleet.getViews()) {
				view.getWindEffectDirX().shift(modId, windDir.x * sizeNormal, durIn, durOut, 1f);
				view.getWindEffectDirY().shift(modId, windDir.y * sizeNormal, durIn, durOut, 1f);
				view.getWindEffectColor().shift(modId, glowColor, durIn, durOut, intensity);
			}
		}
	}
}





