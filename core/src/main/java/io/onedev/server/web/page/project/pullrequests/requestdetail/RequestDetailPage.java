package io.onedev.server.web.page.project.pullrequests.requestdetail;

import static io.onedev.server.model.support.MergeStrategy.ALWAYS_MERGE;
import static io.onedev.server.model.support.MergeStrategy.MERGE_IF_NECESSARY;
import static io.onedev.server.model.support.MergeStrategy.REBASE_MERGE;
import static io.onedev.server.model.support.MergeStrategy.SQUASH_MERGE;
import static io.onedev.server.web.page.project.pullrequests.requestdetail.PullRequestOperation.APPROVE;
import static io.onedev.server.web.page.project.pullrequests.requestdetail.PullRequestOperation.DELETE_SOURCE_BRANCH;
import static io.onedev.server.web.page.project.pullrequests.requestdetail.PullRequestOperation.DISAPPROVE;
import static io.onedev.server.web.page.project.pullrequests.requestdetail.PullRequestOperation.DISCARD;
import static io.onedev.server.web.page.project.pullrequests.requestdetail.PullRequestOperation.REOPEN;
import static io.onedev.server.web.page.project.pullrequests.requestdetail.PullRequestOperation.RESTORE_SOURCE_BRANCH;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.manager.PullRequestUpdateManager;
import io.onedev.server.manager.VerificationManager;
import io.onedev.server.manager.VisitManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.MergePreview;
import io.onedev.server.model.support.MergeStrategy;
import io.onedev.server.model.support.ProjectAndBranch;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Verification;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.comment.ProjectAttachmentSupport;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.BranchLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabLink;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.verification.VerificationStatusPanel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.NoCommitsPage;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.pullrequests.InvalidRequestPage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.changes.RequestChangesPage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.codecomments.RequestCodeCommentsPage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.mergepreview.MergePreviewPage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.overview.RequestOverviewPage;
import io.onedev.server.web.util.DateUtils;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.util.model.EntityModel;
import io.onedev.server.web.websocket.PageDataChanged;
import io.onedev.server.web.websocket.PullRequestChangedRegion;
import io.onedev.server.web.websocket.WebSocketRegion;

@SuppressWarnings("serial")
public abstract class RequestDetailPage extends ProjectPage {

	public static final String PARAM_REQUEST = "request";
	
	private static final String HINT_ID = "hint";
	
	protected IModel<PullRequest> requestModel;
	
	private boolean editingTitle;
	
	private Long reviewUpdateId;
	
