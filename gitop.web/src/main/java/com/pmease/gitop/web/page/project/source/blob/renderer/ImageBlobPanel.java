package com.pmease.gitop.web.page.project.source.blob.renderer;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.page.project.source.blob.FileBlob;

@SuppressWarnings("serial")
public class ImageBlobPanel extends Panel {

	public ImageBlobPanel(String id, IModel<FileBlob> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		FileBlob blob = getBlob();
		add(new ImageBlobImage("image", ImageBlobImage.newParams(
				blob.getProjectId(), 
				blob.getRevision(), 
				blob.getPath())));
	}
	
	private FileBlob getBlob() {
		return (FileBlob) getDefaultModelObject();
	}
}
