package io.onedev.server.web.page.project.issues.issueboards;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.IssueChangeManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.util.IssueFieldBeanUtils;

@SuppressWarnings("serial")
abstract class StateTransitionPanel extends Panel implements InputContext {

	public StateTransitionPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Class<?> fieldBeanClass = IssueFieldBeanUtils.defineBeanClass(getIssue().getProject(), true);
		Serializable fieldBean = getIssue().getFieldBean(fieldBeanClass);
		Collection<String> excludedFields = getIssue().getExcludedFields(fieldBeanClass, getTargetState());

		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		add(form);
		
		form.add(new Label("state", getTargetState()));
		
		BeanEditor editor = BeanContext.editBean("editor", fieldBean, excludedFields); 
		form.add(editor);
		
		form.add(new AjaxButton("ok") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				StateSpec toStateSpec = getIssue().getProject().getIssueWorkflow().getStateSpec(getTargetState());
				if (toStateSpec == null)
					throw new OneException("Unable to find state spec: " + getTargetState());
				Map<String, Object> fieldValues = IssueFieldBeanUtils.getFieldValues(fieldBean, toStateSpec.getFields());
				OneDev.getInstance(IssueChangeManager.class).changeState(getIssue(), getTargetState(), fieldValues, null);
				onSaved(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

		});
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancelled(target);
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancelled(target);
			}
			
		});
		
	}

	@Override
	public List<String> getInputNames() {
		return getIssue().getProject().getIssueWorkflow().getFieldNames();
	}

	@Override
	public InputSpec getInputSpec(String inputName) {
		return getIssue().getProject().getIssueWorkflow().getFieldSpec(inputName);
	}
	
	@Override
	public boolean isReservedName(String inputName) {
		throw new UnsupportedOperationException();
	}
	
	protected abstract Issue getIssue();
	
	protected abstract void onSaved(AjaxRequestTarget target);
	
	protected abstract void onCancelled(AjaxRequestTarget target);
	
	protected abstract String getTargetState();
}
