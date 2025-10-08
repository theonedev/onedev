package io.onedev.server.web.page.project.issues.boards;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.InputContext;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.service.UserService;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.model.support.issue.field.FieldUtils;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField;
import io.onedev.server.model.support.issue.transitionspec.ManualSpec;
import io.onedev.server.model.support.issue.transitionspec.TransitionSpec;
import io.onedev.server.search.entity.issue.*;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.create.CreateIssuePanel;
import io.onedev.server.web.component.issue.progress.QueriedIssuesProgressPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.util.ProjectAware;
import io.onedev.server.web.util.WicketUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.unbescape.html.HtmlEscape;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static io.onedev.server.search.entity.issue.IssueQueryLexer.*;
import static io.onedev.server.security.SecurityUtils.canManageIssues;
import static io.onedev.server.web.translation.Translation._T;
import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

abstract class BoardColumnPanel extends AbstractColumnPanel {

	private final IModel<IssueQuery> queryModel = new LoadableDetachableModel<>() {

		@Override
		protected IssueQuery load() {
			IssueQuery boardQuery = getBoardQuery();
			if (boardQuery != null) {
				List<Criteria<Issue>> criterias = new ArrayList<>();
				if (boardQuery.getCriteria() != null)
					criterias.add(boardQuery.getCriteria());
				if (getIterationSelection().getIteration() != null) {
					criterias.add(new IterationCriteria(getIterationSelection().getIteration().getName(), Is));
				} else if (getIterationSelection() instanceof IterationSelection.Unscheduled) {
					if (getIterationPrefix() != null)	
						criterias.add(new IterationCriteria(getIterationPrefix()+ "*", IsNot));
					else
						criterias.add(new IterationEmptyCriteria(IsEmpty));
				}
				String identifyField = getBoard().getIdentifyField();
				if (identifyField.equals(Issue.NAME_STATE)) {
					criterias.add(new StateCriteria(getColumn(), Is));
				} else if (getColumn() != null) {
					criterias.add(new ChoiceFieldCriteria(identifyField,
							getColumn(), -1, Is, false));
				} else {
					criterias.add(new FieldOperatorCriteria(identifyField, IsEmpty, false));
				}
				return new IssueQuery(Criteria.andCriterias(criterias), boardQuery.getSorts());
			} else {
				return null;
			}
		}

	};
	
	private AbstractPostAjaxBehavior ajaxBehavior;
	
	private Component countLabel;
	
	private Component addToIterationLink;
	
	private CardListPanel cardListPanel;
	
	public BoardColumnPanel(String id) {
		super(id);
	}

	@Override
	protected void onDetach() {
		queryModel.detach();
		super.onDetach();
	}
	
