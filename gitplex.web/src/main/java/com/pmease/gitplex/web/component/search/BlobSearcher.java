package com.pmease.gitplex.web.component.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.SearchManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.BlobQuery;
import com.pmease.gitplex.search.query.SymbolQuery;
import com.pmease.gitplex.search.query.TextQuery;

@SuppressWarnings("serial")
public class BlobSearcher extends Panel {

	private static final Logger logger = LoggerFactory.getLogger(BlobSearcher.class);
	
	private static final int MAX_QUERY_ENTRIES = 20;
	
	private final IModel<Repository> repoModel;
	
	private final String commitHash;
	
	private final boolean caseSensitive;
	
	private TextField<String> input;
	
	private WebMarkupContainer symbolContainer;
	
	private WebMarkupContainer textContainer;

	private final static Map<BlobSearcher, Thread> symbolSearchThreads = 
			new MapMaker().concurrencyLevel(16).weakKeys().makeMap();
	
	private final static Map<BlobSearcher, List<QueryHit>> symbolSearchResults = 
			new MapMaker().concurrencyLevel(16).weakKeys().makeMap();
	
	private final static Map<BlobSearcher, Thread> textSearchThreads = 
			new MapMaker().concurrencyLevel(16).weakKeys().makeMap();
	
	private final static Map<BlobSearcher, List<QueryHit>> textSearchResults = 
			new MapMaker().concurrencyLevel(16).weakKeys().makeMap();
	
	public BlobSearcher(String id, IModel<Repository> repoModel, String commitHash, boolean caseSensitive) {
		super(id);
		
		this.repoModel = repoModel;
		this.commitHash = commitHash;
		this.caseSensitive = caseSensitive;
	}

	private WebMarkupContainer newHitsContainer(String componentId, final boolean forSymbols) {
		final Fragment fragment = new Fragment(componentId, "hitsFrag", this);
		fragment.setOutputMarkupId(true);
		fragment.add(new ListView<QueryHit>("hits", new LoadableDetachableModel<List<QueryHit>>() {

			@Override
			protected List<QueryHit> load() {
				List<QueryHit> hits;
				if (forSymbols)
					hits = symbolSearchResults.remove(BlobSearcher.this);
				else
					hits = textSearchResults.remove(BlobSearcher.this);
				
				if (hits == null)
					hits = new ArrayList<>();
				return hits;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<QueryHit> item) {
				item.add(item.getModelObject().render("hit"));
			}
			
		});
		fragment.add(new WebSocketRenderBehavior() {

			@Override
			protected Object getTrait() {
				return fragment.getPath();
			}
			
		});
		return fragment;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(symbolContainer = newHitsContainer("symbols", true));
		add(textContainer = newHitsContainer("texts", false));
		
		input = new TextField<>("input", Model.of(""));
		input.add(new OnChangeAjaxBehavior() {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				search(true);
				search(false);
			}			
		});
		add(input);
	}
	
	private void search(boolean forSymbol) {
		final BlobQuery query;
		final WebMarkupContainer hitsContainer;
		final Map<BlobSearcher, Thread> searchThreads;
		final Map<BlobSearcher, List<QueryHit>> searchResults;

		if (forSymbol) {
			query = new SymbolQuery(input.getInput(), false, caseSensitive, MAX_QUERY_ENTRIES);
			hitsContainer = symbolContainer;
			searchThreads = symbolSearchThreads;
			searchResults = symbolSearchResults;
		} else {
			query = new TextQuery(input.getInput(), caseSensitive, MAX_QUERY_ENTRIES);
			hitsContainer = textContainer;
			searchThreads = textSearchThreads;
			searchResults = textSearchResults;
		}
		
		ExecutorService executorService = GitPlex.getInstance(ExecutorService.class);
		final Page page = getPage();
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				try {
					Thread thread = Thread.currentThread();
					try {
						Thread prevThread = searchThreads.get(BlobSearcher.this);
						if (prevThread != null) synchronized (prevThread) {
							prevThread = searchThreads.get(BlobSearcher.this);
							if (prevThread != null)
								prevThread.interrupt();
						}
						searchThreads.put(BlobSearcher.this, thread);
						
						SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
						searchResults.put(BlobSearcher.this, searchManager.search(repoModel.getObject(), commitHash, query));
					} finally {
						synchronized (thread) {
							Thread.interrupted();
							searchThreads.remove(BlobSearcher.this);
						}
					}
					WebSocketRenderBehavior.requestToRender(hitsContainer.getPath(), page);
				} catch (Exception e) {
					if (!(e instanceof InterruptedException))
						logger.error("Error searching blob.", e);
				}
			}
			
		}); 
	}
	
	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
