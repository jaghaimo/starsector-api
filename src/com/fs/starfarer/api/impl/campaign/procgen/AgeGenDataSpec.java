package com.fs.starfarer.api.impl.campaign.procgen;

import java.awt.Color;

import org.json.JSONException;
import org.json.JSONObject;

public class AgeGenDataSpec {

	
	//id,minExtraOrbits,maxExtraOrbits,probNebula,freqNormal,freqBinary,freqTrinary
	
	private String id;
	private float minExtraOrbits, maxExtraOrbits, probNebula, freqNormal, freqBinary, freqTrinary, nebulaDensity;

	public AgeGenDataSpec(JSONObject row) throws JSONException {
		id = row.getString("id");
		minExtraOrbits = (float) row.optDouble("minExtraOrbits", 0);
		maxExtraOrbits = (float) row.optDouble("maxExtraOrbits", 0);
		
		probNebula = (float) row.optDouble("probNebula", 0);
		freqNormal = (float) row.optDouble("freqNormal", 0);
		freqBinary = (float) row.optDouble("freqBinary", 0);
		freqTrinary = (float) row.optDouble("freqTrinary", 0);
		nebulaDensity = (float) row.optDouble("nebulaDensity", 0.6f);
	}

	public static Color parseColor(String str, String sep) {
		if (str == null) return Color.white;
		
		String [] parts = str.split(sep);
		if (parts.length != 4) return null;
		
		return new Color(Integer.parseInt(parts[0].trim()),
						 Integer.parseInt(parts[1].trim()),
						 Integer.parseInt(parts[2].trim()),
						 Integer.parseInt(parts[3].trim()));
	}

	public String getId() {
		return id;
	}

	public float getMinExtraOrbits() {
		return minExtraOrbits;
	}

	public float getMaxExtraOrbits() {
		return maxExtraOrbits;
	}

	public float getProbNebula() {
		return probNebula;
	}

	public float getFreqNormal() {
		return freqNormal;
	}

	public float getFreqBinary() {
		return freqBinary;
	}

	public float getFreqTrinary() {
		return freqTrinary;
	}

	public float getNebulaDensity() {
		return nebulaDensity;
	}
}





