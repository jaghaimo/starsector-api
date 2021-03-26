package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

/**
 * 
 */
public class BaseAICoreOfficerPluginImpl implements AICoreOfficerPlugin {
	public PersonAPI createPerson(String aiCoreId, String factionId, Random random) {
		return null;
	}

	public void createPersonalitySection(PersonAPI person, TooltipMakerAPI tooltip) {
		float opad = 10f; 
		Color text = person.getFaction().getBaseUIColor();
		Color bg = person.getFaction().getDarkUIColor();
		CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(person.getAICoreId());
		
		tooltip.addSectionHeading("Personality: fearless", text, bg, Alignment.MID, 20);
		tooltip.addPara("In combat, the " + spec.getName() + " is single-minded and determined. " +
				"In a human captain, its traits might be considered reckless. In a machine, they're terrifying.", opad);
	}
	

//	public StoryPointActionDelegate createIntegrateDelegate(final PersonAPI person, final FleetMemberAPI member) {
//		return new StoryPointActionDelegate() {
//			public String getTitle() {
//				return "Integrating " + person.getNameString() + " into " + member.getShipName();
//			}
//			public TextPanelAPI getTextPanel() {
//				return null;
//			}
//			public float getBonusXPFraction() {
//				return 1f;
//			}
//			public void createDescription(TooltipMakerAPI info) {
//				float opad = 10f;
//				float pad = 3f;
//				float small = 5f;
//				Color h = Misc.getHighlightColor();
//				
//				info.addPara("Fully integrate this AI core into the ship, giving it improved access to all " +
//							 "subsystems.", 0f);
//				
//				info.addPara(BaseIntelPlugin.BULLET + "Increases the AI core's level by %s and grants an additional skill", small, h, "1");
//				info.addPara(BaseIntelPlugin.BULLET + "Can not be removed by any means short of scuttling the ship", 0f);
//			}
//			
//			public void confirm() {
//				OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
//				List<String> skills = plugin.pickLevelupSkills(person, null);
//				if (!skills.isEmpty()) {
//					Misc.setUnremovable(person, true);
//					person.getStats().setLevel(person.getStats().getLevel() + 1);
//					person.getStats().setSkillLevel(skills.get(0), 2);
//				}
//			}
//		};
//	}

}




