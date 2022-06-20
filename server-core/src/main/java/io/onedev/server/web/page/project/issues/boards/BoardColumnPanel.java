package io.onedev.server.web.page.project.issues.boards;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.unbescape.html.HtmlEscape;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputContext;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.model.support.issue.TransitionSpec;
import io.onedev.server.model.support.issue.field.FieldUtils;
import io.onedev.server.model.support.issue.field.spec.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.UserChoiceField;
import io.onedev.server.model.support.issue.transitiontrigger.PressButtonTrigger;
import io.onedev.server.search.entity.issue.ChoiceFieldCriteria;
import io.onedev.server.search.entity.issue.FieldOperatorCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.search.entity.issue.MilestoneCriteria;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.util.ProjectAware;

@SuppressWarnings("serial")
abstract class BoardColumnPanel extends Panel implements EditContext {

	private final IModel<IssueQuery> queryModel = new LoadableDetachableModel<IssueQuery>() {

		@Override
		protected IssueQuery load() {
			IssueQuery boardQuery = getBoardQuery();
			if (boardQuery != null) {
				List<Criteria<Issue>> criterias = new ArrayList<>();
				if (boardQuery.getCriteria() != null)
					criterias.add(boardQuery.getCriteria());
				if (getMilestone() != null)
					criterias.add(new MilestoneCriteria(getMilestone().getName()));
				String identifyField = getBoard().getIdentifyField();
				if (identifyField.equals(Issue.NAME_STATE)) {
					criterias.add(new StateCriteria(getColumn(), IssueQueryLexer.Is));
				} else if (getColumn() != null) {
					criterias.add(new ChoiceFieldCriteria(identifyField, 
							getColumn(), -1, IssueQueryLexer.Is, false));
				} else {
					criterias.add(new FieldOperatorCriteria(identifyField, IssueQueryLexer.IsEmpty, false));
				}
				return new IssueQuery(Criteria.andCriterias(criterias), boardQuery.getSorts());
			} else {
				return null;
			}
		}
		
	};
	
	private final IModel<Integer> countModel = new LoadableDetachableModel<Integer>() {

		@Override
		protected Integer load() {
			if (getQuery() != null) {
				try {
					return getIssueManager().count(getProjectScope(), getQuery().getCriteria());
				} catch(ExplicitException e) {
				}
			} 
			return 0;
		}
		
	};
	
	private AbstractPostAjaxBehavior ajaxBehavior;
	
	public BoardColumnPanel(String id) {
		super(id);
	}

	@Override
	protected void onDetach() {
		queryModel.detach();
		countModel.detach();
		super.onDetach();
	}
	
	private IssueQuery getQuery() {
		return queryModel.getObject();
	}

	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer content = new WebMarkupContainer("content") {

			@Override
			protected void onBeforeRender() {
				addOrReplace(new CardListPanel("body") {

					@Override
					public void onEvent(IEvent<?> event) {
						super.onEvent(event);
						if (event.getPayload() instanceof IssueDragging && getQuery() != null) {
							IssueDragging issueDragging = (IssueDragging) event.getPayload();
							Issue issue = issueDragging.getIssue();
							if (getMilestone() == null || issue.getMilestones().contains(getMilestone())) { 
								// move issue between board columns
								String identifyField = getBoard().getIdentifyField();
								if (identifyField.equals(Issue.NAME_STATE)) {
									issue = SerializationUtils.clone(issue);
									for (TransitionSpec transition: getIssueSetting().getTransitionSpecs()) {
										if (transition.canTransitManually(issue, getColumn())) {
											issue.setState(getColumn());
											break;
										}
									}
								} else {
									FieldSpec fieldSpec = getIssueSetting().getFieldSpec(identifyField);
									if (fieldSpec != null && SecurityUtils.canEditIssueField(getProject(), fieldSpec.getName())) {
										issue = SerializationUtils.clone(issue);
										issue.setFieldValue(identifyField, getColumn());
									}
								}
							} else if (SecurityUtils.canScheduleIssues(issue.getProject())) { 
								// move issue between backlog column and board column
								issue = SerializationUtils.clone(issue);
								IssueSchedule schedule = new IssueSchedule();
								schedule.setIssue(issue);
								schedule.setMilestone(getMilestone());
								issue.getSchedules().add(schedule);
							}
							if (getQuery().matches(issue)) {
								String script = String.format("$('#%s').addClass('issue-droppable');", getMarkupId());
								issueDragging.getHandler().appendJavaScript(script);
							}
						}
						event.dontBroadcastDeeper();
					}
					
					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);
						CharSequence callback = ajaxBehavior.getCallbackFunction(CallbackParameter.explicit("issue"));
						String script = String.format("onedev.server.issueBoards.onColumnDomReady('%s', %s);", 
								getMarkupId(), getQuery()!=null?callback:"undefined");
						// Use OnLoad instead of OnDomReady as otherwise perfect scrollbar is not shown unless resized 
						response.render(OnDomReadyHeaderItem.forScript(script));
					}

					@Override
					protected ProjectScope getProjectScope() {
						return BoardColumnPanel.this.getProjectScope();
					}

					@Override
					protected IssueQuery getQuery() {
						return BoardColumnPanel.this.getQuery();
					}

					@Override
					protected int getCardCount() {
						return countModel.getObject();
					}

				});
				
