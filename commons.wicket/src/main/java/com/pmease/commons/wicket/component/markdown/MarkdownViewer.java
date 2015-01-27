package com.pmease.commons.wicket.component.markdown;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

@SuppressWarnings("serial")
public class MarkdownViewer extends Panel {
	private static final long serialVersionUID = 1L;

	public MarkdownViewer(String id, IModel<String> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("html", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return "";
			}
		}).setEscapeModelStrings(false));

	}
	
}
