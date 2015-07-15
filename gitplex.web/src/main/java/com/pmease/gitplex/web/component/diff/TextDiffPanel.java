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

import jersey.repackaged.com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
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
import org.apache.wicket.util.time.Duration;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.BlobText;
import com.pmease.commons.git.Change;
import com.pmease.commons.util.diff.AroundContext;
import com.pmease.commons.util.diff.DiffHunk;
import com.pmease.commons.util.diff.DiffLine;
import com.pmease.commons.util.diff.DiffUtils;
import com.pmease.commons.util.diff.Token;
import com.pmease.commons.util.diff.WordSplitter;
import com.pmease.commons.wicket.behavior.DirtyIgnoreBehavior;
import com.pmease.commons.wicket.behavior.ScrollBehavior;
import com.pmease.commons.wicket.behavior.StickyBehavior;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.behavior.menu.CheckBoxItem;
import com.pmease.commons.wicket.behavior.menu.MenuBehavior;
import com.pmease.commons.wicket.behavior.menu.MenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.InlineComment;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.component.avatar.AvatarMode;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.comment.CommentPanel;
import com.pmease.gitplex.web.component.comment.event.CommentRemoved;
import com.pmease.gitplex.web.component.userlink.UserLink;
import com.pmease.gitplex.web.model.UserModel;

@SuppressWarnings("serial")
public class TextDiffPanel extends Panel {

	private enum DiffOption {IGNORE_NOTHING, IGNORE_EOL, IGNORE_EOL_SPACES, IGNORE_CHANGE_SPACES};
	
	private static final int SCROLL_MARGIN = 100;
	
	private static final int CONTEXT_SIZE = 5;

	private final IModel<Repository> repoModel;
	
	private final Change change;
	
	private final InlineCommentSupport commentSupport;
	
	private final IModel<Boolean> allowToAddCommentModel;
	
	private BlobText oldText;
	
	private BlobText newText;
	
	private DiffOption diffOption = DiffOption.IGNORE_NOTHING;
	
	private boolean showComments = true;
	
	private int index;
	
	private List<DiffLine> diffs;
	
	private List<DiffHunk> hunks;
	
	private boolean identical;

	private final IModel<Map<Integer, List<InlineComment>>> commentsModel;
	
	private WebMarkupContainer head;
	
	private ListView<DiffHunk> hunksView;
	
	private RepeatingView commentsView;
	
	private AbstractDefaultAjaxBehavior addCommentBehavior;
	
	private String autoScrollScript;
	
	public TextDiffPanel(String id, final IModel<Repository> repoModel, Change change, 
			final @Nullable InlineCommentSupport commentSupport) {
		super(id);
		
		this.repoModel = repoModel;
		
		this.change = change;
		this.commentSupport = commentSupport;
		
		// cache add comment permission check in model to avoid recalculation for every line
		allowToAddCommentModel = new LoadableDetachableModel<Boolean>() {
	
			@Override
			protected Boolean load() {
				User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
				ObjectPermission readPermission = ObjectPermission.ofRepoPull(repoModel.getObject());
				return currentUser != null && SecurityUtils.getSubject().isPermitted(readPermission);
			}
			
		};
		
		commentsModel = new LoadableDetachableModel<Map<Integer, List<InlineComment>>>() {

			@Override
			protected Map<Integer, List<InlineComment>> load() {
				if (commentSupport == null)
					return new HashMap<>();
				
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
		
		onDiffOptionChanged();
	}
	
	private void onDiffOptionChanged() {
		if (diffOption == DiffOption.IGNORE_EOL) {
			oldText = readOldText().ignoreEOL();
			newText = readNewText().ignoreEOL();
		} else if (diffOption == DiffOption.IGNORE_EOL_SPACES) {
			oldText = readOldText().ignoreEOLSpaces();
			newText = readNewText().ignoreEOLSpaces();
		} else if (diffOption == DiffOption.IGNORE_CHANGE_SPACES) {
			oldText = readOldText().ignoreChangeSpaces();
			newText = readNewText().ignoreChangeSpaces();
		} else {
			oldText = readOldText();
			newText = readNewText();
		}
		
		diffs = DiffUtils.diff(oldText.getLines(), newText.getLines(), new WordSplitter());
		
		identical = true;
		for (DiffLine diffLine: diffs) {
			if (diffLine.getAction() != EQUAL) {
				identical = false;
				break;
			}
		}
		
		if (!identical) {
			if (commentSupport != null) {
				hunks = DiffUtils.hunksOf(diffs, commentSupport.getOldComments().keySet(), 
						commentSupport.getNewComments().keySet(), CONTEXT_SIZE);
			} else {
				hunks = DiffUtils.hunksOf(diffs, CONTEXT_SIZE);
			}
		} else {
			hunks = new ArrayList<>();
			hunks.add(new DiffHunk(0, 0, diffs));
		}
	}

	private boolean isDisplayingFull() {
		int lines = 0;
		for (DiffHunk hunk: hunks)
			lines += hunk.getDiffLines().size();
		
		return lines == diffs.size();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		head = new WebMarkupContainer("head");
		
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
				setVisible(!identical);
			}
			
		});
		add(head);
		
