package com.pmease.gitplex.web.page.repository.pullrequest;

import static com.pmease.gitplex.core.model.PullRequest.Status.INTEGRATED;
import static com.pmease.gitplex.core.model.PullRequest.Status.PENDING_UPDATE;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pmease.gitplex.core.GitPlex;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.wicket.behavior.DisableIfBlankBehavior;
import com.pmease.gitplex.core.gatekeeper.checkresult.Approved;
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.CommitComment;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.pullrequest.CloseInfo;
import com.pmease.gitplex.web.component.branch.AffinalBranchSingleChoice;
import com.pmease.gitplex.web.component.branch.BranchLink;
import com.pmease.gitplex.web.component.commit.CommitsTablePanel;
import com.pmease.gitplex.web.model.EntityModel;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.RepositoryInfoPage;
import com.pmease.gitplex.web.page.repository.source.commit.diff.CommitCommentsAware;
import com.pmease.gitplex.web.page.repository.source.commit.diff.DiffViewPanel;

@SuppressWarnings("serial")
public class NewRequestPage extends RepositoryInfoPage implements CommitCommentsAware {

	private IModel<Branch> targetModel, sourceModel;
	
	private IModel<User> submitterModel;
	
	private IModel<List<Commit>> commitsModel;
	
	private IModel<PullRequest> requestModel;
	
	private IModel<PullRequest> checkedRequestModel;
	
	public static PageParameters paramsOf(Repository repository, Branch source, Branch target) {
		PageParameters params = paramsOf(repository);
		params.set("source", source.getId());
		params.set("target", target.getId());
		return params;
	}
	
