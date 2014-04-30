package com.pmease.gitop.web.page.repository.pullrequest;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.repository.RepositoryBasePage;
import com.pmease.gitop.web.page.repository.RepositoryPage;
import com.pmease.gitop.web.page.repository.source.commit.diff.CommitCommentsAware;

@SuppressWarnings("serial")
public class NewRequestPage extends RepositoryPage implements CommitCommentsAware {

	public static PageParameters newParams(Repository repository, String source, String dest) {
		PageParameters params = PageSpec.forRepository(repository);
		params.set("source", source);
		params.set("dest", dest);
		return params;
	}
	
	public NewRequestPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Branch target, source = null;
		BranchManager branchManager = Gitop.getInstance(BranchManager.class);
		RepositoryBasePage page = (RepositoryBasePage) getPage();
		if (page.getRepository().getForkedFrom() != null) {
			target = branchManager.findDefault(page.getRepository().getForkedFrom());
			source = branchManager.findDefault(page.getRepository());
		} else {
			target = branchManager.findDefault(page.getRepository());
			for (Branch each: page.getRepository().getBranches()) {
				if (!each.equals(target)) {
					source = each;
					break;
				}
			}
			if (source == null)
				source = target;
		}
		User currentUser = AppLoader.getInstance(UserManager.class).getCurrent();
		
		add(new NewRequestPanel("content", target, source, currentUser));
	}

	@Override
	public List<CommitComment> getCommitComments() {
		return new ArrayList<>();
	}

	@Override
	public boolean isShowInlineComments() {
		return false;
	}

	@Override
	public boolean canAddComments() {
		return false;
	}

}