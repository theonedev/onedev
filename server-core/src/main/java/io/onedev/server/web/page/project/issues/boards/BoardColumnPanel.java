package io.onedev.server.web.page.project.issues.boards;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.InputContext;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.*;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.model.support.issue.TransitionSpec;
import io.onedev.server.model.support.issue.field.FieldUtils;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField;
import io.onedev.server.model.support.issue.transitiontrigger.PressButtonTrigger;
import io.onedev.server.search.entity.issue.*;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
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
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.util.ProjectAware;
import io.onedev.server.web.util.WicketUtils;
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

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("serial")
abstract class BoardColumnPanel extends Panel implements EditContext {

	private final IModel<IssueQuery> queryModel = new LoadableDetachableModel<>() {

		@Override
		protected IssueQuery load() {
			IssueQuery boardQuery = getBoardQuery();
			if (boardQuery != null) {
				List<Criteria<Issue>> criterias = new ArrayList<>();
				if (boardQuery.getCriteria() != null)
					criterias.add(boardQuery.getCriteria());
				if (getMilestone() != null)
					criterias.add(new MilestoneCriteria(getMilestone().getName()));
				else
					criterias.add(new MilestoneIsEmptyCriteria());
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
	
	private Component countLabel;
	
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
									for (TransitionSpec transition: getIssueSetting().getTransitionSpecs()) {
										if (transition.canTransitManually(issue, getColumn())) {
											issue = SerializationUtils.clone(issue);
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
								issue.setProject(getProjectScope().getProject());
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

					@Override
					protected void onUpdate(IPartialPageRequestHandler handler) {
						handler.add(countLabel);
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

		if (getQuery() != null && getProject().isTimeTracking() && WicketUtils.isSubscriptionActive()) {
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
		
		head.add(new ModalLink("addCard") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new CreateIssuePanel(id) {
					
					@Override
					protected void onSave(AjaxRequestTarget target, Issue issue) {
						getIssueManager().open(issue);
						notifyIssueChange(target, issue);
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
						&& SecurityUtils.getUser() != null
						&& (!getBoard().getIdentifyField().equals(Issue.NAME_STATE) 
								|| getColumn().equals(getIssueSetting().getInitialStateSpec().getName())));
			}
			
		});
		
		head.add(countLabel = new Label("count", countModel).setOutputMarkupId(true));
		
		add(ajaxBehavior = new AbstractPostAjaxBehavior() {
			
			private void markAccepted(AjaxRequestTarget target, boolean accepted) {
				target.appendJavaScript(String.format("$('.issue-boards').data('accepted', %b);", accepted));
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
					getIssueChangeManager().addSchedule(issue, getMilestone());
					notifyIssueChange(target, issue);
					markAccepted(target, true);
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
										markAccepted(target, true);
										close();
									}
									
									@Override
									protected void onCancelled(AjaxRequestTarget target) {
										markAccepted(target, false);
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
						getIssueChangeManager().changeState(issue, getColumn(), 
								new HashMap<>(), transitionRef.get().getRemoveFields(), null);
						notifyIssueChange(target, issue);
						markAccepted(target, true);
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
							protected void onSave(AjaxRequestTarget target, Serializable bean) {
								fieldValues.putAll(FieldUtils.getFieldValues(
										FieldUtils.newBeanComponentContext(beanDescriptor, bean), 
										bean, FieldUtils.getEditableFields(getProject(), dependentFields)));
								close();
								Issue issue = getIssueManager().load(issueId);
								getIssueChangeManager().changeFields(issue, fieldValues);
								notifyIssueChange(target, issue);
								markAccepted(target, true);
							}

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								markAccepted(target, false);
							}
							
						}
						new DependentFieldsEditor(target, fieldBean, propertyNames, false, "Dependent Fields");
					} else {
						getIssueChangeManager().changeFields(issue, fieldValues);
						notifyIssueChange(target, issue);
						markAccepted(target, true);
					}
				}
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	private IssueChangeManager getIssueChangeManager() {
		return OneDev.getInstance(IssueChangeManager.class);
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

	private void notifyIssueChange(AjaxRequestTarget target, Issue issue) {
		((BasePage)getPage()).notifyObservablesChange(target, issue.getChangeObservables(true));
	}
}