		List<String> alerts = new ArrayList<>();
		if (change.getOldPath() != null && change.getNewPath() != null 
				&& !oldText.getCharset().equals(newText.getCharset()))
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
				setVisible(identical);
			}
			
		});
		
		WebMarkupContainer diffNavs = new WebMarkupContainer("diffNavs") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!identical && isDisplayingFull());
			}
			
		};
		head.add(diffNavs);
		
		diffNavs.add(new WebMarkupContainer("prevDiff").add(new ScrollBehavior(".diff-block", SCROLL_MARGIN, false)));
		diffNavs.add(new WebMarkupContainer("nextDiff").add(new ScrollBehavior(".diff-block", SCROLL_MARGIN, true)));

		WebMarkupContainer commentNavs = new WebMarkupContainer("commentNavs") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!commentsModel.getObject().isEmpty());
			}

		};
		head.add(commentNavs);
		
		commentNavs.add(new WebMarkupContainer("prevComment") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentSupport != null && showComments && isDisplayingFull());
			}
			
		}.add(new ScrollBehavior("table.comments>tbody>tr", SCROLL_MARGIN, false)));
		
		commentNavs.add(new WebMarkupContainer("nextComment") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentSupport != null && showComments && isDisplayingFull());
			}
			
		}.add(new ScrollBehavior("table.comments>tbody>tr", SCROLL_MARGIN, true)));
		
		commentNavs.add(new AjaxLink<Void>("showComments") {

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
		commentNavs.add(new AjaxLink<Void>("hideComments") {

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
		
		head.add(new AjaxLink<Void>("displayFull") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				hunks = new ArrayList<>();
				hunks.add(new DiffHunk(0, 0, diffs));
				target.add(TextDiffPanel.this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!isDisplayingFull());
			}
			
		});

		MenuPanel diffOptionMenuPanel = new MenuPanel("diffOptions") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> menuItems = new ArrayList<>();
				
				menuItems.add(new CheckBoxItem() {
					
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						onDiffOptionChanged();
						hide(target);
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
				
				menuItems.add(new CheckBoxItem() {
					
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						onDiffOptionChanged();
						hide(target);
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

				menuItems.add(new CheckBoxItem() {
					
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						onDiffOptionChanged();
						hide(target);
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
		form.add(new FeedbackPanel("feedback", input).hideAfter(Duration.seconds(5)));
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				input.setModelObject("");
				target.appendJavaScript(String.format("gitplex.comments.cancelAdd(%d);", index));
			}
			
		}.add(new DirtyIgnoreBehavior()));
		
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
				BlobIdent commentAt;
				BlobIdent compareWith;
				int lineNo;
				AroundContext commentContext; 
				
				if (diff.getAction() == DELETE) {
					commentAt = change.getOldBlobInfo();
					compareWith = change.getNewBlobInfo();
					lineNo = diff.getOldLineNo();
					commentContext = DiffUtils.around(diffs, lineNo, -1, InlineComment.CONTEXT_SIZE); 
				} else {
					commentAt = change.getNewBlobInfo();
					compareWith = change.getOldBlobInfo();
					lineNo = diff.getNewLineNo();
					commentContext = DiffUtils.around(diffs, -1, lineNo, InlineComment.CONTEXT_SIZE); 
				}
				
				commentSupport.addComment(commentAt, compareWith, commentContext, lineNo, input.getModelObject());
				
				Component commentsRow = newCommentsRow(commentsView.newChildId(), index);
				commentsView.add(commentsRow);
				target.add(commentsRow);
				
				input.setModelObject("");
				String prependScript = String.format("$('#comments-placeholder').append('<table id=\"%s\"></table>')", 
						commentsRow.getMarkupId());
				target.prependJavaScript(prependScript);
				target.appendJavaScript(String.format("gitplex.comments.afterAdd(%d);", index));
				
				if (commentsView.size() == 1)
					target.add(head);
			}

		}.add(new DirtyIgnoreBehavior()));
		add(form);
		
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
		
		add(hunksView = new ListView<DiffHunk>("hunks", new AbstractReadOnlyModel<List<DiffHunk>>() {

			@Override
			public List<DiffHunk> getObject() {
				return hunks;
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<DiffHunk> item) {
				DiffHunk hunk = item.getModelObject();
				
				item.add(newHunkHead("head", item.getIndex()));
				item.add(new Label("body", renderDiffs(hunk.getDiffLines())).setEscapeModelStrings(false));
			}

		});
		
		final WebMarkupContainer lastRow = new WebMarkupContainer("lastRow") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				if (hunks.isEmpty()) {
					setVisible(false);
				} else {
					int lines = 0;
					for (DiffLine diffLine: diffs) {
						if (diffLine.getAction() != DiffLine.Action.DELETE)
							lines++;
					}
					DiffHunk hunk = hunks.get(hunks.size()-1);
					setVisible(hunk.getNewEnd() < lines);
				}
			}
			
		};
		lastRow.setOutputMarkupId(true);
		add(lastRow);
		lastRow.add(new AjaxLink<Void>("expand") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				expandBelow(target, hunks.size()-1, diffs.size());
				target.add(findPrevVisibleHunkHead(hunks.size()));
				target.add(lastRow);
				target.add(head);
			}
			
		}.add(new TooltipBehavior(Model.of("Show more lines"))));

		add(newCommentsView());
		
		autoScrollScript = String.format("pmease.commons.scroll.next('table.comments>tbody>tr.concerned', %d);", SCROLL_MARGIN);
	}
	
	private String renderDiffs(List<DiffLine> diffLines) {
		if (diffLines.isEmpty())
			return "";
		
		StringBuilder builder = new StringBuilder();
		
		DiffLine firstLine = diffLines.get(0);
		int index = 0;
		for (DiffLine diff: diffs) {
			if (diff.getOldLineNo() == firstLine.getOldLineNo() && diff.getNewLineNo() == firstLine.getNewLineNo())
				break;
			index++;
		}
		
		for (DiffLine line: diffLines) {
			String addCommentLink;
			if (allowToAddCommentModel.getObject() && commentSupport != null) {
				addCommentLink = "<a href='javascript: var index=" + index + "; " 
					+ addCommentBehavior.getCallbackScript() 
					+ ";' class='add-comment' title='Add comment'><i class='fa fa-comments'></i></a>";
			} else {
				addCommentLink = "";
			}
			builder.append("<tr id='diffline-").append(index).append("' class='line diff ");
			if (line.getAction() == ADD) {
				if (index == 0 || diffs.get(index-1).getAction() == EQUAL)
					builder.append("new diff-block'>");
				else
					builder.append("new'>");
				builder.append("<td class='old line-no'>").append(addCommentLink).append("</td>");
				builder.append("<td class='new line-no'>");
				builder.append(line.getNewLineNo()+1).append("</td>");
			} else if (line.getAction() == DELETE) {
				if (index == 0 || diffs.get(index-1).getAction() == EQUAL)
					builder.append("old diff-block'>");
				else
					builder.append("old'>");
				builder.append("<td class='old line-no'>").append(addCommentLink);
				builder.append(line.getOldLineNo()+1).append("</td>");
				builder.append("<td class='new line-no'></td>");
			} else {
				builder.append("equal'>");
				builder.append("<td class='old line-no'>").append(addCommentLink)
						.append("  ").append(line.getOldLineNo()+1).append("</td>");
				builder.append("<td class='new line-no'>  ").append(line.getNewLineNo()+1).append("</td>");
			}
			builder.append("<td class='text'>");
			if (line.getAction() == ADD)
				builder.append("+");
			else if (line.getAction() == DELETE)
				builder.append("-");
			else
				builder.append("&nbsp;");
			for (Token partial: line.getTokens()) {
				if (partial.isEmphasized())
					builder.append("<span class='emphasize'>");
				else
					builder.append("<span>");
				String content = StringUtils.replaceChars(partial.getContent(), '\r', ' ');
				builder.append(Strings.escapeMarkup(content, false, false));
				builder.append("</span>");
			}
			builder.append("</td></tr>");
			index++;
		}
		return builder.toString();
	}
	
	private WebMarkupContainer newHunkHead(String id, final int index) {
		final DiffHunk hunk = hunks.get(index);
		final WebMarkupContainer hunkHead = new WebMarkupContainer(id) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(index == 0 || hunk.getNewStart() > hunks.get(index-1).getNewEnd());
			}
			
		};
		hunkHead.add(new AjaxLink<Void>("expand") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (index == 0) {
					expandAbove(target, index, 0);
					target.add(hunkHead);
				} else {
					int diffPos = locateDiffPos(hunk.getNewStart(), DiffLine.Action.DELETE);
					expandBelow(target, index-1, diffPos);
					
					diffPos = locateDiffPos(hunks.get(index-1).getNewEnd(), DiffLine.Action.DELETE);
					expandAbove(target, index, diffPos);
					
					target.add(hunkHead);
					target.add(findPrevVisibleHunkHead(index));
				}
				target.add(head);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				if (index == 0) {
					setVisible(hunk.getOldStart() != 0 && hunk.getNewStart() != 0);
				} else {
					DiffHunk previousHunk = hunks.get(index-1);
					setVisible(previousHunk.getNewEnd() < hunk.getNewStart());
				}
			}
			
		}.add(new TooltipBehavior(Model.of("Show more lines"))));
		
		hunkHead.add(new Label("content", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				int oldStart = hunk.getOldStart();
				int newStart = hunk.getNewStart();
				int oldEnd = hunk.getOldEnd();
				int newEnd = hunk.getNewEnd();
				for (int i=index+1; i<hunks.size(); i++) {
					DiffHunk nextHunk = hunks.get(i);
					if (newEnd == nextHunk.getNewStart()) {
						newEnd = nextHunk.getNewEnd();
						oldEnd = nextHunk.getOldEnd();
					} else {
						break;
					}
				}
				return DiffHunk.describe(oldStart, newStart, oldEnd, newEnd);
			}
			
		}));
		
		hunkHead.setOutputMarkupId(true);
		
		return hunkHead;
	}

	private Component newCommentsRow(String id, final int index) {
		WebMarkupContainer row = new WebMarkupContainer(commentsView.newChildId()) {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				
				if (event.getPayload() instanceof CommentRemoved) {
					CommentRemoved commentRemoved = (CommentRemoved) event.getPayload();
					commentsView.remove(this);
					commentsModel.getObject().get(index).remove(commentRemoved.getComment());
					commentRemoved.getTarget().appendJavaScript(String.format("gitplex.comments.afterRemove(%d);", index));
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
				item.add(new CommentPanel("comment", item.getModel()).setOutputMarkupId(true));
				if (item.getModelObject().equals(commentSupport.getConcernedComment()))
					item.add(AttributeAppender.append("class", " concerned"));
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
		target.add(head);

		target.appendJavaScript("$('.comments.line').show();");
	}

	private void hideComments(AjaxRequestTarget target) {
		showComments = false;
		target.add(head);
		
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
			response.render(OnLoadHeaderItem.forScript(autoScrollScript));
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
	
	private BlobText readOldText() {
		if (change.getOldPath() != null) 
			return Preconditions.checkNotNull(repoModel.getObject().getBlobText(change.getOldBlobInfo()));
		else
			return new BlobText();
	}
	
	private BlobText readNewText() {
		if (change.getNewPath() != null) 
			return Preconditions.checkNotNull(repoModel.getObject().getBlobText(change.getNewBlobInfo()));
		else
			return new BlobText();
	}
	
	private int locateDiffPos(int lineNo, DiffLine.Action excludeAction) {
		int index = 0;
		for (int i=0; i<diffs.size(); i++) {
			if (index < lineNo) {
				if (diffs.get(i).getAction() != excludeAction)
					index++;
			} else {
				return i;
			}
		}
		throw new IllegalStateException();
	}
	
	private void expandBelow(AjaxRequestTarget target, int hunkIndex, int diffLimit) {
		DiffHunk hunk = hunks.get(hunkIndex);
		
		int diffPos = locateDiffPos(hunk.getNewEnd(), DiffLine.Action.DELETE);
		for (int i = diffPos; i<diffs.size(); i++) {
			if (i-diffPos < CONTEXT_SIZE*2 && i<diffLimit) {
				DiffLine line = diffs.get(i);
				hunk.getDiffLines().add(line);
				hunk.setNewEnd(hunk.getNewEnd()+1);
				hunk.setOldEnd(hunk.getOldEnd()+1);
				
				String row = renderDiffs(Lists.newArrayList(line));
				row = StringUtils.replace(row, "\"", "\\\"");
				String script = String.format("$(\"%s\").insertAfter('#diffline-%d')", row, i-1);
				target.appendJavaScript(script);
			} else {
				break;
			}			
		}
	}
	
	private Component findPrevVisibleHunkHead(int index) {
		for (int i=index-1; i>=0; i--) {
			Component hunkHead = hunksView.get(i).get("head");
			hunkHead.configure();
			if (hunkHead.isVisible())
				return hunkHead;
		}
		throw new IllegalStateException();
	}
	
	private void expandAbove(AjaxRequestTarget target, int hunkIndex, int diffLimit) {
		DiffHunk hunk = hunks.get(hunkIndex);
		int diffPos = locateDiffPos(hunk.getNewStart(), DiffLine.Action.DELETE);
		for (int i=diffPos-1; i>=diffLimit; i--) {
			if (diffPos-i <= CONTEXT_SIZE*2) {
				hunk.setOldStart(hunk.getOldStart()-1);
				hunk.setNewStart(hunk.getNewStart()-1);
				DiffLine line = diffs.get(i);
				hunk.getDiffLines().add(0, line);
				String row = renderDiffs(Lists.newArrayList(line));
				row = StringUtils.replace(row, "\"", "\\\"");
				String script = String.format("$(\"%s\").insertBefore('#diffline-%d')", row, i+1);
				target.appendJavaScript(script);
			} else {
				break;
			}
		}
	}	
}
