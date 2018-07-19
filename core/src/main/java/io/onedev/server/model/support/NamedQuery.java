package io.onedev.server.model.support;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface NamedQuery extends Serializable {

	String getName();
	
	String getQuery();
	
}