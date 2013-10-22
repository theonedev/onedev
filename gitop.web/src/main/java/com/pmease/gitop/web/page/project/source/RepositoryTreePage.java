package com.pmease.gitop.web.page.project.source;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.PageSpec;

@SuppressWarnings("serial")
public class RepositoryTreePage extends RepositorySourcePage {
	
	public RepositoryTreePage(PageParameters params) {
		super(params);
		
		if (params.getIndexedCount() <= 0) {
			this.redirectWithInterception(ProjectHomePage.class, PageSpec.forProject(getProject()));
		}
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		System.out.println(objectIdModel.getObject());
		System.out.println(pathModel.getObject());
	}
	
	@Override
	public void onDetach() {
		if (objectIdModel != null) {
			objectIdModel.detach();
		}
		
		if (pathModel != null) {
			pathModel.detach();
		}
		
		super.onDetach();
	}
}
