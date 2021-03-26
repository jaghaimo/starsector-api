package com.fs.starfarer.api.impl.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.BoundsAPI.SegmentAPI;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.FlickerUtilV2;
import com.fs.starfarer.api.util.Misc;

public class TriadShieldStatsBackup extends BaseShipSystemScript implements DamageTakenModifier {

	public static float SIDE_LENGTH = 16f;
	public static float INSIDE_ALPHA = 0.25f;
	
	public static class ShieldPieceConnection {
		public ShieldPiece from;
		public ShieldPiece to;
		public float baseLength = 0f;
		
		public ShieldPieceConnection(ShieldPiece from, ShieldPiece to) {
			this.from = from;
			this.to = to;
			baseLength = Misc.getDistance(from.offset, to.offset);
			baseLength *= 0.9f + (float) Math.random() * 0.2f;
		}

		public void advance(float amount) {
			Vector2f fLoc = from.getAdjustedOffset();
			Vector2f tLoc = to.getAdjustedOffset();
			float length = Misc.getDistance(fLoc, tLoc);
			float diff = length - baseLength;
			
			float k = 1f;
			float accel = diff * k;
			
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(fLoc, tLoc));
			
			dir.scale(accel * amount);
			Vector2f.add(from.vel, dir, from.vel);
			dir.negate();
			Vector2f.add(to.vel, dir, to.vel);
			
			
			float maxOff = 20f;
			from.off.x += from.vel.x * amount;
			from.off.y += from.vel.y * amount;
			if (from.off.length() > maxOff) from.off.scale(maxOff / from.off.length());
			
