package com.pmease.gitplex.web.component.symboltooltip;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.lang.extractors.Symbol;
import com.pmease.commons.wicket.behavior.RunTaskBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.SearchManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.BlobQuery;
import com.pmease.gitplex.search.query.SymbolQuery;
import com.pmease.gitplex.search.query.TextQuery;
import com.pmease.gitplex.web.component.repofile.blobsearch.result.SearchResultPanel;

@SuppressWarnings("serial")
public abstract class SymbolTooltipPanel extends Panel {

	private static final int QUERY_ENTRIES = 20;
	
	private String symbol;
	
	private List<QueryHit> symbolHits = new ArrayList<>();
	
	public SymbolTooltipPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<QueryHit>("declarations", new AbstractReadOnlyModel<List<QueryHit>>() {

			@Override
			public List<QueryHit> getObject() {
				return symbolHits;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<QueryHit> item) {
				final QueryHit hit = item.getModelObject();
				item.add(new Image("icon", hit.getIcon()) {

					@Override
					protected boolean shouldAddAntiCacheParameter() {
						return false;
					}
					
				});
				AjaxLink<Void> link = new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, hit);
					}
					
				};
				link.add(hit.render("label"));
				link.add(new Label("scope", hit.getScope()).setVisible(hit.getScope()!=null));
				
				item.add(link);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!symbolHits.isEmpty());
			}
			
		});
		
		add(new AjaxLink<Void>("findOccurrences") {

			private RunTaskBehavior runTaskBehavior;
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(runTaskBehavior = new RunTaskBehavior() {
					
					@Override
					protected void runTask(AjaxRequestTarget target) {
						BlobQuery query = new TextQuery(symbol, false, true, true, 
									null, null, SearchResultPanel.MAX_QUERY_ENTRIES);
						try {
							SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
							List<QueryHit> hits = searchManager.search(getRepository(), getRevision(), query);
							onSearchComplete(target, hits);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}								
						
					}
					
				});
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				runTaskBehavior.requestRun(target);
			}
			
		});
		
		add(new AbstractDefaultAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				symbol = params.getParameterValue("symbol").toString();
				if (symbol.startsWith("@"))
					symbol = symbol.substring(1);
				try {
					SymbolQuery query = new SymbolQuery(symbol, true, true, null, null, QUERY_ENTRIES);
					SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
					symbolHits = searchManager.search(getRepository(), getRevision(), query);
					if (symbolHits.size() < QUERY_ENTRIES) {
						query = new SymbolQuery(symbol, false, true, null, null, QUERY_ENTRIES - symbolHits.size());
						symbolHits.addAll(searchManager.search(getRepository(), getRevision(), query));
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}								
				target.add(SymbolTooltipPanel.this);
				String script = String.format("gitplex.sourceview.symbolsQueried('%s', '%s');", 
						codeContainer.getMarkupId(), symbolsContainer.getMarkupId());
				target.appendJavaScript(script);
			}

		});				
		
		setOutputMarkupId(true);
	}
	
	protected abstract Repository getRepository();
	
	protected abstract String getRevision();
	
	protected abstract void onSelect(AjaxRequestTarget target, QueryHit hit);

	protected abstract void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits);
}
