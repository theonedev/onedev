package io.onedev.server.web.page.project.issues.issueboards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.IssueChangeManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.IssueBoard;
import io.onedev.server.model.support.issue.query.ChoiceFieldCriteria;
import io.onedev.server.model.support.issue.query.FieldUnaryCriteria;
import io.onedev.server.model.support.issue.query.IssueCriteria;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.model.support.issue.query.IssueQueryLexer;
import io.onedev.server.model.support.issue.query.MilestoneCriteria;
import io.onedev.server.model.support.issue.query.StateCriteria;
import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.model.support.issue.workflow.TransitionSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.util.ComponentContext;

@SuppressWarnings("serial")
abstract class BoardColumnPanel extends Panel implements EditContext {

	private final IModel<IssueQuery> queryModel = new LoadableDetachableModel<IssueQuery>() {

		@Override
		protected IssueQuery load() {
			IssueQuery boardQuery = getBoardQuery();
			if (boardQuery != null) {
				List<IssueCriteria> criterias = new ArrayList<>();
				if (boardQuery.getCriteria() != null)
					criterias.add(boardQuery.getCriteria());
				if (getMilestone() != null)
					criterias.add(new MilestoneCriteria(getMilestone().getName(), IssueQueryLexer.Is));
				String identifyField = getBoard().getIdentifyField();
				if (identifyField.equals(Issue.STATE)) {
					criterias.add(new StateCriteria(getColumn(), IssueQueryLexer.Is));
				} else {
					if (getColumn() != null) {
						criterias.add(new ChoiceFieldCriteria(identifyField, 
								getColumn(), -1, IssueQueryLexer.Is, false));
					} else {
						criterias.add(new FieldUnaryCriteria(identifyField, IssueQueryLexer.IsEmpty));
					}
				}
				return new IssueQuery(IssueCriteria.of(criterias), boardQuery.getSorts());
			} else {
				return null;
			}
		}
		
	};
	
	private AbstractPostAjaxBehavior ajaxBehavior;
	
	public BoardColumnPanel(String id) {
		super(id);
	}

	@Override
	protected void onDetach() {
		queryModel.detach();
		super.onDetach();
	}
	
	private IssueQuery getQuery() {
		return queryModel.getObject();
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
						if (getQuery() != null && event.getPayload() instanceof IssueDragging) {
							IssueDragging issueDragging = (IssueDragging) event.getPayload();
							Issue issue = issueDragging.getIssue();
							if (Objects.equals(issue.getMilestone(), getMilestone())) { 
								// move issue between board columns
								IssueWorkflow workflow = getProject().getIssueWorkflow();
								String identifyField = getBoard().getIdentifyField();
								if (identifyField.equals(Issue.STATE)) {
									issue = SerializationUtils.clone(issue);
									for (TransitionSpec transition: workflow.getTransitionSpecs()) {
										if (transition.canApplyTo(issue) && transition.getToState().equals(getColumn())) {
											issue.setState(getColumn());
											break;
										}
									}
								} else if (SecurityUtils.canModify(issue)) {
									issue = SerializationUtils.clone(issue);
									issue.setFieldValue(identifyField, getColumn());
								}
							} else if (SecurityUtils.canModify(issue)) { 
								// move issue between backlog column and board column
								issue = SerializationUtils.clone(issue);
								issue.setMilestone(getMilestone());
							}
							if (getQuery().matches(issue)) {
								String script = String.format("$('#%s').addClass('issue-droppable');", getMarkupId());
								issueDragging.getHandler().appendJavaScript(script);
							}
						}
						event.dontBroadcastDeeper();
					}
					
