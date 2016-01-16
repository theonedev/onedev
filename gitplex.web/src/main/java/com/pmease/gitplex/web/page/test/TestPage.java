package com.pmease.gitplex.web.page.test;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.revisionpicker.AffinalRevisionPicker;
import com.pmease.gitplex.web.model.RepositoryModel;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Repository repo = GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
		IModel<Repository> repoModel = new RepositoryModel(repo);
		add(new AffinalRevisionPicker("revisionSelector", repoModel, repo.getDefaultBranch()) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				System.out.println(revision);
			}
			
		});
	}

}
