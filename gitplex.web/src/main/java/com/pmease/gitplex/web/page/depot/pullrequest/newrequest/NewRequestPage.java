package com.pmease.gitplex.web.page.depot.pullrequest.newrequest;

import static com.pmease.gitplex.core.entity.PullRequest.Status.INTEGRATED;
import static com.pmease.gitplex.core.entity.PullRequest.Status.PENDING_UPDATE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.lang.diff.WhitespaceOption;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.wicket.behavior.markdown.AttachmentSupport;
import com.pmease.commons.wicket.component.backtotop.BackToTop;
import com.pmease.commons.wicket.component.tabbable.AjaxActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior.PageId;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.entity.PullRequest.Status;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.entity.component.CloseInfo;
import com.pmease.gitplex.core.entity.component.DepotAndBranch;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.security.ObjectPermission;
import com.pmease.gitplex.web.component.BranchLink;
import com.pmease.gitplex.web.component.branchpicker.AffinalBranchPicker;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.comment.DepotAttachmentSupport;
import com.pmease.gitplex.web.component.commitlist.CommitListPanel;
import com.pmease.gitplex.web.component.diff.revision.DiffMark;
import com.pmease.gitplex.web.component.diff.revision.MarkSupport;
import com.pmease.gitplex.web.component.diff.revision.RevisionDiffPanel;
import com.pmease.gitplex.web.component.pullrequest.requestassignee.AssigneeChoice;
import com.pmease.gitplex.web.component.pullrequest.requestreviewer.ReviewerAvatar;
import com.pmease.gitplex.web.component.pullrequest.requestreviewer.ReviewerChoice;
import com.pmease.gitplex.web.model.ReviewersModel;
import com.pmease.gitplex.web.page.depot.NoCommitsPage;
import com.pmease.gitplex.web.page.depot.compare.RevisionComparePage;
import com.pmease.gitplex.web.page.depot.pullrequest.PullRequestPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestlist.RequestListPage;
import com.pmease.gitplex.web.page.security.LoginPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class NewRequestPage extends PullRequestPage implements MarkSupport {

	private static final String TAB_PANEL_ID = "tabPanel";
	
	private DepotAndBranch target;
	
	private DepotAndBranch source;
	
	private IModel<List<Commit>> commitsModel;
	
	private IModel<PullRequest> requestModel;
	
	private Long commentId;
	
	private DiffMark mark;
	
	private String pathFilter;
	
	private String blameFile;
	
	private WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
	
	public static PageParameters paramsOf(Depot depot, DepotAndBranch target, DepotAndBranch source) {
		PageParameters params = paramsOf(depot);
		params.set("target", target.toString());
		params.set("source", source.toString());
		return params;
	}

	public NewRequestPage(PageParameters params) {
		super(params);
		
		if (!getDepot().git().hasRefs()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getDepot()));

		if (params.get("target").toString() != null) {
			target = new DepotAndBranch(params.get("target").toString());
		} else {
			if (getDepot().getForkedFrom() != null) {
				target = new DepotAndBranch(getDepot().getForkedFrom(), 
						getDepot().getForkedFrom().getDefaultBranch());
			} else {
				target = new DepotAndBranch(getDepot(), getDepot().getDefaultBranch());
			}
		}
		
		if (params.get("source").toString() != null) {
			source = new DepotAndBranch(params.get("source").toString());
		} else {
			if (getDepot().getForkedFrom() != null) {
				source = new DepotAndBranch(getDepot(), getDepot().getDefaultBranch());
			} else {
				source = new DepotAndBranch(getDepot(), getDepot().getDefaultBranch());
			}
		}

		Account currentUser = getLoginUser();
		if (currentUser == null)
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
		
		PullRequest pullRequest = GitPlex.getInstance(PullRequestManager.class).findOpen(target, source);
		
		if (pullRequest == null) {
			pullRequest = new PullRequest();
			pullRequest.setTarget(target);
			pullRequest.setSource(source);
			pullRequest.setSubmitter(currentUser);
			
			PullRequestUpdate update = new PullRequestUpdate();
			pullRequest.addUpdate(update);
			update.setRequest(pullRequest);
			update.setHeadCommitHash(source.getObjectName());
			pullRequest.setLastEventDate(new Date());
			
			PullRequestManager pullRequestManager = GitPlex.getInstance(PullRequestManager.class);
			List<IntegrationStrategy> strategies = pullRequestManager.getApplicableIntegrationStrategies(pullRequest);
			Preconditions.checkState(!strategies.isEmpty());
			pullRequest.setIntegrationStrategy(strategies.get(0));
			
			ObjectPermission writePermission = ObjectPermission.ofDepotWrite(getDepot());
			if (currentUser.asSubject().isPermitted(writePermission))
				pullRequest.setAssignee(currentUser);
			else
				pullRequest.setAssignee(getDepot().getAccount());

			if (target.getDepot().equals(source.getDepot())) {
				pullRequest.setBaseCommitHash(pullRequest.git().calcMergeBase(
						target.getObjectName(), source.getObjectName()));			
				if (target.getDepot().isAncestor(source.getObjectName(), target.getObjectName())) {
					CloseInfo closeInfo = new CloseInfo();
					closeInfo.setCloseDate(new Date());
					closeInfo.setCloseStatus(CloseInfo.Status.INTEGRATED);
					pullRequest.setCloseInfo(closeInfo);
				}
			} else {
				Git tempGit = new Git(FileUtils.createTempDir());
				try {
					tempGit.clone(target.getDepot().git(), false, true, true, pullRequest.getTarget().getBranch());
					tempGit.reset(null, null);
					tempGit.fetch(source.getDepot().git(), source.getBranch());
					pullRequest.setBaseCommitHash(tempGit.calcMergeBase(target.getObjectName(), source.getObjectName()));			
				} finally {
					FileUtils.deleteDir(tempGit.depotDir());
				}
				if (pullRequest.getBaseCommitHash().equals(source.getObjectName())) {
					CloseInfo closeInfo = new CloseInfo();
					closeInfo.setCloseDate(new Date());
					closeInfo.setCloseStatus(CloseInfo.Status.INTEGRATED);
					pullRequest.setCloseInfo(closeInfo);
				}
			}
			requestModel = Model.of(pullRequest);
		} else {
			Long requestId = pullRequest.getId();
			requestModel = new LoadableDetachableModel<PullRequest>() {

				@Override
				protected PullRequest load() {
					return GitPlex.getInstance(PullRequestManager.class).load(requestId);
				}
				
			};
			requestModel.setObject(pullRequest);
		}
		
		commitsModel = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				PullRequest request = getPullRequest();
				List<Commit> commits = source.getDepot().git().log(request.getBaseCommitHash(), 
						request.getLatestUpdate().getHeadCommitHash(), null, 0, 0, false);
				Collections.reverse(commits);
				return commits;
			}
			
		};
		
	}
	
	private PullRequest getPullRequest() {
		return requestModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		
		add(new AffinalBranchPicker("target", getDepot().getId(), target.getBranch()) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot depot, String branch) {
				PageParameters params = paramsOf(depot, new DepotAndBranch(depot, branch), source); 
				setResponsePage(NewRequestPage.class, params);
			}
			
		});
		
		add(new AffinalBranchPicker("source", getDepot().getId(), source.getBranch()) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot depot, String branch) {
				PageParameters params = paramsOf(getDepot(), NewRequestPage.this.target,
						new DepotAndBranch(depot, branch)); 
				setResponsePage(NewRequestPage.class, params);
			}
			
		});
		
		add(new Link<Void>("swap") {

			@Override
			public void onClick() {
				PageParameters params = paramsOf(getPullRequest().getSourceDepot(), 
						getPullRequest().getSource(), getPullRequest().getTarget()); 
				setResponsePage(NewRequestPage.class, params);
			}
			
		});
		
		Fragment fragment;
		if (getPullRequest().getId() != null) {
			fragment = newOpenedFrag();
		} else if (getPullRequest().getSource().equals(getPullRequest().getTarget())) {
			fragment = newSameBranchFrag();
		} else if (getPullRequest().getStatus() == INTEGRATED) {
			fragment = newIntegratedFrag();
		} else if (getPullRequest().getStatus() == PENDING_UPDATE) {
			fragment = newRejectedFrag();
		} else {
			fragment = newCanSendFrag();
		}
		add(fragment);

		List<Tab> tabs = new ArrayList<>();
		
		tabs.add(new AjaxActionTab(Model.of("Commits")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				Component panel = newCommitsPanel();
				getPage().replace(panel);
				target.add(panel);
			}
			
		});

		tabs.add(new AjaxActionTab(Model.of("Files")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				Component panel = newRevDiffPanel();
				getPage().replace(panel);
				target.add(panel);
			}
			
		});

		add(new Tabbable("tabs", tabs) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(getPullRequest().getStatus() != INTEGRATED);
			}
			
		});
		
		add(newCommitsPanel());

		add(new BackToTop("backToTop"));
	}
	
	private Component newCommitsPanel() {
		return new CommitListPanel(TAB_PANEL_ID, depotModel, commitsModel) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getStatus() != INTEGRATED);
			}
			
		}.setOutputMarkupId(true);
	}
	
	private RevisionDiffPanel newRevDiffPanel() {
		PullRequest request = getPullRequest();
		
		IModel<Depot> depotModel = new LoadableDetachableModel<Depot>() {

			@Override
			protected Depot load() {
				Depot depot = source.getDepot();
				depot.cacheObjectId(source.getRevision(), 
						ObjectId.fromString(getPullRequest().getLatestUpdate().getHeadCommitHash()));
				return depot;
			}
			
		};
		
		IModel<String> blameModel = new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return blameFile;
			}

			@Override
			public void setObject(String object) {
				blameFile = object;
			}
			
		};
		IModel<String> pathFilterModel = new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return pathFilter;
			}

			@Override
			public void setObject(String object) {
				pathFilter = object;
			}
			
		};
		IModel<WhitespaceOption> whitespaceOptionModel = new IModel<WhitespaceOption>() {

			@Override
			public void detach() {
			}

			@Override
			public WhitespaceOption getObject() {
				return whitespaceOption;
			}

			@Override
			public void setObject(WhitespaceOption object) {
				whitespaceOption = object;
			}
			
		};

		/*
		 * we are passing source revision here instead of head commit hash of latest update
		 * as we want to preserve the branch name in case they are useful at some point 
		 * later. Also it is guaranteed to be resolved to the same commit has as we've cached
		 * it above when loading the depot  
		 */
		RevisionDiffPanel diffPanel = new RevisionDiffPanel(TAB_PANEL_ID, depotModel, 
				new Model<PullRequest>(null), request.getBaseCommitHash(), 
				source.getRevision(), pathFilterModel, whitespaceOptionModel, blameModel, this) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getStatus() != INTEGRATED);
			}
			
		};
		diffPanel.setOutputMarkupId(true);
		return diffPanel;
	}

	private Fragment newOpenedFrag() {
		Fragment fragment = new Fragment("status", "openedFrag", this);
		fragment.add(new Label("no", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getId().toString();
			}
		}));
		fragment.add(new Link<Void>("link") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return getPullRequest().getTitle();
					}
				}));
			}

			@Override
			public void onClick() {
				PageParameters params = RequestDetailPage.paramsOf(getPullRequest());
				setResponsePage(RequestOverviewPage.class, params);
			}
			
		});
		
		return fragment;
	}
	
	private Fragment newSameBranchFrag() {
		return new Fragment("status", "sameBranchFrag", this);
	}
	
	private Fragment newIntegratedFrag() {
		Fragment fragment = new Fragment("status", "integratedFrag", this);
		fragment.add(new BranchLink("sourceBranch", getPullRequest().getSource()));
		fragment.add(new BranchLink("targetBranch", getPullRequest().getTarget()));
		fragment.add(new Link<Void>("swapBranches") {

			@Override
			public void onClick() {
				setResponsePage(
						NewRequestPage.class, 
						paramsOf(getDepot(), getPullRequest().getSource(), getPullRequest().getTarget()));
			}
			
		});
		return fragment;
	}
	
	private Fragment newRejectedFrag() {
		Fragment fragment = new Fragment("status", "rejectedFrag", this);
		fragment.add(new ListView<String>("reasons", new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return getPullRequest().getCheckResult().getReasons();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("reason", item.getModelObject()));
			}

		});
		
		return fragment;
	}

	private Fragment newCanSendFrag() {
		Fragment fragment = new Fragment("status", "canSendFrag", this);
		final Form<?> form = new Form<Void>("form");
		fragment.add(form);
		
		form.add(new Button("send") {

			@Override
			public void onSubmit() {
				super.onSubmit();

				Dao dao = GitPlex.getInstance(Dao.class);
				DepotAndBranch target = getPullRequest().getTarget();
				DepotAndBranch source = getPullRequest().getSource();
				if (!target.getObjectName().equals(getPullRequest().getTarget().getObjectName()) 
						|| !source.getObjectName().equals(getPullRequest().getSource().getObjectName())) {
					getSession().warn("Either target branch or source branch has new commits just now, please re-check.");
					setResponsePage(NewRequestPage.class, paramsOf(getDepot(), target, source));
				} else {
					getPullRequest().setSource(source);
					getPullRequest().setTarget(target);
					for (ReviewInvitation invitation: getPullRequest().getReviewInvitations())
						invitation.setReviewer(dao.load(Account.class, invitation.getReviewer().getId()));
					
					getPullRequest().setAssignee(dao.load(Account.class, getPullRequest().getAssignee().getId()));
					
					GitPlex.getInstance(PullRequestManager.class).open(getPullRequest(), new PageId(getPageId()));
					
					setResponsePage(RequestOverviewPage.class, RequestOverviewPage.paramsOf(getPullRequest()));
				}
			}
			
		});
		
		WebMarkupContainer titleContainer = new WebMarkupContainer("title");
		form.add(titleContainer);
		final TextField<String> titleInput = new TextField<String>("title", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				if (getPullRequest().getTitle() == null) {
					List<Commit> commits = commitsModel.getObject();
					Preconditions.checkState(!commits.isEmpty());
					if (commits.size() == 1)
						getPullRequest().setTitle(commits.get(0).getSubject());
					else
						getPullRequest().setTitle(getPullRequest().getSource().getBranch());
				}
				return getPullRequest().getTitle();
			}

			@Override
			public void setObject(String object) {
				getPullRequest().setTitle(object);
			}
			
		});
		titleInput.setRequired(true);
		titleContainer.add(titleInput);
		
		titleContainer.add(new NotificationPanel("feedback", titleInput));
		
		titleContainer.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return !titleInput.isValid()?" has-error":"";
			}
			
		}));

		form.add(new CommentInput("comment", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return getPullRequest().getDescription();
			}

			@Override
			public void setObject(String object) {
				getPullRequest().setDescription(object);
			}
			
		}) {

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new DepotAttachmentSupport(getDepot(), getPullRequest().getUUID());
			}

			@Override
			protected Depot getDepot() {
				return NewRequestPage.this.getDepot();
			}
			
		});

		WebMarkupContainer assigneeContainer = new WebMarkupContainer("assignee");
		form.add(assigneeContainer);
		IModel<Account> assigneeModel = new PropertyModel<>(getPullRequest(), "assignee");
		AssigneeChoice assigneeChoice = new AssigneeChoice("assignee", depotModel, assigneeModel);
		assigneeChoice.setRequired(true);
		assigneeContainer.add(assigneeChoice);
		
		assigneeContainer.add(new NotificationPanel("feedback", assigneeChoice));
		
		assigneeContainer.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return !assigneeChoice.isValid()?" has-error":"";
			}
			
		}));
		
		WebMarkupContainer reviewersContainer = new WebMarkupContainer("reviewers") {

			@Override
			protected void onBeforeRender() {
				super.onBeforeRender();
			}
			
		};
		reviewersContainer.setOutputMarkupId(true);
		form.add(reviewersContainer);
		reviewersContainer.add(new ListView<ReviewInvitation>("reviewers", new ReviewersModel(requestModel)) {

			@Override
			protected void populateItem(ListItem<ReviewInvitation> item) {
				ReviewInvitation invitation = item.getModelObject();
				
				item.add(new ReviewerAvatar("avatar", invitation) {
					
					@Override
					protected void onAvatarRemove(AjaxRequestTarget target) {
						super.onAvatarRemove(target);
						
						target.add(reviewersContainer);
					}
					
				});
			}
			
		});
		
		reviewersContainer.add(new ReviewerChoice("addReviewer", requestModel) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Account user) {
				super.onSelect(target, user);
				
				target.add(reviewersContainer);
			}
			
		});
		
		reviewersContainer.add(new WebMarkupContainer("prePopulateHint") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getStatus() == Status.PENDING_APPROVAL);
			}
			
		});
		
		return fragment;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(NewRequestPage.class, "new-request.css")));
	}

	@Override
	protected void onDetach() {
		commitsModel.detach();
		requestModel.detach();
		
		super.onDetach();
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(RequestListPage.class, paramsOf(depot));
	}

	@Override
	public DiffMark getMark() {
		return mark;
	}

	@Override
	public String getMarkUrl(DiffMark mark) {
		RevisionComparePage.State state = new RevisionComparePage.State();
		state.mark = mark;
		state.leftSide = new DepotAndBranch(source.getDepot(), getPullRequest().getBaseCommitHash());
		state.rightSide = new DepotAndBranch(source.getDepot(), getPullRequest().getLatestUpdate().getHeadCommitHash());
		state.pathFilter = pathFilter;
		state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
		state.whitespaceOption = whitespaceOption;
		state.compareWithMergeBase = false;
		return urlFor(RevisionComparePage.class, RevisionComparePage.paramsOf(source.getDepot(), state)).toString();
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
		if (comment != null) {
			commentId = comment.getId();
			mark = new DiffMark(comment);
		} else {
			commentId = null;
		}
	}

	@Override
	public String getCommentUrl(CodeComment comment) {
		RevisionComparePage.State state = new RevisionComparePage.State();
		mark = new DiffMark(comment);
		state.commentId = comment.getId();
		state.leftSide = new DepotAndBranch(source.getDepot(), getPullRequest().getBaseCommitHash());
		state.rightSide = new DepotAndBranch(source.getDepot(), getPullRequest().getLatestUpdate().getHeadCommitHash());
		state.pathFilter = pathFilter;
		state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
		state.whitespaceOption = whitespaceOption;
		state.compareWithMergeBase = false;
		return urlFor(RevisionComparePage.class, RevisionComparePage.paramsOf(source.getDepot(), state)).toString();
	}

	@Override
	public void onMark(AjaxRequestTarget target, DiffMark mark) {
		this.mark = mark;
	}

	@Override
	public void onAddComment(AjaxRequestTarget target, DiffMark mark) {
		this.commentId = null;
		this.mark = mark;
	}

}
