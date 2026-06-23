package io.onedev.server.plugin.imports.bitbucketcloud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;

@Editable
public class ImportWorkspace implements Serializable {

	private static final long serialVersionUID = 1L;
	
	ImportServer server;
	
	private String workspace;
	
	private boolean includeForks;

	@Editable(order=100, name="Bitbucket Workspace", description="Select workspace to import from")
	@ChoiceProvider(value="getWorkspaceChoices", displayNames="getWorkspaceDisplayNames")
	@NotEmpty
	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}
	
	private static Map<String, String> getWorkspaceDisplayNames() {
		BeanEditor editor = ComponentContext.get().getComponent().findParent(BeanEditor.class);
		ImportWorkspace setting = (ImportWorkspace) editor.getModelObject();
		return setting.server.listWorkspaces();
	}
	
	@SuppressWarnings("unused")
	private static List<String> getWorkspaceChoices() {
		return new ArrayList<>(getWorkspaceDisplayNames().keySet());
	}
	
	@Editable(order=200, description="Whether or not to include forked repositories")
	public boolean isIncludeForks() {
		return includeForks;
	}

	public void setIncludeForks(boolean includeForks) {
		this.includeForks = includeForks;
	}
	
}
