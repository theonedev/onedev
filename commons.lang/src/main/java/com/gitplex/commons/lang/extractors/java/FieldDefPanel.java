package com.gitplex.commons.lang.extractors.java;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.commons.util.Range;
import com.gitplex.commons.wicket.component.HighlightableLabel;

@SuppressWarnings("serial")
public class FieldDefPanel extends Panel {

	private final FieldDef fieldDef;
	
	private final Range matchRange;
	
	public FieldDefPanel(String id, FieldDef fieldDef, Range matchRange) {
		super(id);
		this.fieldDef = fieldDef;
		this.matchRange = matchRange;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new HighlightableLabel("name", fieldDef.getName(), matchRange));
		
		add(new Label("type", fieldDef.getType()).setVisible(fieldDef.getType()!=null));
	}

}
