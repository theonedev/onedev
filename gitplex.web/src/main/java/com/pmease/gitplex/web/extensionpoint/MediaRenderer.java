package com.pmease.gitplex.web.extensionpoint;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.web.component.view.BlobRenderInfo;

public interface MediaRenderer {
	Panel render(String panelId, BlobRenderInfo blobInfo, IModel<byte[]> blobContentModel);
}
