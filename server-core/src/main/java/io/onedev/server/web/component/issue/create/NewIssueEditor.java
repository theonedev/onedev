package io.onedev.server.web.component.issue.create;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.unbescape.javascript.JavaScriptEscape;

import com.google.common.base.Objects;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputContext;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.issue.IssueTemplate;
import io.onedev.server.model.support.issue.field.FieldUtils;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.search.entitytext.IssueTextManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.behavior.ReferenceInputBehavior;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.component.issue.link.IssueLinkPanel;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.milestone.choice.MilestoneMultiChoice;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.project.comment.CommentInput;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.ProjectAttachmentSupport;

@SuppressWarnings("serial")
public abstract class NewIssueEditor extends FormComponentPanel<Issue> implements InputContext {

	private String uuid = UUID.randomUUID().toString();
	
	private TextField<String> titleInput;
	
	private CommentInput descriptionInput;
	
	private CheckBox confidentialInput;
	
	private MilestoneMultiChoice milestoneChoice;
	
	private BeanEditor fieldEditor;
	
	private String lastDescriptionTemplate;
	
	private AbstractPostAjaxBehavior ajaxBehavior;
	
	public NewIssueEditor(String id) {
		super(id, Model.of((Issue)null));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Issue issue = newIssue();
		Class<?> fieldBeanClass = FieldUtils.getFieldBeanClass();
		Serializable fieldBean = issue.getFieldBean(fieldBeanClass, true);

		Collection<String> fieldNames = getIssueSetting().getPromptFieldsUponIssueOpen(getProject());
		issue.setFieldValues(FieldUtils.getFieldValues(new ComponentContext(this), fieldBean, 
				FieldUtils.getEditableFields(getProject(), fieldNames)));
		
		titleInput = new TextField<String>("title", Model.of("")); 
		titleInput.setRequired(true).setLabel(Model.of("Title"));
		add(titleInput);
		add(new FencedFeedbackPanel("titleFeedback", titleInput));
		
		titleInput.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return !titleInput.isValid()?" is-invalid":"";
			}
			
		}));
		titleInput.add(new ReferenceInputBehavior(false) {

			@Override
			protected Project getProject() {
				return NewIssueEditor.this.getProject();
			}
			
		});

		IModel<List<Issue>> similarIssuesModel = new LoadableDetachableModel<List<Issue>>() {

			@Override
			protected List<Issue> load() {
				String title = titleInput.getInput();
				if (StringUtils.isNotBlank(title)) {
					IssueTextManager issueTextManager = OneDev.getInstance(IssueTextManager.class);
					return issueTextManager.query(
							new ProjectScope(getProject(), true, true), 
							title, false, 0, 5);
				} else {
					return new ArrayList<>();
				}
			}
			
		};
		WebMarkupContainer similarIssuesContainer = new WebMarkupContainer("similarIssues") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!similarIssuesModel.getObject().isEmpty());
			}
			
		};
		titleInput.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				target.add(similarIssuesContainer);
			}

			@Override
			protected void onError(AjaxRequestTarget target, RuntimeException e) {
				super.onError(target, e);
				target.add(similarIssuesContainer);
			}
			
		});
		
		similarIssuesContainer.setOutputMarkupPlaceholderTag(true);
		add(similarIssuesContainer);
		similarIssuesContainer.add(new ListView<Issue>("similarIssues", similarIssuesModel) {

			@Override
			protected void populateItem(ListItem<Issue> item) {
				item.add(new IssueStateBadge("state", item.getModel()));
				item.add(new IssueLinkPanel("numberAndTitle") {

					@Override
					protected Issue getIssue() {
						return item.getModelObject();
					}

					@Override
					protected Project getCurrentProject() {
						return getProject();
					}

					@Override
					protected Cursor getCursor() {
						return null;
					}
					
				});
			}
			
		});

		lastDescriptionTemplate = getDescriptionTemplate(issue);
		add(descriptionInput = newDescriptionInput(lastDescriptionTemplate));
		descriptionInput.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return !descriptionInput.isValid()?" is-invalid":"";
			}
			
		}));
		
		add(new FencedFeedbackPanel("descriptionFeedback", descriptionInput));

		add(confidentialInput = new CheckBox("confidential", Model.of(false))); 
		
		Collection<Milestone> milestones = issue.getMilestones();
		milestoneChoice = new MilestoneMultiChoice("milestones", Model.of(milestones), 
				new LoadableDetachableModel<Collection<Milestone>>() {

			@Override
			protected Collection<Milestone> load() {
				return getProject().getSortedHierarchyMilestones();
			}
			
		});
		milestoneChoice.setVisible(SecurityUtils.canScheduleIssues(getProject()));
		milestoneChoice.setRequired(false);
		
		add(milestoneChoice);
		
		Collection<String> properties = FieldUtils.getEditablePropertyNames(getProject(), 
				fieldBeanClass, fieldNames);
		add(fieldEditor = new BeanContext(fieldBean.getClass(), properties, false)
				.renderForEdit("fields", Model.of(fieldBean)));
		
		add(ajaxBehavior = new AbstractPostAjaxBehavior() {

			private void switchTemplate(AjaxRequestTarget target, String template) {
				descriptionInput = newDescriptionInput(template);
				replace(descriptionInput);
				target.add(descriptionInput);
				lastDescriptionTemplate = template;
			}
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				
				String description;
				if (StringUtils.isNotBlank(params.getParameterValue("description").toOptionalString()))
					description = params.getParameterValue("description").toOptionalString().trim();
				else
					description = null;
				
				String template;
				if (StringUtils.isNotBlank(params.getParameterValue("template").toOptionalString()))
					template = params.getParameterValue("template").toOptionalString().trim();
				else
					template = null;

				if (description != null && !Objects.equal(description, lastDescriptionTemplate)) {
					if (template != null) {
						new ConfirmModalPanel(target) {
							
							@Override
							protected void onConfirm(AjaxRequestTarget target) {
								switchTemplate(target, template);
							}
							
							@Override
							protected String getConfirmMessage() {
								return "A different description template is available. Do you want to discard current description and switch?";
							}
							
							@Override
							protected String getConfirmInput() {
								return null;
							}
							
						};
					}
				} else {
					switchTemplate(target, template);
				}
			}
			
		});
	}
	
	protected abstract Project getProject();
	
	private CommentInput newDescriptionInput(String description) {
		CommentInput descriptionInput = new CommentInput("description", Model.of(description), false) {

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), uuid, 
						SecurityUtils.canManageIssues(getProject()));
			}

			@Override
			protected Project getProject() {
				return NewIssueEditor.this.getProject();
			}
			
		};
		descriptionInput.add(new IValidator<String>() {

			@Override
			public void validate(IValidatable<String> validatable) {
				if (validatable.getValue().length() > Issue.MAX_DESCRIPTION_LEN) {
					validatable.error(new IValidationError() {
						
						@Override
						public Serializable getErrorMessage(IErrorMessageSource messageSource) {
							return "Description too long";
						}
						
					});
				}
			}
			
		});
		descriptionInput.setOutputMarkupId(true);
		return descriptionInput;
	}
	
	@Nullable
	protected Criteria<Issue> getTemplate() {
		return null;
	}
	
	@Nullable
	private String getDescriptionTemplate(Issue issue) {
		IssueQueryParseOption option = new IssueQueryParseOption();
		for (IssueTemplate template: getIssueSetting().getIssueTemplates()) {
			IssueQuery criteria = IssueQuery.parse(getProject(), template.getIssueQuery(), option, true);
			if (criteria.matches(issue)) 
				return template.getIssueDescription();
		}
		return null;
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof BeanUpdating) {
			try {
				Issue issue = getEditingIssue();
				String descriptionTemplate = getDescriptionTemplate(issue);
				if (!Objects.equal(descriptionTemplate, lastDescriptionTemplate)) {
					BeanUpdating beanUpdating = (BeanUpdating)event.getPayload();
					CallbackParameter description = CallbackParameter.explicit("description");
					CallbackParameter template = CallbackParameter.explicit("template");
					String script = String.format("var callback=%s;callback($('.new-issue>.description textarea').val(), '%s');", 
							ajaxBehavior.getCallbackFunction(description, template), 
							JavaScriptEscape.escapeJavaScript(descriptionTemplate!=null?descriptionTemplate:""));
					beanUpdating.getHandler().appendJavaScript(script);
				}
			} catch (ConversionException e) {
			}
		}
		
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new NewIssueCssResourceReference()));
	}

	@Override
	public List<String> getInputNames() {
		throw new UnsupportedOperationException();
	}

	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	@Override
	public InputSpec getInputSpec(String inputName) {
		return getIssueSetting().getFieldSpec(inputName);
	}

	private Issue newIssue() {
		Issue issue = new Issue();
		issue.setUUID(uuid);
		issue.setProject(getProject());
		issue.setSubmitDate(new Date());
		issue.setState(getIssueSetting().getInitialStateSpec().getName());
		issue.setSubmitter(SecurityUtils.getUser());
		
		if (getTemplate() != null)
			getTemplate().fill(issue);
		return issue;
	}
	
	private Issue getEditingIssue() {
		Issue issue = newIssue();
		issue.setTitle(titleInput.getConvertedInput());
		issue.setDescription(descriptionInput.getConvertedInput());
		issue.setConfidential(confidentialInput.getConvertedInput());
		
		fieldEditor.convertInput();
		Collection<String> fieldNames = getIssueSetting().getPromptFieldsUponIssueOpen(getProject());
		issue.setFieldValues(FieldUtils.getFieldValues(fieldEditor.newComponentContext(), 
				fieldEditor.getConvertedInput(), fieldNames));
		
		milestoneChoice.convertInput();
		issue.getSchedules().clear();
		for (Milestone milestone: milestoneChoice.getConvertedInput()) {
			IssueSchedule schedule = new IssueSchedule();
			schedule.setIssue(issue);
			schedule.setMilestone(milestone);
			issue.getSchedules().add(schedule);
		}
		return issue;
	}
	
	@Override
	public void convertInput() {
		try {
			setConvertedInput(getEditingIssue());
		} catch (ConversionException e) {
			error(newValidationError(e));
		}
	}
	
}
