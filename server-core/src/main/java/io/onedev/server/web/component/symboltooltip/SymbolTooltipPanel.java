package io.onedev.server.web.component.symboltooltip;

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
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.jsymbol.Symbol;
import io.onedev.server.OneDev;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.search.code.IndexConstants;
import io.onedev.server.search.code.SearchManager;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.hit.SymbolHit;
import io.onedev.server.search.code.query.BlobQuery;
import io.onedev.server.search.code.query.SymbolQuery;
import io.onedev.server.search.code.query.TextQuery;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.RunTaskBehavior;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.renderers.source.SourceRendererProvider;
import io.onedev.server.web.page.project.blob.search.result.SearchResultPanel;

@SuppressWarnings("serial")
public abstract class SymbolTooltipPanel extends Panel {

	private static final int QUERY_ENTRIES = 20;
	
	private String revision = "";
	
	private String symbolName = "";
	
	private List<QueryHit> symbolHits = new ArrayList<>();
	
	public SymbolTooltipPanel(String id) {
		super(id);
	}
	
	protected abstract Project getProject();

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
						String script = String.format("onedev.server.symboltooltip.removeTooltip(document.getElementById('%s'));", 
								SymbolTooltipPanel.this.getMarkupId());
						target.prependJavaScript(script);						
						onSelect(target, hit);
					}
					
				};

				CharSequence url = RequestCycle.get().urlFor(ProjectBlobPage.class, getQueryHitParams(hit));
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
						String script = String.format("onedev.server.symboltooltip.removeTooltip(document.getElementById('%s'));", 
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
								SearchManager searchManager = OneDev.getInstance(SearchManager.class);
								ObjectId commit = getProject().getRevCommit(revision, true);
								hits = searchManager.search(getProject(), commit, query);
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
				CharSequence url = RequestCycle.get().urlFor(ProjectBlobPage.class, getFindOccurrencesParams());
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

				String charsToStrip = "@#'\"./\\";
				symbolName = StringUtils.stripEnd(StringUtils.stripStart(symbolName, charsToStrip), charsToStrip);
				symbolName = StringUtils.replace(symbolName, "\\", "/");
				if (symbolName.contains("/"))
					symbolName = StringUtils.substringAfterLast(symbolName, "/");
				
				symbolHits.clear();
				
				// do this check to avoid TooGeneralQueryException
				if (symbolName.length() != 0 && symbolName.indexOf('?') == -1 && symbolName.indexOf('*') == -1) {
					BlobIdent blobIdent = new BlobIdent(revision, getBlobPath(), FileMode.TYPE_FILE);
					Blob blob = getProject().getBlob(blobIdent, true);
					
					if (symbolHits.size() < QUERY_ENTRIES) {
						// first find in current file for matched symbols
						List<Symbol> symbols = OneDev.getInstance(SearchManager.class).getSymbols(getProject(), 
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
							SearchManager searchManager = OneDev.getInstance(SearchManager.class);
							ObjectId commit = getProject().getRevCommit(revision, true);
							BlobQuery query;
							if (symbolHits.size() < QUERY_ENTRIES) {
								query = new SymbolQuery.Builder().term(symbolName)
										.excludeBlobPath(blobIdent.path)
										.primary(true)
										.local(false)
										.caseSensitive(true)
										.count(QUERY_ENTRIES)
										.build();
								symbolHits.addAll(searchManager.search(getProject(), commit, query));
							}							
							if (symbolHits.size() < QUERY_ENTRIES) {
								query = new SymbolQuery.Builder().term(symbolName)
										.excludeBlobPath(blobIdent.path)
										.primary(false)
										.local(false)
										.caseSensitive(true)
										.count(QUERY_ENTRIES - symbolHits.size())
										.build();
								symbolHits.addAll(searchManager.search(getProject(), commit, query));
							}
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}								
					}					
				}
				target.add(content);
				String script = String.format("onedev.server.symboltooltip.doneQuery('%s');", content.getMarkupId());
				target.appendJavaScript(script);
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				response.render(JavaScriptHeaderItem.forReference(new SymbolTooltipResourceReference()));
				
				ResourceReference ajaxIndicator =  new PackageResourceReference(
						SymbolTooltipPanel.class, "ajax-indicator.gif");
				String script = String.format("onedev.server.symboltooltip.init('%s', %s, '%s');", 
						getMarkupId(), getCallbackFunction(explicit("revision"), explicit("symbol")), 
						RequestCycle.get().urlFor(ajaxIndicator, new PageParameters()));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

		});				
		
		add(AttributeAppender.append("class", " hidden symbol-tooltip-container"));
		
		setOutputMarkupId(true);
	}
	
	public PageParameters getQueryHitParams(QueryHit hit) {
		BlobIdent blobIdent = new BlobIdent(revision, hit.getBlobPath(), FileMode.REGULAR_FILE.getBits());
		ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
		state.position = SourceRendererProvider.getPosition(hit.getTokenPos());
		return ProjectBlobPage.paramsOf(getProject(), state);
	}
	
	public PageParameters getFindOccurrencesParams() {
		BlobIdent blobIdent = new BlobIdent(revision, getBlobPath(), FileMode.REGULAR_FILE.getBits());
		ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
		state.query = symbolName;
		return ProjectBlobPage.paramsOf(getProject(), state);
	}
	
	public String getSymbol() {
		return symbolName;
	}

	public String getRevision() {
		return revision;
	}
	
	protected abstract String getBlobPath();
	
	protected abstract void onSelect(AjaxRequestTarget target, QueryHit hit);

	protected abstract void onOccurrencesQueried(AjaxRequestTarget target, List<QueryHit> hits);
	
}
