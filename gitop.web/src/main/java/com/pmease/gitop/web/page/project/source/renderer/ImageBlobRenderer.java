package com.pmease.gitop.web.page.project.source.renderer;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.page.project.source.GitBlob;

public class ImageBlobRenderer implements BlobRenderer {

	@Override
	public Panel render(String componentId, IModel<GitBlob> blob) {
		return new ImageBlobPanel(componentId, blob);
	}

}
