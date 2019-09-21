package io.onedev.server.model.support.administration.groovyscript;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.Usage;
import io.onedev.server.util.scriptidentity.ScriptIdentity;
import io.onedev.server.util.scriptidentity.SiteAdministrator;
import io.onedev.server.util.validation.annotation.DnsName;
import io.onedev.server.web.editable.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public class GroovyScript implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String description;
	
	private List<String> content;
	
	private JobAuthorization jobAuthorization = new JobAuthorization();
	
	@Editable(order=100)
	@DnsName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Editable(order=300)
	@Code(language = Code.GROOVY)
	@Size(min=1, message="May not be empty")
	public List<String> getContent() {
		return content;
	}

	public void setContent(List<String> content) {
		this.content = content;
	}
	
	@Editable(order=400, name="Can be Used by Jobs")
	@NameOfEmptyValue("No")
	public JobAuthorization getJobAuthorization() {
		return jobAuthorization;
	}

	public void setJobAuthorization(JobAuthorization jobAuthorization) {
		this.jobAuthorization = jobAuthorization;
	}

	public final boolean isAuthorized(ScriptIdentity identity) {
		return identity instanceof SiteAdministrator 
				|| jobAuthorization != null && jobAuthorization.isAuthorized(identity);
	}
	
	public Usage onDeleteProject(String projectName, int index) {
		Usage usage = new Usage();
		if (jobAuthorization != null)
			usage = jobAuthorization.onDeleteProject(projectName);
		return usage.prefix("groovy script #" + index);
	}
	
	public void onRenameProject(String oldName, String newName) {
		if (jobAuthorization != null)
			jobAuthorization.onRenameProject(oldName, newName);
	}
	
}
