package com.fs.starfarer.api.impl.campaign.velfield;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.impl.campaign.velfield.TurbulenceCalc2.TurbulenceParams;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class TurbulenceEntityPlugin extends BaseCustomEntityPlugin {

	public static class PointParticle {
		float size = 6f;
		float mass = 0.0001f;
		Vector2f loc = new Vector2f();
		Vector2f vel = new Vector2f();
		Color color;
		float remaining;
		float elapsed;
	}
	
	public static class ParticleData {
		public SpriteAPI sprite;
		public Vector2f offset = new Vector2f();
		public Vector2f vel = new Vector2f();
		public float scale = 1f;
		public float scaleIncreaseRate = 1f;
		public float turnDir = 1f;
		public float angle = 1f;
		
		public float maxDur;
		public FaderUtil fader;
		public float elapsed = 0f;
		public float baseSize;
		public float mass;
		protected Color color;
		
		public ParticleData(float baseSize, float mass, float durIn, float durOut, float endSizeMult, float maxAngVel, Color color, String spriteSheetKey) {
			this.color = color;
			if (spriteSheetKey == null) {
				spriteSheetKey = "nebula_particles";
			}
			sprite = Global.getSettings().getSprite("misc", spriteSheetKey);
			//sprite = Global.getSettings().getSprite("graphics/fx/hit_glow.png");
			//sprite = Global.getSettings().getSprite("graphics/fx/particlealpha32sq.png");
			//sprite = Global.getSettings().getSprite("graphics/fx/particlealpha64linear.png");
//			queueResource(Type.TEXTURE, "graphics/fx/particlealpha32sq.png", 1);
//			queueResource(Type.TEXTURE, "graphics/fx/particleline32ln.png", 1);
			//sprite = Global.getSettings().getSprite("misc", "dust_particles");
			float i = Misc.random.nextInt(4);
			float j = Misc.random.nextInt(4);
			sprite.setTexWidth(0.25f);
			sprite.setTexHeight(0.25f);
			sprite.setTexX(i * 0.25f);
			sprite.setTexY(j * 0.25f);
			sprite.setAdditiveBlend();
			
			this.mass = mass;
			angle = (float) Math.random() * 360f;
			
			this.maxDur = durIn + durOut;
			scaleIncreaseRate = endSizeMult / maxDur;
			if (endSizeMult < 1f) {
				scaleIncreaseRate = -1f * endSizeMult;
			}
			scale = 1f;
			
			this.baseSize = baseSize;
			turnDir = Math.signum((float) Math.random() - 0.5f) * maxAngVel * (float) Math.random();
			
			fader = new FaderUtil(0f, durIn, durOut);
			fader.setBounceDown(true);
			fader.forceOut();
			fader.fadeIn();
		}
		
		public void advance(float amount, VelocityField field, float fieldX, float fieldY, float fieldFacing) {
			if (field.isInsideField(offset.x, offset.y, new Vector2f(fieldX, fieldY), fieldFacing)) {
				Vector2f fVel = field.getVelocity(offset.x, offset.y, new Vector2f(fieldX, fieldY), fieldFacing);
				float accelAmount = 1f / mass;
				if (accelAmount > 1f) accelAmount = 1f;
				accelAmount *= 60f;
				//p.vel.set(vel);
				Vector2f diff = Vector2f.sub(fVel, vel, new Vector2f());
				vel.x += diff.x * accelAmount * amount;
				vel.y += diff.y * accelAmount * amount;
			} else {
				fader.fadeOut();
//				if (p.elapsed > 0.5f) {
//					p.remaining = Math.min(p.remaining, 0.5f);
//				}
			}
			
			scale += scaleIncreaseRate * amount;
			
			offset.x += vel.x * amount;
			offset.y += vel.y * amount;
				
			angle += turnDir * amount;
			
			elapsed += amount;
//			if (maxDur - elapsed <= fader.getDurationOut() + 0.1f) {
//				fader.fadeOut();
//			}
			fader.advance(amount);
		}
	}
	
	protected List<ParticleData> particles = new ArrayList<ParticleData>();
	protected List<ParticleData> darkParticles = new ArrayList<ParticleData>();
	
	protected VelocityField field;
	protected List<PointParticle> testParticles = new ArrayList<PointParticle>();
	
	protected int frame = 0;
	protected float distanceTravelled;
 
	public VelocityField getField() {
		return field;
	}

	public void setField(VelocityField field) {
		this.field = field;
	}

	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		readResolve();
	}
	
	Object readResolve() {
		return this;
	}
	
	public float getRenderRange() {
		float cells = Math.max(field.field.length, field.field[0].length);
		return cells * 1.5f * field.cellSize;
	}
	
	public void advance(float amount) {
		//float days = Global.getSector().getClock().convertToDays(amount);
		
		//field.getField()[10][10].set(0, 1000f);
		//if (field.getField()[10][0].y == 0) {
		if (Keyboard.isKeyDown(Keyboard.KEY_P) || true) {
			//field.getField()[10][0].set(0, 21f * 21f * 100f);
			//field.getField()[10][10].set(0, 100f);
			
			for (int i = 0; i < field.getField()[0].length; i++) {
				//field.getField()[5][i].set((float) Math.random() * 20f - 10f, 100f + i * 10);
				//field.getField()[5][i].set((float) Math.random() * 20f - 10f, 1000f);
//				field.getField()[4][i].set(500f, 500f);
//				field.getField()[6][i].set(-500f, 500f);
				//field.getField()[5][i].set(0, 1000f);
				//field.getField()[10][i].set(0, 1000f);
				
//				if (i % 4 == 0) {
//					field.getField()[4][i].set(200f, 500f + (20 - i) * 10f);
//				} else if (i % 4 == 2){
//					field.getField()[6][i].set(-200f, 500f + (20 - i) * 10f);
//				} else {
//					field.getField()[5][i].set(0, 500f + (20 - i) * 10f);
//				}
				//field.getField()[5][i].set(0, 500f + (20 - i) * 10f);
				int x = field.getField().length / 2;
				field.getField()[x][i].set(0, 500f);
				
				
//				field.getField()[4][i].set(500f, 500f);
//				field.getField()[6][i].set(-500f, 500f);
				//field.getField()[5][i].y += 2000f * amount;
				
//				field.getField()[1][i].set(-200f, -200f);
//				field.getField()[9][i].set(200f, -200f);
//				field.getField()[3][i].set((float) Math.random() * 20f - 10f - 0, 100f + i * 20);
//				field.getField()[7][i].set((float) Math.random() * 20f - 10f + 0, 100f + i * 20);
			}
			
//			for (int i = 1; i < 20; i++) {
//				if (i % 2 == 0) {
//					field.getField()[0][i].set(200f, 0);
//				} else {
//					field.getField()[10][i].set(-200f, 0);
//				}
//			}
			
//			for (int i = 15; i <= 20; i++) {
//				field.getField()[8][i].set((float) Math.random() * 20f - 10f - 20, -100f);
//				field.getField()[12][i].set((float) Math.random() * 20f - 10f + 20, -100f);
//			}
//			field.getField()[10][1].set(0, 1000f);
//			field.getField()[10][2].set(0, 1000f);
//			field.getField()[9][0].set(0, 1000f);
//			field.getField()[11][0].set(0, 1000f);
		}
		
		frame++;
		
		TurbulenceParams params = new TurbulenceParams();
		params.field = field;
		params.effectWidth = 210f;
		params.effectLength = 410f;
		params.maxDispersionAngle = 120f;
		params.energyTransferMult = 5f;
		params.dampenFactor = 0.2f;
		params.maxVelocity = 1000f;
		
//		params.maxDispersionAngle = 180f;
//		params.maxDispersionAngle = 160f;
		params.maxDispersionAngle = 0f;
		params.maxVelocity = 1000f;
		params.energyTransferMult = 6f;
//		params.effectWidth = 410f;
//		params.effectLength = 810f;
		
		params.propagationAmount = 6f * amount;
//		if (frame % 2 == 0) {
//			params.propagationAmount = 12f * amount;
			TurbulenceCalc2.advance(params);
//		}
			
//		for (int i = 0; i < 10; i++) {
//			TurbulenceCalc2.advance(field, 210f, 410f, 6f * amount);
//		}
		//TurbulenceCalc2.advance(field, 410f, 620f, 1f);
		
		
		float facing = entity.getFacing();
		float x = entity.getLocation().x;
		float y = entity.getLocation().y;
		float w = field.getCellSize() * (field.getField().length - 1);
		float h = field.getCellSize() * (field.getField()[0].length - 1);
		
		//while (testParticles.size() < 5000) {
		while (testParticles.size() < 500) {
			Vector2f loc = new Vector2f();
			//loc.x = w * (float) Math.random() + x;
			loc.x = w * 0.4f + w * 0.2f * (float) Math.random() ;
			loc.y = h * (1f - (float) Math.random()) ;
			loc = Misc.rotateAroundOrigin(loc, entity.getFacing());
			loc.x += x;
			loc.y += y;
			//float size = 3f + 2f * (float) Math.random();
			float size = 2f;
			float mass = 0.0001f;
			//mass = (float) Math.random() * 10f;
			mass = (float) Math.random() * 10f;
			//mass = 100f;
			float dur = 0f + 4f * Misc.random.nextFloat();
			addTestParticle(loc, getRandomColor(), size, mass, dur);
			
		}
		
//		while (particles.size() < 1000) {
//			Vector2f loc = new Vector2f();
//			//loc.x = w * (float) Math.random() + x;
//			loc.x = w * 0.49f + w * 0.02f * (float) Math.random() + x;
//			loc.x = w * 0.4f + w * 0.2f * (float) Math.random() + x;
//			loc.x = w * 0.2f + w * 0.6f * (float) Math.random() + x;
//			//loc.y = h * (1f - (float) Math.random() * (float) Math.random()) + y;
//			loc.y = h * (1f - (float) Math.random()) + y;
//			float size = 250f + 20f * (float) Math.random();
//			size = 6f + 2f * (float) Math.random();
//			size = 12f;
//			size = 100f;
//			float mass = 0.0001f;
//			mass = (float) Math.random() * 10f;
//			//mass = 100f;
//			float dur = 0.5f + 3.5f * Misc.random.nextFloat();
//			Color c = getRandomColor();
//			//c = Misc.scaleAlpha(c, 0.25f);
//			addTexturedParticle(loc, c, mass, size, 10f, 0.5f, dur - 0.5f);
//		}
		
		Iterator<PointParticle> iter = testParticles.iterator();
		while (iter.hasNext()) {
			PointParticle p = iter.next();
			p.remaining -= amount;
			p.elapsed += amount;
			if (p.remaining <= 0) {
				iter.remove();
				continue;
			}
			
			if (field.isInsideField(p.loc.x, p.loc.y, new Vector2f(x, y), facing)) {
				Vector2f vel = field.getVelocity(p.loc.x, p.loc.y, new Vector2f(x, y), facing);
				float accelAmount = 1f / p.mass;
				if (accelAmount > 1f) accelAmount = 1f;
				accelAmount *= 60f;
				//p.vel.set(vel);
				Vector2f diff = Vector2f.sub(vel, p.vel, new Vector2f());
				p.vel.x += diff.x * accelAmount * amount;
				p.vel.y += diff.y * accelAmount * amount;
			} else {
				if (p.elapsed > 0.5f) {
					p.remaining = Math.min(p.remaining, 0.5f);
				}
//				iter.remove();
//				continue;
			}
			
			p.loc.x += p.vel.x * amount;
			p.loc.y += p.vel.y * amount;
		}
		

		List<ParticleData> remove = new ArrayList<ParticleData>();
		for (ParticleData p : particles) {
			p.advance(amount, field, x, y, facing);
			if (p.elapsed >= p.maxDur) {
				remove.add(p);
			}
		}
		particles.removeAll(remove);
		
		remove = new ArrayList<ParticleData>();
		for (ParticleData p : darkParticles) {
			p.advance(amount, field, x, y, facing);
			if (p.elapsed >= p.maxDur) {
				remove.add(p);
			}
		}
		darkParticles.removeAll(remove);
		
		
		Vector2f vel = new Vector2f(0f, Misc.getSpeedForBurnLevel(30f));
		Vector2f dv = new Vector2f(vel.x * amount, vel.y * amount);
		
		distanceTravelled += dv.length();
		
//		while (distanceTravelled > field.getCellSize()) {
//			dv = Misc.normalise(dv);
//			dv.scale(distanceTravelled);
////			entity.getLocation().x += dv.x;
////			entity.getLocation().y += dv.y;
//			field.shiftDown();
//			
//			distanceTravelled -= field.getCellSize();
//		}
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
	
		float mx = Mouse.getX();
		float my = Mouse.getY();
		
		float wmx = Global.getSector().getViewport().convertScreenXToWorldX(mx);
		float wmy = Global.getSector().getViewport().convertScreenYToWorldY(my);
		
		entity.setFacing(0f);
		
		float facing = entity.getFacing();
		float x = entity.getLocation().x;
		float y = entity.getLocation().y;
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		if (false || Keyboard.isKeyDown(Keyboard.KEY_O)) {
			GL11.glPushMatrix();
			GL11.glTranslatef(x, y, 0);
			GL11.glRotatef(facing, 0, 0, 1);
			
			GL11.glPointSize(20f);
			GL11.glEnable(GL11.GL_POINT_SMOOTH);
			GL11.glBegin(GL11.GL_POINTS);
			GL11.glColor4f(1f,0,0,0.75f);
			for (int i = 0; i < field.field.length; i++) {
				for (int j = 0; j < field.field[0].length; j++) {
					float cx = i * field.cellSize;
					float cy = j * field.cellSize;
					GL11.glVertex2f(cx, cy);
				}
			}
			GL11.glEnd();
			
			GL11.glLineWidth(1);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glBegin(GL11.GL_LINES);
			
			GL11.glColor4f(1f,1f,0,1f);
			float scale = 1f;
			for (int i = 0; i < field.field.length; i++) {
				for (int j = 0; j < field.field[0].length; j++) {
					Vector2f vel = field.field[i][j];
					
	//				float cx = x + i * field.cellSize;
	//				float cy = y + j * field.cellSize;
					float cx = i * field.cellSize;
					float cy = j * field.cellSize;
					
					GL11.glVertex2f(cx, cy);
					GL11.glVertex2f(cx + vel.x * scale, cy + vel.y * scale);
				}
			}
			
			GL11.glEnd();
			
			
			GL11.glPopMatrix();
	
			
	
			GL11.glPointSize(6f);
			GL11.glEnable(GL11.GL_POINT_SMOOTH);
			GL11.glBegin(GL11.GL_POINTS);
			GL11.glColor4f(1,1,0,1);
			GL11.glVertex2f(wmx, wmy);
			GL11.glEnd();
			GL11.glLineWidth(1);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glBegin(GL11.GL_LINES);
			
			GL11.glColor4f(1,1,0,1);
			Vector2f vel = field.getVelocity(wmx, wmy, new Vector2f(x, y), facing);
			GL11.glVertex2f(wmx, wmy);
			GL11.glVertex2f(wmx + vel.x, wmy + vel.y);
			
			GL11.glEnd();
		}
		
		
		
		GL11.glEnable(GL11.GL_POINT_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		float zoom = Global.getSector().getViewport().getViewMult(); 
		//System.out.println(zoom);
		GL11.glPointSize(2f / zoom);
		GL11.glBegin(GL11.GL_POINTS);
		for (PointParticle p : testParticles) {
			if (true) break;
			if (!viewport.isNearViewport(p.loc, 5)) continue;
			float a = 1f;
			if (p.remaining <= 0.5f) {
				a = p.remaining / 0.5f;
			} else if (p.elapsed < 0.5f) {
				a = p.elapsed / 0.5f;
			}
			//a *= 0.5f;
			Misc.setColor(p.color, a);
			GL11.glVertex2f(p.loc.x, p.loc.y);
			
			float zf = 1f;
			if (zoom < 1.75f && false) {
//				float zf = (1.75f - zoom) / 0.25f;
//				if (zf < 0) zf = 0;
//				if (zf > 1) zf = 1;
				Vector2f dir = Misc.normalise(new Vector2f(p.vel));
				float spread = 0.5f;
				//spread = 1f;
				a *= 0.65f;
				Misc.setColor(p.color, a * zf);
				GL11.glVertex2f(p.loc.x + dir.x * spread, p.loc.y + dir.y * spread);
				if (zoom < 1.5f) {
//					zf = (1.5f - zoom) / 0.25f;
//					if (zf < 0) zf = 0;
//					if (zf > 1) zf = 1;
					Misc.setColor(p.color, a * zf);
					GL11.glVertex2f(p.loc.x - dir.x * spread, p.loc.y - dir.y * spread);
				}
				
				if (zoom < 1.25f) {
					spread *= 2f;
					a *= 0.65f;
//					zf = (1.25f - zoom) / 0.25f;
//					if (zf < 0) zf = 0;
//					if (zf > 1) zf = 1;
					Misc.setColor(p.color, a * zf);
					GL11.glVertex2f(p.loc.x + dir.x * spread, p.loc.y + dir.y * spread);
					if (zoom < 1f) {
//						zf = (1f - zoom) / 0.25f;
//						if (zf < 0) zf = 0;
//						if (zf > 1) zf = 1;
						Misc.setColor(p.color, a * zf);
						GL11.glVertex2f(p.loc.x - dir.x * spread, p.loc.y - dir.y * spread);
					}
				}
			}
			
//			for (int i = 0; i < 4; i++) {
//				a *= 0.75f;
//				Misc.setColor(p.color, a);
//				GL11.glVertex2f(p.loc.x - dir.x * spread/zoom, p.loc.y - dir.y * spread/zoom);
//				spread += 0.5f;
//			}
			
		}
		GL11.glEnd();
		
		GL11.glLineWidth(3.5f);
		GL11.glLineWidth(2f);
		//GL11.glLineWidth(1.5f);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glBegin(GL11.GL_LINES);
		for (PointParticle p : testParticles) {
			//if (true) break;
			if (!viewport.isNearViewport(p.loc, 500)) continue;
			float a = 1f;
			if (p.remaining <= 0.5f) {
				a = p.remaining / 0.5f;
			} else if (p.elapsed < 0.5f) {
				a = p.elapsed / 0.5f;
			}
			
//			Vector2f currVel = new Vector2f(p.loc);
//			Vector2f currLoc = new Vector2f(p.vel);
			//Vector2f prevVel = new Vector2f(p.vel);
			Vector2f prev = new Vector2f(p.loc);
			float prevAlpha = 0f;
			float interval = 1/60f;
			float iter = 15f;
			
			for (int i = 0; i < iter; i++) {
				//boolean inField = field.isInsideField(p.loc.x, p.loc.y, new Vector2f(x, y), facing);
				//boolean inField = true;
				Vector2f vel = field.getVelocity(prev.x, prev.y, new Vector2f(x, y), facing);
				//Vector2f vel = new Vector2f(100f, 0f);
				Vector2f loc = new Vector2f(prev);
				loc.x -= vel.x * interval;
				loc.y -= vel.y * interval;
				
//				float velDelta = Vector2f.sub(vel, prevVel, new Vector2f()).length();
//				float baseAlpha = 1f;
				float baseAlpha = 0.5f;
//				baseAlpha += 0.5f * velDelta / 50f;
//				baseAlpha *= 0.5f;
//				//baseAlpha = 0.5f;
//				if (baseAlpha > 1f) baseAlpha = 1f;
				
				float alpha = baseAlpha * a * (iter - i) / iter;
				//if (!inField) alpha = 0f;
				
				Misc.setColor(p.color, prevAlpha);
				GL11.glVertex2f(prev.x, prev.y);
				Misc.setColor(p.color, alpha);
				GL11.glVertex2f(loc.x, loc.y);
				
				prevAlpha = alpha;
				prev = loc;
				//prevVel = vel;
			}

//			Vector2f dir = Misc.normalise(new Vector2f(p.vel));
//			float len = 5f;
//			len = p.vel.length() / 10f;
//			if (len > 30) len = 30;
//			//a *= 0.75f;
//			Misc.setColor(p.color, a);
//			GL11.glVertex2f(p.loc.x, p.loc.y);
//			Misc.setColor(p.color, 0f);
//			GL11.glVertex2f(p.loc.x - dir.x * len, p.loc.y - dir.y * len);
		}
		GL11.glEnd();
		
		renderTextureParticles(layer, viewport);
	}
	
	public void renderTextureParticles(CampaignEngineLayers layer, ViewportAPI viewport) {
		float b = viewport.getAlphaMult();
		
		CampaignEngineLayers normalLayer = layer;
		CampaignEngineLayers darkLayer = layer;
		if (layer == normalLayer) {
			for (ParticleData p : particles) {
				float size = p.baseSize * p.scale;
				Vector2f loc = p.offset;
				
				float alphaMult = 1f;
				
				p.sprite.setAngle(p.angle);
				p.sprite.setSize(size, size);
				p.sprite.setAlphaMult(b * alphaMult * p.fader.getBrightness());
				p.sprite.setColor(p.color);
				p.sprite.renderAtCenter(loc.x, loc.y);
			}
		} 
		if (layer == darkLayer) {
			GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
			
			for (ParticleData p : darkParticles) {
				//float size = proj.getProjectileSpec().getWidth() * 0.6f;
				float size = p.baseSize * p.scale;
				
				Vector2f loc = p.offset;
				
				float alphaMult = 1f;
				
				p.sprite.setAngle(p.angle);
				p.sprite.setSize(size, size);
				p.sprite.setAlphaMult(b * alphaMult * p.fader.getBrightness());
				p.sprite.setColor(p.color);
				p.sprite.renderAtCenter(loc.x, loc.y);
			}
			
			GL14.glBlendEquation(GL14.GL_FUNC_ADD);
		}
	}
	public void addTestParticle(Vector2f loc, Color color, float size, float mass, float dur) {
		PointParticle p = new PointParticle();
		p.loc.set(loc);
		p.mass = mass;
		p.remaining = dur;
		p.size = size;
		p.color = color;
		testParticles.add(p);
	}
	
	public void addTexturedParticle(Vector2f loc, Color color, float mass, float size, float endSizeMult, float durIn, float durOut) {
		ParticleData p = new ParticleData(size, mass, durIn, durOut, endSizeMult, 30f, color, null);
		p.offset.set(loc);
		particles.add(p);
//		p = new ParticleData(size * 0.8f, mass, durIn, durOut, endSizeMult, 30f, color, null);
//		p.offset.set(loc);
//		darkParticles.add(p);
	}

	public Color getRandomColor() {
//		if (true) {
//			return new Color(0.5f, 1f, 0.8f, 1f);
//		}
		float r = 0.5f;
		float g = 0.3f + 0.3f * (float) Math.random();
		float b = 0.75f + 0.25f * (float) Math.random();
		float a = 0.85f + 0.15f * (float) Math.random();
		if (r < 0) r = 0;
		if (r > 1) r = 1;
		if (g < 0) g = 0;
		if (g > 1) g = 1;
		if (b < 0) b = 0;
		if (b > 1) b = 1;
		if (a < 0) a = 0;
		if (a > 1) a = 1;
		a *= 0.33f;
		a = 1f;
		Color c = new Color(r, g, b, a);
		return c;
	}
}







