package io.onedev.server.plugin.imports.gitlab;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ImportProject extends ImportGroup {

	private static final long serialVersionUID = 1L;
	
	private String project;

	@Editable(order=200, name="GitLab Project", description="Select project to import from")
	@ChoiceProvider("getProjectChoices")
	@NotEmpty
	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}
	
	@Override
	public boolean isIncludeForks() {
		return super.isIncludeForks();
	}
	
	@SuppressWarnings("unused")
	private static List<String> getProjectChoices() {
		BeanEditor editor = ComponentContext.get().getComponent().findParent(BeanEditor.class);
		ImportProject project = (ImportProject) editor.getModelObject();
		String groupId = (String) EditContext.get().getInputValue("groupId");
		return project.server.listProjects(groupId, true);
	}
	
}
