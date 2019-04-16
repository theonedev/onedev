package io.onedev.server.web.page.project.blob.render.renderers.source;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
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
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;
import org.unbescape.javascript.JavaScriptEscape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.jsymbol.SymbolExtractor;
import io.onedev.commons.jsymbol.SymbolExtractorRegistry;
import io.onedev.commons.jsymbol.TokenPosition;
import io.onedev.commons.utils.Range;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.matchscore.MatchScoreProvider;
import io.onedev.commons.utils.matchscore.MatchScoreUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.git.BlameBlock;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.BlameCommand;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.MarkPos;
import io.onedev.server.model.support.ProjectAndRevision;
import io.onedev.server.model.support.TextRange;
import io.onedev.server.search.code.SearchManager;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.blamemessage.BlameMessageBehavior;
import io.onedev.server.web.component.codecomment.CodeCommentPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.project.comment.CommentInput;
import io.onedev.server.web.component.revisionpicker.RevisionSelector;
import io.onedev.server.web.component.sourceformat.OptionChangeCallback;
import io.onedev.server.web.component.sourceformat.SourceFormatPanel;
import io.onedev.server.web.component.symboltooltip.SymbolTooltipPanel;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.blob.render.view.BlobViewPanel;
import io.onedev.server.web.page.project.blob.render.view.Markable;
import io.onedev.server.web.page.project.blob.search.SearchMenuContributor;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.util.ProjectAttachmentSupport;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.websocket.PageDataChanged;

