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
import org.apache.wicket.behavior.AttributeAppender;
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
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
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
import com.pmease.gitplex.core.comment.CommentAwareChange;
import com.pmease.gitplex.core.comment.CommentLoader;
import com.pmease.gitplex.core.model.CommitComment;
import com.pmease.gitplex.core.model.IntegrationInfo;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.web.component.diff.BlobDiffPanel;
import com.pmease.gitplex.web.component.diff.ChangedFilesPanel;
import com.pmease.gitplex.web.component.diff.DiffTreePanel;
import com.pmease.gitplex.web.event.CommitCommentRemoved;

@SuppressWarnings("serial")
public class RequestComparePage extends RequestDetailPage {

	private static final String HEAD_ID = "compareHead";
	
	private static final String LOCATE_COMMENT_ID = "locateComment";
	
	private static final String COMPARE_RESULT_ID = "compareResult";
	
	private static final String BLOB_DIFF_ID = "blobDiff";
	
	private String oldCommit;
	
	private String newCommit;
	
	private String filePath;
	
	private Long commentId;
	
	private IModel<CommitComment> commentModel = new LoadableDetachableModel<CommitComment>() {

		@Override
		protected CommitComment load() {
			Preconditions.checkNotNull(commentId);
			return GitPlex.getInstance(Dao.class).load(CommitComment.class, commentId);
		}
		
	};
	
	private IModel<Map<String, CommitDescription>> commitsModel = 
			new LoadableDetachableModel<Map<String, CommitDescription>>() {

		@Override
		protected LinkedHashMap<String, CommitDescription> load() {
			LinkedHashMap<String, CommitDescription> choices = new LinkedHashMap<>();
			PullRequest request = getPullRequest();

			String concernedCommit;
			if (commentId != null)
				concernedCommit = commentModel.getObject().getCommit();
			else
				concernedCommit = null;

			String name = "Base of Pull Request";
			if (request.getBaseCommit().equals(concernedCommit))
				name += " - Concerned";
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
						if (commit.getHash().equals(concernedCommit))
							name += " - Concerned";
						description = new CommitDescription(name, commit.getSubject());
					} else {
						if (commit.getHash().equals(concernedCommit))
							description = new CommitDescription("Concerned", commit.getSubject());
						else
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
					&& !integrationInfo.getIntegrationHead().equals(integrationInfo.getRequestHead())) { 
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
		commentId = params.get("comment").toOptionalLong();
		
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
		head.setOutputMarkupId(true);
		add(head);

		AjaxLink<Void> link = new AjaxLink<Void>(LOCATE_COMMENT_ID) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				filePath = null;
				CommitComment comment = commentModel.getObject();
				CommentAwareChange change = getActiveChange();
				if (change == null || !change.contains(comment)) {
					change = locateChange(comment);
					if (change != null) {
						Component compareResult = newCompareResult(change.getStatus() != UNCHANGED);
						getPage().replace(compareResult);
						target.add(compareResult);
						
						BlobDiffPanel diffPanel = new BlobDiffPanel(BLOB_DIFF_ID, repoModel, change, -1);
						getPage().replace(diffPanel);
						target.add(diffPanel);
						
						target.add(this);
					} else {
						PageParameters params = params4(getPullRequest(), null, null, null, commentId);
						setResponsePage(RequestComparePage.class, params);
					}
				}
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(commentId != null);
			}
			
		};
		link.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Component panel = getPage().get(BLOB_DIFF_ID);
				if (panel instanceof BlobDiffPanel) {
					CommentAwareChange change = ((BlobDiffPanel) panel).getChange();
					if (change.contains(commentModel.getObject())) 
						return "Displaying concerned comment line by comparing";
				}
				
