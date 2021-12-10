package com.fs.starfarer.api.impl.campaign.eventide;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate.DialogCallbacks;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class DuelTutorialPanel implements CustomUIPanelPlugin {

	public static enum TutStage {
		INIT,
		MOVE_FORWARD,
		MOVE_BACK,
		ATTACK,
		BLOCK,
		LEAVE;
		
		private static TutStage [] vals = values();
		public TutStage next() {
			int index = this.ordinal() + 1;
			if (index >= vals.length) index = vals.length - 1;
			return vals[index];
		}
	}
	
	protected InteractionDialogAPI dialog;
	protected DialogCallbacks callbacks;
	protected CustomPanelAPI panel;
	protected PositionAPI p;
	
	protected TutStage curr = null;
	protected TooltipMakerAPI info;
	protected float untilNext = 0f;
	protected boolean triggeredNext = false;
	protected FaderUtil flash = new FaderUtil(0f, 0.125f, 0.5f, false, true);
	
	public DuelTutorialPanel() {
	}
	
	public void init(CustomPanelAPI panel, DialogCallbacks callbacks, InteractionDialogAPI dialog) {
		this.panel = panel;
		this.callbacks = callbacks;
		this.dialog = dialog;
		curr = TutStage.INIT;
		showNext();
	}
	
	public void showNext() {
		if (curr == TutStage.LEAVE) return;
		curr = curr.next();
		if (info != null) {
			panel.removeComponent(info);
		}
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		info = panel.createUIElement(p.getWidth() - 20f, 1000f, false);
		info.setParaInsigniaLarge();
		
		if (curr == TutStage.MOVE_FORWARD) {
			info.addPara("Press RIGHT to move forward.", 0f, h, "RIGHT");
		} else if (curr == TutStage.MOVE_BACK) {
			info.addPara("Press LEFT to move backwards.", 0f, h, "LEFT");
		} else if (curr == TutStage.ATTACK) {
			info.addPara("Press SPACE to attack.", 0f, h, "SPACE");
		} else if (curr == TutStage.BLOCK) {
			info.addPara("Press UP to block or parry. "
					+ "A skilled fighter can also execute a quick attack, or a \"riposte\", by attacking immediately after deflecting their opponent's attack.", 0f, h, "UP");
		} else if (curr == TutStage.LEAVE) {
			info.addPara("Your health is in the top left of the screen.\n\n"
					+ "Make a few practice moves, then press ESCAPE to continue.", 0f, h, "ESCAPE");
		}
		
		panel.addUIElement(info).inTL(opad, opad);
		flash.fadeIn();
	}
	
	public void reportAction(String actionId) {
		boolean triggered = false;
		triggered |= curr == TutStage.MOVE_FORWARD && Actions.MOVE_FORWARD.equals(actionId);
		triggered |= curr == TutStage.MOVE_BACK && Actions.MOVE_BACK.equals(actionId);
		triggered |= curr == TutStage.ATTACK && Actions.ATTACK.equals(actionId);
		triggered |= curr == TutStage.BLOCK && Actions.BLOCK.equals(actionId);
		if (triggered) {
			triggeredNext = true;
			untilNext = 1f;
		}
	}

	public CustomPanelAPI getPanel() {
		return panel;
	}

	public PositionAPI getPosition() {
		return p;
	}
	
	public void positionChanged(PositionAPI position) {
		this.p = position;
	}

	public void render(float alphaMult) {
		
	}

	public void renderBelow(float alphaMult) {
		if (p == null) return;
		float x = p.getX();
		float y = p.getY();
		float cx = p.getCenterX();
		float cy = p.getCenterY();
		float w = p.getWidth();
		float h = p.getHeight();

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		Color c = Misc.getBasePlayerColor();
		float a = alphaMult;
		Misc.renderQuad(x, y, w, 1, c, a);
		Misc.renderQuad(x, y + h - 1, w, 1, c, a);
		Misc.renderQuad(x, y + 1, 1, h - 2, c, a);
		Misc.renderQuad(x + w - 1, y + 1, 1, h - 2, c, a);
		
		Misc.renderQuad(x + w, y - 1, 1, h, Color.black, a);
		Misc.renderQuad(x + 1, y - 1, w - 1, 1, Color.black, a);
		
		Misc.renderQuad(x + 1, y + 1, w - 2, h - 2, Color.black, a * 0.67f);
		
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		Misc.renderQuad(x + 1, y + 1, w - 2, h - 2, Misc.getBrightPlayerColor(), a * 0.25f * flash.getBrightness());
	}
	

	public void advance(float amount) {
		if (p == null) return;
		if (triggeredNext) {
			untilNext -= amount;
			if (untilNext <= 0f) {
				triggeredNext = false;
				showNext();
			}
		}
		flash.advance(amount);
	}
	
	public void processInput(List<InputEventAPI> events) {
		if (p == null) return;

	}
	
}