	public NewRequestPage(PageParameters params) {
		super(params);
		
		BranchManager branchManager = GitPlex.getInstance(BranchManager.class);
		Dao dao = AppLoader.getInstance(Dao.class);
		
		RepositoryPage page = (RepositoryPage) getPage();

		Branch target, source = null;
		if (params.get("target").toString() != null) {
			target = dao.load(Branch.class, params.get("target").toLongObject());
		} else {
			if (page.getRepository().getForkedFrom() != null) {
				target = branchManager.findDefault(page.getRepository().getForkedFrom());
			} else {
				target = branchManager.findDefault(page.getRepository());
			}
		}
		if (params.get("source").toString() != null) {
			source = dao.load(Branch.class, params.get("source").toLongObject());
		} else {
			if (page.getRepository().getForkedFrom() != null) {
				source = branchManager.findDefault(page.getRepository());
			} else {
				for (Branch each: page.getRepository().getBranches()) {
					if (!each.equals(target)) {
						source = each;
						break;
					}
				}
				if (source == null)
					source = target;
			}
		}

		User currentUser = AppLoader.getInstance(UserManager.class).getCurrent();
		
		targetModel = new EntityModel<Branch>(target);
		sourceModel = new EntityModel<Branch>(source);
		submitterModel = new EntityModel<User>(currentUser);

		commitsModel = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				PullRequest request = checkedRequestModel.getObject();
				return request.git().log(getTarget().getHeadCommit(), 
						getSource().getHeadCommit(), null, 0, 0);
			}
			
		};
		
		requestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				PullRequest request = GitPlex.getInstance(PullRequestManager.class).findOpen(getTarget(), getSource());
				if (request == null) {
					request = new PullRequest();
					request.setTarget(getTarget());
					request.setSource(getSource());
					request.setSubmitter(getSubmitter());
					
					PullRequestUpdate update = new PullRequestUpdate();
					request.getUpdates().add(update);
					update.setRequest(request);
					update.setUser(getSubmitter());
					update.setHeadCommit(getSource().getHeadCommit());
					request.setUpdateDate(new Date());
					
			    	String targetHead = getTarget().getHeadCommit();
					request.setBaseCommit(targetHead);
				}
				return request;
			}

		};
		
		checkedRequestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				PullRequest request = requestModel.getObject();
				if (request.getId() == null) {
			    	String targetHead = getTarget().getHeadCommit();
					String sourceHead = getSource().getHeadCommit();

					if (getTarget().getRepository().equals(getSource().getRepository())) {
						if (getTarget().getRepository().git().isAncestor(sourceHead, targetHead)) {
							CloseInfo closeInfo = new CloseInfo();
							closeInfo.setClosedBy(null);
							closeInfo.setCloseStatus(CloseInfo.Status.INTEGRATED);
							closeInfo.setComment("Target branch already contains commit of source branch.");
							request.setCloseInfo(closeInfo);
							request.setCheckResult(new Approved("Already integrated."));
						} else {
							request.setCheckResult(getTarget().getRepository().getGateKeeper().checkRequest(request));
						}
					} else {
						Git sandbox = new Git(FileUtils.createTempDir());
						request.setSandbox(sandbox);
						sandbox.clone(getTarget().getRepository().git(), false, true, true, request.getTarget().getName());
						sandbox.reset(null, null);

						sandbox.fetch(getSource().getRepository().git(), null);
						
						if (sandbox.isAncestor(sourceHead, targetHead)) {
							CloseInfo closeInfo = new CloseInfo();
							closeInfo.setClosedBy(null);
							closeInfo.setCloseStatus(CloseInfo.Status.INTEGRATED);
							closeInfo.setComment("Target branch already contains commit of source branch.");
							request.setCloseInfo(closeInfo);
							request.setCheckResult(new Approved("Already integrated."));
						} else {
							request.setCheckResult(getTarget().getRepository().getGateKeeper().checkRequest(request));
						}
					}
				}
				return request;
			}
			
			@Override
			protected void onDetach() {
				PullRequest request = getObject();
				if (request.getSandbox() != null) {
					FileUtils.deleteDir(request.getSandbox().repoDir());
					request.setSandbox(null);
				}
				super.onDetach();
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
		
		add(new AffinalBranchSingleChoice("target", currentRepositoryModel, targetModel) {

			@Override
			protected void onChange(AjaxRequestTarget target) {
				super.onChange(target);
				setResponsePage(
						NewRequestPage.class, 
						paramsOf(getRepository(), getSource(), getTarget()));
			}
			
		}.setRequired(true));
		
		add(new AffinalBranchSingleChoice("source", currentRepositoryModel, sourceModel) {

			@Override
			protected void onChange(AjaxRequestTarget target) {
				super.onChange(target);
				setResponsePage(
						NewRequestPage.class, 
						paramsOf(getRepository(), getSource(), getTarget()));
			}
			
		}.setRequired(true));
		
		add(new Link<Void>("swap") {

			@Override
			public void onClick() {
				setResponsePage(
						NewRequestPage.class, 
						paramsOf(getRepository(), getTarget(), getSource()));
			}
			
		});
		
		IModel<Repository> repositoryModel = new AbstractReadOnlyModel<Repository>() {

			@Override
			public Repository getObject() {
				return getTarget().getRepository();
			}
			
		};
		add(new CommitsTablePanel("commits", commitsModel, repositoryModel) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(checkedRequestModel.getObject().getStatus() != INTEGRATED);
			}
			
		});
		
		add(new DiffViewPanel("changes", repositoryModel, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return checkedRequestModel.getObject().getTarget().getHeadCommit();
			}
			
		}, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getSource().getHeadCommit();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(checkedRequestModel.getObject().getStatus() != INTEGRATED);
			}
			
		});
	}
	
	@Override
	protected void onBeforeRender() {
		addOrReplace(newStatusFragment());
		
		super.onBeforeRender();
	}

	private Fragment newStatusFragment() {
		Fragment fragment;
		PullRequest request = checkedRequestModel.getObject();
		if (request.getId() != null) {
			fragment = newOpenedFrag();
		} else if (request.getSource().equals(request.getTarget())) {
			fragment = newSameBranchFrag();
		} else if (request.getStatus() == INTEGRATED) {
			fragment = newIntegratedFrag();
		} else if (request.getStatus() == PENDING_UPDATE) {
			fragment = newRejectedFrag();
		} else {
			fragment = newCanSendFrag();
		}
		return fragment;
	}
	
	private Fragment newOpenedFrag() {
		PullRequest request = checkedRequestModel.getObject();
		Fragment fragment = new Fragment("status", "openedFrag", this);
		fragment.add(new Label("requestInfo", "#" + request.getId() + ": " + request.getTitle()));
		fragment.add(new Link<Void>("viewRequest") {

			@Override
			public void onClick() {
				PageParameters params = RequestDetailPage.params4(checkedRequestModel.getObject());
				setResponsePage(RequestActivitiesPage.class, params);
			}
			
		});
		
		return fragment;
	}
	
	private Fragment newSameBranchFrag() {
		return new Fragment("status", "sameBranchFrag", this);
	}
	
	private Fragment newIntegratedFrag() {
		Fragment fragment = new Fragment("status", "integratedFrag", this);
		fragment.add(new BranchLink("sourceBranch", new EntityModel<Branch>(getSource())));
		fragment.add(new BranchLink("targetBranch", new EntityModel<Branch>(getTarget())));
		fragment.add(new Link<Void>("swapBranches") {

			@Override
			public void onClick() {
				setResponsePage(
						NewRequestPage.class, 
						paramsOf(getRepository(), getTarget(), getSource()));
			}
			
		});
		return fragment;
	}
	
	private Fragment newRejectedFrag() {
		Fragment fragment = new Fragment("status", "rejectedFrag", this);
		fragment.add(new ListView<String>("reasons", new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return checkedRequestModel.getObject().getCheckResult().getReasons();
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
		Form<?> form = new Form<Void>("form");
		fragment.add(form);
		
		form.add(new Button("send") {

			@Override
			public void onSubmit() {
				super.onSubmit();

				PullRequest request = requestModel.getObject();
				request.setAutoIntegrate(false);
				
				GitPlex.getInstance(PullRequestManager.class).send(request);
				
				setResponsePage(OpenRequestsPage.class, paramsOf(request.getTarget().getRepository()));
			}
			
		});
		
		form.add(new TextField<String>("title", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				PullRequest request = checkedRequestModel.getObject();
				if (request.getTitle() == null) {
					List<Commit> commits = commitsModel.getObject();
					Preconditions.checkState(!commits.isEmpty());
					if (commits.size() == 1)
			 			request.setTitle(commits.get(0).getSubject());
					else
						request.setTitle(getSource().getName());
				}
				return request.getTitle();
			}

			@Override
			public void setObject(String object) {
				checkedRequestModel.getObject().setTitle(object);
			}
			
		}).setRequired(true).add(new DisableIfBlankBehavior(form.get("send"))));
		
		form.add(new TextArea<String>("comment", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return checkedRequestModel.getObject().getDescription();
			}

			@Override
			public void setObject(String object) {
				checkedRequestModel.getObject().setDescription(object);
			}
			
		}));

		return fragment;
	}

	private Branch getTarget() {
		return targetModel.getObject();
	}
	
	private Branch getSource() {
		return sourceModel.getObject();
	}
	
	private User getSubmitter() {
		return submitterModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		requestModel.detach();
		checkedRequestModel.detach();
		targetModel.detach();
		sourceModel.detach();
		submitterModel.detach();
		commitsModel.detach();

		super.onDetach();
	}

	@Override
	public List<CommitComment> getCommitComments() {
		return new ArrayList<>();
	}

	@Override
	public boolean isShowInlineComments() {
		return false;
	}

	@Override
	public boolean canAddComments() {
		return false;
	}
}
