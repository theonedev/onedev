package com.pmease.gitplex.web.extensionpoint;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.git.BlobInfo;
import com.pmease.gitplex.core.model.Repository;

public interface MediaRenderer {
	Panel render(String panelId, IModel<Repository> repoModel, BlobInfo blobInfo);
}
