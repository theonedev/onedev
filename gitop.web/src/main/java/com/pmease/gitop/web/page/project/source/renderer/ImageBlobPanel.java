package com.pmease.gitop.web.page.project.source.renderer;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.page.project.source.GitBlob;

@SuppressWarnings("serial")
public class ImageBlobPanel extends Panel {

	public ImageBlobPanel(String id, IModel<GitBlob> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		GitBlob blob = getBlob();
		add(new ImageBlobImage("image", ImageBlobImage.newParams(
				blob.getProjectId(), 
				blob.getRevision(), 
				blob.getPath())));
	}
	
	private GitBlob getBlob() {
		return (GitBlob) getDefaultModelObject();
	}
}
