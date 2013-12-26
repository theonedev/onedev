package com.pmease.gitop.web.page.project.source;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.gitop.web.page.project.ProjectCategoryPage;
import com.pmease.gitop.web.page.project.api.IRevisionAware;

@SuppressWarnings("serial")
public abstract class AbstractFilePage extends ProjectCategoryPage implements IRevisionAware {

	protected final IModel<List<String>> pathsModel;
	
	public AbstractFilePage(PageParameters params) {
		super(params);
		
		pathsModel = new LoadableDetachableModel<List<String>>() {

			@Override
			public List<String> load() {
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
	
	protected List<String> getPaths() {
		return pathsModel.getObject();
	}
	
	@Override
	public void onDetach() {
		if (pathsModel != null) {
			pathsModel.detach();
		}
		
		super.onDetach();
	}
}
