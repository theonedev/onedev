package com.pmease.gitplex.web.page;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.lucene.search.NGramPhraseQuery;
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
			}
			
		});
		
		add(new Link<Void>("testSearch") {

			@Override
			public void onClick() {
				Repository repo = GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
				SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
				
				long time = System.currentTimeMillis();
				NGramPhraseQuery query = new NGramPhraseQuery(3);
//				query.add(FieldConstants.BLOB_CONTENT.term("cryptd_alloc_aead"));
				query.add(FieldConstants.BLOB_CONTENT.term("ret"));
				query.add(FieldConstants.BLOB_CONTENT.term("et_"));
				query.add(FieldConstants.BLOB_CONTENT.term("t_v"));
				query.add(FieldConstants.BLOB_CONTENT.term("_va"));
				query.add(FieldConstants.BLOB_CONTENT.term("val"));
				System.out.println("found: " + searchManager.search(repo, repo.git().parseRevision("master", true), 
						query, null, 0).size());
				System.out.println("time: " + (System.currentTimeMillis()-time));
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}

}
