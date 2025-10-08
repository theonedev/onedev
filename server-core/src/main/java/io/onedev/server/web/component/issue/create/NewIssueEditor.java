package io.onedev.server.web.component.issue.create;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

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
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.unbescape.javascript.JavaScriptEscape;

import com.google.common.base.Objects;

import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.buildspecmodel.inputspec.InputContext;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.IssueTemplate;
import io.onedev.server.model.support.issue.field.FieldUtils;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.search.entitytext.IssueTextService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.behavior.ReferenceInputBehavior;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.component.issue.title.IssueTitlePanel;
import io.onedev.server.web.component.iteration.choice.IterationMultiChoice;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.WicketUtils;

public abstract class NewIssueEditor extends FormComponentPanel<Issue> implements InputContext {

	private static final int MAX_SIMILAR_ISSUES = 5;
	private String uuid = UUID.randomUUID().toString();
	
	private TextField<String> titleInput;
	
	private CommentInput descriptionInput;
	
	private CheckBox confidentialInput;
	
	private IterationMultiChoice iterationChoice;
	
	private BeanEditor fieldEditor;
	
	private BeanEditor estimatedTimeEditor;
	
	private String lastDescriptionTemplate;
	
	private String editingTitle;
	
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
		
		var fieldNames = getIssueSetting().getPromptFieldsUponIssueOpen(getProject());
		issue.setFieldValues(FieldUtils.getFieldValues(new ComponentContext(this), fieldBean, 
				FieldUtils.getEditableFields(getProject(), fieldNames)));
		
		titleInput = new TextField<>("title", Model.of("")); 
		titleInput.setRequired(true).setLabel(Model.of(_T("Title")));
		add(titleInput);
		add(new FencedFeedbackPanel("titleFeedback", titleInput));
		
		titleInput.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return !titleInput.isValid()?" is-invalid":"";
			}
			
		}));
		titleInput.add(new ReferenceInputBehavior() {

			@Override
			protected Project getProject() {
				return NewIssueEditor.this.getProject();
			}
			
		});

		IModel<List<Issue>> similarIssuesModel = new LoadableDetachableModel<List<Issue>>() {

			@Override
			protected List<Issue> load() {
				if (StringUtils.isNotBlank(editingTitle)) {
					IssueTextService issueTextService = OneDev.getInstance(IssueTextService.class);
					var projectScope = new ProjectScope(getProject(), true, true);
					var issueIds = issueTextService.query(projectScope, editingTitle, MAX_SIMILAR_ISSUES);
					var issues = OneDev.getInstance(IssueService.class).loadIssues(issueIds);
					projectScope.filter(issues);
					return issues;
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
				editingTitle = titleInput.getInput();
				target.add(similarIssuesContainer);
			}

			@Override
			protected void onError(AjaxRequestTarget target, RuntimeException e) {
				super.onError(target, e);
				editingTitle = null;
				target.add(similarIssuesContainer);
			}
			
		});
		
		similarIssuesContainer.setOutputMarkupPlaceholderTag(true);
		add(similarIssuesContainer);
		similarIssuesContainer.add(new ListView<Issue>("similarIssues", similarIssuesModel) {

			@Override
			protected void populateItem(ListItem<Issue> item) {
				item.add(new IssueStateBadge("state", item.getModel(), false));
				item.add(new IssueTitlePanel("numberAndTitle") {

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
		
		Collection<Iteration> iterations = issue.getIterations();
		iterationChoice = new IterationMultiChoice("iterations", Model.of(iterations), 
				new LoadableDetachableModel<Collection<Iteration>>() {

			@Override
			protected Collection<Iteration> load() {
				return getProject().getSortedHierarchyIterations();
			}
			
		});
		iterationChoice.setVisible(SecurityUtils.canScheduleIssues(getProject()));
		iterationChoice.setRequired(false);
		
		add(iterationChoice);
		
		Collection<String> properties = FieldUtils.getEditablePropertyNames(getProject(), 
				fieldBeanClass, fieldNames);
		add(fieldEditor = new BeanContext(fieldBean.getClass(), properties, false)
				.renderForEdit("fields", Model.of(fieldBean)));
		
		var estimatedTimeEditBean = new EstimatedTimeEditBean();
		add(estimatedTimeEditor = new BeanContext(EstimatedTimeEditBean.class)
				.renderForEdit("estimatedTime", Model.of(estimatedTimeEditBean)));
		estimatedTimeEditor.setVisible(WicketUtils.isSubscriptionActive() && getProject().isTimeTracking());
		
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

			@Override
			protected String getAutosaveKey() {
				return "project:" + getProject().getId() + ":new-issue";
			}
		};
		descriptionInput.add((IValidator<String>) validatable -> {
			if (validatable.getValue().length() > Issue.MAX_DESCRIPTION_LEN) {
				validatable.error((IValidationError) messageSource -> "Description too long");
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
				return template.getIssueDescription().replace("\r\n", "\n");
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
		return OneDev.getInstance(SettingService.class).getIssueSetting();
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
		issue.setSubmitter(SecurityUtils.getAuthUser());
		
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
		
		var fieldNames = getIssueSetting().getPromptFieldsUponIssueOpen(getProject());
		issue.setFieldValues(FieldUtils.getFieldValues(fieldEditor.newComponentContext(), 
				fieldEditor.getConvertedInput(), fieldNames));
		
		iterationChoice.convertInput();
		issue.getSchedules().clear();
		for (Iteration iteration: iterationChoice.getConvertedInput()) {
			IssueSchedule schedule = new IssueSchedule();
			schedule.setIssue(issue);
			schedule.setIteration(iteration);
			issue.getSchedules().add(schedule);
		}
		
		estimatedTimeEditor.convertInput();
		EstimatedTimeEditBean estimatedTimeEditBean = (EstimatedTimeEditBean) estimatedTimeEditor.getConvertedInput();
		if (estimatedTimeEditBean.getEstimatedTime() != null) 
			issue.setOwnEstimatedTime(estimatedTimeEditBean.getEstimatedTime());
		
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
