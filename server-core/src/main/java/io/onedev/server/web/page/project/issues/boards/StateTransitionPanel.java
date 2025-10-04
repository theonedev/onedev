package io.onedev.server.web.page.project.issues.boards;

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
import io.onedev.server.buildspecmodel.inputspec.InputContext;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.field.FieldUtils;
import io.onedev.server.model.support.issue.transitionspec.ManualSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.IssueChangeService;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

abstract class StateTransitionPanel extends Panel implements InputContext {

	public StateTransitionPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Class<?> fieldBeanClass = FieldUtils.getFieldBeanClass();
		Serializable fieldBean = getIssue().getFieldBean(fieldBeanClass, true);

		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		add(form);

		ManualSpec transition = getTransition();
		var toState = getToState();

		form.add(new Label("state", toState));
		
		Collection<String> propertyNames = FieldUtils.getEditablePropertyNames(
				getIssue().getProject(), fieldBeanClass, transition.getPromptFields());
		BeanEditor editor = BeanContext.edit("editor", fieldBean, propertyNames, false); 
		form.add(editor);
		
		form.add(new AjaxButton("ok") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				Collection<String> editableFields = FieldUtils.getEditableFields(
						getIssue().getProject(), transition.getPromptFields());
				Map<String, Object> fieldValues = FieldUtils.getFieldValues(
						editor.newComponentContext(), fieldBean, editableFields);
				OneDev.getInstance(IssueChangeService.class).changeState(SecurityUtils.getUser(), getIssue(),
						getToState(), fieldValues, transition.getPromptFields(), transition.getRemoveFields(), null);
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
		throw new UnsupportedOperationException();
	}

	@Override
	public InputSpec getInputSpec(String inputName) {
		return OneDev.getInstance(SettingService.class).getIssueSetting().getFieldSpec(inputName);
	}
	
	protected abstract Issue getIssue();
	
	protected abstract void onSaved(AjaxRequestTarget target);
	
	protected abstract void onCancelled(AjaxRequestTarget target);
	
	protected abstract ManualSpec getTransition();
	
	protected abstract String getToState();
	
}
