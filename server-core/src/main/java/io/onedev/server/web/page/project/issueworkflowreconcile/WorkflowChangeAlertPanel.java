package io.onedev.server.web.page.project.issueworkflowreconcile;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class WorkflowChangeAlertPanel extends Panel {

	public WorkflowChangeAlertPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (SecurityUtils.isAdministrator()) {
			add(new ModalLink("reconcile") {

				@Override
				protected Component newContent(String id, ModalPanel modal) {
					return new WorkflowReconcilePanel(id) {
						
						@Override
						protected void onCancel(AjaxRequestTarget target) {
							modal.close();
						}

						@Override
						protected void onCompleted(AjaxRequestTarget target) {
							WorkflowChangeAlertPanel.this.onCompleted(target);
						}
						
					};
				}

				@Override
				public IModel<?> getBody() {
					return Model.of("reconcile");
				}
				
			});
		} else {
			add(new Label("reconcile", "contact site administrator to reconcile") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
			});
		}
				
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!OneDev.getInstance(SettingManager.class).getIssueSetting().isReconciled());
	}

	protected abstract void onCompleted(AjaxRequestTarget target);
	
}
