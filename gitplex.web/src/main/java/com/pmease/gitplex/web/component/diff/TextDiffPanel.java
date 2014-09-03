package com.pmease.gitplex.web.component.diff;

import static com.pmease.commons.util.diff.DiffLine.Action.ADD;
import static com.pmease.commons.util.diff.DiffLine.Action.DELETE;
import static com.pmease.commons.util.diff.DiffLine.Action.EQUAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jersey.repackaged.com.google.common.base.Splitter;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobText;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.diff.DiffLine;
import com.pmease.commons.util.diff.DiffUtils;
import com.pmease.commons.util.diff.Partial;
import com.pmease.commons.util.diff.WordSplitter;
import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.commons.wicket.behavior.ScrollBehavior;
import com.pmease.commons.wicket.behavior.StickyBehavior;
import com.pmease.commons.wicket.behavior.menu.CheckMenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuBehavior;
import com.pmease.commons.wicket.behavior.menu.MenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.CommentAwareChange;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.manager.CommentVisitManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.CommitComment;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.markdown.MarkdownPanel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.event.CommitCommentRemoved;
import com.pmease.gitplex.web.model.UserModel;

@SuppressWarnings("serial")
public class TextDiffPanel extends Panel {

	private enum DiffOption {IGNORE_NOTHING, IGNORE_EOL, IGNORE_EOL_SPACES, IGNORE_CHANGE_SPACES};
	
	private static final int SCROLL_MARGIN = 50;
	
	private static final String COMMENTS_ROW_ID = "commentsRow";
	
	private static final String CONTENT_ROW_ID = "contentRow";
	
	private static final String COMMENT_ACTIONS_ID = "commentActions";
	
	private static final String NEW_COMMENT_ID = "newComment";
	
	private static final String HEAD_ID = "head";
	
	private final IModel<Repository> repoModel;
	
	private final BlobText oldText;
	
	private final BlobText newText;
	
	private final CommentAwareChange change;
	
	private final IModel<Boolean> allowToAddCommentModel;
	
	private BlobText effectiveOldText;
	
	private BlobText effectiveNewText;
	
	private DiffOption diffOption = DiffOption.IGNORE_NOTHING;
	
	private boolean showComments = true;
	
	private List<DiffLine> diffs;
	
	private String autoScrollScript;

