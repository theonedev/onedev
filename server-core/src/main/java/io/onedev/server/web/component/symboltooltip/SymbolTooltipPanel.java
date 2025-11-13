package io.onedev.server.web.component.symboltooltip;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
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
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.search.code.CodeSearchService;
import io.onedev.server.search.code.IndexConstants;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.hit.SymbolHit;
import io.onedev.server.search.code.query.BlobQuery;
import io.onedev.server.search.code.query.SymbolQuery;
import io.onedev.server.search.code.query.TextQuery;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.CtrlClickBehavior;
import io.onedev.server.web.behavior.RunTaskBehavior;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;

public abstract class SymbolTooltipPanel extends Panel {

	private static final int QUERY_ENTRIES = 20;

	private static final int BEFORE_CONTEXT_SIZE = 5;

	private static final int AFTER_CONTEXT_SIZE = 5;

	private static final int AT_START_CONTEXT_SIZE = 200;

	private static final Logger logger = LoggerFactory.getLogger(SymbolTooltipPanel.class);
	
	private String revision = "";
	
	private String symbolName = "";

	private String symbolPosition = "";
	
	private List<QueryHit> symbolHits = new ArrayList<>();
	
	@Inject
	private CodeSearchService searchService;

	@Inject
	private SettingService settingService;

	@Inject
	private ObjectMapper objectMapper;

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
		
