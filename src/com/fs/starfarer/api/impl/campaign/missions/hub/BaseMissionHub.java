package com.fs.starfarer.api.impl.campaign.missions.hub;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.DevMenuOptions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch.StarSystemUnexploredReq;
import com.fs.starfarer.api.impl.campaign.rulecmd.CallEvent.CallableEvent;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.loading.PersonMissionSpec;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class BaseMissionHub implements MissionHub, CallableEvent {
	
	public static float UPDATE_INTERVAL = Global.getSettings().getFloat("contactMissionUpdateIntervalDays");
	public static int MIN_TO_SHOW = Global.getSettings().getInt("contactMinMissions");
	public static int MAX_TO_SHOW = Global.getSettings().getInt("contactMaxMissions");
	public static int MAX_TO_SHOW_WITH_BONUS = Global.getSettings().getInt("contactMaxMissionsWithPriorityBonus");
	
	
	public static String CONTACT_SUSPENDED = "$mHub_contactSuspended";
	public static String NUM_BONUS_MISSIONS = "$mHub_numBonusMissions";
	public static String MISSION_QUALITY_BONUS = "$mHub_missionQualityBonus";
	public static String LAST_OPENED = "$mHub_lastOpenedTimestamp";
	
	public static String KEY = "$mHub";
	public static void set(PersonAPI person, MissionHub hub) {
		if (hub == null) {
			person.getMemoryWithoutUpdate().unset(KEY);
		} else {
			person.getMemoryWithoutUpdate().set(KEY, hub);
		}
	}
	public static MissionHub get(PersonAPI person) {
		if (person == null) return null;
		if (person.getMemoryWithoutUpdate().contains(KEY)) {
			return (MissionHub) person.getMemoryWithoutUpdate().get(KEY);
		}
		return null;
	}
	
	public static float getDaysSinceLastOpened(PersonAPI person) {
		if (!person.getMemoryWithoutUpdate().contains(LAST_OPENED)) {
			return -1;
		}
		long ts = person.getMemoryWithoutUpdate().getLong(LAST_OPENED);
		return Global.getSector().getClock().getElapsedDaysSince(ts);
	}
	public static long getLastOpenedTimestamp(PersonAPI person) {
		if (!person.getMemoryWithoutUpdate().contains(LAST_OPENED)) {
			return Long.MIN_VALUE;
		}
		return person.getMemoryWithoutUpdate().getLong(LAST_OPENED);
	}
	
	public static void setDaysSinceLastOpened(PersonAPI person) {
		person.getMemoryWithoutUpdate().set(LAST_OPENED, Global.getSector().getClock().getTimestamp());
	}
	

	//protected List<MHMission> missions = new ArrayList<MHMission>();
	//protected TimeoutTracker<HubMissionCreator> timeout = new TimeoutTracker<HubMissionCreator>();
	
	protected TimeoutTracker<String> timeout = new TimeoutTracker<String>();
	protected TimeoutTracker<String> recentlyAcceptedTimeout = new TimeoutTracker<String>();
	protected List<HubMissionCreator> creators = new ArrayList<HubMissionCreator>();
	protected transient List<HubMission> offered = new ArrayList<HubMission>();
	
	protected PersonAPI person;
	
	public BaseMissionHub(PersonAPI person) {
		this.person = person;
		
		//creators.add(new GADataFromRuinsCreator());
		readResolve();
	}
	
	public void updateMissionCreatorsFromSpecs() {
		List<PersonMissionSpec> specs = getMissionsForPerson(person);
		Set<String> validMissions = new HashSet<String>();
		Set<String> alreadyHaveCreatorsFor = new HashSet<String>();
		for (PersonMissionSpec spec : specs) {
			validMissions.add(spec.getMissionId());
		}
		
		for (HubMissionCreator curr : creators) {
			if (!curr.wasAutoAdded()) continue;
			
			if (!validMissions.contains(curr.getSpecId())) {
				curr.setActive(false);
				//System.out.println("blahsdf");
			} else {
				curr.setActive(true);
				alreadyHaveCreatorsFor.add(curr.getSpecId());
			}
		}
	
		for (PersonMissionSpec spec : specs) {
			if (!alreadyHaveCreatorsFor.contains(spec.getMissionId())) {
				BaseHubMissionCreator curr = new BaseHubMissionCreator(spec);
				curr.setWasAutoAdded(true);
				curr.setActive(true);
				creators.add(curr);
			}
		}
	}
	
	protected Object readResolve() {
		if (recentlyAcceptedTimeout == null) {
			recentlyAcceptedTimeout = new TimeoutTracker<String>();
		}
		//updateMissionCreatorsFromSpecs();
		return this;
	}
	
	
	public boolean callEvent(String ruleId, InteractionDialogAPI dialog, 
							 List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		if (action.equals("setMHOptionText")) {
			person.getMemoryWithoutUpdate().set("$mh_openOptionText", getOpenOptionText(), 0);
		} else if (action.equals("prepare")) {
			prepare(dialog, memoryMap);
		} else if (action.equals("listMissions")) {
			listMissions(dialog, memoryMap, true);
		} else if (action.equals("returnToList")) {
			listMissions(dialog, memoryMap, false);
		} else if (action.equals("doCleanup")) {
			doCleanup(dialog, memoryMap);
		} else if (action.equals("accept")) {
			String missionId = params.get(1).getString(memoryMap);
			accept(dialog, memoryMap, missionId);
		} else {
			throw new RuntimeException("Unhandled action [" + action + "] in " + getClass().getSimpleName() + 
					" for rule [" + ruleId + "], params:[" + params + "]");
		}
		return true;
	}
	
	
	public void accept(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, String missionId) {
		for (HubMission curr : getOfferedMissions()) {
			if (curr.getMissionId().equals(missionId)) {
				curr.accept(dialog, memoryMap);
				getOfferedMissions().remove(curr);
				
				float dur = curr.getCreator().getAcceptedTimeoutDuration();
				timeout.add(curr.getCreator().getSpecId(), dur);
				recentlyAcceptedTimeout.add(curr.getCreator().getSpecId(), getUpdateInterval());
				break;
			}
		}
	}
	
	public void prepare(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		setDaysSinceLastOpened(getPerson());
		updateOfferedMissions(dialog, memoryMap);
		//offered.clear();
		updateCountAndFirstInlineBlurb(dialog, memoryMap);
	}
	
	public void doCleanup(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		//PersonAPI person = dialog.getInteractionTarget().getActivePerson();
		for (HubMission curr : getOfferedMissions()) {
			curr.abort();
		}
		offered = new ArrayList<HubMission>();
	}
	
	protected void updateCountAndFirstInlineBlurb(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		MemoryAPI pMem = dialog.getInteractionTarget().getActivePerson().getMemoryWithoutUpdate();
		
		int count = 0;
		String firstInlineBlurb = null;
		for (HubMission curr : getOfferedMissions()) {
			count++;
			if (firstInlineBlurb == null) firstInlineBlurb = curr.getBlurbText();
		}
		
		pMem.set("$mh_firstInlineBlurb", firstInlineBlurb, 0);
		pMem.set("$mh_count", count, 0);
	}
	
	public void listMissions(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, boolean withBlurbs) {
		if (dialog != null && dialog.getVisualPanel() != null) {
			dialog.getVisualPanel().removeMapMarkerFromPersonInfo();
		}
		MemoryAPI pMem = dialog.getInteractionTarget().getActivePerson().getMemoryWithoutUpdate();
		updateCountAndFirstInlineBlurb(dialog, memoryMap);
		
		if (pMem.getFloat("$mh_count") <= 0) {
			FireAll.fire(null, dialog, memoryMap, "PopulateOptions");
			return;
		}
		
		dialog.getOptionPanel().clearOptions();
		
		String blurb = "\"";
		boolean hasCommonBlurb = false;
		int count = 0;
		int skipped = 0;
		for (HubMission curr : getOfferedMissions()) {
			if (curr.getBlurbText() != null) {
				blurb += curr.getBlurbText() + " ";
				FireBest.fire(null, dialog, memoryMap, curr.getTriggerPrefix() + "_option true");
				hasCommonBlurb = true;
			} else {
				skipped++;
			}
			count++;
			//if (count >= MAX_TO_SHOW) break;
		}
		
		count -= skipped;
		
		// so that the blurbs and the options are in the same order
		for (HubMission curr : getOfferedMissions()) {
			//if (count >= MAX_TO_SHOW) break;
			count++;
			
			if (curr.getBlurbText() == null) {
				if (withBlurbs) {
					if (!FireBest.fire(null, dialog, memoryMap, curr.getTriggerPrefix() + "_blurb true")) {
						dialog.getTextPanel().addPara("No blurb found for " + curr.getTriggerPrefix(), Misc.getNegativeHighlightColor());
					}
				}
				if (!FireBest.fire(null, dialog, memoryMap, curr.getTriggerPrefix() + "_option true")) {
					dialog.getTextPanel().addPara("No option found for " + curr.getTriggerPrefix(), Misc.getNegativeHighlightColor());
				}
			}
		}
		
		FireBest.fire(null, dialog, memoryMap, "AddMHCloseOption true");
		
		if (withBlurbs && hasCommonBlurb && !pMem.getBoolean("$mh_doNotPrintBlurbs")) {
			blurb = blurb.trim();
			blurb += "\"";
			dialog.getTextPanel().addPara(blurb);
		}
		
		if (withBlurbs) {
			FireBest.fire(null, dialog, memoryMap, "MHPostMissionListText");
		}
		
		if (Global.getSettings().isDevMode()) {
			DevMenuOptions.addOptions(dialog);
		}
	}
	
	public String getOpenOptionText() {
		//Inquire about available jobs
		return "\"Do you have any work for me?\"";
	}
	
	
	protected float getUpdateInterval() {
		return UPDATE_INTERVAL;
	}
	
	public List<HubMission> getOfferedMissions() {
		return offered;
	}
	
	
	protected transient Random missionGenRandom = new Random();
	protected long seed = 0;
	protected long lastUpdated = Long.MIN_VALUE;
	protected long lastUpdatedSeeds = 0;
	protected float daysSinceLastUpdate = 0f;
	public void updateOfferedMissions(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		updateMissionCreatorsFromSpecs();
		
		float daysElapsed = Global.getSector().getClock().getElapsedDaysSince(lastUpdated);
		if (lastUpdated <= Long.MIN_VALUE) daysElapsed = getUpdateInterval();
		daysSinceLastUpdate += daysElapsed;
		lastUpdated = Global.getSector().getClock().getTimestamp();
		
		timeout.advance(daysElapsed);
		
		if (daysSinceLastUpdate > getUpdateInterval() || seed == 0) {
			daysSinceLastUpdate = 0;
			//seed = Misc.genRandomSeed();
			seed = BarEventManager.getInstance().getSeed(null, person, "" + lastUpdatedSeeds);
			
			recentlyAcceptedTimeout.clear();
			for (HubMissionCreator creator : creators) {
				//creator.updateSeed();
				creator.setSeed(BarEventManager.getInstance().getSeed(null, person, 
								creator.getSpecId() + "" + lastUpdatedSeeds));
			}
			lastUpdatedSeeds = Global.getSector().getClock().getTimestamp();
		}
		
		missionGenRandom = new Random(seed);
		
		//missionGenRandom = Misc.random;
		
		
		WeightedRandomPicker<HubMissionCreator> picker = new WeightedRandomPicker<HubMissionCreator>(missionGenRandom);
		WeightedRandomPicker<HubMissionCreator> priority = new WeightedRandomPicker<HubMissionCreator>(missionGenRandom);
		float rel = person.getRelToPlayer().getRel();
		
		Set<String> completed = new LinkedHashSet<String>();
		for (HubMissionCreator creator : creators) {
			if (creator.getNumCompleted() > 0) completed.add(creator.getSpecId());
		}
		
		for (HubMissionCreator creator : creators) {
//			if (creator.getSpecId().equals("hijack")) {
//				System.out.println("fweefwew");
//			}
			// keep timeout missions so that after the player accepts a mission
			// the re-generated set using missionGenRandom remains the same (minus the accepted mission)
			if (timeout.contains(creator.getSpecId()) && 
					!recentlyAcceptedTimeout.contains(creator.getSpecId())) continue;
			
			if (!creator.isActive()) continue;
			
			if (creator.getSpec().hasTag(Tags.MISSION_NON_REPEATABLE) &&
					creator.getNumCompleted() > 0) {
				continue;
			}
			
			if (!DebugFlags.ALLOW_ALL_CONTACT_MISSIONS && !creator.getSpec().completedMissionsMatch(completed)) {
				continue;
			}
			
			
			if (!DebugFlags.ALLOW_ALL_CONTACT_MISSIONS) {
				if (!creator.matchesRep(rel)) continue;
				if (!DebugFlags.ALLOW_ALL_CONTACT_MISSIONS) {
					if (person.getImportance().ordinal() < creator.getSpec().getImportance().ordinal()) continue;
				}
			}
			
			float w = creator.getFrequencyWeight();
			if (creator.isPriority()) { 
				priority.add(creator, w);
			} else {
				picker.add(creator, w);
			}
		}
		
		
		int bonusMissions = 0;
		if (person.getMemoryWithoutUpdate().contains(NUM_BONUS_MISSIONS)) {
			float bonus = person.getMemoryWithoutUpdate().getFloat(NUM_BONUS_MISSIONS);
			float rem = bonus - (int) bonus;
			bonusMissions = (int) bonus;
			if (missionGenRandom.nextFloat() < rem) {
				bonusMissions++;
			}
		}
		
		int num = MIN_TO_SHOW + missionGenRandom.nextInt(MAX_TO_SHOW - MIN_TO_SHOW + 1) + bonusMissions;
		if (num > MAX_TO_SHOW_WITH_BONUS) num = MAX_TO_SHOW_WITH_BONUS;
		if (num < 1 && MIN_TO_SHOW > 0) num = 1;
		if (DebugFlags.BAR_DEBUG) num = 8;
		//num = 5;
		
		if (person.getMemoryWithoutUpdate().getBoolean(CONTACT_SUSPENDED)) {
			num = 0;
		}
		
		offered = new ArrayList<HubMission>();

//		resetMissionAngle(person, person.getMarket());
//		getMissionAngle(person, person.getMarket(), missionGenRandom);
		
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		ip.resetExcludeFromGetPerson();
		// existing contacts don't get picked as targets for missions
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(ContactIntel.class)) {
			ip.excludeFromGetPerson(((ContactIntel)intel).getPerson());
		}
		
		while ((!picker.isEmpty() || !priority.isEmpty()) && offered.size() < num) {
			HubMissionCreator creator = priority.pickAndRemove();
			if (creator == null) {
				creator = picker.pickAndRemove();
			}
			
			// so that if a player accepted a mission, overall set will be the same, minus the accepted missions
			if (recentlyAcceptedTimeout.contains(creator.getSpecId())) {
				num--;
				continue;
			}
			
			creator.updateRandom();
			HubMission mission = creator.createHubMission(this);
			if (mission != null) {
				mission.setHub(this);
				mission.setCreator(creator);
				mission.setGenRandom(creator.getGenRandom());
				//mission.setGenRandom(Misc.random);
				mission.createAndAbortIfFailed(getPerson().getMarket(), false);
				//mission.setGenRandom(null);
			}
			if (mission == null || mission.isMissionCreationAborted()) continue;
			offered.add(mission);
			mission.updateInteractionData(dialog, memoryMap);
			
			float dur = creator.getWasShownTimeoutDuration();
			timeout.add(creator.getSpecId(), dur);
			
			//getCreatedMissionsList(person, person.getMarket()).add((BaseHubMission) mission);
		}
		
		ip.resetExcludeFromGetPerson();
		
		//clearCreatedMissionsList(person, person.getMarket());
	}
	
	public PersonAPI getPerson() {
		return person;
	}
	public void setPerson(PersonAPI person) {
		this.person = person;
	}
	
	
	public static List<PersonMissionSpec> getMissionsForPerson(PersonAPI person) {
		List<PersonMissionSpec> result = new ArrayList<PersonMissionSpec>();
		
		Set<String> personTags = new HashSet<String>(person.getTags());
		personTags.add(person.getFaction().getId());
		
		for (PersonMissionSpec spec : Global.getSettings().getAllMissionSpecs()) {
			if (spec.getPersonId() != null && !spec.getPersonId().equals(person.getId())) continue;
			if (!spec.tagsMatch(personTags)) continue;
			
			if (spec.getPersonId() == null && spec.getTagsAll().isEmpty() &&
					spec.getTagsAny().isEmpty() && spec.getTagsNotAny().isEmpty()) continue;
			
			result.add(spec);
		}
		return result;
	}

	
	public static String MISSION_ANGLE_KEY = "$core_missionAngle";
	
