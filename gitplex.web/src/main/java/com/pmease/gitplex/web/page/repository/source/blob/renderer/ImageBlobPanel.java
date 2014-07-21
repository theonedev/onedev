package com.pmease.gitplex.web.page.repository.source.blob.renderer;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.web.service.FileBlob;

@SuppressWarnings("serial")
public class ImageBlobPanel extends Panel {

	public ImageBlobPanel(String id, IModel<FileBlob> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		FileBlob blob = getBlob();
		add(new FileBlobImage("image", FileBlobImage.paramsOf(blob)));
	}
	
	private FileBlob getBlob() {
		return (FileBlob) getDefaultModelObject();
	}
}
