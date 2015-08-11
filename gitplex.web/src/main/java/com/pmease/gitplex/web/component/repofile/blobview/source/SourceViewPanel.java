package com.pmease.gitplex.web.component.repofile.blobview.source;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.eclipse.jgit.lib.FileMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.pmease.commons.git.Blame;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.lang.extractors.ExtractException;
import com.pmease.commons.lang.extractors.Extractor;
import com.pmease.commons.lang.extractors.Extractors;
import com.pmease.commons.lang.extractors.Symbol;
import com.pmease.commons.lang.extractors.TokenPosition;
import com.pmease.commons.wicket.assets.codemirror.CodeMirrorResourceReference;
import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.commons.wicket.behavior.RunTaskBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.search.SearchManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.BlobQuery;
import com.pmease.gitplex.search.query.SymbolQuery;
import com.pmease.gitplex.search.query.TextQuery;
import com.pmease.gitplex.web.DateUtils;
import com.pmease.gitplex.web.component.repofile.blobsearch.result.SearchResultPanel;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewPanel;
import com.pmease.gitplex.web.page.repository.commit.RepoCommitPage;

@SuppressWarnings("serial")
public class SourceViewPanel extends BlobViewPanel {

	private static final int QUERY_ENTRIES = 20;
	
	private static final Logger logger = LoggerFactory.getLogger(SourceViewPanel.class);
	
	private Component codeContainer;
	
	private OutlinePanel outlinePanel;
	
	private WebMarkupContainer symbolsContainer;
	
	private String symbol = "";
	
	private List<QueryHit> symbolHits = new ArrayList<>();
	
	private final List<Symbol> symbols = new ArrayList<>();
	
	public SourceViewPanel(String id, BlobViewContext context) {
		super(id, context);
		
		Preconditions.checkArgument(context.getBlob().getText() != null);
		
		Extractor extractor = GitPlex.getInstance(Extractors.class).getExtractor(context.getState().file.path);
		if (extractor != null) {
			try {
				symbols.addAll(extractor.extract(context.getBlob().getText().getContent()));
			} catch (ExtractException e) {
				logger.debug("Error extracting symbols from blob: " + context.getState().file, e);
			}
		}
	}
	
	@Override
	protected WebMarkupContainer newCustomActions(String id) {
		Fragment fragment = new Fragment(id, "actionsFrag", this);
		fragment.setVisible(!symbols.isEmpty());
		return fragment;
	}

	public void highlightToken(AjaxRequestTarget target, @Nullable TokenPosition tokenPos) {
		String json;
		if (tokenPos != null) {
			try {
				json = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(tokenPos);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		} else {
			json = "undefined";
		}
		String script = String.format("gitplex.sourceview.highlightToken('%s', %s);", 
				codeContainer.getMarkupId(), json);
		target.appendJavaScript(script);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(codeContainer = new WebMarkupContainer("code"));
		codeContainer.setOutputMarkupId(true);
		
		add(outlinePanel = new OutlinePanel("outline", symbols) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Symbol symbol) {
				highlightToken(target, symbol.getPos());
			}
			
		});
		outlinePanel.setVisible(!symbols.isEmpty());
		
