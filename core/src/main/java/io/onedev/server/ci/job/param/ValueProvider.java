package io.onedev.server.ci.job.param;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface ValueProvider extends Serializable {
	
	List<String> getValues();
	
}
