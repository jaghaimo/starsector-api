/**
 * 
 */
package com.fs.starfarer.api.campaign.econ;

public enum PriceVariability {
	V0(0), // price holds steady regardless of demand
	V1(0.4f),
	//V1(0.6f),
	V2(0.8f),
	V3(1.2f),
	V4(1.6f), // "normal"
	V5(2.0f),
	V6(2.4f),
	V7(2.8f),
	V8(3.2f),
	V9(3.6f),
	V10(4.0f),
	;
	public float v;
	private PriceVariability(float v) {
		this.v = v;
	}
}