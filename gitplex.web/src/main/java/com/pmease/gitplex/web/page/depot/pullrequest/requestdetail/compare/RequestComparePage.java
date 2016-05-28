package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.compare;

import static com.pmease.gitplex.core.entity.PullRequest.Event.INTEGRATION_PREVIEW_CALCULATED;
import static com.pmease.gitplex.core.entity.PullRequest.Event.UPDATED;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.lang.diff.WhitespaceOption;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.ajaxlistener.IndicateLoadingListener;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.floating.AlignPlacement;
import com.pmease.commons.wicket.component.floating.FloatingPanel;
import com.pmease.commons.wicket.component.menu.MenuItem;
import com.pmease.commons.wicket.component.menu.MenuLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.component.IntegrationPreview;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.web.component.diff.revision.DiffMark;
import com.pmease.gitplex.web.component.diff.revision.MarkSupport;
import com.pmease.gitplex.web.component.diff.revision.RevisionDiffPanel;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestlist.RequestListPage;
import com.pmease.gitplex.web.websocket.PullRequestChanged;

@SuppressWarnings("serial")
public class RequestComparePage extends RequestDetailPage implements MarkSupport {

	private static final String PARAM_OLD_REV = "old-rev";
	
	private static final String PARAM_NEW_REV = "new-rev";
	
	private static final String PARAM_WHITESPACE_OPTION = "whitespace-option";
	
	private static final String PARAM_PATH_FILTER = "path-filter";
	
	private static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_MARK = "mark";
	
	public static final String REV_BASE = "base";
	
	public static final String REV_UPDATE_PREFIX = "update";
	
	public static final String REV_LAST_UPDATE_PREFIX = "lastUpdate";
	
	public static final String REV_INTEGRATION_PREVIEW = "integrationPreview";
	
	public static final String REV_TARGET_BRANCH = "targetBranch";
	
	private State state = new State();

	// below fields duplicates some members of state field, and they are serving different 
	// purposes and may have different values. State members reflects url parameters, while 
	// below fields records actual value used for comparison. For instance, newCommitHash 
	// in state object can be null to display comparison against latest update, but also 
	// serves as an indication that the out dated alert should be displayed when there are 
	// new updates. 
	private String oldCommitHash;
	
	private String newCommitHash;
	
	private WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
	
	private String pathFilter;
	
	private Long commentId;
	
	private DiffMark mark;
	
	private WebMarkupContainer compareHead;
	
	private Component revisionDiff;
	
	private final IModel<Map<String, CommitDescription>> commitsModel = 
			new LoadableDetachableModel<Map<String, CommitDescription>>() {

		@Override
		protected LinkedHashMap<String, CommitDescription> load() {
			LinkedHashMap<String, CommitDescription> choices = new LinkedHashMap<>();
			PullRequest request = getPullRequest();

			String name = "Pull Request Base";
			CommitDescription description = new CommitDescription(name, request.getBaseCommit().getSubject());
			choices.put(request.getBaseCommitHash(), description);
			
			for (int i=0; i<request.getSortedUpdates().size(); i++) {
				PullRequestUpdate update = request.getSortedUpdates().get(i);
				Commit commit = update.getHeadCommit();
				int updateNo = i+1;
				if (i == request.getSortedUpdates().size()-1)
					name = "Latest Update Head";
				else
					name = "Head of Update " + updateNo;
				description = new CommitDescription(name, commit.getSubject());
				choices.put(commit.getHash(), description);
			}

			if (request.isOpen()) {
				String targetHead = request.getTarget().getObjectName();
				if (!choices.containsKey(targetHead)) {
					description = new CommitDescription("Target Branch Head", 
							getDepot().getCommit(targetHead).getSubject());
					choices.put(targetHead, description);
				}

				IntegrationPreview preview = request.getIntegrationPreview();
				if (preview != null && preview.getIntegrated() != null && 
						!preview.getIntegrated().equals(preview.getRequestHead())) {
					Commit commit = getDepot().getCommit(preview.getIntegrated());
					choices.put(commit.getHash(), new CommitDescription("Integration Preview", commit.getSubject()));
				}
			}
			
			return choices;
		}
		
	};
	
	public RequestComparePage(PageParameters params) {
		super(params);

		state.oldRev = params.get(PARAM_OLD_REV).toString();
		state.newRev = params.get(PARAM_NEW_REV).toString();
		state.pathFilter = params.get(PARAM_PATH_FILTER).toString();
		state.whitespaceOption = WhitespaceOption.of(params.get(PARAM_WHITESPACE_OPTION).toString());
		state.commentId = params.get(PARAM_COMMENT).toOptionalLong();
		state.mark = DiffMark.of(params.get(PARAM_MARK).toString());
		
		initFromState(state);
	}
	
