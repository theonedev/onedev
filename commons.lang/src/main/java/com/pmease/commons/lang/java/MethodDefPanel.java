package com.pmease.commons.lang.java;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class MethodDefPanel extends Panel {

	private final MethodDef methodDef;
	
	public MethodDefPanel(String id, MethodDef methodDef) {
		super(id);
		this.methodDef = methodDef;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("name", methodDef.getName()));
		add(new Label("params", methodDef.getParams()));
		add(new Label("type", methodDef.getType()).setVisible(methodDef.getType()!=null));
	}

}
