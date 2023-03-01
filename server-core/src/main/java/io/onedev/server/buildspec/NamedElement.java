package io.onedev.server.buildspec;

import java.io.Serializable;

import io.onedev.server.annotation.Editable;

@Editable
public interface NamedElement extends Serializable {

	String getName();
	
}
