package io.onedev.server.ci.jobparam;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface ValueProvider extends Serializable {
	
	List<String> getValues();
	
}
