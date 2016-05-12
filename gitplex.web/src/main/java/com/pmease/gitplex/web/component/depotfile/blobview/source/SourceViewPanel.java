package com.pmease.gitplex.web.component.depotfile.blobview.source;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
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
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.assets.codemirror.CodeMirrorResourceReference;
import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.commons.wicket.assets.uri.URIResourceReference;
import com.pmease.commons.wicket.component.PreventDefaultAjaxLink;
import com.pmease.commons.wicket.component.menu.MenuItem;
import com.pmease.commons.wicket.component.menu.MenuLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.component.Mark;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.web.component.comment.CodeCommentPanel;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.comment.DepotAttachmentSupport;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewContext.Mode;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewPanel;
import com.pmease.gitplex.web.component.symboltooltip.SymbolTooltipPanel;
import com.pmease.gitplex.web.page.depot.commit.CommitDetailPage;
import com.pmease.gitplex.web.util.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.jqueryui.JQueryUIJavaScriptReference;

/**
 * Make sure to add only one source view panel per page
 * 
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class SourceViewPanel extends BlobViewPanel {

	private static final Logger logger = LoggerFactory.getLogger(SourceViewPanel.class);
	
	private static final String COOKIE_OUTLINE = "sourceView.outline";
	
	private static final String BODY_ID = "body";
	
	private final List<Symbol> symbols = new ArrayList<>();
	
	private final String viewState;
	
	private final IModel<Collection<CodeComment>> commentsModel = 
			new LoadableDetachableModel<Collection<CodeComment>>() {

		@Override
		protected Collection<CodeComment> load() {
			return GitPlex.getInstance(CodeCommentManager.class).query(
					context.getDepot(), context.getCommit().name(), context.getBlobIdent().path);
		}
		
	};

	private WebMarkupContainer commentContainer;
	
	private WebMarkupContainer outlineContainer;
	
	private SymbolTooltipPanel symbolTooltip;
	
	private AbstractDefaultAjaxBehavior commentBehavior;
	
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
					return outlineContainer.isVisible()?"fa fa-check":null;
				}

				@Override
				public AbstractLink newLink(String id) {
					return new AjaxLink<Void>(id) {

						@Override
						public void onClick(AjaxRequestTarget target) {
							menuLink.close();
							toggleOutline(target);
						}
						
					};
				}
				
			});
		} 
		return menuItems;
	}
	
	private void toggleOutline(AjaxRequestTarget target) {
		WebResponse response = (WebResponse) RequestCycle.get().getResponse();
		Cookie cookie;
		if (outlineContainer.isVisible()) {
			cookie = new Cookie(COOKIE_OUTLINE, "no");
			outlineContainer.setVisible(false);
		} else {
			cookie = new Cookie(COOKIE_OUTLINE, "yes");
			outlineContainer.setVisible(true);
		}
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);
		target.add(outlineContainer);
		target.appendJavaScript("gitplex.sourceview.onToggleOutline();");
	}

	public void mark(AjaxRequestTarget target, Mark mark, boolean scroll) {
		String script = String.format("gitplex.sourceview.mark(%s, %s);", 
				mark.toJSON(), scroll);
		target.appendJavaScript(script);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		commentContainer = new WebMarkupContainer("comment") {
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript("gitplex.sourceview.initComment();"));
			}
			
		};
		commentContainer.add(new AjaxLink<Void>("locate") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				mark(target, context.getComment().getMark(), true);
				context.onMark(target, context.getComment().getMark());
			}
			
		});
		commentContainer.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(commentContainer));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				hideComment(target);
				context.onShowComment(target, null);
			}
			
		});
		commentContainer.setOutputMarkupPlaceholderTag(true);
		if (context.getComment() != null) {
			IModel<CodeComment> commentModel = new LoadableDetachableModel<CodeComment>() {

				@Override
				protected CodeComment load() {
					return context.getComment();
				}
				
			};
			CodeCommentPanel commentPanel = new CodeCommentPanel(BODY_ID, commentModel) {

				@Override
				protected void onCommentDeleted(AjaxRequestTarget target) {
					CodeComment comment = commentModel.getObject();
					SourceViewPanel.this.onCommentDeleted(target, comment);
				}
				
			};
			commentContainer.add(commentPanel);
		} else {
			commentContainer.add(new WebMarkupContainer(BODY_ID));
			commentContainer.setVisible(false);
		}
		add(commentContainer);
		
		add(commentBehavior = new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				
				switch(params.getParameterValue("action").toString()) {
				case "add": 
					Preconditions.checkNotNull(SecurityUtils.getAccount());
					
					// must use commit from source view page in case we are commenting on 
					// a branch and that branch has new commits since the file is being 
					// displayed
					String commitHash = params.getParameterValue("param1").toString();
					
					int fromLine = params.getParameterValue("param2").toInt();
					int fromCh = params.getParameterValue("param3").toInt();
					int toLine = params.getParameterValue("param4").toInt();
					int toCh = params.getParameterValue("param5").toInt();
					
					Fragment fragment = new Fragment(BODY_ID, "newCommentFrag", SourceViewPanel.this);
					fragment.setOutputMarkupId(true);
					
					Form<?> form = new Form<Void>("form");
					
					CommentInput input;
					form.add(input = new CommentInput("input", Model.of("")) {

						@Override
						protected DepotAttachmentSupport getAttachmentSupport() {
							return new DepotAttachmentSupport(context.getDepot());
						}
						
					});
					input.setRequired(true);
					
					NotificationPanel feedback = new NotificationPanel("feedback", input); 
					feedback.setOutputMarkupPlaceholderTag(true);
					form.add(feedback);
					
					form.add(new AjaxLink<Void>("cancel") {

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(form));
						}
						
						@Override
						public void onClick(AjaxRequestTarget target) {
							hideComment(target);
						}
						
					});
					
					form.add(new AjaxButton("save") {

						@Override
						protected void onError(AjaxRequestTarget target, Form<?> form) {
							super.onError(target, form);
							target.add(feedback);
						}

						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							super.onSubmit(target, form);
							
							CodeComment comment = new CodeComment();
							comment.setCommit(commitHash);
							comment.setPath(context.getBlobIdent().path);
							comment.setCompareCommit(commitHash);
							comment.setContent(input.getModelObject());
							comment.setDepot(context.getDepot());
							comment.setUser(SecurityUtils.getAccount());
							Mark mark = new Mark();
							mark.beginLine = fromLine;
							mark.beginChar = fromCh;
							mark.endLine = toLine;
							mark.endChar = toCh;
							comment.setMark(mark);
							GitPlex.getInstance(CodeCommentManager.class).persist(comment);
							
							Long commentId = comment.getId();
							IModel<CodeComment> commentModel = new LoadableDetachableModel<CodeComment>() {

								@Override
								protected CodeComment load() {
									return GitPlex.getInstance(CodeCommentManager.class).load(commentId);
								}
								
							};
							CodeCommentPanel commentPanel = new CodeCommentPanel(fragment.getId(), commentModel) {

								@Override
								protected void onCommentDeleted(AjaxRequestTarget target) {
									CodeComment comment = commentModel.getObject();
									SourceViewPanel.this.onCommentDeleted(target, comment);
								}
								
							};
							fragment.replaceWith(commentPanel);
							target.add(commentPanel);

							String script = String.format("gitplex.sourceview.onCommentAdded(%d, %s);", 
									comment.getMark().getBeginLine(), 
									getJsonOfComment(comment));
							target.appendJavaScript(script);
							context.onShowComment(target, comment);
						}

					});
					fragment.add(form);
					commentContainer.replace(fragment);
					commentContainer.setVisible(true);
					target.add(commentContainer);
					target.appendJavaScript("gitplex.sourceview.onAddingComment();");
					break;
				case "show":
					Long commentId = params.getParameterValue("param1").toLong();
					IModel<CodeComment> commentModel = new LoadableDetachableModel<CodeComment>() {

						@Override
						protected CodeComment load() {
							return GitPlex.getInstance(CodeCommentManager.class).load(commentId);
						}
						
					};
					CodeCommentPanel commentPanel = new CodeCommentPanel(BODY_ID, commentModel) {

						@Override
						protected void onCommentDeleted(AjaxRequestTarget target) {
							CodeComment comment = commentModel.getObject();
							SourceViewPanel.this.onCommentDeleted(target, comment);
						}
						
					};
					commentContainer.replace(commentPanel);
					commentContainer.setVisible(true);
					target.add(commentContainer);
					String script = String.format("gitplex.sourceview.onShowComment(%s);", 
							getJsonOfComment(commentModel.getObject()));
					target.appendJavaScript(script);
					context.onShowComment(target, commentModel.getObject());
					break;
				}
			}
			
		});
		
		outlineContainer = new WebMarkupContainer("outline") {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript("gitplex.sourceview.initOutline();"));
			}
			
		};
		outlineContainer.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				toggleOutline(target);
			}
			
		});
		NestedTree<Symbol> tree;
		outlineContainer.add(tree = new NestedTree<Symbol>(BODY_ID, new ITreeProvider<Symbol>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends Symbol> getRoots() {
				return getChildSymbols(null).iterator();
			}

			@Override
			public boolean hasChildren(Symbol symbol) {
				return !getChildSymbols(symbol).isEmpty();
			}

			@Override
			public Iterator<? extends Symbol> getChildren(Symbol symbol) {
				return getChildSymbols(symbol).iterator();
			}

			@Override
			public IModel<Symbol> model(Symbol symbol) {
				return Model.of(symbol);
			}
			
		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new HumanTheme());				
			}

			@Override
			protected Component newContentComponent(String id, IModel<Symbol> nodeModel) {
				Fragment fragment = new Fragment(id, "outlineNodeFrag", SourceViewPanel.this);
				Symbol symbol = nodeModel.getObject();
				
				fragment.add(new Image("icon", symbol.getIcon()));
				
				AjaxLink<Void> link = new PreventDefaultAjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						context.onSelect(target, context.getBlobIdent(), symbol.getPos());
					}
					
				};
				link.add(symbol.render("label", null));
				fragment.add(link);
				
				return fragment;
			}
			
		});		
		
		for (Symbol root: getChildSymbols(null))
			tree.expand(root);
		
		outlineContainer.setOutputMarkupPlaceholderTag(true);
		add(outlineContainer);
		
		if (!symbols.isEmpty()) {
			WebRequest request = (WebRequest) RequestCycle.get().getRequest();
			Cookie cookie = request.getCookie(COOKIE_OUTLINE);
			if (cookie!=null && cookie.getValue().equals("no"))
				outlineContainer.setVisible(false);
		} else {
			outlineContainer.setVisible(false);
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
	}
	
	private String getJsonOfComment(CodeComment comment) {
		CommentInfo commentInfo = new CommentInfo();
		commentInfo.id = comment.getId();
		commentInfo.mark = comment.getMark();

		String jsonOfCommentInfo;
		try {
			jsonOfCommentInfo = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(commentInfo);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return jsonOfCommentInfo;
	}
	
	private void onCommentDeleted(AjaxRequestTarget target, CodeComment comment) {
		commentContainer.replace(new WebMarkupContainer(BODY_ID));
		commentContainer.setVisible(false);
		target.add(commentContainer);
		String script = String.format("gitplex.sourceview.onCommentDeleted(%d, %d);", 
				comment.getMark().getBeginLine(), comment.getId());
		target.appendJavaScript(script);
		context.onShowComment(target, null);
	}
	
	private void hideComment(AjaxRequestTarget target) {
		commentContainer.replace(new WebMarkupContainer(BODY_ID));
		commentContainer.setVisible(false);
		target.add(commentContainer);
		target.appendJavaScript("gitplex.sourceview.onShowComment();");
	}
	
	private List<Symbol> getChildSymbols(@Nullable Symbol parentSymbol) {
		List<Symbol> children = new ArrayList<>();
		for (Symbol symbol: symbols) {
			if (symbol.getParent() == parentSymbol)
				children.add(symbol);
		}
		return children;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(URIResourceReference.INSTANCE));
		response.render(JQueryUIJavaScriptReference.asHeaderItem());
		
		response.render(JavaScriptHeaderItem.forReference(CookiesResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(CodeMirrorResourceReference.INSTANCE));
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(SourceViewPanel.class, "source-view.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(SourceViewPanel.class, "source-view.css")));
		
		Blob blob = context.getDepot().getBlob(context.getBlobIdent());
		
		String jsonOfBlameInfos;
		if (context.getMode() == Mode.BLAME) {
			List<BlameInfo> blameInfos = new ArrayList<>();
			
			String commitHash = context.getCommit().name();
			
			for (Blame blame: context.getDepot().git().blame(commitHash, context.getBlobIdent().path).values()) {
				BlameInfo blameInfo = new BlameInfo();
				blameInfo.commitDate = DateUtils.formatDate(blame.getCommit().getCommitter().getWhen());
				blameInfo.authorName = HtmlEscape.escapeHtml5(blame.getCommit().getAuthor().getName());
				blameInfo.hash = GitUtils.abbreviateSHA(blame.getCommit().getHash(), 7);
				blameInfo.message = blame.getCommit().getSubject();
				CommitDetailPage.HistoryState state = new CommitDetailPage.HistoryState();
				state.pathFilter = context.getBlobIdent().path;
				PageParameters params = CommitDetailPage.paramsOf(context.getDepot(), 
						blame.getCommit().getHash(), state);
				blameInfo.url = RequestCycle.get().urlFor(CommitDetailPage.class, params).toString();
				blameInfo.ranges = blame.getRanges();
				blameInfos.add(blameInfo);
			}
			try {
				jsonOfBlameInfos = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(blameInfos);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		} else {
			jsonOfBlameInfos = "undefined";
		}
		
		Map<Integer, List<CommentInfo>> commentInfos = new HashMap<>(); 
		for (CodeComment comment: commentsModel.getObject()) {
			if (comment.getMark() != null) {
				int line = comment.getMark().getBeginLine();
				List<CommentInfo> commentInfosAtLine = commentInfos.get(line);
				if (commentInfosAtLine == null) {
					commentInfosAtLine = new ArrayList<>();
					commentInfos.put(line, commentInfosAtLine);
				}
				CommentInfo commentInfo = new CommentInfo();
				commentInfo.id = comment.getId();
				commentInfo.mark = comment.getMark();
				commentInfosAtLine.add(commentInfo);
			}
		}
		for (List<CommentInfo> value: commentInfos.values()) {
			value.sort((o1, o2)->(int)(o1.id-o2.id));
		}
		
		String jsonOfCommentInfos;
		try {
			jsonOfCommentInfos = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(commentInfos);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		CharSequence commentCallback = commentBehavior.getCallbackFunction(
				explicit("action"), explicit("param1"), explicit("param2"), 
				explicit("param3"), explicit("param4"), explicit("param5"));
		String script = String.format("gitplex.sourceview.init('%s', '%s', %s, '%s', '%s', "
				+ "%s, %s, %s, %s, %s);", 
				JavaScriptEscape.escapeJavaScript(blob.getText().getContent()),
				JavaScriptEscape.escapeJavaScript(context.getBlobIdent().path), 
				context.getMark()!=null?context.getMark().toJSON():"undefined",
				symbolTooltip.getMarkupId(), 
				context.getCommit().name(), 
				jsonOfBlameInfos, 
				jsonOfCommentInfos,
				commentCallback, 
				viewState!=null?"JSON.parse('"+viewState+"')":"undefined", 
				SecurityUtils.getAccount()!=null);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected void onDetach() {
		commentsModel.detach();
		super.onDetach();
	}

	@SuppressWarnings("unused")
	private static class BlameInfo {
		
		String hash;
		
		String message;
		
		String url;
		
		String authorName;
		
		String commitDate;
		
		List<Range> ranges;
	}
	
	@SuppressWarnings("unused")
	private static class CommentInfo {
		long id;
		
		Mark mark;
	}
	
}
