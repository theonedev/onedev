package com.pmease.gitplex.web.component.view;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class TextViewPanel extends Panel {

	private final IModel<TextRenderInfo> textModel;
	
	public TextViewPanel(String id, IModel<TextRenderInfo> textModel) {
		super(id);
		
		this.textModel = textModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

	@Override
	protected void onDetach() {
		textModel.detach();
		super.onDetach();
	}

}
