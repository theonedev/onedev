package com.pmease.gitplex.web.page;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.pathnavigator.PathNavigator;
import com.pmease.gitplex.web.component.revisionselector.RevisionSelector;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private IModel<Repository> repoModel = new LoadableDetachableModel<Repository>() {

		@Override
		protected Repository load() {
			return GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
		}
		
	};
	
	private String revision = "master";
	
	private PathNavigator pathNavigator;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new RevisionSelector("revSelector", repoModel, revision) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				TestPage.this.revision = revision;
				target.add(pathNavigator);
			}
			
		});
		
		add(pathNavigator = new PathNavigator("pathSelector", repoModel, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return revision;
			}
			
		}, null) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String path) {
				System.out.println(path);
			}
			
		});
		
		pathNavigator.setOutputMarkupId(true);
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
