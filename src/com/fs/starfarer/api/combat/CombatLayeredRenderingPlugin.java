package com.fs.starfarer.api.combat;

import java.util.EnumSet;

public interface CombatLayeredRenderingPlugin {
	public void init();
	public void cleanup();
	public boolean isExpired();
	
	public void advance(float amount);
	public EnumSet<CombatEngineLayers> getActiveLayers();
	public float getRenderRadius();
	public void render(CombatEngineLayers layer, ViewportAPI viewport);
}
