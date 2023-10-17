package io.onedev.server.plugin.imports.youtrack;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.CreateChildren;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.editable.BeanEditor;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Editable
@ClassValidating
public class ImportProjects implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	ImportServer server;

	private String parentOneDevProject;

	private boolean all;

	private List<String> youTrackProjects;
	
	private boolean populateTagMappings = true;

	@Editable(order=200, name="Parent OneDev Project", description = "Optionally specify a OneDev project " +
			"to be used as parent of imported projects. Leave empty to import as root projects")
	@ChoiceProvider("getParentOneDevProjectChoices")
	public String getParentOneDevProject() {
		return parentOneDevProject;
	}

	public void setParentOneDevProject(String parentOneDevProject) {
		this.parentOneDevProject = parentOneDevProject;
	}

	private static List<String> getParentOneDevProjectChoices() {
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		return projectManager.getPermittedProjects(new CreateChildren()).stream()
				.map(it->it.getPath()).sorted().collect(Collectors.toList());
	}

	@Editable(order=300, name="Import All Projects")
	public boolean isAll() {
		return all;
	}

	public void setAll(boolean all) {
		this.all = all;
	}

	private static boolean isAllDisabled() {
		return !(Boolean) EditContext.get().getInputValue("all");
	}

	@Editable(order=500, name="YouTrack Projects to Import")
	@ChoiceProvider("getYouTrackProjectChoices")
	@ShowCondition("isAllDisabled")
	@Size(min=1, message="At least one project should be selected")
	public List<String> getYouTrackProjects() {
		return youTrackProjects;
	}

	public void setYouTrackProjects(List<String> youTrackProjects) {
		this.youTrackProjects = youTrackProjects;
	}

	private static List<String> getYouTrackProjectChoices() {
		BeanEditor editor = ComponentContext.get().getComponent().findParent(BeanEditor.class);
		ImportProjects projects = (ImportProjects) editor.getModelObject();
		return projects.server.listProjects();
	}

	@Editable(order=600, description = "Whether or not to pre-populate tag mappings in next page. " +
			"You may want to disable this if there are too many tags to display")
	public boolean isPopulateTagMappings() {
		return populateTagMappings;
	}

	public void setPopulateTagMappings(boolean populateTagMappings) {
		this.populateTagMappings = populateTagMappings;
	}
	
	public Collection<String> getImportProjects() {
		if (isAll())
			return server.listProjects();
		else
			return getYouTrackProjects();
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (parentOneDevProject == null && !SecurityUtils.canCreateRootProjects()) {
			context.disableDefaultConstraintViolation();
			var errorMessage = "No permission to import as root projects, please specify parent project";
			context.buildConstraintViolationWithTemplate(errorMessage)
					.addPropertyNode("parentOneDevProject")
					.addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}

}