				super.onBeforeRender();
			}
			
		};
		add(content);
		
		String title;
		String color = null;
		User user = null;
		String identifyField = getBoard().getIdentifyField();
		if (getColumn() != null) {
			title = HtmlEscape.escapeHtml5(getColumn());
			if (identifyField.equals(Issue.NAME_STATE)) {
				StateSpec stateSpec = getIssueSetting().getStateSpec(getColumn());
				if (stateSpec != null)
					color = stateSpec.getColor();
			} else {
				FieldSpec fieldSpec = getIssueSetting().getFieldSpec(identifyField);
				if (fieldSpec instanceof ChoiceField) {
					ChoiceProvider choiceProvider = ((ChoiceField)fieldSpec).getChoiceProvider();
					ComponentContext.push(new ComponentContext(this));
					try {
						color = choiceProvider.getChoices(true).get(getColumn());
					} finally {
						ComponentContext.pop();
					}
				} else if (fieldSpec instanceof UserChoiceField) {
					user = OneDev.getInstance(UserManager.class).findByName(getColumn());
				}
			}
		} else {
			FieldSpec fieldSpec = getIssueSetting().getFieldSpec(identifyField);
			if (fieldSpec != null) 
				title = "<i>" + HtmlEscape.escapeHtml5(fieldSpec.getNameOfEmptyValue()) + "</i>";
			else
				title = "<i>No value</i>";
		}

		WebMarkupContainer head = new WebMarkupContainer("head");
		if (user != null) {
			head.add(new WebMarkupContainer("title").setVisible(false));
			head.add(new UserIdentPanel("userIdent", user, Mode.AVATAR_AND_NAME));
		} else {
			head.add(new Label("title", title).setEscapeModelStrings(false));
			head.add(new WebMarkupContainer("userIdent").setVisible(false));
		}
		
		head.add(AttributeAppender.append("title", getBoard().getIdentifyField()));
		content.add(head);
		if (color != null) {
			head.add(AttributeAppender.append("style", "border-top-color:" + color + ";"));
			content.add(AttributeAppender.append("style", "border-color:" + color + ";"));
		}
		
		if (getQuery() != null) {
			PageParameters params = ProjectIssueListPage.paramsOf(getProject(), getQuery().toString(), 0);
			head.add(new BookmarkablePageLink<Void>("viewAsList", ProjectIssueListPage.class, params));
		} else {
			head.add(new WebMarkupContainer("viewAsList").setVisible(false));
		}
		
		head.add(new ModalLink("addCard") {

			@Override
			protected String getModalCssClass() {
				return "modal-lg";
			}
			
			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new NewCardPanel(id) {

					@Override
					protected void onClose(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected Project getProject() {
						return BoardColumnPanel.this.getProject();
					}

					@Override
					protected Criteria<Issue> getTemplate() {
						return getQuery().getCriteria();
					}

				};
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getQuery() != null 
						&& SecurityUtils.getUser() != null
						&& (!getBoard().getIdentifyField().equals(Issue.NAME_STATE) 
								|| getColumn().equals(getIssueSetting().getInitialStateSpec().getName())));
			}
			
		});
		
		head.add(new CardCountLabel("count") {

			@Override
			protected Project getProject() {
				return BoardColumnPanel.this.getProject();
			}

			@Override
			protected int getCount() {
				return countModel.getObject();
			}

		});
		
		add(ajaxBehavior = new AbstractPostAjaxBehavior() {
			
			private void markAccepted(AjaxRequestTarget target, Issue issue, boolean accepted) {
				target.appendJavaScript(String.format("onedev.server.issueBoards.markAccepted(%d, %b);", 
						issue.getId(), accepted));
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				Long issueId = params.getParameterValue("issue").toLong();
				Issue issue = getIssueManager().load(issueId);
				String fieldName = getBoard().getIdentifyField();
				if (getMilestone() != null && !issue.getMilestones().contains(getMilestone())) { 
					// move a backlog issue to board 
					if (!SecurityUtils.canScheduleIssues(issue.getProject())) 
						throw new UnauthorizedException("Permission denied");

					OneDev.getInstance(IssueChangeManager.class).addSchedule(issue, getMilestone());
					markAccepted(target, issue, true);
				} else if (fieldName.equals(Issue.NAME_STATE)) {
					AtomicReference<TransitionSpec> transitionRef = new AtomicReference<>(null);
					for (TransitionSpec transition: getIssueSetting().getTransitionSpecs()) {
						if (transition.canTransitManually(issue, getColumn())) {
							transitionRef.set(transition);
							break;
						}
					}
					if (transitionRef.get() == null) 
						throw new UnauthorizedException("Permission denied");
					
					boolean hasPromptFields = false;
					PressButtonTrigger trigger = (PressButtonTrigger) transitionRef.get().getTrigger();
					for (String promptField: trigger.getPromptFields()) {
						FieldSpec fieldSpec = getIssueSetting().getFieldSpec(promptField);
						if (fieldSpec != null && SecurityUtils.canEditIssueField(getProject(), fieldSpec.getName())) {
							hasPromptFields = true;
							break;
						}
					}
					if (hasPromptFields) {
						new ModalPanel(target) {

							@Override
							protected Component newContent(String id) {
								return new StateTransitionPanel(id) {
									
									@Override
									protected void onSaved(AjaxRequestTarget target) {
										markAccepted(target, getIssue(), true);
										close();
									}
									
									@Override
									protected void onCancelled(AjaxRequestTarget target) {
										markAccepted(target, getIssue(), false);
										close();
									}
									
									@Override
									protected Issue getIssue() {
										return getIssueManager().load(issueId);
									}

									@Override
									protected TransitionSpec getTransition() {
										return transitionRef.get();
									}
									
								};
							}
							
						};
					} else {
						OneDev.getInstance(IssueChangeManager.class).changeState(issue, getColumn(), 
								new HashMap<>(), transitionRef.get().getRemoveFields(), null);
						markAccepted(target, issue, true);
					}
				} else {
					FieldSpec fieldSpec = getIssueSetting().getFieldSpec(fieldName);
					if (fieldSpec == null)
						throw new ExplicitException("Undefined custom field: " + fieldName);
					
					if (!SecurityUtils.canEditIssueField(getProject(), fieldSpec.getName()))
						throw new UnauthorizedException("Permission denied");
					
					Serializable fieldBean = issue.getFieldBean(FieldUtils.getFieldBeanClass(), true);
					BeanDescriptor beanDescriptor = new BeanDescriptor(fieldBean.getClass());
					beanDescriptor.getProperty(fieldName).setPropertyValue(fieldBean, getColumn());
					
					Collection<String> dependentFields = fieldSpec.getTransitiveDependents();
					boolean hasVisibleEditableDependents = dependentFields.stream()
							.anyMatch(it->SecurityUtils.canEditIssueField(issue.getProject(), it) 
									&& FieldUtils.isFieldVisible(beanDescriptor, fieldBean, it));
					
					Map<String, Object> fieldValues = new HashMap<>();
					fieldValues.put(fieldName, getColumn());
					
					if (hasVisibleEditableDependents) {
						Collection<String> propertyNames = FieldUtils.getEditablePropertyNames(
								issue.getProject(), fieldBean.getClass(), dependentFields);
						class DependentFieldsEditor extends BeanEditModalPanel implements ProjectAware, InputContext {

							public DependentFieldsEditor(IPartialPageRequestHandler handler, Serializable bean,
									Collection<String> propertyNames, boolean exclude, String title) {
								super(handler, bean, propertyNames, exclude, title);
							}

							@Override
							public Project getProject() {
								return BoardColumnPanel.this.getProject();
							}

							@Override
							public List<String> getInputNames() {
								throw new UnsupportedOperationException();
							}

							@Override
							public InputSpec getInputSpec(String inputName) {
								return getIssueSetting().getFieldSpec(inputName);
							}

							@Override
							protected void onSave(AjaxRequestTarget target, Serializable bean) {
								fieldValues.putAll(FieldUtils.getFieldValues(
										FieldUtils.newBeanComponentContext(beanDescriptor, bean), 
										bean, FieldUtils.getEditableFields(getProject(), dependentFields)));
								close();
								Issue issue = getIssueManager().load(issueId);
								OneDev.getInstance(IssueChangeManager.class).changeFields(issue, fieldValues);
								markAccepted(target, issue, true);
							}

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								markAccepted(target, getIssueManager().load(issueId), false);
							}
							
						}
						new DependentFieldsEditor(target, fieldBean, propertyNames, false, "Dependent Fields");
					} else {
						OneDev.getInstance(IssueChangeManager.class).changeFields(issue, fieldValues);
						markAccepted(target, issue, true);
					}
				}
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	@Override
	public Object getInputValue(String name) {
		return null;
	}

	private Project getProject() {
		return getProjectScope().getProject();
	}
	
	protected abstract ProjectScope getProjectScope();

	protected abstract BoardSpec getBoard();

	@Nullable
	protected abstract Milestone getMilestone();
	
	@Nullable
	protected abstract String getColumn();
	
	@Nullable
	protected abstract IssueQuery getBoardQuery();

}
