package com.fs.starfarer.api.impl.codex;

import java.util.List;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;

public class CodexCustomEntryExample extends CodexEntryV2 implements CustomUIPanelPlugin {

	protected CustomPanelAPI panel;
	protected UIPanelAPI relatedEntries;
	protected UIPanelAPI box;
	protected CodexDialogAPI codex;

	public CodexCustomEntryExample(String id, String title, String icon) {
		super(id, title, icon);
	}

	@Override
	public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
		super.createTitleForList(info, width, mode);
	}

	@Override
	public boolean hasCustomDetailPanel() {
		return true;
	}

	@Override
	public CustomUIPanelPlugin getCustomPanelPlugin() {
		return this;
	}
	
	@Override
	public void destroyCustomDetail() {
		panel = null;
		relatedEntries = null;
		box = null;
		codex = null;
	}

	@Override
	public void createCustomDetail(CustomPanelAPI panel, UIPanelAPI relatedEntries, CodexDialogAPI codex) {
		this.panel = panel;
		this.relatedEntries = relatedEntries;
		this.codex = codex;
		
		Color color = Misc.getBasePlayerColor();
		Color dark = Misc.getDarkPlayerColor();
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float opad = 10f;
		float pad = 3f;
		float small = 5f;
		
		float width = panel.getPosition().getWidth();
		
		float initPad = 0f;
		
		float horzBoxPad = 30f;
		
		// the right width for a tooltip wrapped in a box to fit next to relatedEntries
		// 290 is the width of the related entries widget, but it may be null
		float tw = width - 290f - opad - horzBoxPad + 10f;
		
		TooltipMakerAPI text = panel.createUIElement(tw, 0, false);
		text.setParaSmallInsignia();
		
		String design = "Cicero";
		if (design != null && !design.toLowerCase().equals("common")) {
			text.setParaFontDefault();
			Misc.addDesignTypePara(text, design, initPad);
			text.setParaSmallInsignia();
			initPad = opad;
		}
		
		text.addPara("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt "
				+ "ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation "
				+ "ullamco laboris nisi ut aliquip ex ea commodo consequat.", initPad);
		
		// add a bunch of paragraphs so that it requires a scroller
		for (int i = 0; i < 10; i++) {
			text.addPara("Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat "
					+ "nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia "
					+ "deserunt mollit anim id est laborum.", opad); 
		}
		
		panel.updateUIElementSizeAndMakeItProcessInput(text);
		
		box = panel.wrapTooltipWithBox(text);
		panel.addComponent(box).inTL(0f, 0f);
		if (relatedEntries != null) {
			panel.addComponent(relatedEntries).inTR(0f, 0f);
		}
		
		float height = box.getPosition().getHeight();
		if (relatedEntries != null) {
			height = Math.max(height, relatedEntries.getPosition().getHeight());
		}
		panel.getPosition().setSize(width, height);
	}
	
	@Override
	public void positionChanged(PositionAPI position) {
		
	}

	@Override
	public void renderBelow(float alphaMult) {
		// just rendering something to show how one might do it
		PositionAPI p = relatedEntries.getPosition();
		float x = p.getX();
		float y = p.getY();
		float w = p.getWidth();
		float h = p.getHeight();
		Color color = Misc.getDarkPlayerColor();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Misc.renderQuad(x, y - 110f, w, 100f, color, alphaMult);
	}

	@Override
	public void render(float alphaMult) {
		
	}

	@Override
	public void advance(float amount) {
		
	}

	@Override
	public void processInput(List<InputEventAPI> events) {
		
	}

	@Override
	public void buttonPressed(Object buttonId) {
		
	}

	
	
}
