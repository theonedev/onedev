package com.pmease.gitplex.web.component.repofile.blobview.source;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.lib.FileMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;
import org.unbescape.javascript.JavaScriptEscape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.pmease.commons.git.Blame;
import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.lang.extractors.ExtractException;
import com.pmease.commons.lang.extractors.Extractor;
import com.pmease.commons.lang.extractors.Extractors;
import com.pmease.commons.lang.extractors.Symbol;
import com.pmease.commons.util.Range;
import com.pmease.commons.wicket.assets.codemirror.CodeMirrorResourceReference;
import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.commons.wicket.assets.uri.URIResourceReference;
import com.pmease.commons.wicket.component.menu.MenuItem;
import com.pmease.commons.wicket.component.menu.MenuLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewContext.Mode;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewPanel;
import com.pmease.gitplex.web.component.symboltooltip.SymbolTooltipPanel;
import com.pmease.gitplex.web.page.depot.commit.CommitDetailPage;
import com.pmease.gitplex.web.page.depot.file.Mark;
import com.pmease.gitplex.web.util.DateUtils;

@SuppressWarnings("serial")
public class SourceViewPanel extends BlobViewPanel {

	private static final Logger logger = LoggerFactory.getLogger(SourceViewPanel.class);
	
	private static final String COOKIE_OUTLINE = "sourceView.outline";
	
	private final List<Symbol> symbols = new ArrayList<>();
	
	private final String viewState;
	
	private Component codeContainer;
	
	private OutlinePanel outlinePanel;

	private SymbolTooltipPanel symbolTooltip;
	
	public SourceViewPanel(String id, BlobViewContext context, @Nullable String viewState) {
		super(id, context);
		
		Blob blob = context.getDepot().getBlob(context.getBlobIdent());
		Preconditions.checkArgument(blob.getText() != null);
		
		Extractor extractor = GitPlex.getInstance(Extractors.class).getExtractor(context.getBlobIdent().path);
		if (extractor != null) {
			try {
				symbols.addAll(extractor.extract(blob.getText().getContent()));
			} catch (ExtractException e) {
				logger.debug("Error extracting symbols from blob: " + context.getBlobIdent(), e);
			}
		}
		
		this.viewState = viewState;
	}
	
	@Override
	public List<MenuItem> getMenuItems(MenuLink menuLink) {
		List<MenuItem> menuItems = new ArrayList<>();
		if (!symbols.isEmpty()) {
			menuItems.add(new MenuItem() {

				@Override
				public String getLabel() {
					return "Outline";
				}

				@Override
				public String getIconClass() {
					return outlinePanel.isVisible()?"fa fa-check":null;
				}

				@Override
				public AbstractLink newLink(String id) {
					return new AjaxLink<Void>(id) {

						@Override
						public void onClick(AjaxRequestTarget target) {
							menuLink.close();
							WebResponse response = (WebResponse) RequestCycle.get().getResponse();
							if (outlinePanel.isVisible()) {
								response.addCookie(new Cookie(COOKIE_OUTLINE, "no"));
								outlinePanel.setVisible(false);
							} else {
								response.addCookie(new Cookie(COOKIE_OUTLINE, "yes"));
								outlinePanel.setVisible(true);
							}
							target.add(outlinePanel);
							
							String script = String.format(""
									+ "var $sourceView = $('#%s').closest('.source-view');"
									+ "$sourceView.trigger('autofit', [$sourceView.outerWidth(), $sourceView.outerHeight()]);", 
									codeContainer.getMarkupId());
							target.appendJavaScript(script);
						}
						
					};
				}
				
			});
		} 
		return menuItems;
	}

	public void mark(AjaxRequestTarget target, Mark mark) {
		String script = String.format("gitplex.sourceview.mark('%s', %s);", 
				codeContainer.getMarkupId(), mark.toJSON());
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
				context.onSelect(target, context.getBlobIdent(), symbol.getPos());
			}
			
		});
		if (!symbols.isEmpty()) {
			WebRequest request = (WebRequest) RequestCycle.get().getRequest();
			Cookie cookie = request.getCookie(COOKIE_OUTLINE);
			if (cookie!=null && cookie.getValue().equals("yes"))
				outlinePanel.setVisible(true);
			else
				outlinePanel.setVisible(false);
		} else {
			outlinePanel.setVisible(false);
		}
		
		add(symbolTooltip = new SymbolTooltipPanel("symbolTooltip", new AbstractReadOnlyModel<Depot>() {

			@Override
			public Depot getObject() {
				return context.getDepot();
			}
			
		}, new AbstractReadOnlyModel<PullRequest>() {

			@Override
			public PullRequest getObject() {
				return context.getPullRequest();
			}
			
		}) {

			@Override
			protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
				BlobIdent blobIdent = new BlobIdent(
						getRevision(), hit.getBlobPath(), FileMode.REGULAR_FILE.getBits());
				context.onSelect(target, blobIdent, hit.getTokenPos());
			}

			@Override
			protected void onOccurrencesQueried(AjaxRequestTarget target, List<QueryHit> hits) {
				context.onSearchComplete(target, hits);
			}

			@Override
			protected String getBlobPath() {
				return context.getBlobIdent().path;
			}
			
		});

		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(URIResourceReference.INSTANCE));
		
		response.render(JavaScriptHeaderItem.forReference(CookiesResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(CodeMirrorResourceReference.INSTANCE));
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(SourceViewPanel.class, "source-view.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(SourceViewPanel.class, "source-view.css")));
		
		Blob blob = context.getDepot().getBlob(context.getBlobIdent());
		
		String blameCommitsJson;
		if (context.getMode() == Mode.BLAME) {
			List<BlameCommit> commits = new ArrayList<>();
			
			String commitHash = context.getDepot().getObjectId(context.getBlobIdent().revision).name();
			
			for (Blame blame: context.getDepot().git().blame(commitHash, context.getBlobIdent().path).values()) {
				BlameCommit commit = new BlameCommit();
				commit.commitDate = DateUtils.formatDate(blame.getCommit().getCommitter().getWhen());
				commit.authorName = HtmlEscape.escapeHtml5(blame.getCommit().getAuthor().getName());
				commit.hash = GitUtils.abbreviateSHA(blame.getCommit().getHash(), 7);
				commit.message = blame.getCommit().getSubject();
				CommitDetailPage.HistoryState state = new CommitDetailPage.HistoryState();
				state.path = context.getBlobIdent().path;
				PageParameters params = CommitDetailPage.paramsOf(context.getDepot(), 
						blame.getCommit().getHash(), state);
				commit.url = RequestCycle.get().urlFor(CommitDetailPage.class, params).toString();
				commit.ranges = blame.getRanges();
				commits.add(commit);
			}
			try {
				blameCommitsJson = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(commits);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		} else {
			blameCommitsJson = "undefined";
		}
		
		String script = String.format("gitplex.sourceview.init('%s', '%s', '%s', %s, '%s', '%s', %s, %s);", 
				codeContainer.getMarkupId(), 
				JavaScriptEscape.escapeJavaScript(blob.getText().getContent()),
				JavaScriptEscape.escapeJavaScript(context.getBlobIdent().path), 
				context.getMark()!=null?context.getMark().toJSON():"undefined",
				symbolTooltip.getMarkupId(), 
				context.getBlobIdent().revision, 
				blameCommitsJson, 
				viewState!=null?"JSON.parse('"+viewState+"')":"undefined");
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@SuppressWarnings("unused")
	private static class BlameCommit {
		
		String hash;
		
		String message;
		
		String url;
		
		String authorName;
		
		String commitDate;
		
		List<Range> ranges;
	}
	
}
