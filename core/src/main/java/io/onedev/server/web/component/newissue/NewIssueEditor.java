package io.onedev.server.web.component.newissue;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.collect.Sets;

import io.onedev.server.entityquery.issue.IssueCriteria;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.IssueConstants;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.projectcomment.CommentInput;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.util.IssueFieldBeanUtils;
import io.onedev.server.web.util.ProjectAttachmentSupport;

@SuppressWarnings("serial")
public abstract class NewIssueEditor extends FormComponentPanel<Issue> implements InputContext {

	private TextField<String> titleInput;
	
	private CommentInput descriptionInput;
	
	private StringSingleChoice milestoneChoice;
	
	private BeanEditor fieldEditor;
	
	public NewIssueEditor(String id) {
		super(id, Model.of(new Issue()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Issue issue = newIssue();
		
		Class<?> fieldBeanClass = IssueFieldBeanUtils.defineBeanClass(getProject());
		Serializable fieldBean = issue.getFieldBean(fieldBeanClass, true);
		titleInput = new TextField<String>("title", Model.of("")); 
		titleInput.setRequired(true).setLabel(Model.of("Title"));
		add(titleInput);
		add(new FencedFeedbackPanel("titleFeedback", titleInput));
		
		titleInput.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return !titleInput.isValid()?" has-error":"";
			}
			
		}));
		
		add(descriptionInput = new CommentInput("description", Model.of(""), false) {

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), issue.getUUID());
			}

			@Override
			protected Project getProject() {
				return NewIssueEditor.this.getProject();
			}
			
		});

		List<String> milestones = getProject().getMilestones().stream().map(it->it.getName()).collect(Collectors.toList());
		milestoneChoice = new StringSingleChoice("milestone", 
				Model.of(issue.getMilestoneName()), milestones) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().setPlaceholder("No milestone");
			}
			
		};
		milestoneChoice.setRequired(false);
		add(milestoneChoice);
		
		Collection<String> excludedProperties = IssueFieldBeanUtils.getPropertyNames(fieldBeanClass, Sets.newHashSet(IssueConstants.FIELD_STATE));
		add(fieldEditor = new BeanContext(fieldBean.getClass(), excludedProperties).renderForEdit("fields", Model.of(fieldBean)));
	}
	
	protected abstract Project getProject();
	
	@Nullable
	protected IssueCriteria getTemplate() {
		return null;
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

	@Override
	public InputSpec getInputSpec(String inputName) {
		return getProject().getIssueWorkflow().getFieldSpec(inputName);
	}

	@Override
	public boolean isReservedName(String inputName) {
		throw new UnsupportedOperationException();
	}
	
	private Issue newIssue() {
		Issue issue = new Issue();
		issue.setProject(getProject());
		issue.setSubmitDate(new Date());
		issue.setState(getProject().getIssueWorkflow().getInitialStateSpec().getName());
		issue.setSubmitter(SecurityUtils.getUser());
		if (getTemplate() != null)
			getTemplate().fill(issue);
		return issue;
	}
	
	@Override
	public void convertInput() {
		try {
			Issue issue = newIssue();
			issue.setTitle(titleInput.getConvertedInput());
			issue.setDescription(descriptionInput.getConvertedInput());
			issue.setMilestone(getProject().getMilestone(milestoneChoice.getConvertedInput()));
			
			issue.setFieldValues(IssueFieldBeanUtils.getFieldValues(fieldEditor.getConvertedInput()));
			setConvertedInput(issue);
		} catch (ConversionException e) {
			error(newValidationError(e));
		}
	}
	
}
