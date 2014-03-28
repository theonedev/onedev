package com.pmease.gitop.web.page.project.pullrequest;

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
import com.pmease.gitop.web.page.project.RepositoryBasePage;
import com.pmease.gitop.web.page.project.RepositoryTabPage;
import com.pmease.gitop.web.page.project.source.commit.diff.CommitCommentsAware;

@SuppressWarnings("serial")
public class NewRequestPage extends RepositoryTabPage implements CommitCommentsAware {

	public static PageParameters newParams(Repository project, String source, String dest) {
		PageParameters params = PageSpec.forRepository(project);
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
		if (page.getProject().getForkedFrom() != null) {
			target = branchManager.findDefault(page.getProject().getForkedFrom());
			source = branchManager.findDefault(page.getProject());
		} else {
			target = branchManager.findDefault(page.getProject());
			for (Branch each: page.getProject().getBranches()) {
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