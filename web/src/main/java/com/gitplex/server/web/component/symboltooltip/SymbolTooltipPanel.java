package com.gitplex.server.web.component.symboltooltip;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.jsymbol.Symbol;
import com.gitplex.server.GitPlex;
import com.gitplex.server.git.Blob;
import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.support.TextRange;
import com.gitplex.server.search.IndexConstants;
import com.gitplex.server.search.SearchManager;
import com.gitplex.server.search.hit.QueryHit;
import com.gitplex.server.search.hit.SymbolHit;
import com.gitplex.server.search.query.BlobQuery;
import com.gitplex.server.search.query.PathQuery;
import com.gitplex.server.search.query.SymbolQuery;
import com.gitplex.server.search.query.TextQuery;
import com.gitplex.server.web.behavior.AbstractPostAjaxBehavior;
import com.gitplex.server.web.behavior.RunTaskBehavior;
import com.gitplex.server.web.component.link.ViewStateAwareAjaxLink;
import com.gitplex.server.web.page.depot.blob.DepotBlobPage;
import com.gitplex.server.web.page.depot.blob.search.result.SearchResultPanel;

@SuppressWarnings("serial")
public abstract class SymbolTooltipPanel extends Panel {

	private static final int QUERY_ENTRIES = 20;
	
	private final IModel<Depot> depotModel;
	
	private String revision = "";
	
	private String symbolName = "";
	
	private List<QueryHit> symbolHits = new ArrayList<>();
	
