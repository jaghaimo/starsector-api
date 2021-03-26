package com.fs.starfarer.api.impl.campaign.missions.cb;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepRewards;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission.Abortable;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission.EntityAdded;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BaseCustomBountyCreator implements CustomBountyCreator {
	public static float DEFAULT_FREQUENCY = 10f;
	
	public float getBountyDays() {
		return CBStats.DEFAULT_DAYS;
	}
	
	public void addTargetLocationAndDescriptionBulletPoint(TooltipMakerAPI info,
			Color tc, float pad, HubMissionWithBarEvent mission,
			CustomBountyData data) {
		if (data.system != null) {
			info.addPara("Target is in the " + data.system.getNameWithLowercaseTypeShort() + "", tc, pad);
		}
	}
	
	public void addTargetLocationAndDescription(TooltipMakerAPI info, float width, float height, HubMissionWithBarEvent mission, CustomBountyData data) {
		float opad = 10f;
		float pad = 3f;
		Color h = Misc.getHighlightColor();
		if (data.system != null) {
			info.addPara("The target is located in the " + data.system.getNameWithLowercaseType() + ".", opad);
		}
	}
	
	public void addIntelAssessment(TextPanelAPI text, HubMissionWithBarEvent mission, CustomBountyData data) {
		float opad = 10f;
		List<FleetMemberAPI> list = new ArrayList<FleetMemberAPI>();
		List<FleetMemberAPI> members = data.fleet.getFleetData().getMembersListCopy();
		int max = 7;
		int cols = 7;
		float iconSize = 440 / cols;
		Color h = Misc.getHighlightColor();
		
		
		for (FleetMemberAPI member : members) {
			if (list.size() >= max) break;
			
			if (member.isFighterWing()) continue;
			
			FleetMemberAPI copy = Global.getFactory().createFleetMember(FleetMemberType.SHIP, member.getVariant());
			if (member.isFlagship()) {
				copy.setCaptain(data.fleet.getCommander());
			}
			list.add(copy);
		}
		
		if (!list.isEmpty()) {
			TooltipMakerAPI info = text.beginTooltip();
			info.setParaSmallInsignia();
			info.addPara(Misc.ucFirst(mission.getPerson().getHeOrShe()) + " taps a data pad, and " +
						 "an intel assessment shows up on your tripad.", 0f);
			info.addShipList(cols, 1, iconSize, data.fleet.getFaction().getBaseUIColor(), list, opad);
			
			int num = members.size() - list.size();
			
			if (num < 5) num = 0;
			else if (num < 10) num = 5;
			else if (num < 20) num = 10;
			else num = 20;
			
			if (num > 1) {
				info.addPara("The assessment notes the fleet may contain upwards of %s other ships" +
						" of lesser significance.", opad, h, "" + num);
			} else if (num > 0) {
				info.addPara("The assessment notes the fleet may contain several other ships" +
						" of lesser significance.", opad);
			} else {
				info.addPara("It appears to contain complete information about the scope of the assignment.", opad);
			}
			text.addTooltip();
		}
		return;
	}

	public void addFleetDescription(TooltipMakerAPI info, float width, float height, HubMissionWithBarEvent mission, CustomBountyData data) {
//		if (hideoutLocation != null) {
//			SectorEntityToken fake = hideoutLocation.getContainingLocation().createToken(0, 0);
//			fake.setOrbit(Global.getFactory().createCircularOrbit(hideoutLocation, 0, 1000, 100));
//			
//			String loc = BreadcrumbSpecial.getLocatedString(fake);
//			loc = loc.replaceAll("orbiting", "hiding out near");
//			loc = loc.replaceAll("located in", "hiding out in");
//			String sheIs = "She is";
//			if (person.getGender() == Gender.MALE) sheIs = "He is";
//			info.addPara(sheIs + " rumored to be " + loc + ".", opad);
//		}
		
		PersonAPI person = data.fleet.getCommander();
		FactionAPI faction = person.getFaction();
		int cols = 7;
		float iconSize = width / cols;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		
		if (DebugFlags.PERSON_BOUNTY_DEBUG_INFO) {

		}
		boolean deflate = false;
		if (!data.fleet.isInflated()) {
			data.fleet.inflateIfNeeded();
			deflate = true;
		}
		
		List<FleetMemberAPI> list = new ArrayList<FleetMemberAPI>();
		Random random = new Random(person.getNameString().hashCode() * 170000);
		
		List<FleetMemberAPI> members = data.fleet.getFleetData().getMembersListCopy();
		int max = 7;
		for (FleetMemberAPI member : members) {
			if (list.size() >= max) break;
			
			if (member.isFighterWing()) continue;
			
//			float prob = (float) member.getFleetPointCost() / 20f;
//			prob += (float) max / (float) members.size();
//			if (member.isFlagship()) prob = 1f;
//			//if (members.size() <= max) prob = 1f;
//			
//			if (random.nextFloat() > prob) continue;
			
			FleetMemberAPI copy = Global.getFactory().createFleetMember(FleetMemberType.SHIP, member.getVariant());
			if (member.isFlagship()) {
				copy.setCaptain(person);
			}
			list.add(copy);
		}
		
		if (!list.isEmpty()) {
			info.addPara("The bounty posting contains partial intel on some of the ships in the target fleet.", opad);
			info.addShipList(cols, 1, iconSize, faction.getBaseUIColor(), list, opad);
			
			int num = members.size() - list.size();
			//num = Math.round((float)num * (1f + random.nextFloat() * 0.5f));
			
			if (num < 5) num = 0;
			else if (num < 10) num = 5;
			else if (num < 20) num = 10;
			else num = 20;
			
			if (num > 1) {
				info.addPara("The intel assessment notes the fleet may contain upwards of %s other ships" +
						" of lesser significance.", opad, h, "" + num);
			} else if (num > 0) {
				info.addPara("The intel assessment notes the fleet may contain several other ships" +
						" of lesser significance.", opad);
			}
		}
		
		if (deflate) {
			data.fleet.deflate();
		}
	}

	public float getFrequency(HubMissionWithBarEvent mission, int difficulty) {
		PersonAPI person = mission.getPerson();
		if (!isRepeatableGlobally() && getNumCompletedGlobal() > 0) return 0f;
		if (!isRepeatablePerPerson() && getNumCompletedForPerson(person) > 0) return 0f;
		
		float mult = 1f;
		if (isReduceFrequencyBasedOnNumberOfCompletions()) {
			mult = 1f / (1f + (float) getNumCompletedGlobal() * 0.5f);
		}
		return DEFAULT_FREQUENCY * mult;
	}
	
	public String getNumCompletedPerPersonKey() {
		return "$" + getId() + "_numCompleted";
	}
	public String getNumCompletedGlobalKey() {
		return "$" + getId() + "_numCompleted";
	}
	
	
	protected boolean isRepeatablePerPerson() {
		return true;
	}
	protected boolean isRepeatableGlobally() {
		return true;
	}
	protected boolean isReduceFrequencyBasedOnNumberOfCompletions() {
		return false;
	}

	public String getId() {
		return getClass().getSimpleName();
	}

	public boolean systemMatchesRequirement(StarSystemAPI system) {
		return true;
	}

	public float getRepFaction() {
		return RepRewards.TINY;
	}

	public float getRepPerson() {
		return RepRewards.MEDIUM;
	}

	protected static Object STAGE = new Object();
	protected void beginFleet(HubMissionWithBarEvent mission, CustomBountyData data) {
		mission.beginStageTrigger(STAGE);
	}
	
	protected CampaignFleetAPI createFleet(HubMissionWithBarEvent mission, CustomBountyData data) {
		mission.triggerMakeFleetIgnoreOtherFleetsExceptPlayer();
		mission.triggerFleetOnlyEngageableWhenVisibleToPlayer();
		mission.endTrigger();
		
		List<Abortable> before = new ArrayList<Abortable>(mission.getChanges());
		List<CampaignFleetAPI> fleets = mission.runStageTriggersReturnFleets(STAGE);
		if (fleets.isEmpty()) return null;
		
		CampaignFleetAPI fleet = fleets.get(0);
		mission.getChanges().add(new EntityAdded(fleet)); // so it's removed when the mission is abort()ed
		
		for (Abortable curr : mission.getChanges()) {
			if (!before.contains(curr)) {
				data.abortWhenOtherVersionAccepted.add(curr);
			}
		}
		return fleet;
	}

	public void notifyCompleted(HubMissionWithBarEvent mission, CustomBountyData data) {
		PersonAPI person = mission.getPerson();
		
		String key = getNumCompletedGlobalKey();
		Integer num = (Integer) Global.getSector().getMemoryWithoutUpdate().get(key);
		if (num == null) num = 0;
		num++;
		Global.getSector().getMemoryWithoutUpdate().set(key, num);
		
		key = getNumCompletedPerPersonKey();
		num = (Integer) person.getMemoryWithoutUpdate().get(key);
		if (num == null) num = 0;
		num++;
		person.getMemoryWithoutUpdate().set(key, num);
	}

	public void notifyFailed(HubMissionWithBarEvent mission, CustomBountyData data) {
		
	}
	
	public int getNumCompletedForPerson(PersonAPI person) {
		String key = getNumCompletedPerPersonKey();
		Integer num = (Integer) person.getMemoryWithoutUpdate().get(key);
		if (num == null) num = 0;
		return num;
	}
	
	public int getNumCompletedGlobal() {
		String key = getNumCompletedGlobalKey();
		Integer num = (Integer) Global.getSector().getMemoryWithoutUpdate().get(key);
		if (num == null) num = 0;
		return num;
	}


	public int getMinDifficulty() {
		return MIN_DIFFICULTY;
	}
	
	public int getMaxDifficulty() {
		return MAX_DIFFICULTY;
	}

	public CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
		return null;
	}
	
	public void notifyAccepted(MarketAPI createdAt, HubMissionWithBarEvent mission, CustomBountyData data) {
		
	}

	public StarSystemAPI getSystemWithNoTimeLimit(CustomBountyData data) {
		return data.system;
	}
	
	public String getBaseBountyName(HubMissionWithBarEvent mission, CustomBountyData data) {
		return "Bounty";
	}
	
	public String getBountyNamePostfix(HubMissionWithBarEvent mission, CustomBountyData data) {
		return null;
	}
	
	public void setRepChangesBasedOnDifficulty(CustomBountyData data, int difficulty) {
		if (difficulty <= 3) {
			data.repPerson = RepRewards.SMALL;
			data.repFaction = RepRewards.TINY;
		} else if (difficulty <= 7) {
			data.repPerson = RepRewards.MEDIUM;
			data.repFaction = RepRewards.SMALL;
		} else {
			data.repPerson = RepRewards.HIGH;
			data.repFaction = RepRewards.SMALL;
		}
	}
	
	public void updateInteractionData(HubMissionWithBarEvent mission, CustomBountyData data) {
		
	}

}




