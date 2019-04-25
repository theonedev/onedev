package io.onedev.server.util.inputspec;

import java.util.List;

import javax.annotation.Nullable;

public interface InputContext {

	List<String> getInputNames();
	
	@Nullable
	InputSpec getInputSpec(String inputName);
	
	@Nullable
	String validateName(String inputName);
	
}
 