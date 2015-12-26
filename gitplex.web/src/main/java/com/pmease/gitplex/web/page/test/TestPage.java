package com.pmease.gitplex.web.page.test;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
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
		
		add(new Link<Void>("getCommits") {

			@Override
			public void onClick() {
				Repository repository = GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
				long time = System.currentTimeMillis();
				
				Set<String> files = new HashSet<>();
				for (File file: new File("/home/robin/chromium/pdf").listFiles())
					files.add("pdf/" + file.getName());
				Map<String, Map<NameAndEmail, Long>> contributors = GitPlex.getInstance(AuxiliaryManager.class).getContributors(repository, files);
				for (String file: files) {
					System.out.println(file + ": " + contributors.get(file).size());
				}
				System.out.println("cost time: " + (System.currentTimeMillis()-time));
			}
			
		});
		
	}

}