		add(symbolsContainer = new WebMarkupContainer("symbols"));
		symbolsContainer.setOutputMarkupId(true);
		symbolsContainer.add(new ListView<QueryHit>("declarations", new AbstractReadOnlyModel<List<QueryHit>>() {

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
						String script = String.format(
								"$('#%s .CodeMirror')[0].CodeMirror.hideTokenHover();", 
								codeContainer.getMarkupId());
						target.prependJavaScript(script);
						BlobIdent blobIdent = new BlobIdent(
								context.getState().file.revision, 
								hit.getBlobPath(), 
								FileMode.REGULAR_FILE.getBits());
						context.onSelect(target, blobIdent, hit.getTokenPos());
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
		
		symbolsContainer.add(new AjaxLink<Void>("findOccurrences") {

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
							List<QueryHit> hits = searchManager.search(context.getRepository(), 
									context.getState().file.revision, query);
							String script = String.format(
									"$('#%s .CodeMirror')[0].CodeMirror.hideTokenHover();", 
									codeContainer.getMarkupId());
							target.prependJavaScript(script);
							context.onSearchComplete(target, hits);
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
					symbolHits = searchManager.search(context.getRepository(), context.getState().file.revision, query);
					if (symbolHits.size() < QUERY_ENTRIES) {
						query = new SymbolQuery(symbol, false, true, null, null, QUERY_ENTRIES - symbolHits.size());
						symbolHits.addAll(searchManager.search(context.getRepository(), 
								context.getState().file.revision, query));
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}								
				target.add(symbolsContainer);
				String script = String.format("gitplex.sourceview.symbolsQueried('%s', '%s');", 
						codeContainer.getMarkupId(), symbolsContainer.getMarkupId());
				target.appendJavaScript(script);
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);

				response.render(JavaScriptHeaderItem.forReference(CookiesResourceReference.INSTANCE));
				response.render(JavaScriptHeaderItem.forReference(CodeMirrorResourceReference.INSTANCE));
				
				response.render(JavaScriptHeaderItem.forReference(
						new JavaScriptResourceReference(SourceViewPanel.class, "source-view.js")));
				response.render(CssHeaderItem.forReference(
						new CssResourceReference(SourceViewPanel.class, "source-view.css")));
				
				String highlightToken;
				try {
					highlightToken = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(context.getState().tokenPos);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				} 
				ResourceReference ajaxIndicator =  new PackageResourceReference(SourceViewPanel.class, "ajax-indicator.gif");
				String script = String.format("gitplex.sourceview.init('%s', '%s', '%s', %s, '%s', %s, %s);", 
						codeContainer.getMarkupId(), 
						StringEscapeUtils.escapeEcmaScript(context.getBlob().getText().getContent()),
						context.getState().file.path, 
						highlightToken,
						RequestCycle.get().urlFor(ajaxIndicator, new PageParameters()), 
						getCallbackFunction(CallbackParameter.explicit("symbol")), 
						getBlameCommits());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});		
		
		setOutputMarkupId(true);
	}
	
	public void onBlameChange(AjaxRequestTarget target) {
		String script = String.format("gitplex.sourceview.blame('%s', %s);", 
				codeContainer.getMarkupId(), getBlameCommits());
		target.appendJavaScript(script);
	}

	private String getBlameCommits() {
		if (context.getState().blame) {
			List<BlameCommit> commits = new ArrayList<>();
			
			String commitHash = context.getRepository().getObjectId(context.getState().file.revision).name();
			
			for (Blame blame: context.getRepository().git().blame(commitHash, context.getState().file.path).values()) {
				BlameCommit commit = new BlameCommit();
				commit.authorDate = DateUtils.formatDate(blame.getCommit().getAuthor().getWhen());
				commit.authorName = StringEscapeUtils.escapeHtml4(blame.getCommit().getAuthor().getName());
				commit.hash = GitUtils.abbreviateSHA(blame.getCommit().getHash(), 7);
				commit.message = blame.getCommit().getSubject();
				PageParameters params = RepoCommitPage.paramsOf(context.getRepository(), blame.getCommit().getHash());
				commit.url = RequestCycle.get().urlFor(RepoCommitPage.class, params).toString();
				commit.ranges = blame.getRanges();
				commits.add(commit);
			}
			try {
				return GitPlex.getInstance(ObjectMapper.class).writeValueAsString(commits);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		} else {
			return "undefined";
		}
	}
	
	@SuppressWarnings("unused")
	private static class BlameCommit {
		
		String hash;
		
		String message;
		
		String url;
		
		String authorName;
		
		String authorDate;
		
		List<Blame.Range> ranges;
	}
	
}
