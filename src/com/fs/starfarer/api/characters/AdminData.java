package com.fs.starfarer.api.characters;

import com.fs.starfarer.api.campaign.econ.MarketAPI;



public class AdminData {
	private PersonAPI person;
	private MarketAPI market;
	
	
	public AdminData(PersonAPI person) {
		this.person = person;
	}

	public PersonAPI getPerson() {
		return person;
	}

	public void setPerson(PersonAPI person) {
		this.person = person;
	}
	

	public void setMarket(MarketAPI market) {
		this.market = market;
	}
	
	public MarketAPI getMarket() {
		return market;
	}

	protected void assign(MarketAPI market) {

	}
	
	protected void unassign(MarketAPI market) {
		
	}
}