					@Override
					protected List<Issue> queryIssues(int offset, int count) {
						if (getQuery() != null) 
							return OneDev.getInstance(IssueManager.class).query(getProject(), getQuery(), offset, count);
						else 
							return new ArrayList<>();
					}
					
					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);
						CharSequence callback = ajaxBehavior.getCallbackFunction(CallbackParameter.explicit("issue"));
						String script = String.format("onedev.server.issueBoards.onColumnDomReady('%s', %s);", 
								getMarkupId(), getQuery()!=null?callback:"undefined");
						response.render(OnDomReadyHeaderItem.forScript(script));
					}

				});
				
				super.onBeforeRender();
			}
			
		};
		add(content);
		
		String title;
		String color = null;
		User user = null;
		IssueWorkflow workflow = getProject().getIssueWorkflow();
		String identifyField = getBoard().getIdentifyField();
		if (getColumn() != null) {
			title = getColumn();
			if (identifyField.equals(Issue.STATE)) {
				color = workflow.getStateSpec(getColumn()).getColor();
			} else {
				InputSpec field = workflow.getFieldSpec(identifyField);
				if (field instanceof ChoiceInput) {
					ChoiceProvider choiceProvider = ((ChoiceInput)field).getChoiceProvider();
					OneContext.push(new ComponentContext(this));
					try {
						color = choiceProvider.getChoices(true).get(getColumn());
					} finally {
						OneContext.pop();
					}
				} else if (field instanceof UserChoiceInput) {
					user = OneDev.getInstance(UserManager.class).findByName(getColumn());
				}
			}
		} else {
			title = workflow.getFieldSpec(identifyField).getNameOfEmptyValue();
		}

		WebMarkupContainer head = new WebMarkupContainer("head");
		if (user != null) {
			head.add(new WebMarkupContainer("title").setVisible(false));
			head.add(new AvatarLink("avatarLink", user));
			head.add(new UserLink("userLink", user));
		} else {
			head.add(new Label("title", title));
			head.add(new WebMarkupContainer("avatarLink").setVisible(false));
			head.add(new WebMarkupContainer("userLink").setVisible(false));
		}
		
		head.add(AttributeAppender.append("title", getBoard().getIdentifyField()));
		content.add(head);
		if (color != null) {
			head.add(AttributeAppender.append("style", "border-top-color:" + color + ";"));
			content.add(AttributeAppender.append("style", "border-color:" + color + ";"));
		}
		
		head.add(new Label("count", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return String.valueOf(OneDev.getInstance(IssueManager.class).count(getProject(), getQuery().getCriteria()));
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getQuery() != null);
			}
			
		});
		
		head.add(new ModalLink("addCard") {

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
					protected IssueCriteria getTemplate() {
						return getQuery().getCriteria();
					}

					@Override
					protected void onAdded(AjaxRequestTarget target, Issue issue) {
						target.add(BoardColumnPanel.this);
					}
					
				};
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getQuery() != null 
						&& SecurityUtils.getUser() != null
						&& (!getBoard().getIdentifyField().equals(Issue.STATE) 
								|| getColumn().equals(getProject().getIssueWorkflow().getInitialStateSpec().getName())));
			}
			
		});
		
		add(ajaxBehavior = new AbstractPostAjaxBehavior() {
			
			private void markAccepted(AjaxRequestTarget target, Issue issue, boolean accepted) {
				target.appendJavaScript(String.format("onedev.server.issueBoards.markAccepted(%d, %b);", 
						issue.getId(), accepted));
			}
			
			private void checkMatched(AjaxRequestTarget target, Issue issue) {
				if (getQuery().matches(issue)) {
					target.add(BoardColumnPanel.this);
				} else {
					new ModalPanel(target) {

						@Override
						protected Component newContent(String id) {
							return new CardUnmatchedPanel(id) {
								
								@Override
								protected void onClose(AjaxRequestTarget target) {
									close();
								}
								
								@Override
								protected Issue getIssue() {
									return issue;
								}
								
							};
						}
						
					};
				}
			}
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				Long issueId = params.getParameterValue("issue").toLong();
				Issue issue = OneDev.getInstance(IssueManager.class).load(issueId);
				if (issue.getMilestone() == null && getMilestone() != null) { 
					// move a backlog issue to board 
					if (!SecurityUtils.canModify(issue)) 
						throw new UnauthorizedException("Permission denied");
					
					OneDev.getInstance(IssueChangeManager.class).changeMilestone(issue, getMilestone());
					markAccepted(target, issue, true);
					checkMatched(target, issue);
				} else if (getBoard().getIdentifyField().equals(Issue.STATE)) {
					IssueWorkflow workflow = getProject().getIssueWorkflow();
					boolean canTransiteIssue = false;
					for (TransitionSpec transition: workflow.getTransitionSpecs()) {
						if (transition.canApplyTo(issue)) {
							canTransiteIssue = true;
							break;
						}
					}
					if (!canTransiteIssue) 
						throw new UnauthorizedException("Permission denied");
					
					StateSpec stateSpec = workflow.getStateSpec(getColumn());
					if (stateSpec == null)
						throw new OneException("Unable to find state spec: " + getColumn());
					if (!issue.getEffectiveFields().keySet().containsAll(stateSpec.getFields())) {
						new ModalPanel(target) {

							@Override
							protected Component newContent(String id) {
								return new StateTransitionPanel(id) {
									
									@Override
									protected void onSaved(AjaxRequestTarget target) {
										markAccepted(target, getIssue(), true);
										checkMatched(target, getIssue());
										close();
									}
									
									@Override
									protected void onCancelled(AjaxRequestTarget target) {
										markAccepted(target, getIssue(), false);
										close();
									}
									
									@Override
									protected Issue getIssue() {
										return OneDev.getInstance(IssueManager.class).load(issueId);
									}

									@Override
									protected String getTargetState() {
										return getColumn();
									}
									
								};
							}
							
						};
					} else {
						OneDev.getInstance(IssueChangeManager.class).changeState(issue, getColumn(), new HashMap<>(), null);
						markAccepted(target, issue, true);
						checkMatched(target, issue);
					}
				} else {
					if (!SecurityUtils.canModify(issue)) 
						throw new UnauthorizedException("Permission denied");
					Map<String, Object> fieldValues = new HashMap<>();
					fieldValues.put(getBoard().getIdentifyField(), getColumn());
					OneDev.getInstance(IssueChangeManager.class).changeFields(issue, fieldValues);
					markAccepted(target, issue, true);
					checkMatched(target, issue);
				}
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	@Override
	public Object getInputValue(String name) {
		return null;
	}

	protected abstract Project getProject();

	protected abstract IssueBoard getBoard();

	@Nullable
	protected abstract Milestone getMilestone();
	
	protected abstract String getColumn();
	
	@Nullable
	protected abstract IssueQuery getBoardQuery();

}
