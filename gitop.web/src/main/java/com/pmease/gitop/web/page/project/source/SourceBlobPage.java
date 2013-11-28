package com.pmease.gitop.web.page.project.source;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.project.source.component.SourceBreadcrumbPanel;

@SuppressWarnings("serial")
public class SourceBlobPage extends AbstractFilePage {

	public SourceBlobPage(PageParameters params) {
		super(params);
	}

	@Override
	public void onPageInitialize() {
		super.onPageInitialize();
		
		add(new SourceBreadcrumbPanel("breadcrumb", projectModel, revisionModel, pathsModel));
	}
	
	@Override
	protected String getPageTitle() {
		return "";
	}
}
