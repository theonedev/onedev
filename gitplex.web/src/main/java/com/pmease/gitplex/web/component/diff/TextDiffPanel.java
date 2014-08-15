package com.pmease.gitplex.web.component.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
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
import org.apache.wicket.model.PropertyModel;

import com.pmease.commons.git.GitText;
import com.pmease.commons.git.GitUtils;
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
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.manager.CommitCommentManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.CommitComment;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;

@SuppressWarnings("serial")
public class TextDiffPanel extends Panel {

	private enum DiffOption {IGNORE_NOTHING, IGNORE_EOL, IGNORE_EOL_SPACES, IGNORE_CHANGE_SPACES};
	
	private static final int SCROLL_MARGIN = 50;
	
	private final IModel<Repository> repoModel;
	
	private final BlobDiffInfo diffInfo;
	
	private final GitText oldText;
	
	private final GitText newText;
	
	private final IModel<Map<Integer, List<CommitComment>>> oldCommentsModel;
	
	private final IModel<Map<Integer, List<CommitComment>>> newCommentsModel;
	
	private GitText effectiveOldText;
	
	private GitText effectiveNewText;
	
	private DiffOption diffOption = DiffOption.IGNORE_NOTHING;
	
	private List<DiffLine> diffs;

	public TextDiffPanel(String id, IModel<Repository> repoModel, 
			BlobDiffInfo diffInfo, GitText oldText, GitText newText) {
		super(id);
		
		this.repoModel = repoModel;
		
		String oldCommitHash = diffInfo.getOldRevision();
		if (!GitUtils.isHash(oldCommitHash))
			oldCommitHash = repoModel.getObject().git().parseRevision(oldCommitHash, true);
		String newCommitHash = diffInfo.getNewRevision();
		if (!GitUtils.isHash(newCommitHash))
			newCommitHash = repoModel.getObject().git().parseRevision(newCommitHash, true);
		
		this.diffInfo = new BlobDiffInfo(diffInfo.getStatus(), diffInfo.getOldPath(), diffInfo.getNewPath(), 
				diffInfo.getOldMode(), diffInfo.getNewMode(), oldCommitHash, newCommitHash);
		
		this.oldText = oldText;
		this.newText = newText;
		
		oldCommentsModel = new LoadableDetachableModel<Map<Integer, List<CommitComment>>>() {

			@Override
			protected Map<Integer, List<CommitComment>> load() {
				List<CommitComment> comments = GitPlex.getInstance(CommitCommentManager.class).findByCommitAndFile(
						TextDiffPanel.this.repoModel.getObject(), 
						TextDiffPanel.this.diffInfo.getOldRevision(), 
						TextDiffPanel.this.diffInfo.getOldPath());
				return mapCommentsToLines(comments);
			}
			
		};
		
		newCommentsModel = new LoadableDetachableModel<Map<Integer, List<CommitComment>>>() {

			@Override
			protected Map<Integer, List<CommitComment>> load() {
				List<CommitComment> comments = GitPlex.getInstance(CommitCommentManager.class).findByCommitAndFile(
						TextDiffPanel.this.repoModel.getObject(), 
						TextDiffPanel.this.diffInfo.getNewRevision(), 
						TextDiffPanel.this.diffInfo.getNewPath());
				return mapCommentsToLines(comments);
			}
			
		};

		onDiffOptionChanged();
	}
	
	private Map<Integer, List<CommitComment>> mapCommentsToLines(List<CommitComment> comments) {
		Map<Integer, List<CommitComment>> map = new HashMap<>();
		for (CommitComment comment: comments) {
			List<CommitComment> lineComments = map.get(comment.getLine());
			if (lineComments == null) {
				lineComments = new ArrayList<>();
				map.put(comment.getLine(), lineComments);
			}
			lineComments.add(comment);
		}
		return map;
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
			if (diffLine.getAction() != DiffLine.Action.EQUAL)
				return false;
		}
		return true;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		WebMarkupContainer head = new WebMarkupContainer("head");
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
		