/**
 * Make sure to add only one source view panel per page
 * 
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class SourceViewPanel extends BlobViewPanel implements Markable, SearchMenuContributor {

	private static final Logger logger = LoggerFactory.getLogger(SourceViewPanel.class);
	
	private static final String COOKIE_OUTLINE = "sourceView.outline";
	
	private static final String BODY_ID = "body";
	
	private final List<Symbol> symbols = new ArrayList<>();
	
	private final IModel<Map<CodeComment, TextRange>> commentsModel = 
			new LoadableDetachableModel<Map<CodeComment, TextRange>>() {

		@Override
		protected Map<CodeComment, TextRange> load() {
			return OneDev.getInstance(CodeCommentManager.class).findHistory(
					context.getProject(), context.getCommit(), context.getBlobIdent().path);
		}
		
	};

	private WebMarkupContainer commentContainer;
	
	private WebMarkupContainer outline;
	
	private SourceFormatPanel sourceFormat;
	
	private SymbolTooltipPanel symbolTooltip;
	
	private AbstractPostAjaxBehavior ajaxBehavior;
	
	private BlameMessageBehavior blameMessageBehavior;
	
	private final boolean viewPlainMode;
	
	public SourceViewPanel(String id, BlobRenderContext context, boolean viewPlainMode) {
		super(id, context);
		
		this.viewPlainMode = viewPlainMode;
		
		Blob blob = context.getProject().getBlob(context.getBlobIdent());
		
		String blobName = context.getBlobIdent().getName();
		SymbolExtractor<Symbol> extractor = SymbolExtractorRegistry.getExtractor(blobName);
		if (extractor != null) {
			SearchManager searchManager = OneDev.getInstance(SearchManager.class);
			List<Symbol> cachedSymbols = searchManager.getSymbols(context.getProject(), blob.getBlobId(), 
					blob.getIdent().path);
			if (cachedSymbols != null) {
				symbols.addAll(cachedSymbols);
			} else {
				try {
					symbols.addAll(extractor.extract(null, StringUtils.removeBOM(blob.getText().getContent())));
				} catch (Exception e) {
					logger.trace("Can not extract symbols from blob: " + context.getBlobIdent(), e);
				}
			}
		}
		
	}
	
	@Override
	protected WebMarkupContainer newOptions(String id) {
		sourceFormat = new SourceFormatPanel(id, null, new OptionChangeCallback() {
			
			@Override
			public void onOptioneChange(AjaxRequestTarget target) {
				String script = String.format("onedev.server.sourceView.onTabSizeChange(%s);", sourceFormat.getTabSize());
				target.appendJavaScript(script);
			}
			
		}, new OptionChangeCallback() {

			@Override
			public void onOptioneChange(AjaxRequestTarget target) {
				String script = String.format("onedev.server.sourceView.onLineWrapModeChange('%s');", sourceFormat.getLineWrapMode());
				target.appendJavaScript(script);
			}
			
		});
		return sourceFormat;
	}

	@Override
	public WebMarkupContainer newAdditionalActions(String id) {
		WebMarkupContainer actions = new Fragment(id, "actionsFrag", this);
		if (hasOutline()) {
			actions.add(new CheckBox("outline", Model.of(isOutlineVisibleInitially())).add(new OnChangeAjaxBehavior() {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.setMethod(Method.POST);
				}

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					toggleOutline(target);
				}
				
			}));
		} else {
			actions.add(new WebMarkupContainer("outline").setVisible(false));
		}
		return actions;
	}
	
	private void toggleOutline(AjaxRequestTarget target) {
		WebResponse response = (WebResponse) RequestCycle.get().getResponse();
		Cookie cookie;
		if (outline.isVisible()) {
			cookie = new Cookie(COOKIE_OUTLINE, "no");
			outline.setVisible(false);
		} else {
			cookie = new Cookie(COOKIE_OUTLINE, "yes");
			outline.setVisible(true);
		}
		cookie.setPath("/");
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);
		target.add(outline);
		target.appendJavaScript("onedev.server.sourceView.onToggleOutline();");
	}

	private String getJson(TextRange mark) {
		try {
			return OneDev.getInstance(ObjectMapper.class).writeValueAsString(mark);
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
				response.render(OnDomReadyHeaderItem.forScript("onedev.server.sourceView.initComment();"));
			}

		};
		
		WebMarkupContainer head = new WebMarkupContainer("head") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (commentContainer.isVisible() && event.getPayload() instanceof PageDataChanged) {
					PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
					pageDataChanged.getHandler().add(this);
				}
			}
			
		};
		head.setOutputMarkupId(true);
		commentContainer.add(head);
		
		head.add(new DropdownLink("context") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(context.getPullRequest() == null && context.getOpenComment() != null);
			}

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new RevisionSelector(id, new AbstractReadOnlyModel<Project>() {

					@Override
					public Project getObject() {
						return context.getProject();
					}
					
				}) {
					
					@Override
					protected void onSelect(AjaxRequestTarget target, String revision) {
						RevisionComparePage.State state = new RevisionComparePage.State();
						CodeComment comment = context.getOpenComment();
						state.commentId = comment.getId();
						state.mark = comment.getMarkPos();
						state.compareWithMergeBase = false;
						state.leftSide = new ProjectAndRevision(comment.getProject(), 
								comment.getMarkPos().getCommit());
						state.rightSide = new ProjectAndRevision(comment.getProject(), revision);
						state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
						state.whitespaceOption = context.getOpenComment().getCompareContext().getWhitespaceOption();
						PageParameters params = RevisionComparePage.paramsOf(comment.getProject(), state);
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
					mark = comment.getMarkPos().getRange();
				} else {
					mark = (TextRange) commentContainer.getDefaultModelObject();
				}
				mark(target, mark);
				context.onMark(target, mark);
				target.appendJavaScript(String.format("$('#%s').blur();", getMarkupId()));
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				setOutputMarkupId(true);
			}
			
		});
		
		head.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(commentContainer));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				closeComment(target);
			}
			
		});
		
		commentContainer.setOutputMarkupPlaceholderTag(true);
		
		if (context.getOpenComment() != null) {
			CodeCommentPanel commentPanel = new CodeCommentPanel(BODY_ID, context.getOpenComment().getId()) {

				@Override
				protected void onDeleteComment(AjaxRequestTarget target, CodeComment comment) {
					SourceViewPanel.this.onCommentDeleted(target, comment);
				}

				@Override
				protected void onSaveComment(AjaxRequestTarget target, CodeComment comment) {
					target.add(commentContainer.get("head"));
				}

				@Override
				protected PullRequest getPullRequest() {
					return context.getPullRequest();
				}

				@Override
				protected CompareContext getCompareContext() {
					return SourceViewPanel.this.getCompareContext();
				}

			};
			commentContainer.add(commentPanel);
		} else {
			commentContainer.add(new WebMarkupContainer(BODY_ID));
			commentContainer.setVisible(false);
		}
		
		add(commentContainer);
		
		add(ajaxBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				switch(params.getParameterValue("action").toString()) {
				case "openSelectionPopover": 
					TextRange mark = getMark(params, "param1", "param2", "param3", "param4");
					String unableCommentMessage = null;
					PullRequest request = context.getPullRequest();
					String commitHash = context.getCommit().name();
					if (request != null) {
						boolean found = false;
						for (RevCommit commit: context.getPullRequest().getCommits()) {
							if (commit.name().equals(commitHash)) {
								found = true;
								break;
							}
						}
						
						if (!found) { 
							if (request.getMergePreview() != null 
									&& request.getMergePreview().getMerged() != null 
									&& request.getMergePreview().getMerged().equals(commitHash)) {
								unableCommentMessage = "Unable to comment on pull request merge preview";
							} else {
								unableCommentMessage = "Unable to comment on commits not belonging to pull request";
							}
						}
					}
							
					String script = String.format("onedev.server.sourceView.openSelectionPopover(%s, '%s', %s, %s);", 
							getJson(mark), context.getMarkUrl(mark), SecurityUtils.getUser()!=null, 
							unableCommentMessage!=null?"'" + unableCommentMessage + "'":"undefined");
					target.appendJavaScript(script);
					break;
				case "addComment": 
					Preconditions.checkNotNull(SecurityUtils.getUser());
					
					mark = getMark(params, "param1", "param2", "param3", "param4");
					commentContainer.setDefaultModelObject(mark);
					
					Fragment fragment = new Fragment(BODY_ID, "newCommentFrag", SourceViewPanel.this);
					fragment.setOutputMarkupId(true);
					
					Form<?> form = new Form<Void>("form");
					
					String uuid = UUID.randomUUID().toString();
					
					CommentInput contentInput;
					
					StringBuilder mentions = new StringBuilder();

					if (context.getPullRequest() == null) {
						/*
						 * Outside of pull request, no one will be notified of the comment. So we automatically 
						 * mention authors of commented lines
						 */
						Range range = new Range(mark.getBeginLine(), mark.getEndLine());
						for (User user: context.getProject().getAuthors(context.getBlobIdent().path, context.getCommit(), range)) {
							if (user.getEmail() != null)
								mentions.append("@").append(user.getName()).append(" ");
						}
					}
					
					form.add(contentInput = new CommentInput("content", Model.of(mentions.toString()), true) {

						@Override
						protected ProjectAttachmentSupport getAttachmentSupport() {
							return new ProjectAttachmentSupport(context.getProject(), uuid);
						}

						@Override
						protected Project getProject() {
							return context.getProject();
						}
						
					});
					contentInput.setRequired(true).setLabel(Model.of("Comment"));
					
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
							target.appendJavaScript("onedev.server.sourceView.mark(undefined);");
							target.appendJavaScript("$(window).resize();");
							context.onMark(target, null);
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
							comment.setMarkPos(new MarkPos());
							comment.getMarkPos().setCommit(context.getCommit().name());
							comment.getMarkPos().setPath(context.getBlobIdent().path);
							comment.setContent(contentInput.getModelObject());
							comment.setUser(SecurityUtils.getUser());
							comment.getMarkPos().setRange(mark);
							comment.setProject(context.getProject());
							comment.setCompareContext(getCompareContext());
							
							OneDev.getInstance(CodeCommentManager.class).create(comment, context.getPullRequest());
							
							CodeCommentPanel commentPanel = new CodeCommentPanel(fragment.getId(), comment.getId()) {

								@Override
								protected void onDeleteComment(AjaxRequestTarget target, CodeComment comment) {
									SourceViewPanel.this.onCommentDeleted(target, comment);
								}

								@Override
								protected void onSaveComment(AjaxRequestTarget target, CodeComment comment) {
									target.add(commentContainer.get("head"));
								}

								@Override
								protected PullRequest getPullRequest() {
									return context.getPullRequest();
								}

								@Override
								protected CompareContext getCompareContext() {
									return SourceViewPanel.this.getCompareContext();
								}

							};
							commentContainer.replace(commentPanel);
							target.add(commentContainer);

							String script = String.format("onedev.server.sourceView.onCommentAdded(%s);", getJsonOfComment(comment));
							target.appendJavaScript(script);
							
							context.onCommentOpened(target, comment);
						}

					});
					fragment.add(form);
					commentContainer.replace(fragment);
					commentContainer.setVisible(true);
					target.add(commentContainer);
					context.onAddComment(target, mark);
					target.appendJavaScript(String.format("onedev.server.sourceView.onAddComment(%s);", getJson(mark)));
					break;
				case "toggleComment":
					Long commentId = params.getParameterValue("param1").toLong();
					CodeComment comment = OneDev.getInstance(CodeCommentManager.class).load(commentId);
					if (!comment.equals(context.getOpenComment())) {
						CodeCommentPanel commentPanel = new CodeCommentPanel(BODY_ID, commentId) {

							@Override
							protected void onDeleteComment(AjaxRequestTarget target, CodeComment comment) {
								SourceViewPanel.this.onCommentDeleted(target, comment);
							}

							@Override
							protected void onSaveComment(AjaxRequestTarget target, CodeComment comment) {
								target.add(commentContainer.get("head"));
							}

							@Override
							protected PullRequest getPullRequest() {
								return context.getPullRequest();
							}

							@Override
							protected CompareContext getCompareContext() {
								return SourceViewPanel.this.getCompareContext();
							}

						};
						commentContainer.replace(commentPanel);
						commentContainer.setVisible(true);
						target.add(commentContainer);
						script = String.format("onedev.server.sourceView.onOpenComment(%s);", getJsonOfComment(comment));
						target.appendJavaScript(script);
						context.onCommentOpened(target, comment);
					} else {
						closeComment(target);
					}
					break;
				case "outlineSearch":
					new ModalPanel(target) {

						@Override
						protected Component newContent(String id) {
							return newOutlineSearchPanel(id, this);
						}
						
					};
					break;
				case "syncOutline": 
					int line = params.getParameterValue("param1").toInt();
					int ch = params.getParameterValue("param2").toInt();
					Symbol closest = null;
					for (Symbol symbol: symbols) {
						TokenPosition scope = symbol.getScope();
						if (scope != null) {
							if (contains(scope, line, ch)) {
								if (closest != null) {
									if (contains(closest.getScope(), scope.getFromLine(), scope.getFromCh()) 
											&& contains(closest.getScope(), scope.getToLine(), scope.getToCh())) {
										closest = symbol;
									}
								} else {
									closest = symbol;
								}
							}
						}
					}
					if (closest != null) {
						@SuppressWarnings("unchecked")
						NestedTree<Symbol> tree = (NestedTree<Symbol>) outline.get(BODY_ID);
						Symbol current = closest;
						while (current != null) {
							tree.expand(current);
							current = current.getParent();
						}
						script = String.format("onedev.server.sourceView.syncOutline('%s');", 
								getSymbolId(symbols, closest));
						target.appendJavaScript(script);
					}
					break;
				}
			}
			
		});
		
		add(blameMessageBehavior = new BlameMessageBehavior() {
			
			@Override
			protected Project getProject() {
				return context.getProject();
			}
		});
		
		outline = new WebMarkupContainer("outline") {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript("onedev.server.sourceView.initOutline();"));
			}
			
		};
		outline.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				toggleOutline(target);
			}
			
		});
		
		IModel<HashSet<Symbol>> state = new Model<HashSet<Symbol>>(new HashSet<>(getChildSymbols(symbols, null)));
		NestedTree<Symbol> tree = new NestedTree<Symbol>(BODY_ID, newSymbolTreeProvider(symbols), state) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new HumanTheme());				
			}

			@Override
			protected Component newContentComponent(String id, IModel<Symbol> nodeModel) {
				Symbol symbol = nodeModel.getObject();
				
				Fragment fragment = new Fragment(id, "outlineNodeFrag", SourceViewPanel.this);
				fragment.setMarkupId(getSymbolId(symbols, symbol));
				fragment.setOutputMarkupId(true);
				
				fragment.add(symbol.renderIcon("icon"));
				
				AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						context.onSelect(target, context.getBlobIdent(), symbol.getPosition());
					}
					
				};
				link.add(symbol.render("label", null));
				fragment.add(link);
				
				return fragment;
			}
			
		};		
		
		outline.add(tree);
		
		outline.setOutputMarkupPlaceholderTag(true);
		add(outline);
		
		outline.setVisible(isOutlineVisibleInitially());
		
		add(symbolTooltip = new SymbolTooltipPanel("symbolTooltip", new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return context.getProject();
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

	private boolean isOutlineVisibleInitially() {
		if (hasOutline()) {
			WebRequest request = (WebRequest) RequestCycle.get().getRequest();
			Cookie cookie = request.getCookie(COOKIE_OUTLINE);
			if (cookie != null) {
				return cookie.getValue().equals("yes");
			} else {
				return !WicketUtils.isDevice();
			}
		} else {
			return false;
		}
	}
	
	private boolean contains(TokenPosition scope, int line, int ch) {
		int fromLine = scope.getFromLine();
		int toLine = scope.getToLine();
		int fromCh = scope.getFromCh();
		int toCh = scope.getToCh();
		return (fromLine < line || fromLine == line && fromCh<=ch) && (toLine > line || toLine == line && toCh >= ch);
	}
	
	private CompareContext getCompareContext() {
		CompareContext compareContext = new CompareContext();
		compareContext.setCompareCommit(context.getCommit().name());
		if (context.getBlobIdent().path != null)
			compareContext.setPathFilter(PatternSet.quoteIfNecessary(context.getBlobIdent().path));
		return compareContext;
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
		commentInfo.mark = Preconditions.checkNotNull(comment.mapRange(context.getBlobIdent()));

		String jsonOfCommentInfo;
		try {
			jsonOfCommentInfo = OneDev.getInstance(ObjectMapper.class).writeValueAsString(commentInfo);
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
		String script = String.format("onedev.server.sourceView.onCommentDeleted(%s);", 
				getJsonOfComment(comment));
		target.appendJavaScript(script);
		context.onCommentOpened(target, null);
	}
	
	private boolean hasOutline() {
		for (Symbol symbol: symbols) {
			if (symbol.isDisplayInOutline())
				return true;
		}
		return false;
	}
	
	private List<Symbol> getChildSymbols(List<Symbol> symbols, @Nullable Symbol parentSymbol) {
		List<Symbol> children = new ArrayList<>();
		for (Symbol symbol: symbols) {
			if (symbol.isDisplayInOutline() && symbol.getOutlineParent() == parentSymbol)
				children.add(symbol);
		}
		return children;
	}
	
	private String getJsonOfBlameInfos(boolean blamed) {
		String jsonOfBlameInfos;
		if (blamed) {
			List<BlameInfo> blameInfos = new ArrayList<>();
			
			String commitHash = context.getCommit().name();
			
			BlameCommand cmd = new BlameCommand(context.getProject().getGitDir());
			cmd.commitHash(commitHash).file(context.getBlobIdent().path);
			for (BlameBlock blame: cmd.call()) {
				BlameInfo blameInfo = new BlameInfo();
				blameInfo.commitDate = DateUtils.formatDate(blame.getCommit().getCommitter().getWhen());
				blameInfo.authorName = HtmlEscape.escapeHtml5(blame.getCommit().getAuthor().getName());
				blameInfo.hash = blame.getCommit().getHash();
				blameInfo.abbreviatedHash = GitUtils.abbreviateSHA(blame.getCommit().getHash(), 7);
				CommitDetailPage.State state = new CommitDetailPage.State();
				state.revision = blame.getCommit().getHash();
				if (context.getBlobIdent().path != null)
					state.pathFilter = PatternSet.quoteIfNecessary(context.getBlobIdent().path);
				PageParameters params = CommitDetailPage.paramsOf(context.getProject(), state);
				blameInfo.url = RequestCycle.get().urlFor(CommitDetailPage.class, params).toString();
				blameInfo.ranges = blame.getRanges();
				blameInfos.add(blameInfo);
			}
			try {
				jsonOfBlameInfos = OneDev.getInstance(ObjectMapper.class).writeValueAsString(blameInfos);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		} else {
			jsonOfBlameInfos = "undefined";
		}
		return jsonOfBlameInfos;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new SourceViewResourceReference()));
		
		Blob blob = context.getProject().getBlob(context.getBlobIdent());
		
		String jsonOfBlameInfos = getJsonOfBlameInfos(context.getMode() == Mode.BLAME);
		Map<Integer, List<CommentInfo>> commentInfos = new HashMap<>(); 
		for (Map.Entry<CodeComment, TextRange> entry: commentsModel.getObject().entrySet()) {
			CodeComment comment = entry.getKey();
			TextRange textRange = entry.getValue();
			int line = textRange.getBeginLine();
			List<CommentInfo> commentInfosAtLine = commentInfos.get(line);
			if (commentInfosAtLine == null) {
				commentInfosAtLine = new ArrayList<>();
				commentInfos.put(line, commentInfosAtLine);
			}
			CommentInfo commentInfo = new CommentInfo();
			commentInfo.id = comment.getId();
			commentInfo.mark = textRange;
			commentInfosAtLine.add(commentInfo);
		}
		for (List<CommentInfo> value: commentInfos.values()) {
			value.sort((o1, o2)->(int)(o1.id-o2.id));
		}
		
		String jsonOfCommentInfos;
		try {
			jsonOfCommentInfos = OneDev.getInstance(ObjectMapper.class).writeValueAsString(commentInfos);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		
		String jsonOfMark = context.getMark()!=null?getJson(context.getMark()):"undefined";
			
		CharSequence callback = ajaxBehavior.getCallbackFunction(
				explicit("action"), explicit("param1"), explicit("param2"), 
				explicit("param3"), explicit("param4"));
		
		String script = String.format("onedev.server.sourceView.onDomReady('%s', '%s', %s, %s, '%s', '%s', "
				+ "%s, %s, %s, %s, %s, '%s');", 
				JavaScriptEscape.escapeJavaScript(context.getBlobIdent().path),
				JavaScriptEscape.escapeJavaScript(blob.getText().getContent()),
				context.getOpenComment()!=null?getJsonOfComment(context.getOpenComment()):"undefined",
				jsonOfMark,
				symbolTooltip.getMarkupId(), 
				context.getBlobIdent().revision, 
				jsonOfBlameInfos, 
				jsonOfCommentInfos,
				callback, 
				blameMessageBehavior.getCallback(),
				sourceFormat.getTabSize(),
				sourceFormat.getLineWrapMode());
		response.render(OnDomReadyHeaderItem.forScript(script));
		
		script = String.format("onedev.server.sourceView.onWindowLoad(%s);", jsonOfMark);
		response.render(OnLoadHeaderItem.forScript(script));
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
	
	private String getSymbolId(List<Symbol> symbols, Symbol symbol) {
		return "outline-symbol-" + symbols.indexOf(symbol);
	}
	
	@SuppressWarnings("unused")
	private static class CommentInfo {
		long id;
		
		String title;
		
		TextRange mark;
	}

	@Override
	protected boolean isEditSupported() {
		return true;
	}
	
	@Override
	public void mark(AjaxRequestTarget target, TextRange mark) {
		String script;
		if (mark != null) {
			script = String.format("onedev.server.sourceView.mark(%s);", getJson(mark));
		} else {
			script = String.format("onedev.server.sourceView.mark(undefined);");
		}
		target.appendJavaScript(script);
	}

	private ITreeProvider<Symbol> newSymbolTreeProvider(List<Symbol> symbols) {
		return new ITreeProvider<Symbol>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends Symbol> getRoots() {
				return getChildSymbols(symbols, null).iterator();
			}

			@Override
			public boolean hasChildren(Symbol symbol) {
				return !getChildSymbols(symbols, symbol).isEmpty();
			}

			@Override
			public Iterator<? extends Symbol> getChildren(Symbol symbol) {
				return getChildSymbols(symbols, symbol).iterator();
			}

			@Override
			public IModel<Symbol> model(Symbol symbol) {
				return Model.of(symbol);
			}
			
		};		
	}
	
	private NestedTree<Symbol> newOutlineSearchSymbolTree(ModalPanel modal, List<Symbol> symbols, 
			@Nullable String searchInput) {
		IModel<HashSet<Symbol>> state;
		if (StringUtils.isNotBlank(searchInput)) {
			state = new Model<HashSet<Symbol>>(new HashSet<>(symbols));
		} else {
			state = new Model<HashSet<Symbol>>(new HashSet<>(getChildSymbols(symbols, null)));
		}
		NestedTree<Symbol> tree = new NestedTree<Symbol>("result", newSymbolTreeProvider(symbols), state) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new HumanTheme());				
			}

			@Override
			protected Component newContentComponent(String id, IModel<Symbol> nodeModel) {
				Symbol symbol = nodeModel.getObject();
				
				Fragment fragment = new Fragment(id, "outlineSearchNodeFrag", SourceViewPanel.this);
				fragment.setOutputMarkupId(true);
				
				AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
						context.onSelect(target, context.getBlobIdent(), symbol.getPosition());
					}
					
				};
				link.add(symbol.renderIcon("icon"));
				link.add(symbol.render("label", null));
				link.add(AttributeAppender.append("data-symbolindex", symbols.indexOf(symbol)));
				
				fragment.add(link);
				
				if (symbol == symbols.get(0)) {
					link.add(AttributeAppender.append("class", "active"));
				}
				
				return fragment;
			}
			
		};		
		
		tree.setOutputMarkupId(true);
		
		return tree;
	}
	
	private void closeComment(AjaxRequestTarget target) {
		clearComment(target);
		if (context.getOpenComment() != null) 
			context.onCommentOpened(target, null);
		target.appendJavaScript("onedev.server.sourceView.onCloseComment();");
	}
	
	private Component newOutlineSearchPanel(String id, ModalPanel modal) {
		Fragment fragment = new Fragment(id, "outlineSearchFrag", SourceViewPanel.this);
		fragment.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				modal.close();
			}
			
		});
		
		TextField<String> searchField = new TextField<>("input");
		fragment.add(searchField);
		fragment.add(newOutlineSearchSymbolTree(modal, symbols, null));
		
		fragment.add(new AbstractPostAjaxBehavior() {
			
			private List<Symbol> filteredSymbols = new ArrayList<Symbol>(symbols);
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setChannel(new AjaxChannel("outline-search-input", AjaxChannel.Type.DROP));
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String key = params.getParameterValue("key").toString();

				if (key.equals("input")) {
					String searchInput = params.getParameterValue("param").toString();
					
					MatchScoreProvider<Symbol> matchScoreProvider = new MatchScoreProvider<Symbol>() {

						@Override
						public double getMatchScore(Symbol object) {
							return MatchScoreUtils.getMatchScore(object.getName(), searchInput);
						}
						
					};
					
					List<Symbol> matchSymbols = MatchScoreUtils.filterAndSort(symbols, matchScoreProvider);
					
					filteredSymbols = new ArrayList<>();
					for (Symbol symbol: matchSymbols) {
						Symbol current = symbol;
						while (current != null) {
							if (!filteredSymbols.contains(current))
								filteredSymbols.add(current);
							current = current.getOutlineParent();
						}
					}
					
					NestedTree<Symbol> tree = newOutlineSearchSymbolTree(modal, filteredSymbols, searchInput);
					fragment.replace(tree);
					target.add(tree);
				} else if (key.equals("return")) {
					int symbolIndex = params.getParameterValue("param").toInt();
					Symbol symbol = filteredSymbols.get(symbolIndex); 
					context.onSelect(target, context.getBlobIdent(), symbol.getPosition());
					modal.close();
				} else {
					throw new IllegalStateException("Unrecognized key: " + key);
				}
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				String script = String.format(
						"onedev.server.sourceView.onOutlineSearchDomReady('%s', %s);", 
						fragment.getMarkupId(), 
						getCallbackFunction(explicit("key"), explicit("param")));
				
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});	
		fragment.setOutputMarkupId(true);
		
		return fragment;
	}
	
	@Override
	public List<MenuItem> getMenuItems(FloatingPanel dropdown) {
		List<MenuItem> menuItems = new ArrayList<>();
		if (hasOutline()) {
			menuItems.add(new MenuItem() {

				@Override
				public String getShortcut() {
					return "O";
				}

				@Override
				public String getLabel() {
					return "Outline Search";
				}

				@Override
				public WebMarkupContainer newLink(String id) {
					return new ModalLink(id) {

						@Override
						public void onClick(AjaxRequestTarget target) {
							super.onClick(target);
							dropdown.close();
						}
						
						@Override
						protected Component newContent(String id, ModalPanel modal) {
							return newOutlineSearchPanel(id, modal);
						}
						
					};
				}
				
			});
		} 
		return menuItems;
	}

	@Override
	protected boolean isViewPlainSupported() {
		return viewPlainMode;
	}
	
}
