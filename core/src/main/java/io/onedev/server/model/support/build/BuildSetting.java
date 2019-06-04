package io.onedev.server.model.support.build;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.BuildQuery;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class BuildSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private String buildsToPreserve = "all";
	
	@Editable(description="Specify builds to preserve. OneDev will run every night to remove builds not matching "
			+ "query specified here")
	@BuildQuery(noLoginSupport=true)
	@NotEmpty
	public String getBuildsToPreserve() {
		return buildsToPreserve;
	}

	public void setBuildsToPreserve(String buildsToPreserve) {
		this.buildsToPreserve = buildsToPreserve;
	}
	
}