	public SymbolTooltipPanel(String id, IModel<Depot> depotModel) {
		super(id);
		
		this.depotModel = depotModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer content = new WebMarkupContainer("content");
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
				item.add(hit.renderIcon("icon"));
				AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						String script = String.format("gitplex.server.symboltooltip.removeTooltip(document.getElementById('%s'));", 
								SymbolTooltipPanel.this.getMarkupId());
						target.prependJavaScript(script);						
						onSelect(target, hit);
					}
					
				};

				CharSequence url = RequestCycle.get().urlFor(DepotBlobPage.class, getQueryHitParams(hit));
				link.add(AttributeAppender.replace("href", url.toString()));
				link.add(hit.render("label"));
				link.add(new Label("scope", hit.getNamespace()).setVisible(hit.getNamespace()!=null));
				
				item.add(link);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!symbolHits.isEmpty());
			}
			
		});
		
		content.add(new ViewStateAwareAjaxLink<Void>("findOccurrences") {

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
						String script = String.format("gitplex.server.symboltooltip.removeTooltip(document.getElementById('%s'));", 
								SymbolTooltipPanel.this.getMarkupId());
						target.prependJavaScript(script);						
						List<QueryHit> hits;						
						// do this check to avoid TooGeneralQueryException
						if (symbolName.length() >= IndexConstants.NGRAM_SIZE) {
							BlobQuery query = new TextQuery.Builder()
									.term(symbolName)
									.wholeWord(true)
									.caseSensitive(true)
									.count(SearchResultPanel.MAX_QUERY_ENTRIES)
									.build();
							try {
								SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
								ObjectId commit = depotModel.getObject().getRevCommit(revision);
								hits = searchManager.search(depotModel.getObject(), commit, query);
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}								
						} else {
							hits = new ArrayList<>();
						}
						onOccurrencesQueried(target, hits);
					}
					
				});
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				// set href in onComponentTag in order to keep it up to date with symbol value
				CharSequence url = RequestCycle.get().urlFor(DepotBlobPage.class, getFindOccurrencesParams());
				tag.put("href", url.toString());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				runTaskBehavior.requestRun(target);
			}

		});

		add(new AbstractPostAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				revision = params.getParameterValue("revision").toString();
				symbolName = params.getParameterValue("symbol").toString();

				if (symbolName.startsWith("#include")) { 
					// handle c/c++ include directive as CodeMirror return the whole line as a meta  
					symbolName = symbolName.substring("#include".length()).trim();
				}

				boolean canBePath = symbolName.indexOf('\'') != -1 || symbolName.indexOf('"') != -1;
				// normalize the symbol
				String charsToStrip = "@'\"./\\";
				symbolName = StringUtils.stripEnd(StringUtils.stripStart(symbolName, charsToStrip), charsToStrip);
				symbolName = StringUtils.replace(symbolName, "\\", "/");
				
				symbolHits.clear();
				
				// do this check to avoid TooGeneralQueryException
				if (symbolName.length() != 0 && symbolName.indexOf('?') == -1 && symbolName.indexOf('*') == -1) {
					BlobIdent blobIdent = new BlobIdent(revision, getBlobPath(), FileMode.TYPE_FILE);
					Blob blob = depotModel.getObject().getBlob(blobIdent);
					
					if (symbolHits.size() < QUERY_ENTRIES) {
						// first find in current file for matched symbols
						List<Symbol> symbols = GitPlex.getInstance(SearchManager.class).getSymbols(depotModel.getObject(), 
								blob.getBlobId(), getBlobPath());
						if (symbols != null) {
							for (Symbol symbol: symbols) {
								if (symbolHits.size() < QUERY_ENTRIES 
										&& symbol.isSearchable() 
										&& symbolName.equals(symbol.getName()) 
										&& symbol.isPrimary()) {
									symbolHits.add(new SymbolHit(getBlobPath(), symbol, null));
								}
							}
							for (Symbol symbol: symbols) {
								if (symbolHits.size() < QUERY_ENTRIES 
										&& symbol.isSearchable() 
										&& symbolName.equals(symbol.getName())
										&& !symbol.isPrimary()) {
									symbolHits.add(new SymbolHit(getBlobPath(), symbol, null));
								}
							}
						}
					}					
					
					if (symbolHits.size() < QUERY_ENTRIES) {
						// then find in other files for public symbols
						try {
							SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
							ObjectId commit = depotModel.getObject().getRevCommit(revision);
							BlobQuery query;
							if (symbolHits.size() < QUERY_ENTRIES) {
								query = new SymbolQuery.Builder().term(symbolName)
										.excludeBlobPath(blobIdent.path)
										.primary(true)
										.local(false)
										.caseSensitive(true)
										.count(QUERY_ENTRIES)
										.build();
								symbolHits.addAll(searchManager.search(depotModel.getObject(), commit, query));
							}							
							if (symbolHits.size() < QUERY_ENTRIES) {
								query = new SymbolQuery.Builder().term(symbolName)
										.excludeBlobPath(blobIdent.path)
										.primary(false)
										.local(false)
										.caseSensitive(true)
										.count(QUERY_ENTRIES - symbolHits.size())
										.build();
								symbolHits.addAll(searchManager.search(depotModel.getObject(), commit, query));
							}
							if (canBePath && symbolHits.size() < QUERY_ENTRIES) {
								query = new PathQuery(null, symbolName, QUERY_ENTRIES - symbolHits.size());
								symbolHits.addAll(searchManager.search(depotModel.getObject(), commit, query));
							}
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}								
					}					
				}
				target.add(content);
				String script = String.format("gitplex.server.symboltooltip.doneQuery('%s');", content.getMarkupId());
				target.appendJavaScript(script);
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				response.render(JavaScriptHeaderItem.forReference(new SymbolTooltipResourceReference()));
				
				ResourceReference ajaxIndicator =  new PackageResourceReference(
						SymbolTooltipPanel.class, "ajax-indicator.gif");
				String script = String.format("gitplex.server.symboltooltip.init('%s', %s, '%s');", 
						getMarkupId(), getCallbackFunction(explicit("revision"), explicit("symbol")), 
						RequestCycle.get().urlFor(ajaxIndicator, new PageParameters()));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

		});				
		
		add(AttributeAppender.append("class", " hidden symbol-tooltip-container"));
		
		setOutputMarkupId(true);
	}
	
	public PageParameters getQueryHitParams(QueryHit hit) {
		DepotBlobPage.State state = new DepotBlobPage.State();
		state.blobIdent.revision = revision;
		state.blobIdent.path = hit.getBlobPath();
		state.mark = TextRange.of(hit.getTokenPos());
		return DepotBlobPage.paramsOf(depotModel.getObject(), state);
	}
	
	public PageParameters getFindOccurrencesParams() {
		DepotBlobPage.State state = new DepotBlobPage.State();
		state.blobIdent.revision = revision;
		state.blobIdent.path = getBlobPath();
		state.query = symbolName;
		return DepotBlobPage.paramsOf(depotModel.getObject(), state);
	}
	
	public String getSymbol() {
		return symbolName;
	}

	public String getRevision() {
		return revision;
	}
	
	@Override
	protected void onDetach() {
		depotModel.detach();
		super.onDetach();
	}

	protected abstract String getBlobPath();
	
	protected abstract void onSelect(AjaxRequestTarget target, QueryHit hit);

	protected abstract void onOccurrencesQueried(AjaxRequestTarget target, List<QueryHit> hits);
	
}
