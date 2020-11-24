package com.fs.starfarer.api.characters;

public class FullName {
	
	public static enum Gender{
		MALE,
		FEMALE,
		ANY,
	}
	
	private String first, last;
	private Gender gender;

	public FullName(String first, String last, Gender gender) {
		this.first = first;
		this.last = last;
		this.gender = gender;
	}
	public Gender getGender() {
		return gender;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	public String getFirst() {
		return first;
	}
	public void setFirst(String first) {
		this.first = first;
	}
	public String getLast() {
		return last;
	}
	public void setLast(String last) {
		this.last = last;
	}
	public String getFullName() {
		return (first + " " + last).trim();
	}
}