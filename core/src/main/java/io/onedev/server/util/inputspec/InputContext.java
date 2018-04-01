package io.onedev.server.util.inputspec;

import java.util.List;

public interface InputContext {

	List<String> getInputNames();
	
	InputSpec getInput(String inputName);
	
}
