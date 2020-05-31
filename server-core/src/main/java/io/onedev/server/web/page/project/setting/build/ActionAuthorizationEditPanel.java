package io.onedev.server.web.page.project.setting.build;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.build.actionauthorization.ActionAuthorization;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

@SuppressWarnings("serial")
public abstract class ActionAuthorizationEditPanel extends Panel {

	private final int index;
	
	public ActionAuthorizationEditPanel(String id, int index) {
		super(id);
		this.index = index;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		
		ActionAuthorizationBean bean = new ActionAuthorizationBean();
		if (index != -1)
			bean.setAuthorization(getProject().getBuildSetting().getActionAuthorizations().get(index));
		
		BeanEditor editor = BeanContext.edit("editor", bean);
		form.add(editor);
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancelled(target);
			}
			
		});
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				List<ActionAuthorization> authorizations = getProject().getBuildSetting().getActionAuthorizations();
				if (index == -1)
					authorizations.add(bean.getAuthorization());
				else
					authorizations.set(index, bean.getAuthorization());
				
				OneDev.getInstance(ProjectManager.class).save(getProject());
				onSaved(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancelled(target);
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}
			
		});
		add(form);
	}

	protected abstract Project getProject();
	
	protected abstract void onCancelled(AjaxRequestTarget target);

	protected abstract void onSaved(AjaxRequestTarget target);
}
