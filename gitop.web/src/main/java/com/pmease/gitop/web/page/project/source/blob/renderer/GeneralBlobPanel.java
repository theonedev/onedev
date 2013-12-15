package com.pmease.gitop.web.page.project.source.blob.renderer;

import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.page.project.source.blob.FileBlob;

@SuppressWarnings("serial")
public class GeneralBlobPanel extends Panel {

	public GeneralBlobPanel(String id, IModel<FileBlob> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		FileBlob blob = (FileBlob) getDefaultModelObject();
		
		add(new ResourceLink<Void>("link", new RawBlobResourceReference(),
				RawBlobResourceReference.newParams(blob.getProjectId(), 
												   blob.getRevision(), 
												   blob.getPath())));
	}
}