		content.add(new ListView<QueryHit>("definitions", new AbstractReadOnlyModel<>() {

			@Override
			public List<QueryHit> getObject() {
				return symbolHits;
			}

		}) {

			@Override
			protected void populateItem(ListItem<QueryHit> item) {
				var hit = item.getModelObject();
				item.add(hit.renderIcon("icon"));
				
				AjaxLink<Void> delegateLink = new ViewStateAwareAjaxLink<Void>("delegateLink") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						String script = String.format("onedev.server.symboltooltip.removeTooltip(document.getElementById('%s'));", 
								SymbolTooltipPanel.this.getMarkupId());
						target.prependJavaScript(script);						
						onSelect(target, hit);
					}
					
				};
				item.add(delegateLink);
				
				WebMarkupContainer link = new WebMarkupContainer("link");
				link.add(new CtrlClickBehavior(delegateLink));
				
				CharSequence url = RequestCycle.get().urlFor(ProjectBlobPage.class, getQueryHitParams(hit));
				link.add(AttributeAppender.replace("href", url.toString()));
				link.add(hit.render("label"));
				link.add(new Label("scope", hit.getNamespace()).setVisible(hit.getNamespace()!=null));
				
				item.add(link);
				item.setOutputMarkupId(true);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!symbolHits.isEmpty());
			}
			
		});
		content.add(new WebMarkupContainer("definitionInferHint") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(settingService.getAISetting().getLiteModelSetting() == null && symbolHits.size() > 1);
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
							int maxQueryEntries = settingService.getPerformanceSetting().getMaxCodeSearchEntries();
							var query = new TextQuery.Builder(symbolName)
									.wholeWord(true)
									.caseSensitive(true)
									.count(maxQueryEntries)
									.build();
							ObjectId commit = getProject().getRevCommit(revision, true);
							hits = searchService.search(getProject(), commit, query);
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
		
		var definitionInferBehavior = new AbstractPostAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
			}
			
		};
		add(definitionInferBehavior);

		add(new AbstractPostAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				var action = params.getParameterValue("action").toString();
				if (action.equals("query")) {
					revision = params.getParameterValue("revision").toString();
					symbolName = params.getParameterValue("symbolName").toString();
					symbolPosition = params.getParameterValue("symbolPosition").toString();

					if (symbolName.startsWith("#include")) { 
						// handle c/c++ include directive as CodeMirror return the whole line as a meta  
						symbolName = symbolName.substring("#include".length()).trim();
					}

					String charsToStrip = "@#'\"./\\";
					symbolName = StringUtils.stripEnd(StringUtils.stripStart(symbolName, charsToStrip), charsToStrip);
					symbolHits.clear();
					
					// do this check to avoid TooGeneralQueryException
					if (symbolName.length() != 0 && symbolName.indexOf('?') == -1 && symbolName.indexOf('*') == -1) {
						BlobIdent blobIdent = new BlobIdent(revision, getBlobPath(), FileMode.TYPE_FILE);
						Blob blob = getProject().getBlob(blobIdent, true);
						
						if (symbolHits.size() < QUERY_ENTRIES) {
							// first find in current file for matched symbols
							List<Symbol> symbols = searchService.getSymbols(getProject(),
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
							ObjectId commit = getProject().getRevCommit(revision, true);
							BlobQuery query;
							if (symbolHits.size() < QUERY_ENTRIES) {
								query = new SymbolQuery.Builder(symbolName)
										.caseSensitive(true)
										.excludeBlobPath(blobIdent.path)
										.primary(true)
										.local(false)
										.count(QUERY_ENTRIES)
										.build();
								symbolHits.addAll(searchService.search(getProject(), commit, query));
							}							
							if (symbolHits.size() < QUERY_ENTRIES) {
								query = new SymbolQuery.Builder(symbolName)
										.caseSensitive(true)
										.excludeBlobPath(blobIdent.path)
										.primary(false)
										.local(false)
										.count(QUERY_ENTRIES - symbolHits.size())
										.build();
								symbolHits.addAll(searchService.search(getProject(), commit, query));
							}
						}					
					}

					target.add(content);

					CharSequence callback;
					if (settingService.getAISetting().getLiteModelSetting() != null && symbolHits.size() > 1)
						callback = getCallbackFunction(explicit("action"));
					else 
						callback = "undefined";
					String script = String.format("onedev.server.symboltooltip.doneQuery('%s', %s);", 
						content.getMarkupId(), callback);
					target.appendJavaScript(script);
				} else {
					var liteModel = settingService.getAISetting().getLiteModel();
					int index;
					try {
						ObjectMapper mapperCopy = objectMapper.copy();
						mapperCopy.addMixIn(PlanarRange.class, IgnorePlanarRangeMixin.class);
						mapperCopy.addMixIn(LinearRange.class, IgnoreLinearRangeMixin.class);
						var jsonOfSymbolHits = mapperCopy.writeValueAsString(symbolHits);
						var symbolContext = getSymbolContext(symbolPosition, BEFORE_CONTEXT_SIZE, 
								AFTER_CONTEXT_SIZE, AT_START_CONTEXT_SIZE);
						var jsonOfSymbolContext = mapperCopy.writeValueAsString(symbolContext);
						var systemMessage = new SystemMessage("""
								You are familiar with various programming languages. Given a symbol name, a json object of 
								symbol context, and a json array of possible symbol definitions, please determine the most 
								likely symbol definition and return its index in the array. Symbol definition may contain 
								parent symbol, and this is where the symbol is defined inside (namespace, package etc). 
								The @type property in symbol definition means category/kind of the symbol (type, method, 
								variable etc).

								IMPORTANT: only return index of the definition, no other text or comments.
								""");

						var userMessage = new UserMessage(String.format("""
								Symbol name: 
								%s

								Symbol context json: 
								%s

								Possible symbol definitions json:
								%s
								""", symbolName, jsonOfSymbolContext, jsonOfSymbolHits));
						index = Integer.parseInt(liteModel.chat(systemMessage, userMessage).aiMessage().text());
						if (index < 0 || index >= symbolHits.size())
							Session.get().warn("Unable to find most likely definition");
					} catch (Exception e) {
						index = -1;
						logger.error("Error inferring most likely symbol definition", e);
						Session.get().error("Error inferring most likely symbol definition, check server log for details");
					}					
					var script = String.format("onedev.server.symboltooltip.doneInfer('%s', %d);", 
							getMarkupId() + "-symbol-tooltip", index);
					target.appendJavaScript(script);
				}
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				response.render(JavaScriptHeaderItem.forReference(new SymbolTooltipResourceReference()));
				
				var callback = getCallbackFunction(explicit("action"), explicit("revision"), 
						explicit("symbolName"), explicit("symbolPosition"));
				String script = String.format("onedev.server.symboltooltip.init('%s', %s, %s);", 
						getMarkupId(), callback, getSymbolPositionCalcFunction());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

		});				
		
		add(AttributeAppender.append("class", " hidden symbol-tooltip-container"));
		
		setOutputMarkupId(true);
	}
	
	public PageParameters getQueryHitParams(QueryHit hit) {
		BlobIdent blobIdent = new BlobIdent(revision, hit.getBlobPath(), FileMode.REGULAR_FILE.getBits());
		ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
		state.position = BlobRenderer.getSourcePosition(hit.getHitPos());
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

	protected abstract String getSymbolPositionCalcFunction();

	protected abstract SymbolContext getSymbolContext(String symbolPosition, int beforeContextSize, 
			int afterContextSize, int atStartContextSize);

}

@JsonIgnoreType
interface IgnorePlanarRangeMixin {
}

@JsonIgnoreType
interface IgnoreLinearRangeMixin {
}