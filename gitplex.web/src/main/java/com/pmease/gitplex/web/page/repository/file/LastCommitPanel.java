package com.pmease.gitplex.web.page.repository.file;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.Commit;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.UserLink;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.component.commithash.CommitHashPanel;
import com.pmease.gitplex.web.component.commitmessage.CommitMessagePanel;
import com.pmease.gitplex.web.utils.DateUtils;

@SuppressWarnings("serial")
class LastCommitPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final Commit commit;
	
	public LastCommitPanel(String id, IModel<Repository> repoModel, BlobIdent blob) {
		super(id);
		
		this.repoModel = repoModel;

		// call git command line for performance reason
		commit = repoModel.getObject().git().log(null, blob.revision, blob.path, 1, 0, false).iterator().next();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AvatarLink("avatar", commit.getAuthor(), null));
		add(new UserLink("name", commit.getAuthor()));
		
		add(new Label("date", Model.of(DateUtils.formatAge(commit.getAuthor().getWhen()))));
		
		add(new CommitMessagePanel("message", repoModel, Model.of(commit)));
		
		add(new CommitHashPanel("hash", Model.of(commit.getHash())));
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
