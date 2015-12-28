package com.pmease.gitplex.web.page.test;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.wicket.markup.html.link.Link;

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
				
				List<String> paths = GitPlex.getInstance(AuxiliaryManager.class).getFiles(repository);
				Pattern pattern = Pattern.compile("hello.*world/linux.");
				for (String path: paths) {
					pattern.matcher(path).find();
				}
				System.out.println("cost time: " + (System.currentTimeMillis()-time));
			}
			
		});
		
	}

}
