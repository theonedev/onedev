package com.pmease.gitop.web.page.project.source.blob.renderer;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.page.project.source.blob.FileBlob;

public class GeneralBlobRenderer implements BlobRenderer {

	@Override
	public Panel render(String componentId, IModel<FileBlob> blob) {
		return new GeneralBlobPanel(componentId, blob);
	}

}
