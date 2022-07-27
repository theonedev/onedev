package io.onedev.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.validation.annotation.ProjectPath;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ProjectMapping implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_ONEDEV_PROJECT = "oneDevProject";
	
	private String youTrackProject;
	
	private String oneDevProject;
	
	@Editable(order=100, name="YouTrack Project", description="Issues will be imported from specified "
			+ "YouTrack project")
	@ChoiceProvider("getProjectChoices")
	@NotEmpty
	public String getYouTrackProject() {
		return youTrackProject;
	}

	public void setYouTrackProject(String youTrackProject) {
		this.youTrackProject = youTrackProject;
	}

	@Editable(order=200, name="OneDev Project", description="Specify OneDev project to be created as "
			+ "result of importing")
	@ProjectPath
	@NotEmpty
	public String getOneDevProject() {
		return oneDevProject;
	}

	public void setOneDevProject(String oneDevProject) {
		this.oneDevProject = oneDevProject;
	}

	@SuppressWarnings("unused")
	private static List<String> getProjectChoices() {
		BeanEditor editor = ComponentContext.get().getComponent().findParent(BeanEditor.class);
		ImportProjects setting = (ImportProjects) editor.getModelObject();
		return setting.server.listProjects();
	}
	
}