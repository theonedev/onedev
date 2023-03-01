package io.onedev.server.plugin.imports.jiracloud;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;

@Editable
public class ImportProject implements Serializable {

	private static final long serialVersionUID = 1L;
	
	ImportServer server;
	
	private String project;

	@Editable(order=400, name="JIRA Project", description="Choose JIRA project to import issues from")
	@ChoiceProvider("getProjectChoices")
	@NotEmpty
	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getProjectChoices() {
		BeanEditor editor = ComponentContext.get().getComponent().findParent(BeanEditor.class);
		ImportProject setting = (ImportProject) editor.getModelObject();
		return setting.server.listProjects();
	}
	
}
