package com.pmease.gitplex.web.page.repository.info.pullrequest;

import static com.pmease.gitplex.core.model.IntegrationStrategy.MERGE_ALWAYS;
import static com.pmease.gitplex.core.model.IntegrationStrategy.MERGE_IF_NECESSARY;
import static com.pmease.gitplex.core.model.IntegrationStrategy.REBASE_SOURCE;
import static com.pmease.gitplex.core.model.IntegrationStrategy.REBASE_TARGET;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.component.backtotop.BackToTop;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.PageTabLink;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.IntegrationInfo;
import com.pmease.gitplex.core.model.IntegrationStrategy;
import com.pmease.gitplex.core.model.OldCommitComment;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequest.Status;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.Verification;
import com.pmease.gitplex.core.pullrequest.RequestOperateException;
import com.pmease.gitplex.core.pullrequest.RequestOperation;
import com.pmease.gitplex.web.component.branch.BranchLink;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.PersonLink;
import com.pmease.gitplex.web.model.EntityModel;
import com.pmease.gitplex.web.page.repository.info.RepositoryInfoPage;
import com.pmease.gitplex.web.page.repository.info.code.commit.diff.CommitCommentsAware;

@SuppressWarnings("serial")
public abstract class RequestDetailPage extends RepositoryInfoPage implements CommitCommentsAware {

	protected IModel<PullRequest> requestModel;
	
	private boolean editingTitle;
	
	private RequestOperation actionToConfirm;
	
