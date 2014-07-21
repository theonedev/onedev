package com.pmease.gitplex.web.page.repository.source.tags;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.git.command.ArchiveCommand.Format;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

public class GitArchiveResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;
	
	public static final String RESOURCE_NAME = "git-archive";
	
	public GitArchiveResourceReference() {
		super(RESOURCE_NAME);
	}

	@Override
	public IResource getResource() {
		return new GitArchiveResource();
	}

	public static PageParameters newParams(Repository repository, String treeish, Format format) {
		PageParameters params = RepositoryPage.paramsOf(repository);
		params.set("file", treeish + "." + format.getSuffix());
		return params;
	}
}
