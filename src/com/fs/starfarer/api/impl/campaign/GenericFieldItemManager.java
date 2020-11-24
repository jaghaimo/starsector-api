package com.fs.starfarer.api.impl.campaign;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;

public class GenericFieldItemManager {
	
	protected transient List<GenericFieldItemSprite> items;
	protected transient boolean inited = false;

	protected SectorEntityToken entity;
	
	public int numPieces, cellSize;
	public float minSize, maxSize;
	public String category, key;
	
	public GenericFieldItemManager(SectorEntityToken entity) {
		this.entity = entity;
	}

	Object readResolve() {
		return this;
	}
	
	public void advance(float amount) {
		if (amount <= 0) {
			return; // happens during game load
		}
		if (!entity.isInCurrentLocation()) {
			items = null;
			inited = false;
			return;
		}
		
		float days = Global.getSector().getClock().convertToDays(amount);
		initDebrisIfNeeded();
		
		List<GenericFieldItemSprite> remove = new ArrayList<GenericFieldItemSprite>();
		for (GenericFieldItemSprite item : items) {
			item.advance(days);
			if (item.isDone()) {
				remove.add(item);
			}
		}
		items.removeAll(remove);
		
		addPiecesToMax();
	}
		
	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		float alphaMult = viewport.getAlphaMult();
		alphaMult *= entity.getSensorFaderBrightness();
		alphaMult *= entity.getSensorContactFaderBrightness();
		if (alphaMult <= 0) return;

		
		GL11.glPushMatrix();
		GL11.glTranslatef(entity.getLocation().x, entity.getLocation().y, 0);
		
		initDebrisIfNeeded();
		for (GenericFieldItemSprite item : items) {
			item.render(alphaMult);
		}
		
		GL11.glPopMatrix();
	}
	
	protected void addPiecesToMax() {
		while (items.size() < numPieces) {
			float size = minSize + (maxSize - minSize) * (float) Math.random();
			GenericFieldItemSprite item = new GenericFieldItemSprite(entity, category, key, cellSize, size, 
									entity.getRadius() * 0.75f);
			items.add(item);
		}
	}
	
	protected void initDebrisIfNeeded() {
		if (inited) return;
		inited = true;
		
		items = new ArrayList<GenericFieldItemSprite>();
		addPiecesToMax();
		for (GenericFieldItemSprite piece : items) {
			piece.advance(0.1f);
		}
	}
	
	
	
}
