package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.html.link.Link;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				Repository repo = GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
				repo.getObjectId("99b2fe070f19a1ccbb0db633138e3428f69d660d", false);
				long time = System.currentTimeMillis();
				for (int i=0; i<1000; i++)
				repo.getObjectId("99b2fe070f19a1ccbb0db633138e3428f69d660d", false);
				System.out.println(System.currentTimeMillis()-time);
			}
			
		});
	}

}
