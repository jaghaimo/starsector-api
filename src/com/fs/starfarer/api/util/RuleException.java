package com.fs.starfarer.api.util;

public class RuleException extends RuntimeException {

	public RuleException(String string) {
		super(string);
	}
	
	public RuleException(Exception e) {
		super(e);
	}

}
