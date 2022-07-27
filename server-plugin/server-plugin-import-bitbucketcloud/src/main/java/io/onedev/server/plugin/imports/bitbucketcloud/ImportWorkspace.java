package io.onedev.server.plugin.imports.bitbucketcloud;

import java.io.Serializable;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ImportWorkspace implements Serializable {

	private static final long serialVersionUID = 1L;
	
	ImportServer server;
	
	private String workspace;

	@Editable(order=100, name="Bitbucket Workspace", description="Select workspace to import from")
	@ChoiceProvider("getWorkspaceChoices")
	@NotEmpty
	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}
	
	@SuppressWarnings("unused")
	private static Map<String, String> getWorkspaceChoices() {
		BeanEditor editor = ComponentContext.get().getComponent().findParent(BeanEditor.class);
		ImportWorkspace setting = (ImportWorkspace) editor.getModelObject();
		return setting.server.listWorkspaces();
	}
	
}
