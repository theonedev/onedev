package io.onedev.server.web.page.project.setting.workspacespec;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.editable.BeanContext;

abstract class WorkspaceSpecPanel extends Panel {

	private final WorkspaceSpec spec;

	public WorkspaceSpecPanel(String id, WorkspaceSpec spec) {
		super(id);
		this.spec = spec;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("name", spec.getName()));

		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				WorkspaceSpecEditPanel editor = new WorkspaceSpecEditPanel("spec", spec) {

					@Override
					protected void onSave(AjaxRequestTarget target, WorkspaceSpec spec) {
						WorkspaceSpecPanel.this.onSave(target, spec);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						WorkspaceSpecPanel.this.onCancel(target);
					}

				};
				WorkspaceSpecPanel.this.replace(editor);
				target.add(editor);
			}

		});

		add(new AjaxLink<Void>("delete") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to delete this workspace spec?")));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}

		});

		add(BeanContext.view("spec", spec).setOutputMarkupId(true));

		setOutputMarkupId(true);
	}

	protected abstract void onDelete(AjaxRequestTarget target);

	protected abstract void onSave(AjaxRequestTarget target, WorkspaceSpec spec);

	protected abstract void onCancel(AjaxRequestTarget target);

}
