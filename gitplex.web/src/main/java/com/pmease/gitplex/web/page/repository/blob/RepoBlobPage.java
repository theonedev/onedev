package com.pmease.gitplex.web.page.repository.blob;

import javax.annotation.Nullable;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.git.GitPath;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class RepoBlobPage extends RepositoryPage {

	private static final String PARAM_REVISION = "revision";
	
	private static final String PARAM_PATH = "path";
	
	private String revision = "master";
	
	private String path;
	
	public RepoBlobPage(PageParameters params) {
		super(params);
		
		revision = GitPath.normalize(params.get(PARAM_REVISION).toString());
		path = GitPath.normalize(params.get(PARAM_PATH).toString());
	}

	public static PageParameters paramsOf(Repository repository, @Nullable String revision, @Nullable String path) {
		PageParameters params = paramsOf(repository);
		if (revision != null)
			params.set(PARAM_REVISION, revision);
		if (path != null)
			params.set(PARAM_PATH, path);
		return params;
	}
	
}
