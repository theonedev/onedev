package com.pmease.gitplex.web.component.addcommit;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.git.BlobIdent;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public class AddCommitPanel extends Panel {

	public AddCommitPanel(String id, IModel<Repository> repoModel, BlobIdent blobIdent, 
			ObjectId parentCommitId, @Nullable byte[] content) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

}
