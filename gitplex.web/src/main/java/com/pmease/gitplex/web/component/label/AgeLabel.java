package com.pmease.gitplex.web.component.label;

import java.util.Date;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class AgeLabel extends Label {

	public AgeLabel(String id, final IModel<Date> model) {
		super(id, new AgeModel(model));
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		String classes = tag.getAttribute("class");
		if (classes == null) {
			classes = "";
		}
		if (!classes.contains("age")) {
			classes += " age";
		}
		if (!classes.contains("has-tip")) {
			classes += " has-tip";
		}
		
		tag.put("class", classes);
	}
	
}
