package com.fs.starfarer.api.impl.campaign.eventide;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate.DialogCallbacks;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class DuelPanel extends BaseCustomUIPanelPlugin {

	public static enum AttackResult {
		NO_HIT,
		BLOCK,
		HIT,
	}
	
	public static float SOUND_LOC_MULT = 0.25f;
	
	public static boolean DEBUG = true;

	
	protected InteractionDialogAPI dialog;
	protected DialogCallbacks callbacks;
	protected CustomPanelAPI panel;
	protected PositionAPI p;
	
	protected Actor player;
	protected Actor enemy;
	protected List<QuadParticles> particles = new ArrayList<QuadParticles>();
	protected float floorLevel;
	protected float leftBorder, rightBorder;
	protected float viewAreaWidth = 512;
	protected DuelEnemyAI ai;
	protected DuelBackground background;
	
	protected boolean tutorialMode = false;
	protected DuelTutorialPanel prompt;
	
	protected FaderUtil blinker = new FaderUtil(0f, 0.25f, 0.25f, true, true);
	protected String ambienceLoopId = null;
	
	public static DuelPanel createDefault(boolean playerSkilled, boolean enemySkilled, String ambienceLoopId) {
		Actions.initActions();
		//playerSkilled = false;
		
		Actor player = createActor(Actions.TEX, playerSkilled);
//		player.actionRemap1.put(Actions.IDLE, Actions.IDLE_HIGH);
//		player.actionRemap1.put(Actions.MOVE_FORWARD, Actions.MOVE_FORWARD_HIGH);
//		player.actionRemap1.put(Actions.MOVE_BACK, Actions.MOVE_BACK_HIGH);
//		player.actionRemap1.put(Actions.ATTACK, Actions.ATTACK_HIGH);
//		player.actionRemap1.put(Actions.BLOCK, Actions.BLOCK_LOW);
		//player.actionRemap1.put(Actions.ATTACK, Actions.RIPOSTE_HIGH);
		
//		player.actionRemap1.put(Actions.BLOCK, Actions.ATTACK_RECOVERY);
//		player.actionRemap1.put(Actions.BLOCK, Actions.ATTACK_HIGH_RECOVERY);
		//player.actionRemap1.put(Actions.ATTACK, Actions.ATTACK_HIGH);
		//player.actionRemap1.put(Actions.ATTACK, Actions.ATTACK_RECOVERY);
		//player.actionRemap1.put(Actions.BLOCK, Actions.BLOCK_LOW);
		//player.actionRemap1.put(Actions.BLOCK, Actions.ATTACK_HIGH_RECOVERY);
//		player.actionRemap1.put(Actions.ATTACK, Actions.BLOCK_LOW);
//		player.actionRemap1.put(Actions.BLOCK, Actions.FALL);
//		player.actionRemap1.put(Actions.BLOCK, Actions.BLOCK_LOW);
		//player.actionRemap1.put(Actions.ATTACK, Actions.ATTACK_HIGH);
		//player.actionRemap1.put(Actions.ATTACK, Actions.ATTACK_RECOVERY);
//		player.actionRemap1.put(Actions.RIPOSTE, Actions.RIPOSTE_HIGH);
//		player.actionRemap1.put(Actions.IDLE, Actions.IDLE_HIGH);
//		player.actionRemap1.put(Actions.MOVE_FORWARD, Actions.MOVE_FORWARD_HIGH);
		
		Actor enemy = createActor(Actions.TEX, enemySkilled);
		enemy.actionRemap1.put(Actions.IDLE, Actions.IDLE_HIGH);
		enemy.actionRemap1.put(Actions.MOVE_FORWARD, Actions.MOVE_FORWARD_HIGH);
		enemy.actionRemap1.put(Actions.MOVE_BACK, Actions.MOVE_BACK_HIGH);
		enemy.actionRemap1.put(Actions.ATTACK, Actions.ATTACK_HIGH);
		enemy.actionRemap1.put(Actions.ATTACK_RECOVERY, Actions.ATTACK_HIGH_RECOVERY);
		enemy.actionRemap1.put(Actions.RIPOSTE, Actions.RIPOSTE_HIGH);
		enemy.actionRemap1.put(Actions.BLOCK, Actions.BLOCK_LOW);
		
		DuelBackground bg = new EventideDuelBackground();
		DuelPanel panel = new DuelPanel(player, enemy, new DuelEnemyAIImpl(), bg);
		//panel = new DuelPanel(player, enemy, null, bg); // this turns off enemy AI!!!
//		DO_CYCLE = true; // do block - attack - recover cycle automatically w/ no interaction
//		DO_CYCLE_HIGH = true; // do the enemy cycle, on the player actor
		DO_CYCLE = false;
		DO_CYCLE_HIGH = false;
		
		panel.ambienceLoopId = ambienceLoopId;
		
		return panel;
	}
	
	public static DuelPanel createTutorial(boolean playerSkilled, String ambienceLoopId) {
		Actions.initActions();
		
		Actor player = createActor(Actions.TEX, playerSkilled);
		
		Actor enemy = createActor(Actions.TEX, true);
		
		DuelBackground bg = new EventideDuelBackground();
		DuelPanel panel = new DuelPanel(player, enemy, null, bg);
		panel.tutorialMode = true;
		panel.ambienceLoopId = ambienceLoopId;
		return panel;
	}
	
	public static Actor createActor(String tex, boolean skilled) {
		Actor actor = new Actor(tex);
		int health = 5;
		if (!skilled) {
			health = 3;
			actor.actionRemap2.put(Actions.RIPOSTE, Actions.ATTACK);
			actor.actionRemap2.put(Actions.RIPOSTE_HIGH, Actions.ATTACK_HIGH);
//			float speedMult = 0.8f;
//			actor.actionSpeedMult.put(Actions.ATTACK, speedMult);
//			actor.actionSpeedMult.put(Actions.ATTACK_HIGH, speedMult);
//			actor.actionSpeedMult.put(Actions.BLOCK, speedMult);
//			actor.actionSpeedMult.put(Actions.BLOCK_LOW, speedMult);
		}
		
		//health = 15;
		actor.maxHealth = health;
		actor.health = health;
		return actor;
	}
	
	
	public DuelPanel(Actor player, Actor enemy, DuelEnemyAI ai, DuelBackground background) {
		this.player = player;
		this.enemy = enemy;
		this.ai = ai;
		this.background = background;
	}
	
	public void init(CustomPanelAPI panel, DialogCallbacks callbacks, InteractionDialogAPI dialog) {
		this.panel = panel;
		this.callbacks = callbacks;
		this.dialog = dialog;
		
//		Actions.initActions();
//		player = new Actor();
//		enemy = new Actor();
		
		blinker.fadeIn();
		
		if (tutorialMode) {
			prompt = new DuelTutorialPanel();
			CustomPanelAPI p = panel.createCustomPanel(450, 120, prompt);
			panel.addComponent(p).inTL(10, 30);
			prompt.init(p, callbacks, dialog);
		}
	}

	public CustomPanelAPI getPanel() {
		return panel;
	}

	public PositionAPI getPosition() {
		return p;
	}
	
	public float getFloorLevel() {
		return floorLevel;
	}

	protected float desiredViewCenterX = 0f;
	protected float viewCenterX = 0f;
	public void positionChanged(PositionAPI position) {
		this.p = position;
		
		if (player != null) {
			float cx = p.getCenterX();
			float cy = p.getCenterY();
			
			
			float charHeight = getCharacterHeight();
			
			floorLevel = (int)(cy - charHeight/2f);
			leftBorder = (int)(cx - p.getWidth() / 2f + 50);
			rightBorder = (int)(cx + p.getWidth() / 2f - 50);
			
			float width = background.getStageWidth();
			float extra = width - p.getWidth();
			
			leftBorder -= (int)(extra/2f);
			rightBorder += (int)(extra/2f);
			
			float down = 5f;
			player.loc.set(cx - 200, floorLevel + charHeight/2f - down);
			player.facing = 1;
			enemy.loc.set(cx + 200, floorLevel + charHeight/2f - down);
			enemy.facing = -1;
			
			viewAreaWidth = 200f;
			desiredViewCenterX = player.loc.x + viewAreaWidth / 2f - 50f;
			viewCenterX = desiredViewCenterX;
		}
	}

	public void render(float alphaMult) {
		
	}

	public void renderBelow(float alphaMult) {
		//if (tutorialMode) return;
		if (p == null) return;
		
		float x = p.getX();
		float y = p.getY();
		float cx = p.getCenterX();
		float cy = p.getCenterY();
		float w = p.getWidth();
		float h = p.getHeight();

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		
		float s = Global.getSettings().getScreenScaleMult();
		GL11.glScissor((int)(x * s), (int)(y * s), (int)(w * s), (int)(h * s));
		
//		SpriteAPI s = Global.getSettings().getSprite(TEX);
//		s.render(x, y + h - s.getHeight());
		DEBUG = true;
		DEBUG = false;
		
		if (DEBUG && false) {
			float w2 = player.currAction.curr.width * player.currAction.anim.scale;
			float h2 = player.currAction.curr.height * player.currAction.anim.scale;
			Misc.renderQuad(player.loc.x - w2/2f, player.loc.y - h2/2f, w2, h2, new Color(50,50,50,255), alphaMult);
		}
		
		float centerX = player.loc.x - (desiredViewCenterX - cx);
		if (player.currAction != null && player.currAction.anim != null && 
				player.currAction.anim.hasAttackFrames()) {
			centerX = cx;
		} else if (player.currAction != null && player.currAction.anim != null && player.currAction.curr != null) {
			if (!player.currAction.curr.hittableArea.isEmpty()) {
				HitArea area = player.currAction.curr.hittableArea.get(0);
				centerX = player.loc.x + (area.x + area.w / 2f)  - (desiredViewCenterX - cx);
			}
		}
		float leftX = cx - viewAreaWidth / 2f;
		float rightX = cx + viewAreaWidth / 2f;
		
		//System.out.println("[" + leftX + ", " + rightX + "] centerX: " + centerX + ", prev: " + prevOffsetX);
		
		if (centerX < leftX) {
			desiredViewCenterX -= leftX - centerX + 100f;
		} else if (centerX > rightX) {
			desiredViewCenterX += centerX - rightX + 100f;
		}
		float minVX = leftBorder + w / 2f - 50f; 
		float maxVX = rightBorder - w / 2f + 50f;
		if (desiredViewCenterX < minVX) desiredViewCenterX = minVX;
		if (desiredViewCenterX > maxVX) desiredViewCenterX = maxVX;
		
		float offsetX = -(viewCenterX - cx);
		
		if (background == null) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			Misc.renderQuad(x, y, w, h, Color.black, alphaMult);
			Misc.renderQuad(x, y, w, h, new Color(50,50,50,255), alphaMult);
		} else {
			background.render(this, offsetX, 0f, alphaMult);
		}
		
//		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		Misc.renderQuad(leftX, y, 5, h, Color.black, alphaMult);
//		Misc.renderQuad(rightX, y, 5, h, Color.black, alphaMult);
//		Misc.renderQuad(centerX, y, 5, h, Color.green, alphaMult);
		
		GL11.glPushMatrix();
		GL11.glTranslatef(offsetX, 0, 0);
//		Misc.renderQuad(leftBorder, y, 5, h, Color.red, alphaMult);
//		Misc.renderQuad(rightBorder - 5, y, 5, h, Color.red, alphaMult);
		
		for (QuadParticles p : particles) {
			p.render(alphaMult, true);
		}
		
		if (enemy.health > 0 && player.health > 0) {
			boolean playerAttack = player.currAction != null && player.currAction.anim != null && 
									player.currAction.anim.hasAttackFrames();
			if (playerAttack) {
				if (!tutorialMode) enemy.render(alphaMult);
				player.render(alphaMult);
			} else {
				player.render(alphaMult);
				if (!tutorialMode) enemy.render(alphaMult);
			}
		} else if (enemy.health > 0) {
			player.render(alphaMult);
			if (!tutorialMode) enemy.render(alphaMult);
		} else {
			if (!tutorialMode) enemy.render(alphaMult);
			player.render(alphaMult);
		}
		
		for (QuadParticles p : particles) {
			p.render(alphaMult, false);
		}
		GL11.glPopMatrix();
		
		if (background != null) {
			background.renderForeground(this, offsetX, 0f, alphaMult);
		}
		
		renderHealth(alphaMult);
		
		if (ai != null) {
			ai.render(alphaMult);
		}
		
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}
	
	public void renderHealth(float alphaMult) {
		float x = p.getX();
		float y = p.getY();
		float w = p.getWidth();
		float h = p.getHeight();
		
		float pipW = 20f;
		float pipH = 20f / 1.6f;
		float pad = 3f;
		float opad = 10f;
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		for (int i = 0; i < player.maxHealth; i++) {
			boolean filled = i < player.health;
			if (player.health == 1 && i == 0 && blinker.isFadingIn()) filled = false;
			renderPip(x + opad + (pipW + pad) * i, y + h - opad - pipH,
					pipW, pipH, Misc.getPositiveHighlightColor(), alphaMult,
				    filled);
		}
		
		if (!tutorialMode) {
			for (int i = enemy.maxHealth - 1; i >= 0; i--) {
				renderPip(x + w - opad - (pipW + pad) * (i + 1), y + h - opad - pipH,
						pipW, pipH, Misc.getNegativeHighlightColor(), alphaMult,
					    i < enemy.health);
			}
		}
	}
	
	public void renderPip(float x, float y, float w, float h, Color c, float a, boolean filled) {
		if (filled) {
			Misc.renderQuad(x, y, w, h, c, a);
		} else {
			Misc.renderQuad(x, y, w, 1, c, a);
			Misc.renderQuad(x, y + h - 1, w, 1, c, a);
			Misc.renderQuad(x, y + 1, 1, h - 2, c, a);
			Misc.renderQuad(x + w - 1, y + 1, 1, h - 2, c, a);
		}
		Misc.renderQuad(x + w, y - 1, 1, h, Color.black, a);
		Misc.renderQuad(x + 1, y - 1, w - 1, 1, Color.black, a);
		
	}
	
	public static boolean DO_CYCLE = false;
	public static boolean DO_CYCLE_HIGH = false;
	public int cycleIndex = -1;
	public static List<String> cycle = new ArrayList<String>();
	static {
		cycle.add(Actions.BLOCK);
		cycle.add(Actions.ATTACK);
		cycle.add(Actions.ATTACK_RECOVERY);
	}
	public static List<String> cycleHigh = new ArrayList<String>();
	static {
		cycleHigh.add(Actions.BLOCK_LOW);
		cycleHigh.add(Actions.ATTACK_HIGH);
		cycleHigh.add(Actions.ATTACK_HIGH_RECOVERY);
	}

	public void advance(float amount) {
		if (p == null) return;
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Global.getSettings().isDevMode()) {
			amount *= 0.25f;
		}
		if (tutorialMode) {
			panel.bringComponentToTop(prompt.getPanel());
		}
		//amount *= 0.5f;
		//System.out.println(player.loc.x);
		
		viewCenterX = Misc.approach(viewCenterX, desiredViewCenterX, 3f, 1f, amount);
		//Global.getSoundPlayer().setListenerPosOverrideOneFrame(new Vector2f(viewCenterX * 0f, player.loc.y));
		float soundOffset = viewCenterX - player.loc.x;
		soundOffset *= SOUND_LOC_MULT;
		Global.getSoundPlayer().setListenerPosOverrideOneFrame(
				new Vector2f(soundOffset + (player.loc.x + enemy.loc.x)/2f * SOUND_LOC_MULT, player.loc.y));
		//System.out.println("VCX " + viewCenterX + ", playerLoc: " + player.loc.x);
		
		if (ambienceLoopId != null) {
			Global.getSoundPlayer().playUILoop(ambienceLoopId, 1f, 1f);
		}
		
		if (background != null) {
			background.advance(amount);
		}
		
		if (DO_CYCLE || DO_CYCLE_HIGH) {
			amount *= 1f;// 0.25f;
			if (player.getActionId() != null && (player.getActionId().equals(Actions.ATTACK) ||
					player.getActionId().equals(Actions.ATTACK_HIGH))) {
				float timeUntilAttack = 0f;
				for (CharAnimFrame f : player.currAction.anim.frames) {
					if (!f.attackArea.isEmpty()) break;
					timeUntilAttack += f.dur;
				}
				//if (player.currAction.framesUntilAttackFrame() <= 1) {
				if (timeUntilAttack <= player.currAction.progress + 0.05f) {
					player.currAction = null;
				}
			}
			if (player.getActionId().isEmpty() || player.getActionId().equals(Actions.IDLE) ||
					 player.getActionId().equals(Actions.IDLE_HIGH)) {
				cycleIndex++;
				if (cycleIndex < 0) cycleIndex = 0;
				if (DO_CYCLE_HIGH) {
					cycleIndex = cycleIndex % cycleHigh.size();
					player.doAction(cycleHigh.get(cycleIndex), true);
				} else {
					cycleIndex = cycleIndex % cycle.size();
					player.doAction(cycle.get(cycleIndex), true);
				}
			}
		}
		
		//amount *= 0.2f;
		blinker.advance(amount * 1.5f);
		
		Iterator<QuadParticles> iter = particles.iterator();
		while (iter.hasNext()) {
			QuadParticles curr = iter.next();
			curr.advance(amount);
			if (curr.isDone()) {
				iter.remove();
			}
		}
		player.advance(amount);
		enemy.advance(amount);
		
		if (!tutorialMode) {
			if (player.loc.x - 100 > enemy.loc.x) {
				player.facing = -1f;
				enemy.facing = 1f;
			} else if (player.loc.x + 100 < enemy.loc.x){
				player.facing = 1f;
				enemy.facing = -1f;
			}
		}
		
		float minDist = 40;
		//minDist = 100;
		if (getDistance() < minDist && player.health > 0 && enemy.health > 0 && !tutorialMode) {
			player.doAction(Actions.MOVE_BACK, false);
			enemy.doAction(Actions.MOVE_BACK, false);
		} else {
//			if (player.loc.x < leftBorder || player.loc.x > rightBorder) {
//				player.doAction(Actions.MOVE_FORWARD, true);
//			}
//			if (enemy.loc.x < leftBorder || enemy.loc.x > rightBorder) {
//				enemy.doAction(Actions.MOVE_FORWARD, true);
//			}
			if (player.loc.x < leftBorder) {
				player.loc.x = leftBorder;
			}
			if (player.loc.x > rightBorder) {
				player.loc.x = rightBorder;
			}
			if (enemy.loc.x < leftBorder) {
				enemy.loc.x = leftBorder;
			}
			if (enemy.loc.x > rightBorder) {
				enemy.loc.x = rightBorder;
			}
		}
		
		checkBlocksAndHits();
		
		doAI(amount);
	}
	
	public float getDistance() {
		return Math.abs(player.loc.x - enemy.loc.x);
	}
	
	protected boolean prevWasAttack;

	public void doAI(float amount) {
		if (ai != null) {
			ai.advance(amount, this);
		}
//		if (enemy.currAction != null && enemy.currAction.anim.action == Actions.IDLE) {
//			//float r = (float) Math.random();
//			//System.out.println("PICKING " + rand);
//			if (prevWasAttack) {
//				//enemy.doAction(Actions.BLOCK, false);
//				enemy.doAction(Actions.ATTACK, false);
//				prevWasAttack = false;
//			} else {
//				enemy.doAction(Actions.ATTACK, false);
//				prevWasAttack = true;
//			}
////			if (r < 0.5f && false) {
////				enemy.doAction(Actions.ATTACK, false);
////			} else {
////				enemy.doAction(Actions.BLOCK, false);
////			}
//		}
	}
	

	public void processInput(List<InputEventAPI> events) {
		if (p == null) return;
		
		for (InputEventAPI event : events) {
			if (event.isConsumed()) continue;
			if ((Global.getSettings().isDevMode() || tutorialMode) &&
					event.isKeyDownEvent() && event.getEventValue() == Keyboard.KEY_ESCAPE) {
				event.consume();
				callbacks.dismissDialog();
				return;
			}
			
			if (event.isKeyDownEvent() &&
					(event.getEventValue() == Keyboard.KEY_UP ||
					event.getEventValue() == Keyboard.KEY_NUMPAD8 ||
					event.getEventValue() == Keyboard.KEY_W)) {
				event.consume();
				player.doAction(Actions.BLOCK, false);
				if (tutorialMode) prompt.reportAction(Actions.BLOCK);
				continue;
			}
			if (event.isKeyDownEvent() &&
					(event.getEventValue() == Keyboard.KEY_LEFT ||
					event.getEventValue() == Keyboard.KEY_NUMPAD4 ||
					event.getEventValue() == Keyboard.KEY_A)) {
				event.consume();
				if (player.facing > 0) {
					player.doAction(Actions.MOVE_BACK, false);
					if (tutorialMode) prompt.reportAction(Actions.MOVE_BACK);
				} else {
					player.doAction(Actions.MOVE_FORWARD, false);
					if (tutorialMode) prompt.reportAction(Actions.MOVE_FORWARD);
				}
				continue;
			}
			if (event.isKeyDownEvent() &&
					(event.getEventValue() == Keyboard.KEY_RIGHT ||
					event.getEventValue() == Keyboard.KEY_NUMPAD6 ||
					event.getEventValue() == Keyboard.KEY_D)) {
				event.consume();
				if (player.facing > 0) {
					player.doAction(Actions.MOVE_FORWARD, false);
					if (tutorialMode) prompt.reportAction(Actions.MOVE_FORWARD);
				} else {
					player.doAction(Actions.MOVE_BACK, false);
					if (tutorialMode) prompt.reportAction(Actions.MOVE_BACK);
				}
				continue;
			}
			if (event.isKeyDownEvent() &&
					event.getEventValue() == Keyboard.KEY_SPACE) {
				event.consume();
				player.doAction(Actions.ATTACK, false);
				if (tutorialMode) prompt.reportAction(Actions.ATTACK);
				continue;
			}
			if (Global.getSettings().isDevMode() && event.isKeyDownEvent() &&
					event.getEventValue() == Keyboard.KEY_F) {
				event.consume();
				player.doAction(Actions.FALL, true);
				
				//wasHit(player, null);
				//wasHit(enemy, null);
				continue;
			}
			if (event.isKeyDownEvent() &&
					event.getEventValue() == Keyboard.KEY_G) {
				event.consume();
				addSparks(player, enemy);
				continue;
			}
		}
	}
	
	public void checkBlocksAndHits() {
		if (tutorialMode) return;
		
		AttackResult playerAttackResult = checkAttackVsDefense(player, enemy);
		AttackResult enemyAttackResult = checkAttackVsDefense(enemy, player);
		
		if (playerAttackResult == AttackResult.HIT && enemyAttackResult == AttackResult.HIT) {
			if ((float) Math.random() < 0.5f) {
				playerAttackResult = AttackResult.NO_HIT;
			} else {
				enemyAttackResult = AttackResult.NO_HIT;
			}
		}
		
		if (playerAttackResult == AttackResult.BLOCK) {
			addSparks(player, enemy);
			player.currAction.wasBlocked = true;
			player.currAction.undoLastMove();
			enemy.currAction.performedBlock = true;
			player.doAction(Actions.ATTACK_RECOVERY, true);
			//enemy.endCurrentAnimation();
		} else if (playerAttackResult == AttackResult.HIT) {
			wasHit(enemy, player);
			player.currAction.scoredHit = true;
		}
		
		if (enemyAttackResult == AttackResult.BLOCK) {
			addSparks(enemy, player);
			enemy.currAction.wasBlocked = true;
			enemy.currAction.undoLastMove();
			player.currAction.performedBlock = true;
			enemy.doAction(Actions.ATTACK_RECOVERY, true);
			//player.endCurrentAnimation();
		} else if (enemyAttackResult == AttackResult.HIT) {
			wasHit(player, enemy);
			enemy.currAction.scoredHit = true;
		}
	}
	
	public AttackResult checkAttackVsDefense(Actor attacker, Actor defender) {
		AnimAction a = attacker.currAction;
		AnimAction d = defender.currAction;
		if (a.curr == null || a.curr.attackArea.isEmpty()) return AttackResult.NO_HIT;
		if (d.curr == null || d.curr.hittableArea.isEmpty()) return AttackResult.NO_HIT;
		
		if (a.wasBlocked) return AttackResult.NO_HIT;
		if (a.scoredHit) return AttackResult.NO_HIT;
		
		for (HitArea attack : a.curr.attackArea) {
			attack = attack.getAdjustedForAction(a);
			for (HitArea block : d.curr.blockArea) {
				block = block.getAdjustedForAction(d);
				if (attack.intersects(block)) {
					return AttackResult.BLOCK;
				}
			}
		}
		
		for (HitArea attack : a.curr.attackArea) {
			attack = attack.getAdjustedForAction(a);
			for (HitArea hit : d.curr.hittableArea) {
				hit = hit.getAdjustedForAction(d);
				if (attack.intersects(hit)) {
					return AttackResult.HIT;
				}
			}
		}
		
		return AttackResult.NO_HIT;
	}
	
	public void addSparks(Actor attacker, Actor defender) {
		//Global.getSoundPlayer().playUISound(Actions.SOUND_CLASH, 1f, 1f);
		if (true) {
//			attacker.freeze(0.1f);
//			defender.freeze(0.1f);
//			attacker.freeze(1.9f);
//			defender.freeze(1.9f);
			return;
		}
		QuadParticles p = new QuadParticles();
		p.additiveBlend = true;
		p.minDur = 0.1f;
		p.maxDur = 0.3f;
		p.minSize = 1f;
		p.maxSize = 2f;
		p.fadeTime = 0.1f;
		p.minColor = new Color(255, 200, 100, 155);
		p.maxColor = new Color(255, 255, 150, 255);
		
		
		Vector2f loc = Vector2f.add(attacker.loc, defender.loc, new Vector2f());
		loc.scale(0.5f);
		loc.y += getCharacterHeight() * 0.18f;
		
		AnimAction block = defender.currAction;
		
		if (block != null && block.curr != null) {
			if (!block.curr.blockArea.isEmpty()) {
				HitArea area = block.curr.blockArea.get(0);
				area = area.getAdjustedForAction(block);
				Vector2f loc2 = new Vector2f(loc);
				loc2.x = area.x + area.w / 2f + block.actor.facing * area.w * 0.25f;
				float dist1 = Misc.getDistance(defender.loc, loc);
				float dist2 = Misc.getDistance(defender.loc, loc2);
				// if the "in between" location is farther than the one in the block area, use the block area
				// otherwise opponents are close, use in-between location
				if (dist1 > dist2) {
					loc.set(loc2); 
				}
			}
		}
		
		int num = 30 + Misc.random.nextInt(10);
		for (int i = 0; i < num; i++) {
			Vector2f pos = Misc.getPointWithinRadius(loc, 5f);
			Vector2f vel = Misc.getPointWithinRadius(new Vector2f(), 200f);
			p.addParticle(pos.x, pos.y, vel.x, vel.y);
		}
		
		p.gravity = getGravity() * 0.1f;
		
		particles.add(p);
	}
	

	public void wasHit(Actor actor, Actor by) {
		int damage = 1;
		if (by != null && by.currAction != null && by.currAction.curr != null) {
			damage = by.currAction.curr.hitDamage; 
		}
		//damage = 0;
		actor.health -= damage;
		if (actor.health > 0) {
			actor.doAction(Actions.GOT_HIT, true);
		} else {
			actor.doAction(Actions.FALL, true);
		}
		addBlood(actor);
	}
	public void addBlood(Actor actor) {
		Vector2f loc = new Vector2f(actor.loc);
		//loc.y += 3f * player.currAction.anim.scale;
		loc.y += getCharacterHeight() * 0.07f;
		addBlood(loc, actor);
	}
	public void addBlood(Vector2f loc, Actor actor) {
//		if (true) {
//			actor.freeze(1.9f);
//			return;
//		}
		//Global.getSoundPlayer().playUISound(Actions.SOUND_RIP, 1f, 1f);
		
		QuadParticles p = new QuadParticles();
		p.minDur = 5f;
		p.maxDur = 8f;
		p.minSize = 2f;
		p.maxSize = 3f;
		p.fadeTime = 3f;
		p.minColor = new Color(136, 8, 8, 155);
		p.maxColor = new Color(238, 75, 43, 255);
		//p.maxFloorMod = 7f;
		p.maxFloorMod = 30f;
		
		int base = 100;
		base -= actor.health * 15;
		if (base < 20) base = 20;
		//base = 20;
		int num = base + Misc.random.nextInt((int) (base * 0.5f));
		num *= 2f;
		for (int i = 0; i < num; i++) {
			Vector2f pos = Misc.getPointWithinRadius(loc, 15f);
			Vector2f vel = Misc.getPointWithinRadius(new Vector2f(), 150f);
			p.addParticle(pos.x, pos.y, vel.x, vel.y);
		}
		
		p.gravity = getGravity();
		p.floor = floorLevel - 10f;
		p.floorFriction = 100f;
		particles.add(p);
	}
	
	
	public float getGravity() {
		// assume player is about 2m tall
		float h = getCharacterHeight();
		return h / 2f * 9.81f;
	}
	
	public float getCharacterHeight() {
		CharAnim anim = Actions.ANIMATIONS.get(Actions.IDLE);
		return anim.frameHeight * anim.scale;
	}

	public Actor getPlayer() {
		return player;
	}

	public Actor getEnemy() {
		return enemy;
	}
	
	
}



