package com.pmease.gitplex.web.page;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.revisionselector.RevisionSelector;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new RevisionSelector("revSelector", new LoadableDetachableModel<Repository>() {

			@Override
			protected Repository load() {
				return GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
			}
			
		}, "master") {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				System.out.println(revision);
			}
			
		});
	}

}
