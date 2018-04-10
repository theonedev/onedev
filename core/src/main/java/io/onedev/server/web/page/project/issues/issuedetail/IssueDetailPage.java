package io.onedev.server.web.page.project.issues.issuedetail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueFieldManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issueworkflow.IssueWorkflow;
import io.onedev.server.model.support.issueworkflow.StateTransition;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.MultiValueIssueField;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.EditableUtils;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.choiceprovider.ChoiceProvider;
import io.onedev.server.util.inputspec.multichoiceinput.MultiChoiceInput;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.issuestate.IssueStatePanel;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.issueedit.IssueEditPage;
import io.onedev.server.web.page.project.issues.issuelist.IssueListPage;
import io.onedev.server.web.page.project.issues.newissue.NewIssuePage;
import io.onedev.server.web.util.ComponentContext;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.server.web.util.DateUtils;

@SuppressWarnings("serial")
public class IssueDetailPage extends ProjectPage implements InputContext {

	public static final String PARAM_ISSUE = "issue";
	
	private static final String ACTION_OPTIONS_ID = "actionOptions";
	
	private final IModel<Issue> issueModel;
	
	public IssueDetailPage(PageParameters params) {
		super(params);
		
		issueModel = new LoadableDetachableModel<Issue>() {

			@Override
			protected Issue load() {
				return getIssueManager().load(params.get(PARAM_ISSUE).toLong());
			}

		};
	}
	
	private Issue getIssue() {
		return issueModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("title", getIssue().getTitle()));
		add(new IssueStatePanel("state", issueModel) {

			@Override
			protected void onFixed(AjaxRequestTarget target) {
				setResponsePage(IssueDetailPage.class, IssueDetailPage.paramsOf(getIssue()));
			}
			
		});
		add(new UserLink("reporter", User.getForDisplay(getIssue().getReporter(), getIssue().getReporterName())));
		add(new Label("reportDate", DateUtils.formatAge(getIssue().getReportDate())));
		
		RepeatingView transitionsView = new RepeatingView("transitions");

