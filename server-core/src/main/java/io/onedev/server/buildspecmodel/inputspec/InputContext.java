package io.onedev.server.buildspecmodel.inputspec;

import java.util.List;

import org.jspecify.annotations.Nullable;

import io.onedev.server.util.HierarchicalContext;

public interface InputContext {

	List<String> getInputNames();
	
	@Nullable
	InputSpec getInputSpec(String inputName);

	@Nullable
	public static InputContext get() {
		var hierarchicalContext = HierarchicalContext.get();
		if (hierarchicalContext != null)
			return hierarchicalContext.findData(InputContext.class);
		else
			return null;
	}
	
}
 