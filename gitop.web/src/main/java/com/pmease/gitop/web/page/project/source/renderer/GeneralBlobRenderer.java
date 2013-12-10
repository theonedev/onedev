package com.pmease.gitop.web.page.project.source.renderer;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.page.project.source.GitBlob;

public class GeneralBlobRenderer implements BlobRenderer {

	@Override
	public Panel render(String componentId, IModel<GitBlob> blob) {
		return new GeneralBlobPanel(componentId, blob);
	}

}
