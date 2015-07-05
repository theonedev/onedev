package com.pmease.gitplex.web.component.fileedit;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.ObjectId;

import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public class FileEditPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	public FileEditPanel(String id, IModel<Repository> repoModel, String refName, 
			@Nullable String oldPath, ObjectId prevCommitId) {
		super(id);
		this.repoModel = repoModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

	@Override
	protected void onDetach() {
		repoModel.detach();

		super.onDetach();
	}

}
