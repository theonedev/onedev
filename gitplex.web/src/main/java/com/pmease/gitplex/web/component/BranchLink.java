package com.pmease.gitplex.web.component;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.model.DepotAndBranch;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.file.RepoFileState;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;

@SuppressWarnings("serial")
public class BranchLink extends BookmarkablePageLink<Void> {

	private final DepotAndBranch repoAndBranch;
	
	public BranchLink(String id, DepotAndBranch repoAndBranch) {
		super(id, RepoFilePage.class, getPageParams(repoAndBranch));
		this.repoAndBranch = repoAndBranch;
	}
	
	private static PageParameters getPageParams(DepotAndBranch repoAndBranch) {
		RepoFileState state = new RepoFileState();
		state.blobIdent.revision = repoAndBranch.getBranch();
		return RepoFilePage.paramsOf(repoAndBranch.getDepot(), state);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setEnabled(SecurityUtils.canPull(repoAndBranch.getDepot()) 
				&& repoAndBranch.getObjectName(false) != null);
	}

	@Override
	public IModel<?> getBody() {
		String label;
		if (getPage() instanceof RepositoryPage) {
			RepositoryPage page = (RepositoryPage) getPage();
			if (page.getDepot().equals(repoAndBranch.getDepot())) 
				label = repoAndBranch.getBranch();
			else 
				label = repoAndBranch.getFQN();
		} else {
			label = repoAndBranch.getFQN();
		}
		return Model.of(label);
	}

}
