package com.pmease.gitplex.web.page.repository.info.code.commit.diff.renderer.image;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.info.code.commit.diff.patch.FileHeader;
import com.pmease.gitplex.web.page.repository.info.code.commit.diff.renderer.BlobDiffPanel;

@SuppressWarnings("serial")
public class ImageDiffPanel extends BlobDiffPanel {

	public ImageDiffPanel(String id, int index, 
			IModel<Repository> repoModel,
			IModel<FileHeader> fileModel,
			IModel<String> sinceModel,
			IModel<String> untilModel) {
		
		super(id, index, repoModel, fileModel, sinceModel, untilModel);
	}

	@Override
	protected Component createActionsBar(String id) {
		return new WebMarkupContainer(id).setVisibilityAllowed(false);
	}

	@Override
	protected Component createDiffContent(String id) {
		return new ImageBlobDiffPanel(id, repoModel, getFileModel(), sinceModel, untilModel);
	}
}
