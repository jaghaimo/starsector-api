package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.rulecmd.DumpMemory;
import com.fs.starfarer.api.util.Misc;

public class DevMenuOptions {
	
	public static String TOP_MENU = "DMO_top";
	
	public static String REP_MENU = "DMO_rep";
	public static String MUSIC_MENU = "DMO_music";
	public static String BACK_TO_OPTIONS = "DMO_top_back";
	public static String BACK_TO_TOP = "DMO_rep_back";
	
	public static String INCREASE_REP = "DMO_increase_rep";
	public static String REDUCE_REP = "DMO_reduce_rep";
	public static String PRINT_REP = "DMO_print_rep";
	
	public static String MUSIC_PRINT = "DMO_print_current";
	public static String MUSIC_RESTART = "DMO_music_restart";
	
	public static Set<String> options = new HashSet<String>();
	
	static {
		options.add(TOP_MENU);
		options.add(REP_MENU);
		options.add(MUSIC_MENU);
		options.add(BACK_TO_OPTIONS);
		options.add(BACK_TO_TOP);
		
		options.add(INCREASE_REP);
		options.add(REDUCE_REP);
		options.add(PRINT_REP);
		
		options.add(MUSIC_PRINT);
		options.add(MUSIC_RESTART);
	}
	
	public static boolean isDevOption(Object optionData) {
		return options.contains(optionData);
	}
	
	public static void addOptions(InteractionDialogAPI dialog) {
		DumpMemory.addOption(dialog);
		dialog.getOptionPanel().addOption(">> (dev) options", TOP_MENU, Misc.getGrayColor(), null);
		
		savedOptions = dialog.getOptionPanel().getSavedOptionList();
	}
	
	
	public static List savedOptions = null;
	
	public static void execute(InteractionDialogAPI dialog, String option) {
		SectorEntityToken entity = dialog.getInteractionTarget();
		Color g = Misc.getGrayColor();
		if (option == TOP_MENU) {
			dialog.getOptionPanel().clearOptions();
			dialog.getOptionPanel().addOption(">>> (dev) reputation", REP_MENU, g, null);
			dialog.getOptionPanel().addOption(">>> (dev) music", MUSIC_MENU, g, null);
			dialog.getOptionPanel().addOption(">>> (dev) back", BACK_TO_OPTIONS, g, null);
			dialog.getOptionPanel().setShortcut(BACK_TO_OPTIONS, Keyboard.KEY_ESCAPE, false, false, false, true);
		} else if (option == REP_MENU) {
			
			//System.out.println("Go dark active: " +dialog.getInteractionTarget().getAbility(Abilities.GO_DARK).isActive());
			
			dialog.getOptionPanel().clearOptions();
			dialog.getOptionPanel().addOption(">>>> (dev) print", PRINT_REP, g, null);
			dialog.getOptionPanel().addOption(">>>> (dev) increase", INCREASE_REP, g, null);
			dialog.getOptionPanel().addOption(">>>> (dev) decrease", REDUCE_REP, g, null);
			dialog.getOptionPanel().addOption(">>>> (dev) back", BACK_TO_TOP, g, null);
			dialog.getOptionPanel().setShortcut(BACK_TO_TOP, Keyboard.KEY_ESCAPE, false, false, false, true);
		} else if (option == MUSIC_MENU) {
			dialog.getOptionPanel().clearOptions();
			dialog.getOptionPanel().addOption(">>>> (dev) print current music", MUSIC_PRINT, g, null);
			dialog.getOptionPanel().addOption(">>>> (dev) restart music", MUSIC_RESTART, g, null);
			dialog.getOptionPanel().addOption(">>>> (dev) back", BACK_TO_TOP, g, null);
			dialog.getOptionPanel().setShortcut(BACK_TO_TOP, Keyboard.KEY_ESCAPE, false, false, false, true);
		}
		
		if (entity != null && entity.getFaction() != null) {
			if (option == PRINT_REP) {
				if (entity.getActivePerson() != null) {
					dialog.getTextPanel().addParagraph("Reputation with " + entity.getActivePerson().getNameString() + ": " + 
							entity.getActivePerson().getRelToPlayer().getRel());
				} else {
					dialog.getTextPanel().addParagraph("Reputation with " + entity.getFaction().getDisplayName() + ": " + 
								entity.getFaction().getRelationship(Factions.PLAYER));
				}
			} else if (option == INCREASE_REP) {
				if (entity.getActivePerson() != null) {
					CustomRepImpact impact = new CustomRepImpact();
					impact.limit = RepLevel.COOPERATIVE;
					impact.delta = 0.1f;
					Global.getSector().adjustPlayerReputation(
							new RepActionEnvelope(RepActions.CUSTOM, impact,
												  null, null, false), entity.getActivePerson());
					dialog.getTextPanel().addParagraph("Reputation with " + entity.getActivePerson().getNameString() + ": " + 
							entity.getActivePerson().getRelToPlayer().getRel());
				} else {
					entity.getFaction().adjustRelationship(Factions.PLAYER, 0.1f);
					dialog.getTextPanel().addParagraph("Reputation with " + entity.getFaction().getDisplayName() + ": " + 
							entity.getFaction().getRelationship(Factions.PLAYER));
				}
			} else if (option == REDUCE_REP) {
				if (entity.getActivePerson() != null) {
					CustomRepImpact impact = new CustomRepImpact();
					impact.limit = RepLevel.VENGEFUL;
					impact.delta = -0.1f;
					Global.getSector().adjustPlayerReputation(
							new RepActionEnvelope(RepActions.CUSTOM, impact,
												  null, null, false), entity.getActivePerson());
					dialog.getTextPanel().addParagraph("Reputation with " + entity.getActivePerson().getNameString() + ": " + 
							entity.getActivePerson().getRelToPlayer().getRel());
				} else {
					entity.getFaction().adjustRelationship(Factions.PLAYER, -0.1f);
					dialog.getTextPanel().addParagraph("Reputation with " + entity.getFaction().getDisplayName() + ": " + 
							entity.getFaction().getRelationship(Factions.PLAYER));
				}
			}
		}
		
		if (option == MUSIC_RESTART) {
			Global.getSoundPlayer().restartCurrentMusic();
		} else if (option == MUSIC_PRINT) {
			dialog.getTextPanel().addParagraph("Now playing: " + Global.getSoundPlayer().getCurrentMusicId());
		}
		
		
		
		if (option == BACK_TO_OPTIONS) {
			if (savedOptions != null) {
				dialog.getOptionPanel().restoreSavedOptions(savedOptions);
				return;
			}
		}
		if (option == BACK_TO_TOP) {
			execute(dialog, TOP_MENU);
		}
	}

}





