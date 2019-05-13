package io.onedev.server.ci.job.param;

import java.io.Serializable;
import java.util.List;

public interface ValuesProvider extends Serializable {
	
	List<String> getValues();
	
}
