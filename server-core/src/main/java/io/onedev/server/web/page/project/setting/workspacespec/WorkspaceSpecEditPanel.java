package io.onedev.server.web.page.project.setting.workspacespec;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.jspecify.annotations.Nullable;

import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.web.editable.BeanContext;

abstract class WorkspaceSpecEditPanel extends Panel {

	private final WorkspaceSpec spec;

	public WorkspaceSpecEditPanel(String id, WorkspaceSpec spec) {
		super(id);
		this.spec = spec;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);

		Form<?> form = new Form<Void>("form");
		form.add(new FencedFeedbackPanel("feedback", form));

		form.add(BeanContext.edit("editor", spec));

		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				var errorMessage = onSave(target, spec);
				if (errorMessage != null) {
					form.error(errorMessage);
					target.add(form);
				}
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
				onCancel(target);
			}

		});
		add(form);
	}

	@Nullable
	protected abstract String onSave(AjaxRequestTarget target, WorkspaceSpec spec);

	protected abstract void onCancel(AjaxRequestTarget target);
}
