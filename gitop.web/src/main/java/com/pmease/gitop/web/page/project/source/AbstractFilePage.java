package com.pmease.gitop.web.page.project.source;

import java.util.List;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.ProjectCategoryPage;

@SuppressWarnings("serial")
public abstract class AbstractFilePage extends ProjectCategoryPage {

	protected final IModel<String> revisionModel;
	protected final IModel<List<String>> pathsModel;
	
	public AbstractFilePage(PageParameters params) {
		super(params);
		
		revisionModel = new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PageParameters params = AbstractFilePage.this.getPageParameters();
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
		
		pathsModel = new AbstractReadOnlyModel<List<String>>() {

			@Override
			public List<String> getObject() {
				PageParameters params = AbstractFilePage.this.getPageParameters();
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

	protected String getRevision() {
		return revisionModel.getObject();
	}
	
	protected List<String> getPaths() {
		return pathsModel.getObject();
	}
	
	@Override
	protected Category getCategory() {
		return Category.CODE;
	}

	@Override
	public void onDetach() {
		if (revisionModel != null) {
			revisionModel.detach();
		}
		
		if (pathsModel != null) {
			pathsModel.detach();
		}
		
		super.onDetach();
	}
}
