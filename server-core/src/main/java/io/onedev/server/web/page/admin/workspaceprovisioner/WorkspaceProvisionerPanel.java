package io.onedev.server.web.page.admin.workspaceprovisioner;

import static io.onedev.server.web.translation.Translation._T;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.editable.BeanContext;

abstract class WorkspaceProvisionerPanel extends Panel {

	private final List<WorkspaceProvisioner> provisioners;

	private final int provisionerIndex;

	public WorkspaceProvisionerPanel(String id, List<WorkspaceProvisioner> provisioners, int provisionerIndex) {
		super(id);
		this.provisioners = provisioners;
		this.provisionerIndex = provisionerIndex;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("name", getProvisioner().getName()));

		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				WorkspaceProvisionerEditPanel editor = new WorkspaceProvisionerEditPanel("provisioner", provisioners, provisionerIndex) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						WorkspaceProvisionerPanel.this.onSave(target);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						WorkspaceProvisionerPanel.this.onCancel(target);
					}

				};
				WorkspaceProvisionerPanel.this.replace(editor);
				target.add(editor);
			}

		});

		add(new AjaxLink<Void>("delete") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to delete this provisioner?")));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}

		});

		add(new WebMarkupContainer("disabled") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getProvisioner().isEnabled());
			}

		});

		add(new AjaxCheckBox("enable", Model.of(getProvisioner().isEnabled())) {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				getProvisioner().setEnabled(!getProvisioner().isEnabled());
				onSave(target);
				target.add(WorkspaceProvisionerPanel.this);
			}

		});

		var bean = new WorkspaceProvisionerBean();
		bean.setProvisioner(getProvisioner());
		add(BeanContext.view("provisioner", bean).setOutputMarkupId(true));

		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return !getProvisioner().isEnabled() ? "disabled" : "";
			}

		}));

		setOutputMarkupId(true);
	}

	private WorkspaceProvisioner getProvisioner() {
		return provisioners.get(provisionerIndex);
	}

	protected abstract void onDelete(AjaxRequestTarget target);

	protected abstract void onSave(AjaxRequestTarget target);

	protected abstract void onCancel(AjaxRequestTarget target);

}
