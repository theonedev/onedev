package io.onedev.server.issue.fieldsupply;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface ValueProvider extends Serializable {
	
	List<String> getValue();
	
}
