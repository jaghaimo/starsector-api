package com.fs.starfarer.api.impl.campaign.rulecmd.missions;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.intel.misc.LuddicShrineIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * ShrineCMD <command> <parameters> 
 * 
 * <optional id - either market, or entity>
 */
public class ShrineCMD extends BaseCommandPlugin {

	
	public static class ShrineMusicStopFailsafe implements EveryFrameScript {
		public boolean done = false;
		public String musicId;
		
		public ShrineMusicStopFailsafe(String musicId) {
			this.musicId = musicId;
		}
		public void advance(float amount) {
			if (!Global.getSector().isPaused()) {
				if (musicId.equals(Global.getSoundPlayer().getCurrentMusicId())) {
					Global.getSoundPlayer().restartCurrentMusic();
				}
				done = true;
			}
		}
		public boolean isDone() {
			return done;
		}
		public boolean runWhilePaused() {
			return false;
		}
	}
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		SectorEntityToken entity = dialog.getInteractionTarget();
		if (entity.getMarket() != null && !entity.getMarket().isPlanetConditionMarketOnly()) {
			PlanetAPI planet = entity.getMarket().getPlanetEntity();
			if (planet != null) {
				entity = planet;
			}
		}
		if ("addIntel".equals(command)) {
			if (params.size() > 1) {
				String id = params.get(1).getString(memoryMap);
				entity = LuddicShrineIntel.getEntity(id);
			}
			LuddicShrineIntel.addShrineIntelIfNeeded(entity, dialog.getTextPanel());
			return true;
		} else if ("setVisited".equals(command)) {
			if (params.size() > 1) {
				String id = params.get(1).getString(memoryMap);
				entity = LuddicShrineIntel.getEntity(id);
			}
			LuddicShrineIntel.addShrineIntelIfNeeded(entity, dialog.getTextPanel(), true);
			LuddicShrineIntel.setVisited(entity, dialog.getTextPanel());
			return true;	
		} else if ("playMusic".equals(command)) {
			String shrineMusic = "music_luddite_shrine";
			Global.getSoundPlayer().playCustomMusic(1, 1, shrineMusic, true);
			// failsafe is not actually necessary; leaving the entity will give the music player a kick anyway
//			if (!Global.getSector().hasScript(ShrineMusicStopFailsafe.class)) {
//				Global.getSector().addScript(new ShrineMusicStopFailsafe(shrineMusic));
//			}
		} else if ("endMusic".equals(command)) {
			Global.getSoundPlayer().restartCurrentMusic();
		}

		return false;
	}
}










