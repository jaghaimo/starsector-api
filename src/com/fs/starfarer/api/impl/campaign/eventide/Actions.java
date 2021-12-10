package com.fs.starfarer.api.impl.campaign.eventide;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;

public class Actions {

	//public static String TEX = "/photoshop stuff/starfarer/out/pop_anim.png";
	public static String TEX = "graphics/misc/characterSheet00.png";
	
	public static String SOUND_CLOTH = "soe_cloth";
	public static String SOUND_FALL = "soe_fall";
	public static String SOUND_RIP = "soe_rip";
	public static String SOUND_CLASH = "soe_clash";
	public static String SOUND_STEP = "soe_step";
	public static String SOUND_SWISH = "soe_swish";
	
	public static final String IDLE = "IDLE";
	public static final String IDLE_HIGH = "IDLE_HIGH";
	public static final String BLOCK = "BLOCK";
	public static final String ATTACK = "ATTACK";
	public static final String RIPOSTE = "RIPOSTE";
	public static final String BLOCK_LOW = "BLOCK_LOW";
	public static final String ATTACK_HIGH = "ATTACK_HIGH";
	public static final String RIPOSTE_HIGH = "RIPOSTE_HIGH";
	public static final String ATTACK_RECOVERY = "ATTACK_RECOVERY";
	public static final String ATTACK_HIGH_RECOVERY = "ATTACK_HIGH_RECOVERY";
	public static final String MOVE_BACK = "MOVE_BACK";
	public static final String MOVE_BACK_HIGH = "MOVE_BACK_HIGH";
	public static final String MOVE_FORWARD = "MOVE_FORWARD";
	public static final String MOVE_FORWARD_HIGH = "MOVE_FORWARD_HIGH";
	public static final String GOT_HIT = "GOT_HIT";
	public static final String FALL = "FALL";
	
	
	public static Map<String, CharAnim> ANIMATIONS = new LinkedHashMap<String, CharAnim>();
	
	
	public static void initActions() {
		try {
			
//			SOUND_CLOTH = "";
//			SOUND_STEP = "";
//			SOUND_CLOTH = "soe_cloth";
//			SOUND_STEP = "soe_step";
//			SOUND_STEP = "mine_ping";
//			SOUND_CLOTH = "";
//			Global.getSettings().profilerSetEnabled(true);
//			Global.getSettings().profilerReset();
//			
//			for (int i = 0; i < 1; i++) {
//				Global.getSettings().profilerBegin("loading sheet");
				Global.getSettings().unloadTexture(TEX);
				Global.getSettings().forceMipmapsFor(TEX, true);
				Global.getSettings().loadTexture(TEX);
//				Global.getSettings().profilerEnd();
				//Global.getSettings().loadTextureConvertBlackToAlpha(TEX);

//				Global.getSettings().profilerBegin("loading sprites");
//				File down = new File("/Users/Alex/Downloads/Tiles");
//				for (File curr : down.listFiles()) {
//					if (curr.isFile() && curr.getName().endsWith("png")) {
//						String path = curr.getAbsolutePath();
//						Global.getSettings().unloadTexture(path);
//						Global.getSettings().loadTexture(path);
//					}
//				}
//				Global.getSettings().profilerEnd();
//			}
//			Global.getSettings().profilerPrintResultsTree();
//			Global.getSettings().profilerRestore();
//			Global.getSettings().profilerSetEnabled(false);
			
			
		} catch (IOException e) {
			throw new RuntimeException("Error loading texture", e);
		}
		
		//float sheetHeight = 2048;
		float sheetHeight = 1536;
		
		float frameHeight = 207f; //202.5f;
		float frameDur = 1f / 7f;
		frameDur = 1f / 10f;
		//float scale = 3f;
		float scale = 1f;
		//scale = 0.75f;
		//scale = 0.5f;
	//	frameDur *= 3f;
		float textureScale = 1f;
		CharAnim curr;
		
		float bodyW = 38;
		
		float y;
		
		//frameDur *= 3f;
		//y = 752;
		y = sheetHeight - frameHeight;
		curr = new CharAnim(TEX, IDLE, frameHeight);
		curr.interruptableBy(MOVE_BACK, MOVE_FORWARD, 
						ATTACK, RIPOSTE, BLOCK, ATTACK_HIGH, RIPOSTE_HIGH, BLOCK_LOW, GOT_HIT, FALL);
		curr.scale = scale;
		curr.skip(2);
		curr.addFrame(y, 168, frameDur * 2f);
		curr.last.setHittable(-60, bodyW);
		curr.addFrame(y, 167, frameDur * 2f);
		curr.last.setHittable(-60, bodyW);
		curr.addFrame(y, 166, frameDur * 2f);
		curr.last.setHittable(-60, bodyW);
		curr.skip(-166f + -167); // this goes back!
		curr.addFrame(y, 166, frameDur * 2f);
		curr.last.setHittable(-60, bodyW);
		addAnim(curr, textureScale);
		
		
		//frameDur *= 3f;
		y = sheetHeight - frameHeight;
		curr = new CharAnim(TEX, IDLE_HIGH, frameHeight);
		curr.interruptableBy(MOVE_BACK, MOVE_FORWARD, 
				ATTACK, RIPOSTE, BLOCK, ATTACK_HIGH, RIPOSTE_HIGH, BLOCK_LOW, GOT_HIT, FALL);
		curr.scale = scale;
		curr.skip(505);
		curr.addFrame(y, 170, frameDur * 2f);
		curr.last.setHittable(-60, bodyW);
		curr.addFrame(y, 171, frameDur * 2f);
		curr.last.setHittable(-60, bodyW);
		curr.addFrame(y, 169, frameDur * 2f);
		curr.last.setHittable(-60, bodyW);
		curr.skip(-171 -169); // * 2f);// - 1f);
		curr.addFrame(y, 171, frameDur * 2f);
		curr.last.setHittable(-60, bodyW);
		addAnim(curr, textureScale);
		
		
		//frameDur *= 3f;
		y = 296;
		y = sheetHeight - frameHeight;
		curr = new CharAnim(TEX, GOT_HIT, frameHeight);
		curr.interruptableBy(FALL);
		curr.scale = scale;
		curr.initialRelativeOffset = 0f;
		curr.skip(1015);
		// moveback ~54 total
		curr.addFrame(y, 148, frameDur * 2f);
		curr.last.setHittable(-50, bodyW);
		curr.last.soundIds.add(SOUND_RIP);
		//curr.last.move.x -= 14f;
		curr.addFrame(y, 144, frameDur * 1f);
		curr.last.setHittable(-40, bodyW);
		//curr.last.move.x -= 28f;
		curr.last.move.x -= 15f;
		curr.last.soundIds.add(SOUND_CLOTH);
		curr.addFrame(y, 159, frameDur * 1f);
		curr.last.setHittable(-30 - 14f, bodyW);
		curr.last.move.x += 14f;
		curr.moveToIdle.x -= 10f;
		addAnim(curr, textureScale);
		//frameDur /= 3f;
		
		
		//y = 752;
		//frameDur *= 3;
		y = sheetHeight - frameHeight * 2f;
		curr = new CharAnim(TEX, MOVE_FORWARD, frameHeight);
		curr.interruptableBy(ATTACK, RIPOSTE, BLOCK, ATTACK_HIGH, RIPOSTE_HIGH, BLOCK_LOW, GOT_HIT, FALL);
		curr.scale = scale;
		// total move should be >= 21 (which is MOVE_BACK's dist) 
		curr.addFrame(y, 166, frameDur);
		curr.last.setHittable(-60, bodyW);
		curr.last.move.x += 5f;
		curr.last.soundIds.add(SOUND_CLOTH);
		curr.addFrame(y, 167, frameDur);
		curr.last.setHittable(-54, bodyW);
		curr.last.move.x += 7f;
		curr.last.soundIds.add(SOUND_STEP);
		curr.addFrame(y, 164, frameDur);
		curr.last.move.x += 8f;
		curr.last.setHittable(-60, bodyW);
		curr.moveToIdle.x += 3f;
		addAnim(curr, textureScale);
		
		y = sheetHeight - frameHeight * 7f;
		curr = new CharAnim(TEX, MOVE_FORWARD_HIGH, frameHeight);
		curr.interruptableBy(ATTACK, RIPOSTE, BLOCK, ATTACK_HIGH, RIPOSTE_HIGH, BLOCK_LOW, GOT_HIT, FALL);
		curr.scale = scale; 

		curr.addFrame(y, 175, frameDur);
		curr.last.setHittable(-60, bodyW);
		curr.last.move.x += 5f;
		curr.last.soundIds.add(SOUND_CLOTH);
		curr.addFrame(y, 171, frameDur);
		curr.last.setHittable(-54, bodyW);
		curr.last.move.x += 7f;
		curr.last.soundIds.add(SOUND_STEP);
		curr.addFrame(y, 165, frameDur);
		curr.last.move.x += 8f;
		
		curr.last.setHittable(-60, bodyW);
		curr.moveToIdle.x += 3f;
		addAnim(curr, textureScale);
		
		
		//frameDur *= 3;
		y = sheetHeight - frameHeight * 2f;
		curr = new CharAnim(TEX, MOVE_BACK, frameHeight);
		curr.interruptableBy(ATTACK, RIPOSTE, BLOCK, ATTACK_HIGH, RIPOSTE_HIGH, BLOCK_LOW, GOT_HIT, FALL);
		curr.scale = scale;
		curr.skip(507);
		curr.addFrame(y, 181, frameDur);
		curr.last.setHittable(-60, bodyW);
		curr.last.move.x -= 11f;
		curr.last.soundIds.add(SOUND_CLOTH);
		curr.addFrame(y, 177, frameDur);
		curr.last.setHittable(-60, bodyW);
		curr.last.move.x -= 4f;
		curr.last.soundIds.add(SOUND_STEP);
		curr.addFrame(y, 172, frameDur);
		curr.last.move.x -= 3f;
		curr.last.setHittable(-60, bodyW);
		curr.moveToIdle.x -= 1f;
		
//		curr.skip(30);
//		curr.addFrame(y, 38, frameDur);
//		curr.last.setHittable(-12, 12);
//		curr.last.move.x -= 5f;
//		curr.skip(-(30 + 38));
//		curr.addFrame(y, 30, frameDur);
//		curr.last.setHittable(-12, 12);
		addAnim(curr, textureScale);
		
		
		
		// move back ~- 21 total
		y = sheetHeight - frameHeight * 7f;
		curr = new CharAnim(TEX, MOVE_BACK_HIGH, frameHeight);
		curr.interruptableBy(ATTACK, RIPOSTE, BLOCK, ATTACK_HIGH, RIPOSTE_HIGH, BLOCK_LOW, GOT_HIT, FALL);
		curr.scale = scale;
		curr.skip(513);
		
		curr.addFrame(y, 184, frameDur);
		curr.last.setHittable(-60, bodyW);
		curr.last.move.x -= 11f;
		curr.last.soundIds.add(SOUND_CLOTH);
		curr.addFrame(y, 179, frameDur);
		curr.last.setHittable(-60, bodyW);
		curr.last.move.x -= 4f;
		curr.last.soundIds.add(SOUND_STEP);
		curr.addFrame(y, 171, frameDur);
		curr.last.move.x -= 3f;
		curr.last.setHittable(-60, bodyW);
		curr.moveToIdle.x -= 1f;
		
		addAnim(curr, textureScale);
		
		
		
		// Block high
		//y = 752;
		//frameDur *= 5f;
		y = sheetHeight - frameHeight * 6f;
		curr = new CharAnim(TEX, BLOCK, frameHeight);
		curr.interruptableBy(GOT_HIT, FALL);
		curr.scale = scale;
		
		// need +26 movement
		//frameDur *= 3f;
		curr.addFrame(y, 165 , frameDur);// 40f/60f
		curr.last.setHittable(-50, bodyW);
		curr.last.setBlock(-30, 50);
		curr.last.move.x -= 10f;
		curr.last.soundIds.add(SOUND_CLOTH);
		
		curr.addFrame(y, 165, frameDur); // * 80f/60f
		curr.last.setHittable(-50, bodyW);
		curr.last.setBlock(-30, 50);
		curr.last.move.x -= 2f;
		
		
		curr.addFrame(y, 165, frameDur); // * 70f/60f
		curr.last.setHittable(-50, bodyW);
		curr.last.setBlock(-30, 50);
		curr.last.move.x += 5f;
		
		curr.addFrame(y, 165, frameDur); // * 50f/60f
		curr.last.setHittable(-50, bodyW);
		//curr.last.setBlock(-30, 70);
		curr.last.move.x += 12f;
		
		curr.moveToIdle.x += 15f;
		addAnim(curr, textureScale);
		
		
		// Block mid
		// total movement: +24px
		y = 709;
		y = sheetHeight - frameHeight * 6f;
		//frameDur *= 5f;
		curr = new CharAnim(TEX, BLOCK_LOW, frameHeight);
		curr.interruptableBy(GOT_HIT, FALL);
		curr.scale = scale;
		curr.skip(165*4);
		
		curr.addFrame(y, 165, frameDur); // 40f/60f);
		curr.last.setHittable(-50, bodyW);
		curr.last.setBlock(-30, 50);
		curr.last.move.x -= 4f;
		curr.last.soundIds.add(SOUND_CLOTH);
		//curr.last.move.x -= 5f;
		
		curr.addFrame(y, 165, frameDur); // 50f/60f);
		curr.last.setHittable(-55, bodyW);
		curr.last.setBlock(-30, 50);
		curr.last.move.x += 4f;
		
		curr.addFrame(y, 165, frameDur); // 100f/60f);
		curr.last.setHittable(-60, bodyW);
		curr.last.setBlock(-30, 50);
		curr.last.move.x += 7f;
		//curr.last.move.x -= 1f;
		
		curr.addFrame(y, 165, frameDur); // 50f/60f);
		curr.last.setHittable(-55, bodyW);
		//curr.last.setBlock(-30, 90);
		curr.last.move.x += 5f;
		curr.moveToIdle.x += 12f;
		addAnim(curr, textureScale);
		
		
		y = 804;
		y = sheetHeight - frameHeight * 3f;
		//frameDur *= 5f;
		// Total movement: 0px.
		curr = new CharAnim(TEX, ATTACK, frameHeight);
		curr.interruptableBy(GOT_HIT, FALL);
		curr.scale = scale;

		curr.addFrame(y, 118, frameDur);
		curr.last.setHittable(-40, bodyW);
		curr.last.move.x -= 20f;
		curr.last.soundIds.add(SOUND_CLOTH);
		
		curr.addFrame(y, 95, frameDur);
		curr.last.setHittable(-25, bodyW);
		curr.last.move.x -= 20f;
		
		curr.addFrame(y, 98, frameDur);
		curr.last.setHittable(-25, bodyW);
		curr.last.move.x += 10f;
		
		curr.addFrame(y, 113, frameDur);
		curr.last.setHittable(-30, bodyW);
		curr.last.move.x += 4f;
		curr.last.soundIds.add(SOUND_SWISH);
		curr.last.soundIds.add(SOUND_CLOTH);
		
		curr.addFrame(y, 176, frameDur);
		curr.last.setHittable(-40, bodyW);
		//curr.last.setAttack(8, 15);
		curr.last.move.x += 33f;
		
		curr.addFrame(y, 237, frameDur * 2f);
		curr.last.setHittable(-45, bodyW);
		//curr.last.setAttack(18, 15);
		curr.last.setAttack(35, 83);
		curr.last.move.x += 40f;
		curr.last.soundIds.add(SOUND_STEP);
		
		curr.addFrame(y, 200, frameDur);
		curr.last.setHittable(-30, bodyW);
		curr.last.move.x -= 25f;
		curr.last.soundIds.add(SOUND_CLOTH);
		
		curr.addFrame(y, 170, frameDur);
		curr.last.setHittable(-35, bodyW);
		curr.last.move.x -= 15f;
		
		curr.moveToIdle.x -= 7f;
		addAnim(curr, textureScale);
		
		y = 804;
		y = sheetHeight - frameHeight * 4f;

		//frameDur *= 5f;
		
		curr = new CharAnim(TEX, ATTACK_HIGH, frameHeight);
		curr.interruptableBy(GOT_HIT, FALL);
		curr.scale = scale;
		//curr.skip(2);
		//frameDur *= 3f;
		curr.addFrame(y, 124, frameDur);
		curr.last.setHittable(-40, bodyW);
		curr.last.move.x -= 20f;
		curr.last.soundIds.add(SOUND_CLOTH);
		
		curr.addFrame(y, 98, frameDur);
		curr.last.setHittable(-25, bodyW);
		curr.last.move.x -= 20f;
		
		curr.addFrame(y, 98, frameDur);
		curr.last.setHittable(-25, bodyW);
		curr.last.move.x += 10f;
		
		curr.addFrame(y, 121, frameDur);
		curr.last.setHittable(-30, bodyW);
		curr.last.move.x += 4f;
		curr.last.soundIds.add(SOUND_SWISH);
		curr.last.soundIds.add(SOUND_CLOTH);
		
		curr.addFrame(y, 161, frameDur);
		curr.last.setHittable(-40, bodyW);
		curr.last.move.x += 33f;
		
		curr.addFrame(y, 238, frameDur * 2f);
		curr.last.setHittable(-45, bodyW);
		//curr.last.setAttack(18, 15);
		curr.last.setAttack(35, 83);
		curr.last.move.x += 40f;
		curr.last.soundIds.add(SOUND_STEP);
		
		curr.addFrame(y, 210, frameDur);
		curr.last.setHittable(-30, bodyW);
		curr.last.move.x -= 25f;
		curr.last.soundIds.add(SOUND_CLOTH);
		
		curr.addFrame(y, 172, frameDur);
		curr.last.setHittable(-35, bodyW);
		curr.last.move.x -= 15f;
		curr.moveToIdle.x -= 7f;
		addAnim(curr, textureScale);
		
		
		curr = ANIMATIONS.get(ATTACK).clone();
		curr.action = RIPOSTE;
		curr.removeFirstFrame();
		curr.removeFirstFrame();
		curr.frames.get(0).move.x -= 28;
		curr.frames.get(curr.frames.size() - 1).move.x += 5;
		addAnim(curr, textureScale);
		
		curr = ANIMATIONS.get(ATTACK_HIGH).clone();
		curr.action = RIPOSTE_HIGH;
		curr.removeFirstFrame();
		curr.removeFirstFrame();
		//curr.frames.get(0).move.x -= 40;
		addAnim(curr, textureScale);
	
		// will need to be adjusted per frame; this is just for testing, split evenly between
		// the last 2 frames of recovery -am
		float recoveryMoveBack = 62f;
		// ah, right! attack recovery *needs* to be this much to counter the back movement of the last part of
		// the attack that got interrupted by the recovery
		//recoveryMoveBack = 30f;
		recoveryMoveBack = 23;
		
		// Attack recovery ... high?
		y = 752;
		y = sheetHeight - frameHeight * 5f;
		//frameDur *= 5f;
		curr = new CharAnim(TEX, ATTACK_HIGH_RECOVERY, frameHeight);
		curr.interruptableBy(GOT_HIT, FALL);
		curr.scale = scale;

		curr.addFrame(y, 192, frameDur);
		curr.last.setHittable(-45, bodyW);
		curr.last.move.x += 8f;
		curr.last.soundIds.add(SOUND_CLASH);
		
		curr.addFrame(y, 192, frameDur); // +16
		curr.last.setHittable(-45, bodyW);
		curr.last.move.x += -22f;// +8f; 
		curr.last.soundIds.add(SOUND_CLOTH);
		
		curr.addFrame(y, 168, frameDur); // +8
		curr.last.setHittable(-35, bodyW);
		curr.last.move.x += -14f;// +4f; 
		curr.last.soundIds.add(SOUND_STEP);
		
		curr.moveToIdle.x += -6f;
		addAnim(curr, textureScale);
		
		
		// Attack recovery mid?
		// total movement: -16
		
		y = 752;
		//y = sheetHeight - frameHeight * 7f;
		y = sheetHeight - frameHeight * 2f;
		//frameDur *= 5f; 
		curr = new CharAnim(TEX, ATTACK_RECOVERY, frameHeight);
		curr.interruptableBy(GOT_HIT, FALL);
		curr.scale = scale;

		curr.skip(1038);
		curr.addFrame(y, 190, frameDur);
		curr.last.setHittable(-45, bodyW);
		curr.last.move.x += 8f;
		curr.last.soundIds.add(SOUND_CLASH);

		curr.addFrame(y, 176, frameDur);
		curr.last.setHittable(-35, bodyW);
		curr.last.move.x += -22f; 
		curr.last.soundIds.add(SOUND_CLOTH);
		
		y = sheetHeight - frameHeight * 3f; 
		curr.widthSoFar = 1210;
		curr.addFrame(y, 168, frameDur);
		curr.last.setHittable(-11, bodyW);
		curr.last.move.x += -14f; 
		curr.last.soundIds.add(SOUND_STEP);
		
		curr.moveToIdle.x += -6f;


		addAnim(curr, textureScale);
		
		
		//frameDur *= 5f;
		y = 296;
		y = sheetHeight - frameHeight * 5f;
		//frameDur *= 5f;
		curr = new CharAnim(TEX, FALL, frameHeight);
		//curr.interruptableBy(GOT_HIT, FALL);
		curr.scale = scale;
		curr.skip(574);
		//curr.skip(172);
		curr.addFrame(y, 158, frameDur * 1f);
		curr.last.setHittable(-40, bodyW);
		curr.last.move.x -= 0f;
		curr.last.soundIds.add(SOUND_RIP);
		curr.addFrame(y, 144, frameDur * 1f);
		curr.last.setHittable(-40, bodyW);
		curr.last.move.x -= 45f;
		curr.last.soundIds.add(SOUND_CLOTH);
		curr.addFrame(y, 128, frameDur * 1f);
		curr.last.setHittable(-40, bodyW);
		curr.last.move.x -= 19f;
		curr.addFrame(y, 137, frameDur * 1f);
		curr.last.setHittable(-40, bodyW);
		curr.last.move.x -= 40f;
		curr.addFrame(y, 175, frameDur * 1f);
		curr.last.setHittable(-40, bodyW);
		curr.last.move.x -= 12f;
		curr.last.soundIds.add(SOUND_FALL);
		curr.addFrame(y, 174, frameDur * 1f);
		curr.last.setHittable(-40, bodyW);
		curr.last.move.x += 8f;

		curr.widthSoFar = 1049;
		
		y = sheetHeight - frameHeight * 7f;
		curr.addFrame(y, 178, 100000f);
		//curr.last.setHittable(-40, bodyW); non-hittable - no shadow, but otherwise you could still, ah, hit them
		curr.last.move.x += 2f;
		curr.last.move.y += 1f;
		
//		curr.addFrame(y, 34, 100000f);
		//curr.last.move.x -= 8f;
		curr.moveToIdle.x += 10f;
		addAnim(curr, textureScale);
	}
	
	public static void addAnim(CharAnim anim, float textureScale) {
		anim.updateTextureScale(textureScale);
		ANIMATIONS.put(anim.action, anim);
	}
}

