package com.pmease.gitplex.web.page;

import java.io.IOException;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.web.component.search.BlobAdvancedSearchResultPanel;
import com.pmease.gitplex.web.component.sourceview.Source;
import com.pmease.gitplex.web.component.sourceview.SourceViewPanel;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private IModel<Repository> repoModel = new LoadableDetachableModel<Repository>() {

		@Override
		protected Repository load() {
			return GitPlex.getInstance(Dao.class).load(Repository.class, 2L);
		}
		
	};

	private String revision = "master";
	
	private String blobPath = "gitplex.core/src/main/java/com/pmease/gitplex/core/manager/impl/DefaultPullRequestManager.java";
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		/*
		add(new Link<Void>("testIndex") {

			@Override
			public void onClick() {
				IndexManager indexManager = GitPlex.getInstance(IndexManager.class);
				Date since;
				try {
					since = new SimpleDateFormat("yyyy-MM-dd").parse("2010-01-01");
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
				
				long time = System.currentTimeMillis();
				List<Commit> commits = repoModel.getObject().git().log(since, null, null, 0, 0);
				System.out.println("Total commits to index: " + commits.size());
				int count = 0;
				for (Commit commit: commits) {
					System.out.println("" + (count++) + ": " + commit.getCommitter().getWhen());
					indexManager.index(repoModel.getObject(), commit.getHash());			
				}
				System.out.println("Total Minutes: " + (System.currentTimeMillis()-time)/1000/60);
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
		
		add(new BlobSearchPanel("searcher", new LoadableDetachableModel<Repository>() {

			@Override
			protected Repository load() {
				return GitPlex.getInstance(Dao.class).load(Repository.class, 2L);
			}
			
		}, Model.of("master")) {

			@Override
			protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
				Source newSource = openBlob(hit.getBlobPath(), hit.getLineNo());
				SourceViewPanel newSourceView = new SourceViewPanel(sourceView.getId(), repoModel, newSource) {
					
				};
				sourceView.replaceWith(replacement);
				blobPath = hit.getBlobPath();
				activeLine = hit.getLineNo();
				target.add(sourceView);
			}

			@Override
			protected void onCompleteAdvancedSearch(AjaxRequestTarget target, List<QueryHit> hits) {
				BlobAdvancedSearchResultPanel searchResult = new BlobAdvancedSearchResultPanel("searchResult", hits) {

					@Override
					protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
						blobPath = hit.getBlobPath();
						activeLine = hit.getLineNo();
						target.add(sourceView);
					}
					
				};
				searchResult.setOutputMarkupId(true);
				getPage().get("searchResult").replaceWith(searchResult);
				target.add(searchResult);
			}
			
		});
		*/
		
		add(newSourceView("sourceView", blobPath, null));
		add(new WebMarkupContainer("searchResult").setOutputMarkupId(true));
	}
	
	private SourceViewPanel newSourceView(final String id, String blobPath, Integer activeLine) {
		Source source = openBlob(blobPath, activeLine);
		SourceViewPanel sourceView = new SourceViewPanel(id, repoModel, source) {

			@Override
			protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
				target.add(getPage().replace(newSourceView(id, hit.getBlobPath(), hit.getLineNo())));
			}

			@Override
			protected void onCompleteOccurrencesSearch(AjaxRequestTarget target, List<QueryHit> hits) {
				BlobAdvancedSearchResultPanel searchResult = new BlobAdvancedSearchResultPanel("searchResult", hits) {

					@Override
					protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
						target.add(newSourceView(id, hit.getBlobPath(), hit.getLineNo()));
					}
					
				};
				searchResult.setOutputMarkupId(true);
				getPage().replace(searchResult);
				target.add(searchResult);
			}
			
		};
		sourceView.setOutputMarkupId(true);
		return sourceView;
	}
	
	private Source openBlob(String blobPath, Integer activeLine) {
		org.eclipse.jgit.lib.Repository jgitRepo = repoModel.getObject().openAsJGitRepo();
		try {
			RevTree revTree = new RevWalk(jgitRepo).parseCommit(repoModel.getObject().resolveRevision(revision)).getTree();
			TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, blobPath, revTree);
			ObjectLoader objectLoader = jgitRepo.open(treeWalk.getObjectId(0));
			String content = new String(objectLoader.getCachedBytes());
			return new Source(revision, treeWalk.getPathString(), content, activeLine);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			jgitRepo.close();
		}
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		super.onDetach();
	}

}