//	public static void resetMissionAngle(PersonAPI person, MarketAPI market) {
//		MemoryAPI mem;
//		if (market != null) {
//			mem = market.getMemoryWithoutUpdate();
//		} else if (person!= null) {
//			mem = person.getMemoryWithoutUpdate();
//		} else {
//			return;
//		}
//		mem.unset(MISSION_ANGLE_KEY);
//	}
	
	//public static float getMissionAngle(PersonAPI person, MarketAPI market, Random random) {
	public static float getMissionAngle(PersonAPI person, MarketAPI market) {
		MemoryAPI mem;
		if (market != null) {
			mem = market.getMemoryWithoutUpdate();
		} else if (person!= null) {
			mem = person.getMemoryWithoutUpdate();
		} else {
			Random random = Misc.getRandom(BarEventManager.getInstance().getSeed(null, null, null), 11);
			return random.nextFloat() * 360f;
		}
		
		float angle;
		if (mem.contains(MISSION_ANGLE_KEY)) {
			angle = mem.getFloat(MISSION_ANGLE_KEY);
		} else {
			StarSystemUnexploredReq unexplored = new StarSystemUnexploredReq();
			Vector2f loc = Global.getSector().getPlayerFleet().getLocationInHyperspace();
			
			SectorEntityToken entity = null;
			if (market != null) entity = market.getPrimaryEntity();
			if (entity == null && person != null && person.getMarket() != null) {
				entity = person.getMarket().getPrimaryEntity();
			}
			Random random = Misc.getRandom(BarEventManager.getInstance().getSeed(entity, person, null), 11);
			WeightedRandomPicker<Float> picker = new WeightedRandomPicker<Float>(random);
			for (StarSystemAPI system : Global.getSector().getStarSystems()) {
				float dir = Misc.getAngleInDegrees(loc, system.getLocation());
				if (unexplored.systemMatchesRequirement(system)) {
					picker.add(dir, 1f);
				} else {
					float days = system.getDaysSinceLastPlayerVisit();
					float weight = days / 1000f;
					if (weight < 0.01f) weight = 0.01f;
					if (weight > 1f) weight = 1f;
					picker.add(dir, weight * 0.01f);
				}
			}
			
			angle = picker.pick();
			
			mem.set(MISSION_ANGLE_KEY, angle, 0f);
		}
		return angle;
	}
	
	
//	public static String CREATED_MISSIONS_KEY = "$core_createdMissions";
//	@SuppressWarnings("unchecked")
//	public static List<BaseHubMission> getCreatedMissionsList(PersonAPI person, MarketAPI market) {
//		MemoryAPI mem;
//		if (market != null) {
//			mem = market.getMemoryWithoutUpdate();
//		} else if (person!= null) {
//			mem = person.getMemoryWithoutUpdate();
//		} else {
//			return new ArrayList<BaseHubMission>();
//		}
//		List<BaseHubMission> list = (List<BaseHubMission>) mem.get(CREATED_MISSIONS_KEY);
//		if (list == null) {
//			list = new ArrayList<BaseHubMission>();
//			mem.set(CREATED_MISSIONS_KEY, list);
//		}
//		return list;
//	}
//	
//	public static void clearCreatedMissionsList(PersonAPI person, MarketAPI market) {
//		MemoryAPI mem;
//		if (market != null) {
//			mem = market.getMemoryWithoutUpdate();
//		} else if (person!= null) {
//			mem = person.getMemoryWithoutUpdate();
//		} else {
//			return;
//		}
//		mem.unset(CREATED_MISSIONS_KEY);
//	}
}









