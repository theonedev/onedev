package com.pmease.gitplex.web.page;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;

import com.pmease.commons.git.Commit;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.CommonPage;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.IndexManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.FieldConstants;
import com.pmease.gitplex.search.SearchManager;

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
				int count = 0;
				for (Commit commit: repo.git().log("master~500", "master", null, 0, 0)) {
					System.out.println(++count);
					indexManager.index(repo, commit.getHash());			
				}
				
			}
			
		});
		
		add(new Link<Void>("testSearch") {

			@Override
			public void onClick() {
				Repository repo = GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
				SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
				
				long time = System.currentTimeMillis();
				System.out.println(searchManager.search(repo, repo.git().parseRevision("master", true), 
						FieldConstants.BLOB_SYMBOLS.query("ext2_acl_from_disk"), null, 0).size());
				System.out.println("time: " + (System.currentTimeMillis()-time));
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}

}
