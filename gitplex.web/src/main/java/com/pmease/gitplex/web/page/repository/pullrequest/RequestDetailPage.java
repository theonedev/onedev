package com.pmease.gitplex.web.page.repository.pullrequest;

import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.MERGE_ALWAYS;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.MERGE_IF_NECESSARY;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.REBASE_SOURCE_BRANCH;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.REBASE_TARGET_BRANCH;
import static com.pmease.gitplex.core.model.PullRequest.Status.DISCARDED;
import static com.pmease.gitplex.core.model.PullRequest.Status.INTEGRATED;
import static com.pmease.gitplex.core.model.PullRequest.Status.PENDING_INTEGRATE;
import static com.pmease.gitplex.core.model.PullRequest.Status.PENDING_UPDATE;
import static com.pmease.gitplex.core.model.PullRequestOperation.APPROVE;
import static com.pmease.gitplex.core.model.PullRequestOperation.DISAPPROVE;
import static com.pmease.gitplex.core.model.PullRequestOperation.DISCARD;
import static com.pmease.gitplex.core.model.PullRequestOperation.INTEGRATE;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.component.backtotop.BackToTop;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.commons.wicket.component.markdown.MarkdownInput;
import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.PageTabLink;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.IntegrationPreview;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.model.PullRequestOperation;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.Verification;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.component.branch.BranchLink;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.PersonLink;
import com.pmease.gitplex.web.model.EntityModel;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public abstract class RequestDetailPage extends RepositoryPage {

	protected IModel<PullRequest> requestModel;
	
	private boolean editingTitle;
	
	private PullRequestOperation operationToConfirm;
	
	public RequestDetailPage(final PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));

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

		final WebMarkupContainer requestTitle = new WebMarkupContainer("requestTitle");
		requestTitle.setOutputMarkupId(true);
		add(requestTitle);
		
		requestTitle.add(new Label("title", new AbstractReadOnlyModel<String>() {

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
		
		requestTitle.add(new Label("id", "#" + request.getId()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!editingTitle);
			}
			
		});
		
		requestTitle.add(new AjaxLink<Void>("editLink") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				editingTitle = true;
				
				target.add(requestTitle);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();

				AuthorizationManager authorizationManager = GitPlex.getInstance(AuthorizationManager.class);
				setVisible(!editingTitle && authorizationManager.canModify(getPullRequest()));
			}
			
		});

		Form<?> form = new Form<Void>("editForm") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(editingTitle);
			}
			
		};
		requestTitle.add(form);
		
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

				target.add(requestTitle);
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				editingTitle = false;
				
				target.add(requestTitle);
			}
			
		});
		
		User submitter = request.getSubmitter();
		if (submitter != null) {
			PersonIdent person = new PersonIdent(submitter.getName(), submitter.getEmail());
			add(new PersonLink("requestSubmitter", Model.of(person), AvatarMode.NAME_AND_AVATAR));
		} else {
			add(new Label("requestSubmitter", "<i>System</i>").setEscapeModelStrings(false));
		}
		
		add(new BranchLink("targetBranch", new EntityModel<Branch>(request.getTarget())));
		
		BranchLink branchLink = new BranchLink("sourceBranch", new EntityModel<Branch>(request.getSource()));
		branchLink.setVisible(request.getSource() != null);
		add(branchLink);
		
		add(new AgeLabel("requestDate", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getPullRequest().getCreateDate();
			}
			
		}));

		add(newStatusContainer());
		add(newIntegrationContainer());
		add(newOperationsContainer());
		
		List<Tab> tabs = new ArrayList<>();
		
		tabs.add(new RequestTab("Discussions", RequestActivitiesPage.class));
		tabs.add(new RequestTab("Updates", RequestUpdatesPage.class));
		tabs.add(new RequestTab("Compare", RequestComparePage.class));
		
		add(new Tabbable("requestTabs", tabs) {

			@Override
			protected String getCssClasses() {
				return "nav nav-tabs";
			}
			
		}.setOutputMarkupId(true));
		
		add(new BackToTop("backToTop"));
	}
	
	private WebMarkupContainer newOperationsContainer() {
		final WebMarkupContainer operationsContainer = new WebMarkupContainer("requestOperations");
		operationsContainer.setOutputMarkupId(true);
		
		operationsContainer.add(new AjaxLink<Void>("approve") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationToConfirm = APPROVE;
				target.add(operationsContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(APPROVE.canOperate(getPullRequest()) && operationToConfirm == null);
			}
			
		});
		
		operationsContainer.add(new AjaxLink<Void>("disapprove") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationToConfirm = DISAPPROVE;
				target.add(operationsContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(DISAPPROVE.canOperate(getPullRequest()) && operationToConfirm == null);
			}
			
		});
		
		operationsContainer.add(new AjaxLink<Void>("integrate") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationToConfirm = INTEGRATE;
				target.add(operationsContainer);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(INTEGRATE.canOperate(getPullRequest()) && operationToConfirm == null);
			}

		});
		
		operationsContainer.add(new AjaxLink<Void>("discard") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationToConfirm = DISCARD;
				target.add(operationsContainer);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(DISCARD.canOperate(getPullRequest()) && operationToConfirm == null);
			}

		});
		
		Form<?> confirmForm = new Form<Void>("confirm") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(operationToConfirm != null);
			}
			
		};
		operationsContainer.add(confirmForm);
		
		final FormComponent<String> commentInput = new MarkdownInput("comment", Model.of(""));
		confirmForm.add(commentInput);
		confirmForm.add(new FeedbackPanel("feedback", confirmForm));
		confirmForm.add(new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				String actionName = operationToConfirm.name().toLowerCase();

				PullRequest request = getPullRequest();
				try {
					operationToConfirm.operate(request, commentInput.getModelObject());
					operationToConfirm = null;
					commentInput.setModelObject(null);

					setResponsePage(getPage().getClass(), paramsOf(getPullRequest()));
				} catch (Exception e) {
					error("Unable to " + actionName + ": " + e.getMessage());
					target.add(operationsContainer);
				}
			}

		}.add(AttributeModifier.replace("value", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "Confirm " + WordUtils.capitalizeFully(operationToConfirm.name());
			}
			
		})).add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (operationToConfirm == APPROVE)
					return "btn-primary";
				else if (operationToConfirm == DISAPPROVE)
					return "btn-primary";
				else if (operationToConfirm == INTEGRATE)
					return "btn-success";
				else
					return "btn-danger";
			}
			
		})));
		confirmForm.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationToConfirm = null;
				commentInput.setModelObject(null);
				target.add(operationsContainer);
			}
			
		});		
		
		return operationsContainer;
	}
	
	private WebMarkupContainer newIntegrationContainer() {
		final WebMarkupContainer integrationContainer = new WebMarkupContainer("requestIntegration") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isOpen());
			}
			
		};
		integrationContainer.setOutputMarkupId(true);
		
		PullRequest request = getPullRequest();
		
		final List<IntegrationStrategy> strategies = 
				GitPlex.getInstance(PullRequestManager.class).getApplicableIntegrationStrategies(request);
		IModel<IntegrationStrategy> strategyModel = new IModel<IntegrationStrategy>() {

			@Override
			public void detach() {
			}

			@Override
			public IntegrationStrategy getObject() {
				return getPullRequest().getIntegrationStrategy();
			}

			@Override
			public void setObject(IntegrationStrategy object) {
				getPullRequest().setIntegrationStrategy(object);
			}
			
		};
		
		DropDownChoice<IntegrationStrategy> strategySelect = 
				new DropDownChoice<IntegrationStrategy>("strategySelect", strategyModel, strategies) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				ObjectPermission writePermission = ObjectPermission.ofRepositoryWrite(getRepository());
				setVisible(SecurityUtils.getSubject().isPermitted(writePermission) && strategies.size() > 1);						
			}
			
		};
		strategySelect.add(new OnChangeAjaxBehavior() {
					
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				GitPlex.getInstance(Dao.class).persist(getPullRequest());
				target.add(integrationContainer);
			}
			
		});
		integrationContainer.add(strategySelect);
		
		integrationContainer.add(new Label("strategyLabel", request.getIntegrationStrategy()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				ObjectPermission writePermission = ObjectPermission.ofRepositoryWrite(getRepository());
				setVisible(!SecurityUtils.getSubject().isPermitted(writePermission) || strategies.size() == 1);						
			}
			
		});

		ObjectPermission writePermission = ObjectPermission.ofRepositoryWrite(getRepository());

		if (!SecurityUtils.getSubject().isPermitted(writePermission) || strategies.size() == 1) {
			integrationContainer.add(new WebMarkupContainer("strategyHelp").add(
					new TooltipBehavior(Model.of(getPullRequest().getIntegrationStrategy().getDescription()))));
		} else {
			StringBuilder strategyHelp = new StringBuilder("<dl class='integration-strategy-help'>");
			
			for (IntegrationStrategy strategy: strategies) {
				strategyHelp.append("<dt>").append(strategy.toString()).append("</dt>");
				strategyHelp.append("<dd>").append(strategy.getDescription()).append("</dd>");
			}

			strategyHelp.append("</dl>");
			
			integrationContainer.add(new WebMarkupContainer("strategyHelp")
						.add(AttributeAppender.append("data-html", "true"))
						.add(new TooltipBehavior(Model.of(strategyHelp.toString()), new TooltipConfig().withPlacement(Placement.right))));
		}

		integrationContainer.add(new WebMarkupContainer("calculating") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequestManager manager = GitPlex.getInstance(PullRequestManager.class);
				setVisible(manager.previewIntegration(getPullRequest()) == null);
			}
			
		});
		integrationContainer.add(new WebMarkupContainer("conflict") {
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				DropdownPanel resolveInstructions = new DropdownPanel("resolveInstructions", true) {

					@Override
					protected Component newContent(String id) {
						return new ResolveConflictInstructionPanel(id, new EntityModel<PullRequest>(getPullRequest()));
					}
					
				};
				add(resolveInstructions);
				WebMarkupContainer resolveInstructionsTrigger = new WebMarkupContainer("resolveInstructionsTrigger") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getPullRequest().getSource() != null);
					}
					
				};
				resolveInstructionsTrigger.add(new DropdownBehavior(resolveInstructions));
				add(resolveInstructionsTrigger);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequestManager manager = GitPlex.getInstance(PullRequestManager.class);
				IntegrationPreview preview = manager.previewIntegration(getPullRequest());
				setVisible(preview != null && preview.getIntegrated() == null);
			}

		});
		integrationContainer.add(new WebMarkupContainer("noConflict") {
			
			@Override
			protected void onInitialize() {
				super.onInitialize();

				PullRequest request = getPullRequest();
				PullRequestManager manager = GitPlex.getInstance(PullRequestManager.class);
				IntegrationPreview preview = manager.previewIntegration(request);
				
				PageParameters params = RequestComparePage.paramsOf(
						request, request.getLatestUpdate().getHeadCommitHash(), 
						preview!=null?preview.getIntegrated():null, null, null);
				
				Link<Void> link = new BookmarkablePageLink<Void>("preview", RequestComparePage.class, params) {
					
					@Override
					protected void onConfigure() {
						super.onConfigure();

						PullRequest request = getPullRequest();
						PullRequestManager manager = GitPlex.getInstance(PullRequestManager.class);
						IntegrationPreview preview = manager.previewIntegration(request);
						setVisible(!preview.getIntegrated().equals(preview.getRequestHead())
								&& !getRepository().getChanges(preview.getRequestHead(), preview.getIntegrated()).isEmpty());
					}
					
				};
				add(link);

				add(new VerificationStatusPanel("verification", requestModel, new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						PullRequest request = getPullRequest();
						PullRequestManager manager = GitPlex.getInstance(PullRequestManager.class);
						IntegrationPreview preview = manager.previewIntegration(request);
						if (preview != null)
							return preview.getIntegrated();
						else
							return null;
					}
					
				}) {

					@Override
					protected Component newStatusComponent(String id, final IModel<Verification.Status> statusModel) {
						return new Label(id, new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								if (statusModel.getObject() == Verification.Status.PASSED)
									return "successful <i class='caret'></i>";
								else if (statusModel.getObject() == Verification.Status.ONGOING)
									return "running <i class='caret'></i>";
								else if (statusModel.getObject() == Verification.Status.NOT_PASSED) 
									return "failed <i class='caret'></i>";
								else 
									return "";
							}
							
						}) {

							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								
								if (statusModel.getObject() == Verification.Status.PASSED)
									tag.put("class", "label label-success");
								else if (statusModel.getObject() == Verification.Status.ONGOING)
									tag.put("class", "label label-warning");
								else if (statusModel.getObject() == Verification.Status.NOT_PASSED) 
									tag.put("class", "label label-danger");
							}

							@Override
							protected void onDetach() {
								statusModel.detach();
								
								super.onDetach();
							}
							
						}.setEscapeModelStrings(false);
					}
					
				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequestManager manager = GitPlex.getInstance(PullRequestManager.class);
				IntegrationPreview preview = manager.previewIntegration(getPullRequest());
				setVisible(preview != null && preview.getIntegrated() != null);
			}

		});
		
		return integrationContainer;
	}
	
	private WebMarkupContainer newStatusContainer() {
		WebMarkupContainer statusContainer = new WebMarkupContainer("requestStatus");
		statusContainer.setOutputMarkupId(true);
		statusContainer.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (getPullRequest().getStatus() == DISCARDED)
					return " alert alert-danger";
				else if (getPullRequest().getStatus() == INTEGRATED)
					return " alert alert-success";
				else 
					return " well";
			}
			
		}));
		statusContainer.add(new Label("summary", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (getPullRequest().getStatus() == DISCARDED)
					return "This request is discarded";
				else if (getPullRequest().getStatus() == INTEGRATED)
					return "This request is integrated";
				else if (getPullRequest().getStatus() == PENDING_INTEGRATE)
					return "This request is pending integrate";
				else if (getPullRequest().getStatus() == PENDING_UPDATE)
					return "This request is pending update";
				else 
					return "This request is pending approval";
			}
			
		}));
		statusContainer.add(new WebMarkupContainer("fastForwarded") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				IntegrationPreview preview = request.getIntegrationPreview();
				setVisible(request.getStatus() == INTEGRATED && preview != null 
						&& preview.getRequestHead().equals(preview.getIntegrated()));
			}
			
		});
		statusContainer.add(new WebMarkupContainer("merged") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				IntegrationPreview preview = request.getIntegrationPreview();
				setVisible(request.getStatus() == INTEGRATED && preview != null 
						&& !preview.getRequestHead().equals(preview.getIntegrated())
						&& (preview.getIntegrationStrategy() == MERGE_ALWAYS || preview.getIntegrationStrategy() == MERGE_IF_NECESSARY));
			}
			
		});
		statusContainer.add(new WebMarkupContainer("mergedOutside") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				IntegrationPreview preview = request.getIntegrationPreview();
				setVisible(request.getStatus() == INTEGRATED && preview == null);
			}
			
		});
		WebMarkupContainer sourceRebasedContainer = new WebMarkupContainer("sourceRebased") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				IntegrationPreview preview = request.getIntegrationPreview();
				setVisible(request.getStatus() == INTEGRATED && preview != null 
						&& !preview.getRequestHead().equals(preview.getIntegrated())
						&& preview.getIntegrationStrategy() == REBASE_SOURCE_BRANCH);
			}
			
		};
		statusContainer.add(sourceRebasedContainer);
		sourceRebasedContainer.add(new BranchLink("target", new EntityModel<Branch>(getPullRequest().getTarget())));
		sourceRebasedContainer.add(new BranchLink("source", new AbstractReadOnlyModel<Branch>() {

			@Override
			public Branch getObject() {
				return getPullRequest().getSource();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getSource() != null);
			}
			
		});
		
		WebMarkupContainer targetRebasedContainer = new WebMarkupContainer("targetRebased") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				IntegrationPreview preview = request.getIntegrationPreview();
				setVisible(request.getStatus() == INTEGRATED && preview != null 
						&& !preview.getRequestHead().equals(preview.getIntegrated())
						&& preview.getIntegrationStrategy() == REBASE_TARGET_BRANCH);
			}
			
		};
		statusContainer.add(targetRebasedContainer);
		targetRebasedContainer.add(new BranchLink("target", new EntityModel<Branch>(getPullRequest().getTarget())));
		
		statusContainer.add(new WebMarkupContainer("discarded") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(getPullRequest().getStatus() == DISCARDED);
			}
			
		});
		
		statusContainer.add(new ListView<String>("reasons", new AbstractReadOnlyModel<List<String>>() {

			@Override
			public List<String> getObject() {
				return getPullRequest().getCheckResult().getReasons();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("reason", item.getModelObject()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequest request = getPullRequest();
				setVisible(request.isOpen() && !request.getCheckResult().getReasons().isEmpty());				
			}

		});

		return statusContainer;
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(PullRequest request) {
		PageParameters params = RepositoryPage.paramsOf(request.getTarget().getRepository());
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

}