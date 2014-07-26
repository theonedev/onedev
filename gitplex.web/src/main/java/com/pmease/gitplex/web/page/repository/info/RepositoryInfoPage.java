package com.pmease.gitplex.web.page.repository.info;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.component.tabbable.StylelessTabbable;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.info.code.blob.RepoBlobPage;
import com.pmease.gitplex.web.page.repository.info.code.branches.BranchesPage;
import com.pmease.gitplex.web.page.repository.info.code.commit.RepoCommitPage;
import com.pmease.gitplex.web.page.repository.info.code.commits.RepoCommitsPage;
import com.pmease.gitplex.web.page.repository.info.code.contributors.ContributorsPage;
import com.pmease.gitplex.web.page.repository.info.code.tags.TagsPage;
import com.pmease.gitplex.web.page.repository.info.code.tree.RepoTreePage;
import com.pmease.gitplex.web.page.repository.info.pullrequest.ClosedRequestsPage;
import com.pmease.gitplex.web.page.repository.info.pullrequest.NewRequestPage;
import com.pmease.gitplex.web.page.repository.info.pullrequest.OpenRequestsPage;
import com.pmease.gitplex.web.page.repository.info.pullrequest.RequestDetailPage;

@SuppressWarnings("serial")
public abstract class RepositoryInfoPage extends RepositoryPage {

	public RepositoryInfoPage(PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<Tab> codeTabs = new ArrayList<>();
		codeTabs.add(new RepoInfoTab(Model.of("Files"), "icon icon-code", RepoTreePage.class, RepoBlobPage.class));
		codeTabs.add(new RepoInfoTab(Model.of("Commits"), "icon icon-commits", RepoCommitsPage.class, RepoCommitPage.class));
		codeTabs.add(new RepoInfoTab(Model.of("Branches"), "icon icon-git-branch", BranchesPage.class));
		codeTabs.add(new RepoInfoTab(Model.of("Tags"), "icon icon-tags", TagsPage.class));
		codeTabs.add(new RepoInfoTab(Model.of("Contributors"), "icon icon-group-o", ContributorsPage.class));
		
		add(new StylelessTabbable("codeTabs", codeTabs));
		
		List<Tab> requestTabs = new ArrayList<>();
		requestTabs.add(new RepoInfoTab(Model.of("Open"), "icon icon-pull-request", OpenRequestsPage.class) {

				@Override
				public boolean isActive(Page currentPage) {
					if (currentPage instanceof RequestDetailPage) {
						RequestDetailPage detailPage = (RequestDetailPage) currentPage;
						if (detailPage.getPullRequest().isOpen())
							return true;
					}
					return super.isActive(currentPage);
				}
			
		});
		requestTabs.add(new RepoInfoTab(Model.of("Closed"), "icon icon-pull-request-abandon", ClosedRequestsPage.class) {
			
				@Override
				public boolean isActive(Page currentPage) {
					if (currentPage instanceof RequestDetailPage) {
						RequestDetailPage detailPage = (RequestDetailPage) currentPage;
						if (!detailPage.getPullRequest().isOpen())
							return true;
					}
					return super.isActive(currentPage);
				}
			
		});
		requestTabs.add(new RepoInfoTab(Model.of("Create"), "icon icon-pull-request", NewRequestPage.class));		
		
		add(new StylelessTabbable("requestTabs", requestTabs));
	}
	
}
