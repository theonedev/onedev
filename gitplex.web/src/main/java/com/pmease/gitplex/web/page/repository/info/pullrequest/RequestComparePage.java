package com.pmease.gitplex.web.page.repository.info.pullrequest;

import static com.pmease.commons.git.Change.Status.UNCHANGED;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.git.BlobText;
import com.pmease.commons.git.Change;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.commons.git.TreeNode;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.BlobLoader;
import com.pmease.gitplex.core.comment.ChangeComments;
import com.pmease.gitplex.core.comment.CommentLoader;
import com.pmease.gitplex.core.model.CommitComment;
import com.pmease.gitplex.core.model.IntegrationInfo;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.web.component.diff.BlobDiffPanel;
import com.pmease.gitplex.web.component.diff.ChangedFilesPanel;
import com.pmease.gitplex.web.component.diff.DiffTreePanel;

@SuppressWarnings("serial")
public class RequestComparePage extends RequestDetailPage {

	private String originalCommit;
	
	private String revisedCommit;
	
	private String filePath;
	
	private Long commentId;
	
	private boolean changedOnly = true;
	
	private IModel<CommitComment> commentModel = new LoadableDetachableModel<CommitComment>() {

		@Override
		protected CommitComment load() {
			Preconditions.checkNotNull(commentId);
			return GitPlex.getInstance(Dao.class).load(CommitComment.class, commentId);
		}
		
	};
	
