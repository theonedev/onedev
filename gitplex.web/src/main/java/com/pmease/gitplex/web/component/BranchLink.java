package com.pmease.gitplex.web.component;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;

@SuppressWarnings("serial")
public class BranchLink extends BookmarkablePageLink<Void> {

	private final RepoAndBranch repoAndBranch;
	
	public BranchLink(String id, RepoAndBranch repoAndBranch) {
		super(id, RepoFilePage.class, 
				RepoFilePage.paramsOf(repoAndBranch.getRepository(), repoAndBranch.getBranch(), null));
		this.repoAndBranch = repoAndBranch;
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setEnabled(SecurityUtils.canPull(repoAndBranch.getRepository()) 
				&& repoAndBranch.getHead(false) != null);
	}

	@Override
	public IModel<?> getBody() {
		String label;
		if (getPage() instanceof RepositoryPage) {
			RepositoryPage page = (RepositoryPage) getPage();
			if (page.getRepository().equals(repoAndBranch.getRepository())) 
				label = repoAndBranch.getBranch();
			else 
				label = repoAndBranch.getFQN();
		} else {
			label = repoAndBranch.getFQN();
		}
		return Model.of(label);
	}

}
