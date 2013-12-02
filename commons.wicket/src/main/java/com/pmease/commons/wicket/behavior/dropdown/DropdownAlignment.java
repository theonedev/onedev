package com.pmease.commons.wicket.behavior.dropdown;

import java.io.Serializable;

import org.apache.wicket.Component;

public class DropdownAlignment implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public enum IndicatorMode {SHOW, HIDE, AUTO}

	private Component target;
	
	private int targetX = 0, targetY = 100, dropdownX = 0, dropdownY = 0;
	
	private int gap = -1;
	
	private IndicatorMode indicatorMode = IndicatorMode.AUTO;

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
	
	public int gap() {
		return gap;
	}

	public DropdownAlignment gap(int gap) {
		this.gap = gap;
		return this;
	}

	public DropdownAlignment target(Component target) {
		this.target = target;
		return this;
	}

	public DropdownAlignment targetX(int targetX) {
		this.targetX = targetX;
		return this;
	}

	public DropdownAlignment targetY(int targetY) {
		this.targetY = targetY;
		return this;
	}

	public DropdownAlignment dropdownX(int dropdownX) {
		this.dropdownX = dropdownX;
		return this;
	}

	public DropdownAlignment dropdownY(int dropdownY) {
		this.dropdownY = dropdownY;
		return this;
	}

	public Component target() {
		return target;
	}

	public int targetX() {
		return targetX;
	}

	public int targetY() {
		return targetY;
	}

	public int dropdownX() {
		return dropdownX;
	}

	public int dropdownY() {
		return dropdownY;
	}

	public IndicatorMode indicatorMode() {
		return indicatorMode;
	}

	public DropdownAlignment indicatorMode(IndicatorMode indicatorMode) {
		this.indicatorMode = indicatorMode;
		return this;
	}

}
