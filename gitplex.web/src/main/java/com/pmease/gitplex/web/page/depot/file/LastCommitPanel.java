package com.pmease.gitplex.web.page.depot.file;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.Commit;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.component.commithash.CommitHashPanel;
import com.pmease.gitplex.web.component.commitmessage.CommitMessagePanel;
import com.pmease.gitplex.web.component.contributionpanel.ContributionPanel;
import com.pmease.gitplex.web.page.depot.commit.DepotCommitsPage;

@SuppressWarnings("serial")
class LastCommitPanel extends Panel {

	private final IModel<Depot> depotModel;
	
	private final BlobIdent blobIdent;
	
	private final Commit commit;
	
	public LastCommitPanel(String id, IModel<Depot> depotModel, BlobIdent blobIdent) {
		super(id);
		
		this.depotModel = depotModel;

		this.blobIdent = blobIdent;
		
		// call git command line for performance reason
		commit = depotModel.getObject().git().log(null, blobIdent.revision, blobIdent.path, 1, 0, false).iterator().next();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AvatarLink("avatar", commit.getAuthor()));
		add(new ContributionPanel("name", commit.getAuthor(), commit.getCommitter()));
		
		add(new CommitMessagePanel("message", depotModel, Model.of(commit)));
		
		add(new CommitHashPanel("hash", commit.getHash()));
		add(new Link<Void>("history") {

			@Override
			public void onClick() {
				DepotCommitsPage.HistoryState state = new DepotCommitsPage.HistoryState();
				String commitHash = depotModel.getObject().getObjectId(blobIdent.revision).name();
				state.setCompareWith(commitHash);
				if (blobIdent.path != null) 
					state.setQuery(String.format("id(%s) path(%s)", commitHash, blobIdent.path));
				else
					state.setQuery(String.format("id(%s)", commitHash));
				setResponsePage(DepotCommitsPage.class, DepotCommitsPage.paramsOf(depotModel.getObject(), state));
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onDetach() {
		depotModel.detach();
		
		super.onDetach();
	}

}
