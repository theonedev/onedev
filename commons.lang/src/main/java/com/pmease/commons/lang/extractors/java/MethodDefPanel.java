package com.pmease.commons.lang.extractors.java;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.util.Range;
import com.pmease.commons.wicket.component.EmphasizeAwareLabel;

@SuppressWarnings("serial")
public class MethodDefPanel extends Panel {

	private final MethodDef methodDef;
	
	private final Range matchRange;
	
	public MethodDefPanel(String id, MethodDef methodDef, Range matchRange) {
		super(id);
		this.methodDef = methodDef;
		this.matchRange = matchRange;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new EmphasizeAwareLabel("name", methodDef.getName(), matchRange));
		add(new Label("params", methodDef.getParams()));
		add(new Label("type", methodDef.getType()).setVisible(methodDef.getType()!=null));
	}

}