	public RequestDetailPage(PageParameters params) {
		super(params);
		
		if (getProject().getDefaultBranch() == null) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getProject()));

		requestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				Long requestNumber = params.get(PARAM_REQUEST).toLong();
				PullRequest request = OneDev.getInstance(PullRequestManager.class).find(getProject(), requestNumber);
				if (request == null)
					throw new EntityNotFoundException("Unable to find request #" + requestNumber + " in project " + getProject());
				return request;
			}

		};
		
		if (!getPullRequest().isValid())
			throw new RestartResponseException(InvalidRequestPage.class, InvalidRequestPage.paramsOf(getPullRequest()));
			

		reviewUpdateId = requestModel.getObject().getLatestUpdate().getId();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		PullRequest request = getPullRequest();

		WebMarkupContainer requestTitle = new WebMarkupContainer("requestHead");
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
		
		requestTitle.add(new Label("number", "#" + request.getNumber()) {

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

				setVisible(!editingTitle && SecurityUtils.canModify(getPullRequest()));
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
					OneDev.getInstance(Dao.class).persist(getPullRequest());
					editingTitle = false;
				}

				target.add(requestTitle);
				target.appendJavaScript("$(window).resize();");
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				editingTitle = false;
				target.add(requestTitle);
				target.appendJavaScript("$(window).resize();");
			}
			
		});
		
		add(newStatusAndBranchesContainer());

		WebMarkupContainer summaryContainer = new WebMarkupContainer("requestSummary") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PageDataChanged) {
					PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
					for (Component child: this) {
						if (child instanceof MarkupContainer) {
							MarkupContainer container = (MarkupContainer) child;
							Form<?> form = container.visitChildren(Form.class, new IVisitor<Form<?>, Form<?>>() {

								@Override
								public void component(Form<?> object, IVisit<Form<?>> visit) {
									visit.stop(object);
								}
								
							});
							if (form == null) {
								pageDataChanged.getHandler().add(child);
							}
						} else if (!(child instanceof Form)) {
							pageDataChanged.getHandler().add(child);
						}
					}
					WicketUtils.markLastVisibleChild(this);
					pageDataChanged.getHandler().appendJavaScript("$(window).resize();");
				}
			}

			@Override
			protected void onBeforeRender() {
				super.onBeforeRender();
				WicketUtils.markLastVisibleChild(this);
			}

		};
		summaryContainer.setOutputMarkupPlaceholderTag(true);
		add(summaryContainer);

		summaryContainer.add(newDiscardedNoteContainer());
		summaryContainer.add(newMergedNoteContainer());
		summaryContainer.add(newMergePreviewContainer());
		summaryContainer.add(new WebMarkupContainer("doNotMerge") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getMergeStrategy() == MergeStrategy.DO_NOT_MERGE);
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		summaryContainer.add(newOperationsContainer());
		
		WicketUtils.markLastVisibleChild(summaryContainer);
		
		List<Tab> tabs = new ArrayList<>();
		
		tabs.add(new RequestTab("Overview", RequestOverviewPage.class));
		tabs.add(new RequestTab("File Changes", RequestChangesPage.class));
		tabs.add(new RequestTab("Code Comments", RequestCodeCommentsPage.class));
		if (request.isOpen())
			tabs.add(new RequestTab("Merge Preview", MergePreviewPage.class));
		
		add(new Tabbable("requestTabs", tabs).setOutputMarkupId(true));
		
		RequestCycle.get().getListeners().add(new IRequestCycleListener() {
			
			@Override
			public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url) {
			}
			
			@Override
			public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onExceptionRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler, Exception exception) {
			}
			
			@Override
			public IRequestHandler onException(RequestCycle cycle, Exception ex) {
				return null;
			}
			
			@Override
			public void onEndRequest(RequestCycle cycle) {
				if (SecurityUtils.getUser() != null) { 
					OneDev.getInstance(VisitManager.class).visitPullRequest(SecurityUtils.getUser(), getPullRequest());
				}
			}
			
			@Override
			public void onDetach(RequestCycle cycle) {
			}
			
			@Override
			public void onBeginRequest(RequestCycle cycle) {
			}
			
		});
	}
	
	private WebMarkupContainer newStatusAndBranchesContainer() {
		WebMarkupContainer statusAndBranchesContainer = new WebMarkupContainer("statusAndBranches");
		
		PullRequest request = getPullRequest();
		
		statusAndBranchesContainer.add(new Label("status", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PullRequest request = getPullRequest();
				if (request.isOpen())
					return "OPEN";
				else
					return request.getCloseInfo().getCloseStatus().toString();
			}
			
		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				setOutputMarkupId(true);
				
				add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						PullRequest request = getPullRequest();
						if (request.isDiscarded())
							return " label-danger";
						else if (request.isMerged())
							return " label-success";
						else
							return " label-warning";
					}
					
				}));
			}

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PageDataChanged) {
					PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
					pageDataChanged.getHandler().add(this);
				}
			}
			
		});
		
		statusAndBranchesContainer.add(new UserLink("user", 
				User.getForDisplay(request.getSubmitter(), request.getSubmitterName())));
		statusAndBranchesContainer.add(new Label("date", DateUtils.formatAge(request.getSubmitDate())));
		
		statusAndBranchesContainer.add(new BranchLink("target", request.getTarget(), null));
		if (request.getSourceProject() != null) {
			statusAndBranchesContainer.add(new BranchLink("source", request.getSource(), getPullRequest()));
		} else {
			statusAndBranchesContainer.add(new Label("source", "unknown") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
			});
		}
		return statusAndBranchesContainer;
	}

	private WebMarkupContainer newMergePreviewContainer() {
		WebMarkupContainer mergePreviewContainer = new WebMarkupContainer("mergePreview") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				setVisible(request.isOpen() && request.getMergeStrategy() != MergeStrategy.DO_NOT_MERGE);
			}
			
		};
		mergePreviewContainer.setOutputMarkupPlaceholderTag(true);
		
		mergePreviewContainer.add(new WebMarkupContainer("calculating") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getMergePreview() == null);
			}
			
		});
		mergePreviewContainer.add(new WebMarkupContainer("conflict") {
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(new DropdownLink("resolveInstructionsTrigger") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getPullRequest().getSource() != null);
					}

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						return new ResolveConflictInstructionPanel(id, new EntityModel<PullRequest>(getPullRequest()));
					}
					
				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				MergePreview preview = getPullRequest().getMergePreview();
				setVisible(preview != null && preview.getMerged() == null);
			}

		});
		mergePreviewContainer.add(new WebMarkupContainer("noConflict") {
			
			@Override
			protected void onInitialize() {
				super.onInitialize();

				add(new Link<Void>("mergePreview") {
					
					@Override
					public void onClick() {
						PullRequest request = getPullRequest();
						PageParameters params = MergePreviewPage.paramsOf(request);
						setResponsePage(MergePreviewPage.class, params);
					}

				});

				add(new VerificationStatusPanel("verificationStatus", 
						new LoadableDetachableModel<Map<String, Verification>>() {

					@Override
					protected Map<String, Verification> load() {
						return OneDev.getInstance(VerificationManager.class)
								.getVerifications(getProject(), getPullRequest().getMergePreview().getMerged());
					}
					
				}));
				
				add(new DropdownLink("checkoutInstructionsTrigger") {

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						return new CheckoutRequestInstructionPanel(id, new EntityModel<PullRequest>(getPullRequest()));
					}
					
				});
				
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				MergePreview preview = getPullRequest().getMergePreview();
				setVisible(preview != null && preview.getMerged() != null);
			}

		});
		
		return mergePreviewContainer;
	}
	
	private WebMarkupContainer newOperationsContainer() {
		final WebMarkupContainer operationsContainer = new WebMarkupContainer("operations") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				boolean hasVisibleChildren = false;
				for (int i=0; i<size(); i++) {
					@SuppressWarnings("deprecation")
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
		operationsContainer.setOutputMarkupPlaceholderTag(true);
		
		String confirmId = "confirm";
		
		operationsContainer.add(new AjaxLink<Void>("approve") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				reviewUpdateId = getPullRequest().getLatestUpdate().getId();
				operationsContainer.replace(newOperationConfirm(confirmId, APPROVE, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("$(window).resize();");
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
				reviewUpdateId = getPullRequest().getLatestUpdate().getId();
				operationsContainer.replace(newOperationConfirm(confirmId, DISAPPROVE, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("$(window).resize();");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(DISAPPROVE.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}
			
		});
		
		operationsContainer.add(new AjaxLink<Void>("discard") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationsContainer.replace(newOperationConfirm(confirmId, DISCARD, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("$(window).resize();");
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
				target.appendJavaScript("$(window).resize();");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(REOPEN.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}

		});
		operationsContainer.add(new AjaxLink<Void>("deleteSourceBranch") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationsContainer.replace(newOperationConfirm(confirmId, DELETE_SOURCE_BRANCH, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("$(window).resize();");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(DELETE_SOURCE_BRANCH.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}

		});
		operationsContainer.add(new AjaxLink<Void>("restoreSourceBranch") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationsContainer.replace(newOperationConfirm(confirmId, RESTORE_SOURCE_BRANCH, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("$(window).resize();");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(RESTORE_SOURCE_BRANCH.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}

		});
		
		operationsContainer.add(new WebMarkupContainer(confirmId).setVisible(false));
		
		return operationsContainer;
	}
	
	private Component newOperationConfirm(String id, PullRequestOperation operation, 
			WebMarkupContainer operationsContainer) {
		PullRequest request = getPullRequest();

		Fragment fragment = new Fragment(id, "operationConfirmFrag", this);
		Form<?> form = new Form<Void>("form");
		fragment.add(form);

		ProjectAndBranch source = request.getSource();
		Preconditions.checkNotNull(source);
		
		String autosaveKey = "autosave:pullRequestOperation:" + getPullRequest().getId();
		FormComponent<String> noteInput;
		form.add(noteInput = new CommentInput("note", Model.of(""), false) {

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(requestModel.getObject().getTargetProject(), 
						requestModel.getObject().getUUID());
			}

			@Override
			protected Project getProject() {
				return requestModel.getObject().getTargetProject();
			}

			@Override
			protected String getAutosaveKey() {
				return autosaveKey;
			}
			
			@Override
			protected List<AttributeModifier> getInputModifiers() {
				return Lists.newArrayList(AttributeModifier.replace("placeholder", "Leave a note"));
			}
			
		});
		
		WebMarkupContainer hint = new WebMarkupContainer(HINT_ID);
		hint.setOutputMarkupPlaceholderTag(true);
		fragment.add(hint);
		
		NotificationPanel feedback = new NotificationPanel("feedback", form);
		feedback.setOutputMarkupPlaceholderTag(true);
		fragment.add(feedback);
		
		form.add(new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				PullRequest request = getPullRequest();
				if ((operation == APPROVE || operation == DISAPPROVE) && 
						!getPullRequest().getLatestUpdate().getId().equals(reviewUpdateId)) {
					Long prevReviewUpdateId = reviewUpdateId;
					WebMarkupContainer hint = new UnreviewedChangesPanel(HINT_ID, 
							new LoadableDetachableModel<PullRequestUpdate>() {

						@Override
						protected PullRequestUpdate load() {
							return OneDev.getInstance(PullRequestUpdateManager.class).load(prevReviewUpdateId);
						}
						
					});
					hint.setOutputMarkupPlaceholderTag(true);
					fragment.replace(hint);
					
					target.add(feedback);
					target.add(hint);
					target.appendJavaScript("$(window).resize();");
					reviewUpdateId = getPullRequest().getLatestUpdate().getId();
				} else if (!operation.canOperate(request)) {
					error("Not allowed to " + getOperationName(operation) + " at this point");
					target.add(feedback);
					target.add(hint);
					target.appendJavaScript("$(window).resize();");
				} else {
					operation.operate(request, noteInput.getModelObject());
					PageParameters params = RequestOverviewPage.paramsOf(getPullRequest());
					params.add(BasePage.PARAM_AUTOSAVE_KEY_TO_CLEAR, autosaveKey);
					setResponsePage(RequestOverviewPage.class, params);
				}
			}

		}.add(AttributeModifier.replace("value", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "Confirm " + getOperationName(operation);
			}
			
		})).add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (operation == DISCARD)
					return "btn-danger";
				else 
					return "btn-primary";
			}
			
		})));
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				fragment.replaceWith(new WebMarkupContainer(id).setVisible(false));
				target.add(operationsContainer);
				target.appendJavaScript("$(window).resize();");
			}
			
		});		
		
		return fragment;
	}
	
	private WebMarkupContainer newDiscardedNoteContainer() {
		WebMarkupContainer discardedNoteContainer = new WebMarkupContainer("discardedNote") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isDiscarded());
			}
			
		};
		discardedNoteContainer.setOutputMarkupPlaceholderTag(true);
		return discardedNoteContainer;
	}
	
	private String getOperationName(PullRequestOperation operation) {
		return WordUtils.capitalizeFully(operation.name()).replace("_", " ").toLowerCase();		
	}

	private WebMarkupContainer newMergedNoteContainer() {
		WebMarkupContainer mergedNoteContainer = new WebMarkupContainer("mergedNote") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isMerged());
			}
			
		};
		mergedNoteContainer.setOutputMarkupPlaceholderTag(true);
		
		mergedNoteContainer.add(new WebMarkupContainer("fastForwarded") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				MergePreview preview = request.getLastMergePreview();
				setVisible(preview != null && preview.getRequestHead().equals(preview.getMerged()));
			}
			
		});
		mergedNoteContainer.add(new WebMarkupContainer("merged") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				MergePreview preview = request.getLastMergePreview();
				setVisible(preview != null 
						&& !preview.getRequestHead().equals(preview.getMerged())
						&& (preview.getMergeStrategy() == ALWAYS_MERGE || preview.getMergeStrategy() == MERGE_IF_NECESSARY));
			}
			
		});
		mergedNoteContainer.add(new WebMarkupContainer("mergedOutside") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(getPullRequest().getLastMergePreview() == null);
			}
			
		});
		mergedNoteContainer.add(new WebMarkupContainer("squashed") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				MergePreview preview = request.getLastMergePreview();
				setVisible(preview != null 
						&& !preview.getRequestHead().equals(preview.getMerged())
						&& preview.getMergeStrategy() == SQUASH_MERGE);
			}
			
		});
		mergedNoteContainer.add(new WebMarkupContainer("rebased") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				MergePreview preview = request.getMergePreview();
				setVisible(preview != null 
						&& !preview.getRequestHead().equals(preview.getMerged())
						&& preview.getMergeStrategy() == REBASE_MERGE);
			}
			
		});
		
		return mergedNoteContainer;
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(PullRequest request) {
		PageParameters params = ProjectPage.paramsOf(request.getTarget().getProject());
		params.set(PARAM_REQUEST, request.getNumber());
		return params;
	}

	public PullRequest getPullRequest() {
		return requestModel.getObject();
	}
	
	@Override
	public Collection<WebSocketRegion> getWebSocketRegions() {
		Collection<WebSocketRegion> regions = super.getWebSocketRegions();
		regions.add(new PullRequestChangedRegion(getPullRequest().getId()));
		return regions;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new RequestDetailResourceReference()));
	}
	
	private class RequestTab extends PageTab {

		public RequestTab(String title, Class<? extends Page> pageClass) {
			super(Model.of(title), pageClass);
		}
		
		@Override
		public Component render(String componentId) {
			if (getMainPageClass() == RequestCodeCommentsPage.class) {
				Fragment fragment = new Fragment(componentId, "codeCommentsTabLinkFrag", RequestDetailPage.this);
				Link<Void> link = new ViewStateAwarePageLink<Void>("link", RequestCodeCommentsPage.class, paramsOf(getPullRequest())) {

					@Override
					public void onEvent(IEvent<?> event) {
						super.onEvent(event);
						if (event.getPayload() instanceof PageDataChanged) {
							((PageDataChanged)event.getPayload()).getHandler().add(this);
						}
					}
					
				};
				link.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						PullRequest request = getPullRequest();
						if (request.getLastCodeCommentEventDate() != null 
								&& !request.isCodeCommentsVisitedAfter(request.getLastCodeCommentEventDate())) {
							return "new";
						} else {
							return "";
						}
					}
					
				}));
				link.setOutputMarkupId(true);
				fragment.add(link);
				return fragment;
			} else {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new ViewStateAwarePageLink<Void>(linkId, pageClass, paramsOf(getPullRequest()));
					}
					
				};
			}
		}
		
	}
	
}