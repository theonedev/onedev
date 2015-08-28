package com.pmease.gitplex.web.page.repository.pullrequest.newrequest;

import static com.pmease.gitplex.core.model.PullRequest.Status.INTEGRATED;
import static com.pmease.gitplex.core.model.PullRequest.Status.PENDING_UPDATE;

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

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.wicket.behavior.StickyBehavior;
import com.pmease.commons.wicket.component.backtotop.BackToTop;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.commons.wicket.component.tabbable.AjaxActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior.PageId;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequest.CloseStatus;
import com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.model.PullRequest.Status;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.ReviewInvitation;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.component.branchchoice.affinalchoice.AffinalBranchSingleChoice;
import com.pmease.gitplex.web.component.branchlink.BranchLink;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.commitlist.CommitListPanel;
import com.pmease.gitplex.web.component.diff.revision.RevisionDiffPanel;
import com.pmease.gitplex.web.component.diff.revision.option.DiffOptionPanel;
import com.pmease.gitplex.web.component.pullrequest.requestassignee.AssigneeChoice;
import com.pmease.gitplex.web.component.pullrequest.requestreviewer.ReviewerAvatar;
import com.pmease.gitplex.web.component.pullrequest.requestreviewer.ReviewerChoice;
import com.pmease.gitplex.web.model.ReviewersModel;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.pullrequest.PullRequestPage;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.pmease.gitplex.web.page.repository.pullrequest.requestlist.RequestListPage;
import com.pmease.gitplex.web.page.security.LoginPage;

@SuppressWarnings("serial")
public class NewRequestPage extends PullRequestPage {

	private static final String TAB_PANEL_ID = "tabPanel";
	
	private AffinalBranchSingleChoice targetChoice, sourceChoice;
	
	private IModel<List<Commit>> commitsModel;
	
	private IModel<PullRequest> requestModel;
	
	private DiffOptionPanel diffOption;
	
	private String path;
	
	public static PageParameters paramsOf(Repository repository, RepoAndBranch source, RepoAndBranch target) {
		PageParameters params = paramsOf(repository);
		params.set("source", source.getId());
		params.set("target", target.getId());
		return params;
	}

