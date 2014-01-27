package com.pmease.gitop.web.component.comment;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class CommentEditor extends Panel {

	public CommentEditor(String id, IModel<String> textModel) {
		super(id, textModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		
	}
}
