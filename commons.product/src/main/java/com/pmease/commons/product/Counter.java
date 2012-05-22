package com.pmease.commons.product;

public class Counter {
	
	private int value;
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public void increase() {
		value++;
	}
}