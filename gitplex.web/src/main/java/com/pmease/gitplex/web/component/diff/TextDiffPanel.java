package com.pmease.gitplex.web.component.diff;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.web.component.view.TextRenderInfo;

@SuppressWarnings("serial")
public class TextDiffPanel extends Panel {

	private final IModel<TextRenderInfo> originalTextModel;
	
	private final IModel<TextRenderInfo> revisedTextModel;
	
	public TextDiffPanel(String id, IModel<TextRenderInfo> originalTextModel, IModel<TextRenderInfo> revisedTextModel) {
		super(id);
		
		this.originalTextModel = originalTextModel;
		this.revisedTextModel = revisedTextModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

	@Override
	protected void onDetach() {
		originalTextModel.detach();
		revisedTextModel.detach();
		super.onDetach();
	}

}
