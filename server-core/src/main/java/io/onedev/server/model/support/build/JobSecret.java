package io.onedev.server.model.support.build;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.JobMatch;
import io.onedev.server.annotation.RegEx;
import io.onedev.server.annotation.Secret;
import io.onedev.server.job.match.OnBranchCriteria;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Editable
public class JobSecret implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String value;
	
	private String authorization = new OnBranchCriteria("**").toString();
	
	private boolean archived;
	
	@Editable(order=100)
	@RegEx(pattern="[^@]+", message="Character '@' not allowed in secret name")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Editable(order=200)
	@Secret
	@NotEmpty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Editable(order=300, placeholder="Any job", description= "" +
			"Optionally specify branches/users/groups allowed to access this secret. If left empty, " +
			"any job can access this secret, including those triggered via external pull requests")
	@JobMatch
    public String getAuthorization() {
		return authorization;
	}

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	@Editable(order=400, description = "Mark a secret archived if it is no longer used by current " +
			"build spec, but still need to exist to reproduce old builds. Archived secrets will " +
			"not be shown by default")
	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}
}
