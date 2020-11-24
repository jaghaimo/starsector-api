package com.fs.starfarer.api.impl.campaign.procgen;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.util.Misc;

public class StarGenDataSpec {

	
	//id,starAge,freqYOUNG,freqAVERAGE,freqOLD,
	//minRadius,maxRadius,coronaMin,coronaMult,coronaVar,
	//solarWind,minFlare,maxFlare,crLossMult,lightColorMin,lightColorMax
	
	private String id;
	private float minRadius, maxRadius, coronaMin, coronaMult,
				  coronaVar, solarWind, minFlare, maxFlare, crLossMult,
				  freqYOUNG, freqAVERAGE, freqOLD;
	private float probOrbits, minOrbits, maxOrbits, habZoneStart;
	private Color lightColorMin;
	private Color lightColorMax;
	private StarAge age;
	
	private Set<String> tags = new HashSet<String>();
	
	
	public StarGenDataSpec(JSONObject row) throws JSONException {
		id = row.getString("id");
		minRadius = (float) row.getDouble("minRadius");
		maxRadius = (float) row.getDouble("maxRadius");
		
		coronaMin = (float) row.getDouble("coronaMin");
		coronaMult = (float) row.getDouble("coronaMult");
		coronaVar = (float) row.getDouble("coronaVar");
		
		solarWind = (float) row.getDouble("solarWind");
		minFlare = (float) row.getDouble("minFlare");
		maxFlare = (float) row.getDouble("maxFlare");
		crLossMult = (float) row.getDouble("crLossMult");
		
		freqYOUNG = (float) row.getDouble("freqYOUNG");
		freqAVERAGE = (float) row.getDouble("freqAVERAGE");
		freqOLD = (float) row.getDouble("freqOLD");
		
		probOrbits = (float) row.optDouble("probOrbits", 0);
		minOrbits = (float) row.optDouble("minOrbits", 0);
		maxOrbits = (float) row.optDouble("maxOrbits", 0);
		
		habZoneStart = (float) row.optDouble("habZoneStart", 2);
		
		age = Misc.mapToEnum(row, "age", StarAge.class, StarAge.ANY);
		
		lightColorMin = parseColor(row.optString("lightColorMin", null) + " 255", " ");
		lightColorMax = parseColor(row.optString("lightColorMax", null) + " 255", " ");
		
		String tags = row.optString("tags", null);
		if (tags != null) {
			String [] split = tags.split(",");
			for (String tag : split) {
				tag = tag.trim();
				if (tag.isEmpty()) continue;
				addTag(tag);
			}
		}
	}

	public static Color parseColor(String str, String sep) {
		if (str == null) return Color.white;
		
		String [] parts = str.split(sep);
		if (parts.length != 4) return Color.white;
		
		return new Color(Integer.parseInt(parts[0].trim()),
						 Integer.parseInt(parts[1].trim()),
						 Integer.parseInt(parts[2].trim()),
						 Integer.parseInt(parts[3].trim()));
	}

	public String getId() {
		return id;
	}


	public float getMinRadius() {
		return minRadius;
	}


	public float getMaxRadius() {
		return maxRadius;
	}


	public float getCoronaMin() {
		return coronaMin;
	}


	public float getCoronaMult() {
		return coronaMult;
	}


	public float getCoronaVar() {
		return coronaVar;
	}


	public float getSolarWind() {
		return solarWind;
	}


	public float getMinFlare() {
		return minFlare;
	}


	public float getMaxFlare() {
		return maxFlare;
	}


	public float getCrLossMult() {
		return crLossMult;
	}


	public Color getLightColorMin() {
		return lightColorMin;
	}


	public Color getLightColorMax() {
		return lightColorMax;
	}

	public float getFreqYOUNG() {
		return freqYOUNG;
	}

	public float getFreqAVERAGE() {
		return freqAVERAGE;
	}

	public float getFreqOLD() {
		return freqOLD;
	}

	public StarAge getAge() {
		return age;
	}

	public float getProbOrbits() {
		return probOrbits;
	}

	public float getMinOrbits() {
		return minOrbits;
	}

	public float getMaxOrbits() {
		return maxOrbits;
	}

	public float getHabZoneStart() {
		return habZoneStart;
	}
	
	public Set<String> getTags() {
		return tags;
	}
	
	public void addTag(String tag) {
		tags.add(tag);
	}

	public boolean hasTag(String tag) {
		return tags.contains(tag);
	}
}





