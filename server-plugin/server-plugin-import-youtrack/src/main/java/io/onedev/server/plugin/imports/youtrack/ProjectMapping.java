package io.onedev.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.validation.annotation.ProjectPath;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.util.WicketUtils;

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
		return WicketUtils.getPage().getMetaData(ImportServer.META_DATA_KEY).getProjectChoices();
	}
	
}