	private void initFromState(State state) {
		oldCommitHash = getCommitHash(state.oldRev);
		newCommitHash = getCommitHash(state.newRev);
		whitespaceOption = state.whitespaceOption;
		pathFilter = state.pathFilter;
		commentId = state.commentId;
		mark = state.mark;
	}
	
	private String getRevision(String commitHash) {
		PullRequest request = getPullRequest();
		if (request.getBaseCommitHash().equals(commitHash)) {
			return REV_BASE;
		} else {
			List<PullRequestUpdate> updates = request.getSortedUpdates();
			for (int i=0; i<updates.size(); i++) {
				if (updates.get(i).getHeadCommitHash().equals(commitHash))
					return REV_UPDATE_PREFIX + (i+1);
			}
			if (commitHash.equals(request.getTarget().getObjectName(false))) {
				return REV_TARGET_BRANCH;
			} else {
				IntegrationPreview preview = request.getIntegrationPreview();
				if (preview != null && commitHash.equals(preview.getIntegrated()))
					return REV_INTEGRATION_PREVIEW;
				else
					return commitHash;
			}
		}
	}
	
	private String getCommitHash(String revision) {
		PullRequest request = getPullRequest();
		if (GitUtils.isHash(revision)) {
			return revision;
		} else if (revision.equals(REV_BASE)) {
			return request.getBaseCommitHash();
		} else if (revision.equals(REV_INTEGRATION_PREVIEW)) {
			IntegrationPreview preview = request.getIntegrationPreview();
			if (preview == null || preview.getIntegrated() == null)
				return request.getLatestUpdate().getHeadCommitHash();
			else
				return preview.getIntegrated();
		} else if (revision.equals(REV_TARGET_BRANCH)) {
			return request.getTarget().getObjectName();
		} else if (revision.startsWith(REV_UPDATE_PREFIX)) {
			int updateNo = Integer.parseInt(revision.substring(REV_UPDATE_PREFIX.length()));
			if (updateNo == 0)
				return request.getBaseCommitHash();
			else
				return request.getSortedUpdates().get(updateNo-1).getHeadCommitHash();
		} else if (revision.startsWith(REV_LAST_UPDATE_PREFIX)) {
			List<PullRequestUpdate> updates = request.getSortedUpdates();
			int lastUpdateNo = Integer.parseInt(revision.substring(REV_LAST_UPDATE_PREFIX.length()));
			int index = updates.size() - lastUpdateNo;
			if (index < 0)
				return request.getBaseCommitHash();
			else
				return updates.get(index).getHeadCommitHash();
		} else {
			throw new IllegalArgumentException("Unrecognized revision: " + revision);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(compareHead = new WebMarkupContainer("compareHead"));

		DropdownLink oldSelector = new DropdownLink("oldSelector") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				CommitDescription description = commitsModel.getObject().get(oldCommitHash);
				if (description != null) 
					tag.put("title", description.getSubject());
				else 
					tag.put("title", getDepot().getCommit(oldCommitHash).getSubject());
			}

			@Override
			protected Component newContent(String id) {
				return new CommitChoicePanel(id) {

					@Override
					protected void onSelect(AjaxRequestTarget target, String commitHash) {
						oldCommitHash = commitHash;
						state.oldRev = getRevision(oldCommitHash);
						state.newRev = getRevision(newCommitHash);
						close();
						onStateChange(target);
					}
					
				};
			}
			
		};
		compareHead.add(oldSelector);
		oldSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(oldCommitHash);
				if (description != null)
					return GitUtils.abbreviateSHA(oldCommitHash) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(oldCommitHash);
			}
			
		}));
		
		DropdownLink newSelector = new DropdownLink("newSelector") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				CommitDescription description = commitsModel.getObject().get(newCommitHash);
				if (description != null)
					tag.put("title", description.getSubject());
				else
					tag.put("title", getDepot().getCommit(newCommitHash).getSubject());
			}

			@Override
			protected Component newContent(String id) {
				return new CommitChoicePanel(id) {

					@Override
					protected void onSelect(AjaxRequestTarget target, String commitHash) {
						newCommitHash = commitHash;
						state.oldRev = getRevision(oldCommitHash);
						state.newRev = getRevision(newCommitHash);
						close();
						onStateChange(target);
					}
					
				};
			}
			
		};
		compareHead.add(newSelector);
		newSelector.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				CommitDescription description = commitsModel.getObject().get(newCommitHash);
				if (description != null)
					return GitUtils.abbreviateSHA(newCommitHash) + " - " + description.getName();
				else
					return GitUtils.abbreviateSHA(newCommitHash);
			}
			
		}));
		
		compareHead.add(new MenuLink("comparisonSelector", new AlignPlacement(50, 100, 50, 0)) {

			@Override
			protected void onInitialize(FloatingPanel dropdown) {
				super.onInitialize(dropdown);
				dropdown.add(AttributeAppender.append("class", " common-comparisons"));
			}

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> items = new ArrayList<>();
				
				items.add(new ComparisonChoiceItem("Changes of whole request") {

					@Override
					protected void onSelect(AjaxRequestTarget target) {
						close();
						
						state.oldRev = REV_BASE;
						state.newRev = REV_LAST_UPDATE_PREFIX + "1";
						initFromState(state);
						onStateChange(target);
					}

				});

				PullRequest request = getPullRequest();
				if (request.isOpen()) {
					final IntegrationPreview preview = request.getIntegrationPreview();
					if (preview != null && preview.getIntegrated() != null) {
						items.add(new ComparisonChoiceItem("Changes of integration preview") {

							@Override
							protected void onSelect(AjaxRequestTarget target) {
								close();
								
								state.oldRev = REV_TARGET_BRANCH;
								state.newRev = REV_INTEGRATION_PREVIEW;
								initFromState(state);
								onStateChange(target);
							}
							
						});
					}
				}

				List<PullRequestUpdate> updates = getPullRequest().getSortedUpdates();
				for (int i=0; i<updates.size(); i++) {
					final int updateNo = i+1;
					
					String label;
					if (updateNo == updates.size()) 
						label = "Changes of latest update";
					else 
						label = "Changes of update " + updateNo;
					items.add(new ComparisonChoiceItem(label) {

						@Override
						protected void onSelect(AjaxRequestTarget target) {
							close();

							state.oldRev = REV_UPDATE_PREFIX + (updateNo-1);
							state.newRev = REV_UPDATE_PREFIX + updateNo;
							initFromState(state);
							onStateChange(target);
						}
						
					});						
				}
				
				return items;
			}
			
		});
		
		compareHead.add(new WebMarkupContainer("outdatedAlert") {

			@Override
			public void onEvent(final IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PullRequestChanged) {
					PullRequestChanged requestChanged = (PullRequestChanged) event.getPayload();
					IPartialPageRequestHandler partialPageRequestHandler = requestChanged.getPartialPageRequestHandler();
					PullRequest.Event requestEvent = requestChanged.getEvent();

					boolean outdated = false;
					if (requestEvent == INTEGRATION_PREVIEW_CALCULATED) {
						outdated = state.oldRev.equals(REV_INTEGRATION_PREVIEW) 
								|| state.newRev.equals(REV_INTEGRATION_PREVIEW);
					} else if (requestEvent == UPDATED) {
						outdated = !GitUtils.isHash(state.oldRev) && state.oldRev.startsWith(REV_LAST_UPDATE_PREFIX)
								|| !GitUtils.isHash(state.newRev) && state.newRev.startsWith(REV_LAST_UPDATE_PREFIX);
					}

					if (outdated)
						setVisible(true);
					
					if (outdated || requestEvent == UPDATED || requestEvent == INTEGRATION_PREVIEW_CALCULATED) {
						partialPageRequestHandler.add(compareHead);
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
		
		Label noIntegrationPreviewAlert = new Label("noIntegrationPreviewAlert", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				PullRequest request = getPullRequest();
				String message;
				if (request.isOpen()) {
					IntegrationPreview preview = getPullRequest().getIntegrationPreview();
					if (preview == null)
						message = "Integration preview calculation is ongoing";
					else
						message = "There are integration conflicts";
				} else {
					message = "Integration preview is not available for closed pull request";
				}
				return "<i class='fa fa-warning'></i> " + message + ", displaying comparison "
						+ "between target branch and latest update instead.";
			}
			
		});
		if (REV_INTEGRATION_PREVIEW.equals(state.oldRev) || REV_INTEGRATION_PREVIEW.equals(state.newRev)) {
			IntegrationPreview preview = getPullRequest().getIntegrationPreview();
			noIntegrationPreviewAlert.setVisible(preview == null || preview.getIntegrated() == null);
		} else {
			noIntegrationPreviewAlert.setVisible(false);
		}
		noIntegrationPreviewAlert.setEscapeModelStrings(false);
		compareHead.add(noIntegrationPreviewAlert);
		
		newCompareResult(null);
	}

	@Override
	public void onDetach() {
		commitsModel.detach();
		super.onDetach();
	}
	
	public static PageParameters paramsOf(PullRequest request, String oldRev, String newRev) {
		State state = new State();
		state.oldRev = oldRev;
		state.newRev = newRev;
		return paramsOf(request, state);
	}
	
	public static PageParameters paramsOf(PullRequest request, State state) {
		PageParameters params = RequestDetailPage.paramsOf(request);

		if (state.oldRev != null)
			params.set(PARAM_OLD_REV, state.oldRev);
		if (state.newRev != null)
			params.set(PARAM_NEW_REV, state.newRev);
		if (state.whitespaceOption != WhitespaceOption.DEFAULT)
			params.set(PARAM_WHITESPACE_OPTION, state.whitespaceOption.name());
		if (state.pathFilter != null)
			params.set(PARAM_PATH_FILTER, state.pathFilter);
		if (state.commentId != null)
			params.set(PARAM_COMMENT, state.commentId);
		if (state.mark != null)
			params.set(PARAM_MARK, state.mark.toString());
		return params;
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
							attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(revisionDiff));
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

		private final String label;
		
		ComparisonChoiceItem(String label) {
			this.label = label;
		}

		@Override
		public String getIconClass() {
			return null;
		}

		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public AbstractLink newLink(String id) {
			return new AjaxLink<Void>(id) {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new IndicateLoadingListener());
					attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(revisionDiff));
				}

				@Override
				public void onClick(AjaxRequestTarget target) {
					onSelect(target);
				}
				
			};
		}

		protected abstract void onSelect(AjaxRequestTarget target);
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);

		state = (State) data;
		initFromState(state);
		
		target.add(compareHead);
		newCompareResult(target);
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(RequestListPage.class, paramsOf(depot));
	}

	private void pushState(IPartialPageRequestHandler partialPageRequestHandler) {
		PageParameters params = paramsOf(getPullRequest(), state);
		CharSequence url = RequestCycle.get().urlFor(RequestComparePage.class, params);
		pushState(partialPageRequestHandler, url.toString(), state);
	}
	
	private void onStateChange(IPartialPageRequestHandler partialPageRequestHandler) {
		pushState(partialPageRequestHandler);
		
		partialPageRequestHandler.add(compareHead);
		newCompareResult(partialPageRequestHandler);
	}
	
	private void newCompareResult(@Nullable IPartialPageRequestHandler partialPageRequestHandler) {
		revisionDiff = new RevisionDiffPanel("revisionDiff", depotModel,  
				requestModel, oldCommitHash, newCommitHash, pathFilter, 
				whitespaceOption, this) {

			@Override
			protected void onPathFilterChange(AjaxRequestTarget target, String pathFilter) {
				pathFilter = state.pathFilter = pathFilter;
				pushState(target);
			}

			@Override
			protected void onWhitespaceOptionChange(AjaxRequestTarget target,
					WhitespaceOption whitespaceOption) {
				whitespaceOption = state.whitespaceOption = whitespaceOption;
				pushState(target);
			}

		};
		revisionDiff.setOutputMarkupId(true);
		if (partialPageRequestHandler != null) {
			replace(revisionDiff);
			partialPageRequestHandler.add(revisionDiff);
		} else {
			add(revisionDiff);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RequestComparePage.class, "request-compare.css")));
	}

	@Override
	public DiffMark getMark() {
		return mark;
	}

	@Override
	public String getMarkUrl(DiffMark mark) {
		State state = new State();
		state.mark = mark;
		state.oldRev = oldCommitHash;
		state.newRev = newCommitHash;
		state.pathFilter = pathFilter;
		state.whitespaceOption = whitespaceOption;
		return urlFor(RequestComparePage.class, paramsOf(getPullRequest(), state)).toString();
	}

	@Override
	public String getCommentUrl(CodeComment comment) {
		State state = new State();
		state.mark = new DiffMark(comment);
		state.commentId = comment.getId();
		state.oldRev = oldCommitHash;
		state.newRev = newCommitHash;
		state.pathFilter = pathFilter;
		state.whitespaceOption = whitespaceOption;
		return urlFor(RequestComparePage.class, paramsOf(getPullRequest(), state)).toString();
	}
	
	@Override
	public CodeComment getOpenComment() {
		if (commentId != null)
			return GitPlex.getInstance(CodeCommentManager.class).load(commentId);
		else
			return null;
	}

	@Override
	public void onCommentOpened(AjaxRequestTarget target, CodeComment comment) {
		commentId = state.commentId = comment.getId();
		mark = state.mark = new DiffMark(comment);
		pushState(target);
	}

	@Override
	public void onCommentClosed(AjaxRequestTarget target) {
		commentId = state.commentId = null;
		pushState(target);
	}

	@Override
	public void onMark(AjaxRequestTarget target, DiffMark mark) {
		this.mark = state.mark = mark;
		pushState(target);
	}

	@Override
	public void onAddComment(AjaxRequestTarget target, DiffMark mark) {
		commentId = state.commentId = null;
		this.mark = state.mark = mark;
		pushState(target);
	}

	public static class State implements Serializable {

		private static final long serialVersionUID = 1L;

		public String oldRev;
		
		public String newRev;
		
		public WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
		
		public String pathFilter;
		
		public Long commentId;
		
		public DiffMark mark;
		
	}

}
