package com.fs.starfarer.api.impl.campaign.plog;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;

public class PLSnapshot {

	protected Map<String, Long> data = new HashMap<String, Long>();
	protected long timestamp = 0;
	
	public PLSnapshot(String in) {
		setFromString(in);
	}
	
	public PLSnapshot() {
		timestamp = Global.getSector().getClock().getTimestamp();
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Map<String, Long> getData() {
		return data;
	}

	public void setFromString(String in) {
		String [] parts = in.split("\\|");
		
		data = new HashMap<String, Long>();
		
		boolean first = true;
		for (String p : parts) {
			if (first) {
				timestamp = Long.parseLong(p);
				first = false;
				continue;
			}
			String [] p2 = p.split(":");
			String key = p2[0];
			long value = Long.parseLong(p2[1]);
			data.put(key, value);
		}
	}
	
	public String getString() {
		String str = "";
		
		str += timestamp + "|";
		
		for (String key : data.keySet()) {
			str += key + ":" + data.get(key) + "|";
		}
		if (!str.isEmpty()) {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	
}