			to.off.x += to.vel.x * amount;
			to.off.y += to.vel.y * amount;
			if (to.off.length() > maxOff) to.off.scale(maxOff / to.off.length());
		}
	}
	
	public static class ShieldPiece {
		public ShipAPI ship;
		public Vector2f offset = new Vector2f();
		
		public Vector2f off = new Vector2f(); // secondary offset due to movement of individual triangles
		public Vector2f vel = new Vector2f();
		
		public SpriteAPI sprite;
		public boolean upsideDown = false;
		
		public float side;
		public Vector2f p1, p2, p3;
		public float baseAlphaMult = 1f;
		public float p1Alpha = 1f;
		public float p2Alpha = 1f;
		public float p3Alpha = 1f;
		
		public FaderUtil fader;
		public FlickerUtilV2 flicker;
		
		public ShieldPiece(ShipAPI ship, boolean upsideDown, float x, float y, float side) {
			this.ship = ship;
			this.side = side;
			offset.set(x, y);
			this.upsideDown = upsideDown;

			
			fader = new FaderUtil(0f, 0.25f, 0.25f);
			fader.setBrightness((float) Math.random() * 1f);
			fader.setBounce(true, true);
			fader.fadeIn();
			
			flicker = new FlickerUtilV2();
			
			//sprite = Global.getSettings().getSprite("misc", "fx_shield_piece");
			sprite = Global.getSettings().getSprite("graphics/hud/line8x8.png");
			//sprite = Global.getSettings().getSprite("graphics/hud/line32x32.png");
			
			// updside down means the flat side is on the left
			// p1 is always the lone point, p2->p3 the flat side on the left/right
			// triangles are arranged as if ship is pointed at a 0 degree angle, i.e. facing right
			float height = (float) (side * Math.sqrt(3f) / 2f);
			if (upsideDown) {
				p1 = new Vector2f(x + height/2f, y); 
				p2 = new Vector2f(x - height/2f - 1, y - side/2f);
				p3 = new Vector2f(x - height/2f - 1, y + side/2f);
			} else {
				p1 = new Vector2f(x - height/2f, y);
				p2 = new Vector2f(x + height/2f, y - side/2f);
				p3 = new Vector2f(x + height/2f, y + side/2f);
			}
			updatePointAlpha();
		}
		
		public void updatePointAlpha() {
			//if (true) return;
			BoundsAPI bounds = ship.getExactBounds();
			bounds.update(new Vector2f(0, 0), 0f);
			p1Alpha = getPointAlpha(p1);
			p2Alpha = getPointAlpha(p2);
			p3Alpha = getPointAlpha(p3);
			
			baseAlphaMult = Math.max(p1Alpha, p2Alpha);
			baseAlphaMult = Math.max(baseAlphaMult, p3Alpha);
//			if (baseAlphaMult > 0) {
//				p1Alpha = p2Alpha = p3Alpha = 1f;
//			}
		}
		public float getPointAlpha(Vector2f p) {
			BoundsAPI bounds = ship.getExactBounds();
			
			float minDist = Float.MAX_VALUE;
			List<Vector2f> boundsPoints = new ArrayList<Vector2f>();
			for (SegmentAPI segment : bounds.getSegments()) {
				Vector2f n = Misc.closestPointOnSegmentToPoint(segment.getP1(), segment.getP2(), p);
				float dist = Misc.getDistance(n, p);
				if (dist < minDist) minDist = dist;
				
				boundsPoints.add(segment.getP1());
			}
			boundsPoints.add(bounds.getSegments().get(bounds.getSegments().size() - 1).getP2());
			
			float minAlphaAt = SIDE_LENGTH * 1f;
			float minAlpha = 0f;
			boolean inBounds = Misc.isPointInBounds(p, boundsPoints);
			if (inBounds) {
				//if (true) return 1f;
//				minAlpha = INSIDE_ALPHA;
//				minAlpha = 0.25f;
				minAlphaAt = SIDE_LENGTH * 2f;
				minAlphaAt = 0f;
			}
			
			if (minDist > minAlphaAt) {
				return minAlpha;
			}
			
			
			return Math.max(minAlpha, 1f - Math.min(1f, minDist / (minAlphaAt * 2f)));
			
			//return Math.max(minAlpha, 1f - minDist / minAlphaAt);
		}
		
		public Vector2f getAdjustedOffset() {
			return Vector2f.add(offset, off, new Vector2f());
		}
		
		public Vector2f getCenter() {
			Vector2f result = new Vector2f(offset);
			Misc.rotateAroundOrigin(result, ship.getFacing());
			Vector2f.add(ship.getLocation(), result, result);
			return result;
		}
		
		public void advance(float amount) {
			fader.advance(amount * (0.5f + 0.5f * (float) Math.random()));
			//flicker.advance(amount);
		}
		
		/**
		 * Assumes translated to ship location and rotated, i.e. offset is the actual location to render at.
		 * @param alphaMult
		 */
		public void render(float alphaMult) {
			Color color = new Color(255, 165, 100, 255);
			color = new Color(100, 165, 255, 255);
			//color = new Color(0, 0, 255, 255);
			//color = Misc.scaleAlpha(color, 0.5f);
			
//			float size = 14f;
//			float x = offset.x;
//			float y = offset.y;
//			sprite.setSize(size, size);
//			sprite.setColor(color);
//			sprite.setAdditiveBlend();
//			sprite.setNormalBlend();
//			sprite.setAngle(-90);
//			if (upsideDown) {
//				sprite.setAngle(-90 + 180f);
//			}
//			sprite.setAlphaMult(alphaMult);
//			sprite.renderAtCenter(x, y);
			
			if (true) {
//				p1Alpha = p2Alpha = p3Alpha = 1f;
//				baseAlphaMult = 1f;
//				alphaMult = 1f;
				GL11.glPushMatrix();
				GL11.glTranslatef(off.x, off.y, 0f);
				
				for (int i = 0; i < 2; i++) {
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				sprite.bindTexture();
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//				if (i == 0) {
//					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//				}
				
//				float sin = (float) Math.sin(Math.toRadians(30f)); 
//				float cos = (float) Math.sin(Math.toRadians(30f)); 
				float t = 9f;
				float a = 0.25f;
				if (i == 1) {
					t = 4f;
					a = 1f;
				}
				
				//float in = (float) (Math.sin(Math.toRadians(30f)) * t);
				if (upsideDown) {
					GL11.glBegin(GL11.GL_QUAD_STRIP);
					
					Misc.setColor(color, alphaMult * p1Alpha * a);
					GL11.glTexCoord2f(0f, 0f);
					GL11.glVertex2f(p1.x, p1.y);
					GL11.glTexCoord2f(0f, 1f);
					GL11.glVertex2f(p1.x - t, p1.y);
					
					Misc.setColor(color, alphaMult * p2Alpha * a);
					GL11.glTexCoord2f(0f, 0f);
					GL11.glVertex2f(p2.x, p2.y);
					GL11.glTexCoord2f(0f, 1f);
					GL11.glVertex2f(p2.x + t * 0.5f, p2.y + t);
					
					Misc.setColor(color, alphaMult * p3Alpha * a);
					GL11.glTexCoord2f(0f, 0f);
					GL11.glVertex2f(p3.x, p3.y);
					GL11.glTexCoord2f(0f, 1f);
					GL11.glVertex2f(p3.x + t * 0.5f, p3.y - t);
					
					Misc.setColor(color, alphaMult * p1Alpha * a);
					GL11.glTexCoord2f(0f, 0f);
					GL11.glVertex2f(p1.x, p1.y);
					GL11.glTexCoord2f(0f, 1f);
					GL11.glVertex2f(p1.x - t, p1.y);
					
					GL11.glEnd();
				} else {
					GL11.glBegin(GL11.GL_QUAD_STRIP);
	
					Misc.setColor(color, alphaMult * p1Alpha * a);
					GL11.glTexCoord2f(0f, 0f);
					GL11.glVertex2f(p1.x, p1.y);
					GL11.glTexCoord2f(0f, 1f);
					GL11.glVertex2f(p1.x + t, p1.y);
					
					Misc.setColor(color, alphaMult * p2Alpha * a);
					GL11.glTexCoord2f(0f, 0f);
					GL11.glVertex2f(p2.x, p2.y);
					GL11.glTexCoord2f(0f, 1f);
					GL11.glVertex2f(p2.x - t * 0.5f, p2.y + t);
					
					Misc.setColor(color, alphaMult * p3Alpha * a);
					GL11.glTexCoord2f(0f, 0f);
					GL11.glVertex2f(p3.x, p3.y);
					GL11.glTexCoord2f(0f, 1f);
					GL11.glVertex2f(p3.x - t * 0.5f, p3.y - t);
					
					Misc.setColor(color, alphaMult * p1Alpha * a);
					GL11.glTexCoord2f(0f, 0f);
					GL11.glVertex2f(p1.x, p1.y);
					GL11.glTexCoord2f(0f, 1f);
					GL11.glVertex2f(p1.x + t, p1.y);
					
					GL11.glEnd();
				}
				}
				
				GL11.glPopMatrix();
				return;
			}
			
			
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
	
//			GL11.glColor4ub((byte)color.getRed(),
//							(byte)color.getGreen(),
//							(byte)color.getBlue(),
//							(byte)(color.getAlpha() * alphaMult));
			
			alphaMult *= baseAlphaMult;
			//alphaMult *= 0.67f + 0.33f * fader.getBrightness();
			
			
			GL11.glBegin(GL11.GL_TRIANGLES);
			{
				float i = 0f;
				float j = 0f;
//				alphaMult *= 0.2f;
//				float incr = 0.5f;
//				for (float i = -incr; i <= incr; i+=incr) {
//					for (float j = -incr; j <= incr; j+=incr) {
						Misc.setColor(color, alphaMult * p1Alpha);
						GL11.glVertex2f(p1.x + i, p1.y + j);
						Misc.setColor(color, alphaMult * p2Alpha);
						GL11.glVertex2f(p2.x + i, p2.y + j);
						Misc.setColor(color, alphaMult * p3Alpha);
						GL11.glVertex2f(p3.x + i, p3.y + j);
//					}
//				}
			}
			GL11.glEnd();
			
//			GL11.glBegin(GL11.GL_TRIANGLES);
//			{
//				Misc.setColor(color, alphaMult * p1Alpha);
//				GL11.glVertex2f(p1.x, p1.y);
//				Misc.setColor(color, alphaMult * p2Alpha);
//				GL11.glVertex2f(p2.x, p2.y);
//				Misc.setColor(color, alphaMult * p3Alpha);
//				GL11.glVertex2f(p3.x, p3.y);
//			}
//			GL11.glEnd();
			
//			GL11.glBegin(GL11.GL_QUADS);
//			{
//				GL11.glVertex2f(x, y);
//				GL11.glVertex2f(x, y + h);
//				GL11.glVertex2f(x + w, y + h);
//				GL11.glVertex2f(x + w, y);
//			}
//			GL11.glEnd();
		}
	}
	
	
	public static class TriadShieldVisuals extends BaseCombatLayeredRenderingPlugin {
		public ShipAPI ship;
		public TriadShieldStatsBackup script;
		public List<ShieldPiece> pieces = new ArrayList<ShieldPiece>();
		public List<ShieldPieceConnection> connections = new ArrayList<ShieldPieceConnection>();
		
		public TriadShieldVisuals(ShipAPI ship, TriadShieldStatsBackup script) {
			this.ship = ship;
			this.script = script;
			addShieldPieces();
		}
		
		public void addShieldPieces() {
			pieces.clear();
			
			SIDE_LENGTH = 20f;
			//SIDE_LENGTH = 10f;
			//SIDE_LENGTH = 120f;
			
			float side = SIDE_LENGTH;
			float height = (float) (side * Math.sqrt(3f) / 2f);
			float centerFromBottom = (float) (Math.sin(Math.toRadians(30f)) * height);
			//centerFromBottom = side/2f;
			int gridHeight = (int) (ship.getCollisionRadius() / side) * 2;
			if (gridHeight / 2 != 0) gridHeight++;
			if (gridHeight < 6) gridHeight = 6;
			int gridWidth = (int) (ship.getCollisionRadius() / height) * 2;
			if (gridWidth / 2 != 0) gridWidth++;
			if (gridWidth < 6) gridWidth = 6;
			for (int i = -gridWidth/2; i < gridWidth/2; i++) {
				for (int j = -gridHeight/2; j < gridHeight/2; j++) {
					float lowX = i * height + height/2f;
					float highX = (i + 1) * height + height/2f;
					float centerY = j * side + side/2f;
					ShieldPiece piece = new ShieldPiece(ship, true, lowX + centerFromBottom, centerY, side - 2f);
					if (piece.baseAlphaMult > 0) {
						pieces.add(piece);
					}
					
					if (j != gridHeight/2 - 1) {
						centerY += side/2f;
						piece = new ShieldPiece(ship, false, highX - centerFromBottom, centerY, side - 2f);
						if (piece.baseAlphaMult > 0) {
							pieces.add(piece);
						}
					}
				}
			}
			
			float maxDist = SIDE_LENGTH * 1.2f;
			for (int i = 0; i < pieces.size() - 1; i++) {
				ShieldPiece curr = pieces.get(i);
				for (int j = i + 1; j < pieces.size(); j++) {
					ShieldPiece other = pieces.get(j);
					if (curr == other) continue;
					if (Misc.getDistance(curr.offset, other.offset) > maxDist) continue;
					
					ShieldPieceConnection conn = new ShieldPieceConnection(curr, other);
					connections.add(conn);
				}
			}
		}
		
		@Override
		public EnumSet<CombatEngineLayers> getActiveLayers() {
			return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
		}
		@Override
		public boolean isExpired() {
			return false;
		}
		@Override
		public float getRenderRadius() {
			return ship.getCollisionRadius() + 100f;
		}
		
		@Override
		public void advance(float amount) {
			entity.getLocation().set(ship.getLocation());
			if (Global.getCombatEngine().isPaused()) return;
			
			for (ShieldPiece piece : pieces) {
				piece.advance(amount);
			}
			
			for (ShieldPieceConnection conn : connections) {
				conn.advance(amount);
			}
		}

		@Override
		public void render(CombatEngineLayers layer, ViewportAPI viewport) {
			float alphaMult = viewport.getAlphaMult();
			
			ShipSystemAPI system = ship.getPhaseCloak();
			if (system == null) system = ship.getSystem();
			alphaMult *= system.getEffectLevel();
			if (alphaMult <= 0f) return;
			
			GL11.glPushMatrix();
			GL11.glTranslatef(ship.getLocation().x, ship.getLocation().y, 0);
			GL11.glRotatef(ship.getFacing(), 0, 0, 1);
			
			for (ShieldPiece piece : pieces) {
				piece.render(alphaMult);
			}
			GL11.glPopMatrix();
			
//			Color color = Color.red;
//			color = Misc.scaleAlpha(color, 0.5f);
//			
//			float x = ship.getLocation().x - ship.getCollisionRadius() * 0.5f;
//			float y = ship.getLocation().y - ship.getCollisionRadius() * 0.5f;
//			float w = ship.getCollisionRadius();
//			float h = ship.getCollisionRadius();
//			
//			GL11.glDisable(GL11.GL_TEXTURE_2D);
//			GL11.glEnable(GL11.GL_BLEND);
//			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//	
//			
//			GL11.glColor4ub((byte)color.getRed(),
//							(byte)color.getGreen(),
//							(byte)color.getBlue(),
//							(byte)(color.getAlpha() * alphaMult));
//			
//			GL11.glBegin(GL11.GL_QUADS);
//			{
//				GL11.glVertex2f(x, y);
//				GL11.glVertex2f(x, y + h);
//				GL11.glVertex2f(x + w, y + h);
//				GL11.glVertex2f(x + w, y);
//			}
//			GL11.glEnd();
		}
	}
	
	protected TriadShieldVisuals visuals = null;
	
	public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
		return null;
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}
		
		if (visuals == null) {
			visuals = new TriadShieldVisuals(ship, this);
			Global.getCombatEngine().addLayeredRenderingPlugin(visuals);
			ship.addListener(this);
		}
		
		
		
		if (Global.getCombatEngine().isPaused()) {
			return;
		}
		
		if (state == State.COOLDOWN || state == State.IDLE) {
			unapply(stats, id);
			return;
		}
		
		ShipSystemAPI system = ship.getPhaseCloak();
		if (system == null) system = ship.getSystem();
		
		
		if (state == State.IN || state == State.ACTIVE) {
			
		} else if (state == State.OUT) {

		}
	}


	public void unapply(MutableShipStatsAPI stats, String id) {
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}
		
		
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}

}