	public RequestDetailPage(final PageParameters params) {
		super(params);
		
		requestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				return GitPlex.getInstance(Dao.class).load(PullRequest.class, params.get("request").toLong());
			}
			
		};

	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		PullRequest request = getPullRequest();

		final WebMarkupContainer head = new WebMarkupContainer("head");
		head.setOutputMarkupId(true);
		add(head);
		
		head.add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getTitle();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!editingTitle);
			}
			
		});
		
		head.add(new Label("id", "#" + request.getId()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!editingTitle);
			}
			
		});
		
		head.add(new AjaxLink<Void>("editTitle") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				editingTitle = true;
				
				target.add(head);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();

				AuthorizationManager authorizationManager = GitPlex.getInstance(AuthorizationManager.class);
				setVisible(!editingTitle && authorizationManager.canModify(getPullRequest()));
			}
			
		});

		Form<?> form = new Form<Void>("titleEditor") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(editingTitle);
			}
			
		};
		head.add(form);
		
		form.add(new TextField<String>("title", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				if (StringUtils.isNotBlank(getPullRequest().getTitle()))
					return getPullRequest().getTitle();
				else
					return "";
			}

			@Override
			public void setObject(String object) {
				getPullRequest().setTitle(object);
			}
			
		}));
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (StringUtils.isNotBlank(getPullRequest().getTitle())) {
					GitPlex.getInstance(Dao.class).persist(getPullRequest());
					editingTitle = false;
				}

				target.add(head);
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				editingTitle = false;
				
				target.add(head);
			}
			
		});
		
		User submitter = request.getSubmitter();
		if (submitter != null) {
			PersonIdent person = new PersonIdent(submitter.getName(), submitter.getEmail());
			add(new PersonLink("user", Model.of(person), AvatarMode.NAME_AND_AVATAR));
		} else {
			add(new Label("<i>System</i>").setEscapeModelStrings(false));
		}
		
		add(new BranchLink("targetBranch", new EntityModel<Branch>(request.getTarget())));
		
		BranchLink branchLink = new BranchLink("sourceBranch", new EntityModel<Branch>(request.getSource()));
		branchLink.setVisible(request.getSource() != null);
		add(branchLink);
		
		add(new AgeLabel("date", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getPullRequest().getCreateDate();
			}
			
		}));

		final WebMarkupContainer actionsContainer = new WebMarkupContainer("actions");
		actionsContainer.setOutputMarkupId(true);
		add(actionsContainer);
		
		actionsContainer.add(new AjaxLink<Void>("approve") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				actionToConfirm = RequestOperation.APPROVE;
				target.add(actionsContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(RequestOperation.APPROVE.canOperate(getPullRequest()));
			}
			
		});
		
		actionsContainer.add(new AjaxLink<Void>("disapprove") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				actionToConfirm = RequestOperation.DISAPPROVE;
				target.add(actionsContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(RequestOperation.DISAPPROVE.canOperate(getPullRequest()));
			}
			
		});
		
		actionsContainer.add(new AjaxLink<Void>("integrate") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				actionToConfirm = RequestOperation.INTEGRATE;
				target.add(actionsContainer);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(RequestOperation.INTEGRATE.canOperate(getPullRequest()));
			}

		});
		
		actionsContainer.add(new AjaxLink<Void>("discard") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				actionToConfirm = RequestOperation.DISCARD;
				target.add(actionsContainer);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(RequestOperation.DISCARD.canOperate(getPullRequest()));
			}

		});
		
		Form<?> confirmForm = new Form<Void>("confirm") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(actionToConfirm != null);
			}
			
		};
		actionsContainer.add(confirmForm);
		
		final CommentInput commentInput = new CommentInput("comment", Model.of(""));
		confirmForm.add(commentInput);
		confirmForm.add(new FeedbackPanel("feedback", confirmForm));
		confirmForm.add(new Button("submit") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				
				String actionName = actionToConfirm.name().toLowerCase();

				PullRequest request = getPullRequest();
				try {
					actionToConfirm.checkOperate(request);
					actionToConfirm.operate(request, commentInput.getModelObject());
					actionToConfirm = null;
					commentInput.setModelObject(null);
				} catch (UnauthorizedException e) {
					error("Unable to " + actionName + ": Permission denied.");
				} catch (RequestOperateException e) {
					error("Unable to " + actionName + ": " + e.getMessage());
				}
			}
			
		}.add(AttributeModifier.replace("value", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "Confirm " + WordUtils.capitalizeFully(actionToConfirm.name());
			}
			
		})).add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (actionToConfirm == RequestOperation.APPROVE)
					return "btn-primary";
				else if (actionToConfirm == RequestOperation.DISAPPROVE)
					return "btn-primary";
				else if (actionToConfirm == RequestOperation.INTEGRATE)
					return "btn-success";
				else
					return "btn-danger";
			}
			
		})));
		confirmForm.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				actionToConfirm = null;
				commentInput.setModelObject(null);
				target.add(actionsContainer);
			}
			
		});		
		
		List<Tab> tabs = new ArrayList<>();
		
		tabs.add(new RequestTab("Discussions", RequestActivitiesPage.class));
		tabs.add(new RequestTab("Updates", RequestUpdatesPage.class));
		tabs.add(new RequestTab("Compare", RequestComparePage.class));
		add(new Tabbable("tabs", tabs));
		
		add(new BackToTop("backToTop"));
	}
	
	private Component newStatusComponent(String id) {
		PullRequest request = getPullRequest();
		Fragment fragment;
		if (request.getStatus() == Status.INTEGRATED) {
			fragment = new Fragment(id, "integratedFrag", this);
			IntegrationInfo integrationInfo = request.getIntegrationInfo();
			if (integrationInfo.getIntegrationHead().equals(integrationInfo.getRequestHead())) {
				fragment.add(new Label("detail", 
						"Target branch was fast forwarded to this request per the integration strategy."));
			} else {
				IntegrationStrategy strategy = integrationInfo.getIntegrationStrategy();
				if (strategy == MERGE_ALWAYS || strategy == MERGE_IF_NECESSARY) { 
					fragment.add(new Label("detail", 
							"Target branch was merged with this request per the integration strategy."));
				} else if (strategy == REBASE_SOURCE) {
					if (request.getSource() != null) {
						Fragment detailFrag = new Fragment("detail", "sourceRebasedFrag", this);
						fragment.add(detailFrag);
						detailFrag.add(new BranchLink("target", new EntityModel<Branch>(request.getTarget())));
						detailFrag.add(new BranchLink("source", new EntityModel<Branch>(request.getSource())));
					} else {
						fragment.add(new Label("detail", 
								"Request was rebased on top of target branch and then target branch was fast "
								+ "forwarded to rebased result per the integration strategy."));
					}
				} else if (strategy == REBASE_TARGET){
					Fragment detailFrag = new Fragment("detail", "targetRebasedFrag", this);
					fragment.add(detailFrag);
					detailFrag.add(new BranchLink("target", new EntityModel<Branch>(request.getTarget())));
				} else {
					fragment.add(new Label("detail", 
							"Head commit of the request was merged into target branch by some other party."));
				}
			}
		} else if (request.getStatus() == Status.DISCARDED) {
			fragment = new Fragment(id, "discardedFrag", this);
		} else {
			fragment = new Fragment(id, "openFrag", this);
			populateOpenFrag(fragment);
		}
		return fragment;
	}
	
	private void populateOpenFrag(Fragment fragment) {
		WebMarkupContainer checkResultContainer = new WebMarkupContainer("checkResult");
		fragment.add(checkResultContainer);
		PullRequest request = getPullRequest();
		if (request.getStatus() == Status.PENDING_INTEGRATE)
			checkResultContainer.add(AttributeAppender.append("class", " alert alert-info"));
		else
			checkResultContainer.add(AttributeAppender.append("class", " alert alert-warning"));
		
		checkResultContainer.add(new Label("message", 
				"This request is " + request.getStatus().toString().toLowerCase() + "."));
		
		checkResultContainer.add(new ListView<String>("reasons", request.getCheckResult().getReasons()) {

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("reason", item.getModelObject()));
			}

		}.setVisible(!request.getCheckResult().getReasons().isEmpty()));
		
		fragment.add(newIntegrationInfoComponent("integrationInfo"));
		

	}
	
	private Component newIntegrationInfoComponent(String id) {
		Fragment fragment;
		PullRequest request = getPullRequest();
		IntegrationInfo integrationInfo = request.getIntegrationInfo();
		if (integrationInfo.getIntegrationHead() == null) {
			fragment = new Fragment(id, "integrateConflictFrag", this);
			fragment.add(AttributeAppender.append("class", " alert alert-warning"));
			
			String message;
			IntegrationStrategy strategy = integrationInfo.getIntegrationStrategy();
			if (strategy == MERGE_ALWAYS || strategy == MERGE_IF_NECESSARY) {
				message = "Per the integration strategy, this request will be merged with target branch. "
						+ "However there are merge conflicts.";
			} else if (strategy == REBASE_SOURCE) {
				if (request.getSource() == null) {
					message = "Per the integration strategy, this request will be rebased on top of target "
							+ "branch before fast forwarding target branch to rebased result. However there "
							+ "are rebase conflicts.";
				} else {
					message = "Per the integration strategy, source branch will be rebased on top of target "
							+ "branch before fast forwarding target branch to rebased result. However there "
							+ "are rebase conflicts.";
				}
			} else {
				message = "Per the integration strategy, target branch will be rebased on top of this "
						+ "request. However there are rebase conflicts.";
			}
			fragment.add(new Label("message", message));
			
			DropdownPanel resolveInstructions = new DropdownPanel("resolveInstructions") {

				@Override
				protected Component newContent(String id) {
					return new ResolveConflictInstructionPanel(id, new EntityModel<PullRequest>(getPullRequest()));
				}
				
			};
			fragment.add(resolveInstructions);
			WebMarkupContainer resolveInstructionsTrigger = new WebMarkupContainer("resolveInstructionsTrigger");
			resolveInstructionsTrigger.add(new DropdownBehavior(resolveInstructions));
			fragment.add(resolveInstructionsTrigger);
		} else {
			fragment = new Fragment(id, "canIntegrateFrag", this);
			fragment.add(AttributeAppender.append("class", " alert alert-success"));

			String message;
			if (integrationInfo.getIntegrationHead().equals(integrationInfo.getRequestHead())) {
				message = "Per the integration strategy, target branch will be fast forwarded to this request.";
			} else {
				IntegrationStrategy strategy = integrationInfo.getIntegrationStrategy();
				if (strategy == MERGE_ALWAYS || strategy == MERGE_IF_NECESSARY) {
					message = "Per the integration strategy, target branch will be merged with this request.";
				} else if (strategy == REBASE_SOURCE) {
					if (request.getSource() == null) {
						message = "Per the integration strategy, this request will be rebased on top of target branch, "
								+ "and then target branch will be fast forwarded to rebased result.";
					} else {
						message = "Per the integration strategy, source branch will be rebased on top of target branch, "
								+ "and then target branch will be fast forwarded to rebased result.";
					}
				} else {
					message = "Per the integration strategy, target branch will be rebased on top of this request.";
				}
			}
			fragment.add(new Label("message", message));
			
			PageParameters params = RequestComparePage.paramsOf(
					request, 
					request.getLatestUpdate().getHeadCommit(), 
					request.getIntegrationInfo().getIntegrationHead(), 
					null, null);
			
			Link<Void> link = new BookmarkablePageLink<Void>("preview", RequestComparePage.class, params);
			link.setVisible(!integrationInfo.getIntegrationHead().equals(integrationInfo.getRequestHead())
					&& integrationInfo.hasChanges());
			fragment.add(link);

			String integrationHead = getPullRequest().getIntegrationInfo().getIntegrationHead();
			fragment.add(new VerificationStatusPanel("verification", requestModel, integrationHead) {

				@Override
				protected Component newStatusComponent(String id,
						com.pmease.gitplex.core.model.Verification.Status status) {
					if (status == Verification.Status.PASSED) {
						return new Label(id, "successful <i class='caret'></i>")
							.setEscapeModelStrings(false)
							.add(AttributeAppender.append("class", " label label-success"));
					} else if (status == Verification.Status.ONGOING) {
						return new Label(id, "running <i class='caret'></i>")
							.setEscapeModelStrings(false)
							.add(AttributeAppender.append("class", " label label-warning"));
					} else {
						return new Label(id, "failed <i class='caret'></i>")
							.setEscapeModelStrings(false)
							.add(AttributeAppender.append("class", " label label-danger"));
					} 
				}
				
			});
		}
		return fragment;
	}
	
	@Override
	protected void onDetach() {
		requestModel.detach();
		super.onDetach();
	}

	@Override
	public List<OldCommitComment> getCommitComments() {
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

	public static PageParameters paramsOf(PullRequest request) {
		PageParameters params = RepositoryInfoPage.paramsOf(request.getTarget().getRepository());
		params.set("request", request.getId());
		return params;
	}

	private class RequestTab extends PageTab {

		public RequestTab(String title, Class<? extends Page> pageClass) {
			super(Model.of(title), pageClass);
		}
		
		@Override
		public Component render(String componentId) {
			return new PageTabLink(componentId, this) {

				@Override
				protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
					return new BookmarkablePageLink<Void>(linkId, pageClass, paramsOf(getPullRequest()));
				}
				
			};
		}
		
	}
	
	public PullRequest getPullRequest() {
		return requestModel.getObject();
	}

	@Override
	protected void onBeforeRender() {
		addOrReplace(newStatusComponent("status"));
		
		super.onBeforeRender();
	}
	
}