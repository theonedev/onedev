package io.onedev.server.util.input;

import java.util.List;

public interface InputContext {

	List<String> getInputNames();
	
	Input getInput(String inputName);
	
}
