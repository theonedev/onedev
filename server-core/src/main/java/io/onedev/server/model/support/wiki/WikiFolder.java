package io.onedev.server.model.support.wiki;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import io.onedev.server.annotation.Editable;

@Editable
public interface WikiFolder extends Serializable {

	@Nullable
	String getPath();

}
