package com.fs.starfarer.api.impl.campaign;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

public class ExampleCustomEntityPlugin extends BaseCustomEntityPlugin {

	//private SectorEntityToken entity;
	
	transient private SpriteAPI sprite; // needs to be transient - can't save sprites
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		//this.entity = entity;
		readResolve();
	}
	
	// this methods gets called after the object is loaded from a savefile
	// init the sprite here
	Object readResolve() {
		sprite = Global.getSettings().getSprite("misc", "wormhole_ring");
		return this;
	}
	
	public void advance(float amount) {
		
	}

	public float getRenderRange() {
		return entity.getRadius() + 100f;
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		
		float alphaMult = viewport.getAlphaMult();
		Vector2f loc = entity.getLocation();
		sprite.setSize(128, 128);
		sprite.setAlphaMult(alphaMult);
		sprite.setAdditiveBlend();
		sprite.renderAtCenter(loc.x, loc.y);
	}
	

}



