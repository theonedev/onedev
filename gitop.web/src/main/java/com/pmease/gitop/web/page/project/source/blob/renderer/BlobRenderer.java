package com.pmease.gitop.web.page.project.source.blob.renderer;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.service.FileBlob;

public interface BlobRenderer {
	
	Panel render(String componentId, IModel<FileBlob> blob);
	
}
