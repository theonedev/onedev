package io.onedev.server.model.support.pack;

import java.io.Serializable;
import java.util.List;

import org.jspecify.annotations.Nullable;
import jakarta.validation.Valid;

import io.onedev.server.annotation.Editable;

@Editable
public class ProjectPackSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<NamedPackQuery> namedQueries;

	@Nullable
	public List<@Valid NamedPackQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(@Nullable List<NamedPackQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}
	
}
