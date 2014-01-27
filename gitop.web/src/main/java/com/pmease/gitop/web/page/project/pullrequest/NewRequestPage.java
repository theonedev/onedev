package com.pmease.gitop.web.page.project.pullrequest;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.page.project.AbstractProjectPage;
import com.pmease.gitop.web.page.project.ProjectCategoryPage;

@SuppressWarnings("serial")
public class NewRequestPage extends ProjectCategoryPage {

	public NewRequestPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		Branch target, source = null;
		BranchManager branchManager = Gitop.getInstance(BranchManager.class);
		AbstractProjectPage page = (AbstractProjectPage) getPage();
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

}