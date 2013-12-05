package com.pmease.gitop.web.common.bootstrap;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class Icon extends WebMarkupContainer {

	private static final long serialVersionUID = 1L;

	public Icon(String id, final String iconClass) {
		this(id, Model.of(iconClass));
	}

	@SuppressWarnings("serial")
	public Icon(String id, final IModel<String> iconClass) {
		super(id);
		
		add(AttributeAppender.append("class",
				new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return "icon " + iconClass.getObject();
					}
				}));
	}
}