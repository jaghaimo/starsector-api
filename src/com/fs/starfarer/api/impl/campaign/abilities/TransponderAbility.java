package com.fs.starfarer.api.impl.campaign.abilities;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class TransponderAbility extends BaseToggleAbility {

	public static final float DETECTABILITY_MULT = 1f;
	public static final float DETECTABILITY_FLAT = 1000f;
	
//	public String getSpriteName() {
//		return Global.getSettings().getSpriteName("abilities", Abilities.TRANSPONDER);
//	}
	
	
	@Override
	protected String getActivationText() {
		return "Transponder on";
	}



	@Override
	protected String getDeactivationText() {
		//return "Transponder off";
		// currently, doesn't work well - if something like "go dark" cancels transponder,
		// then you get multiple overlapping messages, "Going dark" + "Transponder off"
		return null;
	}


	@Override
	protected void activateImpl() {
		if (entity.isPlayerFleet()) {
			List<FactionAPI> factions = getFactionsThatWouldBecomeHostile();
			if (!factions.isEmpty()) {
				Global.getSector().getCampaignUI().addMessage("Your identity is revealed and linked to your recent hostile actions!");
				for (FactionAPI faction : factions) {
					Global.getSector().adjustPlayerReputation(RepActions.COMBAT_NORMAL, faction.getId());
				}
			}
		}
		
		entity.setTransponderOn(true);
		entity.getMemoryWithoutUpdate().set(MemFlags.JUST_TOGGLED_TRANSPONDER, true, 0.1f);
		
//		AbilityPlugin goDark = entity.getAbility(Abilities.GO_DARK);
//		if (goDark != null && goDark.isActive()) {
//			goDark.deactivate();
//		}
	}

	@Override
	protected void applyEffect(float amount, float level) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		if (level > 0) level = 1;
		fleet.getStats().getDetectedRangeMod().modifyMult(getModId(), 1f + (DETECTABILITY_MULT - 1f) * level, "Transponder on");
		fleet.getStats().getDetectedRangeMod().modifyFlat(getModId(), DETECTABILITY_FLAT * level, "Transponder on");
		
		if (level <= 0) {
			cleanupImpl();
		}
	}
	
	
	@Override
	public void deactivate() {
		super.deactivate();
		if (entity.isTransponderOn()) {
			entity.setTransponderOn(false); // failsafe in case deactivation failed to actually deactivate transponder
			entity.getMemoryWithoutUpdate().set(MemFlags.JUST_TOGGLED_TRANSPONDER, true, 0.1f);
		}
	}



	@Override
	protected void deactivateImpl() {
		entity.setTransponderOn(false);
		entity.getMemoryWithoutUpdate().set(MemFlags.JUST_TOGGLED_TRANSPONDER, true, 0.1f);
		//cleanupImpl();
	}
	

