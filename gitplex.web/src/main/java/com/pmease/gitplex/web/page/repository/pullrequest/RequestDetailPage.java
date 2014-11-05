package com.pmease.gitplex.web.page.repository.pullrequest;

import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.MERGE_ALWAYS;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.MERGE_IF_NECESSARY;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.MERGE_WITH_SQUASH;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.REBASE_SOURCE_ONTO_TARGET;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.REBASE_TARGET_ONTO_SOURCE;
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
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
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
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.PersonIdent;

import com.google.common.base.Objects;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.InheritableThreadLocalData;
import com.pmease.commons.wicket.behavior.AllowLeaveBehavior;
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
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior.PageId;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.IntegrationPreview;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.model.PullRequestOperation;
import com.pmease.gitplex.core.model.PullRequestVerification;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.component.branch.BranchLink;
import com.pmease.gitplex.web.component.comment.event.PullRequestChanged;
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
	
	private WebMarkupContainer overviewContainer;
	
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

		overviewContainer = new WebMarkupContainer("requestOverview") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PullRequestChanged) {
					PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
					pullRequestChanged.getTarget().add(this);
				}
			}
			
		};
		
		add(overviewContainer);
		overviewContainer.add(new Label("title", new AbstractReadOnlyModel<String>() {

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
		overviewContainer.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PullRequest.Status status = getPullRequest().getStatus();
				if (status == INTEGRATED)
					return "panel-success integrated";
				else if (status == DISCARDED)
					return "panel-danger discarded";
				else if (status == PENDING_INTEGRATE)
					return "panel-default pending-integrate";
				else if (status == PENDING_UPDATE)
					return "panel-default pending-update";
				else 
					return "panel-default pending-approval";
			}
			
		}));
		overviewContainer.setOutputMarkupId(true);
		overviewContainer.add(newStatusContainer());
		overviewContainer.add(newIntegrationContainer());
		overviewContainer.add(newOperationsContainer());
		
		overviewContainer.add(new WebSocketRenderBehavior(true) {

			@Override
			protected Object getTrait() {
				IntegrationPreviewUpdateTrait trait = new IntegrationPreviewUpdateTrait();
				trait.requestId = getPullRequest().getId();
				return trait;
			}
			
		});
		
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
		
		add(new WebSocketRenderBehavior(false) {

			@Override
			protected Object getTrait() {
				PullRequestChangeTrait trait = new PullRequestChangeTrait();
				trait.requestId = getPullRequest().getId();
				return trait;
			}

			@Override
			protected void onRender(WebSocketRequestHandler handler) {
				send(getPage(), Broadcast.BREADTH, new PullRequestChanged(handler, getPullRequest()));
			}

		});
	}
	
	private WebMarkupContainer newOperationsContainer() {
		final WebMarkupContainer operationsContainer = new WebMarkupContainer("operations") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				boolean hasVisibleChildren = false;
				for (int i=0; i<size(); i++) {
					Component child = get(i);
					child.configure();
					if (child.isVisible()) {
						hasVisibleChildren = true;
						break;
					}
				}
				
				setVisible(hasVisibleChildren);
			}
			
		};
		
		final String confirmId = "confirm";
		
		operationsContainer.add(new AjaxLink<Void>("approve") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationsContainer.replace(newOperationConfirm(confirmId, APPROVE));
				target.add(overviewContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(APPROVE.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}
			
		});
		
		operationsContainer.add(new AjaxLink<Void>("disapprove") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationsContainer.replace(newOperationConfirm(confirmId, DISAPPROVE));
				target.add(overviewContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(DISAPPROVE.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}
			
		});
		
		operationsContainer.add(new AjaxLink<Void>("integrate") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationsContainer.replace(newOperationConfirm(confirmId, INTEGRATE));
				target.add(overviewContainer);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(INTEGRATE.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}

		});
		
		operationsContainer.add(new AjaxLink<Void>("discard") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationsContainer.replace(newOperationConfirm(confirmId, DISCARD));
				target.add(overviewContainer);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(DISCARD.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}

		});
		operationsContainer.add(new WebMarkupContainer(confirmId).setVisible(false));
		
		return operationsContainer;
	}
	
	private Component newOperationConfirm(final String id, final PullRequestOperation operation) {
		final Fragment fragment = new Fragment(id, "operationConfirmFrag", this);
		Form<?> form = new Form<Void>("form");
		fragment.add(form);
		final FormComponent<String> noteInput;
		if (operation != INTEGRATE) {
			form.add(noteInput = new MarkdownInput("note", Model.of("")));
		} else {
			PullRequestManager manager = GitPlex.getInstance(PullRequestManager.class);
			IntegrationPreview preview = manager.previewIntegration(getPullRequest());
			if (preview == null || preview.getIntegrated() == null) {
				Session.get().warn("Unable to integrate now as integration preview has to be recalculated");
				return new WebMarkupContainer(id).setVisible(false);
			}
			IntegrationStrategy strategy = preview.getIntegrationStrategy();
			if (strategy == REBASE_SOURCE_ONTO_TARGET 
					|| strategy == REBASE_TARGET_ONTO_SOURCE
					|| preview.getIntegrated().equals(preview.getRequestHead())) {
				form.add(noteInput = new MarkdownInput("note", Model.of("")));
			} else {
				Fragment noteFragment = new Fragment("note", "commitMessageFrag", this);
				noteFragment.add(noteInput = new TextArea<String>("commitMessage", Model.of("")));
				noteInput.add(new OnChangeAjaxBehavior() {

					@Override
					protected void onUpdate(AjaxRequestTarget target) {
					}
					
				});
				form.add(noteFragment);
			}
		}
		form.add(new FeedbackPanel("feedback", form));
		form.add(new AjaxButton("submit") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(new AllowLeaveBehavior());
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				String actionName = operation.name().toLowerCase();

				InheritableThreadLocalData.set(new PageId(getPage().getPageId()));
				PullRequest request = getPullRequest();
				try {
					operation.operate(request, noteInput.getModelObject());
					setResponsePage(getPage().getClass(), paramsOf(getPullRequest()));
				} catch (Exception e) {
					error("Unable to " + actionName + ": " + e.getMessage());
					target.add(overviewContainer);
				} finally {
					InheritableThreadLocalData.clear();
				}
			}

		}.add(AttributeModifier.replace("value", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "Confirm " + WordUtils.capitalizeFully(operation.name());
			}
			
		})).add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (operation == APPROVE)
					return "btn-primary";
				else if (operation == DISAPPROVE)
					return "btn-primary";
				else if (operation == INTEGRATE)
					return "btn-success";
				else
					return "btn-danger";
			}
			
		})));
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				fragment.replaceWith(new WebMarkupContainer(id).setVisible(false));
				target.add(overviewContainer);
			}
			
		});		
		
		return fragment;
	}
	
	private WebMarkupContainer newIntegrationContainer() {
		WebMarkupContainer integrationContainer = new WebMarkupContainer("integration") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isOpen());
			}
			
		};
		
		PullRequest request = getPullRequest();
		
		final List<IntegrationStrategy> strategies = 
				GitPlex.getInstance(PullRequestManager.class).getApplicableIntegrationStrategies(request);
		if (!strategies.contains(request.getIntegrationStrategy())) {
			request.setIntegrationStrategy(strategies.get(0));
			GitPlex.getInstance(Dao.class).persist(request);
		}
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
				target.add(overviewContainer);
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
						request, request.getTarget().getHeadCommitHash(), 
						preview!=null?preview.getIntegrated():null, null);
				
				Link<Void> link = new BookmarkablePageLink<Void>("preview", RequestComparePage.class, params) {
					
					@Override
					protected void onConfigure() {
						super.onConfigure();

						PullRequest request = getPullRequest();
						PullRequestManager manager = GitPlex.getInstance(PullRequestManager.class);
						IntegrationPreview preview = manager.previewIntegration(request);
						setVisible(!preview.getIntegrated().equals(preview.getRequestHead()));
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
					protected Component newStatusComponent(String id, final IModel<PullRequestVerification.Status> statusModel) {
						return new Label(id, new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								if (statusModel.getObject() == PullRequestVerification.Status.PASSED)
									return "successful <i class='caret'></i>";
								else if (statusModel.getObject() == PullRequestVerification.Status.ONGOING)
									return "running <i class='caret'></i>";
								else if (statusModel.getObject() == PullRequestVerification.Status.NOT_PASSED) 
									return "failed <i class='caret'></i>";
								else 
									return "";
							}
							
						}) {

							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								
								if (statusModel.getObject() == PullRequestVerification.Status.PASSED)
									tag.put("class", "label label-success");
								else if (statusModel.getObject() == PullRequestVerification.Status.ONGOING)
									tag.put("class", "label label-warning");
								else if (statusModel.getObject() == PullRequestVerification.Status.NOT_PASSED) 
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
		WebMarkupContainer statusContainer = new WebMarkupContainer("status") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				boolean hasVisibleChildren = false;
				for (int i=0; i<size(); i++) {
					Component child = get(i);
					child.configure();
					if (child.isVisible()) {
						hasVisibleChildren = true;
						break;
					}
				}
				
				setVisible(hasVisibleChildren);
			}
			
		};
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
		WebMarkupContainer squashedContainer = new WebMarkupContainer("squashed") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				IntegrationPreview preview = request.getIntegrationPreview();
				setVisible(request.getStatus() == INTEGRATED && preview != null 
						&& !preview.getRequestHead().equals(preview.getIntegrated())
						&& preview.getIntegrationStrategy() == MERGE_WITH_SQUASH);
			}
			
		};
		statusContainer.add(squashedContainer);
		squashedContainer.add(new BranchLink("target", new EntityModel<Branch>(getPullRequest().getTarget())));
		squashedContainer.add(new BranchLink("source", new AbstractReadOnlyModel<Branch>() {

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
		
		WebMarkupContainer sourceRebasedContainer = new WebMarkupContainer("sourceRebased") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				IntegrationPreview preview = request.getIntegrationPreview();
				setVisible(request.getStatus() == INTEGRATED && preview != null 
						&& !preview.getRequestHead().equals(preview.getIntegrated())
						&& preview.getIntegrationStrategy() == REBASE_SOURCE_ONTO_TARGET);
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
						&& preview.getIntegrationStrategy() == REBASE_TARGET_ONTO_SOURCE);
			}
			
		};
		statusContainer.add(targetRebasedContainer);
		targetRebasedContainer.add(new BranchLink("target", new EntityModel<Branch>(getPullRequest().getTarget())));
		
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
	
	private static class PullRequestChangeTrait {
		
		private Long requestId;

		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass())  
				return false;  
			final PullRequestChangeTrait other = (PullRequestChangeTrait) obj;  
		    return Objects.equal(requestId, other.requestId);
		}
		
	}

	private static class IntegrationPreviewUpdateTrait {
		
		private Long requestId;

		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass())  
				return false;  
			final IntegrationPreviewUpdateTrait other = (IntegrationPreviewUpdateTrait) obj;  
		    return Objects.equal(requestId, other.requestId);
		}
		
	}
	
	public static class Updater implements PullRequestListener {
		
		@Override
		public void onOpened(PullRequest request) {
		}

		@Override
		public void onUpdated(PullRequest request) {
			onChange(request);
		}

		@Override
		public void onVoted(PullRequest request) {
			onChange(request);
		}

		@Override
		public void onIntegrated(PullRequest request) {
			onChange(request);
		}

		@Override
		public void onDiscarded(PullRequest request) {
			onChange(request);
		}

		private void onChange(PullRequest request) {
			PullRequestChangeTrait trait = new PullRequestChangeTrait();
			trait.requestId = request.getId();
			WebSocketRenderBehavior.requestToRender(trait, PageId.fromObj(InheritableThreadLocalData.get()));
		}
		
		@Override
		public void onIntegrationPreviewCalculated(PullRequest request) {
			IntegrationPreviewUpdateTrait trait = new IntegrationPreviewUpdateTrait();
			trait.requestId = request.getId();
			WebSocketRenderBehavior.requestToRender(trait, null);
		}

		@Override
		public void onCommented(PullRequest request) {
			onChange(request);
		}
		
	}
}