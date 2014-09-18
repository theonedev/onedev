package com.pmease.gitplex.web.component.diff;

import static com.pmease.commons.util.diff.DiffLine.Action.ADD;
import static com.pmease.commons.util.diff.DiffLine.Action.DELETE;
import static com.pmease.commons.util.diff.DiffLine.Action.EQUAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.Strings;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobText;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.commons.util.diff.DiffLine;
import com.pmease.commons.util.diff.DiffUtils;
import com.pmease.commons.util.diff.Partial;
import com.pmease.commons.util.diff.WordSplitter;
import com.pmease.commons.wicket.behavior.ScrollBehavior;
import com.pmease.commons.wicket.behavior.StickyBehavior;
import com.pmease.commons.wicket.behavior.menu.CheckMenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuBehavior;
import com.pmease.commons.wicket.behavior.menu.MenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.InlineComment;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.comment.InlineContext;
import com.pmease.gitplex.core.comment.InlineContextAware;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.comment.CommentPanel;
import com.pmease.gitplex.web.component.comment.event.CommentRemoved;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;

@SuppressWarnings("serial")
public class TextDiffPanel extends Panel implements InlineContextAware {

	private enum DiffOption {IGNORE_NOTHING, IGNORE_EOL, IGNORE_EOL_SPACES, IGNORE_CHANGE_SPACES};
	
	private static final int SCROLL_MARGIN = 50;
	
	private static final String COMMENT_ACTIONS_ID = "commentActions";
	
	private static final String HEAD_ID = "head";
	
	private final IModel<Repository> repoModel;
	
	private final BlobText oldText;
	
	private final BlobText newText;
	
	private final RevAwareChange change;
	
	private final InlineCommentSupport commentSupport;
	
	private final IModel<Boolean> allowToAddCommentModel;
	
	private BlobText effectiveOldText;
	
	private BlobText effectiveNewText;
	
	private DiffOption diffOption = DiffOption.IGNORE_NOTHING;
	
	private boolean showComments = true;
	
	private int index;
	
	private List<DiffLine> diffs;
	
	private final IModel<Map<Integer, List<InlineComment>>> commentsModel = 
			new LoadableDetachableModel<Map<Integer, List<InlineComment>>>() {

				@Override
				protected Map<Integer, List<InlineComment>> load() {
					Map<Integer, Integer> oldLinesMap = new HashMap<>();
					Map<Integer, Integer> newLinesMap = new HashMap<>();

					int index = 0;
					for (DiffLine diff: diffs) {
						oldLinesMap.put(diff.getOldLineNo(), index);
						newLinesMap.put(diff.getNewLineNo(), index);
						index++;
					}
					
					Map<Integer, List<InlineComment>> comments = new HashMap<>();
					for (Map.Entry<Integer, List<InlineComment>> entry: commentSupport.getOldComments().entrySet()) {
						int diffLineNo = oldLinesMap.get(entry.getKey());
						List<InlineComment> lineComments = comments.get(diffLineNo);
						if (lineComments == null) {
							lineComments = new ArrayList<>();
							comments.put(diffLineNo, lineComments);
						}
						lineComments.addAll(entry.getValue());
					}
					for (Map.Entry<Integer, List<InlineComment>> entry: commentSupport.getNewComments().entrySet()) {
						int diffLineNo = newLinesMap.get(entry.getKey());
						List<InlineComment> lineComments = comments.get(diffLineNo);
						if (lineComments == null) {
							lineComments = new ArrayList<>();
							comments.put(diffLineNo, lineComments);
						}
						lineComments.addAll(entry.getValue());
					}
					
					for (List<InlineComment> lineComments: comments.values()) {
						Collections.sort(lineComments, new Comparator<InlineComment>() {

							@Override
							public int compare(InlineComment o1, InlineComment o2) {
								return o1.getDate().compareTo(o2.getDate());
							}
							
						});
					}
					
					return comments;
				}
		
	};
	
	private RepeatingView commentsView;
	
