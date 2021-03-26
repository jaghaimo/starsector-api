package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * ShowResCost <commotity id> <quantity> <optional: consumed> (repeated); optional: width of each panel
 * 
 * ShowResCost crew 1000 supplies 100 true 200
 * = requires 1000 crew, 100 supplis (consumed), each item is 200 pixels wide   
 */
public class ShowResCost extends BaseCommandPlugin {

	public static class ResData {
		String id;
		int qty;
		boolean consumed;
	}
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		List<ResData> data = new ArrayList<ResData>();

		float widthOverride = -1f;
		for (int i = 0; i < params.size(); i++) {
			Token t = params.get(i);
			
			boolean commodityId = !t.isBoolean(memoryMap) && !t.isFloat(memoryMap);
			if (commodityId) {
				ResData curr = new ResData();
				curr.id = t.getString(memoryMap);
				
				i++;
				t = params.get(i);
				curr.qty = (int) t.getFloat(memoryMap);
				
				if (params.size() > i + 1) {
					t = params.get(i + 1);
					if (t.isBoolean(memoryMap)) {
						curr.consumed = t.getBoolean(memoryMap);
						i++;
					}
				}
				data.add(curr);
				continue;
			} else if (t.isFloat(memoryMap)) {
				widthOverride = t.getFloat(memoryMap);
				break;
			}
		}
		
		String [] ids = new String [data.size()];
		int [] qty = new int [data.size()];
		boolean [] consumed = new boolean [data.size()];
		
		for (int i = 0; i < data.size(); i++) {
			ResData curr = data.get(i);
			ids[i] = curr.id;
			qty[i] = curr.qty;
			consumed[i] = curr.consumed;
		}
		
		Misc.showCost(dialog.getTextPanel(), "Resources: required (available)", true, widthOverride, null, null, ids, qty, consumed);
		
		return true;
	}

}