		head.add(new FileDiffTitle("title", diffInfo, alerts));
		
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
			
		}.add(new ScrollBehavior(head, ".diff-block", SCROLL_MARGIN, false)));

		head.add(new WebMarkupContainer("nextDiff") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!isIdentical());
			}
			
		}.add(new ScrollBehavior(head, ".diff-block", SCROLL_MARGIN, true)));
		
		head.add(new WebMarkupContainer("prevComment").add(new ScrollBehavior(head, ".comments.line", 50, false)));
		head.add(new WebMarkupContainer("nextComment").add(new ScrollBehavior(head, ".comments.line", 50, true)));

		MenuPanel diffOptionMenuPanel = new MenuPanel("diffOptions") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> menuItems = new ArrayList<>();
				
				menuItems.add(new CheckMenuItem() {
					
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						onDiffOptionChanged();
						setResponsePage(getPage());
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
						setResponsePage(getPage());
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
						setResponsePage(getPage());
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

			private int oldLineNo;
			
			private int newLineNo;
			
			@Override
			protected void onBeforeRender() {
				oldLineNo = newLineNo = 0;
				super.onBeforeRender();
			}

			@Override
			protected void populateItem(ListItem<DiffLine> item) {
				final DiffLine diffLine = item.getModelObject();
				final WebMarkupContainer contentRow = new WebMarkupContainer("contentRow");
				
				final WebMarkupContainer commentsRow = new WebMarkupContainer("commentsRow") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						if (!(get("newComment").getClass() == WebMarkupContainer.class))
							setVisible(true);
						else 
							setVisible(((ListView<?>) get("comments")).size() != 0);
					}
					
				};
				commentsRow.setOutputMarkupId(true);
				commentsRow.add(new ListView<CommitComment>("comments", 
						new LoadableDetachableModel<List<CommitComment>>() {

					@Override
					protected List<CommitComment> load() {
						List<CommitComment> oldLineComments = oldCommentsModel.getObject().get(oldLineNo);
						if (oldLineComments == null)
							oldLineComments = new ArrayList<>();
						List<CommitComment> newLineComments = newCommentsModel.getObject().get(newLineNo);
						if (newLineComments == null)
							newLineComments = new ArrayList<>();

						if (diffLine.getAction() == DiffLine.Action.DELETE) {
							return oldLineComments;
						} else if (diffLine.getAction() == DiffLine.Action.ADD) {
							return newLineComments;
						} else {
							List<CommitComment> comments = new ArrayList<>();
							comments.addAll(oldLineComments);
							comments.addAll(newLineComments);
							Collections.sort(comments);
							return comments;
						}
					}
					
				}) {

					@Override
					protected void populateItem(final ListItem<CommitComment> item) {
						CommitComment comment = item.getModelObject();
						
						Fragment fragment = new Fragment("comment", "viewCommentFrag", TextDiffPanel.this);
						item.add(fragment);
						fragment.add(new UserLink("name", new UserModel(comment.getUser()), AvatarMode.NAME));
						
						fragment.add(new AgeLabel("age", new AbstractReadOnlyModel<Date>() {

							@Override
							public Date getObject() {
								return item.getModelObject().getDate();
							}
							
						}));
						
						fragment.add(new AjaxLink<Void>("edit") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								Fragment fragment = new Fragment("comment", "editCommentFrag", TextDiffPanel.this);
								item.replace(fragment);
								Form<?> form = new Form<Void>("form");
								final CommentInput input;
								form.add(input = new CommentInput("input", Model.of(item.getModelObject().getContent())));
								form.add(new AjaxLink<Void>("cancel") {

									@Override
									public void onClick(AjaxRequestTarget target) {
										commentsRow.replace(new WebMarkupContainer("newComment"));
										target.add(commentsRow);
									}
									
								});
								form.add(new AjaxSubmitLink("save") {

									@Override
									protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
										super.onSubmit(target, form);
										
										CommitComment comment = item.getModelObject();
										comment.setDate(new Date());
										comment.setContent(input.getModelObject());
										
										GitPlex.getInstance(Dao.class).persist(comment);
										target.add(commentsRow);
									}

								});
								fragment.add(form);
								target.add(commentsRow);
							}

							@Override
							protected void onConfigure() {
								super.onConfigure();

								setVisible(GitPlex.getInstance(AuthorizationManager.class).canModify(item.getModelObject()));
							}
							
						});
						fragment.add(new AjaxLink<Void>("delete") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								GitPlex.getInstance(Dao.class).remove(item.getModelObject());
								target.add(commentsRow);
							}
							
							@Override
							protected void onConfigure() {
								super.onConfigure();

								setVisible(GitPlex.getInstance(AuthorizationManager.class).canModify(item.getModelObject()));
							}

						}.add(new ConfirmBehavior("Do you really want to delete this comment?")));
						
					}
					
				});

				commentsRow.add(new WebMarkupContainer("newComment"));
				
				item.add(commentsRow);
				
				contentRow.add(new AjaxLink<Void>("addComment") {

					private String newComment;
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						Fragment frag = new Fragment("newComment", "newCommentFrag", TextDiffPanel.this);
						Form<?> form = new Form<Void>("form");
						form.add(new CommentInput("input", new PropertyModel<String>(this, "newComment")));
						
						form.add(new AjaxLink<Void>("cancel") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								commentsRow.replace(new WebMarkupContainer("newComment"));
								target.add(commentsRow);
							}
							
						});
						form.add(new AjaxSubmitLink("save") {

							@Override
							protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
								super.onSubmit(target, form);
								CommitComment comment = new CommitComment();
								comment.setRepository(repoModel.getObject());
								comment.setUser(GitPlex.getInstance(UserManager.class).getCurrent());
								String commit;
								if (diffLine.getAction() == DiffLine.Action.ADD 
										|| diffLine.getAction() == DiffLine.Action.EQUAL) {
									commit = diffInfo.getNewRevision(); 
									comment.setFile(diffInfo.getNewPath());
								} else {
									commit = diffInfo.getOldRevision();
									comment.setFile(diffInfo.getOldPath());
								}
								comment.setCommit(commit);
								comment.setContent(newComment);
								comment.setDate(new Date());
								
								GitPlex.getInstance(Dao.class).persist(comment);
								
								commentsRow.replace(new WebMarkupContainer("newComment"));
								target.add(commentsRow);
							}

						});
						frag.add(form);
						commentsRow.replace(frag);
						if (!commentsRow.isVisible()) {
							String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
									commentsRow.getMarkupId(), contentRow.getMarkupId());
							target.prependJavaScript(script);
						} 
						target.add(commentsRow);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
						ObjectPermission readPermission = ObjectPermission.ofRepositoryRead(repoModel.getObject());
						setVisible(currentUser != null && SecurityUtils.getSubject().isPermitted(readPermission));
					}
					
				});
				if (diffLine.getAction() == DiffLine.Action.ADD) {
					if (item.getIndex() == 0 || diffs.get(item.getIndex()-1).getAction() == DiffLine.Action.EQUAL)
						contentRow.add(AttributeAppender.append("class", " new diff-block"));
					else
						contentRow.add(AttributeAppender.append("class", " new"));
					contentRow.add(new Label("oldLineNo"));
					contentRow.add(new Label("newLineNo", "+ " + (++newLineNo)));
				} else if (diffLine.getAction() == DiffLine.Action.DELETE) {
					if (item.getIndex() == 0 || diffs.get(item.getIndex()-1).getAction() == DiffLine.Action.EQUAL)
						contentRow.add(AttributeAppender.append("class", " old diff-block"));
					else
						contentRow.add(AttributeAppender.append("class", " old"));
					contentRow.add(new Label("oldLineNo", "- " + (++oldLineNo)));
					contentRow.add(new Label("newLineNo"));
				} else {
					contentRow.add(AttributeAppender.append("class", " equal"));
					contentRow.add(new Label("oldLineNo", "  " + (++oldLineNo)));
					contentRow.add(new Label("newLineNo", "  " + (++newLineNo)));
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
				
				item.add(contentRow);
			}
			
		});
	}

	@Override
	protected void onDetach() {
		oldCommentsModel.detach();
		newCommentsModel.detach();
		super.onDetach();
	}

}
