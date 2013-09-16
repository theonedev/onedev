package com.pmease.gitop.web;

import com.pmease.commons.editable.annotation.Editable;

@SuppressWarnings("serial")
@Editable
public class TinaBean extends ChildBean {
	private boolean legInjured;

	@Editable
	public boolean isLegInjured() {
		return legInjured;
	}

	public void setLegInjured(boolean legInjured) {
		this.legInjured = legInjured;
	}
	
}