				return "Click to display concerned comment line";
			}

		}));
		link.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Component panel = getPage().get(BLOB_DIFF_ID);
				if (panel instanceof BlobDiffPanel) {
					CommentAwareChange change = ((BlobDiffPanel) panel).getChange();
					if (change.contains(commentModel.getObject())) 
						return "btn-default";
				}
				
				return "btn-warning";
			}
			
		}));
		head.add(link);

		WebMarkupContainer oldSelector = new WebMarkupContainer("oldSelector");
		head.add(oldSelector);
		oldSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(oldCommit);
				Preconditions.checkNotNull(description);
				if (description.getName() != null)
					return GitUtils.abbreviateSHA(oldCommit) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(oldCommit);
			}
			
		}));
		DropdownPanel oldChoicesDropdown = new DropdownPanel("oldChoices", false) {

			@Override
			protected Component newContent(String id) {
				return new CommitChoicePanel(id, true);
			}
			
		}; 
		head.add(oldChoicesDropdown);
		oldSelector.add(new DropdownBehavior(oldChoicesDropdown).alignWithTrigger(0, 0, 0, 100));
		
		oldSelector.add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(oldCommit);
				Preconditions.checkNotNull(description);
				return description.getSubject();
			}
			
		}));
		
		WebMarkupContainer newSelector = new WebMarkupContainer("newSelector");
		head.add(newSelector);
		newSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(newCommit);
				Preconditions.checkNotNull(description);
				
				if (description.getName() != null)
					return GitUtils.abbreviateSHA(newCommit) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(newCommit);
			}
			
		}));
		DropdownPanel newChoicesDropdown = new DropdownPanel("newChoices", false) {

			@Override
			protected Component newContent(String id) {
				return new CommitChoicePanel(id, false);
			}
			
		}; 
		head.add(newChoicesDropdown);
		newSelector.add(new DropdownBehavior(newChoicesDropdown).alignWithTrigger(0, 0, 0, 100));
		
		newSelector.add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(newCommit);
				Preconditions.checkNotNull(description);
				return description.getSubject();
			}
			
		}));

		add(new CheckBox("changesOnly").add(new OnChangeAjaxBehavior() {
					
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				Component compareResult = getPage().get(COMPARE_RESULT_ID);
				compareResult = newCompareResult(!(compareResult instanceof ChangedFilesPanel));
				RequestComparePage.this.replace(compareResult);
				target.add(compareResult);
			}
			
		}));

		CommentAwareChange change = locateChange();
		if (change != null) {
			BlobDiffPanel blobDiffPanel = new BlobDiffPanel(BLOB_DIFF_ID, repoModel, change, -1);
			blobDiffPanel.setOutputMarkupId(true);
			add(blobDiffPanel);
		} else {
			add(new WebMarkupContainer(BLOB_DIFF_ID).setOutputMarkupId(true));
		}

		add(newCompareResult(change == null || change.getStatus() != UNCHANGED));
	}
	
	private CommentAwareChange locateChange() {
		CommentAwareChange change = null;
		if (oldCommit != null) {
			if (filePath != null) {
				change = locateChange(filePath);
			} else {
				if (commentId != null)
					change = locateChange(commentModel.getObject());
				if (change == null) {
					List<Change> changes = getRepository().getChanges(oldCommit, newCommit);
					if (!changes.isEmpty()) 
						change = loadComments(new RevAwareChange(changes.get(0), oldCommit, newCommit));
				}
			}
		} else if (commentId == null) {
			oldCommit = getPullRequest().getBaseCommit();
			newCommit = getPullRequest().getLatestUpdate().getHeadCommit();
			List<Change> changes = getRepository().getChanges(oldCommit, newCommit);
			if (!changes.isEmpty())
				change = loadComments(new RevAwareChange(changes.get(0), oldCommit, newCommit));
		} else {
			CommitComment comment = commentModel.getObject();
			oldCommit = getPullRequest().getBaseCommit();
			newCommit = getPullRequest().getLatestUpdate().getHeadCommit();
			change = locateChange(comment);
			if (change == null) {
				oldCommit = comment.getCommit();
				newCommit = getPullRequest().getLatestUpdate().getHeadCommit();
				change = locateChange(comment);
				Preconditions.checkNotNull(change);
			}
		}

		return change;
	}
	
	private CommentAwareChange locateChange(CommitComment comment) {
		String path = comment.getPosition().getFilePath();
		for (Change each: getRepository().getChanges(oldCommit, newCommit)) {
			if (path.equals(each.getPath())) {
				CommentAwareChange change = loadComments(new RevAwareChange(each, oldCommit, newCommit));
				if (change.contains(comment))
					return change;
				else
					return null;
			}
		}
		List<TreeNode> nodes = getRepository().git().listTree(newCommit, comment.getPosition().getFilePath());
		if (!nodes.isEmpty() && nodes.get(0).getMode() != FileMode.TYPE_TREE) {
			RevAwareChange change = new RevAwareChange(UNCHANGED, path, path, 
					nodes.get(0).getMode(), nodes.get(0).getMode(), oldCommit, newCommit);
			CommentAwareChange commentAwareChange = loadComments(change);
			if (commentAwareChange.contains(comment))
				return commentAwareChange;
			else
				return null;
		} else {
			return null;
		}
	}
	
	private CommentAwareChange locateChange(String filePath) {
		for (Change each: getRepository().getChanges(oldCommit, newCommit)) {
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
	
	private Component newCompareResult(boolean changesOnly) {
		Component compareResult;
		if (changesOnly) {
			compareResult = new ChangedFilesPanel(COMPARE_RESULT_ID, repoModel, oldCommit, newCommit) {
				
				@Override
				protected WebMarkupContainer newBlobLink(String id, Change change) {
					return new BlobLink(id, change);
				}
			};
		} else {
			compareResult = new DiffTreePanel(COMPARE_RESULT_ID, repoModel, oldCommit, newCommit) {

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
		commentModel.detach();
		
		super.onDetach();
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
		LinkedHashMap<String, Date> commits = new LinkedHashMap<>();
		String baseCommit = getPullRequest().getBaseCommit();
		commits.put(baseCommit, getRepository().git().showRevision(baseCommit).getCommitter().getWhen());
		for (int i=getPullRequest().getSortedUpdates().size()-1; i>=0; i--) {
			for (Commit commit: getPullRequest().getSortedUpdates().get(i).getCommits())
				commits.put(commit.getHash(), commit.getCommitter().getWhen());
		}
		
		return new CommentAwareChange(change, commits, commentLoader, blobLoader);  
	}
	
	public static PageParameters params4(PullRequest request, @Nullable String oldCommit, 
			@Nullable String newCommit, @Nullable String filePath, @Nullable Long commentId) {
		PageParameters params = RequestDetailPage.params4(request);
		
		if (oldCommit != null)
			params.set("original", oldCommit);
		if (newCommit != null)
			params.set("revised", newCommit);
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
										params4(getPullRequest(), entry.getKey(), newCommit, filePath, commentId));
							} else {
								setResponsePage(RequestComparePage.class, 
										params4(getPullRequest(), oldCommit, entry.getKey(), filePath, commentId));
							}
						}
						
					};
					Map.Entry<String, CommitDescription> entry = item.getModelObject();
					String label = GitUtils.abbreviateSHA(entry.getKey());
					String name = entry.getValue().getName();
					if (name != null) {
						label += " - " + name;
						name = name.toLowerCase();
						if (name.contains("base of "))
							item.add(AttributeAppender.append("class", " base special"));
						if (name.contains("concerned"))
							item.add(AttributeAppender.append("class", " concerned special"));
						if (name.contains("head of update"))
							item.add(AttributeAppender.append("class", " update-head special"));
						if (name.contains("integration preview"))
							item.add(AttributeAppender.append("class", " integration-preview special"));
						if (name.contains("head of target"))
							item.add(AttributeAppender.append("class", " target-head special"));
					}
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
			RevAwareChange revAwareChange = new RevAwareChange(change, oldCommit, newCommit);

			BlobDiffPanel diffPanel = new BlobDiffPanel(BLOB_DIFF_ID, repoModel, 
					loadComments(revAwareChange), -1);
			getPage().replace(diffPanel);
			target.add(diffPanel);
			
			Component compareResult = getPage().get(COMPARE_RESULT_ID);
			String script = String.format("$('#%s').find('a.active').removeClass('active');", 
					compareResult.getMarkupId());
			target.prependJavaScript(script);
			
			target.add(this);

			if (commentId != null)
				target.add(getPage().get(HEAD_ID).get(LOCATE_COMMENT_ID));
		}
		
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event instanceof CommitCommentRemoved) {
			CommitCommentRemoved commitCommentRemoved = (CommitCommentRemoved) event;
			CommitComment comment = commitCommentRemoved.getComment();
			if (comment.getId().equals(commentId)) {
				commentId = null;
				commitCommentRemoved.getTarget().add(get(HEAD_ID));
			}
		}
	}
	
}
