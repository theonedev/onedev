package com.pmease.gitplex.web.page.repository.pullrequest;

import static com.pmease.gitplex.core.model.PullRequest.Status.INTEGRATED;
import static com.pmease.gitplex.core.model.PullRequest.Status.PENDING_UPDATE;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
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

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Change;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.wicket.component.backtotop.BackToTop;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.commons.wicket.component.tabbable.AjaxActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior.PageId;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
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
import com.pmease.gitplex.web.component.branch.AffinalBranchSingleChoice;
import com.pmease.gitplex.web.component.branch.BranchLink;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.diff.CompareResultPanel;
import com.pmease.gitplex.web.component.pullrequest.AssigneeChoice;
import com.pmease.gitplex.web.component.pullrequest.ReviewerAvatar;
import com.pmease.gitplex.web.component.pullrequest.ReviewerChoice;
import com.pmease.gitplex.web.model.ReviewersModel;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.security.LoginPage;

@SuppressWarnings("serial")
public class NewRequestPage extends RepositoryPage {

	private AffinalBranchSingleChoice targetChoice, sourceChoice;
	
	private IModel<List<Commit>> commitsModel;
	
	private PullRequest pullRequest;
	
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
		
		pullRequest = GitPlex.getInstance(PullRequestManager.class).findOpen(target, source);
		
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
		}
		
		commitsModel = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				return pullRequest.git().log(pullRequest.getBaseCommitHash(), 
						pullRequest.getLatestUpdate().getHeadCommitHash(), null, 0, 0);
			}
			
		};
		
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
				Model.of(pullRequest.getTarget())) {

			@Override
			protected void onChange(AjaxRequestTarget target) {
				super.onChange(target);
				setResponsePage(
						NewRequestPage.class, 
						paramsOf(getRepository(), sourceChoice.getModelObject(), targetChoice.getModelObject()));
			}
			
		};
		targetChoice.setRequired(true);
		add(targetChoice);
		
		sourceChoice = new AffinalBranchSingleChoice("source", currentRepositoryModel, 
				Model.of(pullRequest.getSource())) {

			@Override
			protected void onChange(AjaxRequestTarget target) {
				super.onChange(target);
				setResponsePage(
						NewRequestPage.class, 
						paramsOf(getRepository(), sourceChoice.getModelObject(), targetChoice.getModelObject()));
			}
			
		};
		sourceChoice.setRequired(true);
		add(sourceChoice);
		
		add(new Link<Void>("swap") {

			@Override
			public void onClick() {
				setResponsePage(
						NewRequestPage.class, 
						paramsOf(getRepository(), pullRequest.getTarget(), pullRequest.getSource()));
			}
			
		});
		
		Fragment fragment;
		if (pullRequest.getId() != null) {
			fragment = newOpenedFrag();
		} else if (pullRequest.getSource().equals(pullRequest.getTarget())) {
			fragment = newSameBranchFrag();
		} else if (pullRequest.getStatus() == INTEGRATED) {
			fragment = newIntegratedFrag();
		} else if (pullRequest.getStatus() == PENDING_UPDATE) {
			fragment = newRejectedFrag();
		} else {
			fragment = newCanSendFrag();
		}
		add(fragment);

		List<Tab> tabs = new ArrayList<>();
		
		tabs.add(new AjaxActionTab(Model.of("Pending Commits")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				Component panel = newCommitsPanel();
				getPage().replace(panel);
				target.add(panel);
			}
			
		});

		tabs.add(new AjaxActionTab(Model.of("Changed Files")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				Component panel = newChangedFilesPanel();
				getPage().replace(panel);
				target.add(panel);
			}
			
		});

		add(new Tabbable("tabs", tabs) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(pullRequest.getStatus() != INTEGRATED);
			}
			
		});
		
		add(new WebMarkupContainer("tabPanel").setOutputMarkupId(true));

		add(new BackToTop("backToTop"));
	}
	
	private Component newCommitsPanel() {
		return new WebMarkupContainer("tabPanel").setOutputMarkupId(true);
	}
	
	private Component newChangedFilesPanel() {
		return new CompareResultPanel("tabPanel", repoModel, pullRequest.getBaseCommitHash(), 
				pullRequest.getLatestUpdate().getHeadCommitHash(), null) {
			
			@Override
			protected void onSelection(AjaxRequestTarget target, Change change) {
			}
			
			@Override
			protected InlineCommentSupport getInlineCommentSupport(Change change) {
				return null;
			}
		}.setOutputMarkupId(true);
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
						return pullRequest.getId().toString();
					}
				}));
			}

			@Override
			public void onClick() {
				PageParameters params = RequestDetailPage.paramsOf(pullRequest);
				setResponsePage(RequestOverviewPage.class, params);
			}
			
		});
		
		fragment.add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return pullRequest.getTitle();
			}
		}));
		
		return fragment;
	}
	
	private Fragment newSameBranchFrag() {
		return new Fragment("status", "sameBranchFrag", this);
	}
	
	private Fragment newIntegratedFrag() {
		Fragment fragment = new Fragment("status", "integratedFrag", this);
		fragment.add(new BranchLink("sourceBranch", Model.of(pullRequest.getSource())));
		fragment.add(new BranchLink("targetBranch", Model.of(pullRequest.getTarget())));
		fragment.add(new Link<Void>("swapBranches") {

			@Override
			public void onClick() {
				setResponsePage(
						NewRequestPage.class, 
						paramsOf(getRepository(), pullRequest.getTarget(), pullRequest.getSource()));
			}
			
		});
		return fragment;
	}
	
	private Fragment newRejectedFrag() {
		Fragment fragment = new Fragment("status", "rejectedFrag", this);
		fragment.add(new ListView<String>("reasons", new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return pullRequest.getCheckResult().getReasons();
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
				RepoAndBranch target = pullRequest.getTarget();
				RepoAndBranch source = pullRequest.getSource();
				if (!target.getHead().equals(pullRequest.getTarget().getHead()) 
						|| !source.getHead().equals(pullRequest.getSource().getHead())) {
					getSession().warn("Either target branch or source branch has new commits just now, please re-check.");
					setResponsePage(NewRequestPage.class, paramsOf(getRepository(), source, target));
				} else {
					pullRequest.setSource(source);
					pullRequest.setTarget(target);
					for (ReviewInvitation invitation: pullRequest.getReviewInvitations())
						invitation.setReviewer(dao.load(User.class, invitation.getReviewer().getId()));
					
					pullRequest.setAssignee(dao.load(User.class, pullRequest.getAssignee().getId()));
					
					GitPlex.getInstance(PullRequestManager.class).open(pullRequest, new PageId(getPageId()));
					
					setResponsePage(RequestOverviewPage.class, RequestOverviewPage.paramsOf(pullRequest));
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
				if (pullRequest.getTitle() == null) {
					List<Commit> commits = commitsModel.getObject();
					Preconditions.checkState(!commits.isEmpty());
					if (commits.size() == 1)
						pullRequest.setTitle(commits.get(0).getSubject());
					else
						pullRequest.setTitle(pullRequest.getSource().getBranch());
				}
				return pullRequest.getTitle();
			}

			@Override
			public void setObject(String object) {
				pullRequest.setTitle(object);
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
				return pullRequest.getDescription();
			}

			@Override
			public void setObject(String object) {
				pullRequest.setDescription(object);
			}
			
		}));

		WebMarkupContainer assigneeContainer = new WebMarkupContainer("assignee");
		form.add(assigneeContainer);
		IModel<User> assigneeModel = new PropertyModel<>(pullRequest, "assignee");
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
		reviewersContainer.add(new ListView<ReviewInvitation>("reviewers", new ReviewersModel(Model.of(pullRequest))) {

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
		
		reviewersContainer.add(new ReviewerChoice("addReviewer", Model.of(pullRequest)) {

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
				setVisible(pullRequest.getStatus() == Status.PENDING_APPROVAL);
			}
			
		});
		
		return fragment;
	}

	@Override
	protected void onDetach() {
		commitsModel.detach();

		if (pullRequest != null && pullRequest.getSandbox() != null) {
			FileUtils.deleteDir(pullRequest.getSandbox().repoDir());
			pullRequest.setSandbox(null);
		}

		super.onDetach();
	}

}
