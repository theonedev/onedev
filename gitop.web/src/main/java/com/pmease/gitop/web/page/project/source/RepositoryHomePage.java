package com.pmease.gitop.web.page.project.source;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.page.project.source.tree.SourceTreePage;

@SuppressWarnings("serial")
public class RepositoryHomePage extends SourceTreePage {

	public RepositoryHomePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String findRevision(PageParameters params) {
		String defaultBranch = GitUtils.getDefaultBranch(getProject().code()); //getProject().code().resolveDefaultBranch();
		return defaultBranch;
	}
	
	@Override
	protected String getPageTitle() {
		return getAccount().getName() + "/" + getProject().getName();
	}
	
	@Override
	protected List<String> getPaths() {
		return Collections.emptyList();
	}
}
