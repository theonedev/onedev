package com.pmease.gitplex.web.page.repository.info.pullrequest;

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
import com.pmease.commons.git.Change;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.TreeNode;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitplex.core.model.IntegrationInfo;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.web.component.diff.BlobDiffInfo;
import com.pmease.gitplex.web.component.diff.BlobDiffPanel;
import com.pmease.gitplex.web.component.diff.ChangedFilesPanel;
import com.pmease.gitplex.web.component.diff.DiffTreePanel;

@SuppressWarnings("serial")
public class RequestComparePage extends RequestDetailPage {

	// base commit represents comparison base
	private String baseCommit;
	
	// head commit represents comparison head
	private String headCommit;
	
	private String filePath;
	
	private boolean changedOnly = true;
	
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
			return getRepository().git().listFileChanges(baseCommit, headCommit, null, true);
		}
		
	};
	
	public RequestComparePage(PageParameters params) {
		super(params);
		
		baseCommit = params.get("base").toString();
		headCommit = params.get("head").toString();
		
		if (baseCommit != null) {
			if (!commitsModel.getObject().containsKey(baseCommit))
				throw new IllegalArgumentException("Commit '" + baseCommit + "' is not relevant to current pull request.");
		} else {
			baseCommit = getPullRequest().getBaseCommit();
		}
		if (headCommit != null) {
			if (!commitsModel.getObject().containsKey(headCommit))
				throw new IllegalArgumentException("Commit '" + headCommit + "' is not relevant to current pull request.");
		} else {
			headCommit = getPullRequest().getLatestUpdate().getHeadCommit();
		}
		
		filePath = params.get("path").toString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer baseSelector = new WebMarkupContainer("baseSelector");
		add(baseSelector);
		baseSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(baseCommit);
				Preconditions.checkNotNull(description);
				if (description.getName() != null)
					return GitUtils.abbreviateSHA(baseCommit) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(baseCommit);
			}
			
		}));
		DropdownPanel baseChoicesDropdown = new DropdownPanel("baseChoices", false) {

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
				CommitDescription description = commitsModel.getObject().get(baseCommit);
				Preconditions.checkNotNull(description);
				return description.getSubject();
			}
			
		}));
		
		WebMarkupContainer headSelector = new WebMarkupContainer("headSelector");
		add(headSelector);
		headSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(headCommit);
				Preconditions.checkNotNull(description);
				if (description.getName() != null)
					return GitUtils.abbreviateSHA(headCommit) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(headCommit);
			}
			
		}));
		DropdownPanel headChoicesDropdown = new DropdownPanel("headChoices", false) {

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
				CommitDescription description = commitsModel.getObject().get(headCommit);
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
		
		BlobDiffInfo blobDiffInfo = null;
		List<Change> changes = changesModel.getObject();
		if (filePath != null) {
			for (Change change: changes) {
				if (filePath.equals(change.getOldPath()) || filePath.equals(change.getNewPath())) {
					blobDiffInfo = new BlobDiffInfo(change, baseCommit, headCommit);
					break;
				}
			}
			if (blobDiffInfo == null) {
				List<TreeNode> result = getRepository().git().listTree(headCommit, filePath);
				if (!result.isEmpty() && result.get(0).getMode() != FileMode.TYPE_TREE) {
					TreeNode blobNode = result.get(0);
					blobDiffInfo = new BlobDiffInfo(BlobDiffInfo.Status.UNCHANGED, filePath, filePath, 
							blobNode.getMode(), blobNode.getMode(), baseCommit, headCommit);
				}
			}
		} else {
			if (!changes.isEmpty())
				blobDiffInfo = new BlobDiffInfo(changes.get(0), baseCommit, headCommit);
		}
		if (blobDiffInfo != null) 
			add(new BlobDiffPanel("blobDiff", repositoryModel, blobDiffInfo).setOutputMarkupId(true));
		else
			add(new WebMarkupContainer("blobDiff").setOutputMarkupId(true));

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
			compareResult = new DiffTreePanel("compareResult", repositoryModel, changesModel, baseCommit, headCommit) {

				@Override
				protected WebMarkupContainer newBlobLink(String id, final Change change) {
					return new BlobLink(id, change);
				}

				@Override
				protected void onInitialize() {
					super.onInitialize();
					
					BlobDiffInfo blobDiffInfo = getBlobDiffInfo();
					if (blobDiffInfo != null)
						reveal(blobDiffInfo);
				}
				
			};
		}
		compareResult.setOutputMarkupId(true);
		return compareResult;
	}
	
	private BlobDiffInfo getBlobDiffInfo() {
		Component panel = getPage().get("blobDiff");
		if (panel instanceof BlobDiffPanel)
			return ((BlobDiffPanel) panel).getBlobDiffInfo();
		else
			return null;
	}
	
	@Override
	public void onDetach() {
		commitsModel.detach();
		changesModel.detach();
		
		super.onDetach();
	}

	public static PageParameters params4(PullRequest request, String baseCommit, String headCommit, @Nullable String filePath) {
		PageParameters params = RequestDetailPage.params4(request);
		
		params.set("base", baseCommit);
		params.set("head", headCommit);
		if (filePath != null)
			params.set("path", filePath);
		
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
										params4(getPullRequest(), entry.getKey(), headCommit, null));
							} else {
								setResponsePage(RequestComparePage.class, 
										params4(getPullRequest(), baseCommit, entry.getKey(), null));
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
			
			BlobDiffInfo blobDiffInfo = getBlobDiffInfo();
			if (blobDiffInfo != null 
					&& Objects.equals(change.getOldPath(), blobDiffInfo.getOldPath()) 
					&& Objects.equals(change.getNewPath(), blobDiffInfo.getNewPath())) {
				tag.put("class", "active");
			}
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			BlobDiffPanel panel = new BlobDiffPanel("blobDiff", repositoryModel, 
					new BlobDiffInfo(change, baseCommit, headCommit));
			panel.setOutputMarkupId(true);
			RequestComparePage.this.replace(panel);
			
			Component compareResult = getPage().get("compareResult");
			String script = String.format("$('#%s').find('a.active').removeClass('active');", compareResult.getMarkupId());
			target.prependJavaScript(script);
			
			target.add(panel);
			target.add(this);
		}
		
	}
}
