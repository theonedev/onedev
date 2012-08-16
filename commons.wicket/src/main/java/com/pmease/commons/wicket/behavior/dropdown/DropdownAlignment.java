package com.pmease.commons.wicket.behavior.dropdown;

import java.io.Serializable;

import org.apache.wicket.Component;

public class DropdownAlignment implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Component target;
	
	private int targetX = 0, targetY = 100, dropdownX = 0, dropdownY = 0;

	public DropdownAlignment() {
	}
	
	public DropdownAlignment(Component target) {
		this.target = target;
	}
	
	public DropdownAlignment(int targetX, int targetY, int dropdownX, int dropdownY) {
		this(null, targetX, targetY, dropdownX, dropdownY);
	}
	
	public DropdownAlignment(Component target, int targetX, int targetY, int dropdownX, int dropdownY) {
		this.target = target;
		this.targetX = targetX; this.targetY = targetY;
		this.dropdownX = dropdownX; this.dropdownY = dropdownY;
	}
	
	public Component getTarget() {
		return target;
	}

	public int getTargetX() {
		return targetX;
	}

	public int getTargetY() {
		return targetY;
	}

	public int getDropdownX() {
		return dropdownX;
	}

	public int getDropdownY() {
		return dropdownY;
	}

}
