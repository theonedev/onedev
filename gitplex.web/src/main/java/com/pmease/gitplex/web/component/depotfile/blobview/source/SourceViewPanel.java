package com.pmease.gitplex.web.component.depotfile.blobview.source;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
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
import com.pmease.commons.wicket.assets.hover.HoverResourceReference;
import com.pmease.commons.wicket.assets.jqueryui.JQueryUIResourceReference;
import com.pmease.commons.wicket.assets.selectionpopover.SelectionPopoverResourceReference;
import com.pmease.commons.wicket.behavior.ViewStateAwareBehavior;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.PreventDefaultAjaxLink;
import com.pmease.commons.wicket.component.menu.MenuItem;
import com.pmease.commons.wicket.component.menu.MenuLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.component.CommentPos;
import com.pmease.gitplex.core.entity.component.CompareContext;
import com.pmease.gitplex.core.entity.component.DepotAndRevision;
import com.pmease.gitplex.core.entity.component.TextRange;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.web.component.comment.CodeCommentPanel;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.comment.DepotAttachmentSupport;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewContext.Mode;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewPanel;
import com.pmease.gitplex.web.component.revisionpicker.RevisionSelector;
import com.pmease.gitplex.web.component.symboltooltip.SymbolTooltipPanel;
import com.pmease.gitplex.web.page.depot.commit.CommitDetailPage;
import com.pmease.gitplex.web.page.depot.compare.RevisionComparePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.util.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

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
	
	private final IModel<Collection<CodeComment>> commentsModel = 
			new LoadableDetachableModel<Collection<CodeComment>>() {

		@Override
		protected Collection<CodeComment> load() {
			return GitPlex.getInstance(CodeCommentManager.class).query(
					context.getDepot(), context.getCommit(), context.getBlobIdent().path);
		}
		
	};

	private WebMarkupContainer commentContainer;
	
	private WebMarkupContainer outlineContainer;
	
	private SymbolTooltipPanel symbolTooltip;
	
	private AbstractDefaultAjaxBehavior ajaxBehavior;
	
	public SourceViewPanel(String id, BlobViewContext context) {
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
		
	}
	
	@Override
	public List<MenuItem> getMenuItems(MenuLink menuLink) {
		List<MenuItem> menuItems = new ArrayList<>();
		menuItems.add(new MenuItem() {
			
			@Override
			public String getIconClass() {
				return context.getMode() == Mode.BLAME?"fa fa-check":null;
			}

			@Override
			public String getLabel() {
				return "Blame";
			}

			@Override
			public AbstractLink newLink(String id) {
				AbstractLink link = new AjaxLink<Void>(id) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						menuLink.close();
						boolean blamed = (context.getMode() != Mode.BLAME);
						String jsonOfBlameInfos = getJsonOfBlameInfos(blamed);
						String script = String.format("gitplex.sourceview.onBlame(%s);", jsonOfBlameInfos);
						target.appendJavaScript(script);
						context.onBlameChange(target, blamed);									
					}
					
				};
				link.add(new ViewStateAwareBehavior());
				return link;
			}
			
		});
		
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

	public void mark(AjaxRequestTarget target, TextRange mark, boolean scroll) {
		String script = String.format("gitplex.sourceview.mark(%s, %s);", 
				getJson(mark), scroll);
		target.appendJavaScript(script);
	}
	
	private String getJson(TextRange mark) {
		try {
			return GitPlex.getInstance(ObjectMapper.class).writeValueAsString(mark);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		commentContainer = new WebMarkupContainer("comment", Model.of((TextRange)null)) {
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript("gitplex.sourceview.initComment();"));
			}
			
		};
		WebMarkupContainer head = new WebMarkupContainer("head");
		head.setOutputMarkupId(true);
		commentContainer.add(head);
		
		head.add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				CodeComment comment = context.getOpenComment();
				return comment!=null?comment.getTitle():"";
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(context.getOpenComment() != null);
			}
			
		});
		head.add(new DropdownLink("context") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(context.getOpenComment() != null);
			}

			@Override
			protected Component newContent(String id) {
				return new RevisionSelector(id, new AbstractReadOnlyModel<Depot>() {

					@Override
					public Depot getObject() {
						return context.getDepot();
					}
					
				}) {
					
					@Override
					protected void onSelect(AjaxRequestTarget target, String revision) {
						RevisionComparePage.State state = new RevisionComparePage.State();
						CodeComment comment = context.getOpenComment();
						state.commentId = comment.getId();
						state.mark = comment.getCommentPos();
						state.compareWithMergeBase = false;
						state.leftSide = new DepotAndRevision(context.getDepot(), comment.getCommentPos().getCommit());
						state.rightSide = new DepotAndRevision(context.getDepot(), revision);
						state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
						PageParameters params = RevisionComparePage.paramsOf(context.getDepot(), state);
						setResponsePage(RevisionComparePage.class, params);
					}
					
				};
			}
			
		});
		head.add(new AjaxLink<Void>("locate") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				CodeComment comment = context.getOpenComment();
				TextRange mark;
				if (comment != null) {
					mark = comment.getCommentPos().getRange();
				} else {
					mark = (TextRange) commentContainer.getDefaultModelObject();
				}
				mark(target, mark, true);
				context.onMark(target, mark);
				target.appendJavaScript(String.format("$('#%s').blur();", getMarkupId()));
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				setOutputMarkupId(true);
			}
			
		});
		
		// use this instead of bookmarkable link as we want to get the link 
		// updated whenever we re-render the comment container
		AttributeAppender appender = AttributeAppender.append("href", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (context.getOpenComment() != null) {
					DepotFilePage.State state = new DepotFilePage.State();
					state.blobIdent = new BlobIdent(context.getBlobIdent());
					state.blobIdent.revision = context.getCommit().name();
					state.commentId = context.getOpenComment().getId();
					state.mark = context.getOpenComment().getCommentPos().getRange();
					return urlFor(DepotFilePage.class, DepotFilePage.paramsOf(context.getDepot(), state)).toString();
				} else {
					return "";
				}
			}
			
		});
		head.add(new WebMarkupContainer("permanent") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(context.getOpenComment() != null);
			}
			
		}.add(appender));
		
		head.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(commentContainer));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				clearComment(target);
				if (context.getOpenComment() != null) 
					context.onCommentOpened(target, null);
				target.appendJavaScript("gitplex.sourceview.onCloseComment();");
			}
			
		});
		
		head.add(new AjaxLink<Void>("toggleResolve") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(commentContainer));
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(context.getOpenComment() != null);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				CodeComment comment = context.getOpenComment();
				if (comment != null) {
					if (SecurityUtils.canModify(comment)) {
						if (comment.isResolved()) {
							tag.put("title", "Comment is currently resolved, click to unresolve");
							tag.put("class", "pull-right resolve resolved");
						} else {
							tag.put("title", "Comment is currently unresolved, click to resolve");
							tag.put("class", "pull-right resolve unresolved");
						}
					} else {
						if (comment.isResolved()) {
							tag.put("title", "Comment is currently resolved, contact comment owner or repository manager to change status of the comment");
							tag.put("class", "pull-right resolve resolved");
						} else {
							tag.put("title", "Comment is currently unresolved, contact comment owner or repository manager to change status of the comment");
							tag.put("class", "pull-right resolve unresolved");
						}
					}
				} 
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (SecurityUtils.canModify(context.getOpenComment())) {
					((CodeCommentPanel)commentContainer.get("body")).onToggleResolve(target);
					target.appendJavaScript("gitplex.sourceview.scrollToCommentBottom();");
				} else {
					Session.get().warn("Only repository manager and comment creator can change status");
				}
			}
			
		}.setOutputMarkupId(true));
		
		commentContainer.setOutputMarkupPlaceholderTag(true);
		if (context.getOpenComment() != null) {
			IModel<CodeComment> commentModel = new LoadableDetachableModel<CodeComment>() {

				@Override
				protected CodeComment load() {
					return context.getOpenComment();
				}
				
			};
			CodeCommentPanel commentPanel = new CodeCommentPanel(BODY_ID, commentModel) {

				@Override
				protected void onCommentDeleted(AjaxRequestTarget target) {
					CodeComment comment = commentModel.getObject();
					SourceViewPanel.this.onCommentDeleted(target, comment);
				}

				@Override
				protected CompareContext getCompareContext() {
					return SourceViewPanel.this.getCompareContext();
				}

				@Override
				protected void onSaveComment(AjaxRequestTarget target) {
					target.add(commentContainer.get("head"));
				}

				@Override
				protected PullRequest getPullRequest() {
					return null;
				}
				
			};
			commentContainer.add(commentPanel);
		} else {
			commentContainer.add(new WebMarkupContainer(BODY_ID));
			commentContainer.setVisible(false);
		}
		add(commentContainer);
		
		add(ajaxBehavior = new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				
				switch(params.getParameterValue("action").toString()) {
				case "showBlameMessage":
					String tooltipId = params.getParameterValue("param1").toString();
					String commitHash = params.getParameterValue("param2").toString();
					String message = context.getDepot().getRevCommit(commitHash).getFullMessage();
					String escapedMessage = JavaScriptEscape.escapeJavaScript(message);
					String script = String.format("gitplex.sourceview.showBlameMessage('%s', '%s');", tooltipId, escapedMessage); 
					target.appendJavaScript(script);
					break;
				case "openSelectionPopover": 
					TextRange mark = getMark(params, "param1", "param2", "param3", "param4");
					script = String.format("gitplex.sourceview.openSelectionPopover(%s, '%s', %s);", 
							getJson(mark), context.getMarkUrl(mark), 
							SecurityUtils.getAccount()!=null);
					target.appendJavaScript(script);
					break;
				case "addComment": 
					Preconditions.checkNotNull(SecurityUtils.getAccount());
					
					mark = getMark(params, "param1", "param2", "param3", "param4");
					commentContainer.setDefaultModelObject(mark);
					
					Fragment fragment = new Fragment(BODY_ID, "newCommentFrag", SourceViewPanel.this);
					fragment.setOutputMarkupId(true);
					
					Form<?> form = new Form<Void>("form");
					
					String uuid = UUID.randomUUID().toString();
					
					TextField<String> titleInput = new TextField<String>("title", Model.of(""));
					titleInput.setRequired(true);
					form.add(titleInput);
					
					CommentInput contentInput;
					form.add(contentInput = new CommentInput("content", Model.of("")) {

						@Override
						protected DepotAttachmentSupport getAttachmentSupport() {
							return new DepotAttachmentSupport(context.getDepot(), uuid);
						}

						@Override
						protected Depot getDepot() {
							return context.getDepot();
						}
						
					});
					contentInput.setRequired(true);
					
					NotificationPanel feedback = new NotificationPanel("feedback", form); 
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
							clearComment(target);
							target.appendJavaScript("gitplex.sourceview.onLayoutChange();");
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
							comment.setUUID(uuid);
							comment.setCommentPos(new CommentPos());
							comment.getCommentPos().setCommit(context.getCommit().name());
							comment.getCommentPos().setPath(context.getBlobIdent().path);
							comment.setTitle(titleInput.getModelObject());
							comment.setContent(contentInput.getModelObject());
							comment.setDepot(context.getDepot());
							comment.setUser(SecurityUtils.getAccount());
							comment.getCommentPos().setRange(mark);
							comment.setCompareContext(getCompareContext());
							
							GitPlex.getInstance(CodeCommentManager.class).save(comment);
							
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

								@Override
								protected CompareContext getCompareContext() {
									return SourceViewPanel.this.getCompareContext();
								}

								@Override
								protected void onSaveComment(AjaxRequestTarget target) {
									target.add(commentContainer.get("head"));
								}

								@Override
								protected PullRequest getPullRequest() {
									return null;
								}
								
							};
							commentContainer.replace(commentPanel);
							target.add(commentContainer);

							String script = String.format("gitplex.sourceview.onCommentAdded(%s);", 
									getJsonOfComment(comment));
							target.appendJavaScript(script);
							context.onCommentOpened(target, comment);
						}

					});
					fragment.add(form);
					commentContainer.replace(fragment);
					commentContainer.setVisible(true);
					target.add(commentContainer);
					context.onAddComment(target, mark);
					target.appendJavaScript(String.format("gitplex.sourceview.onAddComment(%s);", getJson(mark)));
					break;
				case "openComment":
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

						@Override
						protected CompareContext getCompareContext() {
							return SourceViewPanel.this.getCompareContext();
						}

						@Override
						protected void onSaveComment(AjaxRequestTarget target) {
							target.add(commentContainer.get("head"));
						}

						@Override
						protected PullRequest getPullRequest() {
							return null;
						}
						
					};
					commentContainer.replace(commentPanel);
					commentContainer.setVisible(true);
					target.add(commentContainer);
					script = String.format("gitplex.sourceview.onOpenComment(%s);", 
							getJsonOfComment(commentModel.getObject()));
					target.appendJavaScript(script);
					context.onCommentOpened(target, commentModel.getObject());
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
				DepotFilePage.State state = new DepotFilePage.State();
				state.blobIdent = context.getBlobIdent();
				state.commentId = CodeComment.idOf(context.getOpenComment());
				state.mark = TextRange.of(symbol.getPos());
				PageParameters params = DepotFilePage.paramsOf(context.getDepot(), state);
				link.add(AttributeAppender.replace("href", urlFor(DepotFilePage.class, params).toString()));
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
	
	private TextRange getMark(IRequestParameters params, String beginLineParam, String beginCharParam, 
			String endLineParam, String endCharParam) {
		int beginLine = params.getParameterValue(beginLineParam).toInt();
		int beginChar = params.getParameterValue(beginCharParam).toInt();
		int endLine = params.getParameterValue(endLineParam).toInt();
		int endChar = params.getParameterValue(endCharParam).toInt();
		TextRange mark = new TextRange();
		mark.beginLine = beginLine;
		mark.beginChar = beginChar;
		mark.endLine = endLine;
		mark.endChar = endChar;
		return mark;
	}

	private String getJsonOfComment(CodeComment comment) {
		CommentInfo commentInfo = new CommentInfo();
		commentInfo.id = comment.getId();
		commentInfo.mark = comment.getCommentPos().getRange();
		commentInfo.title = comment.getTitle();

		String jsonOfCommentInfo;
		try {
			jsonOfCommentInfo = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(commentInfo);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return jsonOfCommentInfo;
	}

	private void clearComment(AjaxRequestTarget target) {
		commentContainer.replace(new WebMarkupContainer(BODY_ID));
		commentContainer.setVisible(false);
		target.add(commentContainer);
	}
	
	private void onCommentDeleted(AjaxRequestTarget target, CodeComment comment) {
		clearComment(target);
		String script = String.format("gitplex.sourceview.onCommentDeleted(%s);", 
				getJsonOfComment(comment));
		target.appendJavaScript(script);
		context.onCommentOpened(target, null);
	}
	
	private List<Symbol> getChildSymbols(@Nullable Symbol parentSymbol) {
		List<Symbol> children = new ArrayList<>();
		for (Symbol symbol: symbols) {
			if (symbol.getParent() == parentSymbol)
				children.add(symbol);
		}
		return children;
	}
	
	private String getJsonOfBlameInfos(boolean blamed) {
		String jsonOfBlameInfos;
		if (blamed) {
			List<BlameInfo> blameInfos = new ArrayList<>();
			
			String commitHash = context.getCommit().name();
			
			for (Blame blame: context.getDepot().git().blame(commitHash, context.getBlobIdent().path).values()) {
				BlameInfo blameInfo = new BlameInfo();
				blameInfo.commitDate = DateUtils.formatDate(blame.getCommit().getCommitter().getWhen());
				blameInfo.authorName = HtmlEscape.escapeHtml5(blame.getCommit().getAuthor().getName());
				blameInfo.hash = blame.getCommit().getHash();
				blameInfo.abbreviatedHash = GitUtils.abbreviateSHA(blame.getCommit().getHash(), 7);
				CommitDetailPage.State state = new CommitDetailPage.State();
				state.revision = blame.getCommit().getHash();
				state.pathFilter = context.getBlobIdent().path;
				PageParameters params = CommitDetailPage.paramsOf(context.getDepot(), state);
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
		return jsonOfBlameInfos;
	}
	
	private CompareContext getCompareContext() {
		CompareContext compareContext = new CompareContext();
		compareContext.setCompareCommit(context.getCommit().name());
		compareContext.setPathFilter(context.getBlobIdent().path);
		return compareContext;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(JQueryUIResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(SelectionPopoverResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(HoverResourceReference.INSTANCE));
		
		response.render(JavaScriptHeaderItem.forReference(CookiesResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(CodeMirrorResourceReference.INSTANCE));
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(SourceViewPanel.class, "source-view.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(SourceViewPanel.class, "source-view.css")));
		
		Blob blob = context.getDepot().getBlob(context.getBlobIdent());
		
		String jsonOfBlameInfos = getJsonOfBlameInfos(context.getMode() == Mode.BLAME);
		Map<Integer, List<CommentInfo>> commentInfos = new HashMap<>(); 
		for (CodeComment comment: commentsModel.getObject()) {
			if (comment.getCommentPos().getRange() != null) {
				int line = comment.getCommentPos().getRange().getBeginLine();
				List<CommentInfo> commentInfosAtLine = commentInfos.get(line);
				if (commentInfosAtLine == null) {
					commentInfosAtLine = new ArrayList<>();
					commentInfos.put(line, commentInfosAtLine);
				}
				CommentInfo commentInfo = new CommentInfo();
				commentInfo.id = comment.getId();
				commentInfo.mark = comment.getCommentPos().getRange();
				commentInfo.title = comment.getTitle();
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
		CharSequence callback = ajaxBehavior.getCallbackFunction(
				explicit("action"), explicit("param1"), explicit("param2"), 
				explicit("param3"), explicit("param4"));
		String viewState = RequestCycle.get().getMetaData(DepotFilePage.VIEW_STATE_KEY);
		String script = String.format("gitplex.sourceview.init('%s', '%s', %s, %s, '%s', '%s', "
				+ "%s, %s, %s, %s, %s);", 
				JavaScriptEscape.escapeJavaScript(blob.getText().getContent()),
				JavaScriptEscape.escapeJavaScript(context.getBlobIdent().path),
				context.getOpenComment()!=null?getJsonOfComment(context.getOpenComment()):"undefined",
				context.getMark()!=null?getJson(context.getMark()):"undefined",
				symbolTooltip.getMarkupId(), 
				context.getBlobIdent().revision, 
				jsonOfBlameInfos, 
				jsonOfCommentInfos,
				callback, 
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
		
		String abbreviatedHash;
		
		String hash;
		
		String url;
		
		String authorName;
		
		String commitDate;
		
		List<Range> ranges;
	}
	
	@SuppressWarnings("unused")
	private static class CommentInfo {
		long id;
		
		String title;
		
		TextRange mark;
	}
	
}
