package io.onedev.server.web.component.workspace.invalidspec;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Workspace;

public class InvalidWorkspaceSpecIcon extends GenericPanel<Workspace> {

	private static final long serialVersionUID = 1L;

	public InvalidWorkspaceSpecIcon(String id, IModel<Workspace> workspaceModel) {
		super(id, workspaceModel);
	}

	private Workspace getWorkspace() {
		return getModelObject();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(getWorkspace().getSpec() == null);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.put("data-tippy-content",
				_T("Spec not found in workspace project hierarchy"));
	}

}
