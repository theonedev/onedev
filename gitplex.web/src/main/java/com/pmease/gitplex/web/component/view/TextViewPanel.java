package com.pmease.gitplex.web.component.view;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.git.GitText;

@SuppressWarnings("serial")
public class TextViewPanel extends Panel {

	@SuppressWarnings("unused")
	private final BlobRenderInfo blobInfo;
	
	@SuppressWarnings("unused")
	private final GitText blobText;
	
	public TextViewPanel(String id, BlobRenderInfo blobInfo, GitText blobText) {
		super(id);
		
		this.blobInfo = blobInfo;
		this.blobText = blobText;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

}