//	@Override
//	public float getActivationDays() {
//		return 0.0f;
//	}
//
//	@Override
//	public float getDeactivationDays() {
//		return 0.1f;
//	}

	@Override
	protected void cleanupImpl() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		fleet.getStats().getDetectedRangeMod().unmodify(getModId());
	}
	
	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color red = Misc.getNegativeHighlightColor();
		
		String status = " (off)";
		if (entity.isTransponderOn()) {
			status = " (on)";
		}
		
		if (!Global.CODEX_TOOLTIP_MODE) {
			LabelAPI title = tooltip.addTitle(spec.getName() + status);
			title.highlightLast(status);
			title.setHighlightColor(gray);
		} else {
			tooltip.addSpacer(-10f);
		}

		float pad = 10f;
		tooltip.addPara("Transponders transmit identifying information to all fleets within range.", pad);
		
		if (!Global.CODEX_TOOLTIP_MODE) {
			List<FactionAPI> factions = getFactionsThatWouldBecomeHostile();
			if (!factions.isEmpty()) {
				String text = "Turning the transponder on now would reveal your hostile actions to";
				boolean first = true;
				boolean last = false;
				for (FactionAPI faction : factions) {
					last = factions.indexOf(faction) == factions.size() - 1;
					if (first || !last) {
						text += " " + faction.getDisplayNameWithArticle() + ",";
					} else {
						text += " and " + faction.getDisplayNameWithArticle() + ",";
					}
				}
				text = text.substring(0, text.length() - 1) + ".";
				tooltip.addPara(text, red, pad);
			}
		}
		
		tooltip.addPara("When the transponder is on, your fleet can be detected at longer range and " +
				"full information on its composition is available at maximum range.", pad);
		tooltip.addPara("Transponder status also affects the reputation changes from combat and trade. " +
				"With it turned off, you might be able to trade with otherwise inhospitable factions.", pad);
		tooltip.addPara("In most places, running with the transponder off will attract the attention of nearby patrols, " +
				"although several factions and certain free ports allow it. " +
				"Having the transponder off does not grant perfect anonymity.", pad);
		
		
		//tooltip.addPara("Disables \"Go Dark\" when activated.", pad);
		addIncompatibleToTooltip(tooltip, expanded);
	}
	
	public String getFactionList(List<FactionAPI> factions) {
		String text = "";
		boolean first = true;
		boolean last = false;
		for (FactionAPI faction : factions) {
			last = factions.indexOf(faction) == factions.size() - 1;
			if (first || !last) {
				text += " " + faction.getDisplayNameLongWithArticle() + ",";
			} else {
				text += " and " + faction.getDisplayNameLongWithArticle() + ",";
			}
		}
		text = text.substring(0, text.length() - 1) + "";
		return text.trim();
	}
	

	public boolean hasTooltip() {
		return true;
	}
	
	
	public List<FactionAPI> getFactionsThatWouldBecomeHostile() {
		return getFactionsThatWouldBecomeHostile(getFleet());
	}
	public static List<FactionAPI> getFactionsThatWouldBecomeHostile(CampaignFleetAPI fleet) {
		List<FactionAPI> result = new ArrayList<FactionAPI>();
		//CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return result;
		if (fleet.getContainingLocation() == null) return result;
		if (fleet.isTransponderOn()) return result;
		
		List<CampaignFleetAPI> fleets = fleet.getContainingLocation().getFleets();
		for (CampaignFleetAPI other : fleets) {
			if (other.getFleetData().getNumMembers() <= 0) continue;
			
//			VisibilityLevel level = fleet.getVisibilityLevelTo(other);
//			if (level == VisibilityLevel.NONE) continue;

			float dist = Misc.getDistance(fleet.getLocation(), other.getLocation());
			fleet.setTransponderOn(true);
			float detectRange = (other.getMaxSensorRangeToDetect(fleet) + DETECTABILITY_FLAT) * DETECTABILITY_MULT * 1.25f;
			fleet.setTransponderOn(false);
			
			if (dist > detectRange) continue;
			
			MemoryAPI mem = other.getMemoryWithoutUpdate();
			
			if (mem.getBoolean(MemFlags.MEMORY_KEY_LOW_REP_IMPACT)) continue;
			
			if (!result.contains(other.getFaction()) && 
					mem.getBoolean(MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF) &&
					!other.getFaction().isHostileTo(fleet.getFaction())) {
				result.add(other.getFaction());
			}
		}
		return result;
	}

	private transient boolean showAlarm = false;
	@Override
	public void advance(float amount) {
		if (!Global.getSector().isPaused()) {
			super.advance(amount);
		}
		
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null || !fleet.isPlayerFleet()) return;
		sinceWarning += amount;
		
		List<FactionAPI> factions = getFactionsThatWouldBecomeHostile();
		if (factions.isEmpty()) {
			showAlarm = false;
		} else {
			showAlarm = true;
		}
	}
	
	@Override
	public boolean runWhilePaused() {
		return getFleet() != null && getFleet().isPlayerFleet();
	}


	@Override
	public boolean showProgressIndicator() {
		return showAlarm;
	}

	@Override
	public float getProgressFraction() {
		return 1f;
	}


	@Override
	public Color getProgressColor() {
		Color color = Misc.getNegativeHighlightColor();
		return Misc.scaleAlpha(color, Global.getSector().getCampaignUI().getSharedFader().getBrightness());
	}

	
	public static final float CONFIRM_DURATION = 5f;


	@Override
	protected Object readResolve() {
		super.readResolve();
		sinceWarning = 1000f;
		return this;
	}
	
	transient float sinceWarning = 1000f;
	
	@Override
	public void pressButton() {
		if (getFleet().isPlayerFleet()) {
			if (sinceWarning < CONFIRM_DURATION) {
				if (entity.isPlayerFleet()) {
					List<FactionAPI> factions = getFactionsThatWouldBecomeHostile();
					if (!factions.isEmpty()) {
						String list = getFactionList(factions);
						String text = "Turning the transponder on will reveal your identity and make " + list + " hostile.\n\n" +
								"Are you sure you want to do this?";
						Global.getSector().getCampaignUI().showConfirmDialog(text, "Confirm", "Cancel", 
								new Script() {
									public void run() {
										TransponderAbility.super.pressButton();
										Global.getSector().getCampaignUI().getMessageDisplay().removeMessage(getDeactivationMessage());
										Global.getSector().getCampaignUI().getMessageDisplay().removeMessage(getActivationMessage());
										sinceWarning = 1000f;
									}
								}, null);
					} else {
						super.pressButton();
						Global.getSector().getCampaignUI().getMessageDisplay().removeMessage(getDeactivationMessage());
						Global.getSector().getCampaignUI().getMessageDisplay().removeMessage(getActivationMessage());
						sinceWarning = 1000f;
					}
				} else {
					super.pressButton();
					Global.getSector().getCampaignUI().getMessageDisplay().removeMessage(getDeactivationMessage());
					Global.getSector().getCampaignUI().getMessageDisplay().removeMessage(getActivationMessage());
					sinceWarning = 1000f;
				}
			} else {
				sinceWarning = 0f;
				if (isActive()) {
					//Global.getSector().getCampaignUI().getMessageDisplay().addMessage("Use again to confirm transponder deactivation");
					Global.getSector().getCampaignUI().getMessageDisplay().addMessage(getDeactivationMessage());
				} else {
					//Global.getSector().getCampaignUI().getMessageDisplay().addMessage("Use again to confirm transponder activation");
					if (showAlarm) {
						Global.getSector().getCampaignUI().getMessageDisplay().addMessage(getActivationMessage(), Misc.getNegativeHighlightColor());
					} else {
						Global.getSector().getCampaignUI().getMessageDisplay().addMessage(getActivationMessage());
					}
				}
			}
		} else {
			super.pressButton();
		}
	}
	
	protected String getDeactivationMessage() {
		return "Transponder shutdown initiated - use again to deactivate";
	}
	protected String getActivationMessage() {
		if (showAlarm) {
			return "Activating the transponder will reveal your identity - use again to confirm";
		}
		return "Transponder primed - use again to activate";
	}


	@Override
	public float getCooldownFraction() {
		if (sinceWarning < CONFIRM_DURATION) {
			return 0f;
		}
		return super.getCooldownFraction();
	}


	@Override
	public boolean isOnCooldown() {
		return false;
	}


	@Override
	public Color getCooldownColor() {
		if (sinceWarning < CONFIRM_DURATION) {
			Color color = Misc.getNegativeHighlightColor();
			//Color color = Color.red;
			if (!showAlarm) {
				color = Misc.getHighlightColor();
				//color = Color.yellow;
			}
			float b = 1f;
			float t = 0.25f;
			if (sinceWarning < t) {
				b = sinceWarning / t;
			}
			if (sinceWarning > CONFIRM_DURATION - t) {
				b = 1f - (sinceWarning - (CONFIRM_DURATION - t)) / t;
			}
			return Misc.scaleAlpha(color, Global.getSector().getCampaignUI().getSharedFader().getBrightness() * 0.75f * b);
		}
		return super.getCooldownColor();
	}


	@Override
	public boolean isCooldownRenderingAdditive() {
		return true;
	}
	
	
	
}





