package io.onedev.server.plugin.imports.bitbucketcloud;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.validation.annotation.ProjectName;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ProjectMapping implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_ONEDEV_PROJECT = "oneDevProject";
	
	private String bitbucketRepo;
	
	private String oneDevProject;
	
	@Editable(order=100, name="Bitbucket Repository", description="Specify Bitbucket repository in form of "
			+ "<tt>workspace/repository</tt>")
	@NotEmpty
	public String getBitbucketRepo() {
		return bitbucketRepo;
	}

	public void setBitbucketRepo(String bitbucketRepo) {
		this.bitbucketRepo = bitbucketRepo;
	}

	@Editable(order=200, name="OneDev Project", description="Specify OneDev project to be created as "
			+ "result of importing")
	@ProjectName
	@NotEmpty
	public String getOneDevProject() {
		return oneDevProject;
	}

	public void setOneDevProject(String oneDevProject) {
		this.oneDevProject = oneDevProject;
	}

}