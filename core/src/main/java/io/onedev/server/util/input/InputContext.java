package io.onedev.server.util.input;

import java.util.List;
import java.util.Map;

public interface InputContext {

	List<String> getScenarios();
	
	Map<String, Input> getInputs();
	
}
