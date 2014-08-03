package com.pmease.gitplex.web.page;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.diff.DiffTreePanel;
import com.pmease.gitplex.web.page.home.HomePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	public TestPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new DiffTreePanel("test", new LoadableDetachableModel<Repository>() {

			@Override
			protected Repository load() {
				return GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
			}
			
		}, "master", "dev") {

			@Override
			protected Link<Void> newFileLink(String id) {
				return new BookmarkablePageLink<Void>(id, HomePage.class);
			}
			
		});
	}
	
}
