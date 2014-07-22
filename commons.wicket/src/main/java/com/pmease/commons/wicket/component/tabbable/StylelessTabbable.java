package com.pmease.commons.wicket.component.tabbable;

import java.util.List;

@SuppressWarnings("serial")
public class StylelessTabbable extends Tabbable {

	public StylelessTabbable(String id, List<Tab> tabs) {
		super(id, tabs);
	}

	@Override
	protected String getCssClasses() {
		return "";
	}

}