	public TextDiffPanel(String id, final IModel<Repository> repoModel, 
			BlobText oldText, BlobText newText, CommentAwareChange change) {
		super(id);
		
		this.repoModel = repoModel;
		
		Preconditions.checkArgument(!change.getOldRevision().equals(change.getNewRevision()));
		
		this.change = change;
		
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
				setVisible((getOldComments() != null || getNewComments() != null) && showComments);
			}
			
		}.add(new ScrollBehavior(".comments.line", 50, false)));
		
		commentActions.add(new WebMarkupContainer("nextComment") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible((getOldComments() != null || getNewComments() != null) && showComments);
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
				setVisible((getOldComments() != null || getNewComments() != null) && !showComments);
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
				setVisible((getOldComments() != null || getNewComments() != null) && showComments);
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
		
		add(new ListView<DiffLine>("lines", new AbstractReadOnlyModel<List<DiffLine>>() {

			@Override
			public List<DiffLine> getObject() {
				return diffs;
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<DiffLine> lineItem) {
				final DiffLine diffLine = lineItem.getModelObject();
				final WebMarkupContainer contentRow = new WebMarkupContainer(CONTENT_ROW_ID);
				
				final IModel<List<CommitComment>> lineCommentsModel = new LoadableDetachableModel<List<CommitComment>>() {

					@Override
					protected List<CommitComment> load() {
						return getLineComments(diffLine);
					}
					
				};
				
				final WebMarkupContainer commentsRow = new WebMarkupContainer(COMMENTS_ROW_ID) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						if (showComments) {
							if (isAddingComment(this))
								setVisible(true);
							else 
								setVisible(!lineCommentsModel.getObject().isEmpty());
						} else {
							setVisible(false);
						}
					}
					
				};
				
				commentsRow.setOutputMarkupId(true);
				commentsRow.add(new Label("commentCount", new AbstractReadOnlyModel<Integer> () {

					@Override
					public Integer getObject() {
						return lineCommentsModel.getObject().size();
					}
					
				}));
				commentsRow.add(new WebMarkupContainer("concernedFlag") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						setVisible(lineCommentsModel.getObject().contains(change.getConcernedComment()));
					}
					
				});
				
				commentsRow.add(new ListView<CommitComment>("comments", lineCommentsModel) {

					@Override
					protected void populateItem(final ListItem<CommitComment> commentItem) {
						final CommitComment comment = commentItem.getModelObject();
						commentItem.add(new UserLink("avatar", new UserModel(comment.getUser()), AvatarMode.AVATAR));
						commentItem.add(new UserLink("name", new UserModel(comment.getUser()), AvatarMode.NAME));
						
						commentItem.add(new AgeLabel("age", new AbstractReadOnlyModel<Date>() {

							@Override
							public Date getObject() {
								return commentItem.getModelObject().getCommentDate();
							}
							
						}));
						
						commentItem.add(new AddCommentLink("reply", lineItem, comment));
						
						commentItem.add(new AjaxLink<Void>("edit") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								Fragment fragment = new Fragment("content", "editCommentFrag", TextDiffPanel.this);
								fragment.setOutputMarkupId(true);
								Form<?> form = new Form<Void>("form");
								form.setOutputMarkupId(true);
								final CommentInput input;
								form.add(input = new CommentInput("input", Model.of(commentItem.getModelObject().getContent())));
								form.add(new AjaxLink<Void>("cancel") {

									@Override
									public void onClick(AjaxRequestTarget target) {
										target.add(commentsRow);
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
										
										CommitComment comment = commentItem.getModelObject();
										comment.setCommentDate(new Date());
										comment.setContent(input.getModelObject());
										
										GitPlex.getInstance(Dao.class).persist(comment);
										target.add(commentsRow);
									}

								});
								fragment.add(form);
								commentItem.replace(fragment);
								
								target.add(fragment);
							}

							@Override
							protected void onConfigure() {
								super.onConfigure();

								AuthorizationManager manager = GitPlex.getInstance(AuthorizationManager.class);
								setVisible(manager.canModify(commentItem.getModelObject()));
							}
							
						});
						commentItem.add(new AjaxLink<Void>("delete") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								GitPlex.getInstance(Dao.class).remove(comment);
								if (getOldComments() != null) {
									List<CommitComment> lineComments = getOldComments().get(diffLine.getOldLineNo());
									if (lineComments != null) {
										lineComments.remove(comment);
										if (lineComments.isEmpty())
											getOldComments().remove(diffLine.getOldLineNo());
									}
								}
								if (getNewComments() != null) {
									List<CommitComment> lineComments = getNewComments().get(diffLine.getNewLineNo());
									if (lineComments != null) {
										lineComments.remove(comment);
										if (lineComments.isEmpty())
											getOldComments().remove(diffLine.getNewLineNo());
									}
								}
								target.add(commentsRow);
								send(getPage(), Broadcast.BUBBLE, new CommitCommentRemoved(target, comment));
							}
							
							@Override
							protected void onConfigure() {
								super.onConfigure();

								setVisible(GitPlex.getInstance(AuthorizationManager.class).canModify(commentItem.getModelObject()));
							}

						}.add(new ConfirmBehavior("Do you really want to delete this comment?")));
						
						commentItem.add(new MarkdownPanel("content", Model.of(comment.getContent())));
					}
					
				});

				commentsRow.add(new AddCommentLink("addComment", lineItem, null) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						if (isVisible() && isAddingComment(commentsRow))
							setVisible(false);
					}
					
				});
				
				commentsRow.add(new WebMarkupContainer(NEW_COMMENT_ID));
				
				lineItem.add(commentsRow);
				
				contentRow.add(new AddCommentLink("addComment", lineItem, null));
				
				if (diffLine.getAction() == ADD) {
					if (lineItem.getIndex() == 0 || diffs.get(lineItem.getIndex()-1).getAction() == EQUAL)
						contentRow.add(AttributeAppender.append("class", " new diff-block"));
					else
						contentRow.add(AttributeAppender.append("class", " new"));
					contentRow.add(new Label("oldLineNo"));
					contentRow.add(new Label("newLineNo", "+ " + (diffLine.getNewLineNo()+1)));
				} else if (diffLine.getAction() == DELETE) {
					if (lineItem.getIndex() == 0 || diffs.get(lineItem.getIndex()-1).getAction() == EQUAL)
						contentRow.add(AttributeAppender.append("class", " old diff-block"));
					else
						contentRow.add(AttributeAppender.append("class", " old"));
					contentRow.add(new Label("oldLineNo", "- " + (diffLine.getOldLineNo()+1)));
					contentRow.add(new Label("newLineNo"));
				} else {
					contentRow.add(AttributeAppender.append("class", " equal"));
					contentRow.add(new Label("oldLineNo", "  " + (diffLine.getOldLineNo()+1)));
					contentRow.add(new Label("newLineNo", "  " + (diffLine.getNewLineNo()+1)));
				}
				contentRow.add(new ListView<Partial>("partials", diffLine.getPartials()) {

					@Override
					protected void populateItem(ListItem<Partial> item) {
						Partial partial = item.getModelObject();
						Label label;
						if (partial.getContent().equals("\r"))
							label = new Label("partial", " ");
						else
							label = new Label("partial", partial.getContent());
						if (partial.isEmphasized())
							label.add(AttributeAppender.append("class", "emphasize"));
						item.add(label);
					}
					
				});
				contentRow.setOutputMarkupId(true);
				
				lineItem.add(contentRow);
			}
			
		});
		
		String cssClass;
		if (change.contains(change.getConcernedComment()))
			cssClass = ".concerned-comments";
		else
			cssClass = ".diff-block:first";
		autoScrollScript = String.format("pmease.commons.scroll.next('%s', %d);", cssClass, SCROLL_MARGIN);
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
	
	private boolean isAddingComment(WebMarkupContainer commentsRow) {
		return commentsRow.get(NEW_COMMENT_ID) instanceof Fragment;
	}

	private List<CommitComment> getLineComments(DiffLine line) {
		List<CommitComment> lineComments = new ArrayList<>();
		if (getOldComments() != null 
				&& getOldComments().containsKey(line.getOldLineNo()) 
				&& line.getAction() != ADD) {
			lineComments.addAll(getOldComments().get(line.getOldLineNo()));
		}
		if (getNewComments() != null 
				&& getNewComments().containsKey(line.getNewLineNo())
				&& line.getAction() != DELETE) {
			lineComments.addAll(getNewComments().get(line.getNewLineNo()));
		}
		Collections.sort(lineComments);
		return lineComments;
	}
	
	private class AddCommentLink extends AjaxLink<Void> {
		
		private final ListItem<DiffLine> item;
		
		private final CommitComment replyComment;
		
		public AddCommentLink(String id, ListItem<DiffLine> item, CommitComment replyComment) {
			super(id);
			this.item = item;
			this.replyComment = replyComment;
		}
		
		@Override
		public void onClick(AjaxRequestTarget target) {
			if (!showComments)
				showComments(target);
			
			Fragment frag = new Fragment(NEW_COMMENT_ID, "newCommentFrag", TextDiffPanel.this);
			Form<?> form = new Form<Void>("form");
			form.setOutputMarkupId(true);
			
			String content = "";
			if (replyComment != null) {
				for (String line: Splitter.on('\n').split(replyComment.getContent()))
					content += "> " + line + "\n";
			}
			
			final CommentInput input;
			form.add(input = new CommentInput("input", Model.of(content)));
			
			final WebMarkupContainer contentRow = (WebMarkupContainer) item.get(CONTENT_ROW_ID);
			final WebMarkupContainer commentsRow = (WebMarkupContainer) item.get(COMMENTS_ROW_ID);
			final DiffLine diffLine = item.getModelObject();
			
			form.add(new AjaxLink<Void>("cancel") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					commentsRow.replace(new WebMarkupContainer(NEW_COMMENT_ID));
					target.add(commentsRow);
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
					CommitComment comment = new CommitComment();
					comment.setRepository(repoModel.getObject());
					User currentUser = Preconditions.checkNotNull(GitPlex.getInstance(UserManager.class).getCurrent());
					comment.setUser(currentUser);
					String commit;
					CommentPosition position;
					if (replyComment != null) {
						commit = replyComment.getCommit();
						position = replyComment.getPosition();
					} else {
						List<CommitComment> comments = getLineComments(diffLine);
						if (!comments.isEmpty()) {
							CommitComment lastComment = comments.get(comments.size()-1);
							commit = lastComment.getCommit();
							position = lastComment.getPosition();
						} else if (diffLine.getAction() == DELETE) {
							commit = change.getOldRevision();
							position = new CommentPosition(change.getOldPath(), diffLine.getOldLineNo());
						} else if (diffLine.getAction() == ADD) {
							commit = change.getNewRevision();
							position = new CommentPosition(change.getNewPath(), diffLine.getNewLineNo());
						} else if (getNewComments() != null) {
							commit = change.getNewRevision();
							position = new CommentPosition(change.getNewPath(), diffLine.getNewLineNo());
						} else {
							commit = change.getOldRevision();
							position = new CommentPosition(change.getOldPath(), diffLine.getOldLineNo());
						}							
					} 
					
					comment.setCommit(commit);
					comment.setPosition(position);
					comment.setContent(input.getModelObject());
					comment.setCommentDate(new Date());
					comment.setCommitDate(repoModel.getObject().getCommit(commit).getCommitter().getWhen());
					
					change.saveComment(comment);

					GitPlex.getInstance(CommentVisitManager.class).visitComment(repoModel.getObject(), 
							currentUser, comment);
					
					commentsRow.replace(new WebMarkupContainer(NEW_COMMENT_ID));
					target.add(commentsRow);
				}

			});
			frag.add(form);
			commentsRow.replace(frag);
			if (!commentsRow.isVisible()) {
				String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
						commentsRow.getMarkupId(), contentRow.getMarkupId());
				target.prependJavaScript(script);
				commentsRow.setVisible(true);
			} 
			target.add(commentsRow);
		}
		
		@Override
		protected void onConfigure() {
			super.onConfigure();
			
			if (!allowToAddCommentModel.getObject()) {
				setVisible(false);
			} else {
				DiffLine.Action action = item.getModelObject().getAction();
				if (action == DELETE) 
					setVisible(getOldComments() != null);
				else if (action == ADD)
					setVisible(getNewComments() != null);
				else 
					setVisible(getOldComments() != null || getNewComments() != null);
			}
		}

	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		// only auto-scroll this panel once after it is created; otherwise page refreshing 
		// caused by actions such as integrate/discard on request compare page will also 
		// get scrolled
		if (autoScrollScript != null) {
			response.render(OnDomReadyHeaderItem.forScript(autoScrollScript));
			autoScrollScript = null;
		}
	}

	private Map<Integer, List<CommitComment>> getOldComments() {
		return change.getOldComments();
	}
	
	private Map<Integer, List<CommitComment>> getNewComments() {
		return change.getNewComments();
	}
	
	@Override
	protected void onDetach() {
		repoModel.detach();
		allowToAddCommentModel.detach();
		
		super.onDetach();
	}
	
}
