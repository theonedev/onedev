package io.onedev.server.buildspec.job.paramsupply;

import java.io.Serializable;
import java.util.List;

public interface ValuesProvider extends Serializable {
	
	List<List<String>> getValues();
	
}
