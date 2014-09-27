package com.pmease.gitplex.web.page.repository.code.blob.renderer;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.web.service.FileBlob;

class TextBlobRenderer implements BlobRenderer {

	@Override
	public Panel render(String componentId, IModel<FileBlob> blob) {
		return new TextBlobPanel(componentId, blob);
	}

}
