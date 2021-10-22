package io.onedev.server.web.page.project.setting.general;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.ParentChoice;

@Editable
public class ParentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String parentPath;

	@Editable(name="Parent Project", description="Settings and permissions of parent project will be inherited "
			+ "by this project")
	@ParentChoice
	@NameOfEmptyValue("No parent")
	public String getParentPath() {
		return parentPath;
	}

	public void setParentPath(@Nullable String parentPath) {
		this.parentPath = parentPath;
	}
	
	public void setParent(@Nullable Project parent) {
		if (parent != null)
			parentPath = parent.getPath();
		else
			parentPath = null;
	}
	
	@Nullable
	public Project getParent() {
		if (parentPath != null)
			return OneDev.getInstance(ProjectManager.class).find(parentPath);
		else
			return null;
	}
	
}
