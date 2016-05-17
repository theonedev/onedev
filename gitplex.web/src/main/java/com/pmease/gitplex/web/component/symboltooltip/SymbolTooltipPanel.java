package com.pmease.gitplex.web.component.symboltooltip;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.wicket.assets.align.AlignResourceReference;
import com.pmease.commons.wicket.behavior.RunTaskBehavior;
import com.pmease.commons.wicket.component.PreventDefaultAjaxLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.component.Mark;
import com.pmease.gitplex.search.SearchManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.BlobQuery;
import com.pmease.gitplex.search.query.SymbolQuery;
import com.pmease.gitplex.search.query.TextQuery;
import com.pmease.gitplex.web.component.depotfile.blobsearch.result.SearchResultPanel;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage.HistoryState;

@SuppressWarnings("serial")
public abstract class SymbolTooltipPanel extends Panel {

	private static final int QUERY_ENTRIES = 20;
	
	private final IModel<Depot> depotModel;
	
	private final IModel<PullRequest> requestModel;
	
	private ObjectId commitId = ObjectId.zeroId();
	
	private String symbol = "";
	
	private List<QueryHit> symbolHits = new ArrayList<>();
	
	public SymbolTooltipPanel(String id, IModel<Depot> depotModel, IModel<PullRequest> requestModel) {
		super(id);
		
		this.depotModel = depotModel;
		this.requestModel = requestModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final WebMarkupContainer content = new WebMarkupContainer("content");
		content.setOutputMarkupId(true);
		add(content);
		
		content.add(new ListView<QueryHit>("declarations", new AbstractReadOnlyModel<List<QueryHit>>() {

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
				AjaxLink<Void> link = new PreventDefaultAjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						String script = String.format("gitplex.symboltooltip.removeTooltip(document.getElementById('%s'));", 
								SymbolTooltipPanel.this.getMarkupId());
						target.prependJavaScript(script);						
						onSelect(target, hit);
					}
					
				};

				CharSequence url = RequestCycle.get().urlFor(DepotFilePage.class, getQueryHitParams(hit));
				link.add(AttributeAppender.replace("href", url.toString()));
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
		
		content.add(new PreventDefaultAjaxLink<Void>("findOccurrences") {

			private RunTaskBehavior runTaskBehavior;
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(runTaskBehavior = new RunTaskBehavior() {
					
					@Override
					public void requestRun(AjaxRequestTarget target) {
						super.requestRun(target);
						
						String script = String.format(""
								+ "var $tooltip=$(document.getElementById('%s').tooltip);"
								+ "$tooltip.align($tooltip.data('alignment'));", 
								SymbolTooltipPanel.this.getMarkupId());
						target.appendJavaScript(script);
					}

					@Override
					protected void runTask(AjaxRequestTarget target) {
						String script = String.format("gitplex.symboltooltip.removeTooltip(document.getElementById('%s'));", 
								SymbolTooltipPanel.this.getMarkupId());
						target.prependJavaScript(script);						
						
						BlobQuery query = new TextQuery(symbol, false, true, true, 
									null, null, SearchResultPanel.MAX_QUERY_ENTRIES);
						try {
							SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
							List<QueryHit> hits = searchManager.search(depotModel.getObject(), commitId, query);
							onOccurrencesQueried(target, hits);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}								
						
					}
					
				});
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				// set href in onComponentTag in order to keep it up to date with symbol value
				CharSequence url = RequestCycle.get().urlFor(DepotFilePage.class, getFindOccurrencesParams());
				tag.put("href", url.toString());
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
				commitId = ObjectId.fromString(params.getParameterValue("commit").toString());
				symbol = params.getParameterValue("symbol").toString();
				if (symbol.startsWith("@"))
					symbol = symbol.substring(1);
				try {
					SymbolQuery query = new SymbolQuery(symbol, true, true, null, null, QUERY_ENTRIES);
					SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
					symbolHits = searchManager.search(depotModel.getObject(), commitId, query);
					if (symbolHits.size() < QUERY_ENTRIES) {
						query = new SymbolQuery(symbol, false, true, null, null, QUERY_ENTRIES - symbolHits.size());
						symbolHits.addAll(searchManager.search(depotModel.getObject(), commitId, query));
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}								
				target.add(content);
				String script = String.format("gitplex.symboltooltip.doneQuery('%s');", content.getMarkupId());
				target.appendJavaScript(script);
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				response.render(JavaScriptHeaderItem.forReference(AlignResourceReference.INSTANCE));
				response.render(JavaScriptHeaderItem.forReference(
						new JavaScriptResourceReference(SymbolTooltipPanel.class, "symbol-tooltip.js")));
				response.render(CssHeaderItem.forReference(
						new CssResourceReference(SymbolTooltipPanel.class, "symbol-tooltip.css")));
				
				ResourceReference ajaxIndicator =  new PackageResourceReference(
						SymbolTooltipPanel.class, "ajax-indicator.gif");
				String script = String.format("gitplex.symboltooltip.init('%s', %s, '%s');", 
						getMarkupId(), getCallbackFunction(explicit("commit"), explicit("symbol")), 
						RequestCycle.get().urlFor(ajaxIndicator, new PageParameters()));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

		});				
		
		add(AttributeAppender.append("class", " hidden"));
		
		setOutputMarkupId(true);
	}
	
	public PageParameters getQueryHitParams(QueryHit hit) {
		HistoryState state = new HistoryState();
		state.blobIdent.revision = commitId.name();
		state.blobIdent.path = hit.getBlobPath();
		state.mark = Mark.of(hit.getTokenPos());
		state.requestId = PullRequest.idOf(requestModel.getObject());
		return DepotFilePage.paramsOf(depotModel.getObject(), state);
	}
	
	public PageParameters getFindOccurrencesParams() {
		HistoryState state = new HistoryState();
		state.blobIdent.revision = commitId.name();
		state.blobIdent.path = getBlobPath();
		state.requestId = PullRequest.idOf(requestModel.getObject());
		state.query = symbol;
		return DepotFilePage.paramsOf(depotModel.getObject(), state);
	}
	
	public String getSymbol() {
		return symbol;
	}

	public ObjectId getCommitId() {
		return commitId;
	}
	
	@Override
	protected void onDetach() {
		depotModel.detach();
		requestModel.detach();
		super.onDetach();
	}

	protected abstract String getBlobPath();
	
	protected abstract void onSelect(AjaxRequestTarget target, QueryHit hit);

	protected abstract void onOccurrencesQueried(AjaxRequestTarget target, List<QueryHit> hits);
	
}
