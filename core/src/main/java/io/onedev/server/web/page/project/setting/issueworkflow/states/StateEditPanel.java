package io.onedev.server.web.page.project.setting.issueworkflow.states;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issueworkflow.IssueWorkflow;
import io.onedev.server.model.support.issueworkflow.StateSpec;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;

@SuppressWarnings("serial")
abstract class StateEditPanel extends Panel {

	private final int stateIndex;
	
	public StateEditPanel(String id, int stateIndex) {
		super(id);
	
		this.stateIndex = stateIndex;
	}
	
	private Project getProject() {
		ProjectPage page = (ProjectPage) getPage();
		return page.getProject();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		StateSpec state;
		if (stateIndex != -1)
			state = SerializationUtils.clone(getWorkflow().getStates().get(stateIndex));
		else
			state = new StateSpec();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(StateEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		BeanEditor editor = BeanContext.editBean("editor", state);
		form.add(editor);
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				if (stateIndex != -1) { 
					StateSpec oldState = getWorkflow().getStates().get(stateIndex);
					if (!state.getName().equals(oldState.getName()) && getWorkflow().getState(state.getName()) != null) {
						editor.getErrorContext(new PathSegment.Property("name"))
								.addError("This name has already been used by another state");
					}
				} else if (getWorkflow().getState(state.getName()) != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another state");
				}

				if (!editor.hasErrors(true)) {
					if (stateIndex != -1) {
						StateSpec oldState = getWorkflow().getStates().get(stateIndex);
						if (!state.getName().equals(oldState.getName())) 
							getWorkflow().onStateRename(oldState.getName(), state.getName());
						getWorkflow().getStates().set(stateIndex, state);
					} else {
						getWorkflow().getStates().add(state);
					}
					getProject().setIssueWorkflow(getWorkflow());
					OneDev.getInstance(ProjectManager.class).save(getProject());
					onSave(target);
				} else {
					target.add(form);
				}
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(StateEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.setOutputMarkupId(true);
		
		add(form);
	}

	protected abstract IssueWorkflow getWorkflow();
	
	protected abstract void onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);

}
