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
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Change;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.commons.git.TreeNode;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.behavior.menu.MenuBehavior;
import com.pmease.commons.wicket.behavior.menu.MenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.CommentAwareChange;
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
	
	private static final String CONCERN_COMMENT_ID = "concernComment";
	
	private static final String COMPARE_RESULT_ID = "compareResult";
	
	private static final String BLOB_DIFF_ID = "blobDiff";
	
	private String oldCommit;
	
	private String newCommit;
	
	private String filePath;
	
	private final IModel<CommitComment> concernedCommentModel;
	
	private final IModel<Map<String, CommitDescription>> commitsModel = 
			new LoadableDetachableModel<Map<String, CommitDescription>>() {

		@Override
		protected LinkedHashMap<String, CommitDescription> load() {
			LinkedHashMap<String, CommitDescription> choices = new LinkedHashMap<>();
			PullRequest request = getPullRequest();

			String concernedCommit;
			if (getConcernedComment() != null)
				concernedCommit = getConcernedComment().getCommit();
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
		final Long concernedCommentId = params.get("comment").toOptionalLong();
		
		concernedCommentModel = new LoadableDetachableModel<CommitComment>() {

			@Override
			protected CommitComment load() {
				if (concernedCommentId != null) 
					return GitPlex.getInstance(Dao.class).load(CommitComment.class, concernedCommentId);
				else
					return null;
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
		head.setOutputMarkupId(true);
		add(head);

		AjaxLink<Void> link = new AjaxLink<Void>(CONCERN_COMMENT_ID) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				filePath = null;
				CommentAwareChange change = resolveChange(getConcernedComment());
				if (change != null) {
					Component compareResult = newCompareResult(change.getStatus() != UNCHANGED);
					getPage().replace(compareResult);
					target.add(compareResult);
					
					BlobDiffPanel diffPanel = new BlobDiffPanel(BLOB_DIFF_ID, repoModel, change);
					getPage().replace(diffPanel);
					target.add(diffPanel);
					
					target.add(this);
				} else {
					PageParameters params = paramsOf(getPullRequest(), null, null, null, getConcernedComment());
					setResponsePage(RequestComparePage.class, params);
				}
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(getConcernedComment() != null);
			}
			
		};
		link.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Component panel = getPage().get(BLOB_DIFF_ID);
				if (panel instanceof BlobDiffPanel) {
					CommentAwareChange change = ((BlobDiffPanel) panel).getChange();
					if (change.contains(getConcernedComment())) 
						return "Displaying concerned comments by comparing";
				}
				
				return "Click to display concerned comments";
			}

		}));
		link.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Component panel = getPage().get(BLOB_DIFF_ID);
				if (panel instanceof BlobDiffPanel) {
					CommentAwareChange change = ((BlobDiffPanel) panel).getChange();
					if (change.contains(getConcernedComment())) 
						return "btn-primary";
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
			
		}).add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(oldCommit);
				Preconditions.checkNotNull(description);
				return description.getSubject();
			}
			
		})));
		
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
					return GitUtils.abbreviateSHA(newCommit) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(newCommit);
			}
			
		}).add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(newCommit);
				Preconditions.checkNotNull(description);
				return description.getSubject();
			}
			
		})));
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
				if (getConcernedComment() != null) {
					final String latestCommit = getPullRequest().getLatestUpdate().getHeadCommit();
					if (!getConcernedComment().getCommit().equals(latestCommit)) {
						items.add(new ComparisonChoiceItem("Concerned", "Latest") {

							@Override
							protected void onSelect() {
								PageParameters params = paramsOf(getPullRequest(), 
										getConcernedComment().getCommit(), latestCommit, null, getConcernedComment());
								setResponsePage(RequestComparePage.class, params);
							}
							
						});
					}
					final String baseCommit = getPullRequest().getBaseCommit();
					if (!getConcernedComment().getCommit().equals(baseCommit)) {
						items.add(new ComparisonChoiceItem("Base", "Concerned") {

							@Override
							protected void onSelect() {
								PageParameters params = paramsOf(getPullRequest(), 
										baseCommit, getConcernedComment().getCommit(), null, getConcernedComment());
								setResponsePage(RequestComparePage.class, params);
							}

						});
					}
				}
				items.add(new ComparisonChoiceItem("Base", "Latest") {

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
						&& !integrationInfo.getIntegrationHead().equals(integrationInfo.getRequestHead())) { 
					items.add(new ComparisonChoiceItem("Latest", "Integration Preview") {

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
		
		CommentAwareChange change = resolveChange();
		if (change != null) {
			BlobDiffPanel blobDiffPanel = new BlobDiffPanel(BLOB_DIFF_ID, repoModel, change);
			blobDiffPanel.setOutputMarkupId(true);
			add(blobDiffPanel);
		} else {
			add(new WebMarkupContainer(BLOB_DIFF_ID).setOutputMarkupId(true));
		}
		
		boolean changesOnly = (change == null || change.getStatus() != UNCHANGED);
		add(new CheckBox("changesOnly", Model.of(changesOnly)).add(new OnChangeAjaxBehavior() {
					
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				Component compareResult = getPage().get(COMPARE_RESULT_ID);
				compareResult = newCompareResult(!(compareResult instanceof ChangedFilesPanel));
				RequestComparePage.this.replace(compareResult);
				target.add(compareResult);
			}
			
		}));

		add(newCompareResult(changesOnly));
	}
	
	private CommentAwareChange resolveChange() {
		CommentAwareChange change = null;
		if (oldCommit != null) {
			if (filePath != null) {
				change = resolveChange(filePath);
			} else {
				if (getConcernedComment() != null)
					change = resolveChange(getConcernedComment());
				if (change == null) {
					List<Change> changes = getRepository().getChanges(oldCommit, newCommit);
					if (!changes.isEmpty()) 
						change = loadComments(new RevAwareChange(changes.get(0), oldCommit, newCommit));
				}
			}
		} else if (getConcernedComment() == null) {
			oldCommit = getPullRequest().getBaseCommit();
			newCommit = getPullRequest().getLatestUpdate().getHeadCommit();
			List<Change> changes = getRepository().getChanges(oldCommit, newCommit);
			if (!changes.isEmpty())
				change = loadComments(new RevAwareChange(changes.get(0), oldCommit, newCommit));
		} else {
			CommitComment comment = getConcernedComment();
			if (commitsModel.getObject().containsKey(comment.getOldCommit())) {
				oldCommit = comment.getOldCommit();
				newCommit = comment.getCommit();
			} else if (commitsModel.getObject().containsKey(comment.getNewCommit())) {
				newCommit = comment.getNewCommit();
				oldCommit = comment.getCommit();
			} else if (!comment.getCommit().equals(getPullRequest().getLatestUpdate().getHeadCommit())) {
				oldCommit = comment.getCommit();
				newCommit = getPullRequest().getLatestUpdate().getHeadCommit();
			} else {
				oldCommit = getPullRequest().getBaseCommit();
				newCommit = comment.getCommit();
			}
			change = resolveChange(comment);
			Preconditions.checkNotNull(change);
		}

		return change;
	}

	@Nullable
	private CommentAwareChange resolveChange(CommitComment comment) {
		if (!comment.getCommit().equals(oldCommit) && !comment.getCommit().equals(newCommit))
			return null;
		
		boolean enableOldComments = getPullRequest().getCommits().contains(oldCommit); 
		
		boolean enableNewComments = getPullRequest().getCommits().contains(newCommit);

		String path = comment.getPosition().getFilePath();
		for (Change each: getRepository().getChanges(oldCommit, newCommit)) {
			if (path.equals(each.getPath())) {
				CommentAwareChange change = new CommentAwareChange(
						getPullRequest().getTarget().getRepository(),
						new RevAwareChange(each, oldCommit, newCommit), 
						enableOldComments, enableNewComments, comment);
				return change;
			}
		}
		List<TreeNode> nodes = getRepository().git().listTree(newCommit, comment.getPosition().getFilePath());
		Preconditions.checkState(!nodes.isEmpty() && nodes.get(0).getMode() != FileMode.TYPE_TREE);
		RevAwareChange change = new RevAwareChange(UNCHANGED, path, path, 
				nodes.get(0).getMode(), nodes.get(0).getMode(), oldCommit, newCommit);
		CommentAwareChange commentAwareChange = new CommentAwareChange(
				getPullRequest().getTarget().getRepository(), change, 
				enableOldComments, enableNewComments, comment);
		return commentAwareChange;
	}
	
	private CommentAwareChange resolveChange(String filePath) {
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
		concernedCommentModel.detach();
		
		super.onDetach();
	}
	
	public static PageParameters paramsOf(PullRequest request, @Nullable String oldCommit, 
			@Nullable String newCommit, @Nullable String filePath, @Nullable CommitComment concernedComment) {
		PageParameters params = RequestDetailPage.params4(request);
		
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
		boolean enableOldComments = getPullRequest().getCommits().contains(change.getOldRevision());
		boolean enableNewComments = getPullRequest().getCommits().contains(change.getNewRevision());
		return new CommentAwareChange(getRepository(), change, enableOldComments, 
				enableNewComments, getConcernedComment());
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
					String hash = GitUtils.abbreviateSHA(entry.getKey());
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
			filePath = change.getPath();
			RevAwareChange revAwareChange = new RevAwareChange(change, oldCommit, newCommit);

			BlobDiffPanel diffPanel = new BlobDiffPanel(BLOB_DIFF_ID, repoModel, loadComments(revAwareChange));
			getPage().replace(diffPanel);
			target.add(diffPanel);
			
			Component compareResult = getPage().get(COMPARE_RESULT_ID);
			String script = String.format("$('#%s').find('a.active').removeClass('active');", 
					compareResult.getMarkupId());
			target.prependJavaScript(script);
			
			target.add(this);

			if (getConcernedComment() != null)
				target.add(getPage().get(HEAD_ID).get(CONCERN_COMMENT_ID));
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
}
