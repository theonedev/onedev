package com.pmease.gitop.web.page.repository.source.blob.renderer;

import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.service.FileBlob;

@SuppressWarnings("serial")
public class RawBlobPanel extends Panel {

	public RawBlobPanel(String id, IModel<FileBlob> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		FileBlob blob = (FileBlob) getDefaultModelObject();
		
		add(new ResourceLink<Void>("link", 
				new RawBlobResourceReference(),
				RawBlobResourceReference.newParams(blob)));
	}
}
