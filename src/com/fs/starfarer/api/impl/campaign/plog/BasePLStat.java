package com.fs.starfarer.api.impl.campaign.plog;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.util.Misc;

public class BasePLStat implements PLStat {
	
	List<Long> accrued = new ArrayList<Long>();
	
	protected Object readResolve() {
		if (accrued == null) {
			accrued = new ArrayList<Long>();
		}
		return this;
	}
	
	
	public Color getGraphColor() {
		return Color.white;
	}

	public String getGraphLabel() {
		return "Override getGraphLabel()";
	}

	public String getId() {
		return getClass().getSimpleName();
	}

	public void accrueValue() {
		accrued.add(getCurrentValue());
	}

	public long getValueForAllAccrued() {
		long prev = PlaythroughLog.getInstance().getPrevValue(getId());
		long best = 0;
		long maxDiff = Integer.MIN_VALUE;
		for (Long curr : accrued) {
			long diff = Math.abs(curr - prev);
			if (diff > maxDiff) {
				maxDiff = diff;
				best = curr;
			}
		}
		accrued.clear();
		
		return best;
	}
	
	public long getCurrentValue() {
		return 0;
	}


	public long getGraphMax() {
		return -1;
	}

	public String getHoverText(long value) {
		return getGraphLabel() + ": " + Misc.getWithDGS(value);
	}


	public String getSharedCategory() {
		return null;
	}

}


