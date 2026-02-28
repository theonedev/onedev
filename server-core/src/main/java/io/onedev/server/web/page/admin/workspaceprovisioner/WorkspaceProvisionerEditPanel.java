package io.onedev.server.web.page.admin.workspaceprovisioner;

import static io.onedev.server.web.translation.Translation._T;

import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.jspecify.annotations.Nullable;

import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

abstract class WorkspaceProvisionerEditPanel extends Panel {

	private final List<WorkspaceProvisioner> provisioners;

	private final int provisonerIndex;

	public WorkspaceProvisionerEditPanel(String id, List<WorkspaceProvisioner> provisioners, int provisonerIndex) {
		super(id);
		this.provisioners = provisioners;
		this.provisonerIndex = provisonerIndex;
	}

	@Nullable
	private WorkspaceProvisioner getProvisioner(String name) {
		for (WorkspaceProvisioner provisioner : provisioners) {
			if (provisioner.getName().equals(name))
				return provisioner;
		}
		return null;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WorkspaceProvisionerBean bean = new WorkspaceProvisionerBean();
		if (provisonerIndex != -1)
			bean.setProvisioner(SerializationUtils.clone(provisioners.get(provisonerIndex)));

		BeanEditor editor = BeanContext.edit("editor", bean);

		AjaxButton saveButton = new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				WorkspaceProvisioner provisioner = bean.getProvisioner();
				if (provisonerIndex != -1) {
					WorkspaceProvisioner oldProvisioner = provisioners.get(provisonerIndex);
					if (!provisioner.getName().equals(oldProvisioner.getName()) && getProvisioner(provisioner.getName()) != null) {
						editor.error(new Path(new PathNode.Named("provisioner"), new PathNode.Named("name")),
								_T("This name has already been used by another workspace provisioner"));
					}
				} else if (getProvisioner(provisioner.getName()) != null) {
					editor.error(new Path(new PathNode.Named("provisioner"), new PathNode.Named("name")),
							_T("This name has already been used by another workspace provisioner"));
				}

				if (editor.isValid()) {
					if (provisonerIndex != -1)
						provisioners.set(provisonerIndex, provisioner);
					else
						provisioners.add(provisioner);
					onSave(target);
				} else {
					target.add(form);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

		};

		AjaxLink<Void> cancelButton = new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}

		};

		Form<?> form = new Form<Void>("form");
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(editor);
		form.add(saveButton);
		form.add(cancelButton);

		add(form);
		setOutputMarkupId(true);
	}

	protected abstract void onSave(AjaxRequestTarget target);

	protected abstract void onCancel(AjaxRequestTarget target);

}
