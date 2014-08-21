package com.pmease.gitplex.web.page;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private IModel<Repository> repoModel;
	
	public TestPage(PageParameters params) {
		super(params);
		
		repoModel = new LoadableDetachableModel<Repository>() {

			@Override
			protected Repository load() {
				return GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
			}
		};
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}
	
}
