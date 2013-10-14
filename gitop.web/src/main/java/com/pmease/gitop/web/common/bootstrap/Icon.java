package com.pmease.gitop.web.common.bootstrap;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

public class Icon extends WebMarkupContainer {

	private static final long serialVersionUID = 1L;

	public Icon(String id, final IconType type) {
		super(id);
		add(type.newCssClassNameModifier());
	}

	@SuppressWarnings("serial")
	public Icon(String id, final IModel<IconType> type) {
		super(id);
		add(AttributeAppender.append("class",
				new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return type.getObject().cssClassName();
					}
				}));
	}
}