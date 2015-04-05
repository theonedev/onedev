package com.pmease.gitplex.web.page;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.Commit;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.IndexManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.SearchManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.BlobQuery;
import com.pmease.gitplex.search.query.TextQuery;
import com.pmease.gitplex.web.component.search.BlobAdvancedSearchResultPanel;
import com.pmease.gitplex.web.component.search.BlobSearchPanel;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("testIndex") {

			@Override
			public void onClick() {
				Repository repo = GitPlex.getInstance(Dao.class).load(Repository.class, 2L);
				IndexManager indexManager = GitPlex.getInstance(IndexManager.class);
				Date since;
				try {
					since = new SimpleDateFormat("yyyy-MM-dd").parse("2010-01-01");
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
				
				long time = System.currentTimeMillis();
				List<Commit> commits = repo.git().log(since, null, null, 0, 0);
				System.out.println("Total commits to index: " + commits.size());
				int count = 0;
				for (Commit commit: commits) {
					System.out.println("" + (count++) + ": " + commit.getCommitter().getWhen());
					indexManager.index(repo, commit.getHash());			
				}
				System.out.println("Total Minutes: " + (System.currentTimeMillis()-time)/1000/60);
//				indexManager.index(repo, repo.git().parseRevision("master", true));
			}
			
		});
		
		add(new Link<Void>("testSearch") {

			@Override
			public void onClick() {
				Repository repo = GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
				SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
				try {
					long time = System.currentTimeMillis();
					String commitHash = repo.git().parseRevision("master~10", true);
					BlobQuery query = new TextQuery("windows", true, false, false, null, null, 1000);
					List<QueryHit> hits = searchManager.search(repo, commitHash, query);
					System.out.println(hits.size());
					System.out.println(System.currentTimeMillis()-time);
				} catch (InterruptedException e) {
				}
			}
			
		});
		
		add(new WebMarkupContainer("searchResult").setOutputMarkupId(true));
		
		Repository repo = GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
		final String commitHash = repo.git().parseRevision("master~100", true);
		add(new BlobSearchPanel("searcher", Model.of(repo)) {

			@Override
			protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
				System.out.println(hit.getBlobPath() + ": " + hit.getLineNo());
			}

			@Override
			protected String getCurrentCommit() {
				return commitHash;
			}

			@Override
			protected void onCompleteAdvancedSearch(AjaxRequestTarget target, List<QueryHit> hits) {
				BlobAdvancedSearchResultPanel searchResult = new BlobAdvancedSearchResultPanel("searchResult", hits) {

					@Override
					protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
						System.out.println(hit.getBlobPath() + ": " + hit.getLineNo());
					}
					
				};
				searchResult.setOutputMarkupId(true);
				getPage().get("searchResult").replaceWith(searchResult);
				target.add(searchResult);
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}

}
