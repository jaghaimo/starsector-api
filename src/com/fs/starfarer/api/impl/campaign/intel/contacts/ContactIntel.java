package com.fs.starfarer.api.impl.campaign.intel.contacts;

import java.awt.Color;
import java.util.Random;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StoryPointActionDelegate;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseMissionHub;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.BaseOptionStoryPointActionDelegate;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.StoryOptionParams;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ContactIntel extends BaseIntelPlugin {
	
	public static enum ContactState {
		POTENTIAL,
		NON_PRIORITY,
		PRIORITY,
		SUSPENDED,
		LOST_CONTACT_DECIV,
		LOST_CONTACT,
	}
	
	
	public static String NO_CONTACTS_ON_MARKET = "$core_noContactsOnMarket";
	
	public static float MAX_NUM_MISSIONS_BONUS = Global.getSettings().getFloat("priorityContactMaxNumMissionsBonus");
	public static float MAX_MISSION_QUALITY_BONUS = Global.getSettings().getFloat("priorityContactMaxMissionQualityBonus");
	public static float DEFAULT_POTENTIAL_CONTACT_PROB = Global.getSettings().getFloat("defaultPotentialContactProbability");
	public static float ADD_PER_FAIL = Global.getSettings().getFloat("potentialContactProbabilityAddPerFail");
	public static float DAYS_AT_PRIORITY_FOR_FULL_EFFECT = 30;
	
	public static String UPDATE_RELOCATED_CONTACT = "update_relocated_contact";
	public static String UPDATE_LOST_CONTACT = "update_lost_contact";
	
	public static String BUTTON_DEVELOP = "button_develop";
	public static String BUTTON_SUSPEND = "button_suspend";
	public static String BUTTON_DELETE = "button_delete";
	
	public static String BUTTON_PRIORITY = "button_priority";
	
	public static float DURATION = 30f;
	public static boolean POTENTIAL_EXPIRES = false;
	
	protected ContactState state = ContactState.POTENTIAL;
	protected PersonAPI person;
	protected MarketAPI market;
	protected FactionAPI marketFaction;
	//protected CampaignFleetAPI fleet;
	
	protected Boolean wasAddedToCommDirectory = null;
	protected Boolean wasAddedToMarket = null;
	
	protected boolean hadMissionHub;
	protected boolean marketWasDeciv;
	
	protected IntervalUtil tracker = new IntervalUtil(0.75f, 1.25f);
	protected int numPriorityInARow = 0;

	public ContactIntel(PersonAPI person, MarketAPI market) {
		this.person = person;
		this.market = market;
		marketFaction = market.getFaction();
		//person.setMarket(market); // if set here, it can get unset in BaseHubMission.endSuccess()->abort()
		marketWasDeciv = market.hasCondition(Conditions.DECIVILIZED);
		if (!Global.getSector().getScripts().contains(this)) {
			Global.getSector().addScript(this);
		}
		
		setImportant(true);
	}
	
//	public ContactIntel(PersonAPI person, CampaignFleetAPI fleet) {
//		this.person = person;
//		this.fleet = fleet;
//		if (!Global.getSector().getScripts().contains(this)) {
//			Global.getSector().addScript(this);
//		}
//	}
	
	protected void ensureHasMissionHub() {
		hadMissionHub = BaseMissionHub.get(person) != null;
		if (BaseMissionHub.get(person) == null) {
			BaseMissionHub.set(person, new BaseMissionHub(person));
		}
	}
	protected void ensureIsInCommDirectory() {
		if (market == null) return;
		if (market.getCommDirectory() == null) return;
		if (market.getCommDirectory().getEntryForPerson(person) != null) return;
		
		market.getCommDirectory().addPerson(person);
		wasAddedToCommDirectory = true;
	}
	
	public void advanceImpl(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		tracker.advance(days);
		if (tracker.intervalElapsed()) {
			doPeriodicCheck();
		}
	}
	
	public void notifyPlayerAboutToOpenIntelScreen() {
		doPeriodicCheck();
	}

	protected MarketAPI findMarketToRelocateTo() {
		if (market == null) return null;
		
		if (person.getImportance() == PersonImportance.VERY_LOW) return null;
		Random random = new Random(Misc.getSalvageSeed(market.getPrimaryEntity()));
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(random);
		for (MarketAPI curr : Global.getSector().getEconomy().getMarketsInGroup(market.getEconGroup())) {
			if (curr == market) continue;
			if (!curr.getFactionId().equals(market.getFactionId())) continue;
			if (curr.hasCondition(Conditions.DECIVILIZED)) continue;
			picker.add(curr, market.getSize());
		}
		return picker.pick();
	}
	
	protected void unsetFlags() {
		MemoryAPI memory = person.getMemoryWithoutUpdate();
		memory.unset(BaseMissionHub.NUM_BONUS_MISSIONS);
		memory.unset(BaseMissionHub.MISSION_QUALITY_BONUS);
		memory.unset(BaseMissionHub.CONTACT_SUSPENDED);
	}
	
	public void loseContact(InteractionDialogAPI dialog) {
		//endAfterDelay();
		state = ContactState.LOST_CONTACT;
		if (dialog != null) {
			sendUpdate(UPDATE_LOST_CONTACT, dialog.getTextPanel());
		} else {
			sendUpdateIfPlayerHasIntel(UPDATE_LOST_CONTACT, false);
		}
		endImmediately();
	}
	
	public void doPeriodicCheck() {
		if (isEnded() || isEnding()) return;
		
		
		MemoryAPI memory = person.getMemoryWithoutUpdate();
		unsetFlags();
		
		if (state != ContactState.LOST_CONTACT_DECIV && state != ContactState.LOST_CONTACT && market != null) {
			if (!marketWasDeciv && (market.hasCondition(Conditions.DECIVILIZED) || !market.isInEconomy())) {
				MarketAPI other = findMarketToRelocateTo();
				if (other == null) {
					endAfterDelay();
					state = ContactState.LOST_CONTACT_DECIV;
					sendUpdateIfPlayerHasIntel(UPDATE_LOST_CONTACT, false);
				} else {
					relocateToMarket(other, true);
				}
				return;
			}
		}
		if (state == ContactState.LOST_CONTACT) return;
		if (state == ContactState.POTENTIAL) return;
		if (state == ContactState.SUSPENDED) {
			memory.set(BaseMissionHub.CONTACT_SUSPENDED, true);
			return;
		}
		
		if (state == ContactState.PRIORITY) {
			numPriorityInARow++;
		} else {
			numPriorityInARow--;
		}
		if (numPriorityInARow < 0) {
			numPriorityInARow = 0;
		} else if (numPriorityInARow > DAYS_AT_PRIORITY_FOR_FULL_EFFECT) {
			numPriorityInARow = (int) DAYS_AT_PRIORITY_FOR_FULL_EFFECT;
		}
		

		float bonusMissions = getPriorityMult() * MAX_NUM_MISSIONS_BONUS;
		if (bonusMissions > 0) {
			memory.set(BaseMissionHub.NUM_BONUS_MISSIONS, bonusMissions);
		}
		
		float bonusQuality = getPriorityMult() * MAX_MISSION_QUALITY_BONUS;
		if (bonusQuality > 0) {
			memory.set(BaseMissionHub.MISSION_QUALITY_BONUS, bonusMissions);
		}
	}
	
	public void relocateToMarket(MarketAPI other, boolean withIntelUpdate) {
		if (wasAddedToCommDirectory != null && wasAddedToCommDirectory && market != null && market.getCommDirectory() != null) {
			market.getCommDirectory().removePerson(person);
			wasAddedToCommDirectory = null;
		}
		market = other;
		person.setMarket(other);
		marketWasDeciv = other.hasCondition(Conditions.DECIVILIZED);
		ensureIsInCommDirectory();
		ensureIsAddedToMarket();
		person.setImportance(person.getImportance().prev());
		if (withIntelUpdate) {
			sendUpdateIfPlayerHasIntel(UPDATE_RELOCATED_CONTACT, false);
		}
	}
	
	
	public float getPriorityMult() {
		float priority = getPriorityContacts();
		if (priority < 1f) priority = 1f;
		return ((float) numPriorityInARow / (float) DAYS_AT_PRIORITY_FOR_FULL_EFFECT) / priority;
	}
	
	
	@Override
	public void reportPlayerClickedOn() {
		super.reportPlayerClickedOn();
	}

	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		if (wasAddedToCommDirectory != null && wasAddedToCommDirectory && market != null && market.getCommDirectory() != null) {
			market.getCommDirectory().removePerson(person);
			wasAddedToCommDirectory = null;
		}
		if (wasAddedToMarket != null && wasAddedToMarket) {
			market.removePerson(person);
		}
		
		if (!hadMissionHub) {
			BaseMissionHub.set(person, null);
		}
		
		unsetFlags();
	}

	@Override
	public void reportRemovedIntel() {
		super.reportRemovedIntel();
		Global.getSector().removeScript(this);
	}

	public boolean shouldRemoveIntel() {
		if (isEnded()) return true;
		if (state != ContactState.POTENTIAL) return false;
		float days = getDaysSincePlayerVisible();
		if (days >= DURATION && POTENTIAL_EXPIRES) {
			ended = true;
			return true;
		}
		return false;
	}
	
	public String getName() {
		if (state == ContactState.LOST_CONTACT_DECIV) {
			return "Lost Contact: " + person.getNameString();
		} else if (state == ContactState.LOST_CONTACT) {
			return "Lost Contact: " + person.getNameString();
		} else if (state == ContactState.POTENTIAL) {
			return "Potential Contact: " + person.getNameString();
		} else if (state == ContactState.SUSPENDED) {
			return "Suspended Contact: " + person.getNameString();
		} else if (state == ContactState.PRIORITY) {
			return "Priority Contact: " + person.getNameString();
		}
		return "Contact: " + person.getNameString();
	}
	
	@Override
	public String getSortString() {
		if (state == ContactState.POTENTIAL) {
			return "Contact2";
		} else if (state == ContactState.SUSPENDED) {
			return "Contact3";
		} else if (state == ContactState.PRIORITY) {
			return "Contact0";
		}
		return "Contact1";
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
		addBulletPoints(info, mode);
	}
	
	protected void addTypePara(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		String [] tags = person.getSortedContactTagStrings().toArray(new String [0]);
		if (tags.length <= 0) return;
		String str = "Type: ";
		for (String tag : tags) {
			str += tag + ", ";
		}
		if (tags.length > 0) {
			str = str.substring(0, str.length() - 2);
		}
		info.addPara(str, pad, tc, h, tags);
	}
	
	protected void addFactionPara(TooltipMakerAPI info, Color tc, float pad) {
		//String faction = Misc.ucFirst(person.getFaction().getDisplayName());
		String faction = person.getFaction().getDisplayName();
		String str = "Faction: " + faction;
		
		info.addPara(str, pad, tc, person.getFaction().getBaseUIColor(), faction);
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = getBulletColorForMode(mode);
		
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;

		bullet(info);
		
		if (getListInfoParam() == UPDATE_RELOCATED_CONTACT) {
			info.addPara("Relocated to " + market.getName(), tc, initPad);
			initPad = 0f;
			info.addPara("Importance reduced to: %s", initPad, tc, h, person.getImportance().getDisplayName());
			initPad = 0f;
			unindent(info);
			return;
		}
		if (state == ContactState.LOST_CONTACT_DECIV) {
			if (mode != ListInfoMode.IN_DESC) {
				info.addPara(market.getName() + " decivilized", tc, initPad);
				initPad = 0f;
			}
			unindent(info);
			return;
		}
		
		if (state == ContactState.LOST_CONTACT) {
			unindent(info);
			return;
		}
		
		addFactionPara(info, tc, initPad);
		initPad = 0f;
		
		addTypePara(info, tc, initPad);
		initPad = 0f;
		
		if (mode != ListInfoMode.IN_DESC) {
			info.addPara("Importance: %s", initPad, tc, h, person.getImportance().getDisplayName());
			initPad = 0f;
			
			if (state == ContactState.PRIORITY || state == ContactState.NON_PRIORITY || state == ContactState.SUSPENDED) {
				long ts = BaseMissionHub.getLastOpenedTimestamp(person);
				if (ts <= Long.MIN_VALUE) {
					//info.addPara("Never visited.", opad);	
				} else {
					info.addPara("Last visited: %s.", initPad, tc, h, Misc.getDetailedAgoString(ts));
					initPad = 0f;
				}
			}
		}
		
//		info.addPara("Rank: %s", initPad, tc, h, person.getRank());
//		initPad = 0f;
		
//		info.addPara("Post: %s", initPad, tc, h, person.getPost());
//		initPad = 0f;
		
		if (state == ContactState.POTENTIAL && POTENTIAL_EXPIRES) {
			if (mode != ListInfoMode.IN_DESC) {
				float days = DURATION - getDaysSincePlayerVisible();
				info.addPara("%s " + getDaysString(days) + " left to develop", 
							 initPad, tc, h, getDays(days));
				initPad = 0f;
			}
		}
		
		//info.addPara("Personality: %s", initPad, tc, h, pName);
		unindent(info);
	}
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		String pName = Misc.getPersonalityName(person);
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		//info.addImage(person.getPortraitSprite(), width, 128, opad);
		
		FactionAPI faction = person.getFaction();
		info.addImages(width, 128, opad, opad, person.getPortraitSprite(), faction.getCrest());
		
		float relBarWidth = 128f * 2f + 10f;
		float importanceBarWidth = relBarWidth;
		
		float indent = 25;
		info.addSpacer(0).getPosition().setXAlignOffset(indent);
		
		//info.addRelationshipBar(person, relBarWidth, opad);
		
		relBarWidth = (relBarWidth - 10f) / 2f;
		info.addRelationshipBar(person, relBarWidth, opad);
		float barHeight = info.getPrev().getPosition().getHeight();
		info.addRelationshipBar(person.getFaction(), relBarWidth, 0f);
		UIComponentAPI prev = info.getPrev();
		prev.getPosition().setYAlignOffset(barHeight);
		prev.getPosition().setXAlignOffset(relBarWidth + 10f);
		info.addSpacer(0f);
		info.getPrev().getPosition().setXAlignOffset(-(relBarWidth + 10f));
		
		info.addImportanceIndicator(person.getImportance(), importanceBarWidth, opad);
		addImportanceTooltip(info);
