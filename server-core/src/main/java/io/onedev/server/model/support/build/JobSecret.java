package io.onedev.server.model.support.build;

import io.onedev.server.annotation.SecretName;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.JobMatch;
import io.onedev.server.annotation.Multiline;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Editable
public class JobSecret implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String value;
	
	private String authorization;
	
	@Editable(order=100)
	@NotEmpty
	@SecretName
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Editable(order=200)
	@NotEmpty
	@Multiline
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Editable(order=300, placeholder="All Branches", description=
			"Optionally specify branches/roles allowed to access this secret")
	@JobMatch(withProjectCriteria = true)
    public String getAuthorization() {
		return authorization;
	}

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}
	
}
