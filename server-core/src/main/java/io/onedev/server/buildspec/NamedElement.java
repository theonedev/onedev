package io.onedev.server.buildspec;

import io.onedev.server.annotation.Editable;

import java.io.Serializable;

@Editable
public interface NamedElement extends Serializable {

	String getName();
	
}
