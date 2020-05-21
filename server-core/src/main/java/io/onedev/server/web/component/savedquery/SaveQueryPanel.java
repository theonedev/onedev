package io.onedev.server.web.component.savedquery;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public abstract class SaveQueryPanel extends Panel {

	public SaveQueryPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		SaveQueryBean bean = new SaveQueryBean();
		BeanEditor editor = BeanContext.edit("editor", bean); 
		form.add(editor);
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				onSaveForMine(target, bean.getName());
			}
			
		});
		form.add(new AjaxButton("saveForAll") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				onSaveForAll(target, bean.getName());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				if (getPage() instanceof ProjectPage) 
					setVisible(SecurityUtils.canManage(((ProjectPage)getPage()).getProject()));
				else
					setVisible(SecurityUtils.isAdministrator());
			}
			
		});
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.setOutputMarkupId(true);
		add(form);
	}

	protected abstract void onSaveForMine(AjaxRequestTarget target, String name);
	
	protected abstract void onSaveForAll(AjaxRequestTarget target, String name);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
