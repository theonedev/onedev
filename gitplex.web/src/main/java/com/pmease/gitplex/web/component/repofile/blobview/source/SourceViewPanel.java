package com.pmease.gitplex.web.component.repofile.blobview.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.lib.FileMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.pmease.commons.git.Blame;
import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.lang.extractors.ExtractException;
import com.pmease.commons.lang.extractors.Extractor;
import com.pmease.commons.lang.extractors.Extractors;
import com.pmease.commons.lang.extractors.Symbol;
import com.pmease.commons.loader.InheritableThreadLocalData;
import com.pmease.commons.util.Range;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.assets.codemirror.CodeMirrorResourceReference;
import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Comment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.manager.CommentManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.comment.InlineCommentPanel;
import com.pmease.gitplex.web.component.comment.event.CommentRemoved;
import com.pmease.gitplex.web.component.comment.event.CommentResized;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewContext.Mode;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewPanel;
import com.pmease.gitplex.web.component.symboltooltip.SymbolTooltipPanel;
import com.pmease.gitplex.web.page.depot.commit.CommitDetailPage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage.HistoryState;
import com.pmease.gitplex.web.util.DateUtils;
import com.pmease.gitplex.web.page.depot.file.Mark;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class SourceViewPanel extends BlobViewPanel {

	private static final Logger logger = LoggerFactory.getLogger(SourceViewPanel.class);
	
	private static final String INLINE_COMMENT_ID = "inlineComment";
	
	private final List<Symbol> symbols = new ArrayList<>();
	
	private final String clientState;
	
	private final IModel<List<Comment>> commentsModel = new LoadableDetachableModel<List<Comment>>() {

		@Override
		protected List<Comment> load() {
			List<Comment> comments = new ArrayList<>();
			PullRequest request = context.getPullRequest();
			if (request != null) {
				for (Comment comment: request.getComments()) {
					if (comment.getInlineInfo() != null) {
						GitPlex.getInstance(CommentManager.class).updateInlineInfo(comment);
						BlobIdent blobIdent = comment.getBlobIdent();
						if (blobIdent.equals(context.getBlobIdent()))
							comments.add(comment);
					}
				}
				Collections.sort(comments, new Comparator<Comment>() {
	
					@Override
					public int compare(Comment comment1, Comment comment2) {
						return comment1.getDate().compareTo(comment2.getDate());
					}
					
				});
			}
			return comments;
		}
		
	};	
	
	private Component codeContainer;
	
	private OutlinePanel outlinePanel;

	private SymbolTooltipPanel symbolTooltip;
	
	private RepeatingView newCommentForms;
	
	private RepeatingView commentWidgets;
	
	private AbstractDefaultAjaxBehavior addCommentBehavior;
	
	public SourceViewPanel(String id, BlobViewContext context, @Nullable String clientState) {
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
		
		this.clientState = clientState;
	}
	
	@Override
	protected WebMarkupContainer newLeftActions(String id) {
		Fragment fragment = new Fragment(id, "leftActionsFrag", this);
		
		HistoryState state = new HistoryState();
		state.blobIdent = context.getBlobIdent();
		state.requestId = PullRequest.idOf(context.getPullRequest());
		PageParameters params = DepotFilePage.paramsOf(context.getDepot(), state);
		String url = RequestCycle.get().urlFor(DepotFilePage.class, params).toString();
		fragment.add(new WebMarkupContainer("selectionPermalink")
				.add(AttributeAppender.replace("href", url)));
		return fragment;
	}
	
	@Override
	protected WebMarkupContainer newRightActions(String id) {
		Fragment fragment = new Fragment(id, "rightActionsFrag", this);
		fragment.setVisible(!symbols.isEmpty());
		return fragment;
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
		outlinePanel.setVisible(!symbols.isEmpty());
		
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

		add(newCommentForms = new RepeatingView("newComments"));
		
		commentWidgets = new RepeatingView("comments");
		
		for (Comment comment: commentsModel.getObject())
			commentWidgets.add(newCommentWidget(commentWidgets.newChildId(), comment));
		
		add(commentWidgets);
		
		if (context.getPullRequest() != null) {
			add(addCommentBehavior = new AbstractDefaultAjaxBehavior() {
				
				@Override
				protected void respond(AjaxRequestTarget target) {
					IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
					final int lineNo = params.getParameterValue("lineNo").toInt();
					
					final Form<?> newCommentForm = new Form<Void>(newCommentForms.newChildId());
					newCommentForm.setOutputMarkupId(true);
					
					final CommentInput input;
					newCommentForm.add(input = new CommentInput("input", new AbstractReadOnlyModel<PullRequest>() {

						@Override
						public PullRequest getObject() {
							return context.getPullRequest();
						}
						
					}, Model.of("")));
					input.setRequired(true);
					
					final NotificationPanel feedback = new NotificationPanel("feedback", input); 
					feedback.setOutputMarkupPlaceholderTag(true);
					newCommentForm.add(feedback);
					
					newCommentForm.add(new AjaxLink<Void>("cancel") {
	
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(newCommentForm));
						}
						
						@Override
						public void onClick(AjaxRequestTarget target) {
							newCommentForm.remove();
							String script = String.format("$('#%s').parent()[0].lineWidget.clear();", newCommentForm.getMarkupId());
							target.appendJavaScript(script);
						}
						
					});
					
					newCommentForm.add(new AjaxSubmitLink("save") {
	
						@Override
						protected void onError(AjaxRequestTarget target, Form<?> form) {
							super.onError(target, form);
							target.add(feedback);
						}
	
						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							super.onSubmit(target, form);
							
							BlobIdent commentAt = context.getBlobIdent();
							BlobIdent compareWith = context.getBlobIdent();
							
							Comment comment;
							InheritableThreadLocalData.set(new WebSocketRenderBehavior.PageId(getPage().getPageId()));
							try {
								comment = GitPlex.getInstance(CommentManager.class).addInline(
										context.getPullRequest(), commentAt, compareWith, lineNo, input.getModelObject());
							} finally {
								InheritableThreadLocalData.clear();
							}
							
	 						Component commentWidget = newCommentWidget(commentWidgets.newChildId(), comment);
							commentWidgets.add(commentWidget);
							
	 						Component wrapper = commentWidget.get(INLINE_COMMENT_ID);
							wrapper.setMarkupId(newCommentForm.getMarkupId());
							newCommentForm.remove();
							target.add(wrapper);
							
							String script = String.format(""
									+ "var $comment = $('#%s').parent();"
									+ "$comment.attr('id', '%s'); "
									+ "gitplex.sourceview.commentResized($comment);", 
									wrapper.getMarkupId(), commentWidget.getMarkupId());
							target.appendJavaScript(script);
						}
	
					});
					
					newCommentForms.add(newCommentForm);
					
					String script = String.format("gitplex.sourceview.placeComment('%s', %d, '%s');", 
							codeContainer.getMarkupId(), lineNo, newCommentForm.getMarkupId());
					target.prependJavaScript(script);
					
					target.add(newCommentForm);
					script = String.format("gitplex.sourceview.commentResized($('#%s').parent());", 
							newCommentForm.getMarkupId());
					target.appendJavaScript(script);
				}
				
			});		
		}
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
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
		
		CharSequence addCommentCallback;
		if (addCommentBehavior != null) 
			addCommentCallback = addCommentBehavior.getCallbackFunction(CallbackParameter.explicit("lineNo"));
		else
			addCommentCallback = "undefined";
		String script = String.format("gitplex.sourceview.init('%s', '%s', '%s', %s, '%s', '%s', %s, %d, %s, %s);", 
				codeContainer.getMarkupId(), 
				StringEscapeUtils.escapeEcmaScript(blob.getText().getContent()),
				context.getBlobIdent().path, 
				context.getMark()!=null?context.getMark().toJSON():"undefined",
				symbolTooltip.getMarkupId(), 
				context.getBlobIdent().revision, 
				blameCommitsJson, 
				context.getComment()!=null?context.getComment().getId():-1,
				addCommentCallback, 
				clientState!=null?"'"+clientState+"'":"undefined");
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	private Component newCommentWidget(String id, Comment comment) {
		final Long commentId = comment.getId();
		final WebMarkupContainer widget = new WebMarkupContainer(id) {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				
				if (event.getPayload() instanceof CommentRemoved) {
					CommentRemoved commentRemoved = (CommentRemoved) event.getPayload();
					commentWidgets.remove(this);
					String script = String.format("document.getElementById('%s').lineWidget.clear();", getMarkupId());
					commentRemoved.getTarget().appendJavaScript(script);
					
					send(SourceViewPanel.this, Broadcast.BUBBLE, commentRemoved);
				} else if (event.getPayload() instanceof CommentResized) {
					CommentResized commentResized = (CommentResized) event.getPayload();
					String script = String.format("gitplex.sourceview.commentResized('%s');", getMarkupId());
					commentResized.getTarget().appendJavaScript(script);
				} 
			}

		};
		
		widget.add(new InlineCommentPanel(INLINE_COMMENT_ID, new LoadableDetachableModel<Comment>() {

			@Override
			protected Comment load() {
				return GitPlex.getInstance(Dao.class).load(Comment.class, commentId);
			}
			
		}));
		
		widget.add(AttributeAppender.append("data-lineNo", comment.getLine()));
		widget.setMarkupId("pullrequest-comment-" + commentId);
		widget.setOutputMarkupId(true);
		
		return widget;
	}
	
	@Override
	protected void onDetach() {
		commentsModel.detach();
		super.onDetach();
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
