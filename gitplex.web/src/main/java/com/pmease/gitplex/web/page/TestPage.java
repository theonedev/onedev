package com.pmease.gitplex.web.page;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.CommonPage;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.IndexManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.search.BlobSearcher;

@SuppressWarnings("serial")
public class TestPage extends CommonPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("testIndex") {

			@Override
			public void onClick() {
				Repository repo = GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
				IndexManager indexManager = GitPlex.getInstance(IndexManager.class);
				/*
				Date since;
				try {
					since = new SimpleDateFormat("yyyy-MM-dd").parse("2013-07-01");
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
				
				long time = System.currentTimeMillis();
				List<Commit> commits = repo.git().log(since, null, null, 0, 0);
				System.out.println("Total commits to index: " + commits.size());
				int count = 0;
				for (Commit commit: commits) {
					System.out.println(count++);
					indexManager.index(repo, commit.getHash());			
				}
				System.out.println("Total Minutes: " + (System.currentTimeMillis()-time)/1000/60);
				*/
				indexManager.index(repo, repo.git().parseRevision("master", true));
			}
			
		});
		
		Repository repo = GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
		String commitHash = repo.git().parseRevision("master", true);
		add(new BlobSearcher("searcher", Model.of(repo), commitHash, false));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}

}