		List<StateTransition> transitions = getProject().getIssueWorkflow().getStateTransitions();
		Collections.sort(transitions, new Comparator<StateTransition>() {

			@Override
			public int compare(StateTransition o1, StateTransition o2) {
				IssueWorkflow workflow = getProject().getIssueWorkflow();
				return workflow.getStateIndex(o1.getToState()) - workflow.getStateIndex(o2.getToState());
			}
			
		});
		for (StateTransition transition: transitions) {
			if (transition.getFromStates().contains(getIssue().getState()) 
					&& transition.getOnAction().getButton() != null 
					&& getLoginUser() != null
					&& transition.getOnAction().getButton().getAuthorized().matches(getProject(), getLoginUser())) {
				boolean applicable = false;
				if (transition.getPrerequisite() == null) {
					applicable = true;
				} else {
					MultiValueIssueField field = getIssue().getMultiValueFields().get(transition.getPrerequisite().getFieldName());
					if (field != null && transition.getPrerequisite().getFieldValues().containsAll(field.getValues()))
						applicable = true;
				}
				if (applicable) {
					AjaxLink<Void> link = new AjaxLink<Void>(transitionsView.newChildId()) {

						private String comment;
						
						@Override
						public void onClick(AjaxRequestTarget target) {
							Fragment fragment = new Fragment(ACTION_OPTIONS_ID, "transitionFrag", IssueDetailPage.this);
							Serializable fieldBean = getIssueFieldManager().loadFields(getIssue());
							Set<String> excludedFields = getIssueFieldManager().getExcludedFields(getIssue().getProject(), transition.getToState());

							Form<?> form = new Form<Void>("form") {

								@Override
								protected void onError() {
									super.onError();
									RequestCycle.get().find(AjaxRequestTarget.class).add(this);
								}
								
							};
							
							form.add(BeanContext.editBean("fields", fieldBean, excludedFields));
							
							form.add(new CommentInput("comment", new PropertyModel<String>(this, "comment"), false) {

								@Override
								protected Project getProject() {
									return getIssue().getProject();
								}
								
								@Override
								protected List<AttributeModifier> getInputModifiers() {
									return Lists.newArrayList(AttributeModifier.replace("placeholder", "Leave a comment"));
								}
								
							});
							
							form.add(new AjaxButton("save") {

								@Override
								protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
									super.onSubmit(target, form);
									getIssue().setState(transition.getToState());
									getIssueManager().save(getIssue(), fieldBean);
									setResponsePage(IssueDetailPage.class, IssueDetailPage.paramsOf(getIssue()));
								}
								
							});
							
							form.add(new AjaxLink<Void>("cancel") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									newEmptyActionOptions(target);
								}
								
							});
							fragment.add(form);
							
							fragment.setOutputMarkupId(true);
							IssueDetailPage.this.replace(fragment);
							target.add(fragment);
						}
						
					};
					link.add(new Label("label", transition.getOnAction().getButton().getName()));
					transitionsView.add(link);
				}
			}
		}
		
		add(transitionsView);
		
		add(new Link<Void>("edit") {

			@Override
			public void onClick() {
				setResponsePage(IssueEditPage.class, IssueEditPage.paramsOf(getIssue()));
			}
			
		}.setVisible(SecurityUtils.canModify(getIssue())));
		
		Link<Void> deleteLink = new Link<Void>("delete") {

			@Override
			public void onClick() {
				getIssueManager().delete(getIssue());
				setResponsePage(IssueListPage.class, IssueListPage.paramsOf(getProject()));
			}
			
		};
		deleteLink.add(new ConfirmOnClick("Do you really want to delete this issue?"));
		deleteLink.setVisible(SecurityUtils.canModify(getIssue()));
		add(deleteLink);
		
		add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, NewIssuePage.paramsOf(getProject())));
		
		newEmptyActionOptions(null);
		
		add(new MarkdownViewer("description", Model.of(getIssue().getDescription()), null));

		add(new ListView<MultiValueIssueField>("fields", new LoadableDetachableModel<List<MultiValueIssueField>>() {

			@Override
			protected List<MultiValueIssueField> load() {
				Map<String, MultiValueIssueField> multiValueFields = getIssue().getMultiValueFields();
				List<IssueField> undefinedFields = new ArrayList<>(getIssue().getFields());
				for (Iterator<IssueField> it = undefinedFields.iterator(); it.hasNext();) {
					if (multiValueFields.containsKey(it.next().getName()))
						it.remove();
				}
				Collections.sort(undefinedFields, new Comparator<IssueField>() {

					@Override
					public int compare(IssueField o1, IssueField o2) {
						return o1.getName().compareTo(o2.getName());
					}
					
				});
				for (IssueField field: undefinedFields) {
					MultiValueIssueField multiValueField = multiValueFields.get(field.getName());
					if (multiValueField == null) {
						multiValueField = new MultiValueIssueField(field.getName(), field.getType(), new ArrayList<>());
						multiValueFields.put(field.getName(), multiValueField);
					}
					multiValueField.getValues().add(field.getValue());
				}
				return new ArrayList<>(multiValueFields.values());
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getIssue().getFields().isEmpty());
			}

			@Override
			protected void populateItem(ListItem<MultiValueIssueField> item) {
				MultiValueIssueField field = item.getModelObject();
				item.add(new Label("name", field.getName()));
				item.add(new ModalLink("fixInvalidField") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new FieldFixPanel(id) {

							@Override
							Issue getIssue() {
								return IssueDetailPage.this.getIssue();
							}

							@Override
							MultiValueIssueField getInvalidField() {
								return item.getModelObject();
							}

							@Override
							void onFixed(AjaxRequestTarget target) {
								setResponsePage(IssueDetailPage.class, IssueDetailPage.paramsOf(getIssue()));
							}

							@Override
							void onCancel(AjaxRequestTarget target) {
								modal.close();
							}
							
						};
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						MultiValueIssueField field = item.getModelObject();
						InputSpec fieldSpec = getProject().getIssueWorkflow().getField(field.getName());
						if (fieldSpec == null)
							tag.put("title", "Field is undefined, click for details");
						else if (!EditableUtils.getDisplayName(fieldSpec.getClass()).equals(field.getType()))
							tag.put("title", "Incorrect field type, click for details");
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						MultiValueIssueField field = item.getModelObject();
						InputSpec fieldSpec = getProject().getIssueWorkflow().getField(field.getName());
						setVisible(fieldSpec == null || !EditableUtils.getDisplayName(fieldSpec.getClass()).equals(field.getType()));
					}
					
				});
				RepeatingView valuesView = new RepeatingView("values");
				for (String value: field.getValues()) {
					FieldValueContainer valueContainer = new FieldValueContainer(valuesView.newChildId()) {

						@Override
						Issue getIssue() {
							return IssueDetailPage.this.getIssue();
						}

						@Override
						MultiValueIssueField getField() {
							return item.getModelObject();
						}

					};
					if (field.getType().equals(InputSpec.USER_CHOICE) 
							|| field.getType().equals(InputSpec.USER_MULTI_CHOICE)) {
						User user = User.getForDisplay(getUserManager().findByName(value), value);
						Fragment fragment = new Fragment("value", "userFrag", IssueDetailPage.this);
						fragment.add(new AvatarLink("avatar", user, null));
						fragment.add(new UserLink("name", user));
						valueContainer.add(fragment);
					} else {
						Label valueLabel = new Label("value", value);
						valueContainer.add(valueLabel);
						InputSpec fieldSpec = getProject().getIssueWorkflow().getField(field.getName());
						ChoiceProvider choiceProvider = null;
						if (fieldSpec != null && fieldSpec instanceof ChoiceInput) {
							choiceProvider = ((ChoiceInput)fieldSpec).getChoiceProvider();
						} else if (fieldSpec != null && fieldSpec instanceof MultiChoiceInput) {
							choiceProvider = ((MultiChoiceInput)fieldSpec).getChoiceProvider();
						} 
						if (choiceProvider != null) {
							OneContext.push(new ComponentContext(valueContainer));
							try {
								String color = choiceProvider.getChoices(false).get(value);
								String style = "padding: 0 4px; border-radius: 3px;";
								if (color != null)
									style += "background: " + color + "; color: white;";
								valueLabel.add(AttributeAppender.append("style", style));
							} finally {
								OneContext.pop();
							}
						}
					}
					valueContainer.add(new ModalLink("fixInvalidValue") {

						@Override
						protected Component newContent(String id, ModalPanel modal) {
							return new FieldValueFixPanel(id) {

								@Override
								Issue getIssue() {
									return valueContainer.getIssue();
								}

								@Override
								MultiValueIssueField getField() {
									return valueContainer.getField();
								}

								@Override
								String getInvalidValue() {
									return value;
								}

								@Override
								void onFixed(AjaxRequestTarget target) {
									setResponsePage(IssueDetailPage.class, IssueDetailPage.paramsOf(getIssue()));
								}

								@Override
								void onCancel(AjaxRequestTarget target) {
									modal.close();
								}

								@Override
								public Object getInputValue(String name) {
									return valueContainer.getInputValue(name);
								}
								
							};
						}

						@Override
						protected void onConfigure() {
							super.onConfigure();
							
							OneContext.push(new ComponentContext(this));
							try {
								MultiValueIssueField field = item.getModelObject();
								InputSpec fieldSpec = getProject().getIssueWorkflow().getField(field.getName());
								if (fieldSpec != null && EditableUtils.getDisplayName(fieldSpec.getClass()).equals(field.getType())) { 
									List<String> possibleValues;
									if (fieldSpec instanceof ChoiceInput) 
										possibleValues = new ArrayList<>(((ChoiceInput) fieldSpec).getChoiceProvider().getChoices(false).keySet());
									else if (fieldSpec instanceof MultiChoiceInput) 
										possibleValues = new ArrayList<>(((MultiChoiceInput) fieldSpec).getChoiceProvider().getChoices(false).keySet());
									else 
										possibleValues = null;
									if (possibleValues != null)
										setVisible(!possibleValues.contains(value));
									else 
										setVisible(false);
								} else {
									setVisible(false);
								}
							} finally {
								OneContext.pop();
							}
						}
						
					});
					valuesView.add(valueContainer);
				}
				item.add(valuesView);
			}
			
		});
		
	}

	private void newEmptyActionOptions(@Nullable AjaxRequestTarget target) {
		WebMarkupContainer actionOptions = new WebMarkupContainer(ACTION_OPTIONS_ID);
		actionOptions.setOutputMarkupPlaceholderTag(true);
		actionOptions.setVisible(false);
		if (target != null) {
			replace(actionOptions);
			target.add(actionOptions);
		} else {
			add(actionOptions);
		}
	}

	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}

	private IssueFieldManager getIssueFieldManager() {
		return OneDev.getInstance(IssueFieldManager.class);
	}
	
	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueDetailResourceReference()));
	}

	@Override
	protected void onDetach() {
		issueModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Issue issue) {
		PageParameters params = ProjectPage.paramsOf(issue.getProject());
		params.set(PARAM_ISSUE, issue.getId());
		return params;
	}

	@Override
	public List<String> getInputNames() {
		return getProject().getIssueWorkflow().getInputNames();
	}

	@Override
	public InputSpec getInput(String inputName) {
		return getProject().getIssueWorkflow().getInput(inputName);
	}
	
	private abstract static class FieldValueContainer extends WebMarkupContainer implements EditContext {

		public FieldValueContainer(String id) {
			super(id);
		}
		
		@Override
		public Object getInputValue(String name) {
			MultiValueIssueField field = getIssue().getMultiValueFields().get(name);
			InputSpec fieldSpec = getIssue().getProject().getIssueWorkflow().getField(name);
			if (field != null && fieldSpec != null && field.getType().equals(EditableUtils.getDisplayName(fieldSpec.getClass()))) {
				return fieldSpec.convertToObject(field.getValues());
			} else {
				return null;
			}
		}
		
		abstract Issue getIssue();
		
		abstract MultiValueIssueField getField();
		
	}
}