	// map commit name to comit hash
	private IModel<Map<String, CommitDescription>> commitsModel = 
			new LoadableDetachableModel<Map<String, CommitDescription>>() {

		@Override
		protected LinkedHashMap<String, CommitDescription> load() {
			LinkedHashMap<String, CommitDescription> choices = new LinkedHashMap<>();
			PullRequest request = getPullRequest();
			
			CommitDescription description = new CommitDescription("Base of Pull Request", 
					getRepository().git().showRevision(request.getBaseCommit()).getSubject());
			choices.put(request.getBaseCommit(), description);
			
			for (int i=request.getSortedUpdates().size()-1; i>=0; i--) {
				PullRequestUpdate update = request.getSortedUpdates().get(i);
				int updateNo = request.getSortedUpdates().size()-i;
				int j = 0;
				for (Commit commit: update.getCommits()) {
					if (j == update.getCommits().size()-1)
						description = new CommitDescription("Head of Update #" + updateNo, commit.getSubject());
					else
						description = new CommitDescription(null, commit.getSubject());
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
					&& !integrationInfo.getIntegrationHead().equals(integrationInfo.getRequestHead())) { 
				Commit commit = getRepository().git().showRevision(request.getIntegrationInfo().getIntegrationHead());
				choices.put(request.getIntegrationInfo().getIntegrationHead(), 
						new CommitDescription("Integration Preview", commit.getSubject()));
			}
			
			return choices;
		}
		
	};
	
	private IModel<List<Change>> changesModel = new LoadableDetachableModel<List<Change>>() {

		@Override
		protected List<Change> load() {
			return getRepository().git().listFileChanges(originalCommit, revisedCommit, null, true);
		}
		
	};
	
	public RequestComparePage(PageParameters params) {
		super(params);

		filePath = params.get("path").toString();
		commentId = params.get("comment").toLongObject();
		
		originalCommit = params.get("original").toString();
		revisedCommit = params.get("revised").toString();

		if (!(originalCommit == null && revisedCommit == null || originalCommit != null && revisedCommit != null))
			throw new IllegalArgumentException("Param 'original' and 'revised' should be specified both or none.");

		if (originalCommit != null) {
			if (!commitsModel.getObject().containsKey(originalCommit))
				throw new IllegalArgumentException("Commit '" + originalCommit + "' is not relevant to current pull request.");
		}

		if (revisedCommit != null) {
			if (!commitsModel.getObject().containsKey(revisedCommit))
				throw new IllegalArgumentException("Commit '" + revisedCommit + "' is not relevant to current pull request.");
		}
		
		if (filePath != null && originalCommit == null)
			throw new IllegalArgumentException("Param 'path' can only be used together with param 'original' and 'revised'.");
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer commentActions = new WebMarkupContainer("commentActions");
		commentActions.setOutputMarkupId(true);
		add(commentActions);
		
		WebMarkupContainer baseSelector = new WebMarkupContainer("originalSelector");
		add(baseSelector);
		baseSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(originalCommit);
				Preconditions.checkNotNull(description);
				if (description.getName() != null)
					return GitUtils.abbreviateSHA(originalCommit) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(originalCommit);
			}
			
		}));
		DropdownPanel baseChoicesDropdown = new DropdownPanel("originalChoices", false) {

			@Override
			protected Component newContent(String id) {
				return new CommitChoicePanel(id, true);
			}
			
		}; 
		add(baseChoicesDropdown);
		baseSelector.add(new DropdownBehavior(baseChoicesDropdown).alignWithTrigger(0, 0, 0, 100));
		
		baseSelector.add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(originalCommit);
				Preconditions.checkNotNull(description);
				return description.getSubject();
			}
			
		}));
		
		WebMarkupContainer headSelector = new WebMarkupContainer("revisedSelector");
		add(headSelector);
		headSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(revisedCommit);
				Preconditions.checkNotNull(description);
				if (description.getName() != null)
					return GitUtils.abbreviateSHA(revisedCommit) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(revisedCommit);
			}
			
		}));
		DropdownPanel headChoicesDropdown = new DropdownPanel("revisedChoices", false) {

			@Override
			protected Component newContent(String id) {
				return new CommitChoicePanel(id, false);
			}
			
		}; 
		add(headChoicesDropdown);
		headSelector.add(new DropdownBehavior(headChoicesDropdown).alignWithTrigger(0, 0, 0, 100));
		
		headSelector.add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(revisedCommit);
				Preconditions.checkNotNull(description);
				return description.getSubject();
			}
			
		}));

		add(new CheckBox("changedOnly", new PropertyModel<Boolean>(this, "changedOnly"))
				.add(new OnChangeAjaxBehavior() {
					
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						Component compareResult = newCompareResultComponent();
						RequestComparePage.this.replace(compareResult);
						target.add(compareResult);
					}
					
				}));
		
		int lineNo = -1;
		ChangeComments comments = null;
		RevAwareChange change = null;
		if (originalCommit != null) {
			List<Change> changes = changesModel.getObject();
			if (filePath != null) {
				for (Change each: changes) {
					if (filePath.equals(each.getOldPath()) || filePath.equals(each.getNewPath())) {
						change = new RevAwareChange(each, originalCommit, revisedCommit);
						break;
					}
				}
				if (change == null) {
					List<TreeNode> result = getRepository().git().listTree(revisedCommit, filePath);
					if (!result.isEmpty() && result.get(0).getMode() != FileMode.TYPE_TREE) {
						TreeNode blobNode = result.get(0);
						change = new RevAwareChange(UNCHANGED, filePath, filePath, 
								blobNode.getMode(), blobNode.getMode(), originalCommit, revisedCommit);
						changedOnly = false;
					}
				}
			} else {
				if (commentId != null) {
					CommitComment comment = commentModel.getObject();
				}				
				if (change == null && !changes.isEmpty())
					change = new RevAwareChange(changes.get(0), originalCommit, revisedCommit);
			}
		} else if (commentId == null) {
			originalCommit = getPullRequest().getBaseCommit();
			revisedCommit = getPullRequest().getLatestUpdate().getHeadCommit();
		} else {
			
		}

		if (change != null) {
			BlobDiffPanel blobDiffPanel = new BlobDiffPanel("blobDiff", repoModel, 
					change, loadComments(change), lineNo);
			blobDiffPanel.setOutputMarkupId(true);
			add(blobDiffPanel);
		} else {
			add(new WebMarkupContainer("blobDiff").setOutputMarkupId(true));
		}

		add(newCompareResultComponent());
	}
	
	private Component newCompareResultComponent() {
		Component compareResult;
		if (changedOnly) {
			if (!changesModel.getObject().isEmpty()) {
				compareResult = new ChangedFilesPanel("compareResult", changesModel) {
					
					@Override
					protected WebMarkupContainer newBlobLink(String id, Change change) {
						return new BlobLink(id, change);
					}
				};
			} else {
				compareResult = new Label("compareResult", "<i class='fa fa-info-circle'></i> <em>Nothing changed</em>");
				compareResult.setEscapeModelStrings(false);
			}
		} else {
			compareResult = new DiffTreePanel("compareResult", repoModel, changesModel, originalCommit, revisedCommit) {

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
		compareResult.setOutputMarkupId(true);
		return compareResult;
	}
	
	private RevAwareChange getActiveChange() {
		Component panel = getPage().get("blobDiff");
		if (panel instanceof BlobDiffPanel)
			return ((BlobDiffPanel) panel).getChange();
		else
			return null;
	}
	
	@Override
	public void onDetach() {
		commitsModel.detach();
		changesModel.detach();
		commentModel.detach();
		
		super.onDetach();
	}
	
	private ChangeComments loadComments(RevAwareChange change) {
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
		LinkedHashMap<String, Date> commits = new LinkedHashMap<>();
		String baseCommit = getPullRequest().getBaseCommit();
		commits.put(baseCommit, getRepository().git().showRevision(baseCommit).getCommitter().getWhen());
		for (int i=getPullRequest().getSortedUpdates().size()-1; i>=0; i--) {
			for (Commit commit: getPullRequest().getSortedUpdates().get(i).getCommits())
				commits.put(commit.getHash(), commit.getCommitter().getWhen());
		}
		
		return new ChangeComments(change, commits, commentLoader, blobLoader);  
	}

	public static PageParameters params4(PullRequest request, @Nullable String originalCommit, 
			@Nullable String revisedCommit, @Nullable String filePath, @Nullable Long commentId) {
		PageParameters params = RequestDetailPage.params4(request);
		
		if (originalCommit != null)
			params.set("original", originalCommit);
		if (revisedCommit != null)
			params.set("revised", revisedCommit);
		if (filePath != null)
			params.set("path", filePath);
		if (commentId != null)
			params.set("comment", commentId);
		
		return params;
	}
	
	private static class CommitDescription implements Serializable {
		private final String name;
		
		private final String subject;
		
		public CommitDescription(final @Nullable String name, final String subject) {
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
		
		public CommitChoicePanel(String id, boolean forBase) {
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
										params4(getPullRequest(), entry.getKey(), revisedCommit, filePath, commentId));
							} else {
								setResponsePage(RequestComparePage.class, 
										params4(getPullRequest(), originalCommit, entry.getKey(), filePath, commentId));
							}
						}
						
					};
					Map.Entry<String, CommitDescription> entry = item.getModelObject();
					String label = GitUtils.abbreviateSHA(entry.getKey());
					if (entry.getValue().getName() != null)
						label += " - " + entry.getValue().getName();
					link.add(new Label("label", label));
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
		
		public BlobLink(String id, Change change) {
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
			filePath = change.getPath();
			
			RevAwareChange revAwareChange = new RevAwareChange(change, originalCommit, revisedCommit);
			BlobDiffPanel panel = new BlobDiffPanel("blobDiff", repoModel, 
					revAwareChange, loadComments(revAwareChange), -1);
			panel.setOutputMarkupId(true);
			RequestComparePage.this.replace(panel);
			
			Component compareResult = getPage().get("compareResult");
			String script = String.format("$('#%s').find('a.active').removeClass('active');", compareResult.getMarkupId());
			target.prependJavaScript(script);
			
			target.add(panel);
			target.add(this);
			target.add(getPage().get("commentActions"));
		}
		
	}
}
