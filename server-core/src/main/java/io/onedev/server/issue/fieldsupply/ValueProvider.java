package io.onedev.server.issue.fieldsupply;

import java.io.Serializable;
import java.util.List;

public interface ValueProvider extends Serializable {
	
	List<String> getValue();
	
}
