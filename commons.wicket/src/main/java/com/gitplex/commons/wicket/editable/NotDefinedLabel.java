package com.gitplex.commons.wicket.editable;

import org.apache.wicket.markup.html.basic.Label;

@SuppressWarnings("serial")
public class NotDefinedLabel extends Label {

	public NotDefinedLabel(String id) {
		super(id, "<i>Not Defined</i>");
		setEscapeModelStrings(false);
	}

}
