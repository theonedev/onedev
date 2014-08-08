package com.pmease.gitplex.web.extensionpoint;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.web.component.diff.BlobDiffInfo;

public interface DiffRenderer {
	Panel render(String panelId, BlobDiffInfo diffInfo, 
			IModel<byte[]> originalBlobContentModel, IModel<byte[]> revisedBlobContentModel);
}
