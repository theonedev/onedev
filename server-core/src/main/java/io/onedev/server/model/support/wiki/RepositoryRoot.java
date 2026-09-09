package io.onedev.server.model.support.wiki;

import org.jspecify.annotations.Nullable;

import io.onedev.server.annotation.Editable;

@Editable(name="Repository Root")
public class RepositoryRoot implements WikiFolder {

	private static final long serialVersionUID = 1L;

	@Override
	@Nullable
	public String getPath() {
		return null;
	}

}
