package com.pmease.commons.lang.java;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class FieldDefPanel extends Panel {

	private final FieldDef fieldDef;
	
	public FieldDefPanel(String id, FieldDef fieldDef) {
		super(id);
		this.fieldDef = fieldDef;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("name", fieldDef.getName()));
		
		add(new Label("type", fieldDef.getType()).setVisible(fieldDef.getType()!=null));
	}

}
