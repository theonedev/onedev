package io.onedev.server.model.support.pack;

import io.onedev.server.annotation.Editable;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

@Editable
public class ProjectPackSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<NamedPackQuery> namedQueries;

	@Nullable
	public List<NamedPackQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(@Nullable List<NamedPackQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}
	
}
