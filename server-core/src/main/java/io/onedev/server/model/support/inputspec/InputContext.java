package io.onedev.server.model.support.inputspec;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.util.WicketUtils;

public interface InputContext {

	List<String> getInputNames();
	
	@Nullable
	InputSpec getInputSpec(String inputName);

	@Nullable
	public static InputContext get() {
		ComponentContext componentContext = ComponentContext.get();
		if (componentContext != null)
			return WicketUtils.findInnermost(componentContext.getComponent(), InputContext.class);
		else
			return null;
	}
	
}
 