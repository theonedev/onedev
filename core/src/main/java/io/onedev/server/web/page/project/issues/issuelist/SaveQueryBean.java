package io.onedev.server.web.page.project.issues.issuelist;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.OmitName;

@Editable
public class SaveQueryBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	@Editable(description="Specify a name to identify the preset query")
	@NotEmpty
	@OmitName
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}