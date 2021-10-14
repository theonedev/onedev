package io.onedev.server.plugin.imports.gitea;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.validation.annotation.ProjectPath;
import io.onedev.server.util.validation.annotation.UrlPath;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ProjectMapping implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_ONEDEV_PROJECT = "oneDevProject";
	
	private String giteaRepo;
	
	private String oneDevProject;
	
	@Editable(order=100, name="Gitea Repository", description="Specify Gitea repository in form of "
			+ "<tt>organization/repository</tt>")
	@UrlPath
	@NotEmpty
	public String getGiteaRepo() {
		return giteaRepo;
	}

	public void setGiteaRepo(String giteaRepo) {
		this.giteaRepo = giteaRepo;
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

}