	@Override
	protected IssueQuery getQuery() {
		return queryModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer content = new WebMarkupContainer("content") {

			@Override
			protected void onBeforeRender() {
				addOrReplace(cardListPanel = new CardListPanel("body") {

					@Override
					public void onEvent(IEvent<?> event) {
						super.onEvent(event);
						if (event.getPayload() instanceof IssueDragging && getQuery() != null) {
							IssueDragging issueDragging = (IssueDragging) event.getPayload();
							Issue issue = issueDragging.getIssue();
							var iteration = getIterationSelection().getIteration();
							if (iteration == null || issue.getIterations().contains(iteration)) { 
								// move issue between board columns
								String identifyField = getBoard().getIdentifyField();
								if (identifyField.equals(Issue.NAME_STATE)) {
									var subject = SecurityUtils.getSubject();
									for (TransitionSpec transition: getIssueSetting().getTransitionSpecs()) {
										if (transition instanceof ManualSpec && ((ManualSpec)transition).canTransit(subject, issue, getColumn())) {
											issue = SerializationUtils.clone(issue);
											issue.setState(getColumn());
											issue.getLastActivity().setDate(new Date());
											break;
										}
									}
								} else {
									FieldSpec fieldSpec = getIssueSetting().getFieldSpec(identifyField);
									if (fieldSpec != null && SecurityUtils.canEditIssueField(getProject(), fieldSpec.getName())) {
										issue = SerializationUtils.clone(issue);
										issue.setFieldValue(identifyField, getColumn());
										issue.getLastActivity().setDate(new Date());
									}
								}
							} else if (SecurityUtils.canScheduleIssues(issue.getProject())) { 
								// move issue between backlog column and board column
								issue = SerializationUtils.clone(issue);
								issue.setProject(getProjectScope().getProject());
								IssueSchedule schedule = new IssueSchedule();
								schedule.setIssue(issue);
								schedule.setIteration(iteration);
								issue.getSchedules().add(schedule);
								issue.getLastActivity().setDate(new Date());
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
						CharSequence callback = ajaxBehavior.getCallbackFunction(
								explicit("issueId"), explicit("cardIndex"));
						String script = String.format("onedev.server.issueBoards.onColumnDomReady('%s', %s);", 
								getMarkupId(), (getQuery() != null && canManageIssues(getProject()))? callback:"undefined");
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

					@Override
					protected void updateCardCount(IPartialPageRequestHandler handler) {
						handler.add(countLabel);
						if (addToIterationLink.getOutputMarkupId())
							handler.add(addToIterationLink);
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
					user = OneDev.getInstance(UserService.class).findByName(getColumn());
				}
			}
		} else {
			FieldSpec fieldSpec = getIssueSetting().getFieldSpec(identifyField);
			if (fieldSpec != null) 
				title = "<i>" + HtmlEscape.escapeHtml5(fieldSpec.getNameOfEmptyValue()) + "</i>";
			else
				title = "<i>" + _T("No value") + "</i>";
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

		if (getQuery() != null && getProject().isTimeTracking() 
				&& WicketUtils.isSubscriptionActive() 
				&& SecurityUtils.canAccessTimeTracking(getProject())) {
			head.add(new DropdownLink("showProgress") {
				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					return new QueriedIssuesProgressPanel(id) {
						@Override
						protected ProjectScope getProjectScope() {
							return BoardColumnPanel.this.getProjectScope();
						}

						@Override
						protected IssueQuery getQuery() {
							return BoardColumnPanel.this.getQuery();
						}
					};
				}
			});
		} else {
			head.add(new WebMarkupContainer("showProgress").setVisible(false));
		}
		
		if (getQuery() != null) {
			PageParameters params = ProjectIssueListPage.paramsOf(getProject(), getQuery().toString(), 0);
			head.add(new BookmarkablePageLink<Void>("viewAsList", ProjectIssueListPage.class, params));
		} else {
			head.add(new WebMarkupContainer("viewAsList").setVisible(false));
		}
		
		head.add(addToIterationLink = newAddToIterationLink("addToIteration"));
		head.add(new ModalLink("addCard") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new CreateIssuePanel(id) {
					
					@Override
					protected void onSave(AjaxRequestTarget target, Issue issue) {
						onCardAdded(target, issue);
						modal.close();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
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
						&& SecurityUtils.getAuthUser() != null
						&& (!getBoard().getIdentifyField().equals(Issue.NAME_STATE) 
								|| getColumn().equals(getIssueSetting().getInitialStateSpec().getName())));
			}
			
		});
		
		head.add(countLabel = new Label("count", countModel).setOutputMarkupId(true));
		
		add(ajaxBehavior = new AbstractPostAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				if (!canManageIssues(getProject()))
					throw new UnauthorizedException(_T("Permission denied"));
				
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				var issueId = params.getParameterValue("issueId").toLong();
				var cardIndex = params.getParameterValue("cardIndex").toInt();
				var card = cardListPanel.findCard(issueId);
				if (card != null) { // Move in same column
					cardListPanel.onCardDropped(target, issueId, cardIndex, true);
				} else {
					var subject = SecurityUtils.getSubject();
					var user = SecurityUtils.getUser(subject);
					Issue issue = getIssueService().load(issueId);
					String fieldName = getBoard().getIdentifyField();
					var iteration = getIterationSelection().getIteration();
					if (iteration != null && !issue.getIterations().contains(iteration)) {
						getIssueChangeService().addSchedule(user, issue, iteration);
						cardListPanel.onCardDropped(target, issueId, cardIndex, true);
					} else if (fieldName.equals(Issue.NAME_STATE)) {
						AtomicReference<ManualSpec> transitionRef = new AtomicReference<>(null);
						for (TransitionSpec transition : getIssueSetting().getTransitionSpecs()) {
							if (transition instanceof ManualSpec && ((ManualSpec)transition).canTransit(subject, issue, getColumn())) {
								transitionRef.set((ManualSpec) transition);
								break;
							}
						}
						if (transitionRef.get() == null)
							throw new IllegalStateException();

						boolean hasPromptFields = false;
						var transition = transitionRef.get();
						for (String promptField : transition.getPromptFields()) {
							FieldSpec fieldSpec = getIssueSetting().getFieldSpec(promptField);
							if (fieldSpec != null && SecurityUtils.canEditIssueField(getProject(), fieldSpec.getName())) {
								Class<?> fieldBeanClass = FieldUtils.getFieldBeanClass();
								Serializable fieldBean = issue.getFieldBean(fieldBeanClass, true);
								if (FieldUtils.isFieldVisible(new BeanDescriptor(fieldBeanClass), fieldBean, promptField)) {
									hasPromptFields = true;
									break;
								}
							}
						}
						if (hasPromptFields) {
							new ModalPanel(target) {

								@Override
								protected Component newContent(String id) {
									return new StateTransitionPanel(id) {

										@Override
										protected void onSaved(AjaxRequestTarget target) {
											cardListPanel.onCardDropped(target, issueId, cardIndex, true);
											close();
										}

										@Override
										protected void onCancelled(AjaxRequestTarget target) {
											cardListPanel.onCardDropped(target, issueId, cardIndex, false);
											close();
										}

										@Override
										protected Issue getIssue() {
											return getIssueService().load(issueId);
										}

										@Override
										protected ManualSpec getTransition() {
											return transitionRef.get();
										}

										@Override
										protected String getToState() {
											return getColumn();
										}

									};
								}

							};
						} else {
							getIssueChangeService().changeState(user, issue, getColumn(),
									new HashMap<>(), transitionRef.get().getPromptFields(), 
									transitionRef.get().getRemoveFields(), null);
							cardListPanel.onCardDropped(target, issueId, cardIndex, true);
						}
					} else {
						FieldSpec fieldSpec = getIssueSetting().getFieldSpec(fieldName);
						if (fieldSpec == null)
							throw new ExplicitException(_T("Undefined custom field: ") + fieldName);

						Serializable fieldBean = issue.getFieldBean(FieldUtils.getFieldBeanClass(), true);
						BeanDescriptor beanDescriptor = new BeanDescriptor(fieldBean.getClass());
						beanDescriptor.getProperty(fieldName).setPropertyValue(fieldBean, getColumn());

						Collection<String> dependentFields = fieldSpec.getTransitiveDependents();
						boolean hasVisibleEditableDependents = dependentFields.stream()
								.anyMatch(it -> SecurityUtils.canEditIssueField(issue.getProject(), it)
										&& FieldUtils.isFieldVisible(beanDescriptor, fieldBean, it));

						Map<String, Object> fieldValues = new HashMap<>();
						fieldValues.put(fieldName, getColumn());

						if (hasVisibleEditableDependents) {
							Collection<String> propertyNames = FieldUtils.getEditablePropertyNames(
									issue.getProject(), fieldBean.getClass(), dependentFields);
							class DependentFieldsEditor extends BeanEditModalPanel<Serializable>
									implements ProjectAware, InputContext {

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
								protected boolean isDirtyAware() {
									return false;
								}

								@Override
								protected String onSave(AjaxRequestTarget target, Serializable bean) {
									fieldValues.putAll(FieldUtils.getFieldValues(
											FieldUtils.newBeanComponentContext(beanDescriptor, bean),
											bean, FieldUtils.getEditableFields(getProject(), dependentFields)));
									close();
									Issue issue = getIssueService().load(issueId);
									getIssueChangeService().changeFields(SecurityUtils.getUser(), issue, fieldValues);
									cardListPanel.onCardDropped(target, issueId, cardIndex, true);
									return null;
								}

								@Override
								protected void onCancel(AjaxRequestTarget target) {
									cardListPanel.onCardDropped(target, issueId, cardIndex, false);
								}

							}
							new DependentFieldsEditor(target, fieldBean, propertyNames, false, _T("Dependent Fields"));
						} else {
							getIssueChangeService().changeFields(user, issue, fieldValues);
							cardListPanel.onCardDropped(target, issueId, cardIndex, true);
						}
					}
				}
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	protected CardListPanel getCardListPanel() {
		return cardListPanel;
	}

	@Nullable
	protected abstract String getColumn();
	
	protected abstract BoardSpec getBoard();

	@Nullable
	protected abstract IssueQuery getBoardQuery();

	@Override
	protected boolean isBacklog() {
		return false;
	}
	
}