	public NewRequestPage(PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));

		RepoAndBranch target, source = null;
		if (params.get("target").toString() != null) {
			target = new RepoAndBranch(params.get("target").toString());
		} else {
			if (getRepository().getForkedFrom() != null) {
				target = new RepoAndBranch(getRepository().getForkedFrom(), 
						getRepository().getForkedFrom().getDefaultBranch());
			} else {
				target = new RepoAndBranch(getRepository(), getRepository().getDefaultBranch());
			}
		}
		if (params.get("source").toString() != null) {
			source = new RepoAndBranch(params.get("source").toString());
		} else {
			if (getRepository().getForkedFrom() != null) {
				source = new RepoAndBranch(getRepository(), getRepository().getDefaultBranch());
			} else {
				for (String each: getRepository().getBranches()) {
					if (!each.equals(target.getBranch())) {
						source = new RepoAndBranch(getRepository(), each);
						break;
					}
				}
				if (source == null)
					source = target;
			}
		}

		User currentUser = getCurrentUser();
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
			update.setHeadCommitHash(source.getHead());
			pullRequest.setLastEventDate(new Date());
			
			PullRequestManager pullRequestManager = GitPlex.getInstance(PullRequestManager.class);
			List<IntegrationStrategy> strategies = pullRequestManager.getApplicableIntegrationStrategies(pullRequest);
			Preconditions.checkState(!strategies.isEmpty());
			pullRequest.setIntegrationStrategy(strategies.get(0));
			
			ObjectPermission writePermission = ObjectPermission.ofRepoPush(getRepository());
			if (currentUser.asSubject().isPermitted(writePermission))
				pullRequest.setAssignee(currentUser);
			else
				pullRequest.setAssignee(getRepository().getOwner());

			if (target.getRepository().equals(source.getRepository())) {
				pullRequest.setBaseCommitHash(pullRequest.git().calcMergeBase(
						target.getHead(), source.getHead()));			
				if (target.getRepository().git().isAncestor(source.getHead(), target.getHead())) 
					pullRequest.setCloseStatus(CloseStatus.INTEGRATED);
			} else {
				Git sandbox = new Git(FileUtils.createTempDir());
				pullRequest.setSandbox(sandbox);
				sandbox.clone(target.getRepository().git(), false, true, true, pullRequest.getTarget().getBranch());
				sandbox.reset(null, null);

				sandbox.fetch(source.getRepository().git(), source.getBranch());
				
				pullRequest.setBaseCommitHash(pullRequest.git().calcMergeBase(target.getHead(), source.getHead()));			

				if (sandbox.isAncestor(source.getHead(), target.getHead()))
					pullRequest.setCloseStatus(CloseStatus.INTEGRATED);
			}
			requestModel = Model.of(pullRequest);
		} else {
			final Long requestId = pullRequest.getId();
			requestModel = new LoadableDetachableModel<PullRequest>() {

				@Override
				protected PullRequest load() {
					return GitPlex.getInstance(Dao.class).load(PullRequest.class, requestId);
				}
				
			};
			requestModel.setObject(pullRequest);
		}
		
		commitsModel = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				PullRequest request = getPullRequest();
				List<Commit> commits = request.git().log(request.getBaseCommitHash(), 
						request.getLatestUpdate().getHeadCommitHash(), null, 0, 0);
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
		
		IModel<Repository> currentRepositoryModel = new LoadableDetachableModel<Repository>() {

			@Override
			protected Repository load() {
				RepositoryPage page = (RepositoryPage) getPage();
				return page.getRepository();
			}
			
		};
		
		targetChoice = new AffinalBranchSingleChoice("target", currentRepositoryModel, 
				Model.of(getPullRequest().getTarget().getId()), false) {

			@Override
			protected void onChange(AjaxRequestTarget target) {
				super.onChange(target);
				
				PageParameters params = paramsOf(getRepository(), 
						new RepoAndBranch(sourceChoice.getModelObject()), 
						new RepoAndBranch(targetChoice.getModelObject()));
				setResponsePage(NewRequestPage.class, params);
			}
			
		};
		targetChoice.setRequired(true);
		add(targetChoice);
		
		sourceChoice = new AffinalBranchSingleChoice("source", currentRepositoryModel, 
				Model.of(getPullRequest().getSource().getId()), false) {

			@Override
			protected void onChange(AjaxRequestTarget target) {
				super.onChange(target);

				PageParameters params = paramsOf(getRepository(), 
						new RepoAndBranch(sourceChoice.getModelObject()), 
						new RepoAndBranch(targetChoice.getModelObject()));
				setResponsePage(NewRequestPage.class, params);
			}
			
		};
		sourceChoice.setRequired(true);
		add(sourceChoice);
		
		add(new Link<Void>("swap") {

			@Override
			public void onClick() {
				setResponsePage(
						NewRequestPage.class, 
						paramsOf(getRepository(), getPullRequest().getTarget(), getPullRequest().getSource()));
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

		tabs.add(new AjaxActionTab(Model.of("Files Changed")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				Component panel = newComparePanel();
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
		return new CommitListPanel(TAB_PANEL_ID, repoModel, commitsModel).setOutputMarkupId(true);
	}
	
	private Component newComparePanel() {
		final Fragment fragment = new Fragment(TAB_PANEL_ID, "compareFrag", this);
		
		diffOption = new DiffOptionPanel("diffOption", repoModel, getPullRequest().getLatestUpdate().getHeadCommitHash()) {

			@Override
			protected void onSelectPath(AjaxRequestTarget target, String path) {
				NewRequestPage.this.path = path;
				RevisionDiffPanel diffPanel = newRevDiffPanel();
				fragment.replace(diffPanel);
				target.add(diffPanel);
			}

			@Override
			protected void onLineProcessorChange(AjaxRequestTarget target) {
				RevisionDiffPanel diffPanel = newRevDiffPanel();
				fragment.replace(diffPanel);
				target.add(diffPanel);
			}

			@Override
			protected void onDiffModeChange(AjaxRequestTarget target) {
				RevisionDiffPanel diffPanel = newRevDiffPanel();
				fragment.replace(diffPanel);
				target.add(diffPanel);
			}
			
		};
		diffOption.add(new StickyBehavior());
		fragment.add(diffOption);
		fragment.add(newRevDiffPanel());
		
		return fragment;
	}
	
	protected RevisionDiffPanel newRevDiffPanel() {
		PullRequest request = getPullRequest();
		String oldRev = request.getBaseCommitHash();
		String newRev = request.getLatestUpdate().getHeadCommitHash();
		RevisionDiffPanel diffPanel = new RevisionDiffPanel("revisionDiff", repoModel, 
				oldRev, newRev, path, null, diffOption.getLineProcessor(), 
				diffOption.getDiffMode(), null) {

			@Override
			protected void onClearPath(AjaxRequestTarget target) {
				path = null;
				RevisionDiffPanel diffPanel = newRevDiffPanel();
				replaceWith(diffPanel);
				target.add(diffPanel);
			}
			
		};
		diffPanel.setOutputMarkupId(true);
		return diffPanel;
	}

	private Fragment newOpenedFrag() {
		Fragment fragment = new Fragment("status", "openedFrag", this);
		fragment.add(new Link<Void>("view") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("no", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return getPullRequest().getId().toString();
					}
				}));
			}

			@Override
			public void onClick() {
				PageParameters params = RequestDetailPage.paramsOf(getPullRequest());
				setResponsePage(RequestOverviewPage.class, params);
			}
			
		});
		
		fragment.add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getTitle();
			}
		}));
		
		return fragment;
	}
	
	private Fragment newSameBranchFrag() {
		return new Fragment("status", "sameBranchFrag", this);
	}
	
	private Fragment newIntegratedFrag() {
		Fragment fragment = new Fragment("status", "integratedFrag", this);
		fragment.add(new BranchLink("sourceBranch", Model.of(getPullRequest().getSource())));
		fragment.add(new BranchLink("targetBranch", Model.of(getPullRequest().getTarget())));
		fragment.add(new Link<Void>("swapBranches") {

			@Override
			public void onClick() {
				setResponsePage(
						NewRequestPage.class, 
						paramsOf(getRepository(), getPullRequest().getTarget(), getPullRequest().getSource()));
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
				RepoAndBranch target = getPullRequest().getTarget();
				RepoAndBranch source = getPullRequest().getSource();
				if (!target.getHead().equals(getPullRequest().getTarget().getHead()) 
						|| !source.getHead().equals(getPullRequest().getSource().getHead())) {
					getSession().warn("Either target branch or source branch has new commits just now, please re-check.");
					setResponsePage(NewRequestPage.class, paramsOf(getRepository(), source, target));
				} else {
					getPullRequest().setSource(source);
					getPullRequest().setTarget(target);
					for (ReviewInvitation invitation: getPullRequest().getReviewInvitations())
						invitation.setReviewer(dao.load(User.class, invitation.getReviewer().getId()));
					
					getPullRequest().setAssignee(dao.load(User.class, getPullRequest().getAssignee().getId()));
					
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
		
		titleContainer.add(new FeedbackPanel("feedback", titleInput));
		
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
			
		}));

		WebMarkupContainer assigneeContainer = new WebMarkupContainer("assignee");
		form.add(assigneeContainer);
		IModel<User> assigneeModel = new PropertyModel<>(getPullRequest(), "assignee");
		final AssigneeChoice assigneeChoice = new AssigneeChoice("assignee", repoModel, assigneeModel);
		assigneeChoice.setRequired(true);
		assigneeContainer.add(assigneeChoice);
		
		assigneeContainer.add(new FeedbackPanel("feedback", assigneeChoice));
		
		assigneeContainer.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return !assigneeChoice.isValid()?" has-error":"";
			}
			
		}));
		
		final WebMarkupContainer reviewersContainer = new WebMarkupContainer("reviewers") {

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
			protected void onSelect(AjaxRequestTarget target, User user) {
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

		if (getPullRequest() != null && getPullRequest().getSandbox() != null) {
			FileUtils.deleteDir(getPullRequest().getSandbox().repoDir());
			getPullRequest().setSandbox(null);
		}

		requestModel.detach();
		
		super.onDetach();
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Repository repository) {
		setResponsePage(RequestListPage.class, paramsOf(repository));
	}

}
