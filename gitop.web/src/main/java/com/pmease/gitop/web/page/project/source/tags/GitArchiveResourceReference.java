package com.pmease.gitop.web.page.project.source.tags;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.git.command.ArchiveCommand.Format;
import com.pmease.gitop.web.page.PageSpec;

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

	public static PageParameters newParams(Project project, String treeish, Format format) {
		PageParameters params = PageSpec.forProject(project);
		params.set("file", treeish + "." + format.getSuffix());
		return params;
	}
}
