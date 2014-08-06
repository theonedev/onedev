package com.pmease.gitplex.web.component.diff;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class FileDiffPanel extends Panel {

	private final IModel<byte[]> originalContentModel;
	
	private final IModel<byte[]> revisedContentModel;
	
	public FileDiffPanel(String id, IModel<byte[]> originalContentModel, IModel<byte[]> revisedContentModel) {
		super(id);
		
		this.originalContentModel = originalContentModel;
		this.revisedContentModel = revisedContentModel;
	}

	@Override
	protected void onDetach() {
		originalContentModel.detach();
		revisedContentModel.detach();
		super.onDetach();
	}

}
