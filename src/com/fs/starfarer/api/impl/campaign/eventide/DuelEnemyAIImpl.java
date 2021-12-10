package com.fs.starfarer.api.impl.campaign.eventide;

public class DuelEnemyAIImpl implements DuelEnemyAI {

	public static class WaitThenAct {
		float delay;
		String actionId;
		public WaitThenAct(float delay, String actionId) {
			this.delay = delay;
			this.actionId = actionId;
		}
	}
	
	
	public float initialDelay = 1f;
	public float moveDelay = 0f;
	public WaitThenAct next;
	public float recentBlocks = 0;
	public float noBlockDur = 0f;
	
	public DuelEnemyAIImpl() {
		
	}
	
	
	public void advance(float amount, DuelPanel duel) {
		Actor actor = duel.enemy;
		Actor opponent = duel.player;
		
		if (opponent.health <= 0) return;
		
		initialDelay -= amount;
		if (initialDelay > 0) return;
		
		recentBlocks -= amount * 0.25f;
		if (recentBlocks < 0) recentBlocks = 0;
		
		moveDelay -= amount;
		if (moveDelay < 0) moveDelay = 0;
		noBlockDur -= amount;
		if (noBlockDur < 0) noBlockDur = 0;
		
		if (next != null) {
			next.delay -= amount;
			if (next.delay <= 0) {
				actor.doAction(next.actionId, false);
				next = null;
			}
			return;
		}
	
		//float attackRange = 165;
		float attackRange = 142;
		float tooCloseRange = 50;
		float waitRange = attackRange + 50;
		
		float dist = duel.getDistance();
		
		boolean actorAttacking = actor.currAction != null && actor.currAction.anim.hasAttackFrames() &&
							!actor.currAction.scoredHit && !actor.currAction.wasBlocked;  
		boolean actorBlocking = actor.currAction != null && actor.currAction.anim.hasBlockFrames() &&
							!actor.currAction.performedBlock;
		if (actor.nextAction != null && actor.nextAction.anim.hasBlockFrames()) {
			actorBlocking = true;
		}
		
		boolean actorPerformedBlock = actor.currAction != null && actor.currAction.performedBlock;  
		boolean opponentAttacking = opponent.currAction != null && opponent.currAction.anim.hasAttackFrames() &&
							!opponent.currAction.scoredHit && !opponent.currAction.wasBlocked;  
		boolean opponentBlocking = opponent.currAction != null && opponent.currAction.anim.hasBlockFrames() &&
							!opponent.currAction.performedBlock;
		
		if (actorBlocking || actorAttacking) return;
		
		boolean blockOnlyMode = false;
		boolean allowAttack = true;
	//	blockOnlyMode = true;
	//	allowAttack = false;
		
		if (opponentAttacking && dist < attackRange + 10 && opponent.currAction.framesUntilAttackFrame() < 4) {
			if (noBlockDur > 0) return;
			
			if (1f + (float) Math.random() * 4f < recentBlocks) {
				noBlockDur = 0.5f + (float) Math.random() * 0.2f;
				if (!blockOnlyMode) {
					actor.doAction(Actions.MOVE_BACK, false);
					next = new WaitThenAct(0.1f, Actions.MOVE_BACK);
				}
				return;
			}
			//next = new WaitThenAct(0.1f + (float) Math.random() * 0.1f, Actions.BLOCK_LOW);
			float delay = 0.2f * (float) Math.random();
			//delay = 0f;
			//delay += (float) Math.random() * Math.min(0.2f, recentBlocks * 0.1f);
			next = new WaitThenAct(delay, Actions.BLOCK);
			recentBlocks++;
			if (blockOnlyMode) recentBlocks = 0;
			if (recentBlocks > 4f) recentBlocks = 4f;
			return;
		}
		
		if (opponentAttacking) {
			return;
		}
		
		if (actorPerformedBlock) {
			moveDelay = 0.1f + (float) Math.random() * 0.05f;
			//moveDelay = 0.1f;
			//moveDelay = 0f;
			return;
		}
		
		if (moveDelay <= 0 && !blockOnlyMode) {
			if (dist > waitRange) {
				actor.doAction(Actions.MOVE_FORWARD, false);
				moveDelay = 0.3f + (float) Math.random() * 0.2f;
				return;
			} else if (dist > attackRange) {
				actor.doAction(Actions.MOVE_FORWARD, false);
				moveDelay = 0.3f + (float) Math.random() * 0.2f;
				return;
			}
		}
		
		if ((dist < attackRange || actorPerformedBlock) && moveDelay <= 0 && (!blockOnlyMode || allowAttack)) {
			actor.doAction(Actions.ATTACK, false);
			moveDelay = 0.1f + (float) Math.random() * 2f;
			//next = new WaitThenAct((float) Math.random() * 0.1f, Actions.ATTACK_HIGH);
			return;
		}
		
		//if (true) return;
//		if (dist < tooCloseRange && !opponentAttacking) {
//			
//		}
		
		
//		if (opponentAttacking && !actorBlocking) {
//			actor.doAction(Actions.BLOCK_LOW, false);
//			//moveDelay = 0.4f;
//		} else {

//		else if (moveDelay <= 0) {
//			actor.doAction(Actions.ATTACK_HIGH, false);
//			moveDelay = 0.5f;
//		}
		
		//System.out.println("Dist: " + dist);
		
	}
	
	public void render(float alphaMult) {
		
	}
}
