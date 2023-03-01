package io.onedev.server.model.support.issue.field.supply;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.annotation.Editable;

@Editable
public interface ValueProvider extends Serializable {
	
	List<String> getValue();
	
}