	private String autoScrollScript;

	private void onDiffOptionChanged() {
		if (diffOption == DiffOption.IGNORE_EOL) {
			effectiveOldText = oldText.ignoreEOL();
			effectiveNewText = newText.ignoreEOL();
		} else if (diffOption == DiffOption.IGNORE_EOL_SPACES) {
			effectiveOldText = oldText.ignoreEOLSpaces();
			effectiveNewText = newText.ignoreEOLSpaces();
		} else if (diffOption == DiffOption.IGNORE_CHANGE_SPACES) {
			effectiveOldText = oldText.ignoreChangeSpaces();
			effectiveNewText = newText.ignoreChangeSpaces();
		} else {
			effectiveOldText = oldText;
			effectiveNewText = newText;
		}
		
		diffs = DiffUtils.diff(effectiveOldText.getLines(), effectiveNewText.getLines(), new WordSplitter());
	}

	private boolean isIdentical() {
		for (DiffLine diffLine: diffs) {
			if (diffLine.getAction() != EQUAL)
				return false;
		}
		return true;
	}
	
	public TextDiffPanel(String id, final IModel<Repository> repoModel, 
			BlobText oldText, BlobText newText, RevAwareChange change, 
			@Nullable InlineCommentSupport commentContext) {
		super(id);
		
		this.repoModel = repoModel;
		
		Preconditions.checkArgument(!change.getOldRevision().equals(change.getNewRevision()));
		
		this.change = change;
		this.commentSupport = commentContext;
		
		this.oldText = oldText;
		this.newText = newText;
		
		// cache add comment permission check in model to avoid recalculation for every line
		allowToAddCommentModel = new LoadableDetachableModel<Boolean>() {
	
			@Override
			protected Boolean load() {
				User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
				ObjectPermission readPermission = ObjectPermission.ofRepositoryRead(repoModel.getObject());
				return currentUser != null && SecurityUtils.getSubject().isPermitted(readPermission);
			}
			
		};
		
		onDiffOptionChanged();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		final WebMarkupContainer head = new WebMarkupContainer(HEAD_ID);
		
		head.add(new StickyBehavior());
		
		head.add(new DiffStatBar("diffStat", new AbstractReadOnlyModel<List<DiffLine>>() {

			@Override
			public List<DiffLine> getObject() {
				return diffs;
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!isIdentical());
			}
			
		});
		add(head);
		
		List<String> alerts = new ArrayList<>();
		if (!oldText.getCharset().equals(newText.getCharset()))
			alerts.add("Charset is changed from " + oldText.getCharset() + " to " + newText.getCharset());
		if (!oldText.isHasEolAtEof())
			alerts.add("Original text does not have EOL character at EOF");
		if (!newText.isHasEolAtEof())
			alerts.add("Revised text does not have EOL character at EOF");
		
		head.add(new FileDiffTitle("title", change, alerts));
		
		head.add(new WebMarkupContainer("identical") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(isIdentical());
			}
			
		});
		
		head.add(new WebMarkupContainer("prevDiff") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!isIdentical());
			}
			
		}.add(new ScrollBehavior(".diff-block", SCROLL_MARGIN, false)));

		head.add(new WebMarkupContainer("nextDiff") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!isIdentical());
			}
			
		}.add(new ScrollBehavior(".diff-block", SCROLL_MARGIN, true)));
		
		// add a separate comment actions container in order not to refresh the whole
		// sticky head when show/hide comment actions (refreshing sticky head via Wicket 
		// ajax has some displaying issues) 
		WebMarkupContainer commentActions = new WebMarkupContainer(COMMENT_ACTIONS_ID);
		commentActions.setOutputMarkupId(true);
		
		head.add(commentActions);
		commentActions.add(new WebMarkupContainer("prevComment") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentSupport != null && showComments);
			}
			
		}.add(new ScrollBehavior(".comments.line", 50, false)));
		
		commentActions.add(new WebMarkupContainer("nextComment") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentSupport != null && showComments);
			}
			
		}.add(new ScrollBehavior(".comments.line", 50, true)));
		
		commentActions.add(new AjaxLink<Void>("showComments") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				showComments(target);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentSupport != null && !showComments);
			}

		});
		commentActions.add(new AjaxLink<Void>("hideComments") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				hideComments(target);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentSupport != null && showComments);
			}
			
		});

		MenuPanel diffOptionMenuPanel = new MenuPanel("diffOptions") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> menuItems = new ArrayList<>();
				
				menuItems.add(new CheckMenuItem() {
					
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						onDiffOptionChanged();
						close(target);
						target.add(TextDiffPanel.this);
					}
					
					@Override
					protected String getLabel() {
						return "Ignore end of line differences";
					}
					
					@Override
					protected IModel<Boolean> getCheckModel() {
						return new IModel<Boolean>() {

							@Override
							public void detach() {
							}
 
							@Override
							public Boolean getObject() {
								return diffOption == DiffOption.IGNORE_EOL;
							}

							@Override
							public void setObject(Boolean object) {
								if (object)
									diffOption = DiffOption.IGNORE_EOL;
								else
									diffOption = DiffOption.IGNORE_NOTHING;
							}
							
						};
					}
				});
				
				menuItems.add(new CheckMenuItem() {
					
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						onDiffOptionChanged();
						close(target);
						target.add(TextDiffPanel.this);
					}
					
					@Override
					protected String getLabel() {
						return "Ignore white spaces at line end";
					}
					
					@Override
					protected IModel<Boolean> getCheckModel() {
						return new IModel<Boolean>() {

							@Override
							public void detach() {
							}

							@Override
							public Boolean getObject() {
								return diffOption == DiffOption.IGNORE_EOL_SPACES;
							}

							@Override
							public void setObject(Boolean object) {
								if (object)
									diffOption = DiffOption.IGNORE_EOL_SPACES;
								else
									diffOption = DiffOption.IGNORE_NOTHING;
							}
							
						};
					}
				});

				menuItems.add(new CheckMenuItem() {
					
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						onDiffOptionChanged();
						close(target);
						target.add(TextDiffPanel.this);
					}
					
					@Override
					protected String getLabel() {
						return "Ignore white space changes";
					}
					
					@Override
					protected IModel<Boolean> getCheckModel() {
						return new IModel<Boolean>() {

							@Override
							public void detach() {
							}

							@Override
							public Boolean getObject() {
								return diffOption == DiffOption.IGNORE_CHANGE_SPACES;
							}

							@Override
							public void setObject(Boolean object) {
								if (object)
									diffOption = DiffOption.IGNORE_CHANGE_SPACES;
								else
									diffOption = DiffOption.IGNORE_NOTHING;
							}
							
						};
					}
				});

				return menuItems;
			}
			
		};
		
		head.add(diffOptionMenuPanel);
		
		head.add(new WebMarkupContainer("diffOptionsTrigger").add(new MenuBehavior(diffOptionMenuPanel)));

		Form<?> form = new Form<Void>("addComment");
		form.setOutputMarkupId(true);
		
		final CommentInput input;
		form.add(input = new CommentInput("input", Model.of("")));
		input.setRequired(true);
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				input.setModelObject("");
				target.appendJavaScript(String.format("gitplex.comments.cancelAdd(%d);", index));
			}
			
		});
		form.add(new AjaxSubmitLink("save") {

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				DiffLine diff = diffs.get(index);
				String commit;
				int lineNo;
				
				if (diff.getAction() == DELETE) {
					commit = change.getOldRevision();
					lineNo = diff.getOldLineNo();
				} else {
					commit = change.getNewRevision();
					lineNo = diff.getNewLineNo();
				}
				
				commentSupport.addComment(commit, change.getPath(), lineNo, input.getModelObject());
				
				Component commentsRow = newCommentsRow(commentsView.newChildId(), index);
				commentsView.add(commentsRow);
				target.add(commentsRow);
				
				input.setModelObject("");
				String prependScript = String.format("$('#comments-placeholder').append('<table id=\"%s\"></table>')", 
						commentsRow.getMarkupId());
				target.prependJavaScript(prependScript);
				target.appendJavaScript(String.format("gitplex.comments.afterAdd(%d);", index));
			}

		});
		add(form);
		
		final AbstractDefaultAjaxBehavior addCommentBehavior;
		add(addCommentBehavior = new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);

				attributes.getDynamicExtraParameters().add("return {index: index}");
			}

			@Override
			public boolean getStatelessHint(Component component) {
				return false;
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
				index = RequestCycle.get().getRequest().getQueryParameters()
						.getParameterValue("index").toInt();

				if (!showComments)
					showComments(target);
				
				target.appendJavaScript(String.format("gitplex.comments.beforeAdd(%d);", index));
			}
			
		});
		
		add(new Label("diffs", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				StringBuilder builder = new StringBuilder("<table id='diffs-table' class='table-diff'>");
				int index = 0;
				for (DiffLine diff: diffs) {
					String addCommentLink;
					if (allowToAddCommentModel.getObject() && commentSupport != null) {
						addCommentLink = "<a href='javascript: var index=" + index + "; " 
							+ addCommentBehavior.getCallbackScript() 
							+ ";' class='add-comment'><i class='fa fa-comment-add'></i></a>";
					} else {
						addCommentLink = "";
					}
					builder.append("<tr id='diffline-").append(index).append("' class='line content ");
					if (diff.getAction() == ADD) {
						if (index == 0 || diffs.get(index-1).getAction() == EQUAL)
							builder.append("new diff-block'>");
						else
							builder.append("new'>");
						builder.append("<td class='old line-no'>").append(addCommentLink).append("</td>");
						builder.append("<td class='new line-no'>");
						builder.append("+ ").append(diff.getNewLineNo()+1).append("</td>");
					} else if (diff.getAction() == DELETE) {
						if (index == 0 || diffs.get(index-1).getAction() == EQUAL)
							builder.append("old diff-block'>");
						else
							builder.append("old'>");
						builder.append("<td class='old line-no'>").append(addCommentLink);
						builder.append("- ").append(diff.getOldLineNo()+1).append("</td>");
						builder.append("<td class='new line-no'></td>");
					} else {
						builder.append("equal'>");
						builder.append("<td class='old line-no'>").append(addCommentLink)
								.append("  ").append(diff.getOldLineNo()+1).append("</td>");
						builder.append("<td class='new line-no'>  ").append(diff.getNewLineNo()+1).append("</td>");
					}
					builder.append("<td class='text'>");
					
					for (Partial partial: diff.getPartials()) {
						if (partial.isEmphasized())
							builder.append("<span class='emphasize'>");
						else
							builder.append("<span>");
						if (partial.getContent().equals("\r"))
							builder.append(" ");
						else
							builder.append(Strings.escapeMarkup(partial.getContent(), false, false));
						builder.append("</span>");
					}
					builder.append("</td></tr>");
					index++;
				}
				return builder.append("</table>").toString();
			}
			
		}).setEscapeModelStrings(false));
		
		add(newCommentsView());
		
		autoScrollScript = String.format(""
				+ "if ($('.concerned-comment').length != 0) "
				+ "  pmease.commons.scroll.next('.concerned-comment', %d);"
				+ "else"
				+ "  pmease.commons.scroll.next('.diff-block:first', %d);", SCROLL_MARGIN * 2, SCROLL_MARGIN);
	}
	
	private Component newCommentsRow(String id, final int index) {
		WebMarkupContainer row = new WebMarkupContainer(commentsView.newChildId()) {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				
				if (event.getPayload() instanceof CommentRemoved) {
					CommentRemoved commentRemoved = (CommentRemoved) event.getPayload();
					commentsModel.getObject().get(index).remove(commentRemoved.getComment());
					commentRemoved.getTarget().appendJavaScript(
							String.format("$('#%s').closest('tr').remove();", getMarkupId()));
				} 
			}
			
		};
		row.setOutputMarkupId(true);
		row.setMarkupId("comment-diffline-" + index);
		
		row.add(new ListView<InlineComment>("comments", new LoadableDetachableModel<List<InlineComment>>() {

			@Override
			protected List<InlineComment> load() {
				return commentsModel.getObject().get(index); 
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<InlineComment> item) {
				item.add(new UserLink("avatar", new UserModel(item.getModelObject().getUser()), AvatarMode.AVATAR));
				item.add(new WebMarkupContainer("concerned")
						.setVisible(item.getModelObject().equals(commentSupport.getConcernedComment())));
				item.add(new CommentPanel("comment", item.getModel()).setOutputMarkupId(true));
			}
			
		});
		
		return row;
	}
	
	private Component newCommentsView() {
		commentsView = new RepeatingView("lines");
		
		if (commentSupport != null) {
			for (int index: commentsModel.getObject().keySet()) 
				commentsView.add(newCommentsRow(commentsView.newChildId(), index));
		}
		
		return commentsView;
	}
	
	@Override
	protected void onBeforeRender() {
		replace(newCommentsView());
		
		super.onBeforeRender();
	}

	private void showComments(AjaxRequestTarget target) {
		showComments = true;
		target.add(get(HEAD_ID).get(COMMENT_ACTIONS_ID));

		target.appendJavaScript("$('.comments.line').show();");
	}

	private void hideComments(AjaxRequestTarget target) {
		showComments = false;
		target.add(get(HEAD_ID).get(COMMENT_ACTIONS_ID));
		
		target.appendJavaScript("$('.comments.line').hide();");
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(OnDomReadyHeaderItem.forScript("gitplex.comments.position();"));
		
		// only auto-scroll this panel once after it is created; otherwise page refreshing 
		// caused by actions such as integrate/discard on request compare page will also 
		// get scrolled
		if (autoScrollScript != null) {
			response.render(OnDomReadyHeaderItem.forScript(autoScrollScript));
			autoScrollScript = null;
		}
	}
	
	@Override
	protected void onDetach() {
		repoModel.detach();
		allowToAddCommentModel.detach();
		commentsModel.detach();
		
		super.onDetach();
	}
	
	@Override
	public InlineContext getInlineContext(InlineComment comment) {
		int oldLine = -1;
		if (comment.getCommit().equals(change.getOldRevision())) {
			oldLine = comment.getLine();
		} else {
			for (Map.Entry<Integer, List<InlineComment>> entry: commentSupport.getOldComments().entrySet()) {
				if (entry.getValue().contains(comment)) {
					oldLine = entry.getKey();
					break;
				}
			}
		}
				
		int newLine = -1;
		if (comment.getCommit().equals(change.getNewRevision())) {
			newLine = comment.getLine();
		} else {
			for (Map.Entry<Integer, List<InlineComment>> entry: commentSupport.getNewComments().entrySet()) {
				if (entry.getValue().contains(comment)
					|| comment.getCommit().equals(change.getNewRevision()) && comment.getLine() == entry.getKey()) {
					newLine = entry.getKey();
					break;
				}
			}
		}
		List<DiffLine> contextDiffs = new ArrayList<>();
		int index = -1;
		for (int i=0; i<diffs.size(); i++) {
			DiffLine diff = diffs.get(i);
			if (diff.getOldLineNo() == oldLine || diff.getNewLineNo() == newLine) {
				index = i;
				break;
			}
		}
		
		Preconditions.checkState(index != -1);
		
		int start = index - InlineComment.CONTEXT_SIZE;
		if (start < 0)
			start = 0;
		int end = index + InlineComment.CONTEXT_SIZE;
		if (end > diffs.size() - 1)
			end = diffs.size() - 1;
		
		for (int i=start; i<=end; i++)
			contextDiffs.add(diffs.get(i));
		
		return new InlineContext(contextDiffs, index-start, start>0, end<diffs.size()-1);
	}

}
