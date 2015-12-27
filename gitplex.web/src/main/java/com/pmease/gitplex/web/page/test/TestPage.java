package com.pmease.gitplex.web.page.test;

import java.util.Set;

import org.apache.wicket.markup.html.link.Link;

import com.pmease.commons.git.NameAndEmail;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.AuxiliaryManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Link<Void>("check") {

			@Override
			public void onClick() {
				Repository repository = GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
				GitPlex.getInstance(AuxiliaryManager.class).check(repository, "master");
			}
			
		});
		
		add(new Link<Void>("getPaths") {

			@Override
			public void onClick() {
				Repository repository = GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
				long time = System.currentTimeMillis();
				
				byte[] paths = GitPlex.getInstance(AuxiliaryManager.class).getPaths(repository);
				System.out.println("paths: " + paths.length);
				System.out.println("cost time: " + (System.currentTimeMillis()-time));
			}
			
		});
		
	}

}
