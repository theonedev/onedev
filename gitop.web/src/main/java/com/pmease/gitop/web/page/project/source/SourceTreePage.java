package com.pmease.gitop.web.page.project.source;

import java.io.File;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Joiner;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.storage.StorageManager;
import com.pmease.gitop.web.model.ProjectModel;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.source.component.EmptyRepositoryPanel;
import com.pmease.gitop.web.page.project.source.component.ProjectDescriptionPanel;
import com.pmease.gitop.web.page.project.source.component.ReadmePanel;
import com.pmease.gitop.web.page.project.source.component.SourceBreadcrumbPanel;
import com.pmease.gitop.web.page.project.source.component.SourceTreePanel;
import com.pmease.gitop.web.util.GitUtils;

@SuppressWarnings("serial")
public class SourceTreePage extends AbstractFilePage {

	public static PageParameters newParams(Project project, String revision, List<String> paths) {
		PageParameters params = new PageParameters();
		params.add(PageSpec.USER, project.getOwner().getName());
		params.add(PageSpec.PROJECT, project.getName());
		params.add(PageSpec.OBJECT_ID, revision);
		for (int i = 0; i < paths.size(); i++) {
			params.set(i, paths.get(i));
		}
		
		return params;
	}
	
	public SourceTreePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		File gitDir = Gitop.getInstance(StorageManager.class).getStorage(getProject()).ofCode();
		
		if (GitUtils.hasCommits(gitDir)) {
			add(new ProjectDescriptionPanel("description", projectModel).setVisibilityAllowed(getPaths().isEmpty()));
			add(new SourceBreadcrumbPanel("breadcrumb", projectModel, revisionModel, pathsModel));
			add(new SourceTreePanel("tree", projectModel, revisionModel, pathsModel));
			add(new ReadmePanel("readme", projectModel, revisionModel, pathsModel));
			add(new WebMarkupContainer("empty").setVisibilityAllowed(false));
		} else {
			add(new EmptyRepositoryPanel("empty", new ProjectModel(getProject())));
			add(new WebMarkupContainer("tree").setVisibilityAllowed(false));
		}
	}
	
	@Override
	protected String getPageTitle() {
		List<String> paths = getPaths();
		String rev = getRevision();
		Project project = getProject();
		
		if (paths.isEmpty()) {
			return project.getPathName();
		} else {
			StringBuffer sb = new StringBuffer();
			sb.append(Joiner.on("/").join(paths))
				.append(" at ").append(rev)
				.append(" - ").append(project.getPathName());
			
			return sb.toString();
		}
	}

}
