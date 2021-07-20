package io.onedev.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class IssueImportSource implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String project;

	@Editable(order=100, name="YouTrack Project", description="Choose YouTrack project to "
			+ "import issues from. Available projects will be populated when above options "
			+ "are specified")
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
		return WicketUtils.getPage().getMetaData(ImportServer.META_DATA_KEY).getProjectChoices();
	}
	
}
