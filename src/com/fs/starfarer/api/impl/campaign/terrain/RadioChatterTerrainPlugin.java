package com.fs.starfarer.api.impl.campaign.terrain;

import java.util.EnumSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.util.Misc;

public class RadioChatterTerrainPlugin extends BaseRingTerrain {
	
	public static class RadioChatterParams extends RingParams {
		public RadioChatterParams(float bandWidthInEngine, float middleRadius, SectorEntityToken relatedEntity) {
			super(bandWidthInEngine, middleRadius, relatedEntity);
		}
	}
	
	protected RadioChatterParams params;

	
	public void init(String terrainId, SectorEntityToken entity, Object param) {
		super.init(terrainId, entity, param);
		this.params = (RadioChatterParams) param;
	}
	
	@Override
	protected Object readResolve() {
		super.readResolve();
		layers = EnumSet.noneOf(CampaignEngineLayers.class);
		return this;
	}
	
	Object writeReplace() {
		return this;
	}
	
	transient private EnumSet<CampaignEngineLayers> layers = EnumSet.noneOf(CampaignEngineLayers.class);
	public EnumSet<CampaignEngineLayers> getActiveLayers() {
		return layers;
	}


	
	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		
	}
	
	protected transient float phase = 0f;
	public void advance(float amount) {
		super.advance(amount);
		
		float period = (float)Math.PI * 2f;
		phase += period/10f * amount;
	}
	@Override
	public void applyEffect(SectorEntityToken entity, float days) {
		if (!entity.isPlayerFleet()) return;
		
		float prox = getProximitySoundFactor();
		float volumeMult = prox;
		float suppressionMult = prox;
		if (volumeMult <= 0) return;
		volumeMult = (float) Math.sqrt(volumeMult);
		//volumeMult = 1f;
		
		Global.getSector().getCampaignUI().suppressMusic(getSpec().getMusicSuppression() * suppressionMult);
		
		float dirToEntity = Misc.getAngleInDegrees(entity.getLocation(), this.entity.getLocation());
		Vector2f playbackLoc = Misc.getUnitVectorAtDegreeAngle(dirToEntity);
		playbackLoc.scale(500f);
		Vector2f.add(entity.getLocation(), playbackLoc, playbackLoc);
		
		try {
			JSONArray sounds = getSpec().getCustom().getJSONArray("chatter");
			float num = sounds.length();
			float period = (float)Math.PI * 2f;
			
//			float marketSize = 10f;
//			if (params.relatedEntity != null && params.relatedEntity.getMarket() != null) {
//				marketSize = params.relatedEntity.getMarket().getSize();
//			}
			
			//{"sound":"terrain_radio_chatter_1", "phaseMult":1, "threshold":-1},
			for (int i = 0; i < sounds.length(); i++) {
				JSONObject sound = sounds.getJSONObject(i);
				float threshold = (float) sound.getDouble("threshold");
				float phaseMult = (float) sound.getDouble("phaseMult");
				String soundId = sound.getString("sound");
				
//				float thresholdRange = 1f - threshold;
//				float sizePenalty = Math.max(6f - marketSize, 0) / 6f * thresholdRange;
//				threshold += sizePenalty;
				
				//phaseMult = 1f;
				float offset = period / num * (float) i;
				//float cos = (float) Math.cos((phase + offset) * ((float) i * 0.5f + 1f));
				float cos = (float) Math.cos(phase * phaseMult + offset);
				
				float volume = 0f;
				if (cos > threshold) {
					volume = (cos - threshold) / (1f - threshold);
					if (volume < 0) volume = 0;
					if (volume > 1) volume = 1;
				}
				
				
				Global.getSoundPlayer().playLoop(soundId, params.relatedEntity, 
						//volume * 0.25f + 0.75f, volume * volumeMult,
						1f, volume * volumeMult,
						playbackLoc, Misc.ZERO);
			}
			
			
		} catch (JSONException e) {
		}
	}
	
	
	@Override
	public float getProximitySoundFactor() {
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		float dist = Misc.getDistance(player.getLocation(), entity.getLocation());
		float radSum = params.relatedEntity.getRadius() + player.getRadius();
		
		//if (dist < radSum) return 1f;
		dist -= radSum;
		
		float f = 1f - dist / Math.max(1, (params.bandWidthInEngine - params.relatedEntity.getRadius()));
		if (f < 0) f = 0;
		if (f > 1) f = 1;
		return f;
	}

	@Override
	protected float getExtraSoundRadius() {
		return 0f;
	}

	@Override
	public boolean containsPoint(Vector2f point, float radius) {
		return super.containsPoint(point, radius);
	}

	public boolean hasTooltip() {
		return false;
	}
	
	
	public String getTerrainName() {
		return null;
	}
	
	public String getEffectCategory() {
		return "radio_chatter";
	}

	public boolean canPlayerHoldStationIn() {
		return false;
	}
}






