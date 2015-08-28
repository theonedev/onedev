package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.compare;

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
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.hibernate.HibernateUtils;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.InheritableThreadLocalData;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.ajaxlistener.IndicateLoadingListener;
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
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.comment.CommentRemoved;
import com.pmease.gitplex.web.component.diff.revision.RevisionDiffPanel;
import com.pmease.gitplex.web.component.diff.revision.option.DiffOptionPanel;
import com.pmease.gitplex.web.page.repository.pullrequest.PullRequestChanged;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.repository.pullrequest.requestlist.RequestListPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public class RequestComparePage extends RequestDetailPage {

	private static final String TARGET_BRANCH_HEAD = "Target Branch Head";
	
	private static final String INTEGRATION_PREVIEW = "Integration Preview";
	
	private static final String PULL_REQUEST_BASE = "Pull Request Base";
	
	private static final String LATEST_UPDATE_HEAD = "Latest Update Head";

	private static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_PREVIEW_INTEGRATION = "previewIntegration";
	
	private static final String PARAM_OLD_COMMIT = "oldCommit";
	
	private static final String PARAM_NEW_COMMIT = "newCommit";
	
	private static final String PARAM_PATH = "path";
	
	private static final String PARAM_COMPARE_PATH = "comparePath";
	
	private HistoryState state = new HistoryState();
	
	private String oldCommitHash;
	
	private String newCommitHash;
	
	private String path;
	
	private String comparePath;
	
	private final IModel<PullRequestComment> commentModel = new LoadableDetachableModel<PullRequestComment>() {

		@Override
		protected PullRequestComment load() {
			if (state.commentId != null)
				return GitPlex.getInstance(Dao.class).load(PullRequestComment.class, state.commentId);
			else
				return null;
		}
		
	};
	
	private WebMarkupContainer compareOptions;
	
	private DiffOptionPanel diffOption;
	
	private Component compareResult;
	
	private final IModel<Map<String, CommitDescription>> commitsModel = 
			new LoadableDetachableModel<Map<String, CommitDescription>>() {

		@Override
		protected LinkedHashMap<String, CommitDescription> load() {
			LinkedHashMap<String, CommitDescription> choices = new LinkedHashMap<>();
			PullRequest request = getPullRequest();

			String name = PULL_REQUEST_BASE;
			CommitDescription description = new CommitDescription(name, request.getBaseCommit().getSubject());
			choices.put(request.getBaseCommitHash(), description);
			
			for (int i=0; i<request.getSortedUpdates().size(); i++) {
				PullRequestUpdate update = request.getSortedUpdates().get(i);
				Commit commit = update.getHeadCommit();
				int updateNo = i+1;
				if (i == request.getSortedUpdates().size()-1)
					name = LATEST_UPDATE_HEAD;
				else
					name = "Head of Update " + updateNo;
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
	
	public RequestComparePage(PageParameters params) {
		super(params);

		state.commentId = params.get(PARAM_COMMENT).toOptionalLong();
		state.oldCommitHash = params.get(PARAM_OLD_COMMIT).toString();
		state.newCommitHash = params.get(PARAM_NEW_COMMIT).toString();
		state.path = params.get(PARAM_PATH).toString();
		state.comparePath = params.get(PARAM_COMPARE_PATH).toString();
		state.previewIntegration = params.get(PARAM_PREVIEW_INTEGRATION).toBoolean(false);
		
		initFromState(state);
	}
	
	private void initFromState(HistoryState state) {
		PullRequest request = getPullRequest();
		PullRequestComment comment = commentModel.getObject();
		if (comment != null) {
			oldCommitHash = comment.getOldCommitHash();
			newCommitHash = comment.getNewCommitHash();
			path = comment.getBlobIdent().path;
			comparePath = comment.getCompareWith().path;
		} else if (state.previewIntegration) {
			oldCommitHash = request.getTarget().getHead();
			IntegrationPreview preview = request.getIntegrationPreview();
			if (preview != null && preview.getIntegrated() != null)
				newCommitHash = preview.getIntegrated();
			else
				newCommitHash = request.getLatestUpdate().getHeadCommitHash();
			path = state.path;
			comparePath = state.comparePath;
		} else {
			oldCommitHash = state.oldCommitHash;
			newCommitHash = state.newCommitHash;
			path = state.path;
			comparePath = state.comparePath;
			if (oldCommitHash == null)
				oldCommitHash = request.getBaseCommitHash();
			if (newCommitHash == null)
				newCommitHash = request.getLatestUpdate().getHeadCommitHash();
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer compareHead = new WebMarkupContainer("compareHead");
		compareHead.add(new StickyBehavior());
		
		add(compareHead);

		compareOptions = new WebMarkupContainer("options");
		compareOptions.setOutputMarkupId(true);
		compareHead.add(compareOptions);
		
		WebMarkupContainer oldSelector = new WebMarkupContainer("oldSelector");
		compareOptions.add(oldSelector);
		oldSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(oldCommitHash);
				if (description != null)
					return GitUtils.abbreviateSHA(oldCommitHash) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(oldCommitHash);
			}
			
		}).add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(oldCommitHash);
				if (description != null) 
					return description.getSubject();
				else 
					return getRepository().getCommit(oldCommitHash).getSubject();
			}
			
		}, new TooltipConfig().withPlacement(Placement.top))));
		
		DropdownPanel oldChoicesDropdown = new DropdownPanel("oldChoices", false) {

			@Override
			protected Component newContent(String id) {
				return new CommitChoicePanel(id) {

					@Override
					protected void onSelect(AjaxRequestTarget target, String commitHash) {
						oldCommitHash = commitHash;
						freezeState();
						hide(target);
						onStateChange(target);
					}
					
				};
			}
			
		}; 
		compareOptions.add(oldChoicesDropdown);
		oldSelector.add(new DropdownBehavior(oldChoicesDropdown).alignWithTrigger(0, 100, 0, 0));
		
		WebMarkupContainer newSelector = new WebMarkupContainer("newSelector");
		compareOptions.add(newSelector);
		newSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(newCommitHash);
				if (description != null)
					return GitUtils.abbreviateSHA(newCommitHash) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(newCommitHash);
			}
			
		}).add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(newCommitHash);
				if (description != null)
					return description.getSubject();
				else
					return getRepository().getCommit(newCommitHash).getSubject();
			}
			
		}, new TooltipConfig().withPlacement(Placement.top))));
		
		DropdownPanel newChoicesDropdown = new DropdownPanel("newChoices", false) {

			@Override
			protected Component newContent(String id) {
				return new CommitChoicePanel(id) {

					@Override
					protected void onSelect(AjaxRequestTarget target, String commitHash) {
						newCommitHash = commitHash;
						freezeState();
						hide(target);
						onStateChange(target);
					}
					
				};
			}
			
		}; 
		compareOptions.add(newChoicesDropdown);
		newSelector.add(new DropdownBehavior(newChoicesDropdown).alignWithTrigger(0, 100, 0, 0));

		MenuPanel commonComparisons = new MenuPanel("comparisonChoices") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> items = new ArrayList<>();
				
				items.add(new ComparisonChoiceItem(PULL_REQUEST_BASE, LATEST_UPDATE_HEAD) {

					@Override
					protected void onSelect(AjaxRequestTarget target) {
						hide(target);
						
						oldCommitHash = getPullRequest().getBaseCommitHash();
						newCommitHash = getPullRequest().getLatestUpdate().getHeadCommitHash();

						state.commentId = null;
						state.path = path;
						state.comparePath = comparePath;
						state.newCommitHash = null;
						state.oldCommitHash = null;
						state.previewIntegration = false;
						
						onStateChange(target);
					}

				});

				PullRequest request = getPullRequest();
				if (request.isOpen()) {
					final IntegrationPreview preview = request.getIntegrationPreview();
					if (preview != null && preview.getIntegrated() != null) {
						items.add(new ComparisonChoiceItem(TARGET_BRANCH_HEAD, INTEGRATION_PREVIEW) {

							@Override
							protected void onSelect(AjaxRequestTarget target) {
								hide(target);
								
								oldCommitHash = getPullRequest().getTarget().getHead();
								newCommitHash = preview.getIntegrated();
								
								state.commentId = null;
								state.path = path;
								state.comparePath = comparePath;
								state.newCommitHash = null;
								state.oldCommitHash = null;
								state.previewIntegration = true;
								
								onStateChange(target);
							}
							
						});
					}
				}

				List<PullRequestUpdate> updates = getPullRequest().getSortedUpdates();
				if (updates.size() > 1) {
					for (int i=0; i<updates.size(); i++) {
						PullRequestUpdate update = updates.get(i);
						final String baseCommit = update.getBaseCommitHash();
						final String headCommit = update.getHeadCommitHash();
						int index = i+1;
						String oldLabel;
						if (index > 1) 
							oldLabel = "Update " + (index-1);
						else
							oldLabel = PULL_REQUEST_BASE;
						
						String newLabel;
						if (index == updates.size())
							newLabel = LATEST_UPDATE_HEAD;
						else
							newLabel = "Update " + index;
						items.add(new ComparisonChoiceItem(oldLabel, newLabel) {

							@Override
							protected void onSelect(AjaxRequestTarget target) {
								hide(target);

								oldCommitHash = baseCommit;
								newCommitHash = headCommit;
								freezeState();
								
								onStateChange(target);
							}
							
						});
					}
				}
				
				return items;
			}
			
		};
		
		compareOptions.add(commonComparisons);
		compareOptions.add(new WebMarkupContainer("comparisonSelector")
				.add(new MenuBehavior(commonComparisons)
				.alignWithTrigger(50, 100, 50, 0)));
		
		compareOptions.add(diffOption = new DiffOptionPanel("diffOption", repoModel, newCommitHash) {

			@Override
			protected void onSelectPath(AjaxRequestTarget target, String path) {
				RequestComparePage.this.path = path;
				comparePath = null;
				if (state.commentId != null) {
					freezeState();
				} else {
					state.path = path;
					state.comparePath = null;
				}
				newCompareResult(target);
				pushState(target);
			}

			@Override
			protected void onLineProcessorChange(AjaxRequestTarget target) {
				newCompareResult(target);
			}

			@Override
			protected void onDiffModeChange(AjaxRequestTarget target) {
				newCompareResult(target);
			}

			@Override
			protected Component getDirtyContainer() {
				return compareResult;
			}
			
		});
		
		compareHead.add(new WebMarkupContainer("outdatedAlert") {

			@Override
			public void onEvent(final IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PullRequestChanged) {
					PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
					AjaxRequestTarget target = pullRequestChanged.getTarget();
					PullRequest request = getPullRequest();
					boolean outdated;
					if (state.previewIntegration) {
						if (!oldCommitHash.equals(request.getTarget().getHead())) {
							outdated = true;
						} else {
							IntegrationPreview preview = request.getIntegrationPreview();
							String previewCommitHash;
							if (preview != null && preview.getIntegrated() != null)
								previewCommitHash = preview.getIntegrated();
							else
								previewCommitHash = request.getLatestUpdate().getHeadCommitHash();
							outdated = !newCommitHash.equals(previewCommitHash);
						}
					} else {
						outdated = state.newCommitHash == null && !newCommitHash.equals(request.getLatestUpdate().getHeadCommitHash());						
					}
					if (outdated) {
						setVisible(true);
						target.add(this);
					}
					target.add(compareOptions);
				}
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				setVisible(false);
				setOutputMarkupPlaceholderTag(true);
			}

		});
		compareHead.add(new Label("integrationPreviewAlert", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				PullRequest request = getPullRequest();
				String message;
				if (request.isOpen()) {
					IntegrationPreview preview = getPullRequest().getIntegrationPreview();
					if (preview == null)
						message = "Integration preview is being calculating";
					else
						message = "There are integration conflicts";
				} else {
					message = "Integration preview is not available for closed pull request";
				}
				return message + ", displaying comparison between target branch and request head instead.";
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				if (state.previewIntegration) {
					IntegrationPreview preview = getPullRequest().getIntegrationPreview();
					setVisible(preview == null || preview.getIntegrated() == null);
				} else {
					setVisible(false);
				}
			}
			
		});
		
		newCompareResult(null);
	}

	private void freezeState() {
		state.oldCommitHash = oldCommitHash;
		state.newCommitHash = newCommitHash;
		state.path = path;
		state.comparePath = comparePath;
		state.commentId = null;
		state.previewIntegration = false;
	}
	
	@Override
	public void onDetach() {
		commentModel.detach();
		commitsModel.detach();
		
		super.onDetach();
	}
	
	public static PageParameters paramsOf(PullRequestComment comment) {
		PageParameters params = RequestDetailPage.paramsOf(comment.getRequest());
		params.set(PARAM_COMMENT, comment.getId());
		
		return params;
	}

	public static PageParameters paramsOfIntegrationPreview(PullRequest request) {
		return paramsOf(request, null, null, null, null, null, true);
	}
	
	public static PageParameters paramsOf(PullRequest request, @Nullable String oldCommitHash, 
			@Nullable String newCommitHash) {
		return paramsOf(request, oldCommitHash, newCommitHash, null);
	}
	
	public static PageParameters paramsOf(PullRequest request, @Nullable String oldCommitHash, 
			@Nullable String newCommitHash, @Nullable String path) {
		return paramsOf(request, oldCommitHash, newCommitHash, path, null, null, false);
	}
	
	public static PageParameters paramsOf(PullRequest request, @Nullable String oldCommitHash, 
			@Nullable String newCommitHash, @Nullable String path, @Nullable String comparePath, 
			@Nullable Long commentId, boolean previewIntegration) {
		PageParameters params = RequestDetailPage.paramsOf(request);

		if (oldCommitHash != null)
			params.set(PARAM_OLD_COMMIT, oldCommitHash);
		if (newCommitHash != null)
			params.set(PARAM_NEW_COMMIT, newCommitHash);
		if (path != null)
			params.set(PARAM_PATH,  path);
		if (comparePath != null)
			params.set(PARAM_COMPARE_PATH,  path);
		if (commentId != null)
			params.set(PARAM_COMMENT, commentId);
		if (previewIntegration)
			params.set(PARAM_PREVIEW_INTEGRATION, true);
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
			if (HibernateUtils.getId(comment).equals(state.commentId)) {
				freezeState();
				onStateChange(commentRemoved.getTarget());
			}
		}
	}

	private static class CommitDescription implements Serializable {
		private final String name;
		
		private final String subject;
		
		CommitDescription(final String name, final String subject) {
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
	
	private abstract class CommitChoicePanel extends Fragment {

		CommitChoicePanel(String id) {
			super(id, "commitChoiceFrag", RequestComparePage.this);
		}

		protected abstract void onSelect(AjaxRequestTarget target, String commitHash);
		
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
					AjaxLink<Void> link = new AjaxLink<Void>("commit") {

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new IndicateLoadingListener());
							attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(compareResult));
						}

						@Override
						public void onClick(AjaxRequestTarget target) {
							Map.Entry<String, CommitDescription> entry = item.getModelObject();
							onSelect(target, entry.getKey());
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

		protected abstract void onSelect(AjaxRequestTarget target);

		@Override
		public Component newContent(String componentId) {
			Fragment fragment = new Fragment(componentId, "comparisonChoiceFrag", RequestComparePage.this);
			AjaxLink<Void> link = new AjaxLink<Void>("link") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new IndicateLoadingListener());
					attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(compareResult));
				}

				@Override
				public void onClick(AjaxRequestTarget target) {
					onSelect(target);
				}
				
			};
			fragment.add(link);
			
			link.add(new Label("old", oldName));
			link.add(new Label("new", newName));
			
			return fragment;
		}
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);

		HistoryState state = (HistoryState) data;
		initFromState(state);
		
		target.add(compareOptions);
		newCompareResult(target);
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Repository repository) {
		setResponsePage(RequestListPage.class, paramsOf(repository));
	}

	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getPullRequest(), state.oldCommitHash, state.newCommitHash, 
				state.path, state.comparePath, state.commentId, state.previewIntegration);
		CharSequence url = RequestCycle.get().urlFor(RequestComparePage.class, params);
		pushState(target, url.toString(), state);
	}
	
	private void onStateChange(AjaxRequestTarget target) {
		pushState(target);
		
		target.add(compareOptions);
		newCompareResult(target);
	}
	
	private void newCompareResult(@Nullable AjaxRequestTarget target) {
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
					return commentModel.getObject();
				}
				
				@Override
				public InlineComment addComment(BlobIdent blobInfo, BlobIdent compareWith, 
						int line, String content) {
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
					InheritableThreadLocalData.set(new WebSocketRenderBehavior.PageId(getPageId()));
					try {
						GitPlex.getInstance(PullRequestCommentManager.class).save(comment, true);
					} finally {
						InheritableThreadLocalData.clear();
					}
					return comment;
				}

				@Override
				public InlineComment loadComment(Long commentId) {
					return GitPlex.getInstance(Dao.class).load(PullRequestComment.class, commentId);
				}
			};
		};
		
		compareResult = new RevisionDiffPanel("compareResult", 
				repoModel, oldCommitHash, newCommitHash, path, comparePath, 
				diffOption.getLineProcessor(), diffOption.getDiffMode(), 
				commentSupport) {

			@Override
			protected void onClearPath(AjaxRequestTarget target) {
				path = null;
				comparePath = null;
				if (state.commentId != null) {
					freezeState();
				} else {
					state.path = null;
					state.comparePath = null;
				}
				newCompareResult(target);
				pushState(target);
			}
			
		};
		compareResult.setOutputMarkupId(true);
		if (target != null) {
			replace(compareResult);
			target.add(compareResult);
		} else {
			add(compareResult);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RequestComparePage.class, "request-compare.css")));
	}

}
