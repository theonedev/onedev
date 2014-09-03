package com.pmease.gitplex.web.page.repository.info.pullrequest;

import static com.pmease.commons.git.Change.Status.UNCHANGED;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.hibernate.Hibernate;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.git.BlobText;
import com.pmease.commons.git.Change;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.commons.git.TreeNode;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.behavior.StickyBehavior;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.behavior.menu.MenuBehavior;
import com.pmease.commons.wicket.behavior.menu.MenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.BlobLoader;
import com.pmease.gitplex.core.comment.CommentAwareChange;
import com.pmease.gitplex.core.comment.CommentLoader;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.CommitComment;
import com.pmease.gitplex.core.model.IntegrationInfo;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.diff.BlobDiffPanel;
import com.pmease.gitplex.web.component.diff.ChangedFilesPanel;
import com.pmease.gitplex.web.component.diff.DiffTreePanel;
import com.pmease.gitplex.web.event.CommitCommentRemoved;
import com.pmease.gitplex.web.page.repository.info.pullrequest.activity.ActivitiesModel;
import com.pmease.gitplex.web.page.repository.info.pullrequest.activity.PullRequestActivity;
import com.pmease.gitplex.web.page.repository.info.pullrequest.activity.UpdatePullRequest;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public class RequestComparePage extends RequestDetailPage {

	private static final String HEAD_ID = "compareHead";
	
	private static final String CHANGES_TRIGGER_ID = "changesTrigger";
	
	private static final String CHANGES_DROPDOWN_ID = "changesDropdown";
	
	private static final String LABEL_ID = "label";
	
	private static final String PREV_CHANGE_ID = "prevChange";
	
	private static final String NEXT_CHANGE_ID = "nextChange";

	private static final String CHANGES_ID = "changes";
	
	private static final String BLOB_DIFF_ID = "blobDiff";
	
	private String oldCommit;
	
	private String newCommit;
	
	private String filePath;
	
	private final IModel<CommitComment> concernedCommentModel;
	
	private final ActivitiesModel activitiesModel = new ActivitiesModel() {
		
		@Override
		protected PullRequest getPullRequest() {
			return RequestComparePage.this.getPullRequest();
		}
	};
	
	private final IModel<Map<String, CommitDescription>> commitsModel = 
			new LoadableDetachableModel<Map<String, CommitDescription>>() {

		@Override
		protected LinkedHashMap<String, CommitDescription> load() {
			LinkedHashMap<String, CommitDescription> choices = new LinkedHashMap<>();
			PullRequest request = getPullRequest();

			String name = "Base of Pull Request";
			CommitDescription description = new CommitDescription(name, 
					getRepository().git().showRevision(request.getBaseCommit()).getSubject());
			choices.put(request.getBaseCommit(), description);
			
			for (int i=request.getSortedUpdates().size()-1; i>=0; i--) {
				PullRequestUpdate update = request.getSortedUpdates().get(i);
				int updateNo = request.getSortedUpdates().size()-i;
				int j = 0;
				for (Commit commit: update.getCommits()) {
					if (j == update.getCommits().size()-1) {
						name = "Head of Update #" + updateNo;
						description = new CommitDescription(name, commit.getSubject());
					} else {
						description = new CommitDescription(null, commit.getSubject());
					}
					j++;
					choices.put(commit.getHash(), description);
				}
			}

			String targetHead = request.getTarget().getHeadCommit();
			if (!choices.containsKey(targetHead)) {
				description = new CommitDescription("Head of Target Branch", 
						getRepository().git().showRevision(targetHead).getSubject());
				choices.put(targetHead, description);
			}

			IntegrationInfo integrationInfo = request.getIntegrationInfo();
			if (request.isOpen() 
					&& integrationInfo.getIntegrationHead() != null 
					&& !integrationInfo.getIntegrationHead().equals(integrationInfo.getRequestHead())
					&& integrationInfo.hasChanges()) { 
				Commit commit = getRepository().git().showRevision(request.getIntegrationInfo().getIntegrationHead());
				choices.put(request.getIntegrationInfo().getIntegrationHead(), 
						new CommitDescription("Integration Preview", commit.getSubject()));
			}
			
			return choices;
		}
		
	};
	
	public RequestComparePage(PageParameters params) {
		super(params);

		filePath = params.get("path").toString();
		final Long concernedCommentId = params.get("comment").toOptionalLong();
		
		concernedCommentModel = new LoadableDetachableModel<CommitComment>() {

			@Override
			protected CommitComment load() {
				if (concernedCommentId != null) {
					CommitComment comment = GitPlex.getInstance(Dao.class).load(CommitComment.class, concernedCommentId);
					Hibernate.initialize(comment);
					return comment;
				} else {
					return null;
				}
			}
			
		};
		
		oldCommit = params.get("original").toString();
		newCommit = params.get("revised").toString();

		if (!(oldCommit == null && newCommit == null || oldCommit != null && newCommit != null))
			throw new IllegalArgumentException("Param 'original' and 'revised' should be specified both or none.");

		if (oldCommit != null) {
			if (!commitsModel.getObject().containsKey(oldCommit))
				throw new IllegalArgumentException("Commit '" + oldCommit + "' is not relevant to current pull request.");
		}

		if (newCommit != null) {
			if (!commitsModel.getObject().containsKey(newCommit))
				throw new IllegalArgumentException("Commit '" + newCommit + "' is not relevant to current pull request.");
		}
		
		if (filePath != null && oldCommit == null)
			throw new IllegalArgumentException("Param 'path' can only be used together with param 'original' and 'revised'.");
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer head = new WebMarkupContainer(HEAD_ID);
		head.add(new StickyBehavior());
		
		add(head);

		WebMarkupContainer oldSelector = new WebMarkupContainer("oldSelector");
		head.add(oldSelector);
		oldSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(oldCommit);
				Preconditions.checkNotNull(description);
				if (description.getName() != null)
					return GitUtils.abbreviateSHA(oldCommit, 7) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(oldCommit, 7);
			}
			
		}).add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(oldCommit);
				Preconditions.checkNotNull(description);
				return description.getSubject();
			}
			
		}, new TooltipConfig().withPlacement(Placement.bottom))));
		
		DropdownPanel oldChoicesDropdown = new DropdownPanel("oldChoices", false) {

			@Override
			protected Component newContent(String id) {
				return new CommitChoicePanel(id, true);
			}
			
		}; 
		head.add(oldChoicesDropdown);
		oldSelector.add(new DropdownBehavior(oldChoicesDropdown).alignWithTrigger(0, 0, 0, 100));
		
		WebMarkupContainer newSelector = new WebMarkupContainer("newSelector");
		head.add(newSelector);
		newSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(newCommit);
				Preconditions.checkNotNull(description);
				
				if (description.getName() != null)
					return GitUtils.abbreviateSHA(newCommit, 7) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(newCommit, 7);
			}
			
		}).add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(newCommit);
				Preconditions.checkNotNull(description);
				return description.getSubject();
			}
			
		}, new TooltipConfig().withPlacement(Placement.bottom))));
		
		DropdownPanel newChoicesDropdown = new DropdownPanel("newChoices", false) {

			@Override
			protected Component newContent(String id) {
				return new CommitChoicePanel(id, false);
			}
			
		}; 
		head.add(newChoicesDropdown);
		newSelector.add(new DropdownBehavior(newChoicesDropdown).alignWithTrigger(0, 0, 0, 100));

		MenuPanel commonComparisons = new MenuPanel("comparisonChoices") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> items = new ArrayList<>();
				items.add(new ComparisonChoiceItem("Base", "Latest Update") {

					@Override
					protected void onSelect() {
						PageParameters params = paramsOf(getPullRequest(), 
								getPullRequest().getBaseCommit(), 
								getPullRequest().getLatestUpdate().getHeadCommit(), 
								filePath, getConcernedComment());
						setResponsePage(RequestComparePage.class, params);
					}

				});
				
				final IntegrationInfo integrationInfo = getPullRequest().getIntegrationInfo();
				if (getPullRequest().isOpen() 
						&& integrationInfo.getIntegrationHead() != null 
						&& !integrationInfo.getIntegrationHead().equals(integrationInfo.getRequestHead())
						&& integrationInfo.hasChanges()) { 
					items.add(new ComparisonChoiceItem("Latest Update", "Integration Preview") {

						@Override
						protected void onSelect() {
							PageParameters params = paramsOf(getPullRequest(), 
									getPullRequest().getLatestUpdate().getHeadCommit(), 
									integrationInfo.getIntegrationHead(),
									filePath, getConcernedComment());
							setResponsePage(RequestComparePage.class, params);
						}
						
					});
				}
				
				for (int i=0; i<getPullRequest().getSortedUpdates().size(); i++) {
					PullRequestUpdate update = getPullRequest().getSortedUpdates().get(i);
					final String baseCommit = update.getBaseCommit();
					final String headCommit = update.getHeadCommit();
					int index = getPullRequest().getSortedUpdates().size()-i;
					String oldLabel;
					if (index > 1) 
						oldLabel = "Update #" + (index-1);
					else
						oldLabel = "Base";
					
					items.add(new ComparisonChoiceItem(oldLabel, "Update #" + index) {

						@Override
						protected void onSelect() {
							PageParameters params = paramsOf(getPullRequest(), 
									baseCommit, headCommit, filePath, getConcernedComment());
							setResponsePage(RequestComparePage.class, params);
						}
						
					});
				}
				
				return items;
			}
			
		};
		
		head.add(commonComparisons);
		head.add(new WebMarkupContainer("comparisonSelector")
				.add(new MenuBehavior(commonComparisons)
				.alignWithTrigger(50, 100, 50, 0)));
		
		DropdownPanel changesDropdown = new DropdownPanel("changesDropdown") {

			@Override
			protected Component newContent(String id) {
				final Fragment fragment = new Fragment(id, "changesFrag", RequestComparePage.this);
				
				Change change = getActiveChange();
				boolean changesOnly = (change == null || change.getStatus() != UNCHANGED);
				fragment.add(new CheckBox("changesOnly", Model.of(changesOnly)).add(new OnChangeAjaxBehavior() {
							
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						Component changeLister = fragment.get(CHANGES_ID);
						changeLister = newChangeLister(!(changeLister instanceof ChangedFilesPanel));
						fragment.replace(changeLister);
						target.add(changeLister);
					}
					
				}));

				fragment.add(newChangeLister(changesOnly));
				return fragment;
			}
			
		};
		
		head.add(changesDropdown);
		WebMarkupContainer changesTrigger = new WebMarkupContainer(CHANGES_TRIGGER_ID);
		changesTrigger.add(new DropdownBehavior(changesDropdown));
		changesTrigger.add(new Label(LABEL_ID, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				int index = getChangedFiles().indexOf(filePath);
				if (index == -1)
					return String.valueOf(getChanges().size());
				else
					return String.valueOf(index+1) + "/" + getChanges().size();
			}
			
		}).setOutputMarkupId(true));
		head.add(changesTrigger);
		
		head.add(new AjaxLink<Void>(PREV_CHANGE_ID) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				int index = getChangedFiles().indexOf(filePath) - 1;
				onChangeSelected(target, getChanges().get(index));
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				if (getChangedFiles().indexOf(filePath) <= 0) 
					tag.put("disabled", "disabled");
			}
			
		}.setOutputMarkupId(true));

		head.add(new AjaxLink<Void>(NEXT_CHANGE_ID) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				int index = getChangedFiles().indexOf(filePath) + 1;
				onChangeSelected(target, getChanges().get(index));
			}
			
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				List<String> changedFiles = getChangedFiles();
				if (changedFiles.isEmpty() || changedFiles.indexOf(filePath) >= changedFiles.size()-1) 
					tag.put("disabled", "disabled");
			}

		}.setOutputMarkupId(true));

		CommentAwareChange change = resolveChange();
		if (change != null) {
			BlobDiffPanel blobDiffPanel = new BlobDiffPanel(BLOB_DIFF_ID, repoModel, change);
			blobDiffPanel.setOutputMarkupId(true);
			add(blobDiffPanel);
		} else {
			add(new WebMarkupContainer(BLOB_DIFF_ID).setOutputMarkupId(true));
		}
		
	}
	
	private List<Change> getChanges() {
		return getRepository().getChanges(oldCommit, newCommit);
	}
	
	private List<String> getChangedFiles() {
		List<String> files = new ArrayList<>();
		for (Change change: getChanges())
			files.add(change.getPath());
		return files;
	}
	
	private CommentAwareChange resolveChange() {
		CommentAwareChange change = null;
		CommitComment comment = getConcernedComment();
		if (oldCommit != null) {
			if (filePath != null) {
				change = resolveChange(filePath);
			} else if (comment != null) {
				filePath = comment.getPosition().getFilePath();
				change = resolveChange(filePath);
			}
		} else if (comment == null) {
			User user = GitPlex.getInstance(UserManager.class).getCurrent();
			newCommit = getPullRequest().getLatestUpdate().getHeadCommit();
			if (user == null) {
				oldCommit = getPullRequest().getBaseCommit();
			} else {
				List<PullRequestActivity> activities = activitiesModel.getObject();
				boolean found = false;
				for (int i=activities.size()-1; i>=0; i--) {
					PullRequestActivity activity = activities.get(i);
					if (found) {
						if (activity instanceof UpdatePullRequest) {
							oldCommit = ((UpdatePullRequest) activity).getUpdate().getHeadCommit();
							break;
						}
					} else if (activity.getUser().equals(user)) {
						found = true;
					}
				}
				if (oldCommit == null || oldCommit.equals(newCommit))
					oldCommit = getPullRequest().getBaseCommit();
			}
		} else {
			filePath = comment.getPosition().getFilePath();
			oldCommit = getPullRequest().getBaseCommit();
			newCommit = getPullRequest().getLatestUpdate().getHeadCommit();
			change = resolveChange(filePath);
			if (change == null || !change.contains(comment)) {
				if (!comment.getCommit().equals(getPullRequest().getLatestUpdate().getHeadCommit())) {
					oldCommit = comment.getCommit();
					newCommit = getPullRequest().getLatestUpdate().getHeadCommit();
				} else {
					oldCommit = getPullRequest().getBaseCommit();
					newCommit = comment.getCommit();
				}
				change = resolveChange(filePath);
			}
		}
		if (change == null && !getChanges().isEmpty()) {
			filePath = getChanges().get(0).getPath();
			change = resolveChange(filePath);
			Preconditions.checkNotNull(change);
		}

		return change;
	}

	private CommentAwareChange resolveChange(String filePath) {
		for (Change each: getChanges()) {
			if (filePath.equals(each.getPath())) 
				return loadComments(new RevAwareChange(each, oldCommit, newCommit));
		}
		List<TreeNode> result = getRepository().git().listTree(newCommit, filePath);
		if (!result.isEmpty() && result.get(0).getMode() != FileMode.TYPE_TREE) {
			TreeNode blobNode = result.get(0);
			return loadComments(new RevAwareChange(UNCHANGED, filePath, filePath, 
					blobNode.getMode(), blobNode.getMode(), oldCommit, newCommit));
		} else {
			return null;
		}
	}
	
	private Component newChangeLister(boolean changesOnly) {
		Component changeLister;
		if (changesOnly) {
			changeLister = new ChangedFilesPanel(CHANGES_ID, repoModel, oldCommit, newCommit) {
				
				@Override
				protected WebMarkupContainer newBlobLink(String id, Change change) {
					return new BlobLink(id, change);
				}
			};
		} else {
			changeLister = new DiffTreePanel(CHANGES_ID, repoModel, oldCommit, newCommit) {

				@Override
				protected WebMarkupContainer newBlobLink(String id, final Change change) {
					return new BlobLink(id, change);
				}

				@Override
				protected void onInitialize() {
					super.onInitialize();
					
					RevAwareChange activeChange = getActiveChange();
					if (activeChange != null)
						reveal(activeChange);
				}
				
			};
		}
		changeLister.setOutputMarkupId(true);
		return changeLister;
	}
	
	@Nullable
	private CommentAwareChange getActiveChange() {
		Component panel = getPage().get(BLOB_DIFF_ID);
		if (panel instanceof BlobDiffPanel)
			return ((BlobDiffPanel) panel).getChange();
		else
			return null;
	}
	
	@Override
	public void onDetach() {
		commitsModel.detach();
		concernedCommentModel.detach();
		activitiesModel.detach();
		
		super.onDetach();
	}
	
	public static PageParameters paramsOf(PullRequest request, @Nullable String oldCommit, 
			@Nullable String newCommit, @Nullable String filePath, @Nullable CommitComment concernedComment) {
		PageParameters params = RequestDetailPage.paramsOf(request);
		
		if (oldCommit != null)
			params.set("original", oldCommit);
		if (newCommit != null)
			params.set("revised", newCommit);
		if (filePath != null)
			params.set("path", filePath);
		if (concernedComment != null)
			params.set("comment", concernedComment.getId());
		
		return params;
	}
	
	private CommentAwareChange loadComments(RevAwareChange change) {
		BlobLoader blobLoader = new BlobLoader() {

			@Override
			public List<String> loadBlob(BlobInfo blobInfo) {
				BlobText blobText = repoModel.getObject().getBlobText(blobInfo);
				return blobText!=null?blobText.getLines():null;
			}
			
		};
		CommentLoader commentLoader = new CommentLoader() {

			@Override
			public List<CommitComment> loadComments(String commit) {
				List<CommitComment> comments = getPullRequest().getCommitComments().get(commit);
				if (comments == null)
					return new ArrayList<>();
				else
					return comments;
			}
			
		};
		
		return new CommentAwareChange(change, getPullRequest().getCommits(), 
				commentLoader, blobLoader, getConcernedComment());
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof CommitCommentRemoved) {
			CommitCommentRemoved commitCommentRemoved = (CommitCommentRemoved) event.getPayload();
			CommitComment comment = commitCommentRemoved.getComment();
			if (comment.equals(getConcernedComment())) {
				PageParameters params = paramsOf(getPullRequest(), oldCommit, newCommit, 
						comment.getPosition().getFilePath(), null);
				setResponsePage(RequestComparePage.class, params);
			}
		}
	}

	private CommitComment getConcernedComment() {
		return concernedCommentModel.getObject();
	}

	private static class CommitDescription implements Serializable {
		private final String name;
		
		private final String subject;
		
		CommitDescription(final @Nullable String name, final String subject) {
			this.name = name;
			this.subject = subject;
		}

		public @Nullable String getName() {
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
			
			IModel<List<Map.Entry<String, CommitDescription>>> model = new LoadableDetachableModel<List<Map.Entry<String, CommitDescription>>>() {

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
										paramsOf(getPullRequest(), entry.getKey(), newCommit, filePath, getConcernedComment()));
							} else {
								setResponsePage(RequestComparePage.class, 
										paramsOf(getPullRequest(), oldCommit, entry.getKey(), filePath, getConcernedComment()));
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

		@Override
		public void renderHead(IHeaderResponse response) {
			super.renderHead(response);
			String script = String.format(""
					+ "$('#%s').closest('.dropdown-panel').on('show', function() {"
					+ "		var $ul = $(this).find('ul');"
					+ "		$ul.scrollTop($ul[0].scrollHeight);"
					+ "});", getMarkupId());
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
		
	}
	
	private class BlobLink extends AjaxLink<Void> {
		
		private final Change change;
		
		BlobLink(String id, Change change) {
			super(id);
			
			this.change = change;
		}

		@Override
		protected void onComponentTag(ComponentTag tag) {
			super.onComponentTag(tag);
			
			Change activeChange = getActiveChange();
			if (activeChange != null 
					&& Objects.equals(change.getOldPath(), activeChange.getOldPath()) 
					&& Objects.equals(change.getNewPath(), activeChange.getNewPath())) {
				tag.put("class", "active");
			}
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			onChangeSelected(target, change);
		}
		
	}

	private void onChangeSelected(AjaxRequestTarget target, Change change) {
		filePath = change.getPath();
		BlobDiffPanel diffPanel = new BlobDiffPanel(BLOB_DIFF_ID, repoModel, 
				loadComments(new RevAwareChange(change, oldCommit, newCommit)));
		getPage().replace(diffPanel);
		target.add(diffPanel);

		target.add(get(HEAD_ID).get(PREV_CHANGE_ID));
		target.add(get(HEAD_ID).get(NEXT_CHANGE_ID));
		target.add(get(HEAD_ID).get(CHANGES_TRIGGER_ID).get(LABEL_ID));
		
		Component changesDropdown = get(HEAD_ID).get(CHANGES_DROPDOWN_ID);
		String script = String.format("$('#%s').find('a.active').removeClass('active');", 
				changesDropdown.getMarkupId());
		target.prependJavaScript(script);
		
		target.add(this);
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
}