//		faction = Global.getSector().getPlayerFaction();
//		ButtonAPI button = info.addAreaCheckbox("Priority contact", BUTTON_PRIORITY, faction.getBaseUIColor(), 
//				faction.getDarkUIColor(), faction.getBrightUIColor(), relBarWidth, 25f, opad);
//		button.setChecked(state == ContactState.PRIORITY);
//		faction = person.getFaction();
		info.addSpacer(0).getPosition().setXAlignOffset(-indent);
		
		if (state == ContactState.NON_PRIORITY || state == ContactState.PRIORITY) {
			//info.addSpacer(0).getPosition().setXAlignOffset(indent);
			faction = Global.getSector().getPlayerFaction();
			ButtonAPI button = info.addAreaCheckbox("Priority contact", BUTTON_PRIORITY, faction.getBaseUIColor(), 
					faction.getDarkUIColor(), faction.getBrightUIColor(), width, 25f, opad);
			button.setChecked(state == ContactState.PRIORITY);
			addPriorityTooltip(info);
			faction = person.getFaction();
			//info.addSpacer(0).getPosition().setXAlignOffset(-indent);
		}
		
		if (market != null && state == ContactState.LOST_CONTACT_DECIV) {
			info.addPara(person.getNameString() + " was " + 
					person.getPostArticle() + " " + person.getPost().toLowerCase() + 
					" " + market.getOnOrAt() + " " + market.getName() + 
					", a colony controlled by " + marketFaction.getDisplayNameWithArticle() + ".",
					opad, marketFaction.getBaseUIColor(),
					Misc.ucFirst(marketFaction.getDisplayNameWithArticleWithoutArticle()));
			info.addPara("This colony has decivilized, and you've since lost contact with " + person.getHimOrHer() + ".", opad);
		} else if (state == ContactState.LOST_CONTACT) {
			info.addPara("You've lost this contact.", opad);
		} else {
			if (market != null) {
				LabelAPI label = info.addPara(person.getNameString() + " is " + 
						person.getPostArticle() + " " + person.getPost().toLowerCase() + 
						" and can be found " + market.getOnOrAt() + " " + market.getName() + 
						", a size %s colony controlled by " + market.getFaction().getDisplayNameWithArticle() + ".",
						opad, market.getFaction().getBaseUIColor(),
						"" + (int)market.getSize(), market.getFaction().getDisplayNameWithArticleWithoutArticle());
				label.setHighlightColors(h, market.getFaction().getBaseUIColor());
//				LabelAPI label = info.addPara(Misc.ucFirst(person.getPost().toLowerCase()) + 
//						", found " + market.getOnOrAt() + " " + market.getName() + 
//						", a size %s colony controlled by " + market.getFaction().getDisplayNameWithArticle() + ".",
//						opad, market.getFaction().getBaseUIColor(),
//						"" + (int)market.getSize(), Misc.ucFirst(market.getFaction().getPersonNamePrefix()));
//				label.setHighlightColors(h, market.getFaction().getBaseUIColor());
			}
		}
		
		if (state == ContactState.POTENTIAL){ 
			info.addPara("If this contact is developed, " + person.getHeOrShe() + " will periodically " +
						 "have work for you. As the relationship improves, you may gain " +
						 "access to better opportunities.", opad);
		} else if (state == ContactState.SUSPENDED) {
			info.addPara("Your contact with " + person.getNameString() + " is currently suspended.", Misc.getNegativeHighlightColor(), opad);
		}
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
		
		
		if (state == ContactState.PRIORITY || state == ContactState.NON_PRIORITY || state == ContactState.SUSPENDED) {
			
			
			long ts = BaseMissionHub.getLastOpenedTimestamp(person);
			if (ts <= Long.MIN_VALUE) {
				//info.addPara("Never visited.", opad);	
			} else {
				info.addPara("Last visited: %s.", opad, h, Misc.getDetailedAgoString(ts));
			}
			
//			Color color = faction.getBaseUIColor();
//			Color dark = faction.getDarkUIColor();
//			info.addSectionHeading("Personality traits", color, dark, Alignment.MID, opad);
//			info.addPara("Suspicous          Ambitious", opad, h, "Suspicous", "Ambitious");
//			info.addPara("Ambitious: will offer missions that further their advancement more frequently. Refusing " +
//					"these missions will damage the relationship.", opad, h, "Ambitious:");
//			info.addPara("Suspicous: reduced reputation gains.", opad, h, "Suspicous:");
			
		}
		

		Color color = Misc.getStoryOptionColor();
		Color dark = Misc.getStoryDarkColor();
		
		TooltipCreator noDeleteTooltip = new TooltipCreator() {
			public boolean isTooltipExpandable(Object tooltipParam) {
				return false;
			}
			public float getTooltipWidth(Object tooltipParam) {
				return TOOLTIP_WIDTH;
			}
			
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("Can not delete or suspend contact at this time.", 0f);
			}
		};
		
		if (state == ContactState.POTENTIAL || state == ContactState.SUSPENDED){
			if (state == ContactState.POTENTIAL && POTENTIAL_EXPIRES) {
				float days = DURATION - getDaysSincePlayerVisible();
				info.addPara("The opportunity to develop this contact will be available for %s more " + getDaysString(days) + ".", 
						opad, tc, h, getDays(days));
			}
		
			
			int max = getMaxContacts();
			int curr = getCurrentContacts();
	
			info.addPara("Active contacts: %s %s %s", opad,
							 h, "" + curr, "/", "" + max);
			
			ButtonAPI develop = null;
			String developText = "Develop contact";
			if (state == ContactState.SUSPENDED) developText = "Resume contact";
			if (curr >= max) {
//				info.addPara("Developing contacts above the maximum will " +
//							 "require a story point per additional contact.", opad,
//							 Misc.getStoryOptionColor(), "story point");
				develop = addGenericButton(info, width, color, dark, developText, BUTTON_DEVELOP);
				addDevelopTooltip(info);
			} else {
				develop = addGenericButton(info, width, developText, BUTTON_DEVELOP);
			}
			develop.setShortcut(Keyboard.KEY_T, true);
		} else if (state == ContactState.NON_PRIORITY || state == ContactState.PRIORITY) {
			ButtonAPI suspend = addGenericButton(info, width, color, dark, "Suspend contact", BUTTON_SUSPEND);
			suspend.setShortcut(Keyboard.KEY_U, true);
			if (Global.getSector().getIntel().isInShowMap()) {
				suspend.setEnabled(false);
				info.addTooltipToPrevious(noDeleteTooltip, TooltipLocation.LEFT);
			}
		}
		
		info.addSpacer(-10f);
		ButtonAPI delete = addGenericButton(info, width, "Delete contact", BUTTON_DELETE);
		if (Global.getSector().getIntel().isInShowMap()) {
			delete.setEnabled(false);
			info.addTooltipToPrevious(noDeleteTooltip, TooltipLocation.LEFT);
		}
		delete.setShortcut(Keyboard.KEY_G, true);
	}
	
	
	@Override
	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == BUTTON_DEVELOP) {
			develop(ui);
			Global.getSoundPlayer().playUISound("ui_contact_developed", 1f, 1f);
		} else if (buttonId == BUTTON_PRIORITY) {
			if (state == ContactState.NON_PRIORITY) {
				state = ContactState.PRIORITY;
			} else if (state == ContactState.PRIORITY) {
				state = ContactState.NON_PRIORITY;
			}
			ui.updateUIForItem(this);
		} else if (buttonId == BUTTON_DELETE) {
			endImmediately();
			ui.recreateIntelUI();
			//Global.getSector().getCampaignUI().showCoreUITab(CoreUITabId.CARGO);
		}
	}

	public void storyActionConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == BUTTON_DEVELOP) {
			develop(ui);
		} else if (buttonId == BUTTON_SUSPEND) {
			state = ContactState.SUSPENDED;
			person.getMemoryWithoutUpdate().set(BaseMissionHub.CONTACT_SUSPENDED, true);
			ui.updateUIForItem(this);
		}
	}

	public void ensureIsAddedToMarket() {
		if (market == null) return;
		
		boolean hadPerson = market.getPeopleCopy().contains(person) || market == person.getMarket();
		
		if (person.getMarket() != null) {
			person.getMarket().removePerson(person);
		}
		
		if (market.getPeopleCopy().contains(person)) return;
		market.addPerson(person);
		if (!hadPerson) wasAddedToMarket = true;
	}
	public void develop(IntelUIAPI ui) {
		ensureIsInCommDirectory();
		ensureHasMissionHub();
		ensureIsAddedToMarket();
		
		state = ContactState.NON_PRIORITY;
		if (ui != null) {
			ui.updateUIForItem(this);
		}
		person.getMemoryWithoutUpdate().unset(BaseMissionHub.CONTACT_SUSPENDED);
	}
	
	@Override
	public void createConfirmationPrompt(Object buttonId, TooltipMakerAPI prompt) {
		prompt.setParaInsigniaLarge();
		if (buttonId == BUTTON_DELETE) {
			if (state == ContactState.POTENTIAL) {
				prompt.addPara("Are you sure? Deleting a potential contact can not be undone.", 0f);				
			} else if (state == ContactState.SUSPENDED) {
					prompt.addPara("Are you sure? Deleting a contact can not be undone.", 0f);				
			} else {
				prompt.addPara("Are you sure? Deleting a contact can not be undone. " +
						"To stop receiving missions from a contact, but not lose them permanently, you can \"suspend\" the contact instead.", 0f);
			}
			return;
		} else if (buttonId == BUTTON_DEVELOP) {
			prompt.addPara("Develop a relationship with this contact?", 0f);
			return;	
		}
		super.createConfirmationPrompt(buttonId, prompt);
		return;
	}
	
	@Override
	public boolean doesButtonHaveConfirmDialog(Object buttonId) {
		if (buttonId == BUTTON_DELETE) return true;
		if (buttonId == BUTTON_DEVELOP) return true;
		return super.doesButtonHaveConfirmDialog(buttonId);
	}

	public StoryPointActionDelegate getButtonStoryPointActionDelegate(Object buttonId) {
		if (buttonId == BUTTON_DEVELOP && developRequiresStoryPoint()) {
			StoryOptionParams params = new StoryOptionParams(null, 1, "developContactOverMax", 
											Sounds.STORY_POINT_SPEND, 
											"Developed additional contact (" + person.getNameString() + ")");
			return new BaseOptionStoryPointActionDelegate(null, params) {
				@Override
				public void confirm() {
				}
				
				@Override
				public String getTitle() {
					return null;
				}
				@Override
				public void createDescription(TooltipMakerAPI info) {
					//info.setParaInsigniaLarge();
					super.createDescription(info);
				}
			};
		}
		if (buttonId == BUTTON_SUSPEND) {
			StoryOptionParams params = new StoryOptionParams(null, 1, "suspendContact", 
											Sounds.STORY_POINT_SPEND, 
											"Suspended contact (" + person.getNameString() + ")");
			return new BaseOptionStoryPointActionDelegate(null, params) {
				@Override
				public void confirm() {
				}
				
				@Override
				public String getTitle() {
					return null;
				}
				@Override
				public void createDescription(TooltipMakerAPI info) {
					info.setParaInsigniaLarge();
					info.addPara("If suspended, this contact will not offer you missions and will not " +
								 "count against the maximum number of contacts. You will be able to re-develop this " +
								 "contact at any time.", -10f);
					info.addSpacer(20f);
					super.createDescription(info);
				}
			};
		}
		return null;
	}


	@Override
	public String getIcon() {
		return person.getPortraitSprite();
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_CONTACTS);
		return tags;
	}

	@Override
	public String getCommMessageSound() {
		return super.getCommMessageSound();
		//return getSoundMajorPosting();
	}

	@Override
	public FactionAPI getFactionForUIColors() {
		return person.getFaction();
		//return super.getFactionForUIColors();
	}

	public PersonAPI getPerson() {
		return person;
	}

	public void setPerson(PersonAPI person) {
		this.person = person;
	}

	public ContactState getState() {
		return state;
	}

	public void setState(ContactState state) {
		this.state = state;
	}
	
	protected boolean developRequiresStoryPoint() {
		return getCurrentContacts() >= getMaxContacts();
	}
	
	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		if (market != null) {
			return market.getPrimaryEntity();
		}
		return null;
	}
	
	public static float TOOLTIP_WIDTH = 400f;
	protected void addImportanceTooltip(TooltipMakerAPI info) {
		info.addTooltipToPrevious(new TooltipCreator() {
			public boolean isTooltipExpandable(Object tooltipParam) {
				return false;
			}
			public float getTooltipWidth(Object tooltipParam) {
				return TOOLTIP_WIDTH;
			}
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("The importance of this person in whatever hierarchy or social structure they " +
						"belong to. A more highly placed or connected individual will have more difficult - " +
						"and more profitable - missions and opportunities to offer.", 0f);
			}
		}, TooltipLocation.LEFT);
	}
	protected void addPriorityTooltip(TooltipMakerAPI info) {
		info.addTooltipToPrevious(new TooltipCreator() {
			public boolean isTooltipExpandable(Object tooltipParam) {
				return false;
			}
			public float getTooltipWidth(Object tooltipParam) {
				return TOOLTIP_WIDTH;
			}			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("Whether cultivating a relationship with this contact is a priority for you. " +
								"Priority contacts will have more missions available, and the missions will be of a " +
								"higher quality.", 0f);
				tooltip.addPara("The more priority contacts you have, the less the impact there is on each individual contact.", opad);
				tooltip.addPara("It takes about a month for changes in priority status to take full effect.", opad);
			}
		}, TooltipLocation.LEFT);
	}
	protected void addDevelopTooltip(TooltipMakerAPI info) {
		info.addTooltipToPrevious(new TooltipCreator() {
			public boolean isTooltipExpandable(Object tooltipParam) {
				return false;
			}
			public float getTooltipWidth(Object tooltipParam) {
				return TOOLTIP_WIDTH;
			}
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("Developing contacts above the maximum " +
						 "requires a story point for each additional contact.", 0f,
						 Misc.getStoryOptionColor(), "story point");
			}
		}, TooltipLocation.LEFT);
	}
	
	
	public static int getMaxContacts() {
		int base = Global.getSettings().getInt("maxContacts");
		int max = (int) Global.getSector().getPlayerStats().getDynamic().getMod(Stats.NUM_MAX_CONTACTS_MOD).computeEffective(base);
		return max;
	}
	
	public static int getCurrentContacts() {
		int count = 0;
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(ContactIntel.class)) {
			if (intel.isEnding() || intel.isEnded()) continue;
			if (((ContactIntel)intel).getState() == ContactState.POTENTIAL) continue;
			if (((ContactIntel)intel).getState() == ContactState.SUSPENDED) continue;
			if (((ContactIntel)intel).getState() == ContactState.LOST_CONTACT_DECIV) continue;
			if (((ContactIntel)intel).getState() == ContactState.LOST_CONTACT) continue;
			count++;
		}
		return count;
	}
	
	public static int getPriorityContacts() {
		int count = 0;
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(ContactIntel.class)) {
			if (intel.isEnding() || intel.isEnded()) continue;
			if (((ContactIntel)intel).getState() != ContactState.PRIORITY) continue;
			count++;
		}
		return count;
	}
	
	public static boolean playerHasIntelItemForContact(PersonAPI person) {
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(ContactIntel.class)) {
			if (((ContactIntel)intel).getPerson() == person) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean playerHasContact(PersonAPI person, boolean includePotential) {
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(ContactIntel.class)) {
			if (((ContactIntel)intel).getPerson() == person) {
				ContactState state = ((ContactIntel)intel).getState();
				if (state == ContactState.POTENTIAL && !includePotential) {
					continue;
				}
				if (state == ContactState.LOST_CONTACT || state == ContactState.LOST_CONTACT_DECIV) {
					continue;
				}
				return true;
			}
		}
		return false;
	}
	
	public static ContactIntel getContactIntel(PersonAPI person) {
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(ContactIntel.class)) {
			if (((ContactIntel)intel).getPerson() == person) return (ContactIntel)intel;
		}
		return null;
	}
	
	
	public static void addPotentialContact(PersonAPI contact, MarketAPI market, TextPanelAPI text) {
		addPotentialContact(DEFAULT_POTENTIAL_CONTACT_PROB, contact, market, text);
	}
	public static void addPotentialContact(float probability, PersonAPI contact, MarketAPI market, TextPanelAPI text) {
		if (playerHasIntelItemForContact(contact)) return;
		if (contact.getFaction().isPlayerFaction()) return;
		if (market == null) return;
		if (market != null && market.getMemoryWithoutUpdate().getBoolean(NO_CONTACTS_ON_MARKET)) return;
		if (contact != null && contact.getFaction().getCustomBoolean(Factions.CUSTOM_NO_CONTACTS)) return;
		
		Random random = new Random(getContactRandomSeed(contact));
		// if the player already has some existing relationship with the person, use it to 
		// modify the probability they'll be available as a potential contact
		probability += contact.getRelToPlayer().getRel();
		
		
		String key = "$potentialContactRollFails";
		MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();
		float fails = mem.getInt(key);
		probability += ADD_PER_FAIL * fails;
		
		if (random.nextFloat() >= probability && !DebugFlags.ALWAYS_ADD_POTENTIAL_CONTACT) {
			fails++;
			mem.set(key, fails);
			return;
		}
		
		mem.set(key, 0);
		
		
		ContactIntel intel = new ContactIntel(contact, market);
		Global.getSector().getIntelManager().addIntel(intel, false, text);
	}
	
	
	public static long getContactRandomSeed(PersonAPI person) {
		String id = person.getId();
		if (id == null) id = Misc.genUID();
		long seed = Misc.seedUniquifier() ^ (person.getId().hashCode() * 17000);
		Random r = new Random(seed);
		for (int i = 0; i < 5; i++) {
			r.nextLong();
		}
		return r.nextLong();
	}
}

















