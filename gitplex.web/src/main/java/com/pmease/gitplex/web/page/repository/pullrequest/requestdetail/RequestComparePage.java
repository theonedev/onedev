package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.LineProcessor;
import com.pmease.commons.hibernate.HibernateUtils;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.lang.diff.AroundContext;
import com.pmease.commons.loader.InheritableThreadLocalData;
import com.pmease.commons.wicket.behavior.StickyBehavior;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.behavior.menu.MenuBehavior;
import com.pmease.commons.wicket.behavior.menu.MenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.InlineComment;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.IntegrationPreview;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.comment.event.CommentRemoved;
import com.pmease.gitplex.web.component.diff.diffmode.DiffModePanel;
import com.pmease.gitplex.web.component.diff.lineprocess.LineProcessOptionMenu;
import com.pmease.gitplex.web.component.diff.revision.RevisionDiffPanel;
import com.pmease.gitplex.web.event.PullRequestChanged;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public class RequestComparePage extends RequestDetailPage {

	private static final String TARGET_BRANCH_HEAD = "Head of Target Branch";
	
	private static final String INTEGRATION_PREVIEW = "Integration Preview";

	private static final String COMMENT_PARAM = "comment";
	
	private static final String OLD_PARAM = "old";
	
	private static final String NEW_PARAM = "new";
	
	private String oldCommitHash;
	
	private String newCommitHash;
	
	private WebMarkupContainer optionsContainer;
	
	private final IModel<PullRequestComment> commentModel;
	
	private LineProcessOptionMenu lineProcessOptionMenu;
	
	private DiffModePanel diffModePanel;
	
	private final IModel<Map<String, CommitDescription>> commitsModel = 
			new LoadableDetachableModel<Map<String, CommitDescription>>() {

		@Override
		protected LinkedHashMap<String, CommitDescription> load() {
			LinkedHashMap<String, CommitDescription> choices = new LinkedHashMap<>();
			PullRequest request = getPullRequest();

			String name = "Base of Pull Request";
			CommitDescription description = new CommitDescription(name, request.getBaseCommit().getSubject());
			choices.put(request.getBaseCommitHash(), description);
			
			for (int i=0; i<request.getSortedUpdates().size(); i++) {
				PullRequestUpdate update = request.getSortedUpdates().get(i);
				Commit commit = update.getHeadCommit();
				int updateNo = i+1;
				if (i == request.getSortedUpdates().size()-1)
					name = "Head of Latest Update";
				else
					name = "Head of Update #" + updateNo;
				description = new CommitDescription(name, commit.getSubject());
				choices.put(commit.getHash(), description);
			}

			String targetHead = request.getTarget().getHead();
			if (!choices.containsKey(targetHead)) {
				description = new CommitDescription(TARGET_BRANCH_HEAD, 
						getRepository().getCommit(targetHead).getSubject());
				choices.put(targetHead, description);
			}

			if (request.isOpen()) {
				IntegrationPreview preview = request.getIntegrationPreview();
				if (preview != null && preview.getIntegrated() != null && !preview.getIntegrated().equals(preview.getRequestHead())) {
					Commit commit = getRepository().getCommit(preview.getIntegrated());
					choices.put(commit.getHash(), new CommitDescription(INTEGRATION_PREVIEW, commit.getSubject()));
				}
			}
			
			return choices;
		}
		
	};
	
	public RequestComparePage(final PageParameters params) {
		super(params);

		commentModel = new LoadableDetachableModel<PullRequestComment>() {

			@Override
			protected PullRequestComment load() {
				Long commentId = params.get(COMMENT_PARAM).toOptionalLong();
				if (commentId != null)
					return GitPlex.getInstance(Dao.class).load(PullRequestComment.class, commentId);
				else 
					return null;
			}
			
		};
		
		oldCommitHash = params.get(OLD_PARAM).toString();
		newCommitHash = params.get(NEW_PARAM).toString();
		
		PullRequestComment comment = getComment();
		if (comment != null) {
			if (oldCommitHash != null || newCommitHash != null) {
				throw new IllegalArgumentException("Parameter 'old' or 'new' "
						+ "should not be specified if parameter 'comment' is specified.");
			}

			oldCommitHash = comment.getOldCommitHash();
			newCommitHash = comment.getNewCommitHash();
		} else {
			if (oldCommitHash == null)
				oldCommitHash = getPullRequest().getBaseCommitHash();
			if (newCommitHash == null)
				newCommitHash = getPullRequest().getLatestUpdate().getHeadCommitHash();
		}
		if (oldCommitHash != null && !commitsModel.getObject().containsKey(oldCommitHash))
			throw new IllegalArgumentException("Commit '" + oldCommitHash + "' is not relevant to current pull request.");

		if (newCommitHash != null && !commitsModel.getObject().containsKey(newCommitHash))
			throw new IllegalArgumentException("Commit '" + newCommitHash + "' is not relevant to current pull request.");
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		optionsContainer = new WebMarkupContainer("compareOptions") {
			
			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PullRequestChanged) {
					PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
					AjaxRequestTarget target = pullRequestChanged.getTarget();
					for (StickyBehavior behavior: getBehaviors(StickyBehavior.class))
						behavior.unstick(target);
					target.add(this);
				}
			}

		};
		optionsContainer.add(new StickyBehavior());
		
		add(optionsContainer);

		WebMarkupContainer oldSelector = new WebMarkupContainer("oldSelector");
		optionsContainer.add(oldSelector);
		oldSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(oldCommitHash);
				Preconditions.checkNotNull(description);
				if (description.getName() != null)
					return GitUtils.abbreviateSHA(oldCommitHash) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(oldCommitHash);
			}
			
		}).add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(oldCommitHash);
				Preconditions.checkNotNull(description);
				return description.getSubject();
			}
			
		}, new TooltipConfig().withPlacement(Placement.top))));
		
		DropdownPanel oldChoicesDropdown = new DropdownPanel("oldChoices", false) {

			@Override
			protected Component newContent(String id) {
				return new CommitChoicePanel(id, true);
			}
			
		}; 
		optionsContainer.add(oldChoicesDropdown);
		oldSelector.add(new DropdownBehavior(oldChoicesDropdown).alignWithTrigger(0, 100, 0, 0));
		
		WebMarkupContainer newSelector = new WebMarkupContainer("newSelector");
		optionsContainer.add(newSelector);
		newSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(newCommitHash);
				Preconditions.checkNotNull(description);
				
				if (description.getName() != null)
					return GitUtils.abbreviateSHA(newCommitHash) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(newCommitHash);
			}
			
		}).add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(newCommitHash);
				Preconditions.checkNotNull(description);
				return description.getSubject();
			}
			
		}, new TooltipConfig().withPlacement(Placement.top))));
		
		DropdownPanel newChoicesDropdown = new DropdownPanel("newChoices", false) {

			@Override
			protected Component newContent(String id) {
				return new CommitChoicePanel(id, false);
			}
			
		}; 
		optionsContainer.add(newChoicesDropdown);
		newSelector.add(new DropdownBehavior(newChoicesDropdown).alignWithTrigger(0, 100, 0, 0));

		MenuPanel commonComparisons = new MenuPanel("comparisonChoices") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> items = new ArrayList<>();
				
				items.add(new ComparisonChoiceItem("Base", "Latest Update") {

					@Override
					protected void onSelect() {
						PageParameters params = paramsOf(getPullRequest(), null, null);
						setResponsePage(RequestComparePage.class, params);
					}

				});

				PullRequest request = getPullRequest();
				if (request.isOpen()) {
					final IntegrationPreview preview = request.getIntegrationPreview();
					if (preview != null && preview.getIntegrated() != null 
							&& !getRepository().getChanges(preview.getRequestHead(), preview.getIntegrated()).isEmpty()) {
						items.add(new ComparisonChoiceItem("Target Branch", "Integration Preview") {

							@Override
							protected void onSelect() {
								PageParameters params = paramsOf(getPullRequest(), 
										getPullRequest().getTarget().getHead(), preview.getIntegrated());
								setResponsePage(RequestComparePage.class, params);
							}
							
						});
					}
				}

				for (int i=0; i<getPullRequest().getSortedUpdates().size(); i++) {
					PullRequestUpdate update = getPullRequest().getSortedUpdates().get(i);
					final String baseCommit = update.getBaseCommitHash();
					final String headCommit = update.getHeadCommitHash();
					int index = i+1;
					String oldLabel;
					if (index > 1) 
						oldLabel = "Update #" + (index-1);
					else
						oldLabel = "Request Base";
					
					String newLabel;
					if (index == getPullRequest().getSortedUpdates().size())
						newLabel = "Latest Update";
					else
						newLabel = "Update #" + index;
					items.add(new ComparisonChoiceItem(oldLabel, newLabel) {

						@Override
						protected void onSelect() {
							PageParameters params = paramsOf(getPullRequest(), baseCommit, headCommit);
							setResponsePage(RequestComparePage.class, params);
						}
						
					});
				}
				
				return items;
			}
			
		};
		
		optionsContainer.add(commonComparisons);
		optionsContainer.add(new WebMarkupContainer("comparisonSelector")
				.add(new MenuBehavior(commonComparisons)
				.alignWithTrigger(50, 100, 50, 0)));
		
		optionsContainer.add(new WebMarkupContainer("outdatedAlert") {

			@Override
			public void onEvent(final IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PullRequestChanged) {
					PageParameters params = getPageParameters();
					if (params.get(COMMENT_PARAM).toOptionalLong() != null || getPageParameters().get(NEW_PARAM).toString() == null) {
						setVisible(true);
						/*
						compareResult.visitChildren(new IVisitor<Component, Void>() {

							@Override
							public void component(Component object, IVisit<Void> visit) {
								AjaxRequestTarget target = ((PullRequestChanged) event.getPayload()).getTarget();
								for (StickyBehavior behavior: object.getBehaviors(StickyBehavior.class))
									behavior.restick(target);
							}
							
						});
						*/
					}
					
				}
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				setVisible(false);
				setOutputMarkupPlaceholderTag(true);
			}

		});

		lineProcessOptionMenu = new LineProcessOptionMenu("lineProcessOptionMenu") {

			@Override
			protected void onOptionChange(AjaxRequestTarget target) {
			}
			
		};
		optionsContainer.add(lineProcessOptionMenu);
		optionsContainer.add(new WebMarkupContainer("lineProcessOptionMenuTrigger").add(new MenuBehavior(lineProcessOptionMenu)));
		
		optionsContainer.add(diffModePanel = new DiffModePanel("diffMode") {

			@Override
			protected void onModeChange(AjaxRequestTarget target) {
			}
			
		});
		
		InlineCommentSupport commentSupport;
		
		List<String> commentables = getPullRequest().getCommentables();
		int oldCommitIndex = commentables.indexOf(oldCommitHash);
		int newCommitIndex = commentables.indexOf(newCommitHash);
		if (oldCommitIndex == -1 || newCommitIndex == -1 || oldCommitIndex > newCommitIndex) {
			commentSupport = null;
		} else {
			commentSupport = new InlineCommentSupport() {
				
				@Override
				public Map<Integer, List<InlineComment>> getComments(BlobIdent blobIdent) {
					Map<Integer, List<InlineComment>> comments = new HashMap<>();
					for (PullRequestComment comment: getPullRequest().getComments()) {
						if (comment.getInlineInfo() != null && comment.getBlobIdent().equals(blobIdent)) {
							List<InlineComment> commentsAtLine = comments.get(comment.getLine());
							if (commentsAtLine == null) {
								commentsAtLine = new ArrayList<>();
								comments.put(comment.getLine(), commentsAtLine);
							}
							commentsAtLine.add(comment);
						}
					}
					return comments;
				}
				
				@Override
				public InlineComment getConcernedComment() {
					return getComment();
				}
				
				@Override
				public InlineComment addComment(BlobIdent blobInfo, BlobIdent compareWith, 
						AroundContext commentContext, int line, String content) {
					User user = GitPlex.getInstance(UserManager.class).getCurrent();
					Preconditions.checkNotNull(user);
					PullRequestComment comment = new PullRequestComment();
					getPullRequest().getComments().add(comment);
					comment.setUser(user);
					comment.setDate(new Date());
					comment.setContent(content);
					comment.setRequest(getPullRequest());
					comment.setBlobIdent(blobInfo);
					comment.setCompareWith(compareWith);
					comment.setLine(line);
					comment.setContext(commentContext);
					InheritableThreadLocalData.set(new WebSocketRenderBehavior.PageId(getPageId()));
					try {
						GitPlex.getInstance(PullRequestCommentManager.class).save(comment, true);
					} finally {
						InheritableThreadLocalData.clear();
					}
					return comment;
				}
			};
		};
		
		add(new RevisionDiffPanel("compareResult", repoModel, oldCommitHash, newCommitHash, commentSupport) {

			@Override
			protected LineProcessor getLineProcessor() {
				return lineProcessOptionMenu.getOption();
			}

			@Override
			protected boolean isUnified() {
				return diffModePanel.isUnified();
			}
			
		});
	}
	
	@Override
	public void onDetach() {
		commitsModel.detach();
		commentModel.detach();
		
		super.onDetach();
	}
	
	public static PageParameters paramsOf(PullRequestComment concernedComment) {
		PageParameters params = RequestDetailPage.paramsOf(concernedComment.getRequest());
		params.set("comment", concernedComment.getId());
		
		return params;
	}
	
	public static PageParameters paramsOf(PullRequest request, @Nullable String oldCommit, 
			@Nullable String newCommit) {
		PageParameters params = RequestDetailPage.paramsOf(request);
		
		if (oldCommit != null)
			params.set(OLD_PARAM, oldCommit);
		if (newCommit != null)
			params.set(NEW_PARAM, newCommit);
		
		return params;
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof CommentRemoved) {
			CommentRemoved commentRemoved = (CommentRemoved) event.getPayload();
			PullRequestComment comment = (PullRequestComment) commentRemoved.getComment();
			
			// compare identifier instead of comment object as comment may have been deleted
			// to cause LazyInitializationException
			if (getComment() != null 
					&& HibernateUtils.getId(comment).equals(HibernateUtils.getId(getComment()))) {
				PageParameters params = paramsOf(getPullRequest(), oldCommitHash, newCommitHash);
				setResponsePage(RequestComparePage.class, params);
			}
		}
	}

	private static class CommitDescription implements Serializable {
		private final String name;
		
		private final String subject;
		
		CommitDescription(@Nullable final String name, final String subject) {
			this.name = name;
			this.subject = subject;
		}

		public String getName() {
			return name;
		}

		public String getSubject() {
			return subject;
		}
		
	}
	
	private class CommitChoicePanel extends Fragment {

		private final boolean forBase;
		
		CommitChoicePanel(String id, boolean forBase) {
			super(id, "commitChoiceFrag", RequestComparePage.this);
			
			this.forBase = forBase;
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			
			setOutputMarkupId(true);
			
			IModel<List<Map.Entry<String, CommitDescription>>> model = 
					new LoadableDetachableModel<List<Map.Entry<String, CommitDescription>>>() {

				@Override
				protected List<Entry<String, CommitDescription>> load() {
					List<Entry<String, CommitDescription>> entries = new ArrayList<>();
					entries.addAll(commitsModel.getObject().entrySet());
					return entries;
				}
				
			};
			
			add(new ListView<Map.Entry<String, CommitDescription>>("commits", model) {

				@Override
				protected void populateItem(final ListItem<Entry<String, CommitDescription>> item) {
					Link<Void> link = new Link<Void>("commit") {

						@Override
						public void onClick() {
							Map.Entry<String, CommitDescription> entry = item.getModelObject();
							if (forBase) {
								setResponsePage(RequestComparePage.class, 
										paramsOf(getPullRequest(), entry.getKey(), newCommitHash));
							} else {
								setResponsePage(RequestComparePage.class, 
										paramsOf(getPullRequest(), oldCommitHash, entry.getKey()));
							}
						}
						
					};
					Map.Entry<String, CommitDescription> entry = item.getModelObject();
					String hash = GitUtils.abbreviateSHA(entry.getKey(), 7);
					String name = entry.getValue().getName();
					link.add(new Label("commit", hash));
					link.add(new Label("name", name).setVisible(name != null));
					if (entry.getValue().getSubject() != null)
						link.add(new Label("subject", entry.getValue().getSubject()));
					else
						link.add(new WebMarkupContainer("subject").setVisible(false));
					item.add(link);
				}
				
			});
		}

	}
	
	private abstract class ComparisonChoiceItem extends MenuItem {

		private final String oldName;
		
		private final String newName;
		
		ComparisonChoiceItem(String oldName, String newName) {
			this.oldName = oldName;
			this.newName = newName;
		}

		protected abstract void onSelect();

		@Override
		public Component newContent(String componentId) {
			Fragment fragment = new Fragment(componentId, "comparisonChoiceFrag", RequestComparePage.this);
			Link<Void> link = new Link<Void>("link") {

				@Override
				public void onClick() {
					onSelect();
				}
				
			};
			fragment.add(link);
			
			link.add(new Label("old", oldName));
			link.add(new Label("new", newName));
			
			return fragment;
		}
	}

	private PullRequestComment getComment() {
		return commentModel.getObject();
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);

		target.add(optionsContainer);
	}
	
}
