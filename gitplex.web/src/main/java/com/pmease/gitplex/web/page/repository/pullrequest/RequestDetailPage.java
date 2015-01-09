package com.pmease.gitplex.web.page.repository.pullrequest;

import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.MERGE_ALWAYS;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.MERGE_IF_NECESSARY;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.MERGE_WITH_SQUASH;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.REBASE_SOURCE_ONTO_TARGET;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.REBASE_TARGET_ONTO_SOURCE;
import static com.pmease.gitplex.core.model.PullRequestOperation.*;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
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
import org.apache.wicket.markup.html.form.CheckBox;
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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.HibernateUtils;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.InheritableThreadLocalData;
import com.pmease.commons.wicket.behavior.AllowLeaveBehavior;
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
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.IntegrationPreview;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.model.PullRequest.Status;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestCommentReply;
import com.pmease.gitplex.core.model.PullRequestOperation;
import com.pmease.gitplex.core.model.PullRequestVerification;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.web.component.branch.BranchLink;
import com.pmease.gitplex.web.event.PullRequestChanged;
import com.pmease.gitplex.web.model.EntityModel;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public abstract class RequestDetailPage extends RepositoryPage {

	protected IModel<PullRequest> requestModel;
	
	private boolean editingTitle;
	
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

		final WebMarkupContainer requestTitle = new WebMarkupContainer("requestHead");
		requestTitle.setOutputMarkupId(true);
		add(requestTitle);
		
		requestTitle.add(new Label("status", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getStatus().toString();
			}
			
		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				setOutputMarkupId(true);
				
				add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						PullRequest.Status status = getPullRequest().getStatus();
						if (status == Status.DISCARDED)
							return " label-danger";
						else if (status == Status.INTEGRATED)
							return " label-success";
						else
							return " label-warning";
					}
					
				}));
			}

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PullRequestChanged) {
					PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
					pullRequestChanged.getTarget().add(this);
				}
			}
			
		});
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
				setVisible(!editingTitle && authorizationManager.canModifyRequest(getPullRequest()));
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
		
		WebMarkupContainer summaryContainer = new WebMarkupContainer("requestSummary") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PullRequestChanged) {
					PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
					pullRequestChanged.getTarget().add(this);
				}
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
//				setVisible(getPullRequest().getStatus() != Status.DISCARDED);
			}
			
		};
		summaryContainer.setOutputMarkupPlaceholderTag(true);
		add(summaryContainer);
		
		summaryContainer.add(newIntegratedNoteContainer());
		summaryContainer.add(newDiscardedNoteContainer());
		summaryContainer.add(newStatusReasonsContainer());
		summaryContainer.add(newIntegrationPreviewContainer());
		summaryContainer.add(newOperationsContainer());
		
		List<Tab> tabs = new ArrayList<>();
		
		tabs.add(new RequestTab("Overview", RequestOverviewPage.class));
		tabs.add(new RequestTab("Updates", RequestUpdatesPage.class));
		tabs.add(new RequestTab("Compare", RequestComparePage.class));
		
		add(new Tabbable("requestTabs", tabs) {

			@Override
			protected String getCssClasses() {
				return "nav nav-tabs";
			}
			
		}.setOutputMarkupId(true));
		
		add(new BackToTop("backToTop"));
		
		add(new WebSocketRenderBehavior() {

			@Override
			protected Object getTrait() {
				PullRequestChangeTrait trait = new PullRequestChangeTrait();

				// Do not call getPullRequest().getId() here to avoid unnecessary SQL query
				trait.requestId = HibernateUtils.getId(getPullRequest());
				return trait;
			}

			@Override
			protected void onRender(WebSocketRequestHandler handler) {
				send(getPage(), Broadcast.BREADTH, new PullRequestChanged(handler, getPullRequest()));
			}

		});
	}
	
	private WebMarkupContainer newIntegrationPreviewContainer() {
		WebMarkupContainer integrationPreviewContainer = new WebMarkupContainer("integrationPreview") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isOpen());
			}
			
		};
		integrationPreviewContainer.setOutputMarkupId(true);
		
		integrationPreviewContainer.add(new WebMarkupContainer("calculating") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getIntegrationPreview() == null);
			}
			
		});
		integrationPreviewContainer.add(new WebMarkupContainer("conflict") {
			
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
				IntegrationPreview preview = getPullRequest().getIntegrationPreview();
				setVisible(preview != null && preview.getIntegrated() == null);
			}

		});
		integrationPreviewContainer.add(new WebMarkupContainer("noConflict") {
			
			@Override
			protected void onInitialize() {
				super.onInitialize();

				PullRequest request = getPullRequest();
				IntegrationPreview preview = request.getIntegrationPreview();
				
				PageParameters params = RequestComparePage.paramsOf(
						request, request.getTarget().getHeadCommitHash(), 
						preview!=null?preview.getIntegrated():null, null);
				
				Link<Void> link = new BookmarkablePageLink<Void>("preview", RequestComparePage.class, params) {
					
					@Override
					protected void onConfigure() {
						super.onConfigure();

						PullRequest request = getPullRequest();
						IntegrationPreview preview = request.getIntegrationPreview();
						setVisible(!preview.getIntegrated().equals(preview.getRequestHead()));
					}
					
				};
				add(link);

				add(new VerificationStatusPanel("verification", requestModel, new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						PullRequest request = getPullRequest();
						IntegrationPreview preview = request.getIntegrationPreview();
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
				IntegrationPreview preview = getPullRequest().getIntegrationPreview();
				setVisible(preview != null && preview.getIntegrated() != null);
			}

		});
		
		return integrationPreviewContainer;
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
		operationsContainer.setOutputMarkupId(true);
		
		final String confirmId = "confirm";
		
		operationsContainer.add(new AjaxLink<Void>("approve") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationsContainer.replace(newOperationConfirm(confirmId, APPROVE, operationsContainer));
				target.add(operationsContainer);
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
				operationsContainer.replace(newOperationConfirm(confirmId, DISAPPROVE, operationsContainer));
				target.add(operationsContainer);
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
				operationsContainer.replace(newOperationConfirm(confirmId, INTEGRATE, operationsContainer));
				target.add(operationsContainer);
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
				operationsContainer.replace(newOperationConfirm(confirmId, DISCARD, operationsContainer));
				target.add(operationsContainer);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(DISCARD.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}

		});
		operationsContainer.add(new AjaxLink<Void>("reopen") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationsContainer.replace(newOperationConfirm(confirmId, REOPEN, operationsContainer));
				target.add(operationsContainer);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(REOPEN.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}

		});
		operationsContainer.add(new WebMarkupContainer(confirmId).setVisible(false));
		
		return operationsContainer;
	}
	
	private Component newOperationConfirm(final String id, final PullRequestOperation operation, 
			final WebMarkupContainer operationsContainer) {
		PullRequest request = getPullRequest();

		final Fragment fragment = new Fragment(id, "operationConfirmFrag", this);
		Form<?> form = new Form<Void>("form");
		fragment.add(form);
		final FormComponent<String> noteInput;
		final FormComponent<Boolean> deleteSourceCheck = new CheckBox("deleteSource", Model.of(false));

		Branch source = request.getSource();
		Preconditions.checkNotNull(source);
		if (operation != INTEGRATE 
				|| source.isDefault() 
				|| !GitPlex.getInstance(AuthorizationManager.class).canModifyBranch(source)) {
			deleteSourceCheck.setVisible(false);
		}
		
		form.add(deleteSourceCheck);
		if (operation != INTEGRATE) {
			form.add(noteInput = new MarkdownInput("note", Model.of("")));
			deleteSourceCheck.setVisible(false);
		} else {
			IntegrationPreview preview = request.getIntegrationPreview();
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
					if (deleteSourceCheck.getModelObject()) {
						boolean hasOpen = false;
						for (PullRequest each: request.getSource().getIncomingRequests()) {
							if (each.isOpen()) {
								hasOpen = true;
								break;
							}
						}
						if (hasOpen)
							Session.get().warn("Source branch is not deleted as there are pull requests opening against it.");
						else
							GitPlex.getInstance(BranchManager.class).delete(request.getSource());
					}
					setResponsePage(getPage().getClass(), paramsOf(getPullRequest()));
				} catch (Exception e) {
					error("Unable to " + actionName + ": " + e.getMessage());
					target.add(operationsContainer);
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
				if (operation == INTEGRATE)
					return "btn-success";
				else if (operation == DISCARD)
					return "btn-danger";
				else 
					return "btn-primary";
			}
			
		})));
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				fragment.replaceWith(new WebMarkupContainer(id).setVisible(false));
				target.add(operationsContainer);
			}
			
		});		
		
		return fragment;
	}
	
	private WebMarkupContainer newDiscardedNoteContainer() {
		WebMarkupContainer discardedNoteContainer = new WebMarkupContainer("discardedNote") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getStatus() == Status.DISCARDED);
			}
			
		};
		return discardedNoteContainer;
	}
	
	private WebMarkupContainer newIntegratedNoteContainer() {
		WebMarkupContainer integratedNoteContainer = new WebMarkupContainer("integratedNote") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getStatus() == Status.INTEGRATED);
			}
			
		};
		integratedNoteContainer.setOutputMarkupId(true);
		
		integratedNoteContainer.add(new WebMarkupContainer("fastForwarded") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				IntegrationPreview preview = request.getLastIntegrationPreview();
				setVisible(preview != null && preview.getRequestHead().equals(preview.getIntegrated()));
			}
			
		});
		integratedNoteContainer.add(new WebMarkupContainer("merged") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				IntegrationPreview preview = request.getLastIntegrationPreview();
				setVisible(preview != null 
						&& !preview.getRequestHead().equals(preview.getIntegrated())
						&& (preview.getIntegrationStrategy() == MERGE_ALWAYS || preview.getIntegrationStrategy() == MERGE_IF_NECESSARY));
			}
			
		});
		integratedNoteContainer.add(new WebMarkupContainer("mergedOutside") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(getPullRequest().getLastIntegrationPreview() == null);
			}
			
		});
		WebMarkupContainer squashedContainer = new WebMarkupContainer("squashed") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				IntegrationPreview preview = request.getLastIntegrationPreview();
				setVisible(preview != null 
						&& !preview.getRequestHead().equals(preview.getIntegrated())
						&& preview.getIntegrationStrategy() == MERGE_WITH_SQUASH);
			}
			
		};
		integratedNoteContainer.add(squashedContainer);
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
				IntegrationPreview preview = request.getLastIntegrationPreview();
				setVisible(preview != null 
						&& !preview.getRequestHead().equals(preview.getIntegrated())
						&& preview.getIntegrationStrategy() == REBASE_SOURCE_ONTO_TARGET);
			}
			
		};
		integratedNoteContainer.add(sourceRebasedContainer);
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
				IntegrationPreview preview = request.getLastIntegrationPreview();
				setVisible(preview != null 
						&& !preview.getRequestHead().equals(preview.getIntegrated())
						&& preview.getIntegrationStrategy() == REBASE_TARGET_ONTO_SOURCE);
			}
			
		};
		integratedNoteContainer.add(targetRebasedContainer);
		targetRebasedContainer.add(new BranchLink("target", new EntityModel<Branch>(getPullRequest().getTarget())));
		
		return integratedNoteContainer;
	}

	private WebMarkupContainer newStatusReasonsContainer() {
		WebMarkupContainer statusReasonsContainer = new WebMarkupContainer("statusReasons") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequest.Status status = getPullRequest().getStatus();
				setVisible(status == Status.PENDING_APPROVAL || status == Status.PENDING_UPDATE);
			}
			
		};
		statusReasonsContainer.setOutputMarkupId(true);
		
		statusReasonsContainer.add(new ListView<String>("reasons", new AbstractReadOnlyModel<List<String>>() {

			@Override
			public List<String> getObject() {
				return getPullRequest().getCheckResult().getReasons();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("reason", item.getModelObject()));
			}

		});
		
		return statusReasonsContainer;
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

	public static class Updater implements PullRequestListener {
		
		private final Dao dao;
		
		@Inject
		public Updater(Dao dao) {
			this.dao = dao;
		}
		
		@Override
		public void onOpened(PullRequest request) {
		}

		@Override
		public void onUpdated(PullRequest request) {
			onChange(request);
		}

		@Override
		public void onReviewed(Review review) {
			onChange(review.getUpdate().getRequest());
		}

		@Override
		public void onIntegrated(PullRequest request) {
			onChange(request);
		}

		@Override
		public void onDiscarded(PullRequest request) {
			onChange(request);
		}

		private void onChange(final PullRequest request) {
			/*
			 * Make sure that pull request and associated objects are committed before
			 * sending render request; otherwise rendering request may not reflect
			 * expected status as rendering happens in another thread which may get
			 * executed before pull request modification is committed.
			 */
			dao.afterCommit(new Runnable() {

				@Override
				public void run() {
					PullRequestChangeTrait trait = new PullRequestChangeTrait();
					trait.requestId = request.getId();
					WebSocketRenderBehavior.requestToRender(trait, PageId.fromObj(InheritableThreadLocalData.get()));
				}
				
			});
		}
		
		@Override
		public void onIntegrationPreviewCalculated(PullRequest request) {
			onChange(request);
		}

		@Override
		public void onCommented(PullRequestComment comment) {
			onChange(comment.getRequest());
		}

		@Override
		public void onVerified(PullRequest request) {
			onChange(request);
		}

		@Override
		public void onAssigned(PullRequest request) {
			onChange(request);
		}

		@Override
		public void onCommentReplied(PullRequestCommentReply reply) {
			onChange(reply.getComment().getRequest());
		}
		
	}
}