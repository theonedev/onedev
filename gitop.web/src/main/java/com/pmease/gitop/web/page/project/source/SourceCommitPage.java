package com.pmease.gitop.web.page.project.source;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Strings;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.ProjectCategoryPage;

@SuppressWarnings("serial")
public class SourceCommitPage extends ProjectCategoryPage {
	protected final IModel<String> revisionModel;
	
	public static PageParameters newParams(Project project, String revision) {
		PageParameters params = PageSpec.forProject(project);
		params.add(PageSpec.OBJECT_ID, revision);
		return params;
	}
	
	public SourceCommitPage(PageParameters params) {
		super(params);
		
		revisionModel = new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PageParameters params = SourceCommitPage.this.getPageParameters();
				String objectId = params.get(PageSpec.OBJECT_ID).toString();
				if (Strings.isNullOrEmpty(objectId)) {
					String branchName = getProject().getDefaultBranchName();
					if (Strings.isNullOrEmpty(branchName)) {
						return "master";
					} else {
						return branchName;
					}
				} else {
					return objectId;
				}
			}
			
		};
	}

	@Override
	protected Category getCategory() {
		return Category.CODE;
	}

	protected String getRevision() {
		return revisionModel.getObject();
	}
	
	@Override
	protected String getPageTitle() {
		return getRevision() + " - " + getProject().getPathName();
	}
}
