package io.onedev.server.web.component.issue.create;

import java.io.Serializable;
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
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.convert.ConversionException;
import org.unbescape.javascript.JavaScriptEscape;

import com.google.common.base.Objects;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputContext;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.issue.IssueTemplate;
import io.onedev.server.search.entity.issue.IssueCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.ReferenceInputBehavior;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.milestone.choice.MilestoneSingleChoice;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.project.comment.CommentInput;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.util.ProjectAttachmentSupport;

@SuppressWarnings("serial")
public abstract class NewIssueEditor extends FormComponentPanel<Issue> implements InputContext {

	private String uuid = UUID.randomUUID().toString();
	
	private TextField<String> titleInput;
	
	private CommentInput descriptionInput;
	
	private MilestoneSingleChoice milestoneChoice;
	
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
		Class<?> fieldBeanClass = IssueUtils.defineFieldBeanClass(getProject());
		Serializable fieldBean = issue.getFieldBean(fieldBeanClass, true);

		Collection<String> fieldNames = getIssueSetting().getPromptFieldsUponIssueOpen(); 
		issue.setFieldValues(IssueUtils.getFieldValues(new ComponentContext(this), fieldBean, fieldNames));
		
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

		lastDescriptionTemplate = getDescriptionTemplate(issue);
		add(descriptionInput = newDescriptionInput(lastDescriptionTemplate));
		
		milestoneChoice = new MilestoneSingleChoice("milestone", Model.of(issue.getMilestone()), 
				new LoadableDetachableModel<Collection<Milestone>>() {

			@Override
			protected Collection<Milestone> load() {
				return getProject().getSortedMilestones();
			}
			
		});
		milestoneChoice.setVisible(SecurityUtils.canScheduleIssues(getProject()));
		milestoneChoice.setRequired(false);
		
		add(milestoneChoice);
		
		Collection<String> properties = IssueUtils.getPropertyNames(getProject(), 
				fieldBeanClass, getIssueSetting().getPromptFieldsUponIssueOpen());
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
		descriptionInput.setOutputMarkupId(true);
		return descriptionInput;
	}
	
	@Nullable
	protected IssueCriteria getTemplate() {
		return null;
	}
	
	@Nullable
	private String getDescriptionTemplate(Issue issue) {
		for (IssueTemplate template: getIssueSetting().getIssueTemplates()) {
			IssueQuery criteria = IssueQuery.parse(getProject(), template.getIssueQuery(), 
					true, false, false, false, false);
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
		issue.setMilestone(milestoneChoice.getConvertedInput());
		
		fieldEditor.convertInput();
		Collection<String> fieldNames = getIssueSetting().getPromptFieldsUponIssueOpen(); 
		issue.setFieldValues(IssueUtils.getFieldValues(fieldEditor.newComponentContext(), 
				fieldEditor.getConvertedInput(), fieldNames));
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
