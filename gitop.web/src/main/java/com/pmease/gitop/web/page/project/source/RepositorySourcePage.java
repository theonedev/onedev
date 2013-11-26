package com.pmease.gitop.web.page.project.source;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.ProjectCategoryPage;
import com.pmease.gitop.web.page.project.source.component.ProjectDescriptionPanel;
import com.pmease.gitop.web.page.project.source.component.ReadmePanel;
import com.pmease.gitop.web.page.project.source.component.SourceTreePanel;

@SuppressWarnings("serial")
public abstract class RepositorySourcePage extends ProjectCategoryPage {

	protected IModel<String> revisionModel;
	protected IModel<List<String>> pathModel;
	
	public RepositorySourcePage(PageParameters params) {
		super(params);
		
		revisionModel = new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PageParameters params = RepositorySourcePage.this.getPageParameters();
				String objectId = params.get(PageSpec.OBJECT_ID).toString();
				if (Strings.isNullOrEmpty(objectId)) {
					String branchName = getProject().getDefaultBranchName();
					if (!Strings.isNullOrEmpty(branchName)) {
						return "master";
					} else {
						return branchName;
					}
				} else {
					return objectId;
				}
			}
			
		};
		
		pathModel = new AbstractReadOnlyModel<List<String>>() {

			@Override
			public List<String> getObject() {
				PageParameters params = RepositorySourcePage.this.getPageParameters();
				int count = params.getIndexedCount();
				List<String> paths = Lists.newArrayList();
				for (int i = 0; i < count; i++) {
					String p = params.get(i).toString();
					if (!Strings.isNullOrEmpty(p)) {
						paths.add(p);
					}
				}
				
				return paths;
			}
			
		};
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		IModel<List<String>> pathModel = new AbstractReadOnlyModel<List<String>>() {

			@Override
			public List<String> getObject() {
				return new ArrayList<String>();
			}
			
		};
		
		add(new ProjectDescriptionPanel("description", projectModel));
		add(new SourceTreePanel("files", projectModel, revisionModel, pathModel));
		add(new ReadmePanel("readme", projectModel, pathModel));
	}
	
	@Override
	protected Category getCategory() {
		return Category.CODE;
	}

	@Override
	protected String getPageTitle() {
		return getProject().toString();
	}
	
	@Override
	public void onDetach() {
		if (revisionModel != null) {
			revisionModel.detach();
		}
		
		if (pathModel != null) {
			pathModel.detach();
		}
		
		super.onDetach();
	}
}
