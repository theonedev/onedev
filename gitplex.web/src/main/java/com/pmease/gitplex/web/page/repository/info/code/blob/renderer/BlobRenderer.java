package com.pmease.gitplex.web.page.repository.info.code.blob.renderer;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.web.service.FileBlob;

public interface BlobRenderer {
	
	Panel render(String componentId, IModel<FileBlob> blob);
	
}
