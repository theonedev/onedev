package com.pmease.gitplex.web.component.view;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.git.BlobText;

@SuppressWarnings("serial")
public class TextViewPanel extends Panel {

	@SuppressWarnings("unused")
	private final BlobInfo blobInfo;
	
	@SuppressWarnings("unused")
	private final BlobText blobText;
	
	public TextViewPanel(String id, BlobInfo blobInfo, BlobText blobText) {
		super(id);
		
		this.blobInfo = blobInfo;
		this.blobText = blobText;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

}
