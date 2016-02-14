package com.pmease.gitplex.web.page.repository.file;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.Commit;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.component.commithash.CommitHashPanel;
import com.pmease.gitplex.web.component.commitmessage.CommitMessagePanel;
import com.pmease.gitplex.web.component.contributionpanel.ContributionPanel;
import com.pmease.gitplex.web.page.repository.commit.RepoCommitsPage;

@SuppressWarnings("serial")
class LastCommitPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final BlobIdent blobIdent;
	
	private final Commit commit;
	
	public LastCommitPanel(String id, IModel<Repository> repoModel, BlobIdent blobIdent) {
		super(id);
		
		this.repoModel = repoModel;

		this.blobIdent = blobIdent;
		
		// call git command line for performance reason
		commit = repoModel.getObject().git().log(null, blobIdent.revision, blobIdent.path, 1, 0, false).iterator().next();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AvatarLink("avatar", commit.getAuthor()));
		add(new ContributionPanel("name", commit.getAuthor(), commit.getCommitter()));
		
		add(new CommitMessagePanel("message", repoModel, Model.of(commit)));
		
		add(new CommitHashPanel("hash", commit.getHash()));
		add(new Link<Void>("history") {

			@Override
			public void onClick() {
				RepoCommitsPage.HistoryState state = new RepoCommitsPage.HistoryState();
				String commitHash = repoModel.getObject().getObjectId(blobIdent.revision).name();
				state.setCompareWith(commitHash);
				if (blobIdent.path != null) 
					state.setQuery(String.format("id(%s) path(%s)", commitHash, blobIdent.path));
				else
					state.setQuery(String.format("id(%s)", commitHash));
				setResponsePage(RepoCommitsPage.class, RepoCommitsPage.paramsOf(repoModel.getObject(), state));
